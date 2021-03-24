package com.linkage.itms.dispatch.sxlt.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 甘肃电信修改宽带密码接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月10日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ServiceChangeDealXML extends BaseDealXML{

    private static final Logger logger = LoggerFactory.getLogger(ServiceChangeDealXML.class);
    SAXReader reader = new SAXReader();
	
    private String newPassWord = "";
    private String orderType = "";
	private String inXML = null;
	private String wan_type = null;
	public ServiceChangeDealXML(String methodName){
		super(methodName);
	}

	public Document getXML(String inXml) {
		this.inXml = inXml;
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			pppUsename = inRoot.elementTextTrim("adAcount");
			newPassWord = inRoot.elementTextTrim("newPassWord");
			
			orderType = inRoot.elementTextTrim("orderType");
			if("wband-X".equals(orderType)){
				newPassWord = newPassWord.substring("wband_password=".length(),newPassWord.length());
			}
			else if("iptv-X".equals(orderType)){
				newPassWord = newPassWord.substring("iptv_password=".length(),newPassWord.length());
			}
			else{
				this.result ="-99";
				this.errMsg ="orderType格式非法";
				logger.warn(methodName+"["+opId+"]orderType格式非法");
				return null;
			}
			
			opId = inRoot.elementTextTrim("LSHNo");
            
			if( StringUtil.IsEmpty(pppUsename))
			{
				this.result ="-99";
				this.errMsg ="用户账号为空";
				logger.warn(methodName+"["+opId+"]用户账号为空");
				return null;
			}
			
			if( StringUtil.IsEmpty(newPassWord) )
			{
				this.result ="-99";
				this.errMsg ="新密码为空";
				logger.warn(methodName+"["+opId+"]新密码为空");
				return null;
			}
			return inDocument;
		} catch (Exception e) {
			logger.error(methodName+"["+opId+"] Excetion occured!", e);
			this.result="-99";
			this.errMsg ="入参验证没通过";
			return null;
		}
	}
	public static void main(String[] args)
	{
		String newPassWord = "wband_password=111112";
		newPassWord = newPassWord.substring("iptv_password=".length(),newPassWord.length());
		System.out.println(newPassWord);
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

	
	public String getNewPassWord()
	{
		return newPassWord;
	}

	
	public void setNewPassWord(String newPassWord)
	{
		this.newPassWord = newPassWord;
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

	
	public String getOrderType()
	{
		return orderType;
	}

	
	public void setOrderType(String orderType)
	{
		this.orderType = orderType;
	}
}
