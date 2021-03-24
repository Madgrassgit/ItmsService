package com.linkage.itms.hlj.dispatch.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.util.PingUtil;
import com.linkage.itms.hlj.dispatch.obj.PingConnectivityChecker;



public class PingConnectivityService implements HljIService {

	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(PingConnectivityService.class);

	@Override
	public String work(String inXml) {
		PingConnectivityChecker checker = new PingConnectivityChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (false == checker.check()) {
				logger.info("servicename[PingConnectivityService]time[{}]ip[{}]验证未通过，返回：{}",
						new Object[] {checker.getTime(), checker.getIp(), inXml});
				return checker.getReturnXml();
			}
			logger.info("servicename[PingConnectivityService]time[{}]ip[{}]参数校验通过，入参为：{}",
					new Object[] {checker.getTime(), checker.getIp(), inXml});
			
			HashMap<String, String> parMap = new HashMap<String, String>();
			parMap.put("size", checker.getPackageByte());
			parMap.put("num", checker.getPackageNum());
			parMap.put("timeOut", checker.getTimeOut());
			parMap.put("ip", checker.getIp());
			
			HashMap<String, String> map = PingUtil.getPingResult(parMap);
			// 没有查询数据
			if ("0".equals(map.get("result"))) {
				logger.info("servicename[PingConnectivityService]time[{}]ip[{}]执行ping错误，入参为：{}",
						new Object[] {checker.getTime(), checker.getIp(), inXml});
				checker.setResult(0);
				checker.setResultDesc(map.get("resultDesc"));
				writeLog(checker);
				return checker.getReturnXml();
			}
			
			checker.setSuccesNum(getStringValue(map, "succesNum"));
			checker.setFailNum(getStringValue(map, "failNum"));
			checker.setPacketLossRate(getStringValue(map, "packetLossRate"));
			checker.setMinResponseTime(getStringValue(map, "minResponseTime"));
			checker.setAvgResponseTime(getStringValue(map, "avgResponseTime"));
			checker.setMaxResponseTime(getStringValue(map, "maxResponseTime"));
			checker.setTime(getStringValue(map, "time"));

			checker.setResult(1);
			checker.setResultDesc("成功");
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
	private void writeLog(PingConnectivityChecker checker) {
		logger.info("servicename[PingConnectivityService]time[{}]ip[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getTime(), checker.getIp(), checker.getReturnXml()});
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
