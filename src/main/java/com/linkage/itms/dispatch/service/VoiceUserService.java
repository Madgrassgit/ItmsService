package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.VoiceUserServiceChecker;

/**
 * JSDX_ITMS-REQ-20170113-WJY-001（语音数图零配置管理新增功能)
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2017年2月9日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 
 */
public class VoiceUserService implements IService {
	/** 日志 */
	private static Logger logger = LoggerFactory
			.getLogger(VoiceUserService.class);

	@Override
	public String work(String inXml) {
		logger.warn("VoiceUserService==>inParam({})", inXml);
		VoiceUserServiceChecker checker = new VoiceUserServiceChecker(inXml);

		// 验证入参
		if (false == checker.check()) {
			logger.warn(
					"VoiceUserService:入参验证没通过,UserInfoType=[{}],UserName=[{}]",
					new Object[] { checker.getUserInfoType(),
							checker.getUserInfo() });

			logger.warn("VoiceUserService==>returnParam="
					+ checker.getReturnXml());
			return checker.getReturnXml();
		}

		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ArrayList<HashMap<String, String>> userMapList = userDevDao
				.queryUserInfoList(checker.getUserInfoType(),
						checker.getUserInfo(), checker.getCityId());
		if (null == userMapList || userMapList.isEmpty()) {
			logger.warn(
					"servicename[VoiceUserService]cmdId[{}]userinfo[{}]无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("无此客户信息");
			checker.setFailureReason("1");
			checker.setSuccStatus("-1");
			checker.setiSVirtualNetwork("2");
			return checker.getReturnXml();
		}
		// 说明查询到了多个
		if (userMapList.size() != 1 && checker.getUserInfoType() != 1) {
			logger.warn(
					"servicename[VoiceUserService]cmdId[{}]userinfo[{}]查询到多个用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1006);
			checker.setResultDesc("账号对应多个用户，请根据Loid查询");
			return checker.getReturnXml();
		}
		// 正常
		HashMap<String, String> userMap = userMapList.get(0);
		String userId = userMap.get("user_id");
		// 根据用户id去语言参数表里查询是否虚拟网用户
		ServUserDAO servUserDAO = new ServUserDAO();
		List<HashMap<String, String>> voipList = servUserDAO
				.getVoipPhone(userId);
		if (null == voipList || voipList.isEmpty()) {
			logger.warn(
					"servicename[VoiceUserService]cmdId[{}]userinfo[{}]没有语音业务",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(4);
			checker.setResultDesc("没有语音业务");
			checker.setFailureReason("2");
			checker.setSuccStatus("-1");
			checker.setiSVirtualNetwork("2");
			return checker.getReturnXml();
		}
		String digit_map = StringUtil.getStringValue(voipList.get(0),
				"digit_map", "");
		if (StringUtil.IsEmpty(digit_map)) {
			checker.setResult(0);
			checker.setResultDesc("成功");
			checker.setFailureReason("");
			checker.setSuccStatus("-1");
			checker.setiSVirtualNetwork("2");
		} else {
			checker.setResult(0);
			checker.setResultDesc("成功");
			checker.setFailureReason("");
			checker.setSuccStatus("1");
			checker.setiSVirtualNetwork("1");
			checker.setDigitCornet(digit_map);
		}
		logger.warn(
				"servicename[VoiceUserService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						checker.getReturnXml() });
		return checker.getReturnXml();
	}

}
