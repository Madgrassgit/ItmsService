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
 * @author chenxj6
 * @version 1.0
 * @since 2016-10-13
 */
public class DevConfigChecker4NX extends BaseChecker
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
	public DevConfigChecker4NX(String inXml) {
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

		if ("nx_dx".equals(Global.G_instArea)){
			if(1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType){
				result = 2;
				resultDesc = "客户端类型非法";
				return false;
			}
			
			if (1 != userInfoType && 2 != userInfoType && 3 != userInfoType
					&& 4 != userInfoType && 5 != userInfoType) {
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
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
		logger.debug("getReturnXml()");
		
		List<HashMap<String, String>> confInfoList = null;
		
		
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
					if ("PPPoE_Bridged".equals(internetInfo.get("conn_type"))) {
						wanType.addText("1"); // 桥接
					} else if ("IP_Routed".equals(internetInfo.get("conn_type"))) {
						wanType.addText("2"); // 路由
					}
					if ("Static".equals(internetInfo.get("conn_type"))) {
						wanType.addText("3"); // 静态IP
					} else if ("DHCP".equals(internetInfo.get("conn_type"))) {
						wanType.addText("4"); // DHCP
					}
					
					Element bindPort = internet.addElement("BindPort");  // 端口 
					if (null == internetInfo.get("bind_port") || internetInfo.get("bind_port").isEmpty()) {
						bindPort.addText("");
					} else {
						
						String [] bindPortArr = internetInfo.get("bind_port").split(",");
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
					
					Element kdPvcOrVlanId = internet.addElement("KdPvcOrVlanId");
					if (null != internetInfo.get("vlan_id")	&& false == internetInfo.get("vlan_id").isEmpty()) {
						kdPvcOrVlanId.addText(internetInfo.get("vlan_id"));
					} else {
						kdPvcOrVlanId.addText("");
					}
					
					
					// 采集wifiState状态
					Element wifiStateEle = internet.addElement("WifiState");
					if(null != internetInfo.get("status") && false ==internetInfo.get("status").isEmpty() ) {
						wifiStateEle.addText(internetInfo.get("status"));
					} else {
						wifiStateEle.addText("");
					}
				}
				
			// 如果没查询到数据 ，节点内容显示空
			} else {
				Element internet = root.addElement("Internet");
				
				Element wanType = internet.addElement("WanType");
				wanType.addText("");  // 上网方式
				
				Element bindPort = internet.addElement("BindPort");
				bindPort.addText("");
				
				Element kdPvcOrVlanId = internet.addElement("KdPvcOrVlanId");
				kdPvcOrVlanId.addText("");
				
				Element wifiStateEle = internet.addElement("WifiState");
				wifiStateEle.addText("");
			}
			
// 			------Internet------end---------------------------------------------------------------------------
			
			
//			------IPTV------begin-------------------------------------------------------------------------------
			Element iptv = root.addElement("IPTV");
			
			confInfoList = configInfo.get("iptv_list");
			
			if (confInfoList == null || confInfoList.isEmpty()) {
				confInfoList = new ArrayList<HashMap<String,String>>();
			}
			
			// 如果查询到数据 ，则取相关数据
			if (confInfoList.size() > 0) {  
				Map<String,String> iptvInfo = confInfoList.get(0);  // 只取一条数据
				Element wanType = iptv.addElement("WanType");
				// 上网方式
				if ("PPPoE_Bridged".equals(iptvInfo.get("conn_type"))) {
					wanType.addText("1"); // 桥接
				} else if ("IP_Routed".equals(iptvInfo.get("conn_type"))) {
					wanType.addText("2"); // 路由
				}
				if ("Static".equals(iptvInfo.get("conn_type"))) {
					wanType.addText("3"); // 静态IP
				} else if ("DHCP".equals(iptvInfo.get("conn_type"))) {
					wanType.addText("4"); // DHCP
				}
				
//				if ("1".equals(iptvInfo.get("sess_type"))) {  //  PPP
//					if ("PPPoE_Bridged".equals(iptvInfo.get("conn_type"))) {
//						wanType.addText("1");  // 桥接  
//					} else if ("IP_Routed".equals(iptvInfo.get("conn_type"))) {
//						wanType.addText("2");  // 路由
//					}
//				} else if("2".equals(iptvInfo.get("sess_type"))) {
//					if ("Static".equals(iptvInfo.get("ip_type"))) {
//						wanType.addText("3");  // 静态IP
//					} else if ("DHCP".equals(iptvInfo.get("ip_type"))) {
//						wanType.addText("4");  // DHCP
//					}
//				}
				
				Element iPTVPvcOrVlanId = iptv.addElement("IPTVPvcOrVlanId");
				if (null != iptvInfo.get("vlan_id")	&& false == iptvInfo.get("vlan_id").isEmpty()) {
					iPTVPvcOrVlanId.addText(iptvInfo.get("vlan_id"));
				} else {
					iPTVPvcOrVlanId.addText("");
				}
				
				
//				if("1".equals(resultMap.get("AccessType"))) {
//					iPTVPvcOrVlanId.addText(iptvInfo.get("vpi_id")+ "/" + iptvInfo.get("vci_id"));
//				} else {
//					if(null != iptvInfo.get("vlan_id") && false ==iptvInfo.get("vlan_id").isEmpty() ) {
//						iPTVPvcOrVlanId.addText(iptvInfo.get("vlan_id"));
//					} else {
//						iPTVPvcOrVlanId.addText("");
//					}
//				}
				
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
				
				// 采集MulticastVlan
				Element multicastVlanEle = iptv.addElement("MulticastVlan");
				if (null != iptvInfo.get("multicast_vlan") && false == iptvInfo.get("multicast_vlan").isEmpty()) {
					multicastVlanEle.addText(iptvInfo.get("multicast_vlan"));
				} else {
					multicastVlanEle.addText("");
				}
				
			// 如果没查询到数据，节点类容为空
			} else {
				Element wanType = iptv.addElement("WanType");
				wanType.addText("");
				
				Element iPTVPvcOrVlanId = iptv.addElement("IPTVPvcOrVlanId");
				iPTVPvcOrVlanId.addText("");
				
				Element bindPort = iptv.addElement("BindPort");  // 端口 
				bindPort.addText("");
				
				Element multicastVlanEle = iptv.addElement("MulticastVlan");
				multicastVlanEle.addText("");
			}
// 			------IPTV------end-------------------------------------------------------------
			
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
	
}
