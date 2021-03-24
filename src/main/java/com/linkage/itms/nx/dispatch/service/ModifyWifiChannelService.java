package com.linkage.itms.nx.dispatch.service;

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
import com.linkage.itms.nx.dispatch.obj.ModifyWifiChannelChecker;
import com.linkage.itms.obj.ParameValueOBJ;


/**
 * @author songxq
 * @version 1.0
 * @since 2018-7-26 下午2:29:59
 * @category 
 * @copyright 
 */
public class ModifyWifiChannelService implements NxIService
{
	/** 日志记录 */
	private static Logger logger = LoggerFactory.getLogger(ModifyWifiChannelService.class);
	@Override
	public String work(String inXml)
	{
		logger.warn("ModifyWifiChannelService, inParam:({})", inXml);
		ModifyWifiChannelChecker checker = new ModifyWifiChannelChecker(inXml);
		// 验证入参格式是否正确
		if (false == checker.check())
		{
			logger.error("servicename[ModifyWifiChannelService],cmdId[{}],验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getReturnXml() });
			logger.warn("ModifyWifiChannelService,cmdId[{}],return=({})", checker.getCmdId(),
					checker.getReturnXml());
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
		else if (checker.getUserInfoType() == 3) {
			userMap = qdDao.queryUserInfoByDevSNNX(checker.getUserInfo());
		}
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(1002);
			checker.setResultDesc("查无此客户");
			return checker.getReturnXml();
		}
//		if (userMap.size() > 1)
//		{
//			checker.setResult(1000);
//			checker.setResultDesc("数据不唯一");
//			return checker.getReturnXml();
//		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(1004);
			checker.setResultDesc("此用户未绑定");
			return checker.getReturnXml();
		}
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		// 设备正在被操作，不能获取节点值
		if (-3 == flag)
		{
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1000);
			checker.setResultDesc("设备正在被操作，不能正常交互");
			checker.setFailReason("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
		// 设备在线
		else if (1 == flag)
		{
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			/*String channelPath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration."
					+ checker.getSsidType() + ".Channel";*/
			String channelPath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.Channel";
			String channelValue = checker.getChannel();
			ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, channelPath);
//			List<String> iList = corba.getIList(deviceId, channelPath);
			if (null == objLlist || objLlist.isEmpty())
			{
				logger.warn("[{}]获取objLlist失败，返回", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("请确认节点路径是否正确");
				return checker.getReturnXml();
			}
			else
			{
				logger.warn("[{}]获取objLlist成功，objLlist.size={}", deviceId, objLlist.size());
				ParameValueOBJ pvOBJ = new ParameValueOBJ();
				pvOBJ.setName(channelPath);
				pvOBJ.setValue(channelValue);
				
				// 设置参数的类型为3：unsignedInt（电信规范文档中信道 Channel 参数的类型为 unsignedInt）
				pvOBJ.setType("3");
				int retResult = corba.setValue(deviceId, pvOBJ);
				//如果是自动分配信道 那么实际信道则需要通过路径获取  
				/*
				//InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.ChannelsInUse
				if("0".equals(channelValue))
				{
					logger.warn("channel    0 ");
					String channelIsInUsePath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.ChannelsInUse";
					String channelIsInUse = "";
					ArrayList<ParameValueOBJ> objLlist1 = corba.getValue(deviceId, channelIsInUsePath);
					if(null != objLlist && !objLlist.isEmpty())
					{
						channelIsInUse = objLlist1.get(0).getValue();
						logger.warn(" channelIsInUse     "+channelIsInUse);
						checker.setChannelsInUse(channelIsInUse);
					}
				}*/
				
				pvOBJ = null;
				if (0 == retResult || 1 == retResult)
				{
					checker.setResult(0);
					checker.setResultDesc("成功");
					String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
							"ModifyWifiChannelService");
					logger.warn(
							"servicename[ModifyWifiChannelService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),
									returnXml });
					return returnXml;
				}
				else if (-1 == retResult || -6 == retResult)
				{
					checker.setResult(1000);
					checker.setResultDesc("设备不能正常交互");
					checker.setFailReason("设备不能正常交互");
					String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
							"ModifyWifiChannelService");
					logger.warn(
							"servicename[ModifyWifiChannelService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),
									returnXml });
					return returnXml;
				}
				else if (-7 == retResult)
				{
					checker.setResult(1000);
					checker.setResultDesc("系统参数错误");
					checker.setFailReason("系统参数错误");
					String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
							"ModifyWifiChannelService");
					logger.warn(
							"servicename[ModifyWifiChannelService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),
									returnXml });
					return returnXml;
				}
				else if (-9 == retResult)
				{
					checker.setResult(1000);
					checker.setResultDesc("系统内部错误");
					checker.setFailReason("系统内部错误");
					String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
							"ModifyWifiChannelService");
					logger.warn(
							"servicename[ModifyWifiChannelService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),
									returnXml });
					return returnXml;
				}
				else
				{
					checker.setResult(1000);
					checker.setResultDesc("TR069错误");
					String returnXml = checker.getReturnXml();
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
							"ModifyWifiChannelService");
					logger.warn(
							"servicename[ModifyWifiChannelService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),
									returnXml });
					return returnXml;
				}
			}
		}
		// 设备不在线，不能获取节点值
		else
		{
			logger.warn("设备不在线，无法获取节点值,device_id={}", deviceId);
			checker.setResult(1000);
			checker.setResultDesc("设备不在线");
			checker.setFailReason("设备不在线");
			logger.warn("return=({})", checker.getReturnXml());
			return checker.getReturnXml();
		}
	}
	
}

