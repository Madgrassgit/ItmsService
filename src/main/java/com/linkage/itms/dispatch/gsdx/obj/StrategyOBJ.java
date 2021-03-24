/**
 * LINKAGE TECHNOLOGY (NANJING) CO.,LTD.<BR>
 * Copyright 2007-2010. All right reserved.
 */
package com.linkage.itms.dispatch.gsdx.obj;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DbUtils;
import com.linkage.itms.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;


/**
 * object:strategy.
 * 
 * @author Alex.Yan (yanhj@lianchuang.com)
 * @version 2.0, Jun 18, 2009
 * @see
 * @since 1.0
 */
public class StrategyOBJ {

	/** log */
	private static final Logger logger  = LoggerFactory.getLogger(StrategyOBJ.class);

	/** id */
	private long id = 0;

	/** status */
	private int status = 0;

	/** result_id */
	private int resultId = 0;

	/** result_desc */
	private String resultDesc = null;

	/** acc_oid */
	private long accOid = 0;

	/** time */
	private long time = 0;

	/** start_time */
	private long startTime = 0;

	/** end_time */
	private long endTime = 0;

	/** type */
	private int type = 0;

	/** device_id */
	private String deviceId = null;

	/** time */
	private String oui = null;

	/** device_serialnumber */
	private String sn = null;

	/** time */
	private String username = null;

	/** sheet_id */
	private String sheetId = null;

	/** sheet_para */
	private String sheetPara = null;

	/** service_id */
	private int serviceId = 0;
	
	private int subServiceId = 0;

	/** task_id */
	private String taskId = null;

	/** order_id */
	private int orderId = 0;

	/** exec_count */
	private int execCount = 0;

	/** redo */
	private int redo = 0;
	
	private int sheetType = 1;
	
	private int tempId = 1;
	
	private int isLastOne = 1;
	
	private int priority = 2;
	
	private String lineId = null;
	
	private String clientId = "2";
	
	private String idsTaskId = "-1";
	/**
	 * get device_id
	 * 
	 * @param count
	 * @return
	 */
	synchronized public static long createId(int count) {
		logger.debug("createId({})", count);

		long serial = -1;
		
		if (count <= 0) {
			serial = -2;

			return serial;
		}

		if( Global.MIN_UNUSED_STRATEGYID < 0 ){
						
			Global.MIN_UNUSED_STRATEGYID = getMaxId4Oracle(Global.SUM_UNUSED_STRATEGYID) - 1;
			Global.MAX_UNUSED_STRATEGYID = Global.MIN_UNUSED_STRATEGYID + Global.SUM_UNUSED_STRATEGYID;
		}
		
		if( Global.MAX_UNUSED_STRATEGYID < (Global.MIN_UNUSED_STRATEGYID + count)){
			
			if(Global.SUM_UNUSED_STRATEGYID < count){

					Global.MIN_UNUSED_STRATEGYID = getMaxId4Oracle(count) - 1;
				Global.MAX_UNUSED_STRATEGYID = Global.MIN_UNUSED_STRATEGYID + count;
			} else {
				
					Global.MIN_UNUSED_STRATEGYID = getMaxId4Oracle(Global.SUM_UNUSED_STRATEGYID) - 1;
				Global.MAX_UNUSED_STRATEGYID = Global.MIN_UNUSED_STRATEGYID + Global.SUM_UNUSED_STRATEGYID;
			}

		}
		
		serial = Global.MIN_UNUSED_STRATEGYID + 1;
		Global.MIN_UNUSED_STRATEGYID = Global.MIN_UNUSED_STRATEGYID + count;

		logger.debug("ID={}", serial);
		
		return serial;
	}
	
	
	/**
	 * 生成新的策略ID
	 * 
	 */
	public void createId(){
		if(DBUtil.GetDB() == 1 || DBUtil.GetDB() == 2) {
			id = StrategyOBJ.createId(1)+10000000000l;
		}else {
			id = DbUtils.getUnusedID("sql_gw_serv_strategy", 1);
		}
	}
	
	/**
	 * get device_id
	 * 
	 * @param count
	 * @return
	 */
	public static long getMaxId4Sybase(int count) {
		logger.debug("getMaxId4Sybase({})", count);

		long serial = -1;

		if (count <= 0) {
			serial = -2;

			return serial;
		}

		String sql = "maxStrategyIdProc ?";
		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setInt(1, count);

		return DBOperation.executeProcSelect(pSQL.getSQL());
	}
	/**
	 * 获取连接
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2008-10-24
	 * @return Connection
	 */
	public static Connection getConnect() {

		Connection conn = null;
		try {
			conn = DriverManager.getConnection("proxool.preprocess_pool");
			//logger.debug("get the connect");
		} catch (SQLException e) {
			logger.error("connect is not available", e);
		}
		return conn;
	}
	/**
	 * get device_id
	 * 
	 * @param count
	 * @return
	 */
	public static long getMaxId4Oracle(int count) {
		logger.debug("getMaxId4Oracle({})", count);

		long serial = -1;

		if (count <= 0) {
			serial = -2;

			return serial;
		}

		CallableStatement cstmt = null;
		Connection conn = null;
		String sql = "{call maxStrategyIdProc(?,?)}";

		try {
			//conn = DBOperation.getConnect();
			
			conn = getConnect();
			if(conn != null){
				cstmt = conn.prepareCall(sql);
				cstmt.setInt(1, count);
				cstmt.registerOutParameter(2, Types.INTEGER);
				cstmt.execute();
				serial = cstmt.getLong(2);
			}else {
				logger.error("getMaxId4Oracle with get connection null");
			}

		} catch (Exception e) {
			logger.error("getMaxId4Oracle Exception:{}", e.getMessage());
		} finally {
			sql = null;

			if (cstmt != null) {
				try {
					cstmt.close();
				} catch (SQLException e) {
					logger.error("cstmt.close SQLException:{}", e.getMessage());
				}
				cstmt = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					logger.error("conn.close error:{}", e.getMessage());
				}

				conn = null;
			}
		}

		return serial;
	}
	
	/**
	 * get:
	 * @return the id
	 */
	public long getId() {

		return id;
	}

	/**
	 * get:
	 * @return the status
	 */
	public int getStatus() {

		return status;
	}

	/**
	 * get:
	 * @return the resultId
	 */
	public int getResultId() {

		return resultId;
	}

	/**
	 * get:
	 * @return the resultDesc
	 */
	public String getResultDesc() {

		return resultDesc;
	}

	/**
	 * get:
	 * @return the accOid
	 */
	public long getAccOid() {

		return accOid;
	}

	/**
	 * get:
	 * @return the time
	 */
	public long getTime() {

		return time;
	}

	/**
	 * get:
	 * @return the startTime
	 */
	public long getStartTime() {

		return startTime;
	}

	/**
	 * get:
	 * @return the endTime
	 */
	public long getEndTime() {

		return endTime;
	}

	/**
	 * get:
	 * @return the type
	 */
	public int getType() {

		return type;
	}

	/**
	 * get:
	 * @return the deviceId
	 */
	public String getDeviceId() {

		return deviceId;
	}

	/**
	 * get:
	 * @return the oui
	 */
	public String getOui() {

		return oui;
	}

	/**
	 * get:
	 * @return the sn
	 */
	public String getSn() {

		return sn;
	}

	/**
	 * get:
	 * @return the username
	 */
	public String getUsername() {

		return username;
	}

	/**
	 * get:
	 * @return the sheetId
	 */
	public String getSheetId() {

		return sheetId;
	}

	/**
	 * get:
	 * @return the sheetPara
	 */
	public String getSheetPara() {

		return sheetPara;
	}

	/**
	 * get:
	 * @return the service_id
	 */
	public int getServiceId() {

		return serviceId;
	}

	/**
	 * get:
	 * @return the taskId
	 */
	public String getTaskId() {

		return taskId;
	}

	/**
	 * get:
	 * @return the orderId
	 */
	public int getOrderId() {

		return orderId;
	}

	/**
	 * get:
	 * @return the execCount
	 */
	public int getExecCount() {

		return execCount;
	}

	/**
	 * get:
	 * @return the redo
	 */
	public int getRedo() {

		return redo;
	}

	/**
	 * set:
	 * @param id the id to set
	 */
	public void setId(long id) {

		this.id = id;
	}

	/**
	 * set:
	 * @param status the status to set
	 */
	public void setStatus(int status) {

		this.status = status;
	}

	/**
	 * set:
	 * @param resultId the resultId to set
	 */
	public void setResultId(int resultId) {

		this.resultId = resultId;
	}

	/**
	 * set:
	 * @param resultDesc the resultDesc to set
	 */
	public void setResultDesc(String resultDesc) {

		this.resultDesc = resultDesc;
	}

	/**
	 * set:
	 * @param accOid the accOid to set
	 */
	public void setAccOid(long accOid) {

		this.accOid = accOid;
	}

	/**
	 * set:
	 * @param time the time to set
	 */
	public void setTime(long time) {

		this.time = time;
	}

	/**
	 * set:
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {

		this.startTime = startTime;
	}

	/**
	 * set:
	 * @param endTime the endTime to set
	 */
	public void setEndTime(long endTime) {

		this.endTime = endTime;
	}

	/**
	 * set:
	 * @param type the type to set
	 */
	public void setType(int type) {

		this.type = type;
	}

	/**
	 * set:
	 * @param deviceId the deviceId to set
	 */
	public void setDeviceId(String deviceId) {

		this.deviceId = deviceId;
	}

	/**
	 * set:
	 * @param oui the oui to set
	 */
	public void setOui(String oui) {

		this.oui = oui;
	}

	/**
	 * set:
	 * @param sn the sn to set
	 */
	public void setSn(String sn) {

		this.sn = sn;
	}

	/**
	 * set:
	 * @param username the username to set
	 */
	public void setUsername(String username) {

		this.username = username;
	}

	/**
	 * set:
	 * @param sheetId the sheetId to set
	 */
	public void setSheetId(String sheetId) {

		this.sheetId = sheetId;
	}

	/**
	 * set:
	 * @param sheetPara the sheetPara to set
	 */
	public void setSheetPara(String sheetPara) {

		this.sheetPara = sheetPara;
	}

	/**
	 * set:
	 * @param service_id the service_id to set
	 */
	public void setServiceId(int serviceId) {

		this.serviceId = serviceId;
	}

	/**
	 * set:
	 * @param taskId the taskId to set
	 */
	public void setTaskId(String taskId) {

		this.taskId = taskId;
	}

	/**
	 * set:
	 * @param orderId the orderId to set
	 */
	public void setOrderId(int orderId) {

		this.orderId = orderId;
	}

	/**
	 * set:
	 * @param execCount the execCount to set
	 */
	public void setExecCount(int execCount) {

		this.execCount = execCount;
	}

	/**
	 * set:
	 * @param redo the redo to set
	 */
	public void setRedo(int redo) {

		this.redo = redo;
	}

	public int getSheetType() {
		return sheetType;
	}

	public void setSheetType(int sheetType) {
		
		
		this.sheetType = sheetType;
	}

	public int getTempId() {
		return tempId;
	}

	public void setTempId(int tempId) {
		this.tempId = tempId;
	}

	public int getIsLastOne() {
		return isLastOne;
	}

	public void setIsLastOne(int isLastOne) {
		this.isLastOne = isLastOne;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String toString() {
		logger.debug("toString()");

		return "[" + id + "] " + "device_id=" + this.deviceId;
	}

	
	public int getSubServiceId()
	{
		return subServiceId;
	}

	
	public void setSubServiceId(int subServiceId)
	{
		this.subServiceId = subServiceId;
	}

	
	public String getLineId()
	{
		return lineId;
	}

	
	public void setLineId(String lineId)
	{
		this.lineId = lineId;
	}

	
	public String getClientId()
	{
		return clientId;
	}

	
	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}


	public String getIdsTaskId() {
		return idsTaskId;
	}


	public void setIdsTaskId(String idsTaskId) {
		this.idsTaskId = idsTaskId;
	}
}
