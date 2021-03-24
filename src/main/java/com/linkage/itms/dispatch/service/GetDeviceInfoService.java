package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.GetDeviceInfoDao;
import com.linkage.itms.dispatch.obj.GetDeviceInfoChecker;

/**
 * @author songxq
 * @version 1.0
 * @since 2020年1月8日 下午2:38:41
 * @category 
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class GetDeviceInfoService implements IService
{
	private static Logger logger = LoggerFactory.getLogger(GetDeviceInfoService.class);

	@Override
	public String work(String inXml)
	{
		logger.warn("GetDeviceInfoService==>inXml({})",inXml);
		GetDeviceInfoChecker checker = new GetDeviceInfoChecker(inXml);
		if (false == checker.check())
		{
			logger.warn("大唐获取光猫信息接口入参验证失败，UserInfoType=[{}]，UserInfo=[{}]",
	                    new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
	        logger.warn("GetDeviceInfoService==>retParam={}", checker.getReturnXml());
            return checker.getReturnXml();
		}
		
		GetDeviceInfoDao dao = new GetDeviceInfoDao();
		
		List<HashMap<String,String>> deviceMapList = null;
		if(checker.getUserInfoType() == 1)
        {
			deviceMapList = dao.getDeviceInfoByAccount(checker.getUserInfo());
        }
		else if (checker.getUserInfoType() == 2)
        {
        	deviceMapList = dao.getDeviceInfoByLoid(checker.getUserInfo());
        }
		
		if(null != deviceMapList && !deviceMapList.isEmpty())
		{
			HashMap<String, String> deviceMap = deviceMapList.get(0);
			checker.setDevSn(StringUtil.getStringValue(deviceMap,"device_serialnumber",""));
			checker.setVendor(StringUtil.getStringValue(deviceMap,"vendor_name",""));
			checker.setDeviceModel(StringUtil.getStringValue(deviceMap,"device_model",""));
			checker.setHardwareversion(StringUtil.getStringValue(deviceMap,"hardwareversion",""));
			checker.setSoftwareversion(StringUtil.getStringValue(deviceMap,"softwareversion",""));
			checker.setMac(StringUtil.getStringValue(deviceMap,"cpe_mac",""));
		}
		else 
		{
			logger.warn("大唐获取光猫信息接口未查询到设备信息，UserInfoType=[{}]，UserInfo=[{}]",
                    new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("未查询到设备信息");
			return checker.getReturnXml();
		}
		String returnXml = checker.getReturnXml();
		logger.warn(
				"servicename[GetDeviceInfoService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
		return returnXml;
	}
}

