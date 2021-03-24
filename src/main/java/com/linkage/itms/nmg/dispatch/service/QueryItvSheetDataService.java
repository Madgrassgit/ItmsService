package com.linkage.itms.nmg.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.nmg.dispatch.obj.QueryItvSheetDataChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家庭网关ITV业务稽核接口
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-11-20
 */
public class QueryItvSheetDataService implements IService
{

	// 日志记录
	private static final Logger logger = LoggerFactory
			.getLogger(QueryItvSheetDataService.class);

	@SuppressWarnings("static-access")
	@Override
	public String work(String inXml)
	{
		QueryItvSheetDataChecker checker = new QueryItvSheetDataChecker(inXml);
		if (false == checker.check())
		{
			logger.error(
					"servicename[QueryItvSheetDataService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[QueryItvSheetDataService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		// 查询用户设备信息
		Map<String, String> userDevInfo = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo());
		String deviceId = "";
		String userId = "";
		if (null == userDevInfo || userDevInfo.isEmpty())
		{
			logger.warn("servicename[QueryItvSheetDataService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("无此客户信息");
		}
		else
		{
			checker.setLoid(userDevInfo.get("username"));
			deviceId = userDevInfo.get("device_id");
			userId = userDevInfo.get("user_id");
			if (StringUtil.IsEmpty(deviceId))
			{
				// 未绑定设备
				logger.warn(
						"servicename[QueryItvSheetDataService]cmdId[{}]userinfo[{}]此用户没有设备关联信息",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1003);
				checker.setResultDesc("此用户没有设备关联信息");
			}
			else
			{
				// 1.查询此用户开通的业务信息
				Map<String, String> userIptvMap = userDevDao.queryServForIptv(userId);
				Map<String, String> userInternetMap = userDevDao.queryServForNet(userId);
				if (null == userIptvMap || userIptvMap.isEmpty())
				{
					// 没有开通业务
					logger.warn(
							"servicename[QueryItvSheetDataService]cmdId[{}]userinfo[{}]此用户没有开通任何IPTV业务",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1006);
					checker.setResultDesc("此用户没有开通任何IPTV业务");
					return checker.getReturnXml();
				}
				else
				{
					// 工单值
					logger.warn("servicename[QueryItvSheetDataService]cmdId[{}]userinfo[{}]获取上网业务工单配置数据：",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),
									userIptvMap.get("username") });
					checker.setIptvNo(userIptvMap.get("username"));
					if (null != userInternetMap && !userInternetMap.isEmpty())
					{
						// 获取bandNo
						checker.setBandNo(userInternetMap.get("username"));
					}else{
						checker.setBandNo("");
					}
					// 组播vlan数据库的值
					checker.setZbVlan(StringUtil.getStringValue(userIptvMap,"multicast_vlanid", ""));
					// iptvVlan数据库的值
					checker.setIptvVlan(StringUtil.getStringValue(userIptvMap,"vlanid", ""));
					
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
						int temp = 1;
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
						String IPTV = "IPTV";
						String OTHER = "OTHER";
						
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
								logger.warn("[QueryItvSheetDataService] [{}]获取" + wanConnPath + "下实例号失败，返回",
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
									logger.warn("[QueryItvSheetDataService] [{}]获取" + wanConnPath
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
							logger.warn("[QueryItvSheetDataService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回", deviceId);
							checker.setResult(1006);
							checker.setResultDesc("不存在WANIP下的X_CT-COM_ServiceList节点");
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
							logger.warn("[QueryItvSheetDataService] [{}]获取ServiceList失败", deviceId);
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
										&& (entry.getValue().indexOf(IPTV) >= 0 || entry.getValue().indexOf(OTHER) >= 0)){//X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径
									if (temp < Integer.valueOf(j)){
										temp = Integer.valueOf(j);
									}
									pathJ = StringUtil.getStringValue(temp);
									pathK = k;
								}
							}
						}
//						if(pathJList.size()>1){
//							for(int i = 0; i < pathJList.size(); i++){
//								// iptvVlan节点路径
//								String vlanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+pathJList.get(i)+checkAccessType+".VLANIDMark";
//								// 采集 VLANIDMark，43等
//								ArrayList<ParameValueOBJ> vlanList = acsCorba.getValue(deviceId, vlanPath);
//								if (null == vlanList || vlanList.size()==0 || null==vlanList.get(0) || null==vlanList.get(0).getValue()) {
//									checker.setIptvVlanReal("");
//									logger.warn("[{}]采集VLANIDMark失败或者值为空",	deviceId);
//								}else{
//									checker.setIptvVlanReal(vlanList.get(0).getValue());
//									if (checker.getIptvVlan().equals(vlanList.get(0).getValue())){
//										pathJ = pathJList.get(i);
//									}
//									logger.warn("[{}]采集VLANIDMark成功，值为：[{}]",	deviceId,vlanList.get(0).getValue());
//								}
//							}
//						}
							String servListPathJ = wanConnPath + pathJ + ".WANPPPConnection." + pathK;
							// 组播vlan节点路径
							String multicastVlanPath = servListPathJ + ".X_CT-COM_MulticastVlan";
							// iptvVlan节点路径
							String vlanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+pathJ+checkAccessType+".VLANIDMark";
							
							String[] gatherPath = new String[]{multicastVlanPath,vlanPath};
							ArrayList<ParameValueOBJ> objLlist = acsCorba.getValue(deviceId, gatherPath);
							if (null == objLlist || objLlist.isEmpty()) {
								// 采集 组播Vlan
								ArrayList<ParameValueOBJ> multicastVlanList = acsCorba.getValue(deviceId, multicastVlanPath);
								if (null == multicastVlanList || multicastVlanList.size()==0 || null==multicastVlanList.get(0) || null==multicastVlanList.get(0).getValue()) {
									checker.setIptvVlanReal("");
									logger.warn("[{}]采集VLANIDMark失败或者值为空",	deviceId);
								}else{
									checker.setIptvVlanReal(multicastVlanList.get(0).getValue());
									logger.warn("[{}]采集VLANIDMark成功，值为：[{}]",	deviceId,multicastVlanList.get(0).getValue());
								}
								
								// 采集 VLANIDMark，43等
								ArrayList<ParameValueOBJ> vlanList = acsCorba.getValue(deviceId, vlanPath);
								if (null == vlanList || vlanList.size()==0 || null==vlanList.get(0) || null==vlanList.get(0).getValue()) {
									checker.setIptvVlanReal("");
									logger.warn("[{}]采集VLANIDMark失败或者值为空",	deviceId);
								}else{
									checker.setIptvVlanReal(vlanList.get(0).getValue());
									logger.warn("[{}]采集VLANIDMark成功，值为：[{}]",	deviceId,vlanList.get(0).getValue());
								}
							}else{
								for(ParameValueOBJ pvobj : objLlist){
									if(pvobj.getName().contains("X_CT-COM_MulticastVlan")){
										checker.setZbVlanReal(pvobj.getValue());
									}else if(pvobj.getName().contains("VLANIDMark")){
										checker.setIptvVlanReal(pvobj.getValue());
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
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),"QueryItvSheetData");
		logger.warn(
				"servicename[QueryItvSheetDataService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
		checker.setResult(0);
		checker.setResultDesc("成功");
		// 回单
		return returnXml;
	}
}
