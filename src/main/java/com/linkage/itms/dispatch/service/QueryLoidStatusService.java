
package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.QueryLoidStatusDAO;
import com.linkage.itms.dispatch.obj.QueryLoidStatusChecker;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-6-30
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QueryLoidStatusService implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(QueryLoidStatusService.class);

	@Override
	public String work(String inParam)
	{
		logger.warn("QueryLoidStatusService==>inParam:" + inParam);
		QueryLoidStatusChecker checker = new QueryLoidStatusChecker(inParam);
		Map<String, String> resultMap = null;
		checker.setDevSn("");
		if (false == checker.check())
		{
			logger.warn("获取工单状态查询接口，入参验证失败，loid=[{}]",
					new Object[] { checker.getLoid() });
			logger.warn("QueryLoidStatusService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		String userId = "";
		QueryLoidStatusDAO dao = new QueryLoidStatusDAO();
		userId = dao.getUserIdByLoid(checker.getLoid());
		
		if (StringUtil.IsEmpty(userId))
		{
			logger.warn("serviceName[QueryLoidStatusService]cmdId[{}]loid[{}]无此用户",
					new Object[] { checker.getCmdId(), checker.getLoid() });
			checker.setResult(1003);
			checker.setResultDesc("查无此客户");
			return checker.getReturnXml();
		}
		resultMap = dao.getSnByUser(userId);
		if (null == resultMap || resultMap.isEmpty())
		{
			logger.warn("serviceName[QueryLoidStatusService]cmdId[{}]loid[{}]此用户未开通工单",
					new Object[] { checker.getCmdId(), checker.getLoid() });
			checker.setResult(1004);
			checker.setResultDesc("查无工单");
			return checker.getReturnXml();
		}
		
		checker.setDevSn(StringUtil.getStringValue(resultMap,"device_serialnumber", ""));
		checker.setStatus(StringUtil.getStringValue(resultMap,"open_status", ""));
		String returnXML = checker.getReturnXml();
		logger.warn("QueryLoidStatusService==>returnXML:" + returnXML);
		return returnXML;
	}
}
