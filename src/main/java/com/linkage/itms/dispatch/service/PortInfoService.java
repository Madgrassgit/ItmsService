package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.PortInfoChecker;
		
public class PortInfoService implements IService
{

	private static final Logger logger = LoggerFactory.getLogger(PortInfoService.class);
    private UserDeviceDAO userDevDao = new UserDeviceDAO();
	private DeviceConfigDAO deviceConfigDao = new DeviceConfigDAO();
	@Override
	public String work(String inXml)
	{
		PortInfoChecker checker = new PortInfoChecker(inXml);
		if (!checker.check()) 
		{
			logger.error("serviceName[PortInfoService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
			new Object[] { checker.getCmdId(), checker.getUserInfo(),checker.getReturnXml() });
			return checker.getReturnXml();
		}
		//验证通过走采集流程
		logger.warn("serviceName[PortInfoService]cmdId[{}]userinfo[{}]初始参数校验通过，入参为：{}",
		new Object[] { checker.getCmdId(), checker.getUserInfo(),inXml });
		// 结果集：a.user_id,a.username,a.device_id,a.oui,a.device_serialnumber,a.city_id,a.userline,a.access_style_id
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn("serviceName[PortInfoService]cmdId[{}]userinfo[{}]无此用户",
			new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		else
		{
			String deviceId = StringUtil.getStringValue(userInfoMap,"device_id");
			if (StringUtil.IsEmpty(deviceId))
			{
				logger.warn("serviceName[PortInfoService]cmdId[{}]userinfo[{}]未绑定设备",
				new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1004);
				checker.setResultDesc("此用户未绑定设备");
				return checker.getReturnXml();
			}
			else
			{
				logger.warn("serviceName[PortInfoService]cmdId[{}]userinfo[{}]开始采集[{}]",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),deviceId});
				//掉CORBAR 采集    0表示采集所有节点 在原来基础上增加了一个参数(3)
				int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3); 
				logger.warn(
						"serviceName[PortInfoService]cmdId[{}]userinfo[{}]getCpeParams设备配置信息采集结果[{}]",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),rsint});
				// 采集失败
				if (rsint != 1)
				{
					logger.warn(
							"serviceName[PortInfoService]cmdId[{}]userinfo[{}]getData sg fail",
							new Object[] { checker.getCmdId(), checker.getUserInfo()});
					checker.setResult(1006); 
					checker.setResultDesc("设备采集失败");
					
				}
				else//success
				{
					//获取LAN测信息
					ArrayList<HashMap<String,String>> lanInfoList = deviceConfigDao.getLanInfos(deviceId);
					if(lanInfoList!=null){
						for(HashMap<String,String> lanMap:lanInfoList){
							//lan1口
							if("1".equals(StringUtil.getStringValue(lanMap, "lan_eth_id"))){
								checker.setLanStatus(StringUtil.getStringValue(lanMap, "status"));
							}
						}
						
					}
					//获取Wan测信息
					ArrayList<HashMap<String,String>> wanInfoList = deviceConfigDao.getWlanInfos(deviceId);
					if(wanInfoList!=null&&wanInfoList.size()>0){
						checker.setWanStatus(StringUtil.getStringValue(wanInfoList.get(0),"conn_status"));
					}
					if(lanInfoList==null&&wanInfoList==null){
						checker.setResult(1000); 
						checker.setResultDesc("未知错误");
					}
					return checker.getReturnXml();
				}
				
			}
		}
		return checker.getReturnXml();
			
	}
}

	