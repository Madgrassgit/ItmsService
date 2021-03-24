package com.linkage.itms.dispatch.cqdx.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.FeedbackWorkTicketsInfo4CommDealXML;

public class FeedbackWorkTicketsInfo4CommService {
	private static Logger logger = LoggerFactory.getLogger(FeedbackWorkTicketsInfo4CommService.class);

	public String work(String inXml) {
		logger.warn("servicename[FeedbackWorkTicketsInfo4CommService]执行，入参为：{}", inXml);
		FeedbackWorkTicketsInfo4CommDealXML deal = new FeedbackWorkTicketsInfo4CommDealXML();
		Document document = deal.getXML(inXml);
		PublicDAO dao = new PublicDAO();
		
		Document retDocument = DocumentHelper.createDocument();
		retDocument.setXMLEncoding("GBK");
		Element root = retDocument.addElement("response");
		// 接口调用唯一ID
		root.addElement("work_id").addText(deal.getOpId());
		
		Element resultArray = root.addElement("result_array");
	
		if (document == null) {
			logger.warn("servicename[FeedbackWorkTicketsInfo4CommService]解析入参错误！");
			root.addElement("result").addText("-11");
			root.addElement("err_msg").addText("解析入参错误！");
			return retDocument.asXML();
		}
		
		/*if(null == deal.getLoids() || deal.getLoids().isEmpty() || null == deal.getLoids().get(0)){
			logger.warn("servicename[FeedbackWorkTicketsInfo4CommService]入参格式错误！");
			deal.setResult("-11");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}*/
		
		/*root.addElement("result").addText("0");
		root.addElement("err_msg").addText("成功！");*/
		
		for (int i=0;i<deal.getLoids().size();i++) {
			String loid = deal.getLoids().get(i);
			if(StringUtil.isEmpty(loid)){
				Map<String, String> map = dao.qryUserDevice(null, null, deal.getSerialNumbers().get(i),"");
				logger.warn("map="+map);
				if(null == map){
					logger.warn("入参serialnumber{}设备没有查绑定用户",deal.getSerialNumbers().get(i));
				}
				loid = StringUtil.getStringValue(map, "username");
			}
			
			if (StringUtil.isEmpty(loid)) {
				Element loidResult = resultArray.addElement("loid_result");
				loidResult.addElement("loid").addText("");
				loidResult.addElement("serial_number").addText("");
				loidResult.addElement("service_status").addText("2");
				loidResult.addElement("service_list").addText("");
				loidResult.addElement("error_msg").addText("入参loid为空");
				logger.warn("servicename[FeedbackWorkTicketsInfo4CommService]入参loid{}为空", loid);
				continue;
			}
			List<HashMap<String, String>> list = dao.getBussinessInfo(2, loid, "1");
			if (list == null || list.isEmpty()) {
				Element loidResult = resultArray.addElement("loid_result");
				loidResult.addElement("loid").addText(loid);
				loidResult.addElement("serial_number").addText("");
				loidResult.addElement("service_status").addText("1");
				loidResult.addElement("service_list").addText("");
				loidResult.addElement("error_msg").addText("入参没有查到对应的绑定数据");
				logger.warn("servicename[FeedbackWorkTicketsInfo4CommService]入参loid{}没有查到对应的绑定数据", loid);
				continue;
			}
			
			
			StringBuffer serviceList = new StringBuffer();
			StringBuffer broadband = new StringBuffer();
			StringBuffer iptv = new StringBuffer();
			StringBuffer voip = new StringBuffer();
			String serialNumber = "";
			String serviceStatus = "";
			for (HashMap<String, String> map : list) {
				serialNumber = StringUtil.getStringValue(map, "device_serialnumber");
				serviceStatus = StringUtil.getStringValue(map, "open_status");
				if ("10".equals(StringUtil.getStringValue(map, "serv_type_id"))) {
					broadband.append("broadband|");
				}
				if ("11".equals(StringUtil.getStringValue(map, "serv_type_id"))) {
					iptv.append("iptv|");
				}
//				if ("14".equals(StringUtil.getStringValue(map, "serv_type_id"))) {
//					voip.append("voip|");
//				}
			}
			
			// 查询语音开通数
			List<HashMap<String, String>> listVoip = dao.getVoipBussinessInfo(2, loid);
			if(null != listVoip && !listVoip.isEmpty() && null != listVoip.get(0)){
				for(HashMap<String, String> mapVoip : listVoip){
					voip.append("voip|");
				}
			}
			
			if (broadband.length() > 0) {
				serviceList.append(broadband.delete(broadband.length() - 1, broadband.length()));
			}
			if (iptv.length() > 0) {
				if(broadband.length() > 0){
					serviceList.append(",");
				}
				serviceList.append(iptv.delete(iptv.length() - 1, iptv.length()));	
			}
			if (voip.length() > 0) {
				if(broadband.length() > 0 || iptv.length() > 0){
					serviceList.append(",");
				}
				serviceList.append(voip.delete(voip.length() - 1, voip.length()));
			}
			
			Element loidResult = resultArray.addElement("loid_result");
			loidResult.addElement("loid").addText(loid);
			loidResult.addElement("serial_number").addText(serialNumber);
			loidResult.addElement("service_status").addText(getServiceStatus(serviceStatus));
			loidResult.addElement("service_list").addText(serviceList.toString());
			loidResult.addElement("error_msg").addText("成功！");
		}

		String ret = retDocument.asXML();
		// 日志
		deal.recordLog("FeedbackWorkTicketsInfo4CommService", "", "", inXml, ret);
		return ret;
	}
	
	String getServiceStatus(String serviceStatus){
		if("1".equals(serviceStatus)) return "0";
		else return "1";
	}
}
