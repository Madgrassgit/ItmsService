package com.linkage.itms.dispatch.obj;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 接口数据检查基类(抽象类)
 * 
 */
public abstract class CloudBaseChecker {

	private static Logger logger = LoggerFactory
			.getLogger(CloudBaseChecker.class);

	// IP正则表达式
//	private static String ipPattern = "(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";

	// 正则，字符加数字
	static Pattern pattern = Pattern.compile("\\w{1,}+");

	// 正则，mac地址
	static Pattern patternMac = Pattern
			.compile("[A-F\\d]{2}:[A-F\\d]{2}:[A-F\\d]{2}:[A-F\\d]{2}:[A-F\\d]{2}:[A-F\\d]{2}");
	protected String callXml;				// 客户端调用XML字符串
	protected String cmdId;					// 调用ID
	protected String cmdType;				// 调用类型：CX_01,固定
	protected int clientType;				// 调用客户端类型：1：BSS 2：IPOSS 3：综调 4：RADIUS 5:掌上运维
	protected int userInfoType;				// 用户信息类型 1：用户宽带帐号 2：逻辑SN号 3：IPTV宽带帐号 4：VOIP业务电话号码 5：VOIP认证帐号
	protected String userInfo;				// 用户信息
	protected int result;					// 查询结果
	protected String resultDesc = "";		// 查询结果描述
	protected String dealDate = "";			// 业务受理时间
	protected String deviceSN = "";			// 终端信息
	protected String loid = "";
	protected String loidPrev = "";
	protected String deviceType = "";
	protected String deviceVendor = "";
	protected String deviceModel = "";
	protected String softwareversion = "";
	protected String hardwareversion = "";
	protected String isNet = "";
	protected String isIpsecVPN = "";
	protected String wanType = "";
	protected String ipAddr = "";
	protected String ipType = "";
	protected String online = "";
	protected String status = "";
	protected String ipsecStatus = "";
	// 属地ID
	protected String cityId;
	protected String servIpAddr = "";
	protected String ipescVpnStatus = "";
	protected String onlineStatus = "";
	/**
	 * Ping诊断接口
	 */
	protected String wanPassageWay = "";		// Wan通道
	protected String packageByte = "";			// 包大小（Byte）
	protected String ipOrDomainName = "";		// 测试IP或域名
	protected String packageNum = "";			// 包数目
	protected String timeOut = "";				// 超时时间（ms）
	protected String succesNum = "";			// 成功数
	protected String failNum = "";				// 失败数
	protected String avgResponseTime = "";		// 平均响应时间
	protected String minResponseTime = "";		// 最小响应时间
	protected String maxResponseTime = "";		// 最大响应时间
	protected String packetLossRate = "";		// 丢包率

	/**
	 * 业务下发结果查询接口
	 */
	protected String bssStats = "";				// Ipsec vpn业务下发结果
	
	/**
	 * 业务下发接口
	 */
	protected String serviceDoneStats = "";		// 业务下发结果
	
	/**
	 * 网关重启接口
	 */
	protected String rebootStats = "";			// 重启结果
	
	/**
	 * IPSecVPN配置参数查询接口
	 */
	protected String localSubnet = ""; 					// 本端子网
	protected String remoteSubnet = ""; 				// 政企网关	对端子网
	protected String remoteDomain = ""; 				// Site-to-Site模式下对端域名
	protected String iPSecOutInterface = ""; 			//Ipsec封装后报文的出接口，默认出接口是默认路由的出接口，可不配置
	protected String iPSecEncapsulationMode = ""; 		//Ipsec封装模式（Tunnel/Transport）缺省为Tunnel
	protected String ipSecType = ""; 					// IPSec 类型
	protected String remoteIP = ""; 					// Site-to-Site 模式下对端IP 地址
	protected String exchangeMode = ""; 				// IKE 协商方式
	protected String ikeAuthenticationAlgorithm = ""; 	// IKE认证算法
	protected String ikeAuthenticationMethod = ""; 		// IKE 验证方法
	protected String ikeEncryptionAlgorithm = ""; 		// IKE 加密算法
	protected String ikeDHGroup = ""; 					// IKE DH 组
	protected String ikeIDType = ""; 					// IKE 身份类型
	protected String ikeLocalName = ""; 				// IKE 本端名称
	protected String ikeRemoteName = ""; 				// IKE 对端名称
	protected String ikePreshareKey = ""; 				// IKE 预共享密钥
	protected String ipSecTransform = ""; 				// IPSec 安全协议
	protected String espAuthenticationAlgorithm = ""; 	// IPsec 验证算法
	protected String espEncryptionAlgorithm = ""; 		// IPsec 加密算法
	protected String ipSecPFS = ""; 					// IPSec DH 组
	protected String ikeSAPeriod = ""; 					// 设置IKE SA 生命周期
	protected String ipSecSATimePeriod = ""; 			// 设置IPsec SA 时间生命周期
	protected String ipSecSATrafficPeriod = ""; 		// 设置IPsec SA 流量生命周期
	protected String ahAuthenticationAlgorithm = ""; 	// AH 验证算法
	
	/**
	 * 接口是否需要回调0:不回调   1:回调
	 */
	protected String callBack = "";
	protected String callBackUrl = "";
	protected String proInstId = "";
	protected String isAsynchronous = "";
	/**
	 * 检查客户端的XML字符串是否合法，如果合法将字符串转换成对象的属性
	 * 
	 * @param
	 * @return boolean
	 */
	public abstract boolean check();

	/**
	 * 返回调用结果
	 * 
	 * @param
	 * @return String
	 */
	public abstract String getReturnXml();

	public boolean baseCheck() {
		logger.debug("baseCheck()");

		if (StringUtil.IsEmpty(cmdId)) {
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}

		if (1 != clientType && 2 != clientType && 3 != clientType
				&& 4 != clientType && 5 != clientType) {
			result = 1;
			resultDesc = "无此业务类型";
			return false;
		}

		if (false == "CX_01".equals(cmdType)) {
			result = 1001;
			resultDesc = "接口类型非法";
			return false;
		}

		return true;
	}

	/**
	 * 查询方式合法性检查
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
//	boolean searchTypeCheck() {
//		if (1 != searchType && 2 != searchType) {
//			result = 1001;
//			resultDesc = "查询类型非法";
//			return false;
//		}
//		return true;
//	}

	/**
	 * 用户信息类型合法性检查
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	boolean userInfoTypeCheck() {
		if (1 != userInfoType && 2 != userInfoType) {
			result = 2;
			resultDesc = "无此操作类型";
			return false;
		}
		return true;
	}

	/**
	 * 用户信息合法性检查
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	protected boolean userInfoCheck() {
		if (StringUtil.IsEmpty(userInfo)) {
			result = 4;
			resultDesc = "账号不合法";
			return false;
		}
		return true;
	}

	/**
	 * add by zhangchy 2012-02-08
	 * 
	 * 新疆 根据用户LOID获取用户业务放装情况 对入参LOID(FTTH用户逻辑标识)的合法性做判断 FTTH用户逻辑标识 以大写英文字母C结尾
	 * 
	 */
	boolean loidCheck() {
		if (StringUtil.IsEmpty(loid) || !loid.endsWith("C")) {
			result = 1002;
			resultDesc = "用户信息不合法";
			return false;
		}
		return true;
	}


	/** getter, setter methods */

	public String getCallXml() {
		return callXml;
	}

	public void setCallXml(String callXml) {
		this.callXml = callXml;
	}

	public String getCmdId() {
		return cmdId;
	}

	public void setCmdId(String cmdId) {
		this.cmdId = cmdId;
	}

	public String getCmdType() {
		return cmdType;
	}

	public void setCmdType(String cmdType) {
		this.cmdType = cmdType;
	}

	public int getClientType() {
		return clientType;
	}

	public void setClientType(int clientType) {
		this.clientType = clientType;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getResultDesc() {
		return resultDesc;
	}

	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}

	public int getUserInfoType() {
		return userInfoType;
	}

	public void setUserInfoType(int userInfoType) {
		this.userInfoType = userInfoType;
	}

	public String getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}

	public String getLoid() {
		return loid;
	}

	public void setLoid(String loid) {
		this.loid = loid;
	}

	public String getDeviceSN() {
		return deviceSN;
	}

	public void setDeviceSN(String deviceSN) {
		this.deviceSN = deviceSN;
	}

	public String getLoidPrev() {
		return loidPrev;
	}

	public void setLoidPrev(String loidPrev) {
		this.loidPrev = loidPrev;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceVendor() {
		return deviceVendor;
	}

	public void setDeviceVendor(String deviceVendor) {
		this.deviceVendor = deviceVendor;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public String getSoftwareversion() {
		return softwareversion;
	}

	public void setSoftwareversion(String softwareversion) {
		this.softwareversion = softwareversion;
	}

	public String getHardwareversion() {
		return hardwareversion;
	}

	public void setHardwareversion(String hardwareversion) {
		this.hardwareversion = hardwareversion;
	}

	public String getIsNet() {
		return isNet;
	}

	public void setIsNet(String isNet) {
		this.isNet = isNet;
	}

	public String getIsIpsecVPN() {
		return isIpsecVPN;
	}

	public void setIsIpsecVPN(String isIpsecVPN) {
		this.isIpsecVPN = isIpsecVPN;
	}

	public String getWanType() {
		return wanType;
	}

	public void setWanType(String wanType) {
		this.wanType = wanType;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public String getIpType() {
		return ipType;
	}

	public void setIpType(String ipType) {
		this.ipType = ipType;
	}

	public String getOnline() {
		return online;
	}

	public void setOnline(String online) {
		this.online = online;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIpsecStatus() {
		return ipsecStatus;
	}

	public void setIpsecStatus(String ipsecStatus) {
		this.ipsecStatus = ipsecStatus;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getServIpAddr() {
		return servIpAddr;
	}

	public void setServIpAddr(String servIpAddr) {
		this.servIpAddr = servIpAddr;
	}

	public String getIpescVpnStatus() {
		return ipescVpnStatus;
	}

	public void setIpescVpnStatus(String ipescVpnStatus) {
		this.ipescVpnStatus = ipescVpnStatus;
	}

	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public String getWanPassageWay() {
		return wanPassageWay;
	}

	public void setWanPassageWay(String wanPassageWay) {
		this.wanPassageWay = wanPassageWay;
	}

	public String getPackageByte() {
		return packageByte;
	}

	public void setPackageByte(String packageByte) {
		this.packageByte = packageByte;
	}

	public String getIpOrDomainName() {
		return ipOrDomainName;
	}

	public void setIpOrDomainName(String ipOrDomainName) {
		this.ipOrDomainName = ipOrDomainName;
	}

	public String getPackageNum() {
		return packageNum;
	}

	public void setPackageNum(String packageNum) {
		this.packageNum = packageNum;
	}

	public String getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(String timeOut) {
		this.timeOut = timeOut;
	}

	public String getDealDate() {
		return dealDate;
	}

	public void setDealDate(String dealDate) {
		this.dealDate = dealDate;
	}

	public String getSuccesNum() {
		return succesNum;
	}

	public void setSuccesNum(String succesNum) {
		this.succesNum = succesNum;
	}

	public String getFailNum() {
		return failNum;
	}

	public void setFailNum(String failNum) {
		this.failNum = failNum;
	}

	public String getAvgResponseTime() {
		return avgResponseTime;
	}

	public void setAvgResponseTime(String avgResponseTime) {
		this.avgResponseTime = avgResponseTime;
	}

	public String getMinResponseTime() {
		return minResponseTime;
	}

	public void setMinResponseTime(String minResponseTime) {
		this.minResponseTime = minResponseTime;
	}

	public String getMaxResponseTime() {
		return maxResponseTime;
	}

	public void setMaxResponseTime(String maxResponseTime) {
		this.maxResponseTime = maxResponseTime;
	}

	public String getPacketLossRate() {
		return packetLossRate;
	}

	public void setPacketLossRate(String packetLossRate) {
		this.packetLossRate = packetLossRate;
	}

	public String getBssStats() {
		return bssStats;
	}

	public void setBssStats(String bssStats) {
		this.bssStats = bssStats;
	}

	public String getServiceDoneStats() {
		return serviceDoneStats;
	}

	public void setServiceDoneStats(String serviceDoneStats) {
		this.serviceDoneStats = serviceDoneStats;
	}

	public String getRebootStats() {
		return rebootStats;
	}

	public void setRebootStats(String rebootStats) {
		this.rebootStats = rebootStats;
	}

	public String getIpSecType() {
		return ipSecType;
	}

	public void setIpSecType(String ipSecType) {
		this.ipSecType = ipSecType;
	}

	public String getRemoteIP() {
		return remoteIP;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	public String getExchangeMode() {
		return exchangeMode;
	}

	public void setExchangeMode(String exchangeMode) {
		this.exchangeMode = exchangeMode;
	}

	public String getIkeAuthenticationAlgorithm() {
		return ikeAuthenticationAlgorithm;
	}

	public void setIkeAuthenticationAlgorithm(String ikeAuthenticationAlgorithm) {
		this.ikeAuthenticationAlgorithm = ikeAuthenticationAlgorithm;
	}

	public String getIkeAuthenticationMethod() {
		return ikeAuthenticationMethod;
	}

	public void setIkeAuthenticationMethod(String ikeAuthenticationMethod) {
		this.ikeAuthenticationMethod = ikeAuthenticationMethod;
	}

	public String getIkeEncryptionAlgorithm() {
		return ikeEncryptionAlgorithm;
	}

	public void setIkeEncryptionAlgorithm(String ikeEncryptionAlgorithm) {
		this.ikeEncryptionAlgorithm = ikeEncryptionAlgorithm;
	}

	public String getIkeDHGroup() {
		return ikeDHGroup;
	}

	public void setIkeDHGroup(String ikeDHGroup) {
		this.ikeDHGroup = ikeDHGroup;
	}

	public String getIkeIDType() {
		return ikeIDType;
	}

	public void setIkeIDType(String ikeIDType) {
		this.ikeIDType = ikeIDType;
	}

	public String getIkeLocalName() {
		return ikeLocalName;
	}

	public void setIkeLocalName(String ikeLocalName) {
		this.ikeLocalName = ikeLocalName;
	}

	public String getIkeRemoteName() {
		return ikeRemoteName;
	}

	public void setIkeRemoteName(String ikeRemoteName) {
		this.ikeRemoteName = ikeRemoteName;
	}

	public String getIkePreshareKey() {
		return ikePreshareKey;
	}

	public void setIkePreshareKey(String ikePreshareKey) {
		this.ikePreshareKey = ikePreshareKey;
	}

	public String getIpSecTransform() {
		return ipSecTransform;
	}

	public void setIpSecTransform(String ipSecTransform) {
		this.ipSecTransform = ipSecTransform;
	}

	public String getEspAuthenticationAlgorithm() {
		return espAuthenticationAlgorithm;
	}

	public void setEspAuthenticationAlgorithm(String espAuthenticationAlgorithm) {
		this.espAuthenticationAlgorithm = espAuthenticationAlgorithm;
	}

	public String getEspEncryptionAlgorithm() {
		return espEncryptionAlgorithm;
	}

	public void setEspEncryptionAlgorithm(String espEncryptionAlgorithm) {
		this.espEncryptionAlgorithm = espEncryptionAlgorithm;
	}

	public String getIpSecPFS() {
		return ipSecPFS;
	}

	public void setIpSecPFS(String ipSecPFS) {
		this.ipSecPFS = ipSecPFS;
	}

	public String getIkeSAPeriod() {
		return ikeSAPeriod;
	}

	public void setIkeSAPeriod(String ikeSAPeriod) {
		this.ikeSAPeriod = ikeSAPeriod;
	}

	public String getIpSecSATimePeriod() {
		return ipSecSATimePeriod;
	}

	public void setIpSecSATimePeriod(String ipSecSATimePeriod) {
		this.ipSecSATimePeriod = ipSecSATimePeriod;
	}

	public String getIpSecSATrafficPeriod() {
		return ipSecSATrafficPeriod;
	}

	public void setIpSecSATrafficPeriod(String ipSecSATrafficPeriod) {
		this.ipSecSATrafficPeriod = ipSecSATrafficPeriod;
	}

	public String getAhAuthenticationAlgorithm() {
		return ahAuthenticationAlgorithm;
	}

	public void setAhAuthenticationAlgorithm(String ahAuthenticationAlgorithm) {
		this.ahAuthenticationAlgorithm = ahAuthenticationAlgorithm;
	}

	public String getRemoteSubnet() {
		return remoteSubnet;
	}

	public void setRemoteSubnet(String remoteSubnet) {
		this.remoteSubnet = remoteSubnet;
	}

	public String getLocalSubnet() {
		return localSubnet;
	}

	public void setLocalSubnet(String localSubnet) {
		this.localSubnet = localSubnet;
	}

	public String getRemoteDomain() {
		return remoteDomain;
	}

	public void setRemoteDomain(String remoteDomain) {
		this.remoteDomain = remoteDomain;
	}

	public String getiPSecOutInterface() {
		return iPSecOutInterface;
	}

	public void setiPSecOutInterface(String iPSecOutInterface) {
		this.iPSecOutInterface = iPSecOutInterface;
	}

	public String getiPSecEncapsulationMode() {
		return iPSecEncapsulationMode;
	}

	public void setiPSecEncapsulationMode(String iPSecEncapsulationMode) {
		this.iPSecEncapsulationMode = iPSecEncapsulationMode;
	}

	public String getCallBack() {
		return callBack;
	}

	public void setCallBack(String callBack) {
		this.callBack = callBack;
	}

	public String getProInstId() {
		return proInstId;
	}

	public void setProInstId(String proInstId) {
		this.proInstId = proInstId;
	}

	public String getCallBackUrl() {
		return callBackUrl;
	}

	public void setCallBackUrl(String callBackUrl) {
		this.callBackUrl = callBackUrl;
	}

	public String getIsAsynchronous() {
		return isAsynchronous;
	}

	public void setIsAsynchronous(String isAsynchronous) {
		this.isAsynchronous = isAsynchronous;
	}
}
