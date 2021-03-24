
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
import com.linkage.itms.dispatch.service.TestSpeedSXLTService;

/**
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-8-17
 * @category com.linkage.itms.os.main
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DiagnoseServiceSxlt
{

	private static Logger logger = LoggerFactory.getLogger(DiagnoseServiceSxlt.class);
	
	/**
	 * 测速
	 * @param user
	 * @param testType
	 * @param paras
	 * @param res
	 */
	public void diagnoseTest(UserIndex user, String testType, Para[] paras, IntHolder res){
    	String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime()) + StringUtil.getStringValue((int)(Math.random()*1000));  
    	logger.warn("diagnoseTest["+LSHNo+"]==>方法开始UserIndex user[{}],testType:[{}],paras.length:[{}]", new Object[] { user,testType,paras.length});;
    	try {
    		for(int i=0;i<paras.length;i++){
    			logger.warn(i+":"+paras[i].getName()+", "+ paras[i].getValue());
    		}
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
    			String xml = new TestSpeedSXLTService().work(inDocument.asXML());
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
	
}