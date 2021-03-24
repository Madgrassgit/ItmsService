package com.linkage.itms.dispatch.gsdx.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.gsdx.beanObj.GetXDSLInfoResult;
import com.linkage.itms.dispatch.gsdx.beanObj.Para;
import com.linkage.itms.dispatch.gsdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.gsdx.obj.GetXDSLInfoXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetXDSLInfoService extends ServiceFather {
	private static Logger logger = LoggerFactory.getLogger(ResetService.class);
	private GetXDSLInfoXML dealXML;

	public GetXDSLInfoService(String methodName) {
		super(methodName);
	}
	String PPPUserName = "-";
	String DNS = "-";
	String MTU = "-";
	String WiFiChannel_24G = "-";
	String WiFiChannel_5G = "-";
	public GetXDSLInfoResult work(String inXml){
		GetXDSLInfoResult result = new GetXDSLInfoResult();
		ArrayList<Para> paraList = new ArrayList<Para>();
		dealXML = new GetXDSLInfoXML(methodName);
		// 验证入参
		if (null == dealXML.getXML(inXml)) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			result.setIOpRst(-1000);
			return result;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		CpeInfoDao dao = new CpeInfoDao();
		Map<String, String> queryUserInfo = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getType()),dealXML.getIndex());
		if(null == queryUserInfo || queryUserInfo.isEmpty()){
			logger.warn(methodName+"["+dealXML.getOpId()+"]根据入参为获取到数据为空[{}:{}]", dealXML.getIndex(),dealXML.getIndex());
			result.setIOpRst(-1000);
			return result;
		}
		String deviceId = StringUtil.getStringValue(queryUserInfo, "device_id");
		if(null ==  deviceId || StringUtil.IsEmpty(deviceId)){
			logger.warn(methodName+"["+dealXML.getOpId()+"]根据入参为获取到数据未绑定设备[{}:{}]", dealXML.getIndex(),dealXML.getIndex());
			result.setIOpRst(0);
			return result;
		}
		List<HashMap<String, String>> deviceInfo = dao.getDeviceInfo(deviceId);
		/**
		 *	deviceID：终端唯一标识OUI-SN。
			PPPUserName：WANPPPConnection.Username如果找不到时返回”-”。
			DNS：WANPPPConnection.DNSServers如果找不到时返回”-”。
			MTU：WANPPPConnection.MaxMRUSize如果找不到时返回”-”。
			WiFiChannel_2.4G：InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.AutoChannelEnable如果找不到时返回”-”。
			WiFiChannel_5G：InternetGatewayDevice.LANDevice.1.WLANConfiguration.5.AutoChannelEnable如果找不到时返回”-”。
		 * 
		 * */
		if(null ==  deviceInfo || deviceInfo.isEmpty()){
			logger.warn(methodName+"["+dealXML.getOpId()+"]根据入参为获取到数据未绑定设备[{}:{}]", dealXML.getIndex(),dealXML.getIndex());
			result.setIOpRst(0);
			return result;
		}
		
		paraList.add(setPara("deviceID",StringUtil.getStringValue(deviceInfo.get(0),"oui")));
		ACSCorba corba = new ACSCorba();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag){
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备不在线或正在被操作，无法获取节点值，device_id={}", deviceId);
			result.setIOpRst(-1);
			
		}
		else{
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备在线开始采集，device_id={}", deviceId);
			String lanPath = "InternetGatewayDevice.LANDevice.";
			
			String[] gatherPathArray = {"WANPPPConnection.Username","WANPPPConnection.DNSServers","WANPPPConnection.MaxMRUSize"};
			// 处理设备采集结果
			ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPathArray);
			if (null == objLlist || objLlist.isEmpty()){
				logger.warn(methodName+"["+dealXML.getOpId()+"],采集失败，device_id={}", deviceId);
			}
			else{
				for(ParameValueOBJ pvobj : objLlist){
					if(pvobj.getName().contains("PPPUserName")){
						PPPUserName = pvobj.getValue();
					}else if(pvobj.getName().contains("DNSServers")){
						PPPUserName = pvobj.getValue();
					}else if(pvobj.getName().contains("MaxMRUSize")){
						PPPUserName = pvobj.getValue();
					}
				}
			}
		}
		paraList.add(setPara("PPPUserName",""));
		paraList.add(setPara("DNS",""));
		paraList.add(setPara("MTU",""));
		//"InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.AutoChannelEnable",
		//"InternetGatewayDevice.LANDevice.1.WLANConfiguration.5.AutoChannelEnable"
		 String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
		    List<String> iList = corba.getIList(deviceId, lanPath);
			if (null == iList || iList.isEmpty())
			{
				logger.warn("[{}]获取iList失败，返回", deviceId);
			}else{
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
				List<HashMap<String,String>> lanList = new ArrayList<HashMap<String,String>>();
				String auto1 = "";
				String auto5 = "";
				for(String i : iList){
					if("1".equals(i)){
						auto1 =  "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+i+".AutoChannelEnable";
					}else if("5".equals(i)){
						auto5 =  "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+i+".AutoChannelEnable";
					}
				}
				if(!StringUtil.IsEmpty(auto1)){
					ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, auto1);
					WiFiChannel_24G = objLlist.get(0).getValue();
				}
				if(!StringUtil.IsEmpty(auto5)){
					ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, auto1);
					WiFiChannel_5G = objLlist.get(0).getValue();
				}
			}
			paraList.add(setPara("WiFiChannel_2.4G",WiFiChannel_24G));
			paraList.add(setPara("WiFiChannel_5G",WiFiChannel_5G));
			Para[] array = (Para[])paraList.toArray(new Para[paraList.size()]);  
			result.setParaList(array);
			return result;
	}
	
}
