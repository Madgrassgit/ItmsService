package com.linkage.itms.dispatch.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.QueryVoipDAO;
import com.linkage.itms.dispatch.obj.QueryVoipChecker;



public class QueryVoip {
	private static Logger logger = LoggerFactory.getLogger(QueryVoip.class);
	
	public String work(String inXml){
		logger.warn("queryvoip==>inXml({})",inXml);
		QueryVoipChecker checker = new QueryVoipChecker(inXml);
		if (!checker.check()) {
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		String deviceId = "";
		List<HashMap<String, String>> userMapList = null;
		QueryVoipDAO dao = new QueryVoipDAO();
		userMapList = dao.queryegwUserByLoid(checker.getLoid());
		if (userMapList == null || userMapList.isEmpty()) {
			logger.warn("无此客户信息");
			checker.setResult(1002);
			checker.setResultDesc("无此客户信息");
			return checker.getReturnXml();
		}
		deviceId = StringUtil.getStringValue(userMapList.get(0),"device_id", "");
		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("此用户没有设备关联信息");
			checker.setResult(1003);
			checker.setResultDesc("此用户没有设备关联信息");
			return checker.getReturnXml();
		}
		String userId = StringUtil.getStringValue(userMapList.get(0),"user_id", "");
		String voip = checker.getVoip();
		logger.warn("here");
		ArrayList<HashMap<String, String>> voipInfoList = dao.queryVoipInfo(voip, userId);
		logger.warn("there");
		Set<String> keySet = voipInfoList.get(0).keySet();
		for(String key:keySet) {
			logger.warn(key);
		}
		HashMap<String, String> map = voipInfoList.get(0);
		checker.setVOIPPvcOrVlanId(map.get("vlanid"));
		checker.setVOIPType(map.get("protocol"));
		checker.setProxyServer(map.get("prox_serv"));
		checker.setProxyServerPort(map.get("prox_port"));
		checker.setStandByProxyServer(map.get("stand_prox_serv"));
		checker.setStandByOutboundProxyPort(map.get("stand_prox_port"));
		checker.setRegistrarServer(map.get("regi_serv"));
		checker.setRegistrarServerPort(map.get("regi_port"));
		checker.setStandByRegistrarServer("stand_regi_serv");
		checker.setStandByRegistrarServerPort("stand_regi_port");
		checker.setOutboundProxy("out_bound_proxy");
		checker.setOutboundProxyPort("out_bound_port");
		checker.setStandByOutboundProxy(map.get("stand_out_bound_proxy"));
		checker.setStandByOutboundProxyPort(map.get("stand_out_bound_port"));
		List<String> authPasswordList = new ArrayList<String>();
		List<String> authUserNameList = new ArrayList<String>();
		for(HashMap<String, String> voipMap:voipInfoList) {
			authPasswordList.add(voipMap.get("voip_username"));
			authUserNameList.add(voipMap.get("voip_passwd"));
		}
		checker.setAuthPasswordList(authPasswordList);
		checker.setAuthUserNameList(authUserNameList);
		
		return checker.getReturnXml();
	}
}
