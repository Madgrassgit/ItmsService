package com.linkage.itms.dispatch.service;

import com.linkage.WSClient.WebServiceUtil;
import com.linkage.commons.util.DateTimeUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.commom.util.SocketUtil;
import com.linkage.itms.dao.GetServInfofromDevDAO;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dispatch.obj.GetServInfofromDevChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetServInfofromDevService implements IService {
	private static Logger logger = LoggerFactory.getLogger(GetServInfofromDevService.class);

	@Override
	public String work(String inXml) {
		GetServInfofromDevChecker checker = new GetServInfofromDevChecker(inXml);
		if (!checker.check()) {
			logger.warn("servicename[GetServInfofromDevService]cmdId[{}]queryInfo[{}]验证未通过，返回：{}",
					new Object[] {checker.getCmdId(), checker.getQueryInfo(), inXml});
			return checker.getReturnXml();
		}
		int servType = checker.getServType();
		// iptv不作业务
		if (servType == 11) {
			logger.warn("servicename[GetServInfofromDevService]cmdId[{}]queryInfo[{}]iptv直接返回成功",
					new Object[] {checker.getCmdId(), checker.getQueryInfo()});
			checker.setResult(0);
			checker.setResultDesc("成功");
			return checker.getReturnXml();
		}
		GetServInfofromDevDAO dao = new GetServInfofromDevDAO();
		Map<String, String> userInfor = dao.queryUserInfor(checker.getQueryType(), checker.getQueryInfo());
		String userId = StringUtil.getStringValue(userInfor, "user_id");
		String deviceId = StringUtil.getStringValue(userInfor, "device_id");
		String cityId = StringUtil.getStringValue(userInfor, "city_id");
		String loid = StringUtil.getStringValue(userInfor, "username");
		//　用户是否存在
		if (userInfor.isEmpty() || StringUtil.isEmpty(userId)) {
			logger.warn("servicename[GetServInfofromDevService]cmdId[{}]queryInfo[{}]无此用户",
					new Object[] {checker.getCmdId(), checker.getQueryInfo()});
			checker.setResult(1002);
			checker.setResultDesc("无此用户");
			return checker.getReturnXml();
		}
		// 存在对应业务记录
		int recordCount = dao.queryRecordCount(userId, servType);
		if (recordCount > 0) {
			logger.warn("servicename[GetServInfofromDevService]cmdId[{}]queryInfo[{}]存在业务",
					new Object[] {checker.getCmdId(), checker.getQueryInfo()});
			checker.setResult(1004);
			checker.setResultDesc("工单业务已存在，无需采集");
			return checker.getReturnXml();
		}
		// 是否绑定设备
		if (userInfor.isEmpty() || StringUtil.isEmpty(deviceId)) {
			logger.warn("servicename[GetServInfofromDevService]cmdId[{}]queryInfo[{}]未查询到设备信息",
					new Object[] {checker.getCmdId(), checker.getQueryInfo()});
			checker.setResult(1003);
			checker.setResultDesc("未查询到设备信息");
			return checker.getReturnXml();
		}
		
		// 生成随机密码并修改
		String pwsd = modifyPswd();
		// 如果密码为空，则修改密码失败
		if (StringUtil.isEmpty(pwsd)) {
			logger.warn("servicename[GetServInfofromDevService]cmdId[{}]queryInfo[{}]修改密码失败",
					new Object[] {checker.getCmdId(), checker.getQueryInfo()});
			checker.setResult(1008);
			checker.setResultDesc("修改密码失败");
			return checker.getReturnXml();
		}
		
		ACSCorba corba = new ACSCorba();
		// 判断设备是否在线
		int flag = new GetDeviceOnLineStatus().testDeviceOnLineStatus(deviceId, corba);
		if (flag != 1) {
			logger.warn("[{}]对应设备不在线", deviceId);
			checker.setResult(1007);
			checker.setResultDesc("设备不在线");
			return checker.getReturnXml();
		}
		
		StringBuffer sheet = new StringBuffer(); 
		// 宽带业务
		if (servType == 10) {
			String wanConnectionDevice =  "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
			List<String> iList = corba.getIList(deviceId, wanConnectionDevice);
			if (null == iList || iList.isEmpty()) {
				logger.warn("[{}]获取iList失败，返回", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return checker.getReturnXml();
			}
			else {
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId, iList.size());
			}
			String[] servParamArr = new String[iList.size()];
			
			for(int index = 0; index < iList.size(); index++){
				servParamArr[index] = wanConnectionDevice + iList.get(index) + ".WANPPPConnection.1.X_CT-COM_ServiceList";
			}			
			ArrayList<ParameValueOBJ> paramValueList = corba.getValue(deviceId, servParamArr);
			if (null == paramValueList || paramValueList.isEmpty()) {
				logger.warn("[{}]获取paramValueList失败，返回", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return checker.getReturnXml();
			} 
			else {
				logger.warn("[{}]获取paramValueList成功，paramValueList.size={}", deviceId, paramValueList.size());
			}
			String netJ = "";
			for(ParameValueOBJ parameValueOBJ : paramValueList){
				String pName = parameValueOBJ.getName();
				String temp = pName.substring(pName.indexOf("WANConnectionDevice.") + 1, pName.indexOf(".WANPPPConnection"));
				if("INTERNET".equalsIgnoreCase(parameValueOBJ.getValue())) {
					netJ = temp;
				}
			}
			if (StringUtil.isEmpty(netJ)) {
				logger.warn("[{}]没有宽带业务节点", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("没有宽带业务节点");
				return checker.getReturnXml();
			}
			QueryDevDAO qdDao = new QueryDevDAO();
			String accessType = StringUtil.getStringValue(qdDao.queryAccessType(userId), "adsl_hl");
			String path = ".X_CT-COM_WANGponLinkConfig";
			if("3.0".equals(accessType)){
				path = ".X_CT-COM_WANEponLinkConfig";
			}
			String[] pathArr = new String[]{
					"InternetGatewayDevice.WANDevice.1.WANConnectionDevice." + netJ + ".WANPPPConnection.1.ConnectionType",
					"InternetGatewayDevice.WANDevice.1.WANConnectionDevice." + netJ + ".WANPPPConnection.1.Username",
					"InternetGatewayDevice.WANDevice.1.WANConnectionDevice." + netJ + path + ".VLANIDMark"};
			
			ArrayList<ParameValueOBJ>  objLlist = corba.getValue(deviceId, pathArr);
			if (null == objLlist || objLlist.isEmpty()) {
				logger.warn("[{}]获取objLlist失败，返回", deviceId);  // 打印回参
				checker.setResult(1000);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return checker.getReturnXml();
			}
			String username = "";
			String wanType = "";
			String vlanId = "";
			for(ParameValueOBJ pv : objLlist){
				if(pv.getName().contains("ConnectionType")){
					if ("PPPoE_Bridged".equals(pv.getValue())) {
						wanType = "1"; // 桥接
					} else if ("IP_Routed".equals(pv.getValue())) {
						wanType = "2"; // 路由
					}
				}
				else if(pv.getName().contains("Username")){
					username = pv.getValue();
				}
				else if(pv.getName().contains("VLANIDMark")){
					vlanId = pv.getValue();
				}
			}
			
			// 拼接工单
			sheet.append("22|||1|||")
				 .append(new DateTimeUtil().getYYYYMMDDHHMMSS())
				 .append("|||1|||")
				 .append(loid + "|||")
				 .append(username + "|||")
				 .append(pwsd + "|||")
				 .append(cityId + "|||")
				 .append(vlanId + "|||")
				 .append(wanType + "|||")
				 .append("|||||||||LINKAGE");
		}
		// 语音
		else if (servType == 14) {
			// 采集设备上的电话
			String voipPhone = "";
			String voiceProfileLine = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line.";

			List<String> voiceList = corba.getIList(deviceId, voiceProfileLine);
			if (null == voiceList || voiceList.isEmpty()) {
				logger.warn("[{}]获取voiceList失败，返回", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return checker.getReturnXml();
			} 
			else {
				logger.warn("[{}]获取voiceList成功，voiceList.size={}", deviceId, voiceList.size());
			}
			String[] voiceProfileArr = new String[voiceList.size()];
			for(int index=0; index < voiceList.size(); index++){
				voiceProfileArr[index] = voiceProfileLine + voiceList.get(index) + ".Enable";
			}
			ArrayList<ParameValueOBJ> actParamValueList = corba.getValue(deviceId, voiceProfileArr);
			if (null == actParamValueList || actParamValueList.isEmpty()) {
				logger.warn("[{}]获取actParamValueList失败，返回", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return checker.getReturnXml();
			} 
			else {
				logger.warn("[{}]获取actParamValueList成功，actParamValueList.size={}", deviceId, actParamValueList.size());
			}
			String voipJ = "";
			for(ParameValueOBJ parameValueOBJ : actParamValueList){
				String pName = parameValueOBJ.getName();
				String temp = pName.substring(pName.indexOf("Line.") + 1, pName.indexOf(".Enable"));
				if("Enabled".equalsIgnoreCase(parameValueOBJ.getValue())){
					voipJ = temp;
				}
			}
			if (StringUtil.isEmpty(voipJ)) {
				logger.warn("[{}]没有语音业务节点", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("没有语音业务节点");
				return checker.getReturnXml();
			}

			String[] pathArr = new String[]{
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line." + voipJ + ".SIP.AuthUserName",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.ProxyServer",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.ProxyServerPort",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.X_CT-COM_Standby-ProxyServer",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.X_CT-COM_Standby-ProxyServerPort",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.RegistrarServer",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.RegistrarServerPort",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.X_CT-COM_Standby-RegistrarServer",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.X_CT-COM_Standby-RegistrarServerPort",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.OutboundProxy",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.OutboundProxyPort",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.X_CT-COM_Standby-OutboundProxy",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.X_CT-COM_Standby-OutboundProxyPort",
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.ProxyServer",// 语音端口
					"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.SIP.ProxyServer"};//协议类型
			
			ArrayList<ParameValueOBJ>  objLlist = corba.getValue(deviceId, pathArr);
			if (null == objLlist || objLlist.isEmpty()) {
				logger.warn("[{}]获取objLlist失败，返回", deviceId); 
				checker.setResult(1000);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return checker.getReturnXml();
			}
			String username = "";
			String wanType = "";
			String vlanId = "";
			for(ParameValueOBJ pv : objLlist){
				if(pv.getName().contains("ConnectionType")){
					if ("PPPoE_Bridged".equals(pv.getValue())) {
						wanType = "1"; // 桥接
					} else if ("IP_Routed".equals(pv.getValue())) {
						wanType = "2"; // 路由
					}
				}
				else if(pv.getName().contains("Username")){
					username = pv.getValue();
				}
				else if(pv.getName().contains("VLANIDMark")){
					vlanId = pv.getValue();
				}
			}
			// 拼接工单
			sheet.append("14|||1|||")
				 .append(new DateTimeUtil().getYYYYMMDDHHMMSS())
				 .append("|||1|||")
				 .append(loid + "|||")
				 .append(voipPhone + "|||")
				 .append(cityId + "|||")
				 .append(voipPhone + "|||")
				 .append(pwsd + "|||")
				 .append(vlanId + "|||")
				 .append(wanType + "|||")
				 .append("|||||||||LINKAGE");
		}
		
		try {
			logger.warn("SendSheetThread.sheet=" + sheet.toString());
			String  res = SocketUtil.sendStrMesg(Global.G_ITMS_SHEET_SERVER_CHINA_MOBILE, 
					StringUtil.getIntegerValue(Global.G_ITMS_SHEET_PORT_CHINA_MOBILE),
					sheet.toString() + "\n");
			logger.warn("SendSheetThread=回单=" + res);
			if (!"0|||00".equals(res.trim())) {
				logger.warn("路由工单发送失败:   " + res);
				checker.setResult(1009);
				checker.setResultDesc("工单发送失败");
			} else {
				logger.warn("路由工单发送成功！");
				checker.setResult(0);
				checker.setResultDesc("成功");
			}
		} catch (Exception e) {
			logger.error("路由工单异常：" + e);
			checker.setResult(1009);
			checker.setResultDesc("工单发送失败");
		}
		return checker.getReturnXml();
	}
	
	/**
	 * 生成6位数密码
	 * @return
	 */
	private String modifyPswd() {
		/*Random rand = new Random();
		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < 6; j++) {
			sb.append(rand.nextInt(10));
		}*/
		String url = "";
		String targetNamespace = "";
		String method = "";
		String param = "";
			
		String ret = null;
		try {
			ret = WebServiceUtil.call(url, targetNamespace, method, param);
		} catch (Exception e) {
			logger.error("modifyPswd error: ",e);
		}
		return ret;
	}
}
