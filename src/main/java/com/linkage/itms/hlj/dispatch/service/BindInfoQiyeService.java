package com.linkage.itms.hlj.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.hlj.dispatch.dao.BindInfoDAO;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.obj.BindInfoQiyeChecker;

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-25
 * @category com.linkage.itms.hlj.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class BindInfoQiyeService implements HljIService
{
	private static final Logger logger = LoggerFactory.getLogger(BindInfoQiyeService.class);

	@Override
	public String work(String jsonString) 
	{
		logger.warn("BindInfoQiyeService==>inParam:" + jsonString);
		BindInfoQiyeChecker checker = new BindInfoQiyeChecker(jsonString);
		if (false == checker.check())
		{
			logger.warn("企业网关基本信息查询接口，入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("BindInfoQiyeService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		QueryDeviceIdDAO qdDao = new QueryDeviceIdDAO();
		List<HashMap<String, String>> userMap = null;
		if (checker.getQueryType() == 0)
		{
			userMap = qdDao.queryUserByNetAccountQiye(checker.getQueryNum());
		}
		else if (checker.getQueryType() == 1)
		{
			userMap = qdDao.queryUserByLoidQiye(checker.getQueryNum());
		}
		else if (checker.getQueryType() == 2)
		{
			userMap = qdDao.queryUserByDevSNQiye(checker.getQueryNum());
		} else{
		}  
		
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(8);
			checker.setResultDesc("ITMS未知异常-查询结果为空");
			return checker.getReturnXml();
		}
//		if (userMap.size() > 1)
//		{
//			checker.setResult(1001);
//			checker.setResultDesc("数据不唯一，请使用devSn查询");
//			return checker.getReturnXml();
//		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(3);
			checker.setResultDesc("无设备信息");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("user_id")))
		{
			checker.setResult(9);
			checker.setResultDesc("无用户信息");
			return checker.getReturnXml();
		}
		BindInfoDAO dao = new BindInfoDAO();
		String deviceId = userMap.get(0).get("device_id");
		String userId = userMap.get(0).get("user_id");
		Map<String, String> infoMap = dao.queryDeviceBindInfoByDeVQiye(deviceId);
		if(infoMap==null || infoMap.isEmpty()){
			checker.setResult(1);
			checker.setResultDesc("数组为空");
			return checker.getReturnXml();
		}else{
			JSONObject jo = new JSONObject();
			JSONObject queryRspInfo = new JSONObject();
			try
			{
				jo.put("resultCode", 0);
				jo.put("streamingNum", checker.getStreamingNum());
				jo.put("queryRspInfo", queryRspInfo);
				queryRspInfo.put("access_Account", "");//置空
				queryRspInfo.put("pppoe_Account", dao.getPppoeAccountQiye(userId));
				
				ArrayList<HashMap<String, String>> voipList = dao.getVoipAccountQiye(userId);
				queryRspInfo.put("voip_Account", getAccount(voipList));
				queryRspInfo.put("areaCode", infoMap.get("city_id"));
				queryRspInfo.put("MACAddress", infoMap.get("cpe_mac"));
				queryRspInfo.put("cpeStatus", infoMap.get("cpe_allocatedstatus"));
				queryRspInfo.put("cpeManufacturer", infoMap.get("vendor_name"));
				queryRspInfo.put("CpeType", infoMap.get("device_model"));
				queryRspInfo.put("cpeVersion", infoMap.get("hardwareversion"));
				queryRspInfo.put("cpeSoftVer", infoMap.get("softwareversion"));
				queryRspInfo.put("cpeTerType", "");//默认给空
				queryRspInfo.put("enterTime", new DateTimeUtil().getLongDate(StringUtil.getLongValue(infoMap.get("complete_time"))));
				queryRspInfo.put("deviceSN", infoMap.get("device_serialnumber"));
				queryRspInfo.put("ManufatureOUI", infoMap.get("oui"));
				queryRspInfo.put("LOID", infoMap.get("username"));
				queryRspInfo.put("wanType", infoMap.get("access_style_relay_id"));
				queryRspInfo.put("etherNum", infoMap.get("lan_num"));
				if("0".equals(infoMap.get("wlan_num")))
				{
					queryRspInfo.put("wirelessType",0 );//取值0的话给0 ， 其他给1
				}else{
					queryRspInfo.put("wirelessType", 1);
				}
				
				if("e8-b".equals(infoMap.get("device_type")))
				{
					queryRspInfo.put("cpeClass",1 );//取值e8-b的话给1 , e8-c/2,  Navigator1-1/4,  A8-C和其他/3
				}else if("e8-c".equals(infoMap.get("device_type"))){
					queryRspInfo.put("cpeClass", 2);
				}else if ("Navigator1-1".equals(infoMap.get("device_type"))) {
					queryRspInfo.put("cpeClass", 4);
				}
				else if ("A8-C".equals(infoMap.get("device_type"))){
					queryRspInfo.put("cpeClass", 3);
				}
				else {
					queryRspInfo.put("cpeClass", 3);
				}
				queryRspInfo.put("CpeFailReason", "");//置空
				queryRspInfo.put("CpeState", 1);//默认给1
				
				ArrayList<HashMap<String, String>> activeList = dao.getCpeActiveQiye(userId);
				if (activeList == null || activeList.isEmpty()){
					queryRspInfo.put("CpeActiveFailReason", "");
					queryRspInfo.put("CpeActiveState", "");
				}else{
					queryRspInfo.put("CpeActiveFailReason", activeList.get(0).get("fault_reason"));
					queryRspInfo.put("CpeActiveState", activeList.get(0).get("open_status"));
				}
				
				queryRspInfo.put("userName", "");//默认空
				if("1".equals(infoMap.get("is_check")))
				{
					queryRspInfo.put("devTypeStatus","已审核" );//is_check   1：给已审核  其他 给 未审核
				}else{
					queryRspInfo.put("devTypeStatus", "未审核");
				}
				queryRspInfo.put("isNewVersion", 0);//默认0
				if(!"1".equals(infoMap.get("online_status")))
				{
					queryRspInfo.put("onlineStatus","离线");
				}else{
					// 校验设备是否在线
					GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
					ACSCorba acsCorba = new ACSCorba();
					
					int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
					if (flag == -3 || flag == 1){
						queryRspInfo.put("onlineStatus", "在线");
					}else{
						queryRspInfo.put("onlineStatus", "离线");
					}
				}
				String device_model_id = infoMap.get("device_model_id");
				String vendor_id = infoMap.get("vendor_id");
				//Map<String, String> rateMap = dao.queryRateDeV(device_model_id,vendor_id);
				String rate = "1000";
				/*if(rateMap==null || rateMap.isEmpty()){
					rate = "100";
				}*/
				queryRspInfo.put("rate", rate);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			return jo.toString();
		}
		
		
	}
	
	/**
	 * 多宽带， 多语音处理
	 * 
	 * @param list
	 * @return
	 */
	private String getAccount(ArrayList<HashMap<String, String>> list)
	{
		String account = "";
		if (list != null && !list.isEmpty())
		{
			for (HashMap<String, String> map : list)
			{
				account = account + StringUtil.getStringValue(map, "username", "") + ";";
			}
			return account.substring(0, account.length() - 1);
		}
		else
		{
			return "";
		}
	}
}
