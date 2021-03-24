package sendCorbaHeartBeat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

import ACS.DevRpc;
import ACS.RPCManager;
import ACS.Rpc;
import SuperGather.GatherParam;

import com.ailk.tr069.acsalive.dao.AcsCorbaInitDAO;
import com.ailk.tr069.acsalive.obj.conf.AcsAliveConfMapOBJ;
import com.ailk.tr069.acsalive.obj.mq.Acs;
import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;


public class RunSendCorbaHeartBeatTask extends TimerTask
{

	public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Override
	public void run()
	{
		try
		{
		      System.out.println("[ResourceBind]sendCorbaHeartBeat调用corba，当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
		      String result = Global.G_BlManager_ITMS.test("ItmsService", 0, "servUser", "add", "tesCorbatHeartBeat");
		      System.out.println("[ResourceBind]sendCorbaHeartBeat返回值：" + result + ",当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
		      
			  System.out.println("[PreProcess]sendCorbaHeartBeat调用corba，当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
			  Global.G_PPManager.processOOBatch(new String[]{"1234567890"});
			  System.out.println("[PreProcess]sendCorbaHeartBeat调用corba完成，当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
			  
			  System.out.println("[SuperGather]sendCorbaHeartBeat调用corba，当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
			  GatherParam gatherParam = new GatherParam();
			  gatherParam.deviceIdArr = new String[]{};
			  gatherParam.invokeType = 1;
			  gatherParam.paramType = 1;
			  Global.G_SuperGatherManager.gatherCpeParams(gatherParam);
//			  SuperGatherCorba sgCorba = new SuperGatherCorba();
//			  sgCorba.gatherCpeParams("12345", 3);
			  System.out.println("[SuperGather]sendCorbaHeartBeat调用corba完成,当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
			  
			  System.out.println("[ACS]sendCorbaHeartBeat调用corba，当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
			  sendHeartBeatToACS();
			  System.out.println("[ACS]sendCorbaHeartBeat调用corba完成，当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("sendCorbaHeartBeat异常,当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
		}

		
	}
	
	public static void sendHeartBeatToACSBak() {
		System.out.println("sendHeartBeatToACS()");
		PrepareSQL psql = new PrepareSQL();
		psql.append("select * from tab_ior where object_name like '");
		psql.append(Global.getPrefixName(Global.SYSTEM_NAME,Global.GW_TYPE_ITMS));
		psql.append(Global.SYSTEM_ACS);
		psql.append("_%' and object_name != 'ACS_0'");
	
		List<HashMap<String,String>> iorList = DBOperation.getRecords(psql.getSQL());
		
		if (iorList == null || iorList.size() == 0) {
			System.out.println("acs ior is null");
		}else{
			RPCManager rpc = null;
			String ior = null;
			String objectName = "";
			String clientId = "ItmsService";
			DevRpc[] devRPCArr = new DevRpc[1];
			devRPCArr[0] = new DevRpc();
			devRPCArr[0].devId = "123456789";
			Rpc[] rpcArr = new Rpc[1];
			rpcArr[0] = new Rpc();
			rpcArr[0].rpcId = "1";
			rpcArr[0].rpcName = "";
			rpcArr[0].rpcValue = "";
			devRPCArr[0].rpcArr = rpcArr;
			for(HashMap<String,String> map : iorList){
				ior = StringUtil.getStringValue(map, "ior");
				objectName = StringUtil.getStringValue(map, "object_name");
				rpc = AcsCorbaInitDAO.getRPCManager(ior);
				String callId = null;
				try {
					System.out.println("[" + objectName + "]sendCorbaHeartBeat调用corba，当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
					callId = rpc.test(clientId);
					System.out.println("[" + objectName + "]sendCorbaHeartBeat调用corba完成，结果：" + callId + ",当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
				} catch (Exception e) {
					System.out.println("CORBA ACS Error:{" + e.getMessage() + "},Rebind.");
					rpc = AcsCorbaInitDAO.getRPCManager(ior);
					try {
						callId = rpc.ExecRPC("", 0, 1,devRPCArr);
					} catch (Exception e1) {
						System.out.println("CORBA ACS Error:{ " + clientId + " },Rebind.");
					}
				}
			}
		}
	}
	
	public static void sendHeartBeatToACS() {
		System.out.println("sendHeartBeatToACS()");
		String systemKey = Global.getPrefixName(Global.SYSTEM_NAME)+Global.SYSTEM_ACS;
		
		List<String> acsList = AcsAliveConfMapOBJ.getInstance().getObjByKey(systemKey).getAcsGatherIdList();
		System.out.println("acsList=" + acsList.size());
		if(acsList != null && acsList.size() > 0){
			RPCManager rpc = null;
			for(String index : acsList){
				System.out.println("index=" + index);
				Acs acs = AcsAliveConfMapOBJ.getInstance().getObjByKey(systemKey).getAcsMap()
						.get(index);
				String ior = acs.getCorba();
				rpc = acs.getRpcManager();
				if (rpc == null)
				{
					System.out.println("rpc=null --" + index);
					if (ior == null || StringUtil.IsEmpty(ior))
					{
						ior = AcsCorbaInitDAO.getIorByGatherIdAndType(index, systemKey);
					}
					if (ior == null || StringUtil.IsEmpty(ior))
					{
						System.out.println("gatherId " + index + " no ACS");
					}
					else
					{
						acs.setCorba(ior);
						rpc = AcsCorbaInitDAO.getRPCManager(ior);
						acs.setRpcManager(rpc);
						AcsAliveConfMapOBJ.getInstance().getObjByKey(systemKey).getAcsMap().put(index, acs);
					}
				}
				String callId = null;
				String objectName = "ACS_" + index;
				String clientId = "ItmsService";
				DevRpc[] devRPCArr = new DevRpc[1];
				devRPCArr = getDevPrcArr();
				try {
					System.out.println("[" + objectName + "]sendCorbaHeartBeat调用corba，当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
					callId = rpc.ExecRPC(clientId, 0, 1,devRPCArr);
					System.out.println("[" + objectName + "]sendCorbaHeartBeat调用corba完成，结果：" + callId + ",当前时间为：" + df.format(new Date()) + "(" + System.currentTimeMillis() + ")");
				} catch (Exception e) {
					System.out.println("CORBA ACS Error:{" + e.getMessage() + "},Rebind.");
					rpc = AcsCorbaInitDAO.getRPCManager(ior);
					try {
						callId = rpc.ExecRPC("", 0, 1,devRPCArr);
					} catch (Exception e1) {
						System.out.println("CORBA ACS Error:{ " + clientId + " },Rebind.");
					}
				}
			}
		}
	}
	
	public static DevRpc[] getDevPrcArr(){
		DevRpc[] devRPCArr = new DevRpc[1];
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = "123456789";
		Rpc[] rpcArr = new Rpc[1];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "";
		rpcArr[0].rpcValue = "";
		devRPCArr[0].rpcArr = rpcArr;
		return devRPCArr;
	}
}
