package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.ActivationOfVoiceDAO;
import com.linkage.itms.dispatch.obj.ActivationOfVoiceChecker;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-10-17
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ActivationOfVoiceService
{
	private static Logger logger = LoggerFactory.getLogger(ActivationOfVoiceService.class);
	public String work(String inXml)
	{
		logger.warn("ActivationOfVoiceService==>inXml({})",inXml);
		ActivationOfVoiceChecker checker = new ActivationOfVoiceChecker(inXml);
		if (!checker.check()) {
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		List<HashMap<String, String>> userMapList = null;
		List<HashMap<String, String>> useridList = null;
		ActivationOfVoiceDAO dao = new ActivationOfVoiceDAO();
		useridList = dao.queryUserid(checker.getUserType(), String.valueOf(checker.getUserInfoType()), checker.getUserInfo());
		if (useridList == null || useridList.isEmpty()) {
			logger.warn(
					"servicename[ActivationOfVoiceService]UserInfoType[{}]userinfo[{}]无此客户信息",
					new Object[] { checker.getUserInfoType(), checker.getUserInfo()});
			checker.setResult(1002);
			checker.setResultDesc("无此客户信息");
			return checker.getReturnXml();
		}
		if(StringUtil.IsEmpty(useridList.get(0).get("device_id"))) {
			logger.warn(
					"servicename[ActivationOfVoiceService]UserInfoType[{}]userinfo[{}]此用户未绑定",
					new Object[] { checker.getUserInfoType(), checker.getUserInfo()});
			checker.setResult(1004);
			checker.setResultDesc("此用户未绑定");
			return checker.getReturnXml();
		}
		userMapList = dao.queryUserInfo(useridList.get(0).get("device_id"),checker.getUserType());		
		if(userMapList == null || userMapList.isEmpty())
		{
			logger.warn(
					"servicename[ActivationOfVoiceService]UserType[{}]此用户未绑定",
					new Object[] { checker.getUserType()});
			checker.setResult(1004);
			checker.setResultDesc("此用户未绑定");
			return checker.getReturnXml();
		}
		
		checker.setResult(0);
		checker.setResultDesc("成功");
		checker.setVoiplist(useridList);
		checker.setDevicelist(userMapList);
		String returnXml = checker.getReturnXml();
		logger.warn("servicename[ActivationOfVoiceService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
		return returnXml;
	}
}
