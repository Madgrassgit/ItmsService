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
import com.linkage.itms.dispatch.obj.CloudQueryStaticRtCfgChecker;
import com.linkage.itms.dispatch.util.VxlanOperateDeviceUtil;
import com.linkage.itms.obj.ParameValueOBJ;


public class CloudQueryStaticRtCfgService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudQueryStaticRtCfgService.class);

	@Override
	public String work(String inXml) {
		CloudQueryStaticRtCfgChecker checker = new CloudQueryStaticRtCfgChecker(inXml);
		ArrayList<Map<String, String>> retList = new ArrayList<Map<String, String>>();
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudQueryStaticRtCfgService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return getReturnXml(checker, retList);
			}
			logger.warn("servicename[CloudQueryStaticRtCfgService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
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
			String ipsecPath = "InternetGatewayDevice.Layer3Forwarding.Forwarding.";
			List<String> jList = corba.getIList(deviceId, ipsecPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty()) {
				logger.warn("servicename[CloudQueryStaticRtCfgService][{}]获取[{}]下实例号失败", deviceId, ipsecPath);
				checker.setResult(1003);
				checker.setResultDesc("获取InternetGatewayDevice.Layer3Forwarding.Forwarding.下实例号失败");
				return getReturnXml(checker, retList);
			}
			logger.warn("获取到的节点值为：" + jList.toString());
			
			
			ArrayList<String> vxlanList = null;
			Map<String, String> retMap = null;
			for (String j : jList) {
				retMap = new HashMap<String, String>();
				vxlanList = new ArrayList<String>();

				vxlanList.add(ipsecPath + j + ".Enable");
				vxlanList.add(ipsecPath + j + ".DestIPAddress");
				vxlanList.add(ipsecPath + j + ".DestSubnetMask");
				vxlanList.add(ipsecPath + j + ".Nexthop");
				vxlanList.add(ipsecPath + j + ".ForwardingMetric");

				String[] gatherPath = new String[5];
				ArrayList<ParameValueOBJ> objList = corba.getValue(deviceId, vxlanList.toArray(gatherPath));
				if (objList == null || objList.isEmpty()) {
					logger.warn("采集不到" + ipsecPath + j + " 对应网关节点信息，device_id={}, list={}", deviceId, objList);
					continue;
				}
				// 实例是否开启
				String ip = "";
				String mask = "";
				boolean isGather = false;
				for (ParameValueOBJ pvobj : objList) {
					String value = pvobj.getValue();
					if (pvobj.getName().endsWith("Enable")) {
						if ("0".equals(value)) {
							continue;
						}
						isGather = true;
					} 
					else if (pvobj.getName().endsWith("DestIPAddress")) {
						ip = value;
					} 
					else if (pvobj.getName().endsWith("DestSubnetMask")) {
						mask = value;
					} 
					else if (pvobj.getName().endsWith("Nexthop")) {
						retMap.put("nextHop", value);
					} 
					else if (pvobj.getName().endsWith("ForwardingMetric")) {
						retMap.put("priority", value);
					} 
				}
				if (!isGather) {
					continue;
				}
				retMap.put("desIp", ip + "/" + VxlanOperateDeviceUtil.maskToInt(mask));
				retMap.put("rtId", j);
				retList.add(retMap);
			}
			// vxlan开通实例总数
			checker.setResult(0);
			checker.setResultDesc("成功");
		}
		catch (Exception e) {
			logger.warn("CloudQueryStaticRtCfgService is error:", e);
		}
		return getReturnXml(checker, retList);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudQueryStaticRtCfgChecker checker, ArrayList<Map<String, String>> retList) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudQueryStaticRtCfgService", checker.getCmdId());
		String retXml = checker.getReturnXml(retList);
		logger.warn("servicename[CloudQueryStaticRtCfgService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), retXml});
		return retXml;
	}
}
