package com.linkage.itms.dispatch.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.dispatch.obj.CloudStaticRtCfgChecker;
import com.linkage.itms.obj.AddOBJ;
import com.linkage.itms.obj.ParameValueOBJ;

public class VxlanOperateDeviceUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(VxlanOperateDeviceUtil.class);
	
	private static ACSCorba acsCorba = new ACSCorba();
	private static final String MASK_STR = "11111111111111111111111111111111";
	/**
	 * 新增nat节点
	 * @param acsCorba
	 * @param deviceId
	 * @param natPath
	 * @param map
	 * @param natType
	 * @param interfaceStr
	 * @return
	 */
	public static String addNat(String deviceId, String natPath, Map<String, String> map, String natType, String interfaceStr) {
		String pubIpv4 = map.get("pubIpv4");
		logger.warn("VxlanOperateDeviceUtil.addNat [{}] 新增[{}]下实例号", deviceId, natPath);
		AddOBJ obj = acsCorba.add(deviceId, natPath);
		if (obj.getStatus() != 1 && obj.getStatus() != 0) {
			logger.warn("VxlanOperateDeviceUtil.addNat [{}] 新增" + natPath + "下实例号失败", deviceId);
			return pubIpv4;
		}
		pubIpv4 = setNat(deviceId, natPath + obj.getInstance(), map, natType, interfaceStr);
		return StringUtil.IsEmpty(pubIpv4) ? "" : pubIpv4;
	}
	
	/**
	 * nat节点赋值
	 * @param acsCorba
	 * @param deviceId
	 * @param natPathWithIndex
	 * @param map
	 * @param natType
	 * @param interfaceStr
	 * @return
	 */
	public static String setNat(String deviceId, String natPathWithIndex,
			Map<String, String> map, String natType, String interfaceStr){
		String pubIpv4 = StringUtil.getStringValue(map, "pubIpv4");
		String privIpv4 = StringUtil.getStringValue(map, "privIpv4");
		if (StringUtil.IsEmpty(pubIpv4) || StringUtil.IsEmpty(privIpv4)) {
			logger.warn("VxlanOperateDeviceUtil.setNat失败 deviceId[{}]pubIpv4[{}] privIpv4[{}]为空", deviceId, pubIpv4, privIpv4);
			return pubIpv4;
		}
		String protocol = StringUtil.getStringValue(map, "protocol");
		if ("2".equals(natType) && StringUtil.IsEmpty(protocol)) {
			logger.warn("VxlanOperateDeviceUtil.setNat失败 deviceId[{}]pubIpv4[{}]protocol[{}]为空", deviceId, pubIpv4, protocol);
			return pubIpv4;
		}
		
		String privPort = StringUtil.getStringValue(map, "privPort");
		String pubPort = StringUtil.getStringValue(map, "pubPort");
		/** 参数类型：
		 * 1 string
		 * 2 int
		 * 3 unsignedInt
		 * 4 boolean
		 */
		String node = natPathWithIndex + ".";
		logger.warn("[{}]给实例[{}]下参数", deviceId, node);
		ArrayList<ParameValueOBJ> parameList = new ArrayList<ParameValueOBJ>();
		
		ParameValueOBJ objType = new ParameValueOBJ();
		objType.setName(node + "Type");
		objType.setValue(natType);
		objType.setType("1");
		parameList.add(objType);
		
		ParameValueOBJ objInterface = new ParameValueOBJ();
		objInterface.setName(node + "Interface");
		objInterface.setValue(interfaceStr);
		objInterface.setType("1");
		parameList.add(objInterface);
		// 私网IP地址
		ParameValueOBJ objInternalAddress = new ParameValueOBJ();
		objInternalAddress.setName(node + "InternalAddress");
		objInternalAddress.setValue(privIpv4);
		objInternalAddress.setType("1");
		parameList.add(objInternalAddress);
		// 公网IP地址
		ParameValueOBJ objExternalAddress = new ParameValueOBJ();
		objExternalAddress.setName(node + "ExternalAddress");
		objExternalAddress.setValue(pubIpv4);
		objExternalAddress.setType("1");
		parameList.add(objExternalAddress);
		if ("2".equals(natType)) {
			// 地址转换的协议
			ParameValueOBJ objProtocol = new ParameValueOBJ();
			objProtocol.setName(node + "Protocol");
			objProtocol.setValue(protocol);
			objProtocol.setType("1");
			parameList.add(objProtocol);
			// 私网IP地址对应映射端口
			ParameValueOBJ objInternalPort = new ParameValueOBJ();
			objInternalPort.setName(node + "InternalPort");
			objInternalPort.setValue(privPort);
			objInternalPort.setType("3");
			if (!StringUtil.IsEmpty(privPort)) {
				parameList.add(objInternalPort);
			}
			// 公网ip地址对应映射端口
			ParameValueOBJ objExternalPort = new ParameValueOBJ();
			objExternalPort.setName(node + "ExternalPort");
			objExternalPort.setValue(pubPort);
			objExternalPort.setType("3");
			if (!StringUtil.IsEmpty(pubPort)) {
				parameList.add(objExternalPort);
			}
		}
		
		int setResult = acsCorba.setValue(deviceId, parameList);
		if (setResult != 1 && setResult != 0) {
			logger.warn("[{}]下发参数失败：[{}]", deviceId, setResult);
			return pubIpv4;
		}
		return "";
	}
	
	/**
	 * 获取nat intferface节点值
	 * @param acsCorba
	 * @param deviceId
	 * @return
	 */
	public static String getNatInterface(String deviceId){
		logger.warn("采集nat interface 参数，device_id={}", deviceId);
		String ret = "";
		String vxlanRootPath = "InternetGatewayDevice.X_CT-COM_VXLAN.VXLANConfig.";
		//　采集vxlan下节点
		ArrayList<String> vxlanList = acsCorba.getParamNamesPath(deviceId, vxlanRootPath, 0);
		logger.warn("vxlanList :[{}]", vxlanList);
		
		if (vxlanList == null || vxlanList.isEmpty() || (vxlanList.size() == 1 
				&& vxlanList.get(0).equals(vxlanRootPath))) {
			logger.warn("[{}]不存在vxlan参数节点", deviceId);
			return ret;
		}
		// 采集vxlan下节点
		ArrayList<String> paramNameList = new ArrayList<String>();
		for (String vxlanPath : vxlanList) {
			if (vxlanPath.contains(".Enable") 
					|| vxlanPath.contains(".WorkMode")
					|| vxlanPath.contains(".NATEnabled")) {
				paramNameList.add(vxlanPath);
			}
		}
				
		// Collections.reverse(paramNameList);
		// logger.warn("[{}]paramNameList", paramNameList);
		String[] paramNametemp = paramNameList.toArray(new String[paramNameList.size()]);
		Map<String, String> paramValueMap = acsCorba.getParaValueMap(deviceId, paramNametemp);
		if (paramValueMap == null || paramValueMap.isEmpty()) {
			logger.warn("[{}]采集nat interface 参数节点失败", deviceId);
			return ret;
		}
		logger.warn("[{}] 采集nat interface 参数节点结果：[{}]", deviceId, paramValueMap);
		paramNameList = new ArrayList<String>();
		for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
			if (entry.getKey().contains(".Enable") && "1".equals(entry.getValue())) {
				paramNameList.add(entry.getKey().replace(".Enable", ""));
			}
		}
		ArrayList<String> interfaceList = new ArrayList<String>();
		for (String node : paramNameList) {
			// 三层模式(路由)且nat启用
			if ("2".equals(paramValueMap.get(node + ".WorkMode")) && "1".equals(paramValueMap.get(node + ".NATEnabled"))) {
				interfaceList.add(node);
			}
		}
		if (interfaceList == null || interfaceList.isEmpty()) {
			logger.warn("[{}]nat interface 参数节点没有符合的结果", deviceId);
			return ret;
		}
		Collections.sort(interfaceList);
		ret = interfaceList.get(0);
		logger.warn("[{}] 采集nat interface 参数节点为：[{}]", deviceId, ret);
		return ret;
	}
	
	/**
	 * 新增nat节点
	 * @param acsCorba
	 * @param deviceId
	 * @param natPath
	 * @param map
	 * @param natType
	 * @param interfaceStr
	 * @return
	 */
	public static boolean addForwarding(String deviceId, String forwardingPath, String ipMask[], CloudStaticRtCfgChecker checker) {
		logger.warn("VxlanOperateDeviceUtil.addForwarding [{}] 新增[{}]下实例号", deviceId, forwardingPath);
		AddOBJ obj = acsCorba.add(deviceId, forwardingPath);
		if (obj.getStatus() != 1 && obj.getStatus() != 0) {
			logger.warn("VxlanOperateDeviceUtil.addForwarding [{}] 新增[{}]下实例号失败", deviceId, forwardingPath);
			checker.setResult(1003);
			checker.setResultDesc("新增InternetGatewayDevice.Layer3Forwarding.Forwarding.下实例号失败");
			return false;
		}
		return setForwarding(deviceId, forwardingPath + obj.getInstance(), ipMask[0], ipMask[1], checker);
	}
	
	/**
	 * nat节点赋值
	 * @param acsCorba
	 * @param deviceId
	 * @param natPathWithIndex
	 * @param map
	 * @param natType
	 * @param interfaceStr
	 * @return
	 */
	public static boolean setForwarding(String deviceId, String forwardPathWithIndex,
			String ip, String mask, CloudStaticRtCfgChecker checker){
		String node = forwardPathWithIndex + ".";
		logger.warn("[{}]给实例[{}]下参数", deviceId, node);
		ArrayList<ParameValueOBJ> parameList = new ArrayList<ParameValueOBJ>();
		
		ParameValueOBJ objEnable = new ParameValueOBJ();
		objEnable.setName(node + "Enable");
		objEnable.setValue("1");
		objEnable.setType("4");
		
		ParameValueOBJ objDestIPAddress = new ParameValueOBJ();
		objDestIPAddress.setName(node + "DestIPAddress");
		objDestIPAddress.setValue(ip);
		objDestIPAddress.setType("1");
		
		ParameValueOBJ objDestSubnetMask = new ParameValueOBJ();
		objDestSubnetMask.setName(node + "DestSubnetMask");
		objDestSubnetMask.setValue(mask);
		objDestSubnetMask.setType("1");
		
		ParameValueOBJ objNextHopType = new ParameValueOBJ();
		objNextHopType.setName(node + "NextHopType");
		objNextHopType.setValue(isIP(checker.getNextHop()) ? "1" : "2");
		objNextHopType.setType("1");
		
		ParameValueOBJ objNexthop = new ParameValueOBJ();
		objNexthop.setName(node + "Nexthop");
		objNexthop.setValue(checker.getNextHop());
		objNexthop.setType("1");
		
		ParameValueOBJ objForwardingMetric = new ParameValueOBJ();
		objForwardingMetric.setName(node + "ForwardingMetric");
		objForwardingMetric.setValue(checker.getPriority());
		objForwardingMetric.setType("2");
		
		parameList.add(objEnable);
		parameList.add(objDestIPAddress);
		parameList.add(objDestSubnetMask);
		parameList.add(objNextHopType);
		parameList.add(objNexthop);
		// 若值为空则不用设置
		if (!StringUtil.IsEmpty(checker.getPriority())) {
			parameList.add(objForwardingMetric);
		}
		int setResult = acsCorba.setValue(deviceId, parameList);
		if (setResult != 1 && setResult != 0) {
			logger.warn("servicename[CloudStaticRtCfgService][{}]下发参数失败：[{}]", deviceId, setResult);
			checker.setResult(1003);
			checker.setResultDesc("设备下发参数失败");
			return false;
		}
		return true;
	}
	
	/**
	 * 判断是否为ip
	 * @param addr
	 * @return
	 */
	public static boolean isIPMask(String addr) {
		if(addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {  
			return false;  
		}
		if ("0.0.0.0/0".equals(addr)) {
			return true;
		}
		String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}(\\/(?:[1-9]|[12][0-9]|3[012])$)";  
		Pattern pat = Pattern.compile(rexp);    
		Matcher mat = pat.matcher(addr);    

		return mat.find();  
     }
	
	/**
	 * 判断是否为ip
	 * @param addr
	 * @return
	 */
	public static boolean isIP(String addr) {  
		if(addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {  
			return false;  
		}  
		String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";  
		Pattern pat = Pattern.compile(rexp);    
		Matcher mat = pat.matcher(addr);    

		return mat.find();  
     }
	
	/**
	 * 解析ip mask
	 * @param param
	 * @return
	 */
	public static String[] getIpMask(String param) {
		logger.warn("需要解析的ip为[{}]", param);
		String[] ret = {"", ""};
		if (StringUtil.IsEmpty(param)) {
			return ret;
		}
		String[] _param = param.split("/");
		ret[0] = _param[0];

		try {
			int mask = Integer.parseInt(_param[1]);
			if (mask < 0 || mask > 32) {
				throw new Exception("mask值不正确");
			}
			StringBuffer maskStr = new StringBuffer(MASK_STR.substring(0, mask));
			while (maskStr.length() < 32) {
				maskStr.append("0");
			}
			StringBuffer out = new StringBuffer();
			for (int n = 0; n < 4; n++) {
				out.append(Integer.valueOf(maskStr.substring(n*8, (n+1)*8), 2)).append(".");
			}
			ret[1] = out.substring(0, out.length() -1);
		} catch (Exception e) {
			logger.error("[{}]解析掩码失败[{}]", param, e.getMessage());
			ret[1] = "";
		}
		logger.warn("ip[{}]解析为[{}],[{}]", param, ret[0], ret[1]);
		return ret;
	}
	
	/**
	 * 掩码转数字
	 * @param param
	 * @return
	 */
	public static String maskToInt(String param) {
		logger.warn("需要解析的ip为[{}]", param);
		String mask = "";
		int number = 0;
		if (StringUtil.IsEmpty(param)) {
			return mask;
		}
		String[] _param = param.split("\\.");

		if (_param == null || _param.length < 4) {
			return mask;
		}
		try {
			for (String p : _param) {
				mask += Integer.toBinaryString(Integer.parseInt(p));
			}
			char[] chars = mask.toCharArray();
			for(int i = 0; i < chars.length; i++) {
			    if('1' == chars[i]) {
			     number ++;
			    }
			}
			mask = String.valueOf(number);
		} catch (Exception e) {
			logger.error("[{}]解析掩码失败[{}]", param, e.getMessage());
		}
		logger.warn("mask[{}]解析为[{}]", param, mask);
		return mask;
	}
	public static void main(String[] args) {
		getIpMask("0.0.0.0/0");
		maskToInt("255.255.0.0");
	}
}
