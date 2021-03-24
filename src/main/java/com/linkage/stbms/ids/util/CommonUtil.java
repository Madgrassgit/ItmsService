package com.linkage.stbms.ids.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commom.util.StaticTypeCommon;
import com.linkage.stbms.itv.main.Global;


/**
 * 
 * @author zhangshimin(工号) Tel:??
 * @version 1.0
 * @since 2011-6-3 上午11:33:34
 * @category com.linkage.stbms.ids.util
 * @copyright 南京联创科技 网管科技部
 *
 */
public class CommonUtil
{
	private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	
	/**
	 * 是否合法的IP地址
	 * @param ip
	 * @return
	 */
	public static boolean checkIP(String ip)
	{
		if(ip!=null && !Global.NO_IP.equals(ip.trim())
					&& !"".equals(ip.trim()))
		{
			return true;
		}
		
		return false;
	}
	/**
	 * 填充IP 
	 */
	public static String getFillIP(String ip)
	{
		String fillIP = ip;
		String[] ipArray = new String[4];
		ipArray = ip.split("\\.");
		for (int i = 0; i < 4; i++)
		{
			if (ipArray[i].length() == 1)
			{
				ipArray[i] = "00" + ipArray[i];
			}
			else if (ipArray[i].length() == 2)
			{
				ipArray[i] = "0" + ipArray[i];
			}
		}
		fillIP = ipArray[0] +"."+ ipArray[1] +"."+ ipArray[2] +"."+ ipArray[3];

		return fillIP;
	}
	public static String addPrefix(String tabName)
	{
		if(Global.G_Sysytem_Type==StaticTypeCommon.GTMS_Sysytem_Type)
		{
			return "stb_"+tabName;
		}
		else
		{
			return tabName;
		}
	}
	public static String getPrefix4IOR()
	{
		if(Global.G_Sysytem_Type==StaticTypeCommon.GTMS_Sysytem_Type)
		{
			return "STB_";
		}
		else
		{
			return "";
		}
	}
	public static void main(String[] args)
	{
		System.out.println("Global.G_Sysytem_Type=="+StaticTypeCommon.GTMS_Sysytem_Type);
	}
}
