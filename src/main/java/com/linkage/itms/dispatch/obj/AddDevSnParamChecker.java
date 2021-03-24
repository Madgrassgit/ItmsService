
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
 * 光猫管控设备序列号添加接口
 * 
 * @author yinlei3 (Ailk No.73167)
 * @version 1.0
 * @since 2016年9月5日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class AddDevSnParamChecker extends BaseChecker
{

	public static final Logger logger = LoggerFactory
			.getLogger(AddDevSnParamChecker.class);
	// 操作人
	private String operaUser;
	// 操作时间
	private String operaTime;
	// 导入时间
	private String saveTime;
	// 设备型号
	private String model;
	// 厂商
	private String vendor;
	// 购买时间
	private String buyTime;
	// 设备MAC
	private String devMac;

	/**
	 * 构造函数 入参 inXml XML格式
	 * 
	 * @param inXml
	 */
	public AddDevSnParamChecker(String inXml)
	{
		callXml = inXml;
	}

	/**
	 * 检查调用接口入参的合法性
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
			Element devSnParam = param.element("DevSnParam");
			oui = devSnParam.elementTextTrim("OUI");
			devSn = devSnParam.elementTextTrim("DEVSN");
			cityName = devSnParam.elementTextTrim("CITY");
			buyTime = devSnParam.elementTextTrim("BUYTIME");
			devMac = devSnParam.elementTextTrim("DEVMAC");
			vendor = devSnParam.elementTextTrim("VENDOR");
			model = devSnParam.elementTextTrim("MODEL");
			saveTime = devSnParam.elementTextTrim("SAVETIME");
			operaUser = param.elementTextTrim("OperaUser");
			operaTime = param.elementTextTrim("OperaTime");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == macCheck() || false == devSnCheck()
				|| false == parmEmptyCheck())
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	/**
	 * 检查厂商OUI
	 * 
	 * @return
	 */
	private boolean parmEmptyCheck()
	{
		if (StringUtil.IsEmpty(oui))
		{
			result = 1001;
			resultDesc = "设备OUI信息不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(vendor))
		{
			result = 1004;
			resultDesc = "设备厂商不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(model))
		{
			result = 1005;
			resultDesc = "设备型号信息不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(operaUser))
		{
			result = 1007;
			resultDesc = "操作人信息不能为空";
			return false;
		}
		if (StringUtil.IsEmpty(operaTime))
		{
			result = 1008;
			resultDesc = "操作时间不能为空";
			return false;
		}
		return true;
	}

	/**
	 * mac合法性检查
	 */
	protected boolean macCheck()
	{
		if (!StringUtil.IsEmpty(devMac))
		{
			if (false == patternMac.matcher(devMac).matches() || devMac.length() < 6)
			{
				result = 1006;
				resultDesc = "MAC地址不合法";
				return false;
			}
		}
		return true;
	}

	/**
	 * 设备序列号合法性检查
	 */
	protected boolean devSnCheck()
	{
		if (StringUtil.IsEmpty(devSn))
		{
			result = 1002;
			resultDesc = "设备序列号不能为空";
			return false;
		}
		else
		{
			if (false == pattern.matcher(devSn).matches() || devSn.length() < 6)
			{
				result = 1005;
				resultDesc = "设备序列号不合法";
				return false;
			}
		}
		return true;
	}

	/**
	 * 组装XML字符串，并返回
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

		return document.asXML();
	}

	public String getOperaUser()
	{
		return operaUser;
	}

	public String getOperaTime()
	{
		return operaTime;
	}

	public String getSaveTime()
	{
		return saveTime;
	}

	public String getModel()
	{
		return model;
	}

	public String getVendor()
	{
		return vendor;
	}

	public String getBuyTime()
	{
		return buyTime;
	}

	public String getDevMac()
	{
		return devMac;
	}

	public void setOperaUser(String operaUser)
	{
		this.operaUser = operaUser;
	}

	public void setOperaTime(String operaTime)
	{
		this.operaTime = operaTime;
	}

	public void setSaveTime(String saveTime)
	{
		this.saveTime = saveTime;
	}

	public void setModel(String model)
	{
		this.model = model;
	}

	public void setVendor(String vendor)
	{
		this.vendor = vendor;
	}

	public void setBuyTime(String buyTime)
	{
		this.buyTime = buyTime;
	}

	public void setDevMac(String devMac)
	{
		this.devMac = devMac;
	}
}
