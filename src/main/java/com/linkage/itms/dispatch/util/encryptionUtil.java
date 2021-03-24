package com.linkage.itms.dispatch.util;

/**
 *        加密与解密工具类
		* @author Administrator (AILK No.)
		* @version 1.0
		* @since 2015年7月8日
		* @category com.linkage.litms.eserver.util
		* @copyright AILK NBS-Network Mgt. RD Dept.
 */
public class encryptionUtil {

	private static String m_DIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@^%*(),.;:_=-+'\"\\/|&?$#{}[]<>";
	private static String m_KEY = "aiobsAIOBS123";

	
	/**
	 *        加密
			* @param oldPw
			* @return 成功返回加密数据，失败返回""
	 */
	public static String encryption(String oldPw)
	{
		if(oldPw == null || oldPw.equals(""))
		{
			return "";
		}
		
		String pw = "", newPw = "";
		int i,j,lp,ld,rem,iTmp,sum = 0;
		
		ld = m_DIC.length();
		lp = oldPw.length();
		
		for(i = 0; i < m_KEY.length(); i++)
		{
			sum += (int)m_KEY.charAt(i);
		}
		rem = (sum+ld) % lp;
		for(i = 0; i < lp; i++)
		{
			j = rem + i;
			if(j >= lp) j = j - lp;
			pw += oldPw.charAt(j);
			iTmp = m_DIC.indexOf(pw.charAt(i));
			if(iTmp == -1)
				return "";
			newPw += String.format("%02X", m_DIC.indexOf(pw.charAt(i))).toUpperCase();
		}
		
		return newPw;
	}
	
	/**
	 *       解密
			* @param oldPw
			* @return 成功返回解密数据，失败返回""
	 */
	
	public static String decryption(String oldPw)
	{
		if(oldPw == null || oldPw.equals("") || oldPw.length()%2 != 0)
		{
			return "";
		}
		
		String pw = "", newPw = "";
		char c1,c2;
		int i,j,lp,ld,rem,iTmp,sum = 0;
		
		ld = m_DIC.length();
		lp = oldPw.length();
		
		for(i = 0; i < m_KEY.length(); i++)
		{
			sum += (int)m_KEY.charAt(i);
		}
		rem = (sum+ld) % lp;
		
		for(i = 0; (i+2) <= lp; i+=2)
		{
			c1 = oldPw.substring(i, i+1).charAt(0);
			c2 = oldPw.substring(i+1, i+2).charAt(0);
			if (!(((c1 >= '0' && c1 <= '9') ||
				(c1 >= 'A' && c1 <= 'Z'))
				&&
				((c2 >= '0' && c2 <= '9') ||
				(c2 >= 'A' && c2 <= 'Z'))))
			{
				return "";
			}
				
			iTmp = Integer.parseInt(oldPw.substring(i, i+2), 16);
			if (iTmp >= ld) return "";
			pw += m_DIC.charAt(iTmp);
		}
		
		lp = pw.length();
		rem = (sum+ld) % lp;
		for(i = 0; i < lp; i++,rem--)
		{
			if(rem > 0)
				j = lp - rem;
			else
				j = rem * (-1);
			newPw += pw.charAt(j);
		}
		return newPw;
	}
}
