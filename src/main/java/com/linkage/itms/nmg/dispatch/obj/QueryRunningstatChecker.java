package com.linkage.itms.nmg.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 设备是否在线状态接口格式检查
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-7-31
 */
public class QueryRunningstatChecker extends NmgBaseChecker {

	// 日志记录
	private static Logger logger = LoggerFactory.getLogger(QueryRunningstatChecker.class);

	// 0：在线 1：不在线
	private int status = 1;
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public QueryRunningstatChecker(String inXml) {
		callXml = inXml;
	}
	/**
	 * 检查调用接口入参的合法性
	 * 
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
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			devSn = param.elementTextTrim("DeviceInfo");

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		if (1 != userInfoType && 2 != userInfoType && 3 != userInfoType
				&& 4 != userInfoType && 5 != userInfoType) {
			result = 1002;
			resultDesc = "用户信息类型非法";
			return false;
		}
		
		// 参数合法性检查
		if (false == baseCheck()) {
			return false;
		}
		
		// 用户账号合法性检查
		if (StringUtil.IsEmpty(userInfo)) {
			result = 1002;
			resultDesc = "用户帐号不合法";
			return false;
		}
		
		// 设备序列号合法性检查
		if (!StringUtil.IsEmpty(devSn) && devSn.length() < 6) {
			result = 1005;
			resultDesc = "设备序列号不合法";
			return false;
		}

		result = 0;
		resultDesc = "成功";

		return true;
	}
	
	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
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
		// 设备在线状态
		root.addElement("Status").addText(""+StringUtil.getIntegerValue(status));
 
		return document.asXML();
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
}
