package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryDevRegInfoChecker;

public class QueryDevRegInfoService {
	private static Logger logger = LoggerFactory.getLogger(QueryDevRegInfoService.class);

	public String work(String inXml) {
		QueryDevRegInfoChecker checker = new QueryDevRegInfoChecker(inXml);
		if (!checker.check())  {
			logger.warn("serviceName[QueryDevRegInfoService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
			new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml() });
			return checker.getReturnXml();
		}
		String loid = checker.getUserInfo();
		logger.warn("servicename[QueryDevRegInfoService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), loid, inXml });
		//获取用户信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker.getUserInfoType(), loid);
		
		// 用户信息不存在
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn("servicename[QueryDevRegInfoService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), loid});
			checker.setResult(1002);
			checker.setResultDesc("无此客户信息");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		QueryDevDAO queryDevDAO = new QueryDevDAO();
		List<HashMap<String, String>> list = queryDevDAO.queryDevRegInfo(loid);
		if (null == list || list.isEmpty()) {
			logger.warn("servicename[QueryDevRegInfoService]cmdId[{}]userinfo[{}]该用户没有设备解绑记录",
					new Object[] { checker.getCmdId(), loid});
			checker.setResult(1004);
			checker.setResultDesc("此用户没有设备解绑记录");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		HashMap<String, String> map = list.get(0);
		checker.setDevModel(StringUtil.getStringValue(map, "device_model", ""));
		checker.setDevSn(StringUtil.getStringValue(map, "device_serialnumber", ""));
		String unbindDate = StringUtil.getStringValue(map, "binddate", "");
		if (!StringUtil.IsEmpty(unbindDate)) {
			unbindDate =  new DateTimeUtil(Long.parseLong(unbindDate) * 1000).getYYYY_MM_DD_HH_mm_ss();
		}
		else {
			unbindDate = "";
		}
		checker.setUnbindDate(unbindDate);
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, loid, "QueryDevRegInfo");
		logger.warn( "servicename[QueryDevRegInfoService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), loid, returnXml});
		 return returnXml;
	}
}
