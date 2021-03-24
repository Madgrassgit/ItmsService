
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.OpenIgmpSnoopingChecker;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * @author zhaixx (Ailk No.)
 * @version 1.0
 * @since 2018年10月25日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class OpenIgmpSnoopingService {

	private static final Logger logger = LoggerFactory.getLogger(OpenIgmpSnoopingService.class);

	public String work(String inXml) {
		logger.warn("OpenIgmpSnoopingService,inXml:({})", inXml);
		OpenIgmpSnoopingChecker checker = new OpenIgmpSnoopingChecker(inXml);
		if (false == checker.check()) {
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		String deviceId = "";
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn("无此用户信息：" + checker.getUserInfo());
			checker.setResult(1001);
			checker.setResultDesc("无此用户信息");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "CustomerTypeOpenService");
			return checker.getReturnXml();
		}
		else {
			deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");
			if ("".equals(deviceId)) {
				checker.setResult(1002);
				checker.setResultDesc("此用户未绑定");
				logger.warn("return=({})", checker.getReturnXml()); // 打印回参
				new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "CustomerTypeOpenService");
				return checker.getReturnXml();
			}
			else {
				// 判断设备是否在线，只有设备在线，才可以设置设备的节点信息
				GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
				ACSCorba corba = new ACSCorba();
				int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
				// 设备正在被操作，不能设置节点值
				if (-3 == flag) {
					logger.warn("设备正在被操作，无法设置节点值，device_id={}", deviceId);
					checker.setResult(1008);
					checker.setResultDesc("设备正在被操作");
					logger.warn("return=({})", checker.getReturnXml()); // 打印回参
					new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "CustomerTypeOpenService");
					return checker.getReturnXml();
				}
				// 设备在线
				else if (1 == flag) {
					// 设备在线
					logger.warn("设备在线，可以设置节点值，device_id={}", deviceId);
					ArrayList<ParameValueOBJ> objList = new ArrayList<ParameValueOBJ>();
					String igmpPath = "InternetGatewayDevice.Services.X_CT-COM_IPTV.IGMPEnable";
					String igmpvalue = "1";
					String snoopingPath = "InternetGatewayDevice.Services.X_CT-COM_IPTV.SnoopingEnable";
					String snoopingvalue = "1";
					// 设置节点值
					ParameValueOBJ pvOBJ = new ParameValueOBJ();
					pvOBJ.setName(igmpPath);
					pvOBJ.setValue(igmpvalue);
					// 设置参数的类型为4
					pvOBJ.setType("4");
					objList.add(pvOBJ);
					ParameValueOBJ pvOBJ1 = new ParameValueOBJ();
					pvOBJ1.setName(snoopingPath);
					pvOBJ1.setValue(snoopingvalue);
					// 设置参数的类型为4
					pvOBJ1.setType("4");
					objList.add(pvOBJ1);
					int retResult = corba.setValue(deviceId, objList);
					logger.warn("return acs ..... retResult....:{} ", retResult);
					pvOBJ = null;
					pvOBJ1 = null;
					objList = null;
					if (0 == retResult || 1 == retResult) {
						checker.setResult(0);
						checker.setResultDesc("成功");
						String returnXml = checker.getReturnXml();
						// 记录日志
						new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "OpenIgmpSnoopingService");
						logger.warn("servicename[OpenIgmpSnoopingService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
								new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
						return returnXml;
					}
					else if (-1 == retResult || -6 == retResult) {
						checker.setResult(1000);
						checker.setResultDesc("设备不能正常交互");
						String returnXml = checker.getReturnXml();
						// 记录日志
						new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "OpenIgmpSnoopingService");
						logger.warn("servicename[OpenIgmpSnoopingService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
								new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
						return returnXml;
					}
					else if (-7 == retResult) {
						checker.setResult(1000);
						checker.setResultDesc("系统参数错误");
						String returnXml = checker.getReturnXml();
						// 记录日志
						new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "OpenIgmpSnoopingService");
						logger.warn("servicename[OpenIgmpSnoopingService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
								new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
						return returnXml;
					}
					else if (-9 == retResult) {
						checker.setResult(1000);
						checker.setResultDesc("系统内部错误");
						String returnXml = checker.getReturnXml();
						// 记录日志
						new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "OpenIgmpSnoopingService");
						logger.warn("servicename[OpenIgmpSnoopingService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
								new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
						return returnXml;
					}
					else {
						checker.setResult(1000);
						checker.setResultDesc("TR069错误");
						String returnXml = checker.getReturnXml();
						// 记录日志
						new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "OpenIgmpSnoopingService");
						logger.warn("servicename[OpenIgmpSnoopingService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
								new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
						return returnXml;
					}
				}
				// 设备不在线，不能获取节点值
				else {
					logger.warn("设备不在线，无法获取节点值");
					checker.setResult(1006);
					checker.setResultDesc("设备不在线");
					logger.warn("return=({})", checker.getReturnXml()); // 打印回参
					return checker.getReturnXml();
				}
			}
		}
	}
}
