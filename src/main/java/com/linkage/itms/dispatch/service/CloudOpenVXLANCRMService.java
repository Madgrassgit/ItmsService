package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.IpsecServParamDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.CloudOpenVXLANCRMChecker;
import com.linkage.itms.dispatch.util.CommonUtil;


public class CloudOpenVXLANCRMService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudOpenVXLANCRMService.class);
	@Override
	public String work(String inXml) {
		CloudOpenVXLANCRMChecker checker = new CloudOpenVXLANCRMChecker(inXml);
		ArrayList<Map<String, String>> retList = new ArrayList<Map<String, String>>();
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudOpenVXLANCRMService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return getReturnXml(checker, retList);
			}
			logger.warn("servicename[CloudOpenVXLANCRMService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
					new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
			
			IpsecServParamDAO ipsDao = new IpsecServParamDAO();
			// 查询用户是否存在
			List<HashMap<String, String>> userList = ipsDao.custIsExists(checker.getLoid());
			if (userList == null || userList.isEmpty()) {
				checker.setResult(6);
				checker.setResultDesc("逻辑SN不存在，请先走建设流程");
				return getReturnXml(checker, retList);
			}
			
			String userId = StringUtil.getStringValue(userList.get(0), "user_id");
			String servSql = "";
			String busSql = "";
			if (ipsDao.servIsExists(Long.parseLong(userId), 45)) {
				servSql = ipsDao.updateServInfo(Long.parseLong(userId), checker);
			}
			else {
				servSql = ipsDao.saveServInfo(Long.parseLong(userId), checker);
			}
			if (ipsDao.busIsExists(Long.parseLong(userId))) {
				busSql = ipsDao.updateBusInfo(Long.parseLong(userId), checker);
			}
			else {
				busSql = ipsDao.saveBusInfo(Long.parseLong(userId), checker);
			}
			ArrayList<String> servSqlList = new ArrayList<String>();
			servSqlList.add(servSql);
			servSqlList.add(busSql);
			if (DBOperation.executeUpdate(servSqlList) < 1) {
				logger.warn("servicename[CloudOpenVXLANCRMService]cmdId[{}]userinfo[{}]保存vxlan业务失败",
						new Object[] {checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(8);
				checker.setResultDesc("保存vxlan业务失败");
				return getReturnXml(checker, retList);
			}
			logger.warn("servicename[CloudOpenVXLANCRMService]cmdId[{}]userinfo[{}]保存vxlan业务成功",
					new Object[] {checker.getCmdId(), checker.getUserInfo()});
			
			ACSCorba corba = new ACSCorba();
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			Map<String, String> map = ipsDao.getServInfo(Long.parseLong(userId), 10);
			String deviceId = StringUtil.getStringValue(map, "device_id");
			String openStatus = StringUtil.getStringValue(map, "open_status");
			// 没有宽带业务只保存业务
			if (map == null || map.size() < 1 || StringUtil.isEmpty(deviceId)) {
				logger.warn("servicename[CloudOpenVXLANCRMService]cmdId[{}]userinfo[{}]没有宽带业务和设备，只保存工单",
						new Object[] {checker.getCmdId(), checker.getUserInfo()});
				// vxlan开通实例总数
				checker.setResult(0);
				checker.setResultDesc("成功");
				return getReturnXml(checker, retList);
			}
			if ("1".equals(openStatus) && getStatus.testDeviceOnLineStatus(deviceId, corba) == 1) {
				logger.warn("servicename[CloudOpenVXLANCRMService]cmdId[{}]userinfo[{}]直接上报",
						new Object[] {checker.getCmdId(), checker.getUserInfo()});
				CommonUtil.sendServMq(checker.getLoid(), checker.getUserInfo());
			}
			else {
				logger.warn("servicename[CloudOpenVXLANCRMService]cmdId[{}]userinfo[{}]重新下发宽带",
						new Object[] {checker.getCmdId(), checker.getUserInfo()});
				
				// 更新业务用户表的开通状态
				ipsDao.updateServOpenStatus(Long.parseLong(userId), 10);
				// 预读调用对象
				PreServInfoOBJ preInfoObj = new PreServInfoOBJ(userId, deviceId, "", "", "10", "1");
				if (1 != CreateObjectFactory.createPreProcess()
						.processServiceInterface(CreateObjectFactory.createPreProcess()
								.GetPPBindUserList(preInfoObj))) {
					logger.warn("servicename[CloudOpenVXLANCRMService]cmdId[{}]userinfo[{}]业务下发，调用配置模块失败",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1000);
					checker.setResultDesc("未知错误，请稍后重试");
					return getReturnXml(checker, retList);
				}
			}
			// vxlan开通实例总数
			checker.setResult(0);
			checker.setResultDesc("成功");
		}
		catch (Exception e) {
			logger.warn("CloudOpenVXLANCRMService is error:", e);
		}
		return getReturnXml(checker, retList);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudOpenVXLANCRMChecker checker, ArrayList<Map<String, String>> retList) {
		new RecordLogDAO().recordDispatchLog(checker, "OpenVXLANService", checker.getCmdId());
		String retXml = checker.getReturnXml(retList);
		logger.warn("servicename[CloudOpenVXLANCRMService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), retXml});
		return retXml;
	}
}
