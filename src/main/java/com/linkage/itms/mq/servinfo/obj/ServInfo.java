/**
 * 
 */
package com.linkage.itms.mq.servinfo.obj;

/**
 * 配置模块业务下发后调用ItmsService发送MQ消息的OBJ类
 * 
 * <p> mq-topic: servinfo
 * <p> 消息体
 *	<ServInfo>
 *		<devId>438</devId>
 *	</ServInfo>
 *
 * @author chenjie
 * @date 2011-12-15
 */
public class ServInfo {
	
	/**
	 * 设备ID
	 */
	public String devId;

	public String getDevId() {
		return devId;
	}

	public void setDevId(String devId) {
		this.devId = devId;
	}
}
