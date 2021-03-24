
package com.linkage.itms.nmg.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.nmg.dispatch.obj.QueryWanStateChecker;
import com.linkage.itms.obj.ParameValueOBJ;

public class QueryWanStateService implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(QueryWanStateService.class);

	public String work(String inXml)
	{
		logger.warn("QueryWanStateService({})", inXml);
		QueryWanStateChecker checker = new QueryWanStateChecker(inXml);
		if (false == checker.check())
		{
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
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
		else
		{
		}
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(1004);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		if (userMap.size() > 1)
		{
			checker.setResult(1006);
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
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		// 设备正在被操作，不能获取节点值
		if (-3 == flag)
		{
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
		// 设备在线
		else if (1 == flag)
		{
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			
			List<HashMap<String, String>> wanList = new ArrayList<HashMap<String, String>>();
			String[] gatherPath = new String[] {
					"InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.PhysicalLinkStatus",
					"InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.TotalBytesReceived",
					"InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.TotalBytesSent" };
			ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
			if (null == objLlist || objLlist.isEmpty())
			{
				logger.warn("[{}]获取节点失败，返回", deviceId);
				checker.setResult(1007);
				checker.setResultDesc("设备不支持Wan口状态查询");
				return checker.getReturnXml();
			}else
			{
				logger.warn("[{}]获取iList成功，objLlist={}", deviceId, objLlist.toString());
			}
			String status = "";
			String received = "";
			String sent = "";
			for (ParameValueOBJ pvobj : objLlist)
			{
				if (pvobj.getName().contains("PhysicalLinkStatus"))
				{
					status = pvobj.getValue();
				}
				else if (pvobj.getName().contains("TotalBytesReceived"))
				{
					received = pvobj.getValue();
				}
				else if (pvobj.getName().contains("TotalBytesSent"))
				{
					sent = pvobj.getValue();
				}
			}
			HashMap<String, String> tmp = new HashMap<String, String>();
			tmp.put("RstState", status);
			tmp.put("BytesReceived", received);
			tmp.put("BytesSent", sent);
			wanList.add(tmp);
			checker.setWanList(wanList);
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"QueryLanStateService");
			return checker.getReturnXml();
		}
		// 设备不在线，不能获取节点值
		else
		{
			logger.warn("设备不在线，无法获取节点值");
			checker.setResult(1008);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
	}
}
