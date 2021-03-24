
package com.linkage.itms.hlj.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.QueryNetChecker;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-28
 * @category com.linkage.itms.hlj.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QueryNetService implements HljIService
{

	private static Logger logger = LoggerFactory.getLogger(QueryNetService.class);
	// 采集值
	private int rsint = -1;
	// 采集是否超时标识
	private boolean timeOutFlag = true;
	private String queryNumTmp = "";
	private String deviceIdTmp = "";

	@Override
	public String work(String jsonString)
	{
		logger.warn("QueryNetService——》jsonString" + jsonString);
		QueryNetChecker checker = new QueryNetChecker(jsonString);
		if (false == checker.check())
		{
			logger.warn("宽带上网信息查询接口，入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("QueryNetService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 查询用户设备信息
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
		String deviceId = "";
		String userId = "";
		if (null == userMap || userMap.isEmpty())
		{
			checker.setResult(8);
			checker.setResultDesc("ITMS未知异常-查询结果为空");
			return checker.getReturnXml();
		}
		else
		{
			deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
			userId = StringUtil.getStringValue(userMap.get(0), "user_id", "");
			if (StringUtil.IsEmpty(deviceId))
			{
				checker.setResult(3);
				checker.setResultDesc("无设备信息");
				return checker.getReturnXml();
			}
			else if (StringUtil.IsEmpty(userId))
			{
				checker.setResult(9);
				checker.setResultDesc("无用户信息");
				return checker.getReturnXml();
			}
			else
			{
				// 1.查询此用户开通的业务信息
				UserDeviceDAO userDevDao = new UserDeviceDAO();
				Map<String, String> userServMap = userDevDao.queryServForNet(userId);
				if (null == userServMap || userServMap.isEmpty())
				{
					// 没有开通业务
					logger.warn("servicename[QueryNetService]QueryNum[{}]此用户没有开通任何宽带业务",
							new Object[] { checker.getQueryNum() });
					checker.setResult(1002);
					checker.setResultDesc("此用户没有开通任何宽带业务");
					return checker.getReturnXml();
				}
				else
				{
					// 工单值
					logger.warn(
							"servicename[QueryNetService]QueryNum[{}]获取上网业务工单配置数据：",
							new Object[] { checker.getQueryNum(),
									userServMap.get("username") });
					
					if ("hlj_dx".equals(Global.G_instArea)){
						queryNumTmp = checker.getQueryNum();
						deviceIdTmp = deviceId;
						ExecutorService executor = Executors.newSingleThreadExecutor();
						FutureTask<String> future = new FutureTask<String>(
								new Callable<String>() {
									public String call() {
										function();
										return null;
									}
								});
						executor.execute(future);
						try {
							future.get(50 * 1000L, TimeUnit.MILLISECONDS);
						} catch (Exception e) {
							future.cancel(true);
						} finally {
							executor.shutdown();
						}
						// 超时采集失败
						if (timeOutFlag)
						{
							logger.warn("servicename[QueryNetService]QueryNum[{}]采集终端节点值失败",
									new Object[] { checker.getQueryNum() });
							checker.setResult(9);
							checker.setResultDesc("采集终端节点值失败");
							return checker.getReturnXml();
						}
					}else{
						rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3); // 在原来基础上增加了一个参数(3)
					}
					
					logger.warn(
							"servicename[QueryNetService]QueryNum[{}]getCpeParams设备配置信息采集结果：{}",
							new Object[] { checker.getQueryNum(), rsint });
					// 采集失败
					if (rsint != 1)
					{
						logger.warn("servicename[QueryNetService]QueryNum[{}]采集数据失败",
								new Object[] { checker.getQueryNum() });
						checker.setResult(10);
						checker.setResultDesc("设备忙，采集失败");
						return checker.getReturnXml();
					}
					else
					// 采集成功，获取需要的数据
					{
						DeviceConfigDAO dao = new DeviceConfigDAO();
						Map<String, String> internetMap = dao.getInternetSingle(deviceId);
						if (internetMap == null || internetMap.isEmpty())
						{
							logger.warn("servicename[QueryNetService]QueryNum()[{}]未知错误",
									new Object[] { checker.getQueryNum() });
							checker.setResult(4);
							checker.setResultDesc("设备离线");
							return checker.getReturnXml();
						}
						else
						{
							JSONObject jo = new JSONObject();
							String lan;
							if (!StringUtil.IsEmpty(internetMap.get("bind_port"))|| internetMap.get("bind_port") != null){
								lan = StringUtil.getStringValue(
										internetMap, "bind_port", "").replaceAll("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.", "LAN").replaceAll("InternetGatewayDevice.LANDevice.1.WLANConfiguration.", "WLAN");
										
							}else{
								lan = "";
							}
							try
							{
								jo.put("resultCode", 0);
								jo.put("VLANID", StringUtil.getStringValue(internetMap,
										"vlan_id", ""));
								jo.put("ConnectionStatus", StringUtil.getStringValue(
										internetMap, "conn_status", ""));
								jo.put("ConnectionType", StringUtil.getStringValue(
										internetMap, "conn_type", ""));
								
								jo.put("BindLanItf", lan);
								jo.put("serviceList", StringUtil.getStringValue(
										internetMap, "serv_list", ""));
								jo.put("pppoeAccount", StringUtil.getStringValue(
										internetMap, "username", ""));
								jo.put("accountPwd", StringUtil.getStringValue(
										internetMap, "password", ""));
								String ip = StringUtil.getStringValue(internetMap, "ip", "");
								if ("NULL".equals(ip)||"null".equals(ip)){
									jo.put("ipAddress","");
								}else{
									jo.put("ipAddress",ip);
								}
								
								jo.put("errorCode", "");// 默认置空
								String dns = StringUtil.getStringValue(internetMap, "dns", "");
								if ("NULL".equals(ip)||"null".equals(ip)){
									jo.put("DNSServers","");
								}else{
									jo.put("DNSServers",dns);
								}
								jo.put("NATEnabled", StringUtil.getStringValue(
										internetMap, "nat_enab", ""));
								jo.put("TotalTerminalNumber ", "");
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
	
	public void function(){
		logger.warn("servicename[QueryNetService]QueryNum[{"+queryNumTmp+"}]采集终端节点值开始");
		rsint = new SuperGatherCorba().getCpeParams(deviceIdTmp, 0, 3); // 在原来基础上增加了一个参数(3)
		timeOutFlag = false;
		logger.warn("servicename[QueryNetService]QueryNum[{"+queryNumTmp+"}]采集终端节点值结束");
	}
}
