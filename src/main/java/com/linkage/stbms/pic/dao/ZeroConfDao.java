
package com.linkage.stbms.pic.dao;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBAdapter;
import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DbUtils;
import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.pic.Global;
import com.linkage.stbms.pic.object.StbDeviceOBJ;

/**
 * 开机画面运营
 * 
 * @author 王森博
 */
@SuppressWarnings("unchecked")
public class ZeroConfDao
{

	final static Logger logger = LoggerFactory.getLogger(StrategyDao.class);
	
	/**
	 * 根据宽带账号查询用户.
	 * 
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getUserBynetName(String custName)
	{
		logger.debug("getUserByCustName({})", custName);
		PrepareSQL pSQL = new PrepareSQL(
				"select a.customer_id,a.serv_account,a.city_id,b.device_id,a.is_prepay from ? a left join ? b on a.customer_id = b.customer_id "
						+ "where a.cust_stat in ('1','2') and a.cust_account=?  order by a.openUserdate desc ");
		int index = 0;
		pSQL.setStringExt(++index, "stb_tab_customer", false);
		pSQL.setStringExt(++index, "stb_tab_gw_device", false);
		pSQL.setString(++index, custName);
		return DBOperation.getRecords(pSQL.getSQL());
	}
	
	/**
	 * 根据业务账号查询用户.
	 * 
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getUserByservAccount(String servAccount)
	{
		logger.debug("getUserByservAccount({})", servAccount);
		PrepareSQL pSQL = new PrepareSQL(
				"select a.customer_id,a.serv_account,a.city_id,b.device_id,a.is_prepay from ? a left join ? b on a.customer_id = b.customer_id "
						+ "where a.cust_stat in ('1','2') and a.serv_account=?  order by a.openUserdate desc ");
		int index = 0;
		pSQL.setStringExt(++index, "stb_tab_customer", false);
		pSQL.setStringExt(++index, "stb_tab_gw_device", false);
		pSQL.setString(++index, servAccount);
		return DBOperation.getRecords(pSQL.getSQL());
	}

	
	/**
	 * 绑定更新设备表.
	 * 
	 * @param obj
	 * @return
	 */
	public String updateDeviceByBind(String customerId, String cityId, String userName,
			String deviceId, int status)
	{
		logger.warn(
				"updateDeviceByBind(customerId:{},cityId:{},userName:{},deviceId:{})",
				new Object[] { customerId, cityId, userName, deviceId });
		PrepareSQL ppSQL = new PrepareSQL(
				"update ? set customer_id=?,city_id=?, serv_account=?,cpe_currentupdatetime=? "
						+ ",cpe_allocatedstatus=1,bind_time=?,zero_account=?,status=? where device_id=?");
		int index = 0;
		ppSQL.setStringExt(++index, "stb_tab_gw_device", false);
		ppSQL.setLong(++index, StringUtil.getLongValue(customerId));
		ppSQL.setString(++index, cityId);
		ppSQL.setString(++index, userName);
		ppSQL.setLong(++index, System.currentTimeMillis() / 1000);
		ppSQL.setLong(++index, System.currentTimeMillis() / 1000);
		ppSQL.setString(++index, userName);
		ppSQL.setInt(++index, status);
		ppSQL.setString(++index, deviceId);
		return ppSQL.getSQL();
	}
	
	/**
	 * 绑定更新设备表.jx_dx
	 * 
	 * @param obj
	 * @return
	 */
	public String updateDeviceByBindSucess(String customerId, String cityId, String userName,
			int status, StbDeviceOBJ stbDeviceOBJ)
	{
		logger.warn(
				"updateDeviceByBind(customerId:{},cityId:{},userName:{},deviceId:{})",
				new Object[] { customerId, cityId, userName, stbDeviceOBJ.getDeviceId() });
		PrepareSQL ppSQL = new PrepareSQL(
				"update ? set customer_id=?,city_id=?, serv_account=?,cpe_currentupdatetime=? "
						+ ",cpe_allocatedstatus=1,bind_time=?,zero_account=?,status=?,bind_state=?,bind_way=? where device_id=?");
		int index = 0;
		ppSQL.setStringExt(++index, "stb_tab_gw_device", false);
		ppSQL.setLong(++index, StringUtil.getLongValue(customerId));
		ppSQL.setString(++index, cityId);
		ppSQL.setString(++index, userName);
		ppSQL.setLong(++index, stbDeviceOBJ.getCurrTime());
		ppSQL.setLong(++index, stbDeviceOBJ.getCurrTime());
		ppSQL.setString(++index, userName);
		ppSQL.setInt(++index, status);
		ppSQL.setInt(++index, stbDeviceOBJ.getBindState());
		ppSQL.setInt(++index, stbDeviceOBJ.getBindWay());
		ppSQL.setString(++index, stbDeviceOBJ.getDeviceId());
		return ppSQL.getSQL();
	}
	
	/**
	 * 绑定更新零配置工单表
	 * 
	 * @param oui
	 * @param devSn
	 * @param cityId
	 * @param userName
	 * @param operType
	 *            操作类型0：开户1：销户2：移机3：修正
	 * @return
	 */
	public String updateZeroCfgSheetByBind(String oui, String devSn, String cityId,
			String userName, int status, int operType)
	{
		logger.warn("updateZeroCfgSheetByBind(oui:{},devSn:{},cityId:{},userName:{})",
				new Object[] { oui, devSn, cityId, userName });
		PrepareSQL ppSQL = new PrepareSQL(
				"update ? set sn=?,oui=?,city_id=?,conf_down_time=? "
						+ ",work_status=? where serv_account_new=? and oper_type=? and work_status in (2,3,5)");
		int index = 0;
		ppSQL.setStringExt(++index, "stb_tab_zero_conf_report", false);
		ppSQL.setString(++index, devSn);
		ppSQL.setString(++index, oui);
		ppSQL.setString(++index, cityId);
		ppSQL.setLong(++index, System.currentTimeMillis() / 1000);
		ppSQL.setInt(++index, status);
		ppSQL.setString(++index, userName);
		ppSQL.setInt(++index, operType);
		return ppSQL.getSQL();
	}

	/**
	 * 更新用户开通状态
	 * 
	 * @param customerId
	 *            用户ID
	 * @param userStatus
	 *            用户状态，请使用SysConstant中的CUSTOMER_USER_STATUS_开头的常量
	 * @return 返回更新用户状态语句
	 */
	public String updateUserStatusSql(String customerId, int userStatus)
	{
		PrepareSQL pSql = new PrepareSQL(
				"update ? set user_status = ? where customer_id = ?");
		
		if ("jx_dx".equals(com.linkage.stbms.itv.main.Global.G_instArea)) {
			pSql = new PrepareSQL(
				"update ? set user_status = ? ,updatetime = "+ System.currentTimeMillis() / 1000 +" where customer_id = ?");
		}
		int index = 0;
		pSql.setStringExt(++index, "stb_tab_customer", false);
		pSql.setInt(++index, userStatus);
		pSql.setLong(++index, StringUtil.getLongValue(customerId));
		return pSql.getSQL();
	}
	
	/**
	 * 更新用户开通状态
	 * 特别的，如果用户ID等于0，则不更新用户表
	 * @param customerId 用户ID
	 * @param userStatus 用户状态，请使用SysConstant中的CUSTOMER_USER_STATUS_开头的常量
	 * @return 返回更新用户状态语句
	 */
	public void updateUserStatusSql(long customerId, int userStatus)
	{
		if (customerId > 0)
		{
			PrepareSQL pSql = new PrepareSQL(
					"update ? set user_status = ? where customer_id = ?");
			
			if ("jx_dx".equals(com.linkage.stbms.itv.main.Global.G_instArea)) {
				pSql = new PrepareSQL(
					"update ? set user_status = ? ,updatetime = "+ System.currentTimeMillis() / 1000 +" where customer_id = ?");
			}
			int index = 0;
			pSql.setStringExt(++index, "stb_tab_customer", false);
			pSql.setInt(++index, userStatus);
			pSql.setLong(++index, customerId);
			DBOperation.executeUpdate(pSql.getSQL());
		}
	}
	
	/**
	 * 记录零配置失败原因
	 * 
	 * @param userId 用户ID，如果等于0，则设置为null
	 * @param deviceId 上报设备ID
	 * @param failReasonId
	 *            失败原因 0:成功
	 *            1.E8-C终端未上报该机顶盒MAC；2.E8-C上报机顶盒MAC异常（含绑定多个机顶盒MAC）；3.IPTV账号不匹配；4.
	 *            AAA查询不到宽带账号拨号信息；5.AAA反馈宽带账号信息匹配失败
	 * @param reason
	 *            记录一些有用信息，供页面展示，和问题定位
	 */
	public void logZeroCfgFail(long userId, long deviceId, int failReasonId, String reason)
	{
		PrepareSQL pSql = new PrepareSQL(
				"insert into ?(customer_id,device_id,fail_reason_id,return_value,fail_time) values (?,?,?,?,?)");
		int index = 0;
		pSql.setStringExt(++index, "stb_tab_zeroconfig_fail", false);
		if (userId > 0)
		{
			pSql.setLong(++index, userId);
		}
		else
		{
			pSql.setString(++index, null);
		}
		pSql.setLong(++index, deviceId);
		pSql.setInt(++index, failReasonId);
		pSql.setString(++index, reason);
		pSql.setLong(++index, System.currentTimeMillis() / 1000);
		DBOperation.executeUpdate(pSql.getSQL());
	}
	
    /**
     * jx_dx记录零配置信息
     * @param userId
     * @param deviceId
     * @param failReasonId
     * @param reason
     * @param startTime
     * @param bindWay
     * @return
     */
	public String logZeroCfg(long userId, StbDeviceOBJ stbDeviceOBJ) {
		PrepareSQL pSql = new PrepareSQL(
				"insert into ?(buss_id,customer_id,device_id,fail_reason_id,return_value,fail_time,start_time,bind_way) values (?,?,?,?,?,?,?,?)");
		int index = 0;
		pSql.setStringExt(++index, "stb_tab_zeroconfig_fail",false);
		pSql.setString(++index, stbDeviceOBJ.getStbZeroconfigFailObj().getBussId());
		if (userId > 0)
		{
			pSql.setLong(++index, userId);
		}
		else
		{
			pSql.setString(++index, null);
		}
		pSql.setLong(++index, StringUtil.getLongValue(stbDeviceOBJ.getDeviceId()));
		pSql.setInt(++index, stbDeviceOBJ.getStbZeroconfigFailObj().getFailReasonId());
		pSql.setString(++index, stbDeviceOBJ.getStbZeroconfigFailObj().getReturnValue());
		pSql.setLong(++index, stbDeviceOBJ.getCurrTime());
		pSql.setLong(++index, stbDeviceOBJ.getStbZeroconfigFailObj().getStartTime());
		pSql.setInt(++index, stbDeviceOBJ.getBindWay());
		return pSql.getSQL();
	}
	
	/**
	 * jx_dx 更新bind_sate、bind_way字段
	 * @param stbDeviceOBJ
	 * @return
	 */
	public String updateDeviceByBindFail(StbDeviceOBJ stbDeviceOBJ){
		int bindState = stbDeviceOBJ.getBindState();
		int bindWay = stbDeviceOBJ.getBindWay();
		String deviceId = stbDeviceOBJ.getDeviceId();
		logger.warn(
				"updateDeviceByBindFail(bindSate:{},bindWay:{},deviceId:{})",
				new Object[] {bindState, bindWay, deviceId });
		return "update stb_tab_gw_device set bind_state=" + bindState + ",bind_way=" + bindWay
				+ ",bind_time=" + stbDeviceOBJ.getCurrTime()
				+ " where device_id= '" + deviceId + "'";
	}
	
	/**
	 * 查询设备信息
	 * 
	 * @param param
	 * @param paramType
	 *            1:deviceId 2:userId
	 * @return
	 */
	public Map<String, String> queryDeviceInfo(String param, int paramType)
	{
		logger.debug("queryDeviceInfo({},{})", param, paramType);
		PrepareSQL pSQL = new PrepareSQL();
		pSQL.append("select customer_id,serv_account,city_id,device_id,oui,device_serialnumber,status,zero_account,devicetype_id,loopback_ip,complete_time, ");
		if (1 == paramType)
		{
			pSQL.append("is_zero_version,vendor_id,device_model_id,cpe_mac from ");
			pSQL.append("stb_tab_gw_device");
			pSQL.append(" where device_id='" + param + "'");
		}
		else
		{
			pSQL.append("is_zero_version,vendor_id,device_model_id,cpe_mac from ");
			pSQL.append("stb_tab_gw_device");
			pSQL.append(" where customer_id=" + StringUtil.getLongValue(param) + "");
		}
		return DBOperation.getRecord(pSQL.getSQL());
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
		
		// mysql db
		if (3 == DBUtil.GetDB()) {
			sql.append("select pppoe_user,pppoe_pwd,serv_account,serv_pwd from stb_tab_customer where cust_stat in ('1','2') and customer_id=").append(userId);
		}
		else{
			sql.append("select * from stb_tab_customer where cust_stat in ('1','2') and customer_id=").append(userId);
		}
		
		Map<String, String> infoMap = DBOperation.getRecord(sql.toString());
		
		return infoMap;
		
	}
	
	public long createId()
	{
		if(DBUtil.GetDB() == 3) {
			return DbUtils.getUnusedID("sql_stb_logo_record", 1);
		}
		return ZeroConfDao.createUcid(1);
	}

	public synchronized static long createUcid(int count)
	{
		logger.debug("createUcid({})", count);
		long serial = -1;
		if (count <= 0)
		{
			serial = -2;
			return serial;
		}
		if (Global.MIN_UNUSED_PICUCID < 0)
		{
			if (Global.DB_ORACLE.equals(Global.DB_TYPE))
			{// oracle
			 Global.MIN_UNUSED_PICUCID = getMaxId4Oracle(Global.SUM_UNUSED_PICUCID) - 1;
			}
			else if (Global.DB_SYSBASE.equals(Global.DB_TYPE))
			{// sybase
				Global.MIN_UNUSED_PICUCID = getMaxId4Sybase(Global.SUM_UNUSED_PICUCID) - 1;
			}
			Global.MAX_UNUSED_PICUCID = Global.MIN_UNUSED_PICUCID
					+ Global.SUM_UNUSED_PICUCID;
		}
		if (Global.MAX_UNUSED_PICUCID < (Global.MIN_UNUSED_PICUCID + count))
		{
			if (Global.SUM_UNUSED_PICUCID < count)
			{
				if (Global.DB_ORACLE.equals(Global.DB_TYPE))
				{// oracle
				 Global.MIN_UNUSED_PICUCID = getMaxId4Oracle(count) - 1;
				}
				else if (Global.DB_SYSBASE.equals(Global.DB_TYPE))
				{// sybase
					Global.MIN_UNUSED_PICUCID = getMaxId4Sybase(count) - 1;
				}
				Global.MAX_UNUSED_PICUCID = Global.MIN_UNUSED_PICUCID + count;
			}
			else
			{
				if (Global.DB_ORACLE.equals(Global.DB_TYPE))
				{// oracle
				 Global.MIN_UNUSED_PICUCID = getMaxId4Oracle(Global.SUM_UNUSED_PICUCID);
				// - 1;
				}
				else if (Global.DB_SYSBASE.equals(Global.DB_TYPE))
				{// sybase
					Global.MIN_UNUSED_PICUCID = getMaxId4Sybase(Global.SUM_UNUSED_PICUCID) - 1;
				}
				Global.MAX_UNUSED_PICUCID = Global.MIN_UNUSED_PICUCID
						+ Global.SUM_UNUSED_PICUCID;
			}
		}
		serial = Global.MIN_UNUSED_PICUCID + 1;
		Global.MIN_UNUSED_PICUCID = Global.MIN_UNUSED_PICUCID + count;
		logger.debug("ID={}", serial);
		return serial;
	}

	public static long getMaxId4Sybase(int count)
	{
		logger.debug("getMaxId4Sybase({})", count);
		long serial = -1;
		if (count <= 0)
		{
			serial = -2;
			return serial;
		}
		String sql = "maxPicUpRecordIdProc ?";
		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setInt(1, count);
		return DBOperation.executeProcSelect(pSQL.getSQL());
	}
	
	public static long getMaxId4Oracle(int count) {
		logger.debug("getMaxId4Oracle({})", count);

		long serial = -1;

		if (count <= 0) {
			serial = -2;

			return serial;
		}

		CallableStatement cstmt = null;
		Connection conn = null;
		String sql = "{call maxPicUpRecordIdProc(?,?)}";

		try {
			conn = DBAdapter.getJDBCConnection();
			cstmt = conn.prepareCall(sql);
			cstmt.setInt(1, count);
			cstmt.registerOutParameter(2, Types.INTEGER);
			cstmt.execute();
			serial = cstmt.getLong(2);
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
	
}
