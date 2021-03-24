package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
		
public class QueryNormalVersionChecker extends BaseChecker
{

	private static Logger logger = LoggerFactory.getLogger(QueryNormalVersionChecker.class);
	private String vendor;
	private String model;
	private String RstCode;
    private String RstMsg;
    private String handwareVersion;
    private String softwareVersion;
    private String isNormal;
    private String accessStyleRelayId;
    private String ipType;
    private String voipProtocol;
    private String specId;
    private String mbBroadband;
    private String deviceTypeId;
    private List<HashMap<String,String>> sheeInfoList;
    
	public QueryNormalVersionChecker(String inXml){
		callXml=inXml;
	}
	
	public QueryNormalVersionChecker(){
		
	}
	@Override
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
				vendor  = param.elementTextTrim("vendor");
				model=param.elementTextTrim("model");
			}
			catch (Exception e)
			{
				logger.error("解析xml发生异常，e={}",e);
				RstCode = "1";
				RstMsg = "数据格式错误";
				return false;
			}
			RstCode = "0";
			RstMsg = "成功";
			return true;
	}

	@Override
	public String getReturnXml()
	{
		logger.warn("进入到Return");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		Element root =  document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		root.addElement("RstCode").addText(StringUtil.getStringValue(RstCode));
		root.addElement("RstMsg").addText(StringUtil.getStringValue(RstMsg));
		if(null!=sheeInfoList){
			for(HashMap<String,String> map:sheeInfoList){
				Element sheetInfo =  root.addElement("sheetInfo");
				sheetInfo.addElement("vendor").addText(StringUtil.getStringValue(vendor));
				sheetInfo.addElement("DevModel").addText(StringUtil.getStringValue(model));
				sheetInfo.addElement("HandwareVersion").addText(StringUtil.getStringValue(map, "hardwareversion",""));
				sheetInfo.addElement("SoftwareVersion").addText(StringUtil.getStringValue(map, "softwareversion",""));
				sheetInfo.addElement("IsNormal").addText(StringUtil.getStringValue(map, "is_normal",""));
				sheetInfo.addElement("AccessStyleRelayId").addText(StringUtil.getStringValue(map, "access_style_relay_id",""));
				sheetInfo.addElement("IpType").addText(StringUtil.getStringValue(map, "ip_type",""));
				sheetInfo.addElement("voipProtocol").addText(StringUtil.getStringValue(map, "voipProtocol",""));
				sheetInfo.addElement("specId").addText(StringUtil.getStringValue(map, "spec_id",""));
				sheetInfo.addElement("mbBroadband").addText(StringUtil.getStringValue(map, "mbbroadband",""));
			}
		}
		logger.warn("document = {}, xml = {}", document, document.asXML());
		return document.asXML();
	}
	

	
	
	public String getVendor()
	{
		return vendor;
	}

	
	public void setVendor(String vendor)
	{
		this.vendor = vendor;
	}

	public String getModel()
	{
		return model;
	}
	
	public void setModel(String model)
	{
		this.model = model;
	}
	
	public String getRstCode()
	{
		return RstCode;
	}
	
	public void setRstCode(String rstCode)
	{
		RstCode = rstCode;
	}
	
	public String getRstMsg()
	{
		return RstMsg;
	}
	
	public void setRstMsg(String rstMsg)
	{
		RstMsg = rstMsg;
	}
	
	public String getHandwareVersion()
	{
		return handwareVersion;
	}
	
	public void setHandwareVersion(String handwareVersion)
	{
		this.handwareVersion = handwareVersion;
	}
	
	public String getSoftwareVersion()
	{
		return softwareVersion;
	}
	
	public void setSoftwareVersion(String softwareVersion)
	{
		this.softwareVersion = softwareVersion;
	}
	
	public String getIsNormal()
	{
		return isNormal;
	}
	
	public void setIsNormal(String isNormal)
	{
		this.isNormal = isNormal;
	}
	
	public String getAccessStyleRelayId()
	{
		return accessStyleRelayId;
	}
	
	public void setAccessStyleRelayId(String accessStyleRelayId)
	{
		this.accessStyleRelayId = accessStyleRelayId;
	}
	
	public String getIpType()
	{
		return ipType;
	}
	
	public void setIpType(String ipType)
	{
		this.ipType = ipType;
	}
	
	public String getVoipProtocol()
	{
		return voipProtocol;
	}
	
	public void setVoipProtocol(String voipProtocol)
	{
		this.voipProtocol = voipProtocol;
	}
	
	public String getSpecId()
	{
		return specId;
	}
	
	public void setSpecId(String specId)
	{
		this.specId = specId;
	}
	
	public String getMbBroadband()
	{
		return mbBroadband;
	}
	
	public void setMbBroadband(String mbBroadband)
	{
		this.mbBroadband = mbBroadband;
	}
	
	public List<HashMap<String, String>> getSheeInfoList()
	{
		return sheeInfoList;
	}
	
	public void setSheeInfoList(List<HashMap<String, String>> sheeInfoList)
	{
		this.sheeInfoList = sheeInfoList;
	}

	public String getDeviceTypeId()
	{
		return
				deviceTypeId;
	}

	public void setDeviceTypeId(String deviceTypeId)
	{
		this.deviceTypeId =
				deviceTypeId;
	}
	
}

	