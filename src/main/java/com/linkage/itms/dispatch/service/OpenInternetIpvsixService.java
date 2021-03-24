
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.OpenInternetIpvsixChecker;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * 可通过输入用户账号（逻辑SN、宽带账号)开通上网IPV6功能接口(jl_dx)
 * 
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-7-26
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class OpenInternetIpvsixService implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(OpenInternetIpvsixService.class);
	/** 单栈ipv4 */
	private static int SINGLE_STACK_IP_FOUR = 1;
	/** 单栈 ipv6 */
	private static int SINGLE_STACK_IP_SIX = 2;
	/** 双栈 */
	private static int DOUBLE_STACK = 3;

	@Override
	public String work(String inXml)
	{
		logger.warn("getCustomerTypeOpenService：inXml({})", inXml);
		OpenInternetIpvsixChecker checker = new OpenInternetIpvsixChecker(inXml);
		if (false == checker.check())
		{
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		String deviceId = "";
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			if (6 == checker.getUserInfoType())
			{
				logger.warn("此设备不存在：" + checker.getUserInfo());
				checker.setResult(1004);
				checker.setResultDesc("此设备不存在");
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
						"CustomerTypeOpenService");
				return checker.getReturnXml();
			}
			else
			{
				logger.warn("无此用户信息：" + checker.getUserInfo());
				checker.setResult(1001);
				checker.setResultDesc("无此用户信息");
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
						"CustomerTypeOpenService");
				return checker.getReturnXml();
			}
		}
		else
		{
			deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");
			if ("".equals(deviceId))
			{
				checker.setResult(1002);
				checker.setResultDesc("此用户未绑定");
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
						"CustomerTypeOpenService");
				return checker.getReturnXml();
			}
			else
			{
				// 判断设备是否在线，只有设备在线，才可以设置设备的节点信息
				GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
				ACSCorba corba = new ACSCorba();
				int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
				// 设备正在被操作，不能设置节点值
				if (-3 == flag)
				{
					logger.warn("设备正在被操作，无法设置节点值，device_id={}", deviceId);
					checker.setResult(1008);
					checker.setResultDesc("设备正在被操作");
					logger.warn("return=({})", checker.getReturnXml()); // 打印回参
					new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
							"CustomerTypeOpenService");
					return checker.getReturnXml();
				}
				// 设备在线
				else if (1 == flag)
				{
					logger.warn("设备在线，可以设置节点值，device_id={}", deviceId);
					int retResult = StackRefresh(deviceId, checker.getIpMode());
					logger.warn("return result is : " + retResult);
					if (0 == retResult || 1 == retResult)
					{
						String account = StringUtil.getStringValue(
								queryAccountByLoid(StringUtil.getStringValue(userInfoMap,
										"username")), "username");
						int updateResult = updateHgwInfo(
								StringUtil.getStringValue(checker.getIpMode()),
								StringUtil.getLongValue(userInfoMap, "user_id"), account);
						logger.warn("update result is : " + updateResult + " ipMode : "
								+ checker.getIpMode() + " account : " + account);
						checker.setResult(0);
						checker.setResultDesc("成功");
						logger.warn("return=({})", checker.getReturnXml());
						new RecordLogDAO().recordDispatchLog(checker,
								checker.getUserInfo(), "CustomerTypeOpenService");
						return checker.getReturnXml();
					}
					else if (-1 == retResult)
					{
						checker.setResult(1009);
						checker.setResultDesc("设备连接失败");
						logger.warn("return=({})", checker.getReturnXml());
						new RecordLogDAO().recordDispatchLog(checker,
								checker.getUserInfo(), "CustomerTypeOpenService");
						return checker.getReturnXml();
					}
					else if (-6 == retResult)
					{
						checker.setResult(1010);
						checker.setResultDesc("设备正被操作");
						logger.warn("return=({})", checker.getReturnXml());
						new RecordLogDAO().recordDispatchLog(checker,
								checker.getUserInfo(), "CustomerTypeOpenService");
						return checker.getReturnXml();
					}
					else if (-7 == retResult)
					{
						checker.setResult(1011);
						checker.setResultDesc("系统参数错误");
						logger.warn("return=({})", checker.getReturnXml());
						new RecordLogDAO().recordDispatchLog(checker,
								checker.getUserInfo(), "CustomerTypeOpenService");
						return checker.getReturnXml();
					}
					else if (-9 == retResult)
					{
						checker.setResult(1012);
						checker.setResultDesc("系统内部错误");
						logger.warn("return=({})", checker.getReturnXml());
						new RecordLogDAO().recordDispatchLog(checker,
								checker.getUserInfo(), "CustomerTypeOpenService");
						return checker.getReturnXml();
					}
					else
					{
						checker.setResult(1013);
						checker.setResultDesc("tr069错误");
						logger.warn("return=({})", checker.getReturnXml());
						new RecordLogDAO().recordDispatchLog(checker,
								checker.getUserInfo(), "CustomerTypeOpenService");
						return checker.getReturnXml();
					}
				}
				// 设备不在线，不能获取节点值
				else
				{
					logger.warn("设备不在线，无法获取节点值");
					checker.setResult(1006);
					checker.setResultDesc("设备不在线");
					logger.warn("return=({})", checker.getReturnXml()); // 打印回参
					return checker.getReturnXml();
				}
			}
		}
	}

	private List<String> gatherNetIJList(String deviceId)
	{
		// 江苏可以根据wan连接索引节点来生成上网通道
		List<String> ijList = new ArrayList<String>();
		/**
		 * 快速采集代码 ijList = GatherNetWanIndex.gatherNetIJList(deviceId).get("internet"); if
		 * (!ijList.isEmpty()) { return ijList; }
		 **/
		// "1.1;DHCP_Routed;45;TR069","3.1;Bridged;43;OTHER","4.1;DHCP_Routed;42;VOIP","5.1;PPPoE_Routed;312;INTERNET"
		// 获取不到走ijk
		logger.warn("走ijk采集", deviceId);
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		ArrayList<String> wanConnPathsList = null;
		wanConnPathsList = new ACSCorba().getParamNamesPath(deviceId, wanConnPath, 0);
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty())
		{
			logger.warn("[{}]获取WANConnectionDevice下所有节点路径失败，逐层获取", deviceId);
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = new ACSCorba().getIList(deviceId, wanConnPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty())
			{
				logger.warn("[{}]获取" + wanConnPath + "下实例号失败", deviceId);
			}
			else
			{
				for (String j : jList)
				{
					List<String> kPPPList = new ACSCorba().getIList(deviceId, wanConnPath
							+ j + wanPPPConnection);
					if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
					{
						logger.warn("[{}]获取" + wanConnPath + wanConnPath + j
								+ wanPPPConnection + "下实例号失败", deviceId);
					}
					else
					{
						for (String kppp : kPPPList)
						{
							wanConnPathsList.add(wanConnPath + j + wanPPPConnection
									+ kppp + wanServiceList);
						}
					}
				}
			}
		}
		ArrayList<String> serviceListList = new ArrayList<String>();
		ArrayList<String> paramNameList = new ArrayList<String>();
		for (int i = 0; i < wanConnPathsList.size(); i++)
		{
			String namepath = wanConnPathsList.get(i);
			if (namepath.indexOf(wanServiceList) >= 0)
			{
				serviceListList.add(namepath);
				paramNameList.add(namepath);
				continue;
			}
		}
		if (serviceListList.size() == 0 || serviceListList.isEmpty())
		{
			logger.warn("[{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回", deviceId);
		}
		else
		{
			String[] paramNameArr = new String[paramNameList.size()];
			int arri = 0;
			for (String paramName : paramNameList)
			{
				paramNameArr[arri] = paramName;
				arri = arri + 1;
			}
			Map<String, String> paramValueMap = new HashMap<String, String>();
			for (int k = 0; k < (paramNameArr.length / 20) + 1; k++)
			{
				String[] paramNametemp = new String[paramNameArr.length - (k * 20) > 20 ? 20
						: paramNameArr.length - (k * 20)];
				for (int m = 0; m < paramNametemp.length; m++)
				{
					paramNametemp[m] = paramNameArr[k * 20 + m];
				}
				Map<String, String> maptemp = new ACSCorba().getParaValueMap(deviceId,
						paramNametemp);
				if (maptemp != null && !maptemp.isEmpty())
				{
					paramValueMap.putAll(maptemp);
				}
				logger.warn("获取节点值...");
				logger.warn("k : " + k);
			}
			if (paramValueMap.isEmpty())
			{
				logger.warn("[{}]获取ServiceList失败", deviceId);
			}
			for (Map.Entry<String, String> entry : paramValueMap.entrySet())
			{
				logger.debug("[{}]{}={} ",
						new Object[] { deviceId, entry.getKey(), entry.getValue() });
				String paramName = entry.getKey();
				if (paramName.indexOf(wanPPPConnection) >= 0)
				{
				}
				else if (paramName.indexOf(wanIPConnection) >= 0)
				{
					continue;
				}
				if (paramName.indexOf(wanServiceList) >= 0)
				{
					if (!StringUtil.IsEmpty(entry.getValue())
							&& entry.getValue().indexOf("INTERNET") >= 0)
					{
						logger.warn("param path is .." + entry.getKey());
						String res = entry.getKey().substring(0,
								entry.getKey().indexOf("X_CT-COM_ServiceList"));
						try
						{
							String i = res.split("WANConnectionDevice.")[1]
									.split(".WANPPPConnection")[0];
							String j = res.split("WANPPPConnection.")[1].split("\\.")[0];
							ijList.add((i + "##" + j));
							logger.warn("i is : " + i + " j is : " + j);
						}
						catch (Exception e)
						{
						}
					}
				}
			}
		}
		return ijList;
	}

	/**
	 * 先走快速，获取不到走ijk 预读失败，程序终止：返回-10
	 * 
	 * @param deviceId
	 * @param subServiceId
	 * @return
	 */
	private int StackRefresh(String deviceId, int subServiceId)
	{
		String wanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String pppcPath = ".WANPPPConnection.";
		String IPMode = ".X_CT-COM_IPMode";
		String IPv6IPAddressOrigin = ".X_CT-COM_IPv6IPAddressOrigin";
		String IPv6PrefixOrigin = ".X_CT-COM_IPv6PrefixOrigin";
		String IPv6PrefixDelegationEnabled = ".X_CT-COM_IPv6PrefixDelegationEnabled";
		List<String> ijList = gatherNetIJList(deviceId);
		if (null == ijList || ijList.isEmpty())
		{
			logger.warn("[{}][{}]预读失败，程序终止", deviceId, subServiceId);
			logger.error("[{}][{}]预读失败，程序终止", deviceId, subServiceId);
			return -10;
		}
		ArrayList<ParameValueOBJ> parameList = new ArrayList<ParameValueOBJ>();
		if (SINGLE_STACK_IP_FOUR == subServiceId)
		{
			for (int i = 0; i < ijList.size(); i++)
			{
				String[] ijArr = ijList.get(i).split("##");
				String pathTmp = wanPath + ijArr[0] + pppcPath + ijArr[1];
				ParameValueOBJ pvObjIPMode = new ParameValueOBJ();
				pvObjIPMode.setName(pathTmp + IPMode);
				pvObjIPMode.setValue("1");
				pvObjIPMode.setType("1");
				parameList.add(pvObjIPMode);
			}
		}
		else
		{
			for (int i = 0; i < ijList.size(); i++)
			{
				String[] ijArr = ijList.get(i).split("##");
				String pathTmp = wanPath + ijArr[0] + pppcPath + ijArr[1];
				ParameValueOBJ pvObjIPMode = new ParameValueOBJ();
				pvObjIPMode.setName(pathTmp + IPMode);
				if (DOUBLE_STACK == subServiceId)
				{
					pvObjIPMode.setValue("3");
				}
				else if (SINGLE_STACK_IP_SIX == subServiceId)
				{
					pvObjIPMode.setValue("2");
				}
				pvObjIPMode.setType("1");
				ParameValueOBJ pvObjIPv6IPAddressOrigin = new ParameValueOBJ();
				pvObjIPv6IPAddressOrigin.setName(pathTmp + IPv6IPAddressOrigin);
				pvObjIPv6IPAddressOrigin.setValue("AutoConfigured");
				pvObjIPv6IPAddressOrigin.setType("1");
				ParameValueOBJ pvObjIPv6PrefixOrigin = new ParameValueOBJ();
				pvObjIPv6PrefixOrigin.setName(pathTmp + IPv6PrefixOrigin);
				pvObjIPv6PrefixOrigin.setValue("PrefixDelegation");
				pvObjIPv6PrefixOrigin.setType("1");
				ParameValueOBJ pvObjIPv6PrefixDelegationEnabled = new ParameValueOBJ();
				pvObjIPv6PrefixDelegationEnabled.setName(pathTmp + IPv6PrefixDelegationEnabled);
				pvObjIPv6PrefixDelegationEnabled.setValue("1");
				pvObjIPv6PrefixDelegationEnabled.setType("4");
				parameList.add(pvObjIPMode);
				parameList.add(pvObjIPv6IPAddressOrigin);
				parameList.add(pvObjIPv6PrefixOrigin);
				parameList.add(pvObjIPv6PrefixDelegationEnabled);
			}
		}
		ACSCorba acsCorba = new ACSCorba();
		int result = acsCorba.setValue(deviceId, parameList);
		return result;
	}

	private int updateHgwInfo(String ipIype, long userId, String username)
	{
		PrepareSQL psql = new PrepareSQL();
		psql = new PrepareSQL(" update tab_net_serv_param set ip_type=" + ipIype);
		psql.append(" where user_id=" + userId);
		psql.append(" and serv_type_id=10 and username='" + username + "'");
		return DBOperation.executeUpdate(psql.getSQL());
	}

	/**
	 * 根据loid查询业务账号
	 * 
	 * @param
	 * @return
	 */
	public Map<String, String> queryAccountByLoid(String loid)
	{
		PrepareSQL pSQL = new PrepareSQL();
		pSQL.append(" select a.username from hgwcust_serv_info a, tab_hgwcustomer b ");
		pSQL.append(" where a.user_id=b.user_id and a.serv_type_id=10 and a.serv_status=1 and b.username = ?  ");
		pSQL.setString(1, loid);
		return DBOperation.getRecord(pSQL.getSQL());
	}
}
