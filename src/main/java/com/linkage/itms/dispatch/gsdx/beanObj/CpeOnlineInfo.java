package com.linkage.itms.dispatch.gsdx.beanObj;

import java.io.Serializable;
import java.util.Arrays;



/**
 * 
 * @author fanjm (AILK No.35572)
 * @version 1.0
 * @since 2019-6-14
 * @category com.linkage.itms.dispatch.gsdx.beanObj
 * @copyright AILK NBS-Network Mgt. RD Dept.
 */
public class CpeOnlineInfo  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5387236032838649897L;
	//基本信息
	private CpeBasicInfo basicInfo;
	//用户信息
    private UserInfo userInfo;
    //业务信息数组
    private ServiceInfo[] serviceInfoList;
    
    
    
	@Override
	public String toString()
	{
		return "CpeOnlineInfo [basicInfo=" + basicInfo + ", userInfo=" + userInfo
				+ ", serviceInfoList=" + Arrays.toString(serviceInfoList)
				+ ", errorCode=" + errorCode + "]";
	}

	public CpeBasicInfo getBasicInfo()
	{
		return basicInfo;
	}
	
	public void setBasicInfo(CpeBasicInfo basicInfo)
	{
		this.basicInfo = basicInfo;
	}
	
	public UserInfo getUserInfo()
	{
		return userInfo;
	}
	
	public void setUserInfo(UserInfo userInfo)
	{
		this.userInfo = userInfo;
	}
	
	public ServiceInfo[] getServiceInfoList()
	{
		return serviceInfoList;
	}
	
	public void setServiceInfoList(ServiceInfo[] serviceInfoList)
	{
		this.serviceInfoList = serviceInfoList;
	}
	
	public Integer getErrorCode()
	{
		return errorCode;
	}
	
	public void setErrorCode(Integer errorCode)
	{
		this.errorCode = errorCode;
	}
	/**
     * 错误码
     * -1：查询失败
		0：查询成功但未找到结果
		1：成功且有一条数据
		>1：成功且查询到多条数据，此处返回值为结果个数，但是结构体中不返回终端信息。
     */
    private Integer errorCode;
   
	
}
