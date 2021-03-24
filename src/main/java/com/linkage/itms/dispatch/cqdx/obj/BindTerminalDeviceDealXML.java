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
 * bind方法接口的XML元素对象
 * 
 * @author hourui(76958)
 * @date 2010-6-17
 */
public class BindTerminalDeviceDealXML extends BaseDealXML {

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(BindTerminalDeviceDealXML.class);

	private String callXml;
	//用户帐号
	private String ppp_username;
	//逻辑ID
	private String logic_id;
	//设备序列号
	private String serial_number;

	private String product_class;
	//OUI
	private String oui;
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public BindTerminalDeviceDealXML(String inXml) {
		callXml = inXml;
	}

	/**
	 * 检查接口调用字符串的合法性
	 */
	public boolean check() {
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			ppp_username=root.elementTextTrim("ppp_username");
			logic_id=root.elementTextTrim("logic_id");
			serial_number=root.elementTextTrim("serial_number");
			product_class=root.elementTextTrim("product_class");
			oui=root.elementTextTrim("oui");
			
			if( StringUtil.IsEmpty(ppp_username)  && StringUtil.IsEmpty(logic_id)  )
			{
				this.result ="500100";
				this.errMsg ="宽带账号和逻辑ID同时为空";
				return false;
			}
			if( StringUtil.IsEmpty(serial_number) )
			{
				this.result ="500102";
				this.errMsg ="设备序列号为空";
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			result = "-99";
			errMsg = "数据格式错误";
			return false;
		}

		return true;
	}


	/**
	 * 返回绑定调用结果字符串
	 */
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("GBK");		
		Element root = document.addElement("response");
		// 结果代码
		root.addElement("result_code").addText(result);
		// 结果描述
		root.addElement("result_desc").addText(errMsg);

		return document.asXML();
	}



	
	public String getCallXml()
	{
		return callXml;
	}

	
	public void setCallXml(String callXml)
	{
		this.callXml = callXml;
	}

	
	public String getPpp_username()
	{
		return ppp_username;
	}

	
	public void setPpp_username(String ppp_username)
	{
		this.ppp_username = ppp_username;
	}

	
	public String getLogic_id()
	{
		return logic_id;
	}

	
	public void setLogic_id(String logic_id)
	{
		this.logic_id = logic_id;
	}

	
	public String getSerial_number()
	{
		return serial_number;
	}

	
	public void setSerial_number(String serial_number)
	{
		this.serial_number = serial_number;
	}

	
	public String getProduct_class()
	{
		return product_class;
	}

	
	public void setProduct_class(String product_class)
	{
		this.product_class = product_class;
	}

	
	public String getOui()
	{
		return oui;
	}

	
	public void setOui(String oui)
	{
		this.oui = oui;
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
