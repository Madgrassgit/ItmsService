package com.linkage.itms.obj;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ITMS回复结果对象类
 * 
 * @author Jason(3412)
 * @date 2009-12-16
 */
public class DevStatusOBJ {

	private static Logger logger = LoggerFactory.getLogger(DevStatusOBJ.class);

	private String deviceId;
	// 连接状态 1连通 0不通
	private String connectState;
	// WAN口状态 down up
	private String wanState;
	// 拨号错误码 1正常 2不正常 3桥接的话，返回“桥接方式，未检测”
	private String bohaoResult;
	// 上行方式1：AD上行 2：LAN 3：EPON上行
	private String accessType;
	// 设备厂商
	private String modemVendor;
	// 设备型号
	private String modemType;
	// 设备序列号
	private String modemSn;
	// session连接状态
	private String pvcState;
	// pvc值 格式：'PVC:8/85'
	private String pvc;
	// vlanid
	private String vlanid;
	//Qos优先级  1：优先级(优先级最高)  2：优先级(VOIP优先级高于IPTV)  0：无Qos优先级
	private int qosPriority = 0;
	//qos_mode
	private String qosMode = null;
	
	//PVC存在 1：存在  -1：不存在
	@SuppressWarnings("unused")
	private int pvcExists = -1;

	/**
	 * 根据对象返回XML字符串(暂只返回连接状态和上行方式，其他信息不准确不返回)
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-12-16
	 * @return String
	 */
	public String obj2Xml() {
		logger.debug("obj2Xml()");

		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("root");
		
		root.addElement("connect_state").addText(
				connectState == null ? "-1" : connectState);
		root.addElement("wan_state")
				.addText(wanState == null ? "-1" : wanState);
		root.addElement("accesstype").addText(
				accessType == null ? "-1" : accessType);
		//暂无返回值
		root.addElement("bohao_resulte").addText(
				bohaoResult == null ? "-1" : bohaoResult);
		
		root.addElement("modem_vendor").addText(
				modemVendor == null ? "-1" : modemVendor);
		root.addElement("modem_type").addText(
				modemType == null ? "-1" : modemType);
		root.addElement("modem_sn").addText(modemSn == null ? "-1" : modemSn);
		//暂无法判断
		root.addElement("pvc_state")
				.addText(pvcState == null ? "-1" : pvcState);
		root.addElement("pvc").addText(pvc == null ? "-1" : pvc);
		root.addElement("vlanid").addText(vlanid == null ? "-1" : vlanid);
		root.addElement("qos_mode").addText(null == qosMode ? "-1" : qosMode);
//		root.addElement("qos_priority").addText(StringUtil.getStringValue(qosPriority));
//		root.addElement("pvc_exists").addText(StringUtil.getStringValue(pvcExists));
		return document.asXML();
		
	}

	/** getter, setter methods */
	public String getConnectState() {
		return connectState;
	}

	public void setConnectState(String connectState) {
		this.connectState = connectState;
	}

	public String getWanState() {
		return wanState;
	}

	public void setWanState(String wanState) {
		this.wanState = wanState;
	}

	public String getBohaoResult() {
		return bohaoResult;
	}

	public void setBohaoResult(String bohaoResult) {
		this.bohaoResult = bohaoResult;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public String getModemVendor() {
		return modemVendor;
	}

	public void setModemVendor(String modemVendor) {
		this.modemVendor = modemVendor;
	}

	public String getModemType() {
		return modemType;
	}

	public void setModemType(String modemType) {
		this.modemType = modemType;
	}

	public String getModemSn() {
		return modemSn;
	}

	public void setModemSn(String modemSn) {
		this.modemSn = modemSn;
	}

	public String getPvcState() {
		return pvcState;
	}

	public void setPvcState(String pvcState) {
		this.pvcState = pvcState;
	}

	public String getPvc() {
		return pvc;
	}

	public void setPvc(String pvc) {
		this.pvc = pvc;
	}

	public String getVlanid() {
		return vlanid;
	}

	public void setVlanid(String vlanid) {
		this.vlanid = vlanid;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return the qosPriority
	 */
	public int getQosPriority() {
		return qosPriority;
	}

	/**
	 * @param qosPriority the qosPriority to set
	 */
	public void setQosPriority(int qosPriority) {
		this.qosPriority = qosPriority;
	}

	/**
	 * @return the qosMode
	 */
	public String getQosMode() {
		return qosMode;
	}

	/**
	 * @param qosMode the qosMode to set
	 */
	public void setQosMode(String qosMode) {
		this.qosMode = qosMode;
	}

}
