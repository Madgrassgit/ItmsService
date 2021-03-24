package com.linkage.WSClient;

import com.linkage.itms.commom.StringUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

public class WSClientProcess {

	private static final long TIMEOUT = 300000L;
	/**
	 * 日志工具类
	 */
	private static final Logger logger = LoggerFactory.getLogger(WSClientProcess.class);

	/**
	 * 通过服务端URL和方法名称获取Client对象
	 * 
	 * @param url
	 *            服务端URL
	 * @param action
	 *            方法
	 * @return 返回需要的功能存根对象，如果失败，则返回null
	 */
	private static ServiceClient getInstance(String url, String action) {
		
		logger.debug("getInstance({},{})", url, action);
		
		try {
			logger.warn("创建ServiceClient对象");
			ServiceClient sc = new ServiceClient();
			Options opts = new Options();
			opts.setTimeOutInMilliSeconds(TIMEOUT);
			opts.setAction(action);
			opts.setTo(new EndpointReference(url));
			opts.setCallTransportCleanup(true);
			sc.setOptions(opts);
			return sc;
		} catch (AxisFault e) {
			logger.warn("创建ServiceClient对象失败", e);
			logger.error("创建ServiceClient对象失败！ mesg：", e);
			return null;
		}
	}

	/**
	 * 通过服务端URL和方法名称重新绑定
	 *
	 * @param url
	 *            服务端URL
	 * @param action
	 *            方法
	 * @return 返回需要的功能存根对象，如果失败，则返回null
	 */
	private static ServiceClient rebind(String url, String action) {
		logger.debug("ServiceClient({},{})", url, action);
		return getInstance(url, action);
	}

	/**
	 * @param uri
	 * @param prefix
	 * @param methodName
	 * @param param
	 * @return
	 */
	private static OMElement getPayLoad(String uri, String prefix, String methodName, Map<String, String> param) {
		
		logger.debug("getPayLoad({},{},{},{})", uri, prefix, methodName, param);
		
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace(uri, prefix);
		OMElement method = fac.createOMElement(methodName, omNs);
		if (param != null && !param.isEmpty()) {
			Iterator<String> it = param.keySet().iterator();
			while (it.hasNext()) {
				String paraName = it.next();
				OMElement value = fac.createOMElement(paraName, omNs);
				value.setText(param.get(paraName));
				method.addChild(value);
			}
		}
		logger.debug("Method:{}", method.getText());
		return method;
	}

	/**
	 * 匿名同步调用WebService接口,有参数方法,有返回值
	 * 
	 * @param uri
	 *            命名空间
	 * @param prefix
	 *            前缀
	 * @param actionName
	 *            actionName属性
	 * @param methodName
	 *            方法名
	 * @param param
	 *            参数
	 * @param url
	 *            链接
	 * @return
	 */
	public static OMElement serviceReceive(String uri, String prefix,
			String actionName, String methodName, Map<String, String> param,
			String url) {
		
		logger.debug("serviceReceive({},{},{},{},{},{})",uri, prefix,
				actionName, methodName, param, url);
		
		ServiceClient sc = getInstance(url, actionName);
		OMElement element = null;
		try {
			logger.warn("开始调用WebService.serviceReceive");
			if(sc == null){
				sc = rebind(url, actionName);
			}
			if(sc != null){
				element = sc.sendReceive(getPayLoad(uri, prefix, methodName, param));
				logger.warn("WebService.serviceReceive调用成功");
			}
		} catch (Exception e) {
			// 重新绑定
			sc = rebind(url, actionName);
			if (null != sc) {
				try {
					logger.warn("WebService.serviceReceive重新调用");
					// 重新发送请求
					element = sc.sendReceive(getPayLoad(uri, prefix, methodName, param));
					logger.warn("WebService.serviceReceive调用成功");
				} catch (AxisFault e1) {
					logger.warn("WebService.serviceReceive通讯失败", e1);
					logger.error("WebService.serviceReceive通讯失败, mesg: ",e1);
				}
			}
		} finally {
			try {
				// 关闭连接
				if(sc != null){
					sc.cleanupTransport();
					sc.cleanup();
				}
			} catch (AxisFault e) {
				logger.error("WebService客户端关闭连接失败！");
			}
			sc = null;
		}
		return element;
	}

	/**
	 * 匿名同步调用WebService接口,无参数方法,有返回值
	 * 
	 * @param uri
	 *            命名空间
	 * @param prefix
	 *            前缀
	 * @param actionName
	 *            actionName属性
	 * @param methodName
	 *            方法名
	 * @param url
	 *            链接
	 * @return
	 */
	public static OMElement serviceReceive(String uri, String prefix,
			String actionName, String methodName, String url) {
		
		logger.debug("serviceReceive({},{},{},{},{})", uri, prefix,
				actionName, methodName, url);
		
		return serviceReceive(uri, prefix, actionName, methodName, null, url);
	}

	/**
	 * 匿名同步调用WebService接口,有参数方法,无返回值
	 * 
	 * @param uri
	 *            命名空间
	 * @param prefix
	 *            前缀
	 * @param actionName
	 *            actionName属性
	 * @param methodName
	 *            方法名
	 * @param param
	 *            参数
	 * @param url
	 *            链接
	 * @return
	 */
	public static void serviceNoReceive(String uri, String prefix,
			String actionName, String methodName, Map<String, String> param,
			String url) {
		
		logger.debug("serviceNoReceive({},{},{},{},{},{})", uri, prefix,
				actionName, methodName, param, url);
		
		ServiceClient sc = getInstance(url, actionName);
		try {
			logger.warn("WebService.serviceNoReceive开始调用");
			if(sc == null){
				// 重新绑定
				sc = rebind(url, actionName);
			}
			if(sc != null){
				sc.sendRobust(getPayLoad(uri, prefix, methodName, param));
				logger.warn("WebService.serviceNoReceive调用成功");
			}
		} catch (Exception e) {
			// 重新绑定
			sc = rebind(url, actionName);
			if (null != sc) {
				try {
					logger.warn("WebService.serviceNoReceive重新调用");
					// 重新发送请求
					sc.sendRobust(getPayLoad(uri, prefix, methodName, param));
					logger.warn("WebService.serviceNoReceive调用成功");
				} catch (AxisFault e1) {
					logger.warn("WebService.serviceNoReceive通讯失败", e1);
					logger.error("WebService.serviceNoReceive通讯失败, mesg: ", e1);
				}
			}
		} finally {
			try {
				// 关闭连接
				if(sc != null){
					sc.cleanupTransport();
					sc.cleanup();
				}
			} catch (AxisFault e) {
				e.printStackTrace();
			}
			sc = null;
		}
	}

	/**
	 * 匿名同步调用WebService接口,无参数方法,无返回值
	 * 
	 * @param uri
	 *            命名空间
	 * @param prefix
	 *            前缀
	 * @param actionName
	 *            actionName属性
	 * @param methodName
	 *            方法名
	 * @param url
	 *            链接
	 * @return
	 */
	public static void serviceNoReceive(String uri, String prefix,
			String actionName, String methodName, String url) {
		serviceNoReceive(uri, prefix, actionName, methodName, null, url);
	}

	/**
	 * 匿名异步调用WebService接口,有参数方法,无返回值,有回调函数
	 * 
	 * @param uri
	 *            命名空间
	 * @param prefix
	 *            前缀
	 * @param actionName
	 *            actionName属性
	 * @param methodName
	 *            方法名
	 * @param param
	 *            参数
	 * @param url
	 *            链接
	 * @param callback
	 *            回调函数
	 */
	public static void serviceReceiveNonBlocking(String uri, String prefix,
			String actionName, String methodName, Map<String, String> param,
			String url, AxisCallback callback) {
		
		logger.debug("serviceReceiveNonBlocking({},{},{},{},{},{},{})", uri, prefix,
				actionName, methodName, param, url, callback);
		
		ServiceClient sc = getInstance(url, actionName);
		try {
			logger.warn("WebService.serviceReceiveNonBlocking开始调用");
			if(sc == null){
				sc = rebind(url, actionName);
			}
			if(sc != null){
				sc.sendReceiveNonBlocking(getPayLoad(uri, prefix, methodName, param), callback);
				logger.warn("WebService.serviceReceiveNonBlocking调用成功");
			}

		} catch (Exception e) {
			// 重新绑定
			sc = rebind(url, actionName);
			if (null != sc) {
				try {
					logger.warn("WebService.serviceReceiveNonBlocking重新调用");
					// 重新发送请求
					sc.sendReceiveNonBlocking(getPayLoad(uri, prefix, methodName, param), callback);
					logger.warn("WebService.serviceReceiveNonBlocking调用成功");
				} catch (AxisFault e1) {
					logger.warn("WebService.serviceReceiveNonBlocking通讯失败", e1);
					logger.error("WebService.serviceReceiveNonBlocking通讯失败, mesg: ", e1);
				}
			}
		} finally {
			try {
				// 关闭连接
				if(sc != null){
					sc.cleanupTransport();
					sc.cleanup();
				}
			} catch (AxisFault e) {
				e.printStackTrace();
			}
			sc = null;
		}
	}

	/**
	 * 匿名异步调用WebService接口,无参数方法,无返回值,有回调函数
	 * 
	 * @param uri
	 *            命名空间
	 * @param prefix
	 *            前缀
	 * @param actionName
	 *            actionName属性
	 * @param methodName
	 *            方法名
	 * @param param
	 *            参数
	 * @param url
	 *            链接
	 * @param callback
	 *            回调函数
	 */
	public static void serviceReceiveNonBlocking(String uri, String prefix,
			String actionName, String methodName, String url,
			AxisCallback callback) {
		serviceReceiveNonBlocking(uri, prefix, actionName, methodName, null, url, callback);
	}

	/**
	 * 
	 * @param actionName
	 * @param url
	 * @param message
	 */
	public static SOAPEnvelope advanceServiceReceive(String actionName,
			String url, SOAPEnvelope envelope) {
		
		logger.debug("advanceServiceReceive({},{},{})", actionName, url, envelope);
		
		try {
			ServiceClient sc = new ServiceClient();
			OperationClient oc = sc.createClient(ServiceClient.ANON_OUT_IN_OP);

			MessageContext message = new MessageContext();
			Options opts = message.getOptions();
			opts.setTo(new EndpointReference(url));
			opts.setAction(actionName);
			message.setEnvelope(envelope);

			oc.addMessageContext(message);

			oc.execute(true);

			return oc.getMessageContext("In").getEnvelope();
		} catch (AxisFault e) {
			logger.warn("webservice调用时发生错误", e);
			logger.error("webservice调用时发生错误, mesg: ", e);
			return null;
		} catch (Exception e) {
			logger.warn("webservice调用时发生错误", e);
			logger.error("webservice调用时发生错误, mesg: ", e);
			return null;
		}
	}
	
	/**
	 * 调用WebService接口方法
	 * @param url   WebService接口地址
	 * @param inParam  入参
	 * @param method   方法名
	 * @return
	 */
	public static String callItmsService(String url, String inParam, String method)
	{
		logger.debug("callItmsService(url,inParam,method)");
		String returnParam = "";
		try
		{
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(url));
			call.setTimeout(30 * 1000);
			QName qn = new QName(url, method);
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			returnParam = (String) call.invoke(new Object[] { inParam});
		}
		catch (Exception e)
		{
			logger.error("call webservice[{}] method[{}] with param[{}] error.",url,method,inParam);
			logger.error(e.getMessage(), e);
		}
		return returnParam;
	}
	
	/**
	 * 访问服务
	 * @param wsdl
	 * @return
	 * @throws Exception
	 */
	public static String accessService(String wsdl, String userId, String newPassword, String sequenceId,
			String clientId, String clientPasswd){

		String gErrNo = "";
		// 拼接SOAP
		StringBuilder soapRequestData = new StringBuilder();

		soapRequestData.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:crm=\"http://crm.jx.ct10000.com/\">");
		soapRequestData.append("<soapenv:Header/>");
		soapRequestData.append("<soapenv:Body>");
		soapRequestData.append("<crm:ModifyIptvPassword>");
		soapRequestData.append("<crm:userID>" + userId + "</crm:userID>");
		soapRequestData.append("<crm:newPassword>" + newPassword + "</crm:newPassword>");
		soapRequestData.append("<crm:sequenceID>" + sequenceId + "</crm:sequenceID>");
		soapRequestData.append("<crm:invokeClient>");
		soapRequestData.append("<crm:clientId>" + clientId + "</crm:clientId>");
		soapRequestData.append("<crm:clientPasswd>" + clientPasswd + "</crm:clientPasswd>");
		soapRequestData.append("</crm:invokeClient>");
		soapRequestData.append("</crm:ModifyIptvPassword>");
		soapRequestData.append("</soapenv:Body>" + "</soapenv:Envelope>");
		PostMethod postMethod = new PostMethod(wsdl);
		// 然后把Soap请求数据添加到PostMethod中
		byte[] b = null;
		InputStream is = null;
		try {
			b = soapRequestData.toString().getBytes("utf-8");
			is = new ByteArrayInputStream(b, 0, b.length);
			RequestEntity re = new InputStreamRequestEntity(is, b.length, "text/xml; charset=UTF-8");
			postMethod.setRequestEntity(re);
			HttpClient httpClient = new HttpClient();
			int status = httpClient.executeMethod(postMethod);
			
			if (status == 200) {
				String soapResponseData = postMethod.getResponseBodyAsString();
				logger.warn("userID[{}]-调用接口回参为-[{}]", userId, soapResponseData);
				if(!StringUtil.IsEmpty(soapResponseData)){
					gErrNo = soapResponseData.split("<ModifyIptvPasswordResult>")[1].split("</ReturnCode>")[0].split("<ReturnCode>")[1].trim();
				}
			}
		} catch (Exception e) {
			logger.error("userID[{}]-调用接口异常，e[{}]", userId, e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		
		logger.warn("userID[{}]-调用接口返回码为-[{}]", userId, gErrNo);
		return gErrNo;
	}
}
