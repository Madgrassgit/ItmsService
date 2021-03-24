
package com.linkage.itms.cao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.Global;
import com.linkage.itms.ResourceBindInterface;
import com.linkage.itms.dao.InitDAO;

import ResourceBind.BindInfo;
import ResourceBind.BlManager;
import ResourceBind.ResultInfo;
import ResourceBind.UnBindInfo;

/**
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2015年12月25日
 * @category com.linkage.itms.cao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ResourceBindCorba implements ResourceBindInterface
{

	/** log */
	private static final Logger logger = LoggerFactory.getLogger(ResourceBindCorba.class);
	private BlManager G_ResourceBind = null;
	private String gw_type;

	public ResourceBindCorba(String gw_type)
	{
		this.gw_type = gw_type;
		init();
	}

	private void init()
	{
		if (Global.GW_TYPE_ITMS.equals(gw_type))
		{
			G_ResourceBind = Global.G_BlManager_ITMS;
		}
		else if (Global.GW_TYPE_BBMS.equals(gw_type))
		{
			G_ResourceBind = Global.G_BlManager_BBMS;
		}
		else if(Global.GW_TYPE_STB.equals(gw_type))
		{
			G_ResourceBind = Global.G_BlManager_STB;
		}
	}

	/**
	 * 绑定
	 * 
	 * @param bindInfo
	 * @return
	 */
	public ResultInfo bind(BindInfo[] bindInfo)
	{
		logger.debug("ResourceBindCorba>bind({})", bindInfo);
		if (null == bindInfo || bindInfo.length == 0)
		{
			logger.error("bindInfo is null");
			return null;
		}
		ResultInfo resultInfo = null;
		try
		{
			resultInfo = G_ResourceBind.bind("ItmsService", 0, bindInfo);
			logger.warn("bind: {}-{}", new Object[] { resultInfo.status,
					resultInfo.resultId });
		}
		catch (Exception e)
		{
			logger.error("ResourceBind Error.\n{}, rebind!", e);
			InitDAO.initResourceBind(this.gw_type);
			init();
			try
			{
				resultInfo = G_ResourceBind.bind("ItmsService", 0, bindInfo);
			}
			catch (Exception ex)
			{
				logger.error("rebind ResourceBind Error.\n{}", ex);
			}
		}
		return resultInfo;
	}

	/**
	 * 解绑
	 * 
	 * @param bindInfo
	 * @return
	 */
	public ResultInfo release(UnBindInfo[] unBindInfo)
	{
		logger.debug("ResourceBindCorba>unBind({})", unBindInfo);
		if (null == unBindInfo || unBindInfo.length == 0)
		{
			logger.error("unBindInfo is null");
			return null;
		}
		ResultInfo resultInfo = null;
		try
		{
			resultInfo = G_ResourceBind.unBind("ItmsService", 0, unBindInfo);
			logger.warn("unbind: {}-{}", new Object[] { resultInfo.status,
					resultInfo.resultId });
		}
		catch (Exception e)
		{
			logger.error("ResourceBind Error.\n{}, rebind!", e);
			InitDAO.initResourceBind(this.gw_type);
			init();
			try
			{
				resultInfo = G_ResourceBind.unBind("ItmsService", 0, unBindInfo);
			}
			catch (Exception ex)
			{
				logger.error("rebind ResourceBind Error.\n{}", ex);
			}
		}
		return resultInfo;
	}
	
	/**
	 * 绑定
	 */
	public void DoBindSingl(String username,String deviceId,String accName,int userline)
	{
		BindInfo[] arr = new BindInfo[1];
		arr[0] = new BindInfo();
		arr[0].accOid = "0";
		arr[0].accName = "ItmsService";
		arr[0].username = username;
		arr[0].deviceId = deviceId;
		arr[0].userline = userline;
		
		this.DoBindSingl(arr);
	}
	
	/**
	 * 解绑
	 */
	public void DoUnBindSingl(String userid,String deviceId,String accName,int userline)
	{
		UnBindInfo[] arr = new UnBindInfo[1];
		arr[0] = new UnBindInfo();
		arr[0].accOid = "0";
		arr[0].accName = "ItmsService";
		arr[0].userId = userid;
		arr[0].deviceId = deviceId;
		arr[0].userline = userline;
		
		this.DoUnBindSingl(arr);
	}
	
	/**
	 * 绑定
	 */
	public void DoBindSingl(BindInfo[] bindInfo)
	{
		logger.debug("ResourceBindCorba.DoBindSingl");
		if (null == bindInfo || bindInfo.length == 0)
		{
			return ;
		}
		try
		{
			G_ResourceBind.bindSingle("ItmsService", 0, bindInfo);
		}
		catch (Exception e)
		{
			logger.debug("reconn ResourceBind");
			InitDAO.initResourceBind(this.gw_type);
			init();
			try {
				G_ResourceBind.bindSingle("ItmsService", 0, bindInfo);
			} catch (RuntimeException e1) {
				logger.error("CORBA BusinessLogic Error:{}", e1.getMessage());
			}
		}
	}
	
	/**
	 * 解绑
	 */
	public void DoUnBindSingl(UnBindInfo[] unBindInfo)
	{
		logger.debug("ResourceBindCorba.DoUnBind");
		if (null == unBindInfo || unBindInfo.length == 0)
		{
			return ;
		}
		try
		{
			G_ResourceBind.unBindSingl("ItmsService", 0, unBindInfo);
		}
		catch (Exception e)
		{
			logger.debug("reconn ResourceBind");
			InitDAO.initResourceBind(this.gw_type);
			init();
			try
			{
				G_ResourceBind.unBindSingl("ItmsService", 0, unBindInfo);
			}
			catch (Exception ex)
			{
				logger.error("reconn ResourceBind Error.",ex);
			}
		}
	}
	
	/**
	 * 解绑
	 * 
	 * @param bindInfo
	 * @return
	 */
	public ResultInfo release4JL(UnBindInfo[] unBindInfo, int serviceType){
		
		logger.debug("ResourceBindCorba>unBind({})", unBindInfo);
		
		String clientId = "ItmsService";
		if (4==serviceType){
			clientId = "StbService";
		}
		
		if (null == unBindInfo || unBindInfo.length == 0)
		{
			logger.error("unBindInfo is null");
			return null;
		}
		ResultInfo resultInfo = null;
		try
		{
			resultInfo = G_ResourceBind.unBind(clientId, 0, unBindInfo);
			logger.warn("unbind: {}-{}", new Object[] { resultInfo.status,
					resultInfo.resultId });
		}
		catch (Exception e)
		{
			logger.error("ResourceBind Error.\n{}, rebind!", e);
			InitDAO.initResourceBind(this.gw_type);
			init();
			try
			{
				resultInfo = G_ResourceBind.unBind(clientId, 0, unBindInfo);
			}
			catch (Exception ex)
			{
				logger.error("rebind ResourceBind Error.\n{}", ex);
			}
		}
		return resultInfo;
	}

    @Override
	public ResultInfo bind(BindInfo[] bindInfo, String clientId)
	{
		 
		logger.debug("ResourceBindCorba>bind({})", bindInfo);
		if (null == bindInfo || bindInfo.length == 0)
		{
			logger.error("bindInfo is null");
			return null;
		}
		ResultInfo resultInfo = null;
		try
		{
			resultInfo = G_ResourceBind.bind(clientId, 0, bindInfo);
			logger.warn("bind: {}-{}", new Object[] { resultInfo.status,
					resultInfo.resultId });
		}
		catch (Exception e)
		{
			logger.error("ResourceBind Error.\n{}, rebind!", e);
			InitDAO.initResourceBind(this.gw_type);
			init();
			try
			{
				resultInfo = G_ResourceBind.bind("ItmsService", 0, bindInfo);
			}
			catch (Exception ex)
			{
				logger.error("rebind ResourceBind Error.\n{}", ex);
			}
		}
		return resultInfo;
	}

	@Override
	public ResultInfo release(UnBindInfo[] unBindInfo, String clientId)
	{
		logger.debug("ResourceBindCorba>unBind({})", unBindInfo);
		if (null == unBindInfo || unBindInfo.length == 0)
		{
			logger.error("unBindInfo is null");
			return null;
		}
		ResultInfo resultInfo = null;
		try
		{
			resultInfo = G_ResourceBind.unBind(clientId, 0, unBindInfo);
			logger.warn("unbind: {}-{}", new Object[] { resultInfo.status,
					resultInfo.resultId });
		}
		catch (Exception e)
		{
			logger.error("ResourceBind Error.\n{}, rebind!", e);
			InitDAO.initResourceBind(this.gw_type);
			init();
			try
			{
				resultInfo = G_ResourceBind.unBind("ItmsService", 0, unBindInfo);
			}
			catch (Exception ex)
			{
				logger.error("rebind ResourceBind Error.\n{}", ex);
			}
		}
		return resultInfo;
	}

	@Override
	public ResultInfo updateUser(String user)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultInfo delDevice(String device_id)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultInfo updateDevice(String device_id)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultInfo deleteUser(String userName)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
