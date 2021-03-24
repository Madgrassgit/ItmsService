package com.linkage.itms.hlj.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.service.ServiceHandle;
import com.linkage.itms.hlj.dispatch.obj.DoVoipChecker;

public class DoVoipService implements HljIService {

	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(DoVoipService.class);
	UserDeviceDAO userDevDao = new UserDeviceDAO();
	ServiceHandle serviceHandle = new ServiceHandle();
	ServUserDAO servUserDao = new ServUserDAO();

	@Override
	public String work(String inXml) {
		DoVoipChecker checker = new DoVoipChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (false == checker.check()) {
				logger.warn("servicename[DoVoipService]cityId[{}]viopNumber[{}]验证未通过，返回：{}",
						new Object[] {checker.getCityId(), checker.getViopNumber(), inXml});
				return checker.getReturnXml();
			}
			logger.warn("servicename[DoVoipService]cityId[{}]viopNumber[{}]参数校验通过，入参为：{}",
					new Object[] {checker.getCityId(), checker.getViopNumber(), inXml});
			
			// 根据终端序列号
			Map<String, String> userMap = userDevDao.queryUserInfo(4, checker.getViopNumber(), checker.getCityId());
			if (null == userMap) {
				logger.warn("servicename[DoVoipService]cityId[{}]viopNumber[{}]查无此用户",
						new Object[] {checker.getCityId(), checker.getViopNumber()});
				checker.setResult(1002);
				checker.setResultDesc("查无此用户");
				writeLog(checker);
				return checker.getReturnXml();
			}
			// 设备Id
			String deviceId = getStringValue(userMap, "device_id");
			if (StringUtil.IsEmpty(deviceId)) {
				logger.warn("servicename[DoVoipService]cityId[{}]viopNumber[{}]用户未绑定设备",
						new Object[] { checker.getCityId(), checker.getViopNumber()});
				checker.setResult(1004);
				checker.setResultDesc("用户未绑定设备");
				writeLog(checker);
				return checker.getReturnXml();
			}
			// 属地不匹配
			if (!serviceHandle.cityMatch(checker.getCityId(), userMap.get("city_id"))) {
				logger.warn("servicename[DoVoipService]cityId[{}]viopNumber[{}]属地不匹配",
						new Object[] { checker.getCityId(), checker.getViopNumber()});
				checker.setResult(1003);
				checker.setResultDesc("属地不匹配");
				writeLog(checker);
				return checker.getReturnXml();
			}
			//属地匹配
			long userId = StringUtil.getLongValue(userMap.get("user_id"));
			String oui = getStringValue(userMap, "oui");
			String devSn = getStringValue(userMap, "device_serialnumber");
			checker.setLoid(getStringValue(userMap, "username"));
			
			// 获取用户的业务信息
			ArrayList<HashMap<String, String>> servUserMapList = servUserDao.queryHgwcustServUserByDevId(userId);
			// 是否受理了该业务
			if (null == servUserMapList || servUserMapList.isEmpty()) {
				logger.warn("servicename[DoVoipService]cityId[{}]viopNumber[{}]用户为受理任何业务",
						new Object[] { checker.getCityId(), checker.getViopNumber()});
				checker.setResult(1009);
				checker.setResultDesc("用户未受理任何业务");
				writeLog(checker);
				return checker.getReturnXml();
			}
			
			boolean hasSomeServ = false;
			// 遍历业务信息
			for (HashMap<String, String> servUserMap : servUserMapList) {
				if (14 != StringUtil.getIntegerValue(servUserMap.get("serv_type_id"))) {
					continue;
				}
				// 更新业务用户表的业务开通状态
				servUserDao.updateServOpenStatus(userId, 14);
				// 预读调用对象
				PreServInfoOBJ preInfoObj = new PreServInfoOBJ(StringUtil.getStringValue(userId), 
						deviceId, oui, devSn, "14", "1");
				int result = CreateObjectFactory.createPreProcess().processServiceInterface(CreateObjectFactory
						.createPreProcess().GetPPBindUserList(preInfoObj));
				logger.warn("下发业务执行结果[{}]" , result);
				if (1 != result) {
					logger.warn(
							"servicename[DoVoipService]cityId[{}]viopNumber[{}]设备[{}]下发特定业务，调用后台预读模块失败，业务类型为：[{}]", 
							new Object[] { checker.getCityId(), checker.getViopNumber(), deviceId, "14"});
					checker.setResult(1000);
					checker.setResultDesc("未知错误，请稍后重试");
				}
				hasSomeServ = true;
				break;
			}
			if (false == hasSomeServ) {
				logger.warn("servicename[DoVoipService]cityId[{}]viopNumber[{}]用户未受理该业务，无法进行业务下发",
						new Object[] { checker.getCityId(), checker.getViopNumber() });
				checker.setResult(1009);
				checker.setResultDesc("用户未受理该业务");
			}
			writeLog(checker);
		}
		catch (Exception e) {
			logger.warn("DoVoipService is error..", e);
		}
		return checker.getReturnXml();
	}
	
	/**
	 * 记录日志
	 * @param returnXml
	 * @param checker
	 * @param name
	 */
	private void writeLog(DoVoipChecker checker) {
		logger.warn("servicename[DoVoipService]cityId[{}]viopNumber[{}]处理结束，返回响应信息:{}",
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
