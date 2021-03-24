package com.linkage.itms.dispatch.gsdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 甘肃电信北向查询终端 OUI-SN等信息接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月11日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class GetCpeOnlineInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(GetCpeOnlineInfoDealXML.class);
	SAXReader reader = new SAXReader();

	private String index = "";
	private String type = "";
	
	public GetCpeOnlineInfoDealXML(String methodName){
		super(methodName);
	}


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
			else if(!"0".equals(type) && !"1".equals(type) && !"2".equals(type) && !"3".equals(type)){
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
			
			return inDocument;
		} catch (Exception e) {
			logger.error(methodName+"["+opId+"] Excetion occured!", e);
			return null;
		}
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

	
}
