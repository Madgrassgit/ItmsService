
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commom.util.CheckStrategyUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.DevInfoGatherCheck;

/**
 * 设备采集service实现层
 * 
 * @author xuzhicheng
 * @version 1.0
 * @since 2015年3月24日
 */
public class DevInfoGatherService implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(DevInfoGatherService.class);
	private UserDeviceDAO userDevDao = new UserDeviceDAO();
	ServiceHandle serviceHandle = new ServiceHandle();
	/**
	 * 查询采集DAO
	 */
	private DeviceConfigDAO deviceConfigDao = new DeviceConfigDAO();
	/**
	 * LanInfos
	 */
	List<HashMap<String, String>> lanInfos = new ArrayList<HashMap<String, String>>();

	@Override
	public String work(String inXml)
	{
		DevInfoGatherCheck digcheck = new DevInfoGatherCheck(inXml);
		if (!digcheck.check())
		{
			logger.error(
					"serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { digcheck.getCmdId(), digcheck.getUserInfo(),
							digcheck.getReturnXml() });
			return digcheck.getReturnXml();
		}
		// 验证通过走采集流程
		logger.warn(
				"serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]初始参数校验通过，入参为：{}",
				new Object[] { digcheck.getCmdId(), digcheck.getUserInfo(), inXml });
		// 根据参数查询数据库是否有此设备的信息,根据参数判断哪种查询用户信息
		if (1 == digcheck.getSearchType())
		{
			// 结果集：a.user_id,a.username,a.device_id,a.oui,a.device_serialnumber,a.city_id,a.userline,a.access_style_id
			Map<String, String> userInfoMap = userDevDao.queryUserInfo(
					digcheck.getUserInfoType(), digcheck.getUserName(),
					digcheck.getCityId());
			if (null == userInfoMap || userInfoMap.isEmpty())
			{
				logger.warn("serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]无此用户",
						new Object[] { digcheck.getCmdId(), digcheck.getUserName() });
				digcheck.setResult(1002);
				digcheck.setResultDesc("查无此客户");
				return digcheck.getReturnXml();
			}
			else
			{
				String deviceId = userInfoMap.get("device_id");
				String userCityId = userInfoMap.get("city_id");
				if (StringUtil.IsEmpty(deviceId))
				{
					logger.warn(
							"serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]未绑定设备",
							new Object[] { digcheck.getCmdId(), digcheck.getUserName() });
					digcheck.setResult(1003);
					digcheck.setResultDesc("未绑定设备");
					return digcheck.getReturnXml();
				}
				// (江西)判断设备是否繁忙或者业务正在下发
				if ("jx_dx".equals(Global.G_instArea)
						&& false == CheckStrategyUtil.chechStrategy(deviceId))
				{
					logger.warn(
							"serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]设备繁忙或者业务正在下发，请稍候重试",
							new Object[] { digcheck.getCmdId(), digcheck.getUserName() });
					digcheck.setResult(1003);
					digcheck.setResultDesc("设备繁忙或者业务正在下发，请稍候重试");
					return digcheck.getReturnXml();
				}
				else
				{
					// 江西是根据city_id参数模糊匹配找出的数据,所以没必要在验证city_id
					if (!"jx_dx".equals(Global.G_instArea)
							&& !serviceHandle.cityMatch(digcheck.getCityId(), userCityId))
					{// 属地不匹配
						logger.warn(
								"serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]属地不匹配 ",
								new Object[] { digcheck.getCmdId(),
										digcheck.getUserName() });
						digcheck.setResult(1007);
						digcheck.setResultDesc("属地非法");
						return digcheck.getReturnXml();
					}
					else
					{
						logger.warn(
								"serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]开始采集[{}]",
								new Object[] { digcheck.getCmdId(),
										digcheck.getUserName(), deviceId });
						// 掉CORBAR 采集 0表示采集所有节点 在原来基础上增加了一个参数(3)
						int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3);
						logger.warn(
								"serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]getCpeParams设备配置信息采集结果[{}]",
								new Object[] { digcheck.getCmdId(),
										digcheck.getUserName(), rsint });
						// 采集失败
						if (rsint != 1)
						{
							logger.warn(
									"serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]getData sg fail",
									new Object[] { digcheck.getCmdId(),
											digcheck.getUserName() });
							digcheck.setResult(1008);
							digcheck.setResultDesc("设备采集失败");
							return digcheck.getReturnXml();
						}
						else
						// success
						{
							// 获取PON信息
							Map<String, String> ponInfoMap = deviceConfigDao
									.getPonInfo(deviceId);
							if (null != ponInfoMap)
							{
								double rx_power = StringUtil.getDoubleValue(ponInfoMap
										.get("rx_power"));
								double tx_power = StringUtil.getDoubleValue(ponInfoMap
										.get("tx_power"));
								List<Double> rs = conventPower(tx_power, rx_power);
								digcheck.setPonSend(StringUtil.getStringValue(rs.get(0)));
								digcheck.setPonReceive(StringUtil.getStringValue(rs
										.get(1)));
							}
							// 获取LAN测信息
							ArrayList<HashMap<String, String>> lanInfoList = deviceConfigDao
									.getLanInfos(deviceId);
							setDevInfoGatherCheckLans(digcheck, lanInfoList);
							// 获取voip信息
							ArrayList<HashMap<String, String>> voipInfoList = deviceConfigDao
									.getVoipInfos(deviceId);
							setVoipInfo(digcheck, voipInfoList);
							digcheck.setResult(0);
							digcheck.setResultDesc("成功");
							return digcheck.getReturnXml();
						}
					}
				}
			}
		}
		else if (2 == digcheck.getSearchType())
		{
			// 以设备序列号去查询设备信息
			ArrayList<HashMap<String, String>> devInfo = userDevDao.queryDevInfo(digcheck
					.getDevSn());
			if (null == devInfo || devInfo.size() == 0)
			{
				logger.warn("serviceName[DevInfoGatherService]cmdId[{}]devsn[{}]查无此设备",
						new Object[] { digcheck.getCmdId(), digcheck.getDevSn() });
				digcheck.setResult(10004);
				digcheck.setResultDesc("查无此设备");
				return digcheck.getReturnXml();
			}
			else if (devInfo.size() > 1)
			{
				logger.warn("serviceName[DevInfoGatherService]cmdId[{}]devsn[{}]查无此设备",
						new Object[] { digcheck.getCmdId(), digcheck.getDevSn() });
				digcheck.setResult(10006);
				digcheck.setResultDesc("请输入完整的或更多SN");
				return digcheck.getReturnXml();
			}
			else
			{
				Map<String, String> devMap = devInfo.get(0);
				String deviceCityId = devMap.get("city_id");
				String deviceId = devMap.get("device_id");
				if (!serviceHandle.cityMatch(digcheck.getCityId(), deviceCityId))
				{// 属地不匹配
					logger.warn(
							"serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]属地不匹配 查无此设备:{}",
							new Object[] { digcheck.getCmdId(), digcheck.getUserName(),
									digcheck.getDevSn() });
					digcheck.setResult(1007);
					digcheck.setResultDesc("属地非法");
					return digcheck.getReturnXml();
				}
				// (江西)判断设备是否繁忙或者业务正在下发
				if ("jx_dx".equals(Global.G_instArea)
						&& false == CheckStrategyUtil.chechStrategy(deviceId))
				{
					logger.warn(
							"serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]设备繁忙或者业务正在下发，请稍候重试",
							new Object[] { digcheck.getCmdId(), digcheck.getUserName() });
					digcheck.setResult(1003);
					digcheck.setResultDesc("设备繁忙或者业务正在下发，请稍候重试");
					return digcheck.getReturnXml();
				}
				else
				{// 属地匹配
					// 掉CORBAR 采集 0表示采集所有节点 在原来基础上增加了一个参数(3)
					int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3);
					logger.warn(
							"serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]getCpeParams设备配置信息采集结果[{}]",
							new Object[] { digcheck.getCmdId(), digcheck.getUserName(),
									rsint });
					// 采集失败
					if (rsint != 1)
					{
						logger.warn(
								"serviceName[DevInfoGatherService]cmdId[{}]userinfo[{}]getData sg fail",
								new Object[] { digcheck.getCmdId(),
										digcheck.getUserName() });
						digcheck.setResult(1008);
						digcheck.setResultDesc("设备采集失败");
						return digcheck.getReturnXml();
					}
					else
					// success
					{
						// 获取PON信息
						Map<String, String> ponInfoMap = deviceConfigDao
								.getPonInfo(deviceId);
						if (null != ponInfoMap)
						{
							double rx_power = StringUtil.getDoubleValue(ponInfoMap
									.get("rx_power"));
							double tx_power = StringUtil.getDoubleValue(ponInfoMap
									.get("tx_power"));
							List<Double> rs = conventPower(tx_power, rx_power);
							digcheck.setPonSend(StringUtil.getStringValue(rs.get(0)));
							digcheck.setPonReceive(StringUtil.getStringValue(rs.get(1)));
						}
						// 获取LAN测信息
						ArrayList<HashMap<String, String>> lanInfoList = deviceConfigDao
								.getLanInfos(deviceId);
						setDevInfoGatherCheckLans(digcheck, lanInfoList);
						
						// 获取voip信息
						ArrayList<HashMap<String, String>> voipInfoList = deviceConfigDao
								.getVoipInfos(deviceId);
						setVoipInfo(digcheck, voipInfoList);
						digcheck.setResult(0);
						digcheck.setResultDesc("成功");
						return digcheck.getReturnXml();
					}
				}
			}
		}
		return digcheck.getReturnXml();
	}

	/**
	 * 把查询结果集装到XML中
	 * 
	 * @param digcheck
	 * @param queryList
	 */
	private void setDevInfoGatherCheckLans(DevInfoGatherCheck digcheck,
			ArrayList<HashMap<String, String>> queryList)
	{
		ArrayList<HashMap<String, String>> checkList = new ArrayList<HashMap<String, String>>();
		if (null != queryList && queryList.size() > 0)
		{
			for (int i = 0; i < queryList.size(); i++)
			{
				Map<String, String> rsMap = new HashMap<String, String>();
				rsMap.put("lan", "LAN" + queryList.get(i).get("lan_eth_id"));
				rsMap.put("status", queryList.get(i).get("status"));
				rsMap.put("lanSend", queryList.get(i).get("byte_sent"));
				rsMap.put("lanReceive", queryList.get(i).get("byte_rece"));
				checkList.add((HashMap<String, String>) rsMap);
			}
			digcheck.setLanInfos(checkList);
		}
	}

	/**
	 * 把查询结果集装到XML中
	 * 
	 * @param digcheck
	 * @param queryList
	 */
	private void setVoipInfo(DevInfoGatherCheck digcheck,
			ArrayList<HashMap<String, String>> queryList)
	{
		ArrayList<HashMap<String, String>> checkList = new ArrayList<HashMap<String, String>>();
		if (null != queryList && !queryList.isEmpty())
		{
			for (HashMap<String, String> map : queryList)
			{
				Map<String, String> rsMap = new HashMap<String, String>();
				rsMap.put("voip", StringUtil.getStringValue(StringUtil.getIntValue(map, "line_id")));
				rsMap.put("status", StringUtil.getStringValue(map,"status"));
				rsMap.put("PendingTimerInit", StringUtil.getStringValue(map,"pending_timer_init"));
				rsMap.put("RetranIntervalTimer", StringUtil.getStringValue(map,"retran_interval_timer"));
				
				checkList.add((HashMap<String, String>) rsMap);
			}
			digcheck.setVoipInfos(checkList);
		}
	}

	private List<Double> conventPower(double tx_power, double rx_power)
	{
		List<Double> rs = new ArrayList<Double>();
		if (tx_power > 30)
		{
			double temp_tx_power = (Math.log(tx_power / 10000) / Math.log(10)) * 10;
			tx_power = (int) temp_tx_power;
			if (tx_power % 10 >= 5)
			{
				tx_power = (tx_power / 10 + 1) * 10;
			}
			else
			{
				tx_power = tx_power / 10 * 10;
			}
		}
		if (rx_power > 30)
		{
			double temp_rx_power = (Math.log(rx_power / 10000) / Math.log(10)) * 10;
			rx_power = (int) temp_rx_power;
			if (rx_power % 10 >= 5)
			{
				rx_power = (rx_power / 10 + 1) * 10;
			}
			else
			{
				rx_power = rx_power / 10 * 10;
			}
		}
		rs.add(tx_power);
		rs.add(rx_power);
		return rs;
	}
}
