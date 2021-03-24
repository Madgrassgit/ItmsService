package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.IpsecServParamDAO;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.CloudVXLANPublicIPNatCfgChecker;
import com.linkage.itms.dispatch.util.VxlanOperateDeviceUtil;


public class CloudVXLANPublicIPNatCfgService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudVXLANPublicIPNatCfgService.class);
	private ACSCorba corba = new ACSCorba();
	private CloudVXLANPublicIPNatCfgChecker checker = null;
	private String natRootPath = "InternetGatewayDevice.X_CT-COM_NAT.";
	private String interfaceStr = "";
	IpsecServParamDAO ipsDao = new IpsecServParamDAO();
	List<String> failList = new ArrayList<String>();
	
	@Override
	public String work(String inXml) {
		checker = new CloudVXLANPublicIPNatCfgChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudVXLANPublicIPNatCfgService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return getReturnXml(checker);
			}
			logger.warn("servicename[CloudVXLANPublicIPNatCfgService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
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
			
			// 校验设备是否在线可操作
			if (!deviceisBusy(deviceId)) {
				return getReturnXml(checker); 
			}

			// 先查询vxlan节点，没有vxlan节点不用配置nat
			interfaceStr = VxlanOperateDeviceUtil.getNatInterface(deviceId);
			if (StringUtil.isEmpty(interfaceStr)) {
				checker.setResult(15);
				checker.setResultDesc("没有符合的vxlan实例");
				return getReturnXml(checker);
			}
			// 先将数据全部插入或者更新入数据库
			insertData(userId);
			
			//　采集nat下节点
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			ArrayList<String> natPathsList = corba.getParamNamesPath(deviceId, natRootPath, 0);
			logger.warn("natPathsList :[{}]", natPathsList);
			
			
			// 设备上不存在nat实例，做新增业务
			if (!nonExistPublicIp(natPathsList, deviceId, userId)) {
				return getReturnXml(checker);
			}
			
			// 设备上存在nat实例
			if (!existPublicIp(natPathsList, deviceId, userId)) {
				return getReturnXml(checker);
			}
			
			checker.setResult(0);
			checker.setResultDesc("成功");
			return getReturnXml(checker); 
		}
		catch (Exception e) {
			logger.warn("CloudQueryVXLANConfigureService is error:", e);
		}
		return getReturnXml(checker);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudVXLANPublicIPNatCfgChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudQueryVXLANConfigureService", checker.getCmdId());
		String retXml = checker.getReturnXml();
		logger.warn("servicename[CloudQueryVXLANConfigureService]cmdId[{}]处理结束，返回响应信息:{}",
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
			// 打印回参
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
	 * 设备上不存在nat实例，所有实例新增到设备
	 * @param natPathsList
	 * @param deviceId
	 * @return
	 */
	private boolean nonExistPublicIp(ArrayList<String> natPathsList, String deviceId, Long userId) {
		logger.warn("[{}] [{}] [{}]nonExistPublicIp begin", natPathsList, deviceId, userId);
		if (natPathsList != null && natPathsList.size() > 1 && !natPathsList.get(0).equals(natRootPath)) {
			return true;
		}
		
		StringBuffer restStr = new StringBuffer();
		String ret = "";
		// 所有实例新增到设备
		for (Map<String, String> map : checker.getNatList()) {
			if (!StringUtil.isEmpty(ret = VxlanOperateDeviceUtil.addNat(deviceId, natRootPath, map, checker.getNatType(), interfaceStr))) {
				restStr.append(ret).append(";");
				failList.add(ret);
			}
		}
		
		if (restStr.length() != 0) {
			// 更新失败的数据
			updateFailData(userId);
			checker.setResult(15);
			checker.setResultDesc("公网ip为：" + restStr.toString() +" 的实例下发失败");
			return false;
		}
		// vxlan开通实例总数
		checker.setResult(0);
		checker.setResultDesc("成功");
		return false; 
	}
	
	/**
	 * 设备上存在nat实例
	 * @param natPathsList
	 * @param deviceId
	 * @return
	 */
	private boolean existPublicIp(ArrayList<String> natPathsList, String deviceId, Long userId) {
		// 采集nat节点中公网ip
		ArrayList<String> paramNameList = new ArrayList<String>();
		for (String natPath : natPathsList) {
			if (natPath.contains(".ExternalAddress")) {
				paramNameList.add(natPath);
			}
		}

		String[] paramNametemp = paramNameList.toArray(new String[paramNameList.size()]);
		Map<String, String> paramValueMap = corba.getParaValueMap(deviceId, paramNametemp);
		if (paramValueMap == null || paramValueMap.isEmpty()) {
			logger.warn("[CloudQueryVXLANConfigureService] [{}]获取ServiceList失败", deviceId);
			checker.setResult(1004);
			checker.setResultDesc("设备采集失败");
			return false;
		}
		
		// 获取到设备已经存在的公网ip
		List<String> existsIp = new ArrayList<String>();
		Map<String, String> ipNode = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
			existsIp.add(entry.getValue());
			ipNode.put(entry.getValue(), entry.getKey().replace("ExternalAddress", ""));
		}
		
		StringBuffer restStr = new StringBuffer();
		for (Map<String, String> map : checker.getNatList()) {
			String _pubIpv4 = map.get("pubIpv4");
			String ret = "";
			// 设备上已经存在相同的节点做set操作
			if (existsIp.contains(_pubIpv4)) {
				if (!StringUtil.isEmpty(ret = VxlanOperateDeviceUtil.setNat(deviceId, ipNode.get(_pubIpv4), map, checker.getNatType(), interfaceStr))) {
					failList.add(ret);
					restStr.append(ret).append(";");
				}
			}
			// 设备上已经不存在的节点做add操作
			else {
				if (!StringUtil.isEmpty(ret = VxlanOperateDeviceUtil.addNat(deviceId, natRootPath, map, checker.getNatType(), interfaceStr))) {
					failList.add(ret);
					restStr.append(ret).append(";");
				}
			}
		}
		
		if (restStr.length() != 0) {
			// 更新失败的数据
			updateFailData(userId);
			checker.setResult(15);
			checker.setResultDesc("公网ip为：" + restStr.toString() +" 的实例下发失败");
			return false;
		}
		// vxlan开通实例总数
		checker.setResult(0);
		checker.setResultDesc("成功");
		return true; 
	}
	
	/**
	 * 将数据全部入库
	 * @param userId
	 */
	private void insertData(Long userId) {
		ArrayList<String> servSqlList = new ArrayList<String>();
		List<Map<String, String>> list = checker.getNatList();
		List<String> searchList = new ArrayList<String>();
		
		for (Map<String, String> map : list) {
			searchList.add("'" + map.get("pubIpv4") + "'");
		}
		List<String> existList = ipsDao.getNatExistPubIP(userId, StringUtils.join(searchList, ","));
		for (Map<String, String> map : list) {
			StringBuffer sbsql = new StringBuffer();
			if (existList.contains(map.get("pubIpv4"))) {
				sbsql.append(" update tab_vxlan_nat_config set pub_port = '").append(StringUtil.getStringValue(map, "pubPort")).append("',");
				sbsql.append(" priv_ipv4 = '").append(StringUtil.getStringValue(map, "privIpv4")).append("',");
				sbsql.append(" priv_port = '").append(StringUtil.getStringValue(map, "privPort")).append("',");
				sbsql.append(" protocol = '").append(StringUtil.getStringValue(map, "protocol")).append("',");
				sbsql.append(" state = 1");
				sbsql.append(" where user_id = ").append(userId).append(" and ");
				sbsql.append(" pub_ipv4 = '").append(StringUtil.getStringValue(map, "pubIpv4")).append("'");
			}
			else {
				sbsql.append(" insert into tab_vxlan_nat_config (user_id, pub_ipv4, pub_port,");
				sbsql.append(" priv_ipv4, priv_port, protocol, state) values (");
				sbsql.append(userId).append(",");
				sbsql.append("'").append(StringUtil.getStringValue(map, "pubIpv4")).append("',");
				sbsql.append("'").append(StringUtil.getStringValue(map, "pubPort")).append("',");
				sbsql.append("'").append(StringUtil.getStringValue(map, "privIpv4")).append("',");
				sbsql.append("'").append(StringUtil.getStringValue(map, "privPort")).append("',");
				sbsql.append("'").append(StringUtil.getStringValue(map, "protocol")).append("',");
				sbsql.append("1)");
			}
			servSqlList.add(sbsql.toString());
		}
		DBOperation.executeUpdate(servSqlList);
	}
	
	/**
	 * 更新失败数据
	 * @param userId
	 * @param failList
	 */
	private void updateFailData(Long userId) {
		List<String> searchList = new ArrayList<String>();
		for (String failIp : failList) {
			searchList.add("'" + failIp + "'");
		}
		ipsDao.updateFailNatData(userId, StringUtils.join(searchList, ","));
	}
	
	public static void main(String[] args) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();;
		Map<String, String> map = new HashMap<String, String>();
		map.put("privIpv4", "192.168.1.1");
		map.put("pubIpv4", "121.10.10.1/24");
		list.add(map);
		List<String> searchList = new ArrayList<String>();
		
		for (Map<String, String> map1 : list) {
			searchList.add("'" + map1.get("pubIpv4") + "'");
		}
		System.out.println(searchList);
		System.out.println(StringUtils.join(searchList, ","));
	}
}
