
package com.linkage.stbms.ids.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.obj.ChangePasswordCheck;

public class ChangePasswordService {

	/** 日志 */
	private static Logger logger = LoggerFactory.getLogger(ChangePasswordService.class);

	public String work(String inParam) {
		logger.warn("ChangePasswordService==>inParam:" + inParam);
		ChangePasswordCheck checker = new ChangePasswordCheck(inParam);
		// 入参验证
		if (false == checker.check()) {
			logger.warn("servicename[ChangePasService]cmdId[{}]验证未通过，返回!", checker.getCmdId());
			logger.warn("ChangePasswordService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		UserStbInfoDAO dao = new UserStbInfoDAO();
		String servAccount = checker.getUserId();
		String servpwd = checker.getPassword1();
		String pppoeUser = checker.getPppoeId();
		String pppoePwd = checker.getPassword2();
		Map<String, String> devMap = dao.getStbCustomerInfo(servAccount, pppoeUser);
		
		String customerId = StringUtil.getStringValue(devMap, "customer_id");
		if (StringUtil.IsEmpty(customerId)) {
			checker.setRstCode("1001");
			checker.setRstMsg("无此用户信息");
			return checker.getReturnXml();
		}
		int ret = dao.updateStbCustomerPswd(servAccount, servpwd, pppoeUser, pppoePwd);
		if (ret == 0) {
			checker.setRstCode("0");
			checker.setRstMsg("更改密码失败");
		}
		return checker.getReturnXml();
	}
}
