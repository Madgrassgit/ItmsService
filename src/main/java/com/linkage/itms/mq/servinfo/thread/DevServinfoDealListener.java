package com.linkage.itms.mq.servinfo.thread;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.jms.MQConfigParser;
import com.linkage.commons.jms.TopicMsgListener;
import com.linkage.commons.jms.obj.ActiveMQConfig;
import com.linkage.commons.jms.obj.KafkaConfig;
import com.linkage.commons.jms.obj.MQConfig;
import com.linkage.stbms.ids.obj.SysConstant;

public class DevServinfoDealListener  extends Thread
{
	
	static final Logger logger = LoggerFactory.getLogger(DevServinfoDealListener.class);
	private TopicMsgListener myLitener = null;
	private String TOPIC = "dev.servinfo";
	@Override
	public void run(){
		destroy();
		Map<String, MQConfig> map = MQConfigParser.getMQConfig(SysConstant.G_ConfPath + "MQPool.xml", "itms");
		//logger.warn("map.get(TOPIC).getType()=================" + map.get(TOPIC).getType());
		if (null != map && null != map.get(TOPIC)){
			if ("activeMQ".equals(map.get(TOPIC).getType())){
				ActiveMQConfig activeMQ = (ActiveMQConfig) map.get(TOPIC);
				myLitener = new TopicMsgListener(activeMQ.getUrl());			
			}
			else if("kafka".equals(map.get(TOPIC).getType())){
				logger.warn("kafka 监听{}消息。。。。。。。。。。。。",TOPIC);
				KafkaConfig kafka = (KafkaConfig) map.get(TOPIC);
				myLitener = new TopicMsgListener(kafka);
			}
			myLitener.setMessageHandle(new DevServinfoDealHandler());
			myLitener.listenTopic(TOPIC);
		}
		
	}
	@Override
	public void destroy()
	{
		logger.warn("destroy mq message listener ing:"+this.isAlive());
		if(myLitener != null)
		{
			myLitener.close();
		}
		logger.warn("destroy mq message listener end:"+this.isAlive());
	}
}
