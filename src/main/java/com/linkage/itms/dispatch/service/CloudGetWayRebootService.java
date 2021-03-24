package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.cao.DevReboot;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.CloudGetWayRebootChecker;

public class CloudGetWayRebootService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory
			.getLogger(CloudGetWayRebootService.class);

	@Override
	public String work(String inXml) {
		CloudGetWayRebootChecker checker = new CloudGetWayRebootChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn(
						"servicename[CloudGetWayRebootService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
				return checker.getReturnXml();
			}
			logger.warn(
					"servicename[CloudGetWayRebootService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });

			QueryDevDAO qdDao = new QueryDevDAO();
			List<HashMap<String, String>> userMap = null;
			if (checker.getUserInfoType() == 1) {
				userMap = qdDao.queryUserByNetAccountCloud(checker.getUserInfo());
			} else if (checker.getUserInfoType() == 2) {
				userMap = qdDao.queryUserByLoidCloud(checker.getUserInfo());
			}

			if (userMap == null || userMap.isEmpty()) {
				checker.setResult(6);
				checker.setResultDesc("查询不到对应用户");
				return checker.getReturnXml();
			}
			// 设备id
			String deviceId = StringUtil.getStringValue(userMap.get(0), "device_id");
			if (StringUtil.isEmpty(deviceId)) {
				checker.setResult(7);
				checker.setResultDesc("查询不到对应网关");
				return checker.getReturnXml();
			}

			// Loid
			checker.setLoid(StringUtil.getStringValue(userMap.get(0), "username"));
			StringBuffer loidPrev = new StringBuffer();
			int i = 0;
			for (HashMap<String, String> m : userMap) {
				if (i == 0) {
					i++;
					continue;
				}
				loidPrev.append(StringUtil.getStringValue(m, "username"));
				loidPrev.append(";");
			}
			// LoidPrev 先设置为空
//			checker.setLoidPrev("");
			checker.setLoidPrev(loidPrev.toString());

			// 重启
			logger.warn("servicename[CloudGetWayRebootService]cmdId[{}]userinfo[{}]调ACS重启设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			int irt = DevReboot.reboot(deviceId);
			logger.warn("servicename[CloudGetWayRebootService]cmdId[{}]userinfo[{}]调ACS重启设备返回码：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), irt });
			if (1 == irt) {
				logger.warn("servicename[CloudGetWayRebootService]cmdId[{}]userinfo[{}]重启成功",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setRebootStats("1");
			} else {
				logger.warn("servicename[CloudGetWayRebootService]cmdId[{}]userinfo[{}]设备重启失败",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setRebootStats("-1");
			}
			checker.setResult(0);
			checker.setResultDesc("成功");
		} catch (Exception e) {
			logger.warn("CloudGetWayRebootService is error:", e);
		}
		return getReturnXml(checker);
	}

	/**
	 * 记录日志返回xml
	 * 
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudGetWayRebootChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudGetWayRebootService", checker.getCmdId());
		logger.warn("servicename[CloudGetWayRebootService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getReturnXml() });
		return checker.getReturnXml();
	}
}
