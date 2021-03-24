package com.linkage.itms.dispatch.obj;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class CloudQueryVXLANConfigureChecker extends CloudBaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(CloudQueryVXLANConfigureChecker.class);

	private String vxlanConfigSequence = "";	// VXLANConfig目录下的序列号   有值时，查询对应实例配置信息；为空时，查询所有实例配置信息。

	private int instanceNum = 0;				// Vxlan实例数量
	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public CloudQueryVXLANConfigureChecker(String inXml) {
		callXml = inXml;
	}

	@Override
	public boolean check() {
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			dealDate = root.elementTextTrim("DealDate");

			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			vxlanConfigSequence = param.elementTextTrim("VXLANConfigSequence");
			vxlanConfigSequence = ("-1".equals(vxlanConfigSequence) || StringUtil.IsEmpty(vxlanConfigSequence)) ?
					"" : vxlanConfigSequence;
		} catch (Exception e) {
			e.printStackTrace();
			result = 3;
			resultDesc = "入参格式错误";
			return false;
		}
		//参数合法性检查
		if (!baseCheck() || !userInfoTypeCheck() || !userInfoCheck()) {
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	public String getReturnXml(ArrayList<Map<String, String>> retList) {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(String.valueOf(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		
		Element par = root.addElement("Param");
		// Vxlan实例数
		par.addElement("InstanceNum").addText(String.valueOf(instanceNum));
		
		if (retList == null || retList.isEmpty()) {
			Element inst = par.addElement("Instance");
			// 当前最新绑定的Loid
			inst.addElement("Loid").addText(loid);
			// 通过账号查到多个Loid，除去最新绑定Loid之外的Loid集合
			inst.addElement("LoidPrev").addText(loidPrev);
			inst.addElement("VXLANConfigSequence").addText("");
			inst.addElement("TunnelKey").addText("");
			inst.addElement("TunnelRemoteIP").addText("");
			inst.addElement("WorkMode").addText("");
			inst.addElement("MaxMTUSize").addText("");
			inst.addElement("IPAddress").addText("");
			inst.addElement("SubnetMask").addText("");
			inst.addElement("AddressingType").addText("");
			inst.addElement("NATEnabled").addText("");
			inst.addElement("DNSServers_Master").addText("");
			inst.addElement("DNSServers_Slave").addText("");
			inst.addElement("DefaultGateway").addText("");
			inst.addElement("X_CT-COM_VLAN").addText("");
			return document.asXML();
		}
		
		for (Map<String, String> map : retList) {
			Element inst = par.addElement("Instance");
			// 当前最新绑定的Loid
			inst.addElement("Loid").addText(loid);
			// 通过账号查到多个Loid，除去最新绑定Loid之外的Loid集合
			inst.addElement("LoidPrev").addText(loidPrev);
			inst.addElement("VXLANConfigSequence").addText(StringUtil.getStringValue(map, "VXLANConfigSequence", ""));
			inst.addElement("TunnelKey").addText(StringUtil.getStringValue(map, "TunnelKey", ""));
			inst.addElement("TunnelRemoteIP").addText(StringUtil.getStringValue(map, "TunnelRemoteIP", ""));
			inst.addElement("WorkMode").addText(StringUtil.getStringValue(map, "WorkMode", ""));
			inst.addElement("MaxMTUSize").addText(StringUtil.getStringValue(map, "MaxMTUSize", ""));
			inst.addElement("IPAddress").addText(StringUtil.getStringValue(map, "IPAddress", ""));
			inst.addElement("SubnetMask").addText(StringUtil.getStringValue(map, "SubnetMask", ""));
			inst.addElement("AddressingType").addText(StringUtil.getStringValue(map, "AddressingType", ""));
			inst.addElement("NATEnabled").addText(StringUtil.getStringValue(map, "NATEnabled", ""));
			inst.addElement("DNSServers_Master").addText(StringUtil.getStringValue(map, "DNSServers_Master", ""));
			inst.addElement("DNSServers_Slave").addText(StringUtil.getStringValue(map, "DNSServers_Slave", ""));
			inst.addElement("DefaultGateway").addText(StringUtil.getStringValue(map, "DefaultGateway", ""));
			inst.addElement("X_CT-COM_VLAN").addText(StringUtil.getStringValue(map, "X_CT-COM_VLAN", ""));
		}
		return document.asXML();
		
	}

	public static void main(String[] args) {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText("");
		// 结果代码
		root.addElement("RstCode").addText("");
		// 结果描述
		root.addElement("RstMsg").addText("");
		
		Element par = root.addElement("Param");
		// Vxlan实例数
		par.addElement("InstanceNum").addText("");
		Element inst = par.addElement("Instance");
		inst.addElement("X_CT-COM_VLAN").addText("");
		inst = par.addElement("Instance");
		inst.addElement("X_CT-COM_VLAN").addText("");
		System.out.println(document.asXML());
		
	}
	public String getVxlanConfigSequence() {
		return vxlanConfigSequence;
	}

	public void setVxlanConfigSequence(String vxlanConfigSequence) {
		this.vxlanConfigSequence = vxlanConfigSequence;
	}

	public int getInstanceNum() {
		return instanceNum;
	}

	public void setInstanceNum(int instanceNum) {
		this.instanceNum = instanceNum;
	}

	@Override
	public String getReturnXml() {
		return null;
	}
}
