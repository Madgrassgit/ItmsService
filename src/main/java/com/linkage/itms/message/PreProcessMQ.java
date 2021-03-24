package com.linkage.itms.message;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import PreProcess.OneToMany;
import PreProcess.UserInfo;

import com.linkage.commons.jms.MQConfigParser;
import com.linkage.commons.jms.MQPublisher;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.PreProcessInterface;
import com.linkage.itms.cao.PreServInfoOBJ;

/**
 * 发送给配置模块消息公用类
 * 
 * @author jiafh (Ailk NO.)
 * @version 1.0
 * @since 2016-11-3
 * @category com.linkage.module.gwms.util.message
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 
 */
public class PreProcessMQ implements PreProcessInterface {

	/** log */
	private static final Logger logger = LoggerFactory
			.getLogger(PreProcessMQ.class);

	/** 配置参数bean */
	private PreProcessMessage preProcessMessage;
	private String gw_type;
	private String topic = "cm.serv";
	
	public PreProcessMQ(String gw_type){
		this.gw_type = gw_type;
		if(Global.GW_TYPE_STB.equals(gw_type)){
			this.topic = topic + "Stb";
		}
	}
	/**
	 * 生成新的策略.
	 * 
	 * @param idArr
	 *            the id of strategy.
	 */
	@Override
	public boolean processOOBatch(String[] idArr) {

		boolean flag = false;
		if (idArr == null || idArr.length == 0) {
			logger.error("idArr == null");
			return flag;
		}
		preProcessMessage = new PreProcessMessage();
		preProcessMessage.setMethodName("processOOBatch");
		preProcessMessage.setStrategyIdArr(idArr);

		try {
			Global.PROCESS_PUBLISHER.publishMQ("cm.strategy",preProcessMessage);
			flag = true;
		} catch (Exception e) {
			logger.warn("Send MQ PreProcess Error:{}.", e.getMessage());
			try {
				initMQPool();
				Global.PROCESS_PUBLISHER.publishMQ("cm.strategy",preProcessMessage);
				flag = true;
			} catch (RuntimeException e1) {
				logger.error("Send MQ PreProcess Error:{}", e1.getMessage());
			}
		}
		return flag;
	}

	/**
	 * 生成新的策略.
	 * 
	 * @param idArr
	 *            the id of strategy.
	 */
	@Override
	public boolean processOOBatch(String id) {

		if (id == null) {
			logger.error("id == null");
			return false;
		}
		return processOOBatch(new String[] { id });
	}

	/**
	 * 默认业务生成新的策略.
	 * 
	 * @param idArr
	 *            the id of strategy.
	 */
	@Override
	public boolean processOMBatch4DefaultService(OneToMany[] objArr) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 默认业务生成新的策略.
	 * 
	 * @param idArr
	 *            the id of strategy.
	 */
	@Override
	public boolean processOMBatch4DefaultService(OneToMany obj) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 绑定设备后通知PP.
	 * 
	 * @param userInfoArr
	 * @return <li>1:成功</li> <li>-1:参数为空</li> <li>-2:绑定失败</li>
	 */
	@Override
	public int processServiceInterface(UserInfo userInfo) {

		if (userInfo == null) {
			logger.error("userInfo == null");
			return -1;
		}
		return processServiceInterface(new UserInfo[] { userInfo });
	}

	/**
	 * 绑定设备后通知PP.
	 * 
	 * @param userInfoArr
	 * @return <li>1:成功</li> <li>-1:参数为空</li> <li>-2:绑定失败</li>
	 */
	@Override
	public int processServiceInterface(UserInfo[] userInfoArr) {

		if (userInfoArr == null || userInfoArr.length == 0) {
			logger.error("userInfoArr == null");
			return -1;
		}

		UserBean[] userBeanArr = new UserBean[5];
		UserBean userBean = new UserBean();
		for (int index = 0; index < userInfoArr.length; index++) {
			userBean.setDeviceId(userInfoArr[index].deviceId);
			userBean.setDeviceSn(userInfoArr[index].deviceSn);
			userBean.setGatherId(userInfoArr[index].gatherId);
			userBean.setOperTypeId(userInfoArr[index].operTypeId);
			userBean.setOui(userInfoArr[index].oui);
			userBean.setServTypeId(userInfoArr[index].servTypeId);
			userBean.setUserId(userInfoArr[index].userId);
			userBeanArr[index] = userBean;
		}
		preProcessMessage = new PreProcessMessage();
		preProcessMessage.setMethodName("processServiceInterface");
		if(Global.GW_TYPE_STB.equals(gw_type)){
			String paramXMLStr = processService4STB(userBeanArr[0].getUserId(), userBeanArr[0].getDeviceId(), userBeanArr[0].getOui(), userBeanArr[0].getDeviceSn());
			preProcessMessage.setParamXMLStr(paramXMLStr);
		}else{
			preProcessMessage.setUserBeanArr(userBeanArr);
		}

		try {
			Global.PROCESS_PUBLISHER.publishMQ(topic,preProcessMessage);
		} catch (Exception e) {
			logger.warn("Send MQ PreProcess Error:{}.", e.getMessage());
			try {
				initMQPool();
				Global.PROCESS_PUBLISHER.publishMQ(topic,preProcessMessage);
			} catch (RuntimeException e1) {
				logger.error("Send MQ PreProcess Error:{}.", e1);
				return -2;
			}
		}
		return 1;
	}

	public String processService4STB(String customer_id, String deviceId,
			String oui, String deviceSn) {
		StringBuffer xmlSB = new StringBuffer();
		xmlSB.append("<ServXml><servList><serv>");
		xmlSB.append("<userId>").append(String.valueOf(customer_id))
				.append("</userId>");
		xmlSB.append("<deviceId>").append(deviceId).append("</deviceId>");
		xmlSB.append("<serviceId>").append("120").append("</serviceId>");
		xmlSB.append("<oui>").append(oui).append("</oui>");
		xmlSB.append("<deviceSn>").append(deviceSn).append("</deviceSn>");
		xmlSB.append("</serv></servList></ServXml>");
		return xmlSB.toString(); 
	}
	/**
	 * get userInfo from params
	 * 
	 * @param length
	 * @return
	 */
	@Override
	public UserInfo GetPPBindUserList(PreServInfoOBJ preInfoObj) {

		logger.debug("GetScheduleSQLList({})", preInfoObj);

		UserInfo uinfo = new UserInfo();
		uinfo.userId = StringUtil.getStringValue(preInfoObj.getUserId());
		uinfo.deviceId = StringUtil.getStringValue(preInfoObj.getDeviceId());
		uinfo.oui = StringUtil.getStringValue(preInfoObj.getOui());
		uinfo.deviceSn = StringUtil.getStringValue(preInfoObj.getDeviceSn());
		uinfo.gatherId = StringUtil.getStringValue(preInfoObj.getGatherId());
		uinfo.servTypeId = StringUtil
				.getStringValue(preInfoObj.getServTypeId());
		uinfo.operTypeId = StringUtil
				.getStringValue(preInfoObj.getOperTypeId());
		return uinfo;
	}

	/**
	 * 长短定时器配置
	 * 
	 * @param userInfoArr
	 * @return <li>1:成功</li> <li>-1:参数为空</li> <li>-2:失败</li>
	 */
	@Override
	public boolean processDeviceStrategy(String[] deviceIds, String serviceId,
			String[] paramArr) {
		logger.warn("-----------processDeviceStrategy-------------");
		if (deviceIds == null) {
			logger.error("deviceIds == null");
			return false;
		}
		preProcessMessage = new PreProcessMessage();
		preProcessMessage.setMethodName("processDeviceStrategy");
		preProcessMessage.setDeviceIdIdArr(deviceIds);
		preProcessMessage.setServiceId(serviceId);
		preProcessMessage.setParamArr(paramArr);

		try {
			this.publisher(serviceId,preProcessMessage);
		} catch (Exception e) {
			logger.warn("Send MQ PreProcess Error:{}.", e.getMessage());
			try {
				// 调用配置模块和绑定模块主题
				initMQPool();
				this.publisher(serviceId,preProcessMessage);
			} catch (RuntimeException e1) {
				logger.error("Send MQ PreProcess Error:{}.", e1);
				return false;
			}
		}
		return true;
	}

	/**
	 * 根据serviceId获取MQ对象
	 * 
	 * @param serviceId
	 * @return
	 */
	private void publisher(String serviceId,PreProcessMessage preProcess) {
		
		String[] servServiceIdArr = StringUtil.IsEmpty(Global.SERV_SERVICE_ID) ? new String[1] : Global.SERV_SERVICE_ID.split(",");
		Arrays.sort(servServiceIdArr);
		
		String[] softServiceIdArr = StringUtil.IsEmpty(Global.SOFT_SERVICE_ID) ? new String[1] : Global.SOFT_SERVICE_ID.split(",");
		Arrays.sort(softServiceIdArr);
		
		String[] batchServiceIdArr = StringUtil.IsEmpty(Global.BATCH_SERVICE_ID) ? new String[1] : Global.BATCH_SERVICE_ID.split(",");
		Arrays.sort(batchServiceIdArr);
		
		if (Arrays.binarySearch(servServiceIdArr, serviceId) >= 0) {
			Global.PROCESS_PUBLISHER.publishMQ("cm.serv",preProcess);
		} else if (Arrays.binarySearch(softServiceIdArr, serviceId) >= 0) {
			Global.PROCESS_PUBLISHER.publishMQ("cm.soft",preProcess);
		} else if (Arrays.binarySearch(batchServiceIdArr, serviceId) >= 0) {
			Global.PROCESS_PUBLISHER.publishMQ("cm.batch",preProcess);
		} else{
			Global.PROCESS_PUBLISHER.publishMQ("cm.strategy",preProcess);
		}
	}
	
	/**
	 * 机顶盒调servStb
	 * @param xmlstr
	 * @return
	 */
	public boolean processSTBServiceInterface(String xmlstr) {
		preProcessMessage = new PreProcessMessage();
		preProcessMessage.setMethodName("processServiceInterface");
		preProcessMessage.setParamXMLStr(xmlstr);
		logger.debug("processSTBServiceInterface(UserInfo[]:{})",xmlstr);
		try {
			Global.PROCESS_PUBLISHER_STB.publishMQ("cm.servStb",preProcessMessage);
		} catch (Exception e) {
			logger.warn("Send Kafka PreProcess Error:{}.", e.getMessage());
			try {
				initMQPool();
				Global.PROCESS_PUBLISHER_STB.publishMQ("cm.servStb",preProcessMessage);
			} catch (RuntimeException e1) {
				logger.error("Send MQ PreProcess Error:{}.", e1);
				return false;
			}
		}
		return true;
	}
	
	private void initMQPool(){
		
		// 解析MQ生产者配置文件
		Global.MQ_POOL_PUBLISHER_MAP = MQConfigParser.getMQConfig(Global.G_ServerHome + File.separator
				+ "conf" + File.separator + "MQPool.xml", "publisher");
		Global.MQ_POOL_PUBLISHER_MAP_STB = MQConfigParser.getMQConfig(Global.G_ServerHome + File.separator
				+ "conf" + File.separator + "MQPool.xml", "stb");
		
		// 调用配置模块模块主题
		List<String> suffixList = new ArrayList<String>();
		suffixList.add(".serv");
		suffixList.add(".soft");
		suffixList.add(".batch");
		suffixList.add(".strategy");
		Global.PROCESS_PUBLISHER = new MQPublisher("cm",suffixList, Global.MQ_POOL_PUBLISHER_MAP);
		
		List<String> suffixListStb = new ArrayList<String>();
		suffixListStb.add(".servStb");
		suffixListStb.add(".softStb");
		suffixListStb.add(".batchStb");
		suffixListStb.add(".strategyStb");
		com.linkage.itms.Global.PROCESS_PUBLISHER_STB = new MQPublisher("cm",suffixListStb, com.linkage.itms.Global.MQ_POOL_PUBLISHER_MAP_STB);
	}
	 
}
