package com.linkage.itms.dacs.main;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jason(3412)
 * @date 2009-5-25
 */
public class Global {

	/** * Qos设备编码Map */
	public static Map<String, String> qosMap = new ConcurrentHashMap<String, String>();

	static {
		qosMap.put("1101", "INTERNET,TR069,IPTV");
		qosMap.put("1301", "INTERNET,TR069,VOIP");
		qosMap.put("111301", "INTERNET,TR069,VOIP,IPTV");
		qosMap.put("69", "INTERNET,TR069");
	}
}
