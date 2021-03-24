package com.linkage.itms.dispatch.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dao.XinJiangFTTHSheetDao;
import com.linkage.itms.dispatch.obj.QueryByLoidChecker;

public class QueryResultByLoidService implements IService{

	private static Logger logger = LoggerFactory.getLogger(QueryResultByLoidService.class);
	
	public String work(String inXml){
		QueryByLoidChecker queryByLoidChecker = new QueryByLoidChecker(inXml);
		
		if (false == queryByLoidChecker.check()) {
			logger.error(
					"servicename[QueryResultByLoidService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { queryByLoidChecker.getCmdId(), queryByLoidChecker.getUserInfo(),
							queryByLoidChecker.getReturnXml() });
			return queryByLoidChecker.getReturnXml();
		}
		logger.warn(
				"servicename[QueryResultByLoidService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { queryByLoidChecker.getCmdId(), queryByLoidChecker.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		Map<String, String> userInfoMap = null;
		// 宁夏电信，且用户标识为企业网关
		if ("nx_dx".equals(Global.G_instArea) && "2".equals(queryByLoidChecker.getGwType())) {
			// 查询用户信息
			userInfoMap = userDevDao.queryUserInfoForBBMS(queryByLoidChecker.getUserInfoType(), queryByLoidChecker.getUserInfo());
		}else {
			// 查询用户信息
			userInfoMap = userDevDao.queryUserInfo(queryByLoidChecker.getUserInfoType(), queryByLoidChecker.getUserInfo());
		}

		
		if (null == userInfoMap || userInfoMap.isEmpty() || StringUtil.IsEmpty(userInfoMap.get("user_id"))) {
			logger.warn(
					"servicename[QueryResultByLoidService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { queryByLoidChecker.getCmdId(), queryByLoidChecker.getUserInfo()});
			queryByLoidChecker.setResult(1002);
			queryByLoidChecker.setResultDesc("查无此用户");
		}else {
			String userId = userInfoMap.get("user_id");
			String userDevId = userInfoMap.get("device_id");
			
			if (StringUtil.IsEmpty(userDevId)) {
				// 未绑定设备
				logger.warn(
						"servicename[QueryResultByLoidService]cmdId[{}]userinfo[{}]未绑定设备",
						new Object[] { queryByLoidChecker.getCmdId(), queryByLoidChecker.getUserInfo()});
				queryByLoidChecker.setResult(1004);
				queryByLoidChecker.setResultDesc("此用户未绑定设备");
			 }else {
				 XinJiangFTTHSheetDao servUserDAO = new XinJiangFTTHSheetDao(); 
				// 宁夏电信 与 新疆电信公用此方法
					List<Map<String, String>> sheetServInfos = servUserDAO
							.getVOIPAndIPTVBssSheetServInfo(userId,
									queryByLoidChecker.getGwType());
					
				 int counNum = 0;
				 String open_status = "";
				 String serv_type_id = "";
				 String compeleteDate = "";
				 
				 for(Map<String,String> sheetInfo : sheetServInfos){
					 
					 open_status = StringUtil.getStringValue(sheetInfo.get("open_status")); // 业务开通状态  // 0：未做  1：成功  -1：失败
					 serv_type_id = StringUtil.getStringValue(sheetInfo.get("serv_type_id")); //业务类型
					 compeleteDate = StringUtil.getStringValue(sheetInfo.get("completedate")); // 竣工时间
					 
					// IPTV二路
					if ("11".equals(serv_type_id) && StringUtil.getIntValue(sheetInfo, "serv_num") > 2) {
						if("-1".equals(open_status) || "0".equals(open_status)){  // 0：未做  1：成功  -1：失败
							counNum = counNum + 1 ;
							break;  // 如果有一个业务下发失败，则返回，不需要继续判断其他业务下发情况
						}
					} else if("14".equals(serv_type_id)) {  // VOIP 业务
						if("-1".equals(open_status) || "0".equals(open_status)){  // 0：未做  1：成功  -1：失败
							counNum = counNum + 1 ;
							break;  // 如果有一个业务下发失败，则返回，不需要继续判断其他业务下发情况
						}
					}
				}
				 
				if(counNum > 0){  // 说明业务中有下发失败的情况
					queryByLoidChecker.setResult(1006);
					queryByLoidChecker.setResultDesc("工单执行失败");
					// 宁夏电信
					if("nx_dx".equals(Global.G_instArea)){
						queryByLoidChecker.setCompeleteDate(compeleteDate);
					}
				}else {
					queryByLoidChecker.setResult(0);
					queryByLoidChecker.setResultDesc("工单正常执行成功");
					// 宁夏电信
					if("nx_dx".equals(Global.G_instArea)){
						queryByLoidChecker.setCompeleteDate(compeleteDate);
					}
				}
				
				counNum = 0;  // 清零
			}
			
		}
		String returnXml = queryByLoidChecker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(queryByLoidChecker, queryByLoidChecker.getUserInfo(),"QueryResultByLoidService");
		logger.warn(
				"servicename[QueryResultByLoidService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { queryByLoidChecker.getCmdId(), queryByLoidChecker.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}
}
