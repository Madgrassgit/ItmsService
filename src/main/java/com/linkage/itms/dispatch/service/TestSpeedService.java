package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.TestSpeedChecker;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * hgu单个用户测速接口 Service
 * @author fanjm 35572
 * @version 1.0
 * @since 2016年11月29日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class TestSpeedService implements IService{

	private static Logger logger = LoggerFactory.getLogger(TestSpeedService.class);
	@Override
	public String work(String inParam) {
		logger.warn("TestSpeedService==>inParam({})",inParam);

		// 解析获得入参
		TestSpeedChecker checker = new TestSpeedChecker(inParam.trim());
		
		// 验证入参
		if (false == checker.check()) {
			logger.warn("入参验证没通过,TestSpeedService==>inParam({})",inParam);
			
			logger.warn("work==>inParam="+checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		
		// 查询用户设备信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		Map<String,String> userDevInfo = userDevDao.qryUserSDTestSpeed(checker.getUserInfoType(), checker.getUserInfo());
		
		if (null == userDevInfo || userDevInfo.isEmpty()) {
			logger.warn(
					"servicename[TestSpeedService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1001);
			checker.setResultDesc("无此客户信息");
		} 
		else{
			String deviceId = userDevInfo.get("device_id");
			
			if (StringUtil.IsEmpty(deviceId)) {
				// 未绑定设备
				logger.warn(
						"servicename[TestSpeedService]cmdId[{}]userinfo[{}]此客户未绑定",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1002);
				checker.setResultDesc("此用户未绑定设备");
			}
			else {
				//userDevDao.insertTestSpeedDev(userDevInfo);
				int status=doTest(userDevInfo,checker.getUserInfo(),checker);
				if(status!=1 && status!=0)
				{
					checker.setResult(status);
					checker.setResultDesc("失败");
				}else{
					checker.setResult(0);
					checker.setResultDesc("成功");
				}
				
			}
		}
		
		String returnXml = checker.getReturnXml();
		logger.warn(
				"servicename[TestSpeedService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
	
		return returnXml;
		
	}

	
	
	public int doTest(Map<String,String> userDevInfo,String userName, TestSpeedChecker checker){
		// 查询用户设备信息
		UserDeviceDAO dao = new UserDeviceDAO();
		String[] testArr = {
			"InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testMode",
			"InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testURL",
			"InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.reportURL",
			"InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeName",
			"InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupppoename",
			"InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupassword",
			"InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.WANInterface",
			"InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.DiagnosticsState"};
		String cityId=StringUtil.getStringValue(userDevInfo, "city_id");
		String deviceId=StringUtil.getStringValue(userDevInfo, "device_id");
		//String seriresNumber=StringUtil.getStringValue(userDevInfo, "device_serialnumber");
		long userId=StringUtil.getLongValue(userDevInfo, "user_id");
		int servTypeId=StringUtil.getIntValue(userDevInfo,"serv_type_id",10);
		if (StringUtil.IsEmpty(deviceId)) {
			checker.setResult(-2);
			checker.setResultDesc("设备不在线");
			logger.warn("deviceId is null.");
			return -1;
		}
		ACSCorba acsCorba = new ACSCorba();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId,
				acsCorba);
		int status = 1;
		if(flag==1){
			logger.warn("[{}]start to testSpeed...", deviceId);
			String rate = dao.getRateFromUserId(userId,servTypeId,userName);
			logger.warn("[{}]根据用户id[{}]、测试类型[{}]、业务账号[{}]查询速率结果[{}]", new Object[]{deviceId,userId,servTypeId,userName,rate});
			Map<String,String> netAccount = dao.getTestAccount(rate,cityId);//new HashMap<String,String>();//
			String netUsername = "";//StringUtil.getStringValue(netAccount, "username");
			String netPassword = "";//StringUtil.getStringValue(netAccount, "password");
			if(netAccount != null && netAccount.size() > 0){
				netUsername = StringUtil.getStringValue(netAccount, "net_account");
				netPassword = StringUtil.getStringValue(netAccount, "net_password");
				
				String wanWay = "";
				int result = -1;
				logger.warn("[{}]开始获取WAN通道信息", new Object[]{deviceId});
				//获得WAN通道 顺便取出IP节点
				Map<String,String> wanConnDeviceMap = this.gatherWanPassageWay(deviceId,acsCorba);
				if(wanConnDeviceMap == null || wanConnDeviceMap.isEmpty())
				{
					logger.warn("[{}]没有获取到WAN通道信息，结束线程",deviceId);
					status = -1;
				}
				else//成功获取WAN通道信息
				{
					logger.warn("[{}]成功获取到WAN通道信息，结束线程",deviceId);
					String servList = "INTERNET";
					if(servTypeId != 10){
						servList = "IPTV";
					}
					for(String key: wanConnDeviceMap.keySet())
					{
						logger.warn("key:" +key);
						logger.warn("VALUE:" +wanConnDeviceMap.get(key));
						if(key.startsWith(servList))
						{
							wanWay = wanConnDeviceMap.get(key);
						}
					}
					if("".equals(wanWay)){
						logger.warn("deviceId:"+ deviceId +"没有获取到宽带或iptvWAN通道!");
						return -1;
					}
					logger.warn("deviceId:"+ deviceId +"获取到WAN通道信息成功" + wanWay + "，开始下发测速！");
					
					String[] testValue = new String[8];
					testValue[0] = "serverMode";
					testValue[1] = Global.testURL;
					testValue[2] = Global.reportURL;
					testValue[3] = userName;
					testValue[4] = netUsername;
					testValue[5] = netPassword;
					testValue[6] = wanWay;
					testValue[7] = "Requested";
					
					ArrayList<ParameValueOBJ> testList = new ArrayList<ParameValueOBJ>();
					for(int i = 0; i < testArr.length; i++){
						ParameValueOBJ pvObj = new ParameValueOBJ();
						pvObj.setName(testArr[i]);
						pvObj.setType("1");
						pvObj.setValue(testValue[i]);
						testList.add(pvObj);
					}
					result = acsCorba.setValue(deviceId, testList);
				}
				if(result != 1 && result != 0){
					logger.warn("[{}]设备发起测速失败[{}]", deviceId, result);
					status = result;
				}else{
					logger.warn("[{}]设备发起测速成功[{}]", deviceId, result);
				}
				
			}else{
				logger.warn("[{}]未获取到测速账号和密码，结束", deviceId);
				status = -3;
			}
			//dao.updateStatus(status, deviceId,0,taskId,servTypeId);
		}
		else{
			logger.warn("device is not online");
			status=-1;
		}
		return status;
	}
	
	
	/**
	 * 获取测速路径
	 * 
	 * @param deviceId
	 * @return
	 */
	private Map<String, String> gatherWanPassageWay(String deviceId,ACSCorba corba) {
		String SERV_LIST_INTERNET = "INTERNET";
		String SERV_LIST_TR069 = "TR069";
		String SERV_LIST_VOIP = "VOIP";
		String SERV_LIST_IPTV = "IPTV";
		String SERV_LIST_OTHER = "OTHER";
		Map<String, String> restMap = new HashMap<String, String>();
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CU_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";

		ArrayList<String> wanConnPathsList = new ArrayList<String>();
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		logger.warn("wanConnPathsList.size:{},wanConnPath:{},deviceId{}",wanConnPathsList.size(),wanConnPath,deviceId);
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty()) {
			logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",
					deviceId);
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty()) {
				logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
						+ "下实例号失败，返回", deviceId);
			}
			for (String j : jList) {
				// 获取session，
				List<String> kPPPList = corba.getIList(deviceId, wanConnPath
						+ j + wanPPPConnection);
				if (null == kPPPList || kPPPList.size() == 0
						|| kPPPList.isEmpty()) {
					logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
							+ wanConnPath + j + wanPPPConnection + "下实例号失败",
							deviceId);
				} else {
					for (String kppp : kPPPList) {
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection
								+ kppp + wanServiceList);
					}
				}
			}
		}
		// serviceList节点
		ArrayList<String> serviceListList = new ArrayList<String>();
		// 所有需要采集的节点
		ArrayList<String> paramNameList = new ArrayList<String>();
		for (int i = 0; i < wanConnPathsList.size(); i++) {
			String namepath = wanConnPathsList.get(i);
			if (namepath.indexOf(wanServiceList) >= 0) {
				serviceListList.add(namepath);
				paramNameList.add(namepath);
				continue;
			}
		}
		if (serviceListList.size() == 0 || serviceListList.isEmpty()) {
			logger.warn(
					"[TestSpeedService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回",
					deviceId);
		} else {
			String[] paramNameArr = new String[paramNameList.size()];
			int arri = 0;
			for (String paramName : paramNameList) {
				paramNameArr[arri] = paramName;
				arri = arri + 1;
			}
			Map<String, String> paramValueMap = new HashMap<String, String>();
			for (int k = 0; k < (paramNameArr.length / 20) + 1; k++) {
				String[] paramNametemp = new String[paramNameArr.length
						- (k * 20) > 20 ? 20 : paramNameArr.length - (k * 20)];
				for (int m = 0; m < paramNametemp.length; m++) {
					paramNametemp[m] = paramNameArr[k * 20 + m];
				}
				Map<String, String> maptemp = corba.getParaValueMap(deviceId,
						paramNametemp);
				if (maptemp != null && !maptemp.isEmpty()) {
					paramValueMap.putAll(maptemp);
				}
			}
			if (paramValueMap.isEmpty()) {
				logger.warn("[TestSpeedService] [{}]获取ServiceList失败",
						deviceId);
			}
			for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
				logger.debug(
						"[{}]{}={} ",
						new Object[]{deviceId, entry.getKey(), entry.getValue()});
				String paramName = entry.getKey();
				if (paramName.indexOf(wanPPPConnection) >= 0) {
				} else if (paramName.indexOf(wanIPConnection) >= 0) {
					continue;
				}
				if (paramName.indexOf(wanServiceList) >= 0) {
					if (!StringUtil.IsEmpty(entry.getValue())) {// X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径
						String res = entry.getKey().substring(0,
								entry.getKey().indexOf(wanServiceList));
						if (entry.getValue().indexOf(SERV_LIST_INTERNET) >= 0) {
							restMap.put(SERV_LIST_INTERNET, res);
						} else if (entry.getValue().indexOf(SERV_LIST_VOIP) >= 0) {
							restMap.put(SERV_LIST_VOIP, res);
						} else if (entry.getValue().indexOf(SERV_LIST_IPTV) >= 0) {
							restMap.put(SERV_LIST_IPTV, res);
						} else if (entry.getValue().indexOf(SERV_LIST_TR069) >= 0) {
							restMap.put(SERV_LIST_TR069, res);
						} else if (entry.getValue().indexOf(SERV_LIST_OTHER) >= 0) {
							restMap.put(SERV_LIST_OTHER, res);
						}
					}
				}
			}
		}
		return restMap;
	}
	
	

}
