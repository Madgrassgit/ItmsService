package com.linkage.itms.dispatch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.SocketUtil;

public class SendSheetByChinaMobileService implements IService {
	
	private static final Logger logger = LoggerFactory
			.getLogger(SendSheetByChinaMobileService.class);

	public String work(String bssSheet) {
		if(StringUtil.IsEmpty(bssSheet)){
			logger.warn("sendSheet is null");
			return null;
		}
		
		System.out.println("工单："+bssSheet);
		
		String retResult = SocketUtil.sendStrMesg(
				Global.G_ITMS_SHEET_SERVER_CHINA_MOBILE, 
				StringUtil.getIntegerValue(Global.G_ITMS_SHEET_PORT_CHINA_MOBILE), 
				bssSheet+ "\n");

		
		logger.warn("sendSheetByChinaMobile：returnXml= " + retResult);
		
		return retResult;
	}
	
	
//	public String returnXML(String strXML,RecieveSheetOBJ obj)
//	{
//		logger.warn("工单服务器回单："+strXML);
//		String [] arrStrings = strXML.split("\\|\\|\\|");
//		String resultCode = arrStrings[1];
//		String resultMsg = "成功";
//		if(!resultCode.equals("00"))
//		{
//			resultMsg = arrStrings[2];
//		}
//		logger.warn("resultCode:"+resultCode + ",resultMsg:" +resultMsg);
//		Document document = DocumentHelper.createDocument();
//		Element root = document.addElement("root");
//		root.addElement("CmdID").addText(obj.getCmdId());
//		root.addElement("RstCode").addText(resultCode);
//		root.addElement("RstMsg").addText(resultMsg);
//		String strReslt = document.asXML();
//		logger.warn("业务开通接口回参："+strReslt);
//		return strReslt;
//	}
}
