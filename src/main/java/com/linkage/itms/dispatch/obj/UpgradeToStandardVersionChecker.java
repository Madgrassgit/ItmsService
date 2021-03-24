package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;


public class UpgradeToStandardVersionChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(UpgradeToStandardVersionChecker.class);
	
	public UpgradeToStandardVersionChecker(String inXml){
		this.callXml = inXml;
	}
	
	
	/**
	 * 
	 * 验证入参合法性
	 * 
	 */
	public boolean check(){
		
		logger.debug("UpgradeToStandardVersionChecker==>check()");
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {
			document = reader.read(new StringReader(callXml));
			
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));
			
			Element param = root.element("Param");
			
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			
			userInfo = param.elementTextTrim("UserInfo");
			
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		//参数合法性检查
		// chenxj6 xj
		if ("xj_dx".equals(Global.G_instArea)) {
			if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType) {
				result = 2;
				resultDesc = "客户端类型非法";
				return false;
			}
			if(1 != userInfoType && 2 != userInfoType && 3 != userInfoType && 4 != userInfoType && 5 != userInfoType){
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
		}
		
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		
		return true;
	}
	
	
	/**
	 * 回参
	 */
	public String getReturnXml(){
		
		logger.debug("UpgradeToStandardVersionChecker==>getReturnXml()");
		
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		
		return document.asXML();
	}
	
}
