
package com.linkage.itms.socket.pwdsyn.util;

import com.linkage.itms.socket.core.MsgAction;
import com.linkage.itms.socket.core.MsgListener;
import com.linkage.itms.socket.pwdsyn.bio.PwdSynBIO;

/**
 * Socket接口文本协议： UserPwdSync2ITMS {123} {test123} {bfa9e032e4eb4448} {20140221 144600}
 * 当接受Socket消息以UserPwdSync2ITMS则认为是密码同步接口。
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-3-6
 * @category com.linkage.itms.socket.pwdsyn.bio
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class PwdSynListener implements MsgListener
{

	@Override
	public MsgAction handleMessage(String message)
	{
		if (message != null && message.startsWith("UserPwdSync2ITMS"))
		{
			return new PwdSynBIO();
		}
		return null;
	}
}
