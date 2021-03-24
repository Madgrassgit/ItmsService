package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.OuiSearchChecker;
		
public class OuiSearchService implements IService
{
	private static final Logger logger = LoggerFactory.getLogger(OuiSearchService.class);
	 private UserDeviceDAO userDevDao = new UserDeviceDAO();

	@Override
	public String work(String inXml)
	{
		OuiSearchChecker checker = new OuiSearchChecker(inXml);
		if (!checker.check()) 
		{
			logger.error("serviceName[OuiSearchService]cmdId[{}]oui[{}]验证未通过，返回：{}",
			new Object[] { checker.getCmdId(), checker.getOui(),checker.getReturnXml() });
			return checker.getReturnXml();
		}
		Map<String,String> ouiMap=userDevDao.getDevOui(checker.getOui());
		if(null==ouiMap){
			logger.warn("serviceName[OuiSearchService]cmdId[{}]oui[{}]无此oui",
			new Object[] { checker.getCmdId(), checker.getOui()});
			checker.setRstCode("1001");
			checker.setRstMsg("OUI不存在，未通过认证");
			return checker.getReturnXml();
		}else{
			checker.setRstCode("0"); 
			checker.setRstMsg("OUI存在，已通过认证");
		}
		return checker.getReturnXml();
			
	}
}

	