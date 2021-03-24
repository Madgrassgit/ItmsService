package com.linkage.itms.dispatch.main;

import java.io.StringReader;

import javax.xml.rpc.holders.IntHolder;

import org.apache.commons.codec.binary.Base64;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.gsdx.beanObj.ArrayOf_tns1_UserDetail;
import com.linkage.itms.dispatch.gsdx.beanObj.NorthQueryParaResult;
import com.linkage.itms.dispatch.gsdx.beanObj.UserDetail;
import com.linkage.itms.dispatch.gsdx.beanObj.UserIndex;
import com.linkage.itms.dispatch.gsdx.holders.ArrayOf_tns1_UserDetailHolder;
import com.linkage.itms.dispatch.gsdx.holders.NorthQueryParaResultHolder;
import com.linkage.itms.dispatch.gsdx.service.DelSingleCPEService;
import com.linkage.itms.dispatch.gsdx.service.NorthQueryCPEParaService;
import com.linkage.itms.dispatch.gsdx.service.QueryUserDetailService;
import com.linkage.itms.dispatch.gsdx.service.ServiceChangeService;

/**
 * 甘肃电信业务发放场景接口入口类
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2019-5-29
 * @category com.linkage.itms.dispatch.main
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class OperationDistributeService
{
	private static Logger logger = LoggerFactory.getLogger(OperationDistributeService.class);
	private static SAXReader reader = new SAXReader();
	
	
	public void ServiceChange(String adAcount, String LSHNo, String orderType, String newPassWord, IntHolder result){
    	logger.warn("ServiceChange["+LSHNo+"]==>方法开始:adAcount[{}],LSHNo[{}],orderType[{}],newPassWord[{}]", new Object[] {adAcount, LSHNo, orderType, newPassWord});
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("LSHNo").addText(StringUtil.getStringValue(LSHNo));
		    intRequest.addElement("adAcount").addText(StringUtil.getStringValue(adAcount));
		    intRequest.addElement("orderType").addText(StringUtil.getStringValue(orderType));
		    newPassWord = StringUtil.getStringValue(newPassWord);
		    intRequest.addElement("newPassWord").addText(new String(Base64.decodeBase64(
		    		newPassWord.substring("wband_password=".length(),newPassWord.length()).getBytes("GBK"))));
	        String resultXml = new ServiceChangeService("ServiceChange").work(intRequest.asXML());
	        logger.warn("ServiceChange["+LSHNo+"]==>方法結束返回{}", new Object[] { resultXml });
	        
		   	Document outDocument = reader.read(new StringReader(resultXml));
			Element outRoot = outDocument.getRootElement();
			result.value = StringUtil.getIntegerValue(outRoot.elementTextTrim("result_code"));
			logger.warn("ServiceChange["+LSHNo+"]==>方法结束{}", new Object[] { LSHNo });
		} catch (Exception e) {
			logger.error("ServiceChange["+LSHNo+"] Exception occured!", e);
		}
    }
	
	
    public void queryUserDetail(Integer iParaType, String Value, ArrayOf_tns1_UserDetailHolder queryUserDetailReturn) throws java.rmi.RemoteException {
    	String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime()) + StringUtil.getStringValue((int)(Math.random()*1000));  
    	logger.warn("queryUserDetail["+LSHNo+"]==>方法开始iParaType[{}],Value[{}]", new Object[] { iParaType, Value});;
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("op_id").addText(LSHNo);
		    intRequest.addElement("iParaType").addText(StringUtil.getStringValue(iParaType));
		    intRequest.addElement("Value").addText(StringUtil.getStringValue(Value));
		 
	        UserDetail userdetail = new QueryUserDetailService("queryUserDetail").work(intRequest.asXML());
		  
	        UserDetail[] restArrays = new UserDetail[1];
			restArrays[0] = userdetail;
	        ArrayOf_tns1_UserDetail array_tns1_UserDetail = new ArrayOf_tns1_UserDetail();
	        array_tns1_UserDetail.setUserDetail(restArrays);
	        queryUserDetailReturn.value = array_tns1_UserDetail;

		    logger.warn("queryUserDetail["+LSHNo+"]==>方法结束,返回对象{}",userdetail.toString());
		} catch (Exception e) {
			logger.error("queryUserDetail["+LSHNo+"] Exception occured!", e);
		}
    }
    
    
    /**
     * 北向查询终端 OUI-SN等信息
     * @param user
     * @param northQueryCPEParaReturn
     * @throws java.rmi.RemoteException
     */
    public void northQueryCPEPara(UserIndex user, NorthQueryParaResultHolder northQueryCPEParaReturn){
    	String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime()) + StringUtil.getStringValue((int)(Math.random()*1000));  
    	logger.warn("northQueryCPEPara["+LSHNo+"]==>方法开始UserIndex user[{}]", new Object[] { user});;
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("op_id").addText(LSHNo);
		    intRequest.addElement("type").addText(StringUtil.getStringValue(user.getType()));
		    intRequest.addElement("index").addText(user.getIndex());
		 
		    NorthQueryParaResult northQueryParaResult = new NorthQueryCPEParaService("northQueryCPEPara").work(intRequest.asXML());
	        northQueryCPEParaReturn.value = northQueryParaResult;

		    logger.warn("northQueryCPEPara["+LSHNo+"]==>方法结束,返回对象{}",northQueryParaResult.toString());
		} catch (Exception e) {
			logger.error("northQueryCPEPara["+LSHNo+"] Exception occured!", e);
		}
    }
    
    
    
    /**
     * 删除单个终端及该终端在系统中的所有相关信息
     * @param cpeID oui-sn
     * @param res 0：删除终端执行成功
	 *			－1：未找到该终端
	 *			－2：其他错误/系统错误
	 *			－3：已有另一个删除终端操作在执行
	 *
     */
    public void delSingleCPE(String cpeID, IntHolder res){
    	String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime()) + StringUtil.getStringValue((int)(Math.random()*1000));  
    	logger.warn("delSingleCPE["+LSHNo+"]==>方法开始cpeID[{}]", cpeID);;
	    try {
		    Document inDocument = DocumentHelper.createDocument();
		    Element intRequest = inDocument.addElement("request");
		    intRequest.addElement("op_id").addText(LSHNo);
		    intRequest.addElement("cpeID").addText(cpeID);
		 
		    res.value = new DelSingleCPEService("delSingleCPE").work(intRequest.asXML());

		    logger.warn("delSingleCPE["+LSHNo+"]==>方法结束,返回{}",res.value);
		} catch (Exception e) {
			logger.error("delSingleCPE["+LSHNo+"] Exception occured!", e);
		}
    }

	/**
	 * 删除单个终端及该终端在系统中的所有相关信息
	 * @param cpeID oui-sn
	 * @param res 0：删除终端执行成功
	 *			－1：未找到该终端
	 *			－2：其他错误/系统错误
	 *			－3：已有另一个删除终端操作在执行
	 *
	 */
	public void delSingelCPE(String cpeID, IntHolder res){
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime()) + StringUtil.getStringValue((int)(Math.random()*1000));
		logger.warn("delSingleCPE["+LSHNo+"]==>方法开始cpeID[{}]", cpeID);;
		try {
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("cpeID").addText(cpeID);

			res.value = new DelSingleCPEService("delSingleCPE").work(intRequest.asXML());

			logger.warn("delSingleCPE["+LSHNo+"]==>方法结束,返回{}",res.value);
		} catch (Exception e) {
			logger.error("delSingleCPE["+LSHNo+"] Exception occured!", e);
		}
	}
    
}
