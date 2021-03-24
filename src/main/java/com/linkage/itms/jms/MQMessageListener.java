package com.linkage.itms.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.jms.TopicMsgListener;
import com.linkage.commons.jms.obj.ActiveMQConfig;
import com.linkage.commons.jms.obj.KafkaConfig;
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
public class MQMessageListener extends Thread
{
	
	static final Logger logger = LoggerFactory.getLogger(MQMessageListener.class);
	
	private final String TOPIC = "dev.servinfo";
	
	@Override
	public void run(){
		logger.warn("begining init mq message listener...");
		try
		{
			
			TopicMsgListener myLitener = null;
			if("activeMQ".equals(Global.MQ_POOl_MAP.get(TOPIC).getType())){
				myLitener = new TopicMsgListener(((ActiveMQConfig)Global.MQ_POOl_MAP.get(TOPIC)).getUrl());
				
			}
			else if("kafka".equals(Global.MQ_POOl_MAP.get(TOPIC).getType())){
				myLitener = new TopicMsgListener((KafkaConfig)Global.MQ_POOl_MAP.get(TOPIC));
			}
			if(myLitener != null){
				myLitener.setMessageHandle(new MQMessageHandler());
				myLitener.listenTopic(TOPIC);
			}

		}
		catch (Exception e)
		{
			logger.error("init mq message listener failed:", e);
		}
		
		logger.warn("init mq message listener succeed, Topic:[{}] ", TOPIC);
	}
}
