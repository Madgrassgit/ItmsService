package com.alcatel.asb.hdm.report.webservice.OUIMager;

import java.io.Serializable;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.FeedbackTicketsInfo;
import com.linkage.itms.dispatch.cqdx.service.OperateOuiService;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年7月11日
 * @category com.alcatel.asb.hdm.report.webservice.OUIMager
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
@WebService(endpointInterface="com.alcatel.asb.hdm.report.webservice.OUIMager.OperateOui")
@SOAPBinding(style=SOAPBinding.Style.RPC)
public class OperateOui implements Serializable
{
	 
	private static final long serialVersionUID = 1219280596614749668L;
	private static Logger logger = LoggerFactory.getLogger(OperateOui.class);
	
	@WebMethod
	@WebResult(name="response")
	@RequestWrapper(className="com.alcatel.asb.hdm.report.webservice.OUIMager.entity.Request",targetNamespace="requestTarget")
	@ResponseWrapper(className="com.alcatel.asb.hdm.report.webservice.OUIMager.entity.Response",targetNamespace="responseTarget")
    public com.alcatel.asb.hdm.report.webservice.OUIMager.entity.Response operateOui(
    		@WebParam(name="request") com.alcatel.asb.hdm.report.webservice.OUIMager.entity.Request request){
		logger.warn("feedbackWorkTicketsInfo ==> method{}",new Object[]{request.toString()});
		return  new OperateOuiService().managerOuiSrevice(request);
	}
}
