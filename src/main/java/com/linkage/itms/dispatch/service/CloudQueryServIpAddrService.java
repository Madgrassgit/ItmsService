package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cloud.AsynchronousService;
import com.linkage.itms.dispatch.obj.CloudQueryServIpAddrChecker;
import com.linkage.itms.obj.ParameValueOBJ;

public class CloudQueryServIpAddrService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudQueryServIpAddrService.class);

	@Override
	public String work(String inXml) {
		CloudQueryServIpAddrChecker checker = new CloudQueryServIpAddrChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudQueryServIpAddrService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
				return checker.getReturnXml();
			}
			logger.warn("servicename[CloudQueryServIpAddrService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });

			QueryDevDAO qdDao = new QueryDevDAO();
			List<HashMap<String, String>> userMap = null;
			if (checker.getUserInfoType() == 1) {
				userMap = qdDao.queryUserByNetAccountCloud(checker.getUserInfo());
			} else if (checker.getUserInfoType() == 2) {
				userMap = qdDao.queryUserByLoidCloud(checker.getUserInfo());
			}
			if (userMap == null || userMap.isEmpty()) {
				checker.setResult(6);
				checker.setResultDesc("查询不到对应用户");
				return checker.getReturnXml();
			}
			
			String deviceId = StringUtil.getStringValue(userMap.get(0), "device_id");
			
			//String loopback_ip = qdDao.queryLoopBackIp(deviceId);
			if (StringUtil.isEmpty(deviceId)) {
				checker.setResult(7);
				checker.setResultDesc("查询不到对应网关");
				return checker.getReturnXml();
			}

			// Loid
			checker.setLoid(StringUtil.getStringValue(userMap.get(0), "username"));
			StringBuffer loidPrev = new StringBuffer();
			int n = 0;
			for (HashMap<String, String> m : userMap) {
				if (n == 0) {
					n++;
					continue;
				}
				loidPrev.append(StringUtil.getStringValue(m, "username"));
				loidPrev.append(";");
			}
			// LoidPrev 先设置为空
//			checker.setLoidPrev("");
			checker.setLoidPrev(loidPrev.toString());
			if (!"0".equals(checker.getCallBack())) {
				AsynchronousService as = new AsynchronousService(inXml, "QueryServIpAddr");
				Global.G_AsynchronousThread.execute(as);
				return getReturnXml(checker);
			}
			// 校验设备是否在线
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			ACSCorba acsCorba = new ACSCorba();
			int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
			// 设备正在被操作，不能获取节点值
			if (-6 == flag) {
				logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
				checker.setResult(1003);
				checker.setResultDesc("网关正在被操作");
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				return checker.getReturnXml();
			}
			// 设备在线
			else if (1 == flag) {
				logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
				// 采集accessType
				String accessType = null;
				accessType = UserDeviceDAO.getAccType(deviceId);
				if (null == accessType || "null".equals(accessType)
						|| "".equals(accessType)) {
					String accessTypePath = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
					ArrayList<ParameValueOBJ> accessTypeList = acsCorba.getValue(deviceId, accessTypePath);
					if (accessTypeList != null && accessTypeList.size() != 0) {
						for (ParameValueOBJ pvobj : accessTypeList) {
							if (pvobj.getName().endsWith("WANAccessType")) {
								accessType = pvobj.getValue();
							}
						}
					}
				}

				logger.warn("accessType为：[{}]", accessType);
				
				if (!"EPON".equals(accessType) && !"GPON".equals(accessType)) {
					logger.warn("accessType既不是EPON也不是GPON");
					checker.setResult(1012);
					checker.setResultDesc("上行方式既不是EPON也不是GPON");
					logger.warn("return=({})", checker.getReturnXml()); // 打印回参
					return checker.getReturnXml();
				}

				String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
				String wanServiceList = ".X_CT-COM_ServiceList";
				String wanPPPConnection = ".WANPPPConnection.";
				String wanIPConnection = ".WANIPConnection.";
				String INTERNET = "INTERNET";

				// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
				ArrayList<String> wanConnPathsList = acsCorba.getParamNamesPath(deviceId, wanConnPath, 0);
				if (wanConnPathsList == null || wanConnPathsList.size() == 0
						|| wanConnPathsList.isEmpty()) {
					logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取", deviceId);
					wanConnPathsList = new ArrayList<String>();
					List<String> jList = acsCorba.getIList(deviceId, wanConnPath);
					if (null == jList || jList.size() == 0 || jList.isEmpty()) {
						logger.warn("[CloudQueryServIpAddrService] [{}]获取" + wanConnPath + "下实例号失败，返回", deviceId);
						checker.setResult(1006);
						checker.setResultDesc("此路径下获取节点失败");
						return checker.getReturnXml();
					}
					for (String j : jList) {
						// 获取session，
						List<String> kPPPList = acsCorba.getIList(deviceId, wanConnPath + j + wanIPConnection);
						if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty()) {
							logger.warn("[CloudQueryServIpAddrService] [{}]获取" + wanConnPath + wanConnPath + j
									+ wanIPConnection + "下实例号失败", deviceId);
						} else {
							for (String kppp : kPPPList) {
								wanConnPathsList.add(wanConnPath + j + wanIPConnection + kppp
										+ wanServiceList);
							}
						}
					}
				}
				// serviceList节点
				ArrayList<String> serviceListList = new ArrayList<String>();
				// 所有需要采集的节点
				ArrayList<String> paramNameList = new ArrayList<String>();
				for (int i = 0; i < wanConnPathsList.size(); i++) {
					String namepath = wanConnPathsList.get(i);
					if (namepath.indexOf(wanServiceList) >= 0
							/*&& namepath.indexOf(wanPPPConnection) >= 0*/) {
						serviceListList.add(namepath);
						paramNameList.add(namepath);
						continue;
					}
				}
				if (serviceListList.size() == 0 || serviceListList.isEmpty()) {
					logger.warn("[CloudQueryServIpAddrService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回",
							deviceId);
					checker.setResult(1006);
					checker.setResultDesc("不存在WANIP下的X_CT-COM_ServiceList节点");
					return checker.getReturnXml();
				}

				String[] paramNameArr = new String[paramNameList.size()];
				int arri = 0;
				for (String paramName : paramNameList) {
					paramNameArr[arri] = paramName;
					arri = arri + 1;
				}
				Map<String, String> paramValueMap = new HashMap<String, String>();
				for (int k = 0; k < (paramNameArr.length / 20) + 1; k++) {
					String[] paramNametemp = new String[paramNameArr.length
							- (k * 20) > 20 ? 20 : paramNameArr.length
							- (k * 20)];
					for (int m = 0; m < paramNametemp.length; m++) {
						paramNametemp[m] = paramNameArr[k * 20 + m];
					}
					Map<String, String> maptemp = acsCorba.getParaValueMap(
							deviceId, paramNametemp);
					if (maptemp != null && !maptemp.isEmpty()) {
						paramValueMap.putAll(maptemp);
					}
				}
				if (paramValueMap.isEmpty()) {
					logger.warn("[CloudQueryServIpAddrService] [{}]获取ServiceList失败", deviceId);
					checker.setResult(1007);
					checker.setResultDesc("获取ServiceList失败");
					return checker.getReturnXml();
				}
				
				String pathJ = "";
				String pathK = "";
				String connectionNode = "";
				for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
					logger.debug("[{}]{}={} ", new Object[] { deviceId, entry.getKey(), entry.getValue() });
					String paramName = entry.getKey();
					if (paramName.indexOf(wanServiceList) == -1) {
						continue;
					}
					if (paramName.indexOf(wanPPPConnection) == -1 && paramName.indexOf(wanIPConnection) == -1) {
						continue;
					}
					if (!StringUtil.IsEmpty(entry.getValue()) && (entry.getValue().indexOf(INTERNET) >= 0)) {
						pathJ = paramName.substring(wanConnPath.length(), paramName.indexOf(".", wanConnPath.length()));
						pathK = paramName.substring(paramName.indexOf(wanServiceList) - 1, paramName.indexOf(wanServiceList));
						connectionNode = paramName.indexOf(wanPPPConnection) >= 0 ? wanPPPConnection : wanIPConnection;
						break;
					}
				}
				if (StringUtil.IsEmpty(pathJ) || StringUtil.IsEmpty(pathK)) {
					logger.warn("[CloudQueryServIpAddrService] [{}]获取pathJ,pathK节点值失败", deviceId);
					checker.setResult(1007);
					checker.setResultDesc("获取pathJ,pathK节点值失败");
					return checker.getReturnXml();
				}
				// 组播vlan节点路径
				String ipAddresss = wanConnPath + pathJ + connectionNode + pathK + ".ExternalIPAddress";

				ArrayList<ParameValueOBJ> objLlist = acsCorba.getValue(deviceId, ipAddresss);
				logger.warn("采集节点为：{}", ipAddresss);
				if (null != objLlist && !objLlist.isEmpty()) {
					checker.setServIpAddr(objLlist.get(0).getValue());
				} else {
					logger.warn("采集不到对应网关节点信息，device_id={}", deviceId);
				}

			} else {// 设备不在线，不能获取节点值
				logger.warn("设备不在线，无法获取节点值");
				checker.setResult(8);
				checker.setResultDesc("网关不在线");
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				return checker.getReturnXml();
			}
		} catch (Exception e) {
			logger.warn("CloudQueryServIpAddrService is error:", e);
		}
		return getReturnXml(checker);
	}

	/**
	 * 记录日志返回xml
	 * 
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudQueryServIpAddrChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker,
				"CloudIpAddrService", checker.getCmdId());
		logger.warn("servicename[CloudQueryServIpAddrService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getReturnXml() });
		return checker.getReturnXml();
	}
}
