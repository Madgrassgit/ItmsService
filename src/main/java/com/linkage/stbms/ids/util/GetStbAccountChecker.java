package com.linkage.stbms.ids.util;

import java.io.StringReader;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 新疆电信  机顶盒业务账号查询接口
 * @author chenxj6
 * @date 2016-9-29
 * @param inParam
 * @return
 */
public class GetStbAccountChecker extends BaseChecker {
	private static final Logger logger = LoggerFactory.getLogger(GetStbAccountChecker.class);

	private String inParam = null;
	
	// 机顶盒设备序列号
	private String devSn;
	// 业务账号
	private String servAccount;

	public GetStbAccountChecker(String inParam) {
		this.inParam = inParam;
	}

	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check() {

		logger.debug("GetStbAccountChecker==>check()");

		SAXReader reader = new SAXReader();
		Document document = null;

		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();

			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType")); // 1：BSS, 2：IPOSS, 3：综调, 4：RADIUS

			Element param = root.element("Param");
			devSn = param.elementTextTrim("devSN");

		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			rstCode = "1";
			rstMsg = "入参格式错误";
			return false;
		}
		
		// 参数合法性检查
		// 机顶盒序列号校验
		Pattern snPattern = Pattern.compile("\\w{1,}+"); 
		
		if(false == snPattern.matcher(devSn).matches() || devSn.length() < 6){
			rstCode = "1005";
			rstMsg = "设备序列号不合法";
			return false;
		}
		
		if (false == baseCheck()) {
			return false;
		}

		rstCode = "0";
		rstMsg = "成功";

		return true;
	}

	/**
	 * 返回调用结果字符串
	 * 
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
		root.addElement("RstCode").addText(rstCode);
		// 结果描述
		root.addElement("RstMsg").addText(rstMsg);

		return document.asXML();
	}
	
	
	public String getServAccountReturnXml() {
		logger.debug("getNetAccountReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + rstCode);
		// 返回用户名与密码
		Element param = root.addElement("Param");
		param.addElement("servAccount").addText(servAccount);

		return document.asXML();
	}
	

	public String getInParam() {
		return inParam;
	}

	public void setInParam(String inParam) {
		this.inParam = inParam;
	}

	public String getDevSn() {
		return devSn;
	}

	public void setDevSn(String devSn) {
		this.devSn = devSn;
	}

	public String getServAccount() {
		return servAccount;
	}

	public void setServAccount(String servAccount) {
		this.servAccount = servAccount;
	}

}
