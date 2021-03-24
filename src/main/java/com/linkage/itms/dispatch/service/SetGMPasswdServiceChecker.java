package com.linkage.itms.dispatch.service;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.Base64;
import com.linkage.itms.dispatch.obj.BaseChecker;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年9月18日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class SetGMPasswdServiceChecker extends BaseChecker
{
	
	private String passwd = null;

	public SetGMPasswdServiceChecker(String paramXml){
		this.callXml = paramXml;
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
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			passwd = param.elementTextTrim("Passwd");
			if(!StringUtil.IsEmpty(passwd)){
				passwd = Base64.encode(passwd);
			}
			System.out.println(System.currentTimeMillis()/1000+":userInfo["+userInfo+"]加密后的数据passwd["+passwd+"]");
 		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		if(3 != userInfoType && 2 != userInfoType && 1 != userInfoType){
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
		if (StringUtil.IsEmpty(userInfo))
		{
			result = 1002;
			resultDesc = "用户信息不能为空";
			return false;
		}
		if(3==userInfoType && userInfo.length()<6){
			result = 1005;
			resultDesc = "设备序列号非法，设备序列号不可少于6位";
			return false;
		}
		if(StringUtil.IsEmpty(passwd)){
			result = 1011;
			resultDesc = "密码为空";
			return false;
		}
		
		// 参数合法性检查
		if (false == baseCheck())
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true; 
	}

	@Override
	public String getReturnXml()
	{
		 
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		return document.asXML();
	}

	public String getPasswd()
	{
		return passwd;
	}

	public void setPasswd(String passwd)
	{
		this.passwd = passwd;
	}
}
