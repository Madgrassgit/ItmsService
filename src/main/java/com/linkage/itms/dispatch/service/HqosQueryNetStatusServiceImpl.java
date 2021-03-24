package com.linkage.itms.dispatch.service;

import java.util.Map;

import com.linkage.itms.Global;
import com.linkage.itms.dao.HqosUserDeviceDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.HqosQueryNetStatusChecker;

/**
 * @author guxl3 (Ailk No.)
 * @version 1.0
 * @since 2021年2月2日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class HqosQueryNetStatusServiceImpl implements IService
{
	private static Logger logger = LoggerFactory.getLogger(HqosQueryNetStatusServiceImpl.class);
	HqosUserDeviceDAO userDevDao = new HqosUserDeviceDAO();
	
	@Override
	public String work(String inXml)
	{
		String methodName="QueryNetStatus";
		String returnXml="";
		logger.warn("QueryNetStatus inXml:({})",getStr(inXml));
		
		HqosQueryNetStatusChecker checker = new HqosQueryNetStatusChecker(inXml);
		if (!checker.check()) 
		{
			returnXml= checker.getReturnXml();
			logger.warn("servicename[QueryNetStatus] cmdId[{}],userinfo[{}] 验证未通过，返回：{}",
					checker.getCmdId(),checker.getUserInfo(),getStr(inXml));
			new RecordLogDAO().recordDispatchLog(checker,1,checker.getUserInfo(),methodName);
			return returnXml;
		}
		int userInfoType = checker.getUserInfoType();
		
		Map<String, String> userInfoMap = getUserInfoMap(checker.getUserInfo(), userInfoType);

		if (userInfoMap==null || userInfoMap.isEmpty()) {
			logger.warn("servicename[QueryNetStatus] cmdId[{}],userinfo[{}] 查无此用户,返回：{}",
					checker.getCmdId(),checker.getUserInfo(),getStr(inXml));
			if (Global.USERTYPENAME==userInfoType)
			{
				checker.setResult(14);
				checker.setResultDesc("输入的宽带帐号不存在");
			}else {
				checker.setResult(13);
				checker.setResultDesc("未溯源出客户宽带帐号 ");
			}
			
			new RecordLogDAO().recordDispatchLog(checker,1,checker.getUserInfo(),methodName);
			return checker.getReturnXml();
		}
		
		String deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");
		String userId = StringUtil.getStringValue(userInfoMap, "user_id", "");
		if (StringUtil.IsEmpty(deviceId) || StringUtil.IsEmpty(userId)) {
			logger.warn("servicename[QueryNetStatus]cmdId[{}]userinfo[{}]未绑定设备,返回：{}", 
					checker.getCmdId(), checker.getUserInfo(),getStr(inXml));
			checker.setResult(1002);
			checker.setResultDesc("用户未绑定设备");
			new RecordLogDAO().recordDispatchLog(checker,1,checker.getUserInfo(),methodName);
			return checker.getReturnXml();
		}
		String  userName = StringUtil.getStringValue(userInfoMap, "username", "");
		Map<String, String> map = userDevDao.queryNetStatus(deviceId,userName);
		//用于记录接口日志 0代表成功
		int code=1;
		if (map!=null && !map.isEmpty())
		{
			String username=StringUtil.getStringValue(map,"username","");
			String iscloudnet=StringUtil.getStringValue(map,"iscloudnet","");
			if (StringUtil.IsEmpty(username)) {
				checker.setResult(13);
				checker.setResultDesc("未溯源出客户宽带帐号");
			}else if(!"1".equals(iscloudnet)){
				checker.setResult(11);
				checker.setResultDesc("ONT设备型号不支持开通");
			}else {
				code=0;
				checker.setResult(10);
				checker.setResultDesc("可以开通QoS保障");
			}
			checker.setVendor(StringUtil.getStringValue(map,"vendor_add"));
			checker.setDevModel(StringUtil.getStringValue(map,"device_model"));
			checker.setUserName(username);
			
		}else {
			checker.setResult(13);
			checker.setResultDesc("未溯源出客户宽带帐号");
		}
		
		returnXml = checker.getReturnXml();
		new RecordLogDAO().recordDispatchLog(checker,code,checker.getUserInfo(),methodName);
		logger.warn("servicename[QueryNetStatus] cmdId[{}],userinfo[{}] 处理结束，返回响应信息:{}",
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
			userInfoMap=userDevDao.queryNetServUserByLoid(userInfo);
		}else {
			userInfoMap = userDevDao.queryNetServUserInfoByDevSn(userInfo);
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
