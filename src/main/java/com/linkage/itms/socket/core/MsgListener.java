
package com.linkage.itms.socket.core;

/**
 * <pre>
 * 消息监听器
 * 该接口负责根据消息的内容，返回具体的消息逻辑处理类，如果不满足条件，返回null
 * 客户端实现该类后，需要调用SocketWorker.addMsgListener()方法来注册监听
 * </pre>
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-3-6
 * @category com.linkage.itms.socket
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public interface MsgListener
{

	/**
	 * <pre>
	 * 根据消息内容返回一个具体的消息逻辑处理类。
	 * 如果不处理该消息内容，返回null。
	 * </pre>
	 * 
	 * @param message
	 *            消息内容
	 * @return 如果处理消息，则返回具体的MagAction，否则返回null
	 */
	MsgAction handleMessage(String message);
}
