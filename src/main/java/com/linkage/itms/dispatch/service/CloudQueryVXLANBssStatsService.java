package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dao.IpsecServParamDAO;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.CloudQueryVXLANBssStatsChecker;


public class CloudQueryVXLANBssStatsService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudQueryVXLANBssStatsService.class);

	@Override
	public String work(String inXml) {
		CloudQueryVXLANBssStatsChecker checker = new CloudQueryVXLANBssStatsChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudQueryVXLANBssStatsService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return checker.getReturnXml();
			}
			logger.warn("servicename[CloudQueryVXLANBssStatsService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
					new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
			
			QueryDevDAO qdDao = new QueryDevDAO();
			List<HashMap<String, String>> userMap = null;
			if (checker.getUserInfoType() == 1) {
				userMap = qdDao.queryUserByNetAccountCloud(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 2) {
				userMap = qdDao.queryUserByLoidCloud(checker.getUserInfo());
			}
			
			if (userMap == null || userMap.isEmpty()) {
				checker.setResult(6);
				checker.setResultDesc("查询不到对应用户");
				return checker.getReturnXml();
			}
			
			String deviceId =  StringUtil.getStringValue(userMap.get(0), "device_id");
			String userId =  StringUtil.getStringValue(userMap.get(0), "user_id");
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
					i ++;
					continue;
				}
				loidPrev.append(StringUtil.getStringValue(m, "username"));
				loidPrev.append(";");
			}
			// LoidPrev 先设置为空
//			checker.setLoidPrev("");
			checker.setLoidPrev(loidPrev.toString());
			
			// 查询业务Ipsec vpn业务下发结果
			// RstCode为0时必须 1：业务开通成功 0：业务未执行 -1：业务开通失
			IpsecServParamDAO ispDao = new IpsecServParamDAO();
			if (ispDao.queryVxlanServCount(StringUtil.getLongValue(userId)) == 0) {
				logger.warn(
						"servicename[CloudQueryVXLANBssStatsService]cmdId[{}]userinfo[{}]没有对应vxlan业务记录",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1009);
				checker.setResultDesc("没有对应vxlan业务记录");
				return getReturnXml(checker);
			}
			checker.setBssStats(ispDao.queryVxlanServStatus(StringUtil.getLongValue(userId)));
			checker.setResult(0);
			checker.setResultDesc("成功");
		}
		catch (Exception e) {
			logger.warn("CloudQueryVXLANBssStatsService is error:", e);
		}
		return getReturnXml(checker);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudQueryVXLANBssStatsChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudQueryVXLANBssStatsService", checker.getCmdId());
		logger.warn("servicename[CloudQueryVXLANBssStatsService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), checker.getReturnXml()});
		return checker.getReturnXml();
	}
}
