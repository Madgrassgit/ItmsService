
package com.linkage.itms.cao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;
import ACS.Rpc;
import PreProcess.UserInfo;

import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.PreProcessInterface;
import com.linkage.litms.acs.soap.service.FactoryReset;
/**
 * 恢复出厂
 * @author zhangsm
 * @version 1.0
 * @since 2012-3-20 下午02:29:10
 * @category com.linkage.itms.cao<br>
 * @copyright 亚信联创 网管产品部
 */
public class DevReset
{

	private static final Logger logger = LoggerFactory.getLogger(DevReset.class);

	/**
	 * 恢复出厂操作
	 * 
	 * @param
	 * @author zhangsm
	 * @date 2011-3-20
	 * @return
	 */
	public static int reset(String deviceId)
	{
		logger.debug("device FactoryReset. deviceId:" + deviceId);
		DevRpc[] devRPCArr = new DevRpc[1];
		FactoryReset factoryReset = new FactoryReset();
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "FactoryReset";
		rpcArr[0].rpcValue = factoryReset.toRPC();
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
	/**
	 * 恢复出厂操作(湖北)
	 * 
	 * @param
	 * @author zhangsb
	 * @date 2014年5月19日 
	 * @return
	 */
	public static int reset4HB(Map  userInfoMap)
	{
		logger.warn("device FactoryReset. deviceId:" + StringUtil.getStringValue(userInfoMap, "device_id"));
		PreProcessInterface ppc =  CreateObjectFactory.createPreProcess();
		UserInfo[] userInfo = new UserInfo[1];
		userInfo[0] = new UserInfo();
		userInfo[0].deviceId = StringUtil.getStringValue(userInfoMap, "device_id");;
		userInfo[0].oui = "";
		userInfo[0].deviceSn = StringUtil.getStringValue(userInfoMap,
				"device_serialnumber");
		userInfo[0].gatherId = "factory_reset";
		userInfo[0].userId = StringUtil.getStringValue(userInfoMap, "user_id");
		userInfo[0].servTypeId = "0";
		userInfo[0].operTypeId = "1";
		int ret = ppc.processServiceInterface(userInfo);
		return ret;
	}
}
