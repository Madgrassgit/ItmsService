
package com.linkage.itms.radius.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.radius.dao.RadiusSyncDAO;
import com.linkage.itms.radius.obj.RadiusPasswordChecker;

/**
 * 江西电信ITMS系统路由模式与AAA及激活系统修改密码接口
 * 
 * @author fangchao (Ailk No.69934)
 * @version 1.0
 * @since 2013-7-8
 * @category com.linkage.itms.radius.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RadiusSyncService
{

	private static final Logger logger = LoggerFactory.getLogger(RadiusSyncService.class);
	private RadiusSyncDAO syncDAO = new RadiusSyncDAO();

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
		Map<String, String> custMap = syncDAO.queryCustomerByAccout(checker.getUsername());
		if(custMap == null)
		{
			logger.warn("username[{}] is not exist, return",checker.getUsername());
			return;
		}
		
		// 更新用户密码
		int updateRows = syncDAO.updateCustomer(checker.getUsername(),
				checker.getPassword(),custMap.get("wan_type"));
		if (updateRows == 0)
		{
			logger.warn("username[{}] do not exist in local database,return.",
					checker.getUsername());
			return;
		}
		
		// 业务下发只下发路由方式的
		Map<String, String> customerMap = syncDAO.queryCustomer(checker.getUsername());
		if (customerMap == null || customerMap.size() == 0)
		{
			logger.warn("username[{}] do not bind device, no need send service,return.",
					checker.getUsername());
			return;
		}
		// 业务下发
		PreServInfoOBJ preInfoObj = new PreServInfoOBJ(customerMap.get("user_id"),
				customerMap.get("device_id"), customerMap.get("oui"),
				customerMap.get("device_serialnumber"), "10", "1");
		if (1 != CreateObjectFactory.createPreProcess().processServiceInterface(CreateObjectFactory.createPreProcess()
				.GetPPBindUserList(preInfoObj)))
		{
			logger.warn("设备{}下发特定业务，调用后台预读模块失败，业务类型为：10", customerMap.get("device_id"));
		}
		else
		{
			logger.warn("chang user[{}] password[{}] success.", checker.getUsername(),
					checker.getPassword());
		}
	}
}
