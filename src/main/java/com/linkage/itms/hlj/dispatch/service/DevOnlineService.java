package com.linkage.itms.hlj.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.DevOnlineCAO;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.DevOnlineChecker;

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-26
 * @category com.linkage.itms.hlj.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DevOnlineService implements HljIService
{

	private static Logger logger = LoggerFactory
			.getLogger(DevOnlineService.class);

	public String work(String inParam)
	{
		logger.warn("DevOnlineService==>inParam:" + inParam);
		DevOnlineChecker checker = new DevOnlineChecker(inParam);
		// 入参验证
		if (false == checker.check())
		{
			logger.warn("终端在线信息查询接口入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("DevOnlineService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 在线情况
		QueryDeviceIdDAO qdDao = new QueryDeviceIdDAO();
		String deviceId = "";
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
		} else{
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
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		
		// 设备ID
		checker.setResultCode("");
		Map<String, String> onlineMap = qdDao.queryDevStatus(deviceId);
		int online = 0;
		int iOnline = 1;
		if(onlineMap !=null && !onlineMap.isEmpty()){
			online = StringUtil.getIntValue(onlineMap, "online_status", 0);
		}
		if(online == 0){
			// 不在线
			iOnline = 1;
		} else {
		// 实时获取在线状态
			iOnline = DevOnlineCAO.devOnlineTest(deviceId) == 1 ? 0 : 1;
		// 设置参数
		logger.warn("DevOnlineService==>returnXML:" + checker.getReturnXml());
		}
		JSONObject jo = new JSONObject();
		
		try
		{
			jo.put("Loid", StringUtil.getStringValue(userMap.get(0), "loid", ""));
			jo.put("ResultCode", 1);
			jo.put("OnlineState", iOnline);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return jo.toString();
	}

}
