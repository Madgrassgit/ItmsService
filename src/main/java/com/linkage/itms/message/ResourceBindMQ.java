package com.linkage.itms.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ResourceBind.BindInfo;
import ResourceBind.ResultInfo;
import ResourceBind.UnBindInfo;

import com.linkage.commons.jms.MQPublisher;
import com.linkage.itms.Global;
import com.linkage.itms.ResourceBindInterface;

/**
 * 向绑定模块发送消息公共类
 * @author jiafh (Ailk NO.)
 * @version 1.0
 * @since 2016-11-3
 * @category com.linkage.module.gwms.util.message
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ResourceBindMQ implements ResourceBindInterface{
	
	/** log */
	private static final Logger logger = LoggerFactory.getLogger(ResourceBindMQ.class);
	
	/** 配置参数bean */
	private BindInfoMessage bindInfoMessage;
	private String gw_type;
	private String topic = "res.Interface";

	public ResourceBindMQ(String gw_type){
		this.gw_type = gw_type;
		if(Global.GW_TYPE_STB.equals(gw_type)){
			this.topic = topic + "Stb";
		}
	}
	/**
	 * 绑定
	 * @param bindInfo
	 * @return
	 */
	public ResultInfo bind(BindInfo[] bindInfo){
			
		if(null == bindInfo || bindInfo.length == 0){
			logger.error("BindInfo[] bindInfo is null");
			return null;
		}
		
		bindInfoMessage = new BindInfoMessage();
		bindInfoMessage.setMethodName("bind");
		bindInfoMessage.setClientId("ItmsService");
		bindInfoMessage.setPriority("0");
		BindBean[] bindBeanArr = new BindBean[5];
		BindBean bindBean = new BindBean();
		for(int index=0;index < bindInfo.length;index++){
			bindBean.setAccName(bindInfo[index].accName);
			bindBean.setAccOid(bindInfo[index].accOid);
			bindBean.setDeviceId(bindInfo[index].deviceId);
			bindBean.setUserLine(bindInfo[index].userline);
			bindBean.setUserName(bindInfo[index].username);
			bindBeanArr[index] = bindBean;
		}
		bindInfoMessage.setBindBeanArr(bindBeanArr);
		
		ResultInfo resultInfo = new ResultInfo();
		try{
			Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
		}
		catch(Exception e)
		{
			try
			{
				Global.RESOURCE_BIND_PUBLISHER = new MQPublisher(this.topic,Global.MQ_POOL_PUBLISHER_MAP);
				Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
			}
			catch(Exception ex)
			{
				logger.error("rebind ResourceBind Error.\n{}", ex);
				return null;			
			}
		}
		resultInfo.resultId = new String[1];
		resultInfo.resultId[0] = "1";
		resultInfo.status = "1";
		logger.warn("bind: {}-{}",new Object[]{resultInfo.status,resultInfo.resultId});
		return resultInfo;
	}
	
	/**
	 * 解绑
	 * @param bindInfo
	 * @return
	 */
	public ResultInfo release(UnBindInfo[] unBindInfo){
		if(null == unBindInfo || unBindInfo.length == 0){
			logger.error("UnBindInfo[] unBindInfo is null");
			return null;
		}
		
		bindInfoMessage = new BindInfoMessage();
		bindInfoMessage.setMethodName("unBind");
		bindInfoMessage.setClientId("ItmsService");
		bindInfoMessage.setPriority("0");
		BindBean[] bindBeanArr = new BindBean[5];
		BindBean bindBean = new BindBean();
		for(int index=0;index < unBindInfo.length;index++){
			bindBean.setAccName(unBindInfo[index].accName);
			bindBean.setAccOid(unBindInfo[index].accOid);
			bindBean.setDeviceId(unBindInfo[index].deviceId);
			bindBean.setUserLine(unBindInfo[index].userline);
			bindBean.setUserId(unBindInfo[index].userId);
			bindBeanArr[index] = bindBean;
		}
		bindInfoMessage.setBindBeanArr(bindBeanArr);
		
		ResultInfo resultInfo = new ResultInfo();
		try{
			logger.warn("sss"+Global.RESOURCE_BIND_PUBLISHER);
			Global.RESOURCE_BIND_PUBLISHER.publishMQ(topic, bindInfoMessage);
			logger.warn("sss"+topic);
		}
		catch(Exception e)
		{
			try
			{
				Global.RESOURCE_BIND_PUBLISHER = new MQPublisher(topic,Global.MQ_POOL_PUBLISHER_MAP);
				Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
			}
			catch(Exception ex)
			{
				logger.error("reUnbind ResourceBind Error.\n{}", ex);
				return null;			
			}
		}
		resultInfo.resultId = new String[1];
		resultInfo.resultId[0] = "1";
		resultInfo.status = "1";
		logger.warn("unbind: {}-{}",new Object[]{resultInfo.status,resultInfo.resultId});
		return resultInfo;
	}
	
	
	/**
	 * 绑定
	 */
	public void DoBindSingl(String username, String deviceId, String accName,
			int userline) {
		
		BindInfo[] arr = new BindInfo[1];
		arr[0] = new BindInfo();
		arr[0].accOid = "0";
		arr[0].accName = "ItmsService";
		arr[0].username = username;
		arr[0].deviceId = deviceId;
		arr[0].userline = userline;
		this.DoBindSingl(arr);
	}

	/**
	 * 解绑
	 */
	public void DoUnBindSingl(String userid, String deviceId, String accName,
			int userline) {
		
		UnBindInfo[] arr = new UnBindInfo[1];
		arr[0] = new UnBindInfo();
		arr[0].accOid = "0";
		arr[0].accName = "ItmsService";
		arr[0].userId = userid;
		arr[0].deviceId = deviceId;
		arr[0].userline = userline;
		
		this.DoUnBindSingl(arr);	
	}

	/**
	 * 绑定
	 */
	public void DoBindSingl(BindInfo[] bindInfo) {
		
		logger.debug("ResourceBindCorba.DoBindSingl");
		if (null == bindInfo || bindInfo.length == 0) {
			return;
		}	
		bindInfoMessage = new BindInfoMessage();
		bindInfoMessage.setMethodName("bindSingle");
		bindInfoMessage.setClientId("ItmsService");
		bindInfoMessage.setPriority("0");
		BindBean[] bindBeanArr = new BindBean[5];
		BindBean bindBean = new BindBean();
		for(int index=0;index < bindInfo.length;index++){
			bindBean.setAccName(bindInfo[index].accName);
			bindBean.setAccOid(bindInfo[index].accOid);
			bindBean.setDeviceId(bindInfo[index].deviceId);
			bindBean.setUserLine(bindInfo[index].userline);
			bindBean.setUserName(bindInfo[index].username);
			bindBeanArr[index] = bindBean;
		}
		bindInfoMessage.setBindBeanArr(bindBeanArr);
		
		try{
			Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
		}
		catch(Exception e)
		{
			try
			{
				Global.RESOURCE_BIND_PUBLISHER = new MQPublisher("res.Interface",Global.MQ_POOL_PUBLISHER_MAP);
				Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
			}
			catch(Exception ex)
			{
				logger.error("rebind ResourceBind Error.\n{}", ex);			
			}
		}
		
	}

	/**
	 * 解绑
	 */
	public void DoUnBindSingl(UnBindInfo[] unBindInfo) {
		
		logger.debug("ResourceBindCorba.DoUnBind");
		if (null == unBindInfo || unBindInfo.length == 0) {
			return;
		}
		bindInfoMessage = new BindInfoMessage();
		bindInfoMessage.setMethodName("unBindSingle");
		bindInfoMessage.setClientId("ItmsService");
		bindInfoMessage.setPriority("0");
		BindBean[] bindBeanArr = new BindBean[5];
		BindBean bindBean = new BindBean();
		for(int index=0;index < unBindInfo.length;index++){
			bindBean.setAccName(unBindInfo[index].accName);
			bindBean.setAccOid(unBindInfo[index].accOid);
			bindBean.setDeviceId(unBindInfo[index].deviceId);
			bindBean.setUserLine(unBindInfo[index].userline);
			bindBean.setUserId(unBindInfo[index].userId);
			bindBeanArr[index] = bindBean;
		}
		bindInfoMessage.setBindBeanArr(bindBeanArr);
		
		try{
			Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
		}
		catch(Exception e)
		{
			try
			{
				Global.RESOURCE_BIND_PUBLISHER = new MQPublisher("res.Interface",Global.MQ_POOL_PUBLISHER_MAP);
				Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
			}
			catch(Exception ex)
			{
				logger.error("reUnbind ResourceBind Error.\n{}", ex);			
			}
		}
	}
	
	/**
	 * 解绑
	 * @param bindInfo
	 * @return
	 */
	public ResultInfo release4JL(UnBindInfo[] unBindInfo, int serviceType){
		
		String clientId = "ItmsService";
		if (4==serviceType){
			clientId = "StbService";
		}
		
		if(null == unBindInfo || unBindInfo.length == 0){
			logger.error("UnBindInfo[] unBindInfo is null");
			return null;
		}
		
		bindInfoMessage = new BindInfoMessage();
		bindInfoMessage.setMethodName("unBind");
		bindInfoMessage.setClientId(clientId);
		bindInfoMessage.setPriority("0");
		BindBean[] bindBeanArr = new BindBean[5];
		BindBean bindBean = new BindBean();
		for(int index=0;index < unBindInfo.length;index++){
			bindBean.setAccName(unBindInfo[index].accName);
			bindBean.setAccOid(unBindInfo[index].accOid);
			bindBean.setDeviceId(unBindInfo[index].deviceId);
			bindBean.setUserLine(unBindInfo[index].userline);
			bindBean.setUserId(unBindInfo[index].userId);
			bindBeanArr[index] = bindBean;
		}
		bindInfoMessage.setBindBeanArr(bindBeanArr);
		
		ResultInfo resultInfo = new ResultInfo();
		try{
			Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
		}
		catch(Exception e)
		{
			try
			{
				Global.RESOURCE_BIND_PUBLISHER = new MQPublisher("res.Interface",Global.MQ_POOL_PUBLISHER_MAP);
				Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
			}
			catch(Exception ex)
			{
				logger.error("reUnbind ResourceBind Error.\n{}", ex);
				return null;			
			}
		}
		resultInfo.resultId = new String[1];
		resultInfo.resultId[0] = "1";
		resultInfo.status = "1";
		logger.warn("unbind: {}-{}",new Object[]{resultInfo.status,resultInfo.resultId});
		return resultInfo;
	}

	@Override
	public ResultInfo bind(BindInfo[] bindInfo, String clientId)
	{
		
		if(null == bindInfo || bindInfo.length == 0){
			logger.error("BindInfo[] bindInfo is null");
			return null;
		}
		
		bindInfoMessage = new BindInfoMessage();
		bindInfoMessage.setMethodName("bind");
		bindInfoMessage.setClientId(clientId);
		bindInfoMessage.setPriority("0");
		BindBean[] bindBeanArr = new BindBean[5];
		BindBean bindBean = new BindBean();
		for(int index=0;index < bindInfo.length;index++){
			bindBean.setAccName(bindInfo[index].accName);
			bindBean.setAccOid(bindInfo[index].accOid);
			bindBean.setDeviceId(bindInfo[index].deviceId);
			bindBean.setUserLine(bindInfo[index].userline);
			bindBean.setUserName(bindInfo[index].username);
			bindBeanArr[index] = bindBean;
		}
		bindInfoMessage.setBindBeanArr(bindBeanArr);
		
		ResultInfo resultInfo = new ResultInfo();
		try{
			Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
		}
		catch(Exception e)
		{
			try
			{
				Global.RESOURCE_BIND_PUBLISHER = new MQPublisher(topic,Global.MQ_POOL_PUBLISHER_MAP);
				Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
			}
			catch(Exception ex)
			{
				logger.error("rebind ResourceBind Error.\n{}", ex);
				return null;			
			}
		}
		resultInfo.resultId = new String[1];
		resultInfo.resultId[0] = "1";
		resultInfo.status = "1";
		logger.warn("bind: {}-{}",new Object[]{resultInfo.status,resultInfo.resultId});
		return resultInfo;
	}
	@Override
	public ResultInfo release(UnBindInfo[] unBindInfo, String clientId)
	{
		if(null == unBindInfo || unBindInfo.length == 0){
			logger.error("UnBindInfo[] unBindInfo is null");
			return null;
		}
		
		bindInfoMessage = new BindInfoMessage();
		bindInfoMessage.setMethodName("unBind");
		bindInfoMessage.setClientId(clientId);
		bindInfoMessage.setPriority("0");
		BindBean[] bindBeanArr = new BindBean[5];
		BindBean bindBean = new BindBean();
		for(int index=0;index < unBindInfo.length;index++){
			bindBean.setAccName(unBindInfo[index].accName);
			bindBean.setAccOid(unBindInfo[index].accOid);
			bindBean.setDeviceId(unBindInfo[index].deviceId);
			bindBean.setUserLine(unBindInfo[index].userline);
			bindBean.setUserId(unBindInfo[index].userId);
			bindBeanArr[index] = bindBean;
		}
		bindInfoMessage.setBindBeanArr(bindBeanArr);
		
		ResultInfo resultInfo = new ResultInfo();
		try{
			logger.warn("sss"+Global.RESOURCE_BIND_PUBLISHER);
			Global.RESOURCE_BIND_PUBLISHER.publishMQ(topic, bindInfoMessage);
			logger.warn("sss"+topic);
		}
		catch(Exception e)
		{
			try
			{
				Global.RESOURCE_BIND_PUBLISHER = new MQPublisher(topic,Global.MQ_POOL_PUBLISHER_MAP);
				Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
			}
			catch(Exception ex)
			{
				logger.error("reUnbind ResourceBind Error.\n{}", ex);
				return null;			
			}
		}
		resultInfo.resultId = new String[1];
		resultInfo.resultId[0] = "1";
		resultInfo.status = "1";
		logger.warn("unbind: {}-{}",new Object[]{resultInfo.status,resultInfo.resultId});
		return resultInfo;
	}
	
	
	/**
	 * 更新内存中的用户信息
	 * 
	 * @param userName 用户帐号
	 * @return 更新结果
	 */
	public ResultInfo updateUser(String userName){
		return doTest("user","set", userName);
	}
	
	/**
	 * 更新内存中的设备信息
	 * 
	 * @param device_id 设备ID
	 * @return 更新结果
	 */
	public ResultInfo delDevice(String device_id){
		return doTest("device","del", device_id);
	}
	
	/**
	 * 更新内存中的设备信息
	 * 
	 * @param userName 设备ID
	 * @return 更新结果
	 */
	public ResultInfo updateDevice(String deviceid){
		return doTest("device","set", deviceid);
	}
	
	/**
	 * 调用资源绑定模块
	 * 
	 * @param type user\device   user：用户数据，device：设备数据
	 * @param operate set\get\add\del  set：更新，get：获取，add：增加，del：删除
	 * @param parameter 待操作的具体参数  username或者device_id
	 * @return 操作结果
	 */
	public ResultInfo doTest(String type,String operate,String parameter){
		logger.debug("doTest(" + type + "," + operate + "," + parameter + ")");
		bindInfoMessage = new BindInfoMessage();
		bindInfoMessage.setMethodName("test");
		bindInfoMessage.setClientId("ItmsService");
		bindInfoMessage.setPriority("0");
		bindInfoMessage.setType(type);
		bindInfoMessage.setOperate(operate);
		bindInfoMessage.setParameter(parameter);
		
		ResultInfo resultInfo = new ResultInfo();
		try{
			Global.RESOURCE_BIND_PUBLISHER.publishMQ(topic, bindInfoMessage);
		}
		catch(Exception e)
		{
			try
			{
				Global.RESOURCE_BIND_PUBLISHER = new MQPublisher(topic,Global.MQ_POOL_PUBLISHER_MAP);
				Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
			}
			catch(Exception ex)
			{
				logger.error("doTest ResourceBind Error.\n{}", ex);
				return null;			
			}
		}
		resultInfo.resultId = new String[1];
		resultInfo.resultId[0] = "1";
		resultInfo.status = "1";
		logger.warn("doTest: {}-{}",new Object[]{resultInfo.status,resultInfo.resultId});
		return resultInfo;
	}
	
	
	public ResultInfo deleteUser(String username){
		
		bindInfoMessage = new BindInfoMessage();
		bindInfoMessage.setMethodName("userDelete");
		bindInfoMessage.setClientId("ItmsService");
		bindInfoMessage.setPriority("0");
		bindInfoMessage.setUserName(username);
		
		ResultInfo resultInfo = new ResultInfo();
		try{
			Global.RESOURCE_BIND_PUBLISHER.publishMQ(topic, bindInfoMessage);
		}
		catch(Exception e)
		{
			try
			{
				Global.RESOURCE_BIND_PUBLISHER = new MQPublisher(topic,Global.MQ_POOL_PUBLISHER_MAP);
				Global.RESOURCE_BIND_PUBLISHER.publishMQ(bindInfoMessage);
			}
			catch(Exception ex)
			{
				logger.error("userDelete ResourceBind Error.\n{}", ex);
				return null;			
			}
		}
		resultInfo.resultId = new String[1];
		resultInfo.resultId[0] = "1";
		resultInfo.status = "1";
		logger.warn("userDelete: {}-{}",new Object[]{resultInfo.status,resultInfo.resultId});
		return resultInfo;
	}
	
}
