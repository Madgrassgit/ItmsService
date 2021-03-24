
package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.SocketUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.MoSpecInfoChecker;

/**
 * 修改用户终端规格接口
 * 
 * @author yinlei3 (73167)
 * @version 1.0
 * @since 2015年9月21日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class MoSpecInfoService implements IService
{

	private static Logger logger = LoggerFactory.getLogger(MoSpecInfoService.class);
	private final String E8_C_TYPE_ID = "2";
	private final String STATIC_IP = "3";
	private final String ME14 = "15";

	@Override
	public String work(String inXml)
	{
		MoSpecInfoChecker checker = new MoSpecInfoChecker(inXml);
		// 参数检证
		if (false == checker.check())
		{
			logger.error(
					"servicename[MoSpecInfoService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn("servicename[MoSpecInfoService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		// 获取用户信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ServiceHandle serviceHandle = new ServiceHandle();
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo());
		// 用户信息不存在
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn("servicename[MoSpecInfoService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1001);
			checker.setResultDesc("用户不存在");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
		long user_id = StringUtil.getLongValue(userInfoMap, "user_id");
		String userCityId = StringUtil.getStringValue(userInfoMap, "city_id");
		String userSpec_id = StringUtil.getStringValue(userInfoMap, "spec_id");
		String userDevice_id = StringUtil.getStringValue(userInfoMap, "device_id");
		if (false == serviceHandle.cityMatch(checker.transCityId(checker.getCityId()),
				userCityId))
		{
			// 属地不匹配
			logger.warn("servicename[MoSpecInfoService]cmdId[{}]userinfo[{}]属地不匹配",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1007);
			checker.setResultDesc("属地不匹配");
			return checker.getReturnXml();
		}

		if(userSpec_id.equals(checker.transSpecInfo(checker.getNewSpecInfo())))
		{
			logger.warn("servicename[MoSpecInfoService]cmdId[{}]userinfo[{}]用户新旧终端规格相同,无需修改",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1007);
			checker.setResultDesc("用户新旧终端规格相同,无需修改");
			return checker.getReturnXml();
		
		}
		// 目前悦me的规格只有 1290 一种
		// 用户绑定了设备
		if (!StringUtil.IsEmpty(userDevice_id))
		{
			// 不能更新为悦me
			if ("1290".equals(checker.getNewSpecInfo()))
			{
				logger.warn(
						"servicename[MoSpecInfoService]cmdId[{}]userinfo[{}]用户已绑定设备,无法修改为悦me",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1007);
				checker.setResultDesc("用户已绑定设备,无法修改为悦me");
				return checker.getReturnXml();
			}
			// 如果原规格是悦me,不能修改为其他规格
			if (ME14.equals(userSpec_id))
			{
				logger.warn(
						"servicename[MoSpecInfoService]cmdId[{}]userinfo[{}]用户已绑定设备且终端规格是悦me,无法修改",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1007);
				checker.setResultDesc("用户已绑定设备且终端规格是悦me,无法修改");
				return checker.getReturnXml();
			}
		}

		// 查询用户终端是不是e8-c终端
		String type_id = userDevDao.getTypeIdByUserId(user_id);
		if (StringUtil.IsEmpty(type_id) || !E8_C_TYPE_ID.equals(type_id))
		{
			logger.warn(
					"servicename[MoSpecInfoService]cmdId[{}]userinfo[{}]用户BSS终端类型非e8-c",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1007);
			checker.setResultDesc("用户BSS终端类型非e8-c");
			return checker.getReturnXml();
		}
		// 获取新终端设备支持的lan口数和语音口数
		int newSpecId = StringUtil.getIntegerValue(checker.transSpecInfo(checker
				.getNewSpecInfo()));
		Map<String, String> CountMap = userDevDao.getCountLanAndVoice(newSpecId);
		if (CountMap == null || CountMap.isEmpty())
		{
			logger.warn(
					"servicename[MoSpecInfoService]cmdId[{}]userinfo[{}]查不到对应的新终端类型信息",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1001);
			checker.setResultDesc("查不到对应的新终端类型信息");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
		// 获取新终端类型的上网口数和语音口数
		int lanCount = StringUtil.getIntValue(CountMap, "lan_num");
		int voiceCount = StringUtil.getIntValue(CountMap, "voice_num");
		// 查询宽带业务数
		List<HashMap<String, String>> netList_user = userDevDao.getNetByUserId(user_id);
		// 查询ipTv业务数
		List<HashMap<String, String>> iptvList_user = userDevDao.getIptvByUserId(user_id);
		int iptvCount_user = 0;
		for(HashMap<String, String> iptv : iptvList_user){
			iptvCount_user = iptvCount_user + StringUtil.getIntValue(iptv, "serv_num");
		}

		// 查询void业务总数
		int voiceCount_user = userDevDao.getCountVoiceByUserId(user_id);
		if (lanCount < (netList_user.size() + iptvCount_user))
		{
			logger.warn("servicename[MoSpecInfoService]cmdId[{}]userinfo[{}]新终端规格lan口数不足",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1001);
			checker.setResultDesc("新终端规格lan口数不足");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
		if (voiceCount < voiceCount_user)
		{
			logger.warn("servicename[MoSpecInfoService]cmdId[{}]userinfo[{}] 新终端规格语音口数不足",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1001);
			checker.setResultDesc("新终端规格语音口数不足");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
		// 更新库里的用户终端规格
		userDevDao.updateSpecIdByUserId(user_id, newSpecId);
		StringBuffer sheetCmd = new StringBuffer();
		DateTimeUtil dt = new DateTimeUtil();
		String sengSheetCmdResult = "";
		// 验证通过后,E8-C终端IPTV业务工单和E8-C终端VOIP业务不需要重新下发(因为分配后的端口情况与之前一致)
		// E8-C宽带业务
		for (HashMap<String, String> netMap : netList_user)
		{
			//先进行拆机工单
			//ex : 22|||3|||20080107181034|||AA00AA|||0001|||02500000000LINKAGE
			sheetCmd.append("22|||3|||");
			sheetCmd.append(dt.getYYYYMMDDHHMMSS() + "|||");
			// 逻辑SN
			sheetCmd.append(StringUtil.getStringValue(userInfoMap, "username") + "|||");
			// 属地
			sheetCmd.append(userCityId + "|||");
			// 宽带帐号
			sheetCmd.append(StringUtil.getStringValue(netMap, "username") + "LINKAGE");
			logger.warn("宽带业务拆机工单为:		" + sheetCmd.toString());
			sengSheetCmdResult = SocketUtil.sendStrMesg(
					Global.G_ITMS_SHEET_SERVER_CHINA_MOBILE,
					StringUtil.getIntegerValue(Global.G_ITMS_SHEET_PORT_CHINA_MOBILE),
					sheetCmd.toString()+ "\n");
			logger.warn("宽带业务拆机回单结果为:	" + sengSheetCmdResult);
			sheetCmd.setLength(0);
			
			
			// 再进行开户工单
			// ex: 22|||1|||20100507181034|||e8c|||AAA123456ZTELAN|||jstest|||123456|||0002|||1|||1|||1|||
			// ||| ||| ||| |||1LINKAGE
			sheetCmd.append("22|||1|||");
			sheetCmd.append(dt.getYYYYMMDDHHMMSS() + "|||");
			sheetCmd.append("e8c|||");
			// 逻辑SN
			sheetCmd.append(StringUtil.getStringValue(userInfoMap, "username") + "|||");
			// 宽带帐号或专线接入号
			sheetCmd.append(StringUtil.getStringValue(netMap, "username") + "|||");
			// 宽带密码(ADSL方式的必须)
			sheetCmd.append(StringUtil.getStringValue(netMap, "passwd") + "|||");
			// 属地
			sheetCmd.append(userCityId + "|||");
			// VLANID(LAN上行和EPON/GPON上行的时候必须)
			sheetCmd.append(StringUtil.getStringValue(netMap, "vlanid") + "|||");
			// 用户IP类型
			sheetCmd.append(StringUtil.getStringValue(netMap, "ip_type") + "|||");
			// 上网方式
			sheetCmd.append(StringUtil.getStringValue(netMap, "wan_type") + "|||");
			if (STATIC_IP.equals(StringUtil.getStringValue(netMap, "wan_type")))
			{
				// Ip地址(静态IP时必须)
				sheetCmd.append(StringUtil.getStringValue(netMap, "ipaddress") + "|||");
				// 掩码(静态IP时必须)
				sheetCmd.append(StringUtil.getStringValue(netMap, "ipmask") + "|||");
				// 网关(静态IP时必须)
				sheetCmd.append(StringUtil.getStringValue(netMap, "gateway") + "|||");
				// DNS(静态IP时必须)
				sheetCmd.append(StringUtil.getStringValue(netMap, "adsl_ser") + "|||");
			}
			else
			{
				sheetCmd.append("||||||||||||");
			}
			// 宽带标识
			sheetCmd.append(userDevDao.getUniqueNetMark(user_id) + "LINKAGE");
			logger.warn("宽带业务新装工单为:		" + sheetCmd.toString());
			sengSheetCmdResult = SocketUtil.sendStrMesg(
					Global.G_ITMS_SHEET_SERVER_CHINA_MOBILE,
					StringUtil.getIntegerValue(Global.G_ITMS_SHEET_PORT_CHINA_MOBILE),
					sheetCmd.toString()+ "\n");
			logger.warn("宽带业务新装回单结果为:	" + sengSheetCmdResult);
			sheetCmd.setLength(0);
		}
		
		// IPTV业务工单
		if (iptvList_user != null && !iptvList_user.isEmpty())
		{
			HashMap<String, String> iptvMap = iptvList_user.get(0);
			// 先进行拆机工单
			// ex:
			// 21|||3|||20080107181034|||AAA123456|||0001|||iptvuserLINKAGE
			sheetCmd.append("21|||3|||");
			sheetCmd.append(dt.getYYYYMMDDHHMMSS() + "|||");
			// 逻辑SN
			sheetCmd.append(StringUtil.getStringValue(userInfoMap, "username") + "|||");
			// 属地
			sheetCmd.append(userCityId + "|||");
			// IPTV宽带接入账号
			sheetCmd.append(StringUtil.getStringValue(iptvMap, "username") + "LINKAGE");

			logger.warn("iptv业务拆机工单为:		" + sheetCmd.toString());
			sengSheetCmdResult = SocketUtil.sendStrMesg(
					Global.G_ITMS_SHEET_SERVER_CHINA_MOBILE,
					StringUtil.getIntegerValue(Global.G_ITMS_SHEET_PORT_CHINA_MOBILE),
					sheetCmd.toString() + "\n");
			logger.warn("iptv业务拆机回单结果为:	" + sengSheetCmdResult);
			sheetCmd.setLength(0);
			// 再进行新装工单
			// ex:
			// 21|||1|||20100507181034|||e8c|||AAA123456ZTELAN|||iptvuser|||0001|||2|||LINKAGE
			
			sheetCmd.append("21|||1|||");
			sheetCmd.append(dt.getYYYYMMDDHHMMSS() + "|||");
			// 设备类型
			sheetCmd.append("e8c|||");
			// 逻辑SN
			sheetCmd.append(StringUtil.getStringValue(userInfoMap, "username") + "|||");
			// IPTV宽带接入账号
			sheetCmd.append(StringUtil.getStringValue(iptvMap, "username") + "|||");
			// 属地
			sheetCmd.append(userCityId + "|||");
			// IPTV个数
			sheetCmd.append(StringUtil.getIntValue(iptvMap, "serv_num") + "|||");

			sheetCmd.append("LINKAGE");
			logger.warn("iptv业务新装工单为:		" + sheetCmd.toString());
			sengSheetCmdResult = SocketUtil.sendStrMesg(
					Global.G_ITMS_SHEET_SERVER_CHINA_MOBILE,
					StringUtil.getIntegerValue(Global.G_ITMS_SHEET_PORT_CHINA_MOBILE),
					sheetCmd.toString() + "\n");
			logger.warn("iptv业务新装回单结果为:	" + sengSheetCmdResult);
			sheetCmd.setLength(0);
		}
		logger.warn("servicename[MoSpecInfoService]cmdId[{}]userinfo[{}]修改成功",
				new Object[] { checker.getCmdId(), checker.getUserInfo() });
		checker.setResult(0);
		checker.setIsSucc(0);
		checker.setResultDesc("修改成功");
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
				"MoSpecInfoService");
		logger.warn("servicename[MoSpecInfoService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), returnXml });
		return returnXml;
	}
}
