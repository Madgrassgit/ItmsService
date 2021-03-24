package com.alcatel.hdm.nbi.mo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebResult;
import javax.xml.rpc.holders.IntHolder;
import javax.xml.rpc.holders.LongHolder;
import javax.xml.rpc.holders.StringHolder;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alcatel.hdm.nbi.mo.holders.LanInfoTypeHolder;
import com.alcatel.hdm.nbi.mo.holders.Loid_resultTypeHolder;
import com.alcatel.hdm.nbi.mo.holders.PingResultHolder;
import com.alcatel.hdm.nbi.mo.holders.ServiceStatusHolder;
import com.alcatel.hdm.nbi.mo.holders.UserInfoHolder;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dispatch.cqdx.beanObj.ChangeWifiPasswordResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.DHCPAddressType;
import com.linkage.itms.dispatch.cqdx.beanObj.DHCPType;
import com.linkage.itms.dispatch.cqdx.beanObj.FactoryResetResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.FeedbackWorkTicketsInfoResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.GetFactoryResetDiagResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.GetGetServiceStatusResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.LanInfoType;
import com.linkage.itms.dispatch.cqdx.beanObj.Loid_resultArray;
import com.linkage.itms.dispatch.cqdx.beanObj.Loid_resultType;
import com.linkage.itms.dispatch.cqdx.beanObj.PingPara;
import com.linkage.itms.dispatch.cqdx.beanObj.PingResult;
import com.linkage.itms.dispatch.cqdx.beanObj.QueryActionInfoRequest;
import com.linkage.itms.dispatch.cqdx.beanObj.QueryActionInfoResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.QueryBindInfoRequest;
import com.linkage.itms.dispatch.cqdx.beanObj.QueryBindInfoResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.QueryBussinessInfoRequest;
import com.linkage.itms.dispatch.cqdx.beanObj.QueryBussinessInfoResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.QueryConnectionInfoResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.QueryRgModeInfoResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.QueryTerminalInfoResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.QueryTerminalPasswordRequest;
import com.linkage.itms.dispatch.cqdx.beanObj.QueryTerminalPasswordResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.QueryWorkTicketsInfoResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.RESULT_ARRAYType;
import com.linkage.itms.dispatch.cqdx.beanObj.ServiceStatus;
import com.linkage.itms.dispatch.cqdx.beanObj.StartGetUserInfoDiagResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.StartPingDiagResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.StartRebootDiagResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.UnbindWorkTicketResponse;
import com.linkage.itms.dispatch.cqdx.beanObj.UserInfo;
import com.linkage.itms.dispatch.cqdx.service.ChangeWifiPasswordService;
import com.linkage.itms.dispatch.cqdx.service.CommonInterfaceOperationService;
import com.linkage.itms.dispatch.cqdx.service.FeedbackWorkTicketsInfoService;
import com.linkage.itms.dispatch.cqdx.service.GetFactoryResetDiagService;
import com.linkage.itms.dispatch.cqdx.service.GetGetServiceStatusService;
import com.linkage.itms.dispatch.cqdx.service.QueryActionInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryBindInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryBussinessInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryConnectionInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryLanInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryRgModeInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryTerminalInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryTerminalPasswdService;
import com.linkage.itms.dispatch.cqdx.service.QueryWorkTicketsInfoService;
import com.linkage.itms.dispatch.cqdx.service.ServiceReDoneService;
import com.linkage.itms.dispatch.cqdx.service.StartGetUserInfoDiagService;
import com.linkage.itms.dispatch.cqdx.service.StartPingDiagService;
import com.linkage.itms.dispatch.cqdx.service.StartRebootDiagService;
import com.linkage.itms.dispatch.cqdx.service.UnbindWorkTicketService;


/**
 * 重庆电信对外接口入口
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年3月17日
 *
 */
public class MobileOperationInterface
{
    private static Logger logger = LoggerFactory.getLogger(MobileOperationInterface.class);

    private static String logic_id;
    private static String ppp_username;
    private static String ppp_password;
    private static String serial_number;
    private static String work_id;
    private static String terminalPassword;
    private static String terminal_type;
    private static String terminal_name;
    private static String software_version;
    private static String hardware_version;
    private static String serialnumber;
    private static String ip_address;    
    private static String failedReason;
    private static String status;        
    private static int result;
    private static String err_msg;  
    private static SAXReader reader = new SAXReader();
  
  
    
    public void startGetUserInfoDiag(LongHolder op_id, String logic_id, String ppp_usename, String customer_id, String serial_number, String auth_username, IntHolder result, UserInfoHolder userinfo, StringHolder err_msg) throws java.rmi.RemoteException {
    	logger.warn("startGetUserInfoDiag==>方法开始{}", new Object[] { op_id });
	    try {
		    Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(StringUtil.getStringValue(op_id));
			intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
			intRequest.addElement("ppp_usename").addText(StringUtil.getStringValue(ppp_usename));
			intRequest.addElement("customer_id").addText(StringUtil.getStringValue(customer_id));
			intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
			intRequest.addElement("auth_username").addText(StringUtil.getStringValue(auth_username));
		 
	        String resultReturn = new StartGetUserInfoDiagService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			op_id.value = StringUtil.getLongValue(outRoot.elementTextTrim("op_id"));
			result.value = StringUtil.getIntegerValue(outRoot.elementTextTrim("result"));
			err_msg.value = outRoot.elementTextTrim("err_msg");
			UserInfo userinfoObj = new UserInfo();
			if(0 == result.value){
				Element userInfo = outRoot.element("userinfo");
				logic_id = userInfo.elementTextTrim("logic_id");
				ppp_usename = userInfo.elementTextTrim("ppp_usename");
				ppp_password = userInfo.elementTextTrim("ppp_password");
				serial_number = userInfo.elementTextTrim("serial_number");
			    customer_id = userInfo.elementTextTrim("customer_id");
			    auth_username = userInfo.elementTextTrim("auth_username");
			    userinfoObj.setLogic_id(logic_id);
			    userinfoObj.setPpp_password(ppp_password);
			    userinfoObj.setPpp_usename(ppp_usename);
			    userinfoObj.setSerial_number(serial_number);
			    userinfoObj.setCustomer_id(customer_id);
			    userinfoObj.setAuth_username(auth_username);
			}
			userinfo.value = userinfoObj;

		    logger.warn("startGetUserInfoDiag==>方法结束{}-{}",op_id,userinfo.toString());
		} catch (Exception e) {
			logger.error("StartGetUserInfoDiagDealXML.getXML() is error!", e);
		}
    }
    /**
     * 用户信息查询接口
     * @param startGetUserInfoDiag
     * @returnstartGetUserInfoDiag mo
     */
    public static StartGetUserInfoDiagResponse startGetUserInfoDiag_bak(long op_id,String logic_id,String ppp_usename,String customer_id,String serial_number,String auth_username)
    {
        logger.warn("startGetUserInfoDiag==>方法开始{}", new Object[] { op_id });
        StartGetUserInfoDiagResponse response = new StartGetUserInfoDiagResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("op_id").addText(StringUtil.getStringValue(op_id));
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_usename").addText(StringUtil.getStringValue(ppp_usename));
		    intRequest.addElement("customer_id").addText(StringUtil.getStringValue(customer_id));
		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
	  	    intRequest.addElement("auth_username").addText(StringUtil.getStringValue(auth_username));
		 
	        String resultReturn = new StartGetUserInfoDiagService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			op_id = StringUtil.getLongValue(outRoot.elementTextTrim("op_id"));
			result = StringUtil.getIntegerValue(outRoot.elementTextTrim("result"));
			err_msg = outRoot.elementTextTrim("err_msg");
			UserInfo userinfoObj = new UserInfo();
			if(0 == result){
				Element userinfo = outRoot.element("userinfo");
				logic_id = userinfo.elementTextTrim("logic_id");
				ppp_usename = userinfo.elementTextTrim("ppp_usename");
				ppp_password = userinfo.elementTextTrim("ppp_password");
				serial_number = userinfo.elementTextTrim("serial_number");
			    customer_id = userinfo.elementTextTrim("customer_id");
			    auth_username = userinfo.elementTextTrim("auth_username");
			    userinfoObj.setLogic_id(logic_id);
			    userinfoObj.setPpp_password(ppp_password);
			    userinfoObj.setPpp_usename(ppp_usename);
			    userinfoObj.setSerial_number(serial_number);
			    userinfoObj.setCustomer_id(customer_id);
			    userinfoObj.setAuth_username(auth_username);
			}
		    
		    response.setOp_id(StringUtil.getLongValue(op_id));
		    response.setResult(result);
		    response.setUserinfo(userinfoObj);
		    response.setErr_msg(err_msg);
		    logger.warn("startGetUserInfoDiag==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("StartGetUserInfoDiagDealXML.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    
    
    public void startPingDiag(LongHolder op_id, String logic_id, String ppp_usename, String serial_number, String customer_id, PingPara ping_para, IntHolder result, PingResultHolder ping_result, StringHolder err_msg) throws java.rmi.RemoteException {
    	logger.warn("startPingDiag==>方法开始{}", new Object[] { op_id });
        StartPingDiagResponse response = new StartPingDiagResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("op_id").addText(StringUtil.getStringValue(op_id.value));
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_usename").addText(StringUtil.getStringValue(ppp_usename));
		    intRequest.addElement("customer_id").addText(StringUtil.getStringValue(customer_id));
		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
	  	    Element pingparaE = intRequest.addElement("ping_para");
	  	    pingparaE.addElement("ping_host").addText(StringUtil.getStringValue(ping_para.getPing_host()));
	  	    pingparaE.addElement("ping_times").addText(StringUtil.getStringValue(ping_para.getPing_times()));
	  	    pingparaE.addElement("block_size").addText(StringUtil.getStringValue(ping_para.getBlock_size()));
		 
	        String resultReturn = new StartPingDiagService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			op_id.value = StringUtil.getLongValue(outRoot.elementTextTrim("op_id"));
			result.value = StringUtil.getIntegerValue(outRoot.elementTextTrim("result"));
			err_msg.value = outRoot.elementTextTrim("err_msg");
		    PingResult pingRsult = new PingResult();
			if(0 == result.value){
				Element ping_resultXML = outRoot.element("ping_result");
			    pingRsult.setAverage_responsetime(StringUtil.getIntegerValue(ping_resultXML.elementTextTrim("success_count")));
			    pingRsult.setFailure_count(StringUtil.getIntegerValue(ping_resultXML.elementTextTrim("failure_count")));
			    pingRsult.setAverage_responsetime(StringUtil.getIntegerValue(ping_resultXML.elementTextTrim("average_responsetime")));
			    pingRsult.setMinimum_responsetime(StringUtil.getIntegerValue(ping_resultXML.elementTextTrim("minimum_responsetime")));
			    pingRsult.setMaximum_responsetime(StringUtil.getIntegerValue(ping_resultXML.elementTextTrim("maximum_responsetime")));
			}
			ping_result.value = pingRsult;
			response.setPing_result(pingRsult);
			logger.warn("startPingDiag==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("StartGetUserInfoDiagDealXML.getXML() is error!", e);
		}
    }
    
    /**
     * ping接口
     * @param startPingDiag
     * @return
     */
    public static StartPingDiagResponse startPingDiag_bak(long op_id,String logic_id,String ppp_usename,String serial_number,String customer_id,PingPara ping_para)
    {
        logger.warn("startPingDiag==>方法开始{}", new Object[] { op_id });
        StartPingDiagResponse response = new StartPingDiagResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("op_id").addText(StringUtil.getStringValue(op_id));
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_usename").addText(StringUtil.getStringValue(ppp_usename));
		    intRequest.addElement("customer_id").addText(StringUtil.getStringValue(customer_id));
		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
	  	    Element pingparaE = intRequest.addElement("ping_para");
	  	    pingparaE.addElement("ping_host").addText(StringUtil.getStringValue(ping_para.getPing_host()));
	  	    pingparaE.addElement("ping_times").addText(StringUtil.getStringValue(ping_para.getPing_times()));
	  	    pingparaE.addElement("block_size").addText(StringUtil.getStringValue(ping_para.getBlock_size()));
		 
	        String resultReturn = new StartPingDiagService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			op_id = StringUtil.getLongValue(outRoot.elementTextTrim("op_id"));
			result = StringUtil.getIntegerValue(outRoot.elementTextTrim("result"));
			err_msg = outRoot.elementTextTrim("err_msg");
			response.setOp_id(op_id);
		    response.setResult(result);
		    response.setErr_msg(err_msg);
		    PingResult pingRsult = new PingResult();
			if(0 == result){
				Element ping_result = outRoot.element("ping_result");
			    pingRsult.setAverage_responsetime(StringUtil.getIntegerValue(ping_result.elementTextTrim("success_count")));
			    pingRsult.setFailure_count(StringUtil.getIntegerValue(ping_result.elementTextTrim("failure_count")));
			    pingRsult.setAverage_responsetime(StringUtil.getIntegerValue(ping_result.elementTextTrim("average_responsetime")));
			    pingRsult.setMinimum_responsetime(StringUtil.getIntegerValue(ping_result.elementTextTrim("minimum_responsetime")));
			    pingRsult.setMaximum_responsetime(StringUtil.getIntegerValue(ping_result.elementTextTrim("maximum_responsetime")));
			}
			response.setPing_result(pingRsult);
		} catch (Exception e) {
			logger.error("StartGetUserInfoDiagDealXML.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    
    
    public void startRebootDiag(LongHolder op_id, String logic_id, String ppp_usename, String customer_id, String serial_number, IntHolder result, StringHolder err_msg) throws java.rmi.RemoteException {
    	logger.warn("startRebootDiag==>方法开始{}", new Object[] { op_id });
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("op_id").addText(StringUtil.getStringValue(op_id.value));
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_usename").addText(StringUtil.getStringValue(ppp_usename));
		    intRequest.addElement("customer_id").addText(StringUtil.getStringValue(customer_id));
		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
		 
	        String resultReturn = new StartRebootDiagService().work(intRequest.asXML());
	        logger.warn("StartRebootDiagService==>方法結束返回{}", new Object[] { resultReturn });
	        
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			op_id.value = StringUtil.getLongValue(outRoot.elementTextTrim("op_id"));
			result.value = StringUtil.getIntegerValue(outRoot.elementTextTrim("result"));
			err_msg.value = outRoot.elementTextTrim("err_msg");
			logger.warn("startRebootDiag==>方法结束{}", new Object[] { op_id });
		} catch (Exception e) {
			logger.error("StartGetUserInfoDiagDealXML.getXML() is error!", e);
		}
    }
    
    /**
     * 重启接口
     * @param startRebootDiag startRebootDiag bean
     * @return
     */
    public static StartRebootDiagResponse startRebootDiag_bak(long op_id,String logic_id,String ppp_usename,String customer_id,String serial_number)
    {
        logger.warn("startRebootDiag==>方法开始{}", new Object[] { op_id });
        StartRebootDiagResponse response = new StartRebootDiagResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("op_id").addText(StringUtil.getStringValue(op_id));
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_usename").addText(StringUtil.getStringValue(ppp_usename));
		    intRequest.addElement("customer_id").addText(StringUtil.getStringValue(customer_id));
		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
		 
	        String resultReturn = new StartRebootDiagService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			op_id = StringUtil.getLongValue(outRoot.elementTextTrim("op_id"));
			result = StringUtil.getIntegerValue(outRoot.elementTextTrim("result"));
			err_msg = outRoot.elementTextTrim("err_msg");
			response.setOp_id(op_id);
		    response.setResult(result);
		    response.setErr_msg(err_msg);
		} catch (Exception e) {
			logger.error("StartGetUserInfoDiagDealXML.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    
    
    
    public void getFactoryResetDiag(LongHolder op_id, String logic_id, String ppp_usename, String customer_id, String serial_number, IntHolder result, StringHolder err_msg) throws java.rmi.RemoteException {
    	logger.warn("getFactoryResetDiag==>方法开始{}", new Object[] { op_id});
        //GetFactoryResetDiagResponse response = new GetFactoryResetDiagResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("op_id").addText(StringUtil.getStringValue(op_id.value));
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_usename").addText(StringUtil.getStringValue(ppp_usename));
		    intRequest.addElement("customer_id").addText(StringUtil.getStringValue(customer_id));
		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
		    GetFactoryResetDiagService serviceThread = new GetFactoryResetDiagService();
		    serviceThread.setMessage(intRequest.asXML());
		    Global.G_BlThreadPool.execute(serviceThread);
		  
			op_id.value = StringUtil.getLongValue(op_id);
			result.value = StringUtil.getIntegerValue("0");
			err_msg.value = "接收成功";
			logger.warn("getFactoryResetDiag==>方法结束{}", new Object[] { op_id});
		} catch (Exception e) {
			logger.error("StartGetUserInfoDiagDealXML.getXML() is error!", e);
		}
    }
    /**
     * 工厂复位接口
     * @param startRebootDiag
     * @return
     */
    public static GetFactoryResetDiagResponse getFactoryResetDiag_bak(long op_id,String logic_id,String ppp_usename,String customer_id,String serial_number)
    {
        logger.warn("getFactoryResetDiag==>方法开始{}", new Object[] { op_id});
        GetFactoryResetDiagResponse response = new GetFactoryResetDiagResponse();
	    try {
//		    Document inDocument = DocumentHelper.createDocument();
//		    Element intRequest = inDocument.addElement("request");
//		    intRequest.addElement("op_id").addText(StringUtil.getStringValue(op_id));
//		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
//		    intRequest.addElement("ppp_usename").addText(StringUtil.getStringValue(ppp_usename));
//		    intRequest.addElement("customer_id").addText(StringUtil.getStringValue(customer_id));
//		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
//		 
//	        String resultReturn = new GetFactoryResetDiagService().work(intRequest.asXML());
//		  
//		   	Document outDocument = reader.read(new StringReader(resultReturn));
//			Element outRoot = outDocument.getRootElement();
//			op_id = StringUtil.getLongValue(outRoot.elementTextTrim("op_id"));
//			result = StringUtil.getIntegerValue(outRoot.elementTextTrim("result"));
//			err_msg = outRoot.elementTextTrim("err_msg");
//			response.setOp_id(op_id);
//		    response.setResult(result);
//		    response.setErr_msg(err_msg);
		    logger.warn("getFactoryResetDiag==>方法结束{}", new Object[] { op_id});
		} catch (Exception e) {
			logger.error("StartGetUserInfoDiagDealXML.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    
    
    public void getGetServiceStatus(StringHolder op_id, String logic_id, String ppp_usename, String customer_id, String serial_number, IntHolder result, ServiceStatusHolder service_status, StringHolder err_msg) throws java.rmi.RemoteException {
    	logger.warn("getGetServiceStatus==>方法开始{}", new Object[] { op_id });
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("op_id").addText(StringUtil.getStringValue(op_id.value));
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_usename").addText(StringUtil.getStringValue(ppp_usename));
		    intRequest.addElement("customer_id").addText(StringUtil.getStringValue(customer_id));
		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
		 
	        String resultReturn = new GetGetServiceStatusService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			op_id.value = StringUtil.getStringValue(outRoot.elementTextTrim("op_id"));
			result.value = StringUtil.getIntegerValue(outRoot.elementTextTrim("result"));
			err_msg.value = StringUtil.getStringValue(outRoot.elementTextTrim("err_msg"));
		    ServiceStatus servStatus = new ServiceStatus();
			if(0 == result.value){
				Element service_statusXML = outRoot.element("service_status");
				servStatus.setWband_status(StringUtil.getIntegerValue(service_statusXML.elementTextTrim("wband_status")));
				servStatus.setIptv_status(StringUtil.getIntegerValue(service_statusXML.elementTextTrim("iptv_status")));
				servStatus.setVoip1_status(StringUtil.getIntegerValue(service_statusXML.elementTextTrim("voip1_status")));
				servStatus.setVoip2_status(StringUtil.getIntegerValue(service_statusXML.elementTextTrim("voip2_status")));
			}
			service_status.value = servStatus;
			logger.warn("getGetServiceStatus==>方法结束{}", new Object[] { op_id });
		} catch (Exception e) {
			logger.error("getGetServiceStatus.getXML() is error!", e);
		}
    }
    
    /**
     * 业务查询接口
     * @param getGetServiceStatus
     * @return
     */
    public static GetGetServiceStatusResponse getGetServiceStatus_bak(long op_id,String logic_id,String ppp_usename,String customer_id,String serial_number)
    {
        logger.warn("getGetServiceStatus==>方法开始{}", new Object[] { op_id });
        GetGetServiceStatusResponse response = new GetGetServiceStatusResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("op_id").addText(StringUtil.getStringValue(op_id));
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_usename").addText(StringUtil.getStringValue(ppp_usename));
		    intRequest.addElement("customer_id").addText(StringUtil.getStringValue(customer_id));
		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
		 
	        String resultReturn = new GetGetServiceStatusService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			op_id = StringUtil.getLongValue(outRoot.elementTextTrim("op_id"));
			result = StringUtil.getIntegerValue(outRoot.elementTextTrim("result"));
			err_msg = StringUtil.getStringValue(outRoot.elementTextTrim("err_msg"));
			response.setOp_id(op_id);
		    response.setResult(result);
		    response.setErr_msg(err_msg);
		    ServiceStatus servStatus = new ServiceStatus();
			if(0 == result){
				Element service_status = outRoot.element("service_status");
				servStatus.setWband_status(StringUtil.getIntegerValue(service_status.elementTextTrim("wband_status")));
				servStatus.setIptv_status(StringUtil.getIntegerValue(service_status.elementTextTrim("iptv_status")));
				servStatus.setVoip1_status(StringUtil.getIntegerValue(service_status.elementTextTrim("voip1_status")));
				servStatus.setVoip2_status(StringUtil.getIntegerValue(service_status.elementTextTrim("voip2_status")));
			}
			response.setService_status(servStatus);
		} catch (Exception e) {
			logger.error("getGetServiceStatus.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    /**
     * 查询终端超级密码接口
     * @param queryTerminalPassword
     * @return
     */
    public static QueryTerminalPasswordResponse queryTerminalPassword(QueryTerminalPasswordRequest request)
    {
        logger.warn("queryTerminalPassword==>方法开始{}", new Object[] { request.toString() });
        QueryTerminalPasswordRequest queryTerminalPassword = request;
        logic_id = StringUtil.getStringValue(queryTerminalPassword.getLoId());
        ppp_username = StringUtil.getStringValue(queryTerminalPassword.getPppoe());
        work_id = StringUtil.getStringValue(queryTerminalPassword.getWorkId());
        QueryTerminalPasswordResponse response = new QueryTerminalPasswordResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("loId").addText(logic_id);
		    intRequest.addElement("pppoe").addText(ppp_username);
		    intRequest.addElement("workId").addText(work_id);
		 
	        String resultReturn = new QueryTerminalPasswdService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			work_id = StringUtil.getStringValue(outRoot.elementTextTrim("workId"));
			logic_id = StringUtil.getStringValue(outRoot.elementTextTrim("loId"));
			ppp_username = StringUtil.getStringValue(outRoot.elementTextTrim("pppoe"));
			terminalPassword = StringUtil.getStringValue(outRoot.elementTextTrim("terminalPassword"));
			response.setLoId(logic_id);
			response.setPppoe(ppp_username);
			response.setTerminalPassword(terminalPassword);
			response.setWorkId(work_id);
			logger.warn("queryTerminalPassword==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("queryTerminalPassword.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    
    /**
     * 绑定设备信息接口
     * @param queryBindInfo
     * @return
     */
    public static QueryBindInfoResponse queryBindInfo(QueryBindInfoRequest request)
    {
        logger.warn("queryBindInfo==>方法开始{}", new Object[] { request.toString() });
        logic_id = StringUtil.getStringValue(request.getLoId());
        ppp_username = StringUtil.getStringValue(request.getPppoe());
        work_id = StringUtil.getStringValue(request.getWorkId());
        QueryBindInfoResponse response = new QueryBindInfoResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("logic_id").addText(logic_id);
		    intRequest.addElement("ppp_username").addText(ppp_username);
		    intRequest.addElement("work_id").addText(StringUtil.IsEmpty(work_id)?"12345678":work_id);
		 
	        String resultReturn = new QueryBindInfoService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			result = StringUtil.getIntegerValue(outRoot.elementTextTrim("result_code"));
			Element result_info = outRoot.element("result_info");
			work_id = StringUtil.getStringValue(result_info.elementTextTrim("work_id"));
			logic_id = StringUtil.getStringValue(result_info.elementTextTrim("logic_id"));
			ppp_username = StringUtil.getStringValue(result_info.elementTextTrim("ppp_username"));
			terminal_type = StringUtil.getStringValue(result_info.elementTextTrim("terminal_type")); 
			terminal_name = StringUtil.getStringValue(result_info.elementTextTrim("terminal_name"));
			software_version = StringUtil.getStringValue(result_info.elementTextTrim("software_version"));
			hardware_version = StringUtil.getStringValue(result_info.elementTextTrim("hardware_version"));
			serialnumber = StringUtil.getStringValue(result_info.elementTextTrim("serial_number"));
			ip_address = StringUtil.getStringValue(result_info.elementTextTrim("ip_address"));
			response.setLoId(logic_id);
			response.setPppoe(ppp_username);
			response.setResult(result);
			response.setWorkId(work_id);
			response.setTerminalType(terminal_type);
			response.setTerminalName(terminal_name);
			response.setSoftwareVersion(software_version);
			response.setHardwareVersion(hardware_version);
			response.setSerialNumber(serialnumber);
			response.setIpAddress(ip_address);
			logger.warn("queryBindInfo==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("queryBindInfo.getXML() is error!", e);
			return null;
		}
        return response;
    }

    
    /**
     * 业务重发接口
     * @param factoryReset
     * @return
     */
    public static FactoryResetResponse factoryReset(QueryBindInfoRequest request)
    {
        logger.warn("factoryReset==>方法开始{}", new Object[] { request.toString() });
        logic_id = StringUtil.getStringValue(request.getLoId());
        ppp_username = StringUtil.getStringValue(request.getPppoe());
        work_id = StringUtil.getStringValue(request.getWorkId());
        FactoryResetResponse response = new FactoryResetResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("loId").addText(logic_id);
		    intRequest.addElement("pppoe").addText(ppp_username);
		    intRequest.addElement("workId").addText(StringUtil.IsEmpty(work_id)?"1234567":work_id);
		 
	        String resultReturn = new ServiceReDoneService().work(intRequest.asXML(),false);
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			work_id = StringUtil.getStringValue(outRoot.elementTextTrim("workId"));
			logic_id = StringUtil.getStringValue(outRoot.elementTextTrim("loId"));
			ppp_username = StringUtil.getStringValue(outRoot.elementTextTrim("pppoe"));
			status = StringUtil.getStringValue(outRoot.elementTextTrim("result_code")); 
			failedReason = StringUtil.getStringValue(outRoot.elementTextTrim("result_desc"));  
			response.setLoId(logic_id);
			response.setPppoe(ppp_username);
			response.setStatus("0".equals(status)?"01":"02");
			response.setWorkId(work_id);
			response.setFailedReason(failedReason);
			logger.warn("factoryReset==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("factoryReset.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    /**
     * 用户配置执行情况查询接口
     * @param queryBussinessInfo
     * @return
     */
    public static QueryBussinessInfoResponse queryBussinessInfo(QueryBussinessInfoRequest request)
    {
        logger.warn("queryBussinessInfo==>方法开始{}", new Object[] { request.toString() });
        logic_id = StringUtil.getStringValue(request.getLoId());
        ppp_username = StringUtil.getStringValue(request.getPppoe());
        work_id = StringUtil.getStringValue(request.getWorkId());
        QueryBussinessInfoResponse response = new QueryBussinessInfoResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("loId").addText(logic_id);
		    intRequest.addElement("pppoe").addText(ppp_username);
		    intRequest.addElement("workId").addText(work_id);
		 
	        String resultReturn = new QueryBussinessInfoService().work(intRequest.asXML());
	        logger.warn("resultReturn="+resultReturn);
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			work_id = StringUtil.getStringValue(outRoot.elementTextTrim("workId"));
			logic_id = StringUtil.getStringValue(outRoot.elementTextTrim("loId"));
			ppp_username = StringUtil.getStringValue(outRoot.elementTextTrim("pppoe"));
			result = StringUtil.getIntegerValue(outRoot.elementTextTrim("result")); 
			serial_number = StringUtil.getStringValue(outRoot.elementTextTrim("serial_number"));  
			String terminalType = StringUtil.getStringValue(outRoot.elementTextTrim("terminalType")); 
			String serviceList = StringUtil.getStringValue(outRoot.elementTextTrim("serviceList"));
			response.setLoId(logic_id);
			response.setPppoe(ppp_username);
			response.setWorkId(work_id);
			response.setResult(result);
			response.setSerial_number(serial_number);
			response.setTerminalType(terminalType);
			response.setServiceList(serviceList);
			logger.warn("queryBussinessInfo==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("queryBussinessInfo.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    /**
     * 用户配置信息查询接口
     * @param queryActionInfo
     * @return
     */
    public static QueryActionInfoResponse queryActionInfo(QueryActionInfoRequest request)
    {
        logger.warn("queryActionInfo==>方法开始{}", new Object[] { request.toString() });
        logic_id = StringUtil.getStringValue(request.getLoId());
        ppp_username = StringUtil.getStringValue(request.getPppoe());
        work_id = StringUtil.getStringValue(request.getWorkId());
        QueryActionInfoResponse response = new QueryActionInfoResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("loId").addText(logic_id);
		    intRequest.addElement("pppoe").addText(ppp_username);
		    intRequest.addElement("workId").addText(work_id);
		 
	        String resultReturn = new QueryActionInfoService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			work_id = StringUtil.getStringValue(outRoot.elementTextTrim("workId"));
			logic_id = StringUtil.getStringValue(outRoot.elementTextTrim("loId"));
			ppp_username = StringUtil.getStringValue(outRoot.elementTextTrim("pppoe"));
			String actionResult = StringUtil.getStringValue(outRoot.elementTextTrim("actionResult")); 
			String uplinkType = StringUtil.getStringValue(outRoot.elementTextTrim("uplinkType")); 
			response.setLoId(logic_id);
			response.setPppoe(ppp_username);
			response.setWorkId(work_id);
			response.setActionResult(actionResult);
			response.setUplinkType(uplinkType);
			logger.warn("queryActionInfo==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("queryActionInfo.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    
    public void queryTerminalInfo(String logic_id, String ppp_usename, StringHolder manufactory, StringHolder device_type, StringHolder version, StringHolder OUI, StringHolder serial_number, StringHolder terminal_type, StringHolder is_online) throws java.rmi.RemoteException {
    	logger.warn("queryTerminalInfo==>方法开始{}", new Object[] { logic_id });
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_usename").addText(StringUtil.getStringValue(ppp_usename));
		    
	        String resultReturn = new QueryTerminalInfoService().work(intRequest.asXML());
	        logger.warn("QueryTerminalInfoService==>方法结束{}", new Object[] { resultReturn });
	        
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			terminal_type.value = StringUtil.getStringValue(outRoot.elementTextTrim("terminal_type"));
			serial_number.value = StringUtil.getStringValue(outRoot.elementTextTrim("serial_number"));
			manufactory.value = StringUtil.getStringValue(outRoot.elementTextTrim("manufactory"));
			device_type.value = StringUtil.getStringValue(outRoot.elementTextTrim("device_type"));
			version.value = StringUtil.getStringValue(outRoot.elementTextTrim("version"));
			OUI.value = StringUtil.getStringValue(outRoot.elementTextTrim("OUI"));
			is_online.value = StringUtil.getStringValue(outRoot.elementTextTrim("is_online"));
			logger.warn("queryTerminalInfo==>结束{}", new Object[] { logic_id });
		} catch (Exception e) {
			logger.error("queryTerminalInfo.getXML() is error!", e);
		}
    }
    
    
    /**
     * 终端信息查询接口
     * @param queryTerminalInfo
     * @return
     */
    public static QueryTerminalInfoResponse queryTerminalInfo_bak(String logic_id,String ppp_usename)
    {
        logger.warn("queryTerminalInfo==>方法开始{}", new Object[] { logic_id });
        QueryTerminalInfoResponse response = new QueryTerminalInfoResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_usename").addText(StringUtil.getStringValue(ppp_usename));
		    
	        String resultReturn = new QueryTerminalInfoService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			terminal_type = StringUtil.getStringValue(outRoot.elementTextTrim("terminal_type"));
			serial_number = StringUtil.getStringValue(outRoot.elementTextTrim("serial_number"));
			String manufactory = StringUtil.getStringValue(outRoot.elementTextTrim("manufactory"));
			String device_type = StringUtil.getStringValue(outRoot.elementTextTrim("device_type"));
			String version = StringUtil.getStringValue(outRoot.elementTextTrim("version"));
			String OUI = StringUtil.getStringValue(outRoot.elementTextTrim("OUI"));
			String is_online = StringUtil.getStringValue(outRoot.elementTextTrim("is_online"));
			
			response.setDevice_type(device_type);
			response.setIs_online(is_online);
			response.setOUI(OUI);
			response.setVersion(version);
			response.setManufactory(manufactory);
			response.setTerminal_type(terminal_type);
			response.setSerial_number(serial_number);
			logger.warn("queryTerminalInfo==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("queryTerminalInfo.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    
    
    public void feedbackWorkTicketsInfo(StringHolder work_id, String[] loid,Loid_resultTypeHolder RESULT_ARRAY) throws java.rmi.RemoteException {
    	logger.warn("feedbackWorkTicketsInfo==>方法开始{}", new Object[] { work_id.value });
        //List<LOID_ARRAYType> loidArr = LOID_ARRAY;
        
        //FeedbackWorkTicketsInfoResponse response = new FeedbackWorkTicketsInfoResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("work_id").addText(work_id.value);
		    Element loidArrE = intRequest.addElement("LOID_ARRAY");
		    logger.warn("length="+loid.length);
		    for(int i =0;i < loid.length;i++){
		    	String loid_ARRAYType = loid[i];
		    	loidArrE.addElement("loid").addText(StringUtil.getStringValue(loid_ARRAYType));
		    }
		    
	        String resultReturn = new FeedbackWorkTicketsInfoService().work(intRequest.asXML());
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			work_id.value = StringUtil.getStringValue(outRoot.elementTextTrim("work_id"));
			Element outArr = outRoot.element("RESULT_ARRAY"); 
			Loid_resultType result = null;
			@SuppressWarnings("unchecked")
			List<Element> resultElement = outArr.elements();
			Loid_resultType[] resultArr = new Loid_resultType[resultElement.size()];
			for (int i = 0 ; i < resultElement.size(); i++) {
				Element e = resultElement.get(i);
				result = new Loid_resultType();
				result.setLoid(StringUtil.getStringValue(e.elementTextTrim("loid")));
				result.setError_msg(StringUtil.getStringValue(e.elementTextTrim("error_msg")));
				result.setService_list(StringUtil.getStringValue(e.elementTextTrim("service_list")));
				result.setService_status(StringUtil.getIntegerValue(e.elementTextTrim("service_status")));
				result.setSerial_number(StringUtil.getStringValue(e.elementTextTrim("serial_number")));
				resultArr[i] = result;
			}
			//resultArrType.setLoid_result(resultArr);
			//response.setRESULT_ARRAY(resultArrType);
			Loid_resultArray loidresultArr = new Loid_resultArray();
			loidresultArr.setLoid_result(resultArr);
			RESULT_ARRAY.value = loidresultArr;
			logger.warn("feedbackWorkTicketsInfo==>方法结束{}", new Object[] { resultArr.toString() });
		} catch (Exception e) {
			logger.error("feedbackWorkTicketsInfo.getXML() is error!", e);
		}
    }
    /**
     * 绑定关系查询接口
     * @param feedbackWorkTicketsInfo
     * @return
     */
    public static FeedbackWorkTicketsInfoResponse feedbackWorkTicketsInfo_bak(String work_id, String[] loid)
    {
        logger.warn("feedbackWorkTicketsInfo==>方法开始{}", new Object[] { work_id });
        work_id = StringUtil.getStringValue(work_id);
        //List<LOID_ARRAYType> loidArr = LOID_ARRAY;
        
        FeedbackWorkTicketsInfoResponse response = new FeedbackWorkTicketsInfoResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("work_id").addText(StringUtil.getStringValue(work_id));
		    Element loidArrE = intRequest.addElement("LOID_ARRAY");
		    logger.warn("length="+loid.length);
		    for(int i =0;i < loid.length;i++){
		    	String loid_ARRAYType = loid[i];
		    	loidArrE.addElement("loid").addText(StringUtil.getStringValue(loid_ARRAYType));
		    }
		    
	        String resultReturn = new FeedbackWorkTicketsInfoService().work(intRequest.asXML());
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			work_id = StringUtil.getStringValue(outRoot.elementTextTrim("work_id"));
			Element outArr = outRoot.element("RESULT_ARRAY"); 
			RESULT_ARRAYType resultArrType = new RESULT_ARRAYType();
			Loid_resultType result = null;
			@SuppressWarnings("unchecked")
			List<Element> resultElement = outArr.elements();
			Loid_resultType[] resultArr = new Loid_resultType[resultElement.size()];
			for (int i = 0 ; i < resultElement.size(); i++) {
				Element e = resultElement.get(i);
				result = new Loid_resultType();
				result.setLoid(StringUtil.getStringValue(e.elementTextTrim("loid")));
				result.setError_msg(StringUtil.getStringValue(e.elementTextTrim("error_msg")));
				result.setService_list(StringUtil.getStringValue(e.elementTextTrim("service_list")));
				result.setService_status(StringUtil.getIntegerValue(e.elementTextTrim("service_status")));
				result.setSerial_number(StringUtil.getStringValue(e.elementTextTrim("serial_number")));
				resultArr[i] = result;
			}
			resultArrType.setLoid_result(resultArr);
			response.setWork_id(work_id);
			response.setRESULT_ARRAY(resultArrType);
			logger.warn("feedbackWorkTicketsInfo==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("feedbackWorkTicketsInfo.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    /**
     * 工单下发情况查询接口
     * @param queryWorkTicketsInfo
     * @return
     */
    public static QueryWorkTicketsInfoResponse queryWorkTicketsInfo(String logic_id)
    {
        logger.warn("queryWorkTicketsInfo==>方法开始{}", new Object[] { logic_id });
        QueryWorkTicketsInfoResponse response = new QueryWorkTicketsInfoResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		 
	        String resultReturn = new QueryWorkTicketsInfoService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			String work_asgn_id = StringUtil.getStringValue(outRoot.elementTextTrim("work_asgn_id"));
			String result = StringUtil.getStringValue(outRoot.elementTextTrim("result"));
			response.setResult(result);
			response.setWork_asgn_id(work_asgn_id);
			logger.warn("queryWorkTicketsInfo==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("queryWorkTicketsInfo.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    
    /**
     * 桥接/路由模式查询接口
     * @param queryWorkTicketsInfo
     * @return
     */
    public static QueryRgModeInfoResponse queryRgModeInfo(String logic_id)
    {
        logger.warn("queryRgModeInfo==>方法开始{}", new Object[] { logic_id });
        QueryRgModeInfoResponse response = new QueryRgModeInfoResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		 
	        String resultReturn = new QueryRgModeInfoService().work(intRequest.asXML());
	        logger.warn("queryRgModeInfo==>处理结果{}", new Object[] { resultReturn });
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			Element result = outRoot.element("result_info");
			String rgMode = StringUtil.getStringValue(result.elementTextTrim("terminal_rgmode"));
			response.setRgMode(rgMode);
			logger.warn("queryRgModeInfo==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("queryRgModeInfo.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    
    
    
    public void queryLanInfo(String logic_id, String ppp_username, String serial_number,LanInfoTypeHolder lan_info) throws java.rmi.RemoteException {
    	logger.warn("queryLanInfo==>方法开始{},{},{}", new Object[] { logic_id, ppp_username, serial_number });
        LanInfoType[] lanInfoType = null;
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_username").addText(StringUtil.getStringValue(ppp_username));
		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
		 
	        String resultReturn = new QueryLanInfoService().work(intRequest.asXML());
	        
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			Element lanInfoElement = outRoot.element("lan_infos");
			
			List<Element> lanList = lanInfoElement.elements();
			if(lanList.size() > 0){
				lanInfoType = new LanInfoType[lanList.size()];
				for(int i = 0 ; i < lanList.size(); i++){
					LanInfoType lanInfo = new LanInfoType();
					lanInfo.setName(lanList.get(i).elementText("name"));
					lanInfo.setAccept_byte_count(lanList.get(i).elementText("accept_byte_count"));
					lanInfo.setAccept_package_count(lanList.get(i).elementText("accept_package_count"));
					lanInfo.setConnection_rate(lanList.get(i).elementText("connection_rate"));
					lanInfo.setSend_byte_count(lanList.get(i).elementText("send_byte_count"));
					lanInfo.setSend_package_count(lanList.get(i).elementText("send_package_count"));
					lanInfo.setStatus(lanList.get(i).elementText("status"));
					lanInfoType[i] = lanInfo;
				}
			}else{
				lanInfoType = new LanInfoType[1];
				LanInfoType lanInfo = new LanInfoType();
				lanInfoType[0] = lanInfo;
			}
			lan_info.value = lanInfoType;
			logger.warn("queryLanInfo==>方法结束{}", new Object[] { lanInfoType.toString() });
		} catch (Exception e) {
			logger.error("queryLanInfo.getXML() is error!", e);
		}
    }
    
    
    
    /**
     * ONU的网口状态查询接口
     * @param queryLanInfo
     * @return
     */
    public static LanInfoType[] queryLanInfo_bak(String logic_id, String ppp_username,String serial_number)
    {
        logger.warn("queryLanInfo==>方法开始{},{},{}", new Object[] { logic_id, ppp_username, serial_number });
        LanInfoType[] lanInfoType = null;
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_username").addText(StringUtil.getStringValue(ppp_username));
		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
		 
	        String resultReturn = new QueryLanInfoService().work(intRequest.asXML());
	        
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			Element lanInfoElement = outRoot.element("lan_infos");
			
			List<Element> lanList = lanInfoElement.elements();
			if(lanList.size() > 0){
				lanInfoType = new LanInfoType[lanList.size()];
				for(int i = 0 ; i < lanList.size(); i++){
					LanInfoType lanInfo = new LanInfoType();
					lanInfo.setName(lanList.get(i).elementText("name"));
					lanInfo.setAccept_byte_count(lanList.get(i).elementText("accept_byte_count"));
					lanInfo.setAccept_package_count(lanList.get(i).elementText("accept_package_count"));
					lanInfo.setConnection_rate(lanList.get(i).elementText("connection_rate"));
					lanInfo.setSend_byte_count(lanList.get(i).elementText("send_byte_count"));
					lanInfo.setSend_package_count(lanList.get(i).elementText("send_package_count"));
					lanInfo.setStatus(lanList.get(i).elementText("status"));
					lanInfoType[i] = lanInfo;
				}
			}else{
				lanInfoType = new LanInfoType[1];
				LanInfoType lanInfo = new LanInfoType();
				lanInfoType[0] = lanInfo;
			}
			logger.warn("queryLanInfo==>方法结束{}", new Object[] { lanInfoType.toString() });
		} catch (Exception e) {
			logger.error("queryLanInfo.getXML() is error!", e);
			return null;
		}
        return lanInfoType;
    }
    
    /**
     * 设备解绑操作接口
     * @param unbindWorkTicket
     * @return
     */
    public static UnbindWorkTicketResponse unbindWorkTicket(String logic_id, String serial_number, String keep_user_info)
    {
        logger.warn("unbindWorkTicket==>方法开始({},{},{})", new Object[] { logic_id, serial_number, keep_user_info });
        UnbindWorkTicketResponse response = new UnbindWorkTicketResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
		    intRequest.addElement("keep_user_info").addText(StringUtil.getStringValue(keep_user_info));
		 
	        String resultReturn = new UnbindWorkTicketService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			int result = StringUtil.getIntegerValue(outRoot.elementTextTrim("result"));
			err_msg = StringUtil.getStringValue(outRoot.elementTextTrim("err_msg"));
			response.setResult(result);
			response.setErr_msg(err_msg);
			logger.warn("unbindWorkTicket==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("unbindWorkTicket.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    /**
     * ONU的网口状态查询接口
     * @param queryLanInfo
     * @return
     */
    public static QueryConnectionInfoResponse queryConnectionInfo(String logic_id, String ppp_username,String serial_number)
    {
        logger.warn("queryConnectionInfo==>方法开始({},{},{})", new Object[] { logic_id, ppp_username, serial_number });
        QueryConnectionInfoResponse response = new QueryConnectionInfoResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_username").addText(StringUtil.getStringValue(ppp_username));
		    intRequest.addElement("serial_number").addText(StringUtil.getStringValue(serial_number));
		 
	        String resultReturn = new QueryConnectionInfoService().work(intRequest.asXML());
	        
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			String result = StringUtil.getStringValue(outRoot.elementTextTrim("result"));
			String err_msg = StringUtil.getStringValue(outRoot.elementTextTrim("err_msg"));
			String dial_connect_status = StringUtil.getStringValue(outRoot.elementTextTrim("dial_connect_status"));
			String dns = StringUtil.getStringValue(outRoot.elementTextTrim("dns"));
			String send_power = StringUtil.getStringValue(outRoot.elementTextTrim("send_power"));
			String receive_power = StringUtil.getStringValue(outRoot.elementTextTrim("receive_power"));
			Element dhcpDocument = outRoot.element("dhcp");
			List<Element> dhcpList = dhcpDocument.elements();
			DHCPType dhcp = new DHCPType();
			DHCPAddressType[] dhcpAddrArr = null;
			if(dhcpList.size() > 0){
				dhcpAddrArr = new DHCPAddressType[dhcpList.size()];
				List<DHCPAddressType> dhcpAddrList = new ArrayList<DHCPAddressType>();
				for(int i = 0 ; i < dhcpList.size(); i++){
					DHCPAddressType dhcpAddr = new DHCPAddressType();
					dhcpAddr.setIp_address(StringUtil.getStringValue(dhcpList.get(i).elementText("ip_address")));
					dhcpAddr.setMac_address(StringUtil.getStringValue(dhcpList.get(i).elementText("mac_address")));
					dhcpAddrArr[i] = dhcpAddr;
					dhcpAddrList.add(dhcpAddr);
				}
				dhcp.setAddress(dhcpAddrArr);
			}else{
				dhcpAddrArr = new DHCPAddressType[1];
				DHCPAddressType dhcpAddr = new DHCPAddressType();
				dhcpAddrArr[0] = dhcpAddr;
				List<DHCPAddressType> dhcpAddrList = new ArrayList<DHCPAddressType>();
				dhcpAddrList.add(dhcpAddr);
				dhcp.setAddress(dhcpAddrArr);
			}
			response.setResult(StringUtil.getIntegerValue(result));
			response.setErr_msg(err_msg);
			response.setDial_connect_status(dial_connect_status);
			response.setDns(dns);
			response.setSend_power(send_power);
			response.setReceive_power(receive_power);
			response.setDhcp(dhcp);
			logger.warn("queryConnectionInfo==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("queryConnectionInfo.getXML() is error!", e);
			return null;
		}
        return response;
    }
    
    
    /**
     * 通用接口
     * @param commonInterfaceOperation
     * @return
     */
    @WebResult(name="response_xml")
    public static void commonInterfaceOperation(String request_xml,StringHolder response_xml)
    {
    	logger.warn("commonInterfaceOperation==>方法开始{}", new Object[] { request_xml });
        //CommonInterfaceOperationResponse response = new CommonInterfaceOperationResponse();
	    try {
//		    Document inDocument = DocumentHelper.createDocument();
//		    Element intRequest = inDocument.addElement("root");
//		    intRequest.addElement("request_xml").addText(StringUtil.getStringValue(request_xml));
		 
	        String resultReturn = new CommonInterfaceOperationService().work(request_xml);
	        String replaceUTF8 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; 
	        String replaceGBK = "<?xml version=\"1.0\" encoding=\"GBK\"?>"; 
	        resultReturn = resultReturn.replace(replaceUTF8, "").replace(replaceGBK, "");
//		   	Document outDocument = reader.read(new StringReader(resultReturn));
//			Element outRoot = outDocument.getRootElement();
//			Element lanInfoElement = outRoot.element("lan_info");
			response_xml.value = StringUtil.getStringValue(resultReturn);
			//response.setResponse_xml(response_xml);
			logger.warn("commonInterfaceOperation==>方法结束{}", new Object[] { resultReturn });
		} catch (Exception e) {
			logger.error("commonInterfaceOperation.getXML() is error!", e);
		}
    }
    
    
//    /**
//     * 通用接口
//     * @param commonInterfaceOperation
//     * @return
//     */
//    public static CommonInterfaceOperationResponse commonInterfaceOperation_bak(String request_xml)
//    {
//    	logger.warn("commonInterfaceOperation==>方法开始{}", new Object[] { request_xml });
//        CommonInterfaceOperationResponse response = new CommonInterfaceOperationResponse();
//        String response_xml = "";
//	    try {
////		    Document inDocument = DocumentHelper.createDocument();
////		    Element intRequest = inDocument.addElement("root");
////		    intRequest.addElement("request_xml").addText(StringUtil.getStringValue(request_xml));
//		 
//	        String resultReturn = new CommonInterfaceOperationService().work(request_xml);
//	        String replaceUTF8 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; 
//	        String replaceGBK = "<?xml version=\"1.0\" encoding=\"GBK\"?>"; 
//	        resultReturn = resultReturn.replace(replaceUTF8, "").replace(replaceGBK, "");
////		   	Document outDocument = reader.read(new StringReader(resultReturn));
////			Element outRoot = outDocument.getRootElement();
////			Element lanInfoElement = outRoot.element("lan_info");
//			response_xml = StringUtil.getStringValue(resultReturn);
//			response.setResponse_xml(response_xml);
//			logger.warn("commonInterfaceOperation==>方法结束{}", new Object[] { response.toString() });
//		} catch (Exception e) {
//			logger.error("commonInterfaceOperation.getXML() is error!", e);
//			return null;
//		}
//
//        return response_xml;
//    }
    
    
    
    
    public void changeWifiPassword(StringHolder logic_id, StringHolder ppp_usename, String ssid, String wifi_password, StringHolder failed_reason,IntHolder result) throws java.rmi.RemoteException {
    	logger.warn("changeWifiPassword==>方法开始({},{},{},{})", new Object[] { logic_id.value, ppp_usename.value, ssid, wifi_password });
        ChangeWifiPasswordResponse response = new ChangeWifiPasswordResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id.value));
		    intRequest.addElement("ppp_username").addText(StringUtil.getStringValue(ppp_usename.value));
		    intRequest.addElement("ssid").addText(StringUtil.getStringValue(ssid));
		    intRequest.addElement("wifi_password").addText(StringUtil.getStringValue(wifi_password));
		 
	        String resultReturn = new ChangeWifiPasswordService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			result.value = StringUtil.getIntegerValue(outRoot.elementTextTrim("result"));
			failed_reason.value = StringUtil.getStringValue(outRoot.elementTextTrim("failed_reason"));
			logic_id.value = StringUtil.getStringValue(outRoot.elementTextTrim("logic_id"));
			ppp_usename.value = StringUtil.getStringValue(outRoot.elementTextTrim("ppp_username"));
			logger.warn("changeWifiPassword==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("changeWifiPassword.getXML() is error!", e);
		}
    }
    /**
     * 修改WiFi密码接口
     * @param changeWifiPassword
     * @return
     */
    public static ChangeWifiPasswordResponse changeWifiPassword_bak(String logic_id, String ppp_username, String ssid, String wifi_password)
    {
        logger.warn("changeWifiPassword==>方法开始({},{},{},{})", new Object[] { logic_id, ppp_username, ssid, wifi_password });
        ChangeWifiPasswordResponse response = new ChangeWifiPasswordResponse();
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("root");
		    intRequest.addElement("logic_id").addText(StringUtil.getStringValue(logic_id));
		    intRequest.addElement("ppp_username").addText(StringUtil.getStringValue(ppp_username));
		    intRequest.addElement("ssid").addText(StringUtil.getStringValue(ssid));
		    intRequest.addElement("wifi_password").addText(StringUtil.getStringValue(wifi_password));
		 
	        String resultReturn = new ChangeWifiPasswordService().work(intRequest.asXML());
		  
		   	Document outDocument = reader.read(new StringReader(resultReturn));
			Element outRoot = outDocument.getRootElement();
			int result = StringUtil.getIntegerValue(outRoot.elementTextTrim("result"));
			String failed_reason = StringUtil.getStringValue(outRoot.elementTextTrim("failed_reason"));
			String loid = StringUtil.getStringValue(outRoot.elementTextTrim("logic_id"));
			String pppName = StringUtil.getStringValue(outRoot.elementTextTrim("ppp_username"));
			response.setResult(result);
			response.setFailed_reason(failed_reason);
			response.setLogic_id(loid);
			response.setPpp_username(pppName);
			logger.warn("changeWifiPassword==>方法结束{}", new Object[] { response.toString() });
		} catch (Exception e) {
			logger.error("changeWifiPassword.getXML() is error!", e);
			return null;
		}
        return response;
    }
}