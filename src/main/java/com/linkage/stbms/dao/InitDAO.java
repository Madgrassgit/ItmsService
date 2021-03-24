package com.linkage.stbms.dao;

import java.util.Map;

import org.omg.CORBA.ORB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.RPCManagerHelper;
import PreProcess.PPManagerHelper;
import ResourceBind.BlManagerHelper;
import StbCm.CMManagerHelper;
import SuperGather.SuperGatherManagerHelper;

import com.ailk.tr069.acsalive.thread.AcsAliveMessageDealThread;
import com.ailk.tr069.devrpc.thread.AcsDevRpcThread;
import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.ids.util.CommonUtil;
import com.linkage.stbms.itv.main.Global;
import com.linkage.system.utils.database.Cursor;
import com.linkage.system.utils.database.DataSetBean;

/**
 * @author Jason(3412)
 * @date 2009-12-16
 */
public class InitDAO {

	private static Logger logger = LoggerFactory.getLogger(InitDAO.class);
	private static ORB orb = initORB();
	
	private static ORB initORB(){
		String[] args = null;
		return ORB.init(args, null);
	}
	
	/**
	 * init corba SG
	 * 
	 * @return
	 */
	public static boolean initSuperGather() {
		logger.debug("initSuperGather()");

		String sql = "select ior from tab_ior where"
			+ " object_name='"+CommonUtil.getPrefix4IOR()+"SuperGather'";
		logger.info(sql);
		String ior = StringUtil.getStringValue(DBOperation.getRecord(sql),
				"ior");
		
		if (StringUtil.IsEmpty(ior, true)) {
			logger.error("SuperGather ior is null");

			return false;
		}

		String[] args = null;
		ORB orb = ORB.init(args, null);
		org.omg.CORBA.Object objRef = null;
		try {
			objRef = orb.string_to_object(ior);
			Global.G_SuperGatherManager = SuperGatherManagerHelper
					.narrow(objRef);
		} catch (RuntimeException e) {
			logger.error("SuperGather RuntimeException:{}", e.getMessage());

			return false;
		}

		return true;
	}
	public static void initMQ(){
		logger.warn("|||||主程序中开始启动“接受ACS存活消息”服务|||||");
		//logger.warn("----------" + StbServGlobals.getLipossProperty("mq.stb.mqACSAlive.url"));
		logger.warn("Global.ACS_OBJECT_NAME=" + Global.ACS_OBJECT_NAME);
//		AcsAliveMessageDealThread aamdt = new AcsAliveMessageDealThread(
//				Global.ACS_OBJECT_NAME, StbServGlobals.getLipossProperty("mq.stb.mqACSAlive.url"),
//				StbServGlobals.getLipossProperty("mq.stb.mqACSAlive.topic"));
		AcsAliveMessageDealThread aamdt = AcsAliveMessageDealThread.getInstance(Global.ACS_OBJECT_NAME, Global.MQ_POOl_MAP);
		if(null != aamdt ){
			aamdt.start();
		}
		logger.warn("|||||主程序中开始启动“接受ACS的RPC消息”服务|||||");
		logger.warn("AcsDevRpcThread启动");
//		AcsDevRpcThread adrt = new AcsDevRpcThread(
//				Global.ACS_OBJECT_NAME, StbServGlobals.getLipossProperty("mq.stb.mqDevRPC.url"),
//				StbServGlobals.getLipossProperty("mq.stb.mqDevRPC.topic"),StbServGlobals.getLipossProperty("mq.clientId"));
		AcsDevRpcThread adrt = AcsDevRpcThread.getInstance(Global.ACS_OBJECT_NAME, Global.MQ_POOl_MAP);
		if(null != adrt){
			adrt.start();
		}	
	}
	/**
	 * Corba ior.
	 * 
	 * @return
	 */
	public static String getCorbaIor(String objectName) {
		logger.debug("getCorbaIor({})",objectName);

		PrepareSQL pSQL = new PrepareSQL("select ior from tab_ior where object_name=?");
		pSQL.setString(1, objectName);

		String ior = StringUtil.getStringValue(DBOperation.getRecord(pSQL.getSQL()),"ior");

		if (null == ior) {
			logger.error("get Corba ior error");
			return ior;
		} else {
			logger.debug("Corba ior\n{}", ior);
			return ior.trim();
		}
	}
	/**
	 * init corba PP
	 * 
	 * @return
	 */
	public static boolean initPreProcess() {
		logger.debug("initPreProcess()");
		String sql = "select ior from tab_ior where"
			+ " object_name='PreProcess' and object_poa='PreProcess_Poa'";
		logger.info(sql);
		String ior = StringUtil.getStringValue(DBOperation.getRecord(sql),
				"ior");
		if (StringUtil.IsEmpty(ior, true)) {
			logger.error("PreProcess ior is null");

			return false;
		}

		if(null == orb){
			orb = initORB();
		}
		org.omg.CORBA.Object objRef = null;
		try {
			objRef = orb.string_to_object(ior);
			Global.G_PPManager = PPManagerHelper.narrow(objRef);
		} catch (RuntimeException e) {
			logger.error("PreProcess RuntimeException:{}", e.getMessage());

			return false;
		}

		return true;
	}
	
	public static boolean initPreProcessStb() {
		logger.debug("initPreProcess()");
		String sql = "select ior from tab_ior where"
			+ " object_name='STB_PreProcess' and object_poa='STB_PreProcess_Poa'";
		logger.info(sql);
		String ior = StringUtil.getStringValue(DBOperation.getRecord(sql),
				"ior");
		if (StringUtil.IsEmpty(ior, true)) {
			logger.error("PreProcess ior is null");

			return false;
		}

		if(null == orb){
			orb = initORB();
		}
		org.omg.CORBA.Object objRef = null;
		try {
			objRef = orb.string_to_object(ior);
			Global.G_PPManager_STB = CMManagerHelper.narrow(objRef);
		} catch (RuntimeException e) {
			logger.error("PreProcess RuntimeException:{}", e.getMessage());

			return false;
		}

		return true;
	}

	/**
	 * init corba RB
	 * 
	 * @return
	 */
	public static boolean initResourceBind() {
		logger.debug("initResourceBind()");
		String sql = "select ior from tab_ior where"
			+ " object_name='"+CommonUtil.getPrefix4IOR()+"BusinessLogic' and object_poa='"+CommonUtil.getPrefix4IOR()+"BusinessLogic_Poa'";
		logger.info(sql);
		String ior = StringUtil.getStringValue(DBOperation.getRecord(sql),
				"ior");
		if (StringUtil.IsEmpty(ior, true)) {
			logger.error("ResourceBind ior is null");

			return false;
		}

		if(null == orb){
			orb = initORB();
		}
		org.omg.CORBA.Object objRef = null;
		try {
			objRef = orb.string_to_object(ior);
			Global.G_BlManager_STB = BlManagerHelper.narrow(objRef);
		} catch (RuntimeException e) {
			logger.error("ResourceBind RuntimeException:{}", e.getMessage());

			return false;
		}

		return true;
	}
	/**
	 * init corba ACS
	 * 
	 * @return
	 */
	public static boolean initACS() {
		logger.debug("initACS()");

		String sql = "select ior from tab_ior where"
			+ " object_name like '"+CommonUtil.getPrefix4IOR()+"ACS_%'";

		String ior = StringUtil.getStringValue(DBOperation.getRecord(sql), "ior");
		if (StringUtil.IsEmpty(ior, true)) {
			logger.error("ACS ior is null");

			return false;
		}

		String[] args = null;
		ORB orb = ORB.init(args, null);
		org.omg.CORBA.Object objRef = null;
		try {
			objRef = orb.string_to_object(ior);
			Global.G_ACSManager = RPCManagerHelper.narrow(objRef);
		} catch (RuntimeException e) {
			logger.error("ACS RuntimeException:{}", e.getMessage());

			return false;
		}

		return true;
	}
	/**
	 * init fault code.
	 * 
	 * @return
	 */
	public static boolean initFaultCode() {
		logger.debug("initFaultCode()");

		boolean flag = true;

		Cursor cursor = getFaultCode();
		Map map = cursor.getNext();

		while (map != null) {
			Global.G_Fault_Map.put(StringUtil.getIntValue(map, "fault_code"),
					StringUtil.getStringValue(map, "fault_desc"));

			map = cursor.getNext();
		}
		
		Global.G_Fault_Map.put(2, "设备连接失败");
		Global.G_Fault_Map.put(3, "设备正被操作");
		Global.G_Fault_Map.put(4, "设备返回9000后错误");
		Global.G_Fault_Map.put(5, "采集失败");

		return flag;
	}
	/**
	 * 获得错误码
	 * 
	 * @return
	 */
	public static Cursor getFaultCode() {
		logger.debug("getFaultCode()");

		String sql = "select * from tab_cpe_faultcode";
		
		// mysql db
		if (3 == DBUtil.GetDB()) {
			sql = "select FAULT_CODE,FAULT_TYPE,FAULT_NAME,FAULT_DESC,FAULT_REASON,SOLUTIONS from tab_cpe_faultcode";
		}

		PrepareSQL psql = new PrepareSQL(sql);
		psql.getSQL();
		return DataSetBean.getCursor(sql);
	}
}
