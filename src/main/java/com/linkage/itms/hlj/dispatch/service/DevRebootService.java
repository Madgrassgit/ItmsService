
package com.linkage.itms.hlj.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.DevReboot;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.DevRebootChecker;

/**
 * 设备重启接口
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-1
 * @category com.linkage.itms.hlj.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DevRebootService implements HljIService
{

	private static Logger logger = LoggerFactory.getLogger(DevRebootService.class);

	/**
	 * 解绑执行方法
	 */
	@Override
	public String work(String inXml)
	{
		logger.warn("DevRebootService==>jsonString({})", inXml);
		DevRebootChecker checker = new DevRebootChecker(inXml);
		if (false == checker.check())
		{
			logger.warn("超级密码查询接口，入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("DevRebootService==>retParam={}", checker.getReturnXml());
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
			userMap = qdDao.queryDevByLoid(checker.getQueryNum());
		}
		else if (checker.getQueryType() == 2)
		{
			userMap = qdDao.queryDevice(checker.getQueryNum());
		}
		else if (checker.getQueryType() == 3)
		{
			userMap = qdDao.queryDeviceByVoipPhone(checker.getQueryNum());
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
			String userDevId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
			if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
			{// 用户未绑定终端
				checker.setResult(3);
				checker.setResultDesc("无设备信息");
				return checker.getReturnXml();
			}
			else
			{
				// 重启
				logger.warn("servicename[DevRebootService]getQueryNum[{}]调ACS重启设备",
						new Object[] { checker.getQueryNum() });
				int irt = DevReboot.reboot(userDevId);
				logger.warn("servicename[DevRebootService]getQueryNum[{}]调ACS重启设备返回码：{}",
						new Object[] { checker.getQueryNum(), irt });
				JSONObject jo = new JSONObject();
				if (1 == irt)
				{
					logger.warn("servicename[DevRebootService]getQueryNum[{}]重启成功",
							new Object[] { checker.getQueryNum() });
					try
					{
						jo.put("Loid", StringUtil.getStringValue(userMap.get(0), "loid", ""));
						jo.put("OpResult", "成功");
						jo.put("OpErrorNumber", "");
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
					
				}
				else
				{
					logger.warn("servicename[DevRebootService]QueryNum[{}]设备重启失败",
							new Object[] { checker.getQueryNum() });
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
				}
				return jo.toString();
			}
		}
		
	}
}
