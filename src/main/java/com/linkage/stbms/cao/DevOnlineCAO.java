package com.linkage.stbms.cao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;
import ACS.Rpc;

import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;

/**
 * 
 * @author zhangsm(67310) Tel:??
 * @version 1.0
 * @since 2012-5-18 下午04:35:59
 * @category com.linkage.itms.cao
 * @copyright 南京联创科技 网管科技部
 *
 */
public class DevOnlineCAO
{
	private static final Logger logger = LoggerFactory.getLogger(DevOnlineCAO.class);
	/**
	 * 在线状态检查
	 * @param deviceId
	 * @return
	 */
	public static int devOnlineTest(String deviceId)
	{
		DevRpc[] devRPCArr = new DevRpc[1];
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "";
		rpcArr[0].rpcValue = "";
		devRPCArr[0].rpcArr = rpcArr;
		// corba
		List<DevRpcCmdOBJ> devRPCRep = new ACSCorba().exectestRPC(devRPCArr);
		int flag = 0;
		if (devRPCRep == null || devRPCRep.size() == 0)
		{
			logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
		}
		else if (devRPCRep.get(0) == null)
		{
			logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
		}
		else
		{
			flag = devRPCRep.get(0).getStat();
		}
		return flag;
	}
}
