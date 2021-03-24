
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
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.hlj.dispatch.dao.ModifyWanTypeDAO;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.ModifyWanTypeChecker;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-2
 * @category com.linkage.itms.hlj.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ModifyWanTypeService implements HljIService
{

	private static Logger logger = LoggerFactory.getLogger(ModifyWanTypeService.class);

	@Override
	public String work(String jsonString)
	{
		logger.warn("ModifyWanTypeService==>jsonString({})", jsonString);
		ModifyWanTypeChecker checker = new ModifyWanTypeChecker(jsonString);
		if (false == checker.check())
		{
			logger.warn("上网模式修改接口，入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("ModifyWanTypeService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		QueryDeviceIdDAO qdDao = new QueryDeviceIdDAO();
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
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(8);
			checker.setResultDesc("ITMS未知异常-查询结果为空");
			return checker.getReturnXml();
		}
		else
		{// 用户存在
			String userId = StringUtil.getStringValue(userMap.get(0).get("user_id"));
			String deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
			if (StringUtil.IsEmpty(deviceId))
			{// 用户未绑定终端
				checker.setResult(3);
				checker.setResultDesc("无设备信息");
				return checker.getReturnXml();
			}
			ModifyWanTypeDAO mwtDao = new ModifyWanTypeDAO();
			ArrayList<HashMap<String, String>> resultList = null;
			resultList = mwtDao.getMapByUser(userId);
			if (resultList.isEmpty() || resultList == null)
			{
				checker.setResult(1002);
				checker.setResultDesc("设备未开通宽带业务，不可修改");
				return checker.getReturnXml();
			}
			mwtDao.updateWanType(userId, checker.getOpTask());
			// 下发业务部分
			ServUserDAO servUserDao = new ServUserDAO();
			String devSn = null;
			String oui = null;
			int servTypeId = StringUtil.getIntegerValue(resultList.get(0).get(
					"serv_type_id"));
			// 更新业务用户表的开通状态
			servUserDao.updateServOpenStatus(StringUtil.getLongValue(userId), servTypeId);
			// 预读调用对象
			PreServInfoOBJ preInfoObj = new PreServInfoOBJ(
					StringUtil.getStringValue(userId), "" + deviceId, "" + oui, devSn,
					"10", "1");
			if (1 != CreateObjectFactory.createPreProcess().processServiceInterface(CreateObjectFactory.createPreProcess()
					.GetPPBindUserList(preInfoObj)))
			{
				logger.warn("servicename[ModifyWanTypeService] 设备[{}]全业务下发，调用配置模块失败",
						new Object[] { deviceId });
				JSONObject jo = new JSONObject();
				try
				{
					jo.put("Loid", StringUtil.getStringValue(userMap.get(0), "loid", ""));
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
					jo.put("Loid", StringUtil.getStringValue(userMap.get(0), "loid", ""));
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
	}
}
