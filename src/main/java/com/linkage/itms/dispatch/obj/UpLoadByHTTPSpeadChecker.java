package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2019-2-27
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class UpLoadByHTTPSpeadChecker extends BaseChecker
{
	/**
	 * 状态
	 */
	private String dState = "";
	
	/**
	 * 用于上传的URL
	 */
	private String upLoadURL = "";
	
	/**
	 * 上传文件大小
	 */
	private String testFileLength = "";
	
	/**
	 * 上线速率
	 */
	private String uSpeed = "";
	
	/**
	 * 传输开始时间
	 */
	private String bOMTime = "";
	
	/**
	 * 传输结束时间
	 */
	private String eOMTime = "";
	
	/**
	 * 传输开始时间
	 */
	private String tCPOpenRequestTime = "";
	
	/**
	 * 传输结束时间
	 */
	private String tCPOpenResponseTime = "";
	
	/**
	 * 设备IP
	 */
	private String iPAddress = "";
	
	/**
	 * 接受字节数
	 */
	private String totalBytesSent = "";
	
	/**
	 * 宽带账号
	 */
	private String username = "";
	
	/**
	 * 设备id
	 */
	private String deviceId = "";
	
	public UpLoadByHTTPSpeadChecker(String inXml)
	{
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
			dState = param.elementTextTrim("DState");
			upLoadURL = param.elementTextTrim("UpLoadURL");
			testFileLength = param.elementTextTrim("TestFileLength");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		if(3 != userInfoType && 2 != userInfoType && 1 != userInfoType){
			result = 2;
			resultDesc = "用户信息类型非法";
			return false;
		}
		if (StringUtil.IsEmpty(userInfo))
		{
			result = 1;
			resultDesc = "用户信息不能为空";
			return false;
		}
		if(3==userInfoType && userInfo.length()<6){
			result = 1005;
			resultDesc = "设备序列号非法，设备序列号不可少于6位";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck())// || false == cityIdCheck())
		{
			return false;
		}
		if (StringUtil.IsEmpty(upLoadURL))
		{
			result = 1;
			resultDesc = "upLoadURL不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(dState))
		{
			result = 1;
			resultDesc = "DState状态不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(testFileLength))
		{
			result = 1;
			resultDesc = "TestFileLength上传文件大小不能为空";
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
		root.addElement("DevSn").addText(devSn == null ? "" : devSn);
		// username 是宽带账号
		root.addElement("UserName").addText(username == null ? "" : username);
		root.addElement("USpeed").addText(uSpeed);
		root.addElement("BOMTime").addText(bOMTime);
		root.addElement("EOMTime").addText(eOMTime);
		root.addElement("TCPOpenRequestTime").addText(tCPOpenRequestTime);
		root.addElement("TCPOpenResponseTime").addText(tCPOpenResponseTime);
		root.addElement("TotalBytesSent").addText(totalBytesSent);
		return document.asXML();
	}

	
	public String getdState()
	{
		return dState;
	}

	
	public void setdState(String dState)
	{
		this.dState = dState;
	}
	
	public String getUpLoadURL()
	{
		return upLoadURL;
	}

	public void setUpLoadURL(String upLoadURL)
	{
		this.upLoadURL = upLoadURL;
	}

	public String getTestFileLength()
	{
		return testFileLength;
	}
	
	public void setTestFileLength(String testFileLength)
	{
		this.testFileLength = testFileLength;
	}
	
	public String getuSpeed()
	{
		return uSpeed;
	}

	
	public void setuSpeed(String uSpeed)
	{
		this.uSpeed = uSpeed;
	}

	
	public String getbOMTime()
	{
		return bOMTime;
	}

	
	public void setbOMTime(String bOMTime)
	{
		this.bOMTime = bOMTime;
	}

	
	public String geteOMTime()
	{
		return eOMTime;
	}

	
	public void seteOMTime(String eOMTime)
	{
		this.eOMTime = eOMTime;
	}

	
	public String gettCPOpenRequestTime()
	{
		return tCPOpenRequestTime;
	}

	
	public void settCPOpenRequestTime(String tCPOpenRequestTime)
	{
		this.tCPOpenRequestTime = tCPOpenRequestTime;
	}

	
	public String gettCPOpenResponseTime()
	{
		return tCPOpenResponseTime;
	}

	
	public void settCPOpenResponseTime(String tCPOpenResponseTime)
	{
		this.tCPOpenResponseTime = tCPOpenResponseTime;
	}

	
	public String getiPAddress()
	{
		return iPAddress;
	}

	
	public void setiPAddress(String iPAddress)
	{
		this.iPAddress = iPAddress;
	}

	
	public String getTotalBytesSent()
	{
		return totalBytesSent;
	}

	
	public void setTotalBytesSent(String totalBytesSent)
	{
		this.totalBytesSent = totalBytesSent;
	}

	
	public String getUsername()
	{
		return username;
	}

	
	public void setUsername(String username)
	{
		this.username = username;
	}

	
	public String getDeviceId()
	{
		return deviceId;
	}

	
	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}
	
	
}
