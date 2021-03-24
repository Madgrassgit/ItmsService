
package com.linkage.itms.hlj.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.QueryPerformanceChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-2
 * @category com.linkage.itms.hlj.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QueryPerformanceService implements HljIService
{

	/** 日志 */
	private static final Logger logger = LoggerFactory
			.getLogger(QueryPerformanceService.class);

	@Override
	public String work(String jsonString)
	{
		logger.warn("QueryPerformanceService——》jsonString" + jsonString);
		QueryPerformanceChecker checker = new QueryPerformanceChecker(jsonString);
		if (false == checker.check())
		{
			logger.warn("家庭网关性能指标查询接口，入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("QueryPerformanceService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		String deviceId = "";
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
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		List<HashMap<String, String>> deviceInfoMap = qdDao.queryInfoByDevId(deviceId);
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
		else if (1 == flag)
		{
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			String lanPath = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.";
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
			String[] gatherPath = new String[] {
					"InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.Status",
					"InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.TXPower",
					"InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.RXPower",
					"InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.TransceiverTemperature",
					"InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.SupplyVottage",
					"InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.BiasCurrent" };
			ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
			String[] gatherPath2 = new String[] {
					"InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.CPURate",
					"InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.MemRate" };
			ArrayList<ParameValueOBJ> objLlist2 = corba.getValue(deviceId, gatherPath2);
			String status = "";
			String tx_power = "";
			String rx_power = "";
			String transceiver_temperature = "";
			String supply_vottage = "";
			String memRate = "";
			String cpuRate = "";
			String bias_current = "";
			JSONObject jo = new JSONObject();
			JSONObject termLineInfo = new JSONObject();
			JSONObject lineDetailInfo = new JSONObject();
			try
			{
				jo.put("resultCode", 0);
				jo.put("streamingNum", checker.getStreamingNum());
				jo.put("cpeOnlineTime", new DateTimeUtil().getLongDate(StringUtil.getLongValue(StringUtil.getStringValue(deviceInfoMap.get(0),
						"cpe_currentupdatetime", ""))));
				int devOnlineStatus = 1;
				devOnlineStatus = Integer.parseInt(deviceInfoMap.get(0).get(
						"online_status")) == 1 ? 0 : 1;
				jo.put("termFlag", devOnlineStatus);
				jo.put("termLineInfo", termLineInfo);
				termLineInfo.put("lineStatus", "");
				termLineInfo.put("accessType", 1);
				termLineInfo.put("lineDetailInfo", lineDetailInfo);
				lineDetailInfo.put("PortFlag", "");
				lineDetailInfo.put("LineStatus", "");
				if (null == objLlist || objLlist.isEmpty())
				{
					logger.warn("objLlist为空");
				}
				else
				{
					logger.warn("objLlist：{}",objLlist);
					for (ParameValueOBJ obj : objLlist)
					{
						if (obj.getName().endsWith("Status"))
						{
							status = obj.getValue();
						}
						else if (obj.getName().endsWith("TXPower"))
						{
							tx_power = obj.getValue();
						}
						else if (obj.getName().endsWith("RXPower"))
						{
							rx_power = obj.getValue();
						}
						else if (obj.getName().endsWith("TransceiverTemperature"))
						{
							transceiver_temperature = obj.getValue();
						}
						else if (obj.getName().endsWith("SupplyVottage"))
						{
							supply_vottage = obj.getValue();
						}
						else if (obj.getName().endsWith("BiasCurrent"))
						{
							bias_current = obj.getValue();
						}
					}
				}
				if (null == objLlist2 || objLlist2.isEmpty())
				{
					logger.warn("objLlist2为空,暂时采集不到内存和cpu");
				}
				else
				{
					logger.warn("objLlist2：{}",objLlist2);
					for (ParameValueOBJ obj2 : objLlist)
					{
						if (obj2.getName().endsWith("CPURate"))
						{
							cpuRate = obj2.getValue();
						}
						else if (obj2.getName().endsWith("MemRate"))
						{
							memRate = obj2.getValue();
						}
					}
				}
				double tx_powerdouble = StringUtil.getDoubleValue(tx_power);
				double rx_powerdouble = StringUtil.getDoubleValue(rx_power);
				double transceiver_temperaturedouble = StringUtil.getDoubleValue(transceiver_temperature);
				double supply_vottagedouble = StringUtil.getDoubleValue(supply_vottage);
				double bias_currentdouble = StringUtil.getDoubleValue(bias_current);
				// 发射光功率
				if (tx_powerdouble > 30)
				{
					double temp_tx_power = (Math.log(tx_powerdouble / 10000) / Math
							.log(10)) * 10;
					tx_powerdouble = (int) temp_tx_power;
					if (tx_powerdouble % 10 >= 5)
					{
						tx_powerdouble = (tx_powerdouble / 10 + 1) * 10;
					}
					else
					{
						tx_powerdouble = tx_powerdouble / 10 * 10;
					}
				}
				// 接受功率判断
				if (rx_powerdouble > 30)
				{
					double temp_rx_power = (Math.log(rx_powerdouble / 10000) / Math
							.log(10)) * 10;
					rx_powerdouble = (int) temp_rx_power;
					if (rx_powerdouble % 10 >= 5)
					{
						rx_powerdouble = (rx_powerdouble / 10 + 1) * 10;
					}
					else
					{
						rx_powerdouble = rx_powerdouble / 10 * 10;
					}
				}
				transceiver_temperaturedouble= transceiver_temperaturedouble/256;
				supply_vottagedouble = supply_vottagedouble/10000;
				bias_currentdouble = bias_currentdouble/500;
				
				lineDetailInfo.put("Status", status);
				lineDetailInfo.put("TxPower", tx_powerdouble);
				lineDetailInfo.put("RxPower", rx_powerdouble);
				lineDetailInfo.put("TransceiverTemperature", transceiver_temperaturedouble);
				lineDetailInfo.put("SupplyVottage", supply_vottagedouble);
				lineDetailInfo.put("BiasCurrent", bias_currentdouble);
				lineDetailInfo.put("CpuRate", cpuRate);
				lineDetailInfo.put("MemRate", memRate);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			return jo.toString();
		}
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
