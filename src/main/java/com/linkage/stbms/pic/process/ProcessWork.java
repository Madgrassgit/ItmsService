package com.linkage.stbms.pic.process;

import ACS.DevRpc;
import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.pic.Global;
import com.linkage.stbms.pic.Shutdown;
import com.linkage.stbms.pic.dao.DeviceInformDao;
import com.linkage.stbms.pic.dao.LogoConDao;
import com.linkage.stbms.pic.object.*;
import com.linkage.stbms.pic.util.DBUtil;
import com.linkage.stbms.pic.util.SqlUtil;
import com.linkage.stbms.pic.util.StrUtil;
import com.linkage.stbms.pic.util.XmlUtil;
import com.linkage.stbms.pic.util.corba.RPCManagerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class ProcessWork implements Runnable, Shutdown {

	final Logger logger = LoggerFactory.getLogger(ProcessWork.class);
	
	/** 唯一ID */
	private String id = null;
	
	/** 此线程是否正在运行 */
	private boolean isRunning = true;
	
	private String deviceId = null;
	private String serviceId = null;
	/** 大业务ID，1表示iTV,5表示软件升级(因只一条策略，故和serviceId保持一致) */
//	private String tempId = null;
//	private String orderId = null;
	
	/** 策略ID */
	private String strategyId = null;
	
	
	/** 设备表中相关信息（调用ACS用）*/
//	private String device_serialnumber;
//	private String devicetype_id;
//	private String oui;

	/** 工单参数(XML格式) */
	private String sheetParamXml = null;
	
	/** 任务ID */
	private String taskId = null;
	
	/** 1：老工单 2：新工单（模板组合，比如QoS） */
	private String sheetType = "1";
	
	/** 1：优先级最高 2：优先级低 3：优先级更低...*/
//	private int priority = 1;
	
	/** 策略对应的用户账号 */
//	private String username = null;
	
	/** 只保留def_value中当前操作的前一条 */
	String preDefValue = null;
	/** 只保留def_value中当前操作的前一条，点后最后一位 */
	String lastDefValue = null;
	
	private StbSoftObj stbSoftObj = null;
	
	private STBZreoObj stbZreoObj = null;
	
	private RPCManagerClient acsCorba = new RPCManagerClient();  
	
	private int faultCode = 0;
	
	private String startTime = "";
	private String endTime = "";
	private int execCount = 0;
	
	private DeviceInformDao deviceInformDao = new DeviceInformDao();
	
	private ParameterListObj parameterListObj = null;
	
	private LogoObj logoObj = null;
	
	/**
	 * 设置StrategyId
	 * @author gongsj
	 * @date 2009-7-20
	 * @param strategyId
	 */
	public void setStrategyId(String strategyId) {
		this.strategyId = strategyId;
	}

	public String getId() {
		return id;
	}

	public ProcessWork() {}
	
	/**
	 * 构造函数
	 * 
	 * @param id
	 */
	public ProcessWork(String id) {
		this.id = id;
	}

	/**
	 * RUN
	 */
	public void run() {
		if (!isRunning) {
			return;
		}
		try {
			work();
		} catch (Exception ex) {
			logger.error("同步工作线程出错:{}", ex.getStackTrace());
			ex.printStackTrace();
		}
		finally
		{
			shutdown(0);
		}
	}

	/**
	 * WORK
	 * @author gongsj
	 * @date 2009-7-20
	 */
	public DevRpc[] work() {
		if (strategyId == null || "".equals(strategyId)) {
			logger.warn("strategyId为空，线程退出。");
			return null;
		}
		
		DevRpc[] devRpcs = threadProcess();
		updateDB();
		return devRpcs;
		/** 更新策略、停止线程、清理资源 */
//		threadShutDown();
	}

	/**
	 * 预处理操作内容
	 * @param deviceId
	 * @param seviceId
	 */
	private DevRpc[]  threadProcess() {
		logger.warn("开始预处理：strategyId={}", strategyId);
		startTime = String.valueOf(new Date().getTime() / 1000);
		
		/**
		 * 查询到策略信息
		 * 获得deviceId, serviceId, taskId, sheetType, sheetParamXml, sheetParams
		 */
		if (!getPolicy(strategyId)) {
			return null;
		}
		logger.warn("device_id={},service_id={},sheet_type={}", new Object[]{deviceId, serviceId, sheetType});
		
		//针对当前的service_id，进行业务操作
		DevRpc[] devRpcs = doBusiness(serviceId);
		logger.warn("[{}]执行业务结束", deviceId);
		return devRpcs;
	}
	
	/**
	 * 查询到策略信息
	 * @author gongsj
	 * @date 2009-7-20
	 * @param strategyId
	 * @return
	 */
	private boolean getPolicy(String strategyId) {
		Map<String, String> policy = DBUtil.getPolicy(strategyId);
		if (null == policy) {
			logger.warn("策略[{}]信息错误（原因：1)策略信息有误 2)策略ID不存在 3)该策略已执行过）", strategyId);
			faultCode = -7;
			return false;
		}
		
		String param = null;
		
		deviceId = policy.get("device_id");
		serviceId = policy.get("service_id");
		taskId = policy.get("task_id");
		sheetType = policy.get("sheet_type");
//		username = policy.get("username");	
//		tempId = policy.get("temp_id");
//		if (null == policy.get("priority") || "".equals(policy.get("priority"))) {
//			priority = 1;
//		} else {
//			priority = Integer.parseInt(policy.get("priority"));
//		}
//		orderId = policy.get("order_id");
		
		execCount = Integer.parseInt(policy.get("exec_count"))+1;
		
		if (null == deviceId || "".equals(deviceId)) {
			logger.warn("策略[{}]信息错误（原因：设备ID为空）", strategyId);
			faultCode = -9;
			return false;
		}
		if (null == serviceId || "".equals(serviceId)) {
			logger.warn("策略[{}]信息错误（原因：业务ID为空）", strategyId);
			faultCode = -9;
			return false;
		}
		// 查询设备信息(设备表中各信息及ior)
		if (!getDevInfo(deviceId)) {
			logger.warn("[{}]获得设备信息失败", deviceId);
			return false;
		}
		
		if (Global.NEW_SHEET_TYPE.equals(sheetType) 
				&& !serviceId.equals(Global.SERVICE_ID_SOFT)
				&& !serviceId.equals(Global.SERVICE_ID_STB_ZERO)
				&& !serviceId.equals(Global.SERVICE_ID_STB_BATCH_CON)
				&& !serviceId.equals(Global.SERVICE_ID_STB_LOGO_CON)) {
			logger.warn("[{}][{}]业务类型类型未知，工单类型错误！", deviceId, serviceId);
			faultCode = -9;
			return false;
		}
		
		param = policy.get("sheet_para");
		
		if (Global.NEW_SHEET_TYPE.equals(sheetType)) {
			sheetParamXml = param;
			//先解析XML （sheetParamXml）
			parseSheetParamXml(deviceId, serviceId);
			return true;
			/*if(parseSheetParamXml(deviceId, serviceId)) {
				return true;
			} else {
				faultCode = -9;
				return false;
			}*/
		}else{
			logger.warn("[{}][{}]策略工单类型sheet_type错误！", deviceId, serviceId);
			faultCode = -9;
			return false;
		}
	}

	/**
	 * 查询设备信息
	 * 
	 * @param deviceId
	 * @return
	 */
	public boolean getDevInfo(String deviceId) {
		/******************************设备信息*************************************/
		Map<String, String> devInfoMap = DBUtil.getDevInfo(deviceId);
		if (null == devInfoMap) {
			logger.warn("[{}]设备不存在，请确实是否异常！", deviceId);
			faultCode = -7;
			return false;
		}
//		oui = devInfoMap.get("oui");
//		device_serialnumber = (String) devInfoMap.get("device_serialnumber");
//		devicetype_id = devInfoMap.get("devicetype_id");

		devInfoMap = null;
		return true;
	}

	/**
	 * /针对当前的service_id，进行业务操作
	 * @param serviceId
	 */
	private DevRpc[] doBusiness(String serviceId) {
		logger.warn("[{}]开始执行业务，业务ID：{}", deviceId, serviceId);
		DevRpc[] devRpcs = null;
		// 业务下发
		logger.warn("[{}]***********业务下发***********", deviceId);
		if (!DBUtil.updateStrategy(strategyId, "4", "2", "", startTime, "" ,-1)) {
			logger.warn("[{}]下发工单时，更新数据库失败，程序结束...", deviceId);
			faultCode = -9;
			return null;
		}
	if (Global.SERVICE_ID_STB_LOGO_CON.equals(serviceId))
		{
			logger.warn("[{}]机顶盒运营画面升级下发", deviceId);
			devRpcs = setPicSoft();
			faultCode = 1;
			if (1 == faultCode)
			{
				logger.warn("[{}]机顶盒运营画面升级成功", deviceId);
			}
			else
			{
				logger.warn("[{}]机顶盒运营画面升级失败", deviceId);
			}
			LogoConDao dao = new LogoConDao();
//			dao.updateRecord(StrUtil.getLongValue(strategyId), 1,deviceId);
			dao.updateRecord(StrUtil.getLongValue(taskId), deviceId, StrUtil.getLongValue(strategyId), 1);
		} 
		return devRpcs;
	}
	
	private void doNextStragegy(int result) {
		logger.debug("doNextStragegy()");
		
		endTime = String.valueOf(new Date().getTime()/1000);
		StrategyObj nextStrategyObj = null;

		if(execCount<3 && 1 != result){
			DBUtil.updateStrategy(strategyId, "0", StringUtil
					.getStringValue(result), "4", startTime, endTime,execCount);
		}else{
			DBUtil.updateStrategy(strategyId, "100", StringUtil
					.getStringValue(result), "4", startTime, endTime,execCount);
//			if(Global.JX_PROVINCE.equals(Global.PROVINCE)&&Global.SERVICE_ID_STB_BATCH_CON.equals(serviceId)){//江西机顶盒批量配置
//				BatchConBio bio = new BatchConBio();
//				nextStrategyObj = bio.getBatchConStrategy(deviceId);
//			}
		}
	}

	/**
	 * 
	 * @param deviceId
	 * @return
	 */
	private StrategyObj getOtherStrategy(String deviceId) {
		logger.debug("getOtherStrategy({})", deviceId);
	
		StrategyObj obj = null;
	
		obj = SqlUtil.getOtherStrategy(deviceId);
	
		return obj;
	}
	
	/**
	 * 常用：根据参数名获取值
	 * @param paramName
	 * @return
	 */
	private String getParamValueString(String paramName) {
		
		String[] paramNames = new String[] { paramName };
		Map<String, String> paraValues = acsCorba.getParaValueMap(paramNames,deviceId);
		
		if (null == paraValues || 0 == paraValues.size()) {
			return null;
		}
		
		return paraValues.get(paramName);
	}
	
	/**
	 * 实时设置机顶盒运营画面升级路径
	 * @author gongsj
	 * @date 2010-11-9
	 * @return
	 */
//	private int setBatchConfig()
//	{
//		int size = parameterListObj.getParameterList().size();
//		String[] paramNames = new String[size];
//		String[] paramValues = new String[size];
//		String[] paramTypeIds = new String[size];
//		for (int i = 0; i < size; i++)
//		{
//			ParameterObj obj = parameterListObj.getParameterList().get(i);
//			paramNames[i] = obj.getName();
//			paramValues[i] = obj.getValue();
//			paramTypeIds[i] = obj.getType();
//		}
//		return acsCorba.realSetParamsArrInt(paramNames, paramValues, paramTypeIds,
//				deviceId);
//	}
	
	private DevRpc[] setPicSoft() {
		ArrayList<String> paramValuelist = new ArrayList<String>();
		ArrayList<String> paramNamelist = new ArrayList<String>();
		if(1==logoObj.getIsSetStartPicURL()){
			paramValuelist.add(logoObj.getStartPicURL());
			paramNamelist.add("Device.UserInterface.Logo.X_CT-COM_StartPicURL");
		}
		if(1==logoObj.getIsSetBootPicURL()){
			paramValuelist.add(logoObj.getBootPicURL());
			paramNamelist.add("Device.UserInterface.Logo.X_CT-COM_BootPicURL");
		}
		if(1==logoObj.getIsSetAuthenticatePicURL()){
			paramValuelist.add(logoObj.getAuthenticatePicURL());
			paramNamelist.add("Device.UserInterface.Logo.X_CT-COM_AuthenticatePicURL");
		}
		if(paramNamelist.size()>0){
			String[] paramNames = new String[paramNamelist.size()];
			String[] paramValues = new String[paramNamelist.size()];
			String[] paramTypeIds = new String[paramNamelist.size()];
			
			for (int i = 0; i < paramNamelist.size(); i++)
			{
				paramNames[i] = paramNamelist.get(i);
				paramValues[i] = paramValuelist.get(i);
				paramTypeIds[i] = "1";
			}
			
			return acsCorba.realSetParamsArrInt(paramNames, paramValues, paramTypeIds, deviceId);
		}
		return null;
	}
	
	/**
	 * 实时设置软件升级路径
	 * @author gongsj
	 * @date 2010-11-9
	 * @return
	 */
//	private int realSetStbSoft() {
//		String[] paramNames = new String[1];
//		String[] paramValues = new String[1];
//		String[] paramTypeIds = new String[1];
//		
//		paramNames[0] = "Device.UserInterface.AutoUpdateServer";
//		paramValues[0] = stbSoftObj.getVersionPath();
//		paramTypeIds[0] = "1";
//		
//		return acsCorba.realSetParamsArrInt(paramNames, paramValues, paramTypeIds, deviceId);
//	}
//	
//	private int setZeroConfig() {
//		String[] paramNames = new String[1];
//		String[] paramValues = new String[1];
//		String[] paramTypeIds = new String[1];
//		
//		paramNames[0] = "Device.X_CTC_IPTV.ServiceInfo.AuthURL";
//		paramValues[0] = stbZreoObj.getAuthURL();
//		paramTypeIds[0] = "1";
//		
//		return acsCorba.realSetParamsArrInt(paramNames, paramValues, paramTypeIds, deviceId);
//	}
	
//	private int setSTBAccount()
//	{
//		ArrayList<String> paramValuelist = new ArrayList<String>();
//		ArrayList<String> paramNamelist = new ArrayList<String>();
//		if("PPPoE".equals(stbZreoObj.getAddressingType())){
//			paramValuelist.add(stbZreoObj.getAddressingType());
//			paramNamelist.add("Device.LAN.AddressingType");
//			
//			paramValuelist.add(stbZreoObj.getPPPoEID());
//			paramNamelist.add("Device.X_CTC_IPTV.ServiceInfo.PPPoEID");
//			
//			paramValuelist.add(stbZreoObj.getPPPoEPassword());
//			paramNamelist.add("Device.X_CTC_IPTV.ServiceInfo.PPPoEPassword");
//			
//			paramValuelist.add(stbZreoObj.getServAccount());
//			paramNamelist.add("Device.X_CTC_IPTV.ServiceInfo.UserID");
//			
//			paramValuelist.add(stbZreoObj.getServPassword());
//			paramNamelist.add("Device.X_CTC_IPTV.ServiceInfo.UserPassword");
//		}else if("Static".equals(stbZreoObj.getAddressingType())){
//			paramValuelist.add(stbZreoObj.getAddressingType());
//			paramNamelist.add("Device.LAN.AddressingType");
//			
//			paramValuelist.add(stbZreoObj.getIPAddress());
//			paramNamelist.add("Device.LAN.IPAddress");
//			
//			paramValuelist.add(stbZreoObj.getSubnetMask());
//			paramNamelist.add("Device.LAN.SubnetMask");
//			
//			paramValuelist.add(stbZreoObj.getDefaultGateway());
//			paramNamelist.add("Device.LAN.DefaultGateway");
//			
//			paramValuelist.add(stbZreoObj.getDNSServers());
//			paramNamelist.add("Device.LAN.DNSServers");
//			
//			paramValuelist.add(stbZreoObj.getServAccount());
//			paramNamelist.add("Device.X_CTC_IPTV.ServiceInfo.UserID");
//			
//			paramValuelist.add(stbZreoObj.getServPassword());
//			paramNamelist.add("Device.X_CTC_IPTV.ServiceInfo.UserPassword");
//		}else if("DHCP".equals(stbZreoObj.getAddressingType())){
//			paramValuelist.add(stbZreoObj.getAddressingType());
//			paramNamelist.add("Device.LAN.AddressingType");
//			
//			paramValuelist.add(stbZreoObj.getServAccount());
//			paramNamelist.add("Device.X_CTC_IPTV.ServiceInfo.UserID");
//			
//			paramValuelist.add(stbZreoObj.getServPassword());
//			paramNamelist.add("Device.X_CTC_IPTV.ServiceInfo.UserPassword");
//		}else{
//			logger.warn("[{}]机顶盒零配置未知的AddressingType", deviceId);
//		}
//		if(paramNamelist.size()>0){
//			String[] paramNames = new String[paramNamelist.size()];
//			String[] paramValues = new String[paramNamelist.size()];
//			String[] paramTypeIds = new String[paramNamelist.size()];
//			
//			for (int i = 0; i < paramNamelist.size(); i++)
//			{
//				paramNames[i] = paramNamelist.get(i);
//				paramValues[i] = paramValuelist.get(i);
//				paramTypeIds[i] = "1";
//			}
//			
//			return acsCorba.realSetParamsArrInt(paramNames, paramValues, paramTypeIds, deviceId);
//		}
//		return -9;
//	}
	
	private boolean parseSheetParamXml(String deviceId, String serviceId) {
		
		logger.warn("XML：{}", sheetParamXml);
		
		XmlUtil xu = new XmlUtil(sheetParamXml);
		xu.setDeviceId(deviceId);
		if(Global.SERVICE_ID_STB_LOGO_CON.equals(serviceId)){
			logger.warn("[{}]开始机顶盒运营画面升级", deviceId);
			logoObj = xu.generateLogoFromXml();
		}
		
		return true;
	}
	
	/**
	 * 停止线程
	 * @author gongsj
	 * @date 2010-9-19
	 */
	public void threadShutDown() {
		updateDB();
		shutdown(0);
	}
	
	/**
	 * 更新状态
	 * @author gongsj
	 * @date 2010-9-19
	 */
	private void updateDB() {
		updateStrategy();
	}
	
	/**
	 * 更新策略表
	 * @author gongsj
	 * @date 2010-9-19
	 */
	private void updateStrategy() {
		doNextStragegy(faultCode);
	}
	
	/**
	 * 关闭工程线程，如果线程正在working，必须等待线程结束才可以关闭
	 */
	public void shutdown(int cause) {
		
		logger.warn("[{}]停止线程并清理资源", deviceId);
		
		isRunning = false;
		
		/**
		 * 回收全部全局变量
		 */
		id = null;
		deviceId = null;
		serviceId = null;
		strategyId = null;
//		device_serialnumber = null;
//		devicetype_id = null;
//		oui = null;
		sheetParamXml = null;
//		taskId = null;
		preDefValue = null;
		lastDefValue = null;
		acsCorba = null;
	}

}
