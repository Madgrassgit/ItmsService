package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.DateUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.RecycleDevSNChecker;

/**
 *
 */
public class RecycleDevSN implements IService{

	private static Logger logger = LoggerFactory.getLogger(RecycleDevSN.class);
	
	@Override
	public String work(String inParam) {
		logger.warn("RecycleDevSN==>inParam({})",inParam);

		// 解析获得入参
		RecycleDevSNChecker checker = new RecycleDevSNChecker(inParam);
		
		// 验证入参
		if (false == checker.check()) {
			logger.warn("RecycleDevSN:入参验证没通过,UserInfoType=[{}],UserInfo=[{}]",
					new Object[] { checker.getUserInfoType(), checker.getUserInfo() });
			
			logger.warn("RecycleDevSN==>returnParam="+checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		
		// 查询用户设备信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		List<String> devIdList = new ArrayList<String>();
		String devNames = "";
		
		ArrayList<HashMap<String, String>> userInfo = userDevDao.isLoidExisted(checker.getUserInfo());
		if (null == userInfo || userInfo.isEmpty()) {
			logger.warn(
					"servicename[RecycleDevSN]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1001);
			checker.setResultDesc("无此客户信息");
		}
		else{
			ArrayList<HashMap<String, String>> devIdInfo = userDevDao.getBindLog(checker.getUserInfo());
			
			if (null == devIdInfo || devIdInfo.isEmpty()) {
				logger.warn(
						"servicename[RecycleDevSN]cmdId[{}]userinfo[{}]用户48小时内未有解绑设备信息",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1002);
				checker.setResultDesc("用户48小时内未有解绑设备信息");
			} 
			else{
				for (HashMap<String, String> hashMap : devIdInfo) {
					devIdList.add(hashMap.get("device_id"));
				}
				
				devNames = userDevDao.getDeviceName(devIdList);
				checker.setDeviceName(devNames);
				
			}
		}
		
		
		
		String returnXml = checker.getReturnXml();
		logger.warn(
				"servicename[RecycleDevSN]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
	
		return returnXml;
		
	}
	public static void main(String[] args){
		System.out.println(DateUtil.currentTimeInSecond());
	}

}
