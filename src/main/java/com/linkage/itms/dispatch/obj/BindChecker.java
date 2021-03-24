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
 * bind方法接口的XML元素对象
 * 
 * @author Jason(3412)
 * @date 2010-6-17
 */
public class BindChecker extends BaseChecker {

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(BindChecker.class);

	// 绑定类型
	private int bindType;

	// 修障终端原因描述
	private String devDesc;

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public BindChecker(String inXml) {
		callXml = inXml;
	}

	/**
	 * 检查接口调用字符串的合法性
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
			bindType = StringUtil.getIntegerValue(param
					.elementTextTrim("BindType"));
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
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
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck() || false == devSnCheck()
				|| false == cityIdCheck() || false == bindTypeCheck()) {
			return false;
		}

		result = 0;
		resultDesc = "绑定成功";
		
		return true;
	}

	/**
	 * 绑定类型合法性检查
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-21
	 * @return void
	 */
	private boolean bindTypeCheck() {
		logger.debug("bindTypeCheck()");
		if (1 != bindType && 2 != bindType) {
			result = 1001;
			resultDesc = "绑定类型非法";
			return false;
		}
		return true;
	}

	/**
	 * 返回绑定调用结果字符串
	 */
	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		if ("nx_dx".equals(Global.G_instArea)) {
			document.setXMLEncoding(Global.codeTypeValue);
		} else {
			document.setXMLEncoding("GBK");
		}
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);

		return document.asXML();
	}

	public int getBindType() {
		return bindType;
	}

	public void setBindType(int bindType) {
		this.bindType = bindType;
	}

	public String getDevDesc() {
		return devDesc;
	}

	public void setDevDesc(String devDesc) {
		this.devDesc = devDesc;
	}

}
