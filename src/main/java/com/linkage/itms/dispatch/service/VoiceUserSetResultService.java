package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.VoiceUserSetResultChecker;

/**
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2017年2月13日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 
 */
public class VoiceUserSetResultService implements IService {

	private static Logger logger = LoggerFactory
			.getLogger(VoiceUserSetResultService.class);

	@Override
	public String work(String inXml) {
		logger.warn("VoiceUserSetResultService==>inParam({})", inXml);
		VoiceUserSetResultChecker checker = new VoiceUserSetResultChecker(inXml);

		// 验证入参
		if (false == checker.check()) {
			logger.warn(
					"VoiceUserSetResultService:入参验证没通过,UserInfoType=[{}],UserName=[{}]",
					new Object[] { checker.getUserInfoType(),
							checker.getUserInfo() });

			logger.warn("VoiceUserSetResultService==>returnParam="
					+ checker.getReturnXml());
			return checker.getReturnXml();
		}
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ArrayList<HashMap<String, String>> userMapList = userDevDao
				.queryUserInfoList(checker.getUserInfoType(),
						checker.getUserInfo(), checker.getCityId());
		if (null == userMapList || userMapList.isEmpty()) {
			logger.warn(
					"servicename[VoiceUserSetResultService]cmdId[{}]userinfo[{}]无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("无此客户信息");
			checker.setFailureReason("1");
			checker.setSuccStatus("-1");
			return checker.getReturnXml();
		}
		// 说明查询到了多个
		if (userMapList.size() != 1 && checker.getUserInfoType() != 1) {
			logger.warn(
					"servicename[VoiceUserSetResultService]cmdId[{}]userinfo[{}]查询到多个用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1006);
			checker.setResultDesc("账号对应多个用户，请根据Loid查询");
			return checker.getReturnXml();
		}
		// 正常
		HashMap<String, String> userMap = userMapList.get(0);
		String userId = StringUtil.getStringValue(userMap, "user_id", "");
		String deviceId = StringUtil.getStringValue(userMap, "device_id", "");
		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn(
					"servicename[VoiceUserSetResultService]cmdId[{}]userinfo[{}]用户未绑定设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1003);
			checker.setResultDesc("用户未绑定设备");
			checker.setFailureReason("4");
			checker.setSuccStatus("-1");
			return checker.getReturnXml();
		}
		// 根据用户id去语言参数表里查询是否虚拟网用户
		ServUserDAO servUserDAO = new ServUserDAO();
		List<HashMap<String, String>> voipList = servUserDAO
				.getVoipPhone(userId);
		if (null == voipList || voipList.isEmpty()) {
			logger.warn(
					"servicename[VoiceUserSetResultService]cmdId[{}]userinfo[{}]没有语音业务",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(4);
			checker.setResultDesc("没有语音业务");
			checker.setFailureReason("2");
			checker.setSuccStatus("-1");

			return checker.getReturnXml();
		}
		String digit_map = StringUtil.getStringValue(voipList.get(0),
				"digit_map", "");
		if (StringUtil.IsEmpty(digit_map)) {
			checker.setResult(1004);
			checker.setResultDesc("不是虚拟网语音用户");
			checker.setFailureReason("3");
			checker.setSuccStatus("-1");
		} else {
			// 根据设备获取业务策略最近一次下发结果
			Map<String, String> resultMap = servUserDAO.queryStrategyResult(
					deviceId, "1401");
			String resultId = StringUtil.getStringValue(resultMap, "result_id",
					"");
			// 根据短号获取配置数图值
			Map<String, String> map = servUserDAO.getDigitMapValue(digit_map);
			checker.setResult(0);
			checker.setResultDesc("成功");
			if (null != resultId && "1".equals(resultId)) {
				checker.setFailureReason("");
				checker.setSuccStatus("1");
			// 业务未成功下发	
			} else {
				checker.setFailureReason("5");
				checker.setSuccStatus("-1");
			}
			checker.setDigitCornet(digit_map);
			checker.setDigitMapValue(StringUtil.getStringValue(map,
					"digit_map_value", ""));
		}
		logger.warn(
				"servicename[VoiceUserSetResultService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						checker.getReturnXml() });
		return checker.getReturnXml();
	}
}
