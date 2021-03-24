package com.starit.inas.north.adapter;

import java.io.StringReader;

import javax.xml.rpc.holders.IntHolder;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.gsdx.beanObj.Para;
import com.linkage.itms.dispatch.gsdx.beanObj.UserIndex;
import com.linkage.itms.dispatch.gsdx.holders.getDiagnoseResultHolder;
import com.linkage.itms.dispatch.service.GetSpeedResultService4JLLT;
import com.linkage.itms.dispatch.service.TestSpeedService4JLLT;

/**
 * 吉林联通业务发放场景接口入口类
 * @author fanjm (Ailk No.)
 * @version 1.0
 * @since 2019-11-12
 * @category com.linkage.itms.dispatch.main
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DiagnoseService
{
	private static Logger logger = LoggerFactory.getLogger(DiagnoseService.class);
	
	
	
    /**
     * 通用诊断接口
     * @param user
     * @return int 
     */
    public void diagnoseTest(UserIndex user, String testType, Para[] paras, IntHolder res){
    	String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime()) + StringUtil.getStringValue((int)(Math.random()*1000));  
    	logger.warn("diagnoseTest["+LSHNo+"]==>方法开始UserIndex user[{}],procName:[{}]", new Object[] { user,testType});;
    	try {
    		Document inDocument = DocumentHelper.createDocument();
    		Element root = inDocument.addElement("root");
    		Element intRequest = root.addElement("Param");
    		intRequest.addElement("op_id").addText(LSHNo);
    		intRequest.addElement("type").addText(StringUtil.getStringValue(user.getType()));
    		intRequest.addElement("index").addText(user.getIndex());
    		for(Para para : paras){
    			intRequest.addElement(para.getName()).addText(para.getValue());
    		}
    		if("SpeedTest".equals(testType)){
    			String xml = new TestSpeedService4JLLT().work(inDocument.asXML());
    			SAXReader reader = new SAXReader();
    			Document document = null;
    			try {
    				document = reader.read(new StringReader(xml));
    				Element rootRes = document.getRootElement();
    				res.value = StringUtil.getIntegerValue(rootRes.elementTextTrim("RstCode"));
    			} catch (Exception e) {
    				e.printStackTrace();
    				res.value = -1000;
    			}
    		}
    		logger.warn("diagnoseTest["+LSHNo+"]==>方法结束,返回{}",res.value);
    	} catch (Exception e) {
    		e.printStackTrace();
    		logger.error("diagnoseTest["+LSHNo+"] Exception occured!", e);
    	}
    }
    
    
    
    /**
     * 通用诊断接口
     * @param user
     * @return int 
     */
    public void getDiagnoseResult (UserIndex user, String testType, getDiagnoseResultHolder paras, IntHolder iOpRst){
    	String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime()) + StringUtil.getStringValue((int)(Math.random()*1000));  
    	logger.warn("getDiagnoseResult["+LSHNo+"]==>方法开始UserIndex user[{}],procName:[{}]", new Object[] { user,testType});;
    	paras.value = new Para[14]; 
    	try {
    		Document inDocument = DocumentHelper.createDocument();
    		Element root = inDocument.addElement("root");
    		Element intRequest = root.addElement("Param");
    		intRequest.addElement("op_id").addText(LSHNo);
    		intRequest.addElement("type").addText(StringUtil.getStringValue(user.getType()));
    		intRequest.addElement("index").addText(user.getIndex());
    		if("SpeedTest".equals(testType)){
    			String xml = new GetSpeedResultService4JLLT().work(inDocument.asXML());
    			SAXReader reader = new SAXReader();
    			Document document = null;
    			try {
    				document = reader.read(new StringReader(xml));
    				Element rootRes = document.getRootElement();
    				
    				paras.value[0] = new Para("SpeedTest_status", rootRes.elementTextTrim("status"));
    				paras.value[1] = new Para("SpeedTest_pppoeIP", rootRes.elementTextTrim("pppoeIP"));
    				paras.value[2] = new Para("SpeedTest_pppoeName", rootRes.elementTextTrim("pppoeName"));
    				paras.value[3] = new Para("SpeedTest_cspeed", rootRes.elementTextTrim("Cspeed"));
    				paras.value[4] = new Para("SpeedTest_aspeed", rootRes.elementTextTrim("Aspeed"));
    				paras.value[5] = new Para("SpeedTest_bspeed", rootRes.elementTextTrim("Bspeed"));
    				paras.value[6] = new Para("SpeedTest_maxspeed", rootRes.elementTextTrim("maxspeed"));
    				paras.value[7] = new Para("SpeedTest_starttime", rootRes.elementTextTrim("starttime"));
    				paras.value[8] = new Para("SpeedTest_endtime", rootRes.elementTextTrim("endtime"));
    				paras.value[9] = new Para("SpeedTest_totalsize", rootRes.elementTextTrim("totalsize"));
    				paras.value[10] = new Para("SpeedTest_backgroundsize", rootRes.elementTextTrim("backgroundsize"));
    				paras.value[11] = new Para("SpeedTest_failcode", rootRes.elementTextTrim("failcode"));
    				paras.value[12] = new Para("SpeedTest_eupppoename", rootRes.elementTextTrim("userName"));
    				paras.value[13] = new Para("SpeedTest_eupassword", rootRes.elementTextTrim("password"));
    				iOpRst.value = StringUtil.getIntegerValue(rootRes.elementTextTrim("RstCode"));
    			} catch (Exception e) {
    				e.printStackTrace();
    				iOpRst.value = -1000;
    			}
    		}
    		paras.value = paras.value;
    		logger.warn("getDiagnoseResult["+LSHNo+"]==>方法结束,返回{}", iOpRst.value);
    	} catch (Exception e) {
    		logger.error("getDiagnoseResult["+LSHNo+"] Exception occured!", e);
    	}
    	
    }
}
