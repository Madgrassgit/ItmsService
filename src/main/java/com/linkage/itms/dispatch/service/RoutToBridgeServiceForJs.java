
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.commom.util.SocketUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.RoutToBridgeDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.RoutToBridgeChecker;

/**
 * @author yinlei3 (Ailk No.73167)
 * @version 1.0
 * @since 2016年5月18日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RoutToBridgeServiceForJs implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(RoutToBridgeServiceForJs.class);
	/**
	 * 宽带业务id
	 */
	private static final String SERVICE_TYPE_KUANDAI = "10";

	@Override
	public String work(String inXml)
	{
		/** 日志 */
		RoutToBridgeChecker checker = new RoutToBridgeChecker(inXml);
		// 验证入参格式是否正确
		if (false == checker.check())
		{
			logger.error(
					"servicename[RoutToBridgeService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn("servicename[RoutToBridgeService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 查询用户信息
		// loid为空的情况，即综调第一次调接口进行路由改桥接，此时只传宽带账号,userinfoType一直用1
		// loid不为空的情况，即第一次通过宽带账号找到多个loid， 综调那边会选择一个loid再次调用我们呢的接口，userinfoType一直用1
		ArrayList<HashMap<String, String>> userMapList = userDevDao.queryUserList(
				checker.getUserInfoType(), checker.getUserInfo(), checker.getUserLoid());
		if (null == userMapList || userMapList.isEmpty())
		{
			logger.warn("servicename[RoutToBridgeService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("查无此用户");
			checker.setUserSn("");
			checker.setFailureReason("1"); // 用户不存在
			checker.setSuccStatus("-1"); // 失败
		}
		else {
			if (userMapList.size() > 1 && checker.getUserInfoType() != 1)
			{
				logger.warn("servicename[RoutToBridgeService]cmdId[{}]userinfo[{}]账号对应多个用户",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				String loidList = "";
				for (int i = 0; i < userMapList.size(); i++)
				{
					loidList += StringUtil.getStringValue(userMapList.get(i), "username", "")
							+ ",";
				}
				checker.setResult(4);
				checker.setResultDesc(loidList.substring(0, loidList.length() - 1));
				checker.setUserSn("");
				checker.setFailureReason(loidList.substring(0, loidList.length() - 1)); // 查询到多条loid
				checker.setSuccStatus("-1"); // 失败
			}
			else {
				if (StringUtil.IsEmpty(userMapList.get(0).get("user_id")))
				{
					logger.warn("servicename[RoutToBridgeService]cmdId[{}]userinfo[{}]查无此用户",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1002);
					checker.setResultDesc("查无此用户");
					checker.setUserSn("");
					checker.setFailureReason("1"); // 用户不存在
					checker.setSuccStatus("-1"); // 失败
				}
				else
				{
					HashMap<String, String> userInfoMap = userMapList.get(0);
					long userId = StringUtil.getLongValue(userInfoMap.get("user_id"));
					String devId = userInfoMap.get("device_id");
					String oui = userInfoMap.get("oui");
					String devSn = userInfoMap.get("device_serialnumber");
					// 未绑定设备，不能做配置下发，返回失败信息
					if (null == devId || StringUtil.IsEmpty(devId))
					{
						logger.warn("servicename[RoutToBridgeService]cmdId[{}]userinfo[{}]未绑定设备",
								new Object[] { checker.getCmdId(), checker.getUserInfo() });
						checker.setResult(1004);
						checker.setResultDesc("此用户未绑定设备");
						checker.setUserSn(StringUtil.getStringValue(userInfoMap.get("username")));
						checker.setFailureReason("2"); // 没有绑定设备
						checker.setSuccStatus("-1"); // 失败
					}
					else
					{
						RoutToBridgeDAO routToBridgeDAO = new RoutToBridgeDAO();
						List<HashMap<String, String>> list = routToBridgeDAO.checkService(devId);
						if (null == list
								|| list.isEmpty()
								|| !"2".equals(StringUtil.getStringValue(list.get(0), "wan_type",
										"0")))
						{
							checker.setResult(1002);
							checker.setResultDesc("无路由上网业务");
							checker.setUserSn(StringUtil.getStringValue(userInfoMap
									.get("username")));
							checker.setFailureReason("1");
							checker.setSuccStatus("-1");
						}
						else
						{
							// 修改业务表的open_status、wlan_type字段
							int rs = routToBridgeDAO.updateServInfo(userId);
							if (-1 == rs)
							{
								checker.setResult(1000);
								checker.setResultDesc("更新业务表上网类型失败");
								checker.setUserSn(StringUtil.getStringValue(userInfoMap
										.get("username")));
								checker.setFailureReason("1");
								checker.setSuccStatus("-1");
							}
							else
							{
								// 通知配置模块下发参数
								PreServInfoOBJ preInfoObj = new PreServInfoOBJ(
										StringUtil.getStringValue(userId), devId, oui, devSn,
										SERVICE_TYPE_KUANDAI, "1");
								if (1 != CreateObjectFactory.createPreProcess()
										.processServiceInterface(CreateObjectFactory.createPreProcess()
												.GetPPBindUserList(preInfoObj)))
								{
									logger.warn(
											"servicename[RoutToBridgeService]cmdId[{}]userinfo[{}]设备[{}]下发特定业务，调用后台预读模块失败，业务类型为：[{}]",
											new Object[] { checker.getCmdId(),
													checker.getUserInfo(), devId,
													SERVICE_TYPE_KUANDAI });
									checker.setResult(1000);
									checker.setResultDesc("未知错误，请稍后重试");
								}
								else
								{
									// JSDX_ITMS-BUG-20161009-WJY-001（对外服务器接口、变更上网方式WEB，关闭路由通知radius)
									StringBuffer radiusSheet = new StringBuffer();
									radiusSheet.append("90|||3|||");
									// 宽带账号
									radiusSheet.append(checker.getUserInfo()).append("LINKAGE");
									logger.warn("[{}]路由改桥，通知radius系统...[{}]", checker.getUserInfo(), radiusSheet.toString());
									String str = SocketUtil.sendStrMesg(Global.RADIUS_IP, StringUtil.getIntegerValue(Global.RADIUS_PORT), 
											radiusSheet.toString() + "\n");
									logger.warn("[{}]路由改桥，radius系统返回结果[{}]",
											new Object[] { checker.getUserInfo(), str });
									
									checker.setResult(0);
									checker.setResultDesc("成功");
									checker.setUserSn(StringUtil.getStringValue(userInfoMap
											.get("username")));
									checker.setFailureReason("0");
									checker.setSuccStatus("1"); // 成功
								}
							}
						}
					}
				}
			}
		}
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
				"RoutToBridgeService");
		logger.warn(
				"servicename[RoutToBridgeService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
		return returnXml;
	}
}
