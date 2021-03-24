
package com.linkage.itms.socket.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * Socket工作者
 * 该类只启动一个Socket服务监听消息(目前只实现了短连接方式),并将接受的消息调用消息转发器来处理消息。
 * 设计这一列类的目标为：
 * 1.SocketWorker只负责接受消息，将接受的消息调用转发器来处理。
 * 2.消息转发器MsgDispatcher根据注册的消息监听器来获取正确的消息处理类MsgAction。
 * 3.消息监听器MsgListener由客户端实现，实现消息的过滤，根据具体的消息内容返回具体的消息处理类。
 * 4.消息处理类MsgAction由客户端实现，实现消息处理业务逻辑。
 * <b>5.当新增一个消息监听器后，需要调用SocketWorker.addMsgListener()方法来注册监听。</b>
 * </pre>
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-3-6
 * @category com.linkage.itms.socket
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class SocketWorker
{

	protected static final Logger logger = LoggerFactory.getLogger(SocketWorker.class);
	/**
	 * Socket连接方式：长连接。暂未实现
	 */
	public static final int CONNECT_TYPE_LONG = 1;
	/**
	 * Socket连接方式：短连接
	 */
	public static final int CONNECT_TYPE_SHORT = 0;
	protected MsgDispatcher msgDispatcher = new MsgDispatcher();
	protected int port;
	protected int processThreads;

	/**
	 * <pre>
	 * 根据指定端口号，启动Socket服务
	 * 默认使用Socket短连接
	 * </pre>
	 * 
	 * @param port
	 * @return
	 */
	public static SocketWorker newInstance(int port)
	{
		return newInstance(port, CONNECT_TYPE_SHORT, 1);
	}

	/**
	 * <pre>
	 * 根据指定端口号，启动Socket服务
	 * 默认使用Socket短连接
	 * </pre>
	 * 
	 * @param port
	 * @return
	 */
	public static SocketWorker newInstance(int port, int processThreads)
	{
		return newInstance(port, CONNECT_TYPE_SHORT, processThreads);
	}

	/**
	 * <pre>
	 * 根据指定端口号，和Socket连接类型（长连接或短连接），启动Socket服务
	 * 暂只实现Socket短连接方式
	 * </pre>
	 * 
	 * @param port
	 *            端口号
	 * @param connectType
	 *            连接方式
	 * @return
	 * @see #CONNECT_TYPE_LONG
	 * @see #CONNECT_TYPE_SHORT
	 * @see ShortSocketWorkder
	 */
	public static SocketWorker newInstance(int port, int connectType, int processThreads)
	{
		processThreads = processThreads > 0 ? processThreads : 1;
		if (CONNECT_TYPE_SHORT == connectType)
		{
			return new ShortSocketWorkder(port, processThreads);
		}
		throw new IllegalArgumentException("not support long connect socket yet.");
	}

	protected SocketWorker()
	{
	}

	/**
	 * 启动Socket服务监听
	 * 
	 * @see ShortSocketWorkder#start()
	 */
	public void start()
	{
		// subclass overrides
	}

	public void close()
	{
		// default do nothing
	}

	public void addMsgListener(MsgListener listener)
	{
		msgDispatcher.addListener(listener);
	}

	public void setMsgDispatcher(MsgDispatcher msgDispatcher)
	{
		this.msgDispatcher = msgDispatcher;
	}
}
