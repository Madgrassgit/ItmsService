package com.linkage.itms.dispatch.util;

import javax.jms.DeliveryMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.jms.TopicMsgPublisher;
import com.linkage.commons.jms.obj.ActiveMQConfig;
import com.linkage.commons.jms.obj.KafkaConfig;
import com.linkage.itms.Global;

public class CommonUtil {

	final static Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	private static CommonUtil instance = new CommonUtil();

	private TopicMsgPublisher publisher = null;

	private static String IPSEC = "dev.ipsec";

	/**
	 * get instance
	 * 
	 * @return
	 */
	public static CommonUtil getInstance() {
		return instance;
	}

	/**
	 * ipsec 
	 * 
	 * @param deviceId
	 * @param servTypeId
	 * @param servName
	 * @param openStatus
	 * @param requestId
	 */
	public static void sendServMq(String loid, String userinfo) {
		if (Global.MQ_POOl_MAP != null && Global.MQ_POOl_MAP.get(IPSEC) != null) {
			if (CommonUtil.getInstance().getPublisher() == null) {
				if ("activeMQ".equals(Global.MQ_POOl_MAP.get(IPSEC).getType())) {
					CommonUtil.getInstance().setPublisher(new TopicMsgPublisher(((ActiveMQConfig) Global.MQ_POOl_MAP.get(IPSEC)).getUrl()));
					CommonUtil.getInstance().getPublisher().registTopic(IPSEC, DeliveryMode.PERSISTENT);
				} 
				else if ("kafka".equals(Global.MQ_POOl_MAP.get(IPSEC).getType())) {
					CommonUtil.getInstance().setPublisher(new TopicMsgPublisher((KafkaConfig) Global.MQ_POOl_MAP.get(IPSEC)));
				}
			}
			
			StringBuffer bf = new StringBuffer();
			bf.append("<ServInfo>");
			bf.append("<servType>").append("5").append("</servType>");
			bf.append("<loid>").append(loid).append("</loid>");
			bf.append("<userInfo>").append(userinfo).append("</userInfo>");
			bf.append("</ServInfo>");
			try {
				CommonUtil.getInstance().publish(IPSEC, bf.toString());
				logger.warn("[{}][{}]mq message[{}] publish succeed, Topic:[{}] ", new Object[] {loid, userinfo, bf.toString(), IPSEC });
			}catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void sendMq(String param, String type) {
		if (Global.MQ_POOl_MAP != null && Global.MQ_POOl_MAP.get(IPSEC) != null) {
			if (CommonUtil.getInstance().getPublisher() == null) {
				if ("activeMQ".equals(Global.MQ_POOl_MAP.get(IPSEC).getType())) {
					CommonUtil.getInstance().setPublisher(new TopicMsgPublisher(((ActiveMQConfig) Global.MQ_POOl_MAP.get(IPSEC)).getUrl()));
					CommonUtil.getInstance().getPublisher().registTopic(IPSEC, DeliveryMode.PERSISTENT);
				} 
				else if ("kafka".equals(Global.MQ_POOl_MAP.get(IPSEC).getType())) {
					CommonUtil.getInstance().setPublisher(new TopicMsgPublisher((KafkaConfig) Global.MQ_POOl_MAP.get(IPSEC)));
				}
			}
			
			StringBuffer bf = new StringBuffer();
			bf.append("<ServInfo>");
			bf.append("<servType>").append(type).append("</servType>");
			bf.append("<param>").append(param).append("</param>");
			bf.append("</ServInfo>");
			try {
				CommonUtil.getInstance().publish(IPSEC, bf.toString());
				logger.warn("mq message[{}] publish succeed, Topic:[{}] ", new Object[] {bf.toString(), IPSEC });
			}catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
			}
		}
	}

	public TopicMsgPublisher getPublisher() {
		return publisher;
	}

	public void setPublisher(TopicMsgPublisher publisher) {
		this.publisher = publisher;
	}

	public void publish(String topic, String msg) {
		publisher.publish(topic, msg);
	}
}
