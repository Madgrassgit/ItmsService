package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryObj;
import com.linkage.itms.dispatch.obj.QueryRtn;
import com.linkage.itms.dispatch.obj.QueryRtnMsg;

/**
 * 山西 RMS 终端查询服务
 * @author HP (AILK No.)
 * @version 1.0
 * @since 2020-4-15
 * @category com.linkage.itms.dispatch.service
 * @copyright AILK NBS-Network Mgt. RD Dept.
 */
public class QueryByPathService implements IService{

	private static Logger logger = LoggerFactory.getLogger(QueryByPathService.class);
	private long id = RecordLogDAO.getRandomId();
	private String oui_sn = "";
	private String deviceId = "";
	private String service_object = "";
	private String servListPathI = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
	//入参
	QueryObj inObj = new QueryObj();
	//回参
	QueryRtn rtn = new QueryRtn();
	//存放<业务类型, >
	Map<String,String> serviceMap = new HashMap<String,String>();
	@Override
	public String work(String inParam) {
		logger.warn("QueryByPathService==>inParam({})",inParam);
		new RecordLogDAO().recordLog(id, inParam, "deviceQueryByPath");
		try
		{
			inObj = JSONObject.parseObject(inParam, QueryObj.class);
		}
		catch (Exception e)
		{
			rtn.setRstCode("1");
			rtn.setRstDesc("service_obj入参非Json格式");
			return JSON.toJSONString(rtn);
		}
		
		check();
		//校验失败
		if(!"0".equals(rtn.getRstCode())){
			logger.warn("入参验证没通过,QueryByPathService==>inParam({})",inParam);
			return JSON.toJSONString(rtn);
		}
		logger.warn("入参验证通过");

		oui_sn = inObj.getOui_sn();
		service_object = inObj.getService_object();
		ArrayList<HashMap<String, String>> Infolist = new ArrayList<HashMap<String, String>>();
		// 查询用户设备信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		Infolist = userDevDao.qryIdbyOuiSN(oui_sn, service_object);
		
		if(null!=Infolist && Infolist.size()==1){
			deviceId = Infolist.get(0).get("device_id");
		}
		else{
			rtn.setRstCode("2");
			rtn.setRstDesc("查询不到设备/多个设备");
			return JSON.toJSONString(rtn);
		}
		
		logger.warn("deviceId="+deviceId);
		ACSCorba acsCorba = new ACSCorba();
		if("0".equals(inObj.getService_object())){
			acsCorba = new ACSCorba("1");
		}
		else{
			acsCorba = new ACSCorba("4");
		}
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
		logger.warn("[{}]在线状态="+flag, deviceId);
		if(flag==1){
			if("0".equals(inObj.getService_object())){
				preRead(acsCorba);
			}
			doGetValue(acsCorba);
		}
		else{
			rtn.setRstCode("3");
			rtn.setRstDesc("设备不在线");
		}
		
		/*logger.warn(
				"servicename[QueryByPathService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});*/
	
		return JSON.toJSONString(rtn);
	}
	

	/**
	 * 校验入参
	 */
	public void check(){
		serviceMap.put("TR069", "");
		serviceMap.put("INTERNET", "");
		serviceMap.put("IPTV", "");
		serviceMap.put("VOIP", "");
		serviceMap.put("VOICE", "");
		serviceMap.put("OTHER", "");
		serviceMap.put("NONE", "");
		serviceMap.put("STB", "");
		String oui_sn = inObj.getOui_sn();
		String service_obj = inObj.getService_object();
		if(StringUtil.IsEmpty(oui_sn) || !oui_sn.contains("-") || oui_sn.length()!=19){
			rtn.setRstDesc("oui_sn入参非法");
		}
		else if(StringUtil.IsEmpty(service_obj)){
			rtn.setRstDesc("service_obj入参非法");
		}
		else if(!"0".equals(service_obj) && !"1".equals(service_obj)){
			rtn.setRstDesc("service_obj入参非法");
		}
		else if(inObj.getService_name().size()==0){
			rtn.setRstDesc("service_name入参非法");
		}
		else if(inObj.getService_parameters().size()==0){
			rtn.setRstDesc("service_parameters入参非法");
		}
		for(ArrayList<String> paramOne:inObj.getService_parameters()){
			if(paramOne.size()==0){
				rtn.setRstDesc("service_parameters入参非法");
				break;
			}
		}
		for(String paramOne:inObj.getService_name()){
			if(!serviceMap.containsKey(paramOne)){
				rtn.setRstDesc("service_name入参非法");
				break;
			}
		}
		if(inObj.getService_parameters().size()!=inObj.getService_name().size()){
			rtn.setRstDesc("service_name与service_parameters数量不符");
		}
		
		if(StringUtil.IsEmpty(rtn.getRstDesc())){
			rtn.setRstCode("0");
		}
	}
	
	
	/**
	 * 预读ijk
	 * @param corba
	 */
	private void preRead(ACSCorba corba){
		//InternetGatewayDevice.WANDevice.1.WANConnectionDevice.2.WANPPPConnection.3.X_CU_ServiceList
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CU_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";

		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		 ArrayList<String> wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		
		if (null == wanConnPathsList  || wanConnPathsList.isEmpty()) {
			logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",deviceId);
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.isEmpty()) {
				logger.warn("[QueryByPathService] [{}]获取" + wanConnPath + "下实例号失败，返回", deviceId);
			}else{
				for (String j : jList) {
					// 获取session，
					List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j + wanPPPConnection);
					if (null == kPPPList || kPPPList.isEmpty()) {
						logger.warn("[QueryByPathService] [{}]获取" + wanConnPath + wanConnPath + j + wanPPPConnection + "下实例号失败",deviceId);
						kPPPList = corba.getIList(deviceId, wanConnPath + j + wanIPConnection);
						if (null == kPPPList || kPPPList.isEmpty()) {
							logger.warn("[QueryByPathService] [{}]获取" + wanConnPath + wanConnPath + j + wanIPConnection + "下实例号失败",deviceId);
						}
						for (String kppp : kPPPList) {
							wanConnPathsList.add(wanConnPath + j + wanIPConnection + kppp + wanServiceList);
						}
					} else {
						for (String kppp : kPPPList) {
							wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp + wanServiceList);
						}
					}
				}
			}	
		}
		
		if(null != wanConnPathsList && !wanConnPathsList.isEmpty()){
			
			List<String> tempWanConnPathsList = new ArrayList<String>();
			for(String wanConnPaths : wanConnPathsList){
				if(wanConnPaths.endsWith(wanServiceList)){
					tempWanConnPathsList.add(wanConnPaths);
				}
			}
			
			String[] paramNameArr = new String[tempWanConnPathsList.size()];
			for(int index=0;index<tempWanConnPathsList.size();index++){
				paramNameArr[index] = tempWanConnPathsList.get(index);
			}
			
			Map<String, String> paramValueMap = corba.getParaValueMap(deviceId,paramNameArr);
			if (null == paramValueMap || paramValueMap.isEmpty()) {
				logger.warn("[QueryByPathService] [{}]获取ServiceList失败",deviceId);
			}else{
				for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
					logger.debug("[{}]{}={} ",new Object[]{deviceId, entry.getKey(), entry.getValue()});
					String paramName = entry.getKey();
					if (paramName.endsWith(wanServiceList)) {
						//多业务,只记录第一次
						if (serviceMap.containsKey(entry.getValue()) && !StringUtil.IsEmpty(entry.getValue()) && StringUtil.IsEmpty(serviceMap.get(entry.getValue()))) {
							String res = entry.getKey().substring(0,entry.getKey().indexOf(wanServiceList));
							serviceMap.put(entry.getValue(), res);
						}
					}
				}
			}
		}
		
		logger.warn("[{}]预读完成", deviceId);
		for (Map.Entry<String, String> entry : serviceMap.entrySet()){
			logger.warn("[{}]<"+entry.getKey()+","+entry.getValue()+">", deviceId);
		}
		
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		for(int i=0;i<inObj.getService_name().size();i++){
			String servName = inObj.getService_name().get(i);
			ArrayList<String> params = inObj.getService_parameters().get(i);
			if(!StringUtil.IsEmpty(StringUtil.getStringValue(serviceMap, servName ,""))){
				for(int j=0;j<params.size();j++){
					String param = params.get(j);
					/*String paramLast = param.substring(param.lastIndexOf(".", 1000));
					params.set(j, StringUtil.getStringValue(serviceMap, servName ,"") + paramLast);*/
					if(param.contains(".VoiceProfile.")){
						logger.warn("VoiceProfile");
						if(param.contains(".Line.")){
							String line = "1";
							ArrayList<HashMap<String, String>> Infolist = userDevDao.qryLindidByDeviceID(deviceId);
							if(null!=Infolist && Infolist.size()==1){
								line = Infolist.get(0).get("line_id");
							}
							param = param.replace(".Line.-1.", ".Line."+line+".");
						}
						param = param.replace(".-1.", ".1.");
					}
					else{
						int lens = servListPathI.length();
						String value = StringUtil.getStringValue(serviceMap, servName ,"");
						String j_ = value.substring(lens, value.indexOf(".", lens));
						param = param.replace("WANConnectionDevice.-1.", "WANConnectionDevice."+j_+".");
						param = param.replace(".-1.", ".1.");
					}
					params.set(j, param);
				}
			}
			else{
				//不是none，又不存在该业务，参数列表设置为空，不采集
				if(!servName.equals("NONE"))
				for(int j=0;j<params.size();j++) params.set(j, "");
			}
		}
		
		logger.warn("[{}]预读替换完成", deviceId);
		/*for(int i=0;i<inObj.getService_name().size();i++){
			String servName = inObj.getService_name().get(i);
			ArrayList<String> params = inObj.getService_parameters().get(i);
			if(!StringUtil.IsEmpty(StringUtil.getStringValue(serviceMap, servName ,""))){
				for(int j=0;j<params.size();j++){
					String param = params.get(j);
				}
			}
		}*/
	}

	
	/**
	 * 获取参数
	 * @param corba
	 */
	private void doGetValue(ACSCorba corba)
	{
		ArrayList<String> paramsNeedGet = new ArrayList<String>();
		
		for(int i=0;i<inObj.getService_name().size();i++){
			ArrayList<String> params = inObj.getService_parameters().get(i);
			for(int j=0;j<params.size();j++){
				String param = params.get(j).replace(".-1.", ".1.");
				if(!StringUtil.IsEmpty(param) && !paramsNeedGet.contains(param)){
					paramsNeedGet.add(param);
				}
			}
		}
		logger.warn("获取参数列表:");
		for(String param:paramsNeedGet){
			logger.warn(param);
		}
		
		String[] paramNameArr = new String[paramsNeedGet.size()];
		for(int index=0;index<paramsNeedGet.size();index++){
			paramNameArr[index] = paramsNeedGet.get(index);
		}
		
		logger.warn("before corba");
		Map<String, String> paramValueMap = corba.getParaValueMapStat(deviceId,paramNameArr);
		if (null == paramValueMap || paramValueMap.isEmpty()) {
			rtn.setRstCode("4");
			rtn.setRstDesc("终端参数查询失败");
			logger.warn("[QueryByPathService] [{}]获取参数失败",deviceId);
		}
		else if (!"0".equals(paramValueMap.get("status")) && !"1".equals(paramValueMap.get("status"))) {
			rtn.setRstCode(paramValueMap.get("status"));
			rtn.setRstDesc(Global.G_Fault_Map.get(StringUtil.getIntegerValue(paramValueMap.get("status"))).getFaultDesc());
			logger.warn("[QueryByPathService] [{}]获取参数失败[{}]：",deviceId,paramValueMap.get("status"));
		}
		else{
			QueryRtnMsg rtnMsg= rtn.getRstMsg();
			rtnMsg.setOui_sn(oui_sn);
			rtnMsg.setService_name(inObj.getService_name());
			ArrayList<ArrayList<String>> parameters_result = rtnMsg.getService_parameters_result();
			
			for(int i=0;i<inObj.getService_name().size();i++){
				ArrayList<String> params = inObj.getService_parameters().get(i);
				ArrayList<String> parameter_result_one = new ArrayList<String>();
				for(int j=0;j<params.size();j++){
					String param = params.get(j);
					if(paramValueMap.containsKey(param)){
						parameter_result_one.add(paramValueMap.get(param));
					}
					else{
						parameter_result_one.add("");
					}
				}
				parameters_result.add(parameter_result_one);
			}
			rtn.setRstCode("0");
			rtn.setRstDesc("成功");
		}
	}
	
	
	public static void main(String[] args)
	{
		QueryRtn rtn = new QueryRtn();
		rtn.setRstCode("123");
		rtn.setRstDesc("文字描述");
		rtn.getRstMsg().setOui_sn("oui-sn");
		ArrayList<ArrayList<String>> parameters_result = rtn.getRstMsg().getService_parameters_result();
		ArrayList<String> p1 = new ArrayList<String>();
		ArrayList<String> p2 = new ArrayList<String>();
		
		p1.add("222");
		p1.add("333");
		p1.add("");
		p1.add("111");
		
		
		parameters_result.add(p2);
		parameters_result.add(p1);
		
		String res = JSON.toJSONString(rtn);
	}
	
	

}
