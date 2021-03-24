
package com.linkage.stbms.ids.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import PreProcess.UserInfo;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.stbms.cao.ACSCorba;
import com.linkage.stbms.dao.RecordLogDAO;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.obj.StbServiceDoneChecker;

/**
 * 业务下发
 * 
 */
public class StbServiceDoneService {

	private static Logger logger = LoggerFactory.getLogger(StbServiceDoneService.class);

	/*
	 * 接口工作方法
	 */
	public String work(String inXml) {
		// 检查合法性
		StbServiceDoneChecker checker = new StbServiceDoneChecker(inXml);
		if (!checker.check()) {
			logger.error(
					"servicename[StbServiceDoneService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] {checker.getCmdId(), checker.getSearchInfo(), checker.getReturnXml()});
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[StbServiceDoneService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] {checker.getCmdId(), checker.getSearchInfo(), inXml});
		
		String searchType = StringUtil.getStringValue(checker.getSearchType());
		UserStbInfoDAO dao = new UserStbInfoDAO();
		
		Map<String, String> map = dao.getDeviceIdStr(searchType, checker.getSearchInfo(), "");
		
		String deviceId = StringUtil.getStringValue(map, "device_id", "");
		if (null == map || StringUtil.IsEmpty(deviceId)) {
			checker.setRstCode("0");
			checker.setRstMsg("此设备未绑定，无法做业务下发");
			
			logger.warn("此设备未绑定，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			logger.warn("StbServiceDoneService==>returnXML:" + checker.getReturnXml());
			if("xj_dx".equals(Global.G_instArea)){
				new RecordLogDAO().recordLog(checker.getSearchInfo(), inXml, "",
	                    checker.getReturnXml(), 1);
			}
			
			return checker.getReturnXml();
		}
			
		String oui = StringUtil.getStringValue(map, "oui", "");
		String devSn = StringUtil.getStringValue(map, "device_serialnumber", "");
		String customerId = StringUtil.getStringValue(map, "customer_id", "");
			
		// 确认设备是否在线
		int status = new ACSCorba().getDeviceStatus(deviceId);
			
		if (1 != status) {
			checker.setRstCode("0");
			checker.setRstMsg("此设备不在线");
				
			logger.warn("此设备不在线，不能Ping，serchType={}，searchInfo={}，device_id={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo(), deviceId });
			logger.warn("StbServiceDoneService==>retParam:" + checker.getReturnXml());
			if("xj_dx".equals(Global.G_instArea)){
				new RecordLogDAO().recordLog(checker.getSearchInfo(), inXml, "",
	                    checker.getReturnXml(), 1);
			}
			return checker.getReturnXml();
		}
			
		UserInfo[] userInfo = new UserInfo[1];
		userInfo[0] = new UserInfo();
		userInfo[0].deviceId = deviceId;
		userInfo[0].oui = oui;
		userInfo[0].deviceSn = devSn;
		userInfo[0].gatherId = "1";  // 采集点
		userInfo[0].userId = customerId;
		userInfo[0].servTypeId = "120";
		userInfo[0].operTypeId = "1";
		logger.warn("调配置模块，下发业务，deviceId={}", new Object[] { deviceId });
		int result = -2;
		try {
			//更新用户表业务开通状态
			dao.updateServOpenStatus(customerId, 0);
			//调用机顶盒网关下发 
			result = CreateObjectFactory.createPreProcess(Global.GW_TYPE_STB).processServiceInterface(userInfo);
		}
		catch (Throwable e) {
			logger.warn("调配置模块下发业务出现异常{}:{}", new Object[] { result,e.getMessage() });
			e.printStackTrace();
		}
		
		logger.warn("调配置模块下发业务结果{}", new Object[] { result });
		if (-2 == result){
			logger.warn("调配置模块下发业务失败{}", new Object[] { deviceId });
			checker.setRstCode("0");
			checker.setRstMsg("调配置模块下发业务失败");
			logger.warn("StbServiceDoneService==>retParam:" + checker.getReturnXml());
			if("xj_dx".equals(Global.G_instArea)){
				new RecordLogDAO().recordLog(checker.getSearchInfo(), inXml, "",
	                    checker.getReturnXml(), 1);
			}
			return checker.getReturnXml();
		}
	
		logger.warn("调配置模块下发业务成功", new Object[] { deviceId });
		checker.setRstCode("1");
		checker.setRstMsg("调配置模块下发业务成功");
		logger.warn("StbServiceDoneService==>retParam:" + checker.getReturnXml());
		if("xj_dx".equals(Global.G_instArea)){
			new RecordLogDAO().recordLog(checker.getSearchInfo(), inXml, "",
                    checker.getReturnXml(), 1);
		}
		return checker.getReturnXml();
	}
}
