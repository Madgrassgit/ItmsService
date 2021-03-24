
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.IpsecServParamDAO;
import com.linkage.itms.dispatch.util.VxlanOperateDeviceUtil;
import com.linkage.itms.obj.AddOBJ;
import com.linkage.itms.obj.ParameValueOBJ;

public class CloudStaticRtCfgAllService
{

	// 日志
	private static final Logger logger = LoggerFactory
			.getLogger(CloudStaticRtCfgAllService.class);
	private String forwardingRootPath = "InternetGatewayDevice.Layer3Forwarding.Forwarding.";
	IpsecServParamDAO ipsDao = new IpsecServParamDAO();
	private static ACSCorba acsCorba = new ACSCorba();
	private static ArrayList<ParameValueOBJ> parameList = new ArrayList<ParameValueOBJ>();

	public boolean work(String deviceId, Long userId)
	{
		// 校验设备是否在线可操作
		if (!deviceisBusy(deviceId))
		{
			return false;
		}
		// 采集nat下节点
		logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
		ArrayList<String> forwardingPathsList = acsCorba.getParamNamesPath(deviceId,
				forwardingRootPath, 0);
		logger.warn("forwardingPathsList :[{}]", forwardingPathsList);
		ArrayList<HashMap<String, String>> staticIpList = ipsDao.getForwardingIP(userId);
		// 设备上不存在路由实例，做新增业务
		if (forwardingPathsList == null
				|| (forwardingPathsList.size() == 1 && forwardingPathsList.get(0).equals(
						forwardingRootPath)))
		{
			for (int i = 0; i < staticIpList.size(); i++)
			{
				logger.warn("VxlanOperateDeviceUtil.addForwarding [{}] 新增[{}]下全部实例号",
						deviceId, forwardingRootPath);
				AddOBJ obj = acsCorba.add(deviceId, forwardingRootPath);
				if (obj.getStatus() != 1 && obj.getStatus() != 0)
				{
					logger.warn("VxlanOperateDeviceUtil.addForwarding [{}] 新增[{}]下实例号失败",
							deviceId, forwardingRootPath);
					return false;
				}
			}
		}
		else if (forwardingPathsList != null
				&& forwardingPathsList.size() < staticIpList.size())
		{
			for (int i = 0; i < (staticIpList.size() - forwardingPathsList.size()); i++)
			{
				logger.warn("VxlanOperateDeviceUtil.addForwarding [{}] 新增[{}]下缺少部分实例号",
						deviceId, forwardingRootPath);
				AddOBJ obj = acsCorba.add(deviceId, forwardingRootPath);
				if (obj.getStatus() != 1 && obj.getStatus() != 0)
				{
					logger.warn("VxlanOperateDeviceUtil.addForwarding [{}] 新增[{}]下实例号失败",
							deviceId, forwardingRootPath);
					return false;
				}
			}
		}
		// 覆盖设置所有路由信息
		for (int i = 0; i < staticIpList.size(); i++)
		{
			String[] ipMask = VxlanOperateDeviceUtil.getIpMask(StringUtil.getStringValue(
					staticIpList.get(i), "des_ip"));
			setForwarding(deviceId, forwardingRootPath + (i + 1), ipMask[0], ipMask[1],
					staticIpList.get(i));
		}
		int setResult = acsCorba.setValue(deviceId, parameList);
		if (setResult != 1 && setResult != 0)
		{
			return false;
		}
		return true;
	}

	/**
	 * 设备是否在线
	 * 
	 * @param deviceId
	 * @param corba
	 * @param checker
	 * @return
	 */
	private boolean deviceisBusy(String deviceId)
	{
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
		logger.warn("设备[{}],在线状态[{}] ", new Object[] { deviceId, flag });
		// 设备正在被操作，不能获取节点值
		if (-3 == flag)
		{
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			return false;
		}
		// 设备不在线，不能获取节点值
		if (1 != flag)
		{
			logger.warn("设备不在线，无法获取节点值");
			return false;
		}
		return true;
	}

	/**
	 * nat节点赋值
	 * 
	 * @param acsCorba
	 * @param deviceId
	 * @param natPathWithIndex
	 * @param map
	 * @param natType
	 * @param interfaceStr
	 * @return
	 */
	public static void setForwarding(String deviceId, String forwardPathWithIndex,
			String ip, String mask, HashMap<String, String> staticIpMap)
	{
		String node = forwardPathWithIndex + ".";
		logger.warn("[{}]给实例[{}]下参数", deviceId, node);
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
		objNextHopType
				.setValue(isIP(StringUtil.getStringValue(staticIpMap, "next_hop")) ? "1"
						: "2");
		objNextHopType.setType("1");
		ParameValueOBJ objNexthop = new ParameValueOBJ();
		objNexthop.setName(node + "Nexthop");
		objNexthop.setValue(StringUtil.getStringValue(staticIpMap, "next_hop"));
		objNexthop.setType("1");
		ParameValueOBJ objForwardingMetric = new ParameValueOBJ();
		objForwardingMetric.setName(node + "ForwardingMetric");
		objForwardingMetric.setValue(StringUtil.getStringValue(staticIpMap, "priority"));
		objForwardingMetric.setType("2");
		parameList.add(objEnable);
		parameList.add(objDestIPAddress);
		parameList.add(objDestSubnetMask);
		parameList.add(objNextHopType);
		parameList.add(objNexthop);
		// 若值为空则不用设置
		if (!StringUtil.IsEmpty(StringUtil.getStringValue(staticIpMap, "priority")))
		{
			parameList.add(objForwardingMetric);
		}
	}

	/**
	 * 判断是否为ip
	 * 
	 * @param addr
	 * @return
	 */
	public static boolean isIP(String addr)
	{
		if (addr.length() < 7 || addr.length() > 15 || "".equals(addr))
		{
			return false;
		}
		String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
		Pattern pat = Pattern.compile(rexp);
		Matcher mat = pat.matcher(addr);
		return mat.find();
	}
}
