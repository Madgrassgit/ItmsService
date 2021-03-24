package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.CloudQueryConfigureChecker;
import com.linkage.itms.obj.ParameValueOBJ;


public class CloudQueryConfigureService implements IService {

	// 日志
	private static final Logger logger = LoggerFactory.getLogger(CloudQueryConfigureService.class);

	@Override
	public String work(String inXml) {
		CloudQueryConfigureChecker checker = new CloudQueryConfigureChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[CloudQueryConfigureService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return checker.getReturnXml();
			}
			logger.warn("servicename[CloudQueryConfigureService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
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
			
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			ACSCorba corba = new ACSCorba();
			int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
			logger.warn("设备[{}],在线状态[{}] ",new Object[]{deviceId, flag});
			// 设备正在被操作，不能获取节点值
			if (-3 == flag) {
				logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
				checker.setResult(1003);
				checker.setResultDesc("网关正在被操作");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			}
			// 设备在线
			else if (1 == flag) {
				logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
				ACSCorba acsCorba = new ACSCorba();
				String ipsecPath = "InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN.";
				List<String> jList = acsCorba.getIList(deviceId, ipsecPath);
				if (null == jList || jList.size() == 0 || jList.isEmpty()) {
					logger.warn("servicename[CloudQueryConfigureService][{}]获取" + ipsecPath + "下实例号失败", deviceId);
					checker.setResult(1003);
					checker.setResultDesc("获取InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN.下实例号失败");
					return checker.getReturnXml();
				}
				ArrayList<String> ipsecList = new ArrayList<String>();
				for (String j : jList) {
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".RemoteDomain");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IPSecOutInterface");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IPSecEncapsulationMode");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IPSecType");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".RemoteIP");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".ExchangeMode");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IKEAuthenticationAlgorithm");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IKEAuthenticationMethod");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IKEEncryptionAlgorithm");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IKEDHGroup");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IKEIDType");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IKELocalName");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IKERemoteName");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IKEPreshareKey");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IPSecTransform");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".ESPAuthenticationAlgorithm");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".ESPEncryptionAlgorithm");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IPSecPFS");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IKESAPeriod");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IPSecSATimePeriod");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".IPSecSATrafficPeriod");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".AHAuthenticationAlgorithm");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".RemoteSubnet");
					ipsecList.add("InternetGatewayDevice.VPN.X_CT-COM_IPSecVPN." + j + ".LocalSubnet");

					String[] gatherPath = new String[24];
					ArrayList<ParameValueOBJ> objList = corba.getValue(deviceId, ipsecList.toArray(gatherPath));
					if (null != objList && !objList.isEmpty()) {
						for (ParameValueOBJ pvobj : objList) {
							String value = pvobj.getValue();
							if (pvobj.getName().endsWith("RemoteSubnet")) {
								checker.setRemoteSubnet(value);
							} 
							else if (pvobj.getName().endsWith("LocalSubnet")) {
								checker.setLocalSubnet(value);
							} 
							else if (pvobj.getName().endsWith("RemoteDomain")) {
								checker.setRemoteDomain(value);
							} 
							else if (pvobj.getName().endsWith("IPSecOutInterface")) {
								checker.setiPSecOutInterface(value);
							} 
							else if (pvobj.getName().endsWith("IPSecEncapsulationMode")) {
								checker.setiPSecEncapsulationMode(value);
							} 
							else if (pvobj.getName().endsWith("IPSecType")) {
								checker.setIpSecType(value);
							} 
							else if (pvobj.getName().endsWith("RemoteIP")) {
								checker.setRemoteIP(value);
							}
							else if (pvobj.getName().endsWith("ExchangeMode")) {
								checker.setExchangeMode(value);
							} 
							else if (pvobj.getName().endsWith("IKEAuthenticationAlgorithm")) {
								checker.setIkeAuthenticationAlgorithm(value);
							} 
							else if (pvobj.getName().endsWith("IKEAuthenticationMethod")) {
								checker.setIkeAuthenticationMethod(value);
							} 
							else if (pvobj.getName().endsWith("IKEEncryptionAlgorithm")) {
								checker.setIkeEncryptionAlgorithm(value);
							} 
							else if (pvobj.getName().endsWith("IKEDHGroup")) {
								checker.setIkeDHGroup(value);
							} 
							else if (pvobj.getName().endsWith("IKEIDType")) {
								checker.setIkeIDType(value);
							} 
							else if (pvobj.getName().endsWith("IKELocalName")) {
								checker.setIkeLocalName(value);
							} 
							else if (pvobj.getName().endsWith("IKERemoteName")) {
								checker.setIkeRemoteName(value);
							} 
							else if (pvobj.getName().endsWith("IKEPreshareKey")) {
								checker.setIkePreshareKey(value);
							} 
							else if (pvobj.getName().endsWith("IPSecTransform")) {
								checker.setIpSecTransform(value);
							} 
							else if (pvobj.getName().endsWith("ESPAuthenticationAlgorithm")) {
								checker.setEspAuthenticationAlgorithm(value);
							} 
							else if (pvobj.getName().endsWith("ESPEncryptionAlgorithm")) {
								checker.setEspEncryptionAlgorithm(value);
							}
							else if (pvobj.getName().endsWith("IPSecPFS")) {
								checker.setIpSecPFS(value);
							}
							else if (pvobj.getName().endsWith("IKESAPeriod")) {
								checker.setIkeSAPeriod(value);
							}
							else if (pvobj.getName().endsWith("IPSecSATimePeriod")) {
								checker.setIpSecSATimePeriod(value);
							}
							else if (pvobj.getName().endsWith("IPSecSATrafficPeriod")) {
								checker.setIpSecSATrafficPeriod(value);
							}
							else if (pvobj.getName().endsWith("AHAuthenticationAlgorithm")) {
								checker.setAhAuthenticationAlgorithm(value);
							}
						}
					}
					else {
						logger.warn("采集不到对应网关节点信息，device_id={}", deviceId);
					}
				}
				checker.setResult(0);
				checker.setResultDesc("成功");
			}
			// 设备不在线，不能获取节点值
			else {
				logger.warn("设备不在线，无法获取节点值");
				checker.setResult(8);
				checker.setResultDesc("网关不在线");
				logger.warn("return=({})", checker.getReturnXml());
			}
		}
		catch (Exception e) {
			logger.warn("CloudQueryConfigureService is error:", e);
		}
		return getReturnXml(checker);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(CloudQueryConfigureChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, "CloudQueryConfigureService", checker.getCmdId());
		logger.warn("servicename[CloudQueryConfigureService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), checker.getReturnXml()});
		return checker.getReturnXml();
	}
}
