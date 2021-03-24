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
import com.linkage.itms.dispatch.obj.CloudQueryVXLANPublicIPNatCfgChecker;
import com.linkage.itms.obj.ParameValueOBJ;


public class CloudQueryVXLANPublicIPNatCfgService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudQueryVXLANPublicIPNatCfgService.class);
	
	@Override
	public String work(String inXml) {
		ArrayList<Map<String, String>> retList = new ArrayList<Map<String, String>>();
		CloudQueryVXLANPublicIPNatCfgChecker checker = new CloudQueryVXLANPublicIPNatCfgChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudQueryVXLANPublicIPNatCfgService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return getReturnXml(checker, retList);
			}
			logger.warn("servicename[CloudQueryVXLANPublicIPNatCfgService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
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
			String deviceId = StringUtil.getStringValue(userMap.get(0), "device_id");
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
			String ipsecPath = "InternetGatewayDevice.X_CT-COM_NAT.";
			List<String> jList = corba.getIList(deviceId, ipsecPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty()) {
				logger.warn("servicename[CloudQueryVXLANPublicIPNatCfgService][{}]获取" + ipsecPath + "下实例号失败", deviceId);
				checker.setResult(1003);
				checker.setResultDesc("获取InternetGatewayDevice.X_CT-COM_NAT.下实例号失败");
				return getReturnXml(checker, retList);
			}
			logger.warn("获取到的节点值为：" + jList.toString());
			
			
			ArrayList<String> vxlanList = null;
			Map<String, String> retMap = null;
			for (String j : jList) {
				retMap = new HashMap<String, String>();
				vxlanList = new ArrayList<String>();

				vxlanList.add(ipsecPath + j + ".Type");
				//vxlanList.add(ipsecPath + j + ".Interface");
				vxlanList.add(ipsecPath + j + ".InternalAddress");
				vxlanList.add(ipsecPath + j + ".ExternalAddress");
				vxlanList.add(ipsecPath + j + ".Protocol");
				vxlanList.add(ipsecPath + j + ".InternalPort");
				vxlanList.add(ipsecPath + j + ".ExternalPort");

				String[] gatherPath = new String[6];
				ArrayList<ParameValueOBJ> objList = corba.getValue(deviceId, vxlanList.toArray(gatherPath));
				if (objList == null || objList.isEmpty()) {
					logger.warn("采集不到" + ipsecPath + j + " 对应网关节点信息，device_id={}, list={}", deviceId, objList);
					continue;
				}
				// 实例是否开启
				for (ParameValueOBJ pvobj : objList) {
					String value = pvobj.getValue();
					if (pvobj.getName().endsWith("Type")) {
						retMap.put("natType", value);
					} 
					else if (pvobj.getName().endsWith("InternalAddress")) {
						retMap.put("privIpv4", value);
					} 
					else if (pvobj.getName().endsWith("ExternalAddress")) {
						retMap.put("pubIpv4", value);
					} 
					else if (pvobj.getName().endsWith("Protocol")) {
						retMap.put("protocol", value);
					} 
					else if (pvobj.getName().endsWith("InternalPort")) {
						retMap.put("privPort", value);
					} 
					else if (pvobj.getName().endsWith("ExternalPort")) {
						retMap.put("pubPort", value);
					} 
				}
				retList.add(retMap);
			}
			// vxlan开通实例总数
			checker.setResult(0);
			checker.setResultDesc("成功");
			return getReturnXml(checker, retList); 
		}
		catch (Exception e) {
			logger.warn("CloudQueryVXLANPublicIPNatCfgService is error:", e);
		}
		return getReturnXml(checker, retList);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudQueryVXLANPublicIPNatCfgChecker checker, ArrayList<Map<String, String>> retList) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudQueryVXLANPublicIPNatCfgService", checker.getCmdId());
		String retXml = checker.getReturnXml(retList);
		logger.warn("servicename[CloudQueryVXLANPublicIPNatCfgService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), retXml});
		return retXml;
	}
}
