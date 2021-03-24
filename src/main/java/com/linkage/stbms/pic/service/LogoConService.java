
package com.linkage.stbms.pic.service;

import java.rmi.dgc.VMID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.pic.bio.LogoConBio;
import com.linkage.stbms.pic.object.StrategyObj;
import com.linkage.stbms.pic.process.ProcessWork;

public class LogoConService
{

	private final Logger logger = LoggerFactory.getLogger(LogoConService.class);

	public String work(String inXml)
	{
		// 检查合法性
		LogoConChecker serviceDoner = new LogoConChecker(inXml);
		if (false == serviceDoner.check())
		{
			logger.error(
					"servicename[ServiceDoneService]cmdId[{}]deviceid[{}]验证未通过，返回：{}",
					new Object[] { serviceDoner.getCmdId(), serviceDoner.getDeviceId(),
							serviceDoner.getReturnXml() });
			return serviceDoner.getReturnXml();
		}
		logger.warn(
				"servicename[LogoConService]cmdId[{}]deviceid[{}]参数校验通过，入参为：{}",
				new Object[] { serviceDoner.getCmdId(), serviceDoner.getDeviceId(), inXml });
		StrategyObj strategyObjLogoCon = null;
		LogoConBio logoConBio = new LogoConBio();
		strategyObjLogoCon = logoConBio.getLogoConStrategy(serviceDoner.getDeviceId());
		if (strategyObjLogoCon != null)
		{
			String strategyId = StringUtil.getStringValue(strategyObjLogoCon.getId());
			String id = new VMID().toString();
			ProcessWork work = new ProcessWork(id);
			work.setStrategyId(strategyId);
			logger.warn("preProcess-strategyId:{}", strategyId);
			DevRpc[] devRpcs = work.work();
			if (devRpcs.length > 0)
			{
				serviceDoner.setDevRpc(devRpcs[0]);
			}
		}
		String str = serviceDoner.getReturnXml();
		logger.warn(
				"servicename[LogoConService]cmdId[{}]deviceid[{}]处理结束，回参为：{}",
				new Object[] { serviceDoner.getCmdId(), serviceDoner.getDeviceId(), str });
		return str;
	}
}
