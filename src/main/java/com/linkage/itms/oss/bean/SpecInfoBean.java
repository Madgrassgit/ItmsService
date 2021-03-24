
package com.linkage.itms.oss.bean;

/**
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-4-2
 * @category com.linkage.itms.oss.bean
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class SpecInfoBean
{

	/**
	 * 接口调用唯一ID,每次调用此值不可重复
	 */
	private String cmdId;
	/**
	 * 接口类型,CX_01,固定
	 */
	private String cmdType;
	/**
	 * 客户端类型 1：BSS 2：IPOSS 3：综调 4：RADIUS 5：OSS
	 */
	private String clientType;
	/**
	 * 查询类型,1：LOID 2：用户宽带帐号 3：IPTV宽带帐号 4：VOIP业务电话号码 5：VOIP认证帐号
	 */
	private String userInfoType;
	/**
	 * 查询用户账号,用户信息类型所对应的用户信息，如LOID值， 250000000000000000
	 */
	private String username;
	/**
	 * 属地代码
	 */
	private String cityId;

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

	public String getClientType()
	{
		return clientType;
	}

	public void setClientType(String clientType)
	{
		this.clientType = clientType;
	}

	public String getUserInfoType()
	{
		return userInfoType;
	}

	public void setUserInfoType(String userInfoType)
	{
		this.userInfoType = userInfoType;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getCityId()
	{
		return cityId;
	}

	public void setCityId(String cityId)
	{
		this.cityId = cityId;
	}
}
