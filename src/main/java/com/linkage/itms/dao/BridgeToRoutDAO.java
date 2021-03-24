package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.itms.cao.DeviceGatherCAO;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.dispatch.obj.LanEthObj;

/**
 * 江苏ITMS需求：JSDX_ITMS-REQ-20120220-LUHJ-004
 * 
 * 桥改路由
 * 
 * @author Administrator
 *
 */

public class BridgeToRoutDAO {

	public static final Logger logger = LoggerFactory.getLogger(BridgeToRoutDAO.class);
	
	
	/**
	 * 用于判断设备是否绑定用户，以及通过wan_type来判断改用户是否有桥接上网业务
	 * 
	 * modify by zhangchy 2011-08-24 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, String>> checkService(String device_id){
		
		logger.debug("BridgeToRoutDAO==>checkService({})", device_id);

		if(null == device_id || "".equals(device_id)){
			return null;
		}
		
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
		}
		
		List<Map<String, String>> rList = new ArrayList<Map<String,String>>();
		Map<String, String> rsMap = null;
		PrepareSQL psql = new PrepareSQL();
										 
		psql.append("select a.city_id,a.access_style_id, a.oui, a.device_serialnumber, b.vpiid, b.vciid, b.vlanid, b.username, b.passwd, b.wan_type, b.user_id ");
		psql.append("  from " + table_customer + " a, " + table_serv_info + " b ");
		psql.append(" where 1=1 ");
		psql.append("   and a.user_id = b.user_id ");
		psql.append("   and b.serv_type_id = 10 ");
		psql.append("   and a.device_id = '");
		psql.append(device_id);
		psql.append("'");
		
		List<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		
		for (HashMap<String, String> rs : list) {
			
			rsMap = new HashMap<String, String>();
			
			rsMap.put("access_style_id", StringUtil.getStringValue(rs.get("access_style_id")));
			rsMap.put("vpiid", StringUtil.getStringValue(rs.get("vpiid")));
			rsMap.put("vciid", StringUtil.getStringValue(rs.get("vciid")));
			rsMap.put("vlanid", StringUtil.getStringValue(rs.get("vlanid")));
			rsMap.put("username", StringUtil.getStringValue(rs.get("username")));
			rsMap.put("passwd", StringUtil.getStringValue(rs.get("passwd")));
			rsMap.put("wan_type", StringUtil.getStringValue(rs.get("wan_type")));
			rsMap.put("user_id", StringUtil.getStringValue(rs.get("user_id")));
			rsMap.put("city_id", StringUtil.getStringValue(rs.get("city_id")));
			rList.add(rsMap);
		}
		
		return rList;
	}
	
	
	
	
	/**
	 * 获取该设备的所有lan口和wlan口
	 * 
	 * @param deviceId
	 * @param type   type为0则先采集后获取端口；为1则先查数据库，查不到再采集
	 * @return
	 */
	public String getLanInter(String deviceId, String type){
		
		logger.debug("BridgeToRoutDAO==>getLanIntf(deviceId:{})",deviceId);
		
		int rsint;
		
		StringBuffer port = new StringBuffer();
		
		LanEthObj[] lanEthObj = null;
		
		if("0".equals(type)){
			rsint = getSuperCorba(deviceId, DeviceGatherCAO.GATHER_LAN_ETHERNET);
			if ( rsint == 1 ) {
				lanEthObj = getLanEthObj(deviceId);
			}else{
				logger.debug("LAN口获取失败");
				return "端口获取失败！";
			}
		}else if("1".equals(type)){
			lanEthObj = getLanEthObj(deviceId);
			if(null==lanEthObj){
				//SG
				rsint = getSuperCorba(deviceId, DeviceGatherCAO.GATHER_LAN_ETHERNET);
				if ( rsint == 1 ) {
					lanEthObj = getLanEthObj(deviceId);
				}else{
					logger.debug("LAN口获取失败");
					return "端口获取失败！";
				}
			}			
		}else{
			logger.debug("传入type错误");
			return "端口获取失败！";
		}
		
		if(null != lanEthObj){
			for(int i=0;i<lanEthObj.length;i++){
				port.append("InternetGatewayDevice.LANDevice.");
				port.append(lanEthObj[i].getLanid());
				port.append(".LANEthernetInterfaceConfig.");
				port.append(lanEthObj[i].getLanEthid());
				port.append(",");
			}
		}
		
		List<Map<String, String>> wlanList = null;
		
		if("0".equals(type)){
			rsint = getSuperCorba(deviceId, DeviceGatherCAO.GATHER_LAN_WLAN);
			if ( rsint == 1 ) {
				wlanList = getWlan(deviceId);
			}else{
				logger.debug("WLAN口获取失败");
				return "端口获取失败！";
			}
		}else if("1".equals(type)){
			wlanList = getWlan(deviceId);
			if(null == wlanList || wlanList.isEmpty() || wlanList.size()<=0){
				//SG
				rsint = getSuperCorba(deviceId, DeviceGatherCAO.GATHER_LAN_WLAN);
				if ( rsint == 1 ) {
					wlanList = getWlan(deviceId);
				}else{
					logger.debug("WLAN口获取失败");
					return "端口获取失败！";
				}
			}			
		}else{
			logger.debug("传入type错误");
			return "端口获取失败！";
		}
		for(int i=0;i<wlanList.size();i++){
			
			Map<String, String> oneWlanMap = (Map<String, String>) wlanList.get(i);
			
			port.append("InternetGatewayDevice.LANDevice.");
			port.append(oneWlanMap.get("lan_id"));
			port.append(".WLANConfiguration.");
			port.append(oneWlanMap.get("lan_wlan_id"));
			port.append(",");
			
		}
		
		return port.toString();
	}
	
	
	/**
	 * 调用采集模块
	 * 
	 * @param deviceId
	 * @param code
	 * @return
	 */
	public int getSuperCorba(String deviceId, int code){
		
		logger.debug("BridgeToRoutDAO==>getSuperCorba(deviceId:{},code{})",deviceId,code);
		
		SuperGatherCorba superGatherCorba = new SuperGatherCorba();
		
		return superGatherCorba.getCpeParams(deviceId, code, 0);  // "0" 表示在5分钟之内采集过，则不需要再采集
	}
	
	
	
	
	/**
	 * 返回设备的所有状态为state的结点信息
	 * 
	 * @param
	 * @return LanEthObj[]
	 */
	public LanEthObj[] getLanEthObj(String deviceId){
		
		logger.debug("BridgeToRoutDAO==>getLanEthObj({})", new Object[]{deviceId});
		
		LanEthObj[] lanEthArr = null;
		
		if (true == StringUtil.IsEmpty(deviceId)){
			logger.warn("getLanEthObj(deviceId): is null or ''");
		} else {
			
			PrepareSQL psql = new PrepareSQL();
			
			if (3 == DBUtil.GetDB()) {
				psql.append("select lan_id,lan_eth_id,enable,status,mac_address,gather_time from gw_lan_eth ");
			} else {
				psql.append("select * from gw_lan_eth ");
			}
			psql.append(" where 1=1 ");
			psql.append("   and device_id = '");
			psql.append(deviceId);
			psql.append("'");
			
			psql.append(" order by lan_eth_id");
			
			List<HashMap<String, String>> rList = DBOperation.getRecords(psql.getSQL());
			
			if (null == rList || rList.size() < 1) {
				logger.warn("getLanEthObj(deviceId): 没有该deviceId对应的lan结点记录");
			} else {
				
				int size = rList.size();
				
				lanEthArr = new LanEthObj[size];
				
				for (int i = 0; i < size; i++) {
					
					Map<String, String> tMap = (Map<String, String>) rList.get(i);
					
					lanEthArr[i] = new LanEthObj();
					lanEthArr[i].setLanid(StringUtil.getIntegerValue(tMap.get("lan_id")));
					lanEthArr[i].setLanEthid(StringUtil.getIntegerValue(tMap.get("lan_eth_id")));
					lanEthArr[i].setEnable(StringUtil.getStringValue(tMap.get("enable")));
					lanEthArr[i].setStatus(StringUtil.getStringValue(tMap.get("status")));
					lanEthArr[i].setMac(StringUtil.getStringValue(tMap.get("mac_address")));
					lanEthArr[i].setGatherTime(StringUtil.getLongValue(tMap, "gather_time"));
				}
			}
		}
		return lanEthArr;
	}
	
	
	
	/**
	 * 获取WLAN
	 * 
	 * @param device_id
	 * @return
	 */
	public List<Map<String, String>> getWlan(String deviceId) {
		
		logger.debug("BridgeToRoutDAO==>getWlan({})", deviceId);
		
		if (deviceId == null) {
			logger.debug("deviceId == null");
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL();
		
		if (3 == DBUtil.GetDB()) {
			psql.append("select lan_id,lan_wlan_id from gw_lan_wlan where device_id='" + deviceId + "'");
		}
		else {
			psql.append("select * from gw_lan_wlan where device_id='" + deviceId + "'");
		}
		
		List<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		
		List<Map<String, String>> rList = new ArrayList<Map<String,String>>();
		Map<String, String> rsMap = null;
		
		for (HashMap<String, String> rs : list) {
			rsMap = new HashMap<String, String>();
			rsMap.put("lan_id", StringUtil.getStringValue(rs.get("lan_id")));
			rsMap.put("lan_wlan_id", StringUtil.getStringValue(rs.get("lan_wlan_id")));
			rList.add(rsMap);
		}
		return rList;
	}
	
	
	/**
	 * 下发桥改路由配置
	 * 
	 * @param deviceId
	 * @param routeAccount
	 * @param routePasswd
	 * @param pvc
	 * @param vlan
	 * @param accessType
	 * @param user_id
	 * @param deviceserialnumber
	 * @param oui
	 * @param bindPort
	 * @return
	 */
	public String changeConnectionType(String deviceId, String routeAccount,
			String routePasswd, String pvc, String vlan, String accessStyleId,
			String user_id, String deviceserialnumber, String oui,
			String bindPort) {
		
		logger.debug("BridgeToRoutDAO==>changeConnectionType({},{},{},{},{},{},{},{},{},{})",
						new Object[] { deviceId, routeAccount, routePasswd,
								pvc, vlan, accessStyleId, user_id,
								deviceserialnumber, oui, bindPort });
		
		String msg = "";
		
		if(null != accessStyleId && !"".equals(accessStyleId)){
			
			if("1".equals(accessStyleId) || "2".equals(accessStyleId) || "3".equals(accessStyleId)){
				
				int flag = updatePvc(user_id, routeAccount, routePasswd, pvc, vlan, bindPort);
				
				if(flag == -1){
					msg = "-1;配置操作失败!";
				}else {   // UPDATE成功，则进行业务下发
					// 预读调用对象
					PreServInfoOBJ preInfoObj = new PreServInfoOBJ(user_id, deviceId, oui, deviceserialnumber, "10", "1");
					if (1 == CreateObjectFactory.createPreProcess().processServiceInterface(CreateObjectFactory.createPreProcess().GetPPBindUserList(preInfoObj))) {
						logger.warn("设备 "+deviceId+" 下发桥改路由配置，调用后台预读模块成功");
						msg = "1;配置操作成功!";
					} else {
						logger.warn("设备 "+deviceId+" 下发桥改路由配置，调用后台预读模块失败");
						msg = "-1;调用后台预读模块失败!";
					}
				}
			}else {
				logger.warn("未知上行方式");
				msg = "-1;未知上行方式";
			}
		}else {
			logger.warn("未知上行方式");
			msg = "-1;未知上行方式";
		}
		return msg;

	}
	
	
	
	/**
	 * 更新数据库
	 * 
	 * @param user_id
	 * @param routeAccount
	 * @param routePasswd
	 * @param pvc
	 * @param vlan
	 * @param bindPort
	 * @return
	 */
	public int updatePvc(String user_id, String routeAccount,
			String routePasswd, String pvc, String vlan, String bindPort) {
		
		logger.debug("BridgeToRoutDAO==>updatePvc({},{},{},{},{},{})",
				new Object[] { user_id, routeAccount, routePasswd, pvc, vlan,
						bindPort });
		
		if(null != user_id && !"".equals(user_id)){
			
			String[] pvcArr = null;
		
			if(!"".equals(pvc) && null != pvc){
				pvcArr = pvc.split("/");
			}
			
			PrepareSQL psql = new PrepareSQL();
			
			psql.append("update hgwcust_serv_info ");
			psql.append("   set wan_type = 2, open_status = 0 ");
			
			if(null != bindPort && !"".equals(bindPort)){
				psql.append(",bind_port = '"+bindPort+"'");
			}
			if(null != routeAccount && !"".equals(routeAccount)){
				psql.append(",username = '"+routeAccount+"'");
			}
			if(null != routePasswd && !"".equals(routePasswd)){
				psql.append(",passwd = '"+routePasswd+"'");
			}
			if(null != pvcArr && pvcArr.length==2){
				if(null != StringUtil.getStringValue(pvcArr[0]) && !"".equals(StringUtil.getStringValue(pvcArr[0]))){
					psql.append(",vpiid = '"+StringUtil.getStringValue(pvcArr[0])+"'");
				}
				if(null != StringUtil.getStringValue(pvcArr[1]) && !"".equals(StringUtil.getStringValue(pvcArr[1]))){
					psql.append(",vciid = "+StringUtil.getStringValue(pvcArr[1]));
				}
			}
			if(null != vlan && !"".equals(vlan)){
				psql.append(",vlanid = '"+vlan+"'");
			}
			
			psql.append(" where serv_type_id = 10 and user_id = "+user_id);
			
			return DBOperation.executeUpdate(psql.getSQL());
		}else{
			logger.warn("操作失败！user_id为空值或为null！");
			return -1;
		}
		
	}
	
	
	
	/**
	 * 获取设备信息，包含状态信息
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-9-2
	 * @return Map<String,String>
	 */
	public ArrayList<HashMap<String, String>> getDevStatusInfo(String devId) {
		logger.debug("BridgeToRoutDAO==>getDevStatusInfo({})", devId);

		if (StringUtil.IsEmpty(devId)) {
			logger.warn("devId is empty!");
			return null;
		}

		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select b.device_id,b.vendor_id,b.device_model_id,b.oui,b.device_serialnumber,b.devicetype_id, c.online_status, b.city_id");
		psql.append(" from tab_gw_device b left join gw_devicestatus c on b.device_id = c.device_id ");
		psql.append(" where 1=1 ");
		psql.append("   and b.device_id = '"+devId+"'");
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
}
