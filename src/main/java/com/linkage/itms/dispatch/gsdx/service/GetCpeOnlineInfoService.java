package com.linkage.itms.dispatch.gsdx.service;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.gsdx.beanObj.CpeBasicInfo;
import com.linkage.itms.dispatch.gsdx.beanObj.Para;
import com.linkage.itms.dispatch.gsdx.beanObj.ServiceInfo;
import com.linkage.itms.dispatch.gsdx.beanObj.UserInfo;
import com.linkage.itms.dispatch.gsdx.dao.PublicDAO;
import com.linkage.itms.dispatch.gsdx.obj.GetCpeOnlineInfoDealXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 甘肃电信查询终端的详细信息接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月17日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class GetCpeOnlineInfoService extends ServiceFather{
	public GetCpeOnlineInfoService(String methodName)
	{
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(GetCpeOnlineInfoService.class);
	//基本信息
	private CpeBasicInfo basicInfo = new CpeBasicInfo();
	//用户信息
    private UserInfo userInfo = new UserInfo();
    //业务信息数组
    private ServiceInfo[] serviceInfoList ;
    private List<ServiceInfo> serviceInfoList_0 = new ArrayList<ServiceInfo>() ;
    private ServiceInfo tr069=new ServiceInfo();
    private ServiceInfo net = new ServiceInfo();
    private ServiceInfo iptv = new ServiceInfo();
    private ServiceInfo voip = new ServiceInfo();
    private ServiceInfo wifi = new ServiceInfo();
    private String accessType = "";
	
	private ACSCorba corba = new ACSCorba();
	private PublicDAO dao = new PublicDAO();
	private GetCpeOnlineInfoDealXML dealXML;
	private String EPONSTATUS = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaceConfig.Status";
	private String GPONSTATUS = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.Status";
	private String VOIPSTATUS = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line.1.Status";
	private String WANCONNECTION = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
	private String WIFISTATUS = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.Status";
	private String authUserName = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line.1.SIP.AuthUserName";
	private String UP = "Up";
	private String ponstatus = ""; 
	private String netPath = ""; 
	private String iptvPath = ""; 
	private String voipPath = ""; 
	private Para[] netparaList = new Para[2];
	private ArrayList<String> gatherPath = new ArrayList<String>();
	private ArrayList<ParameValueOBJ> objLlist = new ArrayList<ParameValueOBJ>();
	
	public int work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		
		dealXML = new GetCpeOnlineInfoDealXML(methodName);
		// 验证入参
		if (null == dealXML.getXML(inXml)) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			return -1;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		
		ArrayList<HashMap<String, String>> baseList = dao.queryDeviceInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}",baseList.toString());
		
		if(null == baseList || baseList.size()==0){
			return 0;
		}else if(baseList.size() >1){
			logger.warn(methodName+"["+dealXML.getOpId()+"]查询到多个终端.返回：{}",baseList.size());
			return baseList.size();
		}
		else{
			HashMap<String, String> baseMap = baseList.get(0);
			logger.warn(methodName+"["+dealXML.getOpId()+"],终端信息{}",baseMap.toString());
			basicInfo.setId(StringUtil.getStringValue(baseMap, "oui")+StringUtil.getStringValue(baseMap, "sn"));
			basicInfo.setType(StringUtil.getStringValue(baseMap, "device_model"));
			basicInfo.setHwVersion(StringUtil.getStringValue(baseMap, "hardwareversion"));
			basicInfo.setSwVersion(StringUtil.getStringValue(baseMap, "softwareversion"));
			userInfo.setUserid(StringUtil.getStringValue(baseMap, "loid"));
			tr069.setType(0);
			tr069.setServiceStatus(0);
			tr069.setNetworkStatus(0);

			Para[] userNameAndPass = new  Para[2];

			Para userpara = new Para();
			userpara.setName("webMaintainUser");
			userpara.setValue(StringUtil.getStringValue(baseMap, "x_com_username"));
			userNameAndPass[0] = userpara;

			Para passpara = new Para();
			passpara.setName("webMaintainPwd");
			passpara.setValue(StringUtil.getStringValue(baseMap, "x_com_passwd"));
			userNameAndPass[1] = passpara;

			tr069.setParaList(userNameAndPass);

			serviceInfoList_0.add(tr069);
			String deviceId = StringUtil.getStringValue(baseMap, "device_id");
			accessType = UserDeviceDAO.getAccType(deviceId);
			//设置networkStatus采集节点
			if ("GPON".equals(accessType)) ponstatus = GPONSTATUS;
			else ponstatus = EPONSTATUS;
			gatherPath.add(ponstatus);
			
			if(!StringUtil.isEmpty(StringUtil.getStringValue(baseMap, "user_id"))){
				//查询业务信息
				ArrayList<HashMap<String, String>> servList = dao.getServById(StringUtil.getStringValue(baseMap, "user_id"));
				
				if(null != servList && !servList.isEmpty()){
					logger.warn("{}|[{}]查询到业务",methodName,dealXML.getOpId());
					
					for(HashMap<String, String> servMap : servList){
						String servType = servMap.get("serv_type_id");
						if("10".equals(servType)){
							net = new ServiceInfo();
							net.setType(2);
							serviceInfoList_0.add(net);
							Para para = new Para();
							para.setName("adAccount");
							para.setValue(StringUtil.getStringValue(servMap, "username"));
							netparaList[0] = para;
						}
						else if("11".equals(servType)){
							iptv = new ServiceInfo();
							iptv.setType(1);
							serviceInfoList_0.add(iptv);
						}
						else if("14".equals(servType) || "15".equals(servType)){
							voip = new ServiceInfo();
							voip.setType(4);
							serviceInfoList_0.add(voip);
						}
					}




					GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
					int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
					if (1 != flag){
						logger.warn(methodName+"["+dealXML.getOpId()+"]设备不在线或正在被操作，无法获取节点值，device_id={},返回-1", deviceId);
						return -1;
					}
					
					logger.warn(methodName+"["+dealXML.getOpId()+"]设备在线，开始采集，device_id={},返回-1", deviceId);
					
					
					ArrayList<String> wanConnPathsList = new ArrayList<String>();
					// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
					wanConnPathsList = corba.getParamNamesPath(deviceId, WANCONNECTION, 0);
					logger.warn(methodName+"["+dealXML.getOpId()+"]wanConnPathsList.size:{}", wanConnPathsList.size());
					if (wanConnPathsList == null || wanConnPathsList.size() == 0
							|| wanConnPathsList.isEmpty()){
						logger.warn(methodName+"["+dealXML.getOpId()+"]wanConnPathsList 为空，采集失败，返回-1");
						return -1;
					}
					else
					{
						ArrayList<String> paramNameList = new ArrayList<String>();
						for (int i = 0; i < wanConnPathsList.size(); i++)
						{
							String namepath = wanConnPathsList.get(i);
							if (namepath.indexOf(".X_CT-COM_ServiceList") >= 0)
							{
								paramNameList.add(namepath);
							}
						}
						wanConnPathsList = new ArrayList<String>();
						wanConnPathsList.addAll(paramNameList);
					}
					if (wanConnPathsList.size() == 0){
						logger.warn(methodName+"["+dealXML.getOpId()+"]无X_CT-COM_ServiceList节点：", deviceId);
					}
					else{
						Collections.reverse(wanConnPathsList);
						
						String[] paramNametemp = new String[wanConnPathsList.size()];
						for (int i = 0; i < wanConnPathsList.size(); i++)
						{
							paramNametemp[i] = wanConnPathsList.get(i);
						}
						Map<String, String> paramValueMap = corba.getParaValueMap(deviceId,
								paramNametemp);
						if (paramValueMap.isEmpty())
						{
							logger.warn(methodName+"获取ServiceList失败[{}]", deviceId);
						}
						for (Map.Entry<String, String> entry : paramValueMap.entrySet())
						{
							// InternetGatewayDevice.WANDevice.1.WANConnectionDevice.2.WANPPPConnection.3.X_CT-COM_ServiceList
							if (entry.getValue().indexOf("INTERNET") >= 0
									|| entry.getValue().indexOf("internet") >= 0)
							{
								int index = entry.getKey().indexOf(".X_CT-COM_ServiceList");
								String servListPathJ = entry.getKey().substring(0, index);
								logger.warn(methodName+"[{}]获取INTERNET path成功", deviceId);
								gatherPath.add(servListPathJ + ".ConnectionStatus");
								netPath = servListPathJ + ".ConnectionStatus";
								gatherPath.add(servListPathJ + ".ConnectionType");//实际终端上网方式
							}
							else if(entry.getValue().indexOf("IPTV") >= 0 || entry.getValue().indexOf("OTHER") >= 0
									|| entry.getValue().indexOf("iptv") >= 0 || entry.getValue().indexOf("other") >= 0){
								int index = entry.getKey().indexOf(".X_CT-COM_ServiceList");
								String servListPathJ = entry.getKey().substring(0, index);
								logger.warn(methodName+"[{}]获取IPTV path成功", deviceId);
								gatherPath.add(servListPathJ + ".ConnectionStatus");
								iptvPath = servListPathJ + ".ConnectionStatus";
							}
							else if(entry.getValue().indexOf("VOIP") >= 0 || entry.getValue().indexOf("voip") >= 0){
								int index = entry.getKey().indexOf(".X_CT-COM_ServiceList");
								String servListPathJ = entry.getKey().substring(0, index);
								logger.warn(methodName+"[{}]获取VOIP path成功", deviceId);
								gatherPath.add(servListPathJ + ".ConnectionStatus");
								voipPath = servListPathJ + ".ConnectionStatus";
								gatherPath.add(VOIPSTATUS);
								gatherPath.add(authUserName);
							}
						}
						
					}
				}
			}
			
			//用户不论存不存在，都要获取wifi节点
			//wifi
			gatherPath.add(WIFISTATUS);

			// 处理设备采集结果
			String[] gatherPathArray = new String[gatherPath.size()];
			gatherPath.toArray(gatherPathArray);
			objLlist = corba.getValue(deviceId, gatherPathArray);
			if (null == objLlist || objLlist.isEmpty())
			{
				logger.warn("{}|[{}]设备在线，批量采集节点失败，device_id={},返回-1", methodName,dealXML.getOpId(),deviceId);
				return -1;
			}
			else
			{
				logger.warn("{}|[{}]设备在线，批量采集节点成功，device_id={}",methodName, dealXML.getOpId(),deviceId);
				if(null != iptv){
					for (ParameValueOBJ pvobj : objLlist)
					{
						if (pvobj.getName().contains(iptvPath)){
							if("Connected".equalsIgnoreCase(pvobj.getValue())){
								iptv.setServiceStatus(0);
							}
							else{
								iptv.setServiceStatus(-1);
							}
						}
						else if (ponstatus.equals(pvobj.getName())){
							if(UP.equalsIgnoreCase(pvobj.getValue())){
								iptv.setNetworkStatus(0);
							}
							else{
								iptv.setNetworkStatus(-1);
							}
						}
					}
				}
				
				if(null != net){
					for (ParameValueOBJ pvobj : objLlist)
					{
						if (pvobj.getName().contains(netPath)){
							if("Connected".equalsIgnoreCase(pvobj.getValue())){
								net.setServiceStatus(0);
							}
							else{
								net.setServiceStatus(-1);
							}
						}
						else if (ponstatus.equals(pvobj.getName())){
							if(UP.equalsIgnoreCase(pvobj.getValue())){
								net.setNetworkStatus(0);
							}
							else{
								net.setNetworkStatus(-1);
							}
						}
						else if (pvobj.getName().contains("ConnectionType")){
							Para para = new Para();
							para.setName("ConnectionType");
							para.setValue(pvobj.getValue());
							netparaList[1] = para;
							net.setParaList(netparaList);
						}
					}
				}

				if(null != voip){
					for (ParameValueOBJ pvobj : objLlist)
					{
						if (pvobj.getName().contains(voipPath)){
							if("Connected".equalsIgnoreCase(pvobj.getValue())){
								voip.setServiceStatus(0);
							}
							else{
								voip.setServiceStatus(-1);
							}
						}
						else if (VOIPSTATUS.equals(pvobj.getName())){
							if(UP.equalsIgnoreCase(pvobj.getValue())){
								voip.setNetworkStatus(0);
							}
							else{
								voip.setNetworkStatus(-1);
							}
						}
						else if(authUserName.equals(pvobj.getName())){
							Para para = new Para();
							para.setName("phoneNum");
							para.setValue(pvobj.getValue());
							voip.setParaList(new Para[]{para});
						}
					}
				}
				
				//wifi
				wifi = new ServiceInfo();
				wifi.setNetworkStatus(-1);
				wifi.setServiceStatus(-1);
				wifi.setType(3);
				for (ParameValueOBJ pvobj : objLlist){
					if (pvobj.getName().equals(WIFISTATUS)){
						if(UP.equalsIgnoreCase(pvobj.getValue())){
							wifi.setNetworkStatus(0);
							wifi.setServiceStatus(0);
						}
					}
				}
				serviceInfoList_0.add( new ServiceInfo() );

				serviceInfoList_0.add(wifi);
				serviceInfoList = new ServiceInfo[serviceInfoList_0.size()];
				serviceInfoList_0.toArray(serviceInfoList);
				return 1;
			}
		}
	}
	
	
	public CpeBasicInfo getBasicInfo()
	{
		return basicInfo;
	}

	
	public void setBasicInfo(CpeBasicInfo basicInfo)
	{
		this.basicInfo = basicInfo;
	}

	
	public UserInfo getUserInfo()
	{
		return userInfo;
	}

	
	public void setUserInfo(UserInfo userInfo)
	{
		this.userInfo = userInfo;
	}

	
	public ServiceInfo[] getServiceInfoList()
	{
		return serviceInfoList;
	}

	
	public void setServiceInfoList(ServiceInfo[] serviceInfoList)
	{
		this.serviceInfoList = serviceInfoList;
	}
	
}
