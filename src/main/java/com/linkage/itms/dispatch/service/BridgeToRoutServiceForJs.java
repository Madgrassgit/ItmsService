
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.SocketUtil;
import com.linkage.itms.dao.BridgeToRoutDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.BridgeToRoutChecker;

/**
 * @author yinlei3 (Ailk No.73167)
 * @version 1.0
 * @since 2016年5月18日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class BridgeToRoutServiceForJs implements IService
{

	/** 日志 */
	private static final Logger logger = LoggerFactory
			.getLogger(BridgeToRoutServiceForJs.class);

	@Override
	public String work(String inXml)
	{
		BridgeToRoutChecker checker = new BridgeToRoutChecker(inXml);
		// 验证入参格式是否正确
		if (false == checker.check())
		{
			logger.error(
					"servicename[BridgeToRoutService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn("servicename[BridgeToRoutService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 查询用户信息
		// loid为空的情况，即综调第一次调接口进行巧改路由，此时只传宽带账号,userinfoType一直用1
		// loid不为空的情况，即第一次通过宽带账号找到多个loid， 综调那边会选择一个loid再次调用我们呢的接口，userinfoType一直用1
		ArrayList<HashMap<String, String>> userMapList = userDevDao.queryUserList(
				checker.getUserInfoType(), checker.getUserInfo(), checker.getUserLoid());
		if (null == userMapList || userMapList.isEmpty())
		{
			logger.warn("servicename[BridgeToRoutService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("查无此用户");
			checker.setUserSn("");
			checker.setFailureReason("1"); // 用户不存在
			checker.setSuccStatus("-1"); // 失败
		}
		else {
			if (checker.getUserInfoType() != 1 && userMapList.size() > 1) {
				logger.warn("servicename[BridgeToRoutService]cmdId[{}]userinfo[{}]账号对应多个用户",
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
					logger.warn("servicename[BridgeToRoutService]cmdId[{}]userinfo[{}]查无此用户",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1002);
					checker.setResultDesc("查无此用户");
					checker.setUserSn("");
					checker.setFailureReason("1"); // 用户不存在
					checker.setSuccStatus("-1"); // 失败
				}
				else {
					HashMap<String, String> userInfoMap = userMapList.get(0);
					String devId = userInfoMap.get("device_id");
					// 未绑定设备，不能做配置下发，返回失败信息
					if (null == devId || StringUtil.IsEmpty(devId))
					{
						logger.warn("servicename[BridgeToRoutService]cmdId[{}]userinfo[{}]未绑定设备",
								new Object[] { checker.getCmdId(), checker.getUserInfo() });
						checker.setResult(1004);
						checker.setResultDesc("此用户未绑定设备");
						checker.setUserSn(StringUtil.getStringValue(userInfoMap.get("username")));
						checker.setFailureReason("2"); // 没有绑定设备
						checker.setSuccStatus("-1"); // 失败
					}
					else
					{
						BridgeToRoutDAO bridgeToRoutDAO = new BridgeToRoutDAO();
						List<Map<String, String>> list = bridgeToRoutDAO.checkService(devId);
						Map<String, String> resltMap = list.get(0);
						if (resltMap == null
								|| resltMap.isEmpty()
								|| !"1".equals(StringUtil.getStringValue(resltMap, "wan_type",
										"0")))
						{
							checker.setResult(1002);
							checker.setResultDesc("无桥接上网业务");
							checker.setUserSn(StringUtil.getStringValue(userInfoMap
									.get("username")));
							checker.setFailureReason("1");
							checker.setSuccStatus("-1");
						}
						else
						{
							StringBuffer bssSheet = new StringBuffer();
							bssSheet.append("6|||50|||1|||");
							bssSheet.append(new DateTimeUtil().getYYYYMMDDHHMMSS()).append("|||");
							// 属地
							bssSheet.append(StringUtil.getStringValue(resltMap, "city_id"))
									.append("|||");
							// 宽带账号
							bssSheet.append(checker.getUserInfo()).append("|||8|||");
							// loid
							bssSheet.append(
									StringUtil.getStringValue(userInfoMap.get("username")))
									.append("LINKAGE");
							logger.warn("发送工单：" + bssSheet.toString());
							String str = SocketUtil
									.sendStrMesg(
											Global.G_ITMS_SHEET_SERVER_CHINA_MOBILE,
											StringUtil
													.getIntegerValue(Global.G_ITMS_SHEET_PORT_CHINA_MOBILE),
											bssSheet.toString() + "\n");
							logger.warn("{}回单：" + str, checker.getUserInfo());
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
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
				"BridgeToRoutService");
		logger.warn(
				"servicename[BridgeToRoutService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
		return returnXml;
	}

	public static void main(String[] args)
	{
		System.out.println(new DateTimeUtil().getYYYYMMDDHHMMSS());
	}
}
