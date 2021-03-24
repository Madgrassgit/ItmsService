package com.linkage.itms.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.jms.TopicMsgListener;
import com.linkage.commons.jms.obj.ActiveMQConfig;
import com.linkage.commons.jms.obj.KafkaConfig;
import com.linkage.itms.Global;
/**
 * ipsec监听消息
 * @author jlp
 *
 */
public class IpsecMQMessageListener extends Thread
{
	
	static final Logger logger = LoggerFactory.getLogger(IpsecMQMessageListener.class);
	
	private final String TOPIC = "dev.ipsec";
	
	@Override
	public void run(){
		logger.warn("begining initIpsecMQMessageListener...");
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
				myLitener.setMessageHandle(new IpsecMQMessageHandler());
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
