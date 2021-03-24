package com.linkage.itms.dispatch.obj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * VXLAN业务检查基类
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-11-28
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public abstract class VXLANBaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(VXLANBaseChecker.class);

	// 客户 userId
	protected long userId;
	// 客户端调用XML字符串
	protected String callXml = "";
	// 调用ID
	protected String cmdId = "";
	// 调用类型：CX_01,固定
	protected String cmdType = "";
	// 调用客户端类型：1：网厅 2：IPOSS 3：综调 4：RADIUS 5:翼翮
	protected int clientType;
	// 查询结果
	protected int result;
	// 查询结果描述
	protected String resultDesc = "";
	// 业务受理时间 例：20170522170000
	protected String dealDate = "";
	// 业务类型
	protected int servTypeId;
	// 操作类型
	protected int operateId;
	// 查询类型: 1：宽带帐号（默认）; 2：Loid ;（其他值按默认处理）
	protected int userInfoType;
	// 查询信息
	protected String userInfo = "";
	// 用户类型:2
	protected int userType;
	// 请求唯一标识
	protected String requestID = "";
	// Vxlan的vni参数 取值范围：1-16777216
	protected int tunnelKey = 0;
	// 隧道对端地址 如：172.16.132.1
	protected String tunnelRemoteIp = "";
	// 配置vxlan虚接口的工作模式 1、二层模式  2、三层模式  3、混合模式
	protected int workMode = 0;
	// VXLAN虚接口的MTU值不超过缺省值,缺省值为1440
	protected int maxMTUSize = 1440;
	// 三层或者混合模式有效，VXLAN虚接口的IP地址，可以是静态配置也可以是动态获取
	protected String iPAddress = "";
	// 三层或者混合模式有效，VXLAN虚接口IP地址的掩码，可以是静态配置也可以是动态获取
	protected String subnetMask = "";
	// 三层或者混合模式有效，地址类型，有 “DHCP” “Static”。缺省值为“DHCP”。
	protected String addressingType = "DHCP";
	// 是否启用NAT，仅三层模式或者混合模式下有效，值为：true/false，缺省为false 1:true 0:false
	protected int nATEnabled = 0;
	// 主用DNS地址 三层或者混合模式有效，静态配置或者通过DHCP方式获取到的主用DNS地址 多VNI情况下，要求只针对上网VNI分配DNS地址
	protected String dNSServers_Master = "";
	// 备用DNS地址 三层或者混合模式有效，静态配置或者通过DHCP方式获取到的备用DNS地址 多VNI情况下，要求只针对上网VNI分配DNS地址
	protected String dNSServers_Slave = "";
	// 三层或者混合模式有效，静态配置或者通过DHCP方式获取到的网关地址 要求网关自动生成一条指向上网VNI出接口的默认路由，原有Internet WAN连接默认路由失效
	protected String defaultGateway = "";
	// 二层或者混合模式下绑定的LAN侧VLAN接口，缺省为0
	protected int xctcom_vlan = 0;
	// VXLANConfig目录下的序列号
	protected int vXLANConfigSequence = 0;
	// 最新绑定的Loid
	protected String loid = "";
	// 其它Loid信息
	protected String loidPrev = "";
	// 宽带账号
	protected String netUsername = "";
	// 绑定设备的device_id
	protected String deviceId = "";
	// vlanid下绑定口
	protected String bindPort = "";
	
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
		return true;
	}
	
	public long getUserId()
	{
		return userId;
	}
	
	public void setUserId(long userId)
	{
		this.userId = userId;
	}
	
	public String getCallXml()
	{
		return callXml;
	}
	
	public void setCallXml(String callXml)
	{
		this.callXml = callXml;
	}
	
	public String getCmdId()
	{
		return cmdId;
	}
	
	public void setCmdId(String cmdId)
	{
		this.cmdId = cmdId;
	}

	public String getCmdType()
	{
		return cmdType;
	}

	public void setCmdType(String cmdType)
	{
		this.cmdType = cmdType;
	}

	public int getClientType()
	{
		return clientType;
	}

	public void setClientType(int clientType)
	{
		this.clientType = clientType;
	}

	public int getResult()
	{
		return result;
	}
	
	public void setResult(int result)
	{
		this.result = result;
	}

	public String getResultDesc()
	{
		return resultDesc;
	}

	public void setResultDesc(String resultDesc)
	{
		this.resultDesc = resultDesc;
	}

	public String getDealDate()
	{
		return dealDate;
	}

	public void setDealDate(String dealDate)
	{
		this.dealDate = dealDate;
	}
	
	public int getvXLANConfigSequence()
	{
		return vXLANConfigSequence;
	}

	public void setvXLANConfigSequence(int vXLANConfigSequence)
	{
		this.vXLANConfigSequence = vXLANConfigSequence;
	}

	public int getServTypeId()
	{
		return servTypeId;
	}

	public void setServTypeId(int servTypeId)
	{
		this.servTypeId = servTypeId;
	}

	public int getOperateId()
	{
		return operateId;
	}

	public void setOperateId(int operateId)
	{
		this.operateId = operateId;
	}

	public int getUserInfoType()
	{
		return userInfoType;
	}

	public void setUserInfoType(int userInfoType)
	{
		this.userInfoType = userInfoType;
	}

	public String getUserInfo()
	{
		return userInfo;
	}

	public void setUserInfo(String userInfo)
	{
		this.userInfo = userInfo;
	}

	public int getUserType()
	{
		return userType;
	}

	public void setUserType(int userType)
	{
		this.userType = userType;
	}

	public String getRequestID()
	{
		return requestID;
	}

	public void setRequestID(String requestID)
	{
		this.requestID = requestID;
	}

	public int getTunnelKey()
	{
		return tunnelKey;
	}

	public void setTunnelKey(int tunnelKey)
	{
		this.tunnelKey = tunnelKey;
	}

	public String getTunnelRemoteIp()
	{
		return tunnelRemoteIp;
	}

	public void setTunnelRemoteIp(String tunnelRemoteIp)
	{
		this.tunnelRemoteIp = tunnelRemoteIp;
	}
	
	public int getWorkMode()
	{
		return workMode;
	}
	
	public void setWorkMode(int workMode)
	{
		this.workMode = workMode;
	}

	public int getMaxMTUSize()
	{
		return maxMTUSize;
	}

	public void setMaxMTUSize(int maxMTUSize)
	{
		this.maxMTUSize = maxMTUSize;
	}
	
	public String getiPAddress()
	{
		return iPAddress;
	}

	public void setiPAddress(String iPAddress)
	{
		this.iPAddress = iPAddress;
	}

	public String getSubnetMask()
	{
		return subnetMask;
	}

	public void setSubnetMask(String subnetMask)
	{
		this.subnetMask = subnetMask;
	}

	public String getAddressingType()
	{
		return addressingType;
	}

	public void setAddressingType(String addressingType)
	{
		this.addressingType = addressingType;
	}
	
	public int getnATEnabled()
	{
		return nATEnabled;
	}
	
	public void setnATEnabled(int nATEnabled)
	{
		this.nATEnabled = nATEnabled;
	}

	public String getdNSServers_Master()
	{
		return dNSServers_Master;
	}
	
	public void setdNSServers_Master(String dNSServers_Master)
	{
		this.dNSServers_Master = dNSServers_Master;
	}
	
	public String getdNSServers_Slave()
	{
		return dNSServers_Slave;
	}

	public void setdNSServers_Slave(String dNSServers_Slave)
	{
		this.dNSServers_Slave = dNSServers_Slave;
	}
	
	public String getDefaultGateway()
	{
		return defaultGateway;
	}
	
	public void setDefaultGateway(String defaultGateway)
	{
		this.defaultGateway = defaultGateway;
	}
	
	public int getXctcom_vlan()
	{
		return xctcom_vlan;
	}

	public void setXctcom_vlan(int xctcom_vlan)
	{
		this.xctcom_vlan = xctcom_vlan;
	}

	public String getLoid()
	{
		return loid;
	}

	public void setLoid(String loid)
	{
		this.loid = loid;
	}
	
	public String getLoidPrev()
	{
		return loidPrev;
	}

	public void setLoidPrev(String loidPrev)
	{
		this.loidPrev = loidPrev;
	}

	public String getNetUsername()
	{
		return netUsername;
	}
	
	public void setNetUsername(String netUsername)
	{
		this.netUsername = netUsername;
	}
	
	public String getDeviceId()
	{
		return deviceId;
	}
	
	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}

	public String getBindPort()
	{
		return bindPort;
	}
	
	public void setBindPort(String bindPort)
	{
		this.bindPort = bindPort;
	}
	
}
