package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryOnuRateBySNCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2018年11月15日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryOnuRateBySNService implements IService
{
	private static final Logger logger = LoggerFactory.getLogger(QueryOnuRateBySNService.class);

	@Override
	public String work(String inXml)
	{
		QueryOnuRateBySNCheck checker = new QueryOnuRateBySNCheck(inXml);
		//检验参数的合法
		if(!checker.check()){
			logger.error("serviceName[QueryOnuRateBySNService]cmdId[{}]userName[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserName(), checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.info("servicename[QueryOnuRateBySNService]cmdId[{}]参数校验通过，入参为：{}",new Object[] {checker.getCmdId(), inXml});
		
        UserDeviceDAO userDevDao = new UserDeviceDAO();
        List<HashMap<String, String>> bindInfoList = null;
        HashMap<String, String> devInfoMap = null;
        String deviceSN = null;        //设备序列号
        String deviceName = null;        //设备序列号
        int userInfoCount = 0;   //用户信息
        int cpeMegabytes = 0;    //千百兆光猫信息
		String devType = "";   //光猫类型

        //判断用户是否存在
        userInfoCount = userDevDao.queryUserNmaeExist(checker.getUserInfoType(), checker.getUserName());
		if(userInfoCount == 0){
			
			logger.warn("servicename[QueryOnuRateBySNService]username[{}]无此用户",new Object[] {checker.getUserName()});
			checker.setResult(1002);
			checker.setResultDesc("查无此客户");
			
		}else{
			//获取与用户绑定的设备
			bindInfoList = userDevDao.querySnByUserInfo(checker.getUserInfoType(), checker.getUserName());
			if(null == bindInfoList || bindInfoList.isEmpty() || bindInfoList.size() < 1 || null == bindInfoList.get(0))
			{
				 
				 logger.warn("servicename[QueryOnuRateBySNService]username[{}]未绑定设备",new Object[] {checker.getUserName()});
				 checker.setResult(1003);
				 checker.setResultDesc("未绑定设备");
					
			}
			else
			{
				 devInfoMap = bindInfoList.get(0);
				 deviceSN = devInfoMap.get("device_serialnumber");
				 deviceName = devInfoMap.get("device_name");
				 if(StringUtil.IsEmpty(deviceSN))
				 {
					 logger.warn("servicename[QueryOnuRateBySNService]deviceSN[{}]序列号不存在",new Object[] {deviceSN});
					 checker.setResult(1003);
					 checker.setResultDesc("未绑定设备");
					 return checker.getReturnXml();
				 }
				 logger.warn("servicename[QueryOnuRateBySNService]deviceSN[{}]设备已绑定:",new Object[]{deviceSN});
					 //判断设备是否是百兆光猫
				 cpeMegabytes = userDevDao.queryHTMegabytes(deviceName);
 				 if(cpeMegabytes != 0){
					 checker.setGbbroadband("1"); //百兆
 				 }
			 	 else
				 {
					 cpeMegabytes = userDevDao.queryHTMegabyteInfo(deviceSN);
					 if(cpeMegabytes > 0){
 						 checker.setGbbroadband(String.valueOf(cpeMegabytes)); 
					 }else{
						 logger.warn("servicename[QueryOnuRateBySNService++]devSN[{}]:不存在千百兆光猫",new Object[] {deviceSN});
						 checker.setResult(1000);
						 checker.setResultDesc("非千百兆设备");
					 }
				  }

			 	  //JXDX-REQ-ITMS-20200602-WWF-001(江西电信ITMS+家庭网关对外接口-2.29接口调整) 增加devType 字段
				  //JXDX-REQ-ITMS-20200810-WWF-001(ITMS+家庭网关对外接口-2.29接口调整慢必赔) .doc
				Map result = userDevDao.queryDevType(deviceSN);

			 	String isNormal =  StringUtil.getStringValue(result.get("is_normal"));
				if("1".equals(isNormal))
				{
					checker.setIsNormal("1");
				}
				else
				{
					checker.setIsNormal("0");
				}
				devType = StringUtil.getStringValue(result.get("device_version_type"));

				String[] isTywg2UpList = Global.Tywg2UpList.split(";");
				if(Arrays.asList(isTywg2UpList).contains(devType))
				{
					checker.setIsTianyi2Up("1");
				}
				else
				{
					checker.setIsTianyi2Up("0");
				}

				if("4".equals(devType))
				{
					devType = "10GEPON";
				}
				else if("5".equals(devType))
				{
					devType = "XGPON";
				}
				else
				{
					devType = StringUtil.getStringValue(result.get("access_style_relay_id"));
					if("4".equals(devType))
					{
						devType = "GPON";
					}
					else if("3".equals(devType))
					{
						devType = "EPON";
					}
					else
					{
						devType = "其他";
					}
				}
				checker.setDevType(devType);

		    }
		}
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker,"QueryOnuRateBySNService",checker.getUserName());
		logger.warn("servicename[QueryOnuRateBySNService]回参为:{}",new Object[]{returnXml});
		return returnXml; 
	}
}
