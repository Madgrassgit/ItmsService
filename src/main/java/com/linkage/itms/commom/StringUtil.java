
package com.linkage.itms.commom;

import java.util.List;
import java.util.Map;


/**
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-4-2
 * @category com.linkage.itms.commom
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class StringUtil
{

	public static boolean isEmpty(String input)
	{
		return input == null || input.trim().length() == 0;
	}

	public static boolean notEmpty(String input)
	{
		return !isEmpty(input);
	}

	public static String nvl(String input)
	{
		return nvl(input, "");
	}

	public static String nvl(String input, String defaultValue)
	{
		return input == null ? defaultValue : input;
	}
	
	/**
	 * 将list中内容用“,”分隔符组合一个字符串是java String类split反向操作
	 *
	 * @param list
	 * @return String
	 * @author yanhj
	 * @date 2006-2-6
	 */
	@SuppressWarnings("rawtypes")
	public static String weave(List list) {
		StringBuffer sb = new StringBuffer(100);
		if (list.size() != 0) {
			sb.append("'").append(list.get(0)).append("'");

			for (int i = 1; i < list.size(); i++) {
				sb.append(",'").append(list.get(i)).append("'");
			}
		}

		return sb.toString();
	}
	
	/**
	 * 格式化数据
	 * @param map
	 * @param columName
	 * @return
	 */
	public static String getStringValue(Map<String, String> map, String columName) {
		if (null == columName || null == map || null == map.get(columName)) {
			return "";
		}
		return map.get(columName).toString();
	}
	
	/**
	 * 格式化数据
	 * @param map
	 * @param columName
	 * @return
	 */
	public static String getStringValue(Object obj) {
		if (null == obj) {
			return "";
		} else {
			return obj.toString();
		}
	}
	
	/**
	 * 为空判断
	 * @param str
	 * @return
	 */
	public static boolean IsEmpty(String str) {
		return StringUtil.isEmpty(str);
	}
	
	/**
	 * 取int型值
	 * @param map
	 * @param columName
	 * @return
	 */
	public static int getIntValue(Map<String, String> map, String columName) {
		return com.linkage.commons.util.StringUtil.getIntValue(map, columName);
	}
	
	/**
	 * 取long型值
	 * @param columName
	 * @return
	 */
	public static long getLongValue(String columName) { 
		return com.linkage.commons.util.StringUtil.getLongValue(columName);
	}
	
	/**
	 * 取long型值
	 * @param columName
	 * @return
	 */
	public static long getLongValue(Object obj) {
		return com.linkage.commons.util.StringUtil.getLongValue(obj);
	}
	
	/**
	 * 返回对象的int值,不是数字则返回0
	 * 
	 */
	public static int getIntegerValue(Object obj) {
		return com.linkage.commons.util.StringUtil.getIntegerValue(obj);
	}
	
	/**
	 * 返回对象的int值,不是数字则返回defal
	 * 
	 * @param
	 * @return int
	 */
	public static int getIntegerValue(Object obj, int defal) {
		return com.linkage.commons.util.StringUtil.getIntegerValue(obj, defal);
	}

	/**
	 * SQL:获取sql用的字符串, 有如下两种情况
	 * <li>tmp为null或为'':返回 null</li>
	 * <li>否则返回 :"'" + tmp + "'"</li>
	 * 
	 * @param tmp
	 * @return
	 */
	public static String getSQLString(String tmp) {
		return StringUtil.getSQLString(tmp, true);
	}
	/**
	 * SQL:获取sql用的字符串.有如下三种情况
	 * <li>tmp为null:返回 null</li>
	 * <li>tmp为"",flag为true: 返回null</li>
	 * <li>tmp非null非"",返回"'" + tmp + "'"</li>
	 * 
	 * @param tmp
	 * @param flag
	 * @return
	 */
	public static String getSQLString(String tmp, boolean flag) {
		if (tmp == null) {
			return null;
		}

		if (tmp.equals("") && flag == true) {
			return null;
		}

		return "'" + tmp + "'";
	}

}
