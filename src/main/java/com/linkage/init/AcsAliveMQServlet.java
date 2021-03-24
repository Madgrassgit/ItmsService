
package com.linkage.init;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ailk.tr069.acsalive.thread.AcsAliveMessageDealThread;
import com.linkage.itms.Global;

public class AcsAliveMQServlet extends HttpServlet
{

	private static final long serialVersionUID = 1L;

	public void init() throws ServletException
	{
		super.init();
		
		startAcsAliveThread("itms");
		startAcsAliveThread("bbms");
		startAcsAliveThread("stb");
	}
	
	
	public void startAcsAliveThread(String systemType)
	{
		String systemKey = null;
		if ("itms".equals(systemType))
		{
			systemKey = Global.SYSTEM_ITMS_PREFIX + Global.SYSTEM_ACS;
		}
		else if("bbms".equals(systemType))
		{
			systemKey = Global.SYSTEM_BBMS_PREFIX + Global.SYSTEM_ACS;
		}
		else if("stb".equals(systemType))
		{
			systemKey = Global.SYSTEM_STB_PREFIX + Global.SYSTEM_ACS;
		}
		
		try
		{
			AcsAliveMessageDealThread thread = AcsAliveMessageDealThread.getInstance(systemKey,systemType, Global.G_ServerHome + File.separator+ "conf" + File.separator + "MQPool.xml");
			if(null != thread)
			{
				thread.start();
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		doGet(request, response);
	}
}
