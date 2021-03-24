package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.dao.IpsecUserDeviceDAO;
import com.linkage.itms.dispatch.obj.UpdateIpsecVPNChecker;

/**
 * ITMS+向翼翮提供的业务修改工单的接口
 * 
 * @param 综调接口XML字符串参数
 * @author chenxj6
 * @date 2017-10-19
 * @return String 回参的XML字符串
 */
public class UpdateIpsecVPNService implements IService {

	private static final Logger logger = LoggerFactory.getLogger(UpdateIpsecVPNService.class);

	@Override
	public String work(String inParam) {
		logger.warn("UpdateIpsecVPNService==>inParam:" + inParam);
		UpdateIpsecVPNChecker checker = new UpdateIpsecVPNChecker(inParam);
		if (false == checker.check()) {
			logger.warn("检查ITMS+向翼翮提供的业务修改工单的接口，入参验证失败，UserInfoType=[{}]，UserInfo=[{}]",
					new Object[] { checker.getUserInfoType(), checker.getUserInfo() });
			logger.warn("UpdateIpsecVPNService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		long userId = 0L;
		IpsecUserDeviceDAO ipsecUserDeviceDAO = new IpsecUserDeviceDAO();
		ArrayList<HashMap<String, String>> userInfoList = null;
		
		if(1==checker.getUserInfoType()){
			userInfoList = ipsecUserDeviceDAO.queryUserInfo(1, checker.getUserInfo());
		}else if(2==checker.getUserInfoType()){
			userInfoList = ipsecUserDeviceDAO.queryUserInfo(2, checker.getUserInfo());
		}
		
		if (null == userInfoList || userInfoList.isEmpty()) {
			logger.warn("servicename[UpdateIpsecVPNService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(6);
			checker.setResultDesc("查询不到对应用户");
			return checker.getReturnXml();
		}
		String deviceId =  StringUtil.getStringValue(userInfoList.get(0), "device_id");
		if (StringUtil.IsEmpty(deviceId)) {
			checker.setResult(7);
			checker.setResultDesc("查询不到对应网关");
			return checker.getReturnXml();
		}
		
		
		StringBuffer loidPrev = new StringBuffer();
		int i = 0;
		for (HashMap<String, String> m : userInfoList) {
			if (i == 0) {
				i ++;
				continue;
			}
			loidPrev.append(StringUtil.getStringValue(m, "username"));
			loidPrev.append(";");
		}
		// LoidPrev 先将loidPrev设置为空
//		checker.setLoidPrev("");
		checker.setLoidPrev(loidPrev.toString());
		userId = StringUtil.getLongValue(StringUtil.getStringValue(userInfoList.get(0), "user_id"));
		// Loid
		checker.setLoid(StringUtil.getStringValue(userInfoList.get(0), "username"));
		checker.setNetUsername(StringUtil.getStringValue(userInfoList.get(0), "netusername"));
		checker.setUserId(userId);
		checker.setDeviceId(deviceId);
		if (!ipsecUserDeviceDAO.hasSheet(checker)) {
			logger.warn("servicename[UpdateIpsecVPNService]cmdId[{}]userinfo[{}]未开通IPSecVPN业务",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(9);
			checker.setResultDesc("未开通IPSecVPN业务");
			return checker.getReturnXml();
		}
		
		//更新工单
		int res = ipsecUserDeviceDAO.updateSheet(checker);
		if(res < 0){
			logger.warn("servicename[UpdateIpsecVPNService]cmdId[{}]userinfo[{}]处理结束，修改工单失败",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1000);
			checker.setResultDesc("更新工单失败");
			return checker.getReturnXml();
		}else{
			logger.warn("servicename[UpdateIpsecVPNService]cmdId[{}]userinfo[{}]处理结束，修改工单成功",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
		}
//		if (false == CheckStrategyUtil.chechStrategy(checker.getDeviceId())) {
//			logger.warn("servicename[UpdateIpsecVPNService]cmdId[{}]userinfo[{}]设备繁忙或者业务正在下发，请稍候重试",
//					new Object[] { checker.getCmdId(), checker.getUserInfo() });
//			checker.setResult(1009);
//			checker.setResultDesc("设备繁忙或者业务正在下发，请稍候重试");
//		} else {
			if (true != CreateObjectFactory.createPreProcess()
					.processDeviceStrategy(new String[]{checker.getDeviceId()},"2701",new String[]{"27", userId + "", checker.getUserInfo()})) {
					logger.warn("servicename[UpdateIpsecVPNService]cmdId[{}]userinfo[{}]设备[{}]业务下发，调用配置模块失败",
							new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getDeviceId() });
					checker.setResult(1000);
					checker.setResultDesc("未知错误，请稍后重试");
			}
//		}
		logger.warn("servicename[UpdateIpsecVPNService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:[{}]",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml()});
		return checker.getReturnXml();
	}
}
