package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author xiangzl (Ailk No.)
 * @version 1.0
 * @since 2013-12-10
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DecayServInfoCheck extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(DecayServInfoCheck.class);

	public DecayServInfoCheck()
	{
	}

	public DecayServInfoCheck(String inXml)
	{
		callXml = inXml;
	}
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
//			cmdId = root.elementTextTrim("CmdID");
//			cmdType = root.elementTextTrim("CmdType");
//			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			loid = param.elementTextTrim("loid");
			gwType = param.elementTextTrim("gwType");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	@Override
	public String getReturnXml()
	{
		return null;
	}
	public String getReturnXml(String desc, Map<String, String> deviceInfo, ArrayList<HashMap<String, String>> servInfo, Map<String, String> decayMap)
	{
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("info");
		root.addElement("CmdID").addText(StringUtil.getStringValue(Math.round(Math.random() * 1000000000000L)));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + desc);
		// 
		double	tx_power=0;
		double	rx_power=0;
		/*if(decayMap != null)
		{
			tx_power=StringUtil.getDoubleValue(decayMap.get("tx_power"));
			rx_power=StringUtil.getDoubleValue(decayMap.get("rx_power"));
			
			if(tx_power>30){
				double temp_tx_power= (Math.log(tx_power/10000) /Math.log(10))*10;
				tx_power=(int) temp_tx_power;
				if(tx_power%10 >=5){
					tx_power=(tx_power/10+1)*10;
				}
				else{
					tx_power=tx_power/10*10;
				}
			}
			if(rx_power>30){
				double temp_rx_power= (Math.log(rx_power/10000) /Math.log(10))*10;
				rx_power=(int) temp_rx_power;
				if(rx_power%10 >=5){
					rx_power=(rx_power/10+1)*10;
				}
				else{
					rx_power=rx_power/10*10;
				}
			}
		}*/
			
		root.addElement("rLightFade").addText(String.valueOf(rx_power));
		root.addElement("sLightFade").addText(String.valueOf(tx_power));
		Element sheet = root.addElement("Sheets");
		sheet.addElement("SN").addText(loid);
		sheet.addElement("CityId").addText(StringUtil.getStringValue(deviceInfo, "city_id", ""));
		sheet.addElement("DevSN").addText(StringUtil.getStringValue(deviceInfo, "device_serialnumber", ""));
		sheet.addElement("DevType").addText(StringUtil.getStringValue(deviceInfo, "device_type", ""));
		if (null != servInfo && servInfo.size() >0)
		{
			for (HashMap<String, String> map : servInfo)
			{
				Element sheetInfo = root.addElement("sheetInfo");
				sheetInfo.addElement("DealDate").addText(
						StringUtil.getStringValue(map, "dealdate", ""));
				sheetInfo.addElement("ServiceType").addText(
						StringUtil.getStringValue(map, "serv_type_id", ""));
				sheetInfo.addElement("OpenStatus").addText(
						StringUtil.getStringValue(map, "open_status", ""));
				if ("10".equals(StringUtil.getStringValue(map, "serv_type_id", "")))
				{
					sheetInfo.addElement("KdUserName").addText(
							StringUtil.getStringValue(map, "username", ""));
				}
				if ("11".equals(StringUtil.getStringValue(map, "serv_type_id", "")))
				{
					sheetInfo.addElement("IPTVUserName").addText(
							StringUtil.getStringValue(map, "username", ""));
				}
				if ("14".equals(StringUtil.getStringValue(map, "serv_type_id", "")))
				{
					sheetInfo.addElement("VoipUserName").addText(
							StringUtil.getStringValue(map, "username", ""));
				}
			}
		}
		logger.warn(document.asXML());
		return document.asXML();
	}
}
