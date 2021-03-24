
package com.linkage.itms.socket.core;

/**
 * <pre>
 * 具体的消息逻辑处理类
 * 由于消息监听器已经进行消息过滤，所以该类只需要进行业务逻辑处理。
 * <B>由于线程处理类已经记录接受消息和返回消息日志，所以该实现类不需要再次记录</B>
 * </pre>
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-3-6
 * @category com.linkage.itms.socket
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public interface MsgAction
{

	String execute(String message);
}
