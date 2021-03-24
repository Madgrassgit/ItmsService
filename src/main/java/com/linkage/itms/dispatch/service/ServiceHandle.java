package com.linkage.itms.dispatch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.dao.CityDAO;
import com.linkage.itms.dao.UserInstReleaseDAO;

/**
 * 业务处理类
 * 
 * @author Jason(3412)
 * @date 2010-6-21
 */
public class ServiceHandle {

	private static Logger logger = LoggerFactory.getLogger(ServiceHandle.class);

	private UserInstReleaseDAO dao = new UserInstReleaseDAO();

	private boolean rsFlag = true;

	/**
	 * ITMS手工安装
	 * 
	 * @param accOid
	 *            登陆人ID
	 * @param userId
	 *            用户ID
	 * @param username
	 *            用户账号
	 * @param userCityId
	 *            用户属地
	 * @param deviceId
	 *            设备ID
	 * @param deviceCityId
	 *            设备属地
	 * @param oui
	 *            设备oui
	 * @param deviceNo
	 *            设备序列号
	 * @param dealstaff
	 *            操作人
	 * @param userFlag
	 *            1:新装 2:修障安装
	 * @return 操作信息提示
	 */
	public String itmsInst(long accOid, String userId, String username,
			String userCityId, String deviceId, String deviceCityId,
			String oui, String deviceNo, String dealstaff, int userFlag,
			int userline) {
		logger
				.debug(
						"itmsInst(userId:{},username:{},userCityId:{},deviceId:{},deviceCityId:{},oui:{},deviceNo:{},dealstaff:{},userFlag:{})",
						new Object[] { userId, username, userCityId, deviceId,
								deviceCityId, oui, deviceNo, dealstaff,
								userFlag });

		// 返回结果
		String rsMessage = "";

//		StringBuffer sbSQL = new StringBuffer();
//
//		String cityId = userCityId;
//
//		sbSQL.append(dao.getSQLByInstUpdHgwUser(StringUtil.getLongValue(userId), deviceId, oui,
//				deviceNo, cityId, false, userline));

		/**
		 * 更新设备表 更新权限域表
		 * 
		 */
//		sbSQL.append(";");
//		sbSQL.append(dao.getSQLByInstUpdDevice(cityId, userId, deviceId));
//		sbSQL.append(";");
//		sbSQL.append(dao.getSQLByDelResArea(deviceId));
//		if (!"00".equals(cityId)) {
//			List<String> list = AreaDAO.getAllPAreaIdByAreaId(AreaDAO
//					.getAreaIdByCityId(cityId));
//			for (int i = 0; i < list.size(); i++) {
//				sbSQL.append(";");
//				sbSQL.append(dao.getSQLByAddResArea(1, deviceId, Integer
//						.parseInt(list.get(i))));
//			}
//			list = null;
//		}

		// 增加现场安装记录
//		sbSQL.append(";");
//		sbSQL.append(dao.getSQLByAddBindlog(username,deviceId,0,99,null,userline,null,userFlag,1,dealstaff));
//
//		int rs = DBOperation.executeUpdate(sbSQL.toString().split(";"));
//		if (1 == rs) {
//			rsMessage = "手工绑定成功！";
//			this.rsFlag = true;
//		} else {
//			rsMessage = "手工绑定失败！";
//			logger.error("用户账号:{}对应设备:{}手工绑定失败", username, deviceNo);
//			this.rsFlag = false;
//		}

		
		/**
		 * 移动综调接口绑定时不下发业务
		 */
		// 更新业务用户表的开通状态
//		new ServUserDAO().updateServOpenStatus(StringUtil.getLongValue(userId));
//		
//		this.doPreProcessService(userId, deviceId, oui, deviceNo);
		/**调用资源绑定进行设备绑定    zhangshimin  2011-07-11**/
		CreateObjectFactory.createResourceBind(Global.GW_TYPE_ITMS).DoBindSingl(username, deviceId, dealstaff,userline);

		return rsMessage;
	}

	
	/**
	 * ITMS解绑
	 * 
	 * @param userId
	 * @param deviceId
	 * @param dealstaff
	 * @param string
	 * @return 操作信息提示
	 */
	public String itmsRelease(String userId, String username, String cityId,
			String deviceId, String dealstaff, int userline) {
		logger
				.debug(
						"itmsRelease(userId:{};username{};cityId:{};deviceId:{};dealstaff{})",
						new Object[] { userId, username, cityId, deviceId,
								dealstaff });

		// 返回结果
		String rsMessage = "";

//		StringBuffer sbSQL = new StringBuffer();
//		long userIdTemp = Long.parseLong(userId);
//
//		sbSQL.append(dao.getSQLByReleaseUpdHgwUser(userIdTemp));
//
//		/**
//		 * 设备属地更新到本地网
//		 * 
//		 */
//		sbSQL.append(dao.getSQLByReleaseUpdDevice(deviceId, CityDAO
//				.getLocationCityIdByCityId(cityId), true));
//		sbSQL.append(dao.getSQLByDelResArea(deviceId));
//		if (!"00".equals(CityDAO.getLocationCityIdByCityId(cityId))) {
//			List<String> list = AreaDAO.getAllPAreaIdByAreaId(AreaDAO
//					.getAreaIdByCityId(CityDAO
//							.getLocationCityIdByCityId(cityId)));
//			for (int i = 0; i < list.size(); i++) {
//				sbSQL.append(";");
//				sbSQL.append(dao.getSQLByAddResArea(1, deviceId, Integer
//						.parseInt(list.get(i))));
//			}
//			list = null;
//		}
//
//		// 增加现场解绑记录
//		sbSQL.append(";");
//		sbSQL.append(dao.getSQLByAddBindlog(username,deviceId,0,99,null,userline,null,2,1,dealstaff));
//		try {
//			@SuppressWarnings("unused")
//			// int[] rs = dao.batchUpdate(sbSQL.toString().split(";"));
//			int rs = DBOperation.executeUpdate(sbSQL.toString().split(";"));
//			rsMessage = "解绑成功！";
//			this.rsFlag = true;
//		} catch (Exception e) {
//			rsMessage = "解绑失败！";
//			logger.error("设备ID:{}解绑失败", deviceId);
//			this.rsFlag = false;
//		}
		/**调用资源绑定进行设备绑定    zhangshimin  2011-07-11**/
		CreateObjectFactory.createResourceBind(Global.GW_TYPE_ITMS).DoUnBindSingl(userId, deviceId, dealstaff,userline);
		return rsMessage;
	}
	
	/**
	 * IPOSS现场修障
	 * 
	 * @param userId
	 *            修障用户ID
	 * @param username
	 *            修障账号
	 * @param userCityId
	 *            修障用户属地
	 * @param oldDeviceId
	 *            修障前设备ID
	 * @param deviceId
	 *            修障待绑定设备ID
	 * @param deviceCityId
	 *            修障待绑定设备属地
	 * @param oui
	 *            修障待绑定设备OUI
	 * @param deviceNo
	 *            修障待绑定设备序列号
	 * @param faultId
	 *            修障原因
	 * @param dealStaff
	 * @param dealStaffid
	 * @return
	 */
	public String ipossItmsModify(String userId, String username,
			String userCityId, String oldDeviceId, String deviceId,
			String deviceCityId, String oui, String deviceNo, String faultId,
			String dealStaff, String dealStaffid, int userline) {
		logger.debug("ipossItmsModify()");
		this.itmsRelease(userId, username, userCityId, oldDeviceId, dealStaff, userline);
		if (!this.rsFlag) {
			logger.error("ipossItmsModify=>账号{}修障时解绑失败！", username);
			return "修障失败";
		}
		DBOperation.executeUpdate(dao.getSaveFault(username, deviceId, faultId,
				dealStaff, dealStaffid));
		this.itmsInst(1, userId, username, userCityId, deviceId, deviceCityId,
				oui, deviceNo, dealStaff, 2, 0);
		if (!this.rsFlag) {
			logger.error("ipossItmsModify=>账号{}修障时重安装失败！", username);
			return "修障失败";
		}
		return "修障成功";
	}

	/**
	 * 判断属地是否匹配；是否cityId和dbCityId相同或者cityId为父属地
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-22
	 * @return boolean
	 */
	public boolean cityMatch(String cityId, String dbCityId){
		logger.debug("cityMatch({}, {})", cityId, dbCityId);
		
		if(StringUtil.IsEmpty(cityId) || StringUtil.IsEmpty(dbCityId)){
			return false;
		}
		
		if(CityDAO.getLocationCityIdByCityId(cityId).equals(CityDAO.getLocationCityIdByCityId(dbCityId))){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 判断属地是否为省中心
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-22
	 * @return boolean
	 * 是返回true; 否则返回false
	 */
	public boolean isSZX(String cityId){
		logger.debug("isSZX({})", cityId);
		if("-1".equals(Global.G_City_Pcity_Map.get(cityId))){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 调用预读执行业务
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-22
	 * @return void
	 */
	public void doPreProcessService(String userId, String deviceId, String oui, String devSn){
		PreServInfoOBJ preInfoObj = new PreServInfoOBJ(userId, deviceId,
				oui, devSn, "", "1");
		CreateObjectFactory.createPreProcess().processServiceInterface(CreateObjectFactory.createPreProcess().GetPPBindUserList(preInfoObj));
	}
}
