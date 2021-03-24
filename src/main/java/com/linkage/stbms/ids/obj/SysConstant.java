/**
 * 
 */
package com.linkage.stbms.ids.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.linkage.commons.thread.ThreadPoolCommon;


import PreProcess.PPManager;
import StbCm.CMManager;

/**
 * SysConstant
 * 
 * @author Eric(qixq@)
 * @version 1.0
 * @since 1.0
 * @date 2011-5-31
 */
public class SysConstant {

	/** the path */
	public static String G_HomePath = null;
	public static String G_ConfPath = null;
	
	/**
	 * 业务配置模块文件加载常量
	 */
	/** 所属系统名称 **/
	public static String ServerName = null;
	/** 系统版本 **/
	public static String Version = null;
	/** 系统类型 **/
	public static int SystemType = 1;
	/** 程序所在地 **/
	public static String InstArea_Name = null;
	/** 程序安装地 **/
	public static String InstArea_ShortName = null;
	/** 目前程序安装版本 **/
	public static String InstArea_Version = null;
	/** 是否为省中心 **/
	public static boolean InstArea_IsSZX = true;
	/** corba name */
	public static String corba_corbaBl_blCorbaName = null;
	/** corba name */
	public static String corba_corbaCM_name = null;
	/** enab */
	public static int corba_corbaCM_enab = -1;
	/** thread pool size: bl. */
	public static int thread_blPoolSize = 0;
	/** 解绑时业务是否回归. */
	public static int serv_unBindServEnab = 0;
	/** 是否自动下发业务. */
	public static int serv_userServEnab = 0;
	/** 绑定时是否检查设备和用户的设备类型匹配<br>0为不检查，1为检查(默认)*/
	public static int serv_checkDeviceTypeMatch = 1;
    /** 绑定时是否检查设备和用户的上行类型匹配<br>0为不检查，1为检查(默认)*/
    public static int serv_checkAccessTypeMatch = 1;
	/** 采集点 */
	public static String gatherId = "1";

	public static String clientId = null;
	
	/** 接受acs的infrom */
	public static String itmsObjectName = "itms_ACS";
	
	/** 是否需要重新加载数据 */
	public static int memcachedDataInit = 0;
	/** 是否需要查询业务用户表 */
	public static int selectHgwcustServ = 1;
	/** 是否业务用户表为全量加载 */
	public static int hgwcustServInfoAllLoad = 0;
	/** bind1事件是否绑定开关 */
	public static int isBind1 = 0;
	/** bind事件是否绑定开关 */
	public static int isBind = 0;
	/** bind2事件是否绑定开关 */
	public static int isBind2 = 0;
	/** 0 boot事件是否绑定开关 */
	public static int is0Boot = 0;
	/** 1 boot事件是否绑定开关 */
	public static int is1Boot = 0;
	/** 新设备是否绑定开关 */
	public static int isNew = 0;
	/**
	 * 业务配置模块相关业务常量
	 */
	/** bl thread pool */
	public static ThreadPoolCommon G_BlThreadPool = null;
	public static ThreadPoolCommon G_InitDataThreadPool = null;
	/**
	 * 用户类型
	 */
	public static Map<String, String> userTypeMap = new ConcurrentHashMap<String, String>(10);
	/**
	 *计数器配置
	 */
	public static Map<String, Long> countCfgMap = new ConcurrentHashMap<String, Long>(10);
	/**
	 * 属地Map<city_id,parent_id>
	 */
	public static Map<String, String> G_City_Pcity_Map = null;
	
	public static String BIND_STATUS_SUSS = "1";
	public static String BIND_STATUS_UNKNOW_clientId = "0";
	public static String BIND_STATUS_PARAM_ERROR = "-1";
	
	public static String BIND_RESULT_SUSS = "1";
	public static String BIND_RESULT_PARAM_ERROR = "-1";
	public static String BIND_RESULT_DEVICETYPE_ERROR = "-2";//设备类型不匹配
    public static String BIND_RESULT_ACCESSTYPE_ERROR = "-7";//接入类型不匹配
	public static String BIND_RESULT_NOUSER_ERROR = "-3";
	public static String BIND_RESULT_NODEVICE_ERROR = "-4";
	public static String BIND_RESULT_NOBIND_ERROR = "-5";
	public static String BIND_RESULT_DBOPARATE_ERROR = "-6";
	public static String BIND_RESULT_GETITVACCT_ERROR = "-7";
	public static String BIND_RESULT_UNKNOW_ERROR = "-10000";
	/**
	 * 配置模块的corba相关常量
	 */
	/** cm corba client */
	public static PPManager cmManager = null;
	/**
	 * 机顶盒与ITMS融合版本调用配置模块Corba接口
	 */
	public static CMManager cmManagerStbMerge = null;
	
	/**
	 * 为初始化加载数据定义数据,该线程是否初始化完成，完成true,否则false
	 */
	public static Map<String,Boolean> initDataThreadStatus = null;
	 
	/** 命令类型：0：检测连接、1：普通命令（获取参数、设置参数）、2：诊断命令、3：文件下发（软件升级）*/
	public static int RpcTest_Type = 0;
	public static int RpcCmd_Type = 1;
	public static int DiagCmd_Type = 2;
	public static int FileCmd_Type = 3;
	
	/** priority:1,hig;2,low  */
	public static int Priority_Hig = 1;
	public static int Priority_Low = 2;
	public static String STBSERVICE_URL = "";
	/**db type*/
	public static String G_DBType = "";
	//是否定期巡检内存
	public static int inspectMemEnable = 0;
	//巡检内存库周期
	public static int inspectMemPeriod = 30;
	//巡检内存库时间
	public static String inspectMemTime = "2:00:00";
	//绑定业务线程是否休眠
	public static int bindThreadSleepEnable = 0;
	//休眠时间
	public static int bindThreadSleepTime = 5000;
	//零配置软件升级判断版本前休眠，跟acs同步，单位：毫秒
	public static int updateThreadSleepTime = 150;
	/**
	 * 零配置机顶盒状态
	 *  0，新机顶盒，需要下发配置，
		1，机顶盒内配置正常的老机顶盒，不需下发配置，
		2，老机顶盒维修，需要下发配置，
		3，特殊机顶盒，不需要下发配置，
		4，软件版本升级失败机顶盒，不能下发配置，(重复三次)
		5，需要进行串号配置的机顶盒
		6，零配置流程失败机顶盒，需要现场装维人员手工配置。
		7，机顶盒移机
		8，机顶盒销户。
		9，正在自动配置中的机顶盒。
	 */
	public static int ZERO_CFG_NEW = 0;
	public static int ZERO_CFG_GOOD = 1;
	public static int ZERO_CFG_WEIXIU = 2;
	public static int ZERO_CFG_SPECIAL = 3;
	public static int ZERO_CFG_UPFAIL = 4;
	public static int ZERO_CFG_ACCOUNTREPORT = 5;
	public static int ZERO_CFG_AUTOFAIL = 6;
	public static int ZERO_CFG_RELOCATION = 7;
	public static int ZERO_CFG_CANCEL = 8;
	public static int ZERO_CFG_AUTOING = 9;
	//是否零配置版本  1：是  0：否
	public static int IS_ZERO_VERSION = 1;
	public static int NOT_ZERO_VERSION = 0;
	//零配置处理类型 1：软件升级  2：串号配置 3：串号输入 4:重启
	public static int ZERO_CFG_UPGRADE = 1;
	public static int ZERO_CFG_ACCOUNT = 2;
	public static int ZERO_CFG_ACC_IN = 3;
	public static int ZERO_CFG_ACC_REBOOT = 4;
	/**
	 * 零配置工单状态 0：失败1：成功2：待处理3：处理中4：撤销
	 * 2012-05-17 修改为0：失败1：成功2：待处理3：自动配置已下发 4：撤销 5:串号配置已下发 6：串号配置完成 
	 */
	public static int ZERO_CFG_SHEET_FAIL = 0;
	public static int ZERO_CFG_SHEET_SUCC = 1;
	public static int ZERO_CFG_SHEET_WILLDO = 2;
	public static int ZERO_CFG_SHEET_DOING = 3;
	public static int ZERO_CFG_SHEET_CANCEL = 4;
	public static int ZERO_CFG_SHEET_ACCT_DOING = 5;
	public static int ZERO_CFG_SHEET_ACCT_OK = 6;
	public static List<String> devTypeIds = new ArrayList<String>();
	//加载内存数据时每个线程处理的数据量
	public static long memDataInitOnceNum = 200000;
	public static int maxIdNum = 100;
	//BIND事件绑定流程是否匹配身份证号码   1：匹配  0：不匹配
	public static int isCheckCredNo = 0;
	//是否进行资源回收  1：是  0：否，目前江苏在使用
	public static int isResouceReset = 0;
	
}