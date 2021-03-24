package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.DevOnlineCAO;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.dao.DeviceConfigDAO;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.OneKeyDoneChecker;


public class OneKeyDoneServiceForAH implements IService {
	
	private static final Logger logger = LoggerFactory
			.getLogger(OneKeyDoneServiceForAH.class);
	
	
	public String work(String inXml){
		
		logger.warn("oneKeyDone：inXml({})", new Object[] { inXml });
		
		OneKeyDoneChecker checker = new OneKeyDoneChecker(inXml);
		
		if (false == checker.check()) {
			logger.warn("入参验证未通过，返回：{}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		boolean isOnLine = false;
		String devId = null;
		String oui = null;
		String devSn = null;
		long userId = 0L;
		
		// 根据用户信息查询
		if (1 == checker.getSearchType()) {
			Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker
					.getUserInfoType(), checker.getUserInfo());
			
			if (null == userInfoMap || userInfoMap.isEmpty()) {
				logger.warn("无此用户：{}", checker.getUserInfo());
				checker.setResult(1003);
				checker.setResultDesc("查无此客户");
				logger.warn("oneKeyDone：return:{}", new Object[] { checker.getReturnXml() });
				return checker.getReturnXml();
			}
			// 客户信息存在，然后判断该用户是否绑定了设备
			else 
			{
				devId = StringUtil.getStringValue(userInfoMap, "device_id");
				userId = StringUtil.getLongValue(userInfoMap, "user_id");
				
				// 客户未绑定设备
				if (StringUtil.IsEmpty(devId)) {
					logger.warn("此用户：{}，未绑定设备：{}", new Object[] { checker.getUserInfo(),	devId });
					checker.setResult(1004);
					checker.setResultDesc("该用户未绑定设备");
					logger.warn("oneKeyDone：return:{}", new Object[] { checker.getReturnXml() });
					return checker.getReturnXml();
				}
				// 客户已绑定设备，然后判断设备是否实时在线
				else 
				{
					oui = StringUtil.getStringValue(userInfoMap, "oui");
					devSn = StringUtil.getStringValue(userInfoMap, "device_serialnumber");
					
					// 获取设备实时在线状态
					int iOnline = DevOnlineCAO.devOnlineTest(devId) == 1? 1 : -1;
					// 设备不在线
					if (-1 == iOnline) {
						logger.warn("该设备：({}), 不在线", devId);
						checker.setResult(1011);
						checker.setResultDesc("该设备不在线，不能正常交互");
						logger.warn("oneKeyDone：return:{}", new Object[] { checker.getReturnXml() });
						return checker.getReturnXml();
					}
					
					/** 设备实时在线，然后判断该用户是否已受理业务，如果已受理业务，则判断是否已下发成功，
					 *  如果没有下发成功，则调用配置模块，自动调用业务下发
					 */
					else 
					{
						isOnLine = true;
					}
				}
			}
		}
		// 根据设备序列号查询
		else {
			// 根据终端序列号
			ArrayList<HashMap<String, String>> devInfoMapList = userDevDao
					.getTelePasswdByDevSn(checker.getDevSn());
			
			if (null == devInfoMapList || devInfoMapList.isEmpty()) {
				logger.warn("无此设备：" + checker.getDevSn());
				checker.setResult(1005);
				checker.setResultDesc("查无此设备");
				logger.warn("oneKeyDone：return:{}", new Object[] { checker.getReturnXml() });
				return checker.getReturnXml();
			} else if (devInfoMapList.size() > 1) {
				logger.warn("查询到多台设备：" + checker.getDevSn());
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				logger.warn("oneKeyDone：return:{}", new Object[] { checker.getReturnXml() });
				return checker.getReturnXml();
			} else {
				Map<String, String> devMap = devInfoMapList.get(0);
				if (StringUtil.IsEmpty(devMap.get("username"))) {
					logger.warn(".ITMS未绑devSn({})", checker.getDevSn());
					checker.setResult(1004);
					checker.setResultDesc("设备未绑定");
					logger.warn("oneKeyDone：return:{}", new Object[] { checker.getReturnXml() });
					return checker.getReturnXml();
				} else {
					devId = devMap.get("device_id");
					userId = StringUtil.getLongValue(devMap.get("user_id"));
					oui = devMap.get("oui");
					devSn = devMap.get("device_serialnumber");
					
					// 获取设备实时在线状态
					int iOnline = DevOnlineCAO.devOnlineTest(devId) == 1? 1 : -1;
					// 设备不在线
					if (-1 == iOnline) {
						logger.warn("该设备：({}), 不在线", devId);
						checker.setResult(1011);
						checker.setResultDesc("该设备不在线，不能正常交互");
						logger.warn("oneKeyDone：return:{}", new Object[] { checker.getReturnXml() });
						return checker.getReturnXml();
					}
					/** 设备实时在线，然后判断该用户是否已受理业务，如果已受理业务，则判断是否已下发成功，
					 *  如果没有下发成功，则调用配置模块，自动调用业务下发
					 */
					else 
					{
						isOnLine = true;
					}
				}
			}
		}
		
		/**
		 * 如果设备在线，则判断业务是否已下发成功，如果没下发成功，返回提示信息：“用户没有下发业务，已经自动调用业务下发”，并自动调用业务下发
		 * 如果下发成功，则实时采集设备上的语音信息，然后与下发的工单信息进行比对
		 * 如果比对不一致，则返回提示信息：设备内语音数据与语音工单数据不一致
		 * 一致，则返回提示信息：设备业务正常下发
		 */
		if (isOnLine) {
			// 判断该用户是否开通了相关业务
			ServUserDAO servUserDao = new ServUserDAO();
			
			ArrayList<HashMap<String, String>> servUserMapList = servUserDao
					.queryHgwcustServUserByDevId(userId);
			// 该用户没有受理任何业务
			
			if (null == servUserMapList || servUserMapList.isEmpty()) {
				logger.warn("该用户没有受理任何业务，请发送业务工单");
				checker.setResult(1009);
				checker.setResultDesc("用户未受理任何业务，请发送业务工单");
				logger.warn("oneKeyDone：return:{}", new Object[] { checker.getReturnXml() });
				return checker.getReturnXml();
			}
			// 该用户受理了业务，判断业务是否完成业务下发
			else 
			{
				HashMap<String, String> servUserMap = servUserMapList.get(0);
				/**
				 * 目前安徽电信只有语音业务 serv_type_id = 14
				 * 语音业务下发成功，然后调用采集设备语音业务信息与工单中的语音信息进行比对
				 * 
				 * open_status = 1 表示业务下发成功
				 * 
				 */
				if ("1".equals(StringUtil.getStringValue(servUserMap, "open_status"))) {
					// 调用采集  采集设备的语音信息
					int rsint = new SuperGatherCorba().getCpeParams(devId, 0, 3);
					if (1 != rsint) {
						logger.warn("getData sg fail，采集失败");
						checker.setResult(1015);
						checker.setResultDesc("设备采集失败");
						logger.warn("oneKeyDone：return:{}", new Object[] { checker.getReturnXml() });
						return checker.getReturnXml();
					}
					else 
					{
						boolean flag = checkVOIPParam(devId, userId,
								StringUtil.getStringValue(servUserMap, "username"), userDevDao);
						if (false == flag) {
							logger.warn("一键预处理：设备内语音数据与语音工单数据不一致");
							checker.setResult(1014);
							checker.setResultDesc("设备内语音数据与语音工单数据不一致");
							logger.warn("oneKeyDone：return:{}", new Object[] { checker.getReturnXml() });
							return checker.getReturnXml();
						}else {
							logger.warn("一键预处理：设备业务正常下发");
							checker.setResult(1013);
							checker.setResultDesc("设备业务正常下发");
							logger.warn("oneKeyDone：return:{}", new Object[] { checker.getReturnXml() });
							return checker.getReturnXml();
						}
					}
				}
				// 语音业务没有下发成功，则自动调用业务下发
				else 
				{
					// 更新业务用户表的业务开通状态
					servUserDao.updateServOpenStatus(userId, checker.getServiceType());

					// 预读调用对象
					PreServInfoOBJ preInfoObj = new PreServInfoOBJ(
							StringUtil.getStringValue(userId),	devId, oui, devSn, StringUtil
									.getStringValue(checker.getServiceType()), "1");
					
					CreateObjectFactory.createPreProcess()
							.processServiceInterface(CreateObjectFactory.createPreProcess()
									.GetPPBindUserList(preInfoObj));
					
					 checker.setResult(1012);
					 checker.setResultDesc("用户没有下发业务，已经自动调用业务下发");
					 logger.warn("oneKeyDone：return:{}", new Object[] { checker.getReturnXml() });
					 return checker.getReturnXml();
				}
			}
		}
		
		String returnXml = checker.getReturnXml();
		
		logger.warn("oneKeyDone：return:{}", new Object[] { returnXml });
		
		return returnXml;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * 设备上采集到的语音相关信息 与 下发的工单信息 进行比对
	 * 
	 * @param devId
	 * @param userId
	 * @param userName
	 * @param userDevDao
	 * @return
	 */
	private boolean checkVOIPParam(String devId, long userId, String userName, UserDeviceDAO userDevDao){
		
		boolean flag = true;
		Map<String, String> cjVoipPVCMap = null;      // 采集相关信息
		Map<String, String> cjVoiceServiceMap = null; // 采集相关信息
		Map<String, String> linesMap = null;          // 工单相关信息
		String cjWanType = null;
		
		// 采集到的语音相关信息
		// 查询vpi_id， vci_id， vlan_id， conn_type， bind_port
		List<HashMap<String, String>> cjVoipPVClist = new DeviceConfigDAO().getPVCVOIP(devId);
		// 查询 主ProxyServer，主ProxyServerPort，备ProxyServer，备ProxyServerPort等字段
		List<HashMap<String, String>> cjVoiceServiceList = new DeviceConfigDAO().getVoiceService(devId);
//		List<HashMap<String, String>> linesList = new DeviceConfigDAO().getVoipLines(devId);
		
		if (null != cjVoipPVClist && cjVoipPVClist.size() > 0) {
			cjVoipPVCMap = cjVoipPVClist.get(0);
		}else {
			logger.warn("一键预处理没采集到相关语音信息");
			return false;
		}
		if (null != cjVoiceServiceList && cjVoiceServiceList.size() > 0) {
			cjVoiceServiceMap = cjVoiceServiceList.get(0);
		}else {
			logger.warn("一键预处理没采集到相关语音信息");
			return false;
		}
		int sessionType = StringUtil.getIntValue(cjVoipPVCMap, "sess_type");
		//桥接
		if(sessionType==1&&"PPPoE_Bridged".equals(StringUtil.getStringValue(cjVoipPVCMap, "conn_type",""))){
			cjWanType = "1";
		}
		//路由
		else if (sessionType==1&&"IP_Routed".equals(StringUtil.getStringValue(cjVoipPVCMap, "conn_type",""))){
			cjWanType = "2";
		}
		//静态IP
		if(sessionType==2&&"Static".equals(StringUtil.getStringValue(cjVoipPVCMap, "ip_type",""))){
			cjWanType = "3" ;
		}
		//DHCP
		else if(sessionType==2&&"DHCP".equals(StringUtil.getStringValue(cjVoipPVCMap, "ip_type",""))) {
			cjWanType = "4" ; 
		}
		
		
		// 语音工单相关信息
		Map<String, String> VOIPInfoMap = userDevDao.queryServSheetData(String.valueOf(userId), "14", userName);
		List<HashMap<String,String>> lines = userDevDao.queryVoipParam(String.valueOf(userId));
		if (null != lines && lines.size() > 0) {
			linesMap = lines.get(0);
		}else {
			logger.warn("一键预处理：工单信息没下发成功");
			return false;
		}
		
		// WanType比对
		if ("".equals(StringUtil.getStringValue(VOIPInfoMap, "wan_type","")) || null == cjWanType) {
			logger.warn("WanType:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(VOIPInfoMap, "wan_type","").equals(cjWanType)) {
				logger.warn("上网方式wanType不一致");
				return false;
			}
		}
		
		// VlanId比对
		if ("".equals(StringUtil.getStringValue(VOIPInfoMap, "vlanid", ""))
				|| "".equals(StringUtil.getStringValue(cjVoipPVCMap, "vlan_id", ""))) {
			logger.warn("VlanId:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(VOIPInfoMap, "vlanid").equals(
					StringUtil.getStringValue(cjVoipPVCMap, "vlan_id"))) {
				logger.warn("VOIP的VlanId不一致");
				return false;
			}
		}
		
		// ProtocolType比对
		
		// ProxyServer比对
		if ("".equals(StringUtil.getStringValue(linesMap, "prox_serv", ""))
				|| "".equals(StringUtil.getStringValue(cjVoiceServiceMap, "prox_serv", ""))) {
			logger.warn("ProxyServer:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(linesMap, "prox_serv").equals(
					StringUtil.getStringValue(cjVoiceServiceMap, "prox_serv"))) {
				logger.warn("VOIP的ProxyServer不一致");
				return false;
			}
		}
		// ProxyServerPort比对
		if ("".equals(StringUtil.getStringValue(linesMap, "prox_port", ""))
				|| "".equals(StringUtil.getStringValue(cjVoiceServiceMap, "prox_port", ""))) {
			logger.warn("ProxyServerPort:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(linesMap, "prox_port").equals(
					StringUtil.getStringValue(cjVoiceServiceMap, "prox_port"))) {
				logger.warn("VOIP的ProxyServerPort不一致");
				return false;
			}
		}
		// StandByProxyServer比对
		if ("".equals(StringUtil.getStringValue(linesMap, "stand_prox_serv", ""))
				|| "".equals(StringUtil.getStringValue(cjVoiceServiceMap, "prox_serv_2", ""))) {
			logger.warn("StandByProxyServer:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(linesMap, "stand_prox_serv").equals(
					StringUtil.getStringValue(cjVoiceServiceMap, "prox_serv_2"))) {
				logger.warn("VOIP的StandByProxyServer不一致");
				return false;
			}
		}
		// StandByProxyServerPort比对
		if ("".equals(StringUtil.getStringValue(linesMap, "stand_prox_port", ""))
				|| "".equals(StringUtil.getStringValue(cjVoiceServiceMap, "prox_port_2", ""))) {
			logger.warn("StandByProxyServerPort:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(linesMap, "stand_prox_port").equals(
					StringUtil.getStringValue(cjVoiceServiceMap, "prox_port_2"))) {
				logger.warn("VOIP的StandByProxyServerPort不一致");
				return false;
			}
		}
		// RegistrarServer比对
		if ("".equals(StringUtil.getStringValue(linesMap, "regi_serv", ""))
				|| "".equals(StringUtil.getStringValue(cjVoiceServiceMap, "regi_serv", ""))) {
			logger.warn("RegistrarServer:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(linesMap, "regi_serv").equals(
					StringUtil.getStringValue(cjVoiceServiceMap, "regi_serv"))) {
				logger.warn("VOIP的RegistrarServer不一致");
				return false;
			}
		}
		// RegistrarServerPort比对
		if ("".equals(StringUtil.getStringValue(linesMap, "regi_port", ""))
				|| "".equals(StringUtil.getStringValue(cjVoiceServiceMap, "regi_port", ""))) {
			logger.warn("RegistrarServerPort:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(linesMap, "regi_port").equals(
					StringUtil.getStringValue(cjVoiceServiceMap, "regi_port"))) {
				logger.warn("VOIP的RegistrarServerPort不一致");
				return false;
			}
		}
		// StandByRegistrarServer比对
		if ("".equals(StringUtil.getStringValue(linesMap, "stand_regi_serv", ""))
				|| "".equals(StringUtil.getStringValue(cjVoiceServiceMap, "stand_regi_serv", ""))) {
			logger.warn("StandByRegistrarServer:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(linesMap, "stand_regi_serv").equals(
					StringUtil.getStringValue(cjVoiceServiceMap, "stand_regi_serv"))) {
				logger.warn("VOIP的StandByRegistrarServer不一致");
				return false;
			}
		}
		// StandByRegistrarServerPort比对
		if ("".equals(StringUtil.getStringValue(linesMap, "stand_regi_port", ""))
				|| "".equals(StringUtil.getStringValue(cjVoiceServiceMap, "stand_regi_port", ""))) {
			logger.warn("StandByRegistrarServerPort:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(linesMap, "stand_regi_port").equals(
					StringUtil.getStringValue(cjVoiceServiceMap, "stand_regi_port"))) {
				logger.warn("VOIP的StandByRegistrarServerPort不一致");
				return false;
			}
		}
		// OutboundProxy比对
		if ("".equals(StringUtil.getStringValue(linesMap, "out_bound_proxy", ""))
				|| "".equals(StringUtil.getStringValue(cjVoiceServiceMap, "out_bound_proxy", ""))) {
			logger.warn("OutboundProxy:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(linesMap, "out_bound_proxy").equals(
					StringUtil.getStringValue(cjVoiceServiceMap, "out_bound_proxy"))) {
				logger.warn("VOIP的OutboundProxy不一致");
				return false;
			}
		}
		// OutboundProxyPort比对
		if ("".equals(StringUtil.getStringValue(linesMap, "out_bound_port", ""))
				|| "".equals(StringUtil.getStringValue(cjVoiceServiceMap, "out_bound_port", ""))) {
			logger.warn("OutboundProxyPort:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(linesMap, "out_bound_port").equals(
					StringUtil.getStringValue(cjVoiceServiceMap, "out_bound_port"))) {
				logger.warn("VOIP的OutboundProxyPort不一致");
				return false;
			}
		}
		// StandByOutboundProxy 比对
		if ("".equals(StringUtil.getStringValue(linesMap, "stand_out_bound_proxy", ""))
				|| "".equals(StringUtil.getStringValue(cjVoiceServiceMap, "stand_out_bound_proxy", ""))) {
			logger.warn("StandByOutboundProxy:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(linesMap, "stand_out_bound_proxy").equals(
					StringUtil.getStringValue(cjVoiceServiceMap, "stand_out_bound_proxy"))) {
				logger.warn("VOIP的StandByOutboundProxy不一致");
				return false;
			}
		}
		// StandByOutboundProxyPort比对
		if ("".equals(StringUtil.getStringValue(linesMap, "stand_out_bound_port", ""))
				|| "".equals(StringUtil.getStringValue(cjVoiceServiceMap, "stand_out_bound_port", ""))) {
			logger.warn("StandByOutboundProxyPort:业务工单有误或者一键预处理没采集到相关语音信息");
			return false;
		}else {
			if (!StringUtil.getStringValue(linesMap, "stand_out_bound_port").equals(
					StringUtil.getStringValue(cjVoiceServiceMap, "stand_out_bound_port"))) {
				logger.warn("VOIP的StandByOutboundProxyPort不一致");
				return false;
			}
		}
		return flag;
	}
	
}
