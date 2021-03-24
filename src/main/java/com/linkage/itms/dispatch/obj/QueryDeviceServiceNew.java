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

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-9-4
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryDeviceServiceNew extends BaseChecker
{
	// 日志记录对象
		private static Logger logger = LoggerFactory.getLogger(QueryDeviceServiceNew.class);
		// 注册时间:YYYY-MM-DD hh:mm:ss, 样例：2016-03-11 12:33:00
		private String regTime = "";
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
		public QueryDeviceServiceNew(String inXml)
		{
			callXml=inXml;
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

	@Override
	public String getReturnXml()
	{
		List<HashMap<String, String>> confInfoList = null;
		List<HashMap<String, String>> linesList = null;
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
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		
		if(result == 0)
		{
			root.addElement("AccessType").addText(StringUtil.getStringValue(resultMap.get("AccessType")));  // 接入方式
			root.addElement("regTime").addText(StringUtil.getStringValue(regTime));
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
					Map<String,String> test = confInfoList.get(0);  // 只取一条数据
					Element bindPort = internet.addElement("BindPort");  // 端口 
					if (null == test.get("bind_port") || test.get("bind_port").isEmpty()) {
						bindPort.addText("");
					} else {
						
						String [] bindPortArr = test.get("bind_port").split(",");
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
						
						bindPort.addText(StringUtil.getStringValue(bindPortStr.toString()));
					}
					
					Element kdPvcOrVlanId = internet.addElement("KdPvcOrVlanId");
					if("1".equals(resultMap.get("AccessType"))) {
						kdPvcOrVlanId.addText(internetInfo.get("vpi_id")+ "/" + internetInfo.get("vci_id"));
					} else {
						if(null != internetInfo.get("vlan_id") && false ==internetInfo.get("vlan_id").isEmpty() ) {
							kdPvcOrVlanId.addText(StringUtil.getStringValue(internetInfo.get("vlan_id")));
						} else {
							kdPvcOrVlanId.addText("");
						}
					}
					//没有写好处
					Element status = internet.addElement("status");
					status.addText(StringUtil.getStringValue(test.get("conn_status")));
				}
			}else {
				Element internet = root.addElement("Internet");
				Element wanType = internet.addElement("WanType");
				wanType.addText("");  // 上网方式
				Element BindPort = internet.addElement("BindPort");
				wanType.addText(""); 
				Element kdPvcOrVlanId = internet.addElement("KdPvcOrVlanId");
				kdPvcOrVlanId.addText("");
				Element status = internet.addElement("status");
				wanType.addText(""); 
			}
		//iptv===========================
			Element iptv = root.addElement("IPTV");
			
			confInfoList = configInfo.get("iptv_list");
			if (confInfoList == null || confInfoList.isEmpty()) {
				confInfoList = new ArrayList<HashMap<String,String>>();
			}
			
			// 如果查询到数据 ，则取相关数据
			if (confInfoList.size() > 0) {  
				
			for(Map<String,String> internetInfo : confInfoList){
					Element wanType = iptv.addElement("WanType");
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
				Map<String,String> iptvInfo = confInfoList.get(0);  // 只取一条数据
				Element iPTVPvcOrVlanId = iptv.addElement("IPTVPvcOrVlanId");
				if("1".equals(resultMap.get("AccessType"))) {
					iPTVPvcOrVlanId.addText(iptvInfo.get("vpi_id")+ "/" + iptvInfo.get("vci_id"));
				} else {
					if(null != iptvInfo.get("vlan_id") && false ==iptvInfo.get("vlan_id").isEmpty() ) {
						iPTVPvcOrVlanId.addText(StringUtil.getStringValue(iptvInfo.get("vlan_id")));
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
					
					bindPort.addText(StringUtil.getStringValue(bindPortStr.toString()));
				}
				//===========没写好
				
				Element multicastVlan = iptv.addElement("MulticastVlan");
				multicastVlan.addText(StringUtil.getStringValue(iptvInfo.get("multicast_vlan")));
				Element status = iptv.addElement("status"); 
				status.addText(StringUtil.getStringValue(iptvInfo.get("conn_status")));
			}
			// 如果没查询到数据，节点类容为空
			} else {
				Element WanType = iptv.addElement("WanType");
				WanType.addText("");
				Element iPTVPvcOrVlanId = iptv.addElement("IPTVPvcOrVlanId");
				iPTVPvcOrVlanId.addText("");
				Element bindPort = iptv.addElement("BindPort");  // 端口 
				bindPort.addText("");
				Element MulticastVlan = iptv.addElement("MulticastVlan");
				MulticastVlan.addText("");
				Element status = iptv.addElement("status");
				status.addText("");
			}
			
			//-==========================voip
			Element voip = root.addElement("VOIP");
			
			confInfoList = configInfo.get("voip_list");
			
			if (confInfoList == null || confInfoList.isEmpty())
			{
				confInfoList = new ArrayList<HashMap<String,String>>();
			}
			// 如果查询到数据 ，则取相关数据
			if (confInfoList.size() > 0) {
				for(Map<String,String> internetInfo : confInfoList){
					Element wanType = voip.addElement("WanType");
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
				
				//===========没有写好处
				Map<String,String> conn_status = confInfoList.get(0); 
				Map<String,String> statu_list = confInfoList.get(0);
				Element status = voip.addElement("status");
				status.addText(StringUtil.getStringValue(statu_list.get(0)));
				
				
				linesList = configInfo.get("lines_list");  // line 线路信息
				Element vOIPType = voip.addElement("VOIPType");  // VOIP语音协议
				boolean flag = false;
				if (linesList != null && false == linesList.isEmpty()) {
//								如果存在多条线路信息，只要其中某一条线路信息的认证帐号是以"+86"开头的就是IMS协议，否则是SIP协议
					for (int i = 0; i < linesList.size(); i++) {
						
						Map<String,String> linesInfo = linesList.get(i);
						
						if (null != linesInfo.get("username")
								&& false == linesInfo.get("username").isEmpty()
								&& linesInfo.get("username").startsWith("+86")){
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
				confInfoList = configInfo.get("registStatus_List");
				Map<String,String> regist = confInfoList.get(0);
				//没有写好处
				Element registStatus = voip.addElement("registStatus");
				registStatus.addText(StringUtil.getStringValue(regist.get(0)));
			// 如果没查询到数据 ，则节点内容显示空
			}}else {
				Element WanType = voip.addElement("WanType");
				WanType.addText("");
				Element VoipVlanId = voip.addElement("VoipVlanId");
				VoipVlanId.addText("");
				Element status = voip.addElement("status");  // 端口 
				status.addText("");
				Element VoipType = voip.addElement("VoipType");
				VoipType.addText("");
				Element registStatus = voip.addElement("registStatus");
				registStatus.addText("");
			} 
		}
		return document.asXML();
	}
	
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
	
	public String getRegTime()
	{
		return regTime;
	}
	
	public void setRegTime(String regTime)
	{
		this.regTime = regTime;
	}
	
}
