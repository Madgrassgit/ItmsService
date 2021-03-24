package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.QueryIsMulticastVlanDAO;
import com.linkage.itms.dispatch.obj.QueryIsMbBroadBandChecker;

/**
 * (新疆电信)检查光猫版本是否支持百兆宽带
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-5-18
 */
public class QueryIsMbBroadBandService implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(QueryIsMbBroadBandService.class);

	@Override
	public String work(String inParam)
	{
		logger.warn("QueryIsMbBroadBandService==>inParam:" + inParam);
		QueryIsMbBroadBandChecker checker = new QueryIsMbBroadBandChecker(inParam);
		if (false == checker.check())
		{
			logger.warn("检查光猫版本是否支持百兆宽带接口，入参验证失败，UserInfoType=[{}]，UserInfo=[{}]",
					new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
			logger.warn("QueryIsMbBroadBandService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		logger.warn("检查光猫版本是否支持百兆宽带接口，入参验证通过，UserInfoType=[{}]，UserInfo=[{}]",
				new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
		String deviceId = "";
		
		List<HashMap<String,String>> userMapList = null;
		List<HashMap<String,String>> deviceMapList = null;
		QueryIsMulticastVlanDAO dao = new QueryIsMulticastVlanDAO();
		
		
		// 用户信息类型:1：用户宽带帐号;2：LOID;3：IPTV宽带帐号;4：VOIP业务电话号码;5：VOIP认证帐号
		if(checker.getUserInfoType() == 1)
		{
			userMapList = dao.queryUserByNetAccount(checker.getUserInfo());
		}else if (checker.getUserInfoType() == 2)
		{
			userMapList = dao.queryUserByLoid(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 3)
		{
			userMapList = dao.queryUserByIptvAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 4)
		{
			userMapList = dao.queryUserByVoipPhone(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 5)
		{
			userMapList = dao.queryUserByVoipAccount(checker.getUserInfo());
		}
		
		checker.setIsMbBroadBand("2");
		checker.setIsMbBroadBandDesc("不支持");
		if (userMapList == null || userMapList.isEmpty())
		{
			logger.warn("查无此客户");
			checker.setResult(1000);
			checker.setResultDesc("查无此客户");
			return checker.getReturnXml();
		}
		
		
		String devSn = checker.getDevSn();
		if(devSn==null || devSn.trim().length()==0){
			if(userMapList.size()>1){
				logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				return checker.getReturnXml();
			}else{
				deviceId = StringUtil.getStringValue(userMapList.get(0), "device_id", "");
				if (StringUtil.IsEmpty(deviceId))
				{
					logger.warn("用户未绑定设备");
					checker.setResult(1002);
					checker.setResultDesc("用户未绑定设备");
					return checker.getReturnXml();
				}
			} 
		}else{
			devSn = devSn.trim();
			if(devSn.length()<6){
				logger.warn("设备序列号非法，按设备序列号查询时，查询序列号字段少于6位");
				checker.setResult(1005);
				checker.setResultDesc("设备序列号非法");
				return checker.getReturnXml();
			}else{
				deviceMapList = dao.queryDeviceByDevSN(devSn);
				if(deviceMapList==null || deviceMapList.size()==0){
					logger.warn("没有查到设备");
					checker.setResult(1000);
					checker.setResultDesc("没有查到设备");
					return checker.getReturnXml();
				}else if(deviceMapList.size()>1){
					logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询");
					checker.setResult(1006);
					checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
					return checker.getReturnXml();
				}else{
					deviceId = StringUtil.getStringValue(deviceMapList.get(0), "device_id", "");
					boolean flagTemp = false;
					for(HashMap<String,String> userMap : userMapList){
						if(userMap.containsValue(deviceId)){
							flagTemp = true;
							break;
						}
					}
					if(false==flagTemp){
						logger.warn("用户未绑定该设备");
						checker.setResult(1000);
						checker.setResultDesc("用户未绑定该设备");
						return checker.getReturnXml();
					}
				}
			}
		}
		
		List<HashMap<String, String>> mbBroadBand = dao.queryIsMbBroadBandByDevId(deviceId);
		if(mbBroadBand==null || mbBroadBand.size()==0){
			logger.warn("mbBroadBand为空，device_id={}", deviceId);
			checker.setResult(1000);
			checker.setResultDesc("是否支持百兆宽带查询结果为空");
			return checker.getReturnXml();
		}
		
		checker.setResult(0);
		checker.setResultDesc("成功");
		String mbBroad = StringUtil.getStringValue(mbBroadBand.get(0), "mbbroadband", "");
		if ("1".equals(mbBroad)){
			checker.setIsMbBroadBand("1");
			checker.setIsMbBroadBandDesc("支持");
		}else{
			checker.setIsMbBroadBand("2");
			checker.setIsMbBroadBandDesc("不支持");
		}
		
		String deviceTypeId = StringUtil.getStringValue(mbBroadBand.get(0), "devicetype_id", "");
		if(!StringUtil.IsEmpty(deviceTypeId)){
			Map<String,String> deviceVersionMap = dao.getDeviceVersionInfo(deviceTypeId);
			if(null != deviceVersionMap && !deviceVersionMap.isEmpty()){
				checker.setGigabitPort(StringUtil.getStringValue(deviceVersionMap, "gigabit_port", ""));
				checker.setIsWifi(StringUtil.getStringValue(deviceVersionMap, "wifi", ""));
				checker.setDownloadWifi(StringUtil.getStringValue(deviceVersionMap, "download_max_wifi", ""));
				checker.setWifiFrequency(StringUtil.getStringValue(deviceVersionMap, "wifi_frequency", ""));
				String gigabitPortType = StringUtil.getStringValue(deviceVersionMap, "gigabit_port_type", "");
				if("1".equals(gigabitPortType)){
					checker.setGigabitPortNum("1");
					checker.setGigabitPortInfo("lan1");
				}else if ("2".equals(gigabitPortType)){
					checker.setGigabitPortNum("2");
					checker.setGigabitPortInfo("lan1|lan2");
				}else if ("3".equals(gigabitPortType)){
					checker.setGigabitPortNum("4");
					checker.setGigabitPortInfo("lan1|lan2|lan3|lan4");
				}else{
					checker.setGigabitPortNum(gigabitPortType);
					checker.setGigabitPortInfo("");
				}
				checker.setDownloadLan(StringUtil.getStringValue(deviceVersionMap, "download_max_lan", ""));
			}
		}
		

		return checker.getReturnXml();
	}
}

