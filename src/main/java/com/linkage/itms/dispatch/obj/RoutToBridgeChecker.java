
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
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2015年10月19日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RoutToBridgeChecker extends BaseChecker
{

	public static final Logger logger = LoggerFactory
			.getLogger(RoutToBridgeChecker.class);
	// 逻辑SN
	private String userSn = null;
	// 失败原因
	private String failureReason = null;
	// 成功状态
	private String succStatus = null;
	// 逻辑Id
	private String userLoid = null;
	
	private String offlineEnable = null;
	
	private String broadbandPassword;

	/**
	 * 构造函数 入参 inXml XML格式
	 * 
	 * @param inXml
	 */
	public RoutToBridgeChecker(String inXml)
	{
		callXml = inXml;
	}

	/**
	 * 检查调用接口入参的合法性
	 */
	@Override
	public boolean check()
	{
		logger.debug("RoutToBridgeChecker>check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		userSn = "";
		failureReason = "";
		succStatus = "";
		try
		{
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			if("js_dx".equals(Global.G_instArea))
			{
				userInfo = param.elementTextTrim("UserName");
				userLoid = param.elementTextTrim("UserLOID");
			} else {
				userInfo = param.elementTextTrim("UserInfo");
			}
			if("cq_dx".equals(Global.G_instArea)){
				offlineEnable = StringUtil.getStringValue(param.elementTextTrim("OfflineEnable"));
				broadbandPassword = StringUtil.getStringValue(param.elementTextTrim("BroadbandPassword"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck())
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	/**
	 * 组装XML字符串，并返回
	 */
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
		// 逻辑SN
		root.addElement("SN").addText(userSn);
		// 失败原因
		root.addElement("FailureReason").addText(failureReason);
		// 成功状态
		root.addElement("SuccStatus").addText(succStatus);
		
		return document.asXML();
	}

	
	public String getUserSn()
	{
		return userSn;
	}

	
	public void setUserSn(String userSn)
	{
		this.userSn = userSn;
	}

	
	public String getFailureReason()
	{
		return failureReason;
	}

	
	public void setFailureReason(String failureReason)
	{
		this.failureReason = failureReason;
	}

	
	public String getSuccStatus()
	{
		return succStatus;
	}

	
	public void setSuccStatus(String succStatus)
	{
		this.succStatus = succStatus;
	}

	
	public String getUserLoid()
	{
		return userLoid;
	}

	
	public void setUserLoid(String userLoid)
	{
		this.userLoid = userLoid;
	}

	public String getOfflineEnable() {
		return offlineEnable;
	}

	public void setOfflineEnable(String offlineEnable) {
		this.offlineEnable = offlineEnable;
	}

	public String getBroadbandPassword() {
		return broadbandPassword;
	}

	public void setBroadbandPassword(String broadbandPassword) {
		this.broadbandPassword = broadbandPassword;
	}
}
