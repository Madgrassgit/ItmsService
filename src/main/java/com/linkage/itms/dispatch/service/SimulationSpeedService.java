package com.linkage.itms.dispatch.service;

import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.SimulationSpeedDao;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.SimulationSpeedChecker;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * 江苏仿真测试接口
 * 
 * @author jianglp (75508)
 * @version 1.0
 * @since 2016-12-7
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 
 */
public class SimulationSpeedService implements IService { 
	/** 日志 */
	private static final Logger logger = LoggerFactory
			.getLogger(SimulationSpeedService.class);

	public String work(String inXml) {
		
		SimulationSpeedChecker checker = new SimulationSpeedChecker(inXml);
		SimulationSpeedDao speedDao=new SimulationSpeedDao();
		// 验证入参格式
		if (false == checker.check()) {
			logger.error("servicename[SimulationSpeedService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[]{checker.getCmdId(), checker.getUserInfo(),checker.getReturnXml()});
			return checker.getReturnXml();
		}
		logger.warn("servicename[SimulationSpeedService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[]{checker.getCmdId(), checker.getUserName(), inXml});
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ArrayList<HashMap<String, String>> userMapList = userDevDao.queryUserList(checker.getUserInfoType(),
						checker.getUserName(), checker.getUserSn());
		String devId = "";
		String deviceTypeId="";
		if (null == userMapList || userMapList.isEmpty()) {
			checker.setResult(1002);
			checker.setResultDesc("无此账号信息");
			checker.setSuccStatus("-1");
			checker.setFailureReason("没有用户");
			logger.warn("没有用户");
		} else if (userMapList.size() > 1 && checker.getUserInfoType() == 1) {
			String loidListStr = "";
			for (Map<String, String> map : userMapList) {
				loidListStr += map.get("username");
				if (userMapList.indexOf(map) < userMapList.size() - 1) {
					loidListStr += ",";
				}
			}
			checker.setResult(4);
			checker.setResultDesc("宽带账号查询到多个LOID");
			checker.setSuccStatus("-1");
			checker.setFailureReason(loidListStr);
			logger.warn("loidListStr");
		} else if (StringUtil.IsEmpty(userMapList.get(0).get("user_id"))) {
			checker.setResult(1002);
			checker.setResultDesc("无此账号信息");
			checker.setSuccStatus("-1");
			checker.setFailureReason("没有用户!");
			logger.warn("没有用户!");
		} else {
			Map<String, String> userInfoMap =userMapList.get(0);
			devId = userInfoMap.get("device_id");
			deviceTypeId= userInfoMap.get("devicetype_id");
			int isOnline=0;
			String wanPassageWay="";
			// 判断是否绑定设备
			if (StringUtil.IsEmpty(devId))
			{
				checker.setResult(1004);
				checker.setResultDesc("此用户未绑定设备");
				checker.setUserSn(StringUtil.getStringValue(userInfoMap.get("username")));
				checker.setFailureReason("2"); // 没有绑定设备
				checker.setSuccStatus("-1"); // 失败
				logger.warn("2未绑定设备");
			//判断是否路由模式
			} else if (!"2".equals(userInfoMap.get("wan_type"))) {
				checker.setResult(5);
				checker.setResultDesc("宽带不是路由模式，不支持仿真测速");
				checker.setSuccStatus("-1");
				checker.setFailureReason("6");//宽带不是路由模式，不支持仿真测速
				logger.warn("6宽带不是路由模式，不支持仿真测速!");
			//判断设备是否支持仿真测速
			} else if (!speedDao.doSupportSpeed(deviceTypeId)) {
				checker.setResult(5);
				checker.setResultDesc("终端版本不支持仿真测速");
				checker.setSuccStatus("-1");
				checker.setFailureReason("5");//终端版本不支持仿真测速!
				logger.warn("5终端版本不支持仿真测速!");
			//判断设备是否在线
			} else if ((isOnline=isOnline(devId))!=1) {
				checker.setResult(1005);
				checker.setResultDesc("设备不能正常交互");
				if(isOnline==-6){
					checker.setResultDesc("设备正在被操作");
					logger.warn("3设备正在被操作!");
				}
				checker.setSuccStatus("-1");
				checker.setFailureReason("3");//设备不在线
				logger.warn("3设备不在线!");
			//获取wan测速通道，如果获取失败，则返回错误
			} else if(StringUtil.IsEmpty(wanPassageWay=gatherWanPassageWay(devId))){
				checker.setResultDesc("没有采集到wanpassageway");
				checker.setSuccStatus("-1");
				logger.warn("5终端版本不支持仿真测速!");
			//符合所有条件，调用ItmsService测速接口
			}else{
					//包装入参
					StringBuffer inParam = new StringBuffer();
					String downLoadUrl=speedDao.getSpeedUrlByCityId(userInfoMap.get("city_id"));
					inParam.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					inParam.append("<root>\n");
					inParam.append("	<CmdID>").append(checker.getCmdId()).append("</CmdID >\n");
					inParam.append("	<CmdType>CX_01</CmdType>\n");
					inParam.append("	<ClientType>3</ClientType>\n");
					inParam.append("	<Param>\n");
					inParam.append("		<DevSn>").append(userInfoMap.get("device_serialnumber")).append("</DevSn>\n");
					inParam.append("        <OUI>").append(userInfoMap.get("oui")).append("</OUI>\n");
					inParam.append("		<CityId>").append(userInfoMap.get("city_id")).append("</CityId>\n");
					inParam.append("		<WanPassageWay>").append(wanPassageWay).append("</WanPassageWay>\n");
					inParam.append("		<DownURL>").append(downLoadUrl).append("</DownURL>\n");
					inParam.append("		<Priority>").append("2").append("</Priority>\n");
					inParam.append("		<UserName>").append("</UserName>\n");
					inParam.append("		<Password>").append("</Password>\n");
					inParam.append("	</Param>\n");
					inParam.append("</root>\n");
					logger.warn("http:" + inParam.toString());
					String callBack=new DownLoadByHTTP().work(inParam.toString());
					logger.warn("回参：" + callBack);
					// 解析回参
					SAXReader reader = new SAXReader();
					Document document = null;
					Element root =null;
					String RequestsReceivedTime =  "";
					String TransportStartTime =  "";
					String TransportEndTime  = "";
					String ReceiveByteContainHead =  "";
					String ReceiveByte = "";
					String TCPRequestTime =  "";
					String TCPResponseTime = "";
					String MaxSampledValues = "";
					String AvgSampledValues = "";
					try {
						document = reader.read(new StringReader(callBack));
						root = document.getRootElement();
						checker.setResult(StringUtil.getIntegerValue(root.elementTextTrim("RstCode")));
						checker.setResultDesc( root.elementTextTrim("RstMsg"));
						checker.setFailureReason(checker.getFailureReason());
						RequestsReceivedTime =  root.elementTextTrim("RequestsReceivedTime");
						TransportStartTime =  root.elementTextTrim("TransportStartTime");
						TransportEndTime  =  root.elementTextTrim("TransportEndTime");
						ReceiveByteContainHead =  root.elementTextTrim("ReceiveByteContainHead");
						ReceiveByte =  root.elementTextTrim("ReceiveByte");
						TCPRequestTime =  root.elementTextTrim("TCPRequestTime");
						TCPResponseTime =  root.elementTextTrim("TCPResponseTime");
						//成功
						if (0==checker.getResult()) {
							checker.setSuccStatus("1");
							checker.setDevSn(root.elementTextTrim("DevSn"));
							AvgSampledValues = root.elementTextTrim("AvgSampledValues");
							MaxSampledValues = root.elementTextTrim("MaxSampledValues");
							if (!StringUtil.IsEmpty(MaxSampledValues)) {
								checker.setMaxSampledValues(getValue(MaxSampledValues));
							}
							if (!StringUtil.IsEmpty(AvgSampledValues)) {
								checker.setAvgSampledValues(getValue(AvgSampledValues));
							}
						//失败
						} else {
							checker.setSuccStatus("-1");
						}
					} catch (Exception e) {
						checker.setResult(1000);
						checker.setResultDesc("未知错误");
						checker.setSuccStatus("-1");
						checker.setFailureReason("解析测速接口回参有误!");
						logger.warn("解析测速接口回参有误!");
					}finally{
						//记录测速结果
						//speedDao.addSimulationSpeedLog(devId, checker.getSuccStatus(), checker.getAvgSampledValues(), checker.getAvgSampledValues());
						speedDao.addHTTPDiagResult(
								userInfoMap.get("oui"),
								userInfoMap.get("device_serialnumber"), 
								checker.getSuccStatus(),
								StringUtil.getStringValue(System.currentTimeMillis()/1000),
								downLoadUrl,
								"2",
								RequestsReceivedTime,
								TransportStartTime,
								TransportEndTime,
								ReceiveByteContainHead,
								ReceiveByte,
								TCPRequestTime,
								TCPResponseTime,
								Global.G_CityId_CityName_Map.get(userInfoMap.get("city_id")),
								checker.getUserName(),
								userInfoMap.get("username"),
								MaxSampledValues,
								AvgSampledValues
								);
					}
			}
		}
		// 结果包装成xml回参
		String returnXmlStr = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),"SimulationSpeedService");
		logger.warn(
				"servicename[SimulationSpeedService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[]{checker.getCmdId(), checker.getUserInfo(),
						returnXmlStr});
		return returnXmlStr;
	}
	
	/**
	 * 判断是否在线
	 * @param deviceId
	 * @return
	 */
	private int isOnline(String deviceId){
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		int flag = getStatus.testDeviceOnLineStatus(deviceId,
				corba);
		return flag;
	}

	private String gatherWanPassageWay(String deviceId) {
		ACSCorba corba = new ACSCorba();
		String INTERNET = "INTERNET";
		// 江苏可以根据wan连接索引节点来生成上网通道
		String wan_index = "InternetGatewayDevice.WANDevice.1.X_CT-COM_WANIndex";
		String wan_index_result = "";
		logger.warn("[{}]获取wan连接索引",deviceId);
		ArrayList<ParameValueOBJ> valueList = corba.getValue(deviceId,
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
				String wan[] = wan_index_result.replace("\"", "").split(",");
				for (String wanPa : wan) {
					if (wanPa.endsWith(INTERNET) || wanPa.endsWith("internet")) {
						if (wanPa.contains(".") && wanPa.contains(";")) {
							//  路由
							if (wanPa.split(";")[1].equalsIgnoreCase("PPPoE_Routed")) {
								String a = wanPa.split(";")[0].split("\\.")[0];
								String b = wanPa.split(";")[0].split("\\.")[1];
								String result = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."
										+ a + ".WANPPPConnection." + b + ".";
								logger.warn("[{}]走wan连接索引获取上网通道[{}]", deviceId,
										result);
								return result;
							}
						}

					}
				}
			}
		}
		// 获取不到走ijk
		 logger.warn("[{}]未获取到wan连接索引值，走ijk采集",deviceId);
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanIPConnection = ".WANIPConnection.";

		ArrayList<String> wanConnPathsList = null;
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
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
			if (namepath.indexOf(wanServiceList) >= 0) {
				serviceListList.add(namepath);
				paramNameList.add(namepath);
				continue;
			}
		}
		if (serviceListList.size() == 0 || serviceListList.isEmpty()) {
			logger.warn(
					"[SimulationSpeedService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回",
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
				logger.warn("[SimulationSpeedService] [{}]获取ServiceList失败",
						deviceId);
			}
			for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
				logger.debug(
						"[{}]{}={} ",
						new Object[] { deviceId, entry.getKey(),
								entry.getValue() });
				String paramName = entry.getKey();
				if (paramName.indexOf(wanPPPConnection) >= 0) {
				} else if (paramName.indexOf(wanIPConnection) >= 0) {
					continue;
				}
				if (paramName.indexOf(wanServiceList) >= 0) {
					if (!StringUtil.IsEmpty(entry.getValue())
							&& entry.getValue().indexOf(INTERNET) >= 0) {// X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径
						String res = entry.getKey().substring(0,
								entry.getKey().indexOf("X_CT-COM_ServiceList"));
						logger.warn(res);
						return res;
					}
				}
			}

		}

		return "";
	}

	
	/**
	 * kb与m换算
	 * 
	 * @param sampledValues
	 *            数组
	 * @return 平均值
	 */
	private String getValue(String sampledValues) {
		// 保留小数点后两位
		DecimalFormat df = new DecimalFormat("######0.00");
		double result = Double.parseDouble(sampledValues) / 128;
		return StringUtil.getStringValue(df.format(result));
	}
}
