package com.linkage.itms.dispatch.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commom.util.CheckStrategyUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.DevOnlineCAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.DevOnlineChecker;

/**
 * 设备在线情况查询处理类
 * 
 * @author Jason(3412)
 * @date 2010-9-2
 */
public class DevOnlineService implements IService {

	// 日志记录
	private static Logger logger = LoggerFactory
			.getLogger(DevOnlineService.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.linkage.itms.dispatch.service.IService#work(java.lang.String)
	 */
	@Override
	public String work(String inXml) {
		DevOnlineChecker checker = new DevOnlineChecker(inXml);
		if (false == checker.check()) {
			logger.error(
					"servicename[DevOnlineService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUsername(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[DevOnlineService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUsername(),
						inXml });
		// 在线情况
		boolean succ = false;
		String strOnline = null;
		String deviceId = null;
		String complete_time = null;
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		// 获取用户帐号 or 终端序列号
		if (1 == checker.getSearchType()) {
			// 根据用户帐号获取
//			Map<String, String> userMap = userDevDao.getUserDevInfo(checker
//					.getUsername());
			Map<String, String> userMap = userDevDao.queryUserInfo(checker.getUserInfoType(),checker.getUsername(), checker.getCityId());
			if (null == userMap || userMap.isEmpty()) {
				logger.warn(
						"servicename[DevOnlineService]cmdId[{}]userinfo[{}]查无此用户",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1002);
				checker.setResultDesc("查无此客户");
			} else {
				//设备ID
				deviceId = userMap.get("device_id");
				//用户属地
				String userCityId = userMap.get("city_id");
				if (StringUtil.IsEmpty(deviceId)) {
					logger.warn(
							"servicename[DevOnlineService]cmdId[{}]userinfo[{}]用户未绑定设备",
							new Object[] { checker.getCmdId(), checker.getUsername()});
					checker.setResult(1003);
					checker.setResultDesc("未绑定设备");
				} else {
					// 江西是根据city_id参数模糊匹配找出的数据,所以没必要在验证city_id
					if (!"jx_dx".equals(Global.G_instArea) && !"xj_dx".equals(Global.G_instArea) && !"nx_dx".equals(Global.G_instArea) && !"nmg_dx".equals(Global.G_instArea) && false == serviceHandle.cityMatch(checker.getCityId(),
							userCityId) && !"CUC".equalsIgnoreCase(Global.G_OPERATOR)) {// 属地不匹配
						logger.warn(
								"servicename[DevOnlineService]cmdId[{}]userinfo[{}]属地不匹配，查无此用户",
								new Object[] { checker.getCmdId(), checker.getUsername()});
						checker.setResult(1003);
						checker.setResultDesc("查无此用户");
					} else {// 属地匹配
						checker.setResult(0);
						checker.setCityId(userCityId);
						checker.setResultDesc("成功");
						Map<String, String> devMap = userDevDao.getDevStatus(userMap.get("user_id"));
						if(null == devMap || devMap.isEmpty())
						{
							logger.warn(
									"servicename[DevOnlineService]cmdId[{}]userinfo[{}]用户未绑定设备",
									new Object[] { checker.getCmdId(), checker.getUsername()});
							checker.setResult(1003);
							checker.setResultDesc("未绑定设备");
						}
						else
						{
							strOnline = devMap.get("online_status");
							complete_time = devMap.get("complete_time");
							succ = true;
						}
					}
				}
			}
		} else if (2 == checker.getSearchType()) {
			// 根据终端序列号
			ArrayList<HashMap<String, String>> devlsit = userDevDao
					.getDevStatusInfo(checker.getDevSn());
			if (null == devlsit || devlsit.isEmpty()) {
				logger.warn(
						"servicename[DevOnlineService]cmdId[{}]userinfo[{}]无此设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1004);
				checker.setResultDesc("查无此设备");
			} else if (devlsit.size() > 1) {
				logger.warn(
						"servicename[DevOnlineService]cmdId[{}]userinfo[{}]查询到多台设备",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
			} else {
				Map<String, String> devMap = devlsit.get(0);
				//设备ID
				deviceId = devMap.get("device_id");
				//属地
				String deviceCityId = devMap.get("city_id");
				if (!"xj_dx".equals(Global.G_instArea) && !"nx_dx".equals(Global.G_instArea) && !"nmg_dx".equals(Global.G_instArea) && false == serviceHandle.cityMatch(checker.getCityId(),
						deviceCityId) && !"CUC".equalsIgnoreCase(Global.G_OPERATOR)) {// 属地不匹配
					logger.warn(
							"servicename[DevOnlineService]cmdId[{}]userinfo[{}]属地不匹配，查无此设备",
							new Object[] { checker.getCmdId(), checker.getUsername()});
					checker.setResult(1005);
					checker.setResultDesc("查无此设备");
				} else {// 属地匹配
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setCityId(deviceCityId);
					strOnline = devMap.get("online_status");
					complete_time = devMap.get("complete_time");
					succ = true;
				}
			}
		}
		
		if("nx_dx".equals(Global.G_instArea)){
			if(complete_time==null || complete_time.trim().length()==0){
				checker.setRegTime("");
			}else{
				try {
					long time = Long.parseLong(complete_time+"000");
					// 注册时间:YYYY-MM-DD hh:mm:ss, 样例：2016-03-11 12:33:00
					Date date = new Date();
					date.setTime(time);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					checker.setRegTime(sdf.format(date));
				} catch (Exception e) {
					checker.setRegTime(complete_time+"（时间格式转换失败）");
				}
			}
		}

		//成功获取到状态信息
		if (true == succ)
		{
			// (江西)判断设备是否繁忙或者业务正在下发
			if ("jx_dx".equals(Global.G_instArea)
					&& false == CheckStrategyUtil.chechStrategy(deviceId))
			{
				logger.warn(
						"servicename[DevOnlineService]cmdId[{}]userinfo[{}]设备繁忙或者业务正在下发，请稍候重试",
						new Object[] { checker.getCmdId(), checker.getUsername() });
				checker.setResult(1005);
				checker.setResultDesc("设备繁忙或者业务正在下发，请稍候重试");
			}
			else
			{
				// 获取到了在线状态
				int intOnlined = StringUtil.getIntegerValue(strOnline, 100);
				if (0 == intOnlined)
				{// 不在线
					// 设置参数
					checker.setOnlineStatus(-1);
				}
				else
				{
					// 实时获取在线状态
					int iOnline = DevOnlineCAO.devOnlineTest(deviceId) == 1 ? 1 : -1;
					// 设置参数
					checker.setOnlineStatus(iOnline);
				}
			}
		} else {
			logger.warn(
					"servicename[DevOnlineService]cmdId[{}]userinfo[{}]获取在线状态失败",
					new Object[] { checker.getCmdId(), checker.getUsername()});
		}

		// 接口回复XML
		String returnXml = checker.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(),
				"DevOnlineService");
		logger.warn(
				"servicename[DevOnlineService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
		// 回单
		return returnXml;
	}
	
}
