package com.linkage.itms.dispatch.util;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年7月9日
 * @category com.linkage.module.itms.config.util
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ChangeConnectionNetForAHCorba
{
	private static final Logger logger = LoggerFactory.getLogger(ChangeConnectionNetForAHCorba.class);
	String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";//..{j}.X_CT-COM_WANGponLinkConfig.VLANIDMark
	String wanServiceList = ".X_CT-COM_ServiceList";
	String wanPPPConnection = ".WANPPPConnection";
	String wanIPConnection = ".WANIPConnection";
	String gponPath = ".X_CT-COM_WANGponLinkConfig";
	String eponPath = ".X_CT-COM_WANEponLinkConfig";
	String connectionType = ".ConnectionType";
	String INTERNET = "INTERNET";
	
	public String gatherNetInfo(String devId,String gwType){
		
		String accessNodePath = "";
		ACSCorba corba = new ACSCorba(gwType);
		
		//查看设备是否在线
		int devOnlineStatus = corba.testConnection(devId,gwType);
		if(devOnlineStatus != 1 && devOnlineStatus != 0){
			logger.warn("[{}]-[{}]设备不在线",new Object[]{devId,devOnlineStatus});
			return "-1;设备不在线";
		}
		
		//查看上行方式
		String accessType = getAccessType(devId,corba);
		if("gpon".equals(accessType.toLowerCase())){
			accessNodePath = gponPath;
		}else if("epon".equals(accessType.toLowerCase())){
			accessNodePath = eponPath;
		}else{
			logger.warn("[{}]-[{}]设备的上行方式不存在vlanid",new Object[]{devId,accessType});
			return "-1;设备不存在上行方式";  
		}
		logger.warn("[{}]-[{}]-[{}]",new Object[]{devId,accessType,accessNodePath});
		
		//预读
		String wanPPPConType = "" ;
		String wanIPConType = "" ;
		String VLANIDMarkPath = "";
		List<String> jList = corba.getIList(devId, wanConnPath);
		if (jList == null || jList.size() == 0 || jList.isEmpty())
		{
			logger.warn("[{}]获取WANConnectionDevice的j失败", devId);
			return "-1;获取节点失败";
		}else{
			for(String j : jList){
				logger.warn("j:"+j);
				VLANIDMarkPath = wanConnPath + j + accessNodePath + ".VLANIDMark";
				ArrayList<ParameValueOBJ> vlanidValue = corba.getValue(devId, VLANIDMarkPath);
				if("41".equals(vlanidValue.get(0).getValue())){
					wanPPPConType =wanConnPath + j + wanPPPConnection + ".1" + ".ConnectionType";
					wanIPConType = wanConnPath + j + wanIPConnection + ".1" + ".ConnectionType";
					break;
				}
			}
		}
		
		logger.warn("wanPPPConType:{}",new Object[]{wanPPPConType});
		logger.warn("wanIPConType:{}",new Object[]{wanIPConType});
		
		boolean flag = false;
		ArrayList<ParameValueOBJ> connectType = corba.getValue(devId, wanIPConType);
		if(null == connectType || connectType.isEmpty() || connectType.size() < 1){
			connectType = corba.getValue(devId, wanPPPConType);
			flag = true;
		}
		logger.warn("connectType:{}-{}",new Object[]{flag,connectType.get(0).getValue()});
		if(connectType.get(0).getValue().indexOf("Routed") != -1){
			return "1;route;"+(flag ? wanPPPConType : wanIPConType);
		}else
			if(connectType.get(0).getValue().indexOf("Bridged") != -1){
				return "1;bridge;"+(flag ? wanPPPConType : wanIPConType);
			}else{
				logger.warn("不存在上网方式:{}",new Object[]{connectType.get(0).getValue()});
				return "-1;此设备不存在上网方式";
			}
	}


   public String getAccessType(String deviceId,ACSCorba corba) {
		
		//String accessType = dao.getDevAccessType(deviceId);
	    String accessType = "";
		if(StringUtil.IsEmpty(accessType)){
			
			String accessTypeNode = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
			ArrayList<ParameValueOBJ> accessTypeMap = corba.getValue(deviceId, accessTypeNode);
			
			if (null == accessTypeMap) {
				logger.warn("[{}]取得accessType为null，休眠3秒再取一次", deviceId);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error("InterruptedException:",e);
					// Restore interrupted state...      
					Thread.currentThread().interrupt();
				}
				accessTypeMap = corba.getValue(deviceId, accessTypeNode);
				logger.warn("[{}]第二次从设备上取得accessType为：{}",deviceId, accessTypeMap.get(0).getValue());
			}
			accessType = accessTypeMap.get(0).getValue();
			logger.warn("[{}]从设备上取得accessType为：{}",deviceId, accessType);
			
		}
		
		if("X_CT-COM_GPON".equals(accessType)){
			accessType = "GPON";	
		}
		logger.warn("[{}]设备的上行方式是[{}]",new Object[]{deviceId,accessType});
		return accessType;
	}

  public int changeConnectType(String deviceId, String connType,
			String routeAccount, String routePasswd,String path,String gw_type){
	  ACSCorba corba = new ACSCorba(gw_type);
	  ParameValueOBJ obj = null;
	  ArrayList<ParameValueOBJ> objList = new  ArrayList<ParameValueOBJ>();
	  if("1".equals(connType)){
		  obj = new ParameValueOBJ();
		  obj.setName(path);
		  obj.setValue("PPPoE_Bridged");
		  objList.add(obj);
	  }else{
		  String usernameNode = path.replace("ConnectionType", "Username");
		  ParameValueOBJ objName = new ParameValueOBJ();
		  objName.setName(usernameNode);
		  objName.setValue(routeAccount);
		  objList.add(objName);
		  
		  String passNode = path.replace("ConnectionType", "Password");
		  ParameValueOBJ objPass = new ParameValueOBJ();
		  objPass.setName(passNode);
		  objPass.setValue(routePasswd);
		  objList.add(objPass);
		  
		  obj = new ParameValueOBJ();
		  obj.setName(path);
		  obj.setValue("IP_Routed");
		  objList.add(obj);
	  }
	  logger.warn("changeConnectType=>objList:{}",new Object[]{objList});
	  return corba.setValue(deviceId, objList);
  }
}
