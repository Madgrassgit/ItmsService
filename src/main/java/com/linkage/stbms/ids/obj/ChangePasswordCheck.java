
package com.linkage.stbms.ids.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class ChangePasswordCheck {

	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(ChangePasswordCheck.class);
	private String inParam = null;
	private String cmdId = "";
	private String userId = "";
	private String password1 = "";
	private String pppoeId = "";
	private String password2 = "";
	@SuppressWarnings("unused")
	private int userType = 0;
	
	private String rstCode = "";
	private String rstMsg = "";
	private String cmdType = "";
	private int clientType = 0;
	
	// 有参构造函数
	public ChangePasswordCheck(String inParam) {
		this.inParam = inParam;
	}

	public boolean check() {
		logger.debug("ChangePasswordCheck==>check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");

			userId = param.elementTextTrim("userId");
			password1 = param.elementTextTrim("password1");
			pppoeId = param.elementTextTrim("pppoeId");
			password2 = param.elementTextTrim("password2");
			// 用户类型（分普通上网用户、卡式用户等），此处不做解析
			userType = StringUtil.getIntegerValue(root.elementTextTrim("userType"));
		}
		catch (Exception e) {
			logger.error("inParam format is err,msg({})", e.getMessage());
			rstCode = "0";
			rstMsg = "入参格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == dataCheck()) {
			return false;
		}
		rstCode = "1";
		rstMsg = "成功";
		return true;
	}

	/**
	 * 返回xml
	 * @return
	 */
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("result_flag").addText(rstCode);
		// 结果描述
		root.addElement("result").addText(rstMsg);
		return document.asXML();
	}
	
	/**
	 * 数据验证
	 * @return
	 */
	public boolean dataCheck() {
		if (StringUtil.IsEmpty(userId + password1 + pppoeId + password2)) {
			rstCode = "0";
			rstMsg = "入参格式错误";
			return false;
		}
		if ((StringUtil.IsEmpty(userId) && !StringUtil.IsEmpty(password1))
				|| (!StringUtil.IsEmpty(userId) && StringUtil.IsEmpty(password1))) {
			rstCode = "0";
			rstMsg = "入参格式错误";
			return false;
		}
		if ((StringUtil.IsEmpty(pppoeId) && !StringUtil.IsEmpty(password2))
				|| (!StringUtil.IsEmpty(pppoeId) && StringUtil.IsEmpty(password2))) {
			rstCode = "0";
			rstMsg = "入参格式错误";
			return false;
		}
		return true;
	}
	
	/**
	 * 基本验证
	 * @return
	 */
	public boolean baseCheck() {
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)) {
			rstCode = "1000";
			rstMsg = "接口调用唯一ID非法";
			return false;
		}
		
		if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType
				&& 5 != clientType && 6 != clientType) {
			rstCode = "2";
			rstMsg = "客户端类型非法";
			return false;
		}
		
		if(false == "CX_01".equals(cmdType)) {
			rstCode = "3";
			rstMsg = "接口类型非法";
			return false;
		}
		return true;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword1() {
		return password1;
	}

	public void setPassword1(String password1) {
		this.password1 = password1;
	}

	public String getPppoeId() {
		return pppoeId;
	}

	public void setPppoeId(String pppoeId) {
		this.pppoeId = pppoeId;
	}

	public String getPassword2() {
		return password2;
	}

	public void setPassword2(String password2) {
		this.password2 = password2;
	}

	public String getCmdId() {
		return cmdId;
	}

	public void setCmdId(String cmdId) {
		this.cmdId = cmdId;
	}

	public String getRstCode() {
		return rstCode;
	}

	public void setRstCode(String rstCode) {
		this.rstCode = rstCode;
	}

	public String getRstMsg() {
		return rstMsg;
	}

	public void setRstMsg(String rstMsg) {
		this.rstMsg = rstMsg;
	}
}
