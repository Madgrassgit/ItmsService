package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.QueryCityIdChecker;


public class QueryCityIdService implements IService {

	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(QueryCityIdService.class);

	@Override
	public String work(String inXml) {
		QueryCityIdChecker checker = new QueryCityIdChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (false == checker.check()) {
				logger.info("servicename[QueryCityIdService]cmdId[{}]devSn[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getDevSn(), inXml});
				return checker.getReturnXml();
			}
			logger.info("servicename[QueryCityIdService]cmdId[{}]devSn[{}]参数校验通过，入参为：{}",
					new Object[] {checker.getCmdId(), checker.getDevSn(), inXml});
			DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
			ArrayList<HashMap<String, String>> deviceMapList = deviceInfoDAO.queryDevCityId(checker.getDevSn());
			// 没有查询数据
			if (null == deviceMapList || deviceMapList.isEmpty()) {
				logger.info("servicename[QueryCityIdService]cmdId[{}]devSn[{}]查无此设备",
						new Object[] {checker.getCmdId(), checker.getDevSn()});
				checker.setResult(1004);
				checker.setResultDesc("查无此设备");
				writeLog(checker);
				return checker.getReturnXml();
			}
			// 查询多条记录
			if (deviceMapList.size() > 1) {
				logger.info("servicename[QueryCityIdService]cmdId[{}]devSn[{}]请输入完整的或更多SN",
						new Object[] {checker.getCmdId(), checker.getDevSn()});
				String serialList = "";
				for (int i = 0; i < deviceMapList.size(); i++) {
					serialList += StringUtil.getStringValue(deviceMapList.get(i), "device_serialnumber", "") + ";";
				}
				checker.setResult(1006);
				checker.setResultDesc(serialList.substring(0, serialList.length() - 1));
				writeLog(checker);
				return checker.getReturnXml();
			}

			checker.setResult(0);
			checker.setResultDesc("成功");
			checker.setCityId(StringUtil.getStringValue(deviceMapList.get(0), "city_id", ""));
			writeLog(checker);
		}catch (Exception e) {
			logger.info("error..", e);
		}
		return checker.getReturnXml();
	}
	
	/**
	 * 记录日志
	 * @param returnXml
	 * @param checker
	 * @param name
	 */
	private void writeLog(QueryCityIdChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, checker.getDevSn(), "QueryCityIdService");
		logger.info(
				"servicename[QueryCityIdService]cmdId[{}]devSn[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), checker.getDevSn(), checker.getReturnXml()});
	}
}
