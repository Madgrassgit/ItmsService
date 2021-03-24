package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryDeviceInfoChecker;

/**
 * call方法的业务处理类
 * 
 * @author Jason(3412)
 * @date 2010-6-17
 */
public class QueryDeviceInfoService implements IService{

	private static Logger logger = LoggerFactory.getLogger(QueryDeviceInfoService.class);

	/* 
	 * 查询电信维护密码工作方法
	 */
	@Override
	public String work(String inXml) {
		logger.warn("QueryDeviceInfoService inParam:[{}]",inXml);
		//检查合法性
		QueryDeviceInfoChecker checker = new QueryDeviceInfoChecker(inXml);
		if(false == checker.check()){
			logger.error(
					"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUsername(),
							checker.getReturnXml() });
			logger.warn("QueryDeviceInfoService returnParam:[{}]",checker.getReturnXml());
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUsername(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		//获取用户帐号 or 终端序列号
		if(1 == checker.getSearchType()){
			//河北的通过用户账号查询sn，再去设备表查询设备信息
			if ("hb_lt".equals(Global.G_instArea)){
				Map<String, String> userMap = userDevDao.qryDeviceSerial(checker.getUsername());
				if(null == userMap || userMap.isEmpty()){
					logger.warn(
							"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]查无此用户",
							new Object[] { checker.getCmdId(), checker.getUsername()});
					checker.setResult(1002);
					checker.setResultDesc("查无此客户");
				}else{
					String sn = userMap.get("device_serialnumber");
					if(StringUtil.IsEmpty(sn)){
						logger.warn(
								"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]未绑定设备",
								new Object[] { checker.getCmdId(), checker.getUsername()});
						checker.setResult(1003);
						checker.setResultDesc("未绑定设备");
					}else{
							checker.setResult(0);
							checker.setResultDesc("成功");
							
							Map<String, String> devMap = userDevDao.getDevInfoBySn(sn);
							if(null == devMap || devMap.isEmpty())
							{
								logger.warn(
										"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]用户未绑定设备",
										new Object[] { checker.getCmdId(), checker.getUsername()});
								checker.setResult(1003);
								checker.setResultDesc("未绑定设备");
							}
							else
							{
								checker.setDeviceSn(StringUtil.getStringValue(devMap, "device_serialnumber"));
								checker.setOui(StringUtil.getStringValue(devMap, "oui"));
								checker.setCityId(StringUtil.getStringValue(devMap, "city_id"));
							}
//						}
					}
				}
			}
			else{
				//根据用户帐号获取
//				Map<String, String> userMap = userDevDao.getTelePasswdByUsername(checker.getUsername());
				Map<String, String> userMap = userDevDao.queryUserInfo(checker.getUserInfoType(),checker.getUsername());
				if(null == userMap || userMap.isEmpty()){
					logger.warn(
							"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]查无此用户",
							new Object[] { checker.getCmdId(), checker.getUsername()});
					checker.setResult(1002);
					checker.setResultDesc("查无此客户");
				}else{
					String deviceId = StringUtil.getStringValue(userMap.get("device_id"));
					String userCityId = userMap.get("city_id");
					if(StringUtil.IsEmpty(deviceId)){
						logger.warn(
								"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]未绑定设备",
								new Object[] { checker.getCmdId(), checker.getUsername()});
						checker.setResult(1003);
						checker.setResultDesc("未绑定设备");
					}else{
//						if (false == serviceHandle.cityMatch(checker
//								.getCityId(), userCityId)) {// 属地不匹配
//							logger.warn("属地不匹配 查无此用户："
//									+ checker.getUsername());
//							checker.setResult(1003);
//							checker.setResultDesc("查无此用户");
//						} else {// 属地匹配
							checker.setResult(0);
							checker.setResultDesc("成功");
							
							Map<String, String> devMap = userDevDao.getTelePasswdByUsername(userMap.get("user_id"));
							if(null == devMap || devMap.isEmpty())
							{
								logger.warn(
										"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]用户未绑定设备",
										new Object[] { checker.getCmdId(), checker.getUsername()});
								checker.setResult(1003);
								checker.setResultDesc("未绑定设备");
							}
							else
							{
								checker.setDeviceSn(StringUtil.getStringValue(devMap, "device_serialnumber"));
								checker.setOui(StringUtil.getStringValue(devMap, "oui"));
								checker.setOnline(StringUtil.getStringValue(devMap, "cpe_currentstatus"));
								checker.setAccessTypeId(StringUtil.getStringValue(userMap, "adsl_hl"));
								
								logger.warn("Global.G_instArea="+Global.G_instArea);
								if("cq_dx".equals(Global.G_instArea)){
									String device_id = StringUtil.getStringValue(devMap, "device_id");
									Map<String,String> devInfo = userDevDao.getDeviceTypeInfo(device_id);
									logger.warn("devInfo="+devInfo);
									if(null != devInfo){
										logger.warn("device_model="+StringUtil.getStringValue(devInfo, "device_model"));
										checker.setVendor(StringUtil.getStringValue(devInfo, "vendor_name"));
										checker.setSoftwareVersion(StringUtil.getStringValue(devInfo, "softwareversion"));
										checker.setDevModel(StringUtil.getStringValue(devInfo, "device_model"));
									}
									
									//如果在线，实时连接设备查询
									if("1".equals(checker.getOnline())){
										ACSCorba acsCorba = new ACSCorba();
										GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
										// 判断设备是否在线
										int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
										if(1!=flag){
											checker.setOnline("2");
										}
									}
								}
							}
//						}
					}
				}
			}
		}else if (2 == checker.getSearchType()){
			//根据终端序列号
			ArrayList<HashMap<String,String>> devlsit = userDevDao.getTelePasswdByDevSn(checker.getDevSn());
			if(null == devlsit || devlsit.isEmpty()){
				logger.warn(
						"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]无此设备",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1004);
				checker.setResultDesc("查无此设备");
			}else if(devlsit.size() > 1){
				logger.warn(
						"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]查询到多台设备",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
			}else{
				Map<String, String> devMap = devlsit.get(0);
//				String deviceCityId = devMap.get("city_id");
//				if (false == serviceHandle.cityMatch(
//						checker.getCityId(), deviceCityId)) {// 属地不匹配
//					logger.warn("属地不匹配 查无此设备：" + checker.getDevSn());
//					checker.setResult(1005);
//					checker.setResultDesc("查无此设备");
//				} else {// 属地匹配
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setDeviceSn(StringUtil.getStringValue(devMap, "device_serialnumber"));
					checker.setOui(StringUtil.getStringValue(devMap, "oui"));
					checker.setCityId(StringUtil.getStringValue(devMap, "city_id"));
					checker.setOnline(StringUtil.getStringValue(devMap, "cpe_currentstatus"));
					checker.setAccessTypeId(StringUtil.getStringValue(devMap, "adsl_hl"));
//				}
			}
		}
		String returnXml = checker.getReturnXml();
		//记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(), "QueryDeviceInfoService");
		logger.warn(
				"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
		
		//回单
		return returnXml;
	}
	
	
}
