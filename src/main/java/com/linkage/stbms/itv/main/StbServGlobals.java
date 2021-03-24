/*
 * @(#)LipossGlobals.java	1.00 1/5/2006
 *
 * Copyright 2005 联创科技.版权所有
 */
package com.linkage.stbms.itv.main;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.system.utils.xml.XMLProperties;


/**
 * 初始化Web系统版本信息，加载Web配置文件：liposs_init.properties、liposs_cfg.xml。
 * 
 * @author yuht
 * @version 1.00, 1/5/2006
 * @since Liposs 2.1
 */

public class StbServGlobals {
	private static final Logger m_logger = LoggerFactory.getLogger(StbServGlobals.class);
	
	// Web 配置文件名称
	private static final String LIPOSS_CONFIG_FILENAME = "ItmsService_cfg.xml";

	// 存放XML配置文件变量
	private static XMLProperties properties = null;

	// server 部署路径
	public static String G_ServerHome = null;
	
	/**
	 * 构造Liposs Web 系统版本信息
	 * 
	 * @return 返回系统版本信息字符串
	 */
	public static String getLipossVersion() {
		return getLipossProperty("Version");
	}

	/**
	 * 从liposs_init.properties 属性文件中读取系统部署路径
	 * 
	 * @return 返回系统部署路径字符串
	 */
	public static String getLipossHome() {
		return G_ServerHome;
	}
	
	/**
	 * 获取模块标识
	 * @return
	 * @author zhangsm
	 */
	public static String getClientId(){
//		return getLipossProperty("mq.clientId");
		return Global.CLIENT_ID;
	}

	/**
	 * 读取系统部署名称
	 * 
	 * @return 返回系统部署名称字符串
	 */
	public static String getLipossName() {
		return getLipossProperty("ServerName");
	}

	/**
	 * 获取网关设备管理协议
	 * @return 
	 * 		<li>0: All(所有);</li>
	 * 		<li>1: TR069;</li>
	 * 		<li>2: SNMP.</li>
	 */
	public static int getGwProtocol() {
		if (null == getLipossProperty("GwProtocol")) {
			return 0;
		}

		try {
			return Integer.parseInt(getLipossProperty("GwProtocol"));
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	/**
	 * 获取调用ACS前休眠的时间
	 * @return	时间(second)
	 */
	public static int getSleepTime(){
		int tmp = 5000;
		//默认为ITMS
		if (null == getLipossProperty("sleepTime")) {
			return 5000;
		} 
		try {
			tmp = Integer.parseInt(getLipossProperty("sleepTime"));
		} catch (NumberFormatException e) {
			return 5000;
		}
		return tmp;
	}
	
	/**
	 * 系统类型
	 * @return
	 * 		<li>1:家庭网关(默认值)</li>
	 * 		<li>2:企业网关</li>
	 * 		<li>0:家庭网关&企业网关</li>
	 */
	public static int SystemType(){
		int tmp = 1;
		//默认为ITMS
		if (null == getLipossProperty("SystemType")) {
			return 1;
		} 
		try {
			tmp = Integer.parseInt(getLipossProperty("SystemType"));
		} catch (NumberFormatException e) {
			return 1;
		}
		if (tmp > 2 || tmp < 0) {
			return 1;
		} else {
			return tmp;
		}
	}
	
	/**
	 * 获取指点名称的值
	 * 
	 * @param name
	 *            属性名称字符串
	 * @return 返回指点名称的值
	 */
	public static String getLipossProperty(String name) {
		if (properties == null) {
			loadProperties();
		}

		return properties.getProperty(name);
	}

	/**
	 * 设置指点名称的值
	 * 
	 * @param name
	 *            属性名称字符串
	 * @param value
	 *            属性值字符串
	 */
	public static void setLipossProperty(String name, String value) {
		if (properties == null) {
			loadProperties();
		}

		properties.setProperty(name, value);
	}

	/**
	 * 载入XML配置文件到XMLProperties实例中
	 */
	private synchronized static void loadProperties() {
		if (properties == null) {
			if (G_ServerHome == null) {
				G_ServerHome = StbServGlobals.getLipossHome();
			}
			m_logger.debug(G_ServerHome);
			String path = G_ServerHome + File.separator + "conf"
					+ File.separator + LIPOSS_CONFIG_FILENAME;
			m_logger.debug(path);
			properties = new XMLProperties(path);
		}
	}

	/**
	 * 重新加载配置文件liposs_cfg.xml内容到XMLProperties实例中
	 * 
	 */
	public void reload() {
		properties = null;
		loadProperties();
	}
	
	/**
	 * 报表生成工具
	 * 策略配置默认参数
	 * 发送类型
	 * @return
	 */
	public static String getRrctPolicySendType() {
	    String value = getLipossProperty("rrct.policy.send_type");
	    return value;
	}
	/**
	 * 报表生成工具
	 * 策略配置默认参数
	 * 策略
	 * @return
	 */
	public static String getRrctPolicyValue() {
	    String value = getLipossProperty("rrct.policy.value");
	    return value;
	}

	
}


class InitPropLoader {
	private Properties initProps = new Properties();

	private InputStream in = null;

	private String lipossHome = null;

	private String lipossName = null;

	InitPropLoader() {
		try {
			in = getClass().getResourceAsStream("/liposs_init.properties");
			initProps.load(in);
		} catch (Exception e) {
//			m_logger.error("Error reading Linkage properties "
//					+ "in NetworkGlobals");
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
	}

	public String getLipossHome() {
		if (initProps != null) {
			lipossHome = initProps.getProperty("lipossHome");

			if (lipossHome != null) {
				lipossHome = lipossHome.trim();

				while (lipossHome.endsWith("/") || lipossHome.endsWith("\\")) {
					lipossHome = lipossHome.substring(0,
							lipossHome.length() - 1);
				}
			}
		}
		return lipossHome;
	}

	public String getLipossName() {
		if (initProps != null) {
			lipossName = initProps.getProperty("lipossName");

			try {
				lipossName = new String(lipossName.getBytes("ISO8859_1"), "GBK");
			} catch (Exception e) {
				lipossName = "出错";
			}
		}

		return lipossName;
	}
	
	
	
}
