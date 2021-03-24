package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.CityDAO;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.QueryITVLanStateChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-9-11
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryITVLanStateService implements  IService
{
	private static final Logger logger = LoggerFactory.getLogger(QueryITVLanStateService.class);
	@Override
	public String work(String inXml)
	{
		logger.warn("QueryLanStateService：inXml({})", inXml);
		
		QueryITVLanStateChecker checker = new QueryITVLanStateChecker(inXml);
		
		if (false == checker.check()) {
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
		QueryDevDAO qdDao = new QueryDevDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		String deviceId = "";
		String userId="";
		List<HashMap<String, String>> userMap = null;
		ArrayList<String> cityIDs = CityDAO.getAllNextCityIdsByCityPid(checker.getCityId());
		if(checker.getSearchType()==1){
			if (checker.getUserInfoType() == 1)
			{
				userMap = qdDao.queryUserByNetAccount(checker.getUserName(),cityIDs);
			}
			else if (checker.getUserInfoType() == 2)
			{
				userMap = qdDao.queryUserByLoid(checker.getUserName(),cityIDs);
			}
			else if (checker.getUserInfoType() == 3)
			{
				userMap = qdDao.queryUserByIptvAccount(checker.getUserName(),cityIDs);
			}
			else if (checker.getUserInfoType() == 4)
			{
				userMap = qdDao.queryUserByVoipPhone(checker.getUserName(),cityIDs);
			}
			else if (checker.getUserInfoType() == 5)
			{
				userMap = qdDao.queryUserByVoipAccount(checker.getUserName(),cityIDs);
			}else{
			}
		}else if(checker.getSearchType()==2)
		{
			String s=checker.getDevSn();
			String devSubSN=s.substring(s.length()-6,s.length());
			userMap = qdDao.queryUserInfoByDevSNJX(devSubSN,checker.getDevSn());
		}
		
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(1001);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		if (userMap.size() > 1 && checker.getUserInfoType() != 1)
		{
			checker.setResult(1000);
			checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(1002);
			checker.setResultDesc("未绑定设备");
			return checker.getReturnXml();
		}
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		userId = StringUtil.getStringValue(userMap.get(0), "user_id", "");
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		
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
			
		    //String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
		   // List<HashMap<String, String>> iList=qdDao.queryBindPort(userId);
		  // List<String> iList = corba.getIList(deviceId, lanPath);
		    	List<String> iList=getiList(userId);
			if (null == iList || iList.isEmpty())
			{
				logger.warn("[{}]获取iList失败，返回", deviceId);
				checker.setResult(1009);
				checker.setResultDesc("节点值没有获取到，请确认节点路径是否正确");
				return checker.getReturnXml();
			}else{
				logger.warn("[{}]获取iList成功，iList.size={}", deviceId,iList.size());
			}
			List<HashMap<String,String>> lanList = new ArrayList<HashMap<String,String>>();
			for(String i : iList){
				String[] gatherPath = new String[]{
						"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Status",
						"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Stats.BytesReceived",
						"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Stats.BytesSent"};
				
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPath);
				if (null == objLlist || objLlist.isEmpty()) {
					continue;
				}
				
				String status = "";
				String received = "";
				String sent = "";
				for(ParameValueOBJ pvobj : objLlist){
					if(pvobj.getName().contains("Status")){
						status = pvobj.getValue();
					}else if(pvobj.getName().contains("BytesReceived")){
						received = pvobj.getValue();
					}else if(pvobj.getName().contains("BytesSent")){
						sent = pvobj.getValue();
					}
				}
				HashMap<String,String> tmp = new HashMap<String,String>();
				tmp.put("lan", i);
				tmp.put("status", status);
				tmp.put("BytesReceived", received);
				tmp.put("BytesSent", sent);
				lanList.add(tmp);
				tmp = null;
				status = null;
				received = null;
				sent = null;
			}
			checker.setLanList(lanList);
			
			// 记录日志
			new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
					"QueryLanStateService");
						
			return checker.getReturnXml();
			
		}
		// 设备不在线，不能获取节点值
		else {
			logger.warn("设备不在线，无法获取节点值");
			checker.setResult(1003);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		
	}
	public List<String> getiList(String userId)
	{
		QueryDevDAO qdDao = new QueryDevDAO();
		List<HashMap<String, String>> iList=qdDao.queryBindPort(userId);
		List<String> jList = new ArrayList<String>();
		for(int i=0;i<iList.size();i++)
		{
			List<String> jList1 = new ArrayList<String>();
		String[] bindport=iList.get(i).get("bind_port").split(",");
		for(int j=0;j<bindport.length;j++)
		 {
			String[] a= bindport[j].split("\\.");
			if(a[a.length-2].equals("LANEthernetInterfaceConfig"))
			{
			
				jList.add(a[a.length-1]);
			}
		 }
		
		}
			return jList;
	}

}
