package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.ModifyWifiPowerChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModifyWifiPowerService implements IService {
	private static Logger logger = LoggerFactory.getLogger(ModifyWifiPowerService.class);

	@Override
	public String work(String inXml) {
		logger.warn("ModifyWifiPowerService==>inXml({})", inXml);
		ModifyWifiPowerChecker checker = new ModifyWifiPowerChecker(inXml);
		if (false == checker.check()) {
			logger.warn("servicename[ModifyWifiPowerService] cmdId[{}] userInfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml() });
			return checker.getReturnXml();
		}

		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		Map<String, String> userInfoMap = deviceInfoDAO.queryUserInfoForGS(checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.size() == 0) {
			checker.setResult(1002);
			checker.setResultDesc("查无此客户");
			logger.warn("servicename[ModifyWifiPowerService] cmdId[{}] userInfo[{}]查无此客户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		// 设备不存在
		if (StringUtil.IsEmpty(StringUtil.getStringValue(userInfoMap, "device_id", ""))) {
			checker.setResult(1003);
			checker.setResultDesc("未绑定设备");
			logger.warn("servicename[ModifyWifiPowerService] cmdId[{}] userInfo[{}]查无此设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}

		String deviceId = StringUtil.getStringValue(userInfoMap, "device_id");

		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (-6 == flag) {
			checker.setResult(1007);
			checker.setResultDesc("设备正在被操作");
			logger.warn("servicename[ModifyWifiPowerService] cmdId[{}] userInfo[{}]设备正在被操作，无法获取节点值",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
		}
		else if (1 == flag) {
			logger.warn("[{}]设备在线，可以进行操作", deviceId);

			String wanConnPath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.";
			String wifiNamePath = ".SSID";
			String wifiPowerPath = ".X_CT-COM_Powerlevel";

			ArrayList<String> wanConnPathsList = null;
			wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
			if (wanConnPathsList == null || wanConnPathsList.size() == 0 || wanConnPathsList.isEmpty()) {
				logger.warn("[ModifyWifiPowerService] [{}]获取 WLANConfiguration 下所有节点路径失败，逐层获取", deviceId);
				// 采集wanConnPath下实例
				wanConnPathsList = new ArrayList<String>();
				List<String> jList = corba.getIList(deviceId, wanConnPath);
				if (null == jList || jList.size() == 0 || jList.isEmpty())
				{
					logger.warn("[ModifyWifiPowerService] [{}]获取{}下实例号失败，返回", new Object[]{deviceId, wanConnPath});
					checker.setResult(1000);
					checker.setResultDesc("获取 " + deviceId + "," + wanConnPath + " 下实例号失败");
					return checker.getReturnXml();
				}
				for (String j : jList)
				{
					wanConnPathsList.add(wanConnPath + j + wifiNamePath);
				}
			}

			// 找到输入的wifiName对应的wifi信道
			for (String namePath : wanConnPathsList) {
				// 找到wifi名字的路径
				if (namePath.indexOf(wifiNamePath) >= 0) {
					ArrayList<ParameValueOBJ> nameObjLlist = corba.getValue(deviceId, namePath);
					if (null == nameObjLlist || nameObjLlist.isEmpty()) {
						logger.warn("[{}]获取 nameObjLlist 失败，返回", deviceId);
						checker.setResult(1000);
						checker.setResultDesc("请确认name节点路径是否正确");
						return checker.getReturnXml();
					}
					else {
						logger.warn("[{}]获取 nameObjLlist 成功，nameObjLlist.size={}", deviceId, nameObjLlist.size());

						String wifiName = nameObjLlist.get(0).getValue();
						// 找到输入的wifiName对应的wifi信道
						if (wifiName.equals(checker.getWifiName())) {
							String powerPathTemp = namePath.replace(wifiNamePath, wifiPowerPath);

							// 判断信道路径是否正确，正确再去修改强度
							ArrayList<ParameValueOBJ> powerObjLlist = corba.getValue(deviceId, powerPathTemp);
							if (null == powerObjLlist || powerObjLlist.isEmpty()) {
								logger.warn("[{}]获取 powerObjLlist 失败，返回", deviceId);
								checker.setResult(1000);
								checker.setResultDesc("请确认power节点路径是否正确");
								return checker.getReturnXml();
							}

							// 去修改强度
							modifyWifiPower(powerPathTemp, checker, corba, deviceId);
							break;
						}
						else {
							checker.setResult(1000);
							checker.setResultDesc("Wifi名称在设备中不存在");
						}
					}
				}
			}
		}
		else {
			checker.setResult(1005);
			checker.setResultDesc("设备不在线");
			logger.warn("servicename[ModifyWifiPowerService] cmdId[{}] userInfo[{}]设备不在线",new Object[] { checker.getCmdId(), checker.getUserInfo() });
		}
		logger.warn("servicename[ModifyWifiPowerService] cmdId[{}] userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), checker.getUserInfo(),
						checker.getReturnXml() });
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "maxConnectionNumber");
		return checker.getReturnXml();
	}


	private void modifyWifiPower(String powerPathTemp, ModifyWifiPowerChecker checker, ACSCorba corba, String deviceId) {
		ParameValueOBJ pvOBJ = new ParameValueOBJ();
		pvOBJ.setName(powerPathTemp);
		pvOBJ.setValue(String.valueOf(checker.getWifiPower()));
		// 设置参数的类型为3：unsignedInt（电信规范文档中信道 Channel 参数的类型为 unsignedInt）
		pvOBJ.setType("3");
		int retResult = corba.setValue(deviceId, pvOBJ);

		if (0 == retResult || 1 == retResult)
		{
			checker.setResult(0);
			checker.setResultDesc("成功");
		}
		else if (-1 == retResult || -6 == retResult)
		{
			checker.setResult(1000);
			checker.setResultDesc("设备不能正常交互");
		}
		else if (-7 == retResult)
		{
			checker.setResult(1000);
			checker.setResultDesc("系统参数错误");
		}
		else if (-9 == retResult)
		{
			checker.setResult(1000);
			checker.setResultDesc("系统内部错误");
		}
		else
		{
			checker.setResult(1000);
			checker.setResultDesc("TR069错误");
		}
	}

}
