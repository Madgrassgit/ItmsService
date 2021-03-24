package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.SocketUtil;
import com.linkage.itms.dao.BridgeToRoutDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.BridgeToRoutChecker;

/**
 * 江苏ITMS需求：JSDX_ITMS-REQ-20120220-LUHJ-004
 * 
 * 桥改路由
 * 
 * @author Administrator
 *
 */

public class BridgeToRoutService implements IService{

	private static final Logger logger = LoggerFactory.getLogger(BridgeToRoutService.class);
	
	@Override
	public String work(String inXml) {
		BridgeToRoutChecker bridgeToRoutChecker = new BridgeToRoutChecker(inXml);
		
		// 验证入参格式是否正确
		if(false == bridgeToRoutChecker.check()){
			logger.error(
					"servicename[BridgeToRoutService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { bridgeToRoutChecker.getCmdId(), bridgeToRoutChecker.getUserInfo(),
							bridgeToRoutChecker.getReturnXml() });
			return bridgeToRoutChecker.getReturnXml();
		}
		logger.warn(
				"servicename[BridgeToRoutService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { bridgeToRoutChecker.getCmdId(), bridgeToRoutChecker.getUserInfo(),
						inXml });		
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				bridgeToRoutChecker.getUserInfoType(), bridgeToRoutChecker
						.getUserInfo());
		
		// 此用户不存在 不能做配置下发，返回失败信息
		if (null == userInfoMap || userInfoMap.isEmpty() || StringUtil.IsEmpty(userInfoMap.get("user_id"))) {
			logger.warn(
					"servicename[BridgeToRoutService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { bridgeToRoutChecker.getCmdId(), bridgeToRoutChecker.getUserInfo()});
			bridgeToRoutChecker.setResult(1002);
			bridgeToRoutChecker.setResultDesc("查无此用户");
			bridgeToRoutChecker.setUserSn("");
			bridgeToRoutChecker.setFailureReason("1");  // 用户不存在
			bridgeToRoutChecker.setSuccStatus("-1");    // 失败
		// 存在此用户，还要判断是否绑定设备
		} else {
			String userId = userInfoMap.get("user_id");
			String devId = userInfoMap.get("device_id");
			
			// 未绑定设备，不能做配置下发，返回失败信息
			if (null == devId || StringUtil.IsEmpty(devId)) {
				logger.warn(
						"servicename[BridgeToRoutService]cmdId[{}]userinfo[{}]未绑定设备",
						new Object[] { bridgeToRoutChecker.getCmdId(), bridgeToRoutChecker.getUserInfo()});
				bridgeToRoutChecker.setResult(1004);
				bridgeToRoutChecker.setResultDesc("此用户未绑定设备");
				bridgeToRoutChecker.setUserSn(StringUtil.getStringValue(userInfoMap.get("username")));
				bridgeToRoutChecker.setFailureReason("2"); // 没有绑定设备
				bridgeToRoutChecker.setSuccStatus("-1");   // 失败
			
			// 绑定设备，还要判断此设备是否在线
			 } else {
				
				 BridgeToRoutDAO bridgeToRoutDAO = new BridgeToRoutDAO();
				 
				 ArrayList<HashMap<String, String>> devList = bridgeToRoutDAO.getDevStatusInfo(devId);
				 Map<String, String> devMap = devList.get(0);
				 String strOnline = devMap.get("online_status");
				 
				 int intOnlined = StringUtil.getIntegerValue(strOnline, 100);
				 
				 // 如果设备不在线，则不能下发桥改路由配置，返回失败信息，否则可以下发桥改路由配置
				 if (0 == intOnlined && !"cq_dx".equals(Global.G_instArea)) {
						bridgeToRoutChecker.setResult(1003);
						bridgeToRoutChecker.setResultDesc("终端不在线");
						bridgeToRoutChecker.setUserSn(StringUtil.getStringValue(userInfoMap.get("username")));
					 bridgeToRoutChecker.setUserSn(StringUtil.getStringValue(userInfoMap.get("username")));
					 bridgeToRoutChecker.setFailureReason("3"); // 设备不在线
					 bridgeToRoutChecker.setSuccStatus("-1");   // 失败
				} else {
					List<Map<String, String>> list = bridgeToRoutDAO.checkService(devId);
					Map<String, String> resltMap = list.get(0);
					if(resltMap == null || resltMap.isEmpty())
					{
						bridgeToRoutChecker.setResult(1002);
						bridgeToRoutChecker.setResultDesc("无桥接上网业务");
						bridgeToRoutChecker.setUserSn(StringUtil.getStringValue(userInfoMap.get("username")));
						bridgeToRoutChecker.setFailureReason("1"); 
						bridgeToRoutChecker.setSuccStatus("-1");
					}
					else
					{
						if("cq_dx".equals(Global.G_instArea)){
							StringBuffer bssSheet = new StringBuffer();
							bssSheet.append("<?xml version=\"1.0\" encoding=\"GBK\"?>");
							bssSheet.append("<itms_97_interface>");
							bssSheet.append("<service_type>21</service_type>");
							bssSheet.append("<service_opt>9</service_opt>");
							bssSheet.append("<itms_97_info>");
							bssSheet.append("<work_asgn_id>" + System.currentTimeMillis() + "_itmsService" + "</work_asgn_id>");
							if(2 == bridgeToRoutChecker.getUserInfoType()){
								bssSheet.append("<logic_id>" + bridgeToRoutChecker.getUserInfo() + "</logic_id>");
							}else{
								String username = StringUtil.getStringValue(userInfoMap.get("username"));
								bssSheet.append("<logic_id>" + username + "</logic_id>");
							}							
							bssSheet.append("<customer_id></customer_id>");
							if(1 == bridgeToRoutChecker.getUserInfoType()){
								bssSheet.append("<account_name>" + bridgeToRoutChecker.getUserInfo() + "</account_name>");
							}else{
								String netAccount = StringUtil.getStringValue(userInfoMap.get("netAccount"));
								bssSheet.append("<account_name>" + netAccount + "</account_name>");
							}
							bssSheet.append("<passwd>" + bridgeToRoutChecker.getBroadbandPassword() + "</passwd>");
							bssSheet.append("<rg_mode>1</rg_mode> ");
							bssSheet.append("<vlan_id></vlan_id>");
							bssSheet.append("</itms_97_info>");
							bssSheet.append("</itms_97_interface>");
							logger.warn("发送工单："+bssSheet.toString());
							String str = SocketUtil.sendStrMesg(
									Global.G_ITMS_SHEET_SERVER_CHINA_MOBILE, 
									StringUtil.getIntegerValue(Global.G_ITMS_SHEET_PORT_CHINA_MOBILE), 
									bssSheet.toString());
							logger.warn("{}回单："+str,StringUtil.getStringValue(resltMap, "username"));
							bridgeToRoutChecker.setResult(0);
							bridgeToRoutChecker.setResultDesc("成功");
							bridgeToRoutChecker.setUserSn(StringUtil.getStringValue(userInfoMap.get("username")));
							bridgeToRoutChecker.setFailureReason("0"); 
							bridgeToRoutChecker.setSuccStatus("1");   // 成功	
						}else{
							StringBuffer bssSheet = new StringBuffer();
							bssSheet.append("6|||50|||1|||");
							bssSheet.append(new DateTimeUtil().getYYYYMMDDHHMMSS()).append("|||");
							bssSheet.append(StringUtil.getStringValue(resltMap, "city_id")).append("|||");
							bssSheet.append(StringUtil.getStringValue(resltMap, "username")).append("LINKAGE");
							logger.warn("发送工单："+bssSheet.toString());
							String str = SocketUtil.sendStrMesg(
									Global.G_ITMS_SHEET_SERVER_CHINA_MOBILE, 
									StringUtil.getIntegerValue(Global.G_ITMS_SHEET_PORT_CHINA_MOBILE), 
									bssSheet.toString());
							logger.warn("{}回单："+str,StringUtil.getStringValue(resltMap, "username"));
							bridgeToRoutChecker.setResult(0);
							bridgeToRoutChecker.setResultDesc("成功");
							bridgeToRoutChecker.setUserSn(StringUtil.getStringValue(userInfoMap.get("username")));
							bridgeToRoutChecker.setFailureReason("0"); 
							bridgeToRoutChecker.setSuccStatus("1");   // 成功	
						}
					}
					
					/**路由开通更改为开通无线路由，以前的桥改路由方式注释  20121207 zhangsm**/
//					String port = bridgeToRoutDAO.getLanInter(devId, "1");  // "1" 表示先查数据库，如果数据库查不到，则采集
//					
//					String accessStyleId = resltMap.get("access_style_id");
//					String routeAccount = resltMap.get("username"); 
//					String routePasswd = resltMap.get("passwd"); 
//					String pvc = "8/35";  // 如果是AD上行，PVC默认是8/35；LAN上行没有PVC，vlanid为空；PON上行的是vlanid，由工单带进来
//					String vlan = resltMap.get("vlanid");
//					String deviceserialnumbe = resltMap.get("device_serialnumber");
//					String oui = resltMap.get("oui"); 
//					String bindPort = port.substring(0, port.length()-1); // 去掉尾部的","
//					
//					String result = "";
//					if ("1".equals(accessStyleId)) {
//						result = bridgeToRoutDAO.changeConnectionType(devId, routeAccount, routePasswd, pvc, "", accessStyleId, userId, deviceserialnumbe, oui, bindPort);
//					} else if ("2".equals(accessStyleId)) {
//						result = bridgeToRoutDAO.changeConnectionType(devId, routeAccount, routePasswd, "", "0", accessStyleId, userId, deviceserialnumbe, oui, bindPort);
//					} else if ("3".equals(accessStyleId)) {
//						result = bridgeToRoutDAO.changeConnectionType(devId, routeAccount, routePasswd, "", vlan, accessStyleId, userId, deviceserialnumbe, oui, bindPort);
//					} 
//					String[] resu = result.split(";");
//					if ("-1".equals(resu[0])) {
//						 bridgeToRoutChecker.setUserSn(StringUtil.getStringValue(userInfoMap.get("username")));
//						 bridgeToRoutChecker.setFailureReason(resu[1]); 
//						 bridgeToRoutChecker.setSuccStatus("-1");   // 失败
//					}else {
//						bridgeToRoutChecker.setUserSn(StringUtil.getStringValue(userInfoMap.get("username")));
//						 bridgeToRoutChecker.setFailureReason(""); 
//						 bridgeToRoutChecker.setSuccStatus("1");   // 成功
//					}
				}
			}
			
		}
		
		String returnXml = bridgeToRoutChecker.getReturnXml();
		
		// 记录日志
		new RecordLogDAO().recordDispatchLog(bridgeToRoutChecker, bridgeToRoutChecker.getUserInfo(), "BridgeToRoutService");
		logger.warn(
				"servicename[BridgeToRoutService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { bridgeToRoutChecker.getCmdId(), bridgeToRoutChecker.getUserInfo(),returnXml});
		return returnXml;
	}
	public static void main(String[] args)
	{
		System.out.println(new DateTimeUtil().getYYYYMMDDHHMMSS());
	}
}
