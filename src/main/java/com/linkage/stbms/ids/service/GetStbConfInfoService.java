
package com.linkage.stbms.ids.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.ids.dao.GetStbConfInfoDAO;
import com.linkage.stbms.ids.util.GetStbConfInfoCheck;

/**
 * @author Reno (Ailk No.)
 * @version 1.0
 * @since 2015年12月15日
 * @category com.linkage.stbms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class GetStbConfInfoService
{

	/** 日志 */
	private static Logger logger = LoggerFactory.getLogger(GetStbConfInfoService.class);

	public String work(String inParam)
	{
		logger.warn("GetStbConfInfoService==>inParam:" + inParam);
		GetStbConfInfoCheck checker = new GetStbConfInfoCheck(inParam);
		// 入参验证
		if (false == checker.check())
		{
			logger.warn("机顶盒工单参数实时查询接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			logger.warn("GetStbConfInfoService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 查询设备信息
		GetStbConfInfoDAO dao = new GetStbConfInfoDAO();
		Map<String, String> devMap = dao.getStbConfInfo(checker.getSearchType(),
				checker.getSearchInfo());
		if (null == devMap || devMap.isEmpty())
		{
			logger.warn("查无此设备或设备未绑定，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			checker.setRstCode("0");
			checker.setRstMsg("查无此设备或设备未绑定");
			logger.warn("GetStbConfInfoService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		else
		{
			Map<String, String> resultMap = new HashMap<String, String>();
			resultMap.put("result_flag", "1");
			resultMap.put("result", "成功");
			resultMap.put("serv_account",
					StringUtil.getStringValue(devMap, "serv_account"));
			resultMap.put("serv_pwd", getStringValue(devMap, "serv_pwd"));
			resultMap.put("authUrl", getStringValue(devMap, "auth_url"));
			String userStatus = getStringValue(devMap, "user_status");
			if ("1".equals(userStatus))
			{
				resultMap.put("openStatus", "成功");
			}
			else if ("0".equals(userStatus))
			{
				resultMap.put("openStatus", "未做");
			}
			else if ("-1".equals(userStatus))
			{
				resultMap.put("openStatus", "失败");
			}
			else
			{
				resultMap.put("openStatus", "");
			}
			String returnXML = checker.commonReturnParam(resultMap);
			logger.warn("GetStbConfInfoService==>return：" + returnXML);
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
