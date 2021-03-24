
package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryIsAwifiChecker;

/**
 * call方法的业务处理类
 * awifi开通状态
 * @author wanghong5(72780)
 * @date 2015-03-10
 */
public class QueryIsAwifiService implements IService{

	private static Logger logger = LoggerFactory.getLogger(QueryIsAwifiService.class);

	@Override
	public String work(String inXml){
		// 检查合法性
		QueryIsAwifiChecker checker = new QueryIsAwifiChecker(inXml);
		if (false == checker.check()){
			logger.error(
					"servicename[QueryIsAwifiService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(),checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[QueryIsAwifiService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(),inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		int userInfoType=checker.getUserInfoType();
		String userInfo=checker.getUserInfo();
		List<HashMap<String,String>> device_idMap = userDevDao.getUserDeviceSerialNumber(userInfoType,userInfo);
		if(device_idMap == null || device_idMap.isEmpty()){
			logger.warn(
					"servicename[QueryIsAwifiService]cmdId[{}]userinfo[{}]无此用户",
					new Object[] { checker.getCmdId()});
			checker.setResult(1001);
			checker.setIsAwifi(1);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}else {
			 if(device_idMap.size() > 1 && userInfoType != 1) {
				logger.warn(
						"servicename[QueryWanTypeService]cmdId[{}]userinfo[{}]数据不唯一，请使用逻辑SN查询",
						new Object[] { checker.getCmdId()});
				checker.setResult(1000);
				checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
				checker.setIsAwifi(1);
				return checker.getReturnXml();
			 }
			 else {
				 String device_id=StringUtil.getStringValue(device_idMap.get(0), "device_id");
				 if(device_id == null || "".equals(device_id)){
					 logger.warn(
							 "servicename[QueryIsAwifiService]cmdId[{}]userinfo[{}]用户未绑定设备",
							 new Object[] { checker.getCmdId()});
					 checker.setResult(1002);
					 checker.setResultDesc("用户未绑定设备");
					 checker.setIsAwifi(1);
					 return checker.getReturnXml();
				 }
				 List<HashMap<String,String>> userMap = userDevDao.getUserIsAwifi(device_id);
				 logger.warn("userMap.size=" + userMap.size());
				 int isAwifi=1;
				 if(userMap != null && userMap.size() > 0){
					 if(userMap.size() == 1){
						 isAwifi=0;
					 }else{
						 long openTime = 0;
						 long closeTime = 0;
						 for(HashMap<String,String> tmp : userMap){
							 if("2001".equals(StringUtil.getStringValue(tmp, "service_id"))){
								 openTime = StringUtil.getLongValue(tmp, "end_time");
							 }else if("2003".equals(StringUtil.getStringValue(tmp, "service_id"))){
								 closeTime = StringUtil.getLongValue(tmp, "end_time");
							 }
						 }
						 if(openTime > closeTime){
							 isAwifi = 0;
						 }else{
							 isAwifi = 1;
						 }
					 }
					 
				 }else{
					 isAwifi = 1;
				 }
				 
				 checker.setIsAwifi(isAwifi);
				 checker.setResult(0);
				 checker.setResultDesc("成功");
			 }
		}
		
		
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, String.valueOf(userInfoType),userInfo);
		logger.warn(
				"servicename[QueryIsAwifiService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), userInfoType,userInfo,returnXml});
		// 回单
		return returnXml;
	}
}
