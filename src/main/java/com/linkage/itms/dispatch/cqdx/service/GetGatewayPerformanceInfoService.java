package com.linkage.itms.dispatch.cqdx.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.obj.GetGatewayPerformanceInfoDealXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * 家庭网关性能指标查询
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-11-19
 */
public class GetGatewayPerformanceInfoService {
	private static Logger logger = LoggerFactory.getLogger(GetGatewayPerformanceInfoService.class);
	private String ststusPatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.Status";
    private String txPowerPatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.TXPower";
    private String rxPowerEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.RXPower";
    private String temperaturePatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.TransceiverTemperature";
    private String supplyVottagePatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.SupplyVottage";
    private String biasCurrentPatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.BiasCurrent";
    private String ststusPatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.Status";
    private String txPowerPatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.TXPower";
    private String rxPowerGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.RXPower";
    private String temperaturePatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.TransceiverTemperature";
    private String supplyVottagePatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.SupplyVottage";
    private String biasCurrentPatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.BiasCurrent";
    public String ACCESS_TYPE_PATH_DEFAULT = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";

	public String work(String inXml) {
		logger.warn("servicename[GetGatewayPerformanceInfoService]执行，入参为：{}", inXml);
		GetGatewayPerformanceInfoDealXML deal = new GetGatewayPerformanceInfoDealXML();
		
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[GetGatewayPerformanceInfoService]解析入参错误！");
			deal.setResult("-99");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		if("".equals(logicId) && "".equals(pppUsename)){
			logger.warn("servicename[GetGatewayPerformanceInfoService]宽带账号和逻辑账号不能同时为空！");
			deal.setResult("-99");
			deal.setErrMsg("宽带账号和逻辑账号不能同时为空！");
			return deal.returnXML();
		}
		
		QueryDevDAO qdDao = new QueryDevDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		String deviceId = "";
		List<HashMap<String, String>> userMap = null;
		if (!"".equals(pppUsename)){
			userMap = qdDao.queryUserByNetAccount(pppUsename);
		}else{
			userMap = qdDao.queryUserByLoid(logicId);
		}
		
		if (userMap.size() > 1)
		{
			deal.setResult("-99");
			deal.setErrMsg("数据不唯一，请使用逻辑SN查询");
			return deal.returnXML();
		}
		
		if (userMap == null || userMap.isEmpty())
		{
			logger.warn("servicename[GetGatewayPerformanceInfoService]loid[{}]查无此用户",
					new Object[] { logicId });
			deal.setResult("-1");
			deal.setErrMsg("用户不存在");
			return deal.returnXML();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{// 用户未绑定终端
			logger.warn("servicename[GetGatewayPerformanceInfoService]loid[{}]此客户未绑定",
					new Object[] { logicId });
			deal.setResult("-99");
			deal.setErrMsg("此客户未绑定");
			return deal.returnXML();
		}
		
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id");
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		
		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			deal.setResult("-99");
			deal.setErrMsg("设备不能正常交互");
			logger.warn("return=({})", deal.returnXML());  // 打印回参
			return deal.returnXML();
		}
		// 设备在线
		else if (1 == flag) {

			logger.warn(
					"serviceName[QueryPerformanceService] userinfo[{}]开始采集[{}]",
					new Object[] { pppUsename, deviceId });
			ArrayList<ParameValueOBJ> objLlist = null;
			String accessType = UserDeviceDAO.getAccType(deviceId);
			String tx_power = "0";
			String rx_power = "0";
			String transceiverTemperature = "0";
			String supplyVottage = "0";
			String biasCurrent = "0";
			if(null == accessType){
				objLlist = corba.getValue(deviceId, ACCESS_TYPE_PATH_DEFAULT);
				if (null == objLlist || objLlist.isEmpty()) {
					deal.setResult("-99");
					deal.setErrMsg("设备采集失败");
					return deal.returnXML();
				}
				accessType = objLlist.get(0).getValue();
			}
			String[] gatherPath = new String[]{
					this.ststusPatchEPON,this.txPowerPatchEPON,this.rxPowerEPON,this.temperaturePatchEPON,this.supplyVottagePatchEPON,this.biasCurrentPatchEPON};
			if("GPON".equals(accessType))
			{
				gatherPath = new String[]{
						this.ststusPatchGPON,this.txPowerPatchGPON,this.rxPowerGPON,this.temperaturePatchGPON,this.supplyVottagePatchGPON,this.biasCurrentPatchGPON};
			}
			objLlist = corba.getValue(deviceId, gatherPath);
			logger.warn("deviceId:{},objList:{}" ,deviceId,objLlist);
			if (null == objLlist || objLlist.isEmpty()) {
				deal.setResult("-99");
				deal.setErrMsg("设备采集失败");
				return deal.returnXML();
			}
			
			for(ParameValueOBJ pvobj : objLlist){
				if(pvobj.getName().contains("TXPower")){
					//发射光功率
					tx_power = pvobj.getValue();
				}else if(pvobj.getName().contains("RXPower")){
					//接收光功率
					rx_power = pvobj.getValue();
				}else if(pvobj.getName().contains("TransceiverTemperature")){
					//温度
					transceiverTemperature = pvobj.getValue();
				}
				else if(pvobj.getName().contains("SupplyVottage")){
					//电压
					supplyVottage = pvobj.getValue();
				}
				else if(pvobj.getName().contains("BiasCurrent")){
					//偏置电流
					biasCurrent = pvobj.getValue();
				}
			}
			
			double tx_powerdouble = StringUtil.getDoubleValue(tx_power);
			double rx_powerdouble = StringUtil
					.getDoubleValue(rx_power);
			// 发射光功率
			if (tx_powerdouble > 30) {
				double temp_tx_power = (Math
						.log(tx_powerdouble / 10000) / Math.log(10)) * 10;
				tx_powerdouble = (int) temp_tx_power;
				if (tx_powerdouble % 10 >= 5) {
					tx_powerdouble = (tx_powerdouble / 10 + 1) * 10;
				} else {
					tx_powerdouble = tx_powerdouble / 10 * 10;
				}
			}
			// 接受功率判断
			if (rx_powerdouble > 30) {
				double temp_rx_power = (Math
						.log(rx_powerdouble / 10000) / Math.log(10)) * 10;
				rx_powerdouble = (int) temp_rx_power;
				if (rx_powerdouble % 10 >= 5) {
					rx_powerdouble = (rx_powerdouble / 10 + 1) * 10;
				} else {
					rx_powerdouble = rx_powerdouble / 10 * 10;
				}
			}
			
			tx_power = StringUtil.getStringValue(tx_powerdouble);
			rx_power = StringUtil.getStringValue(rx_powerdouble);
			// 接收光功率
			deal.setRXPower(StringUtil.getStringValue(tx_powerdouble));
			// 发射光功率
			deal.setTXPower(StringUtil.getStringValue(rx_powerdouble));
			// 采集到的光模块的工作温度
			deal.setDeviceTemperature(StringUtil.getStringValue(transceiverTemperature));
			// 采集到的光模块的供电电压
			deal.setSupplyVottage(StringUtil.getStringValue(supplyVottage));
			// 采集到的光模块的偏置电流
			deal.setBiasCurrent(StringUtil.getStringValue(biasCurrent));
			return deal.returnXML();
		}// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法采集");
			deal.setResult("-99");
			deal.setErrMsg("设备不能正常交互");
			logger.warn("return=({})", deal.returnXML());  // 打印回参
			return deal.returnXML();
		}
	}
}
