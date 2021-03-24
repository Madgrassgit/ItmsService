
package com.linkage.itms.cao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import PreProcess.OneToMany;
import PreProcess.PPManager;
import PreProcess.UserInfo;
import StbCm.CMManager;

import com.linkage.itms.Global;
import com.linkage.itms.PreProcessInterface;
import com.linkage.itms.dao.InitDAO;

/**
 * CORBA operation for PreProcess by type
 * 
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-6-21
 * @category com.linkage.itms.cao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class PreProcessCorbaByType implements PreProcessInterface
{

	/** log */
	private static final Logger logger = LoggerFactory
			.getLogger(PreProcessCorbaByType.class);
	private PPManager G_PreProcess = null;
	private CMManager G_Stb_PreProcess = null;
	private String type;

	public PreProcessCorbaByType(String type)
	{
		this.type = type;
		init();
	}

	private void init()
	{
		if (Global.GW_TYPE_ITMS.equals(type))
		{
			G_PreProcess = Global.G_PPManager_ITMS;
		}
		else if (Global.GW_TYPE_BBMS.equals(type))
		{
			G_PreProcess = Global.G_PPManager_BBMS;
		}
		else if (Global.GW_TYPE_STB.equals(type))
		{
			G_Stb_PreProcess = Global.G_PPManager_STB;
		}
	}

	/**
	 * 绑定设备后通知PP.
	 * 
	 * @param userInfoArr
	 * @return <li>1:成功</li> <li>-1:参数为空</li> <li>-2:绑定失败</li>
	 */
	public int processServiceInterface(UserInfo userInfo)
	{
		logger.error("processServiceInterface(userInfo:{})", userInfo);
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
		if (userInfoArr == null)
		{
			logger.error("userInfoArr == null");
			return -1;
		}
		try
		{
			if (Global.GW_TYPE_STB.equals(this.type))
			{
				G_Stb_PreProcess.processServiceInterface(transXml(userInfoArr));
			}
			else
			{
				G_PreProcess.processServiceInterface(userInfoArr);
			}
		}
		catch (Exception e)
		{
			logger.error("CORBA PreProcess By Type Error:{},Rebind.", e.getMessage());
			InitDAO.initPreProcess(this.type);
			init();
			try
			{
				if (Global.GW_TYPE_STB.equals(this.type))
				{
					G_Stb_PreProcess.processServiceInterface(transXml(userInfoArr));
				}
				else
				{
					G_PreProcess.processServiceInterface(userInfoArr);
				}
			}
			catch (RuntimeException e1)
			{
				logger.error("rebind PreProcess Error.\n{}", e1);
				return -2;
			}
		}
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

	@Override
	public boolean processOOBatch(String[] idArr)
	{
		return false;
	}

	@Override
	public boolean processOOBatch(String id)
	{
		return false;
	}

	@Override
	public boolean processOMBatch4DefaultService(OneToMany[] objArr)
	{
		return false;
	}

	@Override
	public boolean processOMBatch4DefaultService(OneToMany obj)
	{
		return false;
	}

	@Override
	public UserInfo GetPPBindUserList(PreServInfoOBJ preInfoObj)
	{
		return null;
	}

	@Override
	public boolean processDeviceStrategy(String[] deviceIds, String serviceId,
			String[] paramArr)
	{
		return false;
	}

	@Override
	public boolean processSTBServiceInterface(String xmlstr)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
