package com.linkage.itms.mq.servinfo.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.jms.ITopicMessageHandle;
import com.linkage.itms.Global;


public class DevServinfoDealHandler implements ITopicMessageHandle 
{
	private Logger log = LoggerFactory.getLogger(DevServinfoDealHandler.class);
	
	@Override
	public void handTopicMessage(String topic, String message)
	{
		log.warn("TOPIC:{},message:{}",topic,message);
		DevServinfoDealThread thread = new DevServinfoDealThread();
		thread.setMessage(message);
		thread.setTopic(topic);
		Global.G_SendThreadPool.execute(thread);

	}
}
