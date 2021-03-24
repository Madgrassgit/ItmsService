package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;


public class PhoneConnectivityTestChecker extends BaseChecker{
	
	private static Logger logger = LoggerFactory.getLogger(PhoneConnectivityTestChecker.class);
	private String voipPortNUM;
	private String phoneConnectivity;
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public PhoneConnectivityTestChecker(String inXml) {
		this.callXml = inXml;
	}
	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfoType = Integer.valueOf(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck() || false == PhoneConnectivityCheck()) {
			return false;
		}
		
		
		result = 0;
		resultDesc = "成功";
		
		return true;
	}
	public boolean PhoneConnectivityCheck(){
		if(3 != userInfoType && 2 != userInfoType && 1 != userInfoType && 4 != userInfoType && 5 != userInfoType ){
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}
		logger.debug("PhoneConnectivityCheck()");
		if(StringUtil.IsEmpty(userInfo)){
			result = 1;
			resultDesc = "用户信息不可为空";
			return false;
		}
		return true;
	}
	
	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(""+result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		root.addElement("VoipPortNUM").addText(voipPortNUM==null?"":voipPortNUM);
		root.addElement("PhoneConnectivity").addText(phoneConnectivity==null?"":phoneConnectivity);
		
		return document.asXML();
	}
	public String getVoipPortNUM() {
		return voipPortNUM;
	}
	public void setVoipPortNUM(String voipPortNUM) {
		this.voipPortNUM = voipPortNUM;
	}
	public String getPhoneConnectivity() {
		return phoneConnectivity;
	}
	public void setPhoneConnectivity(String phoneConnectivity) {
		this.phoneConnectivity = phoneConnectivity;
	}
}
