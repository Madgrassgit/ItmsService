package com.linkage.itms.itv.inter;

/**
 * @author Jason(3412)
 * @date 2009-12-15
 */
public interface IService {
	
	// 设备连接状态标识
	public static String ONLINE = "1";
	public static String OFFLINE = "0";
	public static String UNKOWN = "-1";
	public static String NODEV = "-2";
	
	//WAN连接状态
	public static String UP = "up";
	public static String DOWN = "down";
	
	/** * ITV综合网管系统调用xml字符串 */
	public String callXmlStr = "<?xml version=\"1.0\" encoding=\"gb2312\"?><root><username></username></root>";

	/** * ITMS回复结果 */
	public String returnXmlStr = "<?xml version=\"1.0\" encoding=\"gb2312\"?><root>"
			+ "<connect_state>{state}</connect_state>"
			+ "<wan_state>{wan_state}</wan_state>"
			+ "<bohao_resulte>{last_conn_error}</bohao_resulte>"
			+ "<accesstype>{accesstype}</accesstype>"
			+ "<modem_vendor>{dev_vendor}</modem_vendor>"
			+ "<modem_type>{dev_model}</modem_type>"
			+ "<modem_sn>{dev_sn}</modem_sn>"
			+ "<pvc_state>{pvc_state}</pvc_state>"
			+ "<pvc>{pvc}</pvc>"
			+ "<vlanid>{vlanid}</vlanid>" + "</root>";

	/** * ITMS与ITV综合网管系统接口定义 */
	String getUserModemInfo(String strParamXML);
}
