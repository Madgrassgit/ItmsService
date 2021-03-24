package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dispatch.obj.QueryDeviceInItChecker;

public class QueryDeviceInItService implements IService{

	private static Logger logger = LoggerFactory.getLogger(QueryDeviceInItService.class);

	@Override
	public String work(String inXml) {
		logger.warn("InIt -- QueryDeviceInItService inParam:[{}]",inXml);
		//检查合法性
		QueryDeviceInItChecker checker = new QueryDeviceInItChecker(inXml);
		if(false == checker.check()){
			logger.error("servicename[QueryDeviceInItService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUsername(),
							checker.getReturnXml() });
			logger.warn("QueryDeviceInItService returnParam:[{}]",checker.getReturnXml());
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[QueryDeviceInItService]cmdId[{}]userinfo[{}]参数校验通过",
				new Object[] { checker.getCmdId(), checker.getUserInfo() });
		//获取终端序列号==1
		DeviceInfoDAO deviceDao = new DeviceInfoDAO();
		if(1 == checker.getUserInfoType()){
			Map<String, String> queryDevInfo = deviceDao.queryDevInitInfo(checker.getUserInfo());
			checker.setResultDesc("成功");
			checker.setResult(0);
			if(null == queryDevInfo || queryDevInfo.isEmpty()){
				checker.setISinit("2");
			}else{
				checker.setISinit("1");
			}
		}
		logger.warn("QueryDeviceInItService returnParam:[{}]",checker.getReturnXml());
		return checker.getReturnXml();
	}

}
