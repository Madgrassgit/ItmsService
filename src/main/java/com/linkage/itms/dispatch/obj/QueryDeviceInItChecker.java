package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class QueryDeviceInItChecker  extends BaseQueryChecker {

	private static Logger logger = LoggerFactory.getLogger(QueryDeviceInItChecker.class);
	
	private String ISinit;
	/**
	 * 构造方法
	 * 
	 * @param _callXml
	 *            客户端查询XML字符串
	 */
	public QueryDeviceInItChecker(String _callXml) {
		callXml = _callXml;
	}
	@Override
	public boolean check() {

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			
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
		
		if(StringUtil.IsEmpty(cmdId)){
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		if(3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType && 5 != clientType && 6 != clientType && 7 != clientType){
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}
		if(false == "CX_01".equals(cmdType)){
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		if(1 != userInfoType){
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
		if(StringUtil.IsEmpty(userInfo)){
			result = 1002;
			resultDesc = "用户信息非法";
			return false;
		}
		return true;
	}
	@Override
	public String getReturnXml() {
		//返回结果
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		root.addElement("ISinit").addText("" + getISinit());
		
		return document.asXML();
	}
	public String getISinit() {
		return ISinit;
	}
	public void setISinit(String iSinit) {
		ISinit = iSinit;
	}

}
