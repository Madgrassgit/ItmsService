
package com.linkage.itms.socket.pwdsyn.util;

import com.linkage.itms.commom.DateUtil;
import com.linkage.itms.socket.pwdsyn.bean.PwdSynBean;

/**
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-3-6
 * @category com.linkage.itms.socket.pwdsyn.util
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class PwdSynUtil
{

	public static PwdSynBean analize(String message)
	{
		PwdSynBean bean = new PwdSynBean();
		char startCh = '{';
		char endCh = '}';
		int startIx = 0;
		for (int i = 0; (startIx = message.indexOf(startCh, startIx)) > 0; i++)
		{
			int endIx = message.indexOf(endCh, startIx);
			if (endIx > startIx)
			{
				String content = message.substring(startIx + 1, endIx);
				if (i == 0)
				{
					bean.setSerialNo(content);
				}
				else if (i == 1)
				{
					bean.setAccount(content);
				}
				else if (i == 2)
				{
					bean.setUserPwd(Encryption.decrypt(content));
				}
				else if (i == 3)
				{
					bean.setTimestamp(DateUtil.transTime(content, "yyyyMMdd HHmmss"));
				}
			}
			startIx = endIx;
		}
		return bean;
	}
}
