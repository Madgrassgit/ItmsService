package com.linkage.stbms.init;

import com.linkage.commons.jms.MQConfigParser;
import com.linkage.commons.jms.MQPublisher;
import com.linkage.commons.jms.obj.MQConfig;
import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.dao.InitDAO;
import com.linkage.stbms.ids.util.CommonUtil;
import com.linkage.stbms.itv.main.Global;
import com.linkage.stbms.itv.main.StbServGlobals;
import com.linkage.stbms.policy.mq.thread.PicUpTaskThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class InitBIO {

	/** log */
	private static final Logger logger = LoggerFactory.getLogger(InitBIO.class);

	/**
	 * init app.
	 * 
	 * @return
	 */
	public boolean init() {
		logger.debug("init()");
		
		boolean flag = true;
		
		// 初始化配置文件
		InitConfig();
			
//		if("1".equals(Global.MQ_PIC_UP_TASK_ENAB))
		if(Global.MQ_POOl_MAP != null && Global.MQ_POOl_MAP.get("picUp.task") != null)
		{
			logger.warn("开始启动监听MQ消息线程, topic[{}]", "picUp.task");
			
			try {
				PicUpTaskThread servInfoDealThread = new PicUpTaskThread();
				servInfoDealThread.start();
				
				/**
				 * 此段代码是模拟发送MQ消息，用于测试
				 
				Task task = new Task(StbServGlobals.getLipossProperty("mq.clientId"),
						"taskId", "action");
				PicUp picUp = new PicUp(task);
				
				MQPublisher publisher2 = new MQPublisher(Global.MQ_PIC_UP_TASK_ENAB,
						Global.MQ_PIC_UP_TASK_URL, Global.MQ_PIC_UP_TASK_TOPIC);
				
				publisher2.publishMQ(picUp);
				
				*/
				
			} catch (Exception e) {
				flag = false;
				e.printStackTrace();
				logger.error(" MQ消息发布失败， mesg({})", e.getMessage());
			}
			
		}

		return flag;
	}

	/**
	 * 初始化配置文件
	 */
	private void InitConfig() {
		// dev.picUp.task
//		Global.MQ_PIC_UP_TASK_ENAB = StbServGlobals.getLipossProperty("mq.stb.mqPicUpTask.enab");
//		Global.MQ_PIC_UP_TASK_URL = StbServGlobals.getLipossProperty("mq.stb.mqPicUpTask.url");
//		Global.MQ_PIC_UP_TASK_TOPIC = StbServGlobals.getLipossProperty("mq.stb.mqPicUpTask.topic");
		Global.MQ_POOl_MAP = MQConfigParser.getMQConfig(StbServGlobals.G_ServerHome + File.separator
				+ "conf" + File.separator + "MQPool.xml", "stb");
		if(null != Global.MQ_POOl_MAP && !Global.MQ_POOl_MAP.isEmpty()){
			for (Map.Entry<String, MQConfig> entry : Global.MQ_POOl_MAP.entrySet()) {
				Global.CLIENT_ID = entry.getValue().getClientId();
				Global.SYSTEM_NAME = entry.getValue().getSystemName();
				if(Global.CLIENT_ID != null && Global.SYSTEM_NAME != null){
					break;
				}
			}	
		}
//		
//		//  策略同步生效接口 服务端URL  综合网管系统为客户端，IPTV业务管理平台为服务端
//		Global.STB_PIC_POLICY_SYNC_EFF_ADDRESS = StbServGlobals.getLipossProperty("StbPicPolicySyncEff.URL");
//		Global.STB_PIC_POLICY_SYNC_ADDRESS = StbServGlobals.getLipossProperty("StbPicPolicySync.URL");
		
//		// 存放用于FTP上传的附件 以及 FTP下载下来的附件
//		String localFilePath = StbServGlobals.getLipossProperty("localFilePath");
//		if (!localFilePath.endsWith("/")) {
//			Global.LOCAL_FILE_PATH = localFilePath + "/";
//		} else {
//			Global.LOCAL_FILE_PATH = localFilePath ;
//		}
		
//		/** IPTV业务平台FTP的URL 用于存放策略执行结果文件 */
//		Global.IPTV_BUSINESS_PLAT_FORM_FTP_URL = StbServGlobals.getLipossProperty("IptvBusinessPlatFormFtpUrl");
		
//		/** 按照指定的时间 每天定时统计策略同步生效结果 */
//		Global.SCHEDULE_TIME = StbServGlobals.getLipossProperty("ScheduleTime.StartTime");
//		/** 定时统计功能是否开启，0表示不开启，1表示开启 */
//		Global.SCHEDULE_TIME_ENAB = StbServGlobals.getLipossProperty("ScheduleTime.Enab");
		
		Global.G_instArea = StbServGlobals.getLipossProperty("InstArea");
		Global.G_Sysytem_Type = StringUtil.getIntegerValue(StbServGlobals.getLipossProperty("SystemType"));
		Global.ACS_OBJECT_NAME = CommonUtil.getPrefix4IOR()+"ACS";
		Global.JX_MODIFY_PWD_URL = StringUtil.getStringValue(StbServGlobals.getLipossProperty("JxModifyPwd.url"));
		Global.JX_MODIFY_PWD_USERNAME = StringUtil.getStringValue(StbServGlobals.getLipossProperty("JxModifyPwd.username"));
		Global.JX_MODIFY_PWD_PASSWORD = StringUtil.getStringValue(StbServGlobals.getLipossProperty("JxModifyPwd.password"));
		Global.STBSERVICE_URL = StringUtil.getStringValue(StbServGlobals.getLipossProperty("StbService"));
	    com.linkage.stbms.pic.Global.DB_TYPE = StringUtil.getStringValue(StbServGlobals.getLipossProperty("dbType")) == null ? "sybase" : StringUtil.getStringValue(StbServGlobals.getLipossProperty("dbType"));
	    com.linkage.stbms.ids.obj.SysConstant.G_DBType = StringUtil.getStringValue(StbServGlobals.getLipossProperty("dbType")) == null ? "sybase" : StringUtil.getStringValue(StbServGlobals.getLipossProperty("dbType"));

//		if ("xj_dx".equals(Global.G_instArea)) {
//			InitDAO.initPreProcessStb();
//			com.linkage.itms.Global.PRE_PROCESS_TYPE = StringUtil.getStringValue(StbServGlobals.getLipossProperty("preProcessType"));
//			com.linkage.itms.Global.G_PPManager_STB = Global.G_PPManager_STB;
//		}
		if ("nmg_dx".equals(Global.G_instArea) || "nx_dx".equals(Global.G_instArea)) {
			InitDAO.initPreProcessStb();
			com.linkage.itms.Global.G_PPManager_STB = Global.G_PPManager_STB;
		}
		
		if("jx_dx".equals(Global.G_instArea) ||"xj_dx".equals(Global.G_instArea)){
			com.linkage.itms.Global.PRE_PROCESS_TYPE = StringUtil.getStringValue(StbServGlobals.getLipossProperty("preProcessType"));
			com.linkage.itms.Global.RESOURCE_BIND_TYPE = StringUtil.getStringValue(StbServGlobals.getLipossProperty("resourceBindType"));
			// 解析MQ生产者配置文件
			com.linkage.itms.Global.MQ_POOL_PUBLISHER_MAP = MQConfigParser.getMQConfig(StbServGlobals.G_ServerHome + File.separator
					+ "conf" + File.separator + "MQPool.xml", "publisher");
			
			// 调用配置模块模块主题
			List<String> suffixList = new ArrayList<String>();
			suffixList.add(".servStb");
			suffixList.add(".softStb");
			suffixList.add(".batchStb");
			suffixList.add(".strategyStb");
			com.linkage.itms.Global.PROCESS_PUBLISHER = new MQPublisher("cm",suffixList, com.linkage.itms.Global.MQ_POOL_PUBLISHER_MAP);
			com.linkage.itms.Global.RESOURCE_BIND_PUBLISHER = new MQPublisher("res.InterfaceStb", com.linkage.itms.Global.MQ_POOL_PUBLISHER_MAP);
		    logger.warn("com.linkage.itms.Global.PRE_PROCESS_TYPE :"+com.linkage.itms.Global.PRE_PROCESS_TYPE );
		    logger.warn("com.linkage.itms.Global.RESOURCE_BIND_TYPE :"+com.linkage.itms.Global.RESOURCE_BIND_TYPE);
		}
		
		if("sx_lt".equals(Global.G_instArea)){
			// 解析MQ生产者配置文件
			com.linkage.itms.Global.MQ_POOL_PUBLISHER_MAP_STB = MQConfigParser.getMQConfig(StbServGlobals.G_ServerHome + File.separator
					+ "conf" + File.separator + "MQPool.xml", "stb");
			
			// 调用配置模块模块主题
			List<String> suffixList = new ArrayList<String>();
			suffixList.add(".servStb");
			suffixList.add(".softStb");
			suffixList.add(".batchStb");
			suffixList.add(".strategyStb");
			com.linkage.itms.Global.PROCESS_PUBLISHER_STB = new MQPublisher("cm",suffixList, com.linkage.itms.Global.MQ_POOL_PUBLISHER_MAP_STB);
			com.linkage.itms.Global.RESOURCE_BIND_PUBLISHER_STB = new MQPublisher("res.InterfaceStb", com.linkage.itms.Global.MQ_POOL_PUBLISHER_MAP_STB);
		}
	}

}
