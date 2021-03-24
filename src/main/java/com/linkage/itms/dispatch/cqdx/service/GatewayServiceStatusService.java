package com.linkage.itms.dispatch.cqdx.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.GatewayServiceStatusDealXML;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * 网关业务查询
 * @author jiafh
 *
 */
public class GatewayServiceStatusService {
	private static Logger logger = LoggerFactory.getLogger(GatewayServiceStatusService.class);

	public String work(String inXml) {
		logger.warn("servicename[GatewayServiceStatusService]执行，入参为：{}", inXml);
		GatewayServiceStatusDealXML deal = new GatewayServiceStatusDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[GatewayServiceStatusService]解析入参错误！");
			deal.setResult("-11");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String serialNumber = deal.getSerialNumber();
		String voipNumber = deal.getVoipNumber();
		String ipAddress = deal.getIpAddress();
		
		String userInfo = "";
		int userInfoType = 0;
		if (!StringUtil.IsEmpty(logicId)) {
			// 逻辑账号
			userInfo = logicId;
			userInfoType = 2;
		}
		else if(!StringUtil.IsEmpty(pppUsename)) {
			// 宽带账号
			userInfo = pppUsename;
			userInfoType = 1;
		}
		else if(!StringUtil.IsEmpty(serialNumber)) {
			// 设备sn
			userInfo = serialNumber;
			userInfoType = 3;
		}else if(!StringUtil.IsEmpty(voipNumber)) {
			// 语音账号
			userInfo = voipNumber;
			userInfoType = 5;
		}else if(!StringUtil.IsEmpty(ipAddress)){
			// IP地址
			userInfo = ipAddress;
			userInfoType = 8;
		}else {
			logger.warn("servicename[GatewayServiceStatusService]入参格式错误！");
			deal.setResult("-11");
			deal.setErrMsg("入参格式错误！");
			return deal.returnXML();
		}
		
		// 查询用户业务信息
		PublicDAO dao = new PublicDAO();
		Map<String, String> userMap = dao.queryUserInfo(userInfoType, userInfo);
		if (null == userMap || userMap.isEmpty()) {
			logger.warn("servicename[GatewayServiceStatusService]不存在用户！");
			deal.setResult("-1");
			deal.setErrMsg("不存在用户！");
			return deal.returnXML();
		}
		
		// 获取IP地址
		if(userInfoType != 8){
			List<HashMap<String,String>> deviceMapList = dao.queryDeviceInfoByDeviceId(userMap.get("device_id"));
			if(null != deviceMapList && !deviceMapList.isEmpty() && null != deviceMapList.get(0)){
				ipAddress = deviceMapList.get(0).get("loopback_ip");
			}
		}
		
		deal.setResult("0");
		if(StringUtil.IsEmpty(userMap.get("device_id"))){
			deal.setErrMsg("该用户未绑定设备！");
		}else{
			deal.setErrMsg("成功！");
		}
		
		deal.setPppUsename(userMap.get("ppp_usename"));
		deal.setLogicId(userMap.get("logic_id"));
		deal.setSerialNumber(userMap.get("device_serialnumber"));
		deal.setIpAddress(ipAddress);
		
		// 期望开通状态
		// 宽带业务状态
		if("1".equals(userMap.get("net_status"))){
			deal.setNetExpectStatus("2");
		}else{
			deal.setNetExpectStatus("1");
		}
		
		// IPTV业务状态
		String[] iptvStatusArr = StringUtil.getStringValue(userMap.get("iptv_status")).split("\\|");
		if(Arrays.binarySearch(iptvStatusArr, "1") >= 0){
			deal.setIptvExpectStatus("2");
		}else{
			deal.setIptvExpectStatus("0");
		}
		
		// 语音开通状态
		String[] voipStatusArr = StringUtil.getStringValue(userMap.get("voip_status")).split("\\|");
		if(Arrays.binarySearch(voipStatusArr, "1") >= 0){
			deal.setVoipExpectStatus("2");
		}else{
			deal.setVoipExpectStatus("0");
		}
		
		// 实际开通状态和端口
		if(!StringUtil.IsEmpty(userMap.get("device_id"))){
			ACSCorba corba = new ACSCorba();
			
			// 判断设备是否在线
			int flag = new GetDeviceOnLineStatus().testDeviceOnLineStatus(userMap.get("device_id"), corba);
			if(1 == flag){
				
				// 实际状态默认为开通
				deal.setNetActualPort("0");
				deal.setIptvActualStatus("0");
				deal.setVoipActualStatus("0");
				
				String wanConnectionDevice =  "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
				List<String> wanList = corba.getIList(userMap.get("device_id"), wanConnectionDevice);
				if(null != wanList && wanList.isEmpty()){
					
					String[] servParamArr = new String[wanList.size()];
					for(int index=0;index<wanList.size();index++){
						servParamArr[index] = wanConnectionDevice + wanList.get(index) + ".WANPPPConnection.1.X_CT-COM_ServiceList";
					}			
					ArrayList<ParameValueOBJ> paramValueList = corba.getValue(userMap.get("device_id"), servParamArr);
					if(null != paramValueList && !paramValueList.isEmpty() && null != paramValueList.get(0)){
						String netJ = "";
						String iptvJ = "";
						for(ParameValueOBJ parameValueOBJ : paramValueList){
							String temp = parameValueOBJ.getName().substring(parameValueOBJ.getName().indexOf("WANConnectionDevice."),
									parameValueOBJ.getName().indexOf(".WANPPPConnection"));
							if("INTERNET".equalsIgnoreCase(parameValueOBJ.getValue())){
								
								netJ = temp.substring(temp.lastIndexOf(".") + 1);
							}
							if("IPTV".equalsIgnoreCase(parameValueOBJ.getValue())){
								iptvJ = temp.substring(temp.lastIndexOf(".") + 1);
							}
						}
						
						ArrayList<ParameValueOBJ> actParamValueList = null;
						String [] actServParamArr = null;
						if(!StringUtil.IsEmpty(netJ) && !StringUtil.IsEmpty(iptvJ)){
							actServParamArr = new String[2];
							actServParamArr[0] = wanConnectionDevice + netJ + ".WANPPPConnection.1.X_CT-COM_LanInterface";
							actServParamArr[1] = wanConnectionDevice + iptvJ + ".WANPPPConnection.1.X_CT-COM_LanInterface";
							actParamValueList = corba.getValue(userMap.get("device_id"), actServParamArr);
							if(null != actParamValueList && !actParamValueList.isEmpty() && null != actParamValueList.get(0)){
								
								// 宽带端口
								String netPortValue = actParamValueList.get(0).getValue();
								if(!StringUtil.IsEmpty(netPortValue)){
									deal.setNetActualStatus("2");
									String[] netPortArr = netPortValue.split(",");
									String actNetPort = "";
									for(String netPort : netPortArr){
										if(StringUtil.IsEmpty(actNetPort)){
											actNetPort = netPort.substring(netPort.lastIndexOf(".") + 1);
										}else{
											actNetPort = actNetPort + "," + netPort.substring(netPort.lastIndexOf(".") + 1);
										}
									}
									deal.setNetActualPort(actNetPort);
								}
								
								// IPTV端口
								String iptvPortValue = actParamValueList.get(1).getValue();
								if(!StringUtil.IsEmpty(iptvPortValue)){
									deal.setIptvActualStatus("2");
									String[] iptvPortArr = iptvPortValue.split(",");
									String actIptvPort = "";
									for(String iptvPort : iptvPortArr){
										if(StringUtil.IsEmpty(actIptvPort)){
											actIptvPort = iptvPort.substring(iptvPort.lastIndexOf(".") + 1);
										}else{
											actIptvPort = actIptvPort + "," + iptvPort.substring(iptvPort.lastIndexOf(".") + 1);
										}
									}
									deal.setIptvActualPort(actIptvPort);
								}
							}	
						}else if(!StringUtil.IsEmpty(netJ) && StringUtil.IsEmpty(iptvJ)){
							actServParamArr = new String[1];
							actServParamArr[0] = wanConnectionDevice + netJ + ".WANPPPConnection.1.X_CT-COM_LanInterface";
							actParamValueList = corba.getValue(userMap.get("device_id"), actServParamArr);
							if(null != actParamValueList && !actParamValueList.isEmpty() && null != actParamValueList.get(0)){
								// 宽带端口
								String netPortValue = actParamValueList.get(0).getValue();
								if(!StringUtil.IsEmpty(netPortValue)){
									deal.setNetActualStatus("2");
									String[] netPortArr = netPortValue.split(",");
									String actNetPort = "";
									for(String netPort : netPortArr){
										if(StringUtil.IsEmpty(actNetPort)){
											actNetPort = netPort.substring(netPort.lastIndexOf(".") + 1);
										}else{
											actNetPort = actNetPort + "," + netPort.substring(netPort.lastIndexOf(".") + 1);
										}
									}
									deal.setNetActualPort(actNetPort);
								}
							}			
						}else if(StringUtil.IsEmpty(netJ) && !StringUtil.IsEmpty(iptvJ)){
							actServParamArr = new String[1];
							actServParamArr[0] = wanConnectionDevice + iptvJ + ".WANPPPConnection.1.X_CT-COM_LanInterface";
							actParamValueList = corba.getValue(userMap.get("device_id"), actServParamArr);
							if(null != actParamValueList && !actParamValueList.isEmpty() && null != actParamValueList.get(0)){
								// IPTV端口
								String iptvPortValue = actParamValueList.get(0).getValue();
								if(!StringUtil.IsEmpty(iptvPortValue)){
									deal.setIptvActualStatus("2");
									String[] iptvPortArr = iptvPortValue.split(",");
									String actIptvPort = "";
									for(String iptvPort : iptvPortArr){
										if(StringUtil.IsEmpty(actIptvPort)){
											actIptvPort = iptvPort.substring(iptvPort.lastIndexOf(".") + 1);
										}else{
											actIptvPort = actIptvPort + "," + iptvPort.substring(iptvPort.lastIndexOf(".") + 1);
										}
									}
									deal.setIptvActualPort(actIptvPort);
								}
							}
						}
					}
				}
				
				// 语音
				String voiceProfileLine = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line.";
				List<String> voiceList = corba.getIList(userMap.get("device_id"), voiceProfileLine);
				if(null != voiceList && !voiceList.isEmpty() && null != voiceList.get(0)){
					String[] voiceProfileArr = new String[voiceList.size()];
					for(int index=0;index<voiceList.size();index++){
						voiceProfileArr[index] = voiceProfileLine + voiceList.get(index) + ".Enable";
					}
					
					
					ArrayList<ParameValueOBJ> actParamValueList = corba.getValue(userMap.get("device_id"), voiceProfileArr);
					if(null != actParamValueList && !actParamValueList.isEmpty() && null != actParamValueList.get(0)){
						String actVoipPort = "";
						List<String> voipNameList = new ArrayList<String>();
						for(ParameValueOBJ parameValueOBJ : actParamValueList){
							String temp = parameValueOBJ.getName().substring(parameValueOBJ.getName().indexOf("Line."),
									parameValueOBJ.getName().indexOf(".Enable"));
							if("Enabled".equalsIgnoreCase(parameValueOBJ.getValue())){
								voipNameList.add(voiceProfileLine + temp.substring(temp.lastIndexOf(".") + 1) + ".SIP.AuthUserName");
								if(StringUtil.IsEmpty(actVoipPort)){
									actVoipPort = temp.substring(temp.lastIndexOf(".") + 1);
								}else{
									actVoipPort = actVoipPort + "," + temp.substring(temp.lastIndexOf(".") + 1);
								}
							}
						}
						deal.setVoipActualPort(actVoipPort);
						
						// 语音账号
						if(!voipNameList.isEmpty()){
							String voipName = "";
							String[] voipNameArr = new String[voipNameList.size()];
							for(int index=0;index<voipNameList.size();index++){
								voipNameArr[index] = voipNameList.get(0);
							}
							ArrayList<ParameValueOBJ> actVoipNameValueList = corba.getValue(userMap.get("device_id"), voipNameArr);
							if(null != actVoipNameValueList && !actVoipNameValueList.isEmpty() && null != actVoipNameValueList.get(0)){
								for(ParameValueOBJ parameValueOBJ : actVoipNameValueList){
									if(StringUtil.IsEmpty(voipName)){
										voipName = parameValueOBJ.getValue();
									}else{
										voipName = voipName + "," + parameValueOBJ.getValue();
									}
								}
								deal.setVoipNumber(voipName);
							}							
						}
					}
				}
			}else{
				deal.setErrMsg("设备不在线或正忙！");
			}
		}
		
		return deal.returnXML();
	}
	
	public static void main(String[] args) {
		String wanConnectionDevice =  "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.XYZ.WANPPPConnection.1.X_CT-COM_ServiceList";
		String tem = wanConnectionDevice.substring(wanConnectionDevice.indexOf("WANConnectionDevice."),wanConnectionDevice.indexOf(".WANPPPConnection"));
		
		
		System.out.println(tem.substring(tem.indexOf(".") + 1));
	}
}
