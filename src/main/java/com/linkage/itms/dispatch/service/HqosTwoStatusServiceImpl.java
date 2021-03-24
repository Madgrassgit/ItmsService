package com.linkage.itms.dispatch.service;

import java.util.Map;

import com.linkage.itms.Global;
import com.linkage.itms.dao.HqosUserDeviceDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.HqosTwoStatusChecker;

/**
 * @author guxl3 (Ailk No.)
 * @version 1.0
 * @since 2021年2月2日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class HqosTwoStatusServiceImpl implements IService
{
	private static Logger logger = LoggerFactory.getLogger(HqosTwoStatusServiceImpl.class);
	HqosUserDeviceDAO userDevDao = new HqosUserDeviceDAO();
	
	@Override
	public String work(String inXml)
	{
		String methodName="HQoSTwoStatus";
		String returnXml="";
		logger.warn("HQoSTwoStatus inXml:({})",getStr(inXml));
		
		HqosTwoStatusChecker checker = new HqosTwoStatusChecker(inXml);
		if (!checker.check()) 
		{
			returnXml= checker.getReturnXml();
			logger.warn("servicename[HQoSTwoStatus] cmdId[{}],userinfo[{}] 验证未通过，返回：{}",
					checker.getCmdId(),checker.getUserInfo(),getStr(inXml));
			new RecordLogDAO().recordDispatchLog(checker,1,checker.getUserInfo(),methodName);
			return returnXml;
		}
		int userInfoType = checker.getUserInfoType();
		Map<String, String> userInfoMap = getUserInfoMap(checker.getUserInfo(), userInfoType);

		if (userInfoMap==null || userInfoMap.isEmpty()) {
			logger.warn("servicename[HQoSTwoStatus] cmdId[{}],userinfo[{}] 查无此用户,返回：{}",
					checker.getCmdId(),checker.getUserInfo(),getStr(inXml));
			checker.setResult(1001);
			checker.setResultDesc("用户信息不存在");
			new RecordLogDAO().recordDispatchLog(checker,1,checker.getUserInfo(),methodName);
			return checker.getReturnXml();
		}
		
		String userId = StringUtil.getStringValue(userInfoMap, "user_id", "");
		
		if (StringUtil.IsEmpty(userId)) {
			logger.warn("servicename[HQoSTwoStatus]cmdId[{}]userinfo[{}]未绑定设备,返回：{}", 
					checker.getCmdId(), checker.getUserInfo(),getStr(inXml));
			checker.setResult(1002);
			checker.setResultDesc("用户未绑定设备");
			new RecordLogDAO().recordDispatchLog(checker,1,checker.getUserInfo(),methodName);
			return checker.getReturnXml();
		}
		
		//用于记录接口日志
		int code=1;
		Map<String, String> map = userDevDao.hQosTwoStatus(userId);
		if (map!=null && !map.isEmpty())
		{	
			code=0;
			checker.setResult(10);
			checker.setResultDesc("QoS 保障已开通");
		}else {
			checker.setResult(11);
			checker.setResultDesc("QoS 保障未开通");
		}
		
		
		returnXml = checker.getReturnXml();
		new RecordLogDAO().recordDispatchLog(checker,code,checker.getUserInfo(),methodName);
		logger.warn("servicename[HQoSTwoStatus] cmdId[{}],userinfo[{}] 处理结束，返回响应信息:{}",
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
