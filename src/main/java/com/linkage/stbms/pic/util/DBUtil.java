package com.linkage.stbms.pic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.pic.Global;
import com.linkage.stbms.pic.db.Cursor;
import com.linkage.stbms.pic.db.DBOperation;
import com.linkage.stbms.pic.sheet.TableOBJ;

/**
 * DBUtil
 * @author gongsj
 * @date 2009-7-21
 */
public class DBUtil {

	final static Logger logger = LoggerFactory.getLogger(DBUtil.class);

	/**
	 * ����IOR
	 * @author gongsj
	 * @date 2009-9-11
	 * @param objectName
	 * @param poaName
	 * @param ior
	 * @return
	 */
	public static int saveIor(String objectName, String poaName, String ior) {
		StringBuilder sql = new StringBuilder();
		
		if (1 == deleteIor(objectName, poaName)) {
			sql.append("insert into tab_ior values ('").append(objectName).append("','").append(poaName).append("',0,'").append(ior).append("')");
			return DBOperation.executeUpdate(sql.toString());
		}
		return -1;
	}

	/**
	 * ��ȡ�豸��Ϣ
	 * 
	 * @param device_id
	 * @return
	 */
	public static Map<String, String> getDevInfo(String deviceId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select gather_id,oui,device_serialnumber,loopback_ip,cr_port,cr_path,acs_username,acs_passwd,devicetype_id from ").append(TableOBJ.DeviceTabName).append(" where device_id='").append(deviceId).append("'");
		
		return DBOperation.getRecord(sql.toString());
	}

	public static void deleteDeviceOtherStrategy(String deviceId) {
		StringBuilder sql = new StringBuilder();
		sql.append("delete from ").append(Global.TABLENAME).append(" where device_id='").append(deviceId).append("' and status=0");
		if(1 != DBOperation.executeUpdate(sql.toString())) {
			logger.warn("ɾ���豸{}������δ��ҵ�����ʧ��", deviceId);
		}
	}

	/**
	 * ���device_id��strategy_id�����²��Ա��״̬
	 * 
	 * @param strategy_id
	 * @param status
	 * @param result_id
	 * @param type
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static boolean updateStrategy(String strategy_id, String status, String result_id, String type, String startTime, String endTime, int execCount) {
		
		int result = 0;
		ArrayList<String> list = new ArrayList<String>();
		StringBuilder sql = new StringBuilder();
		StringBuilder sql1 = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		if(true==StringUtil.IsEmpty(strategy_id)){
			return false;
		}
		if (null == status && null == result_id && null == startTime && null == endTime) {
			return false;
		}
		sql1.append("update ").append(Global.TABLENAME).append(" set ");
		sql2.append("update ").append(Global.LOGTABLENAME).append(" set ");
		
		String set = "";
		if (status != null && !"".equals(status)) {
			if (!"".equals(set)) {
				sql.append(", ");
			}
			set="set";
			sql.append("status=").append(status);
		}
		
		if (result_id != null && !"".equals(result_id)) {
			if (!"".equals(set)) {
				sql.append(", ");
			}
			set="set";
			sql.append("result_id=").append(result_id);
		}
		
		if (type != null && !"".equals(type)) {
			if (!"".equals(set)) {
				sql.append(", ");
			}
			set="set";
			sql.append("type=").append(type);
		}
		
		if (startTime != null && !"".equals(startTime)) {
			if (!"".equals(set)) {
				sql.append(", ");
			}
			set="set";
			sql.append("start_time=").append(startTime);
		}
		
		if (endTime != null && !"".equals(endTime)) {
			if (!"".equals(set)) {
				sql.append(", ");
			}
			set="set";
			sql.append("end_time=").append(endTime);
		}
		
		if (execCount>0) {
			if (!"".equals(set)) {
				sql.append(", ");
			}
			set="set";
			sql.append("exec_count=").append(execCount);
		}
		
		sql.append(" where id=").append(strategy_id);

		sql1.append(sql);
		sql2.append(sql);
		
		list.add(sql1.toString());
		list.add(sql2.toString());
		
		result = DBOperation.executeBatch(list);
		
		sql = null;
		sql1 = null;
		sql2 = null;
		list = null;
		
		if (1 == result) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * ����ҵ�����ò��Ա����
	 * 
	 * @param device_id
	 * @param service_id
	 * @param sheetId
	 * @return
	 */
	public static boolean updateStrategy(String sheetId, String strategy_id) {
		if (null == sheetId) {
			return false;
		}
		int result = 0;
		ArrayList<String> list = new ArrayList<String>();
		StringBuilder sql1 = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		
		sql1.append("update ").append(Global.TABLENAME).append(" set sheet_id='").append(sheetId).append("' where id=").append(strategy_id);
		sql2.append("update ").append(Global.LOGTABLENAME).append(" set sheet_id='").append(sheetId).append("' where id=").append(strategy_id);
	
		list.add(sql1.toString());
		list.add(sql2.toString());
		
		result = DBOperation.executeBatch(list);
		
		sql1 = null;
		sql2 = null;
		list = null;
		
		if (1 == result) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * ȡ��IOR
	 * 
	 * @param gather_id
	 * @return ior
	 */
	public static String getIOR(String gatherId) {
		String ior = "";
		StringBuilder sql = new StringBuilder();
		sql.append("select ior from tab_ior where object_name='ACS_").append(gatherId)
		   .append("' and object_poa='ACS_Poa_").append(gatherId).append("'");
		
		// logger.warn("getIorSQL={}", getIorSQL);
		Map<String, String> map = DBOperation.getRecord(sql.toString());
		if (null != map) {
			ior = (String) map.get("ior");
		}
		if (null == ior) {
			return "";
		}
		return ior;
	}

	public static String getServTypeId(String serviceId) {
		String sql = "select serv_type_id from tab_service where service_id=" + serviceId;
		
		Map<String, String> map = DBOperation.getRecord(sql);
		if (map != null) {
			return map.get("serv_type_id") == null ? null : (String) map.get("serv_type_id");
		}
		return null;
	}

	public static int deleteIor(String objectName, String poaName) {
		StringBuilder sql = new StringBuilder();
		sql.append("delete from tab_ior where object_name='").append(objectName).append("' and object_poa='").append(poaName).append("'");
		return DBOperation.executeUpdate(sql.toString());
	}

	public static Map<String, String> getPolicy(String strategyId) {
		StringBuilder sql = new StringBuilder();
		
		// mysql db
		if (3 == com.linkage.commons.db.DBUtil.GetDB()) {
			sql.append("select device_id,service_id,task_id,sheet_type,exec_count,sheet_para from ").append(Global.TABLENAME).append(" where id=").append(strategyId).append(" and status=0");
		}
		else
		{
			sql.append("select * from ").append(Global.TABLENAME).append(" where id=").append(strategyId).append(" and status=0");
		}
		return DBOperation.getRecord(sql.toString());
	}

	public static void updatePolicy(String strategyId, String serviceId) {
		ArrayList<String> list = new ArrayList<String>();
		StringBuilder sql1 = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		
		sql1.append("update ").append(Global.TABLENAME).append(" set service_id=").append(serviceId).append(" where id=").append(strategyId);
		sql2.append("update ").append(Global.LOGTABLENAME).append(" set service_id=").append(serviceId).append(" where id=").append(strategyId);
	
		list.add(sql1.toString());
		list.add(sql2.toString());
		
		DBOperation.executeBatch(list);
		
		sql1 = null;
		sql2 = null;
		list = null;
	
	}
	
	/**
	 * 
	 * @param serv_type_id
	 * @return 
	 *         gw_serv_beforehand��ԭ����serv_type_id��Ϊservice_id(��ԣ��·�IPTVʱ������Ҫ�·��汾)
	 */
	public static List<String> getBeforeType(String serviceId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select b.before_type from gw_serv_beforehand b where b.service_id=").append(serviceId).append(" order by b.before_id");
		
		return DBOperation.getListResult(sql.toString());
	}

	/**
	 * ��ȡĬ��ҵ������ò���ֵ
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-3-4
	 * @return void
	 */
	public static Map<String, String> getService(String deviceId, String defaultServId, String accessTypeId) {
		Map<String, String> resultMap = null;
		
		StringBuilder sql = new StringBuilder();
		sql.append("select serv_default_id, value from gw_serv_default_value where serv_default_id=").append(defaultServId).append(" and access_type_id=").append(accessTypeId);
		resultMap = DBOperation.getRecord(sql.toString());
		if (resultMap == null || resultMap.isEmpty()) {
			return null;
		}
		return resultMap;
	}

//	public static Map<String, String> getDefaultServ(String defaultServId) {// 没有地方调用
//		StringBuilder sql = new StringBuilder();
//		sql.append("select * from gw_serv_default where serv_default_id=").append(defaultServId);
//		// logger.warn(sql);
//		return DBOperation.getRecord(sql.toString());
//	}
	
	/**
	 * ��ȡservice_id
	 * 
	 * @param servTypeId
	 *            ��ҵ������ID operTypeId����������ID wanType����������
	 * @author Jason(3412)
	 * @date 2009-3-5
	 * @return Map<String,String>
	 */
	public static String getServiceId(String servTypeId, String operTypeId, String wanType) {
		StringBuilder sql = new StringBuilder();
		
		sql.append("select service_id from tab_service where serv_type_id=").append(servTypeId)
		   .append(" and oper_type_id=").append(operTypeId).append(" and wan_type=").append(wanType);
		// logger.warn(sql);
		return DBOperation.getRecord(sql.toString()).get("service_id");
	}
	
	
	/**
	 * ͨ��ģ��ID���ģ������
	 * @author gongsj
	 * @date 2009-7-21
	 * @param templateId
	 * @return
	 */
	public static Cursor getTemplateCmd(String templateId) {
		StringBuilder cmdSql = new StringBuilder();
		
		cmdSql.append("select tc_serial, rpc_id, rpc_order, is_save from tab_template_cmd where template_id=").append(templateId)
			  .append(" order by tc_serial");
		
		Cursor cursor = DBOperation.getCursor(cmdSql.toString());
		
		return cursor;
	}
	
	/**
	 * ͨ��tcSerial���tab_template_cmd_para
	 * @author gongsj
	 * @date 2009-7-21
	 * @param tcSerial
	 * @return
	 */
	public static Cursor getTemplateCmdPara(String tcSerial) {
		StringBuilder getTemplateCmdParaSql = new StringBuilder();
		
		getTemplateCmdParaSql.append("select tc_serial, para_serial, have_defvalue, def_value, para_type_id from tab_template_cmd_para where tc_serial=")
							 .append(tcSerial);
			
		logger.debug("getTemplateCmd��{}", getTemplateCmdParaSql.toString());
		
		Cursor cursor = DBOperation.getCursor(getTemplateCmdParaSql.toString());
		
		return cursor;
	}

//	public static Map<String, String[]> getConPara()// 没有地方调用
//	{
//		StringBuilder sql = new StringBuilder();
//		sql.append("select * from tab_batch_con_para");
//		Cursor cursor = DBOperation.getCursor(sql.toString());
//		if (null == cursor) {
//			return null;
//		}
//		Map<String,String[]> conParaMap = new HashMap<String,String[]>();
//		
//		Map<String,String> map = cursor.getNext();
//		
//		while (null != map) {
//			conParaMap.put(StringUtil.getStringValue(map.get("para_id")), new String[] {
//					StringUtil.getStringValue(map.get("para_path")),
//					StringUtil.getStringValue(map.get("para_type_id")) });
//			map = cursor.getNext();
//		}
//		
//		sql = null;
//		cursor = null;
//		map = null;
//		
//		return conParaMap;	}

//	public static Map<String, String> getSoftPath()// 没有地方调用
//	{
//		StringBuilder sql = new StringBuilder();
//		sql.append("select * from stb_gw_version_file_path");
//		Cursor cursor = DBOperation.getCursor(sql.toString());
//		if (null == cursor) {
//			return null;
//		}
//		Map<String,String> tmap = new HashMap<String,String>();
//		
//		Map<String,String> map = cursor.getNext();
//		
//		while (null != map) {
//			tmap.put(StrUtil.getStringValue(map.get("id")), StrUtil.getStringValue(map.get("version_path")));
//			map = cursor.getNext();
//		}
//		
//		sql = null;
//		cursor = null;
//		map = null;
//		
//		return tmap;
//	}
}









