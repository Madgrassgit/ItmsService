package com.linkage.stbms.pic.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.PrepareSQL;
import com.linkage.stbms.pic.Global;
import com.linkage.stbms.pic.db.DBOperation;
import com.linkage.stbms.pic.object.StrategyObj;
import com.linkage.stbms.pic.sheet.TableOBJ;

/**
 * @author Jason(3412)
 * @date 2009-3-5
 */
public class SqlUtil {

	static final Logger logger = LoggerFactory.getLogger(SqlUtil.class);
	/**
	 * 增加策略
	 * @author gongsj
	 * @date 2009-7-16
	 * @param obj
	 * @return Object
	 */
	public Boolean addStrategy(StrategyObj obj) {
		logger.debug("addStrategy({})", obj);
		StringBuilder sql = new StringBuilder();
		StringBuilder sql1 = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		
		if (obj == null) {
			logger.debug("obj == null");
			return false;
		}

		int result = 0;
		ArrayList<String> list = new ArrayList<String>();
		
		sql.append("delete from ").append(Global.TABLENAME).append(" where device_id='").append(obj.getDeviceId()).append("' and temp_id=").append(obj.getTempId());
		
		if(1 != DBOperation.executeUpdate(sql.toString())) {
			logger.warn("删除设备{}的iTV业务策略失败", obj.getDeviceId());
		}
		
		sql1.append("insert into ").append(Global.TABLENAME).append(" (");
		sql1.append("redo,id,acc_oid,time,type,device_id,oui,device_serialnumber,username,sheet_para,service_id,task_id,order_id,sheet_type, temp_id, is_last_one,priority");
		sql1.append(") values (");
		sql1.append(obj.getRedo());
		sql1.append(",");
		sql1.append(obj.getId());
		sql1.append("," + obj.getAccOid());
		sql1.append("," + obj.getTime());
		sql1.append("," + obj.getType());
		sql1.append(",'" + obj.getDeviceId());
		sql1.append("','" + obj.getOui());
		sql1.append("','" + obj.getSn());
		sql1.append("','" + obj.getUsername());
		sql1.append("','" + obj.getSheetPara());
		sql1.append("'," + obj.getServiceId());
		sql1.append(",'" + obj.getTaskId());
		sql1.append("'," + obj.getOrderId());
		sql1.append("," + obj.getSheetType());
		sql1.append("," + obj.getTempId());
		sql1.append("," + obj.getIsLastOne());
		sql1.append("," + obj.getPriority());
		sql1.append(")");

		sql2.append("insert into ").append(Global.LOGTABLENAME).append(" (");
		sql2.append("redo,id,acc_oid,time,type,device_id,oui,device_serialnumber,username,sheet_para,service_id,task_id,order_id,sheet_type, temp_id, is_last_one,priority");
		sql2.append(") values (");
		sql2.append(obj.getRedo());
		sql2.append(",");
		sql2.append(obj.getId());
		sql2.append("," + obj.getAccOid());
		sql2.append("," + obj.getTime());
		sql2.append("," + obj.getType());
		sql2.append(",'" + obj.getDeviceId());
		sql2.append("','" + obj.getOui());
		sql2.append("','" + obj.getSn());
		sql2.append("','" + obj.getUsername());
		sql2.append("','" + obj.getSheetPara());
		sql2.append("'," + obj.getServiceId());
		sql2.append(",'" + obj.getTaskId());
		sql2.append("'," + obj.getOrderId());
		sql2.append("," + obj.getSheetType());
		sql2.append("," + obj.getTempId());
		sql2.append("," + obj.getIsLastOne());
		sql2.append("," + obj.getPriority());
		sql2.append(")");
	
		list.add(sql1.toString().replaceAll("'null'", "null"));
		list.add(sql2.toString().replaceAll("'null'", "null"));
		
		logger.debug("入策略表:{}", list);
		
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
	
//	/**
//	 * 增加策略
//	 * @author gongsj
//	 * @date 2009-7-16
//	 * @param obj
//	 * @return Object
//	 */
//	public static ArrayList<String> addStrategySql(StrategyObj obj) {
//		logger.debug("addStrategySql({})", obj);
//		ArrayList<String> list = new ArrayList<String>();
//		StringBuilder sql = new StringBuilder();
//		StringBuilder sql1 = new StringBuilder();
//		StringBuilder sql2 = new StringBuilder();
//		
//		if (obj == null) {
//			logger.debug("obj == null");
//			return list;
//		}
//
//		sql.append("delete from ").append(Global.TABLENAME).append(" where device_id='").append(obj.getDeviceId()).append("' and service_id=").append(obj.getServiceId());
//		
//		
//		list.add(sql.toString());
//		
//		sql1.append("insert into ").append(Global.TABLENAME).append(" (");
//		sql1.append("redo,id,acc_oid,time,type,device_id,oui,device_serialnumber,username,sheet_para,service_id,task_id,order_id,sheet_type, temp_id, is_last_one,priority");
//		sql1.append(") values (");
//		sql1.append(obj.getRedo());
//		sql1.append(",");
//		sql1.append(obj.getId());
//		sql1.append("," + obj.getAccOid());
//		sql1.append("," + obj.getTime());
//		sql1.append("," + obj.getType());
//		sql1.append(",'" + obj.getDeviceId());
//		sql1.append("','" + obj.getOui());
//		sql1.append("','" + obj.getSn());
//		sql1.append("','" + obj.getUsername());
//		sql1.append("','" + obj.getSheetPara());
//		sql1.append("'," + obj.getServiceId());
//		sql1.append(",'" + obj.getTaskId());
//		sql1.append("'," + obj.getOrderId());
//		sql1.append("," + obj.getSheetType());
//		sql1.append("," + obj.getTempId());
//		sql1.append("," + obj.getIsLastOne());
//		sql1.append("," + obj.getPriority());
//		sql1.append(")");
//
//		sql2.append("insert into ").append(Global.LOGTABLENAME).append(" (");
//		sql2.append("redo,id,acc_oid,time,type,device_id,oui,device_serialnumber,username,sheet_para,service_id,task_id,order_id,sheet_type, temp_id, is_last_one,priority");
//		sql2.append(") values (");
//		sql2.append(obj.getRedo());
//		sql2.append(",");
//		sql2.append(obj.getId());
//		sql2.append("," + obj.getAccOid());
//		sql2.append("," + obj.getTime());
//		sql2.append("," + obj.getType());
//		sql2.append(",'" + obj.getDeviceId());
//		sql2.append("','" + obj.getOui());
//		sql2.append("','" + obj.getSn());
//		sql2.append("','" + obj.getUsername());
//		sql2.append("','" + obj.getSheetPara());
//		sql2.append("'," + obj.getServiceId());
//		sql2.append(",'" + obj.getTaskId());
//		sql2.append("'," + obj.getOrderId());
//		sql2.append("," + obj.getSheetType());
//		sql2.append("," + obj.getTempId());
//		sql2.append("," + obj.getIsLastOne());
//		sql2.append("," + obj.getPriority());
//		sql2.append(")");
//	
//		list.add(sql1.toString().replaceAll("'null'", "null"));
//		list.add(sql2.toString().replaceAll("'null'", "null"));
//		
//		logger.debug("入策略表:{}", list);
//		
//		sql = null;
//		sql1 = null;
//		sql2 = null;
//		
//		return list;
//	}
	
	/**
	 * tab_para_type.
	 * 
	 * @return
	 */
	public static List getService() {
		logger.debug("getService()");

		String sql = "select * from tab_service";
		PrepareSQL pSQL = new PrepareSQL(sql);

		return com.linkage.commons.db.DBOperation.getRecords(pSQL.getSQL());
	}
	
	
	/**
	 * get strategy
	 * 
	 * @param deviceId
	 * @return
	 */
	public static StrategyObj getOtherStrategy(String deviceId) {
		logger.debug("getOtherStrategy({})", new Object[] { deviceId });

		StrategyObj obj = null;

		if (null == deviceId) {
			logger.debug("para is null");

			return obj;
		}
		String sql = "select * from "+Global.TABLENAME+" where " + " device_id=?"
				+ " and status=0 and type in (0,4) " + " order by time,order_id";
		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setString(1, deviceId);

		obj = new StrategyObj(DBOperation.getRecord(pSQL.getSQL()));

		sql = null;

		return obj;
	}

	/**
	 * 获得组装策略表需要的用户等信息
	 * @author gongsj
	 * @date 2009-9-1
	 * @param userId
	 * @return
	 */
	public Map<String, String> getUserInfo(String userId) {
		StringBuilder sql = new StringBuilder();
		
		sql.append("select * from ").append(TableOBJ.UserTabName).append(" where cust_stat in ('1','2') and customer_id=").append(userId);
		
		Map<String, String> infoMap = DBOperation.getRecord(sql.toString());
		
		return infoMap;
		
	}
	
}
