package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
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
 * 
 * @author zhangshimin(工号) Tel:78
 * @version 1.0
 * @since 2011-5-16 下午03:44:23
 * @category com.linkage.itms.dispatch.obj
 * @copyright 南京联创科技 网管科技部
 *
 */
public class DevConfigChecker extends BaseChecker
{
	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(BindInfoChecker.class);
	
	private Map<String,List<HashMap<String,String>>> configInfo = new HashMap<String, List<HashMap<String,String>>>();
	private Map<String,String> resultMap = new HashMap<String, String>();
	
	private String LAN1 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1";
	private String LAN2 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2";
	private String LAN3 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.3";
	private String LAN4 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.4";
	
	private String WLAN1 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1";
	private String WLAN2 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.2";
	private String WLAN3 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.3";
	private String WLAN4 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.4";
	
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public DevConfigChecker(String inXml) {
		callXml = inXml;
	}
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
		
		//参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		
		return true;
	}
	
	
	
	/**
	 * 按需求单 JSDX_ITMS-REQ-20120220-LUHJ-006 要求，回参格式有较大变化
	 * 
	 * 顾将此方法重新改写，原方法注释了   add by zhangchy 2012-03-07
	 * 
	 */
	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		
		List<HashMap<String, String>> confInfoList = null;
		List<HashMap<String, String>> voiceServiceList = null;
		List<HashMap<String, String>> linesList = null;
		List<HashMap<String, String>> IGmpSnoopingList = null;
		
		
		Map<String, String> bindPortMap = new HashMap<String, String>();
		
		bindPortMap.put(LAN1, "LAN1");
		bindPortMap.put(LAN2, "LAN2");
		bindPortMap.put(LAN3, "LAN3");
		bindPortMap.put(LAN4, "LAN4");
		
		bindPortMap.put(WLAN1, "WLAN1");
		bindPortMap.put(WLAN2, "WLAN2");
		bindPortMap.put(WLAN3, "WLAN3");
		bindPortMap.put(WLAN4, "WLAN4");
		
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
		
		if(result == 0)
		{
			root.addElement("AccessType").addText(resultMap.get("AccessType"));  // 接入方式
			
// 			------Internet------begin-----------------------------------------------------------------
//			Element internet = root.addElement("Internet");
//			
//			confInfoList = configInfo.get("inter_list");
//			
//			if (confInfoList == null || confInfoList.isEmpty()) {
//				confInfoList = new ArrayList<HashMap<String,String>>();
//			}
//			
//			// 如果查询到数据 ，则取相关数据
//			if (confInfoList.size() > 0) {
//				
//				Map<String,String> internetInfo = confInfoList.get(0);  // 只取一条数据
//				
//				Element wanType = internet.addElement("WanType");
//				// 上网方式
//				if ("1".equals(internetInfo.get("sess_type"))) {  //  PPP
//					if ("PPPoE_Bridged".equals(internetInfo.get("conn_type"))) {
//						wanType.addText("1");  // 桥接  
//					} else if ("IP_Routed".equals(internetInfo.get("conn_type"))) {
//						wanType.addText("2");  // 路由
//					}
//				} else if("2".equals(internetInfo.get("sess_type"))) {
//					if ("Static".equals(internetInfo.get("ip_type"))) {
//						wanType.addText("3");  // 静态IP
//					} else if ("DHCP".equals(internetInfo.get("ip_type"))) {
//						wanType.addText("4");  // DHCP
//					}
//				}
//				
//				
//				Element kdPvcOrVlanId = internet.addElement("KdPvcOrVlanId");
//				if("1".equals(resultMap.get("AccessType"))) {
//					kdPvcOrVlanId.addText(internetInfo.get("vpi_id")+ "/" + internetInfo.get("vci_id"));
//				} else {
//					if(null != internetInfo.get("vlan_id") && false ==internetInfo.get("vlan_id").isEmpty() ) {
//						kdPvcOrVlanId.addText(internetInfo.get("vlan_id"));
//					} else {
//						kdPvcOrVlanId.addText("");
//					}
//				}
//			// 如果没查询到数据 ，节点内容显示空
//			} else {
//				Element wanType = internet.addElement("WanType");
//				wanType.addText("");  // 上网方式
//				
//				Element kdPvcOrVlanId = internet.addElement("KdPvcOrVlanId");
//				kdPvcOrVlanId.addText("");
//			}
//----------------------------------------------------------------------------------------------------------------------------			

			
			
			confInfoList = configInfo.get("inter_list");
			
			if (confInfoList == null || confInfoList.isEmpty()) {
				confInfoList = new ArrayList<HashMap<String,String>>();
			}
			
			// 如果查询到数据 ，则取相关数据
			if (confInfoList.size() > 0) {
				
				for(Map<String,String> internetInfo : confInfoList){
					
					Element internet = root.addElement("Internet");
					
					Element wanType = internet.addElement("WanType");
					// 上网方式
					if ("1".equals(internetInfo.get("sess_type"))) {  //  PPP
						if ("PPPoE_Bridged".equals(internetInfo.get("conn_type"))) {
							wanType.addText("1");  // 桥接  
						} else if ("IP_Routed".equals(internetInfo.get("conn_type"))) {
							wanType.addText("2");  // 路由
						}
					} else if("2".equals(internetInfo.get("sess_type"))) {
						if ("Static".equals(internetInfo.get("ip_type"))) {
							wanType.addText("3");  // 静态IP
						} else if ("DHCP".equals(internetInfo.get("ip_type"))) {
							wanType.addText("4");  // DHCP
						}
					}
					
					
					Element kdPvcOrVlanId = internet.addElement("KdPvcOrVlanId");
					if("1".equals(resultMap.get("AccessType"))) {
						kdPvcOrVlanId.addText(internetInfo.get("vpi_id")+ "/" + internetInfo.get("vci_id"));
					} else {
						if(null != internetInfo.get("vlan_id") && false ==internetInfo.get("vlan_id").isEmpty() ) {
							kdPvcOrVlanId.addText(internetInfo.get("vlan_id"));
						} else {
							kdPvcOrVlanId.addText("");
						}
					}
				}
				
				
			// 如果没查询到数据 ，节点内容显示空
			} else {
				Element internet = root.addElement("Internet");
				
				Element wanType = internet.addElement("WanType");
				wanType.addText("");  // 上网方式
				
				Element kdPvcOrVlanId = internet.addElement("KdPvcOrVlanId");
				kdPvcOrVlanId.addText("");
			}
			
			
// 			------Internet------end---------------------------------------------------------------------------
			
			
			
//			------IPTV------begin-------------------------------------------------------------------------------
			Element iptv = root.addElement("IPTV");
			
			confInfoList = configInfo.get("iptv_list");
			IGmpSnoopingList = configInfo.get("IGmpSnooping_list");
			
			if (confInfoList == null || confInfoList.isEmpty()) {
				confInfoList = new ArrayList<HashMap<String,String>>();
			}
			
			// 如果查询到数据 ，则取相关数据
			if (confInfoList.size() > 0) {  
				
				Map<String,String> iptvInfo = confInfoList.get(0);  // 只取一条数据
				Element iPTVPvcOrVlanId = iptv.addElement("IPTVPvcOrVlanId");
				if("1".equals(resultMap.get("AccessType"))) {
					iPTVPvcOrVlanId.addText(iptvInfo.get("vpi_id")+ "/" + iptvInfo.get("vci_id"));
				} else {
					if(null != iptvInfo.get("vlan_id") && false ==iptvInfo.get("vlan_id").isEmpty() ) {
						iPTVPvcOrVlanId.addText(iptvInfo.get("vlan_id"));
					} else {
						iPTVPvcOrVlanId.addText("");
					}
				}
				
				
				Element bindPort = iptv.addElement("BindPort");  // 端口 
				if (null == iptvInfo.get("bind_port") || iptvInfo.get("bind_port").isEmpty()) {
					bindPort.addText("");
				} else {
					
					String [] bindPortArr = iptvInfo.get("bind_port").split(",");
					StringBuffer bindPortStr = new StringBuffer();
					
					for (int i = 0; i < bindPortArr.length; i++) {
						if(0 != i){
							bindPortStr.append(",");
						}
						if(bindPortArr[i].endsWith(".")){
							bindPortStr.append(bindPortMap.get(bindPortArr[i].subSequence(0, bindPortArr[i].length()-1)));
						} else {
							bindPortStr.append(bindPortMap.get(bindPortArr[i]));
						}
					}
					
					bindPort.addText(bindPortStr.toString());
				}
			// 如果没查询到数据，节点类容为空
			} else {
				Element iPTVPvcOrVlanId = iptv.addElement("IPTVPvcOrVlanId");
				iPTVPvcOrVlanId.addText("");
				Element bindPort = iptv.addElement("BindPort");  // 端口 
				bindPort.addText("");
			}
			
			if (IGmpSnoopingList == null || IGmpSnoopingList.isEmpty()) {
				IGmpSnoopingList = new ArrayList<HashMap<String,String>>();
			}
			
			if (IGmpSnoopingList.size() > 0) {
				
				Map<String, String> IGMPSnooping = IGmpSnoopingList.get(0);
				Element iGMPSnooping = iptv.addElement("IGMPSnooping");  
				
				if (null != IGMPSnooping.get("snooping_enable")
						&& false == IGMPSnooping.get("snooping_enable").isEmpty()
						&& "1".equals(IGMPSnooping.get("snooping_enable"))) {
					iGMPSnooping.addText("1");  // 启用
				} else {
					iGMPSnooping.addText("0");  // 未启用
				}
			} else {
				Element iGMPSnooping = iptv.addElement("IGMPSnooping");  
				iGMPSnooping.addText("");  // 未启用
			}
			
			
// 			------IPTV------end-------------------------------------------------------------
			
			
			
			
// 			-------VOIP ---begin-------------------------------------------------------
			Element voip = root.addElement("VOIP");
			
			confInfoList = configInfo.get("voip_list");
			
			if (confInfoList == null || confInfoList.isEmpty())
			{
				confInfoList = new ArrayList<HashMap<String,String>>();
			}
			
			// 如果查询到数据 ，则取相关数据
			if (confInfoList.size() > 0) {
				
				Map<String,String> voipInfo = confInfoList.get(0);  // 只取一条数据 PVC
				Element vOIPPvcOrVlanId = voip.addElement("VOIPPvcOrVlanId");
				if("1".equals(resultMap.get("AccessType"))) {
					vOIPPvcOrVlanId.addText(voipInfo.get("vpi_id")+ "/" + voipInfo.get("vci_id"));
				} else {
					if(null != voipInfo.get("vlan_id") && false ==voipInfo.get("vlan_id").isEmpty() ) {
						vOIPPvcOrVlanId.addText(voipInfo.get("vlan_id"));
					} else {
						vOIPPvcOrVlanId.addText("");
					}
				}
			
			// 如果没查询到数据 ，则节点内容显示空
			} else {
				Element vOIPPvcOrVlanId = voip.addElement("VOIPPvcOrVlanId");
				vOIPPvcOrVlanId.addText("");
			}
			
			linesList = configInfo.get("lines_list");  // line 线路信息
			Element vOIPType = voip.addElement("VOIPType");  // VOIP语音协议
			boolean flag = false;
			if (linesList != null && false == linesList.isEmpty()) {
//				如果存在多条线路信息，只要其中某一条线路信息的认证帐号是以"+86"开头的就是IMS协议，否则是SIP协议
				for (int i = 0; i < linesList.size(); i++) {
					
					Map<String,String> linesInfo = linesList.get(i);
					
					if (null != linesInfo.get("username")
							&& false == linesInfo.get("username").isEmpty()
							&& linesInfo.get("username").startsWith("+86")) {
						flag = true;
					}
				}
				
				if (flag) {
					vOIPType.addText("2");  // ims协议
				} else {
					vOIPType.addText("1");  // sip协议
				}
			// 如果没查询到数据，节点内容显示空
			} else {
				vOIPType.addText("");
			}

//			---------VoiceService----------------------
			
			Element voiceService = voip.addElement("VoiceService");
			
			voiceServiceList = configInfo.get("voiceService");  // voiceService
			if (voiceServiceList != null && false == voiceServiceList.isEmpty()) {
				
				Map<String, String> voiceServiceMap = voiceServiceList.get(0);  // 只取一条数据
				
				Element proxyServer = voiceService.addElement("ProxyServer");
				proxyServer.addText(StringUtil.getStringValue(voiceServiceMap.get("prox_serv")));  // 主地址
				
				Element proxyServerPort = voiceService.addElement("ProxyServerPort");
				proxyServerPort.addText(StringUtil.getStringValue(voiceServiceMap.get("prox_port")));  // 主端口
				
				Element standByProxyServer = voiceService.addElement("StandByProxyServer");
				standByProxyServer.addText(StringUtil.getStringValue(voiceServiceMap.get("prox_serv_2"))); // 备地址
				
				Element standByProxyServerPort = voiceService.addElement("StandByProxyServerPort");
				standByProxyServerPort.addText(StringUtil.getStringValue(voiceServiceMap.get("prox_port_2"))); // 备端口
				
				Element registrarServer = voiceService.addElement("RegistrarServer");
				registrarServer.addText(StringUtil.getStringValue(voiceServiceMap.get("regi_serv")));  // 注册地址
				
				Element registrarServerPort = voiceService.addElement("RegistrarServerPort");
				registrarServerPort.addText(StringUtil.getStringValue(voiceServiceMap.get("regi_port")));   // 注册端口
				
				Element standByRegistrarServer = voiceService.addElement("StandByRegistrarServer");
				standByRegistrarServer.addText(StringUtil.getStringValue(voiceServiceMap.get("stand_regi_serv"))); // 标准注册地址
				
				Element standByRegistrarServerPort = voiceService.addElement("StandByRegistrarServerPort");
				standByRegistrarServerPort.addText(StringUtil.getStringValue(voiceServiceMap.get("stand_regi_port"))); // 标准注册端口
				
				Element outboundProxy = voiceService.addElement("OutboundProxy");
				outboundProxy.addText(StringUtil.getStringValue(voiceServiceMap.get("out_bound_proxy")));  // 外部绑定地址
				
				Element outboundProxyPort = voiceService.addElement("OutboundProxyPort");
				outboundProxyPort.addText(StringUtil.getStringValue(voiceServiceMap.get("out_bound_port")));  // 外部绑定端口
				
				Element standByOutboundProxy = voiceService.addElement("StandByOutboundProxy");
				standByOutboundProxy.addText(StringUtil.getStringValue(voiceServiceMap.get("stand_out_bound_proxy")));  // 标准绑定地址
				
				Element standByOutboundProxyPort = voiceService.addElement("StandByOutboundProxyPort");
				standByOutboundProxyPort.addText(StringUtil.getStringValue(voiceServiceMap.get("stand_out_bound_port"))); // 标准绑定端口
			// 如果没查询到数据，节点内容显示为空
			} else {
				Element proxyServer = voiceService.addElement("ProxyServer");
				proxyServer.addText("");  // 主地址
				
				Element proxyServerPort = voiceService.addElement("ProxyServerPort");
				proxyServerPort.addText("");  // 主端口
				
				Element standByProxyServer = voiceService.addElement("StandByProxyServer");
				standByProxyServer.addText(""); // 备地址
				
				Element standByProxyServerPort = voiceService.addElement("StandByProxyServerPort");
				standByProxyServerPort.addText(""); // 备端口
				
				Element registrarServer = voiceService.addElement("RegistrarServer");
				registrarServer.addText("");  // 注册地址
				
				Element registrarServerPort = voiceService.addElement("RegistrarServerPort");
				registrarServerPort.addText("");   // 注册端口
				
				Element standByRegistrarServer = voiceService.addElement("StandByRegistrarServer");
				standByRegistrarServer.addText(""); // 标准注册地址
				
				Element standByRegistrarServerPort = voiceService.addElement("StandByRegistrarServerPort");
				standByRegistrarServerPort.addText(""); // 标准注册端口
				
				Element outboundProxy = voiceService.addElement("OutboundProxy");
				outboundProxy.addText("");  // 外部绑定地址
				
				Element outboundProxyPort = voiceService.addElement("OutboundProxyPort");
				outboundProxyPort.addText("");  // 外部绑定端口
				
				Element standByOutboundProxy = voiceService.addElement("StandByOutboundProxy");
				standByOutboundProxy.addText("");  // 标准绑定地址
				
				Element standByOutboundProxyPort = voiceService.addElement("StandByOutboundProxyPort");
				standByOutboundProxyPort.addText(""); // 标准绑定端口
			}
//			------------Line线路信息----------------------------------
			Element lines = voip.addElement("Lines");
			
			if (linesList != null && false == linesList.isEmpty()) {
				for (int i = 0; i < linesList.size(); i++) {
					Map<String,String> linesInfo = linesList.get(i);
					Element line = lines.addElement("Line");
					line.addAttribute("num", StringUtil.getStringValue(linesInfo.get("line_id")));
					Element enable = line.addElement("Enable");
					if ("Enabled".equals(linesInfo.get("enable")) || "1".equals(linesInfo.get("enable"))) {
						enable.addText("1");
					} else {
						enable.addText("0");
					}
					Element status = line.addElement("status");
					if (null == linesInfo.get("status") || linesInfo.get("status").isEmpty()) {
						status.addText("0");  // 未注册
					} else if("Up".equals(StringUtil.getStringValue(linesInfo.get("status")))) {
						status.addText("1"); // 已注册
					} else {
						status.addText("0"); // 未注册
					}
					
					Element authUserName = line.addElement("AuthUserName");
					authUserName.addText(StringUtil.getStringValue(linesInfo.get("username")));
					Element authPassword = line.addElement("AuthPassword");
					authPassword.addText(StringUtil.getStringValue(linesInfo.get("password")));
				}
			} else {
				lines.addText("");
			}
// 			------ VOIP ---end---------------------------------------------------------------------------------------------------------------
		}
		return document.asXML();
	}
	
	

//	@Override
//	public String getReturnXml()
//	{
//		logger.debug("getReturnXml()");
//		List<HashMap<String,String>> confInfoList = null;
//		Document document = DocumentHelper.createDocument();
//		document.setXMLEncoding("GBK");
//		Element root = document.addElement("root");
//		// 接口调用唯一ID
//		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
//		// 结果代码
//		root.addElement("RstCode").addText("" + result);
//		// 结果描述
//		root.addElement("RstMsg").addText("" + resultDesc);
//		if(result == 0)
//		{
//			//上网配置信息
//			Element internet = root.addElement("Internet");
//			internet.addElement("result").addText(resultMap.get("inter_result").toString());
//			confInfoList = configInfo.get("inter_list");
//			if (confInfoList == null || confInfoList.isEmpty())
//			{
//				confInfoList = new ArrayList<HashMap<String,String>>();
//			}
//			for(Map<String,String> confInfo : confInfoList)
//			{
//				Element accessType = internet.addElement("AccessType");
//				accessType.addAttribute("type", resultMap.get("AccessType"));
//				logger.warn(resultMap.get("AccessType"));
//				if(resultMap.get("AccessType").equals("DSL"))
//				{
//					accessType.addText(confInfo.get("vpi_id")+ "/" + confInfo.get("vci_id"));
//				}
//				else
//				{
//					if(confInfo.get("vlan_id")!=null)
//					{
//						accessType.addText(confInfo.get("vlan_id"));
//					}
//					else
//					{
//						accessType.addText("");
//					}
//				}
//				
//			}
//			//IPTV
//			Element iptv = root.addElement("IPTV");
//			iptv.addElement("result").addText(resultMap.get("iptv_result").toString());
//			confInfoList = configInfo.get("iptv_list");
//			if (confInfoList == null || confInfoList.isEmpty())
//			{
//				confInfoList = new ArrayList<HashMap<String,String>>();
//			}
//			for(Map<String,String> confInfo : confInfoList)
//			{
//				Element accessType = iptv.addElement("AccessType");
//				accessType.addAttribute("type", resultMap.get("AccessType"));
//				if(resultMap.get("AccessType").equals("DSL"))
//				{
//					accessType.addText(confInfo.get("vpi_id")+ "/" + confInfo.get("vci_id"));
//				}
//				else
//				{
//					accessType.addText(confInfo.get("vlan_id"));
//				}
//			}
//			//VOIP
//			Element voip = root.addElement("VOIP");
//			voip.addElement("result").addText(resultMap.get("voip_result").toString());
//			confInfoList = configInfo.get("voip_list");
//			if (confInfoList == null || confInfoList.isEmpty())
//			{
//				confInfoList = new ArrayList<HashMap<String,String>>();
//			}
//			for(Map<String,String> confInfo : confInfoList)
//			{
//				Element accessType = voip.addElement("AccessType");
//				accessType.addAttribute("type", resultMap.get("AccessType"));
//				if(resultMap.get("AccessType").equals("DSL"))
//				{
//					accessType.addText(confInfo.get("vpi_id")+ "/" + confInfo.get("vci_id"));
//				}
//				else
//				{
//					accessType.addText(confInfo.get("vlan_id"));
//				}
//			}
//		}
//		return document.asXML();
//	}
	
	public Map<String, List<HashMap<String, String>>> getConfigInfo()
	{
		return configInfo;
	}
	
	public void setConfigInfo(Map<String, List<HashMap<String, String>>> configInfo)
	{
		this.configInfo = configInfo;
	}
	
	public Map<String, String> getResultMap()
	{
		return resultMap;
	}
	
	public void setResultMap(Map<String, String> resultMap)
	{
		this.resultMap = resultMap;
	}
	
}
