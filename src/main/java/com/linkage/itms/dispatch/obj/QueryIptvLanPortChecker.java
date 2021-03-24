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
 * 设备是否在线状态接口格式检查
 * 
 * @author Jason(3412)
 * @date 2010-9-2
 */
public class QueryIptvLanPortChecker extends BaseQueryChecker {

	// 日志记录
	private static Logger logger = LoggerFactory
			.getLogger(QueryIptvLanPortChecker.class);

	private String IptvPort = "";
	
	// 查询用户帐号
	private String username;
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public QueryIptvLanPortChecker(String inXml) {
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
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			searchType = StringUtil.getIntegerValue(param
					.elementTextTrim("SearchType"));
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			username = param.elementTextTrim("UserName");
			devSn = param.elementTextTrim("DevSN");
			

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		if ("nx_dx".equals(Global.G_instArea)) {
			if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType) {
				result = 2;
				resultDesc = "客户端类型非法";
				return false;
			}
			if (1 != userInfoType && 2 != userInfoType && 3 != userInfoType
					&& 4 != userInfoType && 5 != userInfoType) {
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
		}
		
		// 参数合法性检查
		if (false == baseCheck() || false == devSnCheck() || false == searchTypeCheck() || false == userInfoTypeCheck()) {
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
	

	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		//返回结果
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
		
		Element paramEle = root.addElement("Param");
		// 设备在线状态
		paramEle.addElement("IptvPort").addText(IptvPort);
		
		return document.asXML();
	}

	

	public String getIptvPort() {
		return IptvPort;
	}
	public void setIptvPort(String iptvPort) {
		IptvPort = iptvPort;
	}
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
}
