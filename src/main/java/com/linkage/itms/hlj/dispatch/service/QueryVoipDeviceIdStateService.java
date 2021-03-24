package com.linkage.itms.hlj.dispatch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.service.ServiceHandle;
import com.linkage.itms.hlj.dispatch.obj.QueryVoipDeviceIdStateChecker;
import com.linkage.itms.obj.ParameValueOBJ;

public class QueryVoipDeviceIdStateService implements HljIService {

	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(QueryVoipDeviceIdStateService.class);
	UserDeviceDAO userDevDao = new UserDeviceDAO();
	ServiceHandle serviceHandle = new ServiceHandle();
	ServUserDAO servUserDao = new ServUserDAO();

	@Override
	public String work(String inXml) {
		QueryVoipDeviceIdStateChecker checker = new QueryVoipDeviceIdStateChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (false == checker.check()) {
				logger.warn("servicename[QueryVoipDeviceIdStateService]cityId[{}]viopNumber[{}]验证未通过，返回：{}",
						new Object[] {checker.getCityId(), checker.getViopNumber(), inXml});
				return checker.getReturnXml();
			}
			logger.warn("servicename[QueryVoipDeviceIdStateService]cityId[{}]viopNumber[{}]参数校验通过，入参为：{}",
					new Object[] {checker.getCityId(), checker.getViopNumber(), inXml});
			
			// 根据终端序列号
			Map<String, String> userMap = userDevDao.queryUserInfo(4, checker.getViopNumber(), checker.getCityId());
			if (null == userMap) {
				logger.warn("servicename[QueryVoipDeviceIdStateService]cityId[{}]viopNumber[{}]查无此用户",
						new Object[] {checker.getCityId(), checker.getViopNumber()});
				checker.setResult(1002);
				checker.setResultDesc("查无此用户");
				writeLog(checker);
				return checker.getReturnXml();
			}
			// 设备Id
			String deviceId = getStringValue(userMap, "device_id");
			if (StringUtil.IsEmpty(deviceId)) {
				logger.warn("servicename[QueryVoipDeviceIdStateService]cityId[{}]viopNumber[{}]用户未绑定设备",
						new Object[] { checker.getCityId(), checker.getViopNumber()});
				checker.setResult(1004);
				checker.setResultDesc("用户未绑定设备");
				writeLog(checker);
				return checker.getReturnXml();
			}
			// 属地不匹配
			if (!serviceHandle.cityMatch(checker.getCityId(), userMap.get("city_id"))) {
				logger.warn("servicename[QueryVoipDeviceIdStateService]cityId[{}]viopNumber[{}]属地不匹配",
						new Object[] { checker.getCityId(), checker.getViopNumber()});
				checker.setResult(1003);
				checker.setResultDesc("属地不匹配");
				writeLog(checker);
				return checker.getReturnXml();
			}
			
			// 校验设备是否在线
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			ACSCorba acsCorba = new ACSCorba();
			
			int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
			// 设备正在被操作，不能获取节点值
			if (-6 == flag) {
				logger.warn("servicename[QueryVoipDeviceIdStateService]cityId[{}]viopNumber[{}]设备正在被操作，无法获取节点值，device_id={}",
						new Object[] { checker.getCityId(), checker.getViopNumber(), deviceId});
				checker.setResult(1013);
				checker.setResultDesc("设备正在被操作");
				return checker.getReturnXml();
			}
			if (1 != flag) {
				logger.warn("servicename[QueryVoipDeviceIdStateService]cityId[{}]viopNumber[{}]设备不在线，无法获取节点值，device_id={}",
						new Object[] { checker.getCityId(), checker.getViopNumber(), deviceId});
				checker.setResult(1014);
				checker.setResultDesc("设备不能正常交互");
				return checker.getReturnXml();
			}
			logger.warn("servicename[QueryVoipDeviceIdStateService]cityId[{}]viopNumber[{}]设备在线，可以进行采集操作，device_id={}",
					new Object[] { checker.getCityId(), checker.getViopNumber(), deviceId});
			
			String ipBNode = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
			String vlanIdMark = ".X_CT-COM_WANGponLinkConfig.VLANIDMark";
			String ipENode = ".WANIPConnection.1.ExternalIPAddress";
			List<String> ipNodeList = acsCorba.getIList(deviceId, ipBNode);
			String ip = null;
			logger.warn("node list:" + ipNodeList);
			for (String node : ipNodeList) {
				logger.warn("node is:" + node);
				ArrayList<ParameValueOBJ> vlanIdList = acsCorba.getValue(deviceId, ipBNode + node + vlanIdMark);
				// vlanIdMark 为43的是语音业务
				if (null != vlanIdList && vlanIdList.size() != 0 
						&& null != vlanIdList.get(0) && "43".equals(vlanIdList.get(0).getValue())) {
					// 获取vlanIdMark 为43的语音ip
					ArrayList<ParameValueOBJ> ipList = acsCorba.getValue(deviceId, ipBNode + node + ipENode);
					if (null != ipList && ipList.size() != 0 
							&& null != ipList.get(0) && null != ipList.get(0).getValue()) {
						ip = ipList.get(0).getValue();
						logger.warn("ipList value:" + ipList);
						logger.warn("ipList value:" + ipList.get(0).getValue());
						break;
					}
				}
			}
			if (ip == null) {
				logger.warn("设备[{}]采集deiveVoipIp失败或者值为空", deviceId);
			}
			else {
				checker.setDeiveVoipIp(ip);
			}
			
			String idNode = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.X_CT-COM_H248.DeviceID";
			ArrayList<ParameValueOBJ> idList = acsCorba.getValue(deviceId, idNode);
			if (null != idList && idList.size() != 0 
					&& null != idList.get(0) && null != idList.get(0).getValue()) {
				checker.setDeiveId(idList.get(0).getValue());
			}
			else {
				logger.warn("设备[{}]采集deiveId失败或者值为空", deviceId);
			}
			
			String statusNode = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line.1.Status";
			ArrayList<ParameValueOBJ> statusList = acsCorba.getValue(deviceId, statusNode);
			if (null != statusList && statusList.size() != 0 
					&& null != statusList.get(0) && null != statusList.get(0).getValue()) {
				checker.setDeiveState(statusList.get(0).getValue());
			}
			else {
				logger.warn("设备[{}]采集deiveState失败或者值为空", deviceId);
			}
			
			checker.setLoid(getStringValue(userMap, "username"));
		}
		catch (Exception e) {
			logger.warn("QueryVoipDeviceIdStateService is error..", e);
		}
		return checker.getReturnXml();
	}
	
	/**
	 * 记录日志
	 * @param returnXml
	 * @param checker
	 * @param name
	 */
	private void writeLog(QueryVoipDeviceIdStateChecker checker) {
		logger.warn("servicename[QueryVoipDeviceIdStateService]cityId[{}]viopNumber[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCityId(), checker.getViopNumber(), checker.getReturnXml()});
	}
	
	/**
	 * 格式化数据
	 * @param map
	 * @param columName
	 * @return
	 */
	public static String getStringValue(Map<String, String> map, String columName) {
		if (null == columName || null == map || null == map.get(columName)) {
			return "";
		}
		return map.get(columName).toString();
	}
}
