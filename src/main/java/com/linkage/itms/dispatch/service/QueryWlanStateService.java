package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.QueryWlanStateChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class QueryWlanStateService implements IService{
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryWlanStateService.class);
	
	
	public String work(String inXml){
		
		logger.warn("QueryWlanStateService：inXml({})", inXml);
		
		QueryWlanStateChecker checker = new QueryWlanStateChecker(inXml);
		
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
		}else{
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
			
		    String lanPath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.";
		    List<String> iList = corba.getIList(deviceId, lanPath);
			if (null == iList || iList.isEmpty())
			{
				logger.warn("[{}]获取iList失败，返回", deviceId);
				checker.setResult(1009);
				if("nmg_dx".equals(Global.G_instArea)){
					checker.setResultDesc("该设备没有WIFI功能不支持查询");
				} else {
					checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				}
				return checker.getReturnXml();
			}else{
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
			}
			List<HashMap<String,String>> wanList = new ArrayList<HashMap<String,String>>();
			for(String i : iList){
				String[] gatherPath = new String[]{
						"InternetGatewayDevice.LANDevice.1.WLANConfiguration."+i+".SSID",
						"InternetGatewayDevice.LANDevice.1.WLANConfiguration."+i+".Enable",
						"InternetGatewayDevice.LANDevice.1.WLANConfiguration."+i+".TotalBytesReceived",
						"InternetGatewayDevice.LANDevice.1.WLANConfiguration."+i+".TotalBytesSent"
						};
				
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
				if (null == objLlist || objLlist.isEmpty()) {
					continue;
				}
				HashMap<String,String> tmp = new HashMap<String,String>();
				
				String enable = "";
				String ssid = "";
				String received = "";
				String sent = "";
				for(ParameValueOBJ pvobj : objLlist){
					if(pvobj.getName().contains("SSID")){
						ssid = pvobj.getValue();
					}else if(pvobj.getName().contains("Enable")){
						enable = pvobj.getValue();
					}else if(pvobj.getName().contains("TotalBytesReceived")){
						received = pvobj.getValue();
					}else if(pvobj.getName().contains("TotalBytesSent")){
						sent = pvobj.getValue();
					}
				}
				
				tmp.put("SSIDnum", i);
				tmp.put("SSIDname", ssid);
				tmp.put("RstState", enable);
				tmp.put("BytesReceived", received);
				tmp.put("BytesSent", sent);
				
				wanList.add(tmp);
				tmp = null;
				objLlist.clear();
				gatherPath = null;
				enable = null;
				ssid = null;
				received = null;
				sent = null;
			}
			checker.setWanList(wanList);
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"QueryWlanStateService");
						
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
