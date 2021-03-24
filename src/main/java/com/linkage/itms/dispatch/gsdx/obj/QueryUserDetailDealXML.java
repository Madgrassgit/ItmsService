package com.linkage.itms.dispatch.gsdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 甘肃电信查询用户详细信息接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月10日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryUserDetailDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(QueryUserDetailDealXML.class);
	SAXReader reader = new SAXReader();

	private String iParaType = "";
	private String Value = "";
	
	public QueryUserDetailDealXML(String methodName){
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
			else if(!"1".equals(iParaType) && !"2".equals(iParaType) &&!"3".equals(iParaType)){
				this.result ="-99";
				this.errMsg ="参数类型iParaType非法(not in 1 2 3)";
				logger.warn(methodName+"["+opId+"]参数类型iParaType非法(not in 1 2 3)");
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


	
	public String getiParaType()
	{
		return iParaType;
	}

	
	public void setiParaType(String iParaType)
	{
		this.iParaType = iParaType;
	}

	
	public String getValue()
	{
		return Value;
	}

	
	public void setValue(String value)
	{
		Value = value;
	}

	
}
