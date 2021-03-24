package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dao.VXLANUserDeviceDAO;
import com.linkage.itms.dispatch.obj.OpenVXLANChecker;

/**
 * 
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-11-28
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class OpenVXLANService implements IService 
{
	private static final Logger logger = LoggerFactory.getLogger(OpenVXLANService.class);

	@Override
	public String work(String inParam) {
		logger.warn("OpenVXLANService==>inParam:" + inParam);
		OpenVXLANChecker checker = new OpenVXLANChecker(inParam);
		if (false == checker.check()) {
			logger.warn("检查工单的接口，入参验证失败，UserInfoType=[{}]，UserInfo=[{}]",
					new Object[] { checker.getUserInfoType(), checker.getUserInfo() });
			logger.warn("OpenVXLANService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		long userId = 0L;
		VXLANUserDeviceDAO vxlanUserDeviceDAO = new VXLANUserDeviceDAO();
		ArrayList<HashMap<String, String>> userInfoList = vxlanUserDeviceDAO
				.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoList || userInfoList.isEmpty()) {
			logger.warn("servicename[OpenVXLANService]cmdId[{}]userinfo[{}]查无此用户",
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
		int vxlanconfigsequence = 0;
		vxlanconfigsequence = vxlanUserDeviceDAO.querySequence(checker);
		checker.setvXLANConfigSequence(vxlanconfigsequence);
		vxlanconfigsequence = vxlanUserDeviceDAO.querySequenceBytunnelkey(checker);
		if(vxlanconfigsequence > 0)
		{
			logger.warn("servicename[OpenVXLANService]cmdId[{}]，userinfo[{}]，userId[{}] 输入tunnel_key已存在，覆盖通道序列号为[{}]",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getUserId() ,vxlanconfigsequence});
			checker.setvXLANConfigSequence(vxlanconfigsequence);
		}
		else
		{
			logger.warn("servicename[OpenVXLANService]cmdId[{}]，userinfo[{}]，userId[{}] 未输入通道序列号，系统生成通道序列号为[{}]",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getUserId() ,checker.getvXLANConfigSequence()});
		}
		//校验vlanid
		/*
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
		if(vxlanUserDeviceDAO.hasSheet(checker)){
			logger.warn("servicename[OpenVXLANService]cmdId[{}]，userinfo[{}]，userId[{}]工单已经存在，直接更新",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getUserId() });
			//更新工单
			int res = vxlanUserDeviceDAO.updateSheet(checker);
			if(res < 0){
				logger.warn("servicename[OpenVXLANService]cmdId[{}]userinfo[{}]处理结束，更新工单失败",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1000);
				checker.setResultDesc("更新工单失败");
				return checker.getReturnXml();
			}else{
				logger.warn("servicename[OpenVXLANService]cmdId[{}]userinfo[{}]处理结束，更新工单成功",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
			}
		}else{
			//记录工单
			int res = vxlanUserDeviceDAO.saveSheet(checker);
			if(res<0){
				logger.warn("servicename[OpenVXLANService]cmdId[{}]userinfo[{}]处理结束，保存工单失败",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1000);
				checker.setResultDesc("保存工单失败");
				return checker.getReturnXml();
			}else{
				logger.warn("servicename[OpenVXLANService]cmdId[{}]userinfo[{}]处理结束，保存工单成功",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
			}
		}
		if (true != CreateObjectFactory.createPreProcess()
					.processDeviceStrategy(new String[]{checker.getDeviceId()},"2901",new String[]{"29", userId + "", checker.getUserInfo(), String.valueOf(checker.getvXLANConfigSequence())})) {
				logger.warn("servicename[OpenVXLANService]cmdId[{}]userinfo[{}]设备[{}]业务下发，调用配置模块失败",
						new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getDeviceId(), checker.getvXLANConfigSequence()});
				checker.setResult(1000);
				checker.setResultDesc("未知错误，请稍后重试");
		}
		logger.warn("servicename[OpenVXLANService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:[{}]",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml()});
		return checker.getReturnXml();
	}
}
