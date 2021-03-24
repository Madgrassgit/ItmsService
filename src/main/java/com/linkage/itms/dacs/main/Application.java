package com.linkage.itms.dacs.main;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dacs.inter.IService;


/**
 * @author Jason(3412)
 * @date 2009-5-25
 */
public class Application implements IService{

	private static Logger log = LoggerFactory.getLogger(Application.class);
	
	/*** 解析XML字符串 */
//	private OptXML xml = new OptXML();
	
	
	/**
	 * 包含调用的整个业务逻辑流程
	 */
	@Override
	public String configQosCall(String strParamXML) {
		System.out.println("strParamXML:" + strParamXML);
		log.info("strParamXML:" + strParamXML);
		if(null == strParamXML){
			return returnXmlStr.replace("resultcoode", "4").replace("errmessage", "调用无参数");
		}
		String strParam = null;
		String username = null;
		String qosCode = null;
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read(new StringReader(strParamXML));
			Element root = document.getRootElement();
			strParam = root.element("paramDoc").element("param").getTextTrim();
			log.info("param:" + strParam);
			String[] arrParam = strParam.split("\\$\\$");
			if(null == arrParam || arrParam.length < 2){
				return returnXmlStr.replace("resultcoode", "4").replace("errmessage", "参数数目不对");
			}
			username = arrParam[0];
			qosCode = arrParam[1];
		} catch (Exception e){
			username = null;
			qosCode = null;
			log.error("执行错误：");
			log.error(e.getMessage());
		}
		log.info("*****************");
		log.info("user:" + username);
		log.info("user:" + qosCode);
		log.info("");
		if(null != username && null != qosCode){
			if(Global.qosMap.containsKey(qosCode))
				return returnXmlStr.replace("resultcoode", "0").replace("errmessage", "success");
			else
				return returnXmlStr.replace("resultcoode", "4").replace("errmessage", "没有相应的Qos策略");
		}
		
		return returnXmlStr.replace("resultcoode", "1").replace("errmessage", "用户账号不存在");
	}
}
