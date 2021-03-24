package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.PreProcessInterface;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UpgradeToStandardVersionDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.UpgradeToStandardVersionChecker;



public class UpgradeToStandardVersionService implements IService {
	
	private static final Logger logger = LoggerFactory
			.getLogger(UpgradeToStandardVersionService.class);
	
	public String work(String inXml) {
		String retXml = "";   // 回参
UpgradeToStandardVersionChecker checker = new UpgradeToStandardVersionChecker(inXml);
		
		if (false == checker.check()) {
			retXml = checker.getReturnXml();
			logger.error(
					"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return retXml;
		}
		logger.warn(
				"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						inXml });
		try
		{
		
		
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker
				.getUserInfoType(), checker.getUserInfo());

		if (null == userInfoMap || userInfoMap.isEmpty()
				|| StringUtil.IsEmpty(userInfoMap.get("user_id"))) {
			logger.warn(
					"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1003);
			checker.setResultDesc("查无此用户");
			
		} else {
			
			String deviceId = userInfoMap.get("device_id");   // 设备ID
			
			// 用户未绑定设备
			if (StringUtil.IsEmpty(deviceId)) {
				logger.warn(
						"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]用户为绑定设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1009);
				checker.setResultDesc("用户未绑定设备");
			}
			// 绑定了设备
			else {
				
				UpgradeToStandardVersionDAO dao = new UpgradeToStandardVersionDAO();
				
				// 根据device_id 查询设备版本信息
				// 综调软件功能接口，机制需要调整下，升级通过版本对应关系
				//（以前是通过一个型号一个规范版本，但是发现现网一个型号存在江苏版本和苏州版本）
				List<HashMap<String, String>> versionList = dao.queryStandardVersionByDevSn(deviceId);
				
				if (null == versionList || versionList.size() < 1) {
					checker.setResult(1006);
					checker.setResultDesc("无此设备信息");
					logger.warn(
							"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]设备不存在",
							new Object[] { checker.getCmdId(), checker.getUserInfo()});
				}else {
					HashMap<String, String> infoMap = versionList.get(0);
					
					String devicetypeId = StringUtil.getStringValue(infoMap, "devicetype_id", "");
					
					if ("".equals(devicetypeId)) {
						checker.setResult(1007);
						checker.setResultDesc("无对应的版本");
						retXml = checker.getReturnXml();
						logger.warn(
								"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]设备表中查不到对因的版本ID，devicetype_id：{}",
								new Object[] { checker.getCmdId(), checker.getUserInfo(),devicetypeId});
						// 记录日志
						new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "UpgradeToStandardVersionService");
						logger.warn(
								"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
								new Object[] { checker.getCmdId(), checker.getUserInfo(),retXml});
						return retXml;
					}
					
					/**
					 * 根据设备表中的devicetype_id(型号版本ID)查询软件升级目标版本表(gw_soft_upgrade_temp_map)
					 * 判断当前版本是否需要升级，目标版本是否存在
					 * 
					 * 需求单：JSDX_ITMS-REQ-20130425-WUH-001
					 * 
					 * add by zhangchy 2013-04-24  begin
					 */
					List<HashMap<String, String>> destinationVersionList = dao.queryDestinationVersion(devicetypeId);
					
					if (null == destinationVersionList || destinationVersionList.size() < 1) {
						checker.setResult(1005);
						checker.setResultDesc("没有升级目标版本");
						retXml = checker.getReturnXml();
						logger.warn(
								"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]版本对应关系表中没查询到对应的版本",
								new Object[] { checker.getCmdId(), checker.getUserInfo()});
						// 记录日志
						new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "UpgradeToStandardVersionService");
						logger.warn(
								"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
								new Object[] { checker.getCmdId(), checker.getUserInfo(),retXml});
						return retXml;
					}
					
					if (destinationVersionList.size() > 1) {
						checker.setResult(1008);
						checker.setResultDesc("此设备存在多个目标版本");
						retXml = checker.getReturnXml();
						logger.warn(
								"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]异常：此设备存在多个目标版本，无法升级",
								new Object[] { checker.getCmdId(), checker.getUserInfo()});
						// 记录日志
						new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "UpgradeToStandardVersionService");
						logger.warn(
								"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
								new Object[] { checker.getCmdId(), checker.getUserInfo(),retXml});
						return retXml;
					}
					
					// 版本对应关系表中的目标版本ID
					Map<String, String> destinationVersionMap = destinationVersionList.get(0);
					String newDevicetypeId = StringUtil.getStringValue(destinationVersionMap, "devicetype_id");
					
					// 查询目标版本文件
					List<HashMap<String, String>> softWareFileList = dao.querySoftwareFile(newDevicetypeId);
					if (null == softWareFileList || softWareFileList.size() < 1) {
						checker.setResult(1005);
						checker.setResultDesc("没有升级目标版本(版本文件不存在)");
						retXml = checker.getReturnXml();
						logger.warn(
								"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]目标版本文件不存在",
								new Object[] { checker.getCmdId(), checker.getUserInfo()});
						// 记录日志
						new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "UpgradeToStandardVersionService");
						logger.warn(
								"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
								new Object[] { checker.getCmdId(), checker.getUserInfo(),retXml});
						return retXml;
					}
					
					Map<String, String > softWareFileMap = softWareFileList.get(0);
					logger.warn(
							"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]目标版本信息:{}",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),softWareFileMap});
					String[] paramArr = new String[] {
							StringUtil.getStringValue(softWareFileMap, "devicetype_id"),
							StringUtil.getStringValue(softWareFileMap, "file_url"),
							StringUtil.getStringValue(softWareFileMap, "softwarefile_size"),
							StringUtil.getStringValue(softWareFileMap, "softwarefile_name") };
					/**
					 * 需求单：JSDX_ITMS-REQ-20130425-WUH-001
					 * add by zhangchy 2013-04-24  end
					 */
					logger.warn(
							"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]参数组装:{}",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),paramArr});
					PreProcessInterface SoftUpCorba = CreateObjectFactory.createPreProcess();
					logger.warn(
							"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]参数组装1:{}",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),paramArr});
					// 调用软件升级模块
					boolean result = SoftUpCorba.processDeviceStrategy(new String[] { deviceId },
							"ITMSService_5", paramArr);
					logger.warn(
							"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]调用后台结果:{}",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),result});
					if (result) {
						checker.setResult(0);
						checker.setResultDesc("调用成功");
					}
					else
					{
						checker.setResult(1003);
						checker.setResultDesc("调用失败");
					}
//					if (result == -2) {
//						checker.setResult(1007);
//						checker.setResultDesc("调用失败");
//					}
						
				}
			}
		}
		
		retXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "UpgradeToStandardVersionService");
		logger.warn(
				"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),retXml});
		}
		catch (Exception e)
		{
			logger.error(
					"servicename[UpgradeToStandardVersionService]cmdId[{}]userinfo[{}]位置异常，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return retXml;
	}
	
}
