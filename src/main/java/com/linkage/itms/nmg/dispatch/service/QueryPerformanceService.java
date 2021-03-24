package com.linkage.itms.nmg.dispatch.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.nmg.dispatch.obj.QueryPerformanceChecker;

/**
 * @author yinlei3 (Ailk No.73167)
 * @version 1.0
 * @since 2016年6月12日
 * @category com.linkage.itms.nmg.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QueryPerformanceService implements IService {

	/** 日志 */
	private static final Logger logger = LoggerFactory
			.getLogger(QueryPerformanceService.class);
	private UserDeviceDAO userDevDao = new UserDeviceDAO();
	private DeviceConfigDAO deviceConfigDao = new DeviceConfigDAO();

	@Override
	public String work(String inXml) {
		QueryPerformanceChecker checker = new QueryPerformanceChecker(inXml);
		ACSCorba corba = new ACSCorba();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		if (!checker.check()) {
			logger.error(
					"serviceName[QueryPerformanceService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"serviceName[QueryPerformanceService]cmdId[{}]userinfo[{}]初始参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		String deviceId = "";
		if (StringUtil.IsEmpty(checker.getDevSn())) {
			Map<String, String> userInfoMap = userDevDao.queryUserInfo(
					checker.getUserInfoType(), checker.getUserInfo());
			if (null == userInfoMap || userInfoMap.isEmpty()) {
				logger.warn(
						"serviceName[QueryPerformanceService]cmdId[{}]userinfo[{}]无此用户",
						new Object[] { checker.getCmdId(),
								checker.getUserInfo() });
				checker.setResult(1007);
				checker.setResultDesc("无此用户信息");
				return checker.getReturnXml();
			} else {
				deviceId = StringUtil.getStringValue(userInfoMap, "device_id");
				if (StringUtil.IsEmpty(deviceId)) {
					logger.warn(
							"serviceName[QueryPerformanceService]cmdId[{}]userinfo[{}]未绑定设备",
							new Object[] { checker.getCmdId(),
									checker.getUserInfo() });
					checker.setResult(1002);
					checker.setResultDesc("此用户未绑定设备");
					return checker.getReturnXml();
				}
			}
		} else {
			// 根据终端序列号查询设备信息表
			ArrayList<HashMap<String, String>> devlist = userDevDao
					.queryDevInfo2(checker.getDevSn());
			if (null == devlist || devlist.isEmpty()) {
				logger.warn(
						"serviceName[QueryPerformanceService]cmdId[{}]DevInfo[{}]查无此设备",
						new Object[] { checker.getCmdId(), checker.getDevSn() });
				checker.setResult(1008);
				checker.setResultDesc("查无此设备");
				return checker.getReturnXml();
			} else if (devlist.size() > 1) {
				logger.warn(
						"servicename[QueryPerformanceService]cmdId[{}]DevInfo[{}]查询到多台设备",
						new Object[] { checker.getCmdId(), checker.getDevSn(),
								inXml });
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				return checker.getReturnXml();
			} else {
				// 查看是否绑定设备
				String userId = StringUtil.getStringValue(devlist.get(0),
						"user_id", "");
				if (StringUtil.IsEmpty(userId)) {
					logger.warn(
							"serviceName[QueryPerformanceService]cmdId[{}]DevInfo[{}]未绑定用户",
							new Object[] { checker.getCmdId(),
									checker.getDevSn() });
					checker.setResult(1009);
					checker.setResultDesc("未绑定用户");
					return checker.getReturnXml();
				} else {
					deviceId = StringUtil.getStringValue(devlist.get(0),
							"device_id");
				}
			}
		}
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);

		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
		// 设备在线
		else if (1 == flag) {
			// 在线状态
			checker.setStatus(0);
			logger.warn(
					"serviceName[QueryPerformanceService]cmdId[{}]userinfo[{}]开始采集[{}]",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							deviceId });
			// 掉CORBAR 采集 0表示采集所有节点 在原来基础上增加了一个参数(3)
			int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3);
			logger.warn(
					"serviceName[QueryPerformanceService]cmdId[{}]userinfo[{}]getCpeParams设备配置信息采集结果[{}]",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							rsint });
			// 采集失败
			if (rsint != 1) {
				logger.warn(
						"serviceName[QueryPerformanceService]cmdId[{}]userinfo[{}]getData sg fail",
						new Object[] { checker.getCmdId(),
								checker.getUserInfo() });
				checker.setResult(1003);
				checker.setResultDesc("设备采集失败");
			} else
			// success
			{
				// 获取PON信息
				Map<String, String> ponInfoMap = deviceConfigDao
						.getPonInfo(deviceId);
				if (null != ponInfoMap) {
					double tx_powerdouble = StringUtil
							.getDoubleValue(ponInfoMap.get("tx_power"));
					double rx_powerdouble = StringUtil
							.getDoubleValue(ponInfoMap.get("rx_power"));
					// 发射光功率
					if (tx_powerdouble > 30) {
						double temp_tx_power = (Math
								.log(tx_powerdouble / 10000) / Math.log(10)) * 10;
						tx_powerdouble = (int) temp_tx_power;
						if (tx_powerdouble % 10 >= 5) {
							tx_powerdouble = (tx_powerdouble / 10 + 1) * 10;
						} else {
							tx_powerdouble = tx_powerdouble / 10 * 10;
						}
					}
					// 接受功率判断
					if (rx_powerdouble > 30) {
						double temp_rx_power = (Math
								.log(rx_powerdouble / 10000) / Math.log(10)) * 10;
						rx_powerdouble = (int) temp_rx_power;
						if (rx_powerdouble % 10 >= 5) {
							rx_powerdouble = (rx_powerdouble / 10 + 1) * 10;
						} else {
							rx_powerdouble = rx_powerdouble / 10 * 10;
						}
					}
					String tx_power = StringUtil.getStringValue(tx_powerdouble);
					String rx_power = StringUtil.getStringValue(rx_powerdouble);
					if ("jl_dx".equals(Global.G_instArea)){
						double bytes_sentdouble = StringUtil
								.getDoubleValue(ponInfoMap.get("bytes_sent"));
						double bytes_receiveddouble = StringUtil
								.getDoubleValue(ponInfoMap.get("bytes_received"));
						DecimalFormat df = new DecimalFormat("0.0");
						String bytes_sent = df.format((bytes_sentdouble/1024)/1024);
						String bytes_received = df.format((bytes_receiveddouble/1024)/1024);
						checker.setBytesSent(bytes_sent);
						checker.setBytesReceived(bytes_received);
					}
					// 接收光功率
					checker.setRXPower(rx_power);
					// 发射光功率
					checker.setTXPower(tx_power);
					// 采集到的光模块的工作温度
					checker.setDeviceTemperature(StringUtil.getStringValue(
							ponInfoMap, "transceiver_temperature", ""));
					// 采集到的光模块的供电电压
					checker.setSupplyVottage(StringUtil.getStringValue(
							ponInfoMap, "supply_vottage", ""));
					// 采集到的光模块的偏置电流
					checker.setBiasCurrent(StringUtil.getStringValue(
							ponInfoMap, "bias_current", ""));

				} else {
					checker.setResult(1012);
					checker.setResultDesc("未采集到设备PON信息");
				}
			}
		}// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法采集");
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}

		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
				"queryPerformance");
		logger.warn("QueryPerformanceService==>ReturnXml:"
				+ checker.getReturnXml());
		return checker.getReturnXml();
	}
}
