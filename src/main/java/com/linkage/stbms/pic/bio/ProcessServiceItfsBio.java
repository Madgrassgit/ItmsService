
package com.linkage.stbms.pic.bio;

import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.commons.util.TimeUtil;
import com.linkage.stbms.pic.Global;
import com.linkage.stbms.pic.object.StrategyObj;
import com.linkage.stbms.pic.util.SqlUtil;
import com.linkage.stbms.pic.util.StrUtil;

public class ProcessServiceItfsBio
{

	final Logger logger = LoggerFactory.getLogger(ProcessServiceItfsBio.class);

	public String process(String userId, String serviceId, String deviceId, String oui,
			String deviceSn, String taskId, int tempId, int orderId)
	{
		logger.warn(
				"userId={},serviceId={},deviceId={},oui={},deviceSn={}",
				new Object[] { userId, serviceId, deviceId, oui, deviceSn });
		SqlUtil sqlUtil = new SqlUtil();
		String sheetPara = null;
		StrategyObj strategyOBJ = new StrategyObj();
//有注释		if (serviceId.equals(Global.SERVICE_ID_STB_ZERO))
//		{
//			Map<String, String> userMap = sqlUtil.getUserInfo(userId);
//			if(userMap==null){
//				logger.warn("deviceId=[{}]userId=[{}]机顶盒零配置用户不存在", deviceId,
//						userId);
//				return null;
//			}
//			if(Global.JX_PROVINCE.equals(Global.PROVINCE)){//江西
//				logger.warn("deviceId=[{}]userId=[{}]机顶盒零配置开户下发账号密码...", deviceId,
//								userId);
//				sheetPara = StbAccount2Xml_JX(userMap);
//			}
//			if(Global.XJ_PROVINCE.equals(Global.PROVINCE)){//新疆
//				
//				String authUrl = userMap.get("auth_url");
//				if(StringUtil.IsEmpty(authUrl)){
//					logger.warn("[{}]没有对应的零配置业务认证账号，返回！", deviceId);
//					return null;
//				}
//				sheetPara = ZeroConfig2Xml(authUrl);
//			}
//		}
		strategyOBJ.setServiceId(StrUtil.getIntegerValue(serviceId));
		strategyOBJ.createId();
		strategyOBJ.setDeviceId(deviceId);
		strategyOBJ.setOui(oui);
		strategyOBJ.setSn(deviceSn);
		strategyOBJ.setTime(TimeUtil.getCurrentTime());
		strategyOBJ.setSheetPara(sheetPara);
		strategyOBJ.setAccOid(1);
		strategyOBJ.setOrderId(orderId);
		strategyOBJ.setIsLastOne(1);
		strategyOBJ.setPriority(1);
		strategyOBJ.setType(4);
		//需要重做
		strategyOBJ.setRedo(1);
		// 是新类型的策略，策略参数为XML，组装模板
		strategyOBJ.setSheetType(2);
		if (null != taskId)
		{
			strategyOBJ.setTaskId(taskId);
		}
		if (0 != tempId)
		{
			strategyOBJ.setTempId(tempId);
		}
		else
		{
			strategyOBJ.setTempId(Integer.parseInt(serviceId));
		}
		sqlUtil.addStrategy(strategyOBJ);
		sqlUtil = null;
		return StrUtil.getStringValue(strategyOBJ.getId());
	}

	/**
	 * 组装机顶盒零配置xml格式
	 *
	 * @author wangsenbo
	 * @date Dec 2, 2011
	 * @param 
	 * @return String
	 */
	@SuppressWarnings("unused")
	private String ZeroConfig2Xml(String authUrl)
	{
		logger.debug("ZeroConfig2Xml...");
		String strXml = null;
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("ZeroConfig");
		root.addElement("AuthURL").addText(authUrl);
		strXml = doc.asXML();
		return strXml;
	}

	/**
	 * 组装机顶盒零配置xml格式
	 *
	 * @author wangsenbo
	 * @date Dec 2, 2011
	 * @param 
	 * @return String
	 */
	@SuppressWarnings("unused")
	private String StbAccount2Xml_JX(Map<String, String> accountMap)
	{
		logger.debug("StbAccount2Xml...");
		String addvod = "";
		String strXml = null;
		// new doc
		Document doc = DocumentHelper.createDocument();
		// root node: NET
		Element root = doc.addElement("STB");
		root.addElement("AddressingType").addText(
				null == accountMap.get("addressing_type") ? "PPPoE" : accountMap
						.get("addressing_type"));
		root.addElement("ServAccount").addText(
				null == accountMap.get("serv_account") ? "" : accountMap
						.get("serv_account"));
		root.addElement("ServPassword").addText(
				null == accountMap.get("serv_pwd") ? "" : accountMap.get("serv_pwd"));
		root.addElement("PPPoEID").addText(
				null == accountMap.get("pppoe_user") ? "" : accountMap.get("pppoe_user")
						+ (StringUtil.IsEmpty(addvod) ? "" : addvod));
		root.addElement("PPPoEPassword").addText(
				null == accountMap.get("pppoe_pwd") ? "" : accountMap.get("pppoe_pwd"));
		root.addElement("IPAddress").addText(null == accountMap.get("ipaddress") ? "" : accountMap.get("ipaddress"));
		root.addElement("SubnetMask").addText(null == accountMap.get("ipmask") ? "" : accountMap.get("ipmask"));
		root.addElement("DefaultGateway").addText(null == accountMap.get("gateway") ? "" : accountMap.get("gateway"));
		root.addElement("DNSServers").addText(null == accountMap.get("dns") ? "" : accountMap.get("dns"));
		strXml = doc.asXML();
		return strXml;
	}
}
