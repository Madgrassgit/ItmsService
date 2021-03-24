package com.linkage.stbms.policy.mq.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.jms.ITopicMessageHandle;
import com.linkage.commons.jms.TopicMsgListener;
import com.linkage.commons.jms.obj.ActiveMQConfig;
import com.linkage.commons.jms.obj.KafkaConfig;
import com.linkage.stbms.itv.main.Global;

public class PicUpTaskThread extends Thread implements ITopicMessageHandle {

private static Logger logger = LoggerFactory.getLogger(PicUpTaskThread.class);
	
	//	 new Listener
//	final TopicMsgListener topicListener = new TopicMsgListener(Global.MQ_PIC_UP_TASK_URL);
	TopicMsgListener topicListener = null;
	private final String TOPIC = "picUp.task";
	
	public void run()
	{
		logger.debug("sevinfo mq url:({})", TOPIC);
		
		if("activeMQ".equals(Global.MQ_POOl_MAP.get(TOPIC).getType())){
			topicListener = new TopicMsgListener(((ActiveMQConfig)Global.MQ_POOl_MAP.get(TOPIC)).getUrl());
			
		}
		else if("kafka".equals(Global.MQ_POOl_MAP.get(TOPIC).getType())){
			topicListener = new TopicMsgListener((KafkaConfig)Global.MQ_POOl_MAP.get(TOPIC));
		}
		
		// set handle
		topicListener.setMessageHandle(this);
		
		// Regist
		//topicListener.listenTopic(Global.MQ_PIC_UP_TASK_TOPIC);
		topicListener.listenTopic(TOPIC);
		
		logger.debug("servinfo start listen");
	}
	
	public void handTopicMessage(String _topic, String _message) 
	{
		/**
		 * 此段代码是模拟接收MQ消息，用于测试
		logger.debug("handTopicMessage({},{})", _topic, _message);
		if(_topic.equals(Global.MQ_PIC_UP_TASK_TOPIC))
		{
			logger.debug("deal message, topic:[{}]", _topic);
			
			XML2Bean x2b = new XML2Bean(_message);
			
			PicUp picUp = (PicUp)x2b.getBean("PicUp", PicUp.class);
			
			// 没有数据直接返回
			if(null != picUp && false == StringUtil.IsEmpty(picUp.getTask().getClientId())){
				logger.warn("===picUp.getTask()=="+picUp.getTask()+"=====");
				logger.warn("===picUp.getTask().getClientId()=="+picUp.getTask().getClientId()+"=====");
				logger.warn("===picUp.getTask().getTaskId()=="+picUp.getTask().getTaskId()+"=====");
				logger.warn("===picUp.getTask().getAction()=="+picUp.getTask().getAction()+"=====");
			}
		}
		else
		{
			logger.error("unknown topic({})", _topic);
		}
		
		*/
	}
}
