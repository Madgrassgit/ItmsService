package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dao.XJVoipProtocolDao;
import com.linkage.itms.dispatch.obj.XJVoipProtocolChecker;


public class XJVoipProtocolQueryService implements IService {
	
	private static final Logger logger = LoggerFactory
			.getLogger(XJVoipProtocolQueryService.class);
	
	public String work(String inXml){
		XJVoipProtocolChecker checker = new XJVoipProtocolChecker(inXml);
		
		// 验证入参格式是否正确
		if(false == checker.check()){
			logger.error(
					"servicename[XJVoipProtocolQueryService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[XJVoipProtocolQueryService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker
				.getUserInfoType(), checker.getUserInfo());
		
		// 此用户不存在，返回提示信息
		if (null == userInfoMap || userInfoMap.isEmpty()
				|| StringUtil.IsEmpty(userInfoMap.get("user_id"))) { 
			logger.warn(
					"servicename[XJVoipProtocolQueryService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1009);
			checker.setResultDesc("无此客户信息");
		// 存在此用户，则查询VOIP的语音协议
		} else {
			String userId = userInfoMap.get("user_id");
			
			// 根据user_id查询语音协议
			XJVoipProtocolDao dao = new XJVoipProtocolDao();
			
			Map<String, String> map = dao.queryXJVoipProtocol(userId); 
			String protocol = map.get("protocol");
			
			if (null == protocol || "".equals(protocol)) {
				checker.setProtocol("1");
			}else {
				checker.setProtocol(protocol);
			}
			
		}
		
		String retXml = checker.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "XJVoipProtocolQueryService");
		logger.warn(
				"servicename[XJVoipProtocolQueryService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),retXml});
		
		return retXml;
	}
}
