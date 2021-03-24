package com.linkage.itms.dispatch.obj;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class CloudQueryStaticRtCfgChecker extends CloudBaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(CloudQueryStaticRtCfgChecker.class);

	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public CloudQueryStaticRtCfgChecker(String inXml) {
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

	public String getReturnXml(ArrayList<Map<String, String>> retList) {
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
		par.addElement("Loid").addText(loid);
		par.addElement("LoidPrev").addText(loidPrev);
		// Vxlan实例数
		
		if (retList == null || retList.isEmpty()) {
			Element inst = par.addElement("Instance");
			inst.addElement("RtId").addText("");
			inst.addElement("DesIp").addText("");
			inst.addElement("NextHop").addText("");
			inst.addElement("Priority").addText("");
			return document.asXML();
		}
		for (Map<String, String> map : retList) {
			Element inst = par.addElement("Instance");
			inst.addElement("RtId").addText(StringUtil.getStringValue(map, "rtId", ""));
			inst.addElement("DesIp").addText(StringUtil.getStringValue(map, "desIp", ""));
			inst.addElement("NextHop").addText(StringUtil.getStringValue(map, "nextHop", ""));
			inst.addElement("Priority").addText(StringUtil.getStringValue(map, "priority", ""));
		}
		return document.asXML();
		
	}

	public static void main(String[] args) {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText("");
		// 结果代码
		root.addElement("RstCode").addText("");
		// 结果描述
		root.addElement("RstMsg").addText("");
		
		Element par = root.addElement("Param");
		// Vxlan实例数
		par.addElement("InstanceNum").addText("");
		Element inst = par.addElement("Instance");
		inst.addElement("X_CT-COM_VLAN").addText("");
		inst = par.addElement("Instance");
		inst.addElement("X_CT-COM_VLAN").addText("");
		System.out.println(document.asXML());
		
	}

	@Override
	public String getReturnXml() {
		return null;
	}
}
