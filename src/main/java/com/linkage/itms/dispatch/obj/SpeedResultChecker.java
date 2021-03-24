package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class SpeedResultChecker extends BaseChecker {
	private static final Logger logger = LoggerFactory
			.getLogger(SpeedResultChecker.class);
	private String inParam = "";
	// 逻辑Id
	private String userLoid = "";
	// 失败原因
	private String failureReason = "";
	// 平均下载速率
	private String averageDownloadSpeed = "";
	// 最高下载速率
	private String maxDownloadSpeed = "";

	public SpeedResultChecker(String inParam) {
		this.inParam = inParam;
	}

	@Override
	public boolean check() {
		logger.debug("SpeedResultChecker==>check()");

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
			 * 1：用户宽带帐号
			 */
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserName");
			userLoid = param.elementTextTrim("UserLOID");

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
		result = 0;
		resultDesc = "成功";

		return true;
	}

	/**
	 * 用户信息合法性检查
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	@Override
	protected boolean userInfoCheck() {
		if (StringUtil.IsEmpty(userInfo) && StringUtil.IsEmpty(userLoid)) {
			result = 1002;
			resultDesc = "用户信息不合法";
			return false;
		}
		return true;
	}

	@Override
	public String getReturnXml() {
		logger.debug("SpeedResultChecker==>getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(""+cmdId);
		// 结果代码
		root.addElement("RstCode").addText(""+ result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		root.addElement("FailureReason").addText(""+failureReason);
		root.addElement("DevSN ").addText(devSn);
		root.addElement("AverageDownloadSpeed").addText(averageDownloadSpeed);
		root.addElement("MaxDownloadSpeed").addText(maxDownloadSpeed);
		return document.asXML();
	}

	public String getUserLoid() {
		return userLoid;
	}

	public void setUserLoid(String userLoid) {
		this.userLoid = userLoid;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public String getAverageDownloadSpeed() {
		return averageDownloadSpeed;
	}

	public void setAverageDownloadSpeed(String averageDownloadSpeed) {
		this.averageDownloadSpeed = averageDownloadSpeed;
	}

	public String getMaxDownloadSpeed() {
		return maxDownloadSpeed;
	}

	public void setMaxDownloadSpeed(String maxDownloadSpeed) {
		this.maxDownloadSpeed = maxDownloadSpeed;
	}

}
