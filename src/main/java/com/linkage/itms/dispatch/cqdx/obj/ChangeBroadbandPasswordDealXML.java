package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 重庆电信修改宽带密码接口
 * @author hourui 76958
 * @version 1.0
 * @since 2017年11月19日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ChangeBroadbandPasswordDealXML extends BaseDealXML{

    private static final Logger logger = LoggerFactory.getLogger(ChangeBroadbandPasswordDealXML.class);
    SAXReader reader = new SAXReader();
	
    private String broadband_password = "";
	private String inXML = null;
	private String wan_type = null;
	public ChangeBroadbandPasswordDealXML(String inXML){
		this.inXML = inXML;
	}

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			logicId = inRoot.elementTextTrim("logic_id");
			pppUsename = inRoot.elementTextTrim("ppp_username");
			broadband_password = inRoot.elementTextTrim("broadband_password");
            
			if( StringUtil.IsEmpty(pppUsename)  && StringUtil.IsEmpty(logicId)  )
			{
				this.result ="-99";
				this.errMsg ="宽带账号和逻辑ID同时为空";
				return null;
			}
			
			if( StringUtil.IsEmpty(broadband_password) )
			{
				this.result ="-99";
				this.errMsg ="宽带密码为空";
				return null;
			}
			return inDocument;
		} catch (Exception e) {
			logger.error("ChangeWifiPasswordDealXML.getXML() is error!", e);
			this.result="-99";
			this.errMsg ="入参验证没通过";
			return null;
		}
	}
	
	
	/**
	 * 返回调用结果字符串
	 * 
	 * @author hourui(76958)
	 * @date 2017-11-29
	 * @return boolean 是否校验通过
	 */
	public String returnXML(){
		logger.debug("ReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element response = document.addElement("response");
		// 结果代码
		response.addElement("result_code").addText(StringUtil.getStringValue(result));
		// 结果描述
		response.addElement("result_desc").addText(errMsg);
		return document.asXML();
	}
	
	

	
	public String getpppUsename()
	{
		return pppUsename;
	}

	
	public void setpppUsename(String pppUsename)
	{
		this.pppUsename = pppUsename;
	}

	
	public String getBroadband_password()
	{
		return broadband_password;
	}

	
	public void setBroadband_password(String broadband_password)
	{
		this.broadband_password = broadband_password;
	}

	
	public String getInXML()
	{
		return inXML;
	}

	
	public void setInXML(String inXML)
	{
		this.inXML = inXML;
	}

	
	public String getWan_type()
	{
		return wan_type;
	}

	
	public void setWan_type(String wan_type)
	{
		this.wan_type = wan_type;
	}
	
	public void setResulltCode(int result_code)
	{
		this.result = StringUtil.getStringValue(result_code);
	}
	public void setResultDesc(String result_desc)
	{
		this.errMsg=result_desc;
	}
}
