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
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-6-14
 * @category com.linkage.itms.nmg.dispatch.util
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 
 */
public class VoiceMapConfigurationChecker extends BaseChecker {

	private static final Logger logger = LoggerFactory
			.getLogger(VoiceMapConfigurationChecker.class);

	private String inParam = null;
	private String digitCornet = null;
	// 失败原因
	private String failureReason = null;
	// 成功状态
	private String succStatus = "-1";

	public VoiceMapConfigurationChecker(String inParam) {
		this.inParam = inParam;
	}

	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check() {

		logger.debug("BindInfoChecker==>check()" + inParam);

		SAXReader reader = new SAXReader();
		Document document = null;

		try {

			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();

			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));

			Element param = root.element("Param");

			/**
			 * 查询类型 1.用户宽带账号 2.LOID 3.IPTV宽带账号 4.VOIP业务电话号码 5.VOIP认证账号
			 */
			userInfoType = Integer.parseInt(param
					.elementTextTrim("UserInfoType"));
			/**
			 * 查询类型所对应的用户信息
			 */
			userInfo = param.elementTextTrim("UserName");
			digitCornet = param.elementTextTrim("DigitCornet");

		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}

		if (StringUtil.IsEmpty(digitCornet) || digitCornet == null) {
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		} 

		result = 0;
		resultDesc = "成功";

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
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		// 失败原因
				root.addElement("FailureReason").addText(failureReason);
				// 成功状态
				root.addElement("SuccStatus").addText(succStatus);

		return document.asXML();
	}

//	public String commonReturnParam(ArrayList<HashMap<String, String>> devList,
//			Map<String, String> infoMap) {
//		logger.debug("getReturnXml()");
//		Document document = DocumentHelper.createDocument();
//		document.setXMLEncoding("GBK");
//		Element root = document.addElement("root");
//		// 接口调用唯一ID
//		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
//		if (infoMap != null && !infoMap.isEmpty()) {
//			for (Object key : infoMap.keySet()) {
//				root.addElement(key.toString()).addText(infoMap.get(key));
//			}
//		}
//		Element param = root.addElement("Param");
//		for (HashMap<String, String> devMap : devList) {
//			for (Object keyStr : devMap.keySet()) {
//				param.addElement(keyStr.toString()).addText(devMap.get(keyStr));
//			}
//		}
//		return document.asXML();
//	}

	public String getInParam() {
		return inParam;
	}

	public void setInParam(String inParam) {
		this.inParam = inParam;
	}



	public String getDigitCornet() {
		return digitCornet;
	}

	public void setDigitCornet(String digitCornet) {
		this.digitCornet = digitCornet;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public String getSuccStatus() {
		return succStatus;
	}

	public void setSuccStatus(String succStatus) {
		this.succStatus = succStatus;
	}

}
