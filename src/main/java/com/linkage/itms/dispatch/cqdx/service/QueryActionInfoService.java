package com.linkage.itms.dispatch.cqdx.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.QueryActionInfoDealXML;

public class QueryActionInfoService {
	private static Logger logger = LoggerFactory.getLogger(QueryActionInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[QueryActionInfoService]执行，入参为：{}", inXml);
		QueryActionInfoDealXML deal = new QueryActionInfoDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[QueryActionInfoService]解析入参错误！");
			deal.setActionResult("02");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		PublicDAO dao = new PublicDAO();
		List<HashMap<String, String>> list = null;

		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		int userType = 0;
		String username = "";
		UserDeviceDAO userDeviceDao = new UserDeviceDAO();
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			userType = 2; 
			username = logicId;
			
			if(StringUtil.isEmpty(pppUsename)){
				Map<String, String> userInfoMap = userDeviceDao.queryUserInfo4CQ(2, logicId);
				if (null == userInfoMap || userInfoMap.isEmpty())
				{
					logger.warn(
							"servicename[QueryActionInfoService]userinfo[{}]查无此loid的宽带账号",logicId);
				}
				else{
					deal.setPppUsename(StringUtil.getStringValue(userInfoMap, "username"));
				}
			}
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			userType = 1; 
			username = pppUsename;
			if(StringUtil.isEmpty(logicId)){
				Map<String, String> userInfoMap = userDeviceDao.queryUserInfo4CQ(1, pppUsename);
				if (null == userInfoMap || userInfoMap.isEmpty())
				{
					logger.warn(
							"servicename[QueryActionInfoService]userinfo[{}]查无此宽带账号的loid",pppUsename);
				}
				else{
					deal.setLogicId(StringUtil.getStringValue(userInfoMap, "loid"));
				}
			}
		}
		else {
			logger.warn("servicename[QueryActionInfoService]入参格式错误！");
			deal.setActionResult("02");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		list = dao.getBussinessInfo4net(userType, username,"1");
		if (list == null || list.isEmpty()) {
			logger.warn("servicename[QueryActionInfoService]不存在用户！");
			deal.setActionResult("02");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
		
		String actionResult = "02";
		actionResult = "1".equals(StringUtil.getStringValue(list.get(0), "open_status"))?"01":"02";
		String uplinkType = StringUtil.getStringValue(list.get(0), "adsl_hl");
		if ("1".equals(uplinkType)) {
			uplinkType = "02";//adsl
		}
		else if ("2".equals(uplinkType)) {
			uplinkType = "03";//lan
		}
		else{
			uplinkType = "01";//ftth epon gpon
		}
		deal.setActionResult(actionResult);
		deal.setUplinkType(uplinkType);
		deal.setErrMsg("成功！");
		String ret = deal.returnXML();
		// 日志
		deal.recordLog("QueryActionInfoService", username, deal.getSerialNumber(), inXml, ret);
		return ret;
	}
}
