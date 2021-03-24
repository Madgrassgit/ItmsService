package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.dao.VXLANUserDeviceDAO;
import com.linkage.itms.dispatch.obj.UpdateVXLANChecker;

/**
 * 
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-11-29
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class UpdateVXLANService implements IService
{
	private static final Logger logger = LoggerFactory.getLogger(UpdateVXLANService.class);

	@Override
	public String work(String inParam) {
		logger.warn("UpdateVXLANService==>inParam:" + inParam);
		UpdateVXLANChecker checker = new UpdateVXLANChecker(inParam);
		if (false == checker.check()) {
			logger.warn("检查ITMS+向翼翮提供的业务修改工单的接口，入参验证失败，UserInfoType=[{}]，UserInfo=[{}]",
					new Object[] { checker.getUserInfoType(), checker.getUserInfo() });
			logger.warn("UpdateVXLANService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		long userId = 0L;
		VXLANUserDeviceDAO vxlanUserDeviceDAO = new VXLANUserDeviceDAO();
		ArrayList<HashMap<String, String>> userInfoList = null;
		
		if(1==checker.getUserInfoType()){
			userInfoList = vxlanUserDeviceDAO.queryUserInfo(1, checker.getUserInfo());
		}else if(2==checker.getUserInfoType()){
			userInfoList = vxlanUserDeviceDAO.queryUserInfo(2, checker.getUserInfo());
		}
		
		if (null == userInfoList || userInfoList.isEmpty()) {
			logger.warn("servicename[UpdateVXLANService]cmdId[{}]userinfo[{}]查无此用户",
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
		checker.setLoidPrev(loidPrev.toString());
		userId = StringUtil.getLongValue(StringUtil.getStringValue(userInfoList.get(0), "user_id"));
		// Loid
		checker.setLoid(StringUtil.getStringValue(userInfoList.get(0), "username"));
		checker.setNetUsername(StringUtil.getStringValue(userInfoList.get(0), "netusername"));
		checker.setUserId(userId);
		checker.setDeviceId(deviceId);
		if (!vxlanUserDeviceDAO.hasSheet(checker)) {
			logger.warn("servicename[UpdateVXLANService]cmdId[{}]userinfo[{}]未开通VXLAN业务",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(9);
			checker.setResultDesc("未开通VXLAN业务");
			return checker.getReturnXml();
		}
		if(vxlanUserDeviceDAO.hasUpdateTunnelKeySheet(checker))
		{
			logger.warn("servicename[OpenVXLANService]cmdId[{}]，userinfo[{}]，userId[{}]TunnelKey值已经存在",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getUserId() });
			checker.setResult(8);
			checker.setResultDesc("TunnelKey值已经存在");
			return checker.getReturnXml();
		}
		/*
		//校验vlanid
		if(1 == checker.getWorkMode() || 3 == checker.getWorkMode())
		{
			if(vxlanUserDeviceDAO.queryVlanid(checker) > 0)
			{
				logger.warn("servicename[OpenVXLANService]cmdId[{}]，userinfo[{}]，userId[{}] 二层模式或混合模式下输入vlanid[{}]已存在",
						new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getUserId() ,checker.getXctcom_vlan()});
				checker.setResult(1000);
				checker.setResultDesc("输入vlanid已存在");
				return checker.getReturnXml();
			}
		}*/
		//更新工单
		int res = vxlanUserDeviceDAO.updateSheet(checker);
		if(res < 0){
			logger.warn("servicename[UpdateVXLANService]cmdId[{}]userinfo[{}]处理结束，修改工单失败",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1000);
			checker.setResultDesc("更新工单失败");
			return checker.getReturnXml();
		}else{
			logger.warn("servicename[UpdateVXLANService]cmdId[{}]userinfo[{}]处理结束，修改工单成功",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
		}
		if (true != CreateObjectFactory.createPreProcess()
				.processDeviceStrategy(new String[]{checker.getDeviceId()},"2901",new String[]{"29", userId + "", checker.getUserInfo(), String.valueOf(checker.getvXLANConfigSequence())})) {
				logger.warn("servicename[UpdateVXLANService]cmdId[{}]userinfo[{}]设备[{}]业务下发，调用配置模块失败",
						new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getDeviceId() });
				checker.setResult(1000);
				checker.setResultDesc("未知错误，请稍后重试");
		}
		logger.warn("servicename[UpdateVXLANService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:[{}]",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml(),checker.getvXLANConfigSequence()});
		
		return checker.getReturnXml();
	}
}
