package com.linkage.itms.dispatch.cqdx.service;

import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.obj.ServiceReDoneDealXML;
import com.linkage.itms.dispatch.service.ServiceDoneService;


public class ServiceReDoneService {
	private static Logger logger = LoggerFactory.getLogger(ServiceReDoneService.class);

	public String work(String inXml, boolean iscommon) {
		logger.warn("servicename[ServiceReDoneService]执行，入参为：{}", inXml);
		ServiceReDoneDealXML deal = new ServiceReDoneDealXML();
		Document document = deal.getXML(inXml);
		deal.setIscommon(iscommon);
		if (document == null) {
			logger.warn("servicename[ServiceReDoneService]解析入参错误！");
			deal.setResult("-1");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		Element param = document.getRootElement().addElement("Param");
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String sn = deal.getSerialNumber();
		UserDeviceDAO userDeviceDao = new UserDeviceDAO();
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			param.addElement("UserInfoType").addText("2");
			param.addElement("UserInfo").addText(logicId);
			param.addElement("SearchType").addText("1");
			// TODO 入参会验证devSN，业务逻辑中用不到，先随意给个值
			param.addElement("DevSN").addText("00");
			
			Map<String, String> userInfoMap = userDeviceDao.queryUserInfo4CQ(2, logicId);
			if (null == userInfoMap || userInfoMap.isEmpty())
			{
				logger.warn("servicename[ServiceReDoneService]userinfo[{}]查无此loid的宽带账号",logicId);
			}
			else{
				deal.setPppUsename(StringUtil.getStringValue(userInfoMap, "username"));
				deal.setSerialNumber(StringUtil.getStringValue(userInfoMap, "device_serialnumber"));
				deal.setDeviceId(StringUtil.getStringValue(userInfoMap, "device_id"));
			}
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			param.addElement("UserInfoType").addText("1");
			param.addElement("UserInfo").addText(pppUsename);
			param.addElement("SearchType").addText("1");
			// TODO 入参会验证devSN，业务逻辑中用不到，先随意给个值
			param.addElement("DevSN").addText("00");
			
			Map<String, String> userInfoMap = userDeviceDao.queryUserInfo4CQ(1, pppUsename);
			if (null == userInfoMap || userInfoMap.isEmpty())
			{
				logger.warn(
						"servicename[ServiceReDoneService]userinfo[{}]查无此宽带账号的loid",pppUsename);
			}
			else{
				deal.setLogicId(StringUtil.getStringValue(userInfoMap, "loid"));
				deal.setSerialNumber(StringUtil.getStringValue(userInfoMap, "device_serialnumber"));
				deal.setDeviceId(StringUtil.getStringValue(userInfoMap, "device_id"));
			}
		}
		else if(!StringUtil.IsEmpty(sn)){
			
			// sn
			param.addElement("UserInfoType").addText("2");
			param.addElement("UserInfo").addText("00");
			param.addElement("DevSN").addText(sn);
			param.addElement("SearchType").addText("2");
			
			Map<String, String> userInfoMap = userDeviceDao.queryUserInfo4CQ(3, sn);
			if (null == userInfoMap || userInfoMap.isEmpty())
			{
				logger.warn(
						"servicename[ServiceReDoneService]userinfo[{}]查无设备序列号的用户信息",sn);
			}
			else{
				deal.setLogicId(StringUtil.getStringValue(userInfoMap, "loid"));
				deal.setPppUsename(StringUtil.getStringValue(userInfoMap, "username"));
				deal.setDeviceId(StringUtil.getStringValue(userInfoMap, "device_id"));
			}
		}
		
		// TODO 业务类型 怎么赋值
		param.addElement("ServiceType").addText("0");
		// TODO 操作类型怎么赋值
		param.addElement("OperateType").addText("1");
		// TODO 入参会验证cityId，业务逻辑中用不到，先随意给个值
		param.addElement("CityId").addText("00");

		deal.setResultXML(new ServiceDoneService().work(document.asXML()));
		return deal.returnXML();
	}
}
