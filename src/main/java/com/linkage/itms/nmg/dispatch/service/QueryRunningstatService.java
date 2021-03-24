package com.linkage.itms.nmg.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.DevOnlineCAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.nmg.dispatch.obj.QueryRunningstatChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * 家庭网关在线状态查询接口
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-7-31
 */
public class QueryRunningstatService implements IService
{

	private static final Logger logger = LoggerFactory.getLogger(QueryRunningstatService.class);
	
	@Override
	public String work(String inParam)
	{

		logger.warn("QueryRunningstatService==>inParam:" + inParam);
		QueryRunningstatChecker checker = new QueryRunningstatChecker(inParam);
		if (false == checker.check())
		{
			logger.warn("获取家庭网关在线状态查询接口，入参验证失败，UserInfoType=[{}],UserInfo=[{}]",
					new Object[] { checker.getUserInfoType() });
			logger.warn("QueryRunningstatService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 在线情况
		boolean succ = false;
		String deviceId = "";
		String userId = "";
		String strOnline = "";
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		if (StringUtil.IsEmpty(checker.getDevSn()))
		{
			Map<String, String> userInfoMap = userDevDao.queryUserInfo(
					checker.getUserInfoType(), checker.getUserInfo());
			if (null == userInfoMap || userInfoMap.isEmpty())
			{
				logger.warn(
						"serviceName[QueryRunningstatService]cmdId[{}]userinfo[{}]无此用户",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1007);
				checker.setResultDesc("无此用户信息");
				return checker.getReturnXml();
			}
			else
			{
				deviceId = StringUtil.getStringValue(userInfoMap, "device_id");
				userId = StringUtil.getStringValue(userInfoMap, "user_id");
				if (StringUtil.IsEmpty(deviceId))
				{
					logger.warn("serviceName[QueryRunningstatService]cmdId[{}]userinfo[{}]未绑定设备",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1002);
					checker.setResultDesc("此用户未绑定设备");
					return checker.getReturnXml();
				}
				else
				{
					checker.setResult(0);
					checker.setResultDesc("成功");
					Map<String, String> devMap = userDevDao.getDevStatus(userId);
					strOnline = devMap.get("online_status");
					succ = true;
				}
			}
		}
		else
		{
			// 根据终端序列号查询设备信息表
			ArrayList<HashMap<String, String>> devlist = userDevDao.getDevStatusInfo(checker
					.getDevSn());
			if (null == devlist || devlist.isEmpty())
			{
				logger.warn(
						"serviceName[QueryRunningstatService]cmdId[{}]DevInfo[{}]查无此设备",
						new Object[] { checker.getCmdId(), checker.getDevSn() });
				checker.setResult(1008);
				checker.setResultDesc("查无此设备");
				return checker.getReturnXml();
			}
			else if (devlist.size() > 1)
			{
				logger.warn(
						"servicename[QueryRunningstatService]cmdId[{}]DevInfo[{}]查询到多台设备",
						new Object[] { checker.getCmdId(), checker.getDevSn(), inParam });
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				return checker.getReturnXml();
			}
			else
			{
				deviceId = StringUtil.getStringValue(devlist.get(0), "device_id");
				ArrayList<HashMap<String, String>> userList = userDevDao.checkUserByDevId(deviceId);
				if (null == userList || userList.isEmpty())
				{
					logger.warn(
							"serviceName[QueryRunningstatService]cmdId[{}]DevInfo[{}]设备未绑定用户",
							new Object[] { checker.getCmdId(), checker.getDevSn() });
					checker.setResult(1009);
					checker.setResultDesc("设备未绑定用户");
					return checker.getReturnXml();
				}else{
					checker.setResult(0);
					checker.setResultDesc("成功");
					strOnline = devlist.get(0).get("online_status");
					succ = true;
				}
			}
		}
		//成功获取到状态信息
		if (succ)
		{
			// 获取到了在线状态
			int intOnlined = StringUtil.getIntegerValue(strOnline, 100);
			if (0 == intOnlined)
			{
				// 设置参数	不在线
				checker.setStatus(1);
			}
			else
			{
				// 实时获取在线状态 0:在线，1:不在线
				//int iOnline = DevOnlineCAO.devOnlineTest(deviceId) == 1 ? 0 : 1;
				int iOnline = DevOnlineCAO.devOnlineTest(deviceId);
				logger.warn("serviceName[QueryRunningstatService]iOnline[{}]实时获取在线状态 0:在线，1:不在线，-6：设备正忙"+iOnline);
				//修改处
				if(iOnline==-6)
				{
					checker.setResult(1013);
					checker.setResultDesc("设备正在被操作，设备正忙");
					checker.setStatus(0);
				}else if(iOnline==1)
				{
					checker.setStatus(0);
				}else{
					// 设置参数
					checker.setStatus(1);
				}
				
			}
		} else {
			logger.warn(
					"servicename[QueryRunningstatService]cmdId[{}]userinfo[{}]获取在线状态失败",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
		}
		String returnXml = checker.getReturnXml();
		logger.warn("QueryRunningstatService==>returnXML:" + returnXml);
		return returnXml;
	
	}
}
