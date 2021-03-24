
package com.linkage.stbms.ioperation.bio;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.ioperation.dao.IOperationDAO;

/**
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-6-4
 * @category com.linkage.stbms.ioperation.bio
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class IOperationBIO
{

	private static final Logger logger = LoggerFactory.getLogger(IOperationBIO.class);
	/**
	 * 机顶盒MAC地址合法
	 */
	private static final String MAC_VALID = "1";
	/**
	 * 机顶盒MAC地址不合法
	 */
	private static final String MAC_INVALID = "0";
	private IOperationDAO dao = new IOperationDAO();

	public String stbIsValid(String xmlParam)
	{
		logger.warn("stbIsValid({})", xmlParam);
		String mac = getMacFromXml(xmlParam);
		logger.warn("get mac form xml parameter is[{}]", mac);
		if (StringUtil.IsEmpty(mac))
		{
			return returnXml(MAC_INVALID);
		}
		// 接口查询条件兼容处理，mac地址本地使用全大写，没有冒号,中英文都需要替换
		mac = mac.replaceAll(":", "").replaceAll("：", "").toUpperCase();
		boolean isExist = dao.isMacExist(mac);
		logger.warn("mac is exist in table[tab_devmac_init], result is[{}]", isExist);
		return returnXml(isExist ? MAC_VALID : MAC_INVALID);
	}

	private String getMacFromXml(String xmlParam)
	{
		if (StringUtil.IsEmpty(xmlParam))
		{
			return null;
		}
		try
		{
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(new StringReader(xmlParam));
			Element root = document.getRootElement();
			Element macElement = root.element("mac");
			return macElement == null ? null : macElement.getTextTrim();
		}
		catch (DocumentException e)
		{
			logger.warn("get mac from xml parameter[" + xmlParam + "] error.");
			logger.warn(e.getMessage(), e);
			return null;
		}
	}

	private String returnXml(String isValid)
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		Element root = document.addElement("root");
		root.addElement("isValid").addText(isValid);
		String result = document.asXML();
		logger.warn("the result xml of mac is valid is [{}]", result);
		return result;
	}
}
