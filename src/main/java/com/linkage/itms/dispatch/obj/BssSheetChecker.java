package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * 工单查询接口的XML元素对象
 * @author zhangshimin(工号) Tel:??
 * @version 1.0
 * @since 2011-5-11 下午05:00:51
 * @category com.linkage.itms.dispatch.obj
 * @copyright 南京联创科技 网管科技部
 *
 */
public class BssSheetChecker extends BaseChecker
{
	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(BssSheetChecker.class);
	
	private String userSN = "";
	private String devType = "";
	private String dealDate = "";
	private String serviceType = "";
	private String openStatus = "";
	private String iPTVUserName = "";  //  itv帐号
	private String VlanId = "";
	private String specName = "";
	
	private List<Map<String,String>> sheetInfo = new ArrayList<Map<String,String>>();
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public BssSheetChecker(String inXml) {
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
			if ("jx_dx".equals(Global.G_instArea))
			{
				cityId = param.elementTextTrim("CityId");
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		//参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}
		// 江西电信cityId为必须入力项, 其他电信不是
		if ("jx_dx".equals(Global.G_instArea) && false == cityIdCheck())
		{
			return false;
		}
		
		if ("jx_dx".equals(Global.G_instArea) && 6 == userInfoType){
			if( userInfo.length() < 6){
				result = 1005;
				resultDesc = "设备序列号不合法";
				return false;
			}
			return true;
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
		if ("nx_dx".equals(Global.G_instArea)) {
			document.setXMLEncoding(Global.codeTypeValue);
		} else {
			document.setXMLEncoding("GBK");
		}
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		if (sheetInfo != null && !sheetInfo.isEmpty())
		{
			Element sheets = root.addElement("Sheets");
			sheets.addElement("SN").addText(userSN);
			
			if("xj_dx".equals(Global.G_instArea)){  // 新疆电信  add by zhangchy 2012-01-17
				sheets.addElement("CityName").addText(cityName);
			}else {
				sheets.addElement("CityId").addText(cityId);
			}
			
			if (null != devSn) {
				sheets.addElement("DevSN").addText(devSn);
			}else {
				sheets.addElement("DevSN").addText("");
			}
			
			if (null != devType) {
				sheets.addElement("DevType").addText(devType);
			}else {
				sheets.addElement("DevType").addText("");
			}
			
			// SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口)
			if ("sd_lt".equals(Global.G_instArea)) {
				sheets.addElement("specName").addText(specName);
			}
			
			for(Map<String,String> info : sheetInfo)
			{
				Element sheetInfo = sheets.addElement("sheetInfo");
				if (null != info.get("DealDate")) {
					sheetInfo.addElement("DealDate").addText(
							StringUtil.getStringValue(info, "DealDate"));
				}else {
					sheetInfo.addElement("DealDate").addText("");
				}
				 
				if ("jx_dx".equals(Global.G_instArea)){
					
				}else{
					if (null != info.get("CompleteDate")) {
						sheetInfo.addElement("CompleteDate").addText(
								StringUtil.getStringValue(info, "CompleteDate"));
					}else {
						sheetInfo.addElement("CompleteDate").addText("");
					}
				}
				if (null != info.get("ServiceType")) {
					sheetInfo.addElement("ServiceType").addText(
							StringUtil.getStringValue(info, "ServiceType"));
				}else {
					sheetInfo.addElement("ServiceType").addText("");
				}
				
				if (null != info.get("OpenStatus")) {
					sheetInfo.addElement("OpenStatus").addText(
							StringUtil.getStringValue(info, "OpenStatus"));
				}else {
					sheetInfo.addElement("OpenStatus").addText("");
				}

				if("xj_dx".equals(Global.G_instArea)){  // 新疆电信  add by zhangchy 2012-01-17
					// XJDX-REQ-20130123-HUJG3-001 要求增加协议类型 add by zhangchy 2013-02-19
					if (null != info.get("ServiceType")
							&& !"".equals(info.get("ServiceType"))
							&& "14".equals(info.get("ServiceType"))) {
						if (null != info.get("Protocol") && !"".equals(info.get("Protocol"))) {
							sheetInfo.addElement("Protocol").addText(StringUtil.getStringValue(info, "Protocol")); // 协议类型
						}else {
							sheetInfo.addElement("Protocol").addText(""); // 协议类型
						}
					}
					
					
					//chenxj begin
					if ("10".equals(info.get("ServiceType"))) {
						if ("0".equals(getStringValue(info, "OpenStatus"))) {
							sheetInfo.addElement("OpenResult").addText("等待执行");
						}else{
							sheetInfo.addElement("OpenResult").addText(getStringValue(info, "intServResult"));
						}
						sheetInfo.addElement("KdUserName").addText(getStringValue(info, "KdUserName")); // 宽带帐号
					}					
					if ("11".equals(info.get("ServiceType"))) {
						if ("0".equals(getStringValue(info, "OpenStatus"))) {
							sheetInfo.addElement("OpenResult").addText("等待执行");
						}else{
							sheetInfo.addElement("OpenResult").addText(getStringValue(info, "iptvServResult"));
						}
						sheetInfo.addElement("IPTVUserName").addText(getStringValue(info, "IPTVUserName")); // ITV帐号
					}					
					if ("14".equals(info.get("ServiceType"))) {
						if ("0".equals(getStringValue(info, "OpenStatus"))) {
							sheetInfo.addElement("OpenResult").addText("等待执行");
						}else{
							sheetInfo.addElement("OpenResult").addText(getStringValue(info, "voipServResult"));
						}
						sheetInfo.addElement("VoipUserName").addText(getStringValue(info, "VoipUserName")); // VOIP帐号
					}
					//chenxj end
					
					
				}else if("jx_dx".equals(Global.G_instArea)){

					if (null != info.get("ServiceType")) {
						if (null != info.get("KdUserName") && "10".equals(info.get("ServiceType"))) 
						{
							sheetInfo.addElement("KdUserName").addText(StringUtil.getStringValue(info, "KdUserName"));
							sheetInfo.addElement("KdWanType").addText(StringUtil.getStringValue(info, "KdWanType"));
							sheetInfo.addElement("VlanId").addText(StringUtil.getStringValue(info, "KdVlanId"));
						}
						
						if (null != info.get("IPTVUserName") && "11".equals(info.get("ServiceType"))) {
							sheetInfo.addElement("IPTVUserName").addText(
									StringUtil.getStringValue(info, "IPTVUserName")); // ITV帐号
							sheetInfo.addElement("VlanId").addText(StringUtil.getStringValue(info, "IPTVVlanId"));
						}
						
						if (null != info.get("VoipUserName") && "14".equals(info.get("ServiceType"))) {
							sheetInfo.addElement("VoipUserName").addText(
									StringUtil.getStringValue(info, "VoipUserName")); // VOIP帐号
							sheetInfo.addElement("VlanId").addText(StringUtil.getStringValue(info, "VoipVlanId"));
						}
					}
				
				}
				else {
					if ("10".equals(info.get("ServiceType"))) {
						sheetInfo.addElement("KdUserName").addText(getStringValue(info, "KdUserName")); // 宽带帐号
						// SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口)
						if ("sd_lt".equals(Global.G_instArea)) {
							sheetInfo.addElement("bindPort").addText(getStringValue(info, "bind_port"));
							sheetInfo.addElement("openDate").addText(getStringValue(info, "openDate"));
						}
					}
					
					if ("11".equals(info.get("ServiceType"))) {
						sheetInfo.addElement("IPTVUserName").addText(getStringValue(info, "IPTVUserName")); // ITV帐号
						// SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口)
						if ("sd_lt".equals(Global.G_instArea)) {
							sheetInfo.addElement("bindPort").addText(getStringValue(info, "bind_port"));
							sheetInfo.addElement("openDate").addText(getStringValue(info, "openDate"));
						}
					}
					
					if ("14".equals(info.get("ServiceType"))) {
						sheetInfo.addElement("VoipUserName").addText(getStringValue(info, "VoipUserName")); // VOIP帐号
						// SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口)
						if ("sd_lt".equals(Global.G_instArea)) {
							sheetInfo.addElement("bindPort").addText(getStringValue(info, "bind_port"));
							sheetInfo.addElement("openDate").addText(getStringValue(info, "openDate"));
						}
					}
				}
				
			}
		}
		return document.asXML();
	}

	@SuppressWarnings("rawtypes")
	public static String getStringValue(Map map, String columName) {
		if (null == columName || null == map || null == map.get(columName)) {
			return "";
		}
		return map.get(columName).toString();
	}
	
	public String getUserSN()
	{
		return userSN;
	}

	
	public void setUserSN(String userSN)
	{
		this.userSN = userSN;
	}

	
	public String getDevType()
	{
		return devType;
	}

	
	public void setDevType(String devType)
	{
		this.devType = devType;
	}

	
	public String getDealDate()
	{
		return dealDate;
	}

	
	public void setDealDate(String dealDate)
	{
		this.dealDate = dealDate;
	}

	
	public String getServiceType()
	{
		return serviceType;
	}

	
	public void setServiceType(String serviceType)
	{
		this.serviceType = serviceType;
	}

	
	public String getOpenStatus()
	{
		return openStatus;
	}

	
	public void setOpenStatus(String openStatus)
	{
		this.openStatus = openStatus;
	}

	
	public List<Map<String, String>> getSheetInfo()
	{
		return sheetInfo;
	}

	
	public void setSheetInfo(List<Map<String, String>> sheetInfo)
	{
		this.sheetInfo = sheetInfo;
	}

	public String getIPTVUserName() {
		return iPTVUserName;
	}

	public void setIPTVUserName(String iPTVUserName) {
		this.iPTVUserName = iPTVUserName;
	}

	
	public String getVlanId()
	{
		return VlanId;
	}

	
	public void setVlanId(String vlanId)
	{
		VlanId = vlanId;
	}

	public String getSpecName() {
		return specName;
	}

	public void setSpecName(String specName) {
		this.specName = specName;
	}
}
