package com.linkage.itms.dispatch.cqdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import PreProcess.UserInfo;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.obj.ChangeBroadbandPasswordDealXML;

/**
 * 重庆电信修改宽带密码接口
 * @author hourui 76958
 * @version 1.0
 * @since 2017年11月19日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ChangeBroadbandPasswordService {

	private static Logger logger = LoggerFactory.getLogger(ChangeBroadbandPasswordService.class);

	//业务类型（宽带）
	private final String ServiceType = "10";
	
	//用户宽带帐号
	private final int USERINFOTYPE_1 =1;
	//LOID
	private final int USERINFOTYPE_2 =2;

	public String work(String inXml) {
		logger.warn("ChangeBroadbandPassword==>inXml({})",inXml);

		// 解析获得入参
		ChangeBroadbandPasswordDealXML dealXML = new ChangeBroadbandPasswordDealXML(inXml);

		// 验证入参
		if (null == dealXML.getXML(inXml)) {
			logger.warn("work==>inInXML="+dealXML.returnXML());
			logger.warn("入参验证没通过,ppp_username=[{}],logic_id=[{}],broadband_password=[{}]",
					new Object[] { dealXML.getPppUsename(), dealXML.getLogicId(),dealXML.getBroadband_password() });
			return dealXML.returnXML();
		}

		UserDeviceDAO userDevDao = new UserDeviceDAO();
		Map<String, String> userDevInfo  = null;
		if(!StringUtil.IsEmpty(dealXML.getPppUsename()))
		{
			userDevInfo = userDevDao.queryUserInfo(USERINFOTYPE_1, dealXML.getPppUsename(), null);
		}
		else if(!StringUtil.IsEmpty(dealXML.getLogicId()))
		{
			userDevInfo = userDevDao.queryUserInfo(USERINFOTYPE_2, dealXML.getLogicId(), null);
		}

		if (null == userDevInfo || userDevInfo.isEmpty()) {
			logger.warn("servicename[ChangeBroadbandPassword]cmdId[{}]logic_Id=[{}],ppp_username=[{}]查无此用户",
					new Object[] { dealXML.getOpId(), dealXML.getLogicId(), dealXML.getPppUsename()});
			dealXML.setResulltCode(-1);
			dealXML.setResultDesc("用户不存在");
			return dealXML.returnXML();
		}else{
			String deviceId = userDevInfo.get("device_id");
			String user_id = userDevInfo.get("user_id");

			if (StringUtil.IsEmpty(deviceId)) {
				// 未绑定设备
				logger.warn("servicename[ChangeBroadbandPassword]cmdId[{}]ppp_username[{}]broadband_password[{}]此客户未绑定",
						new Object[] { dealXML.getOpId(), dealXML.getPppUsename(),dealXML.getBroadband_password(), });
				dealXML.setResulltCode(-99);
				dealXML.setResultDesc("此用户未绑定设备");
				return dealXML.returnXML();
			}else{
				
				ArrayList<HashMap<String, String>> devList = userDevDao.qryDevId(deviceId);
				if (null == devList || devList.size()==0) {
					// 未绑定设备
					logger.warn("servicename[ChangeBroadbandPassword]查询不到设备信息,device_id="+deviceId);
					dealXML.setResulltCode(-99);
					dealXML.setResultDesc("此用户未绑定设备");
					return dealXML.returnXML();
				}
				
				String oui = devList.get(0).get("oui");
				String devSN = devList.get(0).get("device_serialnumber");
				
				// 1.查询此用户开通的业务信息
				Map<String, String> userServMap = userDevDao.queryServForNet(user_id);
				if (null == userServMap || userServMap.isEmpty())
				{
					// 没有开通业务
					logger.warn("servicename[updateNetPwd]cmdId[{}]ppp_username[{}]此用户没有开通任何宽带业务",
							new Object[] { dealXML.getOpId(), dealXML.getPppUsename() });
					dealXML.setResulltCode(-99);
					dealXML.setResultDesc("此用户没有开通任何宽带业务");
					return dealXML.returnXML();
				}
				//更改密码
				userDevDao.modCustomerPwd(user_id, dealXML.getPppUsename(), dealXML.getBroadband_password());

				//业务下发
				boolean res = serviceDoner(deviceId, user_id, oui, devSN);
				if(!res){
					logger.warn(
							"servicename[UpdateNetPwdService]cmdId[{}]loid[{}]netUserName[{}]netPwd[{}]下发特定业务，调用后台预读模块失败，业务类型为：[{}]",
							new Object[] { dealXML.getOpId(), dealXML.getPppUsename(), dealXML.getBroadband_password() });
					dealXML.setResulltCode(-99);
					dealXML.setResultDesc("下发业务失败，请稍后重试");
					return dealXML.returnXML();
				}
			}
		}
		dealXML.setResulltCode(0);
		dealXML.setResultDesc("执行成功");
		logger.warn("servicename[ChangeBroadbandPasswordService]cmdId[{}]loid[{}]ppp_username[{}]broadband_password[{}]执行成功:{}",
				new Object[] { dealXML.getOpId(), dealXML.getPppUsename(), dealXML.getBroadband_password() });
		return dealXML.returnXML();

	}

	/**
	 * 业务下发
	 * @param deviceId 设备编码
	 * @param user_id 用户ID
	 * @param oui 设备OUI
	 * @param devSN 设备SN
	 * @return 下发结果
	 */
	private boolean serviceDoner(String deviceId, String user_id, String oui,
			String devSN) {
		logger.warn("UpdateNetPwdService==>serviceDoner({})",new Object[]{deviceId,user_id,oui,devSN});
		boolean res = false;

		ServUserDAO servUserDao = new ServUserDAO();

		// 更新业务用户表的业务开通状态
		servUserDao.updateServOpenStatus(StringUtil.getLongValue(user_id),StringUtil.getIntegerValue(ServiceType));
		// 预读调用对象
		PreServInfoOBJ preInfoObj = new PreServInfoOBJ(user_id, deviceId, oui, devSN, ServiceType, "1");
		if (1 == CreateObjectFactory.createPreProcess().processServiceInterface(CreateObjectFactory.createPreProcess()
				.GetPPBindUserList(preInfoObj)))
		{
			res = true;
		}

		return res;
	}


	public UserInfo GetPPBindUserList(PreServInfoOBJ preInfoObj)
	{
		logger.debug("GetScheduleSQLList({})", preInfoObj);
		UserInfo uinfo = new UserInfo();
		uinfo.userId = StringUtil.getStringValue(preInfoObj.getUserId());
		uinfo.deviceId = StringUtil.getStringValue(preInfoObj.getDeviceId());
		uinfo.oui = StringUtil.getStringValue(preInfoObj.getOui());
		uinfo.deviceSn = StringUtil.getStringValue(preInfoObj.getDeviceSn());
		uinfo.gatherId = StringUtil.getStringValue(preInfoObj.getGatherId());
		uinfo.servTypeId = StringUtil.getStringValue(preInfoObj.getServTypeId());
		uinfo.operTypeId = StringUtil.getStringValue(preInfoObj.getOperTypeId());
		return uinfo;
	}


}
