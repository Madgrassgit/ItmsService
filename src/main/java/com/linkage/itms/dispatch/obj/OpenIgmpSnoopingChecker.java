
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
 * @author zhaixx (Ailk No.)
 * @version 1.0
 * @since 2018年10月25日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class OpenIgmpSnoopingChecker extends BaseChecker {

	private static final Logger logger = LoggerFactory.getLogger(OpenIgmpSnoopingChecker.class);

	/**
	 * 构造函数 入参
	 * 
	 * @param inXml
	 */
	public OpenIgmpSnoopingChecker(String inXml) {
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
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		}
		catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseInfoCheck() || false == userInfoTypeChecker() || false == userInfoCheck()) {
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	private boolean baseInfoCheck() {
		if (StringUtil.IsEmpty(cmdId)) {
			result = 1;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		if (3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType) {
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}
		if (false == "CX_01".equals(cmdType)) {
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		return true;
	}

	private boolean userInfoTypeChecker() {
		if (1 != userInfoType && 2 != userInfoType) {
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
		return true;
	}

	/**
	 * 设置回参
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
		return document.asXML();
	}
}
