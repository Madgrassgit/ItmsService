package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.VoipRegistTestChecker4AH;


public class VoipRegistTestService4AH implements IService{
	
	private static Logger logger = LoggerFactory.getLogger(VoipRegistTestService4AH.class);
	
	public String work(String inXml)
	{
		String returnXml = null;
		VoipRegistTestChecker4AH checker = new VoipRegistTestChecker4AH(inXml);
		if (false == checker.check()) {
			logger.warn(
					"servicename[VoipRegistTestService4AH]cmdId[{}]loid[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getLoid(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[VoipRegistTestService4AH]cmdId[{}]loid[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getLoid(),
						inXml });
		
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		
		List<HashMap<String, String>> userList = deviceInfoDAO.queryUserByLoid(checker.getLoid());
		
		if (null == userList || userList.isEmpty()) {
			logger.warn(
					"servicename[VoipRegistTestService4AH]cmdId[{}]loid[{}]无此客户信息",
					new Object[] { checker.getCmdId(), checker.getLoid()});
			checker.setResult(1002);
			checker.setResultDesc("无此客户信息：" + checker.getLoid());
			returnXml = checker.getReturnXml();
			//记录日志
			logger.warn(
					"servicename[VoipRegistTestService4AH]cmdId[{}]loid[{}]处理结束，返回响应信息:{}",
					new Object[] { checker.getCmdId(), checker.getLoid(),returnXml});
			
			return returnXml;
		} 
		else{
			String deviceId = userList.get(0).get("device_id");
//			String user_id = userList.get(0).get("user_id");
			
			Map<String,String> deviceMap = deviceInfoDAO.queryDevInfoByDeviceId(deviceId);
			
			if(null == deviceMap){
				logger.warn(
						"servicename[VoipRegistTestService4AH]cmdId[{}]loid[{}]没有查到设备",
						new Object[] { checker.getCmdId(), checker.getLoid()});
				checker.setResult(1000);
				checker.setResultDesc("没有查到设备：" + checker.getLoid());
				returnXml = checker.getReturnXml();
				logger.warn(
						"servicename[VoipRegistTestService4AH]cmdId[{}]loid[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getLoid(),returnXml});
				
				return returnXml;
			}
			
//			Map<String,String> sipInfoMap = deviceInfoDAO.querySipInfoByUserId(user_id);
			
//			if(null == sipInfoMap){
//				logger.warn(
//						"servicename[VoipRegistTestService4AH]cmdId[{}]loid[{}]没有查到sip服务器信息",
//						new Object[] { checker.getCmdId(), checker.getLoid()});
//				checker.setResult(1000);
//				checker.setResultDesc("没有查到sip服务器信息：" + checker.getLoid());
//				returnXml = checker.getReturnXml();
//				logger.warn(
//						"servicename[VoipRegistTestService4AH]cmdId[{}]loid[{}]处理结束，返回响应信息:{}",
//						new Object[] { checker.getCmdId(), checker.getLoid(),returnXml});
//				
//				return returnXml;
//			}
			
			try { 
				StringBuffer inParam = new StringBuffer();
				inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>");
				inParam.append("<root>");
				inParam.append("<CmdID>" + checker.getCmdId() + "</CmdID>");
				inParam.append("<CmdType>" + checker.getCmdType() + "</CmdType>");
				inParam.append("<ClientType>" + checker.getClientType() + "</ClientType>");
				inParam.append("<Param>");
				inParam.append("<DevSn>" + StringUtil.getStringValue(deviceMap,"device_serialnumber") + "</DevSn>");
				inParam.append("<OUI>" + StringUtil.getStringValue(deviceMap, "oui") + "</OUI>");
				inParam.append("<CityId>" + StringUtil.getStringValue(deviceMap, "city_id") + "</CityId>");
//				inParam.append("<RegisterServer>" + StringUtil.getStringValue(sipInfoMap, "regi_serv") + "</RegisterServer>");
				inParam.append("<RegisterServer>1</RegisterServer>");
				inParam.append("</Param>");
				inParam.append("</root>");
			
				returnXml = new VoipRegistTestService().work(inParam.toString());
		} catch (Exception e) {
			logger.warn(
					"servicename[VoipRegistTestService4AH]cmdId[{}]loid[{}]VoipRegistTestService4AH Exception",
					new Object[] { checker.getCmdId(), checker.getLoid()});
			e.printStackTrace();
			returnXml = "接口异常";
		}
		//记录日志
		logger.warn(
				"servicename[VoipRegistTestService]cmdId[{}]loid[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getLoid(),returnXml});
		// 回单
		return returnXml;
		}
	}
	
}
