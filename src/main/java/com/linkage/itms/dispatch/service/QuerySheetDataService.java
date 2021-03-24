package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.QueryDeviceVersionDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QuerySheetDataChecker;


/**
 * @author zhangsm
 * @version 1.0
 * @since 2011-10-9 上午09:25:40
 * @category com.linkage.itms.dispatch.service<br>
 * @copyright 亚信联创 网管产品部
 */
public class QuerySheetDataService implements IService
{
	// 日志记录
	private static final Logger logger = LoggerFactory.getLogger(QuerySheetDataService.class);
	@Override
	public String work(String inXml)
	{
		
		Map<String,Map<String,String>> sheetDataMap = new HashMap<String, Map<String,String>>();
		QuerySheetDataChecker querySheetDataChecker = new QuerySheetDataChecker(inXml);
		if (false == querySheetDataChecker.check()) {
			logger.error(
					"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { querySheetDataChecker.getCmdId(), querySheetDataChecker.getUserInfo(),
							querySheetDataChecker.getReturnXml() });
			return querySheetDataChecker.getReturnXml();
		}
		logger.warn(
				"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { querySheetDataChecker.getCmdId(), querySheetDataChecker.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		// 查询用户设备信息
		Map<String,String> userDevInfo = userDevDao.queryUserInfo(querySheetDataChecker.getUserInfoType(), querySheetDataChecker.getUserInfo());
		String deviceId = ""; 
				
		if (null == userDevInfo || userDevInfo.isEmpty()) {
			logger.warn(
					"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { querySheetDataChecker.getCmdId(), querySheetDataChecker.getUserInfo() });
			querySheetDataChecker.setResult(1002);
			querySheetDataChecker.setResultDesc("无此客户信息");
		}
		else
		{
			deviceId = userDevInfo.get("device_id");
			if (StringUtil.IsEmpty(deviceId)) {
				// 未绑定设备
				logger.warn(
						"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]此用户没有设备关联信息",
						new Object[] { querySheetDataChecker.getCmdId(), querySheetDataChecker.getUserInfo() });
				querySheetDataChecker.setResult(1003);
				querySheetDataChecker.setResultDesc("此用户没有设备关联信息");
			}
			else
			{
				//1.查询此用户开通的业务信息
				List<HashMap<String,String>> userServList = userDevDao.queryUserServList(userDevInfo.get("user_id"));
				Map<String,String> accessType = new HashMap<String, String>();
				
				accessType.put("AccessType", userDevInfo.get("access_style_id"));
				
				if((null == userServList || userServList.isEmpty()) && !"ah_dx".equals(Global.G_instArea))
				{
					//没有开通业务
					logger.warn(
							"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]此用户没有开通任何业务",
							new Object[] { querySheetDataChecker.getCmdId(), querySheetDataChecker.getUserInfo() });
					querySheetDataChecker.setResult(1004);
					querySheetDataChecker.setResultDesc("此用户没有开通任何业务");
					return querySheetDataChecker.getReturnXml();
				}
				else
				{
					// 安徽电信需要展示设备的基本信息
					if ("ah_dx".equals(Global.G_instArea)) {
						Map<String, String> deviceInfoMap = new QueryDeviceVersionDAO()
								.getDeviceVersion(deviceId);
						
//						Map<String, String> is200MMap = new QueryDeviceVersionDAO().getIs200M(deviceId);
//						if(is200MMap==null || is200MMap.size()==0){
//							deviceInfoMap.put("is200M", "1");
//						}else{
//							deviceInfoMap.put("is200M", "0");
//						}
						
						Map<String, String> devVersionAttributeMap = new QueryDeviceVersionDAO().getDevVersionAttribute(deviceId);
						deviceInfoMap.put("is200M", StringUtil.getStringValue(devVersionAttributeMap, "is_support200", "1"));
						deviceInfoMap.put("is500M", StringUtil.getStringValue(devVersionAttributeMap, "is_support500", "0"));
						
						if (null == deviceInfoMap || deviceInfoMap.isEmpty()) {
							querySheetDataChecker.setResult(1005);
							querySheetDataChecker.setResultDesc("根据设备ID没有检索到设备，device_id="+deviceId);
							return querySheetDataChecker.getReturnXml();
						}
						deviceInfoMap.put("LOID", userDevInfo.get("username"));
						sheetDataMap.put("deviceInfoMap",deviceInfoMap);
					}
					
					//2.获取每个业务的工单配置数据
					//接入方式
					sheetDataMap.put("AccessType",accessType);
					
					
					if(null!=userServList && !userServList.isEmpty()){
						for(Map<String,String> map : userServList)
						{
							String servTypeId = map.get("serv_type_id");
							
							// 安徽电信走此逻辑    安徽电信目前只有VOIP
							if ("ah_dx".equals(Global.G_instArea)) {
								if(servTypeId.equals("14"))
								{
									//VOIP业务
									logger.warn(
											"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]获取VOIP业务工单配置数据{}",
											new Object[] { querySheetDataChecker.getCmdId(), querySheetDataChecker.getUserInfo(),map.get("username") });
									sheetDataMap.put("VOIP", userDevDao.queryServSheetData(map.get("user_id"), 
											servTypeId, map.get("username")));
									//获取业务参数
									List<HashMap<String,String>> lines = userDevDao.queryVoipParam(map.get("user_id"));
									for(Map<String,String> line : lines)
									{
										sheetDataMap.put("line"+line.get("line_id"), line);
									}
								}
								else
								{
									// 未知业务类型
									logger.warn(
											"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]未知业务类型{}",
											new Object[] { querySheetDataChecker.getCmdId(), querySheetDataChecker.getUserInfo(),servTypeId});
									querySheetDataChecker.setResult(1000);
									querySheetDataChecker.setResultDesc("未知错误");
								}
							}
							// 其他省电信走此逻辑
							else {
								if(servTypeId.equals("10"))
								{
									//上网业务
									logger.warn(
											"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]获取上网业务工单配置数据：",
											new Object[] { querySheetDataChecker.getCmdId(), querySheetDataChecker.getUserInfo(),map.get("username")});
									sheetDataMap.put("Internet", userDevDao.queryServSheetData(map.get("user_id"), 
											servTypeId, map.get("username")));
								}
								else if(servTypeId.equals("11"))
								{
									//IPTV业务
									logger.warn(
											"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]获取IPTV业务工单配置数据：",
											new Object[] { querySheetDataChecker.getCmdId(), querySheetDataChecker.getUserInfo(),map.get("username")});
									sheetDataMap.put("IPTV", userDevDao.queryServSheetData(map.get("user_id"), 
											servTypeId, map.get("username")));
								}
								else if(servTypeId.equals("14"))
								{
									//VOIP业务
									logger.warn(
											"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]获取VOIP业务工单配置数据：",
											new Object[] { querySheetDataChecker.getCmdId(), querySheetDataChecker.getUserInfo(),map.get("username")});
									sheetDataMap.put("VOIP", userDevDao.queryServSheetData(map.get("user_id"), 
											servTypeId, map.get("username")));
									//获取业务参数
									List<HashMap<String,String>> lines = userDevDao.queryVoipParam(map.get("user_id"));
									for(Map<String,String> line : lines)
									{
										sheetDataMap.put("line"+line.get("line_id"), line);
									}
								}
								else
								{
									// 未知业务类型
									logger.warn(
											"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]未知业务类型：",
											new Object[] { querySheetDataChecker.getCmdId(), querySheetDataChecker.getUserInfo(),servTypeId});
									querySheetDataChecker.setResult(1000);
									querySheetDataChecker.setResultDesc("未知错误");
								}
							}
							
							querySheetDataChecker.setResult(0);
							querySheetDataChecker.setResultDesc("成功");
							
							querySheetDataChecker.setSheetDataMap(sheetDataMap);
						}
					}
					
				}
				
			}
		}
		
		querySheetDataChecker.setSheetDataMap(sheetDataMap);
		String returnXml = querySheetDataChecker.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordDispatchLog(querySheetDataChecker, querySheetDataChecker.getUserInfo(),
				"querySheetData");
		logger.warn(
				"servicename[QuerySheetDataService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { querySheetDataChecker.getCmdId(), querySheetDataChecker.getUserInfo(),returnXml});

		// 回单
		return returnXml;
	}
//	public Map<String,Map<String,String>> getViopServData()
//	{
//		
//	}
}
