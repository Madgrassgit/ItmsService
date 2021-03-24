package com.linkage.itms.dispatch.cqdx.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.dao.IPTVLanDAO;
import com.linkage.itms.dispatch.cqdx.obj.IPTVLanDealXML;

/**
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2017年11月19日
 * @category com.linkage.itms.dispatch.cqdx.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryIPTVLanService
{
	private static Logger logger = LoggerFactory.getLogger(QueryIPTVLanService.class);

	//用户宽带帐号
	private final int USERINFOTYPE_1 =1;
	//LOID
	private final int USERINFOTYPE_2 =2;
	
	public String work(String inXml) {
		logger.warn("servicename[QueryIPTVLanService]执行，入参为：{}", inXml);
		IPTVLanDealXML deal = new IPTVLanDealXML();
		//校验入参
		Document document = deal.getXML(inXml);
		if (document == null) 
		{
			logger.warn("servicename[QueryIPTVLanService]解析入参错误！");
			return deal.returnXML();
		}
		else
		{
			UserDeviceDAO userDevDao = new UserDeviceDAO();
			Map<String, String> userInfoMap  = null;
			if(!StringUtil.IsEmpty(deal.getPppUsename()))
			{
				userInfoMap = userDevDao.queryUserInfo(USERINFOTYPE_1, deal.getPppUsename(), null);
			}
			else if(!StringUtil.IsEmpty(deal.getLogicId()))
			{
				userInfoMap = userDevDao.queryUserInfo(USERINFOTYPE_2, deal.getLogicId(), null);
			}
			
			//用户不存在
			if (null == userInfoMap || userInfoMap.isEmpty()) {
				logger.warn(
						"servicename[QueryIPTVLanService] ppp_username[{}] , logic_id[{}]查无此用户",
						new Object[] {deal.getPppUsename(), deal.getLogicId()});
				deal.setResult("-1");
				deal.setErrMsg("无此用户信息");
			} 
			//用户存在
			else
			{
				IPTVLanDAO dao = new IPTVLanDAO();
				List<HashMap<String,String>> iptvInfo = dao.iptvPortByUserName(userInfoMap.get("username"));
				if(null == iptvInfo)
				{
					deal.setResult("-99");
					deal.setErrMsg("iptv信息为空");
				}
				else
				{
					deal.setResult("0");
					StringBuilder sb = new StringBuilder();
					for(HashMap<String,String> map : iptvInfo)
					{
						if(!StringUtil.IsEmpty(StringUtil.getStringValue(map, "real_bind_port")))
						{
							sb.append(StringUtil.getStringValue(map, "real_bind_port").replace("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.", "LAN")).append(",");
						}
						if(!StringUtil.IsEmpty(StringUtil.getStringValue(map, "bind_port")))
						{
							deal.setExpectIPTVPort(StringUtil.getStringValue(map, "bind_port").replace("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.", "LAN"));
						}
						
					}
					
					if(sb.length() >=1)
					{
						deal.setActualIPTVPort(sb.toString().replace("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.", "LAN").substring(0, sb.length()-1));
					}
					
				}
			}
		}
			
			
		return deal.returnXML();
	}
}
