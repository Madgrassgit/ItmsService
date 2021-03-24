package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;


/**
 * 新疆语音协议查询
 * 
 * @author Administrator
 *
 */
public class XJVoipProtocolChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(XJVoipProtocolChecker.class);
	
	private String protocol = null;
	
	
	public XJVoipProtocolChecker(String inXml){
		callXml = inXml;
	}

	
	@Override
	public boolean check(){
		
		logger.debug("XJVoipProtocolChecker==>check()");
		
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
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		//参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		
		return true;
	}
	
	
	
	/**
	 * 组装XML字符串，并返回
	 */
	@Override
	public String getReturnXml() {
		
		logger.debug("getReturnXml()");
		
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		// 协议类型
		if (null == protocol || "null".equals(protocol)) {
			root.addElement("Protocol").addText("");
		} else {
			root.addElement("Protocol").addText(protocol);
		}
		
		return document.asXML();
	}
	
	
	
	public String getProtocol() {
		return protocol;
	}

	
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
}
