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

public class QueryMultiInterPortCheck  extends BaseChecker{
	private static Logger logger = LoggerFactory.getLogger(QueryMultiInterPortCheck.class);

	private List<HashMap<String,String>> netInfo = new ArrayList<HashMap<String,String>>();
	
	private String LAN1 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1";
	private String LAN2 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2";
	private String LAN3 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.3";
	private String LAN4 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.4";
	
	private String WLAN1 ="InternetGatewayDevice.LANDevice.1.WLANConfiguration.1";
	private String WLAN2 ="InternetGatewayDevice.LANDevice.1.WLANConfiguration.2";
	
	public QueryMultiInterPortCheck()
	{
	}
	public QueryMultiInterPortCheck(String inXml)
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
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck() || false == userInfoCheck() )
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	@Override
	public String getReturnXml()
	{
		Map<String, String> bindPortMap = new HashMap<String, String>();
		
		bindPortMap.put(LAN1, "LAN1");
		bindPortMap.put(LAN2, "LAN2");
		bindPortMap.put(LAN3, "LAN3");
		bindPortMap.put(LAN4, "LAN4");
		bindPortMap.put(WLAN1, "WLAN1");
		bindPortMap.put(WLAN2, "WLAN2");
		
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
		
		Element internets = root.addElement("Internets");
		Map tempMap = null;
		if(null!=netInfo&&netInfo.size()>0){
			for(int i=0;i<netInfo.size();i++){
				tempMap = netInfo.get(i);
				String vlanId = "";
				String bindPort = "";	
				String username = "";
				String bind_port= "";
				if(null!=tempMap){
					username =  StringUtil.getStringValue(tempMap.get("username"));
					vlanId = StringUtil.getStringValue(tempMap.get("vlanid"));
					bindPort = StringUtil.getStringValue(tempMap.get("bind_port"));
					if(null!=bindPort&&!bindPort.equals("")){
						String[] bindPorts = bindPort.split(",");
						for(int j=0;j<bindPorts.length;j++){
							bind_port += bindPortMap.get(bindPorts[j])+",";
						}
					}else{
						bind_port="";
					}
				}
				Element internet = internets.addElement("Internet").addAttribute("num", i+1 +"");
				internet.addElement("IntenetNum").addText(username);
				internet.addElement("VlanID").addText(vlanId);
				if(bind_port.length()>1){
					internet.addElement("BindPort").addText(bind_port.substring(0, bind_port.length()-1));
				}else{
					internet.addElement("BindPort").addText(bind_port);
				}
			}
		}
		return document.asXML();
	}
	public List<HashMap<String, String>> getNetInfo() {
		return netInfo;
	}
	public void setNetInfo(List<HashMap<String, String>> netInfo) {
		this.netInfo = netInfo;
	}
	
	
}