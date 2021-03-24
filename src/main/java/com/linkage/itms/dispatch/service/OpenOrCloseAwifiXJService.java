package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.OpenOrCloseAwifiChecker;
import com.linkage.itms.obj.ParameValueOBJ;


public class OpenOrCloseAwifiXJService implements IService{
	
	private static final Logger logger = LoggerFactory.getLogger(OpenOrCloseAwifiXJService.class);
	
	
	public String work(String inXml){
		logger.warn("OpenOrCloseAwifiXJ：inXml({})", inXml);
		
		OpenOrCloseAwifiChecker checker = new OpenOrCloseAwifiChecker(inXml);
		
		if (false == checker.check()) {
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		QueryDevDAO qdDao = new QueryDevDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		String deviceId = "";
		List<HashMap<String, String>> userMap = null;
		if (checker.getUserInfoType() == 1)
		{
			userMap = qdDao.queryUserByNetAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 2)
		{
			userMap = qdDao.queryUserByLoid(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 3)
		{
			userMap = qdDao.queryUserByIptvAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 4)
		{
			userMap = qdDao.queryUserByVoipPhone(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 5)
		{
			userMap = qdDao.queryUserByVoipAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 6) {
			String userInfo = checker.getUserInfo();
			userMap = qdDao.queryUserByDevSN(userInfo, userInfo.substring(userInfo.length() - 6));
		}
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(1001);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		if (userMap.size() > 1 && checker.getUserInfoType() != 1)
		{
			checker.setResult(1000);
			checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(1002);
			checker.setResultDesc("未绑定设备");
			return checker.getReturnXml();
		}
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		// 检查设备状态
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		// 设备正在被操作，不能获取节点值
		checker.setResult(1000);
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		// 设备在线
		else if (1 == flag) {
			logger.warn(
					"servicename[OpenOrCloseAwifiXJService]cmdId[{}]userinfo[{}]设备在线，可以设置节点值",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			ParameValueOBJ pvOBJ = new ParameValueOBJ();
			// SSID号
			pvOBJ.setName("InternetGatewayDevice.LANDevice.1.WLANConfiguration." + checker.getSsid()+ ".Enable");
			// 操作类型  2：关闭
			if ("2".equals(checker.getType())) {
				pvOBJ.setValue("0");
			}
			pvOBJ.setType("3");
			
			// 调用Corba 设置节点的值
			logger.warn("servicename[OpenOrCloseAwifiXJService]cmdId[{}]userinfo[{}]调用Corba，设置节点值",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			int retResult = corba.setValue(deviceId, pvOBJ);
			
			if (0 == retResult || 1 == retResult) {
				 checker.setResult(0);
				 checker.setResultDesc("成功");
			}
			else if (-1 == retResult) {
				checker.setResultDesc("设备连接失败");
			}else if (-6 == retResult) {
				checker.setResultDesc("设备正被操作");
			}else if (-7 == retResult) {
				checker.setResultDesc("系统参数错误");
			}else if (-9 == retResult) {
				checker.setResultDesc("系统内部错误");
			}else {
				checker.setResultDesc("TR069错误");
			}
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("servicename[OpenOrCloseAwifiXJService]cmdId[{}]userinfo[{}]设备不在线，无法设置节点值",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResultDesc("设备不在线，无法设置节点值");
		}
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "OpenOrCloseAwifiXJService");
		logger.warn("servicename[OpenOrCloseAwifiXJService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
		return returnXml;
	}
}
