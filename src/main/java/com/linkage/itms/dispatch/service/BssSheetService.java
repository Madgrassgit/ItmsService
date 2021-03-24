package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.CityDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.BssSheetChecker;


/**
 * BSS用户业务工单及开通状态查询
 * @author zhangshimin(工号) Tel:
 * @version 1.0
 * @since 2011-5-11 下午05:22:09
 * @category com.linkage.itms.dispatch.service
 * @copyright 南京联创科技 网管科技部
 *
 */
public class BssSheetService implements IService
{
	private static Logger logger = LoggerFactory.getLogger(BindInfoService.class);
	private Map<String, String> cityMap = null;

	/**
	* 工单查询执行方法
	*/
	@Override
	public String work(String inXml)
	{
		// 应新疆要求回参中不要显示city_id，要显示city_name，但此接口又被江苏等其他地方使用，所以在此做了一下分支
		// 通过读取配置文件litms_conf.xml中的InstArea节点来判断此接口将用于哪个省份
		BssSheetChecker bssSheetChecker = new BssSheetChecker(inXml);
		List<Map<String,String>> sheetInfos = new ArrayList<Map<String,String>>();
		Map<String,String> sheetInfoMap = null;
		if (false == bssSheetChecker.check()) {
			logger.error(
					"servicename[BssSheetService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { bssSheetChecker.getCmdId(), bssSheetChecker.getUserInfo(),
							bssSheetChecker.getReturnXml() });
			return bssSheetChecker.getReturnXml();
		}
		logger.warn(
				"servicename[BssSheetService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { bssSheetChecker.getCmdId(), bssSheetChecker.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				bssSheetChecker.getUserInfoType(), bssSheetChecker.getUserInfo(), bssSheetChecker.getCityId());
		if (null == userInfoMap || userInfoMap.isEmpty() || StringUtil.IsEmpty(userInfoMap.get("user_id"))) {
			logger.warn(
					"servicename[BssSheetService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { bssSheetChecker.getCmdId(), bssSheetChecker.getUserInfo()});
			bssSheetChecker.setResult(1002);
			bssSheetChecker.setResultDesc("查无此用户");
		} else {
			String userId = userInfoMap.get("user_id");
			String userDevId = userInfoMap.get("device_id");
			
			ServUserDAO servUserDAO = new ServUserDAO();  // 将下面注释的代码提前至此处 add by zhangchy 2012-01-18
			List<Map<String,String>> sheetServInfos = servUserDAO.getBssSheetServInfo(userId);  // 将下面注释的代码提前至此处 add by zhangchy 2012-01-18
			cityMap = CityDAO.getCityIdCityNameMap();
			
			 if (StringUtil.IsEmpty(userDevId)) {
				// 未绑定设备
				 logger.warn(
							"servicename[BssSheetService]cmdId[{}]userinfo[{}]未绑定设备",
							new Object[] { bssSheetChecker.getCmdId(), bssSheetChecker.getUserInfo()});
				bssSheetChecker.setResult(0); // 应陆海俊要求，将由原的1004错误码，改为0 by zhangchy 2012-08-27
				bssSheetChecker.setResultDesc("此用户未绑定设备");
				
				/**
				 * 之前新疆电信要求当设备未绑定时也要返回用户信息，现在
				 * 江苏电信也要求，当设备未绑定时，回参也要有相关的用户和设备信息，所以要将if("xj_dx".equals(Global.G_instArea)) 注释
				 * 注释 by zhangchy 2012-08-17
				 */
//				if("xj_dx".equals(Global.G_instArea)){  // 新疆要求如果用户未绑定设备，需要将此用户的信息返回  add by zhangchy 2012-01-17
					
					for(Map<String,String> sheetInfo : sheetServInfos){
						sheetInfoMap = new HashMap<String, String>();
						bssSheetChecker.setUserSN(StringUtil.getStringValue(sheetInfo.get("username")));
//						if (null != sheetInfo.get("device_serialnumber") || !"".equals(sheetInfo.get("device_serialnumber"))) {
//							bssSheetChecker.setDevSn(StringUtil.getStringValue(sheetInfo.get("device_serialnumber")));
//						}
						String city_name = StringUtil.getStringValue(cityMap.get(sheetInfo.get("city_id")));
						bssSheetChecker.setCityName(city_name);
						bssSheetChecker.setCityId(sheetInfo.get("city_id"));
						bssSheetChecker.setDevType(StringUtil.getStringValue(sheetInfo.get("type_id")));
						// SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口)
						if ("sd_lt".equals(Global.G_instArea)) {
							bssSheetChecker.setSpecName(StringUtil.getStringValue(sheetInfo.get("spec_name")));
						}
						
						sheetInfoMap.put("DealDate", StringUtil.getStringValue(sheetInfo.get("dealdate")));
						sheetInfoMap.put("CompleteDate", StringUtil.getStringValue(sheetInfo.get("completedate")));
						sheetInfoMap.put("ServiceType", StringUtil.getStringValue(sheetInfo.get("serv_type_id")));
						sheetInfoMap.put("OpenStatus", StringUtil.getStringValue(sheetInfo.get("open_status")));
						
						if("xj_dx".equals(Global.G_instArea)){
							// XJDX-REQ-20130123-HUJG3-001 要求增加协议类型 add by zhangchy 2013-02-19
							if ("14".equals(StringUtil.getStringValue(sheetInfo.get("serv_type_id")))) {
								List<HashMap<String, String>> hashMapList = servUserDAO.getVoipPhone(userId);
								if (null != hashMapList) {
									String protocol = "";
									String ProtocolVal = "";
									for(Map<String, String> map :hashMapList){
										if ("".equals(protocol)) {
											ProtocolVal = "2".equals(StringUtil.getStringValue(map, protocol, ""))? "H248" : "SIP";
											protocol = ProtocolVal;
										} else {
											ProtocolVal = "2".equals(StringUtil.getStringValue(map, protocol, ""))? "H248" : "SIP";
											protocol = protocol + ";" + ProtocolVal;
										}
									}
									sheetInfoMap.put("Protocol", protocol);  // VOIP帐号
								}else {
									sheetInfoMap.put("Protocol", "");  // VOIP帐号
								}
							}
							
							//chenxj6 begin
							if ("10".equals(sheetInfo.get("serv_type_id"))) {
								sheetInfoMap.put("KdUserName", StringUtil.getStringValue(sheetInfo ,"user_name")); // 宽带帐号
							}
							if ("11".equals(sheetInfo.get("serv_type_id"))) {  // ITV 帐号
								sheetInfoMap.put("IPTVUserName", StringUtil.getStringValue(sheetInfo ,"user_name"));
							}
							if ("14".equals(sheetInfo.get("serv_type_id"))) {
								List<HashMap<String, String>> hashMapList = servUserDAO.getVoipPhone(userId);
								String voipPhone = "";
								if (null != hashMapList) {
									for(Map<String, String> map : hashMapList) {
										if (null != map.get("voip_phone") && !"".equals(map.get("voip_phone"))) {
											if ("".equals(voipPhone)) {
												voipPhone = map.get("voip_phone");
											} else {
												voipPhone += "|"+map.get("voip_phone");
											}
										}
									}
								}
								sheetInfoMap.put("VoipUserName", voipPhone);  // VOIP帐号
							}	
							//chenxj6 end
							
						}else if("jx_dx".equals(Global.G_instArea)){

							if ("10".equals(sheetInfo.get("serv_type_id"))) {
								sheetInfoMap.put("KdUserName", sheetInfo.get("user_name")); // 宽带帐号
								sheetInfoMap.put("KdWanType", sheetInfo.get("wan_type"));
								sheetInfoMap.put("KdVlanId", sheetInfo.get("vlanId"));
							}
							if ("11".equals(sheetInfo.get("serv_type_id"))) {  // ITV 帐号
								sheetInfoMap.put("IPTVUserName", sheetInfo.get("user_name"));
								sheetInfoMap.put("IPTVVlanId", sheetInfo.get("vlanId"));
							}
							if ("14".equals(sheetInfo.get("serv_type_id"))) {
								List<HashMap<String, String>> hashMapList = servUserDAO.getVoipPhone(userId);
								if (null != hashMapList) {
									String voipPhone = "";
									for(Map<String, String> map :hashMapList){
										if (null != map.get("voip_phone")
											&& !"".equals(map.get("voip_phone"))) {
											if ("".equals(voipPhone)) {
												voipPhone = map.get("voip_phone");
											} else {
												voipPhone = voipPhone + "|" + map.get("voip_phone");
											}
										}
									}
									sheetInfoMap.put("VoipUserName", voipPhone);  // VOIP帐号
									sheetInfoMap.put("VoipVlanId", sheetInfo.get("vlanId"));
								}else {
									sheetInfoMap.put("VoipUserName", "");  // VOIP帐号
									sheetInfoMap.put("VoipVlanId", sheetInfo.get("vlanId"));
								}
							}
							bssSheetChecker.setResultDesc("成功");
						} else {
							if ("10".equals(sheetInfo.get("serv_type_id"))) {
								sheetInfoMap.put("KdUserName", sheetInfo.get("user_name")); // 宽带帐号
								sheetInfoMap.put("KdWanType", sheetInfo.get("wan_type"));
								// SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口)
								if ("sd_lt".equals(Global.G_instArea)) {
									sheetInfoMap.put("bind_port", sheetInfo.get("bind_port"));
									sheetInfoMap.put("openDate", sheetInfo.get("opendate"));
								}
							}
							if ("11".equals(sheetInfo.get("serv_type_id"))) {  // ITV 帐号
								sheetInfoMap.put("IPTVUserName", sheetInfo.get("user_name"));
								// SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口)
								if ("sd_lt".equals(Global.G_instArea)) {
									sheetInfoMap.put("bind_port", sheetInfo.get("bind_port"));
									sheetInfoMap.put("openDate", sheetInfo.get("opendate"));
								}
							}
							if ("14".equals(sheetInfo.get("serv_type_id"))) {
								List<HashMap<String, String>> hashMapList = servUserDAO.getVoipPhone(userId);
								String bindPort = "";
								String voipPhone = "";
								if (null != hashMapList) {
									for(Map<String, String> map :hashMapList){
										if (null != map.get("voip_phone") && !"".equals(map.get("voip_phone"))) {
											if ("".equals(voipPhone)) {
												voipPhone = map.get("voip_phone");
											} else {
												voipPhone = voipPhone + "|" + map.get("voip_phone");
											}
										}
										if (null != map.get("voip_port") && !"".equals(map.get("voip_port"))) {
											if ("".equals(bindPort)) {
												bindPort = map.get("voip_port");
											}
											else {
												bindPort = bindPort + "|" + map.get("voip_port");
											}
										}
									}
								}
								sheetInfoMap.put("VoipUserName", voipPhone);  // VOIP帐号
								// SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口)
								if ("sd_lt".equals(Global.G_instArea)) {
									sheetInfoMap.put("bind_port", bindPort);
									sheetInfoMap.put("openDate", sheetInfo.get("opendate"));
								}
							}
						}
						
						sheetInfos.add(sheetInfoMap);
						
						bssSheetChecker.setSheetInfo(sheetInfos);
					}
//				}
			 }
			 else
			 {
//				ServUserDAO servUserDAO = new ServUserDAO();   // 此行代码提前到if之前了  modify by zhangchy 2012-01-18
//				List<Map<String,String>> sheetServInfos = servUserDAO.getBssSheetServInfo(userId);  // 此行代码提前到if之前了  modify by zhangchy 2012-01-18
				 for(Map<String,String> sheetInfo : sheetServInfos)
				{
					sheetInfoMap = new HashMap<String, String>();
					bssSheetChecker.setUserSN(sheetInfo.get("username"));
					bssSheetChecker.setDevSn(sheetInfo.get("device_serialnumber"));
					
					if("xj_dx".equals(Global.G_instArea)){  // 新疆电信  add by zhangchy 2012-01-17
						String city_name = StringUtil.getStringValue(cityMap.get(sheetInfo.get("city_id")));
						bssSheetChecker.setCityName(city_name);
					}else {
						bssSheetChecker.setCityId(sheetInfo.get("city_id"));
					}
					
					bssSheetChecker.setDevType(sheetInfo.get("type_id"));
					sheetInfoMap.put("DealDate", sheetInfo.get("dealdate"));
					// SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口)
					if ("sd_lt".equals(Global.G_instArea)) {
						bssSheetChecker.setSpecName(StringUtil.getStringValue(sheetInfo.get("spec_name")));
					}
					//HBDX-REQ-20140429-XuPan-001
					sheetInfoMap.put("CompleteDate", sheetInfo.get("completedate"));
					sheetInfoMap.put("ServiceType", sheetInfo.get("serv_type_id"));
					sheetInfoMap.put("OpenStatus", sheetInfo.get("open_status"));
					
					if("xj_dx".equals(Global.G_instArea)){ 
						// XJDX-REQ-20130123-HUJG3-001 要求增加协议类型 add by zhangchy 2013-02-19
						if ("14".equals(StringUtil.getStringValue(sheetInfo.get("serv_type_id")))) {
							List<HashMap<String, String>> hashMapList = servUserDAO.getVoipPhone(userId);
							if (null != hashMapList) {
								String protocol = "";
								String ProtocolVal = "";
								for(Map<String, String> map :hashMapList){
									if ("".equals(protocol)) {
										ProtocolVal = "2".equals(StringUtil.getStringValue(map, protocol, ""))? "H248" : "SIP";
										protocol = ProtocolVal;
									} else {
										ProtocolVal = "2".equals(StringUtil.getStringValue(map, protocol, ""))? "H248" : "SIP";
										protocol = protocol + ";" + ProtocolVal;
									}
								}
								sheetInfoMap.put("Protocol", protocol);  // VOIP帐号
							}else {
								sheetInfoMap.put("Protocol", "");  // VOIP帐号
							}
						}
						
						//chenxj6 begin
						List<HashMap<String,String>> servResultList = userDevDao.getServResultList(userDevId);
						if(null!=servResultList && !servResultList.isEmpty()){
							// serviceId : 1001-上网  ; 1110-IPTV ; 1401-VOIP
							for(HashMap<String,String> servResMap : servResultList){
								if ("1001".equals(StringUtil.getStringValue(servResMap,	"service_id"))) 
								{
									sheetInfoMap.put("intServResult", StringUtil.getStringValue(servResMap,"fault_desc"));
								}
								if ("1110".equals(StringUtil.getStringValue(servResMap,	"service_id"))) 
								{
									sheetInfoMap.put("iptvServResult", StringUtil.getStringValue(servResMap,"fault_desc"));
								}
								if ("1401".equals(StringUtil.getStringValue(servResMap,	"service_id"))) 
								{
									sheetInfoMap.put("voipServResult", StringUtil.getStringValue(servResMap,"fault_desc"));
								}
							}
						}
						
						if ("10".equals(sheetInfo.get("serv_type_id"))) {
							sheetInfoMap.put("KdUserName", StringUtil.getStringValue(sheetInfo ,"user_name")); // 宽带帐号
						}
						if ("11".equals(sheetInfo.get("serv_type_id"))) {  // ITV 帐号
							sheetInfoMap.put("IPTVUserName", StringUtil.getStringValue(sheetInfo ,"user_name"));
						}
						if ("14".equals(sheetInfo.get("serv_type_id"))) {
							List<HashMap<String, String>> hashMapList = servUserDAO.getVoipPhone(userId);
							String voipPhone = "";
							if (null != hashMapList) {
								for(Map<String, String> map : hashMapList) {
									if (null != map.get("voip_phone") && !"".equals(map.get("voip_phone"))) {
										if ("".equals(voipPhone)) {
											voipPhone = map.get("voip_phone");
										} else {
											voipPhone += "|"+map.get("voip_phone");
										}
									}
								}
							}
							sheetInfoMap.put("VoipUserName", voipPhone);  // VOIP帐号
						}	
						//chenxj6 end
						
					}else if("jx_dx".equals(Global.G_instArea)){

						if ("10".equals(sheetInfo.get("serv_type_id"))) {
							sheetInfoMap.put("KdUserName", sheetInfo.get("user_name")); // 宽带帐号
							sheetInfoMap.put("KdWanType", sheetInfo.get("wan_type"));
							sheetInfoMap.put("KdVlanId", sheetInfo.get("vlanId"));
						}
						if ("11".equals(sheetInfo.get("serv_type_id"))) {  // ITV 帐号
							sheetInfoMap.put("IPTVUserName", sheetInfo.get("user_name"));
							sheetInfoMap.put("IPTVVlanId", sheetInfo.get("vlanId"));
						}
						if ("14".equals(sheetInfo.get("serv_type_id"))) {
							List<HashMap<String, String>> hashMapList = servUserDAO.getVoipPhone(userId);
							if (null != hashMapList) {
								String voipPhone = "";
								for(Map<String, String> map :hashMapList){
									if (null != map.get("voip_phone")
										&& !"".equals(map.get("voip_phone"))) {
										if ("".equals(voipPhone)) {
											voipPhone = map.get("voip_phone");
										} else {
											voipPhone += "|"+map.get("voip_phone");
										}
									}
								}
								sheetInfoMap.put("VoipUserName", voipPhone);  // VOIP帐号
								sheetInfoMap.put("VoipVlanId", sheetInfo.get("vlanId"));
							}else {
								sheetInfoMap.put("VoipUserName", "");  // VOIP帐号
								sheetInfoMap.put("VoipVlanId", sheetInfo.get("vlanId"));
							}
						}
						bssSheetChecker.setResultDesc("成功");
					} else {
						if ("10".equals(sheetInfo.get("serv_type_id"))) {
							sheetInfoMap.put("KdUserName", sheetInfo.get("user_name")); // 宽带帐号
							sheetInfoMap.put("KdWanType", sheetInfo.get("wan_type"));
							// SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口)
							if ("sd_lt".equals(Global.G_instArea)) {
								sheetInfoMap.put("bind_port", sheetInfo.get("bind_port"));
								sheetInfoMap.put("openDate", sheetInfo.get("opendate"));
							}
						}
						if ("11".equals(sheetInfo.get("serv_type_id"))) {  // ITV 帐号
							sheetInfoMap.put("IPTVUserName", sheetInfo.get("user_name"));
							// SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口)
							if ("sd_lt".equals(Global.G_instArea)) {
								sheetInfoMap.put("bind_port", sheetInfo.get("bind_port"));
								sheetInfoMap.put("openDate", sheetInfo.get("opendate"));
							}
						}
						if ("14".equals(sheetInfo.get("serv_type_id"))) {
							List<HashMap<String, String>> hashMapList = servUserDAO.getVoipPhone(userId);
							String bindPort = "";
							String voipPhone = "";
							if (null != hashMapList) {
								for(Map<String, String> map : hashMapList) {
									if (null != map.get("voip_phone") && !"".equals(map.get("voip_phone"))) {
										if ("".equals(voipPhone)) {
											voipPhone = map.get("voip_phone");
										} else {
											voipPhone += "|"+map.get("voip_phone");
										}
									}
									if (null != map.get("voip_port") && !"".equals(map.get("voip_port"))) {
										if ("".equals(bindPort)) {
											bindPort = map.get("voip_port");
										}
										else {
											bindPort = bindPort + "|" + map.get("voip_port");
										}
									}
								}
							}
							sheetInfoMap.put("VoipUserName", voipPhone);  // VOIP帐号
							// SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口)
							if ("sd_lt".equals(Global.G_instArea)) {
								sheetInfoMap.put("bind_port", bindPort);
								sheetInfoMap.put("openDate", sheetInfo.get("opendate"));
							}
						}
					}
					
					sheetInfos.add(sheetInfoMap);
					
					bssSheetChecker.setSheetInfo(sheetInfos);
				}
			 }
		}
		String returnXml = bssSheetChecker.getReturnXml();

		cityMap = null;  // 清楚缓存 add by zhangchy 2012-01-17
		
		// 记录日志
		new RecordLogDAO().recordDispatchLog(bssSheetChecker, bssSheetChecker.getUserInfo(),
				"BssSheetService");
		logger.warn(
				"servicename[BssSheetService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { bssSheetChecker.getCmdId(), bssSheetChecker.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}
}
