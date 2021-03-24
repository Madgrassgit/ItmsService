package com.linkage.itms.dispatch.sxdx.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

/**
 * 甘肃电信删除单个终端接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月12日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DelSingleCPEDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(DelSingleCPEDealXML.class);
	SAXReader reader = new SAXReader();

	private String cpeID = "";
	
	public DelSingleCPEDealXML(String methodName){
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
			cpeID = StringUtil.getStringValue(inRoot.elementTextTrim("cpeID"));

			if( StringUtil.IsEmpty(cpeID) || !cpeID.contains("-"))
			{
				this.result ="-99";
				this.errMsg ="参数类型cpeID为空或格式有误";
				logger.warn(methodName+"["+opId+"]参数类型cpeID为空或格式有误:{}", cpeID);
				return null;
			}
			
			return inDocument;
		} catch (Exception e) {
			logger.error(methodName+"["+opId+"] Excetion occured!", e);
			return null;
		}
	}


	
	public String getCpeID()
	{
		return cpeID;
	}



	
	public void setCpeID(String cpeID)
	{
		this.cpeID = cpeID;
	}



	
	public String getOpId()
	{
		return opId;
	}



	
	public void setOpId(String opId)
	{
		this.opId = opId;
	}

}
