package com.linkage.itms.dispatch.cqdx.service;

import java.util.Map;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.DevReset;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.FactoryResetDealXML;

public class FactoryResetService implements Runnable{
	private static Logger logger = LoggerFactory.getLogger(FactoryResetService.class);
	
	private String message = "";
	
	public void setMessage(String message)
	{
		logger.debug("setMessage({})",message);
		this.message = message;
	}
	
	@Override
	public void run()
	{

		logger.warn("servicename[FactoryResetService]执行，入参为：{}", message);
		FactoryResetDealXML deal = new FactoryResetDealXML();
		Document document = deal.getXML(message);
		if (document == null) {
			logger.warn("servicename[FactoryResetService]解析入参错误！");
			deal.setResult("-99");
			deal.setErrMsg("解析入参错误！");
			deal.returnXML(deal.getOpId(),deal.getResult(),deal.getErrMsg());
			return;
		}
		
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String serialNumber = deal.getSerialNumber();
		
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		Map<String, String> userInfoMap = null;
		int userType = 1;
		String userInfo = "";
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			userType = 2;
			userInfo = logicId;
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			userType = 1;
			userInfo = pppUsename;
		}
		else if(!StringUtil.IsEmpty(serialNumber) && serialNumber.length()>=6) {
			// 设备sn
			userType = 6;
			userInfo = serialNumber;
		}
		else{
			logger.warn("servicename[FactoryResetService]解析入参错误！");
			deal.setResult("-1");
			deal.setErrMsg("解析入参错误！");
			deal.returnXML(deal.getOpId(),deal.getResult(),deal.getErrMsg());
			return;
		}
		userInfoMap = userDevDao.queryUserInfo(userType, userInfo, "");
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn("servicename[FactoryResetService]userinfo[{}]查无此用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户");
			deal.returnXML(deal.getOpId(),deal.getResult(),deal.getErrMsg());
			return;
		} 	
		long userId = StringUtil.getLongValue(userInfoMap.get("user_id"));
		String userDevId = userInfoMap.get("device_id");

		if (StringUtil.IsEmpty(userDevId)) {// 用户未绑定终端
			logger.warn("servicename[FactoryResetService]cmdId[{}]userinfo[{}]此客户未绑定", new Object[] { deal.getOpId(), userInfo});
			deal.setResult("-99");
			deal.setErrMsg("用户未绑定终端");
			deal.returnXML(deal.getOpId(),deal.getResult(),deal.getErrMsg());
		}else{ 
			 int flag = new GetDeviceOnLineStatus().testDeviceOnLineStatus(userDevId, new ACSCorba());
			 if(1 == flag){
				int irt  = 0;
				 
				/**
				 * 流程调整为:
				 * 1、调用者（WEB或者ItmsService模块），将需要恢复出厂的用户业务状态置成未做，调用配置模块。
				 * 2、配置模块根据未做的业务生成业务下发策略，通知acs恢复出厂
				 * 3、调用者判断恢复出厂如果是失败，将状态还原。
				 */
				userDevDao.updateCustStatus(userId);
				irt = DevReset.reset4HB(userInfoMap);
				logger.warn("servicename[FactoryResetService]cmdId[{}]userinfo[{}]调ACS设备返回码：{}",
						new Object[] { deal.getOpId(), userInfo, irt});
				if(1 == irt)
				{
					logger.warn("servicename[FactoryResetService]cmdId[{}]userinfo[{}]设备恢复出厂设置成功", new Object[] { deal.getOpId(), userInfo});
					deal.setResult("0");
					deal.setErrMsg("执行成功");
					// 入数据库
					new PublicDAO().recordFactoryResetReturnDiag(deal.getOpId(), deal.getResult(), deal.getErrMsg(), userDevId, userInfoMap.get("username"));
				}
				else
				{
					// 调用配置模块，或者acs模块对设备下发恢复出厂设置命令失败后，业务用户表修改成成功状态
					userDevDao.updateCustStatusFailure(userId);
					logger.warn("servicename[FactoryResetService]cmdId[{}]userinfo[{}]设备恢复出厂设置失败", new Object[] { deal.getOpId(), userInfo});
					deal.setResult("-10");
					deal.setErrMsg("平台异常或繁忙");
					deal.returnXML(deal.getOpId(),deal.getResult(),deal.getErrMsg());
				}
				 
			 }else{
				 logger.warn("servicename[FactoryResetService]cmdId[{}]userinfo[{}]设备无法交互", new Object[] { deal.getOpId(), userInfo});
				deal.setResult("-10");
				deal.setErrMsg("设备无法交互，请确认设备是否在线。");
				deal.returnXML(deal.getOpId(),deal.getResult(),deal.getErrMsg());
			}
		}
	
	}
	public String work(String inXml, boolean iscommon) {
		logger.warn("servicename[FactoryResetService]执行，入参为：{}", inXml);
		FactoryResetDealXML deal = new FactoryResetDealXML();
		Document document = deal.getXML(inXml);
		deal.setIscommon(iscommon);
		if (document == null) {
			logger.warn("servicename[FactoryResetService]解析入参错误！");
			deal.setResult("-99");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String serialNumber = deal.getSerialNumber();
		
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		Map<String, String> userInfoMap = null;
		int userType = 1;
		String userInfo = "";
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			userType = 2;
			userInfo = logicId;
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			userType = 1;
			userInfo = pppUsename;
		}
		else if(!StringUtil.IsEmpty(serialNumber) && serialNumber.length()>=6) {
			// 设备sn
			userType = 6;
			userInfo = serialNumber;
		}
		else{
			logger.warn("servicename[FactoryResetService]解析入参错误！");
			deal.setResult("-1");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		userInfoMap = userDevDao.queryUserInfo(userType, userInfo, "");
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn("servicename[FactoryResetService]userinfo[{}]查无此用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户");
			return deal.returnXML();
		} 	
		long userId = StringUtil.getLongValue(userInfoMap.get("user_id"));
		String userDevId = userInfoMap.get("device_id");

		if (StringUtil.IsEmpty(userDevId)) {// 用户未绑定终端
			logger.warn("servicename[FactoryResetService]cmdId[{}]userinfo[{}]此客户未绑定", new Object[] { deal.getOpId(), userInfo});
			deal.setResult("-99");
			deal.setErrMsg("用户未绑定终端");
			return deal.returnXML();
		}else{
			int irt  = 0;
			 
			 /**
			  * 流程调整为:
			  * 1、调用者（WEB或者ItmsService模块），将需要恢复出厂的用户业务状态置成未做，调用配置模块。
			  * 2、配置模块根据未做的业务生成业务下发策略，通知acs恢复出厂
			  * 3、调用者判断恢复出厂如果是失败，将状态还原。
			  */
			 userDevDao.updateCustStatus(userId);
			 
             irt = DevReset.reset4HB(userInfoMap);
			logger.warn("servicename[FactoryResetService]cmdId[{}]userinfo[{}]调ACS设备返回码：{}",
					new Object[] { deal.getOpId(), userInfo, irt});
			if(1 == irt)
			{
				logger.warn("servicename[FactoryResetService]cmdId[{}]userinfo[{}]设备恢复出厂设置成功", new Object[] { deal.getOpId(), userInfo});
				deal.setResult("0");
				deal.setErrMsg("执行成功");
				// 入数据库
				new PublicDAO().recordFactoryResetReturnDiag(deal.getOpId(), deal.getResult(), deal.getErrMsg(), userDevId, userInfoMap.get("username"));
				return deal.returnXML();
			}
			else
			{
				// 调用配置模块，或者acs模块对设备下发恢复出厂设置命令失败后，业务用户表修改成成功状态
				userDevDao.updateCustStatusFailure(userId);
				logger.warn("servicename[FactoryResetService]cmdId[{}]userinfo[{}]设备恢复出厂设置失败", new Object[] { deal.getOpId(), userInfo});
				deal.setResult("-10");
				deal.setErrMsg("平台异常或繁忙");
				return deal.returnXML();
			}
		}
	}
}
