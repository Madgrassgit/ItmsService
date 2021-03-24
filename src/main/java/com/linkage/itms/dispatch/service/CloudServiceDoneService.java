package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dao.IpsecServParamDAO;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dispatch.obj.CloudServiceDoneChecker;


public class CloudServiceDoneService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudServiceDoneService.class);

	@Override
	public String work(String inXml) {
		CloudServiceDoneChecker checker = new CloudServiceDoneChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudServiceDoneService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return checker.getReturnXml();
			}
			logger.warn("servicename[CloudServiceDoneService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
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
//			String oui =  StringUtil.getStringValue(userMap.get(0), "oui");
//			String deviceSn =  StringUtil.getStringValue(userMap.get(0), "device_serialnumber");
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
			
			Long userId = com.linkage.commons.util.StringUtil.getLongValue(userMap.get(0), "user_id");
			ServUserDAO servUserDao = new ServUserDAO();

			// 获取用户的业务信息
			ArrayList<HashMap<String, String>> servUserMapList = servUserDao.queryHgwcustServUserByDevId(userId);
			// 是否受理了该业务
			if (null == servUserMapList || servUserMapList.isEmpty()) {
				logger.warn(
						"servicename[CloudServiceDoneService]cmdId[{}]userinfo[{}]用户为受理任何业务",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1009);
				checker.setResultDesc("用户未受理任何业务");
				// 未执行下发
				checker.setServiceDoneStats("0");
				return getReturnXml(checker);
			}
			
			IpsecServParamDAO ispDao = new IpsecServParamDAO();
			if (ispDao.queryIpsecServCount(userId) == 0) {
				logger.warn(
						"servicename[CloudServiceDoneService]cmdId[{}]userinfo[{}]没有对应Ipsec业务记录",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1009);
				checker.setResultDesc("没有对应Ipsec业务记录");
				// 未执行下发
				checker.setServiceDoneStats("0");
				return getReturnXml(checker);
			}
			// 更新业务用户表的开通状态
			ispDao.updateServStatus(userId);
			// 预读调用对象
			/*PreServInfoOBJ preInfoObj = new PreServInfoOBJ(
					StringUtil.getStringValue(userId), deviceId, oui, deviceSn, "27", "1");*/
//			String userinfo="";
			if(checker.getUserInfoType()==1){
//				userinfo=checker.getUserInfo();
			}
			if (true != CreateObjectFactory.createPreProcess()
					.processDeviceStrategy(new String[]{deviceId},"2701",new String[]{"27", userId + "", checker.getUserInfo()})) {
				checker.setResult(1000);
				checker.setResultDesc("未知错误，请稍后重试");
				logger.warn(
						"servicename[CloudServiceDoneService]cmdId[{}]userinfo[{}]设备[{}]业务下发失败",
						new Object[] { checker.getCmdId(), checker.getUserInfo(), deviceId });
			}
			else {
				logger.warn(
						"servicename[CloudServiceDoneService]cmdId[{}]userinfo[{}]设备[{}]业务下发成功",
						new Object[] { checker.getCmdId(), checker.getUserInfo(), deviceId });
				checker.setServiceDoneStats("1");
			}
			checker.setResult(0);
			checker.setResultDesc("成功");
		}
		catch (Exception e) {
			logger.warn("CloudServiceDoneService is error:", e);
		}
		return getReturnXml(checker);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudServiceDoneChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudServiceDoneService", checker.getCmdId());
		logger.warn("servicename[CloudServiceDoneService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), checker.getReturnXml()});
		return checker.getReturnXml();
	}
}
