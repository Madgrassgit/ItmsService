
package com.linkage.itms.hlj.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.QueryDeviceVersionChecker;

public class QueryDeviceVersionService implements HljIService
{

	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceVersionService.class);

	public String work(String inXml)
	{
		logger.warn("QueryDeviceVersionService——》jsonString" + inXml);
		QueryDeviceVersionChecker checker = new QueryDeviceVersionChecker(inXml);
		if (false == checker.check())
		{
			logger.error("servicename[QueryDeviceVersionService],入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
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
		if (userMap.size() > 1)
		{
			checker.setResult(1001);
			checker.setResultDesc("数据不唯一，请使用devSn查询");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(3);
			checker.setResultDesc("无设备信息");
			return checker.getReturnXml();
		}
		JSONObject jo = new JSONObject();
		try
		{
			jo.put("Loid", StringUtil.getStringValue(userMap.get(0), "loid", ""));
			jo.put("result", 0);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return jo.toString();
	}
}
