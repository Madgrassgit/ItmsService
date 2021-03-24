package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.PortPonInfoChecker;
		
public class PortPonInfoService implements IService
{
	private static final Logger logger = LoggerFactory.getLogger(PortPonInfoService.class);
    private UserDeviceDAO userDevDao = new UserDeviceDAO();
    private DeviceConfigDAO deviceConfigDao = new DeviceConfigDAO();

	@Override
	public String work(String inXml)
	{
		PortPonInfoChecker checker = new PortPonInfoChecker(inXml);
		if (!checker.check()) 
		{
			logger.error("serviceName[PonInfoService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
			new Object[] { checker.getCmdId(), checker.getUserInfo(),checker.getReturnXml() });
			return checker.getReturnXml();
		}
		//验证通过走采集流程
		logger.warn("serviceName[PonInfoService]cmdId[{}]userinfo[{}]初始参数校验通过，入参为：{}",
		new Object[] { checker.getCmdId(), checker.getUserInfo(),inXml });
		// 结果集：a.user_id,a.username,a.device_id,a.oui,a.device_serialnumber,a.city_id,a.userline,a.access_style_id
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn("serviceName[PonInfoService]cmdId[{}]userinfo[{}]无此用户",
			new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setRstCode("1002");
			checker.setRstMsg("无此用户信息");
			return checker.getReturnXml();
		}
		else
		{
			String deviceId = StringUtil.getStringValue(userInfoMap,"device_id");
			if (StringUtil.IsEmpty(deviceId))
			{
				logger.warn("serviceName[PonInfoService]cmdId[{}]userinfo[{}]未绑定设备",
				new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setRstCode("1004");
				checker.setRstMsg("此用户未绑定设备");
				return checker.getReturnXml();
			}
			else
			{
				logger.warn("serviceName[PonInfoService]cmdId[{}]userinfo[{}]开始采集[{}]",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),deviceId});
				//掉CORBAR 采集    0表示采集所有节点 在原来基础上增加了一个参数(3)
				int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3); 
				logger.warn(
						"serviceName[PonInfoService]cmdId[{}]userinfo[{}]getCpeParams设备配置信息采集结果[{}]",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),rsint});
				// 采集失败
				if (rsint != 1)
				{
					logger.warn(
							"serviceName[PonInfoService]cmdId[{}]userinfo[{}]getData sg fail",
							new Object[] { checker.getCmdId(), checker.getUserInfo()});
					checker.setRstCode("1006"); 
					checker.setRstMsg("设备采集失败");
					return checker.getReturnXml();
				}
				else
				{
					//获取PON信息
					Map<String,String> ponInfoMap = deviceConfigDao.getPonInfo(deviceId);
					if(null !=ponInfoMap)
					{
						//接收光功率
						checker.setRXPower(StringUtil.getStringValue(ponInfoMap,"rx_power",""));
						//发射光功率
					    checker.setTXPower(StringUtil.getStringValue(ponInfoMap,"tx_power",""));
					    //状态 nx : 这个status是否是 gw_wan_wireinfo_epon 表中的字段 status
					    if ("nx_dx".equals(Global.G_instArea)){
					    checker.setStatus(StringUtil.getStringValue(ponInfoMap,"status",""));
					    }
					}
					else{
						checker.setRstCode("1007"); 
						checker.setRstMsg("查询光功率失败");
						return checker.getReturnXml();
					}
					//获取LAN测信息
					ArrayList<HashMap<String,String>> lanInfoList = deviceConfigDao.getLanInfos(deviceId);
					if(lanInfoList!=null){
						for(HashMap<String,String> lanMap:lanInfoList){
							//lan1口
							if("1".equals(StringUtil.getStringValue(lanMap, "lan_eth_id"))){
								checker.setLanStatus(StringUtil.getStringValue(lanMap, "status",""));
							}
						}
						
					}
					else{
						checker.setRstCode("1008"); 
						checker.setRstMsg("查询LAN端口信息失败");
						return checker.getReturnXml();
					}
					//获取Wan测信息
					Map<String,String> wanInfoMap = deviceConfigDao.getWlanInfosforPortPon(deviceId);
					if(wanInfoMap!=null){
						checker.setWanStatus(StringUtil.getStringValue(wanInfoMap,"conn_status",""));
					}
					else
					{
						checker.setRstCode("1009"); 
						checker.setRstMsg("查询WAN端口信息失败");
						return checker.getReturnXml();
					}
				}
			}
		}
		return checker.getReturnXml();
			
	}
	
}

	