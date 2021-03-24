package com.linkage.itms.dao;

import PreProcess.PPManagerHelper;
import ResourceBind.BlManagerHelper;
import SoftUp.SoftUpManagerHelper;
import StbCm.CMManagerHelper;
import SuperGather.SuperGatherManagerHelper;
import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.system.utils.database.Cursor;
import com.linkage.system.utils.database.DataSetBean;
import org.omg.CORBA.ORB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Jason(3412)
 * @date 2009-12-16
 */
public class InitDAO {

	private static Logger logger = LoggerFactory.getLogger(InitDAO.class);
	
	
	/**
	 * init corba SG
	 * 
	 * @return
	 */
	public static boolean initSuperGather() {
		logger.debug("initSuperGather()");

		PrepareSQL psql = new PrepareSQL();
		psql.append("select ior from tab_ior where object_name = '");
		psql.append(Global.getPrefixName(Global.SYSTEM_NAME,Global.GW_TYPE_ITMS));
		psql.append(Global.SYSTEM_SUPER_GATHER);
		psql.append("'");
	
		String ior = StringUtil.getStringValue(DBOperation.getRecord(psql.getSQL()), "ior");
		
		if (StringUtil.IsEmpty(ior, true)) {
			logger.error("SuperGather ior is null");

			return false;
		}

		String[] args = null;
		ORB orb = ORB.init(args, null);
		org.omg.CORBA.Object objRef = null;
		try {
			objRef = orb.string_to_object(ior);
			Global.G_SuperGatherManager = SuperGatherManagerHelper.narrow(objRef);
		} catch (RuntimeException e) {
			logger.error("SuperGather RuntimeException:{}", e.getMessage());

			return false;
		}

		return true;

	}
//	/**
//	 * init corba BusinessLogic
//	 * 
//	 * @return
//	 */
//	public static boolean initResourceBind() {
//		logger.debug("initResourceBind()");
//
//		PrepareSQL psql = new PrepareSQL();
//		psql.append("select ior from tab_ior where 1=1 and object_name = '");
//		psql.append(Global.getPrefixName(Global.SYSTEM_NAME));
//		psql.append(Global.SYSTEM_BUSINESS_LOGIC);
//		psql.append("'");
//		
//		String ior = StringUtil.getStringValue(DBOperation.getRecord(psql.getSQL()), "ior");
//		
//		if (StringUtil.IsEmpty(ior, true)) {
//			logger.error("BusinessLogic ior is null");
//
//			return false;
//		}
//
//		String[] args = null;
//		ORB orb = ORB.init(args, null);
//		org.omg.CORBA.Object objRef = null;
//		try {
//			objRef = orb.string_to_object(ior);
//			Global.G_BlManager = BlManagerHelper.narrow(objRef);
//		} catch (RuntimeException e) {
//			logger.error("BusinessLogic RuntimeException:{}", e.getMessage());
//			return false;
//		}
//
//		return true;
//	}
	/**
	 * init corba BusinessLogic
	 * 
	 * @return
	 */
	public static boolean initResourceBind(String gw_type) {
		logger.debug("initResourceBind()");

		PrepareSQL psql = new PrepareSQL();
		psql.append("select ior from tab_ior where 1=1 and object_name = '");
		psql.append(Global.getPrefixName(Global.SYSTEM_NAME,gw_type));
		psql.append(Global.SYSTEM_BUSINESS_LOGIC);
		psql.append("'");
		
		String ior = StringUtil.getStringValue(DBOperation.getRecord(psql.getSQL()), "ior");
		
		if (StringUtil.IsEmpty(ior, true)) {
			logger.error("BusinessLogic ior is null");

			return false;
		}

		String[] args = null;
		ORB orb = ORB.init(args, null);
		org.omg.CORBA.Object objRef = null;
		try {
			objRef = orb.string_to_object(ior);
			
			if(gw_type == Global.GW_TYPE_ITMS)
			{
				Global.G_BlManager_ITMS = BlManagerHelper.narrow(objRef);
			}
			else if(gw_type == Global.GW_TYPE_BBMS)
			{
				Global.G_BlManager_BBMS = BlManagerHelper.narrow(objRef);
			}
			else
			{
				Global.G_BlManager_STB = BlManagerHelper.narrow(objRef);
			}
		} catch (RuntimeException e) {
			logger.error("BusinessLogic RuntimeException:{}", e.getMessage());
			return false;
		}

		return true;
	}
	/**
	 * init corba PP
	 * 
	 * @return
	 */
	public static boolean initPreProcess() {
		logger.debug("initPreProcess()");

		String ior = StringUtil.getStringValue(getPreProcessIOR(),"ior");
		if (StringUtil.IsEmpty(ior, true)) {
			logger.error("PreProcess ior is null");

			return false;
		}

		ORB orb = ORB.init((String[]) null, null);
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
	
	/**
	 * init corba PP By Type
	 * @author banyr
	 * @date 2018-6-21
	 * @return
	 */
	public static boolean initPreProcess(String type) {
		String ior = StringUtil.getStringValue(getPreProcessIOR(type),"ior");
		if (StringUtil.IsEmpty(ior, true)) {
			logger.error("PreProcess ior is null");

			return false;
		}

		ORB orb = ORB.init((String[]) null, null);
		org.omg.CORBA.Object objRef = null;
		try {
			objRef = orb.string_to_object(ior);
			if(type == Global.GW_TYPE_ITMS)
			{
				Global.G_PPManager_ITMS = PPManagerHelper.narrow(objRef);
			}
			else if(type == Global.GW_TYPE_BBMS)
			{
				Global.G_PPManager_BBMS = PPManagerHelper.narrow(objRef);
			}
			else
			{
				Global.G_PPManager_STB = CMManagerHelper.narrow(objRef);
			}
		} catch (RuntimeException e) {
			logger.error("PreProcess RuntimeException:{}", e.getMessage());
			return false;
		}

		return true;
	}
	
	/**
	 * 查询获取预读接口的ior
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-22
	 * @return Map
	 */
	public static Map getPreProcessIOR() {
		
		logger.debug("getPreProcessIOR()");

		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select ior from tab_ior where 1=1 ");
		psql.append(" and object_name = '");
		psql.append(Global.getPrefixName(Global.SYSTEM_NAME,Global.GW_TYPE_ITMS));
		psql.append(Global.SYSTEM_PREPROCESS);
		psql.append("'");      
		psql.append(" and object_poa = '");
		psql.append(Global.getPrefixName(Global.SYSTEM_NAME,Global.GW_TYPE_ITMS));
		psql.append(Global.SYSTEM_PREPROCESS_POA);
		psql.append("'");

		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * 根据网关类型查询获取预读接口的ior
	 * 
	 * @param 
	 * @author banyr
	 * @date 2018-6-21
	 * @return Map
	 */
	public static Map getPreProcessIOR(String type) {
		
		logger.debug("getPreProcessIORByType()");

		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select ior from tab_ior where 1=1 ");
		psql.append(" and object_name = '");
		psql.append(Global.getPrefixName(Global.SYSTEM_NAME,type));
		psql.append(Global.SYSTEM_PREPROCESS);
		psql.append("'");      
		psql.append(" and object_poa = '");
		psql.append(Global.getPrefixName(Global.SYSTEM_NAME,type));
		psql.append(Global.SYSTEM_PREPROCESS_POA);
		psql.append("'");

		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * init corba SoftwareUpgrade
	 * 
	 * @return
	 */
	public static boolean initSofUp() {
		logger.debug("initSoftwareUpgrade()");

		PrepareSQL psql = new PrepareSQL();
		psql.append("select ior from tab_ior where 1=1 ");
		psql.append("   and object_name = '");
		psql.append(Global.getPrefixName(Global.SYSTEM_NAME));
		psql.append(Global.SYSTEM_SOFT_WARE_UPGRADE);
		psql.append("'");
		psql.append(" and object_poa = '");
		psql.append(Global.getPrefixName(Global.SYSTEM_NAME));
		psql.append(Global.SYSTEM_SOFT_WARE_UPGRADE_POA);
		psql.append("'");
		
		String ior = StringUtil.getStringValue(DBOperation.getRecord(psql.getSQL()), "ior");
		
		if (StringUtil.IsEmpty(ior, true)) {
			logger.error("SoftwareUpgrade ior is null");
			return false;
		}

		String[] args = null;
		ORB orb = ORB.init(args, null);
		org.omg.CORBA.Object objRef = null;
		try {
			objRef = orb.string_to_object(ior);
			Global.G_SUManager = SoftUpManagerHelper.narrow(objRef);
		} catch (RuntimeException e) {
			logger.error("SoftwareUpgrade RuntimeException:{}", e.getMessage());
			return false;
		}

		return true;
	}
	
	
	/**
	 * 获得设备错误信息
	 * 
	 * @return
	 */
	public static Cursor getFaultCode() {
		logger.debug("getFaultCode()");

		String sql = "select * from tab_cpe_faultcode";

		PrepareSQL psql = new PrepareSQL(sql);
		psql.getSQL();
		return DataSetBean.getCursor(sql);
	}
	/**
	 * 获得设备错误信息
	 * 
	 * @return 
	 */
	public static ArrayList<HashMap<String,String>> getFaultCodeForInit() {
		logger.debug("getFaultCode()");

		String sql = "select * from tab_cpe_faultcode";

		PrepareSQL psql = new PrepareSQL(sql);
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
}
