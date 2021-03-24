package com.linkage.itms.dispatch.cqdx.service;

import java.util.Map;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.obj.TraceRouteDealXML;

/**
 * traceroute
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2017年11月19日
 * @category com.linkage.itms.dispatch.cqdx.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class TraceRouteService
{
	private static Logger logger = LoggerFactory.getLogger(TraceRouteService.class);
	//用户宽带帐号
	private final int USERINFOTYPE_1 =1;
	//LOID
	private final int USERINFOTYPE_2 =2;


	public String work(String inXml) {
		logger.warn("servicename[TraceRouteService]执行，入参为：{}", inXml);
		TraceRouteDealXML deal = new TraceRouteDealXML();
		
		//校验入参
		Document document = deal.getXML(inXml);
		if (document == null) 
		{
			logger.warn("servicename[TraceRouteService]解析入参错误！");
			return deal.returnXML();
		}
		else
		{
			UserDeviceDAO userDevDao = new UserDeviceDAO();
			Map<String, String> userInfoMap  = null;
			if(!StringUtil.IsEmpty(deal.getPppUsename()))
			{
				userInfoMap = userDevDao.queryUserInfo(USERINFOTYPE_1, deal.getPppUsename(), null);
			}
			else if(!StringUtil.IsEmpty(deal.getLogicId()))
			{
				userInfoMap = userDevDao.queryUserInfo(USERINFOTYPE_2, deal.getLogicId(), null);
			}
			else{
				logger.warn("servicename[TraceRouteService]宽带账号和逻辑账号不能同时为空！");
				deal.setResult("-99");
				deal.setErrMsg("宽带账号和逻辑账号不能同时为空！");
				return deal.returnXML();
			}
			
			//用户不存在
			if (null == userInfoMap || userInfoMap.isEmpty()) {
				logger.warn(
						"servicename[TraceRouteService] ppp_username[{}] , logic_id[{}]查无此用户",
						new Object[] {deal.getPppUsename(), deal.getLogicId()});
				deal.setResult("-1");
				deal.setErrMsg("无此用户信息");
			} 
			//用户存在
			else
			{
				String userDevId = userInfoMap.get("device_id");
				// 用户未绑定终端
				if (StringUtil.IsEmpty(userDevId)) {
					logger.warn(
							"servicename[TraceRouteService] ppp_username[{}] , logic_id[{}],此客户未绑定",
							new Object[] { deal.getPppUsename(), deal.getLogicId()});
					deal.setResult("-99");
					deal.setErrMsg("此客户未绑定");
				}
				//traceroute
				else
				{
					logger.warn(
							"servicename[TraceRouteService] ppp_username[{}] , logic_id[{}] 调用acs执行traceroute",
							new Object[] { deal.getPppUsename(), deal.getLogicId()});
					ACSCorba corba = new ACSCorba();
					
					
					//先检测设备是否在线
					GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
					int flag = getStatus.testDeviceOnLineStatus(userDevId, corba);
					
					if (-3 == flag) {
						logger.warn("设备正在被操作，无法获取节点值，device_id={}", userDevId);
						deal.setResult("-99");
						deal.setErrMsg("设备不能正常交互");
					}
					// 设备在线
					if (1 == flag) {
						logger.warn("设备在线，可以进行采集操作，device_id={}", userDevId);
						DevTraceRouteService servie = new DevTraceRouteService();
						servie.traceRoute(userDevId,deal);
						logger.warn("deal="+deal);
					}
					else {// 设备不在线，不能获取节点值
						logger.warn("设备不在线，无法获取节点值 ,device_id={}", userDevId);
						deal.setResult("-99");
						deal.setErrMsg("设备不能正常交互");
					}
				}
			}
		
		}
		
		return deal.returnXML();
	}
}
