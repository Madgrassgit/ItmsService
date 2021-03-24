package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
//import com.linkage.itms.cao.ACSCorba;
//import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryIsMulticastVlanDAO;
import com.linkage.itms.dispatch.obj.QueryIsMulticastVlanChecker;

/**
 * 
 * (新疆电信)检查光猫版本是否支持组播接口
 * @author chenxj6
 * @since 2016-8-29
 * 
 */
public class QueryIsMulticastVlanService implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(QueryIsMulticastVlanService.class);

	@Override
	public String work(String inParam)
	{
		logger.warn("QueryIsMulticastVlanService==>inParam:" + inParam);
		QueryIsMulticastVlanChecker checker = new QueryIsMulticastVlanChecker(inParam);
		if (false == checker.check())
		{
			logger.warn("检查光猫版本是否支持组播接口，入参验证失败，UserInfoType=[{}]，UserInfo=[{}]",
					new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
			logger.warn("QueryIsMulticastVlanService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		
//		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
//		ACSCorba corba = new ACSCorba();
		String deviceId = "";
//		ChangVlanParamDAO dao = new ChangVlanParamDAO();
		
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
		
//		//判断设备是否在线，此处不需要
//		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
//		
//		// 设备正在被操作
//		if (-3 == flag) {
//			logger.warn("设备正在被操作，不能正常交互，device_id={}", deviceId);
//			checker.setResult(1003);
//			checker.setResultDesc("设备不能正常交互");
//			return checker.getReturnXml();
//		}
//		// 设备在线
//		else if (1 == flag) {
//			logger.warn("设备在线，device_id={}", deviceId);
//		}
//		// 设备不在线
//		else {
//			logger.warn("设备不在线，设备与平台非正常交互状态，device_id={}", deviceId);
//			checker.setResult(1003);
//			checker.setResultDesc("设备不能正常交互");
//			return checker.getReturnXml();
//		}
		
		List<HashMap<String, String>> isMulticastList = dao.queryIsMulticastByDevId(deviceId);
		if(isMulticastList==null || isMulticastList.size()==0){
			logger.warn("isMulticastList为空，device_id={}", deviceId);
			checker.setResult(1000);
			checker.setResultDesc("是否支持组播查询结果为空");
			return checker.getReturnXml();
		}
		
		checker.setResult(0);
		checker.setResultDesc(isMulticastList.get(0).get("is_multicast"));

		return checker.getReturnXml();
	}
}

