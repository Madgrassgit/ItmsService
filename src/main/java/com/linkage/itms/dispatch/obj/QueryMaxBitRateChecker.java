
package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

/**
 * 江西电信ITMS+家庭网关对外接口-2.31新增lan1口协商速率查询接口
 * @author songxq
 * @version 1.0
 * @since 2020-6-17
 */
public class QueryMaxBitRateChecker extends BaseChecker
{

	public QueryMaxBitRateChecker(String inXml)
	{
		this.callXml = inXml;
	}

	private static Logger logger = LoggerFactory
			.getLogger(QueryMaxBitRateChecker.class);

	private String lanPortNum = "";

	private String rstState = "";

	private String maxBitRate = "";

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
			// 入参解析
			userInfo = param.elementTextTrim("UserInfo");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		if(2 != userInfoType && 1 != userInfoType){
			result = 2;
			resultDesc = "用户信息类型非法";
			return false;
		}
		
		if (StringUtil.IsEmpty(userInfo))
		{
			result = 1;
			resultDesc = "用户信息不能为空";
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
		root.addElement("RstMsg").addText(resultDesc);

		Element lanPort = root.addElement("LanPort");

		lanPort.addElement("LanPortNUM").addText(lanPortNum);

		lanPort.addElement("RstState").addText(rstState);

		lanPort.addElement("MaxBitRate").addText(maxBitRate);
		return document.asXML();
	}

	public String getLanPortNum() {
		return lanPortNum;
	}

	public void setLanPortNum(String lanPortNum) {
		this.lanPortNum = lanPortNum;
	}

	public String getRstState() {
		return rstState;
	}

	public void setRstState(String rstState) {
		this.rstState = rstState;
	}

	public String getMaxBitRate() {
		return maxBitRate;
	}

	public void setMaxBitRate(String maxBitRate) {
		this.maxBitRate = maxBitRate;
	}
}
