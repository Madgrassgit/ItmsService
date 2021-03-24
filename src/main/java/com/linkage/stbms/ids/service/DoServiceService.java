package com.linkage.stbms.ids.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import PreProcess.UserInfo;

import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.stbms.cao.ACSCorba;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.util.DoServiceChecker;


public class DoServiceService {
	
	private static Logger logger = LoggerFactory.getLogger(DoServiceService.class);
	
	public String work(String inParam) {
		
		logger.warn("DoServiceService==>inParam={}",inParam);
		
		DoServiceChecker checker = new DoServiceChecker(inParam);
		
		// 入参验证
		if (false == checker.check()) {
			logger.warn("doService，入参验证失败，SearchType=[{}],SearchInfo=[{}]", new Object[] {
					checker.getSearchType(), checker.getSearchInfo() });
			
			logger.warn("DoServiceService==>retParam={}", checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		
		UserStbInfoDAO dao = new UserStbInfoDAO();
		
		Map<String, String> map = dao.getDeviceIdStr(checker.getSearchType(),
				checker.getSearchInfo(), "");
		
		if (null == map || "".equals(map.get("device_id")))
		{
			checker.setRstCode("0");
			checker.setRstMsg("此设备未绑定，无法做业务下发");
			
			logger.warn("此设备未绑定，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			
			logger.warn("DoServiceService==>returnXML:" + checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		else 
		{
			
			String deviceId = map.get("device_id");
			String oui = map.get("oui");
			String devSn = map.get("device_serialnumber");
			String customer_id = map.get("customer_id");
			
			// 确认设备是否在线
			int status = new ACSCorba().getDeviceStatus(deviceId);
			
			if (1 != status) {
				
				checker.setRstCode("0");
				checker.setRstMsg("此设备不在线");
				
				logger.warn("此设备不在线，不能Ping，serchType={}，searchInfo={}，device_id={}",
						new Object[] { checker.getSearchType(), checker.getSearchInfo(), deviceId });
				
				logger.warn("DoServiceService==>retParam:" + checker.getReturnXml());
				
				return checker.getReturnXml();
			}
			else
			{
				UserInfo[] userInfo = new UserInfo[1];
				userInfo[0] = new UserInfo();
				userInfo[0].deviceId = deviceId;
				userInfo[0].oui = oui;
				userInfo[0].deviceSn = devSn;
				userInfo[0].gatherId = "1";  // 采集点
				userInfo[0].userId = customer_id;
				userInfo[0].servTypeId = "120";
				userInfo[0].operTypeId = "1";
				logger.warn("调配置模块，下发业务，deviceId={}", new Object[] { deviceId });
				int result = 1;
				try
				{
					//调用机顶盒网关下发 
					result= CreateObjectFactory.createPreProcess(Global.GW_TYPE_STB).processServiceInterface(userInfo);
				}
				catch (Throwable e)
				{
					logger.warn("调配置模块下发业务出现异常{}:{}", new Object[] { result,e.getMessage() });
					e.printStackTrace();
									}
				
				logger.warn("调配置模块下发业务结果{}", new Object[] { result });
				if (-2 == result){
					logger.warn("调配置模块下发业务失败{}", new Object[] { deviceId });
					
					checker.setRstCode("0");
					checker.setRstMsg("调配置模块下发业务失败");
					
					logger.warn("DoServiceService==>retParam:" + checker.getReturnXml());
					
					return checker.getReturnXml();
				}else {
					logger.warn("调配置模块下发业务成功", new Object[] { deviceId });
					
					checker.setRstCode("1");
					checker.setRstMsg("调配置模块下发业务成功");
					
					logger.warn("DoServiceService==>retParam:" + checker.getReturnXml());
					
					return checker.getReturnXml();
				}
			}
		}
	}
}
