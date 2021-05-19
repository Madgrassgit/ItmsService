package com.linkage.itms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.rubyeye.xmemcached.MemcachedClient;
import PreProcess.PPManager;
import ResourceBind.BlManager;
import SoftUp.SoftUpManager;
import StbCm.CMManager;
import SuperGather.SuperGatherManager;

import com.linkage.commons.jms.MQPublisher;
import com.linkage.commons.jms.obj.MQConfig;
import com.linkage.commons.thread.ThreadPoolCommon;
import com.linkage.init.obj.CpeFaultcodeOBJ;
import com.linkage.itms.dispatch.util.memcached.MemcachedPool;


/**
 * @author Jason(3412)
 * @date 2010-6-21
 */
public class Global {


	// begin add by chenjie 2011-12-9
	
	/**
	 * 是
	 */
	public static int DO = 1;
	
	/**
	 * Web 配置文件名称
	 */
	public static final String CONFIG_FILENAME = "ItmsService_cfg.xml";
	
	/**cfg path*/
	public static String G_strCfgPath = null;
	
	/** InstArea  add by zhangchy 2012-01-17 */
	public static String G_instArea = null;

	/**
	 * server 部署路径
	 */
	public static String G_ServerHome = null;
	
	/**
	 * client id
	 */
	public static String ClIENT_ID = null;	
	
	/** 
	 * MQ acs.alive
	 */

	/**
	 * MQ dev.rpc
	 */

	/**
	 * MQ dev.servinfo
	 */

	public static Map<String, MQConfig> MQ_POOl_MAP = new HashMap<String, MQConfig>();
	
	/**
	 * Radius WebService接口
	 */
	public static String RADIUS_NAMESPACE="";
	public static String RADIUS_URL="";
	
	/**
	 * Radius Socket接口
	 */
	public static String RADIUS_IP="";
	public static String RADIUS_PORT="";

	/**
	 * add by zhangchy 2012-07-23
	 * 
	 * 回参中如果使用encoding="GBK"的话，宁夏联调客户端无法转义，此值用于将encoding="GBK"，替换为encoding="UTF-8"
	 * 此配置项只对方法call,bindInfo,	bind,serviceDone,devOnline,release,queryBssSheetAndOpenStatus,queryDeviceConfig
	 * 有影响，对其他方法没有影响
	 */
	public static String codeTypeValue = "";
	
	/** 系统名称，用于却分是用于ITMS还是BBMS，以后融合的话，此ItmsService模块会部署两份，一份用于ITMS，一份用于BBMS */
	public static String SYSTEM_NAME;
	public static String SYSTEM_ITMS = "ITMS";
	public static String SYSTEM_BBMS = "BBMS";
	public static String SYSTEM_GTMS = "GTMS";
	/** 终端类型  1：ITMS  2：BBMS 4:机顶盒 */
	public static final String GW_TYPE_ITMS = "1";  
	public static final String GW_TYPE_BBMS = "2";
	public static final String GW_TYPE_STB = "4";
	
	/** 定义String的常量，用于拼接 */
	public static String SYSTEM_ITMS_PREFIX = "ITMS_"; 
	public static String SYSTEM_BBMS_PREFIX = "BBMS_"; 
	public static String SYSTEM_STB_PREFIX = "STB_"; 
	
	public static String SYSTEM_ITMS_SUFFIX = "_itms";
	public static String SYSTEM_BBMS_SUFFIX = "_bbms";
	
	public static String SYSTEM_ACS = "ACS";
	/**
	 * 采集
	 */
	public static String SYSTEM_SUPER_GATHER = "SuperGather";
	/***
	 * 配置
	 */
	public static String SYSTEM_PREPROCESS = "PreProcess";
	public static String SYSTEM_PREPROCESS_POA = "PreProcess_Poa";
	public static String SYSTEM_BUSINESS_LOGIC = "BusinessLogic";
	/**
	 * 软件升级
	 */
	public static String SYSTEM_SOFT_WARE_UPGRADE = "SoftUp";
	/**
	 * 软件升级
	 */
	public static String SYSTEM_SOFT_WARE_UPGRADE_POA = "SoftUp_Poa";
	/**
	 * 运营商 CTC:电信 CUC:联通 CMCC:移动
	 */
	public static String G_OPERATOR = "CTC";
	/**
	 * 工单模块返回结果成功
	 */
	public static final String RESULTCODE_000 = "000";
	/**
	 * 是否初始化城市对应测速url  1：初始化   2：不初始化
	 */
	public static String IS_INIT_CITY_SPEED = null;

	
	public static int SPIT_NUM_LARGE_VAL = 3800;
	public static int IS_LARGE_VALUE = 0;
	
	/**吉林电信调3A接口的地址*/
	public static String G_3A_SERVER="136.160.101.12";
	/**吉林电信调3A接口的端口*/
	public static int G_3A_PORT=9656;
	
	public static Map<String,String> G_TABLENAME_MAP = new HashMap<String, String>();
	/***
	 * 城市对应测速url
	 */
	public static Map<String,String> citySpeedUrlMap=new HashMap<String,String>();
    public static MemcachedPool memcachedPool;
	public static Map<String, MemcachedClient> memcachedMap = new ConcurrentHashMap<String, MemcachedClient>();

    /** 获取前缀名称 */
	public static String getPrefixName (String systemName){
		if(SYSTEM_ITMS.equals(systemName)){
			return SYSTEM_ITMS_PREFIX;
		} else if(SYSTEM_BBMS.equals(systemName)){
			return SYSTEM_BBMS_PREFIX;
		} 
		else if(SYSTEM_GTMS.equals(systemName)){
			return SYSTEM_ITMS_PREFIX;
		}
		else {
			return "";
		}
	}
	
	/** 获取前缀名称 */
	public static String getPrefixName (String systemName,String gwType){
		if(SYSTEM_ITMS.equals(systemName)){
			return SYSTEM_ITMS_PREFIX;
		} else if(SYSTEM_BBMS.equals(systemName)){
			return SYSTEM_BBMS_PREFIX;
		} 
		 else if(SYSTEM_GTMS.equals(systemName)){
			 //gw_type=1:ITMS 、2:BBMS
			 if(Global.GW_TYPE_ITMS.equals(gwType))
			 {
				 return SYSTEM_ITMS_PREFIX; 
			 }
			 else if(Global.GW_TYPE_BBMS.equals(gwType))
			 {
				 return SYSTEM_BBMS_PREFIX;
			 }
			 else if(Global.GW_TYPE_STB.equals(gwType))
			 {
				 return SYSTEM_STB_PREFIX;
			 }
			 return "";
			} 
		else {
			return "";
		}
	}
	/**  获取后缀名称 */
	public static String getSuffixName (String systemName){
		if(SYSTEM_ITMS.equals(systemName)){
			return SYSTEM_ITMS_SUFFIX;
		} else if(SYSTEM_BBMS.equals(systemName)){
			return SYSTEM_BBMS_SUFFIX;
		} else {
			return null;
		}
	}
	
	
	/** 命令类型：0：检测连接、1：普通命令（获取参数、设置参数）、2：诊断命令、3：文件下发（软件升级）*/
	public static int RpcTest_Type = 0;
	public static int RpcCmd_Type = 1;
	public static int DiagCmd_Type = 2;
	public static int FileCmd_Type = 3;
	
	/** priority:1,hig;2,low  */
	public static int Priority_Hig = 1;
	public static int Priority_Low = 2;
	
	// end add by chenjie 2011-12-9

	/** PreProcess CORBA */
	public static PPManager G_PPManager = null;
	
	/** the IOR obj of PreProcess CORBA*/
	
	public static PPManager G_PPManager_ITMS = null;
	public static PPManager G_PPManager_BBMS = null;
	public static CMManager G_PPManager_STB = null;

	/** SuperGManager CORBA */
	public static SuperGatherManager G_SuperGatherManager = null;

	/**the IOR obj of Resource Bind CORBA*/
	public static BlManager G_BlManager_ITMS = null;
	public static BlManager G_BlManager_BBMS = null;
	public static BlManager G_BlManager_STB = null;
	
	/** the table name of user. */
	public static String G_UserTab = null;

	/** cpe fault code map(fault_code, CpeFaultcodeOBJ) */
	public static Map<Integer, CpeFaultcodeOBJ> G_Fault_Map = new HashMap<Integer, CpeFaultcodeOBJ>();
	
	
	/** 软件升级 CORBA */
	public static SoftUpManager G_SUManager = null;  
	
	/** 休眠时间   默认值为5000毫秒  */
	public static int sleepTime = 5000;
	
	public static int jxFtthSleepTime = 300;
	public static String jxTestFtthUrl = "";
	public static String jxTestFtthMethod = "";


	public static int TEST_SPEED_TIME = 0 ;
	
	public static String XMEM_TYPE = "M";

	/**
	 * @author onelinesky(4174)
	 * 
	 * 以下属地的全局变量，理论上不允许私有调用，需要调用的话，请使用CityDAO来调用
	 * 另外以下初始化的顺序有以下
	 * 1、G_CityIds
	 * 2、G_CityId_CityName_Map
	 * 3、G_City_Pcity_Map
	 * 4、G_City_Child_List_Map
	 * 如初始化顺序有变化，则出现初始化失败的情况
	 */
	/**属地List<city_id>*/
	public static ArrayList<String> G_CityIds = null;
	/**属地ID、属地名Map<city_id,city_name>*/
	public static Map<String,String> G_CityId_CityName_Map = null;
	/**属地Map<city_id,parent_id>*/
	public static Map<String, String> G_City_Pcity_Map = null;
	/**属地Map<city_id,List<String>>**/
	public static Map<String, ArrayList<String>> G_City_Child_List_Map = null;
	
	/**
	 * @author onelinesky(4174)
	 * 
	 * 以下域的全局变量，理论上不允许私有调用，需要调用的话，请使用AreaDAO来调用
	 * 另外以下初始化的顺序有以下
	 * 1、G_Area_Id_List
	 * 2、G_Area_PArea_Map
	 * 3、G_PArea_AreaList_Map
	 * 4、G_CityId_AreaId_Map
	 * 如初始化顺序有变化，则出现初始化失败的情况
	 */
	/**域List<area_id>*/
	public static ArrayList<String> G_Area_Id_List = null;
	/**域Map<area_id,parent_id>*/
	public static Map<String, String> G_Area_PArea_Map = null;
	/**域Map<area_id,List<String>>**/
	public static Map<String, ArrayList<String>> G_PArea_AreaList_Map = null;
	/**属地域对应关系Map<city_id,area_id>**/
	public static Map<String, String> G_CityId_AreaId_Map = null;
	
	
	/** BSS工单接口的服务器地址和端口 */
	public static String G_ITMS_Sheet_Server = "127.0.0.1";
	public static int G_ITMS_Sheet_Port = 20000;
	
	/** 江苏移动ITMS 给EServer发工单 测试 */
	public static String G_ITMS_SHEET_SERVER_CHINA_MOBILE = "";
	public static String G_ITMS_SHEET_PORT_CHINA_MOBILE = "";
	
	/** 重庆激活工厂复位返回接口*/
	public static String G_ITMS_FINISH_URL = "http://136.3.243.201:7337/services/FactoryResetService";
	public static String G_ITMS_SERV_METHOD = "getFactoryResetReturnDiag";
	public static String G_ITMS_NAME_SPACE = "http://api.server.factoryreset.endpoint.uip.cqpf.cqcis.com/";
	public static String G_ITMS_REQUEST_NAME = "arg0";
	public static String G_ITMS_RESPONSE_NAME = "return";
	
	/** 是否开启软件升级功能 */
	public static String INIT_SOFT_UP_ENABLE = "";
	
	public static long MAX_UNUSED_STRATEGYID = -1L;
	
	public static long MIN_UNUSED_STRATEGYID = -1L;
	
	public static int SUM_UNUSED_STRATEGYID = 50;
	
	/** bl thread pool */
	public static ThreadPoolCommon G_BlThreadPool = null;
	public static ThreadPoolCommon G_CallBackThread = null;
	public static ThreadPoolCommon G_InitDataThreadPool = null;
	public static ThreadPoolCommon G_AsynchronousThread = null;
	/**
	 * RSA算法公钥，在ItmsService_cfg.xml中配置
	 */
	public static String RSA_PUBLIC_KEY = null;
	
	/**
	 * RSA算法私钥，在ItmsService_cfg.xml中配置
	 */
	public static String RSA_PRIVATE_KEY = null;
	
	public static String idsServiceUrl = null;
	
	/**JSDX_ITMS-REQ-20140930-ZJQ-001（ITMS与智能网管同步账号接口)
	 * 从智能网管中取文件入表
	 * */
	public static String ip = "";
	
	public static String port = "";
	
	public static String username = "";
	
	public static String password = "";
	
	public static String localDir = "";
	public static String isVxlan = "0";
	/**
	 * HUG用户及语音业务报表统计相关常量
	 */
	/**HGU用户*/
	public static String HGU_TYPE="1";
	/**VOIP语音业务*/
	public static String VOIP_TYPE="14";
	/**日视图*/
	public static String DAY="1";
	/**周视图*/
	public static String WEEK="2";
	/**月视图*/
	public static String MONTH="3";

	/**JXDX-ITMS-REQ-20150918-WUWF-001(ITMS平台业务下发状态过滤需求)
	 * 业务下发时，判断机制
	 * */
	public static String STRATEGY_TABNAME = "";
	
	/** 调用分配置模块的serviceId*/
	public static String SERV_SERVICE_ID;
	public static String SOFT_SERVICE_ID;
	public static String BATCH_SERVICE_ID;
	public static String STRATEGY_SERVICE_ID;
	

	public static String HTTPSPEED_URL = "";
	public static String HTTPSPEED_METHOD = "";
	public static String HTTPSPEED_NAMESPACE = "";
	public static String HTTPSPEED_SERVICENAMESOAP = "";
	/** 是否发送corba心跳 */
	public static int IsSendCorbaHeartBeat = 0;
	/** 是否发送corba心跳 */
	public static int SendCorbaHeartBeatPeriod = 20;
	
	/** 调用配置模块类型1：corba，2：发送消息*/
	public static String PRE_PROCESS_TYPE = "1";
	
	/** 批量测速的上报地址*/
	public static String TEST_SPEED_REPORT_URL = "";
	
	/** 批量测速的下载地址*/
	public static String TEST_SPEED_DOWN_URL = "";
	
	/** 调用绑定模块类型1：corba，2：发送消息*/
	public static String RESOURCE_BIND_TYPE = "1";
	
	/** MQ配置文件对应的MAP*/
	public static Map<String, MQConfig> MQ_POOL_PUBLISHER_MAP = new HashMap<String, MQConfig>();
	
	public static Map<String, MQConfig> MQ_POOL_PUBLISHER_MAP_STB = new HashMap<String, MQConfig>();
	
	/** cm.Interface主题的生产者*/
	public static MQPublisher PROCESS_PUBLISHER = null;
	
	public static MQPublisher PROCESS_PUBLISHER_STB = null;
	
	/** res.Interface主题的生产者*/
	public static MQPublisher RESOURCE_BIND_PUBLISHER = null;
	
	public static MQPublisher RESOURCE_BIND_PUBLISHER_STB = null;

	/**
	 * TestSpeedService中需要的测速路径
	 */
	public static String testURL;
	/**
	 * TestSpeedService中需要的上报路径
	 */
	public static String reportURL;


	/**
	 * 北向转发发往的工单模块地址
	 */
	public static String ESERVER_URL;
	
	/**
	 * 是否转发到原rms
	 */
	public static String is_SendToOldRMS = "0";
	
	/**
	 * 华为url
	 */
	public static String ESERVER_OLD_URL;
	/**
	 * 华为url
	 */
	public static String BACK_FUKAI_URL;
	public static ThreadPoolCommon G_SendThreadPool = null;
	
	public static ThreadPoolCommon G_TestSpeedThreadPool = null;
	
	/********************云网关相关配置 begin***********************/
	/**
	 * 企业网关和家庭网关用户表，业务表是否融合，1是，0否
	 **/
	public static int IPSEC_ISFUSED = 0;
	/**
	 * ipsec 开通、修改业务回调参数
	 **/
	public static String ipsecUrl;
	public static String ipsecTargetName;
	public static String ipsecMethodName;
	/***
	 *  vxlan 开通、修改、删除业务回调参数
	 */
	public static String vxlanUrl;
	public static String vxlanTargetName;
	public static String vxlanMethodName;
	
	/***
	 *  直通车 开通、修改、删除业务回调参数
	 */
	public static String hqosUrl;
	public static String hqosTargetName;
	public static String hqosMethodName;
	
	/***
	 * ip变动上报(ipsec) 回调参数
	 */
	public static String ipChangeIpsecUrl;
	public static String ipChangeIpsecTargetName;
	public static String ipChangeIpsecMethodName;
	/**
	 * ip变动上报(vxlan) 回调参数
	 */
	public static String ipChangeVxlanUrl;
	public static String ipChangeVxlanTargetName;
	public static String ipChangeVxlanMethodName;
	/**
	 * vxlan新装上报回调参数
	 */
	public static String installVxlanUrl;
	public static String installVxlanTargetName;
	public static String installVxlanMethodName;
	/***
	 * 智网回调是否开启 0：不开启  1：开启     智网为北研测试环境网络限制,默认为0不开启
	 */
	public static int istest = 0;
	/********************云网关相关配置 end***********************/

	/**江西电信测速url*/
	public static String DOWNLOADURL = null;
	/**江西电信专线测速url*/
	public static String SPECIALDOWNLOADURL = null;
	/**JXDX-REQ-ITMS-20200810-WWF-001(ITMS+家庭网关对外接口-2.29接口调整慢必赔) .doc  天翼网关2.0 版本列表  ; 分割*/
	public static String Tywg2UpList = null;

	public static final String CTC = "CTC";

	public static final String JXDX = "jx_dx";
	public static final String XJDX = "xj_dx";
	public static final String JSDX = "js_dx";
	public static final String AHDX = "ah_dx";
	public static final String NXDX = "nx_dx";
	public static final String SDDX = "sd_dx";
	public static final String JLDX = "jl_dx";
	public static final String HLJDX = "hlj_dx";
	public static final String NMGDX = "nmg_dx";
	public static final String CQDX = "cq_dx";
	public static final String HBDX = "hb_dx";

	public static final String JLLT = "jl_lt";
	public static final String HBLT = "hb_lt";
	public static final String SXLT = "sx_lt";
	public static final String SDLT = "sd_lt";
	public static final String NMGLT = "nmg_lt";
	public static final String AHLT = "ah_lt";

	public static final String OK = "OK";
	public static final String ERROR = "ERROR";

	/** 数据库类型 */
	public static int mysql = 3;
	
	public static final int USERTYPENAME = 1;
	public static final int USERTYPELOID = 2;
	public static final int USERTYPEDEVSN = 3;
	
	public static final int DEVSNLENTH = 6;
	
}
