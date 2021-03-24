package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.NetPasswordDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.NetPasswordCheck;

/**
 * 
 * @author Administrator (Ailk No.)
 * @version 1.0
 * @since 2013-12-5
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class NetPasswordService implements IService
{
	private static Logger logger = LoggerFactory.getLogger(NetPasswordService.class);

	@Override
	public String work(String inXml)
	{
		logger.warn("bindInfo:inXml({})", inXml);
		NetPasswordCheck binder = new NetPasswordCheck(inXml);
		if (false == binder.check())
		{
			logger.error(
					"servicename[NetPasswordService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { binder.getCmdId(), binder.getUserInfo(),
							binder.getReturnXml() });
			return binder.getReturnXml();
		}
		logger.warn(
				"servicename[NetPasswordService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { binder.getCmdId(), binder.getUserInfo(),
						inXml });
		NetPasswordDAO netDao = new NetPasswordDAO();
		Map<String,String> netPassMap = netDao.queryNetPassword(binder.getUserInfoType(), binder.getUserInfo());
		if (null == netPassMap || netPassMap.isEmpty())
		{
			logger.warn(
					"servicename[NetPasswordService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { binder.getCmdId(), binder.getUserInfo()});
			binder.setResult(1002);
			binder.setResultDesc("查无此用户");
		}
		else
		{
			String  netUsername = StringUtil.getStringValue(netPassMap.get("username"));
			String  netPassword = StringUtil.getStringValue(netPassMap.get("passwd"));
			binder.setResult(0);
			binder.setResultDesc("成功");
			binder.setNetUsername(netUsername);
			binder.setNetPassword(netPassword);
		}
		
		String returnXml = binder.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(binder, binder.getUserInfo(), "NetPasswordService");
		logger.warn(
				"servicename[NetPasswordService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { binder.getCmdId(), binder.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}
}
