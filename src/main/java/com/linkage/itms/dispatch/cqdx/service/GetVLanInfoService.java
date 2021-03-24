package com.linkage.itms.dispatch.cqdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.obj.GetVLanInfoXML;


public class GetVLanInfoService {

	private static Logger logger = LoggerFactory.getLogger(GetVLanInfoService.class);

	//用户宽带帐号
	private final int USERINFOTYPE_1 =1;
	//LOID
	private final int USERINFOTYPE_2 =2;
	
	/**
	 * 绑定执行方法
	 */
	public String work(String inXml) {
		GetVLanInfoXML getVLanInfoXML = new GetVLanInfoXML(inXml);
		if (false == getVLanInfoXML.check()) {
			logger.error("servicename[GetVLanInfoService]ppp_username[{}],logic_id[{}]验证未通过，返回：{}",
					new Object[] {  getVLanInfoXML.getPpp_username(),getVLanInfoXML.getLogic_id(),getVLanInfoXML.getReturnXml() });
			return getVLanInfoXML.getReturnXml();
		}
		logger.warn("servicename[GetVLanInfoService]ppp_username[{}],logic_id[{}]参数校验通过，入参为：{}",
				new Object[] { getVLanInfoXML.getPpp_username(),getVLanInfoXML.getLogic_id(),inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		Map<String, String> userDevInfo  = null;
		if(!StringUtil.IsEmpty(getVLanInfoXML.getPpp_username()))
		{
			userDevInfo = userDevDao.queryUserInfo(USERINFOTYPE_1, getVLanInfoXML.getPpp_username(), null);
			getVLanInfoXML.setLogic_id(StringUtil.getStringValue(userDevInfo,"username"));
		}
		else if(!StringUtil.IsEmpty(getVLanInfoXML.getLogic_id()))
		{
			userDevInfo = userDevDao.queryUserInfo(USERINFOTYPE_2, getVLanInfoXML.getLogic_id(), null);
		}
		
		if (null == userDevInfo || userDevInfo.isEmpty()) {
			logger.warn("servicename[GetVLanInfoService]ppp_username=[{}],logic_id=[{}]查无此用户",
					new Object[] {  getVLanInfoXML.getPpp_username(),getVLanInfoXML.getLogic_id() });
			getVLanInfoXML.setResulltCode(-1);
			getVLanInfoXML.setResultDesc("用户不存在");
			logger.warn("GetVLanInfoService==>retParam={}",getVLanInfoXML.getReturnXml());
			return getVLanInfoXML.getReturnXml();
		}
			// 查询设备信息 
			ArrayList<HashMap<String, String>> devInfoMapList = userDevDao.getVlanInfoList(null,getVLanInfoXML.getLogic_id());
			logger.warn("devInfoMapList="+devInfoMapList.size());
			if (null == devInfoMapList || devInfoMapList.isEmpty()) {
				logger.warn("servicename[GetVLanInfoService]ppp_username[{}],logic_id[{}]查无此设备",
						new Object[] { getVLanInfoXML.getPpp_username(),getVLanInfoXML.getLogic_id()});
				getVLanInfoXML.setResulltCode(-99);
				getVLanInfoXML.setResultDesc("查无此设备");
				logger.warn("GetVLanInfoService==>retParam={}",getVLanInfoXML.getReturnXml());
				return getVLanInfoXML.getReturnXml();
			}else
			{//查到设备
				int size = devInfoMapList.size();
				if (size <= 0) 
				{
					logger.warn("servicename[GetVLanInfoService]ppp_username[{}],logic_id[{}]未查询到VLAN信息",
							new Object[] {getVLanInfoXML.getPpp_username(),getVLanInfoXML.getLogic_id()});
					getVLanInfoXML.setResulltCode(-99);
					getVLanInfoXML.setResultDesc("未查询到VLAN信息");
					return getVLanInfoXML.getReturnXml();
				}else
				{
					getVLanInfoXML.setResMap(devInfoMapList);
					getVLanInfoXML.setResulltCode(0);
					getVLanInfoXML.setResultDesc("执行成功");
					return getVLanInfoXML.getReturnXml();
				}
				
			}
		}

}
