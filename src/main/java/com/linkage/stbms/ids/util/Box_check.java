package com.linkage.stbms.ids.util;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.ids.obj.BoxCheckObj;
import com.linkage.stbms.itv.main.Global;


/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-3-20
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class Box_check
{
	private static Logger logger = LoggerFactory.getLogger(Box_check.class);

	private static final String Param = "param";
	private static final String UserInfo  = "userinfo ";
	private static final String DevSN = "devSn";
	private static final String MAC = "Mac";
	private static final String VENDOR = "Vendor";
	private static final String DEVMODEL = "DevModel";
	private static final String HARDWAREVERSION = "HardwareVersion";
	private static final String SOFTWAREVERSION = "SoftwareVersion";
	
	/**
	 * 获取入参userinfo
	 */
	public static String getUserInfot(String strXML)
	{
		return readXml(strXML).get(UserInfo);
	}

	/**
	 * 解析接口入参xml
	 */
	private static Map<String, String> readXml(String strXML)
	{
		SAXReader reader = new SAXReader();
		Document document = null;
		Map<String, String> resultMap = new HashMap<String, String>();
		try
		{
			document = reader.read(new StringReader(strXML));
			Element root = document.getRootElement();
			Element param = root.element("Param");
			logger.warn("UserInfo : {}",param.elementTextTrim("UserInfo"));
			resultMap.put(UserInfo,param.elementTextTrim("UserInfo"));
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
		}
		
		return resultMap;
	}

	/**
	 * 回参xml
	 */
	public static String boxXML(List<BoxCheckObj> objList, int resultFlag,String resultDesc)
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		root.addElement("RstCode").addText(""+resultFlag);
		root.addElement("RstMsg").addText(resultDesc);
		
		if(objList != null && !objList.isEmpty()){
			for(BoxCheckObj obj : objList)
			{
				Element dev = root.addElement(Param);
				dev.addElement(DevSN).addText(StringUtil.getStringValue(obj.getDevSn()));
				
				if("xj_dx".equals(Global.G_instArea))
				{
					dev.addElement(MAC).addText(StringUtil.getStringValue(obj.getMac()));
					dev.addElement(VENDOR).addText(StringUtil.getStringValue(obj.getVendor()));
					dev.addElement(DEVMODEL).addText(StringUtil.getStringValue(obj.getDevModel()));
					dev.addElement(HARDWAREVERSION).addText(StringUtil.getStringValue(obj.getHardwareVersion()));
					dev.addElement(SOFTWAREVERSION).addText(StringUtil.getStringValue(obj.getSoftwareVersion()));
				}
			}
		}
		
		return document.asXML();
	}
	
	
}
