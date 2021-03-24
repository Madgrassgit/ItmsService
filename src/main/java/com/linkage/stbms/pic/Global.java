package com.linkage.stbms.pic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.linkage.commons.thread.ThreadPoolCommon;

public class Global 
{
	public static String  EVENT_CODE_BOOTSTRAP = "0";
	public static String  EVENT_CODE_BOOT = "1";
	public static String  EVENT_CODE_PERIODIC = "2";
	public static String  EVENT_CODE_M_REBOOT = "30";
	public static String  EVENT_CODE_LOGOUPDATERESULT = "69";
	
//	//corba接口 接受ACS存活消息的url
//	public static String MQ_ACS_AlIVE_URL = "tcp://192.168.2.14:61616";
//
//	//corba接口 接受RPC存活消息的url
//	public static String MQ_DEV_RPC_URL = "tcp://192.168.2.14:61616";
//	
//	/**调用ACS的clientid 用以标识模块的唯一性。*/
	public static final String CLIENT_ID = "PreProcess" + UUID.randomUUID();
//	
	/** read data from db: true:yes,read;false:no. */
	public static boolean G_ReadDataFromDbFlag = true;

	/** max exec_count of strategy */
	public static int G_MaxExecCountOfStrategy = 3;
	
	/** gw_type */
	public static short G_GwType = 4;
	
	//是否需要重启
	public static boolean IS_NEED_REBOOT = false;
	
	@SuppressWarnings("rawtypes")
	public static Map<String,String> G_SoftPath_Map = null;

	/** gatherid */
	public static String G_strGatherID = "1";
	
	/** serv sheet */
	public static ExecutorService G_ServThreadPool = null;

	/***/
	public static int G_ServThreadPool_SIZE = 50;
	
	/** 1:ITMS; 3:BBMS; 4:STB  */
	public static int G_SystemType = 4;
	
	/** 线程池最大线程数 */
	public static String MAX_THREAD_NUM;
	
	/** 是否休眠 */
	public static int IS_SLEEP;
	
	/** 休眠时间 */
	public static long SLEEP_TIME;
	
	/** 新工单类型(组装模板) */
	public static String NEW_SHEET_TYPE = "2";
	
	/** 旧工单类型(竖线分隔参数) */
	public static String OLD_SHEET_TYPE = "1";
	
	/** 软件升级的temp_id */
	public static String TEMP_ID_SOFT = "5";
	public static String SERVICE_ID_SOFT = "5";
	
	/** 已成功策略的STATUS */
	public static String SUCC_STATUS = "100";
	
	/** 已成功策略的RESULT_ID */
	public static String SUCC_RESULT_ID = "1";
	
	public static String MQ_ADDRESS;
	
	public static String ACS_IOR;
	
	public static Map<String, List<String>> CONF_TMPL_MAP = new HashMap<String, List<String>>();
	
	public static Map<String, String> SOFT_UP_MAP = new HashMap<String, String>();
	
	public static Map<String, String> SOFT_FILE_MAP = new HashMap<String, String>();
	
	public static String TOPIC_INFORM = "dev.inform";
	public static String TOPIC_VERSION = "dev.version";
	
	/** serv sheet */
	public static ThreadPoolCommon G_DevInformThreadPool = null;
	
	public static ThreadPoolCommon G_DealDevInformThreadPool = null;
	
	public static ThreadPoolCommon G_DealDevVersionThreadPool = null;
	
	public static ThreadPoolCommon G_DealDevLogoThreadPool = null;
	
	public static ThreadPoolCommon G_DealTaskThreadPool = null;
	
	/***/
	public static int G_DevInformThreadPool_SIZE = 15;
	
	/***/
	public static int G_DealDevInformThreadPool_SIZE = 15;
	
	/***/
	public static int G_DealDevVersionThreadPool_SIZE = 15;
	
	public static int G_DealDevLogoThreadPool_SIZE = 15;
	
	public static int G_DealTaskThreadPool_SIZE = 15;

	/** 策略查询 */
	public static ThreadPoolCommon G_StrategyThreadPool = null;
	
	/***/
	public static int G_StrategythreadPool_SIZE = 15;
	
	/**
	 * 机顶盒下发账号密码接入方式
	 */
	public static int SERV_TYPE_ID_STB_ACCOUNT = 120;
	
	/** STB的service_id */
	public static String SERVICE_ID_STB_ZERO = "120";
	
	public static String SERVICE_ID_STB_BATCH_CON = "7001";
	
	public static String SERVICE_ID_STB_LOGO_CON = "10";

	public static String Addressing_Type_PPPoE = "PPPoE";
	public static String Addressing_Type_DHCP = "DHCP";
	
	public static String SERVICE_ID_AlramConfig = "9";
	
	public static boolean ACS_DEV_MQ = false;
	
	/**属地List<city_id>*/
	public static ArrayList<String> G_CityIds = null;
	
	public static Map<String, String> G_City_Pcity_Map = null;
	
	/**属地Map<city_id,List<String>>**/
	public static Map<String, ArrayList<String>> G_City_Child_List_Map = null;
	
	//是否使用内存数据库加载策略数据
	public static boolean IS_MEM_STRATEGY = false;
	
	public static ThreadPoolCommon G_InitDataThreadPool = null;
	
	public static int G_INIT_MEM_THREAD_NUM = 15;
	
	public static final String SVR_POA_NAME = "PreProcess_Poa";
	public static final String SVR_OBJECT_NAME = "PreProcess";
	
	//机顶盒配置模块未拆分部署的情况下，使用stb_gw_serv_strategy 作为策略表
	public static String TABLENAME = "stb_gw_serv_strategy";
	public static String LOGTABLENAME = "stb_gw_serv_strategy_log";
	
	//通过存储过程取策略ID
	public static long MAX_UNUSED_STRATEGYID = -1L;
	
	public static long MIN_UNUSED_STRATEGYID = -1L;
	
	public static int SUM_UNUSED_STRATEGYID = 500;
	
	public static String DB_TYPE = "sybase";
	
	public static String DB_ORACLE = "oracle";
	public static String DB_SYSBASE = "sybase";
	
	public static boolean INIT_MEM_STRATEGY = false;
	
	public static int SQL_SIZE = 100;
	
	public static int MAX_QUEUE_NUM = 0;
	
	//XJ：新疆  JS：江苏 JX:江西
	public static String PROVINCE = "JS";
	
	public static String JS_PROVINCE = "JS";
	
	public static String JX_PROVINCE = "JX";
	
	public static String XJ_PROVINCE = "XJ";
	
	public static String SYSTEM_NAME = "STB_";
	
	//批量配置软件升级是否开启
	public static boolean IS_CONF_SOFTUP = false;
	
	//批量参数配置是否开启
	public static boolean IS_BATCH_CONF = false;
	
	//是否开启开机画面任务
	public static boolean IS_LOGO_CONF = false;
	
	public static long MAX_UNUSED_CON_UCID = -1L;
	
	public static long MIN_UNUSED_CON_UCID = -1L;
	
	public static int SUM_UNUSED_CON_UCID = 50;
	
	public static String MQ_BATCH_CON_URL = null;
	
	public static String TOPIC_BATCH_CON_TASK = "stbBatchCon.task";
	
	//通过存储过程取策略ID
	public static long MAX_UNUSED_PICUCID = -1L;
	
	public static long MIN_UNUSED_PICUCID = -1L;
	
	public static int SUM_UNUSED_PICUCID = 50;
	
	/**
	 * 零配置结果0成功，1失败
	 */
	public static int ZERO_CONFIG_RESULT_FAILED = 1;
	
	/**
	 * 零配置结果0成功，1失败
	 */
	public static int ZERO_CONFIG_RESULT_SUCCESS = 0;
	
	/**
	 * 零配置更新超时时间
	 */
	public static int ZERO_CONFIG_RESULT_UPDATETIME = 60 * 5;
	
	public static int IS_DHCP = 1;
}






























