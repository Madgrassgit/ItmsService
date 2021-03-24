package com.linkage.itms.obj;

import java.io.Serializable;

/**
 * 
 * @author fanjm (Ailk No.35572)
 * @version 1.0
 * @since 2017年3月17日
 *
 */
public class Order implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	private String DSLMPort= "";
    private String accessNo= "";
    private String ad_account= "";
    private String ad_password= "";
    private String ad_userid= "";
    private String ad_userpass= "";
    private String area_code= "";
    private String cardKey= "";
    private String cardNo= "";
    private String contact_person= "";
    private String deviceMac= "";
    private String deviceType= "";
    private String device_ID= "";
    private String device_wan= "";
    private String downstreamMaxRate= "";
    private String executeNo= "";
    private String finalorderDocument= "";
    private String home_assist= "";
    private String orderDocument= "";
    private String order_LSH= "";
    private String order_No= "";
    private String order_Remark= "";
    private String order_Time= "";
    private String order_Type= "";
    private String order_kind= "";
    private String order_status= "";
    private String privatePara= "";
    private String protocolType= "";
    private String pvc= "";
    private String replyFlag= "";
    private String replyOrderFlag= "";
    private String replyURL= "";
    private String serviceType= "";
    private String service_code= "";
    private String strIptvAccount= "";
    private String subAreaNum= "";
    private String subArea_code= "";
    private String time_limit= "";
    private String uplink_type= "";
    private String upstreamMaxRate= "";
    private String userBrasIP= "";
    private String userCard= "";
    private String userCustom= "";
    private String userEmail= "";
    private String userPhone= "";
    private String userPort= "";
    private String user_Type= "";
    private String user_address= "";
    private String user_name= "";
    private String vector_argues= "";
    private String ver= "";
    private String vlanID= "";
    
    @Override
	public String toString() {
		return "Order [DSLMPort=" + DSLMPort + ", accessNo=" + accessNo
				+ ", ad_account=" + ad_account + ", ad_password=" + ad_password
				+ ", ad_userid=" + ad_userid + ", ad_userpass=" + ad_userpass
				+ ", area_code=" + area_code + ", cardKey=" + cardKey
				+ ", cardNo=" + cardNo + ", contact_person=" + contact_person
				+ ", deviceMac=" + deviceMac + ", deviceType=" + deviceType
				+ ", device_ID=" + device_ID + ", device_wan=" + device_wan
				+ ", downstreamMaxRate=" + downstreamMaxRate + ", executeNo="
				+ executeNo + ", finalorderDocument=" + finalorderDocument
				+ ", home_assist=" + home_assist + ", orderDocument="
				+ orderDocument + ", order_LSH=" + order_LSH + ", order_No="
				+ order_No + ", order_Remark=" + order_Remark + ", order_Time="
				+ order_Time + ", order_Type=" + order_Type + ", order_kind="
				+ order_kind + ", order_status=" + order_status
				+ ", privatePara=" + privatePara + ", protocolType="
				+ protocolType + ", pvc=" + pvc + ", replyFlag=" + replyFlag
				+ ", replyOrderFlag=" + replyOrderFlag + ", replyURL="
				+ replyURL + ", serviceType=" + serviceType + ", service_code="
				+ service_code + ", strIptvAccount=" + strIptvAccount
				+ ", subAreaNum=" + subAreaNum + ", subArea_code="
				+ subArea_code + ", time_limit=" + time_limit
				+ ", uplink_type=" + uplink_type + ", upstreamMaxRate="
				+ upstreamMaxRate + ", userBrasIP=" + userBrasIP
				+ ", userCard=" + userCard + ", userCustom=" + userCustom
				+ ", userEmail=" + userEmail + ", userPhone=" + userPhone
				+ ", userPort=" + userPort + ", user_Type=" + user_Type
				+ ", user_address=" + user_address + ", user_name=" + user_name
				+ ", vector_argues=" + vector_argues + ", ver=" + ver
				+ ", vlanID=" + vlanID + "]";
	}
	/*解析具体参数以及Itms所需参数begin*/
    /*private String cmdId = "";
    private String authUser = "1";
    private String authPwd = "1";
    private String servTypeId = "";
    private String operateId = "";
    
    //接入工单
    private String officeId = "";
    private String areaId = "";
    private String accessStyle = "";//终端接入类型
    private String linkman = "";
    private String linkPhone = "";
    private String email = "";
    private String mobile = "";
    private String linkAddress = "";
    private String linkmanCredno = "";
    private String customerId = "";
    private String customerAccount = "";
    private String customerPwd = "";
    private String specId = "";
    
    //宽带开户
    private String cityId = "";
    private String dealDate = "";
    private String OltFactory = "";
    private String wband_mode = "";
    private String wband_vlan = "";
    private String wband_name = "";
    private String wband_password = "";
    private String X_CU_LanInterface = "";//表示宽带、Iptv业务与ONT的第几个LAN口绑定
    private String userType = "1";//用户类型，固定传1
    private String wanType = "";//itms的上网方式，1桥接 2路由
    
    //iptv开户
    private String iptv_mode = ""; //0桥接，1路由
    private String X_CU_MulticastVlan = "";//组播vlan
    private String iptv_vlan = "";//单播vlan
    
    private String iptv_name = "";//专属宽带账号
    private String iptv_password = "";//专属宽带密码
    private String destIPAddr1 = "";//静态路由目的地址
    private String destMask1 = "";//静态路由掩码
    //工单模块参数
    private String iptvNum = "";//IPTV个数
    private String speed = "";//用户签约速率
    private String ipaddress = "";
    private String ipmask = "";
    private String gateway = "";
    
    //voip开户
    private String voip_MGCIP = ""; //主用MGC IP
    private String voip_MGCPort = ""; //MGC网关端口号
    private String voip_standbyMGCIP = ""; //备用MGC IP
    private String voip_standbyMGCPort = ""; //备用MGC网关端口号
    private String voip_vlan = ""; //语音业务vlan
    private String voip_MG_Domain = ""; //MG的域名  regId:终端标识：域名，建议配置LOID.com
    private String regIdTypev = ""; //终端标识的类型 0：IP地址，1：域名，2：设备名，当前为1
    private String WANIPAddress = ""; //ont的语音业务地址
    private String SubnetMask = ""; //ont的语音业务地址掩码 
    private String WANDefaultGateway = "";//ont语音业务网关
    private String ipdns = "";//DNS
    private String voip_EID = "";//语音业务EID
    //工单模块传空字段
    private String voipPhone = "";//用户电话号码 加区号
    private String voipPort = "";//标示语音口
    private String deviceName = "";//设备名
    private String dscpMark = "";//H248信令报文的DSCP值
*/    
	public String getDSLMPort() {
		return DSLMPort;
	}
	public String getAccessNo() {
		return accessNo;
	}
	public String getAd_account() {
		return ad_account;
	}
	public String getAd_password() {
		return ad_password;
	}
	public String getAd_userid() {
		return ad_userid;
	}
	public String getAd_userpass() {
		return ad_userpass;
	}
	public String getArea_code() {
		return area_code;
	}
	public String getCardKey() {
		return cardKey;
	}
	public String getCardNo() {
		return cardNo;
	}
	public String getContact_person() {
		return contact_person;
	}
	public String getDeviceMac() {
		return deviceMac;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public String getDevice_ID() {
		return device_ID;
	}
	public String getDevice_wan() {
		return device_wan;
	}
	public String getDownstreamMaxRate() {
		return downstreamMaxRate;
	}
	public String getExecuteNo() {
		return executeNo;
	}
	public String getFinalorderDocument() {
		return finalorderDocument;
	}
	public String getHome_assist() {
		return home_assist;
	}
	public String getOrderDocument() {
		return orderDocument;
	}
	public String getOrder_LSH() {
		return order_LSH;
	}
	public String getOrder_No() {
		return order_No;
	}
	public String getOrder_Remark() {
		return order_Remark;
	}
	public String getOrder_Time() {
		return order_Time;
	}
	public String getOrder_Type() {
		return order_Type;
	}
	public String getOrder_kind() {
		return order_kind;
	}
	public String getOrder_status() {
		return order_status;
	}
	public String getPrivatePara() {
		return privatePara;
	}
	public String getProtocolType() {
		return protocolType;
	}
	public String getPvc() {
		return pvc;
	}
	public String getReplyFlag() {
		return replyFlag;
	}
	public String getReplyOrderFlag() {
		return replyOrderFlag;
	}
	public String getReplyURL() {
		return replyURL;
	}
	public String getServiceType() {
		return serviceType;
	}
	public String getService_code() {
		return service_code;
	}
	public String getStrIptvAccount() {
		return strIptvAccount;
	}
	public String getSubAreaNum() {
		return subAreaNum;
	}
	public String getSubArea_code() {
		return subArea_code;
	}
	public String getTime_limit() {
		return time_limit;
	}
	public String getUplink_type() {
		return uplink_type;
	}
	public String getUpstreamMaxRate() {
		return upstreamMaxRate;
	}
	public String getUserBrasIP() {
		return userBrasIP;
	}
	public String getUserCard() {
		return userCard;
	}
	public String getUserCustom() {
		return userCustom;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public String getUserPhone() {
		return userPhone;
	}
	public String getUserPort() {
		return userPort;
	}
	public String getUser_Type() {
		return user_Type;
	}
	public String getUser_address() {
		return user_address;
	}
	public String getUser_name() {
		return user_name;
	}
	public String getVector_argues() {
		return vector_argues;
	}
	public String getVer() {
		return ver;
	}
	public String getVlanID() {
		return vlanID;
	}
	public void setDSLMPort(String dSLMPort) {
		DSLMPort = dSLMPort;
	}
	public void setAccessNo(String accessNo) {
		this.accessNo = accessNo;
	}
	public void setAd_account(String ad_account) {
		this.ad_account = ad_account;
	}
	public void setAd_password(String ad_password) {
		this.ad_password = ad_password;
	}
	public void setAd_userid(String ad_userid) {
		this.ad_userid = ad_userid;
	}
	public void setAd_userpass(String ad_userpass) {
		this.ad_userpass = ad_userpass;
	}
	public void setArea_code(String area_code) {
		this.area_code = area_code;
	}
	public void setCardKey(String cardKey) {
		this.cardKey = cardKey;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public void setContact_person(String contact_person) {
		this.contact_person = contact_person;
	}
	public void setDeviceMac(String deviceMac) {
		this.deviceMac = deviceMac;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public void setDevice_ID(String device_ID) {
		this.device_ID = device_ID;
	}
	public void setDevice_wan(String device_wan) {
		this.device_wan = device_wan;
	}
	public void setDownstreamMaxRate(String downstreamMaxRate) {
		this.downstreamMaxRate = downstreamMaxRate;
	}
	public void setExecuteNo(String executeNo) {
		this.executeNo = executeNo;
	}
	public void setFinalorderDocument(String finalorderDocument) {
		this.finalorderDocument = finalorderDocument;
	}
	public void setHome_assist(String home_assist) {
		this.home_assist = home_assist;
	}
	public void setOrderDocument(String orderDocument) {
		this.orderDocument = orderDocument;
	}
	public void setOrder_LSH(String order_LSH) {
		this.order_LSH = order_LSH;
	}
	public void setOrder_No(String order_No) {
		this.order_No = order_No;
	}
	public void setOrder_Remark(String order_Remark) {
		this.order_Remark = order_Remark;
	}
	public void setOrder_Time(String order_Time) {
		this.order_Time = order_Time;
	}
	public void setOrder_Type(String order_Type) {
		this.order_Type = order_Type;
	}
	public void setOrder_kind(String order_kind) {
		this.order_kind = order_kind;
	}
	public void setOrder_status(String order_status) {
		this.order_status = order_status;
	}
	public void setPrivatePara(String privatePara) {
		this.privatePara = privatePara;
	}
	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}
	public void setPvc(String pvc) {
		this.pvc = pvc;
	}
	public void setReplyFlag(String replyFlag) {
		this.replyFlag = replyFlag;
	}
	public void setReplyOrderFlag(String replyOrderFlag) {
		this.replyOrderFlag = replyOrderFlag;
	}
	public void setReplyURL(String replyURL) {
		this.replyURL = replyURL;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	public void setService_code(String service_code) {
		this.service_code = service_code;
	}
	public void setStrIptvAccount(String strIptvAccount) {
		this.strIptvAccount = strIptvAccount;
	}
	public void setSubAreaNum(String subAreaNum) {
		this.subAreaNum = subAreaNum;
	}
	public void setSubArea_code(String subArea_code) {
		this.subArea_code = subArea_code;
	}
	public void setTime_limit(String time_limit) {
		this.time_limit = time_limit;
	}
	public void setUplink_type(String uplink_type) {
		this.uplink_type = uplink_type;
	}
	public void setUpstreamMaxRate(String upstreamMaxRate) {
		this.upstreamMaxRate = upstreamMaxRate;
	}
	public void setUserBrasIP(String userBrasIP) {
		this.userBrasIP = userBrasIP;
	}
	public void setUserCard(String userCard) {
		this.userCard = userCard;
	}
	public void setUserCustom(String userCustom) {
		this.userCustom = userCustom;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}
	public void setUserPort(String userPort) {
		this.userPort = userPort;
	}
	public void setUser_Type(String user_Type) {
		this.user_Type = user_Type;
	}
	public void setUser_address(String user_address) {
		this.user_address = user_address;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public void setVector_argues(String vector_argues) {
		this.vector_argues = vector_argues;
	}
	public void setVer(String ver) {
		this.ver = ver;
	}
	public void setVlanID(String vlanID) {
		this.vlanID = vlanID;
	}
	
    
}
