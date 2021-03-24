package com.linkage.itms.dispatch.obj;


import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class CloudPingIPSecDiagnosticChecker extends CloudBaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(CloudPingIPSecDiagnosticChecker.class);

	protected String ipSecPassageWay = "";
	protected String sourceAddress = "";
	

	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public CloudPingIPSecDiagnosticChecker(String inXml) {
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
			ipSecPassageWay = param.elementTextTrim("IPSecPassageWay");
			packageByte = param.elementTextTrim("PackageByte");
			sourceAddress = StringUtil.getStringValue(param.elementTextTrim("SourceAddress"));
			ipOrDomainName = StringUtil.getStringValue(param.elementTextTrim("IPOrDomainName"));
			packageNum = param.elementTextTrim("PackageNum");
			timeOut = param.elementTextTrim("TimeOut");
		} catch (Exception e) {
			e.printStackTrace();
			result = 3;
			resultDesc = "入参格式错误";
			return false;
		}
		//参数合法性检查
		if (!baseCheck() || !isNullCheck() || 
				!userInfoTypeCheck() || !userInfoCheck()) {
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	public String getIpSecPassageWay() {
		return ipSecPassageWay;
	}

	public void setIpSecPassageWay(String ipSecPassageWay) {
		this.ipSecPassageWay = ipSecPassageWay;
	}

	public String getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
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
		
		Element par = root.addElement("Param");
		// 当前最新绑定的Loid
		par.addElement("Loid").addText(loid);
		// 通过账号查到多个Loid，除去最新绑定Loid之外的Loid集合
		par.addElement("LoidPrev").addText(loidPrev);
		// 设备序列号
		par.addElement("DevSn").addText(deviceSN);
		// 成功数
		par.addElement("SuccesNum").addText(succesNum);
		// 失败数
		par.addElement("FailNum").addText(failNum);
		// 平均响应时间
		par.addElement("AvgResponseTime").addText(avgResponseTime);
		// 最小响应时间
		par.addElement("MinResponseTime").addText(minResponseTime);
		// 最大响应时间
		par.addElement("MaxResponseTime").addText(maxResponseTime);
		// 丢包率
		par.addElement("PacketLossRate").addText(packetLossRate);
		// IP地址或域名
		par.addElement("IPOrDomainName").addText(ipOrDomainName);
		// 测试源地址
		par.addElement("SourceAddress").addText(sourceAddress);
		return document.asXML();
	}
	
	/**
	 * 入参为空验证
	 * @return
	 */
	private boolean isNullCheck() {
		if (StringUtil.IsEmpty(ipSecPassageWay) || 
				StringUtil.IsEmpty(packageByte) ||
				StringUtil.IsEmpty(packageNum) ||
				StringUtil.IsEmpty(timeOut)) {
			result = 3;
			resultDesc = "入参格式错误";
			return false;
		}
		return true;
	}
}
