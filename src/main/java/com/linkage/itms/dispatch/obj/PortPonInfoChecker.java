package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
		
public class PortPonInfoChecker extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(PortPonInfoChecker.class);
    private String TXPower;
    private String RXPower;
    private String LanStatus;
    private String WanStatus;
    private String status;
    private String RstCode;
    private String RstMsg;
	

	public PortPonInfoChecker(String inXml)
	{
		callXml=inXml;
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
				userInfo  = param.elementTextTrim("UserInfo");
				userInfoType=Integer.parseInt(param.elementTextTrim("UserInfoType"));
			}
			catch (Exception e)
			{
				logger.error("解析xml发生异常，e={}",e);
				RstCode = "1";
				RstMsg = "数据格式错误";
				return false;
			}
			if(!this.userInfoTypeCheck()){
				return false;
			}
			RstCode = "0";
			RstMsg = "成功";
			return true;
			
	}

	@Override
	public String getReturnXml()
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		Element root =  document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		root.addElement("RstCode").addText(StringUtil.getStringValue(RstCode));
		root.addElement("RstMsg").addText(StringUtil.getStringValue(RstMsg));
		root.addElement("TXPower").addText(StringUtil.getStringValue(TXPower));
		root.addElement("RXPower").addText(StringUtil.getStringValue(RXPower));
		if ("nx_dx".equals(Global.G_instArea)){
			root.addElement("status").addText(StringUtil.getStringValue(status));
		}else{
			root.addElement("LanStatus").addText(StringUtil.getStringValue(LanStatus));
			root.addElement("WanStatus").addText(StringUtil.getStringValue(WanStatus));
		}
		
		logger.warn("document = {}, xml = {}", document, document.asXML());
		return document.asXML();
			
	}
	boolean userInfoTypeCheck(){
		if(1 != userInfoType){
			RstCode = "1001";
			RstMsg = "用户信息类型非法";
			return false;
		}
		return true;
	}
	
	public String getTXPower()
	{
		return TXPower;
	}

	
	public void setTXPower(String tXPower)
	{
		TXPower = tXPower;
	}

	
	public String getRXPower()
	{
		return RXPower;
	}

	
	public void setRXPower(String rXPower)
	{
		RXPower = rXPower;
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

	public String getWanStatus()
	{
		return
				WanStatus;
	}

	public void setWanStatus(String wanStatus)
	{
		WanStatus =
				wanStatus;
	}

	public String getLanStatus()
	{
		return
				LanStatus;
	}

	public void setLanStatus(String lanStatus)
	{
		LanStatus =
				lanStatus;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
}

	