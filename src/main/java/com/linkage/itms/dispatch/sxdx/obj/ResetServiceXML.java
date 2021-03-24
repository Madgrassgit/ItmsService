package com.linkage.itms.dispatch.sxdx.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

public class ResetServiceXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(ResetServiceXML.class);
	SAXReader reader = new SAXReader();
	public ResetServiceXML(String methodName) {
		super(methodName);
	}

	private String iParaType = "";
	private String Value = "";

	public Document getXML(String inXml) {
		this.inXml = inXml;
		try
		{
			logger.warn(methodName+"["+opId+"]入参校验开始");
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("op_id"));
			iParaType = StringUtil.getStringValue(inRoot.elementTextTrim("iParaType"));
			Value = StringUtil.getStringValue(inRoot.elementTextTrim("Value"));

			/*1：终端唯一标识
			2：用户名，对应于dealOrder接口中的user_name参数
			3：宽带帐号*/

			if( StringUtil.IsEmpty(iParaType))
			{
				this.result ="-99";
				this.errMsg ="参数类型iParaType为空";
				logger.warn(methodName+"["+opId+"]参数类型iParaType为空");
				return null;
			}
			else if(!"0".equals(iParaType) && !"1".equals(iParaType) && !"2".equals(iParaType) &&!"3".equals(iParaType)&& !"4".equals(iParaType)&& !"5".equals(iParaType)){
				this.result ="-99";
				this.errMsg ="参数类型iParaType非法(not in 0 1 2 3 4 5)";
				logger.warn(methodName+"["+opId+"]参数类型iParaType非法(not in 0 1 2 3 4 5)");
				return null;
			}

			if( StringUtil.IsEmpty(Value) )
			{
				this.result ="-99";
				this.errMsg ="参数值Value为空";
				logger.warn(methodName+"["+opId+"]参数值Value为空");
				return null;
			}

			return inDocument;
		} catch (Exception e) {
			logger.error(methodName+"["+opId+"] Excetion occured!", e);
			return null;
		}
	}

	public SAXReader getReader() {
		return reader;
	}

	public void setReader(SAXReader reader) {
		this.reader = reader;
	}

	public String getiParaType() {
		return iParaType;
	}

	public void setiParaType(String iParaType) {
		this.iParaType = iParaType;
	}

	public String getValue() {
		return Value;
	}

	public void setValue(String value) {
		Value = value;
	}

}
