package com.linkage.itms.nmg.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.nmg.dispatch.dao.BindInfoDao;
import com.linkage.itms.nmg.dispatch.obj.BindInfoChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-6-13
 * @category com.linkage.itms.nmg.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class BindInfoService implements IService
{

	private static final Logger logger = LoggerFactory.getLogger(BindInfoService.class);
	private Map<String, String> resultMap = new HashMap<String, String>();
	ArrayList<HashMap<String, String>> list = null;
	private UserDeviceDAO userDevDao = new UserDeviceDAO();
	private String RstMsg = "成功";
	private int RstCode = 0;
	
	@Override
	public String work(String inParam)
	{

		logger.warn("BindInfoService==>inParam:" + inParam);
		BindInfoChecker checker = new BindInfoChecker(inParam);
		ArrayList<HashMap<String, String>> resultList = null;
		if (false == checker.check())
		{
			logger.warn("获取家庭网关基础属性信息接口，入参验证失败，UserInfoType=[{}],UserInfo=[{}]",
					new Object[] { checker.getUserInfoType() });
			logger.warn("BindInfoService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		String deviceId = "";
		String userId = "";
		if (StringUtil.IsEmpty(checker.getDevSn()))
		{
			Map<String, String> userInfoMap = userDevDao.queryUserInfo(
					checker.getUserInfoType(), checker.getUserInfo());
			if (null == userInfoMap || userInfoMap.isEmpty())
			{
				logger.warn(
						"serviceName[BindInfoService]cmdId[{}]userinfo[{}]无此用户",
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
							"serviceName[BindInfoService]cmdId[{}]userinfo[{}]未绑定设备",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1003);
					checker.setResultDesc("此用户未绑定设备");
					return checker.getReturnXml();
				}
				else
				{
					BindInfoDao dao = new BindInfoDao();
					resultList = dao.queryDeviceBindInfoByDeV(deviceId);
				}
			}
		}
		else
		{
			// 根据终端序列号查询设备信息表
			ArrayList<HashMap<String, String>> devlist = userDevDao.queryDevInfo2(checker
					.getDevSn());
			if (null == devlist || devlist.isEmpty())
			{
				logger.warn(
						"serviceName[BindInfoService]cmdId[{}]DevInfo[{}]查无此设备",
						new Object[] { checker.getCmdId(), checker.getDevSn() });
				checker.setResult(1004);
				checker.setResultDesc("查无此设备");
				return checker.getReturnXml();
			}
			else if (devlist.size() > 1)
			{
				logger.warn(
						"servicename[BindInfoService]cmdId[{}]DevInfo[{}]查询到多台设备",
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
							"serviceName[BindInfoService]cmdId[{}]DevInfo[{}]未绑定用户",
							new Object[] { checker.getCmdId(), checker.getDevSn() });
					checker.setResult(1010);
					checker.setResultDesc("未绑定用户");
					return checker.getReturnXml();
				}
				else
				{
					deviceId = StringUtil.getStringValue(devlist.get(0), "device_id");
					BindInfoDao dao = new BindInfoDao();
					resultList = dao.queryDeviceBindInfoByDeV(deviceId);
				}
			}
		}
		list = new ArrayList<HashMap<String, String>>();
		BindInfoDao dao = new BindInfoDao();
		int deviceType = 0;
		List<HashMap<String,String>> PppoeList = dao.getPppoeAccount(userId);
		List<HashMap<String,String>> IptvList = dao.getIptvAccount(userId);
		for (HashMap<String, String> infoMap : resultList)
		{
			if("e8-b".equals(infoMap.get("device_type"))){
				deviceType = 1;
			}else if("e8-c".equals(infoMap.get("device_type"))){
				deviceType = 2;
			}
			HashMap<String, String> stbMap = new HashMap<String, String>();
			if(PppoeList != null && !PppoeList.isEmpty())
			{
				stbMap.put("pppoe_Account", StringUtil.getStringValue(PppoeList.get(0),"username", ""));
			}
			else
			{
				stbMap.put("pppoe_Account", "");
			}
			stbMap.put("voip_Account", StringUtil.getStringValue(dao.getVoipPhone(userId)));
			if(IptvList != null && !IptvList.isEmpty())
			{
				stbMap.put("iptv_Account", StringUtil.getStringValue(IptvList.get(0),"username", ""));
			}
			else
			{
				stbMap.put("iptv_Account", "");
			}
			stbMap.put("city_id", StringUtil.getStringValue(infoMap.get("city_id")));
			stbMap.put("device_status", StringUtil.getStringValue(infoMap.get("device_status")));
			stbMap.put("bind_state", StringUtil.getStringValue(infoMap.get("cpe_allocatedstatus")));
			stbMap.put("vendor", StringUtil.getStringValue(infoMap.get("vendor_name")));
			stbMap.put("DevModel", StringUtil.getStringValue(infoMap.get("device_model")));
			stbMap.put("HardwareVersion", StringUtil.getStringValue(infoMap.get("hardwareversion")));
			stbMap.put("SoftwareVersion", StringUtil.getStringValue(infoMap.get("softwareversion")));
			stbMap.put("complete_time",  new DateTimeUtil().getLongDate(StringUtil.getLongValue(infoMap.get("complete_time"))));
			stbMap.put("loid", StringUtil.getStringValue(infoMap.get("username")));
			stbMap.put("devsn", StringUtil.getStringValue(infoMap.get("device_serialnumber")));
			stbMap.put("access_type", StringUtil.getStringValue(dao.getAccessType(deviceId)));
			stbMap.put("lan_num", StringUtil.getStringValue(infoMap.get("lan_num")));
			stbMap.put("device_type", String.valueOf(deviceType));
			stbMap.put("MacAddress", StringUtil.getStringValue(infoMap.get("cpe_mac")));
			stbMap.put("WanType", StringUtil.getStringValue(infoMap.get("wan_type")));
			stbMap.put("Password", StringUtil.getStringValue(infoMap.get("passwd")));
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
		if(resultList != null && !resultList.isEmpty())
		{
			resultMap.put("DevsnMatchStatus", StringUtil.getStringValue(dao.getComepareStatus(resultList.get(0).get("username"))));
		}
		else
		{
			resultMap.put("DevsnMatchStatus","1");
		}
		resultMap.put("VoipOpenStatus", StringUtil.getStringValue(dao.getVoipOpenStatus(userId)));
		if(IptvList != null && !IptvList.isEmpty())
		{
			resultMap.put("IptvOpenStatus", StringUtil.getStringValue(IptvList.get(0),"open_status", ""));
		}
		else
		{
			resultMap.put("IptvOpenStatus","");
		}
		if(PppoeList != null && !PppoeList.isEmpty())
		{
			resultMap.put("PppoeOpenStatus", StringUtil.getStringValue(PppoeList.get(0),"open_status", ""));
		}
		else
		{
			resultMap.put("PppoeOpenStatus","");
		}
		String returnXML = checker.commonReturnParam(list, resultMap);
		logger.warn("BindInfoService==>returnXML:" + returnXML);
		return returnXML;
	
	}
}
