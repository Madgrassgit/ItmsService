package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.PonInfoChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
		
public class PonInfoService4NX implements IService
{
	private static final Logger logger = LoggerFactory.getLogger(PonInfoService4NX.class);
    private UserDeviceDAO userDevDao = new UserDeviceDAO();
    private String ststusPatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.Status";
    private String txPowerPatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.TXPower";
    private String rxPowerEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.RXPower";
    private String ststusPatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.Status";
    private String txPowerPatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.TXPower";
    private String rxPowerGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.RXPower";
    public String ACCESS_TYPE_PATH_DEFAULT = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
    

	@Override
	public String work(String inXml)
	{
		PonInfoChecker checker = new PonInfoChecker(inXml);
		if (!checker.check()) 
		{
			logger.error("serviceName[PonInfoService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
			new Object[] { checker.getCmdId(), checker.getUserInfo(),checker.getReturnXml() });
			return checker.getReturnXml();
		}
		//验证通过走采集流程
		logger.warn("serviceName[PonInfoService]cmdId[{}]userinfo[{}]初始参数校验通过，入参为：{}",
		new Object[] { checker.getCmdId(), checker.getUserInfo(),inXml });
		// 结果集：a.user_id,a.username,a.device_id,a.oui,a.device_serialnumber,a.city_id,a.userline,a.access_style_id
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn("serviceName[PonInfoService]cmdId[{}]userinfo[{}]无此用户",
			new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		else
		{
			String deviceId = StringUtil.getStringValue(userInfoMap,"device_id");
			if (StringUtil.IsEmpty(deviceId))
			{
				logger.warn("serviceName[PonInfoService]cmdId[{}]userinfo[{}]未绑定设备",
				new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1004);
				checker.setResultDesc("此用户未绑定设备");
				return checker.getReturnXml();
			}
			else
			{
				logger.warn("serviceName[PonInfoService]cmdId[{}]userinfo[{}]开始采集[{}]",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),deviceId});
				ACSCorba corba = new ACSCorba();
				ArrayList<ParameValueOBJ> objLlist = null;
				String accessType = UserDeviceDAO.getAccType(deviceId);
				String status = "";
				String tx_power = "0";
				String rx_power = "0";
				if(null == accessType){
					
					objLlist = corba.getValue(deviceId, ACCESS_TYPE_PATH_DEFAULT);
					if (null == objLlist || objLlist.isEmpty()) {
						checker.setResult(1006);
						checker.setResultDesc("设备采集失败");
						return checker.getReturnXml();
					}
					accessType = objLlist.get(0).getValue();
				}
				String[] gatherPath = new String[]{
						this.ststusPatchEPON,this.txPowerPatchEPON,this.rxPowerEPON,};
				if("GPON".equals(accessType))
				{
					gatherPath = new String[]{
							this.ststusPatchGPON,this.txPowerPatchGPON,this.rxPowerGPON,};
				}
				objLlist = corba.getValue(deviceId, gatherPath);
				logger.warn("{}|objList:{}",deviceId,objLlist);
				if (null == objLlist || objLlist.isEmpty()) {
					checker.setResult(1006);
					checker.setResultDesc("设备采集失败");
					return checker.getReturnXml();
				}
				
				for(ParameValueOBJ pvobj : objLlist){
					if(pvobj.getName().contains("Status")){
						//线路状态 
						status = pvobj.getValue();
					}else if(pvobj.getName().contains("TXPower")){
						//发射光功率
						tx_power = pvobj.getValue();
					}else if(pvobj.getName().contains("RXPower")){
						//接收光功率
						rx_power = pvobj.getValue();
					}
				}
				logger.warn("[{}]status[{}]txpower[{}]rxPower[{}]",new Object[] { deviceId,status,tx_power,rx_power});
				double tx_powerdouble = StringUtil.getDoubleValue(tx_power);
				double rx_powerdouble = StringUtil.getDoubleValue(rx_power);
				// 发射光功率
//				if (tx_powerdouble > 30)
//				{
					double temp_tx_power = (Math.log(tx_powerdouble / 10000) / Math
							.log(10)) * 10;
					tx_powerdouble = (int) temp_tx_power;
					if (tx_powerdouble % 10 >= 5)
					{
						tx_powerdouble = (tx_powerdouble / 10 + 1) * 10;
					}
					else
					{
						tx_powerdouble = tx_powerdouble / 10 * 10;
					}
//				}
				// 接受功率判断
				if (rx_powerdouble > 30)
				{
					double temp_rx_power = (Math.log(rx_powerdouble / 10000) / Math
							.log(10)) * 10;
					rx_powerdouble = (int) temp_rx_power;
					if (rx_powerdouble % 10 >= 5)
					{
						rx_powerdouble = (rx_powerdouble / 10 + 1) * 10;
					}
					else
					{
						rx_powerdouble = rx_powerdouble / 10 * 10;
					}
				}
				checker.setStatus(status);
				checker.setRXPower(StringUtil.getStringValue(rx_powerdouble));
				checker.setTXPower(StringUtil.getStringValue(tx_powerdouble));
			}
		}
		return checker.getReturnXml();
	}
}

	