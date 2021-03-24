package com.linkage.itms.hlj.dispatch.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.service.ServiceHandle;
import com.linkage.itms.hlj.dispatch.obj.QueryVoipBssStateChecker;

public class QueryVoipBssStateService implements HljIService {

	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(QueryVoipBssStateService.class);
	UserDeviceDAO userDevDao = new UserDeviceDAO();
	ServiceHandle serviceHandle = new ServiceHandle();
	HashMap<String, String> state = new HashMap<String, String>();

	public QueryVoipBssStateService() {
		state.put("1", "成功");
		state.put("0", "已绑定未下发");
		state.put("-1", "失败");
	}
	
	@Override
	public String work(String inXml) {
		QueryVoipBssStateChecker checker = new QueryVoipBssStateChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (false == checker.check()) {
				logger.warn("servicename[QueryVoipBssStateService]cityId[{}]viopNumber[{}]验证未通过，返回：{}",
						new Object[] {checker.getCityId(), checker.getViopNumber(), inXml});
				return checker.getReturnXml();
			}
			logger.warn("servicename[QueryVoipBssStateService]cityId[{}]viopNumber[{}]参数校验通过，入参为：{}",
					new Object[] {checker.getCityId(), checker.getViopNumber(), inXml});
			
			// 根据终端序列号
			Map<String, String> userMap = userDevDao.queryUserInfo(4, checker.getViopNumber(), checker.getCityId());
			if (null == userMap) {
				logger.warn("servicename[QueryVoipBssStateService]cityId[{}]viopNumber[{}]查无此用户",
						new Object[] {checker.getCityId(), checker.getViopNumber()});
				checker.setResult(1002);
				checker.setResultDesc("查无此用户");
				writeLog(checker);
				return checker.getReturnXml();
			}
			
			// 属地不匹配
			if (!serviceHandle.cityMatch(checker.getCityId(), userMap.get("city_id"))) {
				logger.warn("servicename[QueryVoipBssStateService]cityId[{}]viopNumber[{}]属地不匹配",
						new Object[] { checker.getCityId(), checker.getViopNumber()});
				checker.setResult(1003);
				checker.setResultDesc("属地不匹配");
				writeLog(checker);
				return checker.getReturnXml();
			}
			
			checker.setLoid(getStringValue(userMap, "username"));
			if (StringUtil.IsEmpty(userMap.get("device_id"))) {
				checker.setVoipBindSate("未做");
			}
			else {
				checker.setVoipBindSate(getStringValue(state, getStringValue(userMap, "open_status")));
			}
			
		}
		catch (Exception e) {
			logger.warn("QueryVoipBssStateService is error..", e);
		}
		return checker.getReturnXml();
	}
	
	/**
	 * 记录日志
	 * @param returnXml
	 * @param checker
	 * @param name
	 */
	private void writeLog(QueryVoipBssStateChecker checker) {
		logger.warn("servicename[QueryVoipBssStateService]cityId[{}]viopNumber[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCityId(), checker.getViopNumber(), checker.getReturnXml()});
	}
	
	/**
	 * 格式化数据
	 * @param map
	 * @param columName
	 * @return
	 */
	public static String getStringValue(Map<String, String> map, String columName) {
		if (null == columName || null == map || null == map.get(columName)) {
			return "";
		}
		return map.get(columName).toString();
	}
}
