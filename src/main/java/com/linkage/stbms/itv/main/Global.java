
package com.linkage.stbms.itv.main;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ACS.RPCManager;
import PreProcess.PPManager;
import ResourceBind.BlManager;
import StbCm.CMManager;
import SuperGather.SuperGatherManager;

import com.linkage.commons.jms.obj.MQConfig;
import com.linkage.commons.util.StringUtil;

/**
 * @author Jason(3412)
 * @date 2009-12-16
 */
public class Global
{

	public static String G_instArea = null;
	public static int G_Sysytem_Type = 1;
	
	/** SuperGManager CORBA */
	public static SuperGatherManager G_SuperGatherManager = null;
	/** ACS CORBA */
	public static RPCManager G_ACSManager = null;
	/** PreProcess CORBA */
	public static PPManager G_PPManager = null;
	/**the IOR obj of Resource Bind CORBA*/

	public static BlManager G_BlManager_STB = null;
	/** rpcType */
	public static final int rpcType = 2;
	/** rpcType test */
	public static final int rpcTestType = 0;
	/** priority */
	public static final int priority = 1;
	/**
	 * cpe fault code map(fault_code, fault_desc) 错误代码对应map,启动时加载到内存
	 */
	public static Map<Integer, String> G_Fault_Map = new HashMap<Integer, String>();
	/** ITMS命名空间 */
	public static final String ITMS_NAMESPACE = "http://192.168.32.231:8090/ItmsService/services/ItmsService";
	/** ITMS方法名 */
	public static final String ITMS_METHOD_GETUSERMODEMINFO = "getUserModemInfo";
//	/** ITMS的WEBSERVICE地址 */
	public static final String ITMS_URL = StbServGlobals.getLipossProperty("ITMS.URL");
	/** webservice前缀 */
	public static final String PREFIX = "nsl";
	/** Action前缀 */
	public static final String ACTION_PREFIX = "urn:";
//	/** 业务平台的WEBSERVICE地址 */
//	public static final String BUSS_URL = StbServGlobals
//			.getLipossProperty("BussManager.URL");
//	/** 业务平台的WEBSERVICE参数-管理员账号 */
//	public static final String BUSS_ADMIN = StbServGlobals
//			.getLipossProperty("BussManager.ADMIN");
//	/** 业务平台的WEBSERVICE参数-管理员密码 */
//	public static final String BUSS_PWD = StbServGlobals
//			.getLipossProperty("BussManager.PWD");
	
	/** 通过haproxy转发 调业务管理平台的重置业务管理平台密码功能  */
	public static final String BalanceHaproxyURL = StbServGlobals
			.getLipossProperty("BalanceHaproxy.URL");
	/**
	 * STB采集相关 paramType
	 */
	// 采集配置文件中所有参数
	public static int GATHER_ALL = 0;
	// 1：STBDevice
	public static int GATHER_STBDevice = 1;
	// 12：Capabilities
	public static int GATHER_Capabilities = 12;
	// 2：UserInterface
	public static int GATHER_UserInterface = 2;
	// 3：LAN
	public static int GATHER_LAN = 3;
	// 4：X_CTC_IPTV
	public static int GATHER_X_CTC_IPTV = 4;
	// 41：ServiceInfo
	public static int GATHER_X_CTC_IPTV_ServiceInfo = 41;
	// 32: TraceRoute
	public static int GATHER_TRACEROUTE = 32;
		/** IPOSS命名空间 */
	public static final String IPOSS_NAMESPACE = "http://ws.fault.iptv.liposs.module.linkage.com";
	/** IPOSS方法名 */
	public static final String IPOSS_METHOD_GETNETDEVINFO = "getNetDevInfo";
	public static final String IPOSS_METHOD_GETALLNETDEVINFO = "getALLNetDevInfo";
	public static final String IPOSS_METHOD_GETPVCINFO = "getPVCInfo";
//	/** IPOSS的WEBSERVICE地址 */
//	public static final String IPOSS_URL = StbServGlobals.getLipossProperty("Liposs.URL");
//	/** IPOSS的WEBSERVICE地址 */
//	public static final String IPOSS_PVC_URL = StbServGlobals.getLipossProperty("Liposs.URL2");
	
//	/** CERTUS的HTTP地址 */
//	public static final String CERTUS_URL = StbServGlobals.getLipossProperty("Certus.URL");
//	/** CERTUS的HTTP地址2 */
//	public static final String CERTUS2_URL = StbServGlobals.getLipossProperty("Certus2.URL");
//	/** 用户分组管理接口  */
//	public static final String CERTUS_USER_GROUP_URL = StbServGlobals.getLipossProperty("Certus.UserGroupWSURL");
//	/** 终端放装接口  */
//	public static final String CERTUS_STB_CUD_Port_URL = StbServGlobals.getLipossProperty("Certus.StbCUDPortURL");
//	
//	public static final String CERTUS_NBI_SERVICE_PORT_URL = StbServGlobals.getLipossProperty("Certus.NbiServicePortUrl");

	/** 无设置IP时IP值 */
	public static final String NO_IP = "-1";
	/** 连通状态：通 */
	public static final String STATE_GREEN = "1";
	/** 连通状态：不通 */
	public static final String STATE_RED = "0";
	/** 连通状态：未检测到 */
	public static final String STATE_GRAY = "-1";
	/** ping param **/
	public static final int PING_PACK_SIZE_XJ = 32;
	public static final int PING_PACK_NUM_XJ = 2;
	public static final int PING_TIME_OUT_XJ = 10;
	
	public static final int PING_PACK_SIZE = StringUtil.getIntegerValue(StbServGlobals
			.getLipossProperty("PingParam.PackSize"));
	public static final int PING_PACK_NUM = StringUtil.getIntegerValue(StbServGlobals
			.getLipossProperty("PingParam.PackNum"));
	public static final int PING_TIME_OUT = StringUtil.getIntegerValue(StbServGlobals
			.getLipossProperty("PingParam.Timeout"));
	public static final int PING_DSCP = StringUtil.getIntegerValue(StbServGlobals
			.getLipossProperty("PingParam.Dscp"));
	/** TRACEROUTE PARAM **/
	public static final int TRACEROUTE_MAX_HOP_NUM = StringUtil
			.getIntegerValue(StbServGlobals
					.getLipossProperty("TraceRouteParam.MaxHopCount"));
	public static final int TRACEROUTE_PACK_SIZE = StringUtil
			.getIntegerValue(StbServGlobals.getLipossProperty("TraceRouteParam.PackSize"));
	public static final int TRACEROUTE_TIME_OUT = StringUtil
			.getIntegerValue(StbServGlobals.getLipossProperty("TraceRouteParam.Timeout"));
	public static final int TRACEROUTE_DSCP = StringUtil.getIntegerValue(StbServGlobals
			.getLipossProperty("PingParam.Dscp"));
	
	/**调用ACS的clientid 用以标识模块的唯一性。*/
	public static  String CLIENT_ID = "ItmsService";
	public static  String SYSTEM_NAME;
	
	/** 针对命令类型：0：test connect、1：普通命令（获取参数、设置参数）、2：诊断命令、3：文件下发（软件升级） */
	public static final int rpcType_TEST_CONNNECT = 0;
	public static final int rpcType_NORMAL = 1;
	public static final int rpcType_DIAG = 2;
	public static final int rpcType_SOFT_UPGRADE = 3;
	
	/** 1,hig;2,low */
	public static final int ACS_PRIORITY = 1;
	
	/** 机顶盒运营画面 数据入库后 发布MQ消息，通知配置模块  */
//	public static String MQ_PIC_UP_TASK_ENAB = null;
//	public static String MQ_PIC_UP_TASK_URL = null;
//	public static String MQ_PIC_UP_TASK_TOPIC = null;
	public static Map<String, MQConfig> MQ_POOl_MAP = new HashMap<String, MQConfig>();
	
//	/** 机顶盒运营画面 策略同步生效接口 服务端的URL（IPTV业务管理平台为客户端，综合网管系统为服务端） */
//	public static String STB_PIC_POLICY_SYNC_EFF_ADDRESS = null;
//	/** 机顶盒运营画面 策略同步接口 服务端的URL（IPTV业务管理平台为客户端，综合网管系统为服务端） */
//	public static String STB_PIC_POLICY_SYNC_ADDRESS = null;
	/** 用户存放附件 */
//	public static String LOCAL_FILE_PATH = null;
	/** IPTV业务平台FTP的URL 用于存放策略执行结果文件 */
//	public static String IPTV_BUSINESS_PLAT_FORM_FTP_URL = null;
	
//	/** 策略同步结果 按指定时间统计 */
//	public static String SCHEDULE_TIME = null;
//	/** 定时统计功能是否开启，0表示不开启，1表示开启 */
//	public static String SCHEDULE_TIME_ENAB = null;
	
	/** 业务平台与综合网管属地关系映射表    key是业务平台的属地，value是综合网管的属地 */
	public final static Map<String, String> CITY_MAP = new LinkedHashMap<String, String>(13);
	
	private static long taskId = 1L;
	
	// 获取开机画面 任务定制表(gw_pic_task)中的task_id
	public static synchronized long getTaskId(){
		return ++taskId;
	}
	public static String ACS_OBJECT_NAME = "ACS";
	
	/**预处理平台改密接口地址*/
	public static String  JX_MODIFY_PWD_URL = "";
	/**预处理平台改密接口鉴权账号*/
	public static String  JX_MODIFY_PWD_USERNAME = "";
	/**预处理平台改密接口鉴权密码*/
	public static String  JX_MODIFY_PWD_PASSWORD = "";
	

	/**通过MAC获取接入账户的URL*/
	public static String STBSERVICE_URL = "";
	
	/** 调用配置模块类型1：corba，2：发送消息*/
	public static String PRE_PROCESS_TYPE = "1";
	
	/** 终端类型  1：ITMS  2：BBMS 4:机顶盒 */
	public static final String GW_TYPE_ITMS = "1";  
	public static final String GW_TYPE_BBMS = "2";
	public static final String GW_TYPE_STB = "4";
	
	public static CMManager G_PPManager_STB = null;
}
