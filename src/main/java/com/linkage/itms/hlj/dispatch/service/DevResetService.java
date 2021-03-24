
package com.linkage.itms.hlj.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.DevReset;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.hlj.dispatch.dao.DevResetDAO;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.DevResetChecker;

/**
 * 恢复出厂接口
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-1
 * @category com.linkage.itms.hlj.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DevResetService implements HljIService
{

	private static Logger logger = LoggerFactory.getLogger(DevResetService.class);

	/**
	 * 解绑执行方法
	 */
	@Override
	public String work(String inXml)
	{
		logger.warn("reset:inXml({})", inXml);
		DevResetChecker checker = new DevResetChecker(inXml);
		if (false == checker.check())
		{
			logger.warn("恢复出厂接口接口，入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("DevResetService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		QueryDeviceIdDAO qdDao = new QueryDeviceIdDAO();
		// 查询用户信息 考虑属地因素
		List<HashMap<String, String>> userMap = null;
		if (checker.getQueryType() == 0)
		{
			userMap = qdDao.queryUserByNetAccount(checker.getQueryNum());
		}
		else if (checker.getQueryType() == 1)
		{
			userMap = qdDao.queryUserByLoid(checker.getQueryNum());
		}
		else if (checker.getQueryType() == 2)
		{
			userMap = qdDao.queryUserByDevSN(checker.getQueryNum());
		}
		else
		{
		}
		// 查询用户信息 考虑属地因素
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(8);
			checker.setResultDesc("ITMS未知异常-查询结果为空");
			return checker.getReturnXml();
		}
		else
		{// 用户存在
			long userId = StringUtil.getLongValue(userMap.get(0).get("user_id"));
			String deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
			if (StringUtil.IsEmpty(deviceId))
			{// 用户未绑定终端
				checker.setResult(3);
				checker.setResultDesc("无设备信息");
				return checker.getReturnXml();
			}
			else
			{
				if ("0".equals(checker.getOpTask()))
				{
					ArrayList<HashMap<String, String>> resultList = null;
					resultList = DevResetDAO.getMapByUser(userMap.get(0).get("user_id"));
					if (resultList.isEmpty() || resultList == null){
						checker.setResult(10000);
						checker.setResultDesc("设备未开通宽带业务，不可下发");
						return checker.getReturnXml();
					}
					ServUserDAO servUserDao = new ServUserDAO();
					String devSn = null;
					String oui = null;
					int servTypeId = StringUtil.getIntegerValue(resultList.get(0).get(
							"serv_type_id"));
					// 更新业务用户表的开通状态
					servUserDao.updateServOpenStatus(userId, servTypeId);
					// 预读调用对象
					PreServInfoOBJ preInfoObj = new PreServInfoOBJ(
							StringUtil.getStringValue(userId), "" + deviceId, "" + oui,
							devSn, resultList.get(0).get("serv_type_id"), "1");
					if (1 != CreateObjectFactory.createPreProcess()
							.processServiceInterface(CreateObjectFactory.createPreProcess()
									.GetPPBindUserList(preInfoObj)))
					{
						logger.warn("servicename[DevResetService] 设备[{}]全业务下发，调用配置模块失败",
								new Object[] { deviceId });
						JSONObject jo = new JSONObject();
						try
						{
							jo.put("Loid",
									StringUtil.getStringValue(userMap.get(0), "loid", ""));
							jo.put("OpResult", "不成功");
							jo.put("OpErrorNumber", -1);
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						return jo.toString();
					}
					else
					{
						JSONObject jo = new JSONObject();
						try
						{
							jo.put("Loid",
									StringUtil.getStringValue(userMap.get(0), "loid", ""));
							jo.put("OpResult", "成功");
							jo.put("OpErrorNumber", " ");
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						return jo.toString();
					}
				}
				else
				{
					int irt = 0;
					/**
					 * 流程调整为: 1、调用者（WEB或者ItmsService模块），将需要恢复出厂的用户业务状态置成未做，调用配置模块。
					 * 2、配置模块根据未做的业务生成业务下发策略，通知acs恢复出厂 3、调用者判断恢复出厂如果是失败，将状态还原。
					 */
					DevResetDAO.updateCustStatus(userId);
					irt = DevReset.reset(deviceId);
					logger.warn("servicename[DevResetService] 调ACS设备返回码：{}", irt);
					if (1 == irt)
					{
						logger.warn("servicename[DevResetService] 设备恢复出厂设置成功");
						JSONObject jo = new JSONObject();
						try
						{
							jo.put("Loid",
									StringUtil.getStringValue(userMap.get(0), "loid", ""));
							jo.put("OpResult", "成功");
							jo.put("OpErrorNumber", " ");
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						return jo.toString();
					}
					else
					{
						// 调用配置模块，或者acs模块对设备下发恢复出厂设置命令失败后，业务用户表修改成成功状态
						DevResetDAO.updateCustStatusFailure(userId);
						logger.warn("servicename[DevResetService] 设备恢复出厂设置失败");
						JSONObject jo = new JSONObject();
						try
						{
							jo.put("Loid",
									StringUtil.getStringValue(userMap.get(0), "loid", ""));
							jo.put("OpResult", "不成功");
							jo.put("OpErrorNumber", -2);
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						return jo.toString();
					}
				}
			}
		}
	}
}
