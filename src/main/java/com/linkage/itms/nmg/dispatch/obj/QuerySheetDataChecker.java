
package com.linkage.itms.nmg.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * @author yinlei (Ailk No.73167)
 * @version 1.0
 * @since 2016年6月12日
 * @category com.linkage.itms.nmg.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QuerySheetDataChecker extends NmgBaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(QuerySheetDataChecker.class);
	private String bussinessType = "";
	private String WanType_yingpei = "";
	private String WanType_shipei = "";
	private String BindPort_yingpei = "";
	private String BindPort_shipei = "";
	private String Username_yingpei = "";
	private String Username_shipei = "";
	private String Password_yingpei = "";
	private String Password_shipei = "";
	private String VlanId_yingpei = "";
	private String VlanId_shipei = "";

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public QuerySheetDataChecker(String inXml)
	{
		callXml = inXml;
	}

	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		logger.debug("check()");
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
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			bussinessType = param.elementTextTrim("BussinessType");
			;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck() || false == bussinessTypeCheck())
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	private boolean bussinessTypeCheck()
	{
		if (StringUtil.IsEmpty(bussinessType))
		{
			result = 1004;
			resultDesc = "稽核业务类型不合法";
			return false;
		}
		if (!"1".equals(bussinessType))
		{
			result = 1005;
			resultDesc = "暂不支持宽带业务以外的稽核业务";
			return false;
		}
		return true;
	}

	/**
	 * 返回绑定调用结果字符串
	 */
	@Override
	public String getReturnXml()
	{
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
		Element Internet = root.addElement("Internet");
		Internet.addElement("WanType_yingpei").addText(WanType_yingpei);
		Internet.addElement("WanType_shipei").addText(WanType_shipei);
		Internet.addElement("BindPort_yingpei").addText(BindPort_yingpei);
		Internet.addElement("BindPort_shipei").addText(BindPort_shipei);
		Internet.addElement("Username_yingpei").addText(Username_yingpei);
		Internet.addElement("Username_shipei").addText(Username_shipei);
		Internet.addElement("Password_yingpei").addText(Password_yingpei);
		Internet.addElement("Password_shipei").addText(Password_shipei);
		Internet.addElement("VlanId_yingpei").addText(VlanId_yingpei);
		Internet.addElement("VlanId_shipei").addText(VlanId_shipei);
		return document.asXML();
	}

	public String getBussinessType()
	{
		return bussinessType;
	}

	public void setBussinessType(String bussinessType)
	{
		this.bussinessType = bussinessType;
	}

	public String getWanType_yingpei()
	{
		return WanType_yingpei;
	}

	public String getWanType_shipei()
	{
		return WanType_shipei;
	}

	public String getBindPort_yingpei()
	{
		return BindPort_yingpei;
	}

	public String getBindPort_shipei()
	{
		return BindPort_shipei;
	}

	public String getUsername_yingpei()
	{
		return Username_yingpei;
	}

	public String getUsername_shipei()
	{
		return Username_shipei;
	}

	public String getPassword_yingpei()
	{
		return Password_yingpei;
	}

	public String getPassword_shipei()
	{
		return Password_shipei;
	}

	public String getVlanId_yingpei()
	{
		return VlanId_yingpei;
	}

	public String getVlanId_shipei()
	{
		return VlanId_shipei;
	}

	public void setWanType_yingpei(String wanType_yingpei)
	{
		this.WanType_yingpei = wanType_yingpei;
	}

	public void setWanType_shipei(String wanType_shipei)
	{
		WanType_shipei = wanType_shipei;
	}

	public void setBindPort_yingpei(String bindPort_yingpei)
	{
		BindPort_yingpei = bindPort_yingpei;
	}

	public void setBindPort_shipei(String bindPort_shipei)
	{
		BindPort_shipei = bindPort_shipei;
	}

	public void setUsername_yingpei(String username_yingpei)
	{
		Username_yingpei = username_yingpei;
	}

	public void setUsername_shipei(String username_shipei)
	{
		Username_shipei = username_shipei;
	}

	public void setPassword_yingpei(String password_yingpei)
	{
		Password_yingpei = password_yingpei;
	}

	public void setPassword_shipei(String password_shipei)
	{
		Password_shipei = password_shipei;
	}

	public void setVlanId_yingpei(String vlanId_yingpei)
	{
		VlanId_yingpei = vlanId_yingpei;
	}

	public void setVlanId_shipei(String vlanId_shipei)
	{
		VlanId_shipei = vlanId_shipei;
	}
}
