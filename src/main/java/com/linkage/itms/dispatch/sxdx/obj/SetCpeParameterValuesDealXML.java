package com.linkage.itms.dispatch.sxdx.obj;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.sxdx.beanObj.SetParameterResult;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 甘肃电信重启接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月13日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class SetCpeParameterValuesDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(SetCpeParameterValuesDealXML.class);
	SAXReader reader = new SAXReader();

	private String index = "";
	private String type = "";
	private List<HashMap<String, String>> paramList= new ArrayList<HashMap<String, String>>();

	public SetCpeParameterValuesDealXML(String methodName){
		super(methodName);
	}


	public SetParameterResult checkXML(String inXml) {
		this.inXml = inXml;
		SetParameterResult result = new SetParameterResult();
		try 
		{
			logger.warn(methodName+"["+opId+"]入参校验开始");
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("op_id"));
			type = StringUtil.getStringValue(inRoot.elementTextTrim("type"));
			index = StringUtil.getStringValue(inRoot.elementTextTrim("index"));
			
			if( StringUtil.IsEmpty(type))
			{
				result.setErrorCode(-3);
				result.setErrorInfo("查询类型type为空");
				logger.warn(methodName+"["+opId+"]查询类型type为空");
				return null;
			}
			else if(!"0".equals(type) && !"1".equals(type) && !"2".equals(type) && !"3".equals(type) && !"4".equals(type) && !"5".equals(type)){
				result.setErrorCode(-3);
				result.setErrorInfo("查询类型type范围非法");
				logger.warn(methodName+"["+opId+"]查询类型type范围非法：{}", type);
				return null;
			}
			else if(StringUtil.IsEmpty(index)){
				result.setErrorCode(-3);
				result.setErrorInfo("查询值index为空");
				logger.warn(methodName+"["+opId+"]查询值index为空");
				return null;
			}
			
			List<Element> params = inRoot.elements("param");
			if(null == params || params.size() ==0){
				result.setErrorCode(-3);
				result.setErrorInfo("节点名称和设置值为空");
				return result;
			}
			
			for(int i=0;i<params.size();i++){
				Element param = params.get(i);
				HashMap<String,String> paramMap = new HashMap<String,String>();
				String name = StringUtil.getStringValue(param.elementTextTrim("name"));
				String value = StringUtil.getStringValue(param.elementTextTrim("value")); 
				if(StringUtil.IsEmpty(name) || StringUtil.IsEmpty(value)){
					logger.warn(methodName+"["+opId+"]name or value 为空");
					continue;
				}
				paramMap.put("value", value);
				paramMap.put("name", name);
				paramList.add(paramMap);
			}
			
			if(paramList.size() == 0){
				result.setErrorCode(-3);
				this.errMsg ="节点名称和设置值都不可用";
				logger.warn(methodName+"["+opId+"]节点名称和设置值都不可用");
				return result;
			}
			result.setErrorCode(1);
			return result;
		} catch (Exception e) {
			logger.error(methodName+"["+opId+"] Excetion occured!", e);
			result.setErrorCode(-3);
			return result;
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

	public List<HashMap<String, String>> getParamList()
	{
		return paramList;
	}
	
	public void setParamList(List<HashMap<String, String>> paramList)
	{
		this.paramList = paramList;
	}
}
