package com.linkage.itms.dispatch.cqdx.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.obj.UnbindWorkTicketDealXML;
import com.linkage.itms.dispatch.service.ReleaseService;

public class UnbindWorkTicketService {
	private static Logger logger = LoggerFactory.getLogger(UnbindWorkTicketService.class);

	public String work(String inXml) {
		logger.warn("servicename[UnbindWorkTicketService]执行，入参为：{}", inXml);
		UnbindWorkTicketDealXML deal = new UnbindWorkTicketDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[UnbindWorkTicketService]解析入参错误！");
			deal.setResult("-1");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		Element param = document.getRootElement().addElement("Param");
		String logicId = deal.getLogicId();
		String serialNumber = deal.getSerialNumber();
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			param.addElement("UserInfoType").addText("2");
			param.addElement("UserInfo").addText(logicId);
		}
		else if(!StringUtil.IsEmpty(serialNumber)) {
			// 设备sn
			param.addElement("UserInfoType").addText("6");
			param.addElement("UserInfo").addText(serialNumber);
			
			//查询设备序列号
			ArrayList<HashMap<String, String>> deviceListMap = new UserDeviceDAO().getTelePasswdByDevSn(serialNumber);
			if(null != deviceListMap && !deviceListMap.isEmpty() && null != deviceListMap.get(0)){
				logicId = deviceListMap.get(0).get("username");
			}
		}
		// TODO 入参会验证cityId，业务逻辑中用不到，先随意给个值
		param.addElement("CityId").addText("00");
		
		deal.setResultXML(new ReleaseService().work(document.asXML()));
		
		return deal.returnXML();
	}
}
