
package com.linkage.itms.hlj.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.QueryLanStateChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-9
 * @category com.linkage.itms.hlj.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QueryLanStateService implements HljIService
{

	private static final Logger logger = LoggerFactory
			.getLogger(QueryLanStateService.class);

	public String work(String inXml)
	{
		logger.warn("QueryLanStateService：inXml({})", inXml);
		QueryLanStateChecker checker = new QueryLanStateChecker(inXml);
		if (false == checker.check())
		{
			logger.warn("LAN口连接状态查询接口，入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("QueryLanStateService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
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
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		// 设备正在被操作，不能获取节点值
		if (-3 == flag)
		{
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(10);
			checker.setResultDesc("设备忙，采集失败");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
		// 设备在线
		else if (1 == flag)
		{
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
			List<String> iList = corba.getIList(deviceId, lanPath);
			if (null == iList || iList.isEmpty())
			{
				logger.warn("[{}]获取iList失败，返回", deviceId);
				checker.setResult(6);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return checker.getReturnXml();
			}
			else
			{
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId, iList.size());
			}
			
			List<HashMap<String, String>> deviceInfoMap = qdDao
					.queryInfoByDevId(deviceId);
			JSONObject jo = new JSONObject();
			JSONObject termLineInfo = new JSONObject();
			try
			{
				jo.put("resultCode", 0);
				jo.put("streamingNum", checker.getStreamingNum() );
				jo.put("termLineInfo", termLineInfo);
				for (String i : iList)
				{
					String[] gatherPath = new String[] {
							"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."
									+ i + ".Status",
							"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."
									+ i + ".Stats.BytesReceived",
							"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."
									+ i + ".Stats.BytesSent" };
					ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId,
							gatherPath);
					if (null == objLlist || objLlist.isEmpty())
					{
						continue;
					}
					String status = "";
					String received = "";
					String sent = "";
					for (ParameValueOBJ pvobj : objLlist)
					{
						if (pvobj.getName().contains("Status"))
						{
							status = pvobj.getValue();
						}
						else if (pvobj.getName().contains("BytesReceived"))
						{
							received = pvobj.getValue();
						}
						else if (pvobj.getName().contains("BytesSent"))
						{
							sent = pvobj.getValue();
						}
					}
					Map<String,String> lanMap = new HashMap<String,String>();
					lanMap.put("portId", "LAN"+i);
					if ("Up".equals(status)){
						status = "0";
					}else{
						status = "1";
					}
					lanMap.put("portStatus", status);
					lanMap.put("receiPacks", received);
					lanMap.put("sendPacks", sent);
					JSONArray ja = new JSONArray();
					ja.put(lanMap);
					termLineInfo.put("equipType", 0);// 默认电脑端
					termLineInfo.put("ip", StringUtil.getStringValue(
							deviceInfoMap.get(0), "loopback_ip", ""));
					termLineInfo.put("mac", StringUtil.getStringValue(
							deviceInfoMap.get(0), "cpe_mac", ""));
					termLineInfo.put("time", "");// 默认空
					
					termLineInfo.put("LAN"+i,ja);
					lanMap = null;
					status = null;
					received = null;
					sent = null;
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			return jo.toString();
		}
		// 设备不在线，不能获取节点值
		else
		{
			logger.warn("设备离线，device_id={}", deviceId);
			checker.setResult(4);
			checker.setResultDesc("设备离线");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
	}
}
