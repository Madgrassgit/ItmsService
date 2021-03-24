/**
 * 
 */
package com.linkage.itms.commom.corba;

import ACS.DevRpc;
import com.ailk.tr069.devrpc.dao.corba.AcsCorbaDAO;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.itms.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
/**
 * corba 实时接口.
 * @author liuli
 * 		   Alex.Yan (yanhj@lianchuang.com)
 * 
 *
 */
public class DevRPCManager {

	/** log */
	private static final Logger logger = LoggerFactory.getLogger(DevRPCManager.class);

	private String gwType;

	/**
	 * 
	 */
	public DevRPCManager() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public DevRPCManager(String gwType) {
		super();
		// TODO Auto-generated constructor stub
		this.gwType = gwType;
	}
	

	
	public List<DevRpcCmdOBJ> execRPC(DevRpc[] devRPCRepObj,int rpcType)
	{
		try
		{
			logger.warn("每次调后台前，休眠{}秒..." ,Global.sleepTime / 1000);
			Thread.sleep(Global.sleepTime);
		}
		catch (InterruptedException e1)
		{
			logger.error("error:",e1);
			Thread.currentThread().interrupt();
		}
		logger.warn("休眠结束，开始调用后台...");
		List<DevRpcCmdOBJ> ACSRpcOBJs = null;
		AcsCorbaDAO ascCorbaDAO = new AcsCorbaDAO(Global.getPrefixName(Global.SYSTEM_NAME)+Global.SYSTEM_ACS);
		if (null != devRPCRepObj && devRPCRepObj.length > 0)
		{
			ACSRpcOBJs = ascCorbaDAO.execRPC(Global.ClIENT_ID, rpcType,
					Global.Priority_Hig, devRPCRepObj);
		}
		return ACSRpcOBJs;
	}

}
