package com.linkage.itms.dispatch.cqdx.service;

import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.obj.QueryBindInfoDealXML;
import com.linkage.itms.dispatch.service.BindInfoService;

public class QueryBindInfoService {
	private static Logger logger = LoggerFactory.getLogger(QueryBindInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[QueryBindInfoService]执行，入参为：{}", inXml);
		QueryBindInfoDealXML deal = new QueryBindInfoDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[QueryBindInfoService]解析入参错误！");
			deal.setResult("-11");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		Element param = document.getRootElement().addElement("Param");
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		UserDeviceDAO userDeviceDao = new UserDeviceDAO();
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			param.addElement("UserInfoType").addText("2");
			param.addElement("UserInfo").addText(logicId);
			if(StringUtil.isEmpty(pppUsename)){
				Map<String, String> userInfoMap = userDeviceDao.queryUserInfo4CQ(2, logicId);
				if (null == userInfoMap || userInfoMap.isEmpty())
				{
					logger.warn(
							"servicename[QueryBindInfoService]userinfo[{}]查无此loid的宽带账号",logicId);
				}
				else{
					deal.setPppUsename(StringUtil.getStringValue(userInfoMap, "username"));
				}
			}
			
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			param.addElement("UserInfoType").addText("1");
			param.addElement("UserInfo").addText(pppUsename);
			if(StringUtil.isEmpty(logicId)){
				Map<String, String> userInfoMap = userDeviceDao.queryUserInfo4CQ(1, pppUsename);
				if (null == userInfoMap || userInfoMap.isEmpty())
				{
					logger.warn(
							"servicename[QueryBindInfoService]userinfo[{}]查无此宽带账号的loid",pppUsename);
				}
				else{
					deal.setLogicId(StringUtil.getStringValue(userInfoMap, "loid"));
				}
			}
		}
		else{
			logger.warn("servicename[QueryBindInfoService]解析入参错误！");
			deal.setResult("-1");
			deal.setErrMsg("宽带帐号、逻辑ID同时为空！");
			return deal.returnXML();
		}
		String serialnumber = StringUtil.getStringValue(deal.getSerialNumber());
		if(!StringUtil.isEmpty(serialnumber) && !"null".equalsIgnoreCase(serialnumber)){
			param.addElement("DevSN").addText(serialnumber);
		}
		// TODO 入参会验证cityId，业务逻辑中用不到，先随意给个值
		param.addElement("CityId").addText("00");
		deal.setResultXML(new BindInfoService().work(document.asXML()));
		logger.warn("ResultXML="+deal.getResultXML());
		return deal.returnXML();
	}
}
