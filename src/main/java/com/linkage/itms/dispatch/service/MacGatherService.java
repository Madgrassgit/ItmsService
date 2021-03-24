package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.MacGatherServiceChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 湖北电信下挂MAC采集接口
 * @author jiafh
 *
 */
public class MacGatherService implements IService{
	
	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(MacGatherService.class);
	
	private UserDeviceDAO userDevDao = new UserDeviceDAO();

	public String work(String inXml)
	{	
		MacGatherServiceChecker checker = new MacGatherServiceChecker(inXml);
		if (false == checker.check()) {
			logger.error("servicename[MacGatherService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[MacGatherService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						inXml });
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn(
					"servicename[MacGatherService]cmdId[{}]userinfo[{}]无此用户信息或用户未绑定该设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1003);
			checker.setResultDesc("无此用户信息或用户未绑定该设备");
			return checker.getReturnXml();
		}
		else
		{
			String deviceId = userInfoMap.get("device_id");
			if (StringUtil.IsEmpty(deviceId))
			{
				// 未绑定设备
				logger.warn("servicename[MacGatherService]cmdId[{}]userinfo[{}]用户未绑定设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1004);
				checker.setResultDesc("用户未绑定设备");
				return checker.getReturnXml();
			}
			checker.setDevSn(userInfoMap.get("device_serialnumber"));
			
			logger.warn("[{}]开始批量采集设备MAC地址",deviceId);
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			ACSCorba corba = new ACSCorba();
			
			// 判断当前设备是否在线或被操作
			int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
			if(1 != flag){
				logger.warn("servicename[MacGatherService]cmdId[{}]userinfo[{}]设备正忙或不在线",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1005);
				checker.setResultDesc("设备正忙或不在线");
				return checker.getReturnXml();
			}
				
			String hostPath = "InternetGatewayDevice.LANDevice.1.Hosts.Host.";
			List<String> hostList = corba.getIList(deviceId, hostPath);
			logger.warn("[{}]hostList: [{}]",deviceId,hostList);
			if(null == hostList || hostList.isEmpty()){
				logger.warn("servicename[MacGatherService]cmdId[{}]userinfo[{}]采集InternetGatewayDevice.LANDevice.1.Hosts.Host.节点失败",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1006);
				checker.setResultDesc("没有下挂设备");
				return checker.getReturnXml();
			}


			List<Map<String,String>> gatherMacMapList = new ArrayList<Map<String,String>>();
			List<Map<String,String>> gatherMaxBitRateMapList = new ArrayList<Map<String,String>>();
			for(int index = 0;index < hostList.size(); index++){
				
				String[] lanNodeArr = new String[2];
				lanNodeArr[0] = hostPath + (hostList.get(index)) + ".MACAddress";				
				lanNodeArr[1] = hostPath + (hostList.get(index)) + ".HostName";
				//增加采集协商速率
				ArrayList<ParameValueOBJ> lanMacList = corba.getValue(deviceId, lanNodeArr);

				Map<String,String> gatherMacMap = new HashMap<String, String>();
				gatherMacMap.put("port",StringUtil.getStringValue(index + 1));
				if(null != lanMacList && lanMacList.size()!=0)
				{
					for(int i = 0; i < lanMacList.size(); i++)
					{
						logger.warn("lanMac,name:{},value:{}",lanMacList.get(i).getName(),lanMacList.get(i).getValue());		
						if(lanMacList.get(i).getName().endsWith("MACAddress")){
							gatherMacMap.put("macAddress", lanMacList.get(i).getValue());
						}
						if(lanMacList.get(i).getName().endsWith("HostName")){
							gatherMacMap.put("hostName", lanMacList.get(i).getValue());
						}
					}

					String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
					List<String> iList = corba.getIList(deviceId, lanPath);
					if ((null == iList) || (iList.isEmpty()))
					{
						logger.warn("[{}]获取iList失败，返回", deviceId);
						checker.setResult(1007);
						checker.setResultDesc("获取协商速率节点失败");
						return checker.getReturnXml();
					}
					for (String i : iList) {
						String gatherPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."
								+ i + ".MaxBitRate";

						ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId,
								gatherPath);
						if ((null == objLlist) || (objLlist.isEmpty())) {
							if ((null == objLlist) || (objLlist.isEmpty())) {
								logger.warn("[{}]获取objLlist失败，返回", deviceId);
								checker.setResult(1007);
								checker.setResultDesc("获取协商速率节点失败");
								return checker.getReturnXml();
							}
						}
						logger.warn("[{}]获取objLlist成功，objLlist.size={}", deviceId,
								Integer.valueOf(objLlist.size()));
						HashMap gatherMaxBitRateMap = new HashMap();
						gatherMaxBitRateMap.put("port",i);
						for (ParameValueOBJ pvobj : objLlist)
						{
							String path = pvobj.getName();
							String value = pvobj.getValue();
							if(path.contains("MaxBitRate"))
							{
								gatherMaxBitRateMap.put("maxBitRate",value);
							}
						}
						gatherMaxBitRateMapList.add(gatherMaxBitRateMap);
					}
				}
				else
				{
					logger.warn("servicename[MacGatherService]cmdId[{}]userinfo[{}]采集{}、{},{}节点均为空",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),lanNodeArr[0],lanNodeArr[1],lanNodeArr[2]});
				}
				gatherMacMapList.add(gatherMacMap);
			}
			checker.setGatherMacMapList(gatherMacMapList);
			checker.setGatherMaxBitRateMapList(gatherMaxBitRateMapList);
		}
		String returnXml = checker.getReturnXml();
		logger.warn(
				"servicename[MacGatherService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
		return returnXml;
	}
}
