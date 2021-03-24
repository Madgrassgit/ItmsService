package com.linkage.itms.rms.obj.domain;

/**
 * @author Jason(3412)
 * @date 2009-6-15
 */
public interface IParamTree {

	/** 根结点 */
	public static final String ROOT = "InternetGatewayDevice";
	/** WANDevice结点 */
	public static final String WANDEVICE = "InternetGatewayDevice.WANDevice";
	/** 线路信息 */
	public static final String DEVICEWIREINFO = "WANDSLInterfaceConfig";
	/** WANConnectionDevice */
	public static final String WANCONNDEVICE = "WANConnectionDevice";
	/** WANConnectionSeesion */
	public static final String WANPPPCONN = "WANPPPConnection";
	/** LANDevice */
	public static final String LANDEVICE = "InternetGatewayDevice.LANDevice";
	
	public static final String ETHERNET = "LANEthernetInterfaceConfig";
	/** WLAN信息 */
	public static final String WLANCONFIG = "WLANConfiguration";
	/** ping检测 */
	public static final String PING = "IPPingDiagnostics";

	/** WANConnectionSeesion */
	public static final String WANIPCONN = "WANIPConnection";
}
