package com.linkage.stbms.ids.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.cao.DevReset;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.util.StbResetChecker;
import com.linkage.stbms.itv.main.Global;


public class StbResetService {
	
	private static Logger logger = LoggerFactory.getLogger(StbResetService.class);
	
	public String work(String inXml){
		
		logger.warn("StbResetService==>inParam={}",inXml);
		
		StbResetChecker checker = new StbResetChecker(inXml);
		
		// 入参验证
		if (false == checker.check()) {
			logger.warn("机顶盒回复出厂设置，入参验证失败，SearchType=[{}],SearchInfo=[{}]", new Object[] {
					checker.getSearchType(), checker.getSearchInfo() });
			
			logger.warn("StbResetService==>retParam={}", checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		
		UserStbInfoDAO dao = new UserStbInfoDAO();
		
		Map<String,String> map = dao.getDeviceIdStr(checker.getSearchType(),
				checker.getSearchInfo(), "1");
		
		if (null == map || "".equals(map.get("device_id")))
		{
			checker.setRstCode("0");
			checker.setRstMsg("查无此设备");
			
			logger.warn("查无此设备，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			
			logger.warn("setStbRebootTwo==>returnXML:" + checker.getReturnXml());
			
			return checker.getReturnXml();
		}else {
			
			String deviceId = map.get("device_id");
			
			int relt = DevReset.reset(deviceId);
			
			logger.warn("机顶盒重启结果:"+relt);
			
			if(relt == 1 || relt == 0)
			{
				checker.setRstCode("1");
				checker.setRstMsg("机顶盒已回复出厂设置!");
				
				logger.warn("setStbRebootTwo==>returnXML:" + checker.getReturnXml());
				
				return checker.getReturnXml();
			}
			else
			{
				checker.setRstCode("0");
				String decs = Global.G_Fault_Map.get(StringUtil.getStringValue(relt));
				if(StringUtil.IsEmpty(decs))
				{
					checker.setRstMsg("回复出厂设置失败！");
				}
				else
				{
					checker.setRstMsg(decs);
				}
				
				logger.warn("setStbRebootTwo==>returnXML:" + checker.getReturnXml());
				
				return checker.getReturnXml();
			}
		}
		
	}
	
}
