
package com.linkage.itms.socket.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 非线程安全类<br>
 * <B>由于线程处理类已经记录接受消息和返回消息日志，所以该实现类不需要再次记录</B>
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-3-6
 * @category com.linkage.itms.socket.core
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public abstract class DefaultMsgAction implements MsgAction
{
	protected static final Logger logger = LoggerFactory.getLogger(DefaultMsgAction.class);

	/**
	 * 错误码
	 */
	protected String resultCode;
	/**
	 * 错误原因
	 */
	protected String resultMsg;

	public void setResult(String resultCode, String resultMsg)
	{
		this.resultCode = resultCode;
		this.resultMsg = resultMsg;
	}
}
