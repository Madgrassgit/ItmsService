package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.PvcReformedChecker;

/**
 * 多PVC部署情况
 * 
 * @author Jason(3412)
 * @date 2010-9-2
 */
public class PvcReformedService implements IService {

	// 日志记录
	private static Logger logger = LoggerFactory
			.getLogger(PvcReformedService.class);

	/**
	 * 多PVC考核状态获取方法
	 * 
	 */
	@Override
	public String work(String inXml) {
		
		PvcReformedChecker checker = new PvcReformedChecker(inXml);
		if (false == checker.check()) {
			logger.error(
					"servicename[PvcReformedService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[PvcReformedService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						inXml });
		// DAO
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		boolean succ = false;
		String reformFlag = null;
		// 获取用户帐号 or 终端序列号
		if (1 == checker.getSearchType()) {
			// 根据用户帐号获取
			Map<String, String> iptvUserMap = userDevDao
					.queryPvcReformed(checker.getUsername());
			if (null == iptvUserMap || iptvUserMap.isEmpty()) {
				logger.warn(
						"servicename[PvcReformedService]cmdId[{}]userinfo[{}]查无此用户",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1002);
				checker.setResultDesc("查无此客户");
			} else {
				String userCityId = iptvUserMap.get("city_id");
				if (false == serviceHandle.cityMatch(checker.getCityId(),
						userCityId)) {// 属地不匹配
					logger.warn(
							"servicename[PvcReformedService]cmdId[{}]userinfo[{}]属地不匹配，查无此用户",
							new Object[] { checker.getCmdId(), checker.getUserInfo()});
					checker.setResult(1003);
					checker.setResultDesc("查无此用户");
				} else {// 属地匹配
					checker.setResult(0);
					checker.setResultDesc("成功");
					reformFlag = iptvUserMap.get("reform_flag");
					succ = true;
				}

			}
		} else if (2 == checker.getSearchType()) {
			// 根据终端序列号
			ArrayList<HashMap<String, String>> devlsit = userDevDao
					.getTelePasswdByDevSn(checker.getDevSn());
			if (null == devlsit || devlsit.isEmpty()) {
				logger.warn(
						"servicename[PvcReformedService]cmdId[{}]userinfo[{}]查无此设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1004);
				checker.setResultDesc("查无此设备");
			} else if (devlsit.size() > 1) {
				logger.warn(
						"servicename[PvcReformedService]cmdId[{}]userinfo[{}]查询到多台设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
			} else {
				Map<String, String> devMap = devlsit.get(0);
				if (StringUtil.IsEmpty(devMap.get("username"))) {
					logger.warn(
							"servicename[PvcReformedService]cmdId[{}]userinfo[{}]未绑定用户",
							new Object[] { checker.getCmdId(), checker.getUserInfo()});
					checker.setResult(1003);
					checker.setResultDesc("未绑定用户");
				} else {
					// 属地检查
					String deviceCityId = devMap.get("city_id");
					if (false == serviceHandle.cityMatch(checker.getCityId(),
							deviceCityId)) {// 属地不匹配
						logger.warn(
								"servicename[PvcReformedService]cmdId[{}]userinfo[{}]属地不匹配，查无此设备",
								new Object[] { checker.getCmdId(), checker.getUserInfo()});
						checker.setResult(1005);
						checker.setResultDesc("查无此设备");
					} else {// 属地匹配
						String username = devMap.get("username");
						// 根据用户帐号获取
						Map<String, String> iptvUserMap = userDevDao
								.queryPvcReformed(username);
						if(null == iptvUserMap || iptvUserMap.isEmpty()){
							logger.warn(
									"servicename[PvcReformedService]cmdId[{}]userinfo[{}]无此PVC用户",
									new Object[] { checker.getCmdId(), checker.getUserInfo()});
							checker.setResult(1002);
							checker.setResultDesc("无此设备对应的IPTV客户");
						}else{
							checker.setResult(0);
							checker.setResultDesc("成功");
							reformFlag = iptvUserMap.get("reform_flag");
							succ = true;
						}
					}
				}
			}
		}

		// 设置多PVC改造状态
		if (true == succ) {
			int reformed = StringUtil.getIntegerValue(reformFlag, 100);
			if (1 == reformed) {
				// 多PVC已改造
				checker.setPvcReformed(1);
			} else {
				// 未改造
				checker.setPvcReformed(-1);
			}
		}

		// 接口回复XML
		String returnXml = checker.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(),
				"PvcReformedService");
		logger.warn(
				"servicename[PvcReformedService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}

}
