
package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.RoutToBridgeDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.RoutToBridgeChecker;
import com.linkage.itms.dispatch.util.ChangeConnectionNetForAHCorba;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2015年10月16日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RoutToBridgeService implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(RoutToBridgeService.class);
	/**
	 * 设备在线状态：不在线
	 */
	private static final int OFFLINE = 0;
	/**
	 * 宽带业务id
	 */
	private static final String SERVICE_TYPE_KUANDAI = "10";

	@Override
	public String work(String inXml)
	{
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
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo());
		// 此用户不存在 不能做配置下发，返回失败信息
		if (null == userInfoMap || userInfoMap.isEmpty()
				|| StringUtil.IsEmpty(userInfoMap.get("user_id")))
		{
			logger.warn("servicename[RoutToBridgeService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("查无此用户");
			checker.setUserSn("");
			checker.setFailureReason("1"); // 用户不存在
			checker.setSuccStatus("-1"); // 失败
		}
		// 存在此用户，还要判断是否绑定设备
		else
		{
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
			// 绑定设备，还要判断此设备是否在线
			else
			{
				RoutToBridgeDAO routToBridgeDAO = new RoutToBridgeDAO();
				ArrayList<HashMap<String, String>> devList = routToBridgeDAO
						.getDevStatusInfo(devId);
				Map<String, String> devMap = devList.get(0);
				int intOnlined = StringUtil.getIntValue(devMap, "online_status", OFFLINE);
				// 如果设备不在线，则不能下发桥改路由配置，返回失败信息，否则可以下发桥改路由配置
				if (OFFLINE == intOnlined && !"cq_dx".equals(Global.G_instArea))
				{
					checker.setResult(1003);
					checker.setResultDesc("终端不在线");
					checker.setUserSn(StringUtil.getStringValue(userInfoMap
							.get("username")));
					checker.setFailureReason("3"); // 设备不在线
					checker.setSuccStatus("-1"); // 失败
				}
				else if ("ah_dx".equals(Global.G_instArea))
				{
					String res = getDevNetType(devId, "1");
					String[] result = null;
					if (!StringUtil.IsEmpty(res)) {
						result = res.split(";");
					}
					
					if(StringUtil.IsEmpty(res) || res.length()<3 || result == null || !"1".equals(result[0])){
						logger.warn(
								"servicename[RoutToBridgeService]cmdId[{}]userinfo[{}]获取路径失败",
								checker.getCmdId(), checker.getUserInfo());
						checker.setResult(1005);
						checker.setResultDesc("获取路径失败");
						checker.setUserSn("");
						checker.setFailureReason(result == null ? "" : result[1]);
						checker.setSuccStatus("-1");    // 失败
		            }else{
		            	int code = changeConnectionTypeForAh(devId, "1", "", "", result[2], "1");
		            	if(code == 1 || code ==0){
		            		checker.setResult(0);
							checker.setResultDesc("成功");
							checker.setUserSn(StringUtil.getStringValue(userInfoMap
									.get("username")));
							checker.setFailureReason("0");
							checker.setSuccStatus("1"); // 成功
		            	} else {
		            		logger.warn(
									"servicename[RoutToBridgeService]cmdId[{}]userinfo[{}]下发失败",
									new Object[] { checker.getCmdId(), checker.getUserInfo()});
							checker.setResult(1006);
							checker.setResultDesc("下发失败");
							checker.setUserSn("");
							checker.setFailureReason(result[1]);
							checker.setSuccStatus("-1");    // 失败
		            	}
		            }
				}
				else
				{
					List<HashMap<String, String>> list = routToBridgeDAO
							.checkService(devId);
					// Map<String, String> resltMap = list.get(0);
					// if (resltMap == null || resltMap.isEmpty())
					if (null == list || list.isEmpty())
					{
						checker.setResult(1002);
						checker.setResultDesc("无宽带上网业务");
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
							if(!"cq_dx".equals(Global.G_instArea) || "1".equals(checker.getOfflineEnable())){
								
								// 更新宽带密码
								if(!StringUtil.IsEmpty(checker.getBroadbandPassword())){
									routToBridgeDAO.updatePasswordServInfo(userId, checker.getBroadbandPassword());
								}
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
		new RecordLogDAO().recordDispatchLog(checker,
				checker.getUserInfo(), "RoutToBridgeService");
		logger.warn(
				"servicename[RoutToBridgeService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(),
						checker.getUserInfo(), returnXml });
		return returnXml;
	}
	
	public String getDevNetType(String devId,String gw_type){
		ChangeConnectionNetForAHCorba netCorba = new ChangeConnectionNetForAHCorba();
		return netCorba.gatherNetInfo(devId, gw_type);
	}
	
	public int changeConnectionTypeForAh(String deviceId, String connType,
			String routeAccount, String routePasswd,String path,String gw_type)
	{
		logger.warn("changeConnectionTypeForAh:{},{},{},{},{}",new Object[]{deviceId,connType,routeAccount,routePasswd,path});
		ChangeConnectionNetForAHCorba netCorba = new ChangeConnectionNetForAHCorba();
		return netCorba.changeConnectType(deviceId, connType, routeAccount, routePasswd, path,gw_type);
	}
}
