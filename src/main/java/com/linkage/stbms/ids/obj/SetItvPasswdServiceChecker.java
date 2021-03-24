package com.linkage.stbms.ids.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.Base64;
import com.linkage.stbms.ids.util.BaseChecker;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年9月19日
 * @category com.linkage.stbms.ids.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class SetItvPasswdServiceChecker extends BaseChecker
{
	private int userInfoType ;
	
	private String userInfo ;
	private String servPwd;
	private String pppoePwd;
	private String callXml;

	
	public SetItvPasswdServiceChecker(String inXml){
		this.callXml = inXml;
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
			servPwd = param.elementTextTrim("Passwdy");
			if(!StringUtil.IsEmpty(servPwd)){
				servPwd = Base64.encode(servPwd);
			}
			pppoePwd = param.elementTextTrim("Passwdj");
			if(!StringUtil.IsEmpty(pppoePwd)){
				pppoePwd = Base64.encode(pppoePwd);
			}
			System.out.println(System.currentTimeMillis()/1000+":userInfo["+userInfo+"]加密后的数据servPwd["+servPwd+"]pppoePwd["+pppoePwd+"]");
 		}
		catch (Exception e)
		{
			e.printStackTrace();
			rstCode = "1";
			rstMsg = "数据格式错误";
			return false;
		}
		if(/*3 != userInfoType && 2 != userInfoType &&*/ 1 != userInfoType){
			rstCode = "1001";
			rstMsg = "用户信息类型非法";
			return false;
		}
		if (StringUtil.IsEmpty(userInfo))
		{
			rstCode = "1002";
			rstMsg = "用户信息不合法";
			return false;
		}
		/*if(3==userInfoType && userInfo.length()<6){
			rstCode = "1005";
			rstMsg = "设备序列号非法，设备序列号不可少于6位";
			return false;
		}*/
		if(StringUtil.IsEmpty(servPwd) && StringUtil.IsEmpty(pppoePwd)){
			rstCode = "1011";
			rstMsg = "密码为空";
			return false;
		}
		
		// 参数合法性检查
		if (false == baseCheck())
		{
			return false;
		}
		rstCode = "0";
		rstMsg = "成功";
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
		root.addElement("RstCode").addText(rstCode);
		// 结果描述
		root.addElement("RstMsg").addText(rstMsg);
		
		return document.asXML();
	}
	
	
	public String getUserInfo()
	{
		return userInfo;
	}

	
	public void setUserInfo(String userInfo)
	{
		this.userInfo = userInfo;
	}

	
	public String getServPwd()
	{
		return servPwd;
	}

	
	public void setServPwd(String servPwd)
	{
		this.servPwd = servPwd;
	}

	
	public String getPppoePwd()
	{
		return pppoePwd;
	}

	
	public void setPppoePwd(String pppoePwd)
	{
		this.pppoePwd = pppoePwd;
	}
	public int getUserInfoType()
	{
		return userInfoType;
	}

	
	public void setUserInfoType(int userInfoType)
	{
		this.userInfoType = userInfoType;
	}

	
	public String getCallXml()
	{
		return callXml;
	}

	
	public void setCallXml(String callXml)
	{
		this.callXml = callXml;
	}
	
}
