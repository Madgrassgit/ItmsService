
package com.linkage.itms.dispatch.gsdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class GetCpeInfoBasicXML extends BaseDealXML
{

	public GetCpeInfoBasicXML(String methodName)
	{
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(GetCpeInfoBasicXML.class);
	SAXReader reader = new SAXReader();
	private String index = "";
	private String type = "";
	private String interfaceType = "";
	private String errcode = "-1000";

	@Override
	public Document getXML(String inXml)
	{
		this.inXml = inXml;
		try
		{
			logger.warn("{}[{}]入参校验开始", methodName, opId);
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("op_id"));
			type = StringUtil.getStringValue(inRoot.elementTextTrim("type"));
			index = StringUtil.getStringValue(inRoot.elementTextTrim("index"));
			interfaceType = StringUtil
					.getStringValue(inRoot.elementTextTrim("InterfaceType"));
			/**
			 * 0：逻辑ID，即激活码 1：宽带帐号，即Order结构中的ad_account字段。 2：Device ID(OUI-SN) 3：Device
			 * ID(OUI-SN)
			 */
			if (StringUtil.IsEmpty(type))
			{
				this.result = "-3";
				this.errMsg = "查询类型type为空";
				logger.warn("{}[{}]查询类型type为空", methodName, opId);
				return null;
			}
			else if (!"0".equals(type) && !"1".equals(type) && !"2".equals(type)
					&& !"3".equals(type))
			{
				this.result = "-3";
				this.errMsg = "查询类型type范围非法";
				logger.warn("{}[{}}]查询类型type范围非法：{}", methodName, opId, type);
				return null;
			}
			else if (StringUtil.IsEmpty(index))
			{
				result = errcode;
				errMsg = "查询值index为空";
				logger.warn("{}[{}}]查询值index为空", methodName, opId);
				return null;
			}
			if (StringUtil.IsEmpty(interfaceType))
			{
				result = errcode;
				errMsg = "查询值infoType为空";
				logger.warn("{}[{}]infoType", methodName, opId);
				return null;
			}
			return inDocument;
		}
		catch (Exception e)
		{
			result = errcode;
			errMsg = "其他错误";
			logger.error("{}[{}] Excetion occured!", methodName, opId);
			return null;
		}
	}

	public SAXReader getReader()
	{
		return reader;
	}

	public void setReader(SAXReader reader)
	{
		this.reader = reader;
	}

	public String getIndex()
	{
		return index;
	}

	public void setIndex(String index)
	{
		this.index = index;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getInterfaceType()
	{
		return interfaceType;
	}

	public void setInterfaceType(final String interfaceType)
	{
		this.interfaceType = interfaceType;
	}
}
