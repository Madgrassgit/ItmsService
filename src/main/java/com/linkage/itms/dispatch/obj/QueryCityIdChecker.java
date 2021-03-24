package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class QueryCityIdChecker extends BaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(QueryCityIdChecker.class);

	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public QueryCityIdChecker(String inXml) {
		callXml = inXml;
	}

	@Override
	public boolean check() {
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		cityId = "";
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			searchType = StringUtil.getIntegerValue(param.elementTextTrim("SearchType"));
			devSn = param.elementTextTrim("DevSN");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		//参数合法性检查
		if (false == baseCheck() || false == devSnCheck() 
				||  false == searchTypeCheck()) {
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
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
		// 属地代码
		root.addElement("CityId").addText(cityId);
		return document.asXML();
	}

	@Override
	protected boolean devSnCheck(){
		if(false == pattern.matcher(devSn).matches() || devSn.length() < 6){
			result = 1005;
			resultDesc = "设备序列号不合法";
			return false;
		}
		return true;
	}
}
