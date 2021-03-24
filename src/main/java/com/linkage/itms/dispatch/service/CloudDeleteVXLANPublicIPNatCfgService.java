package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.IpsecServParamDAO;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.CloudDeleteVXLANPublicIPNatCfgChecker;


public class CloudDeleteVXLANPublicIPNatCfgService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudDeleteVXLANPublicIPNatCfgService.class);
	private ACSCorba corba = new ACSCorba();
	private String natRootPath = "InternetGatewayDevice.X_CT-COM_NAT.";
	IpsecServParamDAO ipsDao = new IpsecServParamDAO();
	private CloudDeleteVXLANPublicIPNatCfgChecker checker = null;
	
	@Override
	public String work(String inXml) {
		checker = new CloudDeleteVXLANPublicIPNatCfgChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudDeleteVXLANPublicIPNatCfgService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return getReturnXml(checker);
			}
			logger.warn("servicename[CloudDeleteVXLANPublicIPNatCfgService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
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
				return getReturnXml(checker);
			}
			String deviceId = StringUtil.getStringValue(userMap.get(0), "device_id");
			if (StringUtil.isEmpty(deviceId)) {
				checker.setResult(7);
				checker.setResultDesc("查询不到对应网关");
				return getReturnXml(checker);
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
			
			Long userId = StringUtil.getLongValue(StringUtil.getStringValue(userMap.get(0), "user_id"));
			String pubIpv4 = checker.getPubIpv4();
			
			// 校验设备是否在线可操作
			if (!deviceisBusy(deviceId)) {
				return getReturnXml(checker); 
			}

			//　采集nat下节点
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			List<String> jList = corba.getIList(deviceId, natRootPath);
			// 没有节点直接返回
			if (null == jList || jList.size() == 0 || jList.isEmpty()) {
				// 删除数据
				ipsDao.deleteNatData(userId, pubIpv4);
				checker.setResult(0);
				checker.setResultDesc("成功");
				return getReturnXml(checker);
			}
			
			// 删除所有节点
			if ("all".equals(pubIpv4)) {
				for(String j : jList) {
					logger.warn("[CloudDeleteVXLANPublicIPNatCfgService][{}]删除节点[{}]", deviceId, natRootPath + j);
					int result = corba.del(deviceId, natRootPath + j + ".");
					if (result != 0 && result != 1) {
						logger.warn("[CloudDeleteVXLANPublicIPNatCfgService][{}]删除所有节点失败", deviceId);
						checker.setResult(0);
						checker.setResultDesc("删除节点失败");
						return getReturnXml(checker);
					}
				}
				ipsDao.deleteNatData(userId, pubIpv4);
				checker.setResult(0);
				checker.setResultDesc("成功");
				return getReturnXml(checker); 
			}
			
			// 设备上存在nat实例
			if (!existPublicIp(deviceId, userId)) {
				ipsDao.deleteNatData(userId, pubIpv4);
				return getReturnXml(checker);
			}

			ipsDao.deleteNatData(userId, pubIpv4);
			checker.setResult(0);
			checker.setResultDesc("成功");
		}
		catch (Exception e) {
			logger.warn("CloudDeleteVXLANPublicIPNatCfgService is error:", e);
		}
		return getReturnXml(checker);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudDeleteVXLANPublicIPNatCfgChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudDeleteVXLANPublicIPNatCfgService", checker.getCmdId());
		String retXml = checker.getReturnXml();
		logger.warn("servicename[CloudDeleteVXLANPublicIPNatCfgService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), retXml});
		return retXml;
	}
	
	/**
	 * 设备是否在线
	 * @param deviceId
	 * @param corba
	 * @param checker
	 * @return
	 */
	private boolean deviceisBusy(String deviceId) {
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		logger.warn("设备[{}],在线状态[{}] ",new Object[]{deviceId, flag});
		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1003);
			checker.setResultDesc("网关正在被操作");
			return false;
		}
		// 设备不在线，不能获取节点值
		if (1 != flag) {
			logger.warn("设备不在线，无法获取节点值");
			checker.setResult(8);
			checker.setResultDesc("网关不在线");
			return false;
		}
		return true;
	}
	
	/**
	 * 设备上存在nat实例
	 * @param natPathsList
	 * @param deviceId
	 * @return
	 */
	private boolean existPublicIp(String deviceId, Long userId) {
		String _pubIpv4 = checker.getPubIpv4();
		
		ArrayList<String> natPathsList = corba.getParamNamesPath(deviceId, natRootPath, 0);
		logger.warn("natPathsList :[{}]", natPathsList);
		
		// 采集nat节点中公网ip
		ArrayList<String> paramNameList = new ArrayList<String>();
		for (String natPath : natPathsList) {
			if (natPath.contains(".ExternalAddress")) {
				paramNameList.add(natPath);
			}
		}
	
		String[] paramNametemp = paramNameList.toArray(new String[paramNameList.size()]);
		Map<String, String> paramValueMap = corba.getParaValueMap(deviceId, paramNametemp);
		if (paramValueMap.isEmpty()) {
			logger.warn("[CloudDeleteVXLANPublicIPNatCfgService] [{}]获取节点失败", deviceId);
			checker.setResult(1004);
			checker.setResultDesc("设备采集失败");
			return false;
		}
		logger.warn("[CloudDeleteVXLANPublicIPNatCfgService] [{}]获取节点[{}]", deviceId, paramValueMap);
		String natNode = "";
		// 获取到设备已经存在的公网ip
		for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
			if (_pubIpv4.equals(entry.getValue())) {
				natNode = entry.getKey().replace("ExternalAddress", "");
				break;
			}
		}
		// 没有相同节点
		if (StringUtil.isEmpty(natNode)) {
			checker.setResult(0);
			checker.setResultDesc("成功");
			return false;
		}
		int result = corba.del(deviceId, natNode);
		if (result != 0 && result != 1) {
			logger.warn("[CloudDeleteVXLANPublicIPNatCfgService][{}]删除节点[{}]失败", deviceId, natNode);
			checker.setResult(0);
			checker.setResultDesc("删除节点失败");
			return false;
		}
		return true;
	}
}
