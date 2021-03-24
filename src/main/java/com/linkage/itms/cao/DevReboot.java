
package com.linkage.itms.cao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;
import ACS.Rpc;

import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.litms.acs.soap.service.Reboot;

/**
 * @author Jason(3412)
 * @date 2009-7-1
 */
public class DevReboot
{

	private static final Logger logger = LoggerFactory.getLogger(DevReboot.class);

	/**
	 * 重启操作
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-7-1
	 * @return
	 */
	public static int reboot(String deviceId)
	{
		logger.debug("device reboot. deviceId:" + deviceId);
		DevRpc[] devRPCArr = new DevRpc[1];
		Reboot reboot = new Reboot();
		reboot.setCommandKey("Reboot");
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "Reboot";
		rpcArr[0].rpcValue = reboot.toRPC();
		devRPCArr[0].rpcArr = rpcArr;
		List<DevRpcCmdOBJ> devRPCRep = new ACSCorba().execRPC(deviceId, devRPCArr);
		int flag = -9;
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
