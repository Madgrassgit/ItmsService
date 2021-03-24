package com.linkage.itms.dispatch.obj;


import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class CloudQueryServIpAddrChecker extends CloudBaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(CloudQueryServIpAddrChecker.class);


	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public CloudQueryServIpAddrChecker(String inXml) {
		callXml = inXml;
	}

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
			dealDate = root.elementTextTrim("DealDate");

			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			proInstId = StringUtil.IsEmpty(param.elementTextTrim("ProInstId")) ? "" : param.elementTextTrim("ProInstId");
			
			// 获取是否回调标志，默认不回调
			callBack = param.elementTextTrim("CallBack");
			callBack = StringUtil.IsEmpty(callBack) ? "0" : callBack;
			
			callBackUrl = StringUtil.IsEmpty(param.elementTextTrim("CallBackUrl")) ? "" : param.elementTextTrim("CallBackUrl");
			
			isAsynchronous = StringUtil.IsEmpty(param.elementTextTrim("IsAsynchronous")) ? "" : param.elementTextTrim("IsAsynchronous");
		} catch (Exception e) {
			e.printStackTrace();
			result = 3;
			resultDesc = "入参格式错误";
			return false;
		}
		//参数合法性检查
		if (!baseCheck() || !userInfoTypeCheck() || !userInfoCheck()) {
			return false;
		}
	
		result = 0;
		resultDesc = "成功";
		return true;
	}

	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(String.valueOf(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		Element par = root.addElement("Param");
		// 当前最新绑定的Loid
		par.addElement("Loid").addText(loid);
		// 通过账号查到多个Loid，除去最新绑定Loid之外的Loid集合
		par.addElement("LoidPrev").addText(loidPrev);
		// 网关业务ip地址
		par.addElement("ServIpAddr").addText(servIpAddr);
		par.addElement("ProInstId").addText(proInstId);
		
		if ("1".equals(isAsynchronous)) {
			par.addElement("CallBackUrl").addText(callBackUrl);
		}
		return document.asXML();
	}
}
