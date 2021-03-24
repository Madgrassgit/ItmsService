package com.linkage.itms.mq.publish;

import javax.jms.DeliveryMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.jms.TopicMsgPublisher;
import com.linkage.commons.xml.Bean2XML;

public class MQSimplePublisher {

	/** log */
	private static final Logger logger = LoggerFactory.getLogger(MQSimplePublisher.class);
	
	/** 是否生效 **/
	private int enable;
	
	/** 广播URL **/
	private String url;
	
	/** 广播topic **/
	private String topic;
	
	/** msg publisher **/
	private TopicMsgPublisher publisher;
	
	public MQSimplePublisher(int enable, String url, String topic)
	{
		logger.debug("MQAbsPublisher({},{},{})", new Object[]{enable, url, topic});
		this.enable = enable;
		this.url = url;
		this.topic = topic;
		
		this.publisher = new TopicMsgPublisher(this.url);
		
		
		this.getPublisher().registTopic(this.topic, DeliveryMode.PERSISTENT);
	}
	
	/**
	 * 发布MQ消息
	 * @param obj
	 */
	public void publishMQ(Object obj)
	{
		logger.debug("publishMQ()", obj);
		Bean2XML bean2XML = new Bean2XML();
		String xml = bean2XML.getXML(obj);
		logger.debug("send MQ({}):{}", this.topic, xml);
		this.getPublisher().publish(this.topic, xml);
	}
	
	public int getEnable() {
		return enable;
	}

	public void setEnable(int enable) {
		this.enable = enable;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public TopicMsgPublisher getPublisher() {
		logger.debug("getPublisher()");
		return publisher;
	}

	public void setPublisher(TopicMsgPublisher publisher) {
		this.publisher = publisher;
	}
	
}
