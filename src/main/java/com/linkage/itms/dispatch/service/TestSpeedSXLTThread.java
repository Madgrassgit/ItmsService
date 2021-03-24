package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.TestSpeedSXLTChecker;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * fanjm 山西测速 ，改成异步
 * @author HP (AILK No.)
 * @version 1.0
 * @since 2020-4-16
 * @category com.linkage.itms.dispatch.service
 * @copyright AILK NBS-Network Mgt. RD Dept.
 */
public class TestSpeedSXLTThread implements Runnable{

	private static Logger logger = LoggerFactory.getLogger(TestSpeedSXLTThread.class);
	private TestSpeedSXLTChecker checker;
	private long id;
	private String deviceId = null;
	
	
	public TestSpeedSXLTThread(TestSpeedSXLTChecker checker, long id, String deviceId)
	{
		super();
		this.checker = checker;
		this.id = id;
		this.deviceId = deviceId;
	}


	@Override
	public void run(){
		ACSCorba acsCorba = new ACSCorba();
		// 获取wan通道
		Map<String,String> wanPassageWayMap = gatherWanPassageWay(deviceId,acsCorba);
		
		if(null != wanPassageWayMap && !wanPassageWayMap.isEmpty()){
			String wanPassageWay = null;
			for(String key: wanPassageWayMap.keySet()){
				
				if(key.startsWith("INTERNET")){
					if(!StringUtil.IsEmpty(checker.getVlanId())){
						if(checker.getVlanId().equals(key.split("###")[1])){
							wanPassageWay = wanPassageWayMap.get(key);
							break;
						}
					}else{
						
						// 如果没有多宽带，取第一个
						wanPassageWay = wanPassageWayMap.get(key);
						break;
					}
				}	
			}
			
			logger.warn("wanPassageWay="+wanPassageWay);
			
			ArrayList<ParameValueOBJ> parameList = new ArrayList<ParameValueOBJ>();
			ParameValueOBJ obj1 = new ParameValueOBJ();
			obj1.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testMode");
			obj1.setValue(checker.getSpeedTest_testMode());
			obj1.setType("1");
			
			ParameValueOBJ obj2 = new ParameValueOBJ();
			obj2.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testURL");
			obj2.setValue(checker.getSpeedTest_testURL());
			obj2.setType("1");
			
			ParameValueOBJ obj3 = new ParameValueOBJ();
			obj3.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.WANInterface");
			obj3.setValue(wanPassageWay);
			obj3.setType("1");
			
			ParameValueOBJ obj4 = new ParameValueOBJ();
			obj4.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.DiagnosticsState");
			obj4.setValue("Requested");
			obj4.setType("1");
			
			ParameValueOBJ obj5 = new ParameValueOBJ();
			obj5.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeName");
			obj5.setValue(checker.getUserInfo());
			obj5.setType("1");
			
			ParameValueOBJ obj6 = new ParameValueOBJ();
			obj6.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupppoename");
			obj6.setValue(checker.getEupppoename());
			obj6.setType("1");
			
			ParameValueOBJ obj7 = new ParameValueOBJ();
			obj7.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupassword");
			obj7.setValue(checker.getEupassword());
			obj7.setType("1");
			
			ParameValueOBJ obj8 = new ParameValueOBJ();
			obj8.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.reportURL");
			obj8.setValue(checker.getSpeedTest_reportURL());
			obj8.setType("1");
			
			ParameValueOBJ obj9 = new ParameValueOBJ();
			obj9.setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.backgroundsize");
			obj9.setValue(checker.getBackgroundsize());
			obj9.setType("1");
			
			parameList.add(obj1);
			parameList.add(obj2);
			parameList.add(obj3);
			parameList.add(obj4);
			parameList.add(obj5);
			parameList.add(obj6);
			parameList.add(obj7);
			parameList.add(obj8);
			parameList.add(obj9);
			
			int result = acsCorba.setValue(deviceId, parameList);
			logger.warn("[{}]测速下发完毕，result=[{}]",id,result);
			if (0 == result || 1 == result)
			{
				checker.setResult(0);
				checker.setResultDesc("成功");
			}
			else{
				checker.setResult(1000);
				checker.setResultDesc("下发测速错误");
			}
		}
		else{
			checker.setResult(1000);
			checker.setResultDesc("获取不到wan通道");
		}
		
		new RecordLogDAO().recordDispatchLog(checker,id,"");
	}
	
	
	
	/**
	 * 获取测速路径
	 * 
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> gatherWanPassageWay(String deviceId,ACSCorba corba) {
		String SERV_LIST_INTERNET = "INTERNET";
		Map<String, String> restMap = new HashMap<String, String>();
		
		// logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CU_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		String wanVlan = ".X_CU_VLAN";
		String connectionType = ".ConnectionType";

		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		 ArrayList<String> wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		
		if (null == wanConnPathsList  || wanConnPathsList.isEmpty()) {
			logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",deviceId);
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.isEmpty()) {
				logger.warn("[TestSpeedSXLTService] [{}]获取" + wanConnPath + "下实例号失败，返回", deviceId);
			}else{
				for (String j : jList) {
					wanConnPathsList.add(wanConnPath + j + wanVlan);
					
					// 获取session，
					List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j + wanPPPConnection);
					if (null == kPPPList || kPPPList.isEmpty()) {
						logger.warn("[TestSpeedSXLTService] [{}]获取" + wanConnPath + wanConnPath + j + wanPPPConnection + "下实例号失败",deviceId);
					} else {
						for (String kppp : kPPPList) {
							wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp + wanServiceList);
							wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp + connectionType);
						}
					}
				}
			}	
		}
		
		if(null != wanConnPathsList && !wanConnPathsList.isEmpty()){
			
			List<String> tempWanConnPathsList = new ArrayList<String>();
			for(String wanConnPaths : wanConnPathsList){
				if(wanConnPaths.endsWith(".X_CU_ServiceList") || wanConnPaths.endsWith(".X_CU_VLAN")
						|| wanConnPaths.endsWith(".ConnectionType")){
					tempWanConnPathsList.add(wanConnPaths);
				}
			}
			
			String[] paramNameArr = new String[tempWanConnPathsList.size()];
			for(int index=0;index<tempWanConnPathsList.size();index++){
				paramNameArr[index] = tempWanConnPathsList.get(index);
			}
			
			Map<String, String> paramValueMap = corba.getParaValueMap(deviceId,paramNameArr);
			if (null == paramValueMap || paramValueMap.isEmpty()) {
				logger.warn("[TestSpeedSXLTService] [{}]获取ServiceList失败",deviceId);
			}else{
				for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
					logger.debug("[{}]{}={} ",new Object[]{deviceId, entry.getKey(), entry.getValue()});
					String paramName = entry.getKey();
					if (paramName.endsWith(wanServiceList)) {
						if (!StringUtil.IsEmpty(entry.getValue())) {
							String res = entry.getKey().substring(0,entry.getKey().indexOf(wanServiceList));
							String vlanKey = "";
							String vlanValue = "";
							String conTypeKey = "";
							String conTypeValue = "";
							
							if(entry.getKey().indexOf(wanPPPConnection) > 0){
								vlanKey = entry.getKey().substring(0, entry.getKey().indexOf(wanPPPConnection)) + wanVlan;
								vlanValue = paramValueMap.get(vlanKey);
								conTypeKey = entry.getKey().substring(0, entry.getKey().indexOf(wanServiceList)) + connectionType;
								conTypeValue = paramValueMap.get(conTypeKey);
							}else{
								vlanKey = entry.getKey().substring(0, entry.getKey().indexOf(wanIPConnection)) + wanVlan;
								vlanValue = paramValueMap.get(vlanKey);
								conTypeKey = entry.getKey().substring(0, entry.getKey().indexOf(wanServiceList)) + connectionType;
								conTypeValue = paramValueMap.get(conTypeKey);
							}
							
							if (entry.getValue().indexOf(SERV_LIST_INTERNET) >= 0) {
								restMap.put(SERV_LIST_INTERNET+"###"+vlanValue+"###"+conTypeValue, res);
							}
						}
					}
				}
			}
		}
		return restMap;
	}

}
