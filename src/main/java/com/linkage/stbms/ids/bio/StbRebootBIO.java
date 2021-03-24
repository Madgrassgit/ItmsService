package com.linkage.stbms.ids.bio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.util.AcsCorba;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.dao.corba.AcsCorbaDAO;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.commons.util.StringUtil;
import com.linkage.commons.util.TimeUtil;
import com.linkage.litms.acs.soap.service.Reboot;
import com.linkage.stbms.dao.RecordLogDAO;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.obj.StrategyOBJ;
import com.linkage.stbms.ids.util.CommonParamUtil;
import com.linkage.stbms.ids.util.PreProcessCorba;
import com.linkage.stbms.ids.util.StbRebootChecker;
import com.linkage.stbms.itv.main.Global;
import com.linkage.stbms.itv.main.StbServGlobals;

/**
 * @author zhangshimin(工号) Tel:??
 * @version 1.0
 * @category com.linkage.stbms.ids.bio
 * @copyright 南京联创科技 网管科技部
 * @since 2011-4-21 下午06:09:35
 */
public class StbRebootBIO
{

	private static final Logger logger = LoggerFactory.getLogger(StbRebootBIO.class);
	private UserStbInfoDAO dao;
	// 正则，字符加数字
	Pattern pattern = Pattern.compile("\\w{1,}+");
	private int resultFlag = 0;
	private String result = "";

	/**
	 * 机顶盒重启接口
	 *
	 * @param inParam
	 * @return
	 */
	public String setStbReboot(String inParam)
	{
		logger.debug("setStbReboot()");
		logger.warn("setStbReboot==>inParam:" + inParam);
		// 解析获得入参
		Map<String, String> inParamMap = CommonParamUtil.getCommonInParam(inParam);
		String devSn = inParamMap.get("dev_sn");
		String oui = inParamMap.get("oui");
		if (StringUtil.IsEmpty(devSn) || false == pattern.matcher(devSn).matches())
		{
			logger.warn("非法设备序列号: " + devSn);
			resultFlag = 0;
			result = "非法设备序列号: " + devSn;
		}
		else if (StringUtil.IsEmpty(oui) || false == pattern.matcher(oui).matches())
		{
			logger.warn("非厂商oui: " + oui);
			resultFlag = 0;
			result = "非厂商oui: " + oui;
		}
		else
		{
			dao = new UserStbInfoDAO();
			String deviceId = dao.getDeviceId(devSn, oui);
			if (deviceId == null || deviceId.equals(""))
			{
				resultFlag = 0;
				result = "查无此设备：" + devSn;
				logger.error(result);
			}
			else
			{
				// 机顶盒重启
				int relt = this.rebootStb(deviceId);
				logger.warn("zhangshimin11111111:" + relt);
				if (relt == 1 || relt == 0)
				{
					resultFlag = 1;
					result = "成功";
				}
				else
				{
					resultFlag = 0;
					String decs = Global.G_Fault_Map.get(StringUtil.getStringValue(relt));
					if (StringUtil.IsEmpty(decs))
					{
						result = "重启失败！";
					}
					else
					{
						result = decs;
					}
				}
			}
		}
		Map<String, String> resrultMap = new HashMap<String, String>();
		resrultMap.put("result_flag", StringUtil.getStringValue(resultFlag));
		resrultMap.put("result", result);
		String returnXML = CommonParamUtil.commonReturnParam(resrultMap);
		logger.warn("setStbReboot==>returnXML:" + returnXML);
		return returnXML;
	}

	/**
	 * 机顶盒重启接口 江西电信
	 *
	 * @param inParam
	 * @return
	 */
	public String setStbRebootTwo(String inParam)
	{
		logger.warn("setStbRebootTwo==>inParam=" + inParam);
		// 解析获得入参
		StbRebootChecker checker = new StbRebootChecker(inParam);
		// 验证入参
		if (false == checker.check())
		{
			logger.warn("入参验证没通过,SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			logger.warn("setStbRebootTwo==>retParam=" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		dao = new UserStbInfoDAO();
		Map<String, String> map = dao
				.getDeviceIdStr(checker.getSearchType(), checker.getSearchInfo(), "1");
		if (null == map || "".equals(map.get("device_id")))
		{
			checker.setRstCode("0");
			checker.setRstMsg("查无此设备");
			logger.warn("查无此设备，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			logger.warn("setStbRebootTwo==>returnXML:" + checker.getReturnXml());
			if ("xj_dx".equals(Global.G_instArea))
			{
				new RecordLogDAO()
						.recordLog(checker.getSearchInfo(), checker.getInParam(), "",
								checker.getReturnXml(), 1);
			}
			return checker.getReturnXml();
		}
		else
		{
			if ("xj_dx".equals(Global.G_instArea))
			{
				GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
				ACSCorba acsCorba = new ACSCorba("4");
				String deviceId = map.get("device_id");
				int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
				if (-6 == flag)
				{
					checker.setRstCode("-6");
					checker.setRstMsg("设备正在被操作!");
					return checker.getReturnXml();
				}
				else if (1 == flag)
				{
					// 设备在线  机顶盒重启
					int relt = this.rebootStb(deviceId);
					logger.warn("机顶盒重启结果:" + relt);
					if (relt == 1 || relt == 0)
					{
						checker.setRstCode("1");
						checker.setRstMsg("机顶盒重启成功!");
						logger.warn(
								"setStbRebootTwo==>returnXML:" + checker.getReturnXml());
						new RecordLogDAO()
								.recordLog(checker.getSearchInfo(), checker.getInParam(),
										"", checker.getReturnXml(), 1);
						return checker.getReturnXml();
					}
					else
					{
						checker.setRstCode("0");
						String decs = Global.G_Fault_Map
								.get(StringUtil.getStringValue(relt));
						if (StringUtil.IsEmpty(decs))
						{
							checker.setRstMsg("重启失败！");
						}
						else
						{
							checker.setRstMsg(decs);
						}
						logger.warn(
								"setStbRebootTwo==>returnXML:" + checker.getReturnXml());
						new RecordLogDAO()
								.recordLog(checker.getSearchInfo(), checker.getInParam(),
										"", checker.getReturnXml(), 1);
						return checker.getReturnXml();
					}
				}
				else
				{
					logger.warn("设备不在线,无法重启。device_id={}", deviceId);
					checker.setRstCode("1005");
					checker.setRstMsg("设备不在线");
					logger.warn("return=({})", checker.getReturnXml());
					return checker.getReturnXml();
				}
			}
			else
			{
				String deviceId = map.get("device_id");
				int relt = this.rebootStb(deviceId);
				logger.warn("机顶盒重启结果:" + relt);
				if (relt == 1 || relt == 0)
				{
					checker.setRstCode("1");
					checker.setRstMsg("机顶盒重启成功!");
					logger.warn("setStbRebootTwo==>returnXML:" + checker.getReturnXml());
					return checker.getReturnXml();
				}
				else
				{
					checker.setRstCode("0");
					String decs = Global.G_Fault_Map.get(StringUtil.getStringValue(relt));
					if (StringUtil.IsEmpty(decs))
					{
						checker.setRstMsg("重启失败！");
					}
					else
					{
						checker.setRstMsg(decs);
					}
					logger.warn("setStbRebootTwo==>returnXML:" + checker.getReturnXml());
					return checker.getReturnXml();
				}
			}
		}
	}

	/**
	 * 重启操作
	 *
	 * @param
	 * @return <li>0,1:成功</li>
	 * <li>-7:系统参数错误</li>
	 * <li>-6:设备正被操作</li>
	 * <li>-1:设备连接失败</li>
	 * <li>-9:系统内部错误</li>
	 * @author Jason(3412)
	 * @date 2009-7-1
	 */
	public int rebootStb(String deviceId)
	{
		int flag = -9;
		logger.info("device reboot. deviceId:" + deviceId);
		Reboot reboot = new Reboot();
		reboot.setCommandKey("65535");
		DevRpc[] devRPCArr = new DevRpc[1];
		devRPCArr[0] = new DevRpc();
		devRPCArr[0].devId = deviceId;
		Rpc rpc = new Rpc();
		rpc.rpcId = "1";
		rpc.rpcName = reboot.getClass().getSimpleName();
		rpc.rpcValue = reboot.toRPC();
		devRPCArr[0].rpcArr = new Rpc[] { rpc };
		logger.warn("Global.ACS_OBJECT_NAME=" + Global.ACS_OBJECT_NAME);
		//		List<DevRpcCmdOBJ> list = new AcsCorbaDAO(Global.ACS_OBJECT_NAME).execRPC(StbServGlobals.getLipossProperty("mq.clientId"), Global.rpcType,
		//				Global.priority, devRPCArr);
		List<DevRpcCmdOBJ> list = new AcsCorbaDAO(Global.ACS_OBJECT_NAME)
				.execRPC(Global.CLIENT_ID, Global.rpcType, Global.priority, devRPCArr);
		if (null == list || list.isEmpty())
		{
			flag = -9;
			return flag;
		}
		flag = list.get(0).getStat();
		return flag;
	}
	//	/**
	//	 * 设备重启接口
	//	 *
	//	 * @param devSn
	//	 * @param oui
	//	 * @return
	//	 */
	//	private void reboot(String devSn, String oui)
	//	{
	//		dao = new UserStbInfoDAO();
	//		String deviceId = dao.getDeviceId(devSn, oui);
	//		if (deviceId == null || deviceId.equals(""))
	//		{
	//			resultFlag = 0;
	//			result = "查无此设备：" + devSn;
	//			logger.error(result);
	//		}
	//		else
	//		{
	//			// 获取配置参数(XML)字符串
	//			String strategyXmlParam = setStbRebootXML();
	//			/** 入策略表，调预读 */
	//			// 立即执行
	//			int strategyType = 0;
	//			// 配置的service_id
	//			int serviceId = 5001;
	//			StrategyOBJ strategyObj = new StrategyOBJ();
	//			// 策略ID
	//			strategyObj.createId();
	//			// 策略配置时间
	//			strategyObj.setTime(TimeUtil.getCurrentTime());
	//			// 用户id
	//			strategyObj.setAccOid(0);
	//			// 立即执行
	//			strategyObj.setType(strategyType);
	//			// 设备ID
	//			strategyObj.setDeviceId(deviceId);
	//			// QOS serviceId
	//			strategyObj.setServiceId(serviceId);
	//			// 顺序,默认1
	//			strategyObj.setOrderId(1);
	//			// 工单类型: 新工单,工单参数为xml串的工单
	//			strategyObj.setSheetType(2);
	//			// 参数
	//			strategyObj.setSheetPara(strategyXmlParam);
	//			strategyObj.setTempId(serviceId);
	//			strategyObj.setIsLastOne(1);
	//			// 入策略表
	//			if (dao.addStrategy(strategyObj))
	//			{
	//				// 调用预读
	//				if (true == new PreProcessCorba().processOOBatch(String
	//						.valueOf(strategyObj.getId())))
	//				{
	//					logger.debug("调用后台成功;" + strategyObj.getId());
	//					result = "成功";
	//					resultFlag = 1;
	//				}
	//				else
	//				{
	//					logger.warn("调用预读失败");
	//					result = "调用预读失败";
	//					resultFlag = 0;
	//				}
	//			}
	//			else
	//			{
	//				logger.warn("策略入库失败");
	//				result = "策略入库失败";
	//				resultFlag = 0;
	//			}
	//		}
	//	}

	/**
	 * @param password
	 * @param reboot
	 * @return
	 */
	public String setStbRebootXML()
	{
		String strXml = null;
		// new doc
		Document doc = DocumentHelper.createDocument();
		// root node: STB
		Element root = doc.addElement("STB");
		Element LAN = root.addElement("LAN");
		LAN.addAttribute("flag", "0");
		LAN.addElement("IPAddress");
		LAN.addElement("SubnetMask");
		LAN.addElement("DefaultGateway");
		LAN.addElement("DNSServers");
		Element AddressingType = root.addElement("AddressingType");
		AddressingType.addAttribute("flag", "0");
		Element ServiceInfo = root.addElement("ServiceInfo");
		ServiceInfo.addAttribute("flag", "0");
		ServiceInfo.addElement("UserID");
		ServiceInfo.addElement("UserPassword");
		ServiceInfo.addElement("AuthURL");
		Element ServiceInfoppp = root.addElement("ServiceInfo-ppp");
		ServiceInfoppp.addAttribute("flag", "0");
		ServiceInfoppp.addElement("PPPoEID");
		ServiceInfoppp.addElement("PPPoEPassword");
		Element UserInterface = root.addElement("UserInterface");
		UserInterface.addAttribute("flag", "0");
		UserInterface.addElement("AutoUpdateServer");
		Element ChangePwd = root.addElement("ChangePwd");
		ChangePwd.addAttribute("flag", "0");
		Element Reboot = root.addElement("Reboot");
		Reboot.addAttribute("flag", "1");
		strXml = doc.asXML();
		return strXml;
	}
}
