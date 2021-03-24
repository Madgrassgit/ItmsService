
package com.linkage.stbms.cao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ResourceBind.BindInfo;
import ResourceBind.ResultInfo;
import ResourceBind.UnBindInfo;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.dao.InitDAO;
import com.linkage.stbms.itv.main.Global;


/**
 * Connect with ResourceBind by corba.
 * 
 * @author zhangshimin(工号) Tel:??
 * @version 1.0
 * @since 2011-7-8 下午02:17:27
 * @category com.linkage.litms.eserver.corba
 * @copyright 南京联创科技 网管科技部
 */
public class ResourceBindCorba
{
	/** log */
	private static final Logger logger = LoggerFactory.getLogger(ResourceBindCorba.class);
	
	
	
	/**
	 * upd stb mem info
	 * @param deviceId
	 * @param userID
	 * @param status
	 * @return
	 */
	public String DoStbUpdate(String deviceId,String userID,int status)
	{
		logger.warn("ResourceBindClient.DoStbUpdate[{},{},{}]",new Object[]{deviceId,userID,status});
		if (StringUtil.IsEmpty(deviceId)||StringUtil.IsEmpty(userID))
		{
			return null;
		}
		String stbInfo = deviceId+"||"+userID +"||"+status;
		String rs = null;
		try
		{
			rs = Global.G_BlManager_STB.test("StbService", 0, "device", "set", stbInfo);
		}
		catch (Exception e)
		{
			logger.debug("reconn ResourceBind");
			InitDAO.initResourceBind();
			try
			{
				
				rs = Global.G_BlManager_STB.test("StbService", 0, "device", "set", stbInfo);
			}
			catch (Exception ex)
			{
				logger.error("userupdate[{}]:CORBA BusinessLogic Error:{}", new Object[]{stbInfo,ex.getMessage()});
			}
		}
		logger.warn("修改机顶盒信息【{}】,通知资源绑定模块：【{}】",new Object[]{stbInfo,rs});
		return rs;
	}
	
	
	
	
	public ResultInfo DoBind(String username,String deviceId,String accName,int userline)
	{
		BindInfo[] arr = new BindInfo[1];
		arr[0] = new BindInfo();
		arr[0].accOid = "0";
		arr[0].accName = "StbService";
		arr[0].username = username;
		arr[0].deviceId = deviceId;
		arr[0].userline = userline;
		ResultInfo rs = this.DoBind(arr);
		if(rs == null)
		{
			logger.warn("[{}]-[{}]调用资源绑定失败，返回ResultInfo为null",new Object[] {username,deviceId});
		}
		else
		{
			logger.warn("[{}]-[{}]绑定结果："+this.transferBindResult(rs.status, rs.resultId[0]),new Object[] {username,deviceId});
		}
		return rs;
	}
		
	public void DoBindSingl(String username,String deviceId,String accName,int userline)
	{
		BindInfo[] arr = new BindInfo[1];
		arr[0] = new BindInfo();
		arr[0].accOid = "0";
		arr[0].accName = "StbService";
		arr[0].username = username;
		arr[0].deviceId = deviceId;
		arr[0].userline = userline;
		
		this.DoBindSingl(arr);
	}
	
	public void DoBindSingl(BindInfo[] bindInfo)
	{
		logger.debug("ResourceBindCorba.DoBind");
		if (null == bindInfo || bindInfo.length == 0)
		{
			return ;
		}
		try
		{
			Global.G_BlManager_STB.bindSingle("StbService", 0, bindInfo);
		}
		catch (Exception e)
		{
			logger.debug("reconn ResourceBind");

			InitDAO.initResourceBind();
			try {
				Global.G_BlManager_STB.bindSingle("StbService", 0, bindInfo);
			} catch (RuntimeException e1) {
				logger.error("CORBA BusinessLogic Error:{}", e1.getMessage());
			}
		}
	}
	
	// 绑定
	public ResultInfo DoBind(BindInfo[] bindInfo)
	{
		logger.debug("ResourceBindCorba.DoBind");
		if (null == bindInfo || bindInfo.length == 0)
		{
			return null;
		}
		ResultInfo rs = null;
		try
		{
			rs = Global.G_BlManager_STB.bind("StbService", 0, bindInfo);
		}
		catch (Exception e)
		{
			logger.debug("reconn ResourceBind");

			InitDAO.initResourceBind();
			try {
				rs = Global.G_BlManager_STB.bind("StbService", 0, bindInfo);
			} catch (RuntimeException e1) {
				logger.error("CORBA BusinessLogic Error:{}", e1.getMessage());
			}
		}
		
		return rs;
	}
	
	// 绑定
	public ResultInfo DoBind(BindInfo[] bindInfo,String clientId)
	{
		logger.warn("start***ResourceBindCorba.DoBind**deviceId={};userAccount={}",new Object[]{bindInfo[0].deviceId,bindInfo[0].username});
		if (null == bindInfo || bindInfo.length == 0)
		{
			return null;
		}
		ResultInfo rs = null;
		try
		{
			rs = Global.G_BlManager_STB.bind(clientId, 0, bindInfo); 
		}
		catch (Exception e)
		{
			logger.debug("reconn ResourceBind");
			
			logger.error("绑定失败，失败原因：{[]}", e);

			InitDAO.initResourceBind();
			try {
				rs = Global.G_BlManager_STB.bind(clientId, 0, bindInfo);
			} catch (RuntimeException e1) {
				logger.error("CORBA BusinessLogic Error:{}", e1.getMessage());
			}
		}
		
		return rs;
	}
	/**
	 * 解绑
	 * @param bindInfo
	 * @return
	 */
	public ResultInfo release(UnBindInfo[] unBindInfo)
	{
		logger.debug("release({})", unBindInfo);
		ResultInfo resultInfo = null;
		try{
			resultInfo = Global.G_BlManager_STB.unBind("StbService", 0, unBindInfo);
			logger.warn("unbind: {}-{}",new Object[]{resultInfo.status,resultInfo.resultId});
		}
		catch(Exception e)
		{
			logger.error("ResourceBind Error.\n{}, rebind!", e);
			InitDAO.initResourceBind();
			try
			{
				resultInfo = Global.G_BlManager_STB.unBind("StbService", 0, unBindInfo);
			}
			catch(Exception ex)
			{
				logger.error("rebind ResourceBind Error.\n{}", ex);
			}
		}
		return resultInfo;
	}
	
	

	public String transferBindResult(String status,String resultId)
	{
		logger.debug("transferBindResult({},{})",new Object[] {status,resultId});
		String resultMsg = "";
		if(status.equals("1"))
		{
			if(resultId.equals("1"))
			{
				resultMsg = "绑定成功";
			}
			else if(resultId.equals("-1"))
			{
				resultMsg = "参数错误";
			}
			else if(resultId.equals("-2"))
			{
				resultMsg = "终端类型不匹配";
			}
			else if(resultId.equals("-3"))
			{
				resultMsg = "用户不存在";
			}
			else if(resultId.equals("-4"))
			{
				resultMsg = "设备不存在";
			}
			else if(resultId.equals("-5"))
			{
				resultMsg = "不存在绑定关系";
			}
			else if(resultId.equals("-6"))
			{
				resultMsg = "数据库操作失败";
			}
			else
			{
				resultMsg = "未知错误";
			}
		}
		else if(status.equals("0"))
		{
			resultMsg = "非法客户端";
		}	
		else if(status.equals("-1"))
		{
			resultMsg = "参数错误";
		}
		else
		{
			resultMsg = "未知错误";
		}
		return resultMsg;
	}
}
