package com.linkage.itms.nmg.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.nmg.dispatch.dao.EGWbindInfoDao;
import com.linkage.itms.nmg.dispatch.obj.BizbindInfoChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
*    
* 项目名称：ailk-itms-ItmsService   
* 类名称：BizbindInfoService   
* 类描述：可根据用户账号（逻辑SN、宽带账号、IPTV账号、VOIP业务电话、VOIP认证账号）和设备的序列号查询输入的用户和设备的绑定情况，以及返回设备的基本信息。   
* 创建人：guxl3   
* 创建时间：2019年3月27日 上午11:44:30   
* @version
 */
public class BizbindInfoService implements IService
{

	private static final Logger logger = LoggerFactory.getLogger(BizbindInfoService.class);
	private Map<String, String> resultMap = new HashMap<String, String>();
	ArrayList<LinkedHashMap<String, String>> list = null;
	private UserDeviceDAO userDevDao = new UserDeviceDAO();
	private String RstMsg = "成功";
	private int RstCode = 0;
	
	@Override
	public String work(String inParam)
	{

		logger.warn("BizbindInfoService==>inParam:" + inParam);
		BizbindInfoChecker checker = new BizbindInfoChecker(inParam);
		ArrayList<HashMap<String, String>> resultList = null;
		if (false == checker.check())
		{
			logger.warn("查询政企网关的基本信息接口，入参验证失败，UserInfoType=[{}],UserInfo=[{}]",
					new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
			logger.warn("BizbindInfoService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		logger.warn(
				"servicename[BizbindInfoService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						inParam });
		
		String deviceId = "";
		String userId = "";
		if (StringUtil.IsEmpty(checker.getDevSn()))
		{
			Map<String, String> userInfoMap = userDevDao.queryUserInfo(
					checker.getUserInfoType(), checker.getUserInfo());
			if (null == userInfoMap || userInfoMap.isEmpty())
			{
				logger.warn(
						"serviceName[BizbindInfoService]cmdId[{}]userinfo[{}]无此用户",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1002);
				checker.setResultDesc("无此用户信息");
				return checker.getReturnXml();
			}
			else
			{
				deviceId = StringUtil.getStringValue(userInfoMap, "device_id");
				userId = StringUtil.getStringValue(userInfoMap, "user_id");
				if (StringUtil.IsEmpty(deviceId))
				{
					logger.warn(
							"serviceName[BizbindInfoService]cmdId[{}]userinfo[{}]未绑定设备",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1003);
					checker.setResultDesc("此用户未绑定设备");
					return checker.getReturnXml();
				}
				else
				{
					EGWbindInfoDao dao = new EGWbindInfoDao();
					resultList = dao.queryDeviceBindInfoByDeV(deviceId);
				}
			}
		}
		else
		{
			// 根据终端序列号查询设备信息表
			ArrayList<HashMap<String, String>> devlist = userDevDao.queryDevInfo3(checker
					.getDevSn());
			if (null == devlist || devlist.isEmpty())
			{
				logger.warn(
						"serviceName[BizbindInfoService]cmdId[{}]DevInfo[{}]查无此设备",
						new Object[] { checker.getCmdId(), checker.getDevSn() });
				checker.setResult(1004);
				checker.setResultDesc("查无此设备");
				return checker.getReturnXml();
			}
			else if (devlist.size() > 1)
			{
				logger.warn(
						"servicename[BizbindInfoService]cmdId[{}]DevInfo[{}]查询到多台设备",
						new Object[] { checker.getCmdId(), checker.getDevSn(), inParam });
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				return checker.getReturnXml();
			}
			else
			{
				// 查看是否绑定设备
				userId = StringUtil.getStringValue(devlist.get(0), "user_id", "");
				if (StringUtil.IsEmpty(userId))
				{
					logger.warn(
							"serviceName[BizbindInfoService]cmdId[{}]DevInfo[{}]未绑定用户",
							new Object[] { checker.getCmdId(), checker.getDevSn() });
					checker.setResult(1010);
					checker.setResultDesc("未绑定用户");
					return checker.getReturnXml();
				}
				else
				{
					deviceId = StringUtil.getStringValue(devlist.get(0), "device_id");
					EGWbindInfoDao dao = new EGWbindInfoDao();
					resultList = dao.queryDeviceBindInfoByDeV(deviceId);
				}
			}
		}
		list = new ArrayList<LinkedHashMap<String, String>>();
		EGWbindInfoDao dao = new EGWbindInfoDao();
		List<HashMap<String,String>> PppoeList = dao.getPppoeAccount(userId);
		List<HashMap<String,String>> IptvList = dao.getIptvAccount(userId);
		List<HashMap<String,String>> voipList = dao.getVoipAccount(userId);
		for (HashMap<String, String> infoMap : resultList)
		{
			LinkedHashMap<String, String> stbMap = new LinkedHashMap<String, String>();
			//宽带业务帐号
			String pppoe_Account="";
			for (int i = 0; i < PppoeList.size(); i++) {
				if (i==0) {
					pppoe_Account+=(PppoeList.get(i).get("username").toString()==null?"":PppoeList.get(i).get("username").toString());
				}else {
					pppoe_Account+=","+(PppoeList.get(i).get("username").toString()==null?"":PppoeList.get(i).get("username").toString());
				}
			}
			stbMap.put("pppoe_Account", pppoe_Account);
			
			//语音账号
			String voip_Account="";
			for (int i = 0; i < voipList.size(); i++) {
				if (i==0) {
					voip_Account+=(voipList.get(i).get("voip_phone").toString()==null?"":voipList.get(i).get("voip_phone").toString());
				}else {
					voip_Account+=","+(voipList.get(i).get("voip_phone").toString()==null?"":voipList.get(i).get("voip_phone").toString());
				}
			}
			stbMap.put("voip_Account",voip_Account);
			
			//iptv账号
			String iptv_Account="";
			for (int i = 0; i < IptvList.size(); i++) {
				if (i==0) {
					iptv_Account+=(IptvList.get(i).get("username").toString()==null?"":IptvList.get(i).get("username").toString());
				}else {
					iptv_Account+=","+(IptvList.get(i).get("username").toString()==null?"":IptvList.get(i).get("username").toString());
				}
			}
			stbMap.put("iptv_Account",iptv_Account);
			
			
			stbMap.put("city_id", StringUtil.getStringValue(infoMap.get("city_id")));
			stbMap.put("device_status", StringUtil.getStringValue(infoMap.get("device_status")));
			stbMap.put("vendor", StringUtil.getStringValue(infoMap.get("vendor_name")));
			stbMap.put("DevModel", StringUtil.getStringValue(infoMap.get("device_model")));
			stbMap.put("HardwareVersion", StringUtil.getStringValue(infoMap.get("hardwareversion")));
			stbMap.put("SoftwareVersion", StringUtil.getStringValue(infoMap.get("softwareversion")));
			stbMap.put("complete_time",  new DateTimeUtil().getLongDate(StringUtil.getLongValue(infoMap.get("complete_time"))));
			stbMap.put("loid", StringUtil.getStringValue(infoMap.get("username")));
			stbMap.put("devsn", StringUtil.getStringValue(infoMap.get("device_serialnumber")));
			stbMap.put("access_type", StringUtil.getStringValue(dao.getAccessType(deviceId)));
			stbMap.put("lan_num", StringUtil.getStringValue(infoMap.get("lan_num")));
			stbMap.put("VoIP_num", StringUtil.getStringValue(infoMap.get("voice_num")));
			stbMap.put("MacAddress", StringUtil.getStringValue(infoMap.get("cpe_mac")));
			stbMap.put("WanType", StringUtil.getStringValue(infoMap.get("wan_type")));
			stbMap.put("telecom_password", StringUtil.getStringValue(infoMap.get("x_com_passwd")));
			list.add(stbMap);
		}
		RstCode = 0;
		RstMsg = "成功";
		if (list == null || list.isEmpty())
		{
			RstCode = 1004;
			RstMsg = "查无此设备";
		}
		resultMap.put("RstCode", StringUtil.getStringValue(RstCode));
		resultMap.put("RstMsg", RstMsg);
		
		String returnXML = checker.commonReturnParam(list, resultMap);
		logger.warn("BizbindInfoService==>returnXML:" + returnXML);
		return returnXML;
	
	}
}
