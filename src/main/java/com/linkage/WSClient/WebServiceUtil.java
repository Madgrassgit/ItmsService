package com.linkage.WSClient;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 调用axis2
 * @author jlp
 *
 */
public class WebServiceUtil {
	/** 日志 */
	private static Logger logger = LoggerFactory.getLogger(WebServiceUtil.class);

	public static String call(String url, String targetNamespace,
			String method, Object[] param) throws Exception {
		Service service = new Service();
		String result = "";
		Call call = null;
		call = (Call) service.createCall();
		QName qname = new QName(targetNamespace, method);
		//call.setTimeout(Global.WSTimeOut);
		call.setOperationName(qname);
		call.setTargetEndpointAddress(new java.net.URL(url));
		result = (String) call.invoke(param);
		return result;
	}

	public static String call(String url, String targetNamespace,
			String method, String param) throws Exception {
		return call(url, targetNamespace, method, new Object[] { param });
	}
	
	public static void main(String[] args) {
		
		String url = "http://137.0.13.58:8060/SumSubscriberProfile/AAANaiSubscriber/";
		String targetName ="";
		
		String method="SetMOAttributes";
		StringBuffer psb = new StringBuffer();
		psb.append("<SubscriberClass Name=\"SumSubscriberProfile\">");
		psb.append("<SubscriberSubClass Name=\"AAANaiSubscriber\">");
		psb.append("<Method Name=\"SetMOAttributes\">");
		psb.append("<NAIUSERNAME>XXXX</NAIUSERNAME>");
		psb.append("<NEWPASSWORD>YYYY</NEWPASSWORD>");
		psb.append("</Method>");
		psb.append("</SubscriberSubClass>");
		psb.append("</SubscriberClass>");
        
		try {
			String ret = call(url, targetName, method, psb.toString());
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
