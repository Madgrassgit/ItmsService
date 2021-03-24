
package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryAccessTypeChecker;

/**
 * call方法的业务处理类
 * 接入方式查询
 * @author wanghong5(72780)
 * @date 2015-03-10
 */
public class QueryAccessTypeService implements IService{

	private static Logger logger = LoggerFactory.getLogger(QueryAccessTypeService.class);

	@Override
	public String work(String inXml){
		// 检查合法性
		QueryAccessTypeChecker checker = new QueryAccessTypeChecker(inXml);
		if (false == checker.check()){
			logger.error(
					"servicename[QueryAccessTypeService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(),checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[QueryAccessTypeService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(),inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		int userInfoType=checker.getUserInfoType();
		String userInfo=checker.getUserInfo();
		
		
		List<HashMap<String,String>> userMap = userDevDao.getUserAccessType(userInfoType,userInfo);
		if(null==userMap || userMap.size() == 0){
			logger.warn(
					"servicename[QueryAccessTypeService]cmdId[{}]userinfo[{}]无此用户信息",
					new Object[] { checker.getCmdId()});
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		else if(userMap.size() > 1 && userInfoType != 1){
			logger.warn(
					"servicename[QueryWanTypeService]cmdId[{}]userinfo[{}]数据不唯一，请使用逻辑SN查询",
					new Object[] { checker.getCmdId()});
			checker.setResult(1000);
			checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
			return checker.getReturnXml();
		}
		
		int adsl_hl =Integer.parseInt(userMap.get(0).get("adsl_hl").replace(".0", ""));
		checker.setAdsl_hl(adsl_hl);
		checker.setResult(0);
		checker.setResultDesc("成功");
		
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, String.valueOf(userInfoType),userInfo);
		logger.warn(
				"servicename[QueryAccessTypeService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), userInfoType,userInfo,returnXml});
		// 回单
		return returnXml;
	}
}
