package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class StartPingDiagDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(StartPingDiagDealXML.class);
	
	SAXReader reader = new SAXReader();
	String successCount = "";
	String failureCount = "";
	String averageResponsetime = "";
	String minimumResponsetime = "";
	String maximumResponsetime = "";
	String pingHost = "";
	String pingTimes = "";
	String pingSize = "";

	public Document getXML (String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("op_id"));
			logicId = StringUtil.getStringValue(inRoot.elementTextTrim("logic_id"));																	
			pppUsename = StringUtil.getStringValue(inRoot.elementTextTrim("ppp_usename"));
			serialNumber = StringUtil.getStringValue(inRoot.elementTextTrim("serial_number"));
			customerId = StringUtil.getStringValue(inRoot.elementTextTrim("customer_id"));
			Element inPara = inRoot.element("ping_para");
			pingHost = StringUtil.getStringValue(inPara.elementTextTrim("ping_host"));
			pingTimes = StringUtil.getStringValue(inPara.elementTextTrim("ping_times"));
			pingSize = StringUtil.getStringValue(inPara.elementTextTrim("block_size"));
			
			Document document = DocumentHelper.createDocument();
			document.setXMLEncoding("GBK");
			Element root = document.addElement("root");
			// 接口调用唯一ID
			root.addElement("CmdID").addText(opId);
			// 结果代码
			root.addElement("CmdType").addText("CX_01");
			// 结果描述
			root.addElement("ClientType").addText("3");
			return document;
		} catch (DocumentException e) {
			logger.error("StartPingDiagDealXML.getParam() is error!", e);
			return null;
		}
	}

	
	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		// 接口调用唯一ID
		root.addElement("op_id").addText(opId);
		// 结果代码
		root.addElement("result").addText(result);
		// 结果描述
		root.addElement("err_msg").addText(errMsg);
		
		Element pingResult = root.addElement("ping_result");
		pingResult.addElement("success_count").addText(successCount);
		pingResult.addElement("failure_count").addText(failureCount);
		pingResult.addElement("average_responsetime").addText(averageResponsetime);
		pingResult.addElement("minimum_responsetime").addText(minimumResponsetime);
		pingResult.addElement("maximum_responsetime").addText(maximumResponsetime);
		return document.asXML();
	}

	public String getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(String successCount) {
		this.successCount = successCount;
	}

	public String getFailureCount() {
		return failureCount;
	}

	public void setFailureCount(String failureCount) {
		this.failureCount = failureCount;
	}

	public String getAverageResponsetime() {
		return averageResponsetime;
	}

	public void setAverageResponsetime(String averageResponsetime) {
		this.averageResponsetime = averageResponsetime;
	}

	public String getMinimumResponsetime() {
		return minimumResponsetime;
	}

	public void setMinimumResponsetime(String minimumResponsetime) {
		this.minimumResponsetime = minimumResponsetime;
	}

	public String getMaximumResponsetime() {
		return maximumResponsetime;
	}

	public void setMaximumResponsetime(String maximumResponsetime) {
		this.maximumResponsetime = maximumResponsetime;
	}

	public String getPingHost() {
		return pingHost;
	}

	public void setPingHost(String pingHost) {
		this.pingHost = pingHost;
	}

	public String getPingTimes() {
		return pingTimes;
	}

	public void setPingTimes(String pingTimes) {
		this.pingTimes = pingTimes;
	}

	public String getPingSize() {
		return pingSize;
	}

	public void setPingSize(String pingSize) {
		this.pingSize = pingSize;
	}
}
