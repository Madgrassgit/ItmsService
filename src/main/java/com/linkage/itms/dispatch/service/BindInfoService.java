
package com.linkage.itms.dispatch.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.BindInfoChecker;

/**
 * bindInfo的业务处理类
 * 
 * @author onelinesky
 * @date 2011-1-17
 */
public class BindInfoService implements IService
{

	private static Logger logger = LoggerFactory.getLogger(BindInfoService.class);

	/**
	 * 绑定执行方法
	 */
	@Override
	public String work(String inXml)
	{
		BindInfoChecker binder = new BindInfoChecker(inXml);
		if (false == binder.check())
		{
			logger.error(
					"servicename[BindInfoService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { binder.getCmdId(), binder.getUserInfo(),
							binder.getReturnXml() });
			return binder.getReturnXml();
		}
		logger.warn(
				"servicename[BindInfoService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { binder.getCmdId(), binder.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		// 查询设备信息
		if("xj_dx".equals(Global.G_instArea) && binder.getUserInfoType() == 0)
		{
			ArrayList<HashMap<String, String>> devInfoMapList = userDevDao.getTelePasswdByDevSn1(binder.getDevSn(),binder.getOui());
			if (null == devInfoMapList || devInfoMapList.isEmpty())
			{
				logger.warn(
						"servicename[BindInfoService]cmdId[{}]userinfo[{}]查无此设备",
						new Object[] { binder.getCmdId(), binder.getUserInfo()});
				binder.setResult(1004);
				binder.setResultDesc("查无此设备");
			}
			else
			{
				// 终端数是否唯一
				if (devInfoMapList.size() > 1)
				{
					logger.warn(
							"servicename[BindInfoService]cmdId[{}]userinfo[{}]查询到多台设备",
							new Object[] { binder.getCmdId(), binder.getUserInfo()});
					binder.setResult(1006);
					binder.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				}
				else
				{
					HashMap<String, String> devInfoMap = devInfoMapList.get(0);
					if(StringUtil.IsEmpty(devInfoMap.get("username")))
					{
						// 未绑定设备
						logger.warn(
								"servicename[BindInfoService]cmdId[{}]userinfo[{}]未绑定设备",
								new Object[] { binder.getCmdId(), binder.getUserInfo()});
						binder.setResult(1003);
						binder.setResultDesc("未绑定设备");
					}
					else
					{
						binder.setIp(devInfoMap.get("loopback_ip"));
						binder.setUsername(userDevDao.getAllUsername(devInfoMap.get("username")));
						binder.setDeviceSn(devInfoMap.get("device_serialnumber"));
						binder.setBindType(devInfoMap.get("userline"));
						binder.setOui(devInfoMap.get("oui"));
						String time = devInfoMap.get("complete_time");
						if(!StringUtil.IsEmpty(time)){
							long register_time = StringUtil.getLongValue(devInfoMap.get("complete_time"));
							
							DateTimeUtil dtu = new DateTimeUtil(register_time * 1000);
							int devYear = dtu.getYear();
							int currYear = new DateTimeUtil(Calendar.getInstance()).getYear();
							
							logger.warn("设备注册年份[{}],当前时间年份[{}]",new Object[]{devYear,currYear});
							if(currYear - devYear > 8){
								binder.setIsOld("1");
							}else{
								binder.setIsOld("0");
							}
						}else{
							binder.setIsOld("0");
						}
						
						if(!"BBMS".equals(Global.SYSTEM_NAME)){
							String netuser = StringUtil.getStringValue(devInfoMap.get("netuser"));
							String scrapDev = StringUtil.getStringValue(devInfoMap.get("scrap_dev"));
							if(StringUtil.IsEmpty(netuser) && StringUtil.IsEmpty(scrapDev)){
								binder.setGetPast("0");//正常终端
							}else{
								binder.setGetPast("1");//报废终端
							}
						}
						
						String deviceId = StringUtil.getStringValue(devInfoMap.get("device_id"));
						Map<String, String> devTypeInfoMap = userDevDao.getDeviceTypeInfo(deviceId);
						if (devTypeInfoMap != null && !devTypeInfoMap.isEmpty()){
							binder.setVendor(devInfoMap.get("vendor_name"));
							binder.setHandwareVersion(devInfoMap.get("hardwareversion"));
							binder.setSoftwareVersion(devInfoMap.get("softwareversion"));
							binder.setDevModel(devInfoMap.get("device_model"));
							binder.setCpeMac(StringUtil.getStringValue(devTypeInfoMap, "cpe_mac", ""));
							String deviceTypeId = StringUtil.getStringValue(devTypeInfoMap, "devicetype_id", "");
							if(!StringUtil.IsEmpty(deviceTypeId)){
								Map<String,String> deviceVersionMap = userDevDao.getDeviceVersionInfo(deviceTypeId);
								if(null != deviceVersionMap && !deviceVersionMap.isEmpty()){
									binder.setIsWifi(StringUtil.getStringValue(deviceVersionMap, "wifi", ""));
									binder.setLanNum(StringUtil.getStringValue(deviceVersionMap, "lan_num", ""));
									binder.setVoiceNum(StringUtil.getStringValue(deviceVersionMap, "voice_num", ""));
									binder.setDeviceVersionType(StringUtil.getStringValue(deviceVersionMap, "device_version_type", ""));
									
									String gbbroadband =StringUtil.getStringValue(deviceVersionMap, "gigabit_port", "");
									String isPlugin = StringUtil.getStringValue(deviceVersionMap, "is_security_plugin", "");
									String pluginType = "";

									binder.setGbbroadband(gbbroadband); 
									binder.setIs_security_plugin(isPlugin);
									binder.setSecurity_plugin_type("");
									if(!StringUtil.IsEmpty(isPlugin) &&"1".equals(isPlugin)){
										 pluginType = StringUtil.getStringValue(deviceVersionMap, "security_plugin_type", "");
										if(!StringUtil.IsEmpty(pluginType) && !"0".equals(pluginType)){
											binder.setSecurity_plugin_type(pluginType);
										}
									}
									logger.warn("servicename[BindInfoService]gbbroadband[{}],isPlugin[{}],pluginType[{}]",
											new Object[]{gbbroadband,isPlugin,pluginType});
								}
							}
							
						}
						
						binder.setResult(0);
						binder.setResultDesc("成功");
						// 属地匹配
						// 根据逻辑SN查询用户的所有业务账号，并用'|||'隔开返回
					}
				}
			}
		}
		else
		{
			// 查询用户信息 考虑属地因素
			Map<String, String> userInfoMap = userDevDao.queryUserInfo(
					binder.getUserInfoType(), binder.getUserInfo(), binder.getCityId());
			if (null == userInfoMap || userInfoMap.isEmpty())
			{
				logger.warn(
						"servicename[BindInfoService]cmdId[{}]userinfo[{}]查无此用户",
						new Object[] { binder.getCmdId(), binder.getUserInfo()});
				binder.setResult(1002);
				binder.setResultDesc("查无此用户");
				return binder.getReturnXml();
			}
			else
			{
				String username = StringUtil.getStringValue(userInfoMap.get("username"));
				String userCityId = StringUtil.getStringValue(userInfoMap.get("city_id"));
				String userDevId = StringUtil.getStringValue(userInfoMap.get("device_id"));
				String deviceSn = StringUtil.getStringValue(userInfoMap.get("device_serialnumber"));
				String userline = StringUtil.getStringValue(userInfoMap.get("userline"));
				// 江西是根据city_id参数模糊匹配找出的数据,所以没必要在验证city_id
				if (!"xj_dx".equals(Global.G_instArea) && !"jx_dx".equals(Global.G_instArea)
						&& !"cq_dx".equals(Global.G_instArea)&& false == serviceHandle.cityMatch(binder.getCityId(), userCityId))
				{
					// 属地不匹配
					logger.warn(
							"servicename[BindInfoService]cmdId[{}]userinfo[{}]属地不匹配",
							new Object[] { binder.getCmdId(), binder.getUserInfo()});
					binder.setResult(1007);
					binder.setResultDesc("属地非法");
					return binder.getReturnXml();
					
				}
				else if (StringUtil.IsEmpty(userDevId))
				{
					// 未绑定设备
					logger.warn(
							"servicename[BindInfoService]cmdId[{}]userinfo[{}]用户未绑定设备",
							new Object[] { binder.getCmdId(), binder.getUserInfo()});
					binder.setResult(1003);
					binder.setResultDesc("未绑定设备");
					return binder.getReturnXml();
					
					// } else if (!userDevId.equals(devInfoMap.get("device_id"))) {
					// // 绑定关系不匹配
					// logger.warn("绑定关系不匹配：" + binder.getUserInfo());
					// binder.setResult(1008);
					// binder.setResultDesc("绑定关系不匹配");
				}
				else
				{
					// 获取设备详细信息
					Map<String, String> devTypeInfoMap = userDevDao.getDeviceTypeInfo(userDevId);
					if (devTypeInfoMap != null && !devTypeInfoMap.isEmpty())
					{
						binder.setVendor(StringUtil.getStringValue(devTypeInfoMap.get("vendor_name")));
						binder.setHandwareVersion(StringUtil.getStringValue(devTypeInfoMap.get("hardwareversion")));
						binder.setSoftwareVersion(StringUtil.getStringValue(devTypeInfoMap.get("softwareversion")));
						binder.setDevModel(StringUtil.getStringValue(devTypeInfoMap.get("device_model")));
						binder.setIp(StringUtil.getStringValue(devTypeInfoMap.get("loopback_ip")));
						
						binder.setIsNormal(StringUtil.getStringValue(devTypeInfoMap, "is_normal",""));
						binder.setAccessStyleRelayId(StringUtil.getStringValue(devTypeInfoMap, "access_style_relay_id",""));
						binder.setIpModelType(StringUtil.getStringValue(devTypeInfoMap, "ip_model_type",""));
						binder.setSpecId(StringUtil.getStringValue(devTypeInfoMap, "spec_id",""));
						binder.setMbbroadband(StringUtil.getStringValue(devTypeInfoMap, "mbbroadband",""));
						// JXDX-ITMS-REQ-20170412-LINBX-001(ITMS平台与CRM实时查询光猫设备序列号信息接口）
						if ("jx_dx".equals(Global.G_instArea)) {
							binder.setDevSn(StringUtil.getStringValue(devTypeInfoMap, "device_name", ""));
						}
						
						// XJDX-REQ-20180526-laijun-001
						if("xj_dx".equals(Global.G_instArea)){
							binder.setCpeMac(StringUtil.getStringValue(devTypeInfoMap, "cpe_mac", ""));
							String time = devTypeInfoMap.get("complete_time");
							if(!StringUtil.IsEmpty(time)){
								long register_time = StringUtil.getLongValue(devTypeInfoMap.get("complete_time"));
								
								DateTimeUtil dtu = new DateTimeUtil(register_time * 1000);
								int devYear = dtu.getYear();
								int currYear = new DateTimeUtil(Calendar.getInstance()).getYear();
								
								logger.warn("设备注册年份[{}],当前时间年份[{}]",new Object[]{devYear,currYear});
								if(currYear - devYear > 8){
									binder.setIsOld("1");
								}else{
									binder.setIsOld("0");
								}
							}else{
								binder.setIsOld("0");
							}
							
							if(!"BBMS".equals(Global.SYSTEM_NAME)){
								String netuser = StringUtil.getStringValue(devTypeInfoMap.get("username"));
								String scrapDev = StringUtil.getStringValue(devTypeInfoMap.get("scrap_dev"));
								if(StringUtil.IsEmpty(netuser) && StringUtil.IsEmpty(scrapDev)){
									binder.setGetPast("0");//正常终端
								}else{
									binder.setGetPast("1");//报废终端
								}
							}
							
							String deviceTypeId = StringUtil.getStringValue(devTypeInfoMap, "devicetype_id", "");
							if(!StringUtil.IsEmpty(deviceTypeId)){
								Map<String,String> deviceVersionMap = userDevDao.getDeviceVersionInfo(deviceTypeId);
								if(null != deviceVersionMap && !deviceVersionMap.isEmpty()){
									binder.setIsWifi(StringUtil.getStringValue(deviceVersionMap, "wifi", ""));
									binder.setLanNum(StringUtil.getStringValue(deviceVersionMap, "lan_num", ""));
									binder.setVoiceNum(StringUtil.getStringValue(deviceVersionMap, "voice_num", ""));
									binder.setDeviceVersionType(StringUtil.getStringValue(deviceVersionMap, "device_version_type", ""));
									
									String gbbroadband =StringUtil.getStringValue(deviceVersionMap, "gigabit_port", "");
									String isPlugin = StringUtil.getStringValue(deviceVersionMap, "is_security_plugin", "");
									String pluginType = "";

									binder.setGbbroadband(gbbroadband); 
									binder.setIs_security_plugin(isPlugin);
									binder.setSecurity_plugin_type("");
									if(!StringUtil.IsEmpty(isPlugin) &&"1".equals(isPlugin)){
										 pluginType = StringUtil.getStringValue(deviceVersionMap, "security_plugin_type", "");
										if(!StringUtil.IsEmpty(pluginType) && !"0".equals(pluginType)){
											binder.setSecurity_plugin_type(pluginType);
										}
									}
									logger.warn("servicename[BindInfoService]gbbroadband[{}],isPlugin[{}],pluginType[{}]",
											new Object[]{gbbroadband,isPlugin,pluginType});
								}
							}
						}
					}
					if("hb_dx".equals(Global.G_instArea)){
						// 是否支持千兆宽带
						String gbbroadband = StringUtil.getStringValue(devTypeInfoMap, "gbbroadband","");
						if("1".equals(gbbroadband)){
							binder.setGbbroadband("1");
						}else{
							binder.setGbbroadband("0");
						}
						
						List<HashMap<String,String>> viopProtocolList=userDevDao.getVoipProtocol(userDevId);
						String protocol="";
						if(null!=viopProtocolList&&viopProtocolList.size()>0){
							for(HashMap<String,String> map:viopProtocolList){
								protocol=protocol+StringUtil.getStringValue(map,"server_type")+"|";
							}
							//去掉最后的'|'
							binder.setProtocol(protocol.substring(0, protocol.length()-1));
						}
					}
					// JXDX-ITMS-REQ-20170412-LINBX-001(ITMS平台与CRM实时查询光猫设备序列号信息接口）
					if ("jx_dx".equals(Global.G_instArea)) {
						binder.setBindTime(formateTime(userInfoMap.get("updatetime")));
					}
					binder.setResult(0);
					binder.setResultDesc("成功");
					// 属地匹配
					// 根据逻辑SN查询用户的所有业务账号，并用'|||'隔开返回
					binder.setUsername(userDevDao.getAllUsername(username));
					binder.setDeviceSn(deviceSn);
					binder.setBindType(userline);
					binder.setOui(StringUtil.getStringValue(userInfoMap.get("oui")));
				}
			}
		}
		String returnXml = binder.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(binder, binder.getUserInfo(), "BindInfoService");
		logger.warn(
				"servicename[BindInfoService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { binder.getCmdId(), binder.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}
	
	/**
	 * 格式化时间
	 * @param time
	 * @return
	 */
	private String formateTime(String time) {
		if (null == time || "".equals(time)) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(Long.parseLong(time) * 1000);
	}
}
