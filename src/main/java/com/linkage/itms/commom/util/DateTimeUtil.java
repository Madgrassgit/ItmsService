package com.linkage.itms.commom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 实现对时间相关格式转换
 * 
 * @author suzr
 * @version 1.00, 1/9/2006
 * @since Liposs 2.1
 */
public class DateTimeUtil
{ // 服务器的时间
	private static final Logger m_logger = LoggerFactory.getLogger(DateTimeUtil.class);
	private Calendar calendar = null;
	/**
	 * 构造函数，初始化时间发生器（无参数：当前时间）
	 */
	public DateTimeUtil()
	{
		calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
	}
	/**
	 * 构造函数：初始化时间发生器（带参数: 长整型时间数据：毫秒）
	 */
	public DateTimeUtil(long serverDateTime)
	{
		calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		calendar.setTimeInMillis(serverDateTime); // 指定时间
	}
	/**
	 * 构造函数：初始化时间发生器（带参数: 长整型时间数据）
	 */
	public DateTimeUtil(Date DateTime)
	{
		calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		calendar.setTimeInMillis(DateTime.getTime()); // 指定时间
	}
	/**
	 * 构造函数：初始化时间发生器（带参数:Calendar类实例）
	 */
	public DateTimeUtil(Calendar cal)
	{
		//calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		calendar = cal;
	}
	/**
	 * 构造函数：初始化时间发生器（带参数:DateTime） 参数格式：yyyy-MM-dd HH:mm:ss or yyyy-MM-dd
	 */
	public DateTimeUtil(String DateTime)
	{
		calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		SimpleDateFormat s = null;
		Date date = null;
		try
		{
			if (DateTime.length() > 12)
			{
				s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			}
			else
			{
				s = new SimpleDateFormat("yyyy-MM-dd");
			}
			date = new Date();
			try
			{
				date = s.parse(DateTime);
			}
			catch (ParseException e)
			{
				m_logger.error("e:" + e.getMessage());
			}
		}
		catch (Exception ee)
		{
			m_logger.error("ee:" + ee.getMessage());
		}
		calendar.setTime(date);
	}
	/**
	 * 构造函数：初始化时间发生器（带参数:DateTime） 参数格式：yyyy-MM-dd HH:mm:ss or yyyy-MM-dd
	 */
	public DateTimeUtil(String DateTime, String DateFormat)
	{
		calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		SimpleDateFormat s = null;
		Date date = null;
		try
		{
			s = new SimpleDateFormat(DateFormat);
			date = new Date();
			try
			{
				date = s.parse(DateTime);
			}
			catch (ParseException e)
			{
				m_logger.error("e:" + e.getMessage());
			}
		}
		catch (Exception ee)
		{
			m_logger.error("ee:" + ee.getMessage());
		}
		calendar.setTime(date);
	}
	/**
	 * 获取指定日期所在月的第一天的日期
	 * 
	 * @return
	 */
	public String getFirstDayOfMonth()
	{
		calendar.set(this.getYear(), this.getMonth() - 1, 1);
		return this.getDate();
	}
	/**
	 * 获取指定日期所在月的第一天的日期
	 * @author cczhong
	 * @return
	 */
	public String getFirstDayOfMonth(String date)
	{
		Calendar calendarTemp=this.calendarCreate(date);
		calendarTemp.set(calendarTemp.get(Calendar.YEAR),calendarTemp.get(Calendar.MONTH), 1);
		return this.getDate(calendarTemp);
	}
	/**
	 * 获取指定日期上个月的第一天的日期
	 * yyyy-mm-dd
	 * 
	 * @return
	 */
	public String getFirstDayOfLastMonth()
	{
		calendar.set(this.getYear(), this.getMonth() - 2, 1);
		return this.getDate();
	}
	/**
	 * 获取指定日期上个月的第一天的日期
	 * yyyy-mm-dd
	 * @author cczhong
	 * @return
	 */
	public String getFirstDayOfLastMonth(String date)
	{
		Calendar calendarTemp=this.calendarCreate(date);
		calendarTemp.set(calendarTemp.get(Calendar.YEAR),calendarTemp.get(Calendar.MONTH)-1, 1);
		return this.getDate(calendarTemp);
	}
	/**
	 * 获取指定日期上上个月的第一天的日期
	 * yyyy-mm-dd
	 * @author cczhong
	 * @return
	 */
	public String getFirstDayOfLast2Month(String date)
	{
		Calendar calendarTemp=this.calendarCreate(date);
		calendarTemp.set(calendarTemp.get(Calendar.YEAR),calendarTemp.get(Calendar.MONTH)-2, 1);
		return this.getDate(calendarTemp);
	}
	/**
	        * @author cczhong
	        * @description 获取指定日期下个月的第一天的日期
			* @param date
			* @return
	 */
	public String getFirstDayOfNextMonth(String date){
		Calendar calendarTemp=this.calendarCreate(date);
		calendarTemp.set(calendarTemp.get(Calendar.YEAR),calendarTemp.get(Calendar.MONTH)+1, 1);
		return this.getDate(calendarTemp);
	}

	/**
	        * @author cczhong
	        * @decription 用yyyy-mm-dd创建一个calendar
			* @param DateTime
			* @return
	 */
	public Calendar calendarCreate(String DateTime){
		Calendar calendarTemp = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		SimpleDateFormat s = null;
		Date date = null;
		try
		{
			if (DateTime.length() > 12)
			{
				s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			}
			else
			{
				s = new SimpleDateFormat("yyyy-MM-dd");
			}
			date = new Date();
			try
			{
				date = s.parse(DateTime);
			}
			catch (ParseException e)
			{
				m_logger.error("e:" + e.getMessage());
			}
		}
		catch (Exception ee)
		{
			m_logger.error("ee:" + ee.getMessage());
		}
		calendarTemp.setTime(date);
		return calendarTemp;
	}
	/**
	        * @author cczhong
	        * @description 用秒创立一个Calendar
			* @param time
	 */
	public Calendar calendarCreate(long time)
	{
		Calendar calendarTemp = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		calendarTemp.setTimeInMillis(time*1000); // 指定时间
		return calendarTemp;
	}
	
	/**
	 * 取日期-时间 yyyy-mm-dd HH:mm:ss
	 * 
	 * @return String
	 */
	public String getLongDate()
	{
		return this.getDate() + " " + this.getTime();
	}
	/**
	 * 取日期-时间 yyyy-mm-dd HH:mm:ss
	 * @author cczhong
	 * @return String
	 * @param time为秒
	 */
	public String getLongDate(long time){
		Calendar calendarTemp= Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		calendarTemp.setTimeInMillis(time*1000); // 指定时间
		return this.getDate(calendarTemp)+" "+this.getTime(calendarTemp);
		
	}
	/**
	 * 取日期-时间 yyyymmddHHmmss
	 * 
	 * @return String
	 */
	public String getLongDateChar()
	{
		return this.getShortDate() + this.getShortTime();
	}
	/**
	 * 取时间型日期
	 * 
	 * @return Date
	 */
	public Date getDateTime()
	{
		return calendar.getTime();
	}
	/**
	 * 取日期 yyyy-mm-dd
	 * 
	 * @return String
	 */
	public String getDate()
	{
		String months , days , years;
		years = String.valueOf(this.getYear());
		if (this.getMonth() < 10)
		{
			months = "0" + this.getMonth();
		}
		else
		{
			months = String.valueOf(this.getMonth());
		}
		if (this.getDay() < 10)
		{
			days = "0" + this.getDay();
		}
		else
		{
			days = String.valueOf(this.getDay());
		}
		return years + "-" + months + "-" + days;
	}
	/**
	 * 取日期 yyyy-mm-dd
	 * @author cczhong
	 * @return String
	 */
	public String getDate(Calendar c)
	{
		String months , days , years;
		years = String.valueOf(c.get(Calendar.YEAR));
		if (c.get(Calendar.MONTH)+1 < 10)
		{
			months = "0" + (c.get(Calendar.MONTH)+1);
		}
		else
		{
			months = String.valueOf(c.get(Calendar.MONTH)+1);
		}
		if (c.get(Calendar.DAY_OF_MONTH)< 10)
		{
			days = "0" + c.get(Calendar.DAY_OF_MONTH);
		}
		else
		{
			days = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		}
		return years + "-" + months + "-" + days;
	}
	/**
	 * 取日期 yyyy-m-d add by benyp
	 * 
	 * @return String
	 */
	public String getDate2()
	{
		String months , days , years;
		years = String.valueOf(this.getYear());
		months = String.valueOf(this.getMonth());
		days = String.valueOf(this.getDay());
		return years + "-" + months + "-" + days;
	}
	/**
	 * 取日期 yyyymmdd
	 * 
	 * @return String
	 */
	public String getShortDate()
	{
		String months , days , years;
		years = String.valueOf(this.getYear());
		if (this.getMonth() < 10)
		{
			months = "0" + this.getMonth();
		}
		else
		{
			months = String.valueOf(this.getMonth());
		}
		if (this.getDay() < 10)
		{
			days = "0" + this.getDay();
		}
		else
		{
			days = String.valueOf(this.getDay());
		}
		return years + "" + months + "" + days;
	}
	/**
	 * @description 获取特定日期的前一天0点的从1970-01-01开始的秒
	 * @author cczhong
	 * @return long
	 */
    public long getLastDayStart(String date){
    	//以格林尼治时间为标准还要减掉东八区的8小时
    	return (this.nDaysBetweenTwoDate("1970-01-01", date)-1)*24*3600-8*3600;
	}
    /**
     * @description 获取特定日期的前一天24点(当天0点)的从1970-01-01开始的秒
	 * @author cczhong
	 * @return long
     */
    public long getLastDayEnd(String date){
    	//以格林尼治时间为标准还要减掉东八区的8小时
    	return this.nDaysBetweenTwoDate("1970-01-01", date)*24*3600-8*3600;
    	
    }
    /**
	 * 获取指定日期的前一天
	 * yyyy-mm-dd
	 */
	public String getBeforDay()
	{
		calendar.set(Calendar.DATE, this.getDay()-1);
		return this.getDate();
	}
	/**
	 * 取时间 hh:mm:ss
	 * 
	 * @return String
	 */
	public String getTime()
	{
		String hours , minutes , seconds;
		if (this.getHour() < 10)
		{
			hours = "0" + this.getHour();
		}
		else
		{
			hours = String.valueOf(this.getHour());
		}
		if (this.getMinute() < 10)
		{
			minutes = "0" + this.getMinute();
		}
		else
		{
			minutes = String.valueOf(this.getMinute());
		}
		if (this.getSecond() < 10)
		{
			seconds = "0" + this.getSecond();
		}
		else
		{
			seconds = String.valueOf(this.getSecond());
		}
		return hours + ":" + minutes + ":" + seconds;
	}
	/**
	 * 取时间 hh:mm:ss
	 * @author cczhong
	 * @return String
	 */
	public String getTime(Calendar c)
	{
		String hours , minutes , seconds;
		if (c.get(Calendar.HOUR_OF_DAY) < 10)
		{
			hours = "0" + c.get(Calendar.HOUR_OF_DAY);
		}
		else
		{
			hours = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		}
		if (c.get(Calendar.MINUTE) < 10)
		{
			minutes = "0" + c.get(Calendar.MINUTE);
		}
		else
		{
			minutes = String.valueOf(c.get(Calendar.MINUTE));
		}
		if (c.get(Calendar.SECOND) < 10)
		{
			seconds = "0" + c.get(Calendar.SECOND);
		}
		else
		{
			seconds = String.valueOf(c.get(Calendar.SECOND));
		}
		return hours + ":" + minutes + ":" + seconds;
	}
	/**
	 * 取时间 hhmmss
	 * 
	 * @return String
	 */
	public String getShortTime()
	{
		String hours , minutes , seconds;
		if (this.getHour() < 10)
		{
			hours = "0" + this.getHour();
		}
		else
		{
			hours = String.valueOf(this.getHour());
		}
		if (this.getMinute() < 10)
		{
			minutes = "0" + this.getMinute();
		}
		else
		{
			minutes = String.valueOf(this.getMinute());
		}
		if (this.getSecond() < 10)
		{
			seconds = "0" + this.getSecond();
		}
		else
		{
			seconds = String.valueOf(this.getSecond());
		}
		return hours + minutes + seconds;
	}
	/**
	 * 取年 yyyy
	 * 
	 * @return int
	 */
	public int getYear()
	{
		return calendar.get(Calendar.YEAR);
	}
	/**
	 * 取月 mm
	 * 
	 * @return int
	 */
	public int getMonth()
	{
		return calendar.get(Calendar.MONTH) + 1;
	}
	/**
	 * 取日 dd
	 * 
	 * @return int
	 */
	public int getDay()
	{
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 取日 dd,根据所给日期秒数
	 * @author cczhong
	 * @return int
	 */
	public int getDay(long time)
	{
		return this.calendarCreate(time).get(Calendar.DAY_OF_MONTH);
	}
	/**
	 * 取小时 hh
	 * 
	 * @return int
	 */
	public int getHour()
	{
		return calendar.get(Calendar.HOUR_OF_DAY);
	}
	/**
	 * 取分钟 mm
	 * 
	 * @return int
	 */
	public int getMinute()
	{
		return calendar.get(Calendar.MINUTE);
	}
	/**
	 * 取秒钟 ss
	 * 
	 * @return int
	 */
	public int getSecond()
	{
		return calendar.get(Calendar.SECOND);
	}
	/**
	 * 取从1970年1月1日以来的时间（秒）
	 * 
	 * @return long
	 */
	public long getLongTime()
	{
		return calendar.getTimeInMillis() / 1000;
	}
	/**
	 * 取从1970年1月1日以来,当前时间后一天的时间（秒）
	 * 
	 * @return long
	 */
	public long getNextLongTime()
	{
		Calendar calendar_temp = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		calendar_temp.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
		calendar_temp.add(Calendar.DAY_OF_MONTH, 1);
		return calendar_temp.getTimeInMillis() / 1000;
	}
	/**
	 * 取从当前日期的偏移日期:年份
	 * 
	 * @param years
	 * @return int
	 */
	public int getNextYear(int years)
	{
		calendar.add(Calendar.YEAR, years);
		return calendar.get(Calendar.YEAR);
	}
	/**
	 * 取从当前日期的偏移日期:月份
	 * 
	 * @param months
	 * @return int
	 */
	public int getNextMonth(int months)
	{
		calendar.add(Calendar.MONTH, months);
		return calendar.get(Calendar.MONTH) + 1;
	}
	/**
	 * 取从当前日期的偏移日期:日期
	 * 
	 * @param dates
	 * @return int Calendar.DAY_OF_MONTH
	 */
	public int getNextDate(int dates)
	{
		calendar.add(Calendar.DATE, dates);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	/**
	 * 取从当前日期偏移到的日期:具体时间值字符串
	 * 
	 * @param dateType
	 * @param timeNumber
	 * @return getLongDate()
	 */
	public String getNextDateTime(String dateType, int timeNumber)
	{
		if (dateType.equals("year"))
		{
			calendar.add(Calendar.YEAR, timeNumber);
		}
		else if (dateType.equals("month"))
		{
			calendar.add(Calendar.MONTH, timeNumber);
		}
		else if (dateType.equals("day"))
		{
			calendar.add(Calendar.DATE, timeNumber);
		}
		else if (dateType.equals("hour"))
		{
			calendar.add(Calendar.HOUR, timeNumber);
		}
		else if (dateType.equals("minute"))
		{
			calendar.add(Calendar.MINUTE, timeNumber);
		}
		else if (dateType.equals("second"))
		{
			calendar.add(Calendar.SECOND, timeNumber);
		}
		return this.getLongDate();
	}
	/**
	 * 取从当前日期偏移到的日期:具体时间值字符串
	 * 
	 * @param dateType
	 * @param timeNumber
	 * @return getDate
	 */
	public String getNextDate(String dateType, int timeNumber)
	{
		if (dateType.equals("year"))
		{
			calendar.add(Calendar.YEAR, timeNumber);
		}
		else if (dateType.equals("month"))
		{
			calendar.add(Calendar.MONTH, timeNumber);
		}
		else if (dateType.equals("day"))
		{
			calendar.add(Calendar.DATE, timeNumber);
		}
		else if (dateType.equals("hour"))
		{
			calendar.add(Calendar.HOUR, timeNumber);
		}
		else if (dateType.equals("minute"))
		{
			calendar.add(Calendar.MINUTE, timeNumber);
		}
		else if (dateType.equals("second"))
		{
			calendar.add(Calendar.SECOND, timeNumber);
		}
		return this.getDate();
	}
	/**
	 * 获得特定日期是当年的第几天
	 * 
	 * @return int
	 */
	public int getNoDayOfYear()
	{
		return calendar.get(Calendar.DAY_OF_YEAR);
	}
	/**
	 * 计算两个日期相隔的天数
	 * 
	 * @param firstDate
	 * @param secondDate
	 * @return int
	 */
	public static int nDaysBetweenTwoDate(Date firstDate, Date secondDate)
	{
		return (int) ((secondDate.getTime() - firstDate.getTime()) / (24 * 60 * 60 * 1000));
	}
	/**
	 * 计算两个日期相隔的天数
	 * 
	 * @param firstString
	 * @param secondString
	 * @return int
	 */
	public static int nDaysBetweenTwoDate(String firstString, String secondString)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date firstDate = null;
		Date secondDate = null;
		try
		{
			firstDate = df.parse(firstString);
			secondDate = df.parse(secondString);
		}
		catch (Exception e)
		{
			// 日期型字符串格式错误
		}
		if(secondDate == null || firstDate == null){
			return 0;
		}
		return (int) ((secondDate.getTime() - firstDate.getTime()) / (24 * 60 * 60 * 1000));
	}
	/**
	 * 计算两个日期相隔的日期数
	 * 
	 * @param firstString
	 * @param secondString
	 * @return int
	 */
	public static int nWeeksBetweenTwoDate(String firstString, String secondString)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date firstDate = null;
		Date secondDate = null;
		try
		{
			firstDate = df.parse(firstString);
			secondDate = df.parse(secondString);
		}
		catch (Exception e)
		{
			// 日期型字符串格式错误
		}
		if(secondDate == null || firstDate == null){
			return 0;
		}
		return (int) ((secondDate.getTime() - firstDate.getTime()) / (7 * 24 * 60 * 60 * 1000));
	}
	/**
	 * 返回特定日期处于一年中的第几周
	 * 
	 * @return int
	 */
	public int getNoWeekOfYear()
	{
		calendar.add(Calendar.DATE, -1);
		int weeks = calendar.get(Calendar.WEEK_OF_YEAR);
		calendar.add(Calendar.DATE, 1);
		return weeks;
	}
	/**
	 * 返回特定日期处于一年中的第几周 周的定义：星期日到星期六
	 * 
	 * @return
	 */
	public int getWeekOfYear()
	{
		int weeks = calendar.get(Calendar.WEEK_OF_YEAR);
		// modify by xiaoxf 2008-1-1 求2007-12-31 会算成08年的第一周 故加以下循环
		while (weeks < 3 && this.getMonth() == 12)
		{
			this.getNextDate(-1);
			weeks = calendar.get(Calendar.WEEK_OF_YEAR) + 1;
		}
		return weeks;
	}
	/**
	 * 返回特定日期处于一周中的第几天<br>
	 * 中国周一为一周第一天：CN<br>
	 * 国外周日为一周第一天：US
	 * 
	 * @return int
	 */
	public int getNoDayOfWeek(String country)
	{
		return calendar.get(Calendar.DAY_OF_WEEK)
				- Integer.parseInt(country.equals("CN") ? "1" : "0");
	}
	/**
	 * 返回特定日期处于一周中的第几天<br>
	 * 中国周一为一周第一天：CN<br>
	 * 国外周日为一周第一天：US
	 * @author cczhong
	 * @return int
	 */
	public int getNoDayOfWeek(String country,String date)
	{
		int day=this.calendarCreate(date).get(Calendar.DAY_OF_WEEK);
		int result;
		if("CN".equals(country)){
			if(day==1){
				result=7;
			}else{
				result=day-1;
			}
		}else{
			result=day;
		}
		return result;
	}
	/**
	 * 东方是周一为一周的第一天：CN<br>
	 * 西方是周日为一周的第一天：US<br>
	 * 返回特定日期所处上周的周一所处的日期
	 * @author cczhong
	 * @return String
	 */
	public String getFirstDayOfLastWeek(String country,String date)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date dateA = new Date();
		try
		{
			 dateA=formatter.parse(date);
		}
		catch (ParseException e)
		{
					e.printStackTrace();
				
		}
		// 西方是周日为一周的第一天
		// 东方是周一为一周的第一天
		Date dateBegin = new Date();
		dateBegin.setTime(dateA.getTime()
				- (long) (getNoDayOfWeek(country,date) - 1) * 24 * 60 * 60 * 1000-7*24*60*60*1000);
		return formatter.format(dateBegin);
	}
	/**
	 * 东方是周一为一周的第一天：CN<br>
	 * 西方是周日为一周的第一天：US<br>
	 * 返回特定日期所处这一周的周一所处的日期
	 * @author cczhong
	 * @return String
	 */
	public String getFirstDayOfWeek(String country,String date)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date dateA = new Date();
		try
		{
			 dateA=formatter.parse(date);
		}
		catch (ParseException e)
		{
					e.printStackTrace();
				
		}
		// 西方是周日为一周的第一天
		// 东方是周一为一周的第一天
		Date dateBegin = new Date();
		dateBegin.setTime(dateA.getTime()
				- (long) (getNoDayOfWeek(country,date) - 1) * 24 * 60 * 60 * 1000);
		return formatter.format(dateBegin);
	}
	/**
	 * 东方是周一为一周的第一天：CN<br>
	 * 西方是周日为一周的第一天：US<br>
	 * 返回特定日期所处这一周的周一所处的日期
	 * 
	 * @return String
	 */
	public String getFirstDayOfWeek(String country)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		// 西方是周日为一周的第一天
		// 东方是周一为一周的第一天
		Date dateBegin = new Date();
		dateBegin.setTime(calendar.getTimeInMillis()
				- (long) (getNoDayOfWeek(country) - 1) * 24 * 60 * 60 * 1000);
		return formatter.format(dateBegin);
	}
	/**
	 * 东方是周一为一周的第一天：CN<br>
	 * 西方是周日为一周的第一天：US<br>
	 * 返回特定日期所处这一周的周末所处的日期
	 * 
	 * @return String
	 */
	public String getLastDayOfWeek(String country)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		// 西方是周日为一周的第一天
		// 东方是周一为一周的第一天
		Date dateEnd = new Date();
		dateEnd.setTime(calendar.getTimeInMillis() + (long) (7 - getNoDayOfWeek(country))
				* 24 * 60 * 60 * 1000);
		return formatter.format(dateEnd);
	}
	/**
	 * 获得特定日期是当年的第几天 返回：int
	 */
	public int getNOdays()
	{
		return calendar.get(Calendar.DAY_OF_YEAR);
	}
	/**
	 * 获得指定日期所在月的最后一天的日期
	 * 
	 * @return String
	 */
	public String getLastDayOfMonth()
	{
		// 移动到下一个月的第一天然后再后退一天
		// this.getNextMonth(1);
		calendar.set(this.getYear(), this.getMonth(), 1);
		this.getNextDate(-1);
		// Date dateEnd = new
		// Date(calendar.get(calendar.YEAR)+"-"+calendar.get(calendar.MONTH)+"-01");
		// return
		// this.getYear()+"-"+this.getMonth()+"-"+this.calendar.get(calendar.DAY_OF_MONTH);
		return this.getDate();
	}
	/**
	 * 取从当前日期偏移到的日期:具体时间值字符串 add by benyp(5260)：返回的时间不含0 如:2008-3-1 10:1:1
	 * 
	 * @param dateType
	 * @param timeNumber
	 * @return getLongDate()
	 */
	public String getNextDateTimeWithoutZero(String dateType, int timeNumber)
	{
		if (dateType.equals("year"))
		{
			calendar.add(Calendar.YEAR, timeNumber);
		}
		else if (dateType.equals("month"))
		{
			calendar.add(Calendar.MONTH, timeNumber);
		}
		else if (dateType.equals("day"))
		{
			calendar.add(Calendar.DATE, timeNumber);
		}
		else if (dateType.equals("hour"))
		{
			calendar.add(Calendar.HOUR, timeNumber);
		}
		else if (dateType.equals("minute"))
		{
			calendar.add(Calendar.MINUTE, timeNumber);
		}
		else if (dateType.equals("second"))
		{
			calendar.add(Calendar.SECOND, timeNumber);
		}
		return (this.getDate2() + " " + this.getHour() + ":" + this.getMinute() + ":" + this
				.getSecond());
	}
	public static void main(String[] args)
	{
		
		//Logger m_logger = Logger.getLogger(DateTimeUtil.class);
		DateTimeUtil timeUtil = new DateTimeUtil("");
		System.out.println(timeUtil.getLongDate());
		m_logger.error(String.valueOf(timeUtil.getLongTime()));
		m_logger.error(String.valueOf(timeUtil.getNextLongTime()));
		m_logger.error(String.valueOf(timeUtil.getLongTime()));
	}
}