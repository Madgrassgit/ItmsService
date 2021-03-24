package com.linkage.itms.dispatch.sxdx.service;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.sxdx.beanObj.SetParameterResult;
import com.linkage.itms.dispatch.sxdx.dao.PublicDAO;
import com.linkage.itms.dispatch.sxdx.obj.SetCpeParameterValuesDealXML;
import com.linkage.itms.dispatch.sxdx.obj.SetCpeServiceStateServiceXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 甘肃电信对终端的TR069节点下发配置值
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月19日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class setCpeServiceStateService extends ServiceFather {
	public setCpeServiceStateService(String methodName)
	{
		super(methodName);
	}


	private static Logger logger = LoggerFactory.getLogger(setCpeServiceStateService.class);
	private ACSCorba corba = new ACSCorba();
	private SetCpeServiceStateServiceXML dealXML;
	private String WANCONNECTION = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
	private List<HashMap<String, String>> paramList= new ArrayList<HashMap<String, String>>();

	public int work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		
		dealXML = new SetCpeServiceStateServiceXML(methodName);

		// 验证入参
		if (null == dealXML.getXML(inXml)){
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			return -2;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		
		PublicDAO dao = new PublicDAO();

		ArrayList<HashMap<String, String>> userDevList = dao.queryDeviceInfo(StringUtil.getIntegerValue(dealXML.getiParaType()), dealXML.getValue());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}", userDevList.toString());
		
		if(userDevList.size() > 1){
			logger.warn(methodName+"["+dealXML.getOpId()+"],查询到多条结果返回数量");
			return -1000;
		}
		else if(null == userDevList || userDevList.size()==0 || StringUtil.isEmpty(StringUtil.getStringValue(userDevList.get(0), "device_id"))){
			logger.warn(methodName+"["+dealXML.getOpId()+"],未查询到终端");

			return -1;
		}
		
		String deviceId = StringUtil.getStringValue(userDevList.get(0), "device_id");
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag){
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备不在线或正在被操作，返回-4");

			return -4;
		}
		
		logger.warn(methodName+"["+dealXML.getOpId()+"],设备在线，准备配置参数。");

		ArrayList<String> wanConnPathsList = new ArrayList<String>();
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		wanConnPathsList = corba.getParamNamesPath(deviceId, WANCONNECTION, 0);
		logger.warn(methodName+"["+dealXML.getOpId()+"]wanConnPathsList.size:{}", wanConnPathsList.size());
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty()){
			logger.warn(methodName+"["+dealXML.getOpId()+"]wanConnPathsList 为空，采集失败，返回-1");
			return -1;
		}
		else
		{
			ArrayList<String> paramNameList = new ArrayList<String>();
			for (int i = 0; i < wanConnPathsList.size(); i++)
			{
				String namepath = wanConnPathsList.get(i);
				if (namepath.indexOf(".X_CT-COM_ServiceList") >= 0)
				{
					paramNameList.add(namepath);
				}
			}
			wanConnPathsList = new ArrayList<String>();
			wanConnPathsList.addAll(paramNameList);
		}
		if (wanConnPathsList.size() == 0){
			logger.warn(methodName+"["+dealXML.getOpId()+"]无X_CT-COM_ServiceList节点：", deviceId);
		}
		else {
			Collections.reverse(wanConnPathsList);

			String[] paramNametemp = new String[wanConnPathsList.size()];
			for (int i = 0; i < wanConnPathsList.size(); i++) {
				paramNametemp[i] = wanConnPathsList.get(i);
			}
			Map<String, String> paramValueMap = corba.getParaValueMap(deviceId,
					paramNametemp);
			if (paramValueMap.isEmpty()) {
				logger.warn(methodName + "获取ServiceList失败[{}]", deviceId);
			}

			for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
				// InternetGatewayDevice.WANDevice.1.WANConnectionDevice.2.WANPPPConnection.3.X_CT-COM_ServiceList
				if (entry.getValue().indexOf("INTERNET") >= 0
						|| entry.getValue().indexOf("internet") >= 0) {
					int index = entry.getKey().indexOf(".X_CT-COM_ServiceList");
					String servListPathJ = entry.getKey().substring(0, index);
					logger.warn(methodName + "[{}]获取INTERNET path成功", deviceId);
					if("0".equals(dealXML.getServiceType())){
						HashMap<String,String> paramMap = new HashMap<String,String>();
						paramMap.put(servListPathJ+".Enable",dealXML.getServiceState());
						paramList.add(paramMap);
					}

				} else if (entry.getValue().indexOf("IPTV") >= 0 || entry.getValue().indexOf("OTHER") >= 0
						|| entry.getValue().indexOf("iptv") >= 0 || entry.getValue().indexOf("other") >= 0) {
					int index = entry.getKey().indexOf(".X_CT-COM_ServiceList");
					String servListPathJ = entry.getKey().substring(0, index);
					logger.warn(methodName + "[{}]获取IPTV path成功", deviceId);
					if("1".equals(dealXML.getServiceType())){
						HashMap<String,String> paramMap = new HashMap<String,String>();
						paramMap.put(servListPathJ+".Enable",dealXML.getServiceState());
						paramList.add(paramMap);
					}
				}
			}
		}
		if(paramList.size()<=0){
			return -3;
		}
		int retResult = setParameters(deviceId, paramList);
		if (retResult == 0 || retResult == 1)
		{
			return retResult;
		}
		else{
			return -1000;
		}
	}

	private int setParameters(String deviceId, List<HashMap<String, String>> paramList)
	{
		ArrayList<ParameValueOBJ> objList = this.getObjList(paramList);
		ACSCorba corba = new ACSCorba();
		int retResult = corba.setValue(deviceId, objList);
		
		return retResult;
	}

	private ArrayList<ParameValueOBJ> getObjList(List<HashMap<String, String>> paramList)
	{
		// 修改密码到设备
		ArrayList<ParameValueOBJ> objList = new ArrayList<ParameValueOBJ>();
		ParameValueOBJ obj = new ParameValueOBJ();
		for(HashMap<String, String> map : paramList){
			obj.setName(StringUtil.getStringValue(map,"name"));
			obj.setValue(StringUtil.getStringValue(map,"value"));
			obj.setType("4");
			objList.add(obj);
		}
		return objList;
	}
	
}
