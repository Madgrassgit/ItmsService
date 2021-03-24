package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-11
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ChangVlanParamChecker4NX extends BaseChecker
{
	private static final Logger logger = LoggerFactory.getLogger(ChangVlanParamChecker4NX.class);

	private String inParam = null;
	private String internetVlanID = "";
	private String iptvVlanID = "";
	public ChangVlanParamChecker4NX(String inParam){
		this.inParam = inParam;
	}
	
	@Override
	public boolean check()
	{

		
		logger.debug("ChangVlanParamChecker4NX==>check()"+inParam);
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			
			userInfo = param.elementTextTrim("UserInfo"); 
			internetVlanID = param.elementTextTrim("Internet_VlanID");
			iptvVlanID = param.elementTextTrim("Iptv_VlanID");
			
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			if (userInfoType != 1) {
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
			
			logger.warn(userInfo);
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		if (StringUtil.IsEmpty(userInfo)) {
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
		
		if (!"81".equals(internetVlanID)) {
			result = 1000;
			resultDesc = "internetVlanID应为81";
			return false;
		}
		
		if (!"43".equals(iptvVlanID)) {
			result = 1000;
			resultDesc = "iptvVlanID应为43";
			return false;
		}
		
		// 参数合法性检查
		if (false == baseCheck())
		{
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
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
//		Element Param = root.addElement("Param");
//		Param.addElement("Loid").addText(loid);
		return document.asXML();
	}

	public String getInternetVlanID() {
		return internetVlanID;
	}

	public void setInternetVlanID(String internetVlanID) {
		this.internetVlanID = internetVlanID;
	}

	public String getIptvVlanID() {
		return iptvVlanID;
	}

	public void setIptvVlanID(String iptvVlanID) {
		this.iptvVlanID = iptvVlanID;
	}
	
}
