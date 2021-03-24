/**
 * LINKAGE TECHNOLOGY (NANJING) CO.,LTD.<BR>
 * Copyright 2007-2010. All right reserved.
 */
package com.linkage.stbms.ids.util;

import org.omg.CORBA.ORB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import PreProcess.PPManagerHelper;
import PreProcess.UserInfo;
import StbCm.CMManagerHelper;

import com.linkage.commom.util.StaticTypeCommon;
import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.dao.InitDAO;
import com.linkage.stbms.ids.obj.SysConstant;
import com.linkage.stbms.itv.main.Global;

/**
 * CORBA operation for PreProcess.
 * 
 * @author Alex.Yan (yanhj@lianchuang.com)
 * @version 2.0, Jun 21, 2009
 * @see
 * @since 1.0
 */
public class PreProcessCorba {
	/** log */
	private static final Logger logger = LoggerFactory.getLogger(PreProcessCorba.class);
	/**
	 * 绑定设备后通知PP.
	 * 
	 * @param userInfoArr
	 * @return
	 *            <li>1:成功</li>
	 *            <li>-1:参数为空</li>
	 *            <li>-2:绑定失败</li>
	 */
	public int processServiceInterface(UserInfo[] userInfoArr) {
		logger.debug("processServiceInterface(UserInfo[])");
		if (userInfoArr == null) {
			logger.error("userInfoArr == null");
			return -1;
		}
		if(Global.G_Sysytem_Type==StaticTypeCommon.GTMS_Sysytem_Type)
		{
			// 是机顶盒与ITMS融合版本
			String xml = transXml(userInfoArr);
			boolean result = processServiceInterfaceStbMerge(xml);
			return result ? 1 : -2;
		}
		try {
			SysConstant.cmManager.processServiceInterface(userInfoArr);
		} catch (Exception e) {
			logger.debug("rebind PreProcess");
			String[] args = null;
			ORB PP_ORB = ORB.init(args, null);
			String ior = this.getCorbaIor(CommonUtil.getPrefix4IOR()+"PreProcess");
			if (StringUtil.IsEmpty(ior, true)) {
				return -2;
			}
			org.omg.CORBA.Object objRef = null;
			try {
				objRef = PP_ORB.string_to_object(ior);
				SysConstant.cmManager = PPManagerHelper.narrow(objRef);
				SysConstant.cmManager.processServiceInterface(userInfoArr);
			} catch (Exception ex) {
				logger.error("rebind PreProcess Error.\n{}", ex);
				return -2;
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < (userInfoArr.length); i++) {
			sb.append("[");
			sb.append(userInfoArr[i].deviceId);
			sb.append("]");
			if (i != userInfoArr.length - 1) {
				sb.append(",");
			}
		}
		logger.warn("inform pp user_serv:({})", sb.toString());
		return 1;
	}
	private static String transXml(UserInfo[] userInfoArr)
	{
		if (userInfoArr == null || userInfoArr.length == 0)
		{
			throw new NullPointerException();
		}
		StringBuilder result = new StringBuilder();
		result.append("<ServXml><servList>");
		for (UserInfo user : userInfoArr)
		{
			result.append("<serv><deviceId>").append(lpad(user.deviceId))
					.append("</deviceId><deviceSn>").append(lpad(user.deviceSn))
					.append("</deviceSn><oui>").append(lpad(user.oui))
					.append("</oui><serviceId>120</serviceId><userId>")
					.append(lpad(user.userId)).append("</userId>");
			result.append("</serv>");
		}
		result.append("</servList></ServXml>");
		return result.toString();
	}
	private static String lpad(String input)
	{
		return input == null ? "" : input;
	}
	/**
	 * ITMS与机顶盒融合版本，调用配置模块下发业务参数。
	 * @param xml
	 * @return
	 */
	private static boolean processServiceInterfaceStbMerge(String xml)
	{
		try
		{
			logger.warn("processServiceInterfaceStbMerge xml[{}]", xml);
			SysConstant.cmManagerStbMerge.processServiceInterface(xml);
		}
		catch (Exception e)
		{
			// ignore rebind
			String[] args = null;
			ORB PP_ORB = ORB.init(args, null);
			String ior = InitDAO.getCorbaIor(CommonUtil.getPrefix4IOR()+"PreProcess");
			if (StringUtil.IsEmpty(ior, true))
			{
				logger.warn("the value of corba named[{}] is empty, do not processServiceInterface and return false.",
						SysConstant.corba_corbaCM_name);
				return false;
			}
			org.omg.CORBA.Object objRef = null;
			try
			{
				objRef = PP_ORB.string_to_object(ior);
				SysConstant.cmManagerStbMerge = CMManagerHelper.narrow(objRef);
				SysConstant.cmManagerStbMerge.processServiceInterface(xml);
				return true;
			}
			catch (Exception ex)
			{
				logger.error(ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}
	/**
	 * Corba ior.
	 * 
	 * @return
	 */
	public String getCorbaIor(String objectName) {
		logger.debug("getCorbaIor({})",objectName);

		PrepareSQL pSQL = new PrepareSQL("select ior from tab_ior where object_name=?");
		pSQL.setString(1, objectName);

		String ior = StringUtil.getStringValue(DBOperation.getRecord(pSQL.getSQL()),"ior");

		if (null == ior) {
			logger.error("get Corba ior error");
			return ior;
		} else {
			logger.debug("Corba ior\n{}", ior);
			return ior.trim();
		}
	}
	/**
	 * 生成新的策略.
	 * 
	 * @param idArr
	 *            the id of strategy.
	 */
	public boolean processOOBatch(String id) {
		logger.debug("processOOBatch({})", id);

		boolean flag = false;

		if (id == null) {
			logger.error("id == null");

			return flag;
		}

		String[] idArr = new String[] { id };

		try {
			Global.G_PPManager.processOOBatch(idArr);
			flag = true;
		} catch (Exception e) {
			logger.warn("CORBA PreProcess Error:{},Rebind.", e.getMessage());

			InitDAO.initPreProcess();
			try {
				Global.G_PPManager.processOOBatch(idArr);
				flag = true;
			} catch (RuntimeException e1) {
				logger.error("CORBA SuperGather Error:{}", e1.getMessage());
			}
		}

		return flag;
	}

}
