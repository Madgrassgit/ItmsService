package com.linkage.WSClient;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 调用RestfulService工具类
 * 
 * @author jlp
 *
 */
public class RestClient {
	public static void main(String[] args) {
		final String targetServCode = "10.1157_YWGXXCXJK_CloudCallServiceService_QueryGetWay.SynReq";
	    String NMESB_DCN_URL = "http://42.99.0.99:53099/00.0001.O_OtherBusi_serviceHandler";
	    String sendorCode="10.1155.02";
	    String xmlContent="<root><CmdID>123456789012345</CmdID><CmdType> CX_01</CmdType><ClientType>5</ClientType><DealDate>20170522170000</DealDate><Param><UserInfoType>2</UserInfoType><UserInfo>9918770028C</UserInfo></Param></root>";
	    xmlContent=xmlContent.replaceAll("<", "&lt;");
	    xmlContent=xmlContent.replaceAll(">", "&gt;");
	    String res="";
	    try {
	    	res=RestClient.sendPostOrPut1(NMESB_DCN_URL, buildESBReqContent(targetServCode, "", sendorCode, xmlContent),"POST");
		} catch (Exception e) {
			e.printStackTrace();
		}
	   System.out.println(res);
	}
	public static String buildESBReqContent(String servCode,String reqId, String sender,String contentXml){
		String cot="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:impl=\"http://impl.webservice.apps.itms.ai.com\">"
				+ "<soapenv:Header>"
				+ "<Esb>"
				+ "<Route>"
				+ "<Sender>"+sender+"</Sender>"
				+ "<Time>20160108184413985</Time>"
				+ "<ServCode>"+servCode+"</ServCode>"
				+ "<Version>V0.1</Version>"
				+ "<MsgId>"+reqId+"</MsgId>"
				+ "<TransId>"+reqId+"</TransId>"
				+ "<AuthType/>"
				+ "<AuthCode/>"
				+ "<CarryType/>"
				+ "<ServTestFlag/>"
				+ "<MsgType/>"
				+ "</Route>"
				+ "</Esb>"
				+ "</soapenv:Header>"
				+ "<soapenv:Body>"
				+ "<impl:ServiceDoneReturn>"
				+ "<impl:param>"+contentXml+"</impl:param>"
				+ "</impl:ServiceDoneReturn></soapenv:Body>"
				+ "</soapenv:Envelope>";
		return cot;
	}
	public enum RequestMethod {
		GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
	}
	public static String sendPostOrPut1(String url, Object param,
			String methodType) throws Exception {
		BufferedReader in = null;
		String result = "";
		URL realUrl = new URL(url);
		// 打开和URL之间的连接
		HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
		conn.setRequestMethod(methodType);
		// 设置通用的请求属性
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("user-agent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
		// 发送POST请求必须设置如下两行
		conn.setDoOutput(true);
		conn.setDoInput(true);
		System.out.println(conn.getRequestProperties().toString());
		// 获取URLConnection对象对应的输出流
		//out = new PrintWriter(conn.getOutputStream());
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		// 发送请求参数
		out.write(param.toString().getBytes("UTF-8"));
		// flush输出流的缓冲
		out.flush();
		// 定义BufferedReader输入流来读取URL的响应
		in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			result += line;
		}
		// 使用finally块来关闭输出流、输入流
		if (out != null) {
			out.close();
		}
		if (in != null) {
			in.close();
		}
		return result;
	}
	
}
