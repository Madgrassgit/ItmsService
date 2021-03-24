package com.linkage.itms.dispatch.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dao.ZongDiaoServUserDao;
import com.linkage.itms.dispatch.obj.CompleteProForZDChecker;

/**
 * 综调 报竣工 
 * 
 * 需求单：JSDX_ITMS-REQ-20120220-LUHJ-003
 * 
 * @author Administrator
 *
 */
public class ZongDiaoEndOfWorkService implements IService {

	private static Logger logger = LoggerFactory.getLogger(ZongDiaoEndOfWorkService.class);
	
	
	@Override
	public String work(String inXml)
	{
		CompleteProForZDChecker completeProForZDChecker = new CompleteProForZDChecker(inXml);
	
		// 验证入参的格式是否正确
		if (false == completeProForZDChecker.check()) {
			logger.error(
					"servicename[ZongDiaoEndOfWorkService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { completeProForZDChecker.getCmdId(), completeProForZDChecker.getUserInfo(),
							completeProForZDChecker.getReturnXml() });
			return completeProForZDChecker.getReturnXml();
		}
		logger.warn(
				"servicename[ZongDiaoEndOfWorkService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { completeProForZDChecker.getCmdId(), completeProForZDChecker.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				completeProForZDChecker.getUserInfoType(),
				completeProForZDChecker.getUserInfo());
		
		// 根据传进来的参数判断给用户是否存在，不存在则返回相关信息， 存在，则根据终端类型的不同报竣工的条件也不同
		// 规则：E8-B：只要自动绑定成功，则报竣工，否则不报竣工；E8-C：版本要是规范版本，同时所有业务下发成功，则报竣工，否则不报竣工
		//2014-07-24,xzl：针对E8C的，增加条件，终端规格匹配
		if (null == userInfoMap || userInfoMap.isEmpty() || StringUtil.IsEmpty(userInfoMap.get("user_id"))) {
			logger.warn(
					"servicename[ZongDiaoEndOfWorkService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { completeProForZDChecker.getCmdId(), completeProForZDChecker.getUserInfo()});
			completeProForZDChecker.setResult(1002);
			completeProForZDChecker.setResultDesc("查无此用户");
			
		} else {
			
			String userId = userInfoMap.get("user_id");
			String userDevId = userInfoMap.get("device_id");
			
			ZongDiaoServUserDao zongDiaoServUserDao = new ZongDiaoServUserDao();
			ServUserDAO servUserDAO = new ServUserDAO();
			
			// 未绑定设备直接返回，只有绑定设备后，E8-B 判断绑定方式是否为自动绑定，E8-C 判断版本是否为规范版本，所有业务是否下发成功
			if (StringUtil.IsEmpty(userDevId)) {
				logger.warn(
						"servicename[ZongDiaoEndOfWorkService]cmdId[{}]userinfo[{}]未绑定设备",
						new Object[] { completeProForZDChecker.getCmdId(), completeProForZDChecker.getUserInfo()});
				completeProForZDChecker.setResult(1004);
				completeProForZDChecker.setResultDesc("此用户未绑定设备");
			 } else {
				 
				 String devType = zongDiaoServUserDao.getDevType(userId); // 终端类型
				
				 // 终端类型为E8-B  只判断绑定方式是否为自动绑定，如果是自动绑定可以报竣工，否则不可以报竣工
				 if ("E8-B".equals(devType)) {
					 
					// userline 3:设备SN自动绑定  5:桥接账号自动绑定 	6:逻辑SN自动绑定	7:路由账号自动绑定
					String userline = userInfoMap.get("userline");  // 是否自动绑定
					
					List<Map<String,String>> sheetServInfos = servUserDAO.getBssSheetServInfo(userId);
					Map<String, String> rMap = (Map<String, String>) sheetServInfos.get(0);
					
					String userSN = StringUtil.getStringValue(rMap.get("username"));
					String cityId = StringUtil.getStringValue(rMap.get("city_id"));
					String dealDate = StringUtil.getStringValue(rMap.get("dealdate"));
					String devSn = StringUtil.getStringValue(rMap.get("device_serialnumber"));
					DateTimeUtil dt = new DateTimeUtil();
					
					// 是自动绑定，可以报竣工
					if("3".equals(userline) || "5".equals(userline) || "6".equals(userline) || "7".equals(userline)){
						completeProForZDChecker.setUserSN(userSN);
						completeProForZDChecker.setCityId(cityId);
						completeProForZDChecker.setDealDate(dealDate);
						completeProForZDChecker.setDevSn(devSn);
						completeProForZDChecker.setDevType("E8-B");
						completeProForZDChecker.setFailureReason("-");
						completeProForZDChecker.setConfigTime(dt.getLongDate());
						completeProForZDChecker.setSuccStatus("1");  // 成功状态  1：成功
					
					// 不是自动绑定，不能报竣工，返回错误提示
					} else {  
//						completeProForZDChecker.setUserSN(userSN);
//						completeProForZDChecker.setCityId(cityId);
//						completeProForZDChecker.setDealDate(dealDate);
//						completeProForZDChecker.setDevSn(devSn);
//						completeProForZDChecker.setDevType("E8-B");
						completeProForZDChecker.setFailureReason("2");  // 未自动绑定
						completeProForZDChecker.setConfigTime(dt.getLongDate());
						completeProForZDChecker.setSuccStatus("-1");  // 成功状态  -1：失败
					}
					
				// 终端类型为E8-C  判断版软件版本为规范版本，同时所有业务下发成功，才可以报竣工，否则不能报竣工
				} else {
					// 判断软件版本是否是规范版本(经过审核的版本)
					Map<String, String> map = zongDiaoServUserDao.getVersionIsCheckOrNot(userDevId);
					String isCheck = StringUtil.getStringValue(map.get("is_check"));
					String devSpecId = StringUtil.getStringValue(map, "d_spec_id", "");
					String userSpecId = map.get("u_spec_id");
					String userline = userInfoMap.get("userline");
					
					DateTimeUtil dt = new DateTimeUtil();
					
					// 版本不是规范版本  不能报竣工
					if (!"1".equals(isCheck)) {  
						completeProForZDChecker.setFailureReason("3");  // 不是规范版本
						completeProForZDChecker.setConfigTime(dt.getLongDate());
						completeProForZDChecker.setSuccStatus("-1");  // 成功状态  -1：失败
					
					// 是规范版本 同时还需要业务下发成功，才可以报竣工
					} 
					//终端规格不匹配
					else if(!devSpecId.equals(userSpecId))
					{
						completeProForZDChecker.setFailureReason("4");  // 终端规格不匹配
						completeProForZDChecker.setConfigTime(dt.getLongDate());
						completeProForZDChecker.setSuccStatus("-1");  // 成功状态  -1：失败
					}
					//判断是否自动绑定
					else if("1".equals(userline))
					{
						completeProForZDChecker.setFailureReason("2");  // 未自动绑定
						completeProForZDChecker.setConfigTime(dt.getLongDate());
						completeProForZDChecker.setSuccStatus("-1");  // 成功状态  -1：失败
					}
					else 
					{
						List<Map<String,String>> sheetServInfos = servUserDAO.getBssSheetServInfo(userId);
						
						int count = 0;
						String dealDate = "";
						String userSN = "";
						String cityId = "";
						String devSn = "";
						
						for(Map<String,String> sheetInfo : sheetServInfos){
							
							String openStatus = StringUtil.getStringValue(sheetInfo.get("open_status"));
							dealDate = StringUtil.getStringValue(sheetInfo.get("dealdate"));
							userSN = StringUtil.getStringValue(sheetInfo.get("username"));
							cityId = StringUtil.getStringValue(sheetInfo.get("city_id"));
							devSn = StringUtil.getStringValue(sheetInfo.get("device_serialnumber"));
							
							if (!"1".equals(openStatus)) { // 0：未做   1：成功  -1:失败
								count = count + 1 ;
							} 
						}
						
						// 所有业务下发成功，可以报竣工
						if (count == 0) {  
							completeProForZDChecker.setUserSN(userSN);
							completeProForZDChecker.setCityId(cityId);
							completeProForZDChecker.setDealDate(dealDate);
							completeProForZDChecker.setDevSn(devSn);
							completeProForZDChecker.setDevType("E8-C");
							completeProForZDChecker.setFailureReason("-");
							completeProForZDChecker.setConfigTime(dt.getLongDate());
							completeProForZDChecker.setSuccStatus("1");  // 成功状态  1：成功
						
						// 有业务没能下发失败或者未做，不能报竣工
						} else { 
							completeProForZDChecker.setFailureReason("1");  // 业务未成功下发
							completeProForZDChecker.setConfigTime(dt.getLongDate());
							completeProForZDChecker.setSuccStatus("-1");  // 成功状态  -1：失败
						}
					}
				}
			}
		}
		String returnXml = completeProForZDChecker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(completeProForZDChecker, completeProForZDChecker.getUserInfo(), "ZongDiaoEndOfWorkService");
		logger.warn(
				"servicename[ZongDiaoEndOfWorkService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { completeProForZDChecker.getCmdId(), completeProForZDChecker.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}
	
}
