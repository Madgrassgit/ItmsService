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
 * 仿真测速检查入参
 * @author jianglp (75508)
 * @version 1.0
 * @since 2016-12-13
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 
 */
public class SimulationSpeedChecker extends BaseChecker {

	public static final Logger logger = LoggerFactory
			.getLogger(SimulationSpeedChecker.class);
	// 逻辑SN
	private String userSn = "";
	// 失败原因
	private String failureReason = "";
	private String userName="";
	// 成功状态
	private String succStatus = "";
	//平均速度
	private String avgSampledValues="";
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	//最大速度
	private String maxSampledValues="";
	public SimulationSpeedChecker(String inXml) {
		callXml = inXml;
	}

	@Override
	public boolean check() {
		logger.debug("SimulationSpeedChecker>check()");
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
			userName = param.elementTextTrim("UserName");
			userSn= param.elementTextTrim("UserLOID");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				) {
			return false;
		}
		if (StringUtil.IsEmpty(userName)){
			result=1;
			resultDesc="用户宽带账号为空";
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
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		// 逻辑SN
		root.addElement("SN").addText(userSn);
		// 失败原因
		root.addElement("FailureReason").addText(failureReason);
		// 成功状态
		root.addElement("SuccStatus").addText(succStatus);
		//平均速度
		root.addElement("AvgSampledValues").addText(avgSampledValues);
		//最大速度
		root.addElement("MaxSampledValues").addText(maxSampledValues);
		return document.asXML();
	}
	
	
	/*public String getReturnXML(String rstCode,String resMsg,String devSn,String failureReason,String succStatus,String avgSampledValues,String maxSampledValues){
		this.result=Integer.parseInt(rstCode);
		this.resultDesc=resMsg;
		this.devSn=devSn;
		this.failureReason=failureReason;
		this.succStatus=succStatus;
		this.avgSampledValues=avgSampledValues;
		this.maxSampledValues=maxSampledValues;
		return getReturnXml().toString();
	}*/
	

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public String getSuccStatus() {
		return succStatus;
	}

	public void setSuccStatus(String succStatus) {
		this.succStatus = succStatus;
	}

	public String getAvgSampledValues() {
		return avgSampledValues;
	}

	public void setAvgSampledValues(String avgSampledValues) {
		this.avgSampledValues = avgSampledValues;
	}

	public String getMaxSampledValues() {
		return maxSampledValues;
	}

	public void setMaxSampledValues(String maxSampledValues) {
		this.maxSampledValues = maxSampledValues;
	}

	public String getUserSn() {
		return userSn;
	}

	public void setUserSn(String userSn) {
		this.userSn = userSn;
	}
}
