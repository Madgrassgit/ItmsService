package com.linkage.stbms.ids.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.ids.dao.GetStbConfigResultDAO;
import com.linkage.stbms.ids.util.GetStbConfigResultChecker;

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-18
 * @category com.linkage.stbms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class GetStbConfigResultService
{
	private static Logger logger = LoggerFactory
			.getLogger(GetStbConfigResultService.class);

	public String work(String inParam)
	{
		logger.warn("GetStbConfigResultService==>inParam:" + inParam);
		GetStbConfigResultChecker checker = new GetStbConfigResultChecker(inParam);
		// 入参验证
		if (false == checker.check())
		{
			logger.warn("零配置结果查询接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			logger.warn("GetStbConfigResultService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 在线情况
		String deviceId = null;
		GetStbConfigResultDAO dao = new GetStbConfigResultDAO();
		Map<String, String> userMap = dao.getDeviceIdStr(checker.getSearchType(),
				checker.getSearchInfo());
		
		if (null == userMap
				|| StringUtil.IsEmpty(StringUtil.getStringValue(userMap, "device_id")))
		{
			logger.warn("查无此设备，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			checker.setRstCode("1005");
			checker.setRstMsg("查无此设备");
			return checker.getReturnXml();
		}else if ("0".equals(StringUtil.getStringValue(userMap, "cpe_allocatedstatus"))){
			if("1".equals(checker.getSearchType())){
				logger.warn("设备未绑定");
				checker.setRstCode("1006");
				checker.setRstMsg("业务账号没有绑定设备");
			}else{
				logger.warn("设备没有和业务账号绑定");
				checker.setRstCode("1006");
				checker.setRstMsg("设备没有和业务账号绑定");
			}
			
			return checker.getReturnXml();
		}
		deviceId = userMap.get("device_id");
		Map<String, String> configResultMap = dao.getReturnValue(deviceId);
		if (null == configResultMap){
			logger.warn("查询成功，无零配置结果");
			checker.setRstCode("1007");
			checker.setRstMsg("查询成功，无零配置结果");
			return checker.getReturnXml();
		}
		
		if ("1".equals(StringUtil.getStringValue(userMap, "cpe_allocatedstatus"))){
			checker.setConfigResult("查询成功， " + configResultMap.get("return_value"));
		}
		
		checker.setRstCode("1");
		checker.setRstMsg("成功");
		logger.warn("GetStbConfigResultService==>returnXML:" + checker.getReturnXml());
		return checker.getReturnXml();
	}
}
