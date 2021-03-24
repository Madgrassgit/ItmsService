package com.linkage.itms.ct.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 路由下发支持情况查询数据对象类
 * 
 * @author Jason(3412)
 * @date 2010-7-13
 */
public class CtInfoQueryChecker extends CtBaseChecker {

	private static Logger logger = LoggerFactory
			.getLogger(CtInfoQueryChecker.class);

	// 终端厂商
	private String devFactory;
	// 终端型号
	private String devModel;
	// 完整终端序列号
	private String devSerialWhole;
	// 是否支持路由 1：支持 -1：不支持
	private int routeSupported;
	// 路由还是桥接 1：桥接 2：路由
	private int netType;

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public CtInfoQueryChecker(String inXml) {
		logger.debug("CtInfoQueryChecker()");
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
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));

			Element param = root.element("Param");

			username = param.elementTextTrim("UserName");
			devSn = param.elementTextTrim("DevSN");

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck() || false == usernameCheck()
				|| false == devSnCheck()) {
			return false;
		}

		result = 0;
		resultDesc = "成功";

		return true;
	}

	@Override
	boolean cmdTypeCheck() {
		logger.debug("cmdTypeCheck()");
		return "CX_01".equals(cmdType);
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
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);

		if (0 == result) {
			Element param = root.addElement("Param");
			param.addElement("DevFactory").addText("" + devFactory);
			param.addElement("DevModel").addText("" + devModel);
			param.addElement("DevSn").addText("" + devSerialWhole);
			param.addElement("RouteSupported").addText("" + routeSupported);
			param.addElement("NetType").addText("" + netType);
		}

		return document.asXML();
	}

	/** getter, setter methods */

	public String getDevFactory() {
		return devFactory;
	}

	public void setDevFactory(String devFactory) {
		this.devFactory = devFactory;
	}

	public String getDevModel() {
		return devModel;
	}

	public void setDevModel(String devModel) {
		this.devModel = devModel;
	}

	public String getDevSerialWhole() {
		return devSerialWhole;
	}

	public void setDevSerialWhole(String devSerialWhole) {
		this.devSerialWhole = devSerialWhole;
	}

	public int getRouteSupported() {
		return routeSupported;
	}

	public void setRouteSupported(int routeSupported) {
		this.routeSupported = routeSupported;
	}

	public int getNetType() {
		return netType;
	}

	public void setNetType(int netType) {
		this.netType = netType;
	}

}
