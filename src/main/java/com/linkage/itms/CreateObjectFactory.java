package com.linkage.itms;

import com.linkage.itms.cao.PreProcessCorba;
import com.linkage.itms.cao.PreProcessCorbaByType;
import com.linkage.itms.cao.ResourceBindCorba;
import com.linkage.itms.message.PreProcessMQ;
import com.linkage.itms.message.ResourceBindMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 创建Corab、MQ公用类对象
 * 
 * @author jiafh (Ailk NO.)
 * @version 1.0
 * @since 2016-11-3
 * @category com.linkage.module.gwms.util
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 
 */
public class CreateObjectFactory {
	private static Logger logger = LoggerFactory.getLogger(CreateObjectFactory.class);
	// 常量
	private static final String TWO = "2";
	
	
	/**
	 * 生成调用配置模块的对象，不带参数
	 * 
	 * @return
	 */
	public static PreProcessInterface createPreProcess() {
		
		// 如果配置的为2，则使用发送消息方式，否则使用corba方式
		if (TWO.equals(Global.PRE_PROCESS_TYPE)) {
			return new PreProcessMQ("");
		} else {
			return new PreProcessCorba();
		}
	}
	
	/**
	 * 生成调用配置模块的对象，带参数
	 * @author banyr (Ailk No.)
	 * @since 2018-6-21
	 * @return
	 */
	public static PreProcessInterface createPreProcess(String type) {

		// 如果配置的为2，则使用发送消息方式，否则使用corba方式
		if (TWO.equals(Global.PRE_PROCESS_TYPE)) {
			return new PreProcessMQ(type);
		} else {
			return new PreProcessCorbaByType(type);
		}
	}
	
	/**
	 * 生成调用绑定模块的对象，带参数
	 * 
	 * @param gwType
	 * @return
	 */
	public static ResourceBindInterface createResourceBind(String gwType) {

		// 如果配置的为2，则使用发送消息方式，否则使用corba方式
		if (TWO.equals(Global.RESOURCE_BIND_TYPE)) {
			return new ResourceBindMQ(gwType);
		} else {
			return new ResourceBindCorba(gwType);
		}
	}
}
