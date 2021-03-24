package com.linkage.itms.nmg.dispatch.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * 设备重启接口的XML元素对象
 * @author zhangsm(工号) Tel:??
 * @version 1.0
 * @since 2012-2-25 下午02:59:10
 * @category com.linkage.itms.dispatch.obj
 * @copyright 南京联创科技 网管科技部
 *
 */
public class BizServicePortChecker extends NmgBaseChecker
{
	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(BizServicePortChecker.class);
	
	private List<LinkedHashMap<String,String>> lanList = new ArrayList<LinkedHashMap<String,String>>();
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public BizServicePortChecker(String inXml) {
		callXml = inXml;
	}
	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}

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
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		
		Element PortTable = root.addElement("PortTable");
		if(lanList != null && lanList.size() > 0){
			HashMap<String,String> tmp = null;
			for(int i = 0; i < lanList.size(); i++){
				tmp = lanList.get(i);
				String PortNUM = StringUtil.getStringValue(tmp.get("PortNUM"));
				String PortType = StringUtil.getStringValue(tmp.get("PortType"));
				String RstState = StringUtil.getStringValue(tmp.get("RstState"));
				String ServiceAccount = StringUtil.getStringValue(tmp.get("ServiceAccount"));
				Element lanPortNums = PortTable.addElement("Port").addAttribute("num", (i+1)+"");
				lanPortNums.addElement("PortNUM").addText(PortNUM);
				lanPortNums.addElement("PortType").addText(PortType);
				lanPortNums.addElement("RstState").addText(RstState);
				lanPortNums.addElement("ServiceAccount").addText(ServiceAccount);
			}
		}

		return document.asXML();
	}
	
	public List<LinkedHashMap<String, String>> getLanList() {
		return lanList;
	}

	public void setLanList(List<LinkedHashMap<String, String>> lanList) {
		this.lanList = lanList;
	}
}
