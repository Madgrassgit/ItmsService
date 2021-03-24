package com.linkage.itms.dispatch.obj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 接口数据检查基类(抽象类)
 * 
 * @author chenxj(75081)
 * @date 2017-10-19
 */
public abstract class IpsecBaseChecker {

	private static Logger logger = LoggerFactory.getLogger(IpsecBaseChecker.class);

	// 客户 userId
	protected long userId;
	// 客户端调用XML字符串
	protected String callXml;
	// 调用ID
	protected String cmdId;
	// 调用类型：CX_01,固定
	protected String cmdType;
	// 调用客户端类型：1：网厅 2：IPOSS 3：综调 4：RADIUS 5:翼翮
	protected int clientType;
	// 查询结果
	protected int result;
	// 查询结果描述
	protected String resultDesc;
	// 业务受理时间 例：20170522170000
	protected String dealDate;
	// 业务类型
	protected int servTypeId;
	// 操作类型
	protected int operateId;
	// 查询类型: 1：宽带帐号（默认）; 2：Loid ;（其他值按默认处理）
	protected int userInfoType;
	// 查询信息
	protected String userInfo;
	// 用户类型:2
	protected int userType;
	// 请求唯一标识
	protected String requestID;
	// 类型: （Site-to-Site/PC-to-Site），缺省为Site-to-Site
	protected String iPSecType;
	// 模式下对端IP地址 : Site-to-Site 模式下对端IP 地址
	protected String remoteIP;
	// 协商方式 : Main 或者Aggressive，默认设置为Main。
	protected String exchangeMode;
	// IKE 认证算法 : IKE 验证算法（MD5 / SHA1），缺省为SHA1
	protected String iKEAuthenticationAlgorithm;
	// IKE 验证方法 : IKE 验证方法 （设置为PreShareKey/RsaSignature），缺省为PreShareKey 。
	protected String iKEAuthenticationMethod;
	// IKE 加密算法: （DES/3DES /AES128 /AES192/AES256）缺省为DES
	protected String iKEEncryptionAlgorithm;
	// IKE DH组: （ Group1/ Group2/Group5/ Group14），缺省为Group1。
	protected String iKEDHGroup="Group1";
	// IKE 身份类型: （IP/Name），缺省为IP。
	protected String iKEIDType;
	// IKE 本端名称
	protected String iKELocalName;
	// IKE 对端名称
	protected String iKERemoteName;
	// IKE 预共享密钥: 128 8～128 个字符。无缺省值。
	protected String iKEPreshareKey;
	// IPSec 安全协议: （ AH/ESP/AH-ESP），缺省为ESP。
	protected String iPSecTransform;
	// IPsec 验证算法: None/MD5/SHA1），缺省为MD5
	protected String eSPAuthenticationAlgorithm;
	// IPsec 加密算法: (DES/3DES/AES128/AES192/AES256)，缺省为3DES。
	protected String eSPEncryptionAlgorithm;
	// IPSec DH组: （None/Group1/Group2/Group5/Group14）， 缺省为None。
	protected String iPSecPFS="None";
	// 设置IKE SA 生命周期(秒)
	protected int iKESAPeriod;
	// 设置IPsec SA 时间生命周期(秒)
	protected int iPSecSATimePeriod;
	// 设置IPsec SA 流量生命周期
	protected int iPSecSATrafficPeriod;
	// AH 验证算法: （MD5/SHA1），缺省为MD5
	protected String aHAuthenticationAlgorithm;
	// 最新绑定的Loid
	protected String loid;
	// 其它Loid信息
	protected String loidPrev;
	// 宽带账号
	protected String netUsername;
	// 对端子网,例如:192.168.1.0/24
	protected String remoteSubnet;
	// 对端域名
	protected String remoteDomain;
	public String getRemoteDomain() {
		return remoteDomain;
	}

	public void setRemoteDomain(String remoteDomain) {
		this.remoteDomain = remoteDomain;
	}

	// 本地子网,例如：192.168.1.0/24
	protected String localSubnet;
	// IPSec封装模式,(Tunnel/Transport)缺省为Tunnel
	protected String iPSecEncapsulationMode="Tunnel";
	// DPD使能,默认0
	protected int dPDEnable;
	// DPD空闲时间,10-3600秒,默认10秒
	protected int dPDThreshold=10;
	// 未收到DPD响应,再次尝试时间间隔,2~10秒，默认5秒
	protected int dPDRetry=5;
	// 绑定设备的device_id
	protected String deviceId;

	/**
	 * 检查客户端的XML字符串是否合法，如果合法将字符串转换成对象的属性
	 */
	public abstract boolean check();

	/**
	 * 返回调用结果
	 */
	public abstract String getReturnXml();

	public boolean baseCheck() {
		logger.debug("baseCheck()");

		if (StringUtil.IsEmpty(cmdId)) {
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		if (false == "CX_01".equals(cmdType)) {
			result = 1001;
			resultDesc = "接口类型非法";
			return false;
		}

		if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType && 5 != clientType) {
			result = 1;
			resultDesc = "无此业务类型";
			return false;
		}
		
		if (1 != userInfoType && 2 != userInfoType) {
			result = 2;
			resultDesc = "无此操作类型";
			return false;
		}

		if (StringUtil.IsEmpty(userInfo)) {
			result = 4;
			resultDesc = "账号不合法";
			return false;
		}

		/*if (2 != userType) {
			result = 1000;
			resultDesc = "用户类型不正确";
			return false;
		}*/
		
		return true;
	}
	
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

	public String getDealDate() {
		return dealDate;
	}

	public void setDealDate(String dealDate) {
		this.dealDate = dealDate;
	}

	public int getServTypeId() {
		return servTypeId;
	}

	public void setServTypeId(int servTypeId) {
		this.servTypeId = servTypeId;
	}

	public int getOperateId() {
		return operateId;
	}

	public void setOperateId(int operateId) {
		this.operateId = operateId;
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

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public String getRequestID() {
		return requestID;
	}

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	public String getiPSecType() {
		return iPSecType;
	}

	public void setiPSecType(String iPSecType) {
		this.iPSecType = iPSecType;
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

	public String getiKEAuthenticationAlgorithm() {
		return iKEAuthenticationAlgorithm;
	}

	public void setiKEAuthenticationAlgorithm(String iKEAuthenticationAlgorithm) {
		this.iKEAuthenticationAlgorithm = iKEAuthenticationAlgorithm;
	}

	public String getiKEAuthenticationMethod() {
		return iKEAuthenticationMethod;
	}

	public void setiKEAuthenticationMethod(String iKEAuthenticationMethod) {
		this.iKEAuthenticationMethod = iKEAuthenticationMethod;
	}

	public String getiKEEncryptionAlgorithm() {
		return iKEEncryptionAlgorithm;
	}

	public void setiKEEncryptionAlgorithm(String iKEEncryptionAlgorithm) {
		this.iKEEncryptionAlgorithm = iKEEncryptionAlgorithm;
	}

	public String getiKEDHGroup() {
		return iKEDHGroup;
	}

	public void setiKEDHGroup(String iKEDHGroup) {
		this.iKEDHGroup = iKEDHGroup;
	}

	public String getiKEIDType() {
		return iKEIDType;
	}

	public void setiKEIDType(String iKEIDType) {
		this.iKEIDType = iKEIDType;
	}

	public String getiKELocalName() {
		return iKELocalName;
	}

	public void setiKELocalName(String iKELocalName) {
		this.iKELocalName = iKELocalName;
	}

	public String getiKERemoteName() {
		return iKERemoteName;
	}

	public void setiKERemoteName(String iKERemoteName) {
		this.iKERemoteName = iKERemoteName;
	}

	public String getiKEPreshareKey() {
		return iKEPreshareKey;
	}

	public void setiKEPreshareKey(String iKEPreshareKey) {
		this.iKEPreshareKey = iKEPreshareKey;
	}

	public String getiPSecTransform() {
		return iPSecTransform;
	}

	public void setiPSecTransform(String iPSecTransform) {
		this.iPSecTransform = iPSecTransform;
	}

	public String geteSPAuthenticationAlgorithm() {
		return eSPAuthenticationAlgorithm;
	}

	public void seteSPAuthenticationAlgorithm(String eSPAuthenticationAlgorithm) {
		this.eSPAuthenticationAlgorithm = eSPAuthenticationAlgorithm;
	}

	public String geteSPEncryptionAlgorithm() {
		return eSPEncryptionAlgorithm;
	}

	public void seteSPEncryptionAlgorithm(String eSPEncryptionAlgorithm) {
		this.eSPEncryptionAlgorithm = eSPEncryptionAlgorithm;
	}

	public String getiPSecPFS() {
		return iPSecPFS;
	}

	public void setiPSecPFS(String iPSecPFS) {
		this.iPSecPFS = iPSecPFS;
	}

	public int getiKESAPeriod() {
		return iKESAPeriod;
	}

	public void setiKESAPeriod(int iKESAPeriod) {
		this.iKESAPeriod = iKESAPeriod;
	}

	public int getiPSecSATimePeriod() {
		return iPSecSATimePeriod;
	}

	public void setiPSecSATimePeriod(int iPSecSATimePeriod) {
		this.iPSecSATimePeriod = iPSecSATimePeriod;
	}

	public int getiPSecSATrafficPeriod() {
		return iPSecSATrafficPeriod;
	}

	public void setiPSecSATrafficPeriod(int iPSecSATrafficPeriod) {
		this.iPSecSATrafficPeriod = iPSecSATrafficPeriod;
	}

	public String getaHAuthenticationAlgorithm() {
		return aHAuthenticationAlgorithm;
	}

	public void setaHAuthenticationAlgorithm(String aHAuthenticationAlgorithm) {
		this.aHAuthenticationAlgorithm = aHAuthenticationAlgorithm;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getLoid() {
		return loid;
	}

	public void setLoid(String loid) {
		this.loid = loid;
	}

	public String getLoidPrev() {
		return loidPrev;
	}

	public void setLoidPrev(String loidPrev) {
		this.loidPrev = loidPrev;
	}

	public String getNetUsername() {
		return netUsername;
	}

	public void setNetUsername(String netUsername) {
		this.netUsername = netUsername;
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

	public String getiPSecEncapsulationMode() {
		return iPSecEncapsulationMode;
	}

	public void setiPSecEncapsulationMode(String iPSecEncapsulationMode) {
		this.iPSecEncapsulationMode = iPSecEncapsulationMode;
	}

	public int getdPDEnable() {
		return dPDEnable;
	}

	public void setdPDEnable(int dPDEnable) {
		this.dPDEnable = dPDEnable;
	}

	public int getdPDThreshold() {
		return dPDThreshold;
	}

	public void setdPDThreshold(int dPDThreshold) {
		this.dPDThreshold = dPDThreshold;
	}

	public int getdPDRetry() {
		return dPDRetry;
	}

	public void setdPDRetry(int dPDRetry) {
		this.dPDRetry = dPDRetry;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
}