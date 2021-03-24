package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.IpsecServParamDAO;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.CloudStaticRtCfgChecker;
import com.linkage.itms.dispatch.util.VxlanOperateDeviceUtil;


public class CloudStaticRtCfgService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudStaticRtCfgService.class);
	private String forwardingRootPath = "InternetGatewayDevice.Layer3Forwarding.Forwarding.";
	private ACSCorba corba = new ACSCorba();
	private CloudStaticRtCfgChecker checker = null;
	IpsecServParamDAO ipsDao = new IpsecServParamDAO();

	@Override
	public String work(String inXml) {
		checker = new CloudStaticRtCfgChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudStaticRtCfgService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return getReturnXml(checker);
			}
			logger.warn("servicename[CloudStaticRtCfgService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
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
			
			String deviceId =  StringUtil.getStringValue(userMap.get(0), "device_id");
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

			// 校验设备是否在线可操作
			if (!deviceisBusy(deviceId)) {
				return getReturnXml(checker); 
			}
			
			// 先将数据全部插入或者更新入数据库
			insertData(userId);

			//　采集nat下节点
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			ArrayList<String> forwardingPathsList = corba.getParamNamesPath(deviceId, forwardingRootPath, 0);
			logger.warn("forwardingPathsList :[{}]", forwardingPathsList);
			
			String[] ipMask = VxlanOperateDeviceUtil.getIpMask(checker.getDesIp());
			
			// 设备上不存在路由实例，做新增业务
			if (forwardingPathsList == null || (forwardingPathsList.size() == 1 && forwardingPathsList.get(0).equals(forwardingRootPath))) {
				if (!VxlanOperateDeviceUtil.addForwarding(deviceId, forwardingRootPath, ipMask, checker)) {
					ipsDao.updateFailForwardingData(userId, checker.getNextHop(), checker.getDesIp());
					return getReturnXml(checker);
				}
				checker.setResult(0);
				checker.setResultDesc("成功");
				return getReturnXml(checker);
			}
			
			// 设备上存在路由实例
			if (!existForwarding(forwardingPathsList, deviceId, userId, ipMask)) {
				ipsDao.updateFailForwardingData(userId, checker.getNextHop(), checker.getDesIp());
				return getReturnXml(checker);
			}
			
			// vxlan开通实例总数
			checker.setResult(0);
			checker.setResultDesc("成功");
		}
		catch (Exception e) {
			logger.warn("CloudStaticRtCfgService is error:", e);
		}
		return getReturnXml(checker);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudStaticRtCfgChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudStaticRtCfgService", checker.getCmdId());
		String retXml = checker.getReturnXml();
		logger.warn("servicename[CloudStaticRtCfgService]cmdId[{}]处理结束，返回响应信息:{}",
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
	 * 将数据全部入库
	 * @param userId
	 */
	private void insertData(Long userId) {
		Map<String, String> map = ipsDao.getForwardingExistPubIP(userId, checker.getNextHop(), checker.getDesIp());
		
		StringBuffer sbsql = new StringBuffer();
		PrepareSQL psql;
		if (null == map || map.isEmpty()) {
			sbsql.append(" insert into tab_vxlan_forwarding_config (user_id, rt_id, next_hop,");
			sbsql.append(" des_ip, priority, state) values (?, ?, ?, ?, ?, 1)");
			psql = new PrepareSQL(sbsql.toString());
			psql.setLong(1, userId);
			psql.setString(2, checker.getRtId());
			psql.setString(3, checker.getNextHop());
			psql.setString(4, checker.getDesIp());
			psql.setString(5, StringUtil.getStringValue(checker.getPriority()));
		}
		else {
			sbsql.append(" update tab_vxlan_forwarding_config set rt_id = ?, priority = ?, state = 1");
			sbsql.append(" where user_id = ? and next_hop = ? and des_ip = ?");
			psql = new PrepareSQL(sbsql.toString());
			psql.setString(1, checker.getRtId());
			psql.setString(2, StringUtil.getStringValue(checker.getPriority()));
			psql.setLong(3, userId);
			psql.setString(4, checker.getNextHop());
			psql.setString(5, checker.getDesIp());
		}
		DBOperation.executeUpdate(psql.getSQL());
	}

	/**
	 * 设备上存在路由实例
	 * @param natPathsList
	 * @param deviceId
	 * @return
	 */
	private boolean existForwarding(ArrayList<String> forwardingPathsList, String deviceId, Long userId, String[] ipMask) {
		// 采集nat节点中公网ip
		ArrayList<String> paramNameList = new ArrayList<String>();
		for (String forwardingPath : forwardingPathsList) {
			if (forwardingPath.contains(".Nexthop") ||
					forwardingPath.contains(".DestIPAddress") ||
					forwardingPath.contains(".DestSubnetMask")) {
				paramNameList.add(forwardingPath);
			}
		}
		
		String[] paramNametemp = paramNameList.toArray(new String[paramNameList.size()]);
		Map<String, String> paramValueMap = corba.getParaValueMap(deviceId, paramNametemp);
		if (paramValueMap == null || paramValueMap.isEmpty()) {
			logger.warn("[CloudStaticRtCfgService] [{}]获取ServiceList失败", deviceId);
			checker.setResult(1004);
			checker.setResultDesc("设备采集失败");
			return false;
		}
		
		boolean isSame = false;
		String nexthop = checker.getNextHop();
		String indexKey = "";
		// 获取到设备已经存在的公网ip
		for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
			if (entry.getValue().equals(nexthop)) {
				indexKey = entry.getKey().replace(".Nexthop", "");
				if (ipMask[0].equals(paramValueMap.get(indexKey + ".DestIPAddress"))
						&& ipMask[1].equals(paramValueMap.get(indexKey + ".DestSubnetMask"))) {
					isSame = true;
					break;
				}
			}
		}
		
		// 设备上已经存在相同的节点做set操作
		if (isSame) {
			return VxlanOperateDeviceUtil.setForwarding(deviceId, indexKey, ipMask[0], ipMask[1], checker);
		}
		
		// 设备上不存在相同的节点做add操作
		return VxlanOperateDeviceUtil.addForwarding(deviceId, forwardingRootPath, ipMask, checker);
	}

	public static void main(String[] args) {
		String ip1 = "125.11.11.266/25";
		String ip2 = "0.11.11.22/24";
		String ip3 = "125.11.11.22/0";
//		String ip4 = "125.11.11.22/32";
		String ip4 = "125.11.11.22";
		System.out.println(VxlanOperateDeviceUtil.isIPMask(ip1));
		System.out.println(VxlanOperateDeviceUtil.isIPMask(ip2));
		System.out.println(VxlanOperateDeviceUtil.isIPMask(ip3));
		System.out.println(VxlanOperateDeviceUtil.isIPMask(ip4));
	}
}
