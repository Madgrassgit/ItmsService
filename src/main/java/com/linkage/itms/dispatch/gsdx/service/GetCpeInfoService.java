package com.linkage.itms.dispatch.gsdx.service;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.gsdx.beanObj.CpeInfo;
import com.linkage.itms.dispatch.gsdx.beanObj.CpeInfoRst;
import com.linkage.itms.dispatch.gsdx.beanObj.Para;
import com.linkage.itms.dispatch.gsdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.gsdx.obj.GetCpeInfoXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * getCpeInfo
 * @author zhaixx
 *
 */
public class GetCpeInfoService  extends ServiceFather{

	public GetCpeInfoService(String methodName)
	{
		super(methodName);
	}
	private static Logger logger = LoggerFactory.getLogger(GetCpeInfoService.class);
    private String ststusPatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.Status";
    private String txPowerPatchEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.TXPower";
    private String rxPowerEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.RXPower";
    private String packetsReceivedEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.Stats.PacketsReceived";
    private String packetsSendEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.Stats.PacketsSent";
    private String transceiverEPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.TransceiverTemperature";
    
    private String ststusPatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.Status";
    private String txPowerPatchGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.TXPower";
    private String rxPowerGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.RXPower";
    // 接收侦个数
    private String packetsReceivedGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.Stats.PacketsReceived";
    private String packetsSentGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.Stats.PacketsSent";
    private String transceiverGPON = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.TransceiverTemperature";
    public String ACCESS_TYPE_PATH_DEFAULT = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
    private ACSCorba corba = new ACSCorba();
    private GetCpeInfoXML dealXML = new GetCpeInfoXML(methodName);
    private CpeInfoRst result = new CpeInfoRst();
    private GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
	private CpeInfo info = new CpeInfo();
	public CpeInfoRst work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		if(null == dealXML.getXML(inXml)){
			result.setiOpRst(-1);
			return result;
		}
		
		CpeInfoDao dao = new CpeInfoDao();

		 Map<String, String> queryUserInfo = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}",queryUserInfo);
		
		if(null == queryUserInfo || queryUserInfo.size()==0){
			result.setiOpRst(0);
			return result;
		}
		String device_serialnumber = StringUtil.getStringValue(queryUserInfo, "oui") +
				"-" + StringUtil.getStringValue(queryUserInfo, "device_serialnumber");
		String deviceId = StringUtil.getStringValue(queryUserInfo, "device_id") ;
		if(StringUtil.isEmpty(deviceId)){
			result.setiOpRst(0);
			return result;
		}

		info.setDeviceID(device_serialnumber);

		info.setAccessNo(StringUtil.getStringValue(queryUserInfo, "ppp_usename"));

		info.setUserId(StringUtil.getStringValue(queryUserInfo, "loid"));

		info.setDeviceIP(StringUtil.getStringValue(queryUserInfo, "loopback_ip"));

		String InterfaceType = dealXML.getInterfaceType();
		// 判断InterfaceType
		String[] split = InterfaceType.split(",");
		ArrayList<Para> paraList = new ArrayList<Para>();
		if(null != split && split.length>0){
			for (String Interface : split) {
				/*getCpeOnlineStatus	查询终端/家庭网关在线状态信息 **/
				if("getCpeOnlineStatus".equals(Interface)){
					Map<String, String> devMapss= dao.getDevStatus(StringUtil.getStringValue(queryUserInfo,"user_id"));
					if(null == devMapss || devMapss.isEmpty()){
						//成功但是没有结果
						result.setiOpRst(0);
					}
					else{
						paraList.add(setPara("onlineStatus", devMapss.get("online_status")));
						paraList.add(setPara("registerTime", getTime(devMapss.get("last_time"))));
						result.setiOpRst(1);
					}
				}
				/*getCpeBasicInfo	查询终端/家庭网关基本信息及注册信息。	**/
				if("getCpeBasicInfo".equals(Interface)){
					getCpeBasicInfo(deviceId,result,paraList,queryUserInfo);
				}
				
				/*	getCpeConfigInfo	查询终端/家庭网关配置信息。	**/
				if("getCpeConfigInfo".equals(Interface)){
					getCpeConfigInfo(deviceId,result,paraList,queryUserInfo);
				}
				// 设备在线
				/*
				getCpeWanInfo
				查询终端/家庭网关WAN口信息。
				**/
				if("getCpeWanInfo".equals(Interface)){
					getCpeWanInfo(deviceId,corba,paraList);
				}
				/*
				getCpeLanInfo
				查询终端/家庭网关LAN口信息
				**/
				if("getCpeLanInfo".equals(Interface)){
					getCpeLanInfo(deviceId,corba,paraList);
				}
			
				/*
				getCpeVoipRegStatus
				查询语音接口状态信息（仅供甘肃电信局点使用）。
				**/
				if("getCpeVoipRegStatus".equals(Interface)){
					getCpeVoipRegStatus(deviceId, corba, paraList,StringUtil.getStringValue(queryUserInfo,"user_id"));
				}
				/*
				getCpeVoipVlanValue
				获取语音端口VLAN值（仅供甘肃电信局点使用）。
				**/
				if("getCpeVoipVlanValue".equals(Interface)){
					getCpeVoipVlanValue(deviceId, corba, paraList);
				}

				Para[] array = (Para[])paraList.toArray(new Para[paraList.size()]);
				info.setParaList(array);
				if(array.length>0){
					result.setiOpRst(1);
				}else{
					result.setiOpRst(0);
				}
				result.setCpeInfo(info);
			}
		}	
		
		return result;
	}
	
	private void getCpeVoipVlanValue(String deviceId, ACSCorba corba, ArrayList<Para> paraList)
	{
		// 查询设备是epon
		ArrayList<ParameValueOBJ> objLlist = null;
		String accessType = UserDeviceDAO.getAccType(deviceId);
		logger.warn("accessType:{}",accessType);
		if(null == accessType){
			objLlist = corba.getValue(deviceId, ACCESS_TYPE_PATH_DEFAULT);
			logger.warn("[{}]获取objLlist成功，objLlist.size={}", deviceId, objLlist.size());
			if (null == objLlist || objLlist.isEmpty()) {
				result.setiOpRst(-1);
				return;
			}
			accessType = objLlist.get(0).getValue();
		}
		//采集accessType
		String checkAccessType = null;
		if("EPON".equals(accessType))
		{
			checkAccessType = ".X_CT-COM_WANEponLinkConfig";
		}
		else if("GPON".equals(accessType))
		{
			checkAccessType = ".X_CT-COM_WANGponLinkConfig";
		}		
		String wanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		List<String> vlanList = corba.getIList(deviceId, wanPath);
		List<String> vlanIdList = new ArrayList<String>();
		for (String i : vlanList)
		{
			vlanIdList.add(wanPath + i + checkAccessType + ".VLANIDMark");
		}
		String[] gatherPath = vlanIdList.toArray(new String[vlanIdList.size()]);
		ArrayList<ParameValueOBJ> vlanlist = corba.getValue(deviceId, gatherPath);
		if (null != vlanlist) 
		{
			for(ParameValueOBJ pvobj : vlanlist){
				if(pvobj.getValue().equals("45")){
					paraList.add(setPara(pvobj.getName(), pvobj.getValue()));
				}
			}
		}
	}

	private void getCpeVoipRegStatus(String deviceId, ACSCorba corba, ArrayList<Para> paraList,String userId)
	{
		CpeInfoDao dao = new CpeInfoDao();
		Map<String, String> voipMap = dao.queryVoipInfo(userId);
		String lindId = StringUtil.getStringValue(voipMap, "line_id");
		if(null == lindId || lindId.isEmpty())
		{
			lindId = "1";
		}
		ArrayList<ParameValueOBJ> objLlist = null;
		String[] gatherPath = new String[]{"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line." + lindId + ".Status"};
		objLlist = corba.getValue(deviceId, gatherPath);
		logger.warn("deviceId:{}|objList:{}",deviceId,objLlist);
		if (null == objLlist || objLlist.isEmpty()) {
			logger.warn("[{}]获取iList失败，返回", deviceId);
			result.setiOpRst(-1);
			return;
		}

		for(ParameValueOBJ pvobj : objLlist){
			paraList.add(setPara(pvobj.getName(),pvobj.getValue()));
		}
		logger.warn("[{}]获取paraList，返回", paraList);

	
	}

	private void getCpeConfigInfo(String deviceId, CpeInfoRst result2, ArrayList<Para> paraList, Map<String, String> queryUserInfo) {
		// WBANDINFO_VLAN	String SIZE(254)	上网VLAN
	/*	WBAND_IPTVINFO_VLAN	String SIZE(254)	IPTV VLAN
		WBANDINFO_CONNECTTYPE	String SIZE(254)	用户连接方式（路由、桥接）
		SERVICE_IGMPENABLE	String SIZE(254)	单主播配置信息   iptv业务表
		SERVICE_LANINTERFACE	String SIZE(254)	VLAN与端口绑定信息
		LANINFO_LAN_DHCPENABLE	String SIZE(254)	DHCP功能开放情况
		WLAN_ENABLE	String SIZE(254)	WIFI是否启用
		此项信息从数据库查询即可，无需从设备采集，不过要判断是否在线，不在线要返回设备不在线*/
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			result.setiOpRst(0);
			return;
		}
		String wantype = StringUtil.getStringValue(queryUserInfo, "wan_type");
		if(wantype.equals("5"))
		{
			paraList.add(setPara("WBANDINFO_VLAN", "41"));
			paraList.add(setPara("WBANDINFO_VLAN", "41"));
			paraList.add(setPara("WBANDINFO_CONNECTTYPE", "PPPoE_Bridged"));
			paraList.add(setPara("WBANDINFO_CONNECTTYPE", "IP_Routed"));
			paraList.add(setPara("SERVICE_LANINTERFACE", StringUtil.getStringValue(queryUserInfo, "net_port")));
			paraList.add(setPara("SERVICE_LANINTERFACE", "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1."));
		}
		else if(wantype.equals("1"))
		{
			paraList.add(setPara("WBANDINFO_VLAN", "41"));
			paraList.add(setPara("WBANDINFO_CONNECTTYPE", "PPPoE_Bridged"));
			paraList.add(setPara("SERVICE_LANINTERFACE", StringUtil.getStringValue(queryUserInfo, "net_port")));
		}
		else if(wantype.equals("2"))
		{
			paraList.add(setPara("WBANDINFO_VLAN", "41"));
			paraList.add(setPara("WBANDINFO_CONNECTTYPE", "IP_Routed"));
			paraList.add(setPara("SERVICE_LANINTERFACE", StringUtil.getStringValue(queryUserInfo, "net_port")));
		}
		String iptvName = StringUtil.getStringValue(queryUserInfo, "iptv_name");
		if(!iptvName.isEmpty())
		{
			paraList.add(setPara("WBAND_IPTVINFO_VLAN", "43"));
		}
		paraList.add(setPara("WLAN_ENABLE", "-"));

		String lanDevice = "InternetGatewayDevice.LANDevice.";
		List<String> lanList = corba.getIList(deviceId, lanDevice);
		List<String> lanIdList = new ArrayList<String>();
		for (String i : lanList)
		{
			lanIdList.add(lanDevice + i + ".X_HW_WlanEnable");
		}
		String[] gatherPath = lanIdList.toArray(new String[lanIdList.size()]);

		ArrayList<ParameValueOBJ> lanIdObjlist = corba.getValue(deviceId, gatherPath);
		if(lanIdObjlist!=null&&lanIdObjlist.size()>0) {
			for (ParameValueOBJ pvobj : lanIdObjlist) {
				String pvobjStr = pvobj.getValue()==null?"-":pvobj.getValue();
				paraList.add(setPara("WLAN_ENABLE", "-"));
			}
		}else{
			paraList.add(setPara("WLAN_ENABLE", "-"));
		}


		String[] gatherPathArray =  new String[]{"InternetGatewayDevice.Services.X_CT-COM_IPTV.IGMPEnable","InternetGatewayDevice.LANDevice.1.LANHostConfigManagement.DHCPServerEnable"};
		ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPathArray);

		if(null != objLlist && !objLlist.isEmpty() && objLlist.size() > 0){
			for(ParameValueOBJ pvobj : objLlist) {
				if (("InternetGatewayDevice.Services.X_CT-COM_IPTV.IGMPEnable").equals(pvobj.getName()) ) {
					paraList.add(setPara("SERVICE_IGMPENABLE", pvobj.getValue()));
				}
				if (("InternetGatewayDevice.LANDevice.1.LANHostConfigManagement.DHCPServerEnable").equals(pvobj.getName())) {
					paraList.add(setPara("LANINFO_LAN_DHCPENABLE", pvobj.getValue()));
				}
			}
		}

		/*
		String connectDevGather = connectDevGather(deviceId);
		if("9999".equals(connectDevGather)){
			result.setiOpRst(0);
			paraList.add(setPara("WLAN_ENABLE","0"));
			return;
		}else{
			paraList.add(setPara("WLAN_ENABLE","0"));
			result.setiOpRst(1);
		}*/
	}

	/**
	 * 根据设备id查询wifi开关是否打开
	 * @param deviceId 设备id
	 */
	@SuppressWarnings("unused")
	private String connectDevGather(String deviceId)
	{
			//操作设备
			int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
			// 设备正在被操作，不能获取节点值
			if (1 != flag) {
				logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
				return "9999";
			}
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备在线开始采集，device_id={}", deviceId);
			String lanPath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.";
			// 获取全部路径
			ArrayList<String> landevicePathsList = new ArrayList<String>();
			landevicePathsList = corba.getParamNamesPath(deviceId, lanPath, 0);
			if (landevicePathsList == null || landevicePathsList.size() == 0 || landevicePathsList.isEmpty()){
				return "9999";
			}
			else
			{
				ArrayList<String> paramNameList = new ArrayList<String>();
				for (int i = 0; i < landevicePathsList.size(); i++)
				{
					String namepath = landevicePathsList.get(i);
					if (namepath.indexOf(".Enable") > 0){
						paramNameList.add(namepath);
					}
				}
				
				if(paramNameList.size() > 0){
					String[] gatherPathArray = new String[paramNameList.size()];
					paramNameList.toArray(gatherPathArray);
					// 处理设备采集结果
					ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPathArray);
					if (null == objLlist || objLlist.isEmpty()){
						logger.warn("{} [{}],采集wifi使能失败，device_id={}",methodName, dealXML.getOpId(),deviceId);
						return "9999";
					}
					else{
						for (ParameValueOBJ pvobj : objLlist){
							if ("1".equals(pvobj.getValue())){
								return pvobj.getValue();
							}
						}
					}
				}
				return "9999";
			}
		}

	private void getCpeBasicInfo(String deviceId, CpeInfoRst result2, ArrayList<Para> paraList, Map<String, String> queryUserInfo) {
		CpeInfoDao dao = new CpeInfoDao();
		Map<String, String> infoMap = dao.getDeviceVersion(deviceId);
		Map<String, String> servTimeMap = dao.getServResultInfo(deviceId);
		if (null == infoMap || infoMap.isEmpty())
		{
			result2.setiOpRst(0);
			return;
		}
		String vendorAdd = StringUtil.getStringValue(infoMap, "vendor_add");
		if ("".equals(vendorAdd))
		{
			vendorAdd = StringUtil.getStringValue(infoMap, "vendor_name");
		}
		// 设备厂商
		paraList.add(setPara("Factory", vendorAdd));
		// 设备型号
		paraList.add(setPara("Type", StringUtil.getStringValue(infoMap, "device_model")));
		// 软件版本
		paraList.add(setPara("hwVersion",  StringUtil.getStringValue(infoMap,"hardwareversion")));
		// 硬件版本
		paraList.add(setPara("swVersion", StringUtil.getStringValue(infoMap,"softwareversion")));
		// 注册失败原因
		paraList.add(setPara("FailReason", ""));
		// 终端入网时间
		paraList.add(setPara("deviceEnterNetTime", getTime(StringUtil.getStringValue(infoMap, "complete_time"))));
		// 终端绑定时间
		paraList.add(setPara("deviceBindTime", getTime(StringUtil.getStringValue(queryUserInfo, "binddate"))));
		//终端注册业务时间
		paraList.add(setPara("deviceRegistTime", getTime(StringUtil.getStringValue(servTimeMap, "end_time"))));

	}
	
	public String getTime(String time)
	{
		if(null == time || time.isEmpty())
		{
			return "";
		}
		long timeStamp = StringUtil.getLongValue(time) * 1000L;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sd = sdf.format(new Date(timeStamp));  
		return sd;
	}

	/**
	 * 查询Lan--info
	 * @param deviceId
	 * @param paraList
	 */
	private void getCpeLanInfo(String deviceId, ACSCorba corba, ArrayList<Para> paraList) {
		//操作设备
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		// 设备正在被操作，不能获取节点值
		if (1 != flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			result.setiOpRst(0);
			return;
		}
		logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			
		ArrayList<ParameValueOBJ> numberlist = corba.getValue(deviceId, "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceNumberOfEntries");
		if (null == numberlist || numberlist.isEmpty()) {
			result.setiOpRst(-1);
			return;
		}

		paraList.add(setPara("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceNumberOfEntries", numberlist.get(0).getValue()));
	    String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
	    List<String> iList = corba.getIList(deviceId, lanPath);
		if (null == iList || iList.isEmpty()) {
			logger.warn("[{}]获取iList失败，返回", deviceId);
			result.setiOpRst(-1);
			return;
		}
		logger.warn("[{}]获取iList成功，iList.size={}", deviceId, iList.size());
		for(String i : iList){
			String[] gatherPath = new String[]{
				"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Status",
				"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Name",
				"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".MaxBitRate"
			};

			for (int j = 0; j <gatherPath.length ; j++) {

				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath[j]);
				if (null == objLlist || objLlist.isEmpty()) {
					continue;
				}
				logger.debug("[{}]获取objLlist成功，path={},objLlist.size={}", deviceId,gatherPath[j], objLlist.size());
				for(ParameValueOBJ pvobj : objLlist){
					if(pvobj.getName().contains("Status")){
						paraList.add(setPara("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Status", pvobj.getValue()));
					}
					else if(pvobj.getName().contains("Name")){
						paraList.add(setPara("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Name", pvobj.getValue()));
					}
					else if(pvobj.getName().contains("MaxBitRate")){
						paraList.add(setPara("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".MaxBitRate",pvobj.getValue()));
					}
				}

			}


		}
	}



	/**
	 * 查询wan-info
	 * @param deviceId
	 * @param paraList
	 */
	private void getCpeWanInfo(String deviceId, ACSCorba corba, ArrayList<Para> paraList) {
		// 查询设备是epon
		ArrayList<ParameValueOBJ> objLlist = null;
		String accessType = UserDeviceDAO.getAccType(deviceId);
		logger.warn("accessType:{}",accessType);
		if(null == accessType){
			objLlist = corba.getValue(deviceId, ACCESS_TYPE_PATH_DEFAULT);
			if (null == objLlist || objLlist.isEmpty()) {
				result.setiOpRst(-1);
				return;
			}
			logger.warn("[{}]获取objLlist成功，objLlist.size={}", deviceId, objLlist.size());
			accessType = objLlist.get(0).getValue();
		}

		String[] gatherPath = new String[]{this.ststusPatchEPON,this.txPowerPatchEPON,this.rxPowerEPON,this.packetsReceivedEPON,this.packetsSendEPON,this.transceiverEPON};
		if("GPON".equals(accessType))
		{
			gatherPath = new String[]{this.ststusPatchGPON,this.txPowerPatchGPON,this.rxPowerGPON,this.packetsReceivedGPON,this.packetsSentGPON,this.transceiverGPON};
		}
		objLlist = corba.getValue(deviceId, gatherPath);
		logger.warn("{},objList:{}",deviceId,objLlist);
		if (null == objLlist || objLlist.isEmpty()) {
			logger.warn("[{}]获取iList失败，返回", deviceId);
			result.setiOpRst(-1);
			return;
		}

		for(ParameValueOBJ pvobj : objLlist){
			paraList.add(setPara(pvobj.getName(),pvobj.getValue()));
		}
		logger.warn("[{}]获取paraList，返回", paraList);

	}


}
