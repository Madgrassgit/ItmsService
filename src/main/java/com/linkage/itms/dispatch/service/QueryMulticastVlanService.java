package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryMulticastVlanDAO;
import com.linkage.itms.dispatch.obj.QueryMulticastVlanChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 江西电信：Itv业务组播vlan查询接口
 * @author chenxj6
 * @date 2017-03-24
 * @param param
 * @return
 */
public class QueryMulticastVlanService implements IService {

	private static final Logger logger = LoggerFactory.getLogger(QueryMulticastVlanService.class);

	@Override
	public String work(String inParam) {
		logger.warn("QueryMulticastVlanService==>inParam:" + inParam);
		
		String iptvMulticastVlanId = null;
		
		QueryMulticastVlanChecker checker = new QueryMulticastVlanChecker(inParam);
		if (false == checker.check()) {
			logger.warn("Itv业务组播vlan查询接口，入参验证失败，devSn=[{}]，UserInfo=[{}]",
					new Object[] { checker.getDevSn(),checker.getUserInfo() });
			logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
			return checker.getReturnXml();
		}

		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		String deviceId = "";
		String userId = "";

		List<HashMap<String, String>> userMapList = null;
		List<HashMap<String, String>> deviceMapList = null;
		QueryMulticastVlanDAO dao = new QueryMulticastVlanDAO();

//		searchType 1：客户账号；2：设备序列号
		if (checker.getSearchType() == 1) {
//			userInfoType 1：用户宽带帐号；2：LOID；3：IPTV宽带帐号；4：VOIP业务电话号码；5：VOIP认证帐号
			if (checker.getUserInfoType() == 1) {
				userMapList = dao.queryUserByNetAccount(checker.getUserInfo());
			} else if (checker.getUserInfoType() == 2) {
				userMapList = dao.queryUserByLoid(checker.getUserInfo());
			} else if (checker.getUserInfoType() == 3) {
				userMapList = dao.queryUserByIptvAccount(checker.getUserInfo());
			} else if (checker.getUserInfoType() == 4) {
				userMapList = dao.queryUserByVoipPhone(checker.getUserInfo());
			} else if (checker.getUserInfoType() == 5) {
				userMapList = dao.queryUserByVoipAccount(checker.getUserInfo());
			}

			if (null == userMapList || userMapList.isEmpty()) {
				logger.warn("查无此客户");
				checker.setResult(1002);
				checker.setResultDesc("查无此客户");
				logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
				return checker.getReturnXml();
			}
			if (userMapList.size() > 1) {
				logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
				return checker.getReturnXml();
			}
			
			deviceId = StringUtil.getStringValue(userMapList.get(0),"device_id", "");
			userId = StringUtil.getStringValue(userMapList.get(0),"user_id", "");
			
		} else if (checker.getSearchType() == 2) {
			String devSn = checker.getDevSn();
			devSn = devSn.trim();
			deviceMapList = dao.queryDeviceByDevSN(devSn);
			if (deviceMapList == null || deviceMapList.size() == 0) {
				logger.warn("查无此设备");
				checker.setResult(1004);
				checker.setResultDesc("查无此设备");
				logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
				return checker.getReturnXml();
			} else if (deviceMapList.size() > 1) {
				logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
				return checker.getReturnXml();
			} else {
				deviceId = StringUtil.getStringValue(deviceMapList.get(0),"device_id", "");
				userId = StringUtil.getStringValue(deviceMapList.get(0),"user_id", "");
			}
		}
		
		if (StringUtil.IsEmpty(userId)) {
			logger.warn("查无此客户");
			checker.setResult(1002);
			checker.setResultDesc("查无此客户");
			logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("查无此设备");
			checker.setResult(1004);
			checker.setResultDesc("查无此设备");
			logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);

		logger.warn("设备[{}],在线状态[{}] ", new Object[] { deviceId, flag });

		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id["+deviceId+"]");
			checker.setResult(1003);
			checker.setResultDesc("设备正在被操作，无法获取节点值，device_id["+deviceId+"]");
			logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 设备在线
		else if (1 == flag) {
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			
			String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
			String wanServiceList = ".X_CT-COM_ServiceList";
			String wanPPPConnection = ".WANPPPConnection.";
			String wanIPConnection = ".WANIPConnection.";
			
			ArrayList<String> wanConnPathsList = new ArrayList<String>();
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			
			if (null == jList || jList.size() == 0 || jList.isEmpty())
			{
				logger.warn("["+deviceId+"]获取" + wanConnPath + "下实例失败，返回");
				checker.setResult(1000);
				checker.setResultDesc("["+deviceId+"]获取" + wanConnPath + "下实例失败，返回");
				logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
				return checker.getReturnXml();
			}else{
				for (String j : jList){
					List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j + wanPPPConnection);
					if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
					{
						logger.warn("[{}]获取" + wanConnPath	+ j + wanPPPConnection + "下实例失败", deviceId);
						kPPPList = corba.getIList(deviceId, wanConnPath + j + wanIPConnection);
						if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
						{
							logger.warn("["+deviceId+"]获取" + wanConnPath + j + wanIPConnection + "下实例失败");
							checker.setResult(1000);
							checker.setResultDesc("["+deviceId+"]获取" + wanConnPath + j + wanIPConnection + "下实例失败");
							logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
							return checker.getReturnXml();
						}else{
							logger.warn("[{}]获取" + wanConnPath + j + wanIPConnection + "下实例成功", deviceId);
							for (String kppp : kPPPList)
							{
								wanConnPathsList.add(wanConnPath + j + wanIPConnection + kppp + wanServiceList);
							}
						}
					}
					else
					{
						logger.warn("[{}]获取" + wanConnPath + j + wanPPPConnection + "下实例成功", deviceId);
						for (String kppp : kPPPList)
						{
							wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp + wanServiceList);
						}
					}
				}
			}
				
			// serviceList节点
			ArrayList<String> serviceListList = new ArrayList<String>();
			for (int i = 0; i < wanConnPathsList.size(); i++)
			{
				String namepath = wanConnPathsList.get(i);
				if (namepath.indexOf(wanServiceList) >= 0)
				{
					serviceListList.add(namepath);
					continue;
				}
			}
			
			
			if (serviceListList.isEmpty())
			{
				logger.warn("[{}}]不存在WANIP下的X_CT-COM_ServiceList节点，返回",deviceId);
				checker.setResult(1000);
				checker.setResultDesc("["+deviceId+"]不存在WANIP下的X_CT-COM_ServiceList节点，返回");
				logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
				return checker.getReturnXml();
			}else{
				boolean hasIptv = false;
				String iptvServiceListPath = "";
				for(String serviceListPath : serviceListList){
					ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, serviceListPath);
					if (null == objLlist || objLlist.isEmpty()) {
						continue;
					}
					for(ParameValueOBJ pvobj : objLlist){
						if(pvobj.getName().endsWith("ServiceList")){
							String ServiceListValue = pvobj.getValue();
							if("OTHER".equals(ServiceListValue)){
								iptvServiceListPath = serviceListPath;
								hasIptv = true;
								break;
							}
						}
					}
				}
				// 路由和桥接都是 WANPPPConnection，这里只考虑路由和桥接
				if(!hasIptv){
					logger.warn("["+deviceId+"]不存在IPTV业务WAN连接，返回");
					checker.setResult(1000);
					checker.setResultDesc("["+deviceId+"]不存在IPTV业务WAN连接，返回");
					logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
					return checker.getReturnXml();
				}
				String iptvMulticastVlanPath = iptvServiceListPath.replaceAll("X_CT-COM_ServiceList", "") + "X_CT-COM_MulticastVlan";
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId,iptvMulticastVlanPath);

				if (objLlist != null && objLlist.size() != 0) {
					for (ParameValueOBJ pvobj : objLlist) {
						if (pvobj.getName().endsWith("X_CT-COM_MulticastVlan")) {
							iptvMulticastVlanId = pvobj.getValue();
							break;
						}
					}
				}
				if(StringUtil.IsEmpty(iptvMulticastVlanId)){
					logger.warn("["+deviceId+"]itv组播vlanId采集结果为空，返回");
					checker.setResult(1000);
					checker.setResultDesc("["+deviceId+"]itv组播vlanId采集结果为空，返回");
					logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
					return checker.getReturnXml();
				}
			}
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值");
			checker.setResult(1003);
			checker.setResultDesc("设备不在线，无法获取节点值");
			logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
			return checker.getReturnXml();
		}
		
//		checker.setResult(0);
//		checker.setResultDesc("成功");
		checker.setMulticastVlanId(iptvMulticastVlanId);
		logger.warn("QueryMulticastVlanService==>retParam={}",checker.getReturnXml());
		return checker.getReturnXml();
	}
}
