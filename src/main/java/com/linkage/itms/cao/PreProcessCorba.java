/**
 * LINKAGE TECHNOLOGY (NANJING) CO.,LTD.<BR>
 * Copyright 2007-2010. All right reserved.
 */

package com.linkage.itms.cao;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import PreProcess.OneToMany;
import PreProcess.UserInfo;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.PreProcessInterface;
import com.linkage.itms.dao.InitDAO;

/**
 * CORBA operation for PreProcess.
 * 
 * @author Alex.Yan (yanhj@lianchuang.com)
 * @version 2.0, Jun 21, 2009
 * @see
 * @since 1.0
 */
public class PreProcessCorba implements PreProcessInterface
{

	/** log */
	private static final Logger logger = LoggerFactory.getLogger(PreProcessCorba.class);

	/**
	 * 生成新的策略.
	 * 
	 * @param idArr
	 *            the id of strategy.
	 */
	public boolean processOOBatch(String[] idArr)
	{
		logger.debug("processOOBatch({})", idArr);
		boolean flag = false;
		if (idArr == null || idArr.length == 0)
		{
			logger.error("idArr == null");
			return flag;
		}
		try
		{
			Global.G_PPManager.processOOBatch(idArr);
			flag = true;
		}
		catch (Exception e)
		{
			logger.warn("CORBA PreProcess Error:{},Rebind.", e.getMessage());
			InitDAO.initPreProcess();
			try
			{
				Global.G_PPManager.processOOBatch(idArr);
				flag = true;
			}
			catch (RuntimeException e1)
			{
				logger.error("CORBA SuperGather Error:{}", e1.getMessage());
			}
		}
		return flag;
	}

	/**
	 * 生成新的策略.
	 * 
	 * @param idArr
	 *            the id of strategy.
	 */
	public boolean processOOBatch(String id)
	{
		logger.debug("processOOBatch({})", id);
		boolean flag = false;
		if (id == null)
		{
			logger.error("id == null");
			return flag;
		}
		String[] idArr = new String[] { id };
		try
		{
			Global.G_PPManager.processOOBatch(idArr);
			flag = true;
		}
		catch (Exception e)
		{
			logger.warn("CORBA PreProcess Error:{},Rebind.", e.getMessage());
			InitDAO.initPreProcess();
			try
			{
				Global.G_PPManager.processOOBatch(idArr);
				flag = true;
			}
			catch (RuntimeException e1)
			{
				logger.error("CORBA SuperGather Error:{}", e1.getMessage());
			}
		}
		return flag;
	}

	/**
	 * 默认业务生成新的策略.
	 * 
	 * @param idArr
	 *            the id of strategy.
	 */
	public boolean processOMBatch4DefaultService(OneToMany[] objArr)
	{
		logger.debug("processOMBatch4DefaultService({})", objArr);
		boolean flag = false;
		if (objArr == null || objArr.length == 0)
		{
			logger.error("objArr == null");
			return flag;
		}
		try
		{
			Global.G_PPManager.processOMBatch4DefaultService(objArr);
			flag = true;
		}
		catch (Exception e)
		{
			logger.warn("CORBA PreProcess Error:{},Rebind.", e.getMessage());
			InitDAO.initPreProcess();
			try
			{
				Global.G_PPManager.processOMBatch4DefaultService(objArr);
				flag = true;
			}
			catch (RuntimeException e1)
			{
				logger.error("CORBA SuperGather Error:{}", e1.getMessage());
			}
		}
		return flag;
	}

	/**
	 * 默认业务生成新的策略.
	 * 
	 * @param idArr
	 *            the id of strategy.
	 */
	public boolean processOMBatch4DefaultService(OneToMany obj)
	{
		logger.debug("processOMBatch4DefaultService({})", obj);
		boolean flag = false;
		if (obj == null)
		{
			logger.error("objArr == null");
			return flag;
		}
		OneToMany[] objArr = new OneToMany[1];
		objArr[0] = obj;
		try
		{
			Global.G_PPManager.processOMBatch4DefaultService(objArr);
			flag = true;
		}
		catch (Exception e)
		{
			logger.warn("CORBA PreProcess Error:{},Rebind.", e.getMessage());
			InitDAO.initPreProcess();
			try
			{
				Global.G_PPManager.processOMBatch4DefaultService(objArr);
				flag = true;
			}
			catch (RuntimeException e1)
			{
				logger.error("CORBA SuperGather Error:{}", e1.getMessage());
			}
		}
		return flag;
	}

	/**
	 * 绑定设备后通知PP.
	 * 
	 * @param userInfoArr
	 * @return <li>1:成功</li> <li>-1:参数为空</li> <li>-2:绑定失败</li>
	 */
	public int processServiceInterface(UserInfo userInfo)
	{
		logger.debug("processServiceInterface(userInfo:{})", userInfo);
		if (userInfo == null)
		{
			logger.error("userInfo == null");
			return -1;
		}
		UserInfo[] userInfoArr = new UserInfo[] { userInfo };
		return processServiceInterface(userInfoArr);
	}

	/**
	 * 绑定设备后通知PP.
	 * 
	 * @param userInfoArr
	 * @return <li>1:成功</li> <li>-1:参数为空</li> <li>-2:绑定失败</li>
	 */
	public int processServiceInterface(UserInfo[] userInfoArr)
	{
		logger.debug("processServiceInterface(UserInfo[])");
		if (userInfoArr == null)
		{
			logger.error("userInfoArr == null");
			return -1;
		}
		try
		{
			Global.G_PPManager.processServiceInterface(userInfoArr);
		}
		catch (Exception e)
		{
			logger.warn("CORBA PreProcess Error:{},Rebind.", e.getMessage());
			InitDAO.initPreProcess();
			try
			{
				Global.G_PPManager.processServiceInterface(userInfoArr);
			}
			catch (RuntimeException e1)
			{
				logger.error("rebind PreProcess Error.\n{}", e1);
				return -2;
			}
		}
		return 1;
	}

	/**
	 * <pre>
	 * 调用配置模块
	 * paramArr[0]=开启/关闭 
	 * paramArr[1]=上报周期 
	 * paramArr[2]=文件上传IP地址
	 * paramArr[3]=参数列表（多个用逗号隔开，例如1,3,4）
	 * paramArr[4]=端口号
	 * paramArr[5]=task_id 
	 * 参数列表：1. 物理状态  2.语音业务注册状态 3. 语音业务注册失败原因
	 * 4. PPP拨号上网的连接状态 5. 拨号错误代码
	 * </pre>
	 * 
	 * @param deviceIdArr
	 * @param serviceId
	 * @param paramArr
	 * @return
	 */
	public boolean processDeviceStrategy(String[] deviceIdArr, String serviceId,
			String[] paramArr)
	{
		if (deviceIdArr == null || deviceIdArr.length == 0)
		{
			throw new IllegalArgumentException("array of device id must not empty");
		}
		if (paramArr == null || deviceIdArr.length == 0)
		{
			throw new IllegalArgumentException("array of param must not empty");
		}
		try
		{
			Global.G_PPManager.processDeviceStrategy(deviceIdArr, serviceId, paramArr);
			return true;
		}
		catch (Exception e)
		{
			try
			{
				InitDAO.initPreProcess();
				Global.G_PPManager
						.processDeviceStrategy(deviceIdArr, serviceId, paramArr);
				return true;
			}
			catch (Exception ee)
			{
				logger.error("processDeviceStrategy args, deviceIdArr=["
						+ Arrays.toString(deviceIdArr) + "], serviceId=[" + serviceId
						+ "], paramArr=[" + Arrays.toString(paramArr));
				logger.error(ee.getMessage(), ee);
				return false;
			}
		}
	}

	/**
	 * get userInfo from params
	 * 
	 * @param length
	 * @return
	 */
	public UserInfo GetPPBindUserList(PreServInfoOBJ preInfoObj)
	{
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

	@Override
	public boolean processSTBServiceInterface(String xmlstr)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
