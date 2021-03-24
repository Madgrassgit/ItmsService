package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;


public class QueryVoipChecker extends BaseChecker {

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(QueryVoipChecker.class);

	/**
	 * 逻辑id
	 */
	private String loid = null;
	/**
	 * 语音号码
	 */
	private String voip  = null;
	private String VOIPPvcOrVlanId  = null;
	private String VOIPType = null;
	private String ProxyServer = null;
	private String ProxyServerPort = null;
	private String StandByProxyServer = null;
	private String StandByProxyServerPort = null;
	private String RegistrarServer = null;
	private String RegistrarServerPort = null;
	private String StandByRegistrarServer = null;
	private String StandByRegistrarServerPort = null;
	private String OutboundProxy = null;
	private String OutboundProxyPort = null;
	private String StandByOutboundProxy  = null;
	private String StandByOutboundProxyPort  = null;
	private List<String> AuthUserNameList  = null;
	private List<String> AuthPasswordList  = null;




	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public QueryVoipChecker(String inXml) {
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
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");

			loid = param.elementTextTrim("Loid");
			voip = param.elementTextTrim("Voip");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		if (false == baseCheck()) {
			return false;
		}
		if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType && 5 != clientType) {
			result = 2;
			resultDesc = "客户端类型非法";
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
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(""+result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		Element voipEle = root.addElement("VOIP");
		voipEle.addElement("VOIPPvcOrVlanId").addText(StringUtil.getStringValue(VOIPPvcOrVlanId));
		voipEle.addElement("VOIPType").addText(StringUtil.getStringValue(VOIPType));
		Element voiceServiceEle = voipEle.addElement("VoiceService");
		voiceServiceEle.addElement("ProxyServer").addText(StringUtil.getStringValue(ProxyServer));
		voiceServiceEle.addElement("ProxyServerPort").addText(StringUtil.getStringValue(ProxyServerPort));
		voiceServiceEle.addElement("StandByProxyServer").addText(StringUtil.getStringValue(StandByProxyServer));
		voiceServiceEle.addElement("StandByProxyServerPort").addText(StringUtil.getStringValue(StandByProxyServerPort));
		voiceServiceEle.addElement("RegistrarServer").addText(StringUtil.getStringValue(RegistrarServer));
		voiceServiceEle.addElement("RegistrarServerPort").addText(StringUtil.getStringValue(RegistrarServerPort));
		voiceServiceEle.addElement("StandByRegistrarServer").addText(StringUtil.getStringValue(StandByRegistrarServer));
		voiceServiceEle.addElement("StandByRegistrarServerPort").addText(StringUtil.getStringValue(StandByRegistrarServerPort));
		voiceServiceEle.addElement("OutboundProxy").addText(StringUtil.getStringValue(OutboundProxy));
		voiceServiceEle.addElement("OutboundProxyPort").addText(StringUtil.getStringValue(OutboundProxyPort));
		voiceServiceEle.addElement("StandByOutboundProxy").addText(StringUtil.getStringValue(StandByOutboundProxy ));
		voiceServiceEle.addElement("StandByOutboundProxyPort").addText(StringUtil.getStringValue(StandByOutboundProxyPort));
		Element LinesEle = voipEle.addElement("Lines");
		Element lineEle = LinesEle.addElement("line");
		
		if(null!=AuthUserNameList) {
			for(int i=0;i<AuthUserNameList.size();i++) {
				lineEle.addAttribute("num", String.valueOf(i+1));
				lineEle.addElement("AuthUserName").setText(AuthUserNameList.get(i));
				lineEle.addElement("AuthPassword").setText(AuthPasswordList.get(i));
			}
		}else {
			lineEle.addAttribute("num", "1");
			lineEle.addElement("AuthUserName").setText("");
			lineEle.addElement("AuthPassword").setText("");
		}
		return document.asXML();
	}
	public String getLoid() {
		return loid;
	}
	public void setLoid(String loid) {
		this.loid = loid;
	}
	public String getVoip() {
		return voip;
	}
	public void setVoip(String voip) {
		this.voip = voip;
	}
	public String getVOIPPvcOrVlanId() {
		return VOIPPvcOrVlanId;
	}
	public void setVOIPPvcOrVlanId(String vOIPPvcOrVlanId) {
		VOIPPvcOrVlanId = vOIPPvcOrVlanId;
	}
	public String getVOIPType() {
		return VOIPType;
	}
	public void setVOIPType(String vOIPType) {
		VOIPType = vOIPType;
	}
	public String getProxyServer() {
		return ProxyServer;
	}
	public void setProxyServer(String proxyServer) {
		ProxyServer = proxyServer;
	}
	public String getProxyServerPort() {
		return ProxyServerPort;
	}
	public void setProxyServerPort(String proxyServerPort) {
		ProxyServerPort = proxyServerPort;
	}
	public String getStandByProxyServer() {
		return StandByProxyServer;
	}
	public void setStandByProxyServer(String standByProxyServer) {
		StandByProxyServer = standByProxyServer;
	}
	public String getStandByProxyServerPort() {
		return StandByProxyServerPort;
	}
	public void setStandByProxyServerPort(String standByProxyServerPort) {
		StandByProxyServerPort = standByProxyServerPort;
	}
	public String getRegistrarServer() {
		return RegistrarServer;
	}
	public void setRegistrarServer(String registrarServer) {
		RegistrarServer = registrarServer;
	}
	public String getRegistrarServerPort() {
		return RegistrarServerPort;
	}
	public void setRegistrarServerPort(String registrarServerPort) {
		RegistrarServerPort = registrarServerPort;
	}
	public String getStandByRegistrarServer() {
		return StandByRegistrarServer;
	}
	public void setStandByRegistrarServer(String standByRegistrarServer) {
		StandByRegistrarServer = standByRegistrarServer;
	}
	public String getStandByRegistrarServerPort() {
		return StandByRegistrarServerPort;
	}
	public void setStandByRegistrarServerPort(String standByRegistrarServerPort) {
		StandByRegistrarServerPort = standByRegistrarServerPort;
	}
	public String getOutboundProxy() {
		return OutboundProxy;
	}
	public void setOutboundProxy(String outboundProxy) {
		OutboundProxy = outboundProxy;
	}
	public String getOutboundProxyPort() {
		return OutboundProxyPort;
	}
	public void setOutboundProxyPort(String outboundProxyPort) {
		OutboundProxyPort = outboundProxyPort;
	}
	public String getStandByOutboundProxy() {
		return StandByOutboundProxy;
	}
	public void setStandByOutboundProxy(String standByOutboundProxy) {
		StandByOutboundProxy = standByOutboundProxy;
	}
	public String getStandByOutboundProxyPort() {
		return StandByOutboundProxyPort;
	}
	public void setStandByOutboundProxyPort(String standByOutboundProxyPort) {
		StandByOutboundProxyPort = standByOutboundProxyPort;
	}
	public List<String> getAuthUserNameList() {
		return AuthUserNameList;
	}
	public void setAuthUserNameList(List<String> authUserNameList) {
		AuthUserNameList = authUserNameList;
	}
	public List<String> getAuthPasswordList() {
		return AuthPasswordList;
	}
	public void setAuthPasswordList(List<String> authPasswordList) {
		AuthPasswordList = authPasswordList;
	}

}
