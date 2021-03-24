package com.linkage.itms.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.jms.ITopicMessageHandle;
import com.linkage.itms.Global;


/**
 * 
 * @author xiangzl (Ailk No.)
 * @version 1.0
 * @since May 16, 2013
 * @category com.linkage.itms.jms
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class MQMessageHandler implements ITopicMessageHandle 
{
	private Logger log = LoggerFactory.getLogger(MQMessageHandler.class);
	
	@Override
	public void handTopicMessage(String topic, String message)
	{
//		log.debug("deal infrom message, topic:[{}]", topic);
		log.debug("[{}]-[{}]",topic,message);
		ProcessRusultThread bindThread = new ProcessRusultThread();
		bindThread.setMessage(message);
		Global.G_BlThreadPool.execute(bindThread);

	}
}
