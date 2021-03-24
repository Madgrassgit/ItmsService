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

/**
 * 接口数据检查查询基类(抽象类)
 * 
 * @author Jason(3412)
 * @date 2010-9-2
 */
public abstract class BaseQueryChecker extends BaseChecker {

	// 日志记录
	private static Logger logger = LoggerFactory
			.getLogger(BaseQueryChecker.class);

	// 查询用户帐号
	private String username;

	//回复使用的XML的Document
	Document document; 
	
	//XML结构root结点
	Element root;
		
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.linkage.itms.dispatch.obj.BaseChecker#check()
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
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			searchType = StringUtil.getIntegerValue(param
					.elementTextTrim("SearchType"));
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			username = param.elementTextTrim("UserName");
			devSn = param.elementTextTrim("DevSN");
			if(!"nmg_dx".equals(Global.G_instArea)){
				cityId = param.elementTextTrim("CityId");
			}
			

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		
		// 参数合法性检查
		if (false == baseCheck() || false == devSnCheck()
				|| false == cityIdCheck() || false == searchTypeCheck() || false == userInfoTypeCheck()) {
			return false;
		}
		
		// 用户账号合法性检查
		if (1 == searchType && StringUtil.IsEmpty(username)) {
			result = 1002;
			resultDesc = "用户帐号不合法";
			return false;
		}

		result = 0;
		resultDesc = "成功";

		return true;
	}

	
	/**
	 * 接口回复基本头格式
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-9-2
	 * @return String
	 */
	public void getBaseReturnXml(){
		logger.debug("getBaseReturnXml()");
		document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.linkage.itms.dispatch.obj.BaseChecker#getReturnXml()
	 */
	@Override
	public abstract String getReturnXml();

	
	/** getter, setter methods */
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
