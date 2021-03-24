package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * 
 * @author guxl3 (Ailk No.)
 * @version 1.0
 * @since 2021年2月5日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class HqosQueryDevSnChecker extends BaseChecker
{

	private static Logger logger = LoggerFactory.getLogger(HqosQueryDevSnChecker.class);
	private String mac;

	public HqosQueryDevSnChecker(String inXml)
	{
		callXml = inXml;
	}

	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		SAXReader reader = new SAXReader();
		try
		{
			reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
		}
		catch (SAXException e)
		{
			logger.error("HqosQueryDevSnChecker.check error:", e);
		}
		Document document = null;
		try
		{
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("cmdId");
			authUser = root.elementTextTrim("authUser");
			authPwd = root.elementTextTrim("authPwd");
			Element param = root.element("param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("userType"));
			userInfo = param.elementTextTrim("userInfo");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (!baseCheck())
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	@Override
	public boolean baseCheck(){
		
		if(StringUtil.IsEmpty(cmdId)){
			result = 1001;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		
		if(Global.USERTYPENAME != userInfoType && Global.USERTYPELOID != userInfoType
				&& Global.USERTYPEDEVSN != userInfoType ){
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
		
		if(StringUtil.IsEmpty(userInfo)){
			result = 1001;
			resultDesc = "用户信息非法";
			return false;
		}
		return true;
	}
	
	
	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		Element root = document.addElement("root");
		// 结果代码
		root.addElement("resultCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("resultDes").addText(resultDesc);

		root.addElement("loid").addText(StringUtil.getStringValue(loid));

		root.addElement("mac").addText(StringUtil.getStringValue(mac));

		root.addElement("devsn").addText(StringUtil.getStringValue(devSn));
		return document.asXML();
	}

	public String getMac()
	{
		return mac;
	}

	
	public void setMac(String mac)
	{
		this.mac = mac;
	}
	
}
