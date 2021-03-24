package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dao.VoiceMapConfigurationDao;
import com.linkage.itms.dispatch.obj.VoiceMapConfigurationChecker;

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-11-22
 * @category com.linkage.itms.nmg.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 
 */
public class VoiceMapConfiguration implements IService {

	private static final Logger logger = LoggerFactory
			.getLogger(VoiceMapConfiguration.class);
	ArrayList<HashMap<String, String>> list = null;
	private UserDeviceDAO userDevDao = new UserDeviceDAO();
	private VoiceMapConfigurationDao digitDao = new VoiceMapConfigurationDao();

	@Override
	public String work(String inParam) {

		logger.warn("VoiceMapConfiguration==>inParam:" + inParam);
		VoiceMapConfigurationChecker checker = new VoiceMapConfigurationChecker(inParam);
		if (false == checker.check()) {
			logger.warn("获取语音数图配置接口，入参验证失败，UserInfoType=[{}],UserInfo=[{}]",
					new Object[] { checker.getUserInfoType() });
			logger.warn("VoiceMapConfiguration==>retParam={}", checker.getReturnXml());
			checker.setFailureReason("1");
			return checker.getReturnXml();
		}
		String deviceId = "";
		String userId = "";
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn(
					"serviceName[VoiceMapConfiguration]cmdId[{}]userinfo[{}]无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
			checker.setFailureReason("1");
			return checker.getReturnXml();
		} else {
			deviceId = StringUtil.getStringValue(userInfoMap, "device_id");
			userId = StringUtil.getStringValue(userInfoMap, "user_id");
			if (StringUtil.IsEmpty(deviceId)) {
				logger.warn(
						"serviceName[VoiceMapConfiguration]cmdId[{}]userinfo[{}]未绑定设备",
						new Object[] { checker.getCmdId(),
								checker.getUserInfo() });
				checker.setResult(1003);
				checker.setResultDesc("此用户未绑定设备");
				checker.setFailureReason("2");
				return checker.getReturnXml();
			}
		}
		
		// 根据userId去判断用户是否开通语音业务
		Map<String, String> voipMap = userDevDao.queryVoipByUserId(userId);
		if (null == voipMap || voipMap.isEmpty()) {
			logger.warn(
					"serviceName[VoiceMapConfiguration]cmdId[{}]userinfo[{}]无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(4);
			checker.setResultDesc("该设备无语音业务");
			checker.setFailureReason("4");
			return checker.getReturnXml();
		}
		int code = Integer.parseInt(checker.getDigitCornet()) +2;
		// 根据数图短号类型查询数图管理表中数图值
		String digitMapValue = StringUtil.getStringValue(digitDao.queryDigitMapByCode(Integer.toString(code)),"digit_map_value");

		if (null == digitMapValue ||"".equals(digitMapValue)) {
			logger.warn(
					"serviceName[VoiceMapConfiguration]cmdId[{}]userinfo[{}]无此数图模型",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(3);
			checker.setResultDesc("无此数图模型");
			checker.setFailureReason("3");
			return checker.getReturnXml();
		}
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		String[] deviceIds = deviceId.split(",");
		String[] paramArr = new String[1];
		paramArr[0] = digitMapValue;
		boolean flag2 = CreateObjectFactory.createPreProcess().processDeviceStrategy(deviceIds, "7", paramArr);
		if (flag2 == false){
			logger.warn("下发策略失败");
			checker.setResult(1005);
			checker.setResultDesc("下发策略失败");
			checker.setFailureReason("3");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		// 设备不在线
		if (1 != flag) {
			logger.warn("设备不在线，无法下发数图模型");
			checker.setResult(1005);
			checker.setResultDesc("设备不在线，等下次下发");
			checker.setFailureReason("3");
			checker.setSuccStatus("1");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}else{
			// 设备在线 下发数图值到绑定设备参数节点中
			checker.setResult(0); 
			checker.setResultDesc("成功");
			checker.setFailureReason("0");
			checker.setSuccStatus("1");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
	}
}
