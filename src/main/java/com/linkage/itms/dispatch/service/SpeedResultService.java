package com.linkage.itms.dispatch.service;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.SpeedResultChecker;

public class SpeedResultService implements IService {

	private static Logger logger = LoggerFactory
			.getLogger(SpeedResultService.class);

	@Override
	public String work(String inXml) {
		logger.warn("SpeedResultService==>inParam({})", inXml);
		SpeedResultChecker checker = new SpeedResultChecker(inXml);

		// 验证入参
		if (false == checker.check()) {
			logger.warn(
					"SpeedResultService:入参验证没通过,UserInfoType=[{}],UserName=[{}]",
					new Object[] { checker.getUserInfoType(),
							checker.getUserInfo() });

			logger.warn("SpeedResultService==>returnParam="
					+ checker.getReturnXml());
			return checker.getReturnXml();
		}
		UserDeviceDAO userDevDao = new UserDeviceDAO();

		// 查询测速表里宽带账号对应了几个loid
		if (StringUtil.IsEmpty(checker.getUserLoid())) {
			List<HashMap<String, String>> loidList = userDevDao.queryLoid(checker.getUserInfo());
			if(null == loidList || loidList.isEmpty()){
				logger.warn(
						"servicename[SpeedResultService]cmdId[{}]userinfo[{}]无此用户",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(5);
				checker.setResultDesc("该用户未做过仿真测速");
				checker.setFailureReason("2");
				return checker.getReturnXml();
			} else if(loidList.size() > 1){
				logger.warn(
						"servicename[SpeedResultService]cmdId[{}]userinfo[{}]查询到多个Loid",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				String loid = "";
				for (int i = 0; i < loidList.size(); i++)
				{
					loid += StringUtil.getStringValue(loidList.get(i), "loid", "")
							+ ",";
				}
				checker.setResult(4);
				checker.setResultDesc("宽带账号查询到多个LOID");
				checker.setFailureReason(loid.substring(0, loid.length() - 1));
				return checker.getReturnXml();
			}
		}
		Map<String, String> userMap = userDevDao
				.queryHttpResult(checker.getUserInfo(), checker.getUserLoid());
		if (null == userMap || userMap.isEmpty()) {
			logger.warn(
					"servicename[SpeedResultService]cmdId[{}]userinfo[{}]无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(5);
			checker.setResultDesc("该用户未做过仿真测速");
			checker.setFailureReason("2");
			return checker.getReturnXml();
		}
		// 正常
		String status = StringUtil.getStringValue(userMap,
				"status", "");
		String devSN = StringUtil.getStringValue(userMap,
				"device_serialnumber", "");
		if(!"0".equals(status) && !"1".equals(status)){
			checker.setResult(1005);
			checker.setResultDesc("最近一次仿真测速失败");
			checker.setDevSn(devSN);
			checker.setFailureReason("4");
			return checker.getReturnXml();
		}
		
		String maxSpeed = StringUtil.getStringValue(userMap, "maxspeed", "");
		String avgSpeed = StringUtil.getStringValue(userMap, "avgspeed", "");

		checker.setResult(0);
		checker.setResultDesc("成功");
		checker.setDevSn(devSN);
		checker.setFailureReason("");
		checker.setAverageDownloadSpeed(getValue(avgSpeed));
		checker.setMaxDownloadSpeed(getValue(maxSpeed));
		logger.warn(
				"servicename[SpeedResultService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						checker.getReturnXml() });
		return checker.getReturnXml();
	}

	/*
	 * kb/s与Mbps转换
	 */
	private String getValue(String sampledValues) {
		if (StringUtil.IsEmpty(sampledValues)) {
			return "0(Mbps)";
		}
		// 保留小数点后两位
		DecimalFormat df = new DecimalFormat("######0.00");
		double result = Double.parseDouble(sampledValues) / 128;
		return StringUtil.getStringValue(df.format(result)) + "(Mbps)";
	}
}
