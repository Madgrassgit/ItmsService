/**
 * DiagnosticInfoOBJ.java
 * 业务诊断信息查询接口-信息对象类
 */
package com.linkage.itms.dispatch.obj;

import java.util.HashMap;
import java.util.List;

/**
 * 业务诊断信息查询接口，返回的信息存放在这个OBJ里面
 * 
 * @author chenjie
 * @date 2011-12-5
 * 
 * 以下"注释 by zhangchy 2012-03-06" 是根据需求单JSDX_ITMS-REQ-20120220-LUHJ-005要求
 * 注释的字段不需要返回
 * 
 */
public class DiagnosticInfoOBJ {
	
	/**
	 * deviceId
	 */
	private String deviceId;
	
	/**
	 * userId
	 */
	private String userId;
	
	/**
	 * 接入方式
	 */
	private String accessType;
	
	/**
	 * Internet连接类型
	 */
	private String internetConnectType;
	
	/**
	 * Internet连接状态
	 */
	private String internetConnectStatus;   // 注释 by zhangchy 2012-03-06  // 新疆需要这个字段   zhangsm  20120405
	
	/**
	 * Internet Ip地址
	 */
	private String internetIPAddress;
	
	/**
	 * Internet DNS
	 */
	private String internetDNSServer;
	/**
	 * Internet PVC
	 */
	private String internetPVC;
	/**
	 * Internet VLAN
	 */
	private String internetVLAN;
	/**
	 * Internet username
	 */
	private String internetUsername;
	/**
	 * Internet 拨号失败码
	 */
	private String internetErrorCode;
	/**
	 * Internet mac
	 */
	private String internetMAC;
	/**
	 * Internet sesson
	 */
	private String internetSession;
	/**
	 * Internet bindport
	 */
	private String internetBindport;
	
	/**
	 * IPTV连接类型
	 */
	private String iptvConnectType;
	
	/**
	 * IPTV连接状态
	 */
	private String iptvConnectStatus;
	/**
	 * iptv PVC
	 */
	private String iptvPVC;
	/**
	 * iptv VLAN
	 */
	private String iptvVLAN;
	/**
	 * iptv bindport
	 */
	private String iptvBindport;
	/**
	 * VOIP连接类型
	 */
	private String voipConnectType;
	
	/**
	 * VOIP连接状态
	 */
	private String voipConnectStatus;
	
	/**
	 * VOIP注册状态
	 */
	private String voipRegistStatus;
	
	/**
	 * VOIP ip地址
	 */
	private String voipIPAddress;
	
	/**
	 * VOIP DNS
	 */
	private String voipDNSServer;
	
	/**
	 * VOIP 子网掩码
	 */
	private String voipSubnetMask;
	
	/**
	 * VOIP 默认网关
	 */
	private String defaultGateWay;
	/**
	 * voip PVC
	 */
	private String voipPVC;
	/**
	 * voip VLAN
	 */
	private String voipVLAN;
	/**
	 * voip RegistErrorType
	 */
	private String voipRegistErrorType ;
	/**
	 * voip ProtocolType
	 */
	private String voipProtocolType ;
	/**
	 * voip RegistrarServer
	 */
	private String voipRegistrarServer;
	/**
	 * voip RegistrarServerPort
	 */
	private String voipRegistrarServerPort;
	/**
	 * voip StandByRegistrarServer
	 */
	private String voipStandByRegistrarServer;
	/**
	 * voip StandByRegistrarServerPort
	 */
	private String voipStandByRegistrarServerPort;
	/**
	 * voip AuthUserName
	 */
	private String voipAuthUserName;
	/**
	 * voip AuthPassword
	 */
	private String voipAuthPassword;
	/**
	 * IAD诊断模块状态
	 */
	private String IADDiagnosticState;
	
	/**
	 * IAD 测试服务器
	 */
	private String IADDiagnosticsTestServer;
	
	/**
	 * IAD 注册是否成功
	 */
	private String IADDiagnosticsRegistResult;
	
	/**
	 * IAD 失败原因
	 */
	private String IADDiagnosticsReason;
	
	/**
	 * VOIP-line结果集合
	 */
	private List<HashMap<String, String>> voipLineList;
	
	/**
	 * PON信息
	 */
	private String ponStatus;
	
	private String txPower;
	
	private String rxPower;
	
	private String transceiverTemperature;
	
	private String supplyVottage;
	
	
	private String biasCurrent;
	
/**
 * 注释 by zhangchy 2012-03-06
 * 新疆需要用这些字段   zhangsm  20120405
 * */
	private String sentBytes;
	
	private String receivedBytes;
	
	private String sentPackets;
	
	private String receivedPackets;
	
	private String sUnicastPackets;
	
	private String rUnicastPackets;
	
	private String sMulticastPackets;
	
	private String rMulticastPackets;
	
	private String sBroadcastPackets;
	
	private String rBroadcastPackets;
	
	private String fecError;
	
	private String hecError;
	
	private String dropPackets;
	
	private String sPausePackets;
	
	private String rPausePackets;
	

	
	
	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public String getBiasCurrent() {
		return biasCurrent;
	}

	public void setBiasCurrent(String biasCurrent) {
		this.biasCurrent = biasCurrent;
	}

	public String getDefaultGateWay() {
		return defaultGateWay;
	}

	public void setDefaultGateWay(String defaultGateWay) {
		this.defaultGateWay = defaultGateWay;
	}
/**
 * 注释 by zhangchy 2012-03-06
 * 新疆需要用这些字段   zhangsm  20120405
 */
	public String getDropPackets() {
		return dropPackets;
	}

	public void setDropPackets(String dropPackets) {
		this.dropPackets = dropPackets;
	}

	public String getFecError() {
		return fecError;
	}

	public void setFecError(String fecError) {
		this.fecError = fecError;
	}

	public String getHecError() {
		return hecError;
	}

	public void setHecError(String hecError) {
		this.hecError = hecError;
	}

	public String getIADDiagnosticsReason() {
		return IADDiagnosticsReason;
	}

	public void setIADDiagnosticsReason(String diagnosticsReason) {
		IADDiagnosticsReason = diagnosticsReason;
	}

	public String getIADDiagnosticsRegistResult() {
		return IADDiagnosticsRegistResult;
	}

	public void setIADDiagnosticsRegistResult(String diagnosticsRegistResult) {
		IADDiagnosticsRegistResult = diagnosticsRegistResult;
	}

	public String getIADDiagnosticsTestServer() {
		return IADDiagnosticsTestServer;
	}

	public void setIADDiagnosticsTestServer(String diagnosticsTestServer) {
		IADDiagnosticsTestServer = diagnosticsTestServer;
	}
/**
 * 注释 by zhangchy 2012-03-06
 * 新疆需要这个字段   zhangsm  20120405
 * */
	public String getInternetConnectStatus() {
		return internetConnectStatus;
	}

	public void setInternetConnectStatus(String internetConnectStatus) {
		this.internetConnectStatus = internetConnectStatus;
	}

	public String getInternetConnectType() {
		return internetConnectType;
	}

	public void setInternetConnectType(String internetConnectType) {
		this.internetConnectType = internetConnectType;
	}

	public String getInternetDNSServer() {
		return internetDNSServer;
	}

	public void setInternetDNSServer(String internetDNSServer) {
		this.internetDNSServer = internetDNSServer;
	}

	public String getInternetIPAddress() {
		return internetIPAddress;
	}

	public void setInternetIPAddress(String internetIPAddress) {
		this.internetIPAddress = internetIPAddress;
	}

	public String getIptvConnectStatus() {
		return iptvConnectStatus;
	}

	public void setIptvConnectStatus(String iptvConnectStatus) {
		this.iptvConnectStatus = iptvConnectStatus;
	}

	public String getIptvConnectType() {
		return iptvConnectType;
	}

	public void setIptvConnectType(String iptvConnectType) {
		this.iptvConnectType = iptvConnectType;
	}

	public List<HashMap<String, String>> getVoipLineList() {
		return voipLineList;
	}

	public void setVoipLineList(List<HashMap<String, String>> voipLineList) {
		this.voipLineList = voipLineList;
	}

	public String getPonStatus() {
		return ponStatus;
	}

	public void setPonStatus(String ponStatus) {
		this.ponStatus = ponStatus;
	}
/**
 * 注释 by zhangchy 2012-03-06
 * 新疆需要用这些字段   zhangsm  20120405
 */
	public String getRBroadcastPackets() {
		return rBroadcastPackets;
	}

	public void setRBroadcastPackets(String broadcastPackets) {
		rBroadcastPackets = broadcastPackets;
	}

	public String getReceivedBytes() {
		return receivedBytes;
	}

	public void setReceivedBytes(String receivedBytes) {
		this.receivedBytes = receivedBytes;
	}

	public String getReceivedPackets() {
		return receivedPackets;
	}

	public void setReceivedPackets(String receivedPackets) {
		this.receivedPackets = receivedPackets;
	}

	public String getRMulticastPackets() {
		return rMulticastPackets;
	}

	public void setRMulticastPackets(String multicastPackets) {
		rMulticastPackets = multicastPackets;
	}

	public String getRPausePackets() {
		return rPausePackets;
	}

	public void setRPausePackets(String pausePackets) {
		rPausePackets = pausePackets;
	}

	public String getRUnicastPackets() {
		return rUnicastPackets;
	}

	public void setRUnicastPackets(String unicastPackets) {
		rUnicastPackets = unicastPackets;
	}

	public String getRxPower() {
		return rxPower;
	}

	public void setRxPower(String rxPower) {
		this.rxPower = rxPower;
	}
/**
 * 注释 by zhangchy 2012-03-06
 * 新疆需要用这些字段   zhangsm  20120405
 */
	public String getSBroadcastPackets() {
		return sBroadcastPackets;
	}

	public void setSBroadcastPackets(String broadcastPackets) {
		sBroadcastPackets = broadcastPackets;
	}

	public String getSentPackets() {
		return sentPackets;
	}

	public void setSentPackets(String sentPackets) {
		this.sentPackets = sentPackets;
	}

	public String getSMulticastPackets() {
		return sMulticastPackets;
	}

	public void setSMulticastPackets(String multicastPackets) {
		sMulticastPackets = multicastPackets;
	}

	public String getSPausePackets() {
		return sPausePackets;
	}

	public void setSPausePackets(String pausePackets) {
		sPausePackets = pausePackets;
	}

	public String getSUnicastPackets() {
		return sUnicastPackets;
	}

	public void setSUnicastPackets(String unicastPackets) {
		sUnicastPackets = unicastPackets;
	}

	public String getSupplyVottage() {
		return supplyVottage;
	}

	public void setSupplyVottage(String supplyVottage) {
		this.supplyVottage = supplyVottage;
	}

	public String getTransceiverTemperature() {
		return transceiverTemperature;
	}

	public void setTransceiverTemperature(String transceiverTemperature) {
		this.transceiverTemperature = transceiverTemperature;
	}

	public String getTxPower() {
		return txPower;
	}

	public void setTxPower(String txPower) {
		this.txPower = txPower;
	}

	public String getVoipConnectStatus() {
		return voipConnectStatus;
	}

	public void setVoipConnectStatus(String voipConnectStatus) {
		this.voipConnectStatus = voipConnectStatus;
	}

	public String getVoipConnectType() {
		return voipConnectType;
	}

	public void setVoipConnectType(String voipConnectType) {
		this.voipConnectType = voipConnectType;
	}

	public String getVoipDNSServer() {
		return voipDNSServer;
	}

	public void setVoipDNSServer(String voipDNSServer) {
		this.voipDNSServer = voipDNSServer;
	}

	public String getVoipIPAddress() {
		return voipIPAddress;
	}

	public void setVoipIPAddress(String voipIPAddress) {
		this.voipIPAddress = voipIPAddress;
	}

	public String getVoipRegistStatus() {
		return voipRegistStatus;
	}

	public void setVoipRegistStatus(String voipRegistStatus) {
		this.voipRegistStatus = voipRegistStatus;
	}

	public String getVoipSubnetMask() {
		return voipSubnetMask;
	}

	public void setVoipSubnetMask(String voipSubnetMask) {
		this.voipSubnetMask = voipSubnetMask;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
 
	public String toString()
	{
		return "DiagnosticInfoOBJ: deviceId-" + deviceId;
	}
	
/**
 * 注释 by zhangchy 2012-03-06
 * 新疆需要用这些字段   zhangsm  20120405
 */
	public String getSentBytes() {
		return sentBytes;
	}

	public void setSentBytes(String sendBytes) {
		this.sentBytes = sendBytes;
	}

	public String getIADDiagnosticState() {
		return IADDiagnosticState;
	}

	public void setIADDiagnosticState(String diagnosticState) {
		IADDiagnosticState = diagnosticState;
	}

	
	public String getInternetPVC()
	{
		return internetPVC;
	}

	
	public void setInternetPVC(String internetPVC)
	{
		this.internetPVC = internetPVC;
	}

	
	public String getInternetVLAN()
	{
		return internetVLAN;
	}

	
	public void setInternetVLAN(String internetVLAN)
	{
		this.internetVLAN = internetVLAN;
	}

	
	public String getInternetUsername()
	{
		return internetUsername;
	}

	
	public void setInternetUsername(String internetUsername)
	{
		this.internetUsername = internetUsername;
	}

	
	public String getInternetErrorCode()
	{
		return internetErrorCode;
	}

	
	public void setInternetErrorCode(String internetErrorCode)
	{
		this.internetErrorCode = internetErrorCode;
	}

	
	public String getInternetMAC()
	{
		return internetMAC;
	}

	
	public void setInternetMAC(String internetMAC)
	{
		this.internetMAC = internetMAC;
	}

	
	public String getInternetSession()
	{
		return internetSession;
	}

	
	public void setInternetSession(String internetSession)
	{
		this.internetSession = internetSession;
	}

	
	public String getInternetBindport()
	{
		return internetBindport;
	}

	
	public void setInternetBindport(String internetBindport)
	{
		this.internetBindport = internetBindport;
	}

	
	public String getIptvPVC()
	{
		return iptvPVC;
	}

	
	public void setIptvPVC(String iptvPVC)
	{
		this.iptvPVC = iptvPVC;
	}

	
	public String getIptvVLAN()
	{
		return iptvVLAN;
	}

	
	public void setIptvVLAN(String iptvVLAN)
	{
		this.iptvVLAN = iptvVLAN;
	}

	
	public String getIptvBindport()
	{
		return iptvBindport;
	}

	
	public void setIptvBindport(String iptvBindport)
	{
		this.iptvBindport = iptvBindport;
	}

	
	public String getVoipPVC()
	{
		return voipPVC;
	}

	
	public void setVoipPVC(String voipPVC)
	{
		this.voipPVC = voipPVC;
	}

	
	public String getVoipVLAN()
	{
		return voipVLAN;
	}

	
	public void setVoipVLAN(String voipVLAN)
	{
		this.voipVLAN = voipVLAN;
	}

	
	public String getVoipRegistErrorType()
	{
		return voipRegistErrorType;
	}

	
	public void setVoipRegistErrorType(String voipRegistErrorType)
	{
		this.voipRegistErrorType = voipRegistErrorType;
	}

	
	public String getVoipProtocolType()
	{
		return voipProtocolType;
	}

	
	public void setVoipProtocolType(String voipProtocolType)
	{
		this.voipProtocolType = voipProtocolType;
	}

	
	public String getVoipRegistrarServer()
	{
		return voipRegistrarServer;
	}

	
	public void setVoipRegistrarServer(String voipRegistrarServer)
	{
		this.voipRegistrarServer = voipRegistrarServer;
	}

	
	public String getVoipRegistrarServerPort()
	{
		return voipRegistrarServerPort;
	}

	
	public void setVoipRegistrarServerPort(String voipRegistrarServerPort)
	{
		this.voipRegistrarServerPort = voipRegistrarServerPort;
	}

	
	public String getVoipStandByRegistrarServer()
	{
		return voipStandByRegistrarServer;
	}

	
	public void setVoipStandByRegistrarServer(String voipStandByRegistrarServer)
	{
		this.voipStandByRegistrarServer = voipStandByRegistrarServer;
	}

	
	public String getVoipStandByRegistrarServerPort()
	{
		return voipStandByRegistrarServerPort;
	}

	
	public void setVoipStandByRegistrarServerPort(String voipStandByRegistrarServerPort)
	{
		this.voipStandByRegistrarServerPort = voipStandByRegistrarServerPort;
	}

	
	public String getVoipAuthUserName()
	{
		return voipAuthUserName;
	}

	
	public void setVoipAuthUserName(String voipAuthUserName)
	{
		this.voipAuthUserName = voipAuthUserName;
	}

	
	public String getVoipAuthPassword()
	{
		return voipAuthPassword;
	}

	
	public void setVoipAuthPassword(String voipAuthPassword)
	{
		this.voipAuthPassword = voipAuthPassword;
	}

	
	public String getsUnicastPackets()
	{
		return sUnicastPackets;
	}

	
	public void setsUnicastPackets(String sUnicastPackets)
	{
		this.sUnicastPackets = sUnicastPackets;
	}

	
	public String getrUnicastPackets()
	{
		return rUnicastPackets;
	}

	
	public void setrUnicastPackets(String rUnicastPackets)
	{
		this.rUnicastPackets = rUnicastPackets;
	}

	
	public String getsMulticastPackets()
	{
		return sMulticastPackets;
	}

	
	public void setsMulticastPackets(String sMulticastPackets)
	{
		this.sMulticastPackets = sMulticastPackets;
	}

	
	public String getrMulticastPackets()
	{
		return rMulticastPackets;
	}

	
	public void setrMulticastPackets(String rMulticastPackets)
	{
		this.rMulticastPackets = rMulticastPackets;
	}

	
	public String getsBroadcastPackets()
	{
		return sBroadcastPackets;
	}

	
	public void setsBroadcastPackets(String sBroadcastPackets)
	{
		this.sBroadcastPackets = sBroadcastPackets;
	}

	
	public String getrBroadcastPackets()
	{
		return rBroadcastPackets;
	}

	
	public void setrBroadcastPackets(String rBroadcastPackets)
	{
		this.rBroadcastPackets = rBroadcastPackets;
	}

	
	public String getsPausePackets()
	{
		return sPausePackets;
	}

	
	public void setsPausePackets(String sPausePackets)
	{
		this.sPausePackets = sPausePackets;
	}

	
	public String getrPausePackets()
	{
		return rPausePackets;
	}

	
	public void setrPausePackets(String rPausePackets)
	{
		this.rPausePackets = rPausePackets;
	}

	
	public String getUserId()
	{
		return userId;
	}

	
	public void setUserId(String userId)
	{
		this.userId = userId;
	}
	
}
