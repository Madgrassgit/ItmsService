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
 * 
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2014年12月25日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class UpgradeResultsChecker extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(UpgradeResultsChecker.class);
	private Long taskNumber;
	
	// 版本型号：
	String device_model;
	// 设备型号id
	String device_model_id;
	// 厂商id
	String vendor_id;
	// 软件版本
	String softwareversion;
	// 老版本
	String softwareversions;
	private String reason;
	private int upgradeNumber;
	private int SuccessfulNumber;
	
	public UpgradeResultsChecker(String callXml)
	{
		this.callXml = callXml;
	}

	public boolean check()
	{
		SAXReader reader = new SAXReader();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			cityName  = param.elementTextTrim("CityName");
			taskNumber =  StringUtil.getLongValue(param.elementTextTrim("Tasknumber"));
		}
		catch (Exception e)
		{
			logger.error("解析xml发生异常，e={}",e);
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		return true;
	}
	
	
	public String getWrongReturnXml(){
		logger.debug("getWrongReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root =  document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		return document.asXML();
	}
	
	/**
	 * 获得返回值
	 * @return 返回值
	 */
	public String getReturnXml(){
		logger.debug("getBaseReturnXml(), cmdId={}, cmdType={}, device_model={}",new Object[]{cmdId,cmdType,device_model});
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root =  document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		root.addElement("CmdType").addText(cmdType);
		root.addElement("ClientType").addText(clientType+"");
		Element param = root.addElement("Param");
		
		param.addElement("device_model").addText(device_model);
		param.addElement("softwareversion").addText(softwareversion);
		param.addElement("softwareversions").addText(softwareversions);
		param.addElement("Reason").addText(reason);
		param.addElement("upgradeNumber").addText(upgradeNumber+"");
		param.addElement("SuccessfulNumber").addText(SuccessfulNumber+"");
		param.addElement("CityName").addText(cityName);
		param.addElement("Tasknumber").addText(taskNumber+"");
		return document.asXML();
	}
	
	public Long getTaskNumber()
	{
		return taskNumber;
	}
	
	public void setTaskNumber(Long taskNumber)
	{
		this.taskNumber = taskNumber;
	}

	public String getDevice_model()
	{
		return device_model;
	}
	
	public void setDevice_model(String device_model)
	{
		this.device_model = device_model;
	}
	
	public String getDevice_model_id()
	{
		return device_model_id;
	}
	
	public void setDevice_model_id(String device_model_id)
	{
		this.device_model_id = device_model_id;
	}
	
	public String getVendor_id()
	{
		return vendor_id;
	}
	
	public void setVendor_id(String vendor_id)
	{
		this.vendor_id = vendor_id;
	}
	
	public String getSoftwareversion()
	{
		return softwareversion;
	}
	
	public void setSoftwareversion(String softwareversion)
	{
		this.softwareversion = softwareversion;
	}
	
	public String getSoftwareversions()
	{
		return softwareversions;
	}

	public void setSoftwareversions(String softwareversions)
	{
		this.softwareversions = softwareversions;
	}

	public String getReason()
	{
		return reason;
	}

	public void setReason(String reason)
	{
		this.reason = reason;
	}
	
	public int getUpgradeNumber()
	{
		return upgradeNumber;
	}
	
	public void setUpgradeNumber(int upgradeNumber)
	{
		this.upgradeNumber = upgradeNumber;
	}
	
	public int getSuccessfulNumber()
	{
		return SuccessfulNumber;
	}
	
	public void setSuccessfulNumber(int successfulNumber)
	{
		SuccessfulNumber = successfulNumber;
	}
	
}
