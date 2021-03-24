/**
 * QueryDevConfigInfoServiceChecker.java 终端信息查询接口 检查类
 */

package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 终端信息查询接口 检查类
 * 
 * @author chenzj5
 * @date 2015-06-24
 */
public class QueryDevConfigInfoServiceChecker extends BaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory
			.getLogger(QueryDevConfigInfoServiceChecker.class);
	private DevConfigInfoObj obj = new DevConfigInfoObj();

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public QueryDevConfigInfoServiceChecker(String inXml)
	{
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
		try
		{
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
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
	 * 返回绑定调用结果字符串
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
		Element wlan = root.addElement("WLAN");
		wlan.addElement("SSIDname").addText(StringUtil.getStringValue(obj.getsSIDname()));
		wlan.addElement("DevNumber").addText(StringUtil.getStringValue(obj.getDevNumber()));
		Element internet = root.addElement("Internet");
		internet.addElement("connectType").addText(StringUtil.getStringValue(obj.getConnectType()));
		internet.addElement("connectStatus").addText(StringUtil.getStringValue(obj.getConnectStatus()));
		internet.addElement("IPAddress").addText(StringUtil.getStringValue(obj.getiPAddress()));
		internet.addElement("DNSServer").addText(StringUtil.getStringValue(obj.getdNSServer()));
		internet.addElement("vlan").addText(StringUtil.getStringValue(obj.getVlan()));
		internet.addElement("bindPort").addText(StringUtil.getStringValue(obj.getBindPort()));
		internet.addElement("PPPoE").addText(StringUtil.getStringValue(obj.getpPPoE()));
		internet.addElement("LanName").addText(StringUtil.getStringValue(obj.getLanName()));
		internet.addElement("LinkRate").addText(StringUtil.getStringValue(obj.getLinkRate()));
		internet.addElement("LinkStats").addText(StringUtil.getStringValue(obj.getLinkStats()));
		Element ponInfo = root.addElement("PonInfo");
		ponInfo.addElement("PonStat").addText(StringUtil.getStringValue(obj.getPonStat()));
		ponInfo.addElement("TXPower").addText(StringUtil.getStringValue(obj.gettXPower()));
		ponInfo.addElement("RXPower").addText(StringUtil.getStringValue(obj.getrXPower()));
		return document.asXML();
	}

	public DevConfigInfoObj getObj()
	{
		return obj;
	}

	public void setObj(DevConfigInfoObj obj)
	{
		this.obj = obj;
	}

	public static void main(String[] args)
	{
		QueryDevConfigInfoServiceChecker qc = new QueryDevConfigInfoServiceChecker("");
		DevConfigInfoObj obj = new DevConfigInfoObj();
		System.out.println(qc.getReturnXml());
	}
}