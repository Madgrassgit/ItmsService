
package com.linkage.itms.dispatch.service;

import com.linkage.commom.util.CheckStrategyUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.BridgeToRoutChecker;
import com.linkage.itms.dispatch.util.ChangeConnectionNetForAHCorba;
import com.linkage.itms.dispatch.util.SocketUtil;
import com.linkage.itms.dispatch.util.encryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 江西需求，路由桥接相互转化。JXDX-ITMS-REQ-20140314-LINBX-001
 * 
 * @author xiangzl (Ailk No.)
 * @version 1.0
 * @since 2014-3-25
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class BridgeToRoutService4Jx implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(BridgeToRoutService4Jx.class);
	private static final String ONE = "1";
	private static final String USER_ID = "user_id";
	private static final String DEVICE_ID = "device_id";
	private static final String WAN_TYPE = "wan_type";
	private static final String USERNAME = "username";
	private static int three = 3;
	private static int two = 2;

	@Override
	public String work(String inXml)
	{
		BridgeToRoutChecker bridgeToRoutChecker = new BridgeToRoutChecker(inXml);
		Map<String, String> userInfoMap = null;
		// 验证入参格式是否正确
		if (!bridgeToRoutChecker.check())
		{
			logger.error(
					"servicename[BridgeToRoutService4JX]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					bridgeToRoutChecker.getCmdId(), bridgeToRoutChecker.getUserInfo(),
					bridgeToRoutChecker.getReturnXml());
			return bridgeToRoutChecker.getReturnXml();
		}
		logger.warn(
				"servicename[BridgeToRoutService4JX]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { bridgeToRoutChecker.getCmdId(),
						bridgeToRoutChecker.getUserInfo(), inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 查询用户信息
		Map<String, String> netInfoMap = null;
		userInfoMap = userDevDao.queryUserInfo(bridgeToRoutChecker.getUserInfoType(),
				bridgeToRoutChecker.getUserInfo(), bridgeToRoutChecker.getCityId());
		// 此用户不存在 不能做配置下发，返回失败信息
		if (null == userInfoMap || userInfoMap.isEmpty()
				|| StringUtil.IsEmpty(userInfoMap.get(USER_ID)))
		{
			logger.warn("servicename[BridgeToRoutService4JX]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { bridgeToRoutChecker.getCmdId(),
							bridgeToRoutChecker.getUserInfo() });
			bridgeToRoutChecker.setResult(1002);
			bridgeToRoutChecker.setResultDesc("查无此用户");
			bridgeToRoutChecker.setUserSn("");
			// 用户不存在
			bridgeToRoutChecker.setFailureReason("1");
			// 失败
			bridgeToRoutChecker.setSuccStatus("-1");
		}
		else
		{
			if(Global.JXDX.equals(Global.G_instArea))
			{
				dealUserInfoMap(userInfoMap,userDevDao);
			}
			// (江西)判断设备是否繁忙或者业务正在下发
			if (Global.JXDX.equals(Global.G_instArea) && false == CheckStrategyUtil
					.chechStrategy(userInfoMap.get(DEVICE_ID)))
			{
				logger.warn(
						"servicename[BridgeToRoutService4JX]cmdId[{}]userinfo[{}]设备繁忙或者业务正在下发，请稍候重试",
						bridgeToRoutChecker.getCmdId(),
						bridgeToRoutChecker.getUserInfo());
				bridgeToRoutChecker.setResult(1002);
				bridgeToRoutChecker.setResultDesc("设备繁忙或者业务正在下发，请稍候重试");
				bridgeToRoutChecker.setUserSn("");
				bridgeToRoutChecker.setFailureReason("");
				// 失败
				bridgeToRoutChecker.setSuccStatus("-1");
				if (Global.JXDX.equals(Global.G_instArea))
				{
					String wanType = StringUtil
							.getStringValue(userInfoMap.get(WAN_TYPE));
					String operType = "";
					if (bridgeToRoutChecker.getOperateType().equals(wanType))
					{
						if (ONE.equals(bridgeToRoutChecker.getOperateType()))
						{
							operType = "3";
						}
						else
						{
							operType = "4";
						}
					}
					else
					{
						operType = bridgeToRoutChecker.getOperateType();
					}
					new RecordLogDAO().recordRouteAndBridge(bridgeToRoutChecker,
							userInfoMap, "2", "设备繁忙或者业务正在下发，请稍候重试", operType);
				}
			}
			else if (Global.AHDX.equals(Global.G_instArea))
			{
				String res = getDevNetType(userInfoMap.get(DEVICE_ID), "1");
				String[] result = null;
				if (!StringUtil.IsEmpty(res))
				{
					result = res.split(";");
				}
				if (StringUtil.IsEmpty(res) || res.length() < three || result == null || !ONE.equals(result[0]))
				{
					logger.warn(
							"servicename[BridgeToRoutService4JX]cmdId[{}]userinfo[{}]获取路径失败",
							bridgeToRoutChecker.getCmdId(),
							bridgeToRoutChecker.getUserInfo());
					bridgeToRoutChecker.setResult(1005);
					bridgeToRoutChecker.setResultDesc("获取路径失败");
					bridgeToRoutChecker.setUserSn("");
					bridgeToRoutChecker.setFailureReason(result == null ? "" : result[1]);
					// 失败
					bridgeToRoutChecker.setSuccStatus("-1");
				}
				else
				{
					int code = changeConnectionTypeForAh(userInfoMap.get(DEVICE_ID),
							"2", bridgeToRoutChecker.getRoutUsername(),
							bridgeToRoutChecker.getRoutPassword(), result[2], "1");
					if (code == 1 || code == 0)
					{
						bridgeToRoutChecker.setResult(0);
						bridgeToRoutChecker.setResultDesc("成功");
						bridgeToRoutChecker.setUserSn(
								StringUtil.getStringValue(userInfoMap.get(USERNAME)));
						bridgeToRoutChecker.setFailureReason("0");
						// 成功
						bridgeToRoutChecker.setSuccStatus("1");
					}
					else
					{
						bridgeToRoutChecker.setResult(1006);
						bridgeToRoutChecker.setResultDesc("下发失败");
						bridgeToRoutChecker.setUserSn("");
						// 用户不存在
						bridgeToRoutChecker.setFailureReason("3");
						// 失败
						bridgeToRoutChecker.setSuccStatus("-1");
					}
				}
			}
			else
			{
				// (吉林)判断设备是否繁忙或者业务正在下发
				if (Global.JLDX.equals(Global.G_instArea))
				{
					netInfoMap = userDevDao.queryServForNet(userInfoMap.get(USER_ID));
					if (null == netInfoMap || netInfoMap.isEmpty()
							|| StringUtil.IsEmpty(netInfoMap.get(USERNAME)))
					{
						logger.warn(
								"servicename[BridgeToRoutService4JX]cmdId[{}]userinfo[{}]该用户无宽带业务",
								new Object[] { bridgeToRoutChecker.getCmdId(),
										bridgeToRoutChecker.getUserInfo() });
						bridgeToRoutChecker.setResult(1004);
						bridgeToRoutChecker.setResultDesc("该用户无宽带业务");
						bridgeToRoutChecker.setUserSn("");
						// 用户不存在
						bridgeToRoutChecker.setFailureReason("");
						// 失败
						bridgeToRoutChecker.setSuccStatus("-1");
						// 记录日志
						new RecordLogDAO().recordDispatchLog(bridgeToRoutChecker,
								bridgeToRoutChecker.getUserInfo(),
								"BridgeToRoutService4JX");
						return bridgeToRoutChecker.getReturnXml();
					}
					// 桥接和路由切换接口的时候，先去查询宽带账号，去AAA查询密码
					String netpasswd = getNetPassword(netInfoMap.get(USERNAME));
					if (null == netpasswd || StringUtil.IsEmpty(netpasswd))
					{
						logger.warn(
								"servicename[BridgeToRoutService4JX]cmdId[{}]userinfo[{}]宽带账号未在3A中查到",
								new Object[] { bridgeToRoutChecker.getCmdId(),
										bridgeToRoutChecker.getUserInfo() });
						bridgeToRoutChecker.setResult(1002);
						bridgeToRoutChecker.setResultDesc("宽带账号未在3A中查到");
						bridgeToRoutChecker.setUserSn("");
						bridgeToRoutChecker.setFailureReason("");
						// 失败
						bridgeToRoutChecker.setSuccStatus("-1");
						// 记录日志
						new RecordLogDAO().recordDispatchLog(bridgeToRoutChecker,
								bridgeToRoutChecker.getUserInfo(),
								"BridgeToRoutService4JX");
						return bridgeToRoutChecker.getReturnXml();
					}
					else
					{
						userDevDao.updateWanTypeAndPwd(
								bridgeToRoutChecker.getOperateType(),
								userInfoMap.get(USER_ID), netpasswd);
					}
				}
				else
				{
					// 更改数据库上午方式，和业务状态为未做；
					userDevDao.updateWanType(bridgeToRoutChecker.getOperateType(),
							userInfoMap.get(USER_ID));
				}
				// 调用配置模块，下发业务。
				// 预读调用对象
				PreServInfoOBJ preInfoObj = new PreServInfoOBJ(userInfoMap.get(USER_ID),
						"" + userInfoMap.get(DEVICE_ID), "" + userInfoMap.get("oui"),
						userInfoMap.get("device_serialnumber"), "10", "1");
				if (1 != CreateObjectFactory.createPreProcess()
						.processServiceInterface(CreateObjectFactory.createPreProcess()
								.GetPPBindUserList(preInfoObj)))
				{
					bridgeToRoutChecker.setResult(1000);
					bridgeToRoutChecker.setResultDesc("未知错误，请稍后重试");
					bridgeToRoutChecker.setUserSn("");
					// 用户不存在
					bridgeToRoutChecker.setFailureReason("3");
					// 失败
					bridgeToRoutChecker.setSuccStatus("-1");
					if (Global.JXDX.equals(Global.G_instArea))
					{
						String wanType = StringUtil
								.getStringValue(userInfoMap.get(WAN_TYPE));
						String operType = "";
						if (bridgeToRoutChecker.getOperateType().equals(wanType))
						{
							if (ONE.equals(bridgeToRoutChecker.getOperateType()))
							{
								operType = "3";
							}
							else
							{
								operType = "4";
							}
						}
						else
						{
							operType = bridgeToRoutChecker.getOperateType();
						}
						new RecordLogDAO().recordRouteAndBridge(bridgeToRoutChecker,
								userInfoMap, "2", "未知错误，请稍后重试", operType);
					}
				}
				else
				{
					bridgeToRoutChecker.setResult(0);
					bridgeToRoutChecker.setResultDesc("成功");
					bridgeToRoutChecker.setUserSn(
							StringUtil.getStringValue(userInfoMap.get(USERNAME)));
					bridgeToRoutChecker.setFailureReason("0");
					// 成功
					bridgeToRoutChecker.setSuccStatus("1");
					if (Global.JXDX.equals(Global.G_instArea))
					{
						String wanType = StringUtil
								.getStringValue(userInfoMap.get(WAN_TYPE));
						String operType = "";
						if (bridgeToRoutChecker.getOperateType().equals(wanType))
						{
							if (ONE.equals(bridgeToRoutChecker.getOperateType()))
							{
								operType = "3";
							}
							else
							{
								operType = "4";
							}
						}
						else
						{
							operType = bridgeToRoutChecker.getOperateType();
						}
						new RecordLogDAO().recordRouteAndBridge(bridgeToRoutChecker,
								userInfoMap, "1", "成功", operType);
					}
				}
			}
		}
		String returnXml = bridgeToRoutChecker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(bridgeToRoutChecker,
				bridgeToRoutChecker.getUserInfo(), "BridgeToRoutService4JX");
		logger.warn(
				"servicename[BridgeToRoutService4JX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { bridgeToRoutChecker.getCmdId(),
						bridgeToRoutChecker.getUserInfo(), returnXml });
		return returnXml;
	}


	/**
	 * 吉林电信根据3a接口获取宽带密码
	 * 
	 * @param netUsername
	 * @return
	 */
	private String getNetPassword(String netAccount)
	{
		String netPassword = null;
		String sheet = "QryPasswd {" + netAccount + "}";
		logger.warn("调3a接口获取账户:[{}]的宽带密码", new Object[] { netAccount });
		logger.warn("给3A发过去的参数为[{}]", new Object[] { sheet });
		String rest = SocketUtil.sendStrMesgJldx(Global.G_3A_SERVER, Global.G_3A_PORT,
				sheet);
		logger.warn("调用3A接口返回的结果是:[{}]", rest);
		if (null != rest)
		{
			try
			{
				rest = rest.replace("} {", "//");
				String[] results = rest.substring(1, rest.length() - 1).split("//");
				if (results.length == three && (Global.OK).equals(results[1])
						&& ("成功").equals(results[2]))
				{
					netPassword = encryptionUtil.decryption(results[0]);
					if ("".equals(netPassword))
					{
						netPassword = "decryptionFailed";
						logger.warn("解密3A返回的密码失败");
					}
					logger.warn("[{}]从3A获取的宽带密码为[{}]",
							new Object[] { netAccount, netPassword });
				}
				else if (results.length == two && (Global.ERROR).equals(results[0])
						&& ("用户未找到").equals(results[1]))
				{
					netPassword = "";
					logger.warn("查无[{}]此宽带账号", new Object[] { netAccount });
				}
				else
				{
					netPassword = "errorFormat";
					logger.warn("{}与文档描述返回的结果不一致",rest);
				}
			}
			catch (Exception e)
			{
				netPassword = "errorFormat";
				logger.warn("{}与文档描述返回的结果不一致,解析时发生异常",rest);
				e.printStackTrace();
			}
		}
		else
		{
			logger.warn("[{}]从3A获取宽带密码失败，返回结果为空", new Object[] { netAccount });
		}
		return netPassword;
	}

	public String getDevNetType(String devId, String gwType)
	{
		ChangeConnectionNetForAHCorba netCorba = new ChangeConnectionNetForAHCorba();
		return netCorba.gatherNetInfo(devId, gwType);
	}

	public int changeConnectionTypeForAh(String deviceId, String connType,
			String routeAccount, String routePasswd, String path, String gwType)
	{
		logger.warn("changeConnectionTypeForAh:{},{},{},{},{}",
				new Object[] { deviceId, connType, routeAccount, routePasswd, path });
		ChangeConnectionNetForAHCorba netCorba = new ChangeConnectionNetForAHCorba();
		return netCorba.changeConnectType(deviceId, connType, routeAccount, routePasswd,
				path, gwType);
	}

	private void dealUserInfoMap(Map<String, String> userInfoMap,
			UserDeviceDAO userDevDao)
	{
		long userId = StringUtil.getLongValue(userInfoMap.get(USER_ID));
		HashMap<String,String> netInfo = userDevDao.getNetInfo(userId);
		String netAcount = StringUtil.getStringValue(netInfo.get("netaccount"));
		String wanType = StringUtil.getStringValue(netInfo.get(WAN_TYPE));
		userInfoMap.put("netaccount",netAcount);
		userInfoMap.put(WAN_TYPE,wanType);
	}
}
