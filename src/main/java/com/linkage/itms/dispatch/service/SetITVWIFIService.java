package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.setITVWIFIChecker;
import com.linkage.itms.nx.dispatch.obj.ModifyWifiChannelChecker;
import com.linkage.itms.obj.ParameValueOBJ;

public class SetITVWIFIService implements IService {
	/** 日志记录 */
	private static Logger logger = LoggerFactory.getLogger(SetITVWIFIService.class);

	@Override
	public String work(String inXml) {
		logger.warn("SetITVWIFIService, inParam:({})", inXml);
		setITVWIFIChecker checker = new setITVWIFIChecker(inXml);
		// 验证入参格式是否正确
		if (false == checker.check()) {
			logger.error("servicename[SetITVWIFIService],cmdId[{}],验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getReturnXml() });
			logger.warn("SetITVWIFIService,cmdId[{}],return=({})", checker.getCmdId(), checker.getReturnXml());
			return checker.getReturnXml();
		}
		QueryDevDAO qdDao = new QueryDevDAO();
		List<HashMap<String, String>> userMap = null;
		if (checker.getUserInfoType() == 1) {
			userMap = qdDao.queryUserByNetAccount(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 2) {
			userMap = qdDao.queryUserByLoid(checker.getUserInfo());
		} else if (checker.getUserInfoType() == 3) {
			String userInfo = checker.getUserInfo();
			userMap = qdDao.queryUserByDevSN(userInfo, userInfo.substring(userInfo.length() - 6));
		}

		if (userMap == null || userMap.isEmpty()) {
			checker.setResult(1002);
			checker.setResultDesc("查无此客户");
			return checker.getReturnXml();
		}
		if (userMap.size() > 1) {
			checker.setResult(1000);
			checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id"))) {
			checker.setResult(1002);
			checker.setResultDesc("未绑定设备");
			return checker.getReturnXml();
		}

		String deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		// 设备正在被操作，不能获取节点值
		if (-6 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1000);
			checker.setResultDesc("设备正在被操作，不能正常交互");
		} 
		// 设备在线
		else if (1 == flag) {
			logger.warn("设备在线，可以进行操作，device_id={}", deviceId);
			String path = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.";
			List<String> iList = corba.getIList(deviceId, path);
			if (null == iList || iList.isEmpty() || iList.size() < 1) {
				logger.warn("servicename[SetITVWIFIService],cmdId[{}],设备{}获取{}下实例失败，返回",
						new Object[] { checker.getCmdId(), deviceId, path });
				checker.setResult(1000);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
			} else {
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId, iList.size());
				List<String> list=new ArrayList<String>();
				for (String i : iList) {
					String[] gatherPath = new String[] { path + i + ".SSID" };
					ArrayList<ParameValueOBJ> objList = corba.getValue(deviceId, gatherPath);
					if (null == objList || objList.isEmpty() || objList.size() < 1) {
						continue;
					} else {
						String value = objList.get(0).getValue();
						if (value.startsWith("iTV-")) {
							list.add(i);
						}
					}
				}
				
				if (list!=null && list.size()>0) {
					ArrayList<ParameValueOBJ> objList = new ArrayList<ParameValueOBJ>();
					for (String i : list) {
						ParameValueOBJ enableObj = new ParameValueOBJ();
						enableObj.setName(path+i+".Enable");
						enableObj.setValue(checker.getEnable());
						enableObj.setType("1");
						objList.add(enableObj);
					}
					
					int retResult = corba.setValue(deviceId, objList);
					if (0 == retResult || 1 == retResult) {
						checker.setResult(0);
						checker.setResultDesc("成功");
					}else {
						checker.setResult(1000);
						checker.setResultDesc("配置不能正常生效");
					}
					
				}else {
					checker.setResult(1000);
					checker.setResultDesc("没有ITV无线口");
				}
			}

		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值,device_id={}", deviceId);
			checker.setResult(1012);
			checker.setResultDesc("设备不在线");
		}

		String returnXml = checker.getReturnXml();
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "SetITVWIFIService");
		logger.warn("servicename[SetITVWIFIService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
		return returnXml;
	}

}
