package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.Map;

import com.linkage.itms.Global;
import com.linkage.itms.dao.HqosUserDeviceDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.HqosQueryVersionChecker;

/**
 * @author guxl3 (Ailk No.)
 * @version 1.0
 * @since 2021年2月2日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class HqosQueryVersionServiceImpl implements IService
{
	private static Logger logger = LoggerFactory.getLogger(HqosQueryVersionServiceImpl.class);
	HqosUserDeviceDAO userDevDao = new HqosUserDeviceDAO();
	private static Map<String, String> devTypeMap = new HashMap<String, String>();
	
	
	public HqosQueryVersionServiceImpl()
	{
		devTypeMap.put("", "其他");
		devTypeMap.put("0", "其他");
		devTypeMap.put("1", "e8-b");
		devTypeMap.put("2", "e8-c");
		devTypeMap.put("3", "Navigator1-2P");
		devTypeMap.put("4", "Navigator2-1P");
		devTypeMap.put("5", "A8-B");
		devTypeMap.put("6", "A8-C");
	}
	
	
	
	@Override
	public String work(String inXml)
	{
		String methodName="QueryVersion";
		String returnXml="";
		logger.warn("QueryVersion inXml:({})",getStr(inXml));
		
		HqosQueryVersionChecker checker = new HqosQueryVersionChecker(inXml);
		if (!checker.check()) 
		{
			returnXml= checker.getReturnXml();
			logger.warn("servicename[QueryVersion] cmdId[{}],userinfo[{}] 验证未通过，返回：{}",
					checker.getCmdId(),checker.getUserInfo(),getStr(inXml));
			new RecordLogDAO().recordDispatchLog(checker,1,checker.getUserInfo(),methodName);
			return returnXml;
		}
		int userInfoType = checker.getUserInfoType();
		Map<String, String> userInfoMap = getUserInfoMap(checker.getUserInfo(), userInfoType);

		if (userInfoMap==null || userInfoMap.isEmpty()) {
			logger.warn("servicename[QueryVersion] cmdId[{}],userinfo[{}] 查无此用户,返回：{}",
					checker.getCmdId(),checker.getUserInfo(),getStr(inXml));
			checker.setResult(1001);
			checker.setResultDesc("用户信息不存在");
			new RecordLogDAO().recordDispatchLog(checker,1,checker.getUserInfo(),methodName);
			return checker.getReturnXml();
		}
		
		String deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");
		String userId = StringUtil.getStringValue(userInfoMap, "user_id", "");
		
		if (StringUtil.IsEmpty(deviceId) || StringUtil.IsEmpty(userId)) {
			logger.warn("servicename[QueryVersion]cmdId[{}]userinfo[{}]未绑定设备,返回：{}", 
					checker.getCmdId(), checker.getUserInfo(),getStr(inXml));
			checker.setResult(1002);
			checker.setResultDesc("用户未绑定设备");
			new RecordLogDAO().recordDispatchLog(checker,1,checker.getUserInfo(),methodName);
			return checker.getReturnXml();
		}
		
		Map<String, String> map = userDevDao.queryVersion(deviceId);
		if (map!=null && !map.isEmpty())
		{
			checker.setMac(StringUtil.getStringValue(map,"cpe_mac",""));
			checker.setDevSn(StringUtil.getStringValue(map,"device_name",""));
			checker.setDevHardwareversion(StringUtil.getStringValue(map,"hardwareversion",""));
			checker.setDevName(StringUtil.getStringValue(map,"device_name",""));
			checker.setDevVendor(StringUtil.getStringValue(map,"vendor_add",""));
			checker.setDevModel(StringUtil.getStringValue(map,"device_model",""));
			checker.setDevSoftwareversion(StringUtil.getStringValue(map,"softwareversion",""));
			checker.setDeviceSpec(StringUtil.getStringValue(map,"lan_num","")+"+"+StringUtil.getStringValue(map,"wlan_num",""));
			String devType=StringUtil.getStringValue(map,"rela_dev_type_id","");
			checker.setDevType(devTypeMap.get(devType));
		}
		checker.setResult(0);
		checker.setResultDesc("成功");
		
		
		returnXml = checker.getReturnXml();
		new RecordLogDAO().recordDispatchLog(checker,0,checker.getUserInfo(),methodName);
		logger.warn("servicename[QueryVersion] cmdId[{}],userinfo[{}] 处理结束，返回响应信息:{}",
				 checker.getCmdId(),checker.getUserInfo(),returnXml);
		return returnXml;
	}
	
	
	
	
	private Map<String, String> getUserInfoMap(String userInfo,int userInfoType)
	{
		Map<String, String> userInfoMap=null;
		if (Global.USERTYPENAME==userInfoType)
		{
			userInfoMap=userDevDao.queryUserByNetAccount(userInfo);
		}else if (Global.USERTYPELOID==userInfoType) {
			userInfoMap=userDevDao.queryUserByLoid(userInfo);
		}else {
			userInfoMap = userDevDao.queryUserInfoByDevSn(userInfo);
		}
		return userInfoMap;
	}
	
	
	
	/**
	 * xml去换行
	 */
	private String getStr(String str)
	{
		if(!StringUtil.IsEmpty(str)){
			return str.replace("\n","");
		}
		return str;
	}
}
