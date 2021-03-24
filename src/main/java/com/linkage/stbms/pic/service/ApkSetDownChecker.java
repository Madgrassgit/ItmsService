package com.linkage.stbms.pic.service;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.obj.BaseChecker;

/**
 * 江西 APK 系统调用ITV终端网管平台下发业务接口对象
 * 
 * @param xmlData
 * @return
 */
/**
 * @author Administrator
 *
 */
public class ApkSetDownChecker extends BaseChecker {

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(ApkSetDownChecker.class);
	private String username;
	private String pppoeuser;
	private String mac;

	/**
	 * 构造方法
	 * @param inXml
	 * 接口调用入参，xml字符串
	 */
	public ApkSetDownChecker(String inXml) {
		callXml = inXml;
	}

	/**
	 * 参数合法性检查
	 */
	@Override
	public boolean check() {
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
			username = param.elementTextTrim("username");
			pppoeuser = param.elementTextTrim("pppoeuser");
			mac = param.elementTextTrim("MAC");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck4APK()) {
			return false;
		}
		
		if(StringUtil.IsEmpty(username)){
			result = 1002;
			resultDesc = "业务账号不可为空";
			return false;
		}
		
		if(StringUtil.IsEmpty(pppoeuser)){
			result = 1002;
			resultDesc = "接入账号不可为空";
			return false;
		}
		
		if(StringUtil.IsEmpty(mac)){
			result = 1002;
			resultDesc = "mac地址不可为空";
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		return true;
	}
	
	/**
	 * 返回结果字符串
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
		root.addElement("result_flag").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("result").addText(StringUtil.getStringValue(resultDesc));

		Element ele = root.addElement("Sheets").addElement("sheetInfo");
		ele.addElement("username").addText(StringUtil.getStringValue(username));
		ele.addElement("pppoeuser").addText(StringUtil.getStringValue(pppoeuser));
		ele.addElement("MAC").addText(StringUtil.getStringValue(mac));
		
		return document.asXML();
	}
	
	public boolean baseCheck4APK(){
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)){
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		
		if(1 != clientType){
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}
		
		if(false == "CX_01".equals(cmdType)){
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		
		return true;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPppoeuser() {
		return pppoeuser;
	}

	public void setPppoeuser(String pppoeuser) {
		this.pppoeuser = pppoeuser;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

}
