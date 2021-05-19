
package com.linkage.init;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sendCorbaHeartBeat.RunSendCorbaHeartBeatTask;

import com.linkage.commons.jms.MQConfigParser;
import com.linkage.commons.jms.MQPublisher;
import com.linkage.commons.jms.obj.MQConfig;
import com.linkage.commons.redis.RedisPoolUtil;
import com.linkage.commons.thread.ThreadPoolCommon;
import com.linkage.commons.util.StringUtil;
import com.linkage.commons.xml.XML2Bean;
import com.linkage.init.obj.CpeFaultcodeOBJ;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.XML;
import com.linkage.itms.dao.AreaDAO;
import com.linkage.itms.dao.CityDAO;
import com.linkage.itms.dao.InitDAO;
import com.linkage.itms.dao.SimulationSpeedDao;
import com.linkage.itms.dispatch.util.memcached.MemcachedClientUtil;
import com.linkage.itms.dispatch.util.memcached.MemcachedPool;
import com.linkage.itms.mq.servinfo.thread.ServInfoDealThread;
import com.linkage.itms.socket.core.SocketWorker;
import com.linkage.itms.socket.pwdsyn.util.PwdSynListener;
import com.linkage.itms.socket.userBandSyn.UserBandSynListener;
import com.linkage.system.utils.database.Cursor;

/**
 * @author Jason(3412)
 * @date 2010-6-25
 */
public class InitBIO {

	/** log */
	private static final Logger logger = LoggerFactory.getLogger(InitBIO.class);

	/**
	 * xml对象
	 */
	private XML xml;

	/**
	 * init app.
	 * 
	 * @return
	 */
	public boolean init() throws IOException {
		logger.debug("init()");
		boolean flag = true;
		
		// 初始化属地信息
		this.initCityInfo();
		
		// 初始化配置文件 add by chenjie 2011-12-9
		try {
			InitConfig();
		}catch (Exception e) {
			logger.error("InitConfig is error", e);
		}
		logger.warn("before InitTableName");
		InitTableName();
		logger.warn("before initSuperGather");	
		// 初始化SuperGather Corba
		InitDAO.initSuperGather();
		logger.warn("before initPreProcess");	
		// 初始化PreProcess Corba
		InitDAO.initPreProcess();
		
		// 初始化设备错误信息
		this.initFaultCode();
		// jx_dx 初始化PreProcess Corba(带参)
		if("jx_dx".equals(Global.G_instArea))
		{
			InitDAO.initPreProcess(Global.GW_TYPE_ITMS);
			InitDAO.initPreProcess(Global.GW_TYPE_BBMS);
			InitDAO.initPreProcess(Global.GW_TYPE_STB);
		}
		//初始化资源绑定
		InitDAO.initResourceBind(Global.GW_TYPE_ITMS);
		InitDAO.initResourceBind(Global.GW_TYPE_BBMS);
		InitDAO.initResourceBind(Global.GW_TYPE_STB);
		//初始化软件升级
		if ("1".equals(Global.INIT_SOFT_UP_ENABLE)) {
			InitDAO.initSofUp(); 
		}

		// 是否初始化城市对应测速url  1：初始化   2：不初始化 
		if ("1".equals(Global.IS_INIT_CITY_SPEED)) {
			this.initCitySpeedMap();
		}

		if ("jl_lt".equals(Global.G_instArea)) {
			try {
				
				if ("R".equals(Global.XMEM_TYPE)) {
					RedisPoolUtil.getInstance().setRedisConfPath("");
					RedisPoolUtil.getInstance().init();
				}else {
					// 初始化缓存文件
					String memcache = FileUtils.readFileToString(new File(Global.G_ServerHome + File.separator
							+ "conf" + File.separator + "memcached.xml"));
					XML2Bean bean = new XML2Bean(memcache);
					Global.memcachedPool = (MemcachedPool) bean.getBean("MemcachedPool", MemcachedPool.class);

					// 是否初始化城市对应测速url  1：初始化   2：不初始化
					new MemcachedClientUtil().init();
				}
				
			} catch (IOException e) {
				logger.error("Memcached init is error");
			}
		}

		// 初始化域信息
		this.initAreaInfo();
		startServerSocket();
//		if(Global.MQ_SERVINFO_ENAB == Global.DO)
		if(Global.MQ_POOl_MAP != null && Global.MQ_POOl_MAP.get("servinfo") != null)
		{
			logger.warn("开始启动监听"+Global.SYSTEM_NAME+" MQ消息线程, topic[{}]", "servinfo");
			
			try {
				ServInfoDealThread servInfoDealThread = new ServInfoDealThread();
				servInfoDealThread.start();
				
				//servInfoDealThread.sleep(3000);   // 等待3秒
				
				/**
				 * 此段代码是模拟发送MQ消息，用于测试
				 * 
				MQSimplePublisher publisher2 = new MQSimplePublisher(1,"tcp://192.168.2.4:61616","servinfo");
				ServInfo obj = new ServInfo();
				//obj.setDevId("538");  // 用于暂时测试
				publisher2.publishMQ(obj);
				*/
				
			} catch (Exception e) {
				flag = false;
				e.printStackTrace();
				logger.error(Global.SYSTEM_NAME+" MQ消息发布失败， mesg({})", e);
			}
			
		}
		if(Global.IsSendCorbaHeartBeat == 1){
			try
			{
				Thread.sleep(60000L);
			}
			catch (InterruptedException e)
			{
				logger.error("error:",e);
				// Restore interrupted state...      
				Thread.currentThread().interrupt();
			}
			new Timer().schedule(new RunSendCorbaHeartBeatTask(), 10 * 1000L, 1000L * 60 * Global.SendCorbaHeartBeatPeriod);
		}
		return flag;
	}

	private void InitTableName(){
		// 电信
		if("CTC".equalsIgnoreCase(Global.G_OPERATOR)){
			Global.G_TABLENAME_MAP.put("gw_wan_conn", "gw_wan_conn");
			Global.G_TABLENAME_MAP.put("gw_wan_conn_session", "gw_wan_conn_session");
			Global.G_TABLENAME_MAP.put("gw_lan_eth", "gw_lan_eth");
			Global.G_TABLENAME_MAP.put("gw_wan_wireinfo_epon", "gw_wan_wireinfo_epon");
			Global.G_TABLENAME_MAP.put("gw_voip_prof_line", "gw_voip_prof_line");
		}
		// 联通
		else if("CUC".equalsIgnoreCase(Global.G_OPERATOR)){
			Global.G_TABLENAME_MAP.put("gw_wan_conn", "cuc_gw_wan_conn");
			Global.G_TABLENAME_MAP.put("gw_wan_conn_session", "cuc_gw_wan_conn_session");
			Global.G_TABLENAME_MAP.put("gw_lan_eth", "cuc_gw_lan_eth");
			Global.G_TABLENAME_MAP.put("gw_wan_wireinfo_epon", "cuc_gw_wan_wireinfo_epon");
			Global.G_TABLENAME_MAP.put("gw_voip_prof_line", "cuc_gw_voip_prof_line");
		}
		// 移动
		else{
			Global.G_TABLENAME_MAP.put("gw_wan_conn", "gw_wan_conn");
			Global.G_TABLENAME_MAP.put("gw_wan_conn_session", "gw_wan_conn_session");
			Global.G_TABLENAME_MAP.put("gw_lan_eth", "gw_lan_eth");
			Global.G_TABLENAME_MAP.put("gw_wan_wireinfo_epon", "gw_wan_wireinfo_epon");
			Global.G_TABLENAME_MAP.put("gw_voip_prof_line", "gw_voip_prof_line");
		}
	}
	/**
	 * 初始化配置文件
	 */
	@SuppressWarnings("deprecation")
	private void InitConfig() {
		
		xml = new XML(Global.G_ServerHome + File.separator + "conf" + File.separator + Global.CONFIG_FILENAME);
		
//		Global.ClIENT_ID = xml.getStringValue("mq.clientId");
//		
//		Global.SYSTEM_NAME = xml.getStringValue("mq.systemName");
		
		Global.G_OPERATOR = StringUtil.IsEmpty(xml.getStringValue("Operator")) ? "CTC": xml.getStringValue("Operator");
		Global.ESERVER_URL = this.xml.getStringValue("BusinessService.url");
		Global.ESERVER_OLD_URL = this.xml.getStringValue("BusinessService.oldurl");
		Global.is_SendToOldRMS = this.xml.getStringValue("BusinessService.is_SendToOldRMS");
		Global.G_SendThreadPool = ThreadPoolCommon.getFixedThreadPool(Integer.valueOf(xml.getStringValue("thread.blPoolSize")));
		Global.G_TestSpeedThreadPool = ThreadPoolCommon.getFixedThreadPool(xml.getIntValue("HttpSpead.PoolSize") == 0 ? 100:xml.getIntValue("HttpSpead.PoolSize"));
		
//		// acs.alive
//		Global.MQ_ACS_ALIVE_ENAB = xml.getIntValue("mq.mqACSAlive.enab");
//		Global.MQ_ACS_ALIVE_URL = xml.getStringValue("mq.mqACSAlive.url");
//		Global.MQ_ACS_ALIVE_TOPIC = xml.getStringValue("mq.mqACSAlive.topic");
//		
//		// dev.rpc
//		Global.MQ_DEV_RPC_ENAB = xml.getIntValue("mq.mqDevRPC.enab");
//		Global.MQ_DEV_RPC_URL = xml.getStringValue("mq.mqDevRPC.url");
//		Global.MQ_DEV_RPC_TOPIC = xml.getStringValue("mq.mqDevRPC.topic");
//		
//		// dev.servinfo
//		Global.MQ_SERVINFO_ENAB = xml.getIntValue("mq.mqServInfo.enab");
//		Global.MQ_SERVINFO_URL = xml.getStringValue("mq.mqServInfo.url");
//		Global.MQ_SERVINFO_TOPIC = xml.getStringValue("mq.mqServInfo.topic");
//		
//
//		Global.MQ_SERVINFOFINISH_ENAB = xml.getIntValue("mq.mqServInfoFinish.enab");
//		Global.MQ_SERVINFOFINISH_URL = xml.getStringValue("mq.mqServInfoFinish.url");
//		Global.MQ_SERVINFOFINISH_TOPIC = xml.getStringValue("mq.mqServInfoFinish.topic");
		
		Global.PRE_PROCESS_TYPE = xml.getStringValue("preProcessType");
		Global.TEST_SPEED_REPORT_URL = xml.getStringValue("testSpeedReportUrl");
		Global.TEST_SPEED_DOWN_URL = xml.getStringValue("testSpeedDownUrl");
		Global.RESOURCE_BIND_TYPE = xml.getStringValue("resourceBindType");
		Global.DOWNLOADURL = xml.getStringValue("JxdxDownLoadUrl");
		Global.SPECIALDOWNLOADURL = xml.getStringValue("SpecialJxdxDownLoadUrl");

		Global.TEST_SPEED_TIME = xml.getIntValue("testSpeedTime");
		
		Global.XMEM_TYPE = StringUtil.getStringValue(xml.getStringValue("memType"));


		logger.warn(Global.G_ServerHome + File.separator+ "conf" + File.separator + "MQPool.xml");
		Global.MQ_POOl_MAP = MQConfigParser.getMQConfig(Global.G_ServerHome + File.separator
				+ "conf" + File.separator + "MQPool.xml", "itms");
		if(Global.MQ_POOl_MAP != null){
			for (Map.Entry<String, MQConfig> entry : Global.MQ_POOl_MAP.entrySet()) {
				Global.ClIENT_ID = entry.getValue().getClientId();
				Global.SYSTEM_NAME = entry.getValue().getSystemName();
				if(Global.ClIENT_ID != null && Global.SYSTEM_NAME != null){
					break;
				}
			}
		}
		
		// 解析MQ生产者配置文件
		Global.MQ_POOL_PUBLISHER_MAP = MQConfigParser.getMQConfig(Global.G_ServerHome + File.separator
				+ "conf" + File.separator + "MQPool.xml", "publisher");
		
		// 调用配置模块模块主题
		List<String> suffixList = new ArrayList<String>();
		suffixList.add(".serv");
		suffixList.add(".soft");
		suffixList.add(".batch");
		suffixList.add(".strategy");
		Global.PROCESS_PUBLISHER = new MQPublisher("cm",suffixList, Global.MQ_POOL_PUBLISHER_MAP);
		Global.RESOURCE_BIND_PUBLISHER = new MQPublisher("res.Interface", Global.MQ_POOL_PUBLISHER_MAP);
		
		//radius web service
		Global.RADIUS_NAMESPACE = xml.getStringValue("Radius.namespace");
		Global.RADIUS_URL = xml.getStringValue("Radius.url");
		// radius Socket
		Global.RADIUS_IP = xml.getStringValue("Radius_Socket.ip");
		Global.RADIUS_PORT = xml.getStringValue("Radius_Socket.port");

		Global.G_instArea = xml.getStringValue("InstArea");
		logger.warn("InstArea="+Global.G_instArea);
		
		Global.codeTypeValue = xml.getStringValue("codeType.codeTypeValue");
		logger.warn("codeTypeValue="+Global.codeTypeValue);
		
		// 江苏省移动 ITMS 测试 通过ItmsService给EServer发工单
		Global.G_ITMS_SHEET_SERVER_CHINA_MOBILE = xml.getStringValue("bss_sheet.server");
		Global.G_ITMS_SHEET_PORT_CHINA_MOBILE = xml.getStringValue("bss_sheet.port");
		

		Global.G_ITMS_FINISH_URL = xml.getStringValue("finishIntr.finishUri");
		Global.G_ITMS_SERV_METHOD = xml.getStringValue("finishIntr.servMethod");
		/** 是否开启软件升级功能 */
		Global.INIT_SOFT_UP_ENABLE = xml.getStringValue("initSoftUp.enab");
		
		// //初始化业务逻辑模块的线程池
		Global.G_BlThreadPool = ThreadPoolCommon.getFixedThreadPool(Integer.valueOf(xml.getStringValue("thread.blPoolSize")));
		//回调
		Global.G_CallBackThread = ThreadPoolCommon.getFixedThreadPool(Integer.valueOf(xml.getStringValue("thread.callBackSize")));
		// vxlan执行异步的线程池
		Global.G_AsynchronousThread = ThreadPoolCommon.getFixedThreadPool(Integer.valueOf(xml.getStringValue("thread.callBackSize")));
		
		// 初始化RSA算法公钥和私钥
		Global.RSA_PUBLIC_KEY = xml.getStringValue("RSA.publicKey");
		Global.RSA_PRIVATE_KEY = xml.getStringValue("RSA.privateKey");
		/** 休眠时间  */
		Global.sleepTime = xml.getIntValue("sleepTime");
		Global.jxFtthSleepTime = xml.getIntValue("jxFtthSleepTime");
		Global.jxTestFtthUrl = xml.getStringValue("jxTestFtthUrl");
		Global.jxTestFtthMethod = xml.getStringValue("jxTestFtthMethod");
		Global.idsServiceUrl = xml.getStringValue("idsService.url");
		/** 测速接口需要的两个参数*/
		Global.testURL=xml.getStringValue("testURL");
		Global.reportURL=xml.getStringValue("reportURL");
		/**JSDX_ITMS-REQ-20140930-ZJQ-001（ITMS与智能网管同步账号接口)*/
		if("js_dx".equals(Global.G_instArea))
		{
			Global.ip =  xml.getStringValue("ftp.ip");
			Global.port = xml.getStringValue("ftp.port");
			Global.username = xml.getStringValue("ftp.username");
			Global.password = xml.getStringValue("ftp.password");
			Global.localDir = xml.getStringValue("ftp.localDir");
			Global.isVxlan = xml.getStringValue("isVxlan");
			if("1".equals(Global.isVxlan)){
				Global.SYSTEM_ITMS_PREFIX  = "";
			}
		}
		/** JXDX-ITMS-REQ-20150918-WUWF-001(ITMS平台业务下发状态过滤需求) */
//		if ("jx_dx".equals(Global.G_instArea) || "xj_dx".equals(Global.G_instArea) || "hb_dx".equals(Global.G_instArea) )
//		{
			Global.STRATEGY_TABNAME = xml.getStringValue("strategy_tabname.serv.tabname");
			if (StringUtil.IsEmpty(Global.STRATEGY_TABNAME))
			{
				Global.STRATEGY_TABNAME = xml.getStringValue("strategy_tabname.strategy.tabname");
			}
//		}
		Global.SERV_SERVICE_ID = xml.getStringValue("strategy_tabname.serv.serviceId");
		Global.SOFT_SERVICE_ID = xml.getStringValue("strategy_tabname.soft.serviceId");
		Global.BATCH_SERVICE_ID = xml.getStringValue("strategy_tabname.batch.serviceId");
		Global.STRATEGY_SERVICE_ID = xml.getStringValue("strategy_tabname.strategy.serviceId");

		// 安徽预检预修调用测速平台接口方法
		if ("ah_dx".equals(Global.G_instArea))
		{
			Global.HTTPSPEED_URL = xml.getStringValue("HttpSpead.url");
			Global.HTTPSPEED_METHOD = xml.getStringValue("HttpSpead.method");
			Global.HTTPSPEED_NAMESPACE = xml.getStringValue("HttpSpead.nameSpace");
			Global.HTTPSPEED_SERVICENAMESOAP = xml.getStringValue("HttpSpead.serviceNameSoap");
		}
		logger.warn("Global.HTTPSPEED_URL:" + Global.HTTPSPEED_URL);
		logger.warn("Global.HTTPSPEED_METHOD:" + Global.HTTPSPEED_METHOD);
		logger.warn("Global.HTTPSPEED_NAMESPACE:" + Global.HTTPSPEED_NAMESPACE);
		logger.warn("Global.HTTPSPEED_SERVICENAMESOAP:" + Global.HTTPSPEED_SERVICENAMESOAP);
		logger.warn("Global.isVxlan:" + Global.isVxlan);
		
		Global.IsSendCorbaHeartBeat = xml.getIntValue("isSendCorbaHeartBeat");
		Global.SendCorbaHeartBeatPeriod = xml.getIntValue("sendCorbaHeartBeatPeriod");
		logger.warn("IsSendCorbaHeartBeat:" + Global.IsSendCorbaHeartBeat);
		logger.warn("SendCorbaHeartBeatPeriod:" + Global.SendCorbaHeartBeatPeriod);
		
		Global.IS_INIT_CITY_SPEED = xml.getStringValue("isInitCitySpeed");
		
		/** 重庆激活工厂复位返回接口*/
		Global.G_ITMS_FINISH_URL = xml.getStringValue("factoryReturn.wsdl");
		Global.G_ITMS_SERV_METHOD = xml.getStringValue("factoryReturn.method");
		Global.G_ITMS_NAME_SPACE = xml.getStringValue("factoryReturn.namespace");
		Global.G_ITMS_REQUEST_NAME = xml.getStringValue("factoryReturn.request");
		Global.G_ITMS_RESPONSE_NAME = xml.getStringValue("factoryReturn.response");
		logger.warn("Global.G_ITMS_FINISH_URL==" + Global.G_ITMS_FINISH_URL);
		logger.warn("Global.G_ITMS_SERV_METHOD==" + Global.G_ITMS_SERV_METHOD);
		logger.warn("Global.G_ITMS_NAME_SPACE==" + Global.G_ITMS_NAME_SPACE);
		logger.warn("Global.G_ITMS_REQUEST_NAME==" + Global.G_ITMS_REQUEST_NAME);
		logger.warn("Globals.G_ITMS_RESPONSE_NAME==" + Global.G_ITMS_RESPONSE_NAME);
		Global.BACK_FUKAI_URL = xml.getStringValue("backFukaiUrl");
		logger.warn("BACK_FUKAI_URL:" + Global.BACK_FUKAI_URL);

		if("jx_dx".equals(Global.G_instArea))
		{
			Global.Tywg2UpList =  xml.getStringValue("jxdxTywg2UpList");
		}
		// 初始化云网关相关参数
		initCloudInfo();
	}

	/**
	 * 初始化云网关相关参数
	 */
	public void initCloudInfo() {
		// 企业网关和家庭网关用户表，业务表是否融合，1是，0否
		Global.IPSEC_ISFUSED = xml.getIntValue("Ipsec.isfused");
		logger.warn("IPSEC_ISFUSED:" + Global.IPSEC_ISFUSED);
		
		// ipsec 开通、修改业务回调参数
		Global.ipsecUrl = xml.getStringValue("Ipsec.ipsecUrl");
		Global.ipsecTargetName = xml.getStringValue("Ipsec.ipsecTargetName");
		Global.ipsecMethodName = xml.getStringValue("Ipsec.ipsecMethodName");
		
		// vxlan 开通、修改、删除业务回调参数
		Global.vxlanUrl = xml.getStringValue("Ipsec.vxlanUrl");
		Global.vxlanTargetName = xml.getStringValue("Ipsec.vxlanTargetName");
		Global.vxlanMethodName = xml.getStringValue("Ipsec.vxlanMethodName");
		
		// 直通车 开通、修改、删除业务回调参数
		Global.hqosUrl = xml.getStringValue("Ipsec.hqosUrl");
		Global.hqosTargetName = xml.getStringValue("Ipsec.hqosTargetName");
		Global.hqosMethodName = xml.getStringValue("Ipsec.hqosMethodName");
		
		// ip变动上报(ipsec) 回调参数
		Global.ipChangeIpsecUrl = xml.getStringValue("Ipsec.ipChangeIpsecUrl");
		Global.ipChangeIpsecTargetName = xml.getStringValue("Ipsec.ipChangeIpsecTargetName");
		Global.ipChangeIpsecMethodName = xml.getStringValue("Ipsec.ipChangeIpsecMethodName");
		
		// ip变动上报(vxlan) 回调参数
		Global.ipChangeVxlanUrl = xml.getStringValue("Ipsec.ipChangeVxlanUrl");
		Global.ipChangeVxlanTargetName = xml.getStringValue("Ipsec.ipChangeVxlanTargetName");
		Global.ipChangeVxlanMethodName = xml.getStringValue("Ipsec.ipChangeVxlanMethodName");
		
		// vxlan新装上报回调参数
		Global.installVxlanUrl = xml.getStringValue("Ipsec.installVxlanUrl");
		Global.installVxlanTargetName = xml.getStringValue("Ipsec.installVxlanTargetName");
		Global.installVxlanMethodName = xml.getStringValue("Ipsec.installVxlanMethodName");
		
		// 智网回调是否开启 0：不开启  1：开启     智网为北研测试环境网络限制,默认为0不开启
		Global.istest = xml.getIntValue("Ipsec.istest");
	}
	/**
	 * 初始化属地 必须
	 * 
	 */
	public void initCityInfo() {
		CityDAO cityAct = new CityDAO();
		// 取所有属地的ID
		Global.G_CityIds = cityAct.getAllCityIdListCore();
		// 属地ID、属地名Map<city_id,city_name>
		
		Global.G_CityId_CityName_Map = cityAct.getCityIdCityNameMapCore();
		
		// 取属地ID和父属地ID的对应Map
		Global.G_City_Pcity_Map = cityAct.getCityIdPidMapCore();
		
		// 所有的属地的子属地List集合
		Global.G_City_Child_List_Map = cityAct.getAllCityIdChildListMap();
		
	}

	/**
	 * 城市对应测速url
	 */
	public void initCitySpeedMap(){
		SimulationSpeedDao speedDao=new SimulationSpeedDao();
		Global.citySpeedUrlMap=speedDao.getCitySpeedUrlMap();
	}
	/**
	 * 初始化属地 必须
	 * 
	 */
	public void initAreaInfo() {
		AreaDAO areaDAO = new AreaDAO();

		/** 域List<area_id> */
		Global.G_Area_Id_List = areaDAO.getAllAreaIdListCore();
		/** 域Map<area_id,parent_id> */
		Global.G_Area_PArea_Map = areaDAO.getAreaIdPidMapCore();
		/** 域Map<area_id,List<String>>* */
		Global.G_PArea_AreaList_Map = areaDAO.getAllAreaIdChildListMapCore();
		/** 属地域对应关系Map<city_id,area_id>* */
		Global.G_CityId_AreaId_Map = areaDAO.getCityIdAreaIdMapCore();
	}
	
	
	/**
	 * init fault code.
	 * 
	 * @return
	 */
	private boolean initFaultCode() {
		logger.debug("initFaultCode()");

		boolean flag = true;

		if(Global.G_instArea.equals("hb_dx") || Global.G_instArea.equals("sd_lt")){
			ArrayList<HashMap<String,String>> faultList  = InitDAO.getFaultCodeForInit();
			if(null!=faultList){
				for(HashMap<String,String> tempMap : faultList){
					 if(null != tempMap){
						    CpeFaultcodeOBJ  obj = new CpeFaultcodeOBJ();

						    obj.setFaultCode(StringUtil.getIntValue(tempMap, "fault_code"));
							
							obj.setFaultType(StringUtil.getIntValue(tempMap, "fault_type"));
							
							obj.setFaultName(StringUtil.getStringValue(tempMap, "fault_name"));
							
							obj.setFaultDesc(StringUtil.getStringValue(tempMap, "fault_desc"));
							
							obj.setFaultReason(StringUtil.getStringValue(tempMap, "fault_reason"));
							
							obj.setSolutions(StringUtil.getStringValue(tempMap, "solutions"));
							
							Global.G_Fault_Map.put(obj.getFaultCode(),obj);

					 }
				}
			}
		}else{
			Cursor cursor = InitDAO.getFaultCode();
			@SuppressWarnings("rawtypes")
			Map map = cursor.getNext();
	
			while (map != null) {
				
				CpeFaultcodeOBJ obj = new CpeFaultcodeOBJ();
				
				obj.setFaultCode(StringUtil.getIntValue(map, "fault_code"));
				
				obj.setFaultType(StringUtil.getIntValue(map, "fault_type"));
				
				obj.setFaultName(StringUtil.getStringValue(map, "fault_name"));
				
				obj.setFaultDesc(StringUtil.getStringValue(map, "fault_desc"));
				
				obj.setFaultReason(StringUtil.getStringValue(map, "fault_reason"));
				
				obj.setSolutions(StringUtil.getStringValue(map, "solutions"));
				
				
				Global.G_Fault_Map.put(obj.getFaultCode(),obj);
	
				map = cursor.getNext();
			}
		}
		logger.warn("Global.G_Fault_Map：{}",Global.G_Fault_Map);
		return flag;
	}

	/**
	 * <pre>
	 * 启动Socket服务器监听
	 * 根据配置文件中配置参数，如果需要启动Socket服务端，则根据端口和线程数启动服务。
	 * 注：需要增加Socket消息监听，就在该方法内增加。
	 * </pre>
	 */
	private void startServerSocket()
	{
		String enable = xml.getStringValue("ServerSocket.enable");
		logger.warn("enable server socket? [{}]", enable);
		if ("true".equalsIgnoreCase(enable))
		{
			int port = xml.getIntValue("ServerSocket.port");
			int processThreads = xml.getIntValue("ServerSocket.processTheads");
			SocketWorker worker = SocketWorker.newInstance(port, processThreads);
			// 在此增加消息监听器
			worker.addMsgListener(new PwdSynListener());
			worker.addMsgListener(new UserBandSynListener());
			worker.start();
		}
	}
}
