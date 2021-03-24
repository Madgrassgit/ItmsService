
package com.linkage.itms.radius.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.linkage.itms.commom.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.radius.dao.RadiusSyncDAO;
import com.linkage.itms.radius.obj.RadiusPasswordChecker;
import scala.util.parsing.combinator.testing.Str;

/**
 * 江西电信ITMS系统路由模式与AAA及激活系统修改密码接口
 * 
 * @author fangchao (Ailk No.69934)
 * @version 1.0
 * @since 2013-7-8
 * @category com.linkage.itms.radius.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RadiusSyncServiceJxdx
{

	private static final Logger logger = LoggerFactory
			.getLogger(RadiusSyncServiceJxdx.class);
	private RadiusSyncDAO syncDAO = new RadiusSyncDAO();
	private static String ROUTE = "2";

	public void changePassword(String request)
	{
		logger.warn("changePassword ({})", request);
		RadiusPasswordChecker checker = new RadiusPasswordChecker(request);
		if (!checker.check())
		{
			logger.warn("user request parameter is invalid, return");
			return;
		}
		// 1.查询用户信息
		ArrayList<HashMap<String, String>> custMapList = syncDAO
				.queryCustomerByAccoutJxdx(checker.getUsername());
		if (null == custMapList || custMapList.size() == 0)
		{
			logger.warn("username[{}] is not exist, return", checker.getUsername());
			return;
		}
		for (HashMap<String, String> custMap : custMapList)
		{
			String userId = StringUtil.getStringValue(custMap.get("user_id"));
			String wanType = StringUtil.getStringValue(custMap.get("wan_type"));
			// 更新用户密码
			int updateRows = syncDAO.updateCustomerJxdx(checker.getUsername(),
					checker.getPassword(), wanType, userId);
			if (updateRows == 0)
			{
				logger.warn(
						"username[{}] ,userId[{}] do not exist in local database,return.",
						checker.getUsername(), userId);
				continue;
			}
			//未绑定设备 不予下发
			Map<String, String> customerMap = syncDAO.queryCustomerJxdx(userId);
			if (customerMap == null || customerMap.size() == 0)
			{
				logger.warn(
						"username[{}],userId[{}]  do not bind device, no need send service,return.",
						checker.getUsername(), userId);
				continue;
			}
			// 业务下发只下发路由方式的
			if(!ROUTE.equals(wanType))
			{
				logger.warn(
						"username[{}],userId[{}]  wanType is not route, no need send service,return.",
						checker.getUsername(), userId);
				continue;
			}
			// 业务下发
			PreServInfoOBJ preInfoObj = new PreServInfoOBJ(customerMap.get("user_id"),
					customerMap.get("device_id"), customerMap.get("oui"),
					customerMap.get("device_serialnumber"), "10", "1");
			if (1 != CreateObjectFactory.createPreProcess().processServiceInterface(
					CreateObjectFactory.createPreProcess().GetPPBindUserList(preInfoObj)))
			{
				logger.warn("设备{}下发特定业务，调用后台预读模块失败，业务类型为：10",
						customerMap.get("device_id"));
			}
			else
			{
				logger.warn("chang user[{}],userId[{}] password[{}] success.",
						checker.getUsername(), userId, checker.getPassword());
			}
		}
	}
}
