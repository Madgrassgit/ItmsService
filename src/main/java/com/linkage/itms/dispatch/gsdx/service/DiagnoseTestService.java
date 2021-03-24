package com.linkage.itms.dispatch.gsdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.gsdx.PvcVlanIdTool;
import com.linkage.itms.dispatch.gsdx.beanObj.Para;
import com.linkage.itms.dispatch.gsdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.gsdx.obj.DiagnoseTestXML;
import com.linkage.itms.dispatch.util.ConfigUtil;
import com.linkage.itms.obj.ParameValueOBJ;

public class DiagnoseTestService extends ServiceFather {

	public DiagnoseTestService(String methodName) {
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(DiagnoseTestService.class);
	private ACSCorba corba = new ACSCorba();
	private DiagnoseTestXML dealXML;

	public int work(String inXml,Para[] param) {
		logger.warn(methodName + "执行，inXml入参为：{} ", inXml);
		logger.warn(methodName + "执行，param长度为{} ",param.length);
		logger.warn(methodName + "执行，param入参为{} ",param.toString());
		dealXML = new DiagnoseTestXML(methodName);
		// 验证入参
		if (null == dealXML.getXML(inXml)) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", -3);
			return -3;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		
		CpeInfoDao dao = new CpeInfoDao();
		Map<String, String> queryUserInfo = new HashMap<String, String>();
		if ("DOWNLOAD_SPEED_TEST".equals(dealXML.getProcName()) || "UPLOAD_SPEED_TEST".equals(dealXML.getProcName())) {
			logger.warn(methodName + "[" + dealXML.getOpId() + "],拉力测速，按照时间排序");
			queryUserInfo = dao.queryUserInfoNew(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		}
		else {
		 	queryUserInfo = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		}

		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}",queryUserInfo);
		
		//在线，连设备校验是否真的在线，如果在，进一步采集wifi使能相关节点
		if(null == queryUserInfo || queryUserInfo.size()==0){
			return 0;
		}
		String deviceId = StringUtil.getStringValue(queryUserInfo, "device_id");
		
		if(StringUtil.isEmpty(deviceId)){
			logger.warn(methodName+"["+dealXML.getOpId()+"],deviceId{}",deviceId);
			return 0;
		}
		Map<String,String> requestParam = new HashMap<String,String>();
		for (Para para : param) {
			requestParam.put(StringUtil.getStringValue(para.getName()), StringUtil.getStringValue(para.getValue()));
		}
		logger.warn(methodName + "执行，requestParam{} ",requestParam);
		// 获取vlanID
		// wifi-20 iptv-11 kuandai-10 yuyin-14
		// 获取通道类型
		String valueInterface = requestParam.get("Interface");
		String connectiontype = requestParam.get("ConnectionType");
		String InterfacePath = "";
		String vlanIdvalue = "";
		/*-1：终端使用 默认的通道，ITMS再转为空串；
		0：管理通道，ITMS再转为实际接口的TR069全路径；固定46
		1：上网通道，ITMS再转为实际接口的TR069全路径；
		2：IPTV通道，ITMS再转为实际接口的TR069全路径；
		3：语音通道，ITMS再转为实际接口的TR069全路径；
		//桥接
		public static final int BRIDGE = 1;
		//路由
		public static final int ROUTE = 2;
		*/
		
		if("-1".equals(valueInterface)){
			InterfacePath = "";
		}else if("0".equals(valueInterface)){
			vlanIdvalue = "46";
		}else if("1".equals(valueInterface)){
			ArrayList<HashMap<String, String>> vlanIdMAP = dao.getVlanID(StringUtil.getLongValue(StringUtil.getStringValue(queryUserInfo, "user_id")),"10");
			if(null == vlanIdMAP || vlanIdMAP.isEmpty()){
				return -3;
			}
			//wan_type,vlanid  connectiontype --0:选择使用桥接连接类型的通道   1:选择使用路由连接类型的通道
			Map<Integer,Integer> wanTypeVlanIdmap = new HashMap<Integer,Integer>();
			for (HashMap<String, String> hashMap : vlanIdMAP) {
				wanTypeVlanIdmap.put(StringUtil.getIntegerValue(StringUtil.getStringValue(hashMap, "wan_type")),StringUtil.getIntegerValue(StringUtil.getStringValue(hashMap, "vlanid")));
			}
			logger.warn(methodName+"["+dealXML.getOpId()+"],查询宽带vlanid结果{}",wanTypeVlanIdmap);

			vlanIdvalue = StringUtil.getStringValue(vlanIdMAP.get(0),"vlanid");

			if(StringUtil.isEmpty(vlanIdvalue)){
				logger.warn(methodName+"["+dealXML.getOpId()+"],查询宽带vlanid,查询连接类型{}结果为空",connectiontype);
				return -3;
			}
		}else if("2".equals(valueInterface)){
			ArrayList<HashMap<String, String>> vlanIdMAP = dao.getVlanID(StringUtil.getLongValue(StringUtil.getStringValue(queryUserInfo, "user_id")),"10");
			if(null == vlanIdMAP || vlanIdMAP.isEmpty()){
				return -3;
			}
			vlanIdvalue = StringUtil.getStringValue(vlanIdMAP.get(0),"vlanid");
		}else if("3".equals(valueInterface)){
			ArrayList<HashMap<String, String>> vlanIdMAP = dao.getVlanID(StringUtil.getLongValue(StringUtil.getStringValue(queryUserInfo, "user_id")),"10");
			if(null == vlanIdMAP || vlanIdMAP.isEmpty()){
				logger.warn(methodName+"["+dealXML.getOpId()+"],查询vlanid,查询通道类型{}结果为空",valueInterface);
				return -3;
			}
			vlanIdvalue = StringUtil.getStringValue(vlanIdMAP.get(0),"vlanid");
		}
		if(StringUtil.isEmpty(vlanIdvalue)){
			logger.warn(methodName+"["+dealXML.getOpId()+"],查询vlanid,查询通道类型{}结果为空",valueInterface);
			return -3;
		}
		
		//检查设备是否在线
		ACSCorba acsCorba = new ACSCorba();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
		if (flag == 1)
		{
			return diagnoseTest(InterfacePath,vlanIdvalue,acsCorba,requestParam,deviceId,valueInterface,dao);
		}else {
			logger.warn(methodName+"["+dealXML.getOpId()+"],{}设备不在线... ...", deviceId);
			return -1;
		}

	}
	
	/**
	 * 从策略表取得需要预读的指定PVC/VLANID
	 * 
	 * @author gongsj
	 * @date 2010-6-7
	 * @param accessType
	 * @return
	 */
	private int getdSLFromXml(String accessType)
	{
		int isDSL = 0;
		if (null == accessType || "null".equals(accessType)
				|| "".equals(accessType))
		{
			return isDSL;
		}
		if ("DSL".equals(accessType))
		{
			isDSL = 1;
		}
		else if ("Ethernet".equals(accessType))
		{
			isDSL = 2;
		}
		else if ("PON".equals(accessType) || "EPON".equals(accessType))
		{
			isDSL = 3;
		}
		else if ("GPON".equals(accessType))
		{
			isDSL = 4;
		}
		else
		{
			return isDSL;
		}
		return isDSL;
	}

	private int diagnoseTest(String interfacePath, String vlanIdvalue,
			ACSCorba acsCorba, Map<String, String> requestParam, String deviceId, String valueInterface, CpeInfoDao dao) {
			// vlanID有了，获取ijk
			PvcVlanIdTool vlanIdtool = new PvcVlanIdTool();
			vlanIdtool.setVlanid(vlanIdvalue);
			vlanIdtool.setDeviceId(deviceId);
			// 获取设备类型是gpon or epon
			String accessType = dao.getAccessType(deviceId);
			if(null == accessType || accessType.isEmpty()){
				logger.warn("[{}]开始从设备获取上行方式", deviceId);
				ConfigUtil cu = new ConfigUtil();
				accessType = cu.getAccessType(deviceId, true);
			}
			vlanIdtool.setIsDSL(getdSLFromXml(accessType));
			String serviceListAndVlanId = vlanIdtool.getServiceListAndVlanId();
			if(StringUtil.isEmpty(serviceListAndVlanId)){
				logger.warn(methodName+"["+dealXML.getOpId()+"], {}查询通道ijk值结果为空",valueInterface);
				return -4;
			}
			logger.warn("[{}]serviceListAndVlanId is {}",deviceId,serviceListAndVlanId);
			String i = "1";
			String j = serviceListAndVlanId.split("#")[0];
			String k = serviceListAndVlanId.split("#")[1];
		/*
			-1：终端使用 默认的通道，ITMS再转为空串；
			0：管理通道，ITMS再转为实际接口的TR069全路径；固定46
			1：上网通道，ITMS再转为实际接口的TR069全路径；
			2：IPTV通道，ITMS再转为实际接口的TR069全路径；
			3：语音通道，ITMS再转为实际接口的TR069全路径；
			InternetGatewayDevice.WANDevice.i.WANConnectionDevice.j.WANPPPC
		*/
			interfacePath = "InternetGatewayDevice.WANDevice."+i+".WANConnectionDevice."+j+".WANPPPConnection."+k;
			if("3".equals(valueInterface)){
				interfacePath = "InternetGatewayDevice.WANDevice."+i+".WANConnectionDevice."+j+".WANIPConnection."+k;
			}
			
			ArrayList<ParameValueOBJ> pvObjList= new ArrayList<ParameValueOBJ>();
			//procName = DOWNLOAD_SPEED_TEST
			if("DOWNLOAD_SPEED_TEST".equals(dealXML.getProcName())){
				
				ParameValueOBJ pvObj = new ParameValueOBJ();
				/*DiagnosticsState 诊断状态，发起测速须设置为“Requested”。节点路径：InternetGatewayDevice.DownloadDiagnostics.DiagnosticsState*/
				String DiagnosticsState = requestParam.get("DiagnosticsState");
				if(StringUtil.isEmpty(DiagnosticsState)){
					logger.warn(methodName+"["+dealXML.getOpId()+"],DiagnosticsState参数值为空{}",DiagnosticsState);
					return -4;
				}
				pvObj.setName("InternetGatewayDevice.DownloadDiagnostics.DiagnosticsState");
				pvObj.setType("1");
				pvObj.setValue(DiagnosticsState);
				pvObjList.add(pvObj);
				
				/*DownloadURL、下载URL：	节点路径：InternetGatewayDevice.DownloadDiagnostics.DownloadURL*/
				String DownloadURL = requestParam.get("DownloadURL");
				if(StringUtil.isEmpty(DownloadURL)){
					logger.warn(methodName+"["+dealXML.getOpId()+"],DownloadURL参数值为空[{}]",DownloadURL);
					return -4;
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.DownloadDiagnostics.DownloadURL");
				pvObj.setType("1");
				pvObj.setValue(DownloadURL);
				pvObjList.add(pvObj);
				
				/* DSCP	差分服务值：须默认为“0”。节点路径：InternetGatewayDevice.DownloadDiagnostics.DSCP*/
				String DSCP = requestParam.get("DSCP");
				if(StringUtil.isEmpty(DSCP)){
					DSCP = "0";
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.DownloadDiagnostics.DSCP");
				pvObj.setType("3");
				pvObj.setValue(DSCP);
				pvObjList.add(pvObj);
			
				/* EthernetPriority	报文设置的优先级：须默认为“0”。节点路径：	InternetGatewayDevice.DownloadDiagnostics.EthernetPriority*/
				String EthernetPriority = requestParam.get("EthernetPriority");
				if(StringUtil.isEmpty(EthernetPriority)){
					EthernetPriority = "0";
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.DownloadDiagnostics.EthernetPriority");
				pvObj.setType("3");
				pvObj.setValue(EthernetPriority);
				pvObjList.add(pvObj);
				
				/*节点路径：InternetGatewayDevice.DownloadDiagnostics.Interface*/
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.DownloadDiagnostics.Interface");
				pvObj.setType("1");
				pvObj.setValue(interfacePath);
				pvObjList.add(pvObj);
			}
			//procName = TRACEROUTETEST
			if("TRACEROUTETEST".equals(dealXML.getProcName())){
				ParameValueOBJ pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.TraceRouteDiagnostics.Interface");
				pvObj.setType("1");
				pvObj.setValue(interfacePath);
				pvObjList.add(pvObj);
				String DiagnosticsState = requestParam.get("DiagnosticsState");
				if(StringUtil.isEmpty(DiagnosticsState)){
					logger.warn(methodName+"["+dealXML.getOpId()+"],DiagnosticsState参数值为空[{}]",DiagnosticsState);
					return -4;
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.TraceRouteDiagnostics.DiagnosticsState");
				pvObj.setType("1");
				pvObj.setValue(DiagnosticsState);
				pvObjList.add(pvObj);
				
				
				//探测最大跳数，取值范围(1-255)，缺省29节点路径
				String MaxHopCount = requestParam.get("MaxHopCount");
				if(StringUtil.isEmpty(MaxHopCount)){
					MaxHopCount = "29";
				}else{
					if(StringUtil.getIntegerValue(MaxHopCount)>255 || StringUtil.getIntegerValue(MaxHopCount)<1){
						logger.warn(methodName+"["+dealXML.getOpId()+"],MaxHopCount参数值不正确{}",MaxHopCount);
						return -4;
					}
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.TraceRouteDiagnostics.MaxHopCount");
				pvObj.setType("3");
				pvObj.setValue(MaxHopCount);
				pvObjList.add(pvObj);
				//超时时间,单位：毫秒（探测等待时间，取值范围(1－65535)，缺省4999，单位毫秒）
				String Timeout = requestParam.get("Timeout");
				if(StringUtil.isEmpty(Timeout)){
					Timeout = "4999";
				}else{
					if(StringUtil.getIntegerValue(Timeout)>65535 || StringUtil.getIntegerValue(Timeout)<1){
						logger.warn(methodName+"["+dealXML.getOpId()+"],Timeout参数值不正确{}",Timeout);
						return -4;
					}
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.TraceRouteDiagnostics.Timeout");
				pvObj.setType("3");
				pvObj.setValue(Timeout);
				pvObjList.add(pvObj);
				
				String DataBlockSize = requestParam.get("DataBlockSize");
				if(StringUtil.isEmpty(DataBlockSize)){
					logger.warn(methodName+"["+dealXML.getOpId()+"],DataBlockSize参数值为空[{}]",DataBlockSize);
					return -4;
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.TraceRouteDiagnostics.DataBlockSize");
				pvObj.setType("1");
				pvObj.setValue(DataBlockSize);
				pvObjList.add(pvObj);
				
				
				String Host = requestParam.get("Host");
				if(StringUtil.isEmpty(Host)){
					logger.warn(methodName+"["+dealXML.getOpId()+"],Host参数值为空[{}]",Host);
					return -4;
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.TraceRouteDiagnostics.Host");
				pvObj.setType("1");
				pvObj.setValue(Host);
				pvObjList.add(pvObj);
				
				
				String NumberOfTries = requestParam.get("NumberOfTries");
				if(StringUtil.isEmpty(NumberOfTries)){
					logger.warn(methodName+"["+dealXML.getOpId()+"],NumberOfTries参数值为空[{}]",NumberOfTries);
					return -4;
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.TraceRouteDiagnostics.NumberOfTries");
				pvObj.setType("3");
				pvObj.setValue(NumberOfTries);
				pvObjList.add(pvObj);
				
			}
			//procName =PINGTEST
			if("PINGTEST".equals(dealXML.getProcName())){
				
				ParameValueOBJ pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.IPPingDiagnostics.Interface");
				pvObj.setType("1");
				pvObj.setValue(interfacePath);
				pvObjList.add(pvObj);
				
				String DiagnosticsState = requestParam.get("DiagnosticsState");
				if(StringUtil.isEmpty(DiagnosticsState)){
					logger.warn(methodName+"["+dealXML.getOpId()+"],DiagnosticsState参数值为空[{}]",DiagnosticsState);
					return -4;
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.IPPingDiagnostics.DiagnosticsState");
				pvObj.setType("1");
				pvObj.setValue(DiagnosticsState);
				pvObjList.add(pvObj);
				
				String DSCP = requestParam.get("DSCP");
				if(StringUtil.isEmpty(DSCP)){
					DSCP = "0";
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.IPPingDiagnostics.DSCP");
				pvObj.setType("3");
				pvObj.setValue(DSCP);
				pvObjList.add(pvObj);
				
				
				
				
				String DataBlockSize = requestParam.get("DataBlockSize");
				if(StringUtil.isEmpty(DataBlockSize)){
					logger.warn(methodName+"["+dealXML.getOpId()+"],DataBlockSize参数值为空[{}]",DataBlockSize);
					return -4;
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.IPPingDiagnostics.DataBlockSize");
				pvObj.setType("1");
				pvObj.setValue(DataBlockSize);
				pvObjList.add(pvObj);
				
				
				String Host = requestParam.get("Host");
				if(StringUtil.isEmpty(Host)){
					logger.warn(methodName+"["+dealXML.getOpId()+"],Host参数值为空[{}]",Host);
					return -4;
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.IPPingDiagnostics.Host");
				pvObj.setType("1");
				pvObj.setValue(Host);
				pvObjList.add(pvObj);
				
				
				
				String NumberOfRepetitions = requestParam.get("NumberOfRepetitions");
				if(StringUtil.isEmpty(NumberOfRepetitions)){
					logger.warn(methodName+"["+dealXML.getOpId()+"],NumberOfTries参数值为空[{}]",NumberOfRepetitions);
					return -4;
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.IPPingDiagnostics.NumberOfRepetitions");
				pvObj.setType("3");
				pvObj.setValue(NumberOfRepetitions);
				pvObjList.add(pvObj);
				
				
				
				String Timeout = requestParam.get("Timeout");
				if(StringUtil.isEmpty(Timeout)){
					logger.warn(methodName+"["+dealXML.getOpId()+"],Timeout参数值不正确{}",Timeout);
					return -4;
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.IPPingDiagnostics.Timeout");
				pvObj.setType("3");
				pvObj.setValue(Timeout);
				pvObjList.add(pvObj);
				
				
				
				
			}
			//procName =UPLOAD_SPEED_TEST
			if ("UPLOAD_SPEED_TEST".equals(dealXML.getProcName())) {
				Map<String, String> infoMap = dao.getDeviceVersion(deviceId);
				if (null == infoMap || infoMap.isEmpty())
				{
					logger.warn(methodName + "[" + dealXML.getOpId() + "], vendor_name search error, deviceId is {}", deviceId);
					return -4;
				}
				String vendorName = StringUtil.getStringValue(infoMap, "vendor_name");

				ParameValueOBJ pvObj = new ParameValueOBJ();

				// 2.DownloadURL、下载URL：
				// 节点路径：友华(包括YHTC)和华为 InternetGatewayDevice.DownloadDiagnostics.UploadURL
				// 其他的是UpLoadServerURL
				String UploadURL = requestParam.get("UploadURL");
				if (StringUtil.isEmpty(UploadURL)) {
					logger.warn(methodName + "[" + dealXML.getOpId() + "],UploadURL参数值为空{}", valueInterface);
					return -4;
				}

				// 友华(包括YHTC)和华为是UploadURL，其他的是UpLoadServerURL
				if ("HUAWEI".equals(vendorName) || "YHTC".equals(vendorName) || "YOUHUA".equals(vendorName)) {
					pvObj.setName("InternetGatewayDevice.UploadDiagnostics.UploadURL");
					pvObj.setType("1");
					pvObj.setValue(UploadURL);
					pvObjList.add(pvObj);
				}
				else {
					pvObj.setName("InternetGatewayDevice.UploadDiagnostics.UpLoadServerURL");
					pvObj.setType("1");
					pvObj.setValue(UploadURL);
					pvObjList.add(pvObj);
				}

				// 1.DiagnosticsState
				String DiagnosticsState = requestParam.get("DiagnosticsState");
				if (StringUtil.isEmpty(DiagnosticsState)) {
					logger.warn(methodName + "[" + dealXML.getOpId() + "], DiagnosticsState参数值为空{}", DiagnosticsState);
					return -4;
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.UploadDiagnostics.DiagnosticsState");
				pvObj.setType("1");
				pvObj.setValue(DiagnosticsState);
				pvObjList.add(pvObj);

				// 3.DSCP	差分服务值：须默认为“0”。
				// 节点路径：InternetGatewayDevice.UploadDiagnostics.DSCP
				String DSCP = requestParam.get("DSCP");
				if (StringUtil.isEmpty(DSCP)) {
					DSCP = "0";
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.UploadDiagnostics.DSCP");
				pvObj.setType("3");
				pvObj.setValue(DSCP);
				pvObjList.add(pvObj);

				// 4.EthernetPriority
				String EthernetPriority = requestParam.get("EthernetPriority");
				if (StringUtil.isEmpty(EthernetPriority)) {
					EthernetPriority = "0";
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.UploadDiagnostics.EthernetPriority");
				pvObj.setType("3");
				pvObj.setValue(EthernetPriority);
				pvObjList.add(pvObj);

				// 5.Interface
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.UploadDiagnostics.Interface");
				pvObj.setType("1");
				pvObj.setValue(interfacePath);
				pvObjList.add(pvObj);

				// 6.TestFileLength
				String TestFileLength = requestParam.get("TestFileLength");
				if (StringUtil.isEmpty(TestFileLength)) {
					logger.warn(methodName + "[" + dealXML.getOpId() + "], TestFileLength参数值为空{}", valueInterface);
					return -4;
				}
				pvObj = new ParameValueOBJ();
				pvObj.setName("InternetGatewayDevice.UploadDiagnostics.TestFileLength");
				pvObj.setType("3");
				pvObj.setValue(TestFileLength);
				pvObjList.add(pvObj);

			}
			
			int result = acsCorba.setValue(deviceId, pvObjList);
			if (result != 1 && result != 0) {
				logger.warn(methodName + "[" + dealXML.getOpId() + "], 终端设备[{}]不支持，无法测速！", deviceId);
				return -4;
			}
			return 1;
		
	}
	
	
	

}
