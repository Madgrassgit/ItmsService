package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryLoidNetServiceChecker;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2018年9月10日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryLoidNetService implements IService
{

	private static Logger logger = LoggerFactory.getLogger(QueryLoidNetService.class);
	
	
	@Override
	public String work(String inXml)
	{
		
		QueryLoidNetServiceChecker queryLoidNetService = new QueryLoidNetServiceChecker(inXml);
		
		//入参检查
		if(!queryLoidNetService.check()){
			
			logger.error("servicename[QueryLoidNetService]cmdId[{}]devSn[{}]验证未通过，返回：{}",
					new Object[] { queryLoidNetService.getCmdId(), queryLoidNetService.getDevSn(),
					queryLoidNetService.getReturnXml() });
			return queryLoidNetService.getReturnXml();
			
		}
		logger.warn("servicename[QueryLoidNetService]参数校验通过，入参为：{}",new Object[] {inXml });
		
		String loid = "";
		String netAccounts = "";
		String split = "";     //多宽带返回结果处理
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		//查找设备是否存在
		ArrayList<HashMap<String, String>> devInfoMapList = userDevDao.queryDevInfo2(queryLoidNetService.getDevSn());
		
		if(null == devInfoMapList || devInfoMapList.isEmpty()){
			
			logger.warn("servicename[QueryLoidNetService]devSN[{}]无此设备",new Object[] {queryLoidNetService.getDevSn()});
			queryLoidNetService.setResult(1004);
			queryLoidNetService.setResultDesc("查无此设备");
			
		}else 
			if(devInfoMapList.size() > 1){
				
				//设备不唯一则返回
				logger.warn("servicename[QueryLoidNetService]devSN[{}]查询到多台设备",new Object[] {queryLoidNetService.getDevSn()});
				queryLoidNetService.setResult(1006);
				queryLoidNetService.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				
			}else{
				
				//检测设备和用户是否绑定
				String userId = StringUtil.getStringValue(devInfoMapList.get(0), "user_id", "");
				if (StringUtil.IsEmpty(userId))
				{
					logger.warn("servicename[QueryLoidNetService]devSN[{}]未绑定用户",new Object[] {queryLoidNetService.getDevSn()});
					queryLoidNetService.setResult(1003);
					queryLoidNetService.setResultDesc("未绑定设备");
					 
				}else{
					
					//获取loid及宽带账号
					ArrayList<HashMap<String, String>> userInfoList = userDevDao.queryUserInfoByUserId(userId);
					if(null == userInfoList || userInfoList.isEmpty()){
						
						logger.error("servicename[QueryLoidNetService]devSN[{}] userId[{}]不存在与设备绑定的宽带账号及loid",
								new Object[] {queryLoidNetService.getDevSn(),userId});
						queryLoidNetService.setResult(1000);
						queryLoidNetService.setResultDesc("不存在与设备绑定的宽带账号及loid");
						
					}else{
						loid = userInfoList.get(0).get("loid");
						for(HashMap<String, String> userMaps : userInfoList){
							 netAccounts = netAccounts + split + userMaps.get("netaccount");
							 split = ",";
						}
						queryLoidNetService.setResult(0);
						queryLoidNetService.setResultDesc("成功");
						queryLoidNetService.setLoid(loid);
						queryLoidNetService.setUserInfo(netAccounts);
						logger.warn("servicename[QueryLoidNetService]获取数据成功");
						
					}
				}
			}
		
		String returnXml = queryLoidNetService.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(queryLoidNetService,"QueryLoidNetService", queryLoidNetService.getUserInfo());
		logger.warn("servicename[QueryLoidNetService]回参为:{}",new Object[]{returnXml});
		return returnXml;
	}
	
}
