package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.QueryDeviceIPorDNSChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QueryDeviceIPorDNSService implements IService{
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceIPorDNSService.class);
	
	
	public String work(String inXml){
		
		logger.warn("QueryDeviceIPorDNSService：inXml({})", inXml);
		
		QueryDeviceIPorDNSChecker checker = new QueryDeviceIPorDNSChecker(inXml);
		
		if (false == checker.check()) {
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		QueryDevDAO qdDao = new QueryDevDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		String deviceId = "";
		String userId = "";
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
			}else{
			}
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
		userId = StringUtil.getStringValue(userMap.get(0), "user_id", "");
		Map<String,String> tmpMap = qdDao.queryVlanId(userId);
		String wanType = StringUtil.getStringValue(tmpMap,"wan_type");
		if(!"2".equals(wanType)){
			logger.warn("设备非路由模式，不采集，device_id={}", deviceId);
			checker.setResult(1000);
			checker.setResultDesc("设备非路由模式，不采集");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
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
		    String wanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		    List<String> iList = corba.getIList(deviceId, wanPath);
			if (null == iList || iList.isEmpty())
			{
				logger.warn("[{}]获取iList失败，返回", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return checker.getReturnXml();
			}else{
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
			}
			String accessType = StringUtil.getStringValue(qdDao.queryAccessType(userId),"adsl_hl");
			String path = "X_CT-COM_WANGponLinkConfig";
			if("3.0".equals(accessType)){
				path = "X_CT-COM_WANEponLinkConfig";
			}
			List<HashMap<String,String>> tmpList = qdDao.queryWanTypeAndVlanIds(userId);
			StringBuffer vlanStr = new StringBuffer();
			if(tmpList != null && tmpList.size() > 0){
				for(HashMap<String,String> tmpHash : tmpList){
					vlanStr.append(StringUtil.getStringValue(tmpHash,"vlanid"));
					vlanStr.append(",");
				}
			}
			
			List<HashMap<String,String>> wanList = new ArrayList<HashMap<String,String>>();
			for(String i : iList){
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, "InternetGatewayDevice.WANDevice.1.WANConnectionDevice." + i + "." + path + ".VLANIDMark");
				if (null == objLlist || objLlist.isEmpty()) {
					path = "X_CT-COM_WANEponLinkConfig";
					objLlist = corba.getValue(deviceId, "InternetGatewayDevice.WANDevice.1.WANConnectionDevice." + i + "." + path + ".VLANIDMark");
					if(null == objLlist || objLlist.isEmpty()){
						continue;
					}
				}
				String vlanid = objLlist.get(0).getValue();
				if(vlanStr.indexOf(vlanid) < 0){
					objLlist = null;
					continue;
				}
				String[] gatherPathIpV4 = new String[]{
										"InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+i+".WANPPPConnection.1.ExternalIPAddress",
										"InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+i+".WANPPPConnection.1.DNSServers"};
				String[] gatherPathIpV6 = new String[]{
						"InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+i+".WANPPPConnection.1.X_CT-COM_IPv6IPAddress",
						"InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+i+".WANPPPConnection.1.X_CT-COM_IPv6DNSServers"
							};
				objLlist = corba.getValue(deviceId, gatherPathIpV4);
				ArrayList<ParameValueOBJ> objLlistv6 = corba.getValue(deviceId, gatherPathIpV6);
				if (null == objLlist || objLlist.isEmpty()) {
					checker.setResult(1000);
					checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					return checker.getReturnXml();
				}
				HashMap<String,String> tmp = new HashMap<String,String>();
				String ipv4ip = "";
				String ipv4dns = "";
				String ipv6ip = "";
				String ipv6dns = "";
				for(ParameValueOBJ pv : objLlist){
					if(pv.getName().contains("ExternalIPAddress")){
						ipv4ip = pv.getValue();
					}else{
						ipv4dns = pv.getValue();
					}
				}
				if(objLlistv6 != null){
					for(ParameValueOBJ pv6 : objLlistv6){
						if(pv6.getName().contains("X_CT-COM_IPv6IPAddress")){
							ipv6ip = pv6.getValue();
						}else if(pv6.getName().contains("X_CT-COM_IPv6DNSServers")){
							ipv6dns = pv6.getValue();
						}
					}
				}
				
				tmp.put("IntelnetVlan", vlanid);
				tmp.put("IPv4IPAddress", ipv4ip);
				tmp.put("IPv4DNSAddress", ipv4dns);
				tmp.put("IPv6IPAddress", ipv6ip);
				tmp.put("IPv6DNSAddress", ipv6dns);
				wanList.add(tmp);
				objLlist = null;
				objLlistv6 = null;
			}
			
			checker.setWanList(wanList);
			
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"QueryDeviceIPorDNSService");
						
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
