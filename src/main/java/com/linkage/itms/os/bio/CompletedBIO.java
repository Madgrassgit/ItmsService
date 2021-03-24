package com.linkage.itms.os.bio;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.os.dao.UserBindDAO;

/**
 * 
 * @author zhangshimin(工号) Tel:??
 * @version 1.0
 * @since 2011-6-25 上午10:45:16
 * @category com.linkage.itms.os.bio
 * @copyright 南京联创科技 网管科技部
 *
 */
public class CompletedBIO
{
	private static Logger logger = LoggerFactory.getLogger(CompletedBIO.class);
	private int result = 0;
	private String desc = "成功";
	private String orderId = "";
	private String loid = "";
	public String completedInfo(String strXML)
	{
		logger.warn("ITMS报竣-服开参数：" + strXML);
		Map<String,String> paramMap = this.readXML(strXML);
		Map<String, String>  devMapInfo = new HashMap<String, String>();
		UserBindDAO dao = new UserBindDAO();
		if(paramMap.isEmpty())
		{
			result = 1;
			desc = "请检查入参是否为空："+strXML;
			logger.warn("请检查入参是否为空："+strXML);
		}else
		{
			orderId = paramMap.get("OrderID");
			loid = paramMap.get("Loid");
			if(StringUtil.IsEmpty(orderId))
			{
				result = 1;
				desc = "入参中的订单ID为空";
				logger.warn("入参中的订单ID为空");
			}
			else if(StringUtil.IsEmpty(loid))
			{
				result = 1;
				desc = "入参中的LOID为空";
				logger.warn("入参中的LOID为空");
			}
			else
			{
				//根据订单ID查询相关联的设备信息
				devMapInfo = dao.getUserBind(loid);
			}
			
		}
		//拼装回参
		String strRes = returnXML(devMapInfo);
		logger.warn("ITMS报竣回单：" + strRes);
		return strRes;
	}
	private String returnXML(Map<String, String> devMapInfo)
	{
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("root");
		root.addElement("CmdID").addText("123456789012345");
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		root.addElement("RstMsg").addText(desc);
		Element param = root.addElement("Param");
		param.addElement("Loid").addText(loid);
		param.addElement("OrderID").addText(orderId);
		if(result == 1 || devMapInfo.isEmpty())
		{
			param.addElement("OUI").addText("");
			param.addElement("DevSN").addText("");
			param.addElement("DevType").addText("");
			param.addElement("DevName").addText("");
			param.addElement("VendorName").addText("");
			param.addElement("DevModel").addText("");
			param.addElement("MAC").addText("");
		}
		else
		{
			param.addElement("OUI").addText(devMapInfo.get("oui"));
			param.addElement("DevSN").addText(devMapInfo.get("device_serialnumber")==null ? "" : devMapInfo.get("device_serialnumber"));
			String deviceType = devMapInfo.get("rela_dev_type_id");
			if(deviceType.equals("1"))
			{
				param.addElement("DevType").addText("e8-b");
			}
			else if(deviceType.equals("2"))
			{
				param.addElement("DevType").addText("e8-b");
			}
			else
			{
				param.addElement("DevType").addText("");
			}
			param.addElement("DevName").addText(devMapInfo.get("device_name")==null ? "" : devMapInfo.get("device_name"));
			param.addElement("VendorName").addText(devMapInfo.get("vendor_name")==null ? "" : devMapInfo.get("vendor_name"));
			param.addElement("DevModel").addText(devMapInfo.get("device_model")==null ? "" : devMapInfo.get("device_model"));
			param.addElement("MAC").addText(devMapInfo.get("cpe_mac")==null ? "" : devMapInfo.get("cpe_mac"));
		}
		return document.asXML();
	}
	private Map<String,String> readXML(String strXML)
	{
		Map<String,String> reMap = new HashMap<String, String>();
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(strXML));
			Element root = document.getRootElement();
			reMap.put("OrderID", root.elementText("OrderID"));
			reMap.put("Loid", root.elementText("Loid"));
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return  reMap;
	}
}
