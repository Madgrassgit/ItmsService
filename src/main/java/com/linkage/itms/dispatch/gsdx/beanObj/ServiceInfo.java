package com.linkage.itms.dispatch.gsdx.beanObj;

import java.io.Serializable;
import java.util.Arrays;


/**
 * 基本信息
 * @author fanjm (AILK No.35572)
 * @version 1.0
 * @since 2019-6-14
 * @category com.linkage.itms.dispatch.gsdx.beanObj
 * @copyright AILK NBS-Network Mgt. RD Dept.
 */
public class ServiceInfo  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8129034020011498417L;
	
	//私有参数列表
	private Para[] paraList;
	//业务类型
    private Integer type;
    //业务状态
    private Integer serviceStatus;
    //网络状态
    private Integer networkStatus;
	
	@Override
	public String toString()
	{
		return "ServiceInfo [paraList=" + Arrays.toString(paraList) + ", type=" + type
				+ ", serviceStatus=" + serviceStatus + ", networkStatus=" + networkStatus
				+ "]";
	}
	

	public Para[] getParaList()
	{
		return paraList;
	}
	
	public void setParaList(Para[] paraList)
	{
		this.paraList = paraList;
	}
	
	public Integer getType()
	{
		return type;
	}
	
	public void setType(Integer type)
	{
		this.type = type;
	}
	
	public Integer getServiceStatus()
	{
		return serviceStatus;
	}
	
	public void setServiceStatus(Integer serviceStatus)
	{
		this.serviceStatus = serviceStatus;
	}
	
	public Integer getNetworkStatus()
	{
		return networkStatus;
	}
	
	public void setNetworkStatus(Integer networkStatus)
	{
		this.networkStatus = networkStatus;
	}

	
	
}
