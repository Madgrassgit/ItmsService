package com.linkage.itms.ct.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.ct.obj.CtInfoQueryChecker;
import com.linkage.itms.dao.DeviceTypeDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;

/**
 * 
 * 路由下发支持情况查询类
 * 
 * @author Jason(3412)
 * @date 2010-7-13
 */
public class CtInfoQueryService implements IService {

	private static Logger logger = LoggerFactory
			.getLogger(CtInfoQueryService.class);
	
	//接口调用数据对象
	private CtInfoQueryChecker ctInfoQueryChecker;
	
	
	/**
	 * 查询用户终端是否支持路由开通
	 * 
	 */
	@Override
	public String ctWorkService(String xmlParam) {
		logger.debug("ctWorkService()");
		// 检查合法性
		ctInfoQueryChecker = new CtInfoQueryChecker(xmlParam);
		if (false == ctInfoQueryChecker.check()) {
			logger.error("验证未通过，返回：\n" + ctInfoQueryChecker.getReturnXml());
			return ctInfoQueryChecker.getReturnXml();
		}

		//初始化
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		String ctUsername = ctInfoQueryChecker.getUsername(); 
		String ctDevSn = ctInfoQueryChecker.getDevSn();
		Map<String, String> userDevMap = null;
		
		//查询用户信息
		userDevMap = userDevDao.queryUserInfo(1, ctUsername);
		if(null == userDevMap || userDevMap.isEmpty()){
			//未查询到用户
			logger.warn("查无此客户：" + ctInfoQueryChecker.getUsername());
			ctInfoQueryChecker.setResult(1002);
			ctInfoQueryChecker.setResultDesc("查无此客户");
		}else{
			long userId = StringUtil.getLongValue(userDevMap.get("user_id"));
			String userDevId = userDevMap.get("device_id");
			if(StringUtil.IsEmpty(userDevId)){
				//用户未绑定终端
				logger.warn("未绑定终端：" + ctInfoQueryChecker.getUsername());
				ctInfoQueryChecker.setResult(1003);
				ctInfoQueryChecker.setResultDesc("未绑定设备");
			}else{
				//查询终端信息
				ArrayList<HashMap<String, String>> devMapList = userDevDao.queryDevInfo(ctDevSn);
				if(null == devMapList || devMapList.isEmpty()){
					//未查询到终端
					logger.warn("未查询到终端：" + ctInfoQueryChecker.getDevSn());
					ctInfoQueryChecker.setResult(1004);
					ctInfoQueryChecker.setResultDesc("查无此设备");
				}else{
					int devSize = devMapList.size();
					if(devSize > 1){
						//查询到多台终端
						logger.warn("查询到多台设备：" + ctInfoQueryChecker.getDevSn());
						ctInfoQueryChecker.setResult(1006);
						ctInfoQueryChecker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
					}else{
						//只查询到一台
						HashMap<String, String> devMap = devMapList.get(0);
						String devId = devMap.get("device_id");
						if(userDevId.equals(devId)){
							//用户终端绑定关系正确获取终端相关信息
							ctInfoQueryChecker.setDevSerialWhole(devMap.get("device_serialnumber"));
							int devTypeId = StringUtil.getIntegerValue(devMap.get("devicetype_id"));
							//查询设备厂商，型号等信息
							DeviceTypeDAO devTypeDao = new DeviceTypeDAO();
							devTypeDao.queryDeviceType(devTypeId);
							
							ctInfoQueryChecker.setDevFactory(devTypeDao.getDeviceVendor());
							ctInfoQueryChecker.setDevModel(devTypeDao.getDeviceModel());
							
							//查询终端是否支持路由
							int routed = DeviceTypeDAO.routedSupported(devTypeId);
							ctInfoQueryChecker.setRouteSupported(routed);
							//获取用户现在的上网方式
							ctInfoQueryChecker.setNetType(routeOrBridge(userId));
						}else{
							//用户终端绑定关系不正确
							logger.warn("查询到多台设备：" + ctInfoQueryChecker.getDevSn());
							ctInfoQueryChecker.setResult(1008);
							ctInfoQueryChecker.setResultDesc("用户终端绑定关系不正确");
						}
					}
				}
			}
		}

		String returnXml = ctInfoQueryChecker.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordCtLog(ctInfoQueryChecker, "ctInfoQuery");

		logger.warn("return({})", returnXml);

		// 回单
		return returnXml;
	}
	
	
	/**
	 * 获取用户上网方式，桥接还是路由
	 * 
	 * @param 
	 * 	_userId：用户ID
	 * @author Jason(3412)
	 * @date 2010-7-14
	 * @return int 
	 * 	1:桥接 2:路由
	 */
	private int routeOrBridge(long _userId){
		logger.debug("routeOrBridge({})", _userId);
		if(1 == routeStat(_userId)){
			return 2;
		}
		return 1;
	}
	
	
	/**
	 * 获取用户上网业务路由方式的开通状态
	 * 
	 * @param 
	 * 	_userId：用户ID
	 * @author Jason(3412)
	 * @date 2010-7-14
	 * @return int 
	 * 	1:路由开通成功  -1:路由开通失败  0:未做路由开通
	 */
	public static int routeStat(long _userId){
		logger.debug("routeStat({})", _userId);
		//查询用户是否做过路由业务,是否路由业务已开通
		int stat = 0;
		ServUserDAO servDao = new ServUserDAO();
		Map<String, String>  routeUser = servDao.queryRoutedUserInfo(_userId);
		if(null != routeUser && false == routeUser.isEmpty()){
			int parmStat = StringUtil.getIntegerValue(routeUser.get("parm_stat"));
			if(1 == parmStat){
				stat = 1;
			}else{
				stat = -1;
			}
		}else{
			//未做过该业务
			stat = 0;
		}
		return stat;
	}
	
}
