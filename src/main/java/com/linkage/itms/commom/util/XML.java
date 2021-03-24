/**
 * 
 */
package com.linkage.itms.commom.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * XML
 * 
 * @author Eric(qixq@)
 * @version 1.0
 * @since 1.0
 * @date 2011-5-31
 */
public class XML {
	/** log */
	private static Logger logger = LoggerFactory.getLogger(XML.class.getName());

	/**Document*/
	private Document doc;

	/**root Element*/
	private Element rootElement;

	/**
	 * Constractor
	 * @param file -String 
	 *        the path and name of xml file.
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public XML(String file) {
		SAXBuilder saxBuilder = new SAXBuilder();

		try {
			
			doc = saxBuilder.build(new File(file));

		} catch (JDOMException e) {
			logger.error("JDOMException:\n{}", e);
		}catch(IOException e) {
			logger.error("IOException:\n{}", e);
		}

		if (null != doc) {
			rootElement = doc.getRootElement();
		}
	}

	/**
	 * get Element list.
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Element> getElements(String name) {

		Element root = doc.getRootElement();
		if (null == root) {
			return null;
		}
		List<Element> list = new ArrayList<Element>();
		String[] splitName = name.split("\\.");
		Element element = root.getChild(splitName[0]);
		if (element == null)
		{
			// 如果第一层节点不存在，则返回null
			return null;
		}
		if(1==splitName.length){
			list.add(element);
		}else{
			List<Element> tempList = new ArrayList<Element>();
			list.add(element);
			for(int i=1;i<splitName.length;i++){
				tempList.clear();
				tempList.addAll(list);
				list.clear();
				for(Element o:tempList){
					list.addAll(o.getChildren(splitName[i]));
				}
			}
		}
		return list;
	}

	/**
	 * get Element.
	 * @param name
	 * @return
	 */
	public Element getElement(String name) {
		if (null == rootElement) {
			return null;
		}

		List<Element> list = this.getElements(name);

		if (null == list || list.isEmpty()) {
			return null;
		} else {
			return (Element) list.get(0);
		}
	}

	/**
	 * get string value of element.
	 * @param name
	 * @return
	 */
	public ArrayList<String> getListStringValue(String name) {
		List<Element> elementList = this.getElements(name);
		ArrayList<String> list = null;
		if (null == elementList) {
			return list;
		} else {
			list = new ArrayList<String>();
			for(Element o:elementList){
				list.add(o.getTextNormalize());
			}
			return list;
		}
	}
	
	/**
	 * get string value of element.
	 * @param name
	 * @return
	 */
	public String getStringValue(String name) {
		Element element = this.getElement(name);

		if (null == element) {
			return null;
		} else {
			return element.getTextNormalize();
		}
	}

	/**
	 * get int value of element.
	 * @param name
	 * @return
	 */
	public int getIntValue(String name) {

		int value = 0;
		String strTmp = this.getStringValue(name);

		if (null == strTmp) {
			return value;
		} else {
			try {
				value = Integer.parseInt(strTmp);
			} catch (NumberFormatException e) {
			}
			strTmp = null;

			return value;
		}
	}

	/**
	 * get long value of element.
	 * @param name
	 * @return
	 */
	public long getLongValue(String name) {

		long value = 0;
		String strTmp = this.getStringValue(name);

		if (null == strTmp) {
			return value;
		} else {
			try {
				value = Long.parseLong(strTmp);
			} catch (NumberFormatException e) {
			}
			strTmp = null;

			return value;
		}
	}

	/**
	 * get double value of element.
	 * @param name
	 * @return
	 */
	public double getDoubleValue(String name) {

		double value = 0.00;
		String strTmp = this.getStringValue(name);

		if (null == strTmp) {
			return value;
		} else {
			try {
				value = Double.parseDouble(strTmp);
			} catch (NumberFormatException e) {
			}
			strTmp = null;

			return value;
		}
	}

	/**
	 * get boolean value of element.
	 * @param name
	 * @return
	 */
	public boolean getBooleanValue(String name) {
		boolean value = false;
		String strTmp = this.getStringValue(name);

		if (null != strTmp && (strTmp.equals("1") || strTmp.equals("true"))) {
			value = true;
		}

		strTmp = null;

		return value;
	}

	/**
	 * is debug model.
	 * @return
	 */
	public boolean isDebug() {

		return this.getBooleanValue("DEBUG");
	}

}
