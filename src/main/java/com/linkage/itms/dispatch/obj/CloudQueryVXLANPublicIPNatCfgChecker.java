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

public class CloudQueryVXLANPublicIPNatCfgChecker extends CloudBaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(CloudQueryVXLANPublicIPNatCfgChecker.class);

	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public CloudQueryVXLANPublicIPNatCfgChecker(String inXml) {
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
		
		// Vxlan实例数
		if (retList == null || retList.isEmpty()) {
			Element inst = par.addElement("NatList");
			// 当前最新绑定的Loid
			inst.addElement("PubIpv4").addText("");
			inst.addElement("PrivIpv4").addText("");
			return document.asXML();
		}
		String natType = "";
		boolean flat = true;
		for (Map<String, String> map : retList) {
			String natTypeNode = StringUtil.getStringValue(map, "natType");
			Element inst = null;
			
			if (!natTypeNode.equals(natType) && !StringUtil.IsEmpty(natType) && flat) {
				natType = "3";
				flat = false;
			}
			natType = natTypeNode;
			if ("1".equals(natTypeNode)) {
				inst = par.addElement("NatList");
			}
			else {
				inst = par.addElement("PatList");
				inst.addElement("Protocol").addText(StringUtil.getStringValue(map, "protocol", ""));
				inst.addElement("PrivPort").addText(StringUtil.getStringValue(map, "privPort", ""));
				inst.addElement("PubPort").addText(StringUtil.getStringValue(map, "pubPort", ""));
			}
			inst.addElement("PubIpv4").addText(StringUtil.getStringValue(map, "pubIpv4"));
			inst.addElement("PrivIpv4").addText(StringUtil.getStringValue(map, "privIpv4"));
		}
		par.addElement("NatType").addText(natType);
		return document.asXML();
		
	}

	@Override
	public String getReturnXml() {
		return null;
	}
}
