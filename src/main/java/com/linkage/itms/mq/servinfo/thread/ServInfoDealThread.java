/**
 * 
 */
package com.linkage.itms.mq.servinfo.thread;

import java.util.HashMap;

import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.WSClient.WSClientProcess;
import com.linkage.commons.jms.ITopicMessageHandle;
import com.linkage.commons.jms.TopicMsgListener;
import com.linkage.commons.jms.obj.ActiveMQConfig;
import com.linkage.commons.jms.obj.KafkaConfig;
import com.linkage.commons.util.StringUtil;
import com.linkage.commons.xml.XML2Bean;
import com.linkage.itms.Global;
import com.linkage.itms.mq.servinfo.obj.ServInfo;
import com.linkage.itms.mq.servinfo.xml.servInfoToXML;

/**
 * 配置模块下发业务后发送MQ消息给ItmsService
 * <p> mq-topic: servinfo
 * <p> 消息体
 * <ServInfo>
 *		<devId>438</devId>
 * </ServInfo>
 * 
 * @author chenjie
 * @date 2011-12-15
 */
public class ServInfoDealThread extends Thread implements ITopicMessageHandle{

	private static Logger logger = LoggerFactory.getLogger(ServInfoDealThread.class);
	
	//	 new Listener
//	final TopicMsgListener topicListener = new TopicMsgListener(Global.MQ_SERVINFO_URL);
	TopicMsgListener topicListener = null;
	private final String TOPIC = "servinfo";
	
	public void run()
	{
		//logger.debug("sevinfo mq url:({})", ((ActiveMQConfig)Global.MQ_POOl_MAP.get(TOPIC)).getUrl());
		if("activeMQ".equals(Global.MQ_POOl_MAP.get(TOPIC).getType())){
			topicListener = new TopicMsgListener(((ActiveMQConfig)Global.MQ_POOl_MAP.get(TOPIC)).getUrl());
			
		}
		else if("kafka".equals(Global.MQ_POOl_MAP.get(TOPIC).getType())){
			topicListener = new TopicMsgListener((KafkaConfig)Global.MQ_POOl_MAP.get(TOPIC));
		}
		
		// set handle
		topicListener.setMessageHandle(this);
		
		// Regist
		//topicListener.listenTopic(Global.MQ_SERVINFO_TOPIC);
		topicListener.listenTopic("servinfo");
		
		logger.debug("servinfo start listen");
	}
	
	public void handTopicMessage(String _topic, String _message) 
	{
		logger.debug("handTopicMessage({},{})", _topic, _message);
//		if(_topic.equals(Global.MQ_SERVINFO_TOPIC))
		if(_topic.equals(TOPIC))
		{
			logger.debug("deal message, topic:[{}]", _topic);
			
			XML2Bean x2b = new XML2Bean(_message);
			
			ServInfo servInfo = (ServInfo)x2b.getBean("ServInfo", ServInfo.class);
			
			String strXML = "";
			
			// 没有数据直接返回
			if(null != servInfo && false == StringUtil.IsEmpty(servInfo.getDevId())){
				servInfoToXML servInfo2XML = new servInfoToXML();
				strXML =  servInfo2XML.getXML(StringUtil.getStringValue(servInfo.getDevId()));
			}
			
//			logger.warn("===="+strXML+"====");
			
			// 处理数据
			try {
				/** 命名空间 */
				String NAMESPACE = "http://ws.netcutover_js.epon.liposs.module.linkage.com";
				/** 方法名 */
				String METHOD_GETUSERMODEMINFO = "completedInfo";
				/** webservice前缀 */
				String PREFIX = "ns1";
				/** Action前缀 */
				String ACTION_PREFIX = "ns:";
				
				String URL = "http://112.4.93.137:8000/liposs/services/liposs_epon_netcutover_WS4ITMS?wsdl";
				
				
				HashMap<String, String> param = new HashMap<String, String>();
				param.put("para", strXML.toString());
			
				OMElement element = WSClientProcess.serviceReceive(NAMESPACE, PREFIX, ACTION_PREFIX + METHOD_GETUSERMODEMINFO, METHOD_GETUSERMODEMINFO, param, URL);
				
				// xml返回值
				if (element != null) {
					try {
						String returnParam = element.getFirstElement().getText();
//						logger.warn("===returnParam=="+returnParam+"=====");
					} catch (Exception e) {
						logger.error("WebService解析XML失败, mesg:({})", e.getMessage());
					}
				}
			} catch (Exception e) {
				logger.error("方法completedInfo处理数据失败!,mesg({})", e.getMessage());
			}
		}
		else
		{
			logger.error("unknown topic({})", _topic);
		}
	}
	
	public static void main(String[] args) {
		String message = "<ServInfo><devId>438</devId></ServInfo>";
		XML2Bean x2b = new XML2Bean(message);
		//DeviceStbBindObj deviceStbBindObj = (DeviceStbBindObj)x2b.getBean("DevStbBind", DeviceStbBindObj.class);
		ServInfo servInfo = (ServInfo)x2b.getBean("ServInfo", ServInfo.class);
		System.err.println(servInfo.getDevId());
	}
}
