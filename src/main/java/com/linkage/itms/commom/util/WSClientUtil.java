package com.linkage.itms.commom.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import org.apache.axis.AxisEngine;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.soap.SOAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * WebService 工具类
 * @author zhangsm
 * @version 1.0
 * @since 2011-9-26 下午04:18:49
 * @category com.linkage.common<br>
 * @copyright 亚信联创 网管产品部
 */
public class WSClientUtil
{
	private static Logger logger = LoggerFactory.getLogger(WSClientUtil.class);
	
	/**
	 * 调用WebService接口方法
	 * @param url   WebService接口地址
	 * @param inParam  入参
	 * @param method   方法名
	 * @return
	 */
	public static String callRemoteService(String url, String inParam, String method)
	{
		String returnParam = "";
		try
		{
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(url));
			QName qn = new QName(url, method);
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			returnParam = (String) call.invoke(new Object[] { inParam.toString() });
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		return returnParam;
	}
	
	/**
	 * 重庆工厂复位返回接口
	 * @param url
	 * @param inParam
	 * @param method
	 * @param nameSpace
	 * @param requetName
	 * @param responseName
	 * @return
	 */
	public static String callRemoteService(String url,String inParam,String method,String nameSpace,String requetName,String responseName){
		
        String _resp = null;
		try {
			OperationDesc oper = new OperationDesc();
			oper.setName(method);
			ParameterDesc param = new ParameterDesc(new QName(nameSpace, requetName), ParameterDesc.IN, 
					new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, false, false);
			param.setOmittable(true);
			param.setNillable(true);
			oper.addParameter(param);
			oper.setReturnType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
			oper.setReturnClass(String.class);
			oper.setReturnQName(new QName(nameSpace, responseName));
			oper.setStyle(Style.WRAPPED);
			oper.setUse(Use.LITERAL);
			Service service = new Service();
			org.apache.axis.client.Call _call = (Call)service.createCall();
			_call.setOperation(oper);
			_call.setUseSOAPAction(true);
			_call.setSOAPActionURI("");
			_call.setEncodingStyle(null);
			_call.setProperty(Call.SEND_TYPE_ATTR, Boolean.FALSE);
			_call.setProperty(AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
			_call.setSOAPVersion(SOAPConstants.SOAP11_CONSTANTS);
			_call.setOperationName(new QName(nameSpace, method));
			_call.setTargetEndpointAddress(new URL(url));
			_resp = (String)_call.invoke(new java.lang.Object[] {inParam});
		} catch (MalformedURLException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (RemoteException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (ServiceException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
        return (String)_resp;
	}
	
	public static void main(String[] args) throws MalformedURLException, RemoteException, ServiceException {
		
		StringBuffer inParam = new StringBuffer();
		inParam.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		inParam.append("<ROOT>");
		inParam.append("<op_id>ECF20150511000001000</op_id>");
		inParam.append("<time>2015-11-26 18:18:00</time>");
		inParam.append("<result>1</result>");
		inParam.append("<err_msg>成功</err_msg>");
		inParam.append("</ROOT>");
		
		//aaaa("http://136.3.243.201:7337/services/FactoryResetService",inParam.toString());
		System.out.println(callRemoteService("http://136.3.243.201:7337/services/FactoryResetService",inParam.toString(),"getFactoryResetReturnDiag",
				"http://api.server.factoryreset.endpoint.uip.cqpf.cqcis.com/","arg0","return"));
	}
}
