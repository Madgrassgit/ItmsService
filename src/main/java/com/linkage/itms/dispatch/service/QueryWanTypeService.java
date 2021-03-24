
package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryWanTypeChecker;

/**
 * call方法的业务处理类
 * 上网方式查询
 * 
 * @author wanghong5（72780）
 * @date 2015-03-10
 */
public class QueryWanTypeService implements IService{

	private static Logger logger = LoggerFactory.getLogger(QueryWanTypeService.class);

	@Override
	public String work(String inXml){
		// 检查合法性
		QueryWanTypeChecker checker = new QueryWanTypeChecker(inXml);
		if (false == checker.check()){
			logger.error(
					"servicename[QueryWanTypeService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(),checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[QueryWanTypeService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(),inXml });
		
		int userInfoType=checker.getUserInfoType();
		String userInfo=checker.getUserInfo();
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		List<HashMap<String,String>> userMap = userDevDao.getUserWanType(userInfoType,userInfo);
		if(userMap==null || userMap.size()==0){
			logger.warn(
					"servicename[QueryWanTypeService]cmdId[{}]userinfo[{}]无此用户相关信息",
					new Object[] { checker.getCmdId()});
			checker.setResult(1001);
			checker.setResultDesc("无此用户相关信息");
			return checker.getReturnXml();
		}else if(userMap.size() > 1){
			logger.warn(
					"servicename[QueryWanTypeService]cmdId[{}]userinfo[{}]数据不唯一，请使用逻辑SN查询",
					new Object[] { checker.getCmdId()});
			checker.setResult(1000);
			checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
			return checker.getReturnXml();
		}
		int wanType = StringUtil.getIntegerValue(userMap.get(0).get("wan_type"));
		checker.setWanType(wanType);
		checker.setResult(0);
		checker.setResultDesc("成功");
		
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, String.valueOf(userInfoType),userInfo);
		logger.warn(
				"servicename[QueryWanTypeService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), userInfoType,userInfo,returnXml});
		// 回单
		return returnXml;
	}
}
