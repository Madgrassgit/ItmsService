package com.linkage.itms.dispatch.obj;


import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.util.VxlanOperateDeviceUtil;

public class CloudDeleteStaticRtCfgChecker extends CloudBaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(CloudDeleteStaticRtCfgChecker.class);

	private String rtId = "";
	private String desIp = "";
	private String nextHop = "";
	private String priority = "";

	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public CloudDeleteStaticRtCfgChecker(String inXml) {
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
			rtId = param.elementTextTrim("RtId");
			desIp = param.elementTextTrim("DesIp");
			nextHop = param.elementTextTrim("NextHop");
			priority = param.elementTextTrim("Priority");
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
		if (StringUtil.IsEmpty(rtId) || StringUtil.IsEmpty(desIp) || StringUtil.IsEmpty(nextHop)) {
			result = 3;
			resultDesc = "入参格式错误";
			return false;
		}
		if (!VxlanOperateDeviceUtil.isIPMask(desIp)) {
			result = 3;
			resultDesc = "目标IP地址入参格式不正确";
			return false;
		}
		if (!StringUtil.IsEmpty(priority) && (Integer.parseInt(priority) < 1 || Integer.parseInt(priority) > 255)) {
			result = 3;
			resultDesc = "优先级取值不正确";
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

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
		return document.asXML();
	}

	public String getRtId() {
		return rtId;
	}

	public void setRtId(String rtId) {
		this.rtId = rtId;
	}

	public String getDesIp() {
		return desIp;
	}

	public void setDesIp(String desIp) {
		this.desIp = desIp;
	}

	public String getNextHop() {
		return nextHop;
	}

	public void setNextHop(String nextHop) {
		this.nextHop = nextHop;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}
}
