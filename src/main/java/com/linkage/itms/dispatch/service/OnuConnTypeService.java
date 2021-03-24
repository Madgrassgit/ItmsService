
package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.OnuConnTypeChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zzs (Ailk No.78987)
 * @version 1.0
 * @since 2018-11-29
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class OnuConnTypeService implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(OnuConnTypeService.class);
	public static String SERV_LIST_INTERNET = "INTERNET";

	@Override
	public String work(String inXml)
	{
		OnuConnTypeChecker checker = new OnuConnTypeChecker(inXml);
		// 检验参数的合法
		if (!checker.check())
		{
			logger.error(
					"serviceName[OnuConnTypeService]cmdId[{}]userName[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserName(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.info("servicename[OnuConnTypeService]cmdId[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		String deviceId = "";
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn("无此用户信息：" + checker.getUserInfo());
			checker.setResult(1001);
			checker.setResultDesc("无此用户信息");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"OnuConnTypeService");
			return checker.getReturnXml();
		}
		deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");
		logger.warn("获取到的设备id为：" + deviceId);
		if ("".equals(deviceId))
		{
			checker.setResult(1002);
			checker.setResultDesc("此用户未绑定");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"OnuConnTypeService");
			return checker.getReturnXml();
		}
		else
		{
			// 判断设备是否在线，只有设备在线，才可以进行操作
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			ACSCorba corba = new ACSCorba();
			int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
			logger.warn("设备状态为flag：" + flag);
			// 设备正在被操作，不能获取节点值
			if (-3 == flag)
			{
				logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
				checker.setResult(1008);
				checker.setResultDesc("设备正在被操作");
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
						"CustomerTypeOpenService");
				return checker.getReturnXml();
			}
			else if (1 == flag)
			{
				logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
				// 获取conntype的key
				String connTypePath = getPingInterface(deviceId);
				logger.warn("获取上网方式的节点路径为：" + connTypePath);
				ArrayList<ParameValueOBJ> connTypeList = corba.getValue(deviceId,
						connTypePath+".");
				String connType = null;
				if (connTypeList != null && connTypeList.size() != 0)
				{
					for (ParameValueOBJ pvobj : connTypeList)
					{
						if (pvobj.getName().endsWith("ConnectionType"))
						{
							connType = pvobj.getValue();
							logger.warn("connType::"+connType);
						}
					}
				}
				checker.setResult(0);
				checker.setResultDesc("查询wlan连接状态成功！");
				//桥接：PPPoE_Bridged  路由：IP_Routed
				if ("IP_Routed".equals(connType))
				{
					checker.setConnType(2);
				}
				else if ("PPPoE_Bridged".equals(connType))
				{
					checker.setConnType(1);
				}else
				{
					checker.setConnType(0);
					checker.setResult(1);
					checker.setResultDesc("查询wlan连接状态异常！");
				}
				String returnXml = checker.getReturnXml();
				// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, "OnuConnTypeService",
						checker.getUserName());
				logger.warn("servicename[OnuConnTypeService]回参为:{}",
						new Object[] { returnXml });
				return returnXml;
			}
			// 设备不在线，不能获取节点值
			else
			{
				logger.warn("设备不在线，无法获取节点值");
				checker.setResult(1006);
				checker.setResultDesc("设备不在线");
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				return checker.getReturnXml();
			}
		}
	}

	private String getPingInterface(String deviceId)
	{
		ACSCorba corba = new ACSCorba();
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		String SERV_LIST_VOIP = "INTERNET";
		// 江苏可以根据wan连接索引节点来生成上网通道
		String wan_index = "InternetGatewayDevice.WANDevice.1.X_CT-COM_WANIndex";
		String wan_index_result = "";
		logger.warn("[{}]获取wan连接索引", deviceId);
		ArrayList<ParameValueOBJ> valueList = corba.getValue(deviceId, wan_index);
		if (valueList != null && valueList.size() != 0)
		{
			for (ParameValueOBJ pvobj : valueList)
			{
				if (pvobj.getName().endsWith("X_CT-COM_WANIndex"))
				{
					wan_index_result = pvobj.getValue();
					break;
				}
			}
			// "1.1;DHCP_Routed;45;TR069","3.1;Bridged;43;OTHER","4.1;DHCP_Routed;42;VOIP","5.1;PPPoE_Routed;312;INTERNET"
			if (!StringUtil.IsEmpty(wan_index_result))
			{
				String wan[] = wan_index_result.replace("\"", "").split(",");
				for (String wanPa : wan)
				{
					if (wanPa.endsWith(SERV_LIST_INTERNET) || wanPa.endsWith("internet"))
					{
						if (wanPa.contains(".") && wanPa.contains(";"))
						{
							if (wanPa.split(";")[1].equalsIgnoreCase("PPPoE_Routed"))
							{
								String a = wanPa.split(";")[0].split("\\.")[0];
								String b = wanPa.split(";")[0].split("\\.")[1];
								String vlanid = wanPa.split(";")[2];
								String result = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."
										+ a + ".WANPPPConnection." + b + "";
								return result;
							}
						}
					}
				}
			}
		}
		ArrayList<String> wanConnPathsList = new ArrayList<String>();
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		// 直接采集路径名
		if (wanConnPathsList == null || wanConnPathsList.isEmpty())
		{
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.isEmpty())
			{
				logger.warn("[QueryVOIPWanInfoService] [{}]获取{}下实例号失败，返回", deviceId,wanConnPath);
				return null;
			}
			for (String j : jList)
			{
				// 获取wanPPPConnection下的k
				List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j
						+ wanPPPConnection);
				if (null == kPPPList || kPPPList.isEmpty())
				{
					wanConnPathsList.add(wanConnPath + j + wanIPConnection + "1"
							+ wanServiceList);
				}
				else
				{
					for (String kppp : kPPPList)
					{
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp
								+ wanServiceList);
					}
				}
			}
		}
		else
		{
			ArrayList<String> paramNameList = new ArrayList<String>();
			for (int i = 0; i < wanConnPathsList.size(); i++)
			{
				String namepath = wanConnPathsList.get(i);
				if (namepath.indexOf(wanServiceList) >= 0)
				{
					paramNameList.add(namepath);
				}
			}
			wanConnPathsList = new ArrayList<String>();
			wanConnPathsList.addAll(paramNameList);
		}
		if (wanConnPathsList.isEmpty())
		{
			logger.warn("[QueryVOIPWanInfoService] [{}]无节点：{}.j.wanPPPConnection/wanIPConnection.{}下实例号失败，返回",
					deviceId,wanConnPath,wanServiceList);
			return null;
		}
		String[] paramNametemp = new String[wanConnPathsList.size()];
		for (int i = 0; i < wanConnPathsList.size(); i++)
		{
			paramNametemp[i] = wanConnPathsList.get(i);
		}
		Map<String, String> paramValueMap = corba
				.getParaValueMap(deviceId, paramNametemp);
		if (paramValueMap.isEmpty())
		{
			logger.warn("[QueryVOIPWanInfoService] [{}]获取ServiceList失败", deviceId);
		}
		for (Map.Entry<String, String> entry : paramValueMap.entrySet())
		{
			logger.debug("[{}]{}={} ",
					new Object[] { deviceId, entry.getKey(), entry.getValue() });
			// 语音节点
			if (entry.getValue().indexOf(SERV_LIST_VOIP) >= 0)
			{
				return entry.getKey()
						.substring(0, entry.getKey().indexOf(wanServiceList));
			}
		}
		return null;
	}
}
