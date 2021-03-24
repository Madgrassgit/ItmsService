
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
import com.linkage.itms.dao.ChangVlanParamDAO;
import com.linkage.itms.dispatch.obj.ChangVlanParamChecker4NX;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * 宁夏修改光猫VLAN参数接口
 * 
 * @author chenxj6
 * @version 1.0
 * @since 2016-10-12
 */
public class ChangVlanParamService4NX implements IService
{
//	目前宁夏光猫宽带固定VLAN 81 (lan1,lan4,WLAN 1),   IPTV 固定VLAN 43(LAN 2,LAN3,SSID2)；当端到端调用该接口是， chenxj6
//	ITMS需要判断该端口是几个VLAN，如果是一个VLAN，就直接将旧VLAN置成81或者43,；如果一个端口有一个以上的VLAN，需要将非81或者非43的VLAN删除。
	private static final Logger logger = LoggerFactory
			.getLogger(ChangVlanParamService4NX.class);

	@Override
	public String work(String inParam)
	{
		logger.warn("ChangVlanParamService4NX==>inParam:" + inParam);
		ChangVlanParamChecker4NX checker = new ChangVlanParamChecker4NX(inParam);
		if (false == checker.check())
		{
			logger.warn("获取修改光猫VLAN参数接口，入参验证失败，loid=[{}]",
					new Object[] { checker.getUserInfo() });
			logger.warn("ChangVlanParamService4NX==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		String userId = "";
		String deviceId = "";
		String oui = "";
		String devSn = "";
		
		ChangVlanParamDAO dao = new ChangVlanParamDAO();
		Map<String, String> userMap = dao.getUserMapByLoid(checker.getUserInfo());
		if(null == userMap){
			logger.warn("serviceName[ChangVlanParamService4NX]cmdId[{}]loid[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("查无此用户信息");
			return checker.getReturnXml();
		}
		userId = userMap.get("user_id");
		deviceId = userMap.get("device_id");
		oui = userMap.get("oui");
		devSn = userMap.get("device_serialnumber");
		if (StringUtil.IsEmpty(userId))
		{
			logger.warn("serviceName[ChangVlanParamService4NX]cmdId[{}]loid[{}]无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		
		if (StringUtil.IsEmpty(deviceId)){
			logger.warn("serviceName[ChangVlanParamService4NX]cmdId[{}]loid[{}]没有绑定设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("没有绑定设备");
			return checker.getReturnXml();
		}
		
	
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		
		logger.warn("设备[{}],在线状态[{}] ",new Object[]{deviceId, flag});
		
		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		// 设备在线
		else if (1 == flag) {
			logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
			
			
			String vlanIdPath = "";
			
			String accessType = dao.getAccessType(deviceId);
			// 以下获取的方式是NX专用
			if (null == accessType)
			{
				String[] gatherPath = new String[]{
						"InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType"
						};
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
				
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
				logger.warn("accessType既不是EPON也不是GPON");
				checker.setResult(1000);
				checker.setResultDesc("accessType既不是EPON也不是GPON");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			
			String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
			
			String wanServiceList = ".X_CT-COM_ServiceList";
			String wanPPPConnection = ".WANPPPConnection.";
			String wanIPConnection = ".WANIPConnection.";
			String INTERNET = "INTERNET";
			String OTHER = "OTHER";
			String Other = "Other";
			List<String> netIjk = new ArrayList<String>();
			List<String> iptvIjk = new ArrayList<String>();
			Map<String,String> vlanId = new HashMap<String,String>();
			
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
					logger.warn("[{}]获取WANConnectionDevice下iList失败,iList为空", deviceId);
					checker.setResult(1000);
					checker.setResultDesc("WANConnectionDevice节点值没有获取到");
					return checker.getReturnXml();
				}
				for (String j : jList)
				{
					// 获取session，
					List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j
							+ wanPPPConnection);
					if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
					{
						logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
								+ wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
						kPPPList = corba.getIList(deviceId, wanConnPath + j
								+ wanIPConnection);
						if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
						{
							logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
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
			// serviceList节点
			ArrayList<String> serviceListList = new ArrayList<String>();
			// 所有需要采集的节点
			ArrayList<String> paramNameList = new ArrayList<String>();
			ArrayList<String> vlanNameList = new ArrayList<String>();
			for (int i = 0; i < wanConnPathsList.size(); i++)
			{
				String namepath = wanConnPathsList.get(i);
				if(namepath.indexOf(vlanIdPath) >=0){
					paramNameList.add(namepath);
				}
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
				checker.setResult(1006);
				checker.setResultDesc("设备没有vlan");
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
				Map<String, String> maptemp = corba.getParaValueMap(deviceId,
						paramNametemp);
				if (maptemp != null && !maptemp.isEmpty())
				{
					paramValueMap.putAll(maptemp);
				}
			}
			if (paramValueMap.isEmpty())
			{
				logger.warn("设备[{}]：获取wan连接失败",deviceId);
				checker.setResult(1000);
				checker.setResultDesc("获取wan连接失败");
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
						
						netIjk.add("1," + j + "," + k);
					}
					if (!StringUtil.IsEmpty(entry.getValue())
							&& (entry.getValue().indexOf(OTHER) >= 0 || entry.getValue().indexOf(Other) >= 0) || "OTHER".equalsIgnoreCase(entry.getValue())){//X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径
						iptvIjk.add("1," + j + "," + k);
					}
				}
				if (paramName.indexOf(vlanIdPath) >= 0)
				{
					vlanId.put("1," + j + ",1", StringUtil.getStringValue(entry.getValue()));
				}
			}
			
			
			if(netIjk.size()==0){
				logger.warn("设备[{}]：INTERNET的wan连接个数为0",deviceId);
				checker.setResult(1000);
				checker.setResultDesc("INTERNET的wan连接个数为0");
				logger.warn("return=({})", checker.getReturnXml());
				return checker.getReturnXml();
			}
			
			if(iptvIjk.size()==0){
				logger.warn("设备[{}]：IPTV的wan连接个数为0",deviceId);
				checker.setResult(1000);
				checker.setResultDesc("IPTV的wan连接个数为0");
				logger.warn("return=({})", checker.getReturnXml());
				return checker.getReturnXml();
			}
			
			
			
			for(int i = 0; i < netIjk.size(); i++){
				String ijkPath = netIjk.get(i);
				String netVlan = StringUtil.getStringValue(vlanId.get(ijkPath));
				logger.warn("[{}]ijkPath= " + ijkPath+ " ,netVlan = " + netVlan, deviceId);
				String j = ijkPath.split(",")[1];
				if(i > 0){
					int status = corba.del(deviceId, wanConnPath + j + ".");
					if (1 == status || 0 == status) {
						logger.warn("删除参数节点成功，节点路径为：[{}]",	wanConnPath + j + ".");
					} else {
						logger.warn("删除参数节点失败，节点路径为：[{}]",	wanConnPath + j + ".");
						checker.setResult(1000);
						checker.setResultDesc("删除参数节点失败，节点路径为："+ (wanConnPath + j + "."));
						return checker.getReturnXml();
					}
				}else{
					if (!"81".equals(netVlan) && !"".equals(netVlan)) {
						ParameValueOBJ pvOBJ = new ParameValueOBJ();
						pvOBJ.setName(wanConnPath + j + vlanIdPath);
						// 参数类型：1 string , 2 int , 3 unsignedInt , 4 boolean
						pvOBJ.setType("1");
						pvOBJ.setValue("81");
						int retResult = corba.setValue(deviceId, pvOBJ);

						if (0 == retResult || 1 == retResult) {
							checker.setResult(0);
							checker.setResultDesc("节点值设置成功");
							String returnXml = checker.getReturnXml();
							logger.warn("servicename[ChangVlanParamService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
									new Object[] {checker.getCmdId(),checker.getUserInfo(),returnXml });
						} else if (-1 == retResult) {
							checker.setResult(1000);
							checker.setResultDesc("设备连接失败");
							String returnXml = checker.getReturnXml();
							logger.warn("servicename[ChangVlanParamService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
									new Object[] {checker.getCmdId(),checker.getUserInfo(),	returnXml });
							return returnXml;
						} else if (-6 == retResult) {
							checker.setResult(1000);
							checker.setResultDesc("设备正被操作");
							String returnXml = checker.getReturnXml();
							logger.warn("servicename[ChangVlanParamService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
									new Object[] {checker.getCmdId(),checker.getUserInfo(),returnXml });
							return returnXml;
						} else if (-7 == retResult) {
							checker.setResult(1000);
							checker.setResultDesc("系统参数错误");
							String returnXml = checker.getReturnXml();
							logger.warn("servicename[ChangVlanParamService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
									new Object[] {checker.getCmdId(),checker.getUserInfo(),returnXml });
							return returnXml;
						} else if (-9 == retResult) {
							checker.setResult(1000);
							checker.setResultDesc("系统内部错误");
							String returnXml = checker.getReturnXml();
							logger.warn("servicename[ChangVlanParamService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
									new Object[] {checker.getCmdId(),checker.getUserInfo(),returnXml });
							return returnXml;
						} else {
							checker.setResult(1000);
							checker.setResultDesc("TR069错误");
							String returnXml = checker.getReturnXml();
							logger.warn("servicename[ChangVlanParamService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
									new Object[] {checker.getCmdId(),checker.getUserInfo(),returnXml });
							return returnXml;
						}
						}
					} 
				}
			for(int i = 0; i < iptvIjk.size(); i++){
				String ijkPath = iptvIjk.get(i);
				String iptvVlan = StringUtil.getStringValue(vlanId.get(ijkPath));
				logger.warn("[{}]ijkPath= " + ijkPath+ " ,iptvVlan = " + iptvVlan , deviceId);
				String j = ijkPath.split(",")[1];
				if(i > 0){
					int status = corba.del(deviceId, wanConnPath + j + ".");
					if (1 == status || 0 == status) {
						logger.warn("删除参数节点成功，节点路径为：[{}]",	wanConnPath + j + ".");
					} else {
						logger.warn("删除参数节点失败，节点路径为：[{}]",	wanConnPath + j + ".");
						checker.setResult(1000);
						checker.setResultDesc("删除参数节点失败，节点路径为："+ (wanConnPath + j + "."));
						return checker.getReturnXml();
					}
				}else{
					if (!"43".equals(iptvVlan) && !"".equals(iptvVlan)) {
						ParameValueOBJ pvOBJ = new ParameValueOBJ();
						pvOBJ.setName(wanConnPath + j + vlanIdPath);
						// 参数类型：1 string , 2 int , 3 unsignedInt , 4 boolean
						pvOBJ.setType("1");
						pvOBJ.setValue("43");
						int retResult = corba.setValue(deviceId, pvOBJ);
						if (0 == retResult || 1 == retResult) {
							checker.setResult(0);
							checker.setResultDesc("节点值设置成功");
							String returnXml = checker.getReturnXml();
							logger.warn("servicename[ChangVlanParamService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
									new Object[] {checker.getCmdId(),checker.getUserInfo(),returnXml });
						} else if (-1 == retResult) {
							checker.setResult(1000);
							checker.setResultDesc("设备连接失败");
							String returnXml = checker.getReturnXml();
							logger.warn("servicename[ChangVlanParamService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
									new Object[] {checker.getCmdId(),checker.getUserInfo(),	returnXml });
							return returnXml;
						} else if (-6 == retResult) {
							checker.setResult(1000);
							checker.setResultDesc("设备正被操作");
							String returnXml = checker.getReturnXml();
							logger.warn("servicename[ChangVlanParamService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
									new Object[] {checker.getCmdId(),checker.getUserInfo(),returnXml });
							return returnXml;
						} else if (-7 == retResult) {
							checker.setResult(1000);
							checker.setResultDesc("系统参数错误");
							String returnXml = checker.getReturnXml();
							logger.warn("servicename[ChangVlanParamService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
									new Object[] {checker.getCmdId(),checker.getUserInfo(),returnXml });
							return returnXml;
						} else if (-9 == retResult) {
							checker.setResult(1000);
							checker.setResultDesc("系统内部错误");
							String returnXml = checker.getReturnXml();
							logger.warn("servicename[ChangVlanParamService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
									new Object[] {checker.getCmdId(),checker.getUserInfo(),returnXml });
							return returnXml;
						} else {
							checker.setResult(1000);
							checker.setResultDesc("TR069错误");
							String returnXml = checker.getReturnXml();
							logger.warn("servicename[ChangVlanParamService4NX]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
									new Object[] {checker.getCmdId(),checker.getUserInfo(),returnXml });
							return returnXml;
						}
						}
					} 
				}
			}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值");
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml());
			return checker.getReturnXml();
		}
		logger.warn("ReturnXml[{}]",checker.getReturnXml());
		return checker.getReturnXml();
	}
}
