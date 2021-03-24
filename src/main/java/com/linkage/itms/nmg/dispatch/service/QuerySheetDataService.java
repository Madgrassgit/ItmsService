
package com.linkage.itms.nmg.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.nmg.dispatch.obj.QuerySheetDataChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author yinlei3 (Ailk No.73167)
 * @version 1.0
 * @since 2016年6月12日
 * @category com.linkage.itms.nmg.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QuerySheetDataService implements IService
{

	// 日志记录
	private static final Logger logger = LoggerFactory
			.getLogger(QuerySheetDataService.class);
	private static final ACSCorba acsCorba = null;

	@Override
	public String work(String inXml)
	{
		QuerySheetDataChecker checker = new QuerySheetDataChecker(inXml);
		if (false == checker.check())
		{
			logger.error(
					"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 查询用户设备信息
		Map<String, String> userDevInfo = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo());
		String deviceId = "";
		if (null == userDevInfo || userDevInfo.isEmpty())
		{
			logger.warn("servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("无此客户信息");
		}
		else
		{
			deviceId = userDevInfo.get("device_id");
			if (StringUtil.IsEmpty(deviceId))
			{
				// 未绑定设备
				logger.warn(
						"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]此用户没有设备关联信息",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1003);
				checker.setResultDesc("此用户没有设备关联信息");
			}
			else
			{
				// 1.查询此用户开通的业务信息
				Map<String, String> userServMap = userDevDao.queryServForNet(userDevInfo
						.get("user_id"));
				if (null == userServMap || userServMap.isEmpty())
				{
					// 没有开通业务
					logger.warn(
							"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]此用户没有开通任何宽带业务",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1006);
					checker.setResultDesc("此用户没有开通任何宽带业务");
					return checker.getReturnXml();
				}
				else
				{
					// 工单值
					logger.warn(
							"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]获取上网业务工单配置数据：",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),
									userServMap.get("username") });
					String wan_type = StringUtil.getStringValue(userServMap, "wan_type",
							"");
					if ("1".equals(wan_type))
					{
						wan_type = "PPPoE_Bridged";
					}
					else if ("2".equals(wan_type))
					{
						wan_type = "IP_Routed";
					}
					else if ("3".equals(wan_type))
					{
						wan_type = "静态IP";
					}
					else if ("4".equals(wan_type))
					{
						wan_type = "DHCP";
					}
					checker.setWanType_yingpei(wan_type);
					String portYingPei = StringUtil.getStringValue(userServMap,
							"bind_port");
					if ("".equals(portYingPei) || portYingPei == null){
						checker.setBindPort_yingpei("");
					}else if (!portYingPei.contains(",")){
						checker.setBindPort_yingpei(portYingPei);
					}else{
						String[] yingpeiStr = portYingPei.split(",");
						Arrays.sort(yingpeiStr); 
						int i = 0;
						String newPortYingPei ="";
						for(String str : yingpeiStr) {
							newPortYingPei += str;
						    if(i < yingpeiStr.length-1){
						    	newPortYingPei = newPortYingPei+",";
						    }
						    i++;
						} 
						checker.setBindPort_yingpei(newPortYingPei);
					}
					
					if ("PPPoE_Bridged".equals(wan_type))
					{
						checker.setUsername_yingpei("");
						checker.setPassword_yingpei("");
					}else{
						checker.setUsername_yingpei(StringUtil.getStringValue(userServMap,
								"username", ""));
						checker.setPassword_yingpei(StringUtil.getStringValue(userServMap,
								"passwd", ""));
					}
					checker.setVlanId_yingpei(StringUtil.getStringValue(userServMap,
							"vlanid", ""));
					
					// 校验设备是否在线
					GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
					ACSCorba acsCorba = new ACSCorba();
					
					int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
					// 设备正在被操作，不能获取节点值
					if (-6 == flag) {
						logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
						checker.setResult(1013);
						checker.setResultDesc("设备正在被操作");
						logger.warn("return=({})", checker.getReturnXml());  // 打印回参
						return checker.getReturnXml();
					}
					// 设备在线
					else if (1 == flag) {
						logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
						
						String pathJ = "1";
						String pathK = "1";
						//采集accessType
						String accessType = null;
						accessType = userDevDao.getAccType(deviceId);
						if (null == accessType || "null".equals(accessType) || "".equals(accessType))
						{
							String accessTypePath = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
							ArrayList<ParameValueOBJ> accessTypeList = acsCorba.getValue(deviceId, accessTypePath);
							if (accessTypeList != null && accessTypeList.size() != 0) {
								for (ParameValueOBJ pvobj : accessTypeList) {
									if (pvobj.getName().endsWith("WANAccessType")) {
										accessType = pvobj.getValue();
									}
								}
							}
						}
						
						logger.warn("accessType为：[{}]", accessType);
						String checkAccessType = null;
						
						if("EPON".equals(accessType)){
							checkAccessType = ".X_CT-COM_WANEponLinkConfig";
						}else if("GPON".equals(accessType)){
							checkAccessType = ".X_CT-COM_WANGponLinkConfig";
						}else{
							logger.warn("accessType既不是EPON也不是GPON");
							checker.setResult(1012);
							checker.setResultDesc("上行方式既不是EPON也不是GPON");
							logger.warn("return=({})", checker.getReturnXml());  // 打印回参
							return checker.getReturnXml();
						}
						
						String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
						String wanServiceList = ".X_CT-COM_ServiceList";
						String wanPPPConnection = ".WANPPPConnection.";
						String wanIPConnection = ".WANIPConnection.";
						String INTERNET = "INTERNET";
						
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
								logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath + "下实例号失败，返回",
										deviceId);
								checker.setResult(1006);
								checker.setResultDesc("此路径下获取节点失败");
								return checker.getReturnXml();
							}
							for (String j : jList)
							{
								// 获取session，
								List<String> kPPPList = acsCorba.getIList(deviceId, wanConnPath + j
										+ wanIPConnection);
								if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
								{
									logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
											+ wanConnPath + j + wanIPConnection + "下实例号失败", deviceId);
								}
								else
								{
									for (String kppp : kPPPList)
									{
										wanConnPathsList.add(wanConnPath + j + wanIPConnection + kppp
												+ wanServiceList);
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
							if (namepath.indexOf(wanServiceList) >= 0 && namepath.indexOf(wanPPPConnection)>=0)
							{
								serviceListList.add(namepath);
								paramNameList.add(namepath);
								continue;
							}
						}
						if (serviceListList.size() == 0 || serviceListList.isEmpty())
						{
							logger.warn("[QuerySheetDataService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回", deviceId);
							checker.setResult(0);
							checker.setResultDesc("成功");
							return checker.getReturnXml();
						}
						
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
							logger.warn("[QuerySheetDataService] [{}]获取ServiceList失败", deviceId);
							checker.setResult(1007);
							checker.setResultDesc("获取ServiceList失败");
							return checker.getReturnXml();
						}
						for (Map.Entry<String, String> entry : paramValueMap.entrySet())
						{
							logger.debug("[{}]{}={} ", new Object[] { deviceId, entry.getKey(),
									entry.getValue() });
							String paramName = entry.getKey();
							String j = paramName.substring(wanConnPath.length(), paramName.indexOf(".",wanConnPath.length()));
							if (paramName.indexOf(wanPPPConnection) >= 0)
							{
							}
							else if (paramName.indexOf(wanIPConnection) >= 0)
							{
								continue;
							}
							if (paramName.indexOf(wanServiceList) >= 0)
							{
								String k = paramName.substring(paramName.indexOf(wanServiceList) - 1,
										paramName.indexOf(wanServiceList));
								if (!StringUtil.IsEmpty(entry.getValue())
										&& entry.getValue().indexOf(INTERNET) >= 0){//X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径
									
									pathJ = j;
									pathK = k;
								}
							}
						}
							String servListPathJ = wanConnPath + pathJ + ".WANPPPConnection." + pathK;
							String connTypePath = servListPathJ + ".ConnectionType";
							String bindPortPath = servListPathJ + ".X_CT-COM_LanInterface";
							String userNamePath = servListPathJ + ".Username";
							String passwordPath = servListPathJ + ".Password";
							String vlanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+pathJ+checkAccessType+".VLANIDMark";
							
							String[] gatherPath = new String[]{connTypePath,bindPortPath,userNamePath,passwordPath,vlanPath};
							ArrayList<ParameValueOBJ> objLlist = acsCorba.getValue(deviceId, gatherPath);
							if (null == objLlist || objLlist.isEmpty()) {
								// 采集 ConnectionType，WanType，上网方式：PPPoE_Bridged 等
								ArrayList<ParameValueOBJ> connTypeList = acsCorba.getValue(deviceId, connTypePath);
								if (null == connTypeList || connTypeList.size()==0 || null==connTypeList.get(0) || null==connTypeList.get(0).getValue()) {
									checker.setWanType_shipei("");
									logger.warn("[{}]采集ConnectionType失败或者值为空",	deviceId);
									checker.setResult(0);
									checker.setResultDesc("成功 ");
									logger.warn("return=({})", checker.getReturnXml()); // 打印回参
									return checker.getReturnXml();
								}else{
									checker.setWanType_shipei(connTypeList.get(0).getValue());
									logger.warn("[{}]采集ConnectionType成功，值为：[{}]",	deviceId,connTypeList.get(0).getValue());
								}
								if ("PPPoE_Bridged".equals(connTypeList.get(0).getValue())){
									checker.setUsername_shipei("");
									checker.setPassword_shipei("");
								}else{
									// 采集username
									ArrayList<ParameValueOBJ> userNameList = acsCorba.getValue(deviceId, userNamePath);
									if (null == userNameList || userNameList.size()==0 || null==userNameList.get(0) || null==userNameList.get(0).getValue()) {
										checker.setUsername_shipei("");
										logger.warn("[{}]采集Username失败或者值为空",	deviceId);
									}else{
										checker.setUsername_shipei(userNameList.get(0).getValue());
										logger.warn("[{}]采集Username成功，值为：[{}]",	deviceId,userNameList.get(0).getValue());
									}
									
									// 采集password
									ArrayList<ParameValueOBJ> passwordList = acsCorba.getValue(deviceId, passwordPath);
									if (null == passwordList || passwordList.size()==0 || null==passwordList.get(0) || null==passwordList.get(0).getValue()) {
										checker.setPassword_shipei("");
										logger.warn("[{}]采集Password失败或者值为空",	deviceId);
									}else{
										checker.setPassword_shipei(passwordList.get(0).getValue());
										logger.warn("[{}]采集Password成功，值为：[{}]",	deviceId,passwordList.get(0).getValue());
									}
								}
								
								// 采集 X_CT-COM_LanInterface,绑定端口
								ArrayList<ParameValueOBJ> bindPortList = acsCorba.getValue(deviceId, bindPortPath);
								if (null == bindPortList || bindPortList.size()==0 || null==bindPortList.get(0) || null==bindPortList.get(0).getValue()) {
									checker.setBindPort_shipei("");
									logger.warn("[{}]采集LanInterface失败或者值为空",	deviceId);
								}else{
									String portShiPei = bindPortList.get(0).getValue();
									if (!portShiPei.contains(",")) {
										checker.setBindPort_shipei(portShiPei);
									} else {
										String[] shipeiStr = portShiPei.split(",");
										Arrays.sort(shipeiStr);
										int i = 0;
										String newPortShiPei = "";
										for (String str : shipeiStr) {
											newPortShiPei += str;
											if (i < shipeiStr.length - 1) {
												newPortShiPei = newPortShiPei + ",";
											}
											i++;
										}
										checker.setBindPort_shipei(newPortShiPei);
									}
									logger.warn("[{}]采集LanInterface成功，值为：[{}]",	deviceId,bindPortList.get(0).getValue());
								}
								
								// 采集 VLANIDMark，43等
								ArrayList<ParameValueOBJ> vlanList = acsCorba.getValue(deviceId, vlanPath);
								if (null == vlanList || vlanList.size()==0 || null==vlanList.get(0) || null==vlanList.get(0).getValue()) {
									checker.setVlanId_shipei("");
									logger.warn("[{}]采集VLANIDMark失败或者值为空",	deviceId);
								}else{
									checker.setVlanId_shipei(vlanList.get(0).getValue());
									logger.warn("[{}]采集VLANIDMark成功，值为：[{}]",	deviceId,vlanList.get(0).getValue());
								}
							}else{
								for(ParameValueOBJ pvobj : objLlist){
									if(pvobj.getName().contains("ConnectionType")){
										checker.setWanType_shipei(pvobj.getValue());
									}else if(pvobj.getName().contains("X_CT-COM_LanInterface")){
										String portShiPei = pvobj.getValue();
										if ("".equals(portShiPei) || portShiPei == null) {
											checker.setBindPort_shipei("");
										} else if (!portShiPei.contains(",")) {
											checker.setBindPort_shipei(portShiPei);
										} else {
											String[] shipeiStr = portShiPei.split(",");
											Arrays.sort(shipeiStr);
											int i = 0;
											String newPortShiPei = "";
											for (String str : shipeiStr) {
												newPortShiPei += str;
												if (i < shipeiStr.length - 1) {
													newPortShiPei = newPortShiPei + ",";
												}
												i++;
											}
											checker.setBindPort_shipei(newPortShiPei);
										}
									}else if(pvobj.getName().contains("Username")){
										checker.setUsername_shipei(pvobj.getValue());
									}
									else if(pvobj.getName().contains("Password")){
										checker.setPassword_shipei(pvobj.getValue());
									}
									else if(pvobj.getName().contains("VLANIDMark")){
										checker.setVlanId_shipei(pvobj.getValue());
									}
								}

							}
							
					} else {// 设备不在线，不能获取节点值
						logger.warn("设备不在线，无法获取节点值");
						checker.setResult(1014);
						checker.setResultDesc("设备不能正常交互");
						logger.warn("return=({})", checker.getReturnXml()); // 打印回参
						return checker.getReturnXml();
					}
					 
				}	 
			}
		}
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
				"querySheetData");
		logger.warn(
				"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
		// 回单
		return returnXml;
	}
}
