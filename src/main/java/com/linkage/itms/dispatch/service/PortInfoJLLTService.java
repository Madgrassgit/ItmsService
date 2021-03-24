package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.DevOnlineCAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.PortInfoJLLTChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 吉林联通获取家庭网关端口信息全查询接口业务处理
 */
public class PortInfoJLLTService implements IService {

    private static final Logger logger = LoggerFactory.getLogger(PortInfoJLLTService.class);
    private UserDeviceDAO userDevDao = new UserDeviceDAO();
    private ACSCorba corba = new ACSCorba();
    private String deviceId = null;

    @Override
    public String work(String inXml) {
        PortInfoJLLTChecker checker = new PortInfoJLLTChecker(inXml);
        //1、验证入参
        if (!checker.check()) {
            logger.error("[PortInfoJLLTService]cmdId[{}] userinfo[{}]验证未通过，返回：{}",
                    checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml());
            return checker.getReturnXml();
        }
        //验证通过走采集流程
        logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}]初始参数校验通过，入参为：{}",
                checker.getCmdId(), checker.getUserInfo(), inXml);

        //2、处理用户信息 校验用户信息是否存在
        Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo());
        if (null == userInfoMap || userInfoMap.isEmpty()) {
            logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}]无此用户",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            checker.setResult(1001);
            checker.setResultDesc("无此用户信息");
            return checker.getReturnXml();
        }

        //3、用户信息存在 获取设备id
        deviceId = StringUtil.getStringValue(userInfoMap, "device_id");
        if (StringUtil.IsEmpty(deviceId)) {
            logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}]未绑定设备",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            checker.setResult(1002);
            checker.setResultDesc("此用户未绑定设备");
            return checker.getReturnXml();
        }

        //4、设备id存在 开始采集节点
        logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}]开始采集[{}]",
                checker.getCmdId(), checker.getUserInfo(), deviceId);

        //4.1 检查设备是否在线 不在线则组装返回
		if (!checkDevOnline(checker)){
			return checker.getReturnXml();
		}

		//4.2 设备在线 组装需要采集的节点路径
        ArrayList<String> pathList = new ArrayList<String>();

        //wan端口状态路径
		String wanStatusPath = getWanStatusPath(checker, pathList);

		//Lan口状态路径
		List<String> iList = getLanStatusPath(checker, pathList);

		//cpu使用率节点 单独获取 现有设备很多没有该节点
		ArrayList<String> pathDevList = new ArrayList<String>();
        String cpuRatePath = "InternetGatewayDevice.DeviceInfo.CPURate";
		pathDevList.add(cpuRatePath);

        //内存占用率节点 单独获取 现有设备很多没有该节点
        String memRatePath = "InternetGatewayDevice.DeviceInfo.MemRate";
		pathDevList.add(memRatePath);

        //4.3 根据节点路径集合 批量采集节点值
		ArrayList<ParameValueOBJ> resultList = callGatherParams(checker, pathList);
		if (null == resultList || resultList.size() == 0) {
            logger.warn("[PortInfoJLLTService]device is online,but batch gather params fail，cmdId[{}] userinfo[{}] device_id[{}],resultList[{}]",
                    checker.getCmdId(), checker.getUserInfo(), deviceId, resultList);
            checker.setResult(1005);
            checker.setResultDesc("节点采集失败");
            return checker.getReturnXml();
        }
		//cpu使用率节点和内存占用率节点采集 这两个节点未采集到则返回空值
		ArrayList<ParameValueOBJ> resultLDevInfoList = callGatherParamsDev(checker, pathList);
		if (null == resultLDevInfoList || resultLDevInfoList.size() == 0) {
			logger.warn("[PortInfoJLLTService]device is online,but batch gather devInfoParams fail，cmdId[{}] userinfo[{}] device_id[{}],resultList[{}]",
					checker.getCmdId(), checker.getUserInfo(), deviceId, resultLDevInfoList);
			checker.setMemRate("");
			checker.setCpuRate("");
		}

		//5、处理采集结果
		analysisResult(checker, wanStatusPath, iList, cpuRatePath, memRatePath, resultList,resultLDevInfoList);
		checker.setResult(0);
		checker.setResultDesc("成功");
		return checker.getReturnXml();

    }

	private void analysisResult(PortInfoJLLTChecker checker, String wanStatusPath, List<String> iList, String cpuRatePath, String memRatePath, ArrayList<ParameValueOBJ> resultList,ArrayList<ParameValueOBJ> resultLDevInfoList) {
		Map<String, String> paramValueMap = new HashMap<String, String>();
		for (ParameValueOBJ valueOBJ : resultList) {
			if (!StringUtil.IsEmpty(wanStatusPath) && valueOBJ.getName().contains(wanStatusPath)) {
				//wan状态
				checker.setWanStatus(valueOBJ.getValue());
			} else {
				//为保证返回LAN口的name顺序和status顺序一致，这里各LAN口的采集情况整理成MAP
				paramValueMap.put(valueOBJ.getName(), valueOBJ.getValue());
			}
		}
		if(resultLDevInfoList != null && resultLDevInfoList.size() > 0){
			for(ParameValueOBJ devValueOBJ : resultLDevInfoList){
				if (devValueOBJ.getName().contains(cpuRatePath)) {
					//cpu占用率
					checker.setCpuRate(devValueOBJ.getValue());
				} else if (devValueOBJ.getName().contains(memRatePath)) {
					//内存使用率
					checker.setMemRate(devValueOBJ.getValue());
				}
			}
		}

		//处理返回 lanName 和 lanStatus
		logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}] get lanParamsValueMap end,deviceId[{}],valueMap[{}]",
				checker.getCmdId(), checker.getUserInfo(), deviceId, paramValueMap);
		getLanStatusResult(checker, iList, paramValueMap);
	}

	private void getLanStatusResult(PortInfoJLLTChecker checker, List<String> iList, Map<String, String> paramValueMap) {
		if (paramValueMap.size() == 0) {
			return;
		}
		StringBuilder lanNameStr = new StringBuilder();
		StringBuilder lanStatusStr = new StringBuilder();
		for (String i : iList) {
			lanNameStr.append("LAN").append(i).append(',');
			String statusPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig." + i + ".Status";
			if (paramValueMap.get(statusPath) != null) {
				lanStatusStr.append(paramValueMap.get(statusPath)).append(',');
			} else {
				lanStatusStr.append(' ').append(',');
			}
		}
		checker.setLanName(lanNameStr.toString().substring(0, lanNameStr.toString().length() - 1));
		checker.setLinkStatus(lanStatusStr.toString().substring(0, lanStatusStr.toString().length() - 1));
	}

	private ArrayList<ParameValueOBJ> callGatherParams(PortInfoJLLTChecker checker, ArrayList<String> pathList) {
		String[] gatherPathArray = new String[pathList.size()];
		pathList.toArray(gatherPathArray);
		ArrayList<ParameValueOBJ> resultList;
		logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}] batch gather params start with pathList:[{}]",
				checker.getCmdId(), checker.getUserInfo(), gatherPathArray);
		resultList = corba.getValue(deviceId, gatherPathArray);
		logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}] batch gather params end with deviceId[{}],resultList[{}]",
				checker.getCmdId(), checker.getUserInfo(), deviceId, resultList);
		return resultList;
	}

	private ArrayList<ParameValueOBJ> callGatherParamsDev(PortInfoJLLTChecker checker, ArrayList<String> pathList) {
		String[] gatherPathArray = new String[pathList.size()];
		pathList.toArray(gatherPathArray);
		ArrayList<ParameValueOBJ> resultList;
		logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}] batch gather devInfoParams start with pathList:[{}]",
				checker.getCmdId(), checker.getUserInfo(), gatherPathArray);
		resultList = corba.getValue(deviceId, gatherPathArray);
		logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}] batch gather devInfoParams end with deviceId[{}],resultList[{}]",
				checker.getCmdId(), checker.getUserInfo(), deviceId, resultList);
		return resultList;
	}

	private List<String> getLanStatusPath(PortInfoJLLTChecker checker, ArrayList<String> pathList) {
		//先获取LAN口下的所有i值
		String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
		List<String> iList = corba.getIList(deviceId, lanPath);
		logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}] gather LAN iList end，deviceId:{},iList:{}", checker.getCmdId(), checker.getUserInfo(), deviceId, iList);
		List<String> lanStatusList = new ArrayList<String>();
		if (null == iList || iList.isEmpty()) {
			logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}] gather LAN iList fail，deviceId:{}", checker.getCmdId(), checker.getUserInfo(), deviceId);
			checker.setLanName("");
			checker.setLinkStatus("");
		} else {
			for (String i : iList) {
				//获取LAN口状态
				lanStatusList.add("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig." + i + ".Status");
			}
		}
		if (lanStatusList.size() > 0) {
			pathList.addAll(lanStatusList);
		}
		return iList;
	}

	private String getWanStatusPath(PortInfoJLLTChecker checker, ArrayList<String> pathList) {
    	//获取accessType
		String accessType = getAccessType(checker);
		logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}]get accessType[{}]", checker.getCmdId(), checker.getUserInfo(), accessType);
		String wanStatusPath;
		if (!StringUtil.IsEmpty(accessType) && accessType.contains("GPON")) {
			wanStatusPath = "InternetGatewayDevice.WANDevice.1.X_CU_WANGPONInterfaceConfig.Status";
			pathList.add(wanStatusPath);
		} else if (!StringUtil.IsEmpty(accessType) && accessType.contains("EPON")) {
			wanStatusPath = "InternetGatewayDevice.WANDevice.1.X_CU_WANEPONInterfaceConfig.Status";
			pathList.add(wanStatusPath);
		} else {
			wanStatusPath = "";
			logger.error("[PortInfoJLLTService]cmdId[{}] userinfo[{}]get accessType[{}] is not GPON or EPON",
					checker.getCmdId(), checker.getUserInfo(), accessType);
			checker.setWanStatus("Down");
		}
		return wanStatusPath;
	}

	private String getAccessType(PortInfoJLLTChecker checker) {
		String accessType = UserDeviceDAO.getAccType(deviceId);
		if (!StringUtil.IsEmpty(accessType)) {
			return accessType;
		}
		String accessTypePatg = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
		ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, accessTypePatg);
		if (null == objLlist || objLlist.size() == 0) {
			logger.error("[PortInfoJLLTService]cmdId[{}] userinfo[{}]gather accessType fail!", checker.getCmdId(), checker.getUserInfo());
			accessType = "";
		} else {
			accessType = objLlist.get(0).getValue();
		}
		return accessType;
	}

	private boolean checkDevOnline(PortInfoJLLTChecker checker) {
		int onlineStatus = DevOnlineCAO.devOnlineTest(deviceId);
		logger.warn("[PortInfoJLLTService]cmdId[{}] userinfo[{}]get onlineStatus[{}]",
				checker.getCmdId(), checker.getUserInfo(), onlineStatus);
		if (onlineStatus == -3) {
			checker.setResult(1003);
			checker.setResultDesc("设备正在被操作，无法读取！");
			return false;
		}
		if (onlineStatus != 1) {
			checker.setResult(1004);
			checker.setResultDesc("设备不在线，无法读取！");
			checker.setDeviceStatus("1");
			return false;
		}
		checker.setDeviceStatus("0");
		return true;
	}
}

	