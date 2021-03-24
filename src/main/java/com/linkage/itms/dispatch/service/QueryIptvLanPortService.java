package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryIptvLanPortChecker;

/**
 * 查询ITV所在LAN口情况
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2018-5-16
 */
public class QueryIptvLanPortService implements IService {

	// 日志记录
	private static Logger logger = LoggerFactory
			.getLogger(QueryIptvLanPortService.class);

	@Override
	public String work(String inXml) {
		QueryIptvLanPortChecker checker = new QueryIptvLanPortChecker(inXml);
		if (false == checker.check()) {
			logger.error("servicename[QueryIptvLanPortService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUsername(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn("servicename[QueryIptvLanPortService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUsername(),inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		String userId = "";
		// 获取用户帐号 or 终端序列号 
		if (1 == checker.getSearchType()) {
			// 根据用户帐号获取
			Map<String, String> userMap = userDevDao.queryUserInfo(checker.getUserInfoType(),checker.getUsername(), checker.getCityId());
			if (null == userMap || userMap.isEmpty()) {
				logger.warn("servicename[QueryIptvLanPortService]cmdId[{}]userinfo[{}]查无此用户",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1002);
				checker.setResultDesc("查无此客户");
				return checker.getReturnXml();
			} else {
				// 用户ID
				userId = userMap.get("user_id");
			}
		} else if (2 == checker.getSearchType()) {
			// 根据终端序列号
			Map<String, String> devMap  = userDevDao.queryUserIdByDevSn(checker.getDevSn());
			if (null == devMap || devMap.isEmpty()) {
				logger.warn("servicename[QueryIptvLanPortService]cmdId[{}]userinfo[{}]无此设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1004);
				checker.setResultDesc("查无此设备");
				return checker.getReturnXml();
			}else{
				// 用户ID
				userId = devMap.get("user_id");
			}
		}
		
		Map<String, String> servInfoMap  = userDevDao.queryServForIptv(userId);
		String bindPort = "";
		if (null == servInfoMap || servInfoMap.isEmpty()) {
			logger.warn("servicename[QueryIptvLanPortService]cmdId[{}]userinfo[{}]无此设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1004);
			checker.setResultDesc("该用户无IPTV业务");
			return checker.getReturnXml();
		}else{
			bindPort = StringUtil.getStringValue(servInfoMap, "bind_port","");
			bindPort = bindPort.replaceAll("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.", "LAN");
			bindPort = bindPort.replaceAll("InternetGatewayDevice.LANDevice.1.WLANConfiguration.", "WLAN");
		}
		checker.setIptvPort(bindPort);

		// 接口回复XML
		String returnXml = checker.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUsername(),
				"QueryIptvLanPortService");
		logger.warn(
				"servicename[QueryIptvLanPortService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
		// 回单
		return returnXml;
	}
	
}
