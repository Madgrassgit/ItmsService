package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
		
public class BindFailedChecker extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(BindFailedChecker.class);
    private String bindFailed;
    private String RstCode;
    private String RstMsg;
	public BindFailedChecker(String inXml)
	{
		callXml=inXml;
			
	}
/**
 * @author cczhong
 * @decirption xml格式检验
 */
	@Override
	public boolean check()
{       SAXReader reader = new SAXReader();
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
/**
 * @author cczhong
 * @decription 返回xml
 */
	@Override
	public String getReturnXml(){
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		Element root =  document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		root.addElement("RstCode").addText(StringUtil.getStringValue(RstCode));
		root.addElement("RstMsg").addText(StringUtil.getStringValue(RstMsg));
		root.addElement("bindFailed").addText(StringUtil.getStringValue(bindFailed));
		
		logger.warn("document = {}, xml = {}", document, document.asXML());
		return document.asXML();
			
	}
	
	boolean userInfoTypeCheck(){
		if(1 != userInfoType && 2 != userInfoType){
			RstCode = "1002";
			RstMsg = "用户信息类型非法";
			return false;
		}
		return true;
	}

public String getBindFailed()
{
	return bindFailed;
}

public void setBindFailed(String bindFailed)
{
	this.bindFailed = bindFailed;
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
	
}

	