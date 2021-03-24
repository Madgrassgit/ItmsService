package com.linkage.itms.dispatch.obj;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class CloudVXLANPublicIPNatCfgChecker extends CloudBaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(CloudVXLANPublicIPNatCfgChecker.class);

	private String natType;
	
	private List<Map<String, String>> natList = null;

	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public CloudVXLANPublicIPNatCfgChecker(String inXml) {
		callXml = inXml;
	}

	@SuppressWarnings("unchecked")
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
			
			natType = param.elementTextTrim("NatType");
			
			List<Element> natNodes =  param.elements("NatList");
			
			List<Element> patNodes = param.elements("PatList");
			
			natList = new ArrayList<Map<String, String>>();
			
			String pubIpv4 = "";
			String privIpv4 = "";
			String protocol = "";
			if (natNodes != null && natNodes.size() > 0) {
				for (Element e : natNodes) {
					Map<String, String> natMap = new HashMap<String, String>();
					pubIpv4 = e.elementTextTrim("PubIpv4");
					privIpv4 = e.elementTextTrim("PrivIpv4");
					if (StringUtil.IsEmpty(pubIpv4) || StringUtil.IsEmpty(privIpv4)) {
						logger.warn("过滤NatList下 pubIpv4[{}] privIpv4[{}]非法数据", pubIpv4, privIpv4);
						continue;
					}
					natMap.put("pubIpv4", pubIpv4);
					natMap.put("privIpv4", privIpv4);
					natList.add(natMap);
				}
			}
			if (patNodes != null && patNodes.size() > 0) {
				for (Element e : patNodes) {
					Map<String, String> patMap = new HashMap<String, String>();
					pubIpv4 = e.elementTextTrim("PubIpv4");
					privIpv4 = e.elementTextTrim("PrivIpv4");
					protocol = e.elementTextTrim("Protocol");
					if (StringUtil.IsEmpty(pubIpv4) || StringUtil.IsEmpty(privIpv4)) {
						logger.warn("过滤NatList下 pubIpv4[{}] privIpv4[{}]非法数据", pubIpv4, privIpv4);
						continue;
					}
					patMap.put("pubIpv4", pubIpv4);
					patMap.put("pubPort", e.elementTextTrim("PubPort"));
					patMap.put("privIpv4", privIpv4);
					patMap.put("privPort", e.elementTextTrim("PrivPort"));
					patMap.put("protocol", protocol);
					natList.add(patMap);
				}
			}
			logger.warn("[{}]解析出的natList[{}]", userInfo, natList);
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
		if (StringUtil.IsEmpty(natType) || !"1,2".contains(natType)) {
			result = 3;
			resultDesc = "入参格式错误";
			return false;
		}
		if (natList == null || natList.size() < 1) {
			result = 3;
			resultDesc = "没有传入地址映射";
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	public String getNatType() {
		return natType;
	}

	public void setNatType(String natType) {
		this.natType = natType;
	}

	public List<Map<String, String>> getNatList() {
		return natList;
	}

	public void setNatList(List<Map<String, String>> natList) {
		this.natList = natList;
	}

	@Override
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
		// Vxlan实例数
		return document.asXML();
	}
}
