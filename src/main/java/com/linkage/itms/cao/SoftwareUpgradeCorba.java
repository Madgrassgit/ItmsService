package com.linkage.itms.cao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import SoftUp.UserInfo;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.InitDAO;


public class SoftwareUpgradeCorba {
	/** log */
	private static final Logger logger = LoggerFactory.getLogger(SoftwareUpgradeCorba.class);

	/**
	 * 生成新软件升级的策略.
	 * 
	 * @param idArr
	 *            the id of strategy.
	 */
	public boolean softUpOOBatch(String[] idArr) {
		logger.debug("softUpOOBatch({})", idArr);

		boolean flag = false;

		if (idArr == null || idArr.length == 0) {
			logger.error("idArr == null");

			return flag;
		}

		try {
			Global.G_SUManager.processOOBatch(idArr);
			flag = true;
		} catch (Exception e) {
			logger.warn("CORBA SoftUp Error:{},Rebind.", e.getMessage());
			
			InitDAO.initSofUp();
			try {
				Global.G_SUManager.processOOBatch(idArr);
				flag = true;
			} catch (RuntimeException e1) {
				logger.error("CORBA SoftUp Error:{}", e1.getMessage());
			}
		}

		return flag;
	}

	/**
	 * 生成新的软件升级策略.
	 * 
	 * @param idArr
	 *            the id of strategy.
	 */
	public boolean softUpOOBatch(String id) {
		logger.debug("softUpOOBatch({})", id);

		boolean flag = false;

		if (id == null) {
			logger.error("id == null");

			return flag;
		}

		String[] idArr = new String[] { id };

		try {
			Global.G_SUManager.processOOBatch(idArr);
			flag = true;
		} catch (Exception e) {
			logger.warn("CORBA SoftUp Error:{},Rebind.", e.getMessage());

			InitDAO.initSofUp();
			try {
				Global.G_SUManager.processOOBatch(idArr);
				flag = true;
			} catch (RuntimeException e1) {
				logger.error("CORBA SoftUp Error:{}", e1.getMessage());
			}
		}

		return flag;
	}

	
	/**
	 * @param userInfo
	 * 
	 * @return  <li>1:成功</li> <li>-1:参数为空</li> <li>-2:绑定失败</li>
	 */
	public int softUpServiceInterface(UserInfo userInfo) {
		logger.debug("processServiceInterface(userInfo:{})", userInfo);
		if (userInfo == null) {
			logger.error("userInfo == null");
			return -1;
		}
		UserInfo[] userInfoArr = new UserInfo[]{userInfo};
		
		return softUpServiceInterface(userInfoArr);
	}
	
	
	/**
	 * @param userInfoArr
	 * 
	 * @return <li>1:成功</li> <li>-1:参数为空</li> <li>-2:绑定失败</li>
	 */
	public int softUpServiceInterface(UserInfo[] userInfoArr) {
		logger.debug("softUpServiceInterface(UserInfo[])");
		if (userInfoArr == null) {
			logger.error("userInfoArr == null");
			return -1;
		}

		try {
			Global.G_SUManager.processServiceInterface(userInfoArr);
		} catch (Exception e) {
			logger.warn("CORBA SoftUp Error:{},Rebind.", e.getMessage());

			InitDAO.initSofUp();
			try {
				Global.G_SUManager.processServiceInterface(userInfoArr);
			} catch (RuntimeException e1) {
				logger.error("rebind SoftUp Error.\n{}", e1);
				return -2;
			}
		}

		return 1;
	}
	
	
	
	
	/**
	 * 
	 * @param preInfoObj
	 * @return
	 */
	public static UserInfo GetSoftUpBindUserList(PreServInfoOBJ preInfoObj) {
		logger.debug("GetScheduleSQLList({})", preInfoObj);

		UserInfo uinfo = new UserInfo();
		uinfo.userId = StringUtil.getStringValue(preInfoObj.getUserId());
		uinfo.deviceId = StringUtil.getStringValue(preInfoObj.getDeviceId());
		uinfo.oui = StringUtil.getStringValue(preInfoObj.getOui());
		uinfo.deviceSn = StringUtil.getStringValue(preInfoObj.getDeviceSn());
		uinfo.gatherId = StringUtil.getStringValue(preInfoObj.getGatherId());
		uinfo.servTypeId = StringUtil.getStringValue(preInfoObj.getServTypeId());
		uinfo.operTypeId = StringUtil.getStringValue(preInfoObj.getOperTypeId());
		return uinfo;
	}
	
	
	
	/**
	 * 
	 * 长短定时器配置
	 * 
	 * @param deviceIds <br>
	 * @param serviceId <br>
	 * @param paramArr <br>
	 * 
	 * @return <li>1:成功</li> <li>-1:参数为空</li> <li>-2:失败</li>
	 */
	public int processDeviceStrategy(String[] deviceIds, String serviceId,	String[] paramArr) {
		logger.debug("processDeviceStrategy({},{},{})",new Object[]{deviceIds, serviceId, paramArr});
		if (deviceIds == null) {
			logger.error("deviceIds == null");
			return -1;
		}

		try {
			Global.G_SUManager.processDeviceStrategy(deviceIds, serviceId, paramArr);
		} catch (Exception e) {
			logger.warn("CORBA SoftUp Error:{},Rebind.", e.getMessage());
			
			InitDAO.initSofUp();
			try {
				Global.G_SUManager.processDeviceStrategy(deviceIds, serviceId, paramArr);
			} catch (RuntimeException e1) {
				logger.error(" SoftUp Error.\n{}", e1);
				return -2;
			}
		}

		return 1;
	}
	
}
