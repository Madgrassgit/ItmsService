package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class GetServInfofromDevChecker extends BaseChecker {
	private static final Logger logger = LoggerFactory.getLogger(GetServInfofromDevChecker.class);
	// 查询类型 1：loid 2：deviceSN
	private int queryType;
	// 查询内容
	private String queryInfo;
	// 业务类型 10：宽带 11：iptv 14：voip
	private int servType;

	/**
	 * 构造函数 入参
	 * 
	 * @param inXml
	 */
	public GetServInfofromDevChecker(String inXml) {
		callXml = inXml;
	}

	/**
	 * 参数合法性检查
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
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			queryType = StringUtil.getIntegerValue(param.elementTextTrim("queryType"));
			queryInfo = param.elementTextTrim("queryInfo");
			servType = StringUtil.getIntegerValue(param.elementTextTrim("servType"));

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck() || false == paramCheck()) {
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	private boolean paramCheck() {
		if (1 != queryType && 2 != queryType) {
			result = 1001;
			resultDesc = "查询类型非法";
			return false;
		}
		if (StringUtil.IsEmpty(queryInfo)) {
			result = 1005;
			resultDesc = "查询内容为空";
			return false;
		}
		if (10 != servType && 11 != servType && 14 != servType) {
			result = 1006;
			resultDesc = "业务类型非法";
			return false;
		}
		return true;
	}

	/**
	 * 回参
	 */
	@Override
	public String getReturnXml() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(String.valueOf(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		return document.asXML();
	}

	public int getQueryType() {
		return queryType;
	}

	public void setQueryType(int queryType) {
		this.queryType = queryType;
	}

	public String getQueryInfo() {
		return queryInfo;
	}

	public void setQueryInfo(String queryInfo) {
		this.queryInfo = queryInfo;
	}

	public int getServType() {
		return servType;
	}

	public void setServType(int servType) {
		this.servType = servType;
	}
}
