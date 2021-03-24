package com.linkage.stbms.ids.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkage.stbms.cao.ResourceBindCorba;
import com.linkage.stbms.ids.dao.SuperDAO;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.util.DoServiceServiceChecker;

public class DoService extends SuperDAO{
private static final Logger logger = LoggerFactory.getLogger(DoService.class);
	
	public String work(String inParam){
			
			logger.warn("DoService==>inParam={}",inParam);
			
			DoServiceServiceChecker checker = new DoServiceServiceChecker(inParam);
			
			// 入参验证
			if (false == checker.check()) {
				logger.warn("doService，入参验证失败，ConditionType=[{}],Condition=[{}]", new Object[] {
						checker.getQueryConditionType(), checker.getQueryCondition() });
				
				logger.warn("DoServiceService==>retParam={}", checker.getReturnXml());
				
				return checker.getReturnXml();
			}
			
			UserStbInfoDAO dao = new UserStbInfoDAO();
			
			List<HashMap<String,String>> resultMap = new ArrayList<HashMap<String,String>>();
			resultMap = dao.getDeviceInfo(checker.getQueryConditionType(), checker.getQueryCondition());
			if (null ==resultMap || resultMap.isEmpty())
			{
				if (1 == checker.getQueryConditionType())
				{
					checker.setRstCode("1002");
					checker.setRstMsg("查无此用户信息");
				}else if (2 == checker.getQueryConditionType())
				{
					checker.setRstCode("1003");
					checker.setRstMsg("未绑定机顶盒");
				}
				logger.warn("未查到设备信息，ConditionType=[{}],Condition=[{}]", new Object[] {
						checker.getQueryConditionType(), checker.getQueryCondition() });
				
				logger.warn("StbBindInfoService==>returnXML:" + checker.getReturnXml());
				
				return checker.getReturnXml();
			}
			if (resultMap.size() > 1)
			{
				checker.setRstCode("1004");
				checker.setRstMsg("查询多个设备,无法确认");
				logger.warn("未查到设备信息，ConditionType=[{}],Condition=[{}]", new Object[] {
						checker.getQueryConditionType(), checker.getQueryCondition() });
				
				logger.warn("StbBindInfoService==>returnXML:" + checker.getReturnXml());
				
				return checker.getReturnXml();
			}
			
			
			HashMap<String,String> map = resultMap.get(0);

			String cityId = map.get("city_id");
			String deviceId = map.get("device_id");
			String customId = map.get("custom_id");
			String deviceSerialnumber = map.get("device_serialnumber");
			String servAccount = map.get("serv_account");
			String addressType = map.get("addressing_type");
			String workId=getWorkId("3", servAccount);

			//手工下发
			if (1 == checker.getOperType())
			{
			     dao.insertZeroConfReportManul(cityId,deviceSerialnumber,workId,servAccount,addressType);
			//返修登记
			}else if (2 == checker.getOperType())
			{
				dao.updateDevieStatus(deviceSerialnumber,"2");
				int result = dao.insertZeroConfReportRepair(cityId,deviceSerialnumber,workId,servAccount,addressType);
				if(result>0)
				{
					logger.warn("update mem dev :"+ deviceSerialnumber);
					//同步缓存库
					new ResourceBindCorba().DoStbUpdate(deviceId, customId, 2);
				}
			}
			
			checker.setRstCode("0");
			checker.setRstMsg("成功");
			return checker.getReturnXml();
	}
	
	
	/**
	 * 获得工单号
	 * @param opr_type 操作类型
	 * @param serAccout 业务账号
	 * @return	工单号
	 */
	public static String getWorkId(String opr_type,String serAccout)
	{
		StringBuilder workId=new StringBuilder();
		workId.append(new Date().getTime()/1000).append(opr_type).append(serAccout);
		return workId.toString();
	}
}
