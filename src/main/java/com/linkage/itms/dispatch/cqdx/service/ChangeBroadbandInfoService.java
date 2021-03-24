package com.linkage.itms.dispatch.cqdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.ChangeBroadbandInfoDealXML;

public class ChangeBroadbandInfoService {
	private static Logger logger = LoggerFactory.getLogger(ChangeBroadbandInfoService.class);

	public String work(String inXml) {
		logger.warn("servicename[ChangeBroadbandInfoService]执行，入参为：{}", inXml);
		ChangeBroadbandInfoDealXML deal = new ChangeBroadbandInfoDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[ChangeBroadbandInfoService]解析入参错误！");
			deal.setResult("-99");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		String logicId = deal.getLogicId();
		String netName = deal.getPppUsename();
		
		PublicDAO publicDAO = new PublicDAO();
		List<HashMap<String,String>> userIdList = new ArrayList<HashMap<String,String>>();
		if(!StringUtil.IsEmpty(logicId)){
			userIdList	= publicDAO.queryUserId(2, logicId);
		}else if(!StringUtil.IsEmpty(netName)){
			userIdList	= publicDAO.queryUserId(1, netName);
		}else{
			logger.warn("servicename[ChangeBroadbandInfoService]入参格式错误！");
			deal.setResult("-99");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		
		if(null == userIdList || userIdList.isEmpty() || null == userIdList.get(0)){
			logger.warn("servicename[ChangeBroadbandInfoService]无此客户信息！");
			deal.setResult("-1");
			deal.setErrMsg("无此客户信息！");
			return deal.returnXML();
		}
		
		if(!StringUtil.IsEmpty(deal.getAreaCode()) || !StringUtil.IsEmpty(deal.getUserAddress())){
			int res = publicDAO.updateCustomerInfo(StringUtil.getLongValue(userIdList.get(0).get("user_id")),
					deal.getUserAddress(), deal.getAreaCode());
			if(res<=0){
				deal.setResult("-99");
				deal.setErrMsg("更新失败！");
				return deal.returnXML();
			}
		}
		
		if(!StringUtil.IsEmpty(deal.getVlanId())){
			int res = publicDAO.updateHgwcustInfo(StringUtil.getLongValue(userIdList.get(0).get("user_id")), deal.getVlanId());
			if(res<=0){
				deal.setResult("-99");
				deal.setErrMsg("更新失败！");
				return deal.returnXML();
			}
		}
		deal.setResult("0");
		deal.setErrMsg("执行成功！");
		
		// 记录日志
		String ret = deal.returnXML();
		deal.recordLog("ChangeBroadbandInfoService", "", "", inXml, ret);
		return deal.returnXML();
				
	}
}
