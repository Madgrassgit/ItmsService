package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dispatch.obj.IsStbBindChecker;

/**
 * 是否支持零配置开通设备版本查询接口
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-5-12
 */
public class IsStbBindService implements IService
{

	/** 日志 */
	private static final Logger logger = LoggerFactory
			.getLogger(IsStbBindService.class);

	@Override
	public String work(String inXml)
	{
		IsStbBindChecker checker = new IsStbBindChecker(inXml);
		// 验证入参格式是否正确
		if (false == checker.check())
		{
			logger.error(
					"servicename[IsStbBindService]cmdId[{}]loid[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getLoid(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn("servicename[IsStbBindService]cmdId[{}]loid[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getLoid(), inXml });
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		
		List<HashMap<String, String>> userList = deviceInfoDAO.queryUserByLoid(checker.getLoid());
		if (null == userList || userList.isEmpty()) {
			logger.warn(
					"servicename[IsStbBindService]cmdId[{}]loid[{}]无此客户信息",
					new Object[] { checker.getCmdId(), checker.getLoid()});
			checker.setResult(1002);
			checker.setResultDesc("无用户信息：" + checker.getLoid());
			checker.setZeroConf("0");
			//记录日志
			logger.warn(
					"servicename[IsStbBindService]cmdId[{}]loid[{}]处理结束，返回响应信息:{}",
					new Object[] { checker.getCmdId(), checker.getLoid()});
			
		}else{
			String deviceId = userList.get(0).get("device_id");
			if (null == deviceId || "".equals(deviceId)){
				logger.warn(
						"servicename[IsStbBindService]cmdId[{}]loid[{}]无此客户信息",
						new Object[] { checker.getCmdId(), checker.getLoid()});
				checker.setResult(1002);
				checker.setResultDesc("用户未绑定设备：" + checker.getLoid());
				checker.setZeroConf("0");
			}else{
				Map<String,String> deviceMap = deviceInfoDAO.queryZeroConfByDeviceId(deviceId);
				
				if(null == deviceMap){
					logger.warn(
							"servicename[IsStbBindService]cmdId[{}]loid[{}]没有查到设备",
							new Object[] { checker.getCmdId(), checker.getLoid()});
					checker.setResult(1000);
					checker.setResultDesc("没有查到设备：" + checker.getLoid());
					checker.setZeroConf("0");
					logger.warn("servicename[IsStbBindService]cmdId[{}]loid[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getLoid()});
				}else{
					checker.setResult(0);
					checker.setResultDesc("成功");
					if ("1".equals(deviceMap.get("zeroconf"))){
						checker.setZeroConf("1");
					}else {
						checker.setZeroConf("0");
					}
				}
			}
		}
		String returnXml = checker.getReturnXml();
		logger.warn("servicename[IsStbBindService]cmdId[{}]devSn[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getDevSn(), returnXml });
		return returnXml;
	}
}
