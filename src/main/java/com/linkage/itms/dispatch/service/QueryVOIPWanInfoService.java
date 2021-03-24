
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commom.util.CheckStrategyUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryVOIPWanInfoDao;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryVOIPWanInfoChecker;

/**
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2016年4月22日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QueryVOIPWanInfoService implements IService
{

	/** 日志对象 */
	private static final Logger logger = LoggerFactory
			.getLogger(QueryVOIPWanInfoService.class);
	private UserDeviceDAO userDevDao = new UserDeviceDAO();
	private QueryVOIPWanInfoDao voipDao = new QueryVOIPWanInfoDao();

	@Override
	public String work(String inXml)
	{
		logger.warn("QueryVOIPWanInfoService inXml ({})", inXml);
		QueryVOIPWanInfoChecker checker = new QueryVOIPWanInfoChecker(inXml);
		if (false == checker.check())
		{
			logger.error(
					"servicename[QueryVOIPWanInfoService]cmdId[{}]UserInfo[{}]验证未通过.返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[QueryVOIPWanInfoService]cmdId[{}]UserInfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		// 根据参数查询数据库是否有此设备的信息,根据参数判断哪种查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo(), checker.getCityId());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn("serviceName[QueryVOIPWanInfoService]cmdId[{}]userinfo[{}]无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("查无此客户");
			return checker.getReturnXml();
		}
		else
		{
			String deviceId = userInfoMap.get("device_id");
			String userId = userInfoMap.get("user_id");
			if (StringUtil.IsEmpty(deviceId))
			{
				logger.warn(
						"serviceName[QueryVOIPWanInfoService]cmdId[{}]userinfo[{}]未绑定设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1003);
				checker.setResultDesc("未绑定设备");
				return checker.getReturnXml();
			}
			// (江西)判断设备是否繁忙或者业务正在下发
			if ("jx_dx".equals(Global.G_instArea)
					&& false == CheckStrategyUtil.chechStrategy(deviceId))
			{
				logger.warn(
						"serviceName[QueryVOIPWanInfoService]cmdId[{}]userinfo[{}]设备繁忙或者业务正在下发，请稍候重试",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1003);
				checker.setResultDesc("设备繁忙或者业务正在下发，请稍候重试");
				return checker.getReturnXml();
			}
			//江西优化采集效率
			else if("jx_dx".equals(Global.G_instArea)){
				Map<String, String> voipMap = getVoipParam(deviceId, checker);
				
				if(null!=voipMap){
					List<HashMap<String, String>> voipPortList = voipDao
							.getVoipPort(userId);
					
					for(HashMap<String, String> tempMap : voipPortList)
					{
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("ExternalIPAddress", voipMap.get("ExternalIPAddress"));
						map.put("ConnectionStatus", voipMap.get("ConnectionStatus"));
						map.put("InterfaceID", tempMap.get("voip_port"));
						checker.getVoipInfos().add(map);
					}
				}
				else{
					checker.setResult(1000);
					return checker.getReturnXml();
				}
			}
			else
			{
				// 开始采集
				logger.warn(
						"serviceName[QueryVOIPWanInfoService]cmdId[{}]userinfo[{}]开始采集[{}]",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),
								deviceId });
				// 调CORBAR 采集 0表示采集所有节点 在原来基础上增加了一个参数(3)
				int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3);
				logger.warn(
						"serviceName[QueryVOIPWanInfoService]cmdId[{}]userinfo[{}]getCpeParams设备配置信息采集结果[{}]",
						new Object[] { checker.getCmdId(), checker.getUserInfo(), rsint });
				// 采集失败
				if (rsint != 1)
				{
					logger.warn(
							"serviceName[QueryVOIPWanInfoService]cmdId[{}]userinfo[{}]getData sg fail",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1000);
					checker.setResultDesc("设备采集失败");
					return checker.getReturnXml();
				}
				else
				{
					// 采集成功
					HashMap<String, String> voipMap = getVoip(deviceId);
					//voip信息查出来只有一条
					if(null==voipMap || voipMap.isEmpty())
					{
						checker.setResult(1000);
						checker.setResultDesc("没有voip信息");
						checker.getReturnXml();
					}
					else
					{
						logger.warn("查询物理标识，user_id{}", userId);
						List<HashMap<String, String>> voipPortList = voipDao
								.getVoipPort(userId);
						
						for(HashMap<String, String> tempMap : voipPortList)
						{
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("ExternalIPAddress", voipMap.get("ip"));
							map.put("ConnectionStatus", voipMap.get("conn_status"));
							map.put("InterfaceID", tempMap.get("voip_port"));
							checker.getVoipInfos().add(map);
						}
						
					}
				}
			}
		}
		return checker.getReturnXml();
	}

	/**
	 * 获取所有的session信息,根据serv_list分类
	 * 
	 * @param device_id
	 * @return
	 */
//	private List<HashMap<String, String>> getAllChannel(String device_id)
//	{
//		logger.debug("getAllChannel({})", device_id);
//		List<HashMap<String, String>> list = voipDao.getAllChannel(device_id);
//		List<HashMap<String, String>> voip_list = new ArrayList<HashMap<String, String>>();
//		HashMap<String, String> map = new HashMap<String, String>();
//		String servList = null;
//		for (int i = 0; i < list.size(); i++)
//		{
//			map = list.get(i);
//			if (map == null || map.size() == 0)
//				continue;
//			servList = (String) map.get("serv_list");
//			if (StringUtil.IsEmpty(servList))
//				continue;
//			else
//			{
//				if (servList.toUpperCase().indexOf("VOIP") != -1)
//				{
//					voip_list.add(map);
//				}
//			}
//		}
//		return voip_list;
//	}
	
	private HashMap<String, String> getVoip(String device_id)
	{
		logger.debug("getAllChannel({})", device_id);
		List<HashMap<String, String>> list = voipDao.getAllChannel(device_id);
		HashMap<String, String> map = new HashMap<String, String>();
		String servList = null;
		for (int i = 0; i < list.size(); i++)
		{
			map = list.get(i);
			if (map == null || map.size() == 0)
				continue;
			servList = (String) map.get("serv_list");
			if (StringUtil.IsEmpty(servList))
				continue;
			else
			{
				if (servList.toUpperCase().indexOf("VOIP") != -1)
				{
					return map;
				}
			}
		}
		return null;
	}
	
	/**
	 * 返回采集到的参数Map，key为("ExternalIPAddress","ConnectionStatus")
	 * @param device_id
	 * @return''[]
	 */
	private Map<String, String> getVoipParam(String device_id, QueryVOIPWanInfoChecker checker)
	{
		ACSCorba acsCorba = new ACSCorba();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(device_id,
				acsCorba);
		if(flag==1){
			logger.warn("[{}]开始获取WAN通道信息", new Object[]{device_id});
			//获得InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice. {i}.WANIPConnection. 
			String IPConnPath = this.getVoipwanCon(device_id,acsCorba);
			if(null != IPConnPath){
				List<String> kList = acsCorba.getIList(device_id, IPConnPath);
				
				if (null == kList || kList.size() == 0 || kList.isEmpty()) {
					logger.warn("[QueryVOIPWanInfoService] [{}]获取WANIPConnection" + 
							 "下实例号失败，返回", device_id);
					return null;
				}
				else if(kList.size() > 1){
					logger.warn("[QueryVOIPWanInfoService] [{}]获取WANIPConnection" + 
							 "下实例号大于1，取第一条", device_id);
				}
				
				String[] paramName = new String[2];
				paramName[0] = IPConnPath + kList.get(0) + ".ExternalIPAddress";
				paramName[1] = IPConnPath + kList.get(0) + ".ConnectionStatus";
				
				Map<String, String> paramValueMap = acsCorba.getParaValueMap(device_id,
						paramName);
				if (paramValueMap.isEmpty()) {
					logger.warn("[QueryVOIPWanInfoService] [{}]获取语音参数失败",
							device_id);
				}
				
				Map<String,String> res = new HashMap<String,String>();
				for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
					logger.debug("[{}]{}={} ",
							new Object[]{device_id, entry.getKey(), entry.getValue()});
					//语音节点
					if (entry.getKey().indexOf("ExternalIPAddress") >= 0) {
						res.put("ExternalIPAddress", entry.getValue());
						continue;
					}
					else if (entry.getKey().indexOf("ConnectionStatus") >= 0) {
						res.put("ConnectionStatus", entry.getValue());
					}
				}
				return res;
			}
			else{
				return null;
			}
		}
		else{
			checker.setResultDesc("设备不在线");
			logger.warn("device is not online, device_id is {}",device_id);
		}
		
		return null;
	}

	/**
	 * 获取InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice. {i}.WANIPConnection. 
	 * @param deviceId
	 * @param corba
	 * @return
	 */
	private String getVoipwanCon(String deviceId, ACSCorba corba)
	{
		String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
		String wanPPPConnection = ".WANPPPConnection.";
		String wanServiceList = ".X_CT-COM_ServiceList";
		String wanIPConnection = ".WANIPConnection.";
		String SERV_LIST_VOIP = "VOIP";
		
		ArrayList<String> wanConnPathsList = new ArrayList<String>();
		// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
		wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);
		logger.warn("wanConnPathsList.size:{}",wanConnPathsList.size());
		if (wanConnPathsList == null || wanConnPathsList.size() == 0
				|| wanConnPathsList.isEmpty()) {
			//直接采集路径名
			/*List<String> jList = corba.getIList(deviceId, wanConnPath);
			if (null == jList || jList.size() == 0 || jList.isEmpty()) {
				logger.warn("[QueryVOIPWanInfoService] [{}]获取" + wanConnPath
						+ "下实例号失败，返回", deviceId);
				return null;
			}
			logger.warn("jList is : " + jList);
			for (String j : jList) {
				// 获取wanPPPConnection下的k
				List<String> kPPPList = corba.getIList(deviceId, wanConnPath
						+ j + wanPPPConnection);
				if (null != kPPPList && kPPPList.size() != 0
						&& !kPPPList.isEmpty()) {
					for (String kppp : kPPPList) {
						wanConnPathsList.add(wanConnPath + j + wanPPPConnection
								+ kppp + wanServiceList);
					}
				}
				// 获取wanIPConnection下的k
				List<String> kIPList = corba.getIList(deviceId, wanConnPath
						+ j + wanIPConnection);
				if (null != kIPList && kIPList.size() != 0
						&& !kIPList.isEmpty()) {
					for (String kppp : kIPList) {
						wanConnPathsList.add(wanConnPath + j + wanIPConnection
								+ kppp + wanServiceList);
					}
				}
			}
			logger.warn("wanConnPathsList is : " + wanConnPathsList);*/
			return null;
		}
		else{
			ArrayList<String> paramNameList = new ArrayList<String>();
			for (int i = 0; i < wanConnPathsList.size(); i++) {
				String namepath = wanConnPathsList.get(i);
				if (namepath.indexOf(wanServiceList) >= 0) {
					paramNameList.add(namepath);
				}
			}
			wanConnPathsList = new ArrayList<String>();
			wanConnPathsList.addAll(paramNameList);
		}
		
		if(wanConnPathsList.size()==0){
			logger.warn("[QueryVOIPWanInfoService] [{}]无节点：" + wanConnPath+".j.wanPPPConnection/wanIPConnection."+wanServiceList
					+ "下实例号失败，返回", deviceId);
			return null;
		}
		
		String[] paramNametemp = new String[wanConnPathsList.size()];
		for(int i=0;i<wanConnPathsList.size();i++){
			paramNametemp[i] = wanConnPathsList.get(i);
		}
		
		Map<String, String> paramValueMap = corba.getParaValueMap(deviceId,
				paramNametemp);
		
		if (paramValueMap.isEmpty()) {
			logger.warn("[QueryVOIPWanInfoService] [{}]获取ServiceList失败",
					deviceId);
		}
		for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
			logger.debug("[{}]{}={} ",
					new Object[]{deviceId, entry.getKey(), entry.getValue()});
			//语音节点
			if (entry.getValue().indexOf(SERV_LIST_VOIP) >= 0) {
				if(entry.getKey().indexOf(wanPPPConnection) >= 0){
					return entry.getKey().substring(0, entry.getKey().indexOf(wanPPPConnection))+wanPPPConnection;
				}
				return entry.getKey().substring(0, entry.getKey().indexOf(wanIPConnection))+wanIPConnection;
			}
		}
		return null;
	}
}
