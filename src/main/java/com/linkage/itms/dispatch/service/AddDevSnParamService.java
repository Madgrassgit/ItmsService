
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.CityDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.AddDevSnParamChecker;

/**
 * 光猫管控设备序列号添加接口
 * 
 * @author yinlei3 (Ailk No.73167)
 * @version 1.0
 * @since 2016年9月5日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class AddDevSnParamService implements IService
{

	/** 日志 */
	private static final Logger logger = LoggerFactory
			.getLogger(AddDevSnParamService.class);

	@Override
	public String work(String inXml)
	{
		AddDevSnParamChecker checker = new AddDevSnParamChecker(inXml);
		// 验证入参格式是否正确
		if (false == checker.check())
		{
			logger.error(
					"servicename[AddDevSnParamService]cmdId[{}]devSn[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getDevSn(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn("servicename[AddDevSnParamService]cmdId[{}]devSn[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getDevSn(), inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 根据终端序列号查询设备信息表
		ArrayList<HashMap<String, String>> devlsit = userDevDao.queryDevInfo(checker
				.getDevSn());
		if (null == devlsit || devlsit.isEmpty())
		{
			// 根据终端序列号查询设备预置表
			devlsit = userDevDao.queryDevInfoInit(checker.getDevSn());
			// 两表均没有， 入设备预置表
			if (null == devlsit || devlsit.isEmpty())
			{
				logger.warn(
						"servicename[AddDevSnParamService]cmdId[{}]devSn[{}] 设备入tab_gw_device_init表",
						new Object[] { checker.getCmdId(), checker.getDevSn() });
				String city_id = CityDAO.getCityId(checker.getCityName());
				if (StringUtil.IsEmpty(city_id))
				{
					city_id = "00";
				}
				long saveTime = new DateTimeUtil(checker.getSaveTime()).getLongTime();
				long buyTime = 0l;
				if (!StringUtil.IsEmpty(checker.getBuyTime()))
				{
					buyTime = new DateTimeUtil(checker.getBuyTime()).getLongTime();
				}
				userDevDao.insertDevInit(checker.getOui(), checker.getDevSn(),
						checker.getDevMac(), city_id, buyTime, saveTime,
						checker.getVendor(), checker.getModel(), checker.getOperaUser());
				checker.setResult(0);
				checker.setResultDesc("设备信息添加成功");
			}
			else
			{
				logger.warn(
						"servicename[AddDevSnParamService]cmdId[{}]devSn[{}] tab_gw_device_init已存在此设备信息",
						new Object[] { checker.getCmdId(), checker.getDevSn() });
				checker.setResult(1003);
				checker.setResultDesc("设备信息已存在");
			}
		}
		else
		{
			logger.warn(
					"servicename[AddDevSnParamService]cmdId[{}]devSn[{}] tab_gw_device已存在此设备信息",
					new Object[] { checker.getCmdId(), checker.getDevSn() });
			checker.setResult(1003);
			checker.setResultDesc("设备信息已存在");
		}
		String returnXml = checker.getReturnXml();
		logger.warn("servicename[AddDevSnParamService]cmdId[{}]devSn[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getDevSn(), returnXml });
		return returnXml;
	}
}
