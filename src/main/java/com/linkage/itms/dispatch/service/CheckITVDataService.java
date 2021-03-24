package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.CheckITVDataDAO;
import com.linkage.itms.dispatch.obj.CheckITVDataChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 新疆电信：ITMS检查光猫ITV口数据配置是否正确，采集itv的vlan配置是否正确
 * 
 * @author chenxj6
 * @date 2016-8-29
 * @param param
 * @return
 */
public class CheckITVDataService implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(CheckITVDataService.class);

	@Override
	public String work(String inParam)
	{
		logger.warn("CheckITVDataService==>inParam:" + inParam);
		CheckITVDataChecker checker = new CheckITVDataChecker(inParam);
		if (false == checker.check())
		{
			logger.warn("检查光猫ITV口数据配置是否正确，入参验证失败，UserInfoType=[{}]，UserInfo=[{}]",
					new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
			logger.warn("CheckITVDataService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		String deviceId = "";
		
		List<HashMap<String,String>> userMapList = null;
		List<HashMap<String,String>> deviceMapList = null;
//		QueryIsMulticastVlanDAO dao = new QueryIsMulticastVlanDAO(); // QueryWlanStateService
		CheckITVDataDAO dao = new CheckITVDataDAO();
		
		
		if(checker.getUserInfoType()==1)
		{
			userMapList = dao.queryUserByNetAccount(checker.getUserInfo());
		}else if (checker.getUserInfoType() == 2)
		{
			userMapList = dao.queryUserByLoid(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 3)
		{
			userMapList = dao.queryUserByIptvAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 4)
		{
			userMapList = dao.queryUserByVoipPhone(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 5)
		{
			userMapList = dao.queryUserByVoipAccount(checker.getUserInfo());
		}
		
		if (userMapList == null || userMapList.isEmpty())
		{
			logger.warn("查无此客户");
			checker.setResult(1000);
			checker.setResultDesc("查无此客户");
			return checker.getReturnXml();
		}
		
		
		String devSn = checker.getDevSn();
		if(devSn==null || devSn.trim().length()==0){
			if(userMapList.size()>1){
				logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				return checker.getReturnXml();
			}else{
				deviceId = StringUtil.getStringValue(userMapList.get(0), "device_id", "");
				if (StringUtil.IsEmpty(deviceId))
				{
					logger.warn("用户未绑定设备");
					checker.setResult(1002);
					checker.setResultDesc("用户未绑定设备");
					return checker.getReturnXml();
				}
			} 
		}else{
			devSn = devSn.trim();
			if(devSn.length()<6){
				logger.warn("按设备序列号查询时，查询序列号字段少于6位");
				checker.setResult(1005);
				checker.setResultDesc("设备序列号非法");
				return checker.getReturnXml();
			}else{
				deviceMapList = dao.queryDeviceByDevSN(devSn);
				if(deviceMapList==null || deviceMapList.size()==0){
					logger.warn("没有查到设备");
					checker.setResult(1000);
					checker.setResultDesc("没有查到设备");
					return checker.getReturnXml();
				}else if(deviceMapList.size()>1){
					logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询");
					checker.setResult(1006);
					checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
					return checker.getReturnXml();
				}else{
					deviceId = StringUtil.getStringValue(deviceMapList.get(0), "device_id", "");
					boolean flagTemp = false;
					for(HashMap<String,String> userMap : userMapList){
						if(userMap.containsValue(deviceId)){
							flagTemp = true;
							break;
						}
					}
					if(false==flagTemp){
						logger.warn("用户未绑定该设备");
						checker.setResult(1000);
						checker.setResultDesc("用户未绑定该设备");
						return checker.getReturnXml();
					}
				}
			}
		}
		
		
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
			
			String vlanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
			String vlan45Path = "";
			
		    List<String> iList = corba.getIList(deviceId, vlanPath);
			if (null == iList || iList.isEmpty())
			{
				logger.warn("[{}]获取iList失败,iList为空", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return checker.getReturnXml();
			}else{
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
			}
			
			
			String accessType = dao.getAccessType(deviceId);
			logger.warn("数据库中，accessType为：[{}]", accessType);
			
			// 以下获取的方式是新疆专用
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
				
				if (null != accessType && !"null".equals(accessType) && !"".equals(accessType))
				{
					dao.insertWan(deviceId, "1", accessType);
				}
				
				logger.warn("采集到的，accessType为：[{}]", accessType);
				
				// xj 专用
//				if (Global.SERVICE_ID_VOIP.equals(serviceId)) {
//					dao.updateHgwcustServInfoVOIP_XJ(deviceId, -1);
//				}
			}
			
			String checkAccessType = "";
			String checkAccessTypeVLANIDMARK = "";
			
			if("EPON".equals(accessType)){
				checkAccessType = ".X_CT-COM_WANEponLinkConfig.VLANID";
				checkAccessTypeVLANIDMARK = ".X_CT-COM_WANEponLinkConfig.VLANIDMark";
			}else if("GPON".equals(accessType)){
				checkAccessType = ".X_CT-COM_WANGponLinkConfig.VLANID";
				checkAccessTypeVLANIDMARK = ".X_CT-COM_WANGponLinkConfig.VLANIDMark";
			}else{
				logger.warn("accessType既不是EPON也不是GPON");
				checker.setResult(1000);
				checker.setResultDesc("accessType既不是EPON也不是GPON");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			 
			for(String i : iList){
				
				String[] gatherPath = new String[]{
						"InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+i+checkAccessType
						};
				
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
				if (null == objLlist || objLlist.isEmpty()) {
					gatherPath = new String[]{
							"InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+i+checkAccessTypeVLANIDMARK
							};
					objLlist = corba.getValue(deviceId, gatherPath);
					if (null == objLlist || objLlist.isEmpty()) {
					continue;
					}
				}
				
				String vlanId = "";
				for(ParameValueOBJ pvobj : objLlist){
					if(pvobj.getName().contains("VLANID") || pvobj.getName().contains("VLANIDMark")){
						vlanId = pvobj.getValue();
					}
				}
				
				if("45".equals(vlanId)){
					vlan45Path = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice."+i+".WANPPPConnection.";
					break;
				}
			}
			
			
			if("".equals(vlan45Path)){
				logger.warn("VLANID节点采集结果都不是45");
				checker.setResult(1000);
				checker.setResultDesc("VLANID节点采集结果都不是45");
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			
			
		    List<String> vlan45List = corba.getIList(deviceId, vlan45Path);
			if (null == vlan45List || vlan45List.isEmpty())
			{
				logger.warn("[{}]获取vlan45List失败，返回", deviceId);
				checker.setResult(1000);
				checker.setResultDesc("vlanId是45的节点采集为空，请确认节点路径是否正确");
				return checker.getReturnXml();
			}else{
				logger.warn("[{}]获取vlan45List成功，vlan45List.size={}", deviceId,vlan45List.size());
			}
			
			String LanInterface = "";
			String ServiceList = "";
			String LanInterfaceDHCPEnable = "";
			
			
			for(String i : vlan45List){
				String[] gatherPath = new String[]{
						vlan45Path+i+".X_CT-COM_LanInterface",
						vlan45Path+i+".X_CT-COM_ServiceList",
						vlan45Path+i+".X_CT-COM_LanInterface-DHCPEnable"
						};
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
				if (null == objLlist || objLlist.isEmpty()) {
					continue;
				}
				
				for(ParameValueOBJ pvobj : objLlist){
					if(pvobj.getName().endsWith("LanInterface")){
						LanInterface = pvobj.getValue();
					}else if(pvobj.getName().endsWith("ServiceList")){
						ServiceList = pvobj.getValue();
					}else if(pvobj.getName().endsWith("LanInterface-DHCPEnable")){
						LanInterfaceDHCPEnable = pvobj.getValue();
					}
				}
				
			}
			
			String errResult = "";
			
			logger.warn("LanInterface 采集结果为: [{}]", LanInterface);
			logger.warn("ServiceList 采集结果为: [{}]", ServiceList);
			logger.warn("LanInterfaceDHCPEnable 采集结果为: [{}]", LanInterfaceDHCPEnable);

			if(LanInterface==null || !LanInterface.contains("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2")){
				errResult += ".X_CT-COM_LanInterface采集结果有误; ";
			}
			if(!"OTHER".equals(ServiceList)){
				errResult += ".X_CT-COM_ServiceList采集结果有误; ";
			}
			if(!"0".equals(LanInterfaceDHCPEnable)){
				errResult += ".X_CT-COM_LanInterface-DHCPEnable采集结果有误; ";
			}
			// InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2
			
			String[] gatherPath = new String[] {
					"InternetGatewayDevice.Services.X_CT-COM_IPTV.IGMPEnable",
					"InternetGatewayDevice.Services.X_CT-COM_IPTV.ProxyEnable",
					"InternetGatewayDevice.Services.X_CT-COM_IPTV.SnoopingEnable" };

			ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId,gatherPath);
			if (null == objLlist || objLlist.isEmpty()) {
				errResult += ".X_CT-COM_IPTV没有采到结果; ";
			}

			String iGMPEnable = "";
			String proxyEnable = "";
			String snoopingEnable = "";
			for (ParameValueOBJ pvobj : objLlist) {
				if (pvobj.getName().endsWith("IGMPEnable")) {
					iGMPEnable = pvobj.getValue();
				} else if (pvobj.getName().endsWith("ProxyEnable")) {
					proxyEnable = pvobj.getValue();
				} else if (pvobj.getName().endsWith("SnoopingEnable")) {
					snoopingEnable = pvobj.getValue();
				}
			}
			
			logger.warn("iGMPEnable 采集结果为: [{}]", iGMPEnable);
			logger.warn("proxyEnable 采集结果为: [{}]", proxyEnable);
			logger.warn("snoopingEnable 采集结果为: [{}]", snoopingEnable);
				
			if(!"0".equals(iGMPEnable)){
				errResult += ".IGMPEnable采集结果有误; ";
			}
			if(!"1".equals(proxyEnable)){
				errResult += ".ProxyEnable采集结果有误; ";
			}
			if(!"1".equals(snoopingEnable)){
				errResult += ".SnoopingEnable采集结果有误; ";
			}
			
			
			if(!"".equals(errResult)){
				logger.warn("比对失败，errResult：[{}]",errResult);
				checker.setResult(1008);
				checker.setResultDesc("比对失败:"+errResult);
				logger.warn("return=({})", checker.getReturnXml());  // 打印回参
				return checker.getReturnXml();
			}
			
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值");
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		logger.warn("对比成功");
		checker.setResult(0);
		checker.setResultDesc("成功");
		return checker.getReturnXml();
	}
}


