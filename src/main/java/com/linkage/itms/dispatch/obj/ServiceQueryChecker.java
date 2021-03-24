package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * @author Jason(3412)
 * @date 2010-6-21
 */
public class ServiceQueryChecker extends BaseChecker {

	// 日志记录对象
	private static Logger logger = LoggerFactory
			.getLogger(ServiceQueryChecker.class);
	// 业务数目
	private int servNum = 0;
	// 业务代码
	private int[] arrServCode;
	// 业务结果
	private int[] arrServResult;
	

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public ServiceQueryChecker(String inXml) {
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
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			searchType = StringUtil.getIntegerValue(param
					.elementTextTrim("SearchType"));
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			devSn = param.elementTextTrim("DevSN");
			cityId = param.elementTextTrim("CityId");

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck() || false == devSnCheck()
				|| false == cityIdCheck() || false == searchTypeCheck()) {
			return false;
		}

		result = 0;
		resultDesc = "成功";

		return true;
	}

	/**
	 * 返回结果字符串
	 */
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

		if (0 == result) {// 查询成功
			Element param = root.addElement("Param");
			for (int i = 0; i < servNum; i++) {
				Element service = param.addElement("Service");
				service.addElement("ServiceCode").addText("" + arrServCode[i]);
				service.addElement("Result").addText("" + arrServResult[i]);
				service.addElement("Desc").addText(convertResultDesc(arrServResult[i]));
			}
		}

		return document.asXML();
	}
	
	/**
	 * 将结果标识转换成结果描述
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-24
	 * @return String
	 */
	private String convertResultDesc(int result){
		logger.debug("convertResultDesc({})", result);
		String desc = null;
		switch(result){
		case 1:
			desc = "已成功开通";
			break;
		case 0:
			desc = "未开通";
			break;
		default:
			desc = "开通失败";
		}
		return desc;
	}
	
	

	public int getServNum() {
		return servNum;
	}

	public void setServNum(int servNum) {
		this.servNum = servNum;
	}

	public int[] getArrServCode() {
		return arrServCode;
	}

	public void setArrServCode(int[] arrServCode) {
		this.arrServCode = arrServCode;
	}

	public int[] getArrServResult() {
		return arrServResult;
	}

	public void setArrServResult(int[] arrServResult) {
		this.arrServResult = arrServResult;
	}

}
