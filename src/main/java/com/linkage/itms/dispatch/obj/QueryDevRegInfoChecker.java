package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;


public class QueryDevRegInfoChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory.getLogger(QueryDevRegInfoChecker.class);
	
	// 设备型号
	private String devModel;
	// 注册时间
	private String unbindDate;

	/**
	 * 构造函数 入参
	 * @param inXml
	 */
	public QueryDevRegInfoChecker(String inXml){
		callXml = inXml;
	}
	
	
	/**
	 * 参数合法性检查
	 */
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
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		
		return true;
	}
	
	boolean userInfoTypeCheck(){
		if(2 != userInfoType){
			result = 1002;
			resultDesc = "用户信息类型非法";
			return false;
		}
		return true;
	}

	/**
	 * 回参
	 */
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(StringUtil.getStringValue(resultDesc));
		// 设备型号
		root.addElement("DevModel").addText(StringUtil.getStringValue(devModel));
		// loid
		root.addElement("Loid").addText(StringUtil.getStringValue(loid));
		// 设备序列号
		root.addElement("DevSN").addText(StringUtil.getStringValue(devSn));
		// 注册时间
		root.addElement("UnbindDate").addText(StringUtil.getStringValue(unbindDate));

		return document.asXML();
	}

	public String getDevModel() {
		return devModel;
	}

	public void setDevModel(String devModel) {
		this.devModel = devModel;
	}

	public String getUnbindDate() {
		return unbindDate;
	}

	public void setUnbindDate(String unbindDate) {
		this.unbindDate = unbindDate;
	}
}
