package com.linkage.itms.nmg.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 家庭网关ITV业务稽核接口
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-11-20
 */
public class QueryItvSheetDataChecker extends NmgBaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(QueryItvSheetDataChecker.class);
	// 宽带账号
	private String bandNo = "";
	// iptv账号
	private String iptvNo = "";
	// 组播VLAN应配值
	private String zbVlan = "";
	// 组播VLAN实配值
	private String zbVlanReal = "";
	// IPTV 业务 VLAN应配值
	private String iptvVlan = "";
	// IPTV 业务 VLAN实配值
	private String iptvVlanReal = "";

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public QueryItvSheetDataChecker(String inXml)
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
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserName");
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

//	private boolean bussinessTypeCheck()
//	{
//		if (StringUtil.IsEmpty(bussinessType))
//		{
//			result = 1004;
//			resultDesc = "稽核业务类型不合法";
//			return false;
//		}
//		if (!"1".equals(bussinessType))
//		{
//			result = 1005;
//			resultDesc = "暂不支持宽带业务以外的稽核业务";
//			return false;
//		}
//		return true;
//	}

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
		if (0 == result){
			root.addElement("bandNo").addText(bandNo);
			root.addElement("iptvNo").addText(iptvNo);
			root.addElement("loid").addText(loid);
			root.addElement("zbVlan").addText(zbVlan);
			root.addElement("zbVlanReal").addText(zbVlanReal);
			root.addElement("iptvVlan").addText(iptvVlan);
			root.addElement("iptvVlanReal").addText(iptvVlanReal);
		}

		return document.asXML();
	}

	public String getBandNo() {
		return bandNo;
	}

	public void setBandNo(String bandNo) {
		this.bandNo = bandNo;
	}

	public String getIptvNo() {
		return iptvNo;
	}

	public void setIptvNo(String iptvNo) {
		this.iptvNo = iptvNo;
	}

	public String getZbVlan() {
		return zbVlan;
	}

	public void setZbVlan(String zbVlan) {
		this.zbVlan = zbVlan;
	}

	public String getZbVlanReal() {
		return zbVlanReal;
	}

	public void setZbVlanReal(String zbVlanReal) {
		this.zbVlanReal = zbVlanReal;
	}

	public String getIptvVlan() {
		return iptvVlan;
	}

	public void setIptvVlan(String iptvVlan) {
		this.iptvVlan = iptvVlan;
	}

	public String getIptvVlanReal() {
		return iptvVlanReal;
	}

	public void setIptvVlanReal(String iptvVlanReal) {
		this.iptvVlanReal = iptvVlanReal;
	}

}
