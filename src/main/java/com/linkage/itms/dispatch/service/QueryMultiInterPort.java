package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.QueryMultiInterPortDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.QueryMultiInterPortCheck;

/**
 * 查询多宽带
 * @author zhangsibei
 *
 */
public class QueryMultiInterPort implements IService{
	private static Logger logger = LoggerFactory.getLogger(QueryMultiInterPort.class);

	@Override
	public String work(String param) {
		String user_id = "";
		
		List<HashMap<String,String>> netList = null;
		QueryMultiInterPortCheck checker = new QueryMultiInterPortCheck(param);
		if(false == checker.check())
		{
			logger.error(
					"servicename[QueryMultiInterPort]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[QueryMultiInterPort]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						param });
		QueryMultiInterPortDAO dao = new QueryMultiInterPortDAO();
		Map<String,String> NetData = dao.queryServInfo(checker.getUserInfoType(), checker.getUserInfo());
		if(null == NetData || NetData.isEmpty())
		{
			logger.warn(
					"servicename[QueryMultiInterPort]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1002);
			checker.setResultDesc("查无此用户");
		}
		else
		{
			 user_id = StringUtil.getStringValue(NetData, "user_id");
			if(StringUtil.IsEmpty(user_id))
			{
				logger.warn(
						"servicename[QueryMultiInterPort]cmdId[{}]userinfo[{}]用户不存在",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1002);
				checker.setResultDesc("查无此用户");
			}
			else
			{
				//查询多宽带
				netList = dao.queryMultiNetInfo(checker.getUserInfoType(), checker.getUserInfo());
				if(null!=netList){
					checker.setNetInfo(netList);
				}
			}
		}
		
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "QueryMultiInterPort");
		logger.warn(
				"servicename[QueryMultiInterPort]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}

}
