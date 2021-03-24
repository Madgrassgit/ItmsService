package com.linkage.itms.dispatch.service;

import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.WanConnChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import com.linkage.system.utils.database.Cursor;
import com.linkage.system.utils.database.DataSetBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * call方法的业务处理类
 * 
 * @author Jason(3412)
 * @date 2010-6-17
 */
public class WanConnService implements IService{

	private static Logger logger = LoggerFactory.getLogger(WanConnService.class);
	//WANDevice
	public static int GATHER_WAN = 2;
	public static String SERV_LIST_INTERNET = "INTERNET";
	public static String SERV_LIST_TR069 = "TR069";
	public static String SERV_LIST_VOIP = "VOIP";
	public static String SERV_LIST_OTHER = "OTHER";
	
	public static String GW_TYPE = "1";
	/* 
	 * 查询电信维护密码工作方法
	 */
	@Override
	public String work(String inXml) {
		logger.warn("WanConnService inParam:[{}]",inXml);
		String deviceId = "";
		//检查合法性
		WanConnChecker checker = new WanConnChecker(inXml);
		if(false == checker.check()){
			logger.error(
					"servicename[WanConnService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUsername(),
							checker.getReturnXml() });
			logger.warn("WanConnService returnParam:[{}]",checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		logger.warn("WanConnService 入参校验通过");
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		//获取用户帐号 or 终端序列号
		if(1 == checker.getSearchType()){
			//根据用户帐号获取
//			Map<String, String> userMap = userDevDao.getTelePasswdByUsername(checker.getUsername());
			Map<String, String> userMap = userDevDao.queryUserInfo(checker.getUserInfoType(),checker.getUsername());
			if(null == userMap || userMap.isEmpty()){
				logger.warn(
						"servicename[WanConnService]cmdId[{}]userinfo[{}]查无此用户",
						new Object[] { checker.getCmdId(), checker.getUsername()});
				checker.setResult(1002);
				checker.setResultDesc("查无此客户");
			}else{
				deviceId = userMap.get("device_id");
				checker.setOui(StringUtil.getStringValue(userMap,"oui"));
				checker.setDevSn(StringUtil.getStringValue(userMap,"device_serialnumber"));
			}
		}else if (2 == checker.getSearchType()){
			//根据终端序列号
			ArrayList<HashMap<String,String>> devlsit = userDevDao.getTelePasswdByDevSn(checker.getDevSn());
			if(null == devlsit || devlsit.isEmpty()){
				logger.warn(
						"servicename[WanConnService]cmdId[{}]DevSn[{}]无此设备",
						new Object[] { checker.getCmdId(), checker.getDevSn()});
				checker.setResult(1004);
				checker.setResultDesc("查无此设备");
			}else if(devlsit.size() > 1){
				logger.warn(
						"servicename[WanConnService]cmdId[{}]DevSn[{}]查询到多台设备",
						new Object[] { checker.getCmdId(), checker.getDevSn()});
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
			}else{
				Map<String, String> devMap = devlsit.get(0);
//				
//				String deviceCityId = devMap.get("city_id");
//				if (false == serviceHandle.cityMatch(
//						checker.getCityId(), deviceCityId)) {// 属地不匹配
//					logger.warn("属地不匹配 查无此设备：" + checker.getDevSn());
//					checker.setResult(1005);
//					checker.setResultDesc("查无此设备");
//				} else {// 属地匹配
//					checker.setResult(0);
//					checker.setResultDesc("成功");
					deviceId = devMap.get("device_id");
//				}
					checker.setOui(StringUtil.getStringValue(devMap,"oui"));
					checker.setDevSn(StringUtil.getStringValue(devMap,"device_serialnumber"));
					
					//河北的测速（桥接）需要在页面显示表中的username，不存在则自己填写
					if(Global.G_instArea.equals("hb_lt")){
						ArrayList<HashMap<String, String>> res = userDevDao.getUserNameByDevSn(checker.getDevSn());
						if(null!=res && res.size()>0){
							checker.setUsername(res.get(0).get("pppoe_name"));
						}
						else{
							checker.setUsername("");
						}
						
					}
			}
		}
		//获取wan通道
		Map<String,String> wanConnDeviceMap = this.getPingInterface(deviceId,GW_TYPE);
		checker.setWanConnDeviceMap(wanConnDeviceMap);
		if(wanConnDeviceMap==null || wanConnDeviceMap.isEmpty())
		{
			checker.setResult(1005);
			checker.setResultDesc("没有获取到WAN通道信息");
		}
		else
		{
			checker.setResult(0);
			checker.setResultDesc("成功");
		}
		String returnXml = checker.getReturnXml();
		
		//记录日志
		new RecordLogDAO().recordDispatchLog(checker, "WanConnService", checker.getUsername());
		logger.warn(
				"servicename[WanConnService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUsername(),returnXml});
		//回单
		return returnXml;
	}
	/**
	 * 获取wan
	 * @param device_id
	 * @param gw_type
	 * @return
	 */
	public Map<String,String> getPingInterface(String device_id, String gw_type)
	{
		logger.warn("getPingInterface({},{})",new Object[]{device_id,gw_type});
		Map<String,String> restMap = new HashMap<String, String>();
		logger.warn("Global.G_instArea="+Global.G_instArea);
		if (Global.G_instArea.equals("js_dx")) {
			
			// 江苏可以根据wan连接索引节点来生成上网通道
			ACSCorba corba = new ACSCorba();
			String wan_index = "InternetGatewayDevice.WANDevice.1.X_CT-COM_WANIndex";
			String wan_index_result = "";
			logger.warn("[{}]获取wan连接索引", device_id);
			ArrayList<ParameValueOBJ> valueList = corba.getValue(device_id,
					wan_index);
			if (valueList != null && valueList.size() != 0) {
				for (ParameValueOBJ pvobj : valueList) {
					if (pvobj.getName().endsWith("X_CT-COM_WANIndex")) {
						wan_index_result = pvobj.getValue();
						break;
					}
				}
				// "1.1;DHCP_Routed;45;TR069","3.1;Bridged;43;OTHER","4.1;DHCP_Routed;42;VOIP","5.1;PPPoE_Routed;312;INTERNET"
				if (!StringUtil.IsEmpty(wan_index_result)) {
					String wan[] = wan_index_result.replace("\"", "")
							.split(",");
					for (String wanPa : wan) {
						if (wanPa.endsWith(SERV_LIST_INTERNET)
								|| wanPa.endsWith("internet")) {
							if (wanPa.contains(".") && wanPa.contains(";")) {
								if (wanPa.split(";")[1].equalsIgnoreCase("PPPoE_Routed")) {
									String a = wanPa.split(";")[0].split("\\.")[0];
									String b = wanPa.split(";")[0].split("\\.")[1];
									String vlanid = wanPa.split(";")[2];
									String result = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."
											+ a
											+ ".WANPPPConnection."
											+ b
											+ ".";
									logger.warn("[{}]走wan连接索引获取上网通道[{}]",
											device_id, result);
									restMap.put(SERV_LIST_INTERNET + "###"
											+ vlanid, result);
								}
								
							}

						}
					}
					if (!restMap.isEmpty()) {
						logger.warn("restMap is:" + restMap);
						return restMap;
					}
				}
			}
			// 获取不到走采集
			logger.warn("[{}]未获取到wan连接索引值，走采集模块", device_id);
		}
		else if(Global.G_instArea.equals("hb_lt")||Global.G_instArea.equals("jl_lt")
				||Global.G_instArea.equals("jx_lt") || Global.G_instArea.equals("ah_lt")
		 		|| "zj_lt".equals(Global.G_instArea)||Global.G_instArea.equals("nx_lt")
				||Global.G_instArea.equals(Global.NMGLT)){
			ACSCorba acsCorba = new ACSCorba();
			GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
			int flag = getStatus.testDeviceOnLineStatus(device_id,
					acsCorba);
			if(flag==1){
				logger.warn("[{}]开始获取WAN通道信息", new Object[]{device_id});
				//获得WAN通道 顺便取出IP节点
				Map<String,String> wanConnDeviceMap = this.gatherWanPassageWay(device_id,acsCorba);
				if (null!= wanConnDeviceMap && !wanConnDeviceMap.isEmpty()) {
					logger.warn("restMap is:" + wanConnDeviceMap);
					return wanConnDeviceMap;
				}
			}
			else{
				logger.warn("device is not online");
			}
		}
		

		String value = "";
		String wanConnDevice = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		
		SuperGatherCorba sgCorba = new SuperGatherCorba();
		// 获取Wan
		// 1、调用采集,采集InternetGatewayDevice.WANDevice下节点
		int irt = sgCorba.getCpeParams(device_id, GATHER_WAN, 1);
		logger.warn("[{}]调用采集获取Wan的结果：" + irt,device_id);
		String errorMsg = "";
		if (irt != 1)
		{
			errorMsg = "调用采集失败";
			logger.warn("[{}]"+errorMsg,device_id);
		}
		else
		{
			// 2、从数据库获取wan_conn_id/wan_conn_sess_id
			List<Map> wanConnIds = getWanConnIds(device_id);
			if (wanConnIds == null || wanConnIds.isEmpty())
			{
				errorMsg = "没有获取到Wan接口";
				logger.warn("[{}]"+errorMsg,device_id);
			}
			else
			{

				for (Map map : wanConnIds)
				{
					String wan_conn_id = StringUtil
							.getStringValue(map.get("wan_conn_id"));
					String wan_conn_sess_id = StringUtil.getStringValue(map
							.get("wan_conn_sess_id"));
					String sessType = StringUtil.getStringValue(map.get("sess_type"));
					String serv_list = StringUtil.getStringValue(map.get("serv_list"));
					String vlanid = StringUtil.getStringValue(map.get("vlan_id"));
					String connType=StringUtil.getStringValue(map,"conn_type");
					if (sessType.equals("1"))
					{
						value = wanConnDevice + wan_conn_id + ".WANPPPConnection."
								+ wan_conn_sess_id + ".";
					}
					else if (sessType.equals("2"))
					{
						value = wanConnDevice + wan_conn_id + ".WANIPConnection."
								+ wan_conn_sess_id + ".";
					}
					else
					{
						logger.warn("[{}]sessType值不对：" + sessType,device_id);
						continue;
					}
					if(SERV_LIST_INTERNET.equals(serv_list))
					{
						if(Global.G_instArea.equals("ah_dx")){
							restMap.put(SERV_LIST_INTERNET+"###"+vlanid+"###"+connType, value);
						}else{
							restMap.put(SERV_LIST_INTERNET+"###"+vlanid, value);
						}
					}
					else if (SERV_LIST_VOIP.equals(serv_list))
					{
						restMap.put(SERV_LIST_VOIP+"###"+vlanid, value);
					}
					else if (SERV_LIST_TR069.equals(serv_list))
					{
						restMap.put(SERV_LIST_TR069+"###"+vlanid, value);
					}
					else if (SERV_LIST_OTHER.equals(serv_list))
					{
						restMap.put(SERV_LIST_OTHER+"###"+vlanid, value);
					}
					else
					{
						logger.warn("[{}]serv_list值不对：" + serv_list,device_id);
						continue;
					}

				}
			
			}
		}
		logger.warn("restMap is:"+restMap);
		return restMap;
	}
	private List getWanConnIds(String device_id)
	{
		StringBuffer sql = new StringBuffer();
		List<Map> list = new ArrayList<Map>();
		
		//oracle db
		if(1 == DBUtil.GetDB())
		{
			sql
			.append("select b.conn_type,b.sess_type,b.serv_list,a.vlan_id,to_char(a.vpi_id) || '/' || to_char(a.vci_id) pvc,b.wan_conn_id,b.wan_conn_sess_id ");
		}else
		{
			sql
			.append("select b.conn_type,b.sess_type,b.serv_list,a.vlan_id,convert(varchar,a.vpi_id)+'/'+convert(varchar,a.vci_id) pvc,b.wan_conn_id,b.wan_conn_sess_id ");
		}
		
		sql
				.append("from "+Global.G_TABLENAME_MAP.get("gw_wan_conn")+" a,"+Global.G_TABLENAME_MAP.get("gw_wan_conn_session")+" b where a.device_id=b.device_id and a.wan_conn_id=b.wan_conn_id  and a.device_id='");
		sql.append(device_id).append("'");
		PrepareSQL psql = new PrepareSQL(sql.toString());
		psql.getSQL();
		Cursor cursor = DataSetBean.getCursor(sql.toString());
		for (int i = 0; i < cursor.getRecordSize(); i++)
		{
			list.add(cursor.getRecord(i));
		}
		return list;
	}
	
	
	
	/**
	 * 获取测速路径
	 * 
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> gatherWanPassageWay(String deviceId,ACSCorba corba) {
		String SERV_LIST_INTERNET = "INTERNET";
		String SERV_LIST_TR069 = "TR069";
		String SERV_LIST_VOIP = "VOIP";
		String SERV_LIST_IPTV = "IPTV";
		String SERV_LIST_OTHER = "OTHER";
		Map<String, String> restMap = new HashMap<String, String>();
		// logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CU_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";
		String wanVlan = ".X_CU_VLAN";
		String connectionType = ".ConnectionType";

		ArrayList<String> wanConnPathsList = new ArrayList<String>();
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		logger.warn("wanConnPathsList.size:{}",wanConnPathsList.size());
		
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty()) {
			logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",
					deviceId);
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty()) {
				logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
						+ "下实例号失败，返回", deviceId);
			}
			for (String j : jList) {
				wanConnPathsList.add(wanConnPath + j + wanVlan);
				
				// 获取session，
				List<String> kPPPList = corba.getIList(deviceId, wanConnPath
						+ j + wanPPPConnection);
				if (null == kPPPList || kPPPList.size() == 0
						|| kPPPList.isEmpty()) {
					logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
							+ wanConnPath + j + wanPPPConnection + "下实例号失败",
							deviceId);
				} else {
					for (String kppp : kPPPList) {
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection
								+ kppp + wanServiceList);
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection
								+ kppp + connectionType);
					}
				}
			}
			
		}
		// serviceList节点
		ArrayList<String> serviceListList = new ArrayList<String>();
		// 所有需要采集的节点
		ArrayList<String> paramNameList = new ArrayList<String>();
		for (int i = 0; i < wanConnPathsList.size(); i++) {
			String namepath = wanConnPathsList.get(i);
			if (namepath.indexOf(wanServiceList) >= 0 || namepath.indexOf(wanVlan) >= 0 || namepath.indexOf(connectionType) >= 0) {
				serviceListList.add(namepath);
				paramNameList.add(namepath);
				continue;
			}
		}
		if (serviceListList.size() == 0 || serviceListList.isEmpty()) {
			logger.warn(
					"[TestSpeedService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回",
					deviceId);
		} else {
			String[] paramNameArr = new String[paramNameList.size()];
			int arri = 0;
			for (String paramName : paramNameList) {
				paramNameArr[arri] = paramName;
				arri = arri + 1;
			}
			Map<String, String> paramValueMap = new HashMap<String, String>();
			for (int k = 0; k < (paramNameArr.length / 20) + 1; k++) {
				String[] paramNametemp = new String[paramNameArr.length
						- (k * 20) > 20 ? 20 : paramNameArr.length - (k * 20)];
				for (int m = 0; m < paramNametemp.length; m++) {
					paramNametemp[m] = paramNameArr[k * 20 + m];
				}
				Map<String, String> maptemp = corba.getParaValueMap(deviceId,
						paramNametemp);
				if (maptemp != null && !maptemp.isEmpty()) {
					paramValueMap.putAll(maptemp);
				}
			}
			if (paramValueMap.isEmpty()) {
				logger.warn("[TestSpeedService] [{}]获取ServiceList失败",
						deviceId);
			}
			for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
				logger.debug(
						"[{}]{}={} ",
						new Object[]{deviceId, entry.getKey(), entry.getValue()});
				String paramName = entry.getKey();
				if (paramName.indexOf(wanPPPConnection) >= 0) {
				} else if (paramName.indexOf(wanIPConnection) >= 0) {
					continue;
				}
				if (paramName.indexOf(wanServiceList) >= 0) {
					if (!StringUtil.IsEmpty(entry.getValue())) {// X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径
						String res = entry.getKey().substring(0,
								entry.getKey().indexOf(wanServiceList));
						String vlanKey = entry.getKey().substring(0, entry.getKey().indexOf(wanPPPConnection)) + wanVlan;
						String vlanValue = "";
						vlanValue = paramValueMap.get(vlanKey);
						String conTypeKey = entry.getKey().substring(0, entry.getKey().indexOf(wanServiceList)) + connectionType;
						String conTypeValue = paramValueMap.get(conTypeKey);
						
						if (entry.getValue().indexOf(SERV_LIST_INTERNET) >= 0) {
							restMap.put(SERV_LIST_INTERNET+"###"+vlanValue+"###"+conTypeValue, res);
						} else if (entry.getValue().indexOf(SERV_LIST_VOIP) >= 0) {
							restMap.put(SERV_LIST_VOIP+"###"+vlanValue+"###"+conTypeValue, res);
						} else if (entry.getValue().indexOf(SERV_LIST_IPTV) >= 0) {
							restMap.put(SERV_LIST_IPTV+"###"+vlanValue+"###"+conTypeValue, res);
						} else if (entry.getValue().indexOf(SERV_LIST_TR069) >= 0) {
							restMap.put(SERV_LIST_TR069+"###"+vlanValue+"###"+conTypeValue, res);
						} else if (entry.getValue().indexOf(SERV_LIST_OTHER) >= 0) {
							restMap.put(SERV_LIST_OTHER+"###"+vlanValue+"###"+conTypeValue, res);
						}
					}
				}
			}
		}
		return restMap;
	}
	
	
	/**
	 * 获取测速路径(电信)
	 * 
	 * @param deviceId
	 * @return
	 */
	public Map<String, String> getWanPassageWay(String deviceId,ACSCorba corba) {
		String SERV_LIST_INTERNET = "INTERNET";
		String SERV_LIST_TR069 = "TR069";
		String SERV_LIST_VOIP = "VOIP";
		String SERV_LIST_IPTV = "IPTV";
		String SERV_LIST_OTHER = "OTHER";
		Map<String, String> restMap = new HashMap<String, String>();
		// logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";

		ArrayList<String> wanConnPathsList = new ArrayList<String>();
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		//wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		logger.warn("wanConnPathsList.size:{}",wanConnPathsList.size());
		
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty()) {
			logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",
					deviceId);
			wanConnPathsList = new ArrayList<String>();
			List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty()) {
				logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
						+ "下实例号失败，返回", deviceId);
			}
			for (String j : jList) {
				
				// 获取session，
				List<String> kPPPList = corba.getIList(deviceId, wanConnPath
						+ j + wanPPPConnection);
				if (null == kPPPList || kPPPList.size() == 0
						|| kPPPList.isEmpty()) {
					logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
							+ wanConnPath + j + wanPPPConnection + "下实例号失败",
							deviceId);
				} else {
					for (String kppp : kPPPList) {
						logger.warn("--------------获取成功："+wanConnPath + j + wanPPPConnection
								+ kppp + wanServiceList);
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection
								+ kppp + wanServiceList);
					}
				}
			}
			
		}
		// serviceList节点
		ArrayList<String> serviceListList = new ArrayList<String>();
		// 所有需要采集的节点
		ArrayList<String> paramNameList = new ArrayList<String>();
		for (int i = 0; i < wanConnPathsList.size(); i++) {
			String namepath = wanConnPathsList.get(i);
			if (namepath.indexOf(wanServiceList) >= 0 ) {
				serviceListList.add(namepath);
				paramNameList.add(namepath);
				logger.warn("-----------namepath="+namepath);
				continue;
			}
		}
		if (serviceListList.size() == 0 || serviceListList.isEmpty()) {
			logger.warn(
					"[TestSpeedService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回",
					deviceId);
		} else {
			String[] paramNameArr = new String[paramNameList.size()];
			int arri = 0;
			for (String paramName : paramNameList) {
				paramNameArr[arri] = paramName;
				arri = arri + 1;
			}
			Map<String, String> paramValueMap = new HashMap<String, String>();
			for (int k = 0; k < (paramNameArr.length / 20) + 1; k++) {
				String[] paramNametemp = new String[paramNameArr.length
						- (k * 20) > 20 ? 20 : paramNameArr.length - (k * 20)];
				for (int m = 0; m < paramNametemp.length; m++) {
					paramNametemp[m] = paramNameArr[k * 20 + m];
				}
				logger.warn("-----获取值paramNametemp="+paramNametemp);
				Map<String, String> maptemp = corba.getParaValueMap(deviceId,
						paramNametemp);
				logger.warn("-----获取到值maptemp="+maptemp);
				if (maptemp != null && !maptemp.isEmpty()) {
					paramValueMap.putAll(maptemp);
				}
			}
			if (paramValueMap.isEmpty()) {
				logger.warn("[TestSpeedService] [{}]获取ServiceList失败",
						deviceId);
			}
			for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
				logger.debug(
						"[{}]{}={} ",
						new Object[]{deviceId, entry.getKey(), entry.getValue()});
				String paramName = entry.getKey();
				if (paramName.indexOf(wanPPPConnection) >= 0) {
				} else if (paramName.indexOf(wanIPConnection) >= 0) {
					continue;
				}
				if (paramName.indexOf(wanServiceList) >= 0) {
					if (!StringUtil.IsEmpty(entry.getValue())) {// X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径
						String res = entry.getKey().substring(0,
								entry.getKey().indexOf(wanServiceList));
						
						if (entry.getValue().indexOf(SERV_LIST_INTERNET) >= 0) {
							restMap.put(SERV_LIST_INTERNET, res);
						} else if (entry.getValue().indexOf(SERV_LIST_VOIP) >= 0) {
							restMap.put(SERV_LIST_VOIP, res);
						} else if (entry.getValue().indexOf(SERV_LIST_IPTV) >= 0) {
							restMap.put(SERV_LIST_IPTV, res);
						} else if (entry.getValue().indexOf(SERV_LIST_TR069) >= 0) {
							restMap.put(SERV_LIST_TR069, res);
						} else if (entry.getValue().indexOf(SERV_LIST_OTHER) >= 0) {
							restMap.put(SERV_LIST_OTHER, res);
						}
					}
				}
			}
		}
		return restMap;
	}
}
