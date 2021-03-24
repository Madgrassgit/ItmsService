package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dao.VlanStatusServiceDao;
import com.linkage.itms.dispatch.obj.VlanStatusChecker;
import com.linkage.itms.dispatch.obj.VlanStatusChecker.VlanInfo;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * AHDX_ITMS-REQ-20171225YQW-001激活ITV组播vlan修改接口
 * @author jlp
 *
 */
public class VlanStatusService implements IService{

	private static Logger logger = LoggerFactory.getLogger(VlanStatusService.class);

	public String work(String inXml)
	{
		VlanStatusServiceDao dao=new VlanStatusServiceDao();
		logger.warn("VlanStatus==>inXml({})",inXml);
		VlanStatusChecker checker = new VlanStatusChecker(inXml);
		if (false == checker.check()) {
			logger.warn("验证未通过，返回：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		List<?>  userList=userDevDao.queryUserList(2, checker.getLoid(), null);
		/*String loidSupport=dao.selectLoidInTempTable(checker.getLoid());
		if(StringUtil.IsEmpty(loidSupport)){
			checker.setResult(1000);
			checker.setResultDesc("不需要修改vlan信息");
			return checker.getReturnXml();
		}*/
		if(userList==null|userList.size()<1){
			checker.setResult(1002);
			checker.setResultDesc("无此客户信息");
			return checker.getReturnXml();
		}
		List<HashMap<String, String>> devList=userDevDao.queryDeviceInfoByLoid(checker.getLoid());
		if(null==devList|devList.size()<1 |StringUtil.IsEmpty(devList.get(0).get("device_id"))){
			checker.setResult(1004);
			checker.setResultDesc("此用户未绑定设备");
			return checker.getReturnXml();
		}
		Map<String,String> deviceObj=devList.get(0);
		String deviceId=deviceObj.get("device_id");
		logger.warn("[{}]start to set...",deviceId);
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba acsCorba = new ACSCorba();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
		if (-6 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1004);
			checker.setResultDesc("无法连接设备");
			return checker.getReturnXml();
		} else if (1 == flag) {
			logger.warn("[VlanStatusService][{}]设备在线，可以进行采集操作", deviceId);
			String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
			String wanServiceList = ".X_CT-COM_ServiceList";
			String wanPPPConnection = ".WANPPPConnection.";
			String wanIPConnection = ".WANIPConnection.";
			String wanLanInterface = ".X_CT-COM_LanInterface";
			String lANEthernetInterfaceConfig=".LANEthernetInterfaceConfig";
			String connectionStatus=".ConnectionStatus";
			//String INTERNET = "INTERNET";
			String vlanIdPath = "";
			String L2 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2";
			String L1 = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1";
			String lanHead = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
			String wanType = ".ConnectionType";
			
			String accessType = dao.getAccessType(deviceId);
			// 以下获取的方式是NX专用
			if (null == accessType)
			{
				String[] gatherPath = new String[]{
						"InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType"
						};
				ArrayList<ParameValueOBJ> objLlist = acsCorba.getValue(deviceId, gatherPath);
				
				if(objLlist!=null && objLlist.size()!=0){
					for(ParameValueOBJ pvobj : objLlist){
						if(pvobj.getName().endsWith("WANAccessType")){
							accessType = pvobj.getValue();
						}
					}
				}
				logger.warn("[{}]采集到的，accessType为：[{}]", deviceId,accessType);
			}
			
			if("EPON".equals(accessType)){
				vlanIdPath = ".X_CT-COM_WANEponLinkConfig.VLANIDMark";
			}else if("GPON".equals(accessType)){
				vlanIdPath = ".X_CT-COM_WANGponLinkConfig.VLANIDMark";
			}else{
				logger.warn("[{}]accessType既不是EPON也不是GPON", deviceId);
				vlanIdPath = "noPon";
			}
			
			ArrayList<String> wanConnPathsList = null;
			// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
			wanConnPathsList = acsCorba.getParamNamesPath(deviceId, wanConnPath, 0);
			if (wanConnPathsList == null || wanConnPathsList.size() == 0
					|| wanConnPathsList.isEmpty())
			{
				logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",deviceId);
				
				wanConnPathsList = new ArrayList<String>();
				List<String> jList = acsCorba.getIList(deviceId, wanConnPath);
				if (null == jList || jList.size() == 0 || jList.isEmpty())
				{
					logger.warn("[VlanStatusService] [{}]获取" + wanConnPath + "下实例号失败，返回", deviceId);
				}else{
					for (String j : jList){
						// 获取session，
						List<String> kPPPList = acsCorba.getIList(deviceId, wanConnPath + j
								+ wanPPPConnection);
						if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
						{
							logger.warn("[VlanStatusService] [{}]获取" + wanConnPath
									+ wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
							kPPPList = acsCorba.getIList(deviceId, wanConnPath + j
									+ wanIPConnection);
							if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
							{
								logger.warn("[VlanStatusService] [{}]获取" + wanConnPath
										+ wanConnPath + j + wanIPConnection + "下实例号失败", deviceId);
							}else{
								for (String kppp : kPPPList)
								{
									wanConnPathsList.add(wanConnPath + j + wanIPConnection + kppp
											+ wanServiceList);
								}
							}
						}
						else
						{
							for (String kppp : kPPPList)
							{
								wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp
										+ wanServiceList);
							}
						}
					}
				}
			}
				
			// serviceList节点
			ArrayList<String> serviceListList = new ArrayList<String>();
			// 所有需要采集的节点
			ArrayList<String> paramNameList = new ArrayList<String>();
			for (int i = 0; i < wanConnPathsList.size(); i++)
			{
				String namepath = wanConnPathsList.get(i);
				if (namepath.indexOf(vlanIdPath) >= 0 |namepath.indexOf(wanType) >= 0
						|namepath.indexOf(wanLanInterface) >= 0 |namepath.indexOf(connectionStatus) >= 0 
						|namepath.indexOf(lANEthernetInterfaceConfig) >= 0 )
				{
					serviceListList.add(namepath);
					paramNameList.add(namepath);
					continue;
				}
			}
			if (serviceListList.size() == 0 || serviceListList.isEmpty())
			{
				logger.warn("[VlanStatusService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回", deviceId);
			}else{
				String[] paramNameArr = new String[paramNameList.size()];
				int arri = 0;
				for (String paramName : paramNameList)
				{
					paramNameArr[arri] = paramName;
					arri = arri + 1;
				}
				Map<String, String> paramValueMap = new HashMap<String, String>();
				for (int k = 0; k < (paramNameArr.length / 20) + 1; k++)
				{
					String[] paramNametemp = new String[paramNameArr.length - (k * 20) > 20 ? 20
							: paramNameArr.length - (k * 20)];
					for (int m = 0; m < paramNametemp.length; m++)
					{
						paramNametemp[m] = paramNameArr[k * 20 + m];
					}
					Map<String, String> maptemp = acsCorba.getParaValueMap(deviceId,
							paramNametemp);
					if (maptemp != null && !maptemp.isEmpty())
					{
						paramValueMap.putAll(maptemp);
					}
				}
				if (paramValueMap.isEmpty())
				{
					logger.warn("[VlanStatusService] [{}]获取ServiceList失败", deviceId);
					checker.setResult(1000);
					checker.setResultDesc("位置错误");
					return  checker.getReturnXml();
				}
				VlanInfo v41 = new VlanInfo(),v43 = new VlanInfo();
				String j43="" ,j41="";
				for (Map.Entry<String, String> entry : paramValueMap.entrySet())
				{
					logger.debug("[{}]{}={} ", new Object[] { deviceId, entry.getKey(),
							entry.getValue() });
					String paramName = entry.getKey();
					//InternetGatewayDevice.WANDevice.1.WANConnectionDevice.
					String j = paramName.substring(wanConnPath.length(), paramName.indexOf(".",wanConnPath.length()));
					if (paramName.indexOf(vlanIdPath) >= 0)
					{
						if (!StringUtil.IsEmpty(entry.getValue())){//X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径
							if(StringUtil.getIntegerValue(entry.getValue()) == 43){
								j43 = j;
							}
							if(StringUtil.getIntegerValue(entry.getValue()) == 41){
								j41 = j;
							}
						}
					}
					
				}
				for (Map.Entry<String, String> entry : paramValueMap.entrySet())
				{
					logger.debug("[{}]{}={} ", new Object[] { deviceId, entry.getKey(),
							entry.getValue() });
					String paramName = entry.getKey();
					String j = paramName.substring(wanConnPath.length(), paramName.indexOf(".",wanConnPath.length()));
					//path里面包含.ConnectionStatus
					if (paramName.indexOf(connectionStatus) >= 0)
					{
						if (!StringUtil.IsEmpty(entry.getValue())){
							if(j43.equals(j)){//j值和43的j值相同
								v43.setConnection(entry.getValue());
								v43.setVlan("43");
//								v43.setWanType(dao.selectWanType(deviceId));
							}
							if(j41.equals(j)){//j值和41的j值相同
								v41.setVlan("41");
								v41.setConnection(entry.getValue());
//								v41.setWanType(dao.selectWanType(deviceId));
							}
						}
					}
				}
				for (Map.Entry<String, String> entry : paramValueMap.entrySet())
				{
					logger.debug("[{}]{}={} ", new Object[] { deviceId, entry.getKey(),
							entry.getValue() });
					String paramName = entry.getKey();
					String j = paramName.substring(wanConnPath.length(), paramName.indexOf(".",wanConnPath.length()));
					//path里面包含.ConnectionType
					if (paramName.indexOf(wanType) >= 0)
					{
						if (!StringUtil.IsEmpty(entry.getValue())){
							String wanType1=entry.getValue();
							if(j43.equals(j)){//j值和43的j值相同
								v43.setWanType(wanType1);
							}
							if(j41.equals(j)){//j值和41的j值相同
								v41.setWanType(wanType1);
							}
						}
					}
					//path里面包含.ConnectionStatus
					//lanHead = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."
					if (paramName.indexOf(wanLanInterface) >= 0 && !paramName.contains("DHCPEnable"))
					{
						if (!StringUtil.IsEmpty(entry.getValue())){
							//String bindLan=entry.getValue().equals(L1)?"lan1":"lan2";
							String bindLan= "";
							String [] bindLanArr = entry.getValue().split(",");
							logger.warn("lan : {}",entry.getValue());
							for(int i = 0 ; i < bindLanArr.length ; i++){
								String lan = bindLanArr[i].replace(lanHead, "");
								if(!StringUtil.IsEmpty(lan)){
									bindLan = bindLan + "lan" + lan + ",";
								}
							}
							if(j43.equals(j)){//j值和43的j值相同
								v43.setBindLan(bindLan);
							}
							if(j41.equals(j)){//j值和41的j值相同
								v41.setBindLan(bindLan);
							}
						}
					}
				}
				checker.setV41(v41);
				checker.setV43(v43);
			}
		}else {
			logger.warn("设备离线，device_id={}", deviceId);
			checker.setResult(1004);
			checker.setResultDesc("无法连接设备");
		}
		return checker.getReturnXml();
	}
	public static void main(String[] args) {
		
		//System.out.println("InternetGatewayDevice.WANDevice.1.WANConnectionDevice.3.X_CT-COM_WANGponLinkConfig.VLANIDMark".indexOf(".X_CT-COM_WANGponLinkConfig.VLANIDMark"));
		String bindLan= "";
		String lanHead = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
		String str= "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2";
		String [] bindLanArr = str.split(",");
		for(int i = 0 ; i < bindLanArr.length ; i++){
			String lan = bindLanArr[i].replace(lanHead, "");
			if(!StringUtil.IsEmpty(lan)){
				bindLan = bindLan + "lan" + lan + ",";
			}
		}
		
		logger.warn("====>bindLan:{}",bindLan);
	}
}
