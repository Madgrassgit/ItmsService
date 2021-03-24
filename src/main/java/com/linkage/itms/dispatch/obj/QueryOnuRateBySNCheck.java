package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2018年11月15日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryOnuRateBySNCheck extends BaseChecker
{
	private static final Logger logger = LoggerFactory.getLogger(QueryOnuRateBySNCheck.class);
	
	private String gbbroadband ="";

	private String devType ="";

	private String isNormal ="";

	private String isTianyi2Up = "";

	public QueryOnuRateBySNCheck(String inXml){
		callXml = inXml;
	}
	
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		ip = "";
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userName = param.elementTextTrim("UserName");
			//cityId = param.elementTextTrim("CityId");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		//参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()) {
			return false;
		}
		
		/*if(!cityIdCheck()){
			return false;
		}*/
		result = 0;
		resultDesc = "成功";
		return true;
	}

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
		root.addElement("RstCode").addText(String.valueOf(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
	
		Element param = root.addElement("Param");
		param.addElement("GBBROADBAND").addText(gbbroadband);
		param.addElement("DEV_TYPE").addText(devType);
		param.addElement("Is_normal").addText(isNormal);
		param.addElement("Is_tianyi2up").addText(isTianyi2Up);
		return document.asXML();
	}
	
	public boolean userInfoTypeCheck(){
		if(userInfoType !=1 && userInfoType != 2){
			result = 1001;
			resultDesc = "查询类型非法";
			return false;
		}
		return true;
	}
	
	public String getGbbroadband()
	{
		return gbbroadband;
	}

	
	public void setGbbroadband(String gbbroadband)
	{
		this.gbbroadband = gbbroadband;
	}

	public String getDevType() {
		return devType;
	}

	public void setDevType(String devType) {
		this.devType = devType;
	}

	public String getIsNormal() {
		return isNormal;
	}

	public void setIsNormal(String isNormal) {
		this.isNormal = isNormal;
	}

	public String getIsTianyi2Up() {
		return isTianyi2Up;
	}

	public void setIsTianyi2Up(String isTianyi2Up) {
		this.isTianyi2Up = isTianyi2Up;
	}
}
