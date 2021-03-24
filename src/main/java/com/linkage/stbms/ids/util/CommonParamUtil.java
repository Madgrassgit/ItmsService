package com.linkage.stbms.ids.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author zhangshimin(工号) Tel:??
 * @version 1.0
 * @since 2011-4-29 上午09:38:25
 * @category com.linkage.stbms.ids.util
 * @copyright 南京联创科技 网管科技部
 *
 */
public class CommonParamUtil
{
	// 终端厂商标识
	private static final String OUI = "oui";
	// 终端序列号
	private static final String DEV_SN = "dev_sn";


	/**
	 * 解析只有dev_sn和oui的接口入参xml
	 * 
	 * @param
	 * @author zhangsm
	 * @date 2011-04-20
	 * @return Map
	 */
	public static Map<String, String> getCommonInParam(String strXML)
	{
		SAXReader reader = new SAXReader();
		Map<String, String> resultMap = new HashMap<String, String>();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(strXML));
			Element root = document.getRootElement();
			resultMap.put(OUI, root.elementText(OUI));
			resultMap.put(DEV_SN, root.elementText(DEV_SN));
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
		}
		return resultMap;
	}
	public static void main(String[] args) {
		String strXML = "<?xml version='1.0' encoding='gb2312'?><root><oui>00E0FC</oui><dev_sn>00100199006030811033308H3C370289</dev_sn></root>";
		Map<String, String> map = CommonParamUtil.getCommonInParam(strXML);
		System.out.println(map.get("oui"));
	}
	/**
	 * 生成接口回参
	 * 
	 * @param infoMap
	 * @return
	 */
	public static String commonReturnParam(Map<String, String> infoMap)
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GB2312");
		Element root = document.addElement("root");
		if(infoMap != null && !infoMap.isEmpty()){
			for(Object key : infoMap.keySet())
			{
				root.addElement(key.toString()).addText(StringUtil.getStringValue(infoMap.get(key)));
			}
		}
		return document.asXML();
	}
	/**
	 * 生成接口回参
	 * 
	 * @param infoMap
	 * @param infoMap 
	 * @return
	 */
	public static String commonReturnParam(ArrayList<HashMap<String, String>> devList, Map<String, String> infoMap)
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GB2312");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(infoMap.get("CmdID")));
		// 结果代码
		root.addElement("result_flag").addText(StringUtil.getStringValue(infoMap.get("result_flag")));
		// 结果描述
		root.addElement("result").addText(StringUtil.getStringValue(infoMap.get("result")));
		
		Element sheets = root.addElement("Sheets");
		for(HashMap<String,String> devMap : devList)
		{
			Element sheetInfo = sheets.addElement("sheetInfo");
			for(Object keyStr : devMap.keySet())
			{
				sheetInfo.addElement(keyStr.toString()).addText(StringUtil.getStringValue(devMap.get(keyStr)));
			}
		}
		return document.asXML();
	}
}
