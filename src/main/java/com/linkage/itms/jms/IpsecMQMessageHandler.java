package com.linkage.itms.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.jms.ITopicMessageHandle;
import com.linkage.itms.Global;

/**
 * ipsec消息处理
 * @author jlp
 *
 */
public class IpsecMQMessageHandler implements ITopicMessageHandle 
{
	private Logger logger = LoggerFactory.getLogger(IpsecMQMessageHandler.class);
	
	@Override
	public void handTopicMessage(String topic, String message)
	{
		logger.warn("IpsecMQMessageHandler-->handTopicMessage");
		IpSecCallBackThread callbackThread = new IpSecCallBackThread();
		callbackThread.setMessage(message);
		Global.G_CallBackThread.execute(callbackThread);
	}
	
	
}
