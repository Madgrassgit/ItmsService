package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.QueryDeviceMACChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QueryDeviceMACService implements IService{
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceMACService.class);
	
	
	public String work(String inXml){
		
		logger.warn("QueryDeviceMACService：inXml({})", inXml);
		
		QueryDeviceMACChecker checker = new QueryDeviceMACChecker(inXml);
		
		if (false == checker.check()) {
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		QueryDevDAO qdDao = new QueryDevDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		String deviceId = "";
		List<HashMap<String, String>> userMap = null;
		if(checker.getDevSn() != null && !"".equals(checker.getDevSn())){
			if(checker.getDevSn().length() < 6){
				checker.setResult(1005);
				checker.setResultDesc("设备序列号非法,按设备序列号查询时，查询序列号字段少于6位");
				return checker.getReturnXml();
			}
			String dev_sub_sn = checker.getDevSn().substring(checker.getDevSn().length()-6,checker.getDevSn().length());
			userMap = qdDao.queryUserByDevSN(checker.getDevSn(),dev_sub_sn);
			if (userMap.size() > 1)
			{
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				return checker.getReturnXml();
			}
		}else{
			if (checker.getUserInfoType() == 1)
			{
				userMap = qdDao.queryUserByNetAccount(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 2)
			{
				userMap = qdDao.queryUserByLoid(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 3)
			{
				userMap = qdDao.queryUserByIptvAccount(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 4)
			{
				userMap = qdDao.queryUserByVoipPhone(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 5)
			{
				userMap = qdDao.queryUserByVoipAccount(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 6)
			{// 2020/10/30 HLJDX-REQ-ITMS-20201028-JL001 黑龙江电信ITMS与网厅接口升级改造
				userMap = qdDao.queryDevByMac(checker.getUserInfo());
			}
			else{
			}
		}
		
		// 2020/10/30 黑龙江根据设备mac，先校验设备，再查用户，和后面逻辑不同，所以单独在这写
		if(checker.getUserInfoType() == 6) 
		{
			if(userMap == null || userMap.isEmpty()) 
			{
				checker.setResult(1001);
				checker.setResultDesc("无此设备信息");
				return checker.getReturnXml();
			}
			if (userMap.size() > 1)
			{
				checker.setResult(1000);
				checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
				return checker.getReturnXml();
			}
			String customer_id = StringUtil.getStringValue(userMap.get(0), "customer_id", "");
			if(StringUtil.IsEmpty(customer_id)) 
			{
				checker.setResult(1002);
				checker.setResultDesc("未绑定用户");
				return checker.getReturnXml();
			}
			
			// DeviceMAC 要求为设备序列号后12位
			String device_serialnumber = StringUtil.getStringValue(userMap.get(0), "device_serialnumber", "");
			String deviceMAC = "";
			if(!StringUtil.IsEmpty(device_serialnumber) && device_serialnumber.length()>12){
				deviceMAC = device_serialnumber.substring(device_serialnumber.length()-12);
			}
			checker.setDeviceMAC(deviceMAC);
			
			// NetAccount
			userMap = qdDao.queryUserByCustomerId(customer_id);
			if(userMap == null || userMap.size() == 0 
					|| StringUtil.IsEmpty(userMap.get(0).get("username"))) {
				checker.setResult(1007);
				checker.setResultDesc("用户未开通宽带业务");
				return checker.getReturnXml();
			}
			
			String username = StringUtil.getStringValue(userMap.get(0), "username", "");
			checker.setNetAccount(username);
			
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, "QueryDeviceMACService", username);
			return checker.getReturnXml();
		}
		
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(1001);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		if (userMap.size() > 1 && checker.getUserInfoType() != 1)
		{
			checker.setResult(1000);
			checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(1002);
			checker.setResultDesc("未绑定设备");
			return checker.getReturnXml();
		}
		
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		Map<String,String> macMap = qdDao.queryDeviceMac(deviceId);
		//黑龙江直接取数据数据
		if ("hlj_dx".equals(Global.G_instArea)){
			String macString = StringUtil.getStringValue(macMap, "device_serialnumber", "");
			String username = StringUtil.getStringValue(userMap.get(0), "username", "");
			
			if(!StringUtil.IsEmpty(macString) && macString.length()>12){
				macString = macString.substring(macString.length()-12);
			}
			checker.setDeviceMAC(macString);
			checker.setNetAccount(username);
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"QueryDeviceMACService");
			return checker.getReturnXml();
		}
		// 设备表tab_gw_device中获取数据，字段为cpe_mac，如果该字段为空，那么就再通过终端节点方式来查询
		String deviceMac = StringUtil.getStringValue(macMap, "cpe_mac", "");
		if (!StringUtil.IsEmpty(deviceMac))
		{
			checker.setDeviceMAC(deviceMac);
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"QueryDeviceMACService");
			return checker.getReturnXml();
		}
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		// 设备在线
		else if (1 == flag) {
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			String[] gatherPath = new String[]{"InternetGatewayDevice.WANDevice.1.WANConnectionDevice.i.WANIPConnection.1.MACAddress"};
		    String macPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		    List<String> iList = corba.getIList(deviceId, macPath);
			if (null == iList || iList.isEmpty())
			{
				logger.warn("[{}]获取iList失败，返回", deviceId);
				checker.setResult(1009);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return checker.getReturnXml();
			}else{
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
			}
			String userId = StringUtil.getStringValue(userMap.get(0), "user_id", "");
			String accessType = StringUtil.getStringValue(qdDao.queryAccessType(userId),"adsl_hl");
			String path = "X_CT-COM_WANGponLinkConfig";
			if("3.0".equals(accessType)){
				path = "X_CT-COM_WANEponLinkConfig";
			}
			for(String i : iList){
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, "InternetGatewayDevice.WANDevice.1.WANConnectionDevice." + i + "."+ path +".VLANIDMark");
				if (null == objLlist || objLlist.isEmpty()) {
					path = "X_CT-COM_WANEponLinkConfig";
					objLlist = corba.getValue(deviceId, "InternetGatewayDevice.WANDevice.1.WANConnectionDevice." + i + "."+ path +".VLANIDMark");
					if(null == objLlist || objLlist.isEmpty()){
						continue;
					}
				}
				if(!"45".equals(objLlist.get(0).getValue())){
					objLlist = null;
					continue;
				}
				gatherPath[0] = gatherPath[0].replace(".i.", "." + i + ".");
				objLlist = corba.getValue(deviceId, gatherPath);
				if (null == objLlist || objLlist.isEmpty()) {
					checker.setResult(1009);
					checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					return checker.getReturnXml();
				}
				checker.setDeviceMAC(objLlist.get(0).getValue());
				break;
			}
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"QueryDeviceMACService");
						
			return checker.getReturnXml();
			
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值");
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
	}
}
