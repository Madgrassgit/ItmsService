package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.SerialNoChecker;


public class SerialNoService implements IService {

	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(SerialNoService.class);

	@Override
	public String work(String inXml) {
		SerialNoChecker checker = new SerialNoChecker(inXml);
		try {
			logger.info("begin..");
			// 验证入参格式是否正确
			if (false == checker.check()) {
				logger.info(
						"servicename[SerialNoService]cmdId[{}]Loid[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getLoid(), checker.getReturnXml()});
				return checker.getReturnXml();
			}
			logger.info("servicename[SerialNoService]cmdId[{}]Loid[{}]参数校验通过，入参为：{}",
					new Object[] {checker.getCmdId(), checker.getLoid(), inXml});
			UserDeviceDAO userDevDao = new UserDeviceDAO();
			ArrayList<HashMap<String, String>> userMapList = userDevDao.queryDeviceIdByLoid(checker.getLoid());
			// 没有查询数据
			if (null == userMapList || userMapList.isEmpty()) {
				logger.info("servicename[SerialNoService]cmdId[{}]Loid[{}]查无此用户1",
						new Object[] {checker.getCmdId(), checker.getLoid()});
				checker.setResult(1002);
				checker.setResultDesc("查无此用户");
				checker.setDevSn("");
				checker.setSerialNo("");
				writeLog(checker);
				return checker.getReturnXml();
			}
			// 查询多条记录
			else if (userMapList.size() > 1) {
				logger.info("servicename[SerialNoService]cmdId[{}]Loid[{}]账号对应多个用户",
						new Object[] {checker.getCmdId(), checker.getLoid()});
				String loidList = "";
				for (int i = 0; i < userMapList.size(); i++) {
					loidList += StringUtil.getStringValue(userMapList.get(i), "username", "") + ",";
				}
				checker.setResult(4);
				checker.setResultDesc(loidList.substring(0, loidList.length() - 1));
				checker.setDevSn("");
				checker.setSerialNo("");
				writeLog(checker);
				return checker.getReturnXml();
			}
			// 根据loid没有查询到对应用户id
			if (StringUtil.IsEmpty(userMapList.get(0).get("user_id"))) {
				logger.info("servicename[SerialNoService]cmdId[{}]Loid[{}]查无此用户2",
						new Object[] {checker.getCmdId(), checker.getLoid()});
				checker.setResult(1002);
				checker.setResultDesc("查无此用户");
				checker.setDevSn("");
				checker.setSerialNo("");
				writeLog(checker);
				return checker.getReturnXml();
			}
			// 根据loid没有查询到对应设备id
			String deviceId = userMapList.get(0).get("device_id");
			if (StringUtil.IsEmpty(deviceId)) {
				logger.info("servicename[SerialNoService]cmdId[{}]Loid[{}]未绑定设备",
						new Object[] {checker.getCmdId(), checker.getLoid()});
				checker.setResult(1004);
				checker.setResultDesc("此用户未绑定设备");
				checker.setDevSn("");
				checker.setSerialNo("");
				writeLog(checker);
				return checker.getReturnXml();
			}
			ArrayList<HashMap<String, String>> deviceList = userDevDao.querySerialNoByDeviceId(deviceId);
			if (null == deviceList || deviceList.isEmpty()) {
				logger.info("servicename[SerialNoService]cmdId[{}]Loid[{}]未绑定设备",
						new Object[] {checker.getCmdId(), checker.getLoid()});
				checker.setResult(1004);
				checker.setResultDesc("此用户未绑定设备");
				checker.setDevSn("");
				checker.setSerialNo("");
				writeLog(checker);
				return checker.getReturnXml();
			}
			checker.setResult(0);
			checker.setResultDesc("成功");
			checker.setDevSn(deviceList.get(0).get("device_serialnumber"));
			checker.setSerialNo(deviceList.get(0).get("serial_no"));
			writeLog(checker);
		}catch (Exception e) {
			logger.info("begin..", e);
		}
		return checker.getReturnXml();
	}
	
	/**
	 * 记录日志
	 * @param returnXml
	 * @param checker
	 * @param name
	 */
	private void writeLog(SerialNoChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, checker.getLoid(), "SerialNoService");
		logger.info(
				"servicename[SerialNoService]cmdId[{}]getLoid[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), checker.getLoid(), checker.getReturnXml()});
	}
}
