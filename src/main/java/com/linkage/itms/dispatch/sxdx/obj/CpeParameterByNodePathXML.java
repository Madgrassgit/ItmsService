package com.linkage.itms.dispatch.sxdx.obj;

import com.linkage.commons.util.StringUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

public class CpeParameterByNodePathXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(NorthQueryCPEParaDealXML.class);
	public CpeParameterByNodePathXML(String methodName) {
		super(methodName);
	}
	private String index = "";
	private String type = "";
	private String nodePath = "";
	SAXReader reader = new SAXReader();

	public Document getXML(String inXml) {
		logger.warn(methodName + "[" + opId + "]入参校验开始");
		this.inXml = inXml;
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("op_id"));
			type = StringUtil.getStringValue(inRoot.elementTextTrim("type"));
			index = StringUtil.getStringValue(inRoot.elementTextTrim("index"));
			nodePath = StringUtil.getStringValue(inRoot.elementTextTrim("nodePath"));
			/**
			 * 0：逻辑ID，即激活码
			   1：宽带帐号，即Order结构中的ad_account字段。
			   2：Device ID(OUI-SN)
			   3：Device ID(OUI-SN)
			 */
			if( StringUtil.IsEmpty(type))
			{
				this.result ="-3";
				this.errMsg ="查询类型type为空";
				logger.warn(methodName+"["+opId+"]查询类型type为空");
				return null;
			}
			else if(!"0".equals(type) && !"1".equals(type) && !"2".equals(type) && !"3".equals(type)&& !"4".equals(type)&& !"5".equals(type)){
				this.result ="-3";
				this.errMsg ="查询类型type范围非法";
				logger.warn(methodName+"["+opId+"]查询类型type范围非法：{}", type);
				return null;
			}
			else if(StringUtil.IsEmpty(index)){
				this.result ="-3";
				this.errMsg ="查询值index为空";
				logger.warn(methodName+"["+opId+"]查询值index为空");
				return null;
			}
			if(StringUtil.IsEmpty(nodePath)){
				this.result ="-3";
				this.errMsg ="查询值nodePath为空";
				logger.warn(methodName+"["+opId+"]查询值nodePath为空");
				return null;
			}
			if(!nodePath.endsWith(".")){
				this.result ="-3";
				this.errMsg ="查询值nodePath值不正确";
				logger.warn(methodName+"["+opId+"]查询值nodePath值不正确");
				return null;
			}
			if(nodePath.split("\\.").length<4){
				this.result ="-3";
				this.errMsg ="查询值nodePath值不正确";
				logger.warn(methodName+"["+opId+"]查询值nodePath值不正确");
				return null;
			}
			return inDocument;
		} catch (Exception e) {
			logger.error("转换错误:{}", ExceptionUtils.getStackTrace(e));
			return null;
		}
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

	public void setType(String type) {
		this.type = type;
	}

	public String getNodePath() {
		return nodePath;
	}

	public void setNodePath(String nodePath) {
		this.nodePath = nodePath;
	}


}
