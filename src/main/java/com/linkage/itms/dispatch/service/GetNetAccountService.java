package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dispatch.obj.GetNetAccountChecker;

public class GetNetAccountService implements IService {
	
	private static Logger logger = LoggerFactory.getLogger(GetNetAccountService.class);

	@Override
	public String work(String inXml) {
		logger.warn("GetNetAccountService==>inXml({})",inXml);
		
		GetNetAccountChecker checker = new GetNetAccountChecker(inXml);
		if (false == checker.check()) {
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}

		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();

		String netAccount = "";
		List<HashMap<String, String>> netAccountMapList = null;
		netAccountMapList = deviceInfoDAO.getNetAccountByDevSn(checker.getDevSn());

		if (netAccountMapList == null || netAccountMapList.isEmpty()) {
			logger.warn("没有查询结果");
			checker.setResult(1000);
			checker.setResultDesc("没有查询结果");
			return checker.getReturnXml();
		}

		if (netAccountMapList.size() > 1) {
			logger.warn("查到多条记录,请输入更多位设备序列号进行查询");
			checker.setResult(1000);
			checker.setResultDesc("查到多条记录,请输入更多位设备序列号进行查询");
			return checker.getReturnXml();
		} else {
			netAccount = StringUtil.getStringValue(netAccountMapList.get(0), "netaccount", "");
			if (StringUtil.IsEmpty(netAccount)) {
				logger.warn("宽带账号为空");
				checker.setResult(1000);
				checker.setResultDesc("宽带账号为空");
				return checker.getReturnXml();
			}
		}
		
		checker.setNetAccount(netAccount);
		return checker.getNetAccountReturnXml();
	}
}
