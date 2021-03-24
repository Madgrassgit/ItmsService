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
 * 综调报竣工
 * 
 * 用于综调报竣工 需求单 JSDX_ITMS-REQ-20120220-LUHJ-003
 * 
 * @author Administrator
 *
 */
public class CompleteProForZDChecker extends BaseChecker{

	private static Logger logger = LoggerFactory.getLogger(CompleteProForZDChecker.class);
	
	// 逻辑SN
	private String userSN = "";
	
	// 业务受理时间
	private String dealDate = "";
	
	// 终端类型
	private String devType = "";
	
	// 失败原因
	private String failureReason  = "";
	
	// 报竣工时间
	private String configTime = "";
	
	// 成功状态
	private String succStatus = "";
	
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public CompleteProForZDChecker(String inXml) {
		callXml = inXml;
	}
	
	
	/**
	 * 检查调用接口入参的合法性
	 * 
	 */
	@Override
	public boolean check(){
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
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		//参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		
		return true;
	}
	
	
	
	/**
	 * 组装XML字符串，并返回
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
		
		// 逻辑SN
		if (null != userSN && false == userSN.isEmpty()) {
			root.addElement("SN").addText(userSN);
		} 
		
		// 设备属地
		if (null != cityId && false == cityId.isEmpty()) {
			root.addElement("CityId").addText(cityId);
		}
		
		// 业务受理时间
		if (null != dealDate && false == dealDate.isEmpty()) {
			root.addElement("DealDate").addText(dealDate);
		}
		
		// 设备序列号
		if (null != devSn && false == devSn.isEmpty()) {
			root.addElement("DevSN").addText(devSn);
		}
		
		// 终端类型
		if (null != devType && false == devType.isEmpty()) {
			root.addElement("DevType").addText(devType);
		}
		
		// 失败原因
		if (null != failureReason && false == failureReason.isEmpty()) {
			root.addElement("FailureReason").addText(failureReason);
		}
		
		// 报竣工时间
		if (null != configTime && false == configTime.isEmpty()) {
			root.addElement("ConfigTime").addText(configTime);
		}
		
		// 成功状态
		if (null != succStatus && false == succStatus.isEmpty()) {
			root.addElement("SuccStatus").addText(succStatus);
		}
		
		
		return document.asXML();
	}

	

	public String getUserSN() {
		return userSN;
	}


	public void setUserSN(String userSN) {
		this.userSN = userSN;
	}


	public String getDealDate() {
		return dealDate;
	}


	public void setDealDate(String dealDate) {
		this.dealDate = dealDate;
	}


	public String getDevType() {
		return devType;
	}


	public void setDevType(String devType) {
		this.devType = devType;
	}


	public String getFailureReason() {
		return failureReason;
	}


	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}


	public String getConfigTime() {
		return configTime;
	}


	public void setConfigTime(String configTime) {
		this.configTime = configTime;
	}


	public String getSuccStatus() {
		return succStatus;
	}


	public void setSuccStatus(String succStatus) {
		this.succStatus = succStatus;
	}
	
}
