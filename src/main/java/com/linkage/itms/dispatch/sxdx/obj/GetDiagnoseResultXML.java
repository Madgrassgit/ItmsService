package com.linkage.itms.dispatch.sxdx.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

public class GetDiagnoseResultXML extends BaseDealXML {
	public GetDiagnoseResultXML(String methodName) {
		super(methodName);
	}
	private static Logger logger = LoggerFactory.getLogger(NorthQueryCPEParaDealXML.class);
	SAXReader reader = new SAXReader();

	private String index = "";
	private String type = "";
	private String procName = "";
	
	public Document getXML(String inXml) {
		this.inXml = inXml;
		try 
		{
			logger.warn(methodName+"["+opId+"]入参校验开始");
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("op_id"));
			type = StringUtil.getStringValue(inRoot.elementTextTrim("type"));
			index = StringUtil.getStringValue(inRoot.elementTextTrim("index"));
			procName = StringUtil.getStringValue(inRoot.elementTextTrim("procName"));
			
			/**
			 * 0：逻辑ID，即激活码
			   1：宽带帐号，即Order结构中的ad_account字段。
			   2：Device ID(OUI-SN)
			   3：Device ID(OUI-SN)
			 */
			if( StringUtil.IsEmpty(type))
			{
				this.result ="-99";
				this.errMsg ="查询类型type为空";
				logger.warn(methodName+"["+opId+"]查询类型type为空");
				return null;
			}
			else if(!"0".equals(type) && !"1".equals(type) && !"2".equals(type) && !"3".equals(type)&& !"4".equals(type)&& !"5".equals(type)){
				this.result ="-99";
				this.errMsg ="查询类型type范围非法";
				logger.warn(methodName+"["+opId+"]查询类型type范围非法：{}", type);
				return null;
			}
			else if(StringUtil.IsEmpty(index)){
				this.result ="-99";
				this.errMsg ="查询值index为空";
				logger.warn(methodName+"["+opId+"]查询值index为空");
				return null;
			}
			 if(StringUtil.IsEmpty(procName)){
				this.result ="-99";
				this.errMsg ="查询值procName为空";
				logger.warn(methodName+"["+opId+"]查询值procName为空");
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

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public String getProcName() {
		return procName;
	}

	public void setProcName(String procName) {
		this.procName = procName;
	}

	public void setType(String type) {
		this.type = type;
	}
}
