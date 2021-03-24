package com.linkage.itms;

import ResourceBind.BindInfo;
import ResourceBind.ResultInfo;
import ResourceBind.UnBindInfo;

/**
 * 调用绑定模块接口
 * @author jiafh (Ailk NO.)
 * @version 1.0
 * @since 2016-11-3
 * @category com.linkage.module.gwms.util
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public interface ResourceBindInterface {
	
	/**
	 * 绑定
	 * @param bindInfo
	 * @return
	 */
	public ResultInfo bind(BindInfo[] bindInfo);
	
	/**
	 * 绑定
	 * @param bindInfo
	 * @return
	 */
	public ResultInfo bind(BindInfo[] bindInfo,String clientId);
	
	/**
	 * 解绑
	 * @param bindInfo
	 * @return
	 */
	public ResultInfo release(UnBindInfo[] unBindInfo);
	
	/**
	 * 解绑
	 * @param bindInfo
	 * @return
	 */
	public ResultInfo release(UnBindInfo[] unBindInfo,String clientId);
	
	/**
	 * 绑定
	 * @param username
	 * @param deviceId
	 * @param accName
	 * @param userline
	 */
	public void DoBindSingl(String username,String deviceId,String accName,int userline);
	
	/**
	 * 解绑
	 * @param userid
	 * @param deviceId
	 * @param accName
	 * @param userline
	 */
	public void DoUnBindSingl(String userid,String deviceId,String accName,int userline);
	
	/**
	 * 绑定
	 * @param bindInfo
	 */
	public void DoBindSingl(BindInfo[] bindInfo);
	
	/**
	 * 解绑
	 * @param unBindInfo
	 */
	public void DoUnBindSingl(UnBindInfo[] unBindInfo);
	
	/**
	 * 解绑
	 * @param bindInfo
	 * @return
	 */
	public ResultInfo release4JL(UnBindInfo[] unBindInfo, int serviceType);
	
	public ResultInfo updateUser(String user);

	public ResultInfo delDevice(String device_id);
	
	public ResultInfo updateDevice(String device_id);

	public ResultInfo deleteUser(String userName);
}
