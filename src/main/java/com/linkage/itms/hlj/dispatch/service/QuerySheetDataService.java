package com.linkage.itms.hlj.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.hlj.dispatch.dao.BindInfoDAO;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.QuerySheetDataChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-2
 * @category com.linkage.itms.hlj.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QuerySheetDataService implements HljIService {

	// 日志记录
	private static final Logger logger = LoggerFactory
			.getLogger(QuerySheetDataService.class);

	@Override
	public String work(String jsonString) {
		logger.warn("QuerySheetDataService——》jsonString" + jsonString);
		QuerySheetDataChecker checker = new QuerySheetDataChecker(jsonString);
		if (false == checker.check()) {
			logger.warn("家庭网关配置稽核接口，入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("QuerySheetDataService==>retParam={}",
					checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 查询用户设备信息
		QueryDeviceIdDAO qdDao = new QueryDeviceIdDAO();
		List<HashMap<String, String>> userMap = null;
		if (checker.getQueryType() == 0) {
			userMap = qdDao.queryUserByNetAccount(checker.getQueryNum());
		} else if (checker.getQueryType() == 1) {
			userMap = qdDao.queryUserByLoid(checker.getQueryNum());
		} else if (checker.getQueryType() == 2) {
			userMap = qdDao.queryUserByDevSN(checker.getQueryNum());
		} else {
		}
		String deviceId = "";
		String userId = "";
		if (null == userMap || userMap.isEmpty()) {
			checker.setResult(8);
			checker.setResultDesc("ITMS未知异常-查询结果为空");
			return checker.getReturnXml();
		} else {
			deviceId = StringUtil.getStringValue(userMap.get(0), "device_id",
					"");
			userId = StringUtil.getStringValue(userMap.get(0), "user_id", "");
			if (StringUtil.IsEmpty(deviceId)) {
				logger.warn("无设备信息", deviceId);
				checker.setResult(3);
				checker.setResultDesc("无设备信息");
				return checker.getReturnXml();
			} else if (StringUtil.IsEmpty(userId)) {
				logger.warn("无用户信息", deviceId);
				checker.setResult(9);
				checker.setResultDesc("无用户信息");
				return checker.getReturnXml();
			} else {
				// 查询设备上行方式
				BindInfoDAO bindDao = new BindInfoDAO();
				Map<String, String> infoMap = bindDao
						.queryDeviceBindInfoByDeV(deviceId);
				String access_style = StringUtil.getStringValue(infoMap,
						"access_style_relay_id", "");
				if (!"3".equals(access_style) && !"4".equals(access_style)) {
					logger.warn("非GPON或EPON设备", deviceId);
					checker.setResult(8);
					checker.setResultDesc("非GPON或EPON设备");
					return checker.getReturnXml();
				}
				// 1.查询此用户开通的业务信息
				UserDeviceDAO userDevDao = new UserDeviceDAO();
				Map<String, String> userServMap = userDevDao
						.queryServForNet(userId);
				if (null == userServMap || userServMap.isEmpty()) {
					// 没有开通业务
					logger.warn(
							"servicename[QuerySheetDataService]QueryNum[{}]此用户没有开通任何宽带业务",
							new Object[] { checker.getQueryNum() });
					checker.setResult(1002);
					checker.setResultDesc("此用户没有开通任何宽带业务");
					return checker.getReturnXml();
				} else {
					// 工单值
					logger.warn(
							"servicename[QuerySheetDataService]QueryNum[{}]获取上网业务工单配置数据：",
							new Object[] { checker.getQueryNum(),
									userServMap.get("username") });
					// 获取桥接还是路由
					String wan_type = StringUtil.getStringValue(userServMap,
							"wan_type", "");
					// 只针对路由稽核
					if ("2".equals(wan_type)) {
						GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
						ACSCorba corba = new ACSCorba();
						int flag = getStatus.testDeviceOnLineStatus(deviceId,
								corba);
						// 设备正在被操作，不能获取节点值
						if (-3 == flag) {
							logger.warn("设备正在被操作，无法获取节点值，device_id={}",
									deviceId);
							checker.setResult(10);
							checker.setResultDesc("设备忙，采集失败");
							logger.warn("return=({})", checker.getReturnXml()); // 打印回参
							return checker.getReturnXml();
						} else if (1 == flag) {
							logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
							
							String pathJ = "1";
							String pathK = "1";
							String username_realValue = "";
							String vlanId_realValue = "";
							
							String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
							String wanServiceList = ".X_CT-COM_ServiceList";
							String wanPPPConnection = ".WANPPPConnection.";
							String wanIPConnection = ".WANIPConnection.";
							String INTERNET = "INTERNET";
							
							ArrayList<String> wanConnPathsList = null;
							// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
							wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
							if (wanConnPathsList == null || wanConnPathsList.size() == 0
									|| wanConnPathsList.isEmpty())
							{
								logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",deviceId);
								wanConnPathsList = new ArrayList<String>();
								List<String> jList = corba.getIList(deviceId, wanConnPath);
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
									List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j
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
									Map<String, String> maptemp = corba.getParaValueMap(deviceId,
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
								int internetNum = 0;
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
											internetNum++;
											pathK = k;
											if(internetNum>1){
												if(StringUtil.getIntegerValue(j) > StringUtil.getIntegerValue(pathJ)){
													pathJ = j;
												}
											}else{
												pathJ = j;
											}
											
										}
									}
								}
									String servListPathJ = wanConnPath + pathJ + ".WANPPPConnection." + pathK;
									String userNamePath = servListPathJ + ".Username ";
									String vlanPath = "3".equals(access_style) ? "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+pathJ+".X_CT-COM_WANEponLinkConfig.VLANIDMark"
											: "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+pathJ+".X_CT-COM_WANGponLinkConfig.VLANIDMark";

								String[] gatherPath = new String[]{userNamePath,vlanPath};
								ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
								
								if (null == objLlist || objLlist.isEmpty())
								{
									logger.warn("[{}]objLlist为空",deviceId);
								}
								else
								{
									logger.warn("[{}]objLlist：" + objLlist, deviceId);
									for (ParameValueOBJ obj : objLlist)
									{
										if (obj.getName().endsWith("Username"))
										{
											username_realValue = obj.getValue();
										}
										else if (obj.getName().endsWith("VLANIDMark"))
										{
											vlanId_realValue = obj.getValue();
										}
									}
								}
							}
							
							String username_rightValue = StringUtil
									.getStringValue(userServMap, "username", "");
							String vlanId_rightValue = StringUtil
									.getStringValue(userServMap, "vlanid", "");
							JSONObject jo = new JSONObject();
							JSONArray cfgResult = new JSONArray();

							try {
								jo.put("resultCode", 0);
								jo.put("streamingNum",
										checker.getStreamingNum());
								jo.put("Mode", 1);
								jo.put("cfgResult", cfgResult);

								JSONObject vlanIdJo = new JSONObject();
								vlanIdJo.put("indicator", "上网VLAN");
								if (vlanId_realValue.equals(vlanId_rightValue)) {
									vlanIdJo.put("isConfig", 0);
								} else {
									vlanIdJo.put("isConfig", 1);
								}
								vlanIdJo.put("realValue", vlanId_realValue);
								vlanIdJo.put("rightValue", vlanId_rightValue);
								cfgResult.put(vlanIdJo);

								JSONObject userNameJo = new JSONObject();
								userNameJo.put("indicator", "宽带账号");
								if (username_realValue
										.equals(username_rightValue)) {
									userNameJo.put("isConfig", 0);
								} else {
									userNameJo.put("isConfig", 1);
								}
								userNameJo.put("realValue", username_realValue);
								userNameJo.put("rightValue",
										username_rightValue);
								cfgResult.put(userNameJo);

							} catch (JSONException e) {
								e.printStackTrace();
							}
							return jo.toString();

						} else {
							logger.warn("设备离线，device_id={}", deviceId);
							checker.setResult(4);
							checker.setResultDesc("设备离线");
							logger.warn("return=({})", checker.getReturnXml()); // 打印回参
							return checker.getReturnXml();

						}

					}

					else {
						JSONObject jo = new JSONObject();
						try {
							jo.put("resultCode", 1);
							jo.put("streamingNum", checker.getStreamingNum());
							jo.put("Mode", 0);
							jo.put("cfgResult", "");
						} catch (JSONException e) {
							e.printStackTrace();
						}
						return jo.toString();
					}
				}
			}
		}
	}
}
