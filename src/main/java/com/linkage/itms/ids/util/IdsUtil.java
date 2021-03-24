
package com.linkage.itms.ids.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.linkage.commons.util.StringUtil;

/**
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-10-18
 * @category com.linkage.itms.ids.util
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class IdsUtil
{

	/**
	 * 设备状态信息上报功能开启
	 */
	public static final String DIAGNOSTIC_ENABLE_YES = "1";
	/**
	 * 设备状态信息上报功能关闭
	 */
	public static final String DIAGNOSTIC_ENABLE_NO = "0";
	/**
	 * 设备不存在
	 */
	public static final String DEVICE_STATUS_NOT_EXIST = "1";
	/**
	 * 设备状态信息等待下发
	 */
	public static final String DEVICE_STATUS_SENDING = "2";
	/**
	 * serviceId
	 */
	public static final String DEFAULT_SERVICE_ID = "8001";
	
	public static final String REPORT_PEROID_SERVICE_ID = "8005";
	/**
	 * 该接口开放的客户端
	 */
	public static final List<String> VALID_CLIENT_TYPES = Arrays.asList("1", "2", "3",
			"4", "5");
	/**
	 * 客户的设备状态上报参数
	 */
	public static final List<String> VALID_DIAGNOSTIC_PARAMS = Arrays.asList("1", "2",
			"3", "4", "5", "6", "7","8");

	/**
	 * IDS接口合法客户端
	 * 
	 * @param clientType
	 *            客户端编号
	 * @return 如果合法返回true，否则返回false
	 */
	public static boolean validClientType(String clientType)
	{
		if (StringUtil.IsEmpty(clientType))
		{
			return false;
		}
		return VALID_CLIENT_TYPES.contains(clientType);
	}

	/**
	 * 开启关闭参数是否合法
	 * 
	 * @param enable
	 *            开始关闭参数
	 * @return 如果参数合法返回true，否则返回false
	 */
	public static boolean validEnable(String enable)
	{
		return DIAGNOSTIC_ENABLE_YES.equals(enable)
				|| DIAGNOSTIC_ENABLE_NO.equals(enable);
	}

	/**
	 * 判断设备状态参数是否合法
	 * 
	 * @param param
	 *            状态参数
	 * @return 如果合法返回true，否则返回false
	 */
	public static boolean validParam(String param)
	{
		if (StringUtil.IsEmpty(param))
		{
			return false;
		}
		String[] params = param.split(",");
//		if (params.length > VALID_DIAGNOSTIC_PARAMS.size())
//		{
//			return false;
//		}
		for (String pm : params)
		{
			if (!VALID_DIAGNOSTIC_PARAMS.contains(pm.trim()))
			{
				return false;
			}
		}
		return true;
	}
public static void main(String[] args) {
	System.out.println(validParam("1, 2, 3, 4, 5"));
}
	public static boolean validNumber(String input)
	{
		if (StringUtil.IsEmpty(input))
		{
			return false;
		}
		return Pattern.matches("[0-9]+", input);
	}
}
