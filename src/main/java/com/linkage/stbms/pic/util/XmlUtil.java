package com.linkage.stbms.pic.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.commons.xml.XML2Bean;
import com.linkage.stbms.pic.object.ParameterListObj;
import com.linkage.stbms.pic.object.LogoObj;
import com.linkage.stbms.pic.object.STBZreoObj;
import com.linkage.stbms.pic.object.StbSoftObj;

public class XmlUtil {

	Logger logger = LoggerFactory.getLogger(XmlUtil.class);
	
	private Document document = null;
	
	private String deviceId = null;
	
	private StbSoftObj stbSoftObj = null;
	
	private STBZreoObj stbZreoObj = null;
	
	/**
	 * Constructor
	 * 
	 * @param inputXml
	 */
	public XmlUtil(File nodeXml) {
		SAXReader saxReader = new SAXReader();
		try {
			saxReader.setEncoding("UTF-8");
			document = saxReader.read(nodeXml);
		} catch (DocumentException e) {
			logger.warn("加载XML错误！");
			e.printStackTrace();
		}
	}
	
	/**
	 * 字符串参数
	 * @param nodeXml
	 */
	public XmlUtil(String nodeXml) {
		SAXReader saxReader = new SAXReader();
		try {
			saxReader.setEncoding("UTF-8");
			document = DocumentHelper.parseText(nodeXml);
		} catch (DocumentException e) {
			logger.warn("加载XML错误！");
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据路径取得XML值，如路径不唯一，则只取第一个
	 * 
	 * @date 2009-4-23
	 * @param path
	 * @return value
	 */
	@SuppressWarnings("unchecked")
	public String getXmlValue(String path) {

		String value = null;

		List list = document.selectNodes(path);

		Iterator<Element> it = list.iterator();

		// 如名字有相同，则只取第一个
		if (it.hasNext()) {
			Element element = it.next();
			value = element.getText();
		}

		return value;
	}
	
	
	/**
	 * 根据路径取得XML值，如路径不唯一，取全部
	 * 
	 * @date 2009-4-23
	 * @param path
	 * @return value
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAllXmlValue(String path) {

		List<String> valueList = new ArrayList<String>();
		
		String value = null;

		List list = document.selectNodes(path);

		Iterator<Element> it = list.iterator();

		while (it.hasNext()) {
			Element element = it.next();
			value = element.getText();
			
			valueList.add(value);
		}

		return valueList;
	}
	
	/**
	 * 取得属性的List
	 * @author gongsj
	 * @date 2010-5-18
	 * @param path
	 * @param attributeName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAllXmlAttribute(String path, String attributeName) {

		List<String> attributeList = new ArrayList<String>();
		
		String attribute = null;

		List list = document.selectNodes(path);

		Iterator<Element> it = list.iterator();

		// 如名字有相同，则只取第一个
		while (it.hasNext()) {
			Element element = it.next();
			attribute = element.attributeValue(attributeName);
			
			attributeList.add(attribute);
		}

		return attributeList;
	}
	
	/**
	 * 根据路径取得XML属性，如路径不唯一，则取第一个
	 * @author gongsj
	 * @date 2009-7-21
	 * @param path
	 * @param attributeName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getXmlAttribute(String path, String attributeName) {

		String attribute = null;

		List list = document.selectNodes(path);

		Iterator<Element> it = list.iterator();

		// 如名字有相同，则只取第一个
		if (it.hasNext()) {
			Element element = it.next();
			attribute = element.attributeValue(attributeName);
		}

		return attribute;
	}
	
	/**
	 * 机顶盒软件升级
	 * @author gongsj
	 * @date 2010-11-17
	 */
	public void generateStbSoftFromXml() {
		
		String versionPath = getXmlValue("//STBSOFT/VersionPath");
		
		stbSoftObj = new StbSoftObj();
		stbSoftObj.setVersionPath(versionPath);
		
		this.setStbSoftObj(stbSoftObj);
	}

	/**
	 * 零配置下发机顶盒
	 */
	public void generateZeroConfigFromXml()
	{
		String authURL = getXmlValue("//ZeroConfig/AuthURL");
		
		stbZreoObj = new STBZreoObj();
		stbZreoObj.setAuthURL(authURL);
	
	}
	
	public void generateStbAccountFromXml_JX()
	{
		String addressingType = getXmlValue("//STB/AddressingType");
		stbZreoObj = new STBZreoObj();
		stbZreoObj.setAddressingType(addressingType);
	
		String servAccount = getXmlValue("//STB/ServAccount");
		String servPassword = getXmlValue("//STB/ServPassword");
		
		String PPPoEID = getXmlValue("//STB/PPPoEID");
		String PPPoEPassword = getXmlValue("//STB/PPPoEPassword");
		
		stbZreoObj.setServAccount(servAccount);
		stbZreoObj.setServPassword(servPassword);
	
		stbZreoObj.setPPPoEID(PPPoEID);
		stbZreoObj.setPPPoEPassword(PPPoEPassword);
		
		String address = getXmlValue("//STB/IPAddress");
		String subnetMask = getXmlValue("//STB/SubnetMask");
		String defaultGateway = getXmlValue("//STB/DefaultGateway");
		String servers = getXmlValue("//STB/DNSServers");
		
		stbZreoObj.setIPAddress(address);
		stbZreoObj.setSubnetMask(subnetMask);
		stbZreoObj.setDefaultGateway(defaultGateway);
		stbZreoObj.setDNSServers(servers);
	}
	
	public ParameterListObj generateBatchConfigFromXml(String sheetParamXml)
	{
		XML2Bean x2b = new XML2Bean(sheetParamXml);
		ParameterListObj parameterListObj = (ParameterListObj) x2b.getBean("STB",
				ParameterListObj.class);
		return parameterListObj;
	}
	
	public LogoObj generateLogoFromXml() {
		
		String isSetStartPicURL = getXmlAttribute("//STB/StartPicURL", "flag");
		logger.warn("//STB/StartPicURL========={}",isSetStartPicURL);
		String isSetBootPicURL = getXmlAttribute("//STB/BootPicURL", "flag");
		logger.warn("//STB/BootPicURL========={}",isSetBootPicURL);
		String isSetAuthenticatePicURL = getXmlAttribute("//STB/AuthenticatePicURL", "flag");
		logger.warn("//STB/AuthenticatePicURL========={}",isSetAuthenticatePicURL);
		LogoObj logoObj = new LogoObj();
		logoObj.setIsSetStartPicURL(StringUtil.getIntegerValue(isSetStartPicURL));
		logoObj.setIsSetBootPicURL(StringUtil.getIntegerValue(isSetBootPicURL));
		logoObj.setIsSetAuthenticatePicURL(StringUtil.getIntegerValue(isSetAuthenticatePicURL));
		
		String startPicURL = getXmlValue("//STB/StartPicURL");
		String bootPicURL = getXmlValue("//STB/BootPicURL");
		String authenticatePicURL = getXmlValue("//STB/AuthenticatePicURL");
		
		logoObj.setStartPicURL(startPicURL);
		logoObj.setBootPicURL(bootPicURL);
		logoObj.setAuthenticatePicURL(authenticatePicURL);
		return logoObj;
	}
	
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public StbSoftObj getStbSoftObj() {
		return stbSoftObj;
	}

	public void setStbSoftObj(StbSoftObj stbSoftObj) {
		this.stbSoftObj = stbSoftObj;
	}

	public STBZreoObj getStbZreoObj()
	{
		return stbZreoObj;
	}

	
	public void setStbZreoObj(STBZreoObj stbZreoObj)
	{
		this.stbZreoObj = stbZreoObj;
	}
}
































