package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.OpenFristChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OpenFristService implements IService {
	
	private static final Logger logger = LoggerFactory
			.getLogger(OpenFristService.class);
	
	@Override
	public String work(String inXml) {
		
		logger.warn("OpenFristService：inXml=({})", new Object[]{inXml});

		OpenFristChecker checker = new OpenFristChecker(inXml);
		if (false == checker.check()) {
			logger.error("入参验证未通过，返回：{} \n", checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		logger.warn(corba.toString());
		String deviceId = "";

		//用户信息提取
		Map<String, String> userInfoMap = null;

		// 根据设备序列号查询设备
		if (6 == checker.getUserInfoType()) {
			ArrayList<HashMap<String, String>> arrayList = userDevDao.getDevStatusInfo(checker.getUserInfo());
			if (null == arrayList || arrayList.size() < 1) {
				checker.setResult(1004);
				checker.setResultDesc("此设备不存在");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				userDevDao.insertSchoolDevLog(checker);//日志入库
				return checker.getReturnXml();
			}
			
			userInfoMap = arrayList.get(0);

		}
		// 根据用户信息查询设备
		else {
			// 查询用户信息
			userInfoMap =userDevDao.queryUserInfo(checker
					.getUserInfoType(), checker.getUserInfo());

		}
		// 用户信息不存在
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn("无此客户信息："+checker.getUserInfo());
			checker.setResult(1002);
			checker.setResultDesc("查不到对应的客户信息");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			userDevDao.insertSchoolDevLog(checker);//日志入库
			return checker.getReturnXml();
		}
		// 用户信息存在   再判断此用户是否绑定了设备
		else {
			deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");

			if ("".equals(deviceId)) {
				logger.warn("此用户没有设备关联信息："+checker.getUserInfo());
				checker.setResult(1003);
				checker.setResultDesc("此用户没有设备关联信息");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				userDevDao.insertSchoolDevLog(checker);//日志入库
				return checker.getReturnXml();
			}

			// 判断设备是否在线，只有设备在线，才可以设置设备的节点信息
			int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
			logger.warn("设备编码为{}，设备在线查询返回{}",deviceId,flag);
			// 设备正在被操作
			if (-3 == flag) {
				logger.warn("设备正在被操作，无法设置节点值，device_id={}", deviceId);
				checker.setResult(1008);
				checker.setResultDesc("设备正在被操作");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				userDevDao.insertSchoolDevLog(checker);//日志入库
				return checker.getReturnXml();
			}
			// 设备在线
			else if (1 == flag) {
				logger.warn("设备在线，可以调用工单接口，device_id={}", deviceId);

				ArrayList<ParameValueOBJ> objList = checker.getObjList();

				// 调用Corba 设置节点的值
				logger.warn("调用Corba，设置节点值");
				int retResult = corba.setValue(deviceId, objList);

				if (0 == retResult || 1 == retResult) {
					checker.setResult(0);
					checker.setResultDesc("节点值设置成功");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					userDevDao.insertSchoolDevLog(checker);//日志入库
					return checker.getReturnXml();
				}else if (-1 == retResult) {
					checker.setResult(1009);
					checker.setResultDesc("设备连接失败");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					userDevDao.insertSchoolDevLog(checker);//日志入库
					return checker.getReturnXml();
				}else if (-6 == retResult) {
					checker.setResult(1010);
					checker.setResultDesc("设备正被操作");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					userDevDao.insertSchoolDevLog(checker);//日志入库
					return checker.getReturnXml();
				}else if (-7 == retResult) {
					checker.setResult(1011);
					checker.setResultDesc("系统参数错误");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					userDevDao.insertSchoolDevLog(checker);//日志入库
					return checker.getReturnXml();
				}else if (-9 == retResult) {
					checker.setResult(1012);
					checker.setResultDesc("系统内部错误");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					userDevDao.insertSchoolDevLog(checker);//日志入库
					return checker.getReturnXml();
				}else {
					checker.setResult(1013);
					checker.setResultDesc("TR069错误");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					userDevDao.insertSchoolDevLog(checker);//日志入库
					return checker.getReturnXml();
				}
			}
			// 设备不在线
			else {
				logger.warn("设备不在线，无法设置节点值");
				checker.setResult(1006);
				checker.setResultDesc("设备不在线，无法设置节点值");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				userDevDao.insertSchoolDevLog(checker);//日志入库
				return checker.getReturnXml();
			}
		}
	}
}
