
package com.linkage.itms.socket.core;

import java.util.LinkedList;
import java.util.List;

/**
 * <pre>
 * 消息转发器
 * 该类不对消息做任何处理，只是将消息传送给监听器。
 * 1.默认以FIFO的方式将消息传递给监听器。
 * 2.获取到第一个接受消息的监听器处理消息后，不再传递给其他监听器。
 * 3.当所有监听器都不接受消息，则默认返回一个默认消息。
 * 4.当以上规则不满足项目需求时，可以拓展该类。
 * </pre>
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-3-6
 * @category com.linkage.itms.socket
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class MsgDispatcher
{

	private List<MsgListener> listeners = new LinkedList<MsgListener>();
	
	private String defaultResponse = "no message to response.\n";

	public String dispatch(String message)
	{
		MsgAction action = null;
		for (MsgListener listener : listeners)
		{
			if ((action = listener.handleMessage(message)) != null)
			{
				return action.execute(message);
			}
		}
		return defaultResponse;
	}

	public void addListener(MsgListener listener)
	{
		if (listener == null)
		{
			throw new NullPointerException();
		}
		listeners.add(listener);
	}
}
