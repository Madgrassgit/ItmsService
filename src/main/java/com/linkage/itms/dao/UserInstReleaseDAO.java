package com.linkage.itms.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.PrepareSQL;

/**
 * @author Jason(3412)
 * @date 2010-6-21
 */
public class UserInstReleaseDAO {

	private static Logger logger = LoggerFactory.getLogger(UserInstReleaseDAO.class);
	
	// /////////////////////////////////////////////////////////////////////////
	// ////////////////////////////家庭网关现场安装相关SQL//////////////////////////
	// /////////////////////////////////////////////////////////////////////////
	/**
	 * 家庭网关更新数据库（绑定时操作）
	 * 
	 * @param userId
	 *            不可为空
	 * @param deviceId
	 *            不可为空
	 * @param oui
	 *            不可为空
	 * @param deviceNo
	 *            不可为空
	 * @param cityId
	 *            不可为空
	 * @param cityUpdFlag
	 *            不可为空
	 * @return
	 */
	public String getSQLByInstUpdHgwUser(long userId, String deviceId,
			String oui, String deviceNo, String cityId, boolean cityUpdFlag, int userline) {
		logger.debug("UserInstReleaseDAO=>getSQLByInstUpdHgwUser(userId:{},deviceId:{},oui:{},deviceNo:{},cityId:{},cityUpdFlag:{})",
						new Object[] { userId, deviceId, oui, deviceNo, cityId,
								cityUpdFlag });

		PrepareSQL ppSQL = new PrepareSQL(
				" update tab_hgwcustomer set oui=?,device_serialnumber=?, device_id=? ,updatetime=? , binddate=? , userline=?");
		if (cityUpdFlag) {
			ppSQL.append(" ,city_id='");
			ppSQL.append(cityId);
			ppSQL.append("' ");
		}
		long time = System.currentTimeMillis()/1000;
		ppSQL.append(" where user_id= ");
		ppSQL.append(String.valueOf(userId));
		ppSQL.setString(1, oui);
		ppSQL.setString(2, deviceNo);
		ppSQL.setString(3, deviceId);
		ppSQL.setLong(4, time);
		ppSQL.setLong(5, time);
		ppSQL.setInt(6, userline);

		return ppSQL.toString();
	}
	
	
	/**
	 * 更新设备表，现场安装时更新 （为了性能，传入字段不能为空，但不作控制）
	 * 
	 * @param cityId
	 *            不可为空
	 * @param customerId
	 *            不可为空
	 * @param deviceId
	 *            不可为空,为空则返回null的sql
	 * @return
	 */
	public String getSQLByInstUpdDevice(String cityId, String customerId,
			String deviceId) {
		logger.debug("UserInstReleaseDAO=>getSQLByAddUserinst(cityId:{},customerId:{},deviceId:{})",
						new Object[] { cityId, customerId, deviceId });

		if (null == deviceId || "".equals(deviceId)) {
			return null;
		}

		PrepareSQL ppSQL = new PrepareSQL(
				" update tab_gw_device set device_status=1,cpe_allocatedstatus=1,city_id=?,customer_id=? where device_id = ?");
		ppSQL.setString(1, cityId);
		ppSQL.setString(2, customerId);
		ppSQL.setString(3, deviceId);

		return ppSQL.toString();
	}


	/**
	 * modify date 2010-5-20
	 * 
	 * 插入绑定日志表，bind_log，自修改日起，所有绑定日志均操作bind_log
	 * 
	 */
	public String getSQLByAddBindlog(String username,String deviceId,
			int bindStatus,int bindResult,String bindDesc,int userline,
			String remark,int operType,int bindType,String dealstaff){
		logger.debug("UserInstReleaseDAO=>getSQLByAddBindlog()");
		
		PrepareSQL ppSQL = new PrepareSQL(" insert into bind_log (bind_id,username," +
							"device_id,binddate,bind_status,bind_result," +
							"bind_desc,userline,remark,oper_type,bind_type," +
							"dealstaff) values (?,?,?,?,?,?,?,?,?,?,?,?)");
		
		ppSQL.setLong(1,generateLongId());
		ppSQL.setString(2, username);
		ppSQL.setString(3, deviceId);
		ppSQL.setLong(4, System.currentTimeMillis()/1000);
		ppSQL.setInt(5, bindStatus);
		ppSQL.setInt(6, bindResult);
		ppSQL.setString(7, bindDesc);
		ppSQL.setInt(8, userline);
		ppSQL.setString(9, remark);
		ppSQL.setInt(10, operType);
		ppSQL.setInt(11, bindType);
		ppSQL.setString(12, dealstaff);
		
		return ppSQL.toString();
	}

	/**
	 * 更新设备表，解绑时更新
	 * 
	 * @param deviceId
	 *            不可为空 为空则返回sql为null；
	 * @param cityId
	 *            不可为空 一般来说，ITMS到本地网级，BBMS到省中心；
	 * @param cityUpdFlag
	 *            不可为空，true表示需要更新属地，false表示不需要更新属地；
	 * @return
	 */
	public String getSQLByReleaseUpdDevice(String deviceId,String cityId, boolean cityUpdFlag) {
		logger.debug("UserInstReleaseDAO=>getSQLByReleaseUpdDevice(deviceId:{},cityId:{},cityUpdFlag:{})",
				new Object[] {deviceId,cityId,cityUpdFlag});

		if (null == deviceId || "".equals(deviceId)) {
			return null;
		}

		PrepareSQL ppSQL = new PrepareSQL(" update tab_gw_device set cpe_allocatedstatus=0,customer_id=null ");
		if (cityUpdFlag) {
			ppSQL.append(" ,city_id='");
			ppSQL.append(cityId);
			ppSQL.append("' ");
		}
		ppSQL.append(PrepareSQL.WHERE, "device_id", PrepareSQL.EQUEAL,deviceId, false);

		return ppSQL.toString();
	}

	/**
	 * 删除权限域表
	 * 
	 * @param resId
	 * @return
	 */
	public String getSQLByDelResArea(String resId) {
		logger.debug("UserInstReleaseDAO=>getSQLByDelResArea(resId:{})", resId);
		
		PrepareSQL ppSQL = new PrepareSQL(" delete from tab_gw_res_area where res_id =? and res_type = 1 ");
		ppSQL.setString(1, resId);

		return ppSQL.toString();
	}

	/**
	 * 插入权限域表
	 * 
	 * @param resType
	 * @param resId
	 * @param AreaId
	 * @return
	 */
	public String getSQLByAddResArea(int resType, String resId, int AreaId) {
		logger.debug("UserInstReleaseDAO=>getSQLByAddResArea(resType:{},resId:{},AreaId:{})",
						new Object[] { resType, resId, AreaId });

		PrepareSQL ppSQL = new PrepareSQL(
				" insert into tab_gw_res_area(res_type,res_id,area_id) values(?,?,?) ");
		ppSQL.setInt(1, resType);
		ppSQL.setString(2, resId);
		ppSQL.setInt(3, AreaId);

		return ppSQL.toString();
	}

	/**
	 * 针对修障，添加修障原因
	 * 
	 * @param username 修障的账号
	 * @param device_id 修障的设备
	 * @param fault_id 修障原因ID
	 * @param dealStaff 
	 * @param dealStaffid
	 * @return
	 */
	public String getSaveFault(String username, String deviceId, String faultId,
			String dealStaff, String dealStaffid){
		logger.debug("UserInstReleaseDAO=>getSaveFault(username:{},deviceId:{},faultId:{},dealStaff:{},dealStaffid:{})",
				new Object[] { username, deviceId, faultId,dealStaff,dealStaffid });
		PrepareSQL ppSQL = new PrepareSQL("insert into tab_devicefault(username,device_id,fault_id,faulttime,dealstaff,dealstaffid) values(?,?,?,?,?,?)");
		ppSQL.setString(1, username);
		ppSQL.setString(2, deviceId);
		ppSQL.setString(3, faultId);
		ppSQL.setLong(4, System.currentTimeMillis()/1000);
		ppSQL.setString(5, dealStaff);
		ppSQL.setString(6, dealStaffid);
		return ppSQL.toString();		
	}
	
	/**
	 * 更新家庭网关用户数据（家庭网关解绑时操作）
	 * 
	 * @param userId
	 * @return
	 */
	public String getSQLByReleaseUpdHgwUser(long userId){
		logger.debug("UserInstReleaseDAO=>getSQLByReleaseUpdHgwUser(userId:{})",userId);

		PrepareSQL ppSQL = new PrepareSQL(
				" update tab_hgwcustomer set oui=null,device_serialnumber=null,device_id=null, binddate=null, updatetime=? where user_id=?");
		ppSQL.setLong(1, System.currentTimeMillis()/1000);
		ppSQL.setLong(2, userId);

		return ppSQL.toString();
	}
	
	
	/**
	 * 产生唯一主键，利用较低的重复概率
	 * @return long
	 */
	public static long generateLongId() {
		return Math.round(Math.random() * 10000000000L);
	}

}
