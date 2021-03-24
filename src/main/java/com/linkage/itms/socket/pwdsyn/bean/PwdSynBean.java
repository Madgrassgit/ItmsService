
package com.linkage.itms.socket.pwdsyn.bean;

/**
 * 密码同步javaBean
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-3-6
 * @category com.linkage.itms.socket.pwdsyn.bean
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class PwdSynBean
{

	/**
	 * 唯一序列号
	 */
	private String serialNo;
	/**
	 * 用户账号，对应hgwcust_serv_info.username
	 */
	private String account;
	/**
	 * 用户密码，解密后保存的密码，对应hgwcust_serv_info.passwd
	 */
	private String userPwd;
	/**
	 * 时间戳，单位秒
	 */
	private long timestamp;

	public String getSerialNo()
	{
		return serialNo;
	}

	public void setSerialNo(String serialNo)
	{
		this.serialNo = serialNo;
	}

	public String getAccount()
	{
		return account;
	}

	public void setAccount(String account)
	{
		this.account = account;
	}

	public String getUserPwd()
	{
		return userPwd;
	}

	public void setUserPwd(String userPwd)
	{
		this.userPwd = userPwd;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("PwdSynBean [serialNo=");
		builder.append(serialNo);
		builder.append(", account=");
		builder.append(account);
		builder.append(", userPwd=");
		builder.append(userPwd);
		builder.append(", timestamp=");
		builder.append(timestamp);
		builder.append("]");
		return builder.toString();
	}
}
