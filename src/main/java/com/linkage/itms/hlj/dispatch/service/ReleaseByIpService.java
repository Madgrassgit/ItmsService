
package com.linkage.itms.hlj.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ResourceBind.ResultInfo;
import ResourceBind.UnBindInfo;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.ResourceBindInterface;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.ReleaseByIpChecker;
//import com.linkage.itms.hlj.dispatch.obj.ServiceHandle;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-1
 * @category com.linkage.itms.hlj.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ReleaseByIpService implements HljIService
{

	private static Logger logger = LoggerFactory.getLogger(ReleaseByIpService.class);
	// 处理人
	private static final String DEAL_STAFF = "综调";
	// 用户来源
	private static final int USERLINE = 1;

	/**
	 * 解绑执行方法
	 */
	@Override
	public String work(String inXml)
	{
		logger.warn("ReleaseByIpService==>jsonString({})", inXml);
		ReleaseByIpChecker checker = new ReleaseByIpChecker(inXml);
		if (false == checker.check())
		{
			logger.warn("根据Ip解绑接口，入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("ReleaseByIpService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		QueryDeviceIdDAO qdDao = new QueryDeviceIdDAO();
//		ServiceHandle serviceHandle = new ServiceHandle();
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
			String loid = StringUtil.getStringValue(userMap.get(0), "loid", "");
			String userCityId = StringUtil.getStringValue(userMap.get(0), "city_id", "");
			// if (false == serviceHandle.cityMatch(checker.getCityId(), userCityId))
			// {// 属地不匹配
			// logger.warn(
			// "servicename[ReleaseService]userinfo[{}]属地不匹配 查无此用户",
			// new Object[] { checker.getQueryNum() });
			// checker.setResult(1002);
			// checker.setResultDesc("无此用户信息");
			// return checker.getReturnXml();
			// }
			// else
			// {
			if (StringUtil.IsEmpty(deviceId))
			{
				checker.setResult(3);
				checker.setResultDesc("无设备信息");
				return checker.getReturnXml();
			}
			else
			{
				// 解绑
				String releaseCode = hljItmsRelease(StringUtil.getStringValue(userId),
						loid, userCityId, deviceId, DEAL_STAFF, USERLINE);
				logger.warn("servicename[ReleaseByIpService]QueryNum[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getQueryNum() });
				JSONObject jo = new JSONObject();
				try
				{
					jo.put("Loid", StringUtil.getStringValue(userMap.get(0), "loid", ""));
					if ("1".equals(releaseCode))
					{
						jo.put("OpResult", 0);
						jo.put("OpErrorNumber", "");
					}
					else
					{
						jo.put("OpResult", -1);
						jo.put("OpErrorNumber", releaseCode);
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				return jo.toString();
			}
		}
	}

	/**
	 * @author 岩
	 * @date 2016-8-4
	 * @param userId
	 * @param username
	 * @param cityId
	 * @param deviceId
	 * @param dealstaff
	 * @param userline
	 * @return
	 */
	private String hljItmsRelease(String userId, String username, String cityId,
			String deviceId, String dealstaff, int userline)
	{
		logger.debug(
				"itmsRelease(userId:{};username{};cityId:{};deviceId:{};dealstaff:{},userline:{})",
				new Object[] { userId, username, cityId, deviceId, dealstaff, userline });
		String msg = "";
		ResourceBindInterface corba = CreateObjectFactory.createResourceBind("1");
		UnBindInfo[] arr = new UnBindInfo[1];
		arr[0] = new UnBindInfo();
		arr[0].accOid = "0";
		arr[0].accName = "ItmsService";
		arr[0].userId = userId;
		arr[0].deviceId = deviceId;
		arr[0].userline = userline;
		ResultInfo rs = corba.release(arr);
		if (rs == null)
		{
			msg = "-10000";
		}
		else
		{
			msg = "" + Integer.parseInt(rs.resultId[0]);
		}
		return msg;
	}
}
