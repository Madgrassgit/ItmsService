package com.linkage.stbms.cao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.stbms.itv.inter.IService;

/**
 * @author Jason(3412)
 * @date 2009-12-15
 */
public class DeviceGatherCAO {
	
	private static Logger logger = LoggerFactory.getLogger(DeviceGatherCAO.class);
	
	
	/**
	 * 采集STB的所有结点信息
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return int
	 */
	public int gatherStbAll(String deviceId){
		logger.debug("gatherStbAll({})", deviceId);
		// 调用采集模块
		int iresult = new SuperGatherCorba().getCpeParams(deviceId, IService.GATHER_ALL);
		
		return iresult;
	}
	
	
	/**
	 * 采集STB的Device.LAN.结点信息
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return int
	 */
	public int gatherLan(String deviceId){
		logger.debug("gatherLan({})", deviceId);
		// 调用采集模块
		int iresult = new SuperGatherCorba().getCpeParams(deviceId, IService.GATHER_LAN);
		
		return iresult;
	}
	
	
	/**
	 * 采集STB的Device.X_CTC_IPTV.[ServiceInfo.]结点信息
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return int
	 */
	public int gatherIPTVServiceInfo(String deviceId){
		logger.debug("gatherIPTVServiceInfo({})", deviceId);
		// 调用采集模块
		int iresult = new SuperGatherCorba().getCpeParams(deviceId, IService.GATHER_X_CTC_IPTV);
		
		return iresult;
	}
	
	
	/**
	 * 采集STB的Device.UserInterface.结点信息
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return int
	 */
	public int gatherUserInterfaceInfo(String deviceId){
		logger.debug("gatherUserInterfaceInfo({})", deviceId);
		// 调用采集模块
		int iresult = new SuperGatherCorba().getCpeParams(deviceId, IService.GATHER_UserInterface);
		
		return iresult;
	}
	
	
	/**
	 * 采集STB的Device.STBDevice.[1.AVProfile.AudienceStats.]结点信息
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return int
	 */
	public int gatherSTBDeviceInfo(String deviceId){
		logger.debug("gatherSTBDeviceInfo({})", deviceId);
		// 调用采集模块
		int iresult = new SuperGatherCorba().getCpeParams(deviceId, IService.GATHER_STBDevice);
		
		return iresult;
	}
}
