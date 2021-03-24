package com.linkage.stbms.cao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;
import ACS.Rpc;

import com.ailk.tr069.devrpc.dao.corba.AcsCorbaDAO;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.litms.acs.soap.service.FactoryReset;
import com.linkage.stbms.itv.main.Global;
import com.linkage.stbms.itv.main.StbServGlobals;


/**
 * @author Jason(3412)
 * @date 2009-7-1
 */
public class DevReset {

	private static final Logger logger = LoggerFactory.getLogger(DevReset.class);
	
	/**
	 * 恢复出厂设置
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-7-1
	 * @return
	 *            <li>0,1:成功</li>
	 *            <li>-7:系统参数错误</li>
	 *            <li>-6:设备正被操作</li>
	 *            <li>-1:设备连接失败</li>
	 *            <li>-9:系统内部错误</li>
	 */
	public static int reset(String deviceId) {
		int flag = -9;
		logger.info("device reset. deviceId:" + deviceId);
		FactoryReset factoryReset = new FactoryReset();
		
		DevRpc[] devRPCArr = new DevRpc[1];
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc rpc = new Rpc();
		rpc.rpcId = "1";
		rpc.rpcName = "FactoryReset";
		rpc.rpcValue = factoryReset.toRPC();
		devRPCArr[0].rpcArr = new Rpc[] { rpc };
		
//		List<DevRpcCmdOBJ> list = new AcsCorbaDAO(Global.ACS_OBJECT_NAME).execRPC(
//				StbServGlobals.getLipossProperty("mq.clientId"), Global.rpcType,
//				Global.priority, devRPCArr);
		List<DevRpcCmdOBJ> list = new AcsCorbaDAO(Global.ACS_OBJECT_NAME).execRPC(
				Global.CLIENT_ID, Global.rpcType,
				Global.priority, devRPCArr);
		logger.info("device reset. deviceId:" + deviceId+"|aaaaaa");
		if (null == list || list.isEmpty())
		{
			logger.info("device reset. deviceId:" + deviceId+"|bbbbbb");
			flag = -9;
		    return flag;
		}
		logger.info("device reset. deviceId:" + deviceId+"|{}",flag);
		flag = list.get(0).getStat();
		logger.info("device reset. deviceId:" + deviceId+"|{}",flag);
		return flag;
		
	}
}
