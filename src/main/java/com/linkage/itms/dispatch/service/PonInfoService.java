package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.PonInfoChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
		
public class PonInfoService implements IService
{
	private static final Logger logger = LoggerFactory.getLogger(PonInfoService.class);
    private UserDeviceDAO userDevDao = new UserDeviceDAO();
    private DeviceConfigDAO deviceConfigDao = new DeviceConfigDAO();

	@Override
	public String work(String inXml)
	{
		//修改处
		QueryDevDAO qdDao = new QueryDevDAO();
		ACSCorba corba = new ACSCorba();
		
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
			//修改处
			String userId=StringUtil.getStringValue(userInfoMap,"user_id");
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
				//掉CORBAR 采集    0表示采集所有节点 在原来基础上增加了一个参数(3)
				int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3); 
				logger.warn(
						"serviceName[PonInfoService]cmdId[{}]userinfo[{}]getCpeParams设备配置信息采集结果[{}]",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),rsint});
				// 采集失败
				if (rsint != 1)
				{
					logger.warn(
							"serviceName[PonInfoService]cmdId[{}]userinfo[{}]getData sg fail",
							new Object[] { checker.getCmdId(), checker.getUserInfo()});
					checker.setResult(1006); 
					checker.setResultDesc("设备采集失败");
					
				}
				else//success
				{
					//获取PON信息
					Map<String,String> ponInfoMap = deviceConfigDao.getPonInfo(deviceId);
					if(null !=ponInfoMap)
					{
						//修改处 os_jiangk
						if("hb_dx".equals(Global.G_instArea))
						{
						String accessType = StringUtil.getStringValue(qdDao.queryAccessType(userId),"adsl_hl");
						String path = "X_CT-COM_GponInter";
						if("3.0".equals(accessType)){
							path = "X_CT-COM_EponInter";
						}
						if("CUC".equalsIgnoreCase(Global.G_OPERATOR)){
							path = "X_CU_WANGPONInter";
							if("3".equals(accessType) || "3.0".equals(accessType)){
								path = "X_CU_WANEPONInter";
							}
						}
						else if("CMCC".equalsIgnoreCase(Global.G_OPERATOR)){
							
						}
						String[] gatherPath = new String[]{
								"InternetGatewayDevice.WANDevice.1." + path + "faceConfig.TransceiverTemperature"};
						String[] gatherPath1 = new String[]{
								"InternetGatewayDevice.WANDevice.1." + path + "afceConfig.TransceiverTemperature"};
						
						if("CUC".equalsIgnoreCase(Global.G_OPERATOR)){
							gatherPath = new String[]{
									"InternetGatewayDevice.WANDevice.1." + path + "faceConfig.OpticalTransceiver.Temperature"};
						}
						else if("CMCC".equalsIgnoreCase(Global.G_OPERATOR)){
							
						}
						ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath[0]);
						if("CTC".equalsIgnoreCase(Global.G_OPERATOR)){
							logger.warn("运营商是：CTC");
							if (null == objLlist || objLlist.isEmpty()) {
								objLlist = corba.getValue(deviceId, gatherPath1[0]);
								if(null == objLlist || objLlist.isEmpty()){
									path = "X_CT-COM_EponInter";
									objLlist = corba.getValue(deviceId, gatherPath[0]);
									if(null == objLlist || objLlist.isEmpty()){
										objLlist = corba.getValue(deviceId, gatherPath1[0]);
										if(null == objLlist || objLlist.isEmpty()){
											checker.setResult(1006);
											checker.setResultDesc("设备采集失败");
											checker.setTemperature("");
											logger.warn("return=({})", checker.getReturnXml());  // 打印回参
											return checker.getReturnXml();
										}
									}
								}
							}
						}
						else if("CUC".equalsIgnoreCase(Global.G_OPERATOR)){
							if(null == objLlist || objLlist.isEmpty()){
								checker.setResult(1006);
								checker.setResultDesc("设备采集失败");
								checker.setTemperature("");
								logger.warn("return=({})", checker.getReturnXml());  // 打印回参
								return checker.getReturnXml();
							}
						}
						else if("CMCC".equalsIgnoreCase(Global.G_OPERATOR)){
							
						}
						//光猫温度
						checker.setTemperature(objLlist.get(0).getValue());
						}
						//接收光功率
						checker.setRXPower(StringUtil.getStringValue(ponInfoMap,"rx_power",""));
						//发射光功率
					    checker.setTXPower(StringUtil.getStringValue(ponInfoMap,"tx_power",""));
					    //线路状态 chenxj6
					    checker.setStatus(StringUtil.getStringValue(ponInfoMap,"status",""));
					}
					else{
						checker.setResult(1000); 
						checker.setResultDesc("未知错误");
					}
					return checker.getReturnXml();
				}
				
			}
		}
		return checker.getReturnXml();
			
	}
	
}

	