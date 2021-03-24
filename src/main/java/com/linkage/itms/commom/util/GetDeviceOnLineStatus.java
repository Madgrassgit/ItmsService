package com.linkage.itms.commom.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;
import ACS.Rpc;

import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.itms.cao.ACSCorba;


public class GetDeviceOnLineStatus {
	
	private static final Logger logger = LoggerFactory
			.getLogger(GetDeviceOnLineStatus.class);
	
	/**
	 * 调用ACS检测设备在线情况（实时）
	 * @param deviceId
	 * @return
	 */
	public int testDeviceOnLineStatus(String deviceId, ACSCorba corba) {
		
		logger.debug("testDeviceOnLineStatus({})", deviceId);
		
		DevRpc[] devRPCArr = new DevRpc[1];
		int flag = 0;
		
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "";
		rpcArr[0].rpcValue = "";
		devRPCArr[0].rpcArr = rpcArr;
		
		// corba
		List<DevRpcCmdOBJ> devRPCRep = corba.exectestRPC(devRPCArr);
		
		if (devRPCRep == null || devRPCRep.size() == 0) {
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
		} else if (devRPCRep.get(0) == null) {
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
		} else {
			flag = devRPCRep.get(0).getStat();
		}
		
		return flag;
		
	}
	
}
