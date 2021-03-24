package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.QueryFailedCodeChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QueryFailedCodeService implements IService{
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryFailedCodeService.class);
	
	
	public String work(String inXml){
		
		logger.warn("queryFailedCode：inXml({})", inXml);
		
		QueryFailedCodeChecker checker = new QueryFailedCodeChecker(inXml);
		
		if (false == checker.check()) {
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		QueryDevDAO qdnDao = new QueryDevDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		
		String deviceId = "";
		List<HashMap<String, String>> userMap = null;
		if (checker.getUserInfoType() == 1)
		{
			userMap = qdnDao.queryUserByNetAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 2)
		{
			userMap = qdnDao.queryUserByLoid(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 3)
		{
			userMap = qdnDao.queryUserByIptvAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 4)
		{
			userMap = qdnDao.queryUserByVoipPhone(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 5)
		{
			userMap = qdnDao.queryUserByVoipAccount(checker.getUserInfo());
		}else{
		}
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(1001);
			checker.setResultDesc("无此用户信息");
			checker.setFailedCode("");
			return checker.getReturnXml();
		}
		if (userMap.size() > 1 && checker.getUserInfoType() != 1)
		{
			checker.setResult(1000);
			checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
			checker.setFailedCode("");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(1002);
			checker.setResultDesc("未绑定设备");
			checker.setFailedCode("");
			return checker.getReturnXml();
		}
		
		
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		
		// 判断设备是否在线，只有设备在线，才可以获取设备的节点信息
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		
		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			checker.setFailedCode("");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		// 设备在线
		else if (1 == flag) {
			logger.warn("设备在线，可以获取节点值，device_id={}", deviceId);
			
			String [] arr = new String[]{"InternetGatewayDevice.WANDevice.1.WANConnectionDevice.i.WANPPPConnection.1.LastConnectionError"};
			String wanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
			String wanPathI = ".X_CT-COM_WANEponLinkConfig.VLANIDMark";
			Map<String,String> tmp = qdnDao.queryVlanId(StringUtil.getStringValue(userMap.get(0), "user_id"));
//			if(!"2".equals(StringUtil.getStringValue(tmp, "wan_type"))){
//				checker.setResult(1001);
//				checker.setResultDesc("用户无路由业务");
//				checker.setFailedCode("");
//				return checker.getReturnXml();
//			}
			List<String> iList = corba.getIList(deviceId, wanPath);
			if (null == iList || iList.isEmpty())
			{
				logger.warn("[{}]获取iList失败，返回", deviceId);
				checker.setResult(1009);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				checker.setFailedCode("");
				return checker.getReturnXml();
			}else{
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
			}
			String vlanId = StringUtil.getStringValue(tmp, "vlanid");
			String iValue = "";
			for(String i : iList){
				String path = wanPath + i + wanPathI;
				logger.warn("path:{}",path);
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, path);
				if (null == objLlist || objLlist.isEmpty()) {
					continue;
				}
				String vlanTmp = objLlist.get(0).getValue();
				if(vlanId.equals(vlanTmp)){
					iValue = i;
					break;
				}
			}
			
			if("".equals(iValue)){
				logger.warn("[{}][iValue:{}]未获取到i，返回", deviceId,iValue);
				checker.setResult(1009);
				checker.setResultDesc("用户信息有误");
				checker.setFailedCode("");
				return checker.getReturnXml();
			}else{
				logger.warn("[{}]获取到i值[iValue:{}]", deviceId,iValue);
				arr[0] = arr[0].replace(".i.", "." + iValue + ".");
			}
			
			// 调用Corba 获取节点的值
			ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, arr);
			
			if (null == objLlist || objLlist.isEmpty()) {
				checker.setResult(1009);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				checker.setFailedCode("");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				
				return checker.getReturnXml();
			}
			
			checker.setFailedCode(StringUtil.getStringValue(objLlist.get(0).getValue()));
			
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"queryFailedCode");
			
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
			
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			checker.setFailedCode("");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
	}
}
