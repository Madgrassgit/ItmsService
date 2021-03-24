
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryDeviceControlChecker;

/**
 * @author yinlei3 (Ailk No.73167)
 * @version 1.0
 * @since 2016年3月29日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QueryDeviceControlService implements IService
{

	/** 日志对象 */
	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceControlService.class);

	@Override
	public String work(String inXml)
	{
		logger.warn("QueryDeviceControlService：inXml({})", inXml);
		QueryDeviceControlChecker checker = new QueryDeviceControlChecker(inXml);
		if (false == checker.check())
		{
			logger.error(
					"servicename[QueryAccessTypeService]cmdId[{}]devSn[{}]验证未通过.返回：{}",
					new Object[] { checker.getCmdId(), checker.getCmdId(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[QueryDeviceControlService]cmdId[{}]devSn[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getDevSn(), inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 根据终端序列号查询设备信息表
		ArrayList<HashMap<String, String>> devlsit = userDevDao.queryDevInfo(checker
				.getDevSn());
		if (null == devlsit || devlsit.isEmpty())
		{
			// 根据终端序列号查询设备预置表
			devlsit = userDevDao.queryDevInfoInit(checker.getDevSn());
			if (null == devlsit || devlsit.isEmpty())
			{
				checker.setResult(0);
				checker.setResultDesc("成功");
				checker.setDevStatus("2");
			}
			else if (devlsit.size() > 1)
			{
				logger.warn(
						"servicename[QueryDeviceControlService]cmdId[{}]devSn[{}]查询到多台设备",
						new Object[] { checker.getCmdId(), checker.getDevSn() });
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				checker.setDevStatus("");
			}
			else
			{
				checker.setResult(0);
				checker.setResultDesc("成功");
				checker.setDevStatus("1");
			}
		}
		else if (devlsit.size() > 1)
		{
			logger.warn(
					"servicename[QueryDeviceControlService]cmdId[{}]devSn[{}]查询到多台设备",
					new Object[] { checker.getCmdId(), checker.getDevSn() });
			checker.setResult(1006);
			checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
			checker.setDevStatus("");
		}
		else
		{
			logger.warn(
					"servicename[QueryDeviceControlService]cmdId[{}]devSn[{}]成功",
					new Object[] { checker.getCmdId(), checker.getDevSn() });
			checker.setResult(0);
			checker.setResultDesc("成功");
			checker.setDevStatus("1");
		}
		return checker.getReturnXml();
	}
}
