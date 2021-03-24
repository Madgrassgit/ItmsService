package com.linkage.itms.dispatch.cqdx.service;

import java.io.StringReader;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

public class CommonInterfaceOperationService {
	private static Logger logger = LoggerFactory.getLogger(CommonInterfaceOperationService.class);

	@SuppressWarnings("unchecked")
	public String work(String inXml) {
		logger.warn("servicename[CommonInterfaceOperationService]执行，入参为：{}", inXml);
		String result = "";
		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(new StringReader(inXml));
			Element root = document.getRootElement();
			String interfaceName = root.elementTextTrim("interface_name");
			logger.warn("interfaceName=" + interfaceName);
			Element paramsNode = root.element("interface_param");
			List<Element> params = paramsNode.elements();
			
			Document outDocument = DocumentHelper.createDocument();
			document.setXMLEncoding("GBK");
			Element outRoot = outDocument.addElement("root");
			for (Element e : params) {
				 outRoot.addElement(e.getName()).addText(e.getTextTrim());
				if("trace_param".equals(e.getName()) && "gatewayTracertOperation".equals(interfaceName)){
					Element trans_param = outRoot.element("trace_param");
					List<Element> _params = e.elements();
					for (Element _e : _params) {
						trans_param.addElement(_e.getName()).addText(_e.getTextTrim());
					}
				}
				else if("input_array".equals(e.getName()) && "feedbackWorkTicketsInfo".equals(interfaceName)){
					Element input_arrayO = outRoot.element("input_array");
					List<Element> query_keys = e.elements();
					for (Element _f : query_keys) {
						Element query_keyO = input_arrayO.addElement("query_key");
						List<Element> _gs = _f.elements();
						for (Element _g : _gs) {
							query_keyO.addElement(_g.getName()).addText(_g.getTextTrim());
						}
					}
				}
			}
			
			if ("getRgModeOfTerminal".equals(interfaceName)) {
				result = new QueryRgModeInfoService().work(outDocument.asXML());
			}
			else if ("factoryResetOperation".equals(interfaceName)) 
			{
				FactoryResetService serviceThread = new FactoryResetService();
			    serviceThread.setMessage(outDocument.asXML());
			    Global.G_BlThreadPool.execute(serviceThread);
//				result = new FactoryResetService().work(outDocument.asXML(),true);
				Document returnDocument = DocumentHelper.createDocument();
				Element returnroot = returnDocument.addElement("response");
				// 结果代码
				returnroot.addElement("result_code").addText("0");
				returnroot.addElement("result_desc").addText("接收成功");
				result = returnDocument.asXML();
			}//修改宽带密码
			else if("changeBroadbandPassword".equals(interfaceName))
			{
				result = new ChangeBroadbandPasswordService().work(outDocument.asXML());
			}//查询状态信息
			else if("getWANConnectionStatusInfo".equals(interfaceName) )
			{
				result = new GetWANConnectionStatusInfoService().work(outDocument.asXML());
			}//设备绑定操作
			else if("bindTerminalDevice".equals(interfaceName) )
			{
				result = new BindTerminalDeviceService().work(outDocument.asXML());
			}//VLAN信息查询
			else if("getVLanInfo".equals(interfaceName) )
			{
				result = new GetVLanInfoService().work(outDocument.asXML());
			}
			//查询工单详细信息及执行情况
			else if("getTicketDetailInfo".equals(interfaceName) )
			{
				result = new QueryTicketDetailInfoService().work(outDocument.asXML());
			}
			//家庭网关基本信息查询
			else if("getGatewayBasicInfo".equals(interfaceName) )
			{
				result = new QueryGatewayBasicInfoService().work(outDocument.asXML());
			}
			//桥改路由
			else if("changeRgMode".equals(interfaceName) )
			{
				result = new ChangeRgModeService().work(outDocument.asXML());
			}
			//更改宽带信息
			else if("changeBroadbandInfo".equals(interfaceName) )
			{
				result = new ChangeBroadbandInfoService().work(outDocument.asXML());
			}
			//家庭网关tracert测试
			else if ("gatewayTracertOperation".equals(interfaceName)) {
				result = new TraceRouteService().work(outDocument.asXML());
			}
			//查询承载IPTV的LAN端口
			else if ("getGatewayIptvLanPort".equals(interfaceName)) {
				result = new QueryIPTVLanService().work(outDocument.asXML());
			}
			//在终端上实时查询承载IPTV的LAN端口(实时采集）
			else if ("getIptvLanPortOfTerminal".equals(interfaceName)) {
				result = new GatherIPTVLanPortService().work(outDocument.asXML());
			}
			// 修改或重置终端超级密码
			else if ("changeTerminalPassword".equals(interfaceName)) {
				result = new ChangeTerminalPasswordService().work(outDocument.asXML());
			}
			// 家庭网关LAN口连接状态查询接口
			else if ("getLanConnectionStatusInfo".equals(interfaceName)) {
				result = new GetLanConnectionStatusInfoService().work(outDocument.asXML());
			}
			// 家庭网关性能指标查询接口
			else if ("getGatewayPerformanceInfo".equals(interfaceName)) {
				result = new GetGatewayPerformanceInfoService().work(outDocument.asXML());
			}
			// 家庭网关配置稽核接口
			else if ("getGatewayConfigInfo".equals(interfaceName)) {
				result = new GetGatewayConfigInfoService().work(outDocument.asXML());
			}
			else if("getVLanInfoOfTerminal".equals(interfaceName)){
				result = new GetVLanInfoOfTerminalService().work(outDocument.asXML());
			}
			else if("getWLanConnectionStatusInfo".equals(interfaceName)){
				result = new GetWLanConnStatusInfoService().work(outDocument.asXML());
			}
			else if("queryBindInfo".equals(interfaceName)){
				result = new QueryBindInfoService().work(outDocument.asXML());
			}
			//修改光猫无线接入数
			else if("setGatewayTerminalNumber".equals(interfaceName)){
				result = new SetGatewayTerminalNumberService().work(outDocument.asXML());
			}
			//查询终端无线接入数
			else if("getGatewayTerminalNumber".equals(interfaceName)){
				result = new GetGatewayTerminalNumber().work(outDocument.asXML());
			}
			//2.18.4.22	绑定关系查询
			else if("feedbackWorkTicketsInfo".equals(interfaceName)){
				result = new FeedbackWorkTicketsInfo4CommService().work(outDocument.asXML());
			}
			//修改SSID名称和秘钥接口
			else if("changeSSID".equals(interfaceName)){
				result = new ChangeWifiPasswordService().work(outDocument.asXML().replace("ssid_name", "ssid").replace("ssid_passwd", "wifi_password"));
				Document resultDocument = reader.read(new StringReader(result));
				Element resultRoot = resultDocument.getRootElement();
				String resultMes = StringUtil.getStringValue(resultRoot.elementTextTrim("result"));
				String failed_reason = StringUtil.getStringValue(resultRoot.elementTextTrim("failed_reason"));
				Document returnDocument = DocumentHelper.createDocument();
				Element returnroot = returnDocument.addElement("response");
				// 结果代码
				returnroot.addElement("result_code").addText(resultMes);
				returnroot.addElement("result_desc").addText(failed_reason);
				result = returnDocument.asXML();
			}else if("queryGatewayServiceStatus".equals(interfaceName)){
				result = new GatewayServiceStatusService().work(outDocument.asXML());
			}
		} catch (DocumentException e) {
			logger.error("servicename[CommonInterfaceOperationService]is error", e);
		}
		return result;
	}
	
	public static void main(String[] args) {
		CommonInterfaceOperationService s = new CommonInterfaceOperationService();
		s.work("<request><interface_name>getWLanConnectionStatusInfo</interface_name>" +
        "<interface_param><ppp_username></ppp_username><logic_id>LG2060323627</logic_id>" +
        "<trace_param><trace_host>1</trace_host><number_of_tries>1</number_of_tries><max_hop_count>1</max_hop_count>" +
        "<data_block_size>1</data_block_size><time_out>1</time_out></trace_param></interface_param></request>");
	}
}
