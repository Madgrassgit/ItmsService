package com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo;

import com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity.LogicIdResult;
import com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity.Request;
import com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity.Response;
import com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity.ResultArray;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.cqdx.service.FeedbackWorkTicketsInfoService;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 重庆电子回笼专用接口
 * @author jiafh
 *
 */
@WebService(endpointInterface="com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.FeedbackTicketsInfo")
@SOAPBinding(style=SOAPBinding.Style.RPC)
public class FeedbackTicketsInfo implements Serializable
{
	private static Logger logger = LoggerFactory.getLogger(FeedbackTicketsInfo.class);
	private static final long serialVersionUID = 3379158374999427384L;
	private static SAXReader reader = new SAXReader();

	@SuppressWarnings("unchecked")
	@WebMethod
	@WebResult(name="response")
	@RequestWrapper(className="com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity.Request", targetNamespace="requestTarget")
	@ResponseWrapper(className="com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity.Response", targetNamespace="responseTarget")
	public Response feedbackWorkTicketsInfo(@WebParam(name="request") Request request){
		if(request == null){
			logger.warn("feedbackWorkTicketsInfo with get request null");
			return null;
		}
		logger.warn("feedbackWorkTicketsInfo==>方法开始{}", request.toString());
		String workId = request.getWork_id();
		Response response = new Response(workId);  
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("work_id").addText(workId);
		    Element loidArrE = intRequest.addElement("LOID_ARRAY");
		    
		    List<String> loidList = request.getLOID_ARRAY().getLoid();
		    for(String loid : loidList){
		    	loidArrE.addElement("loid").addText(StringUtil.getStringValue(loid));
		    }
		    
	        String resultReturn = new FeedbackWorkTicketsInfoService().work(intRequest.asXML());
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			Element outArr = outRoot.element("RESULT_ARRAY"); 
			ResultArray resultArray = new ResultArray();
			List<Element> resultElement = outArr.elements();
			LogicIdResult logicIdResult = null;
			List<LogicIdResult> logicIdResultList = new ArrayList<LogicIdResult>();
			for (int i = 0 ; i < resultElement.size(); i++) {
				Element e = resultElement.get(i);
				logicIdResult = new LogicIdResult();
				logicIdResult.setLoid(StringUtil.getStringValue(e.elementTextTrim("loid")));
				logicIdResult.setError_msg(StringUtil.getStringValue(e.elementTextTrim("error_msg")));
				logicIdResult.setService_list(StringUtil.getStringValue(e.elementTextTrim("service_list")));
				logicIdResult.setService_status(StringUtil.getIntegerValue(e.elementTextTrim("service_status")));
				logicIdResult.setSerial_number(StringUtil.getStringValue(e.elementTextTrim("serial_number")));
				logicIdResultList.add(logicIdResult);
			}
			
			resultArray.setLoid_result(logicIdResultList);
			response.setRESULT_ARRAY(resultArray);
			logger.warn("feedbackWorkTicketsInfo==>方法结束{}", response.toString());
		} catch (Exception e) {
			logger.error("feedbackWorkTicketsInfo.getXML() is error!", e);
		}
	    return response;
	}
}