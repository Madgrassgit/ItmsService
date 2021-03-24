package com.linkage.itms.ct.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.ct.obj.CtRoutedQueryChecker;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;

/**
 * 桥改路由下发情况查询
 * 
 * @author Jason(3412)
 * @date 2010-7-13
 */
public class CtRoutedQueryService implements IService {

	private static Logger logger = LoggerFactory
			.getLogger(CtRoutedQueryService.class);

	//接口调用数据对象
	CtRoutedQueryChecker ctRoutedQueryChecker;
	
	
	/**
	 * 路由下发结果查询
	 * 
	 */
	@Override
	public String ctWorkService(String xmlParam) {
		logger.debug("ctWorkService()");
		// 检查合法性
		ctRoutedQueryChecker = new CtRoutedQueryChecker(xmlParam);
		if (false == ctRoutedQueryChecker.check()) {
			logger.error("验证未通过，返回：\n" + ctRoutedQueryChecker.getReturnXml());
			return ctRoutedQueryChecker.getReturnXml();
		}
		
		//初始化
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		String ctUsername = ctRoutedQueryChecker.getUsername(); 
		String ctDevSn = ctRoutedQueryChecker.getDevSn();
		Map<String, String> userDevMap = null;
		
		//查询用户信息
		userDevMap = userDevDao.queryUserInfo(1, ctUsername);
		if(null == userDevMap || userDevMap.isEmpty()){
			//未查询到用户
			logger.warn("查无此客户：" + ctRoutedQueryChecker.getUsername());
			ctRoutedQueryChecker.setResult(1002);
			ctRoutedQueryChecker.setResultDesc("查无此客户");
		}else{
			long userId = StringUtil.getLongValue(userDevMap.get("user_id"));
			String userDevId = userDevMap.get("device_id");
			if(StringUtil.IsEmpty(userDevId)){
				//用户未绑定终端
				logger.warn("未绑定终端：" + ctRoutedQueryChecker.getUsername());
				ctRoutedQueryChecker.setResult(1003);
				ctRoutedQueryChecker.setResultDesc("未绑定设备");
			}else{
				//查询终端信息
				ArrayList<HashMap<String, String>> devMapList = userDevDao.queryDevInfo(ctDevSn);
				if(null == devMapList || devMapList.isEmpty()){
					//未查询到终端
					logger.warn("未查询到终端：" + ctRoutedQueryChecker.getDevSn());
					ctRoutedQueryChecker.setResult(1004);
					ctRoutedQueryChecker.setResultDesc("查无此设备");
				}else{
					int devSize = devMapList.size();
					if(devSize > 1){
						//查询到多台终端
						logger.warn("查询到多台设备：" + ctRoutedQueryChecker.getDevSn());
						ctRoutedQueryChecker.setResult(1006);
						ctRoutedQueryChecker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
					}else{
						//只查询到一台
						HashMap<String, String> devMap = devMapList.get(0);
						String devId = devMap.get("device_id");
						if(userDevId.equals(devId)){ //用户终端绑定关系正确获取终端相关信息
							//查询用户是否做过路由业务,是否路由业务已开通
							int routeStat = CtInfoQueryService.routeStat(userId);
							setServResult(routeStat);
						}else{
							//用户终端绑定关系不正确
							logger.warn("查询到多台设备：" + ctRoutedQueryChecker.getDevSn());
							ctRoutedQueryChecker.setResult(1008);
							ctRoutedQueryChecker.setResultDesc("用户终端绑定关系不正确");
						}
					}
				}
			}
		}

		String returnXml = ctRoutedQueryChecker.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordCtLog(ctRoutedQueryChecker, "ctRoutedQuery");

		logger.warn("return({})", returnXml);

		// 回单
		return returnXml;
	}
	
	/**
	 * 置业务结果
	 * 1：下发成功	2：下发失败	0：无下发记录
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-7-14
	 * @return void
	 */
	private void setServResult(int _routeStat){
		logger.debug("setServResult({})", _routeStat);
		switch(_routeStat){
		case 1:
			ctRoutedQueryChecker.setServResult(1);
			ctRoutedQueryChecker.setServResultDesc("下发成功");
			break;
		case -1:
			ctRoutedQueryChecker.setServResult(2);
			ctRoutedQueryChecker.setServResultDesc("下发失败");
			break;
		case 0:
			ctRoutedQueryChecker.setServResult(0);
			ctRoutedQueryChecker.setServResultDesc("无下发记录");
			break;
		default:
			ctRoutedQueryChecker.setServResult(2);
			ctRoutedQueryChecker.setServResultDesc("下发失败");
		}
	}

}
