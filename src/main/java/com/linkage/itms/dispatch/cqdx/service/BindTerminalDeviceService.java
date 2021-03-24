package com.linkage.itms.dispatch.cqdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.cqdx.obj.BindTerminalDeviceDealXML;
import com.linkage.itms.dispatch.service.ServiceHandle;

/**
 * bind方法的业务处理类 用户设备绑定
 * 
 * @author hourui(76958)
 * @date 2017-11-19
 */
public class BindTerminalDeviceService {

	private static Logger logger = LoggerFactory.getLogger(BindTerminalDeviceService.class);

	// 登录用户ID
	private static final long ACC_OID = -3;
	// 处理人
	private static final String DEAL_STAFF = "综调";
	// 用户来源
	private static final int USERLINE = 1;
	
	//用户宽带帐号
	private final int USERINFOTYPE_1 =1;
	//LOID
	private final int USERINFOTYPE_2 =2;
	
	/**
	 * 绑定执行方法
	 */
	public String work(String inXml) {
		BindTerminalDeviceDealXML binder = new BindTerminalDeviceDealXML(inXml);
		if (false == binder.check()) {
			logger.error("servicename[BindTerminalDeviceService]ppp_username[{}],logic_id[{}]验证未通过，返回：{}",
					new Object[] {  binder.getPpp_username(),binder.getLogic_id(),binder.getReturnXml() });
			return binder.getReturnXml();
		}
		logger.warn("servicename[BindTerminalDeviceService]ppp_username[{}],logic_id[{}]参数校验通过，入参为：{}",
				new Object[] { binder.getPpp_username(),binder.getLogic_id(),inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		
		// 查询设备信息 
		ArrayList<HashMap<String, String>> devInfoMapList = userDevDao.queryDevInfo(binder.getSerial_number());
		if (null == devInfoMapList || devInfoMapList.isEmpty()) {
			logger.warn("servicename[BindTerminalDeviceService]ppp_username[{}],logic_id[{}],serial_number[{}]查无此设备",
					new Object[] { binder.getPpp_username(),binder.getLogic_id(),binder.getSerial_number()});
			binder.setResulltCode(-99);
			binder.setResultDesc("查无此设备");
			return binder.getReturnXml();
		} else { // 查询到终端
			// 终端数是否唯一
			int size = devInfoMapList.size();
			if (size > 1) {
				logger.warn(
						"servicename[BindTerminalDeviceService]ppp_username[{}],logic_id[{}],serial_number[{}]查询到多台设备",
						new Object[] {binder.getPpp_username(),binder.getLogic_id(),binder.getSerial_number()});
				binder.setResulltCode(-99);
				binder.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				return binder.getReturnXml();
			} else { // 终端唯一
				HashMap<String, String> resMap = devInfoMapList.get(0);
				String deviceCityId = resMap.get("city_id");
				if (!"cq_dx".equals(Global.G_instArea) ) {// 属地不匹配
					logger.warn("servicename[BindTerminalDeviceService]ppp_username[{}],logic_id[{}],serial_number[{}]",
							new Object[] { binder.getPpp_username(),binder.getLogic_id(),binder.getSerial_number()});
					binder.setResulltCode(-99);
					binder.setResultDesc("查无此设备");
					return binder.getReturnXml();
				} else {// 属地匹配
					String bindFlag = resMap.get("cpe_allocatedstatus");
					if ("1".equals(bindFlag)) {
						logger.warn(
								"servicename[BindTerminalDeviceService]ppp_username[{}],logic_id[{}],serial_number[{}]设备已绑定",
								new Object[] {binder.getPpp_username(),binder.getLogic_id(),binder.getSerial_number()});
						binder.setResulltCode(-99);
						binder.setResultDesc("设备已被绑定");
						return binder.getReturnXml();
					} else { // 终端未绑定
						String deviceId = resMap.get("device_id");
						String deviceOui = resMap.get("oui");
						String deviceSn = resMap.get("device_serialnumber");
						// 查询用户信息 考虑属地因素
						Map<String, String> userInfoMap = null;
						
						if(!StringUtil.IsEmpty(binder.getPpp_username()))
						{
							userInfoMap = userDevDao.queryUserInfo(USERINFOTYPE_1, binder.getPpp_username());
						}
						else if(!StringUtil.IsEmpty(binder.getLogic_id()))
						{
							userInfoMap = userDevDao.queryUserInfo(USERINFOTYPE_2, binder.getLogic_id());
						}
						
						if (null == userInfoMap || userInfoMap.isEmpty()) {
							logger.warn("servicename[BindTerminalDeviceService]ppp_username[{}],logic_id[{}],serial_number[{}]查无此用户",
									new Object[] { binder.getPpp_username(),binder.getLogic_id(),binder.getSerial_number()});
							binder.setResulltCode(-1);
							binder.setResultDesc("查无此用户");
							return binder.getReturnXml();
						} else {// 用户存在
							long userId = StringUtil.getLongValue(userInfoMap.get("user_id"));
							String username = userInfoMap.get("username");
							String userCityId = userInfoMap.get("city_id");
							String userDevId = userInfoMap.get("device_id");
							// 江西是根据city_id参数模糊匹配找出的数据,所以没必要在验证city_id
							if (!"cq_dx".equals(Global.G_instArea) ) {// 属地不匹配
								logger.warn("servicename[BindTerminalDeviceService]ppp_username[{}],logic_id[{}],serial_number[{}]用户属地不匹配",
										new Object[] { binder.getPpp_username(),binder.getLogic_id(),binder.getSerial_number()});
								binder.setResulltCode(-1);
								binder.setResultDesc("查无此用户");
								return binder.getReturnXml();
							} else {// 属地匹配
								if(!userDevDao.getUserType(userId).equals(resMap.get("device_type")))
								{//终端类型不匹配
									logger.warn("servicename[BindTerminalDeviceService]ppp_username[{}],logic_id[{}],serial_number[{}]用户与终端类型不匹配，不予绑定",
											new Object[] { binder.getPpp_username(),binder.getLogic_id(),binder.getSerial_number()});
									binder.setResulltCode(-99);
									binder.setResultDesc("用户与设备终端类型不匹配，不予绑定");
									return binder.getReturnXml();
								}
								else
								{//终端类型匹配
									if (StringUtil.IsEmpty(userDevId)) {// 用户未绑定终端
										// 绑定
										serviceHandle.itmsInst(ACC_OID, StringUtil.getStringValue(userId), username,userCityId, deviceId, deviceCityId,
												deviceOui, deviceSn, DEAL_STAFF, 1,
												USERLINE);
										binder.setResulltCode(0);
										binder.setResultDesc("已经调用后台进行绑定");
										return binder.getReturnXml();
									} else {// 用户已绑定
								            // 新装
											logger.warn("servicename[BindTerminalDeviceService]ppp_username[{}],logic_id[{}],serial_number[{}]用户已绑定终端",
													new Object[] { binder.getPpp_username(),binder.getLogic_id(),binder.getSerial_number()});
											binder.setResulltCode(-99);
											binder.setResultDesc("用户已绑定终端");
											return binder.getReturnXml();
									
									}
								}
								
							}
						}
					}
				}
			}
		}
	}

}
