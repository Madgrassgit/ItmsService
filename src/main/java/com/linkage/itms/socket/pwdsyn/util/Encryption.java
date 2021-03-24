
package com.linkage.itms.socket.pwdsyn.util;


public class Encryption
{

	private static String m_DIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~!@^*()_-,.;:=`|?$#{}[]<>+%/";
	private static String m_KEY = "aiobsAIOBS123";

	public static void main(String[] args)
	{
		String pw = "3C3C3C3C3C3C";
//		System.out.println(pw);
//		String str1 = encrypt(pw);
//		System.out.println(str1);
		String str2 = decrypt(pw);
		System.out.println(str2);
	}

	// 加密
	// return: 成功返回加密数据，失败返回空“”
	public static String encrypt(String srcPw)
	{
		if(srcPw == null || srcPw.equals(""))
		{
			return "";
		}
		
		String pw = "", newPw = "";
		int i,j,lp,ld,rem,iTmp,sum = 0;
		
		ld = m_DIC.length();
		lp = srcPw.length();
		
		for(i = 0; i < m_KEY.length(); i++)
		{
			sum += (int)m_KEY.charAt(i);
		}
		rem = (sum+ld) % lp;
		for(i = 0; i < lp; i++)
		{
			j = rem + i;
			if(j >= lp) j = j - lp;
			pw += srcPw.charAt(j);
			iTmp = m_DIC.indexOf(pw.charAt(i));
			if(iTmp == -1)
				return "";
			newPw += String.format("%02X", m_DIC.indexOf(pw.charAt(i))).toUpperCase();
		}
		
		return newPw;
	}

	// 解密
	// return: 成功返回解密数据，失败返回空“”
	public static String decrypt(String encodedPw)
	{
		if(encodedPw == null ||encodedPw.equals("") || encodedPw.length()%2 != 0)
		{
			return "";
		}
		
		String pw = "", newPw = "";
		char c1,c2;
		int i,j,lp,ld,rem,iTmp,sum = 0;
		
		ld = m_DIC.length();
		lp = encodedPw.length();
		
		for(i = 0; i < m_KEY.length(); i++)
		{
			sum += (int)m_KEY.charAt(i);
		}
		rem = (sum+ld) % lp;
		
		for(i = 0; (i+2) <= lp; i+=2)
		{
			c1 = encodedPw.substring(i, i+1).charAt(0);
			c2 = encodedPw.substring(i+1, i+2).charAt(0);
			if (!(((c1 >= '0' && c1 <= '9') ||
				(c1 >= 'A' && c1 <= 'Z'))
				&&
				((c2 >= '0' && c2 <= '9') ||
				(c2 >= 'A' && c2 <= 'Z'))))
			{
				return "";
			}
				
			iTmp = Integer.parseInt(encodedPw.substring(i, i+2), 16);
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
