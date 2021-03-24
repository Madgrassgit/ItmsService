package com.linkage.itms.dispatch.sxdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.gsdx.beanObj.Para;
import com.linkage.itms.dispatch.sxdx.beanObj.CpeFlow;
import com.linkage.itms.dispatch.sxdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.sxdx.obj.GetCpeFlowXML;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * 采集终端流量节点值
 * @author zhangyu25
 *
 */
@SuppressWarnings("all")
public class GetCpeFlowService extends ServiceFather {
	private static Logger logger = LoggerFactory.getLogger(GetCpeFlowService.class);

	public GetCpeFlowService(String methodName)
	{
		super(methodName);
	}
	
    private ACSCorba corba = new ACSCorba();
    private GetCpeFlowXML dealXML = new GetCpeFlowXML(methodName);
    private CpeFlow cpeFlow = new CpeFlow();
 	private CpeInfoDao dao = new CpeInfoDao();
    
    public CpeFlow work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		
		// 校验入参
		if(null == dealXML.getXML(inXml)){
			return cpeFlow;
		}
		
		// 获取用户信息
		Map<String, String> queryUserInfo = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}",queryUserInfo);
		if(null == queryUserInfo || queryUserInfo.size()==0){
			return cpeFlow;
		}
		String deviceId = StringUtil.getStringValue(queryUserInfo, "device_id") ;
		if(StringUtil.isEmpty(deviceId)){
			return cpeFlow;
		}
		
		// 判断设备在线状态
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag){
			logger.warn(methodName+"["+dealXML.getOpId()+"],device_id={},设备不在线或正在被操作，无法获取节点值", deviceId);
			return cpeFlow;
		}
		
		// 获取参数节点数组
	    String[] parameterNames = getParamPathArr(deviceId);
	    if(parameterNames == null) {
	    	logger.warn(methodName+"["+dealXML.getOpId()+"],device_id={},获取参数节点失败", deviceId);
	    	return cpeFlow;
	    }
	    
		// 采集节点值
		ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, parameterNames);
		if (null == objLlist || objLlist.isEmpty()){
			logger.warn(methodName+"["+dealXML.getOpId()+"],device_id={},流量节点采集失败", deviceId);
			return cpeFlow;
		}
		
		for (ParameValueOBJ obj : objLlist) {
			if(obj.getName().contains("BytesSent")) {
				cpeFlow.setBytesSent(obj.getValue());
			}else if(obj.getName().contains("BytesReceived")){
				cpeFlow.setBytesReceived(obj.getValue());
			}else if(obj.getName().contains("PacketsSent")){
				cpeFlow.setPacketsSent(obj.getValue());
			}else if(obj.getName().contains("PacketsReceived")){
				cpeFlow.setPacketsReceived(obj.getValue());
			}
		}
		
		return cpeFlow;
	}


	/**
	 * 获取参数节点路径
	 * @param deviceId
	 * @return
	 */
	private String[] getParamPathArr(String deviceId) {
		
		// 上行方式为EPON或PON
	    String pathType = "X_CT-COM_EponInterfaceConfig";
		
		// 从数据库 获取上行方式
		String accessType = dao.getAccessType(deviceId);
		if(accessType == null) {
			logger.warn(methodName+"["+dealXML.getOpId()+"],device_id={},从数据库获取上行方式失败,通过采集获取上行方式", deviceId);
			// 通过采集获取 上行方式
			String accessTypePath = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
			ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, accessTypePath);
			if (null == objLlist || objLlist.isEmpty()){
				logger.warn(methodName+"["+dealXML.getOpId()+"],device_id={},采集获取上行方式失败", deviceId);
				return null;
			}
			accessType = objLlist.get(0).getValue();
		}
		
		if(accessType.equals("GPON")) {
			pathType = "X_CT-COM_GponInterfaceConfig";
		}
		
		// 光猫发送字节数 节点			
	    String bytesSent = "InternetGatewayDevice.WANDevice.1." + pathType + ".Stats.BytesSent";
	    // 光猫接收字节数 节点
	    String successCount = "InternetGatewayDevice.WANDevice.1." + pathType + ".Stats.BytesReceived";
	    // 光猫发送包 节点
	    String packetsSent = "InternetGatewayDevice.WANDevice.1." + pathType + ".Stats.PacketsSent";
	    // 光猫接收包 节点
	    String packetsReceived = "InternetGatewayDevice.WANDevice.1." + pathType + ".Stats.PacketsReceived";
	    
	    // 节点数组
	    String[] parameterNames = {
	    	bytesSent, successCount, packetsSent, packetsReceived
	    };
	    
	    return parameterNames;
	}
	

}
