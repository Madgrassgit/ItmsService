/**
 * DiagnosticServiceChecker.java
 * 业务诊断信息查询接口 检查类
 */
package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * 业务诊断信息查询接口 检查类
 * 
 * @author chenjie
 * @date 2011-12-5
 */
public class DiagnosticServiceChecker extends BaseChecker{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(DiagnosticServiceChecker.class);
	
	private DiagnosticInfoOBJ obj = new DiagnosticInfoOBJ(); 
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public DiagnosticServiceChecker(String inXml) {
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
		
		/**
		if (sheetInfo != null && !sheetInfo.isEmpty())
		{
			Element sheets = root.addElement("Sheets");
			sheets.addElement("SN").addText(userSN);
			sheets.addElement("CityId").addText(cityId);
			sheets.addElement("DevSN").addText(devSn);
			sheets.addElement("DevType").addText(devType);
			for(Map<String,String> info : sheetInfo)
			{
				Element sheetInfo = sheets.addElement("sheetInfo");
				sheetInfo.addElement("DealDate").addText(info.get("DealDate"));
				sheetInfo.addElement("ServiceType").addText(info.get("ServiceType"));
				sheetInfo.addElement("OpenStatus").addText(info.get("OpenStatus"));
			}
		}
		**/
		
		
		
		/**
		 * 以下由"注释by zhangchy 2012-03-06" 注释的代码是根据需求单JSDX_ITMS-REQ-20120220-LUHJ-005修改的
		 * 要求根据ItmsService接口文档中的要求，注释的字段信息不需要返回给综调 所以将不需要返回的字段注释
		 */
		
		// 拼接xml
		if(result == 0)
		{
			Element diagnosticsInfo = root.addElement("DiagnosticsInfo");
			// 上行方式
			Element accessType = diagnosticsInfo.addElement("AccessType");
			accessType.addText(obj.getAccessType());
			
			/**
			 * 安徽电信目前只有VOIP语音业务，没有IPTV和宽带上网业务 
			 * modify by zhangchy 2013-04-07
			 */
			if ("ah_dx".equals(Global.G_instArea)) {
				
			}else {
				// Internet
				Element internet = diagnosticsInfo.addElement("Internet");
				internet.addElement("connectType").addText(StringUtil.getStringValue(obj.getInternetConnectType()));
//				internet.addElement("connectStatus").addText(StringUtil.getStringValue(obj.getInternetConnectStatus())); // 注释 by zhangchy 2012-03-06
				internet.addElement("IPAddress").addText(StringUtil.getStringValue(obj.getInternetIPAddress()));
				internet.addElement("DNSServer").addText(StringUtil.getStringValue(obj.getInternetDNSServer()));
				//新疆要用   zhangsm  20120405
				if("xj_dx".equals(Global.G_instArea))
				{
					internet.addElement("connectStatus").addText(StringUtil.getStringValue(obj.getInternetConnectStatus()));
					internet.addElement("Pvc").addText(StringUtil.getStringValue(obj.getInternetPVC()));
					internet.addElement("VlanId").addText(StringUtil.getStringValue(obj.getInternetVLAN()));
					internet.addElement("Username").addText(StringUtil.getStringValue(obj.getInternetUsername()));
					internet.addElement("errorType").addText(StringUtil.getStringValue(obj.getInternetErrorCode()));
					internet.addElement("MAC").addText(StringUtil.getStringValue(obj.getInternetMAC()));
					internet.addElement("Session").addText(StringUtil.getStringValue(""));
					internet.addElement("BindPort").addText(StringUtil.getStringValue(obj.getInternetBindport()));
				}
				
				// IPTV
				Element iptv = diagnosticsInfo.addElement("IPTV");
				iptv.addElement("connectType").addText(StringUtil.getStringValue(obj.getIptvConnectType()));
				iptv.addElement("connectStatus").addText(StringUtil.getStringValue(obj.getIptvConnectStatus()));
				//新疆要用   zhangsm  20120405
				if("xj_dx".equals(Global.G_instArea))
				{
					iptv.addElement("Pvc").addText(StringUtil.getStringValue(obj.getIptvPVC()));
					iptv.addElement("VlanId").addText(StringUtil.getStringValue(obj.getIptvVLAN()));
					iptv.addElement("BindPort").addText(StringUtil.getStringValue(obj.getIptvBindport()));
				}
			}
			
			// VOIP
			Element voip = diagnosticsInfo.addElement("VOIP");
			voip.addElement("connectType").addText(StringUtil.getStringValue(obj.getVoipConnectType()));
			voip.addElement("connectStatus").addText(StringUtil.getStringValue(obj.getVoipConnectStatus()));
			voip.addElement("registStatus").addText(StringUtil.getStringValue(obj.getVoipRegistStatus()));
			voip.addElement("IPAddress").addText(StringUtil.getStringValue(obj.getVoipIPAddress()));
			voip.addElement("DNSServer").addText(StringUtil.getStringValue(obj.getVoipDNSServer()));
			voip.addElement("SubnetMask").addText(StringUtil.getStringValue(obj.getVoipSubnetMask()));
			voip.addElement("DefaultGateway").addText(StringUtil.getStringValue(obj.getDefaultGateWay()));
		
			// IAD诊断
			Element IADDiagnostics = voip.addElement("IADDiagnostics");
			if("xj_dx".equals(Global.G_instArea)){
				IADDiagnostics.addElement("IADDiagnosticsState").addText("");
				IADDiagnostics.addElement("TestServer").addText("");
				IADDiagnostics.addElement("RegistResult").addText("");
				IADDiagnostics.addElement("Reason").addText("");
			} else {
				IADDiagnostics.addElement("IADDiagnosticsState").addText(StringUtil.getStringValue(obj.getIADDiagnosticState()));
				IADDiagnostics.addElement("TestServer").addText(StringUtil.getStringValue(obj.getIADDiagnosticsTestServer()));
				IADDiagnostics.addElement("RegistResult").addText(StringUtil.getStringValue(obj.getIADDiagnosticsRegistResult()));
				IADDiagnostics.addElement("Reason").addText(StringUtil.getStringValue(obj.getIADDiagnosticsReason()));
			}
			
			//新疆要用   zhangsm  20120405
			if("xj_dx".equals(Global.G_instArea))
			{
				voip.addElement("Pvc").addText(StringUtil.getStringValue(obj.getVoipPVC()));
				voip.addElement("VlanId").addText(StringUtil.getStringValue(obj.getVoipVLAN()));
				voip.addElement("registErrorType").addText(StringUtil.getStringValue(obj.getIADDiagnosticsReason()));
				voip.addElement("protocolType").addText(StringUtil.getStringValue(obj.getVoipProtocolType()));
				voip.addElement("RegistrarServer").addText(StringUtil.getStringValue(obj.getVoipRegistrarServer()));
				voip.addElement("RegistrarServerPort").addText(StringUtil.getStringValue(obj.getVoipRegistrarServerPort()));
				voip.addElement("StandByRegistrarServer").addText(StringUtil.getStringValue(obj.getVoipStandByRegistrarServer()));
				voip.addElement("StandByRegistrarServerPort").addText(StringUtil.getStringValue(obj.getVoipStandByRegistrarServerPort()));
			}
			//line信息
			Element lines = voip.addElement("Lines");
			List<HashMap<String, String>> voipLineList = obj.getVoipLineList();
			Element line = null;
			HashMap map = null;
			for(int i=0; i<voipLineList.size(); i++)
			{
				map = voipLineList.get(i);
				line = lines.addElement("line");
				line.addAttribute("num", StringUtil.getStringValue(map.get("line_id")));
				Element status = line.addElement("status");
				status.addText(StringUtil.getStringValue(map.get("status")));
				if("xj_dx".equals(Global.G_instArea))
				{
					line.addElement("AuthUserName").addText(StringUtil.getStringValue(map.get("username")));
					line.addElement("AuthPassword").addText(StringUtil.getStringValue(map.get("password")));
				}
			}
			
			
			/**
			 * 安徽电信不需要展示PON信息节点
			 * modify by zhangchy 2013-04-07
			 */
			if ("ah_dx".equals(Global.G_instArea)) {
				
			}else{
				//PON信息
				Element ponInfo = diagnosticsInfo.addElement("PonInfo");
				ponInfo.addElement("Status").addText(StringUtil.getStringValue(obj.getPonStatus()));
				ponInfo.addElement("TXPower").addText(StringUtil.getStringValue(obj.getTxPower()));
				ponInfo.addElement("RXPower").addText(StringUtil.getStringValue(obj.getRxPower()));
				ponInfo.addElement("TransceiverTemperature").addText(StringUtil.getStringValue(obj.getTransceiverTemperature()));
				ponInfo.addElement("SupplyVottage").addText(StringUtil.getStringValue(obj.getSupplyVottage()));
				ponInfo.addElement("BiasCurrent").addText(StringUtil.getStringValue(obj.getBiasCurrent()));
				/**
				 * 注释 by zhangchy 2012-03-06
				 * 新疆要用   zhangsm  20120405
				 */
				if("xj_dx".equals(Global.G_instArea))
				{
					ponInfo.addElement("sentBytes").addText(StringUtil.getStringValue(obj.getSentBytes()));
					ponInfo.addElement("receivedBytes").addText(StringUtil.getStringValue(obj.getReceivedBytes()));
					ponInfo.addElement("sentPackets").addText(StringUtil.getStringValue(obj.getSentPackets()));
					ponInfo.addElement("receivedPackets").addText(StringUtil.getStringValue(obj.getReceivedPackets()));
					ponInfo.addElement("sUnicastPackets").addText(StringUtil.getStringValue(obj.getSUnicastPackets()));
					ponInfo.addElement("rUnicastPackets").addText(StringUtil.getStringValue(obj.getRUnicastPackets()));
					ponInfo.addElement("sMulticastPackets").addText(StringUtil.getStringValue(obj.getSMulticastPackets()));
					ponInfo.addElement("rMulticastPackets").addText(StringUtil.getStringValue(obj.getRMulticastPackets()));
					ponInfo.addElement("sBroadcastPackets").addText(StringUtil.getStringValue(obj.getSBroadcastPackets()));
					ponInfo.addElement("rBroadcastPackets").addText(StringUtil.getStringValue(obj.getRBroadcastPackets()));
					ponInfo.addElement("FECError").addText(StringUtil.getStringValue(obj.getFecError()));
					ponInfo.addElement("HECError").addText(StringUtil.getStringValue(obj.getHecError()));
					ponInfo.addElement("dropPackets").addText(StringUtil.getStringValue(obj.getDropPackets()));
					ponInfo.addElement("sPausePackets").addText(StringUtil.getStringValue(obj.getSPausePackets()));
					ponInfo.addElement("rPausePackets").addText(StringUtil.getStringValue(obj.getRPausePackets()));
				}
			}
		}
		
		return document.asXML();
	}

	public DiagnosticInfoOBJ getObj() {
		return obj;
	}

	public void setObj(DiagnosticInfoOBJ obj) {
		this.obj = obj;
	}
	
	public static void main(String[] args) {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		
		root.addElement("test1").addText("111");
		
		root.addElement("test2").addAttribute("attr", "222").addText("333");
		
		System.err.println(document.asXML());
		
	}
}