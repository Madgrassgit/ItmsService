
package com.linkage.stbms.ids.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.stbms.ids.dao.GetStbBaseInfoDAO;
import com.linkage.stbms.ids.util.GetStbBaseInfoChecker;

/**
 * @author yinlei3 (73167.)
 * @version 1.0
 * @since 2015年12月15日
 * @category com.linkage.stbms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class GetStbBaseInfoService
{

	/** 日志 */
	private static Logger logger = LoggerFactory.getLogger(GetStbBaseInfoService.class);

	public String work(String inParam)
	{
		logger.warn("GetStbBaseInfoService==>inParam:" + inParam);
		GetStbBaseInfoChecker checker = new GetStbBaseInfoChecker(inParam);
		// 入参验证
		if (false == checker.check())
		{
			logger.warn("机顶盒设备信息实时查询接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			logger.warn("GetStbBaseInfoService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 查询设备信息
		GetStbBaseInfoDAO dao = new GetStbBaseInfoDAO();
		Map<String, String> devMap = dao.getDevBaseInfo(checker.getSearchType(),
				checker.getSearchInfo());
		if (null == devMap || devMap.isEmpty())
		{
			logger.warn("查无此设备，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			checker.setRstCode("0");
			checker.setRstMsg("查无此设备");
			logger.warn("GetStbBaseInfoService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		else
		{
			Map<String, String> resultMap = new HashMap<String, String>();
			resultMap.put("result_flag", "1");
			resultMap.put("result", "成功");
			resultMap.put("stb_vendor", getStringValue(devMap, "vendor_name"));
			resultMap.put("stb_hardwareversion", getStringValue(devMap, "hardwareversion"));
			resultMap.put("stb_softversion", getStringValue(devMap, "softwareversion"));
			resultMap.put("stb_type", getStringValue(devMap, "device_model"));
			resultMap.put("stb_city", getStringValue(devMap, "city_name"));
			String returnXML = checker.commonReturnParam(resultMap);
			logger.warn("GetStbBaseInfoService==>return：" + returnXML);
			return returnXML;
		}
	}

	@SuppressWarnings("rawtypes")
	private String getStringValue(Map map, String columName)
	{
		if (null == columName)
		{
			return null;
		}
		if (null == map)
		{
			return null;
		}
		if (null == map.get(columName))
		{
			return "";
		}
		return map.get(columName).toString();
	}
}
