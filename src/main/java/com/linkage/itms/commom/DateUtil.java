
package com.linkage.itms.commom;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-12-14
 * @category org.enyes.useful
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DateUtil
{

	public static final long MILLIS_PER_SECOND = 1000;
	private static final Log log = LogFactory.getLog(DateUtil.class);

	public static long currentTimeInSecond()
	{
		return System.currentTimeMillis() / MILLIS_PER_SECOND;
	}

	/**
	 * 根据传入的DateFormat参数，返回当前时间的format字符串时间
	 * 
	 * @param format
	 *            字符串参数,never null，eg. yyyy-MM-dd HH:mm:ss
	 * @return 返回当前时间的format字符串时间格式
	 */
	public static String getNowTime(String format)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date());
	}

	/**
	 * <pre>
	 * 将入参以秒为单位的时间<code>timeInSecond</code>按照时间格式<code>timeFormat</code>转换为时间字符串。
	 * 如果按时间格式转化失败，该方法都返回null
	 * </pre>
	 * 
	 * @param timeInSecond
	 *            待转化的时间,单位为秒
	 * @param timeFormat
	 *            时间转为格式，never null。如果按该格式转化失败，返回null
	 * @return 返回将时间按格式转化成字符串，如果按时间格式转化失败，都返回null
	 */
	public static String transTime(long timeInSecond, String timeFormat)
	{
		try
		{
			Date date = new Date(timeInSecond * MILLIS_PER_SECOND);
			SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
			return sdf.format(date);
		}
		catch (Exception e)
		{
			log.error("format timeInSecond[" + timeInSecond + "] by time format["
					+ timeFormat + "] error.");
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 将特定时间格式<code>timeFormat</code>的时间字符串转<code>time</code>化为秒单位的时间
	 * 
	 * @param time
	 *            时间字符串，如果为空，返回0
	 * @param timeFormat
	 *            时间字符串格式
	 * @return 如果时间字符串按指定格式转化成功，返回特定的秒单位时间，否则返回0
	 */
	public static long transTime(String time, String timeFormat)
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
			Date date = sdf.parse(time);
			return date.getTime() / MILLIS_PER_SECOND;
		}
		catch (Exception e)
		{
			log.error("format time[" + time + "] by time format[" + timeFormat
					+ "] error.");
			log.error(e.getMessage(), e);
			return 0;
		}
	}
}
