package com.linkage.itms.dispatch.cloud;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.WSClient.WebServiceUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dispatch.service.CloudPingDiagnosticService;
import com.linkage.itms.dispatch.service.CloudPingVXLANDiagnosticService;
import com.linkage.itms.dispatch.service.CloudQueryServIpAddrService;

public class AsynchronousService implements Runnable {

	static final Logger logger = LoggerFactory.getLogger(AsynchronousService.class);
	private String inXml = "";
	private String methodName = "";
	public AsynchronousService (String inXml, String methodName) {
		// 将回调参数置成0
		Pattern p = Pattern.compile("<CallBack>\\d");
		Matcher m = p.matcher(inXml); 
		inXml = m.replaceAll("<CallBack>0");
		// 增加异步标识
		StringBuffer sb = new StringBuffer(inXml.substring(0, inXml.indexOf("</Param></root>")));
		sb.append("<IsAsynchronous>1</IsAsynchronous></Param></root>");
		
		
		this.inXml = sb.toString();
		this.methodName = methodName;
		logger.warn("[AsynchronousService] with methodName[{}] ,inXml[{}]", this.methodName, this.inXml);
	}
	@Override
	public void run() {
		String retXml = "";
		
		if ("QueryServIpAddr".equals(methodName)) {
			retXml = new CloudQueryServIpAddrService().work(inXml);
			methodName = "QueryServIpAddrAsynchronous";
		}
		else if ("PingDiagnostic".equals(methodName)) {
			retXml = new CloudPingDiagnosticService().work(inXml);
			methodName = "PingDiagnosticAsynchronous";
		}
		else if ("PingVXLANDiagnostic".equals(methodName)) {
			retXml = new CloudPingVXLANDiagnosticService().work(inXml);
			methodName = "PingDiagnosticAsynchronous";
		}
		logger.warn("AsynchronousService begin rul: [{}], targetname: [{}], methodname: [{}], retXml[{}]",
				new Object[] {Global.vxlanUrl, Global.vxlanTargetName, methodName, retXml});
		String res = "";
		try {
			res = WebServiceUtil.call(Global.vxlanUrl, Global.vxlanTargetName, methodName, retXml);
		} catch (Exception e) {
			logger.error("[AsynchronousService] is error, methodname[{}] retXml[{}] error message[{}]", 
					new Object[] {methodName, retXml, e.getMessage()});
			e.printStackTrace();
		}
		logger.warn("AsynchronousService done [{}]", res);
	}
	public static void main(String[] args) {
//		Global.G_AsynchronousThread = ThreadPoolCommon.getFixedThreadPool(3);
		String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><ClientType>5</ClientType>" +
				"<CmdID>00000000000000000001</CmdID><CmdType>CX_01</CmdType><DealDate>20171116101010" +
				"</DealDate><Param><CallBack>1</CallBack><UserInfo>pppoetest02@chn.xj</UserInfo><UserInfoType>" +
				"1</UserInfoType></Param></root>";
//		AsynchronousService as = new AsynchronousService(str, "QueryServIpAddr");
//		Global.G_AsynchronousThread.execute(as);
		
	
		System.out.println(	new CloudQueryServIpAddrService().work(str));
//		StringBuffer sb = new StringBuffer(str.substring(0, str.indexOf("</Param></root>")));
//		sb.append("<IsAsynchronous>1</IsAsynchronous></Param></root>");
//		System.out.println(sb.toString());
		
	}
}
