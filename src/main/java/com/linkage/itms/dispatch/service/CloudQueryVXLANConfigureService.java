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
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.CloudQueryVXLANConfigureChecker;
import com.linkage.itms.obj.ParameValueOBJ;


public class CloudQueryVXLANConfigureService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudQueryVXLANConfigureService.class);

	@Override
	public String work(String inXml) {
		CloudQueryVXLANConfigureChecker checker = new CloudQueryVXLANConfigureChecker(inXml);
		ArrayList<Map<String, String>> retList = new ArrayList<Map<String, String>>();
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudQueryVXLANConfigureService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return getReturnXml(checker, retList);
			}
			logger.warn("servicename[CloudQueryVXLANConfigureService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
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
				return getReturnXml(checker, retList);
			}
			
			String deviceId =  StringUtil.getStringValue(userMap.get(0), "device_id");
			if (StringUtil.isEmpty(deviceId)) {
				checker.setResult(7);
				checker.setResultDesc("查询不到对应网关");
				return getReturnXml(checker, retList);
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
			
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			ACSCorba corba = new ACSCorba();
			int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
			logger.warn("设备[{}],在线状态[{}] ",new Object[]{deviceId, flag});
			// 设备正在被操作，不能获取节点值
			if (-3 == flag) {
				logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
				checker.setResult(1003);
				checker.setResultDesc("网关正在被操作");
				// 打印回参
				logger.warn("return=({})", checker.getReturnXml());
				return getReturnXml(checker, retList);
			}
			// 设备不在线，不能获取节点值
			if (1 != flag) {
				logger.warn("设备不在线，无法获取节点值");
				checker.setResult(8);
				checker.setResultDesc("网关不在线");
				logger.warn("return=({})", checker.getReturnXml());
				return getReturnXml(checker, retList);
			}
			// 设备在线
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			ACSCorba acsCorba = new ACSCorba();
			String ipsecPath = "InternetGatewayDevice.X_CT-COM_VXLAN.VXLANConfig.";
			List<String> jList = acsCorba.getIList(deviceId, ipsecPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty()) {
				logger.warn("servicename[CloudQueryVXLANConfigureService][{}]获取" + ipsecPath + "下实例号失败", deviceId);
				checker.setResult(1003);
				checker.setResultDesc("获取InternetGatewayDevice.X_CT-COM_VXLAN.VXLANConfig.下实例号失败");
				return getReturnXml(checker, retList);
			}
			logger.warn("获取到的节点值为：" + jList.toString());
			
			String vxlanConfigSequence = checker.getVxlanConfigSequence();
			if (!StringUtil.isEmpty(vxlanConfigSequence) && !jList.contains(vxlanConfigSequence)) {
				logger.warn("servicename[CloudQueryVXLANConfigureService][{}]没有VXLANConfigSequence为：{} 实例", deviceId, vxlanConfigSequence);
				checker.setResult(1003);
				checker.setResultDesc("没有VXLANConfigSequence为" + vxlanConfigSequence + "的实例");
				return getReturnXml(checker, retList);
			}
			
			ArrayList<String> vxlanList = null;
			Map<String, String> retMap = null;
			for (String j : jList) {
				// 查询的实例vxlanConfigSequence如果为空，则查询所有
				if (!StringUtil.isEmpty(vxlanConfigSequence)) {
					// 查询的实例vxlanConfigSequence如果不为空，则查询对应实例
					if (!j.equals(vxlanConfigSequence)) {
						continue;
					}
				}
				retMap = new HashMap<String, String>();
				vxlanList = new ArrayList<String>();

				vxlanList.add(ipsecPath + j + ".TunnelKey");
				vxlanList.add(ipsecPath + j + ".Enable");
				vxlanList.add(ipsecPath + j + ".TunnelRemoteIp");
				vxlanList.add(ipsecPath + j + ".WorkMode");
				vxlanList.add(ipsecPath + j + ".MaxMTUSize");
				vxlanList.add(ipsecPath + j + ".IPAddress");
				vxlanList.add(ipsecPath + j + ".SubnetMask");
				vxlanList.add(ipsecPath + j + ".AddressingType");
				vxlanList.add(ipsecPath + j + ".NATEnabled");
				vxlanList.add(ipsecPath + j + ".DNSServers_Master");
				vxlanList.add(ipsecPath + j + ".DNSServers_Slave");
				vxlanList.add(ipsecPath + j + ".DefaultGateway");
				vxlanList.add(ipsecPath + j + ".X_CT-COM_VLAN");

				String[] gatherPath = new String[12];
				ArrayList<ParameValueOBJ> objList = corba.getValue(deviceId, vxlanList.toArray(gatherPath));
				if (objList == null || objList.isEmpty()) {
					logger.warn("采集不到" + ipsecPath + j + " 对应网关节点信息，device_id={}, list={}", deviceId, objList);
					continue;
				}
				// 实例是否开启
				boolean isEnable = false;
				for (ParameValueOBJ pvobj : objList) {
					String value = pvobj.getValue();
					retMap.put("VXLANConfigSequence", j);
					if (pvobj.getName().endsWith("TunnelKey")) {
						retMap.put("TunnelKey", value);
					}
					else if (pvobj.getName().endsWith("Enable")) {
						// 判断该实例是否开启
						isEnable = "1".equals(value);
					} 
					else if (pvobj.getName().endsWith("TunnelRemoteIp")) {
						retMap.put("TunnelRemoteIP", value);
					} 
					else if (pvobj.getName().endsWith("WorkMode")) {
						retMap.put("WorkMode", value);
					} 
					else if (pvobj.getName().endsWith("MaxMTUSize")) {
						retMap.put("MaxMTUSize", value);
					} 
					else if (pvobj.getName().endsWith("IPAddress")) {
						retMap.put("IPAddress", value);
					} 
					else if (pvobj.getName().endsWith("SubnetMask")) {
						retMap.put("SubnetMask", value);
					} 
					else if (pvobj.getName().endsWith("AddressingType")) {
						retMap.put("AddressingType", value);
					}
					else if (pvobj.getName().endsWith("NATEnabled")) {
						retMap.put("NATEnabled", value);
					} 
					else if (pvobj.getName().endsWith("DNSServers_Master")) {
						retMap.put("DNSServers_Master", value);
					} 
					else if (pvobj.getName().endsWith("DNSServers_Slave")) {
						retMap.put("DNSServers_Slave", value);
					} 
					else if (pvobj.getName().endsWith("DefaultGateway")) {
						retMap.put("DefaultGateway", value);
					} 
					else if (pvobj.getName().endsWith("X_CT-COM_VLAN")) {
						retMap.put("X_CT-COM_VLAN", value);
					} 
				}
				// 销户的实例不采集
				if (!isEnable) {
					logger.warn("servicename[CloudQueryVXLANConfigureService][{}] " +
							"VXLANConfigSequence为：{} 的实例已销户", deviceId, j);
					continue;
				}
				retList.add(retMap);
			}
			// 已经销户的节点 
			if (!StringUtil.isEmpty(vxlanConfigSequence) && retList.size() == 0) {
				logger.warn("servicename[CloudQueryVXLANConfigureService][{}] VXLANConfigSequence为：{} 的实例已销户", deviceId, vxlanConfigSequence);
				checker.setResult(1003);
				checker.setResultDesc("VXLANConfigSequence为" + vxlanConfigSequence + "的实例已经销户");
				return getReturnXml(checker, retList);
			}
			// vxlan开通实例总数
			checker.setInstanceNum(retList.size());
			checker.setResult(0);
			checker.setResultDesc("成功");
		}
		catch (Exception e) {
			logger.warn("CloudQueryVXLANConfigureService is error:", e);
		}
		return getReturnXml(checker, retList);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudQueryVXLANConfigureChecker checker, ArrayList<Map<String, String>> retList) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudQueryVXLANConfigureService", checker.getCmdId());
		String retXml = checker.getReturnXml(retList);
		logger.warn("servicename[CloudQueryVXLANConfigureService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), retXml});
		return retXml;
	}
}
