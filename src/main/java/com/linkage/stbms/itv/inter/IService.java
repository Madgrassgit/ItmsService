package com.linkage.stbms.itv.inter;

/**
 * @author Jason(3412)
 * @date 2009-12-15
 */
public interface IService {

	// 设备连接状态标识
	public static int ONLINE = 1;
	public static int OFFLINE = 0;
	public static int UNKOWN = -1;
	public static int UNMANAGE = -2;
	
	/** 采集接口调用值定义 */
	//所有结点
	public static int GATHER_ALL = 0;
	//Device.STBDevice.
	public static int GATHER_STBDevice = 1;
	//Device.UserInterface.
	public static int GATHER_UserInterface = 2;
	//Device.LAN.
	public static int GATHER_LAN = 3;
	//Device.X_CTC_IPTV.ServiceInfo.
	public static int GATHER_X_CTC_IPTV = 41;

	
	/** * ITMS与ITV综合网管系统接口定义 */
	
	//设备信息查询接口
	String getUserStbInfo(String para);
	//Ping操作
	String setStbPingIP(String para);
	//赛特斯版本升级定制
//	String setStbSpecVersionUpgrade (String para);
}
