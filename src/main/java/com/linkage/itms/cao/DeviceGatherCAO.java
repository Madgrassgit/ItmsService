package com.linkage.itms.cao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 设备采集操作类
 * 
 * @author Jason(3412)
 * @date 2009-12-15
 */
public class DeviceGatherCAO {
	
	private static Logger logger = LoggerFactory.getLogger(DeviceGatherCAO.class);
	
	final int iGatherOnline = 2;
	
	//LANDevice.LANEthernetInterfaceConfig
	public static int GATHER_LAN_ETHERNET = 11;  // add by zhangchy 2012-03-05 用于桥改路由LAN端口的采集
	//LANDevice.WLANConfiguration
	public static int GATHER_LAN_WLAN = 12;      // add by zhangchy 2012-03-05 用于桥改路由WLAN端口的采集
	
	//WANDevice 采集
	public static int GATHER_WAN = 2;
	//QOS采集
	public static int GATHER_QOS = 5;
	
	/**
	 * 采集设备的WAN口状态信息
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return int
	 */
	public int gatherWan(String deviceId){
		logger.debug("gatherWan({})", deviceId);
		// 调用采集模块
		int iresult = new SuperGatherCorba().getCpeParams(deviceId, DeviceGatherCAO.GATHER_WAN);
		
		return iresult;
	}

	/**
	 * 采集设备的QOS信息
	 * 
	 * @param 
	 * @author qixueqi(4174)
	 * @date 2010-12-30
	 * @return int
	 */
	public int gatherQoS(String deviceId){
		logger.debug("gatherWan({})", deviceId);
		// 调用采集模块
		int iresult = new SuperGatherCorba().getCpeParams(deviceId, DeviceGatherCAO.GATHER_QOS);
		
		return iresult;
	}
	
	/**
	 * 获取设备的在线状态
	 * 
	 * @param 
	 * deviceId：设备ID
	 * 
	 * @author Jason(3412)
	 * @date 2010-9-2
	 * @return boolean 在线返回true，不在线返回false
	 */
	public boolean gatherDevOnline(String deviceId){
		logger.debug("gatherDevOnline({})", deviceId);
		// 调用采集模块
		int iresult = new SuperGatherCorba().getCpeParams(deviceId, iGatherOnline, 3);  // 在原来基础上增加了一个参数(3)
		if(-2 == iresult || 100000 == iresult){
			return false;
		}
		return true;
	}
}
