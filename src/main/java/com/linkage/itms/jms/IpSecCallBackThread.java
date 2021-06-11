package com.linkage.itms.jms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.WSClient.RestClient;
import com.linkage.WSClient.WebServiceUtil;
import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.commons.xml.XML2Bean;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.itms.dao.IpsecServParamDAO;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.service.CloudStaticRtCfgAllService;

/**
 * Ipsec开通，dev.ipsec消息后回调适配层接口
 * 
 * @author jlp
 * 
 */
public class IpSecCallBackThread implements Runnable {
	/** log */
	private static final Logger logger = LoggerFactory.getLogger(IpSecCallBackThread.class);
	// 针对mq过来的消息
	private String message = null;

	private DeviceInfo obj = null;

	public void setMessage(String message) {
		logger.debug("setMessage({})", message);
		this.message = message;
		// 将mq消息转换为obj
		XML2Bean x2b = new XML2Bean(this.message);
		this.obj = (DeviceInfo) x2b.getBean("ServInfo", DeviceInfo.class);
	}

	@Override
	public void run() {
		try {
			int servType = StringUtil.getIntegerValue(obj.getServType(), 0);
			if (servType == 0) {
				logger.warn("servType为[{}]不正确, 不处理 该条message:[{}]", servType, message);
				return;
			}
			switch (servType) {
			case 1: // ipsec开通,修改回调 
				servIpsecCallBack();
				break;
			case 2: // vxlan开通,修改,删除回调
				servVxlanCallBack();
				break;
			case 3: // ip变动上报(ipsec)
				ipChangeIpsecCallBack();
				break;
			case 4: // ip变动上报(vxlan)
				ipChangeVxlanCallBack();
				break;
			case 5: // vxlan新装上报
				vxlanGatewayInstall();
				break;
			default:
				logger.warn("servType为[{}], 不处理 该条message:[{}]", servType, message);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("run error [{}],", e.getMessage());
		}
		// 智网回调 北研测试用
		if (Global.istest == 1) {
			try {
				callRestByBuss("", getRetXml(false));
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("callRestByBuss error [{}],", e.getMessage());
			}
		}
	}
	
	
	/**
	 * ipsec 开通修改业务回调方法
	 * @throws Exception
	 */
	public void servIpsecCallBack() throws Exception {
		String retXml = getRetXml(false);
		logger.warn("servIpsecCallBack begin message: [{}], rul: [{}], targetname: [{}], methodname: [{}]",
				new Object[] { message, Global.ipsecUrl, Global.ipsecTargetName, Global.ipsecMethodName});
		String res = WebServiceUtil.call(Global.ipsecUrl, Global.ipsecTargetName, Global.ipsecMethodName, retXml);
		logger.warn("servIpsecCallBack done [{}]", res);
	}

	/**
	 * vxlan 开通修改删除业务回调方法
	 * @throws Exception
	 */
	public void servVxlanCallBack() throws Exception {
		String retXml = getRetXml(true);
		logger.warn("servVxlanCallBack begin message: [{}], rul: [{}], targetname: [{}], methodname: [{}]",
				new Object[] { message, Global.vxlanUrl, Global.vxlanTargetName, Global.vxlanMethodName});
		String res = WebServiceUtil.call(Global.vxlanUrl, Global.vxlanTargetName, Global.vxlanMethodName, retXml);
		logger.warn("servVxlanCallBack done [{}]", res);
	}

	/**
	 * ipsec ip变动业务回调
	 * @throws Exception
	 */
	public void ipChangeIpsecCallBack() throws Exception {
		
	}
	
	/**
	 * vxlan ip变动业务回调
	 * @throws Exception
	 */
	public void ipChangeVxlanCallBack() throws Exception {
		logger.warn("ipChangeVxlanCallBack begin[{}]", message);
		// 判断ip和宽带账号
		if (StringUtil.IsEmpty(obj.getIpAddress()) || StringUtil.IsEmpty(obj.getLoid())) {
			logger.warn("ip[{}]或者loid[{}]为空, 不处理message[{}]", obj.getIpAddress(), obj.getLoid(), message);
			return ;
		}
		QueryDevDAO qdDao = new QueryDevDAO();
		List<HashMap<String, String>> userMap = qdDao.queryUserByLoidCloud(obj.getLoid());
		
		if (userMap == null || userMap.isEmpty()) {
			logger.warn("loid[{}]查询不到对应用户", obj.getLoid());
			return ;
		}
		String userInfo = StringUtil.getStringValue(userMap.get(0), "account");
		if (StringUtil.IsEmpty(userInfo)) {
			logger.warn("loid[{}]查询不到对应宽带账号", obj.getLoid());
			return ;
		}
		String deviceId =  StringUtil.getStringValue(userMap.get(0), "device_id");
		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("loid[{}]查询不到对应网关", obj.getLoid());
			return ;
		}
		
		Long userId = StringUtil.getLongValue(userMap.get(0), "user_id");
		IpsecServParamDAO ispDao = new IpsecServParamDAO();
		int isVxlan = ispDao.queryVxlanServCount(userId);
		// 没有需要开通的vxlan业务，直接上报
//		if (isVxlan < 1) {
		String retXml = getIpChangeRetXml(true, isVxlan > 0 ? "1" : "0", userInfo);
		logger.warn("ipChangeVxlanCallBack begin rul: [{}], targetname: [{}], methodname: [{}]",
				new Object[] { Global.ipChangeVxlanUrl, Global.ipChangeVxlanTargetName, Global.ipChangeVxlanMethodName});
		String res = WebServiceUtil.call(Global.ipChangeVxlanUrl, Global.ipChangeVxlanTargetName, 
				Global.ipChangeVxlanMethodName, retXml);
		logger.warn("ipChangeVxlanCallBack done [{}]", res);
//		}
//		// 开通vxlan业务
//		else {
//			if (true != CreateObjectFactory.createPreProcess().processDeviceStrategy(
//					new String[]{deviceId}, "2901", 
//					new String[]{"29", String.valueOf(userId), obj.getLoid(), String.valueOf(0)})) {
//				
//			}
//		}
	}

	/**
	 * vxlan新装接口
	 * @throws Exception
	 */
	public void vxlanGatewayInstall() throws Exception {
		logger.warn("vxlanGatewayInstall begin[{}]", message);
		String loid = obj.getLoid();
		String userInfo = obj.getUserInfo();
		// 判断ip和宽带账号
		if (StringUtil.IsEmpty(loid) || StringUtil.IsEmpty(userInfo)) {
			logger.warn("loid[{}]或者宽带账号[{}]为空, 不处理message[{}]", loid, userInfo, message);
			return ;
		}
		
		QueryDevDAO qdDao = new QueryDevDAO();
		List<HashMap<String, String>> userMap = qdDao.queryUserByLoidCloud(loid);
		if (userMap == null || userMap.isEmpty()) {
			logger.warn("loid[{}]查询不到对应用户", loid);
			return ;
		}
		String devSN =  StringUtil.getStringValue(userMap.get(0), "device_serialnumber");
		String deviceId =  StringUtil.getStringValue(userMap.get(0), "device_id");
		if (StringUtil.IsEmpty(deviceId)) {
			logger.warn("loid[{}]查询不到对应网关", loid);
			return ;
		}
		Long userId = StringUtil.getLongValue(userMap.get(0), "user_id");
		IpsecServParamDAO ispDao = new IpsecServParamDAO();
		
		Map<String, String> map = ispDao.getSerBusInfo(userId);
		int openStatus = StringUtil.getIntValue(map, "open_status");
		if (openStatus != 1) {
			ispDao.updateVxlanServOpenStatus(userId);
		}

		int isVxlan = ispDao.queryIsVxlan(userId);
		UserDeviceDAO udDao = new UserDeviceDAO();
		Map<String,String> devMap = udDao.getDeviceTypeInfo(deviceId);
		if (null == devMap || devMap.isEmpty()) {
			logger.warn("loid[{}]查询不到对应网关", loid);
			return ;
		}
		//Map<String, String> map = ispDao.getSerBusInfo(userId);
		String requestId = StringUtil.getStringValue(map, "contact");
		String proInstId = StringUtil.getStringValue(map, "detail");
		if (map == null || map.size() < 1 || StringUtil.IsEmpty(requestId) || StringUtil.IsEmpty(proInstId)) {
			logger.warn("loid[{}]没有获取到产品流水信息[{}]", loid, map);
			return ;
		}
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<root>");
		sb.append("<CmdID>").append("123456789012345").append("</CmdID>");
		sb.append("<CmdType>CX_01</CmdType>");
		sb.append("<ClientType>6</ClientType>");
		sb.append("<InstallDate>").append(new DateTimeUtil().getYYYYMMDDHHMMSS()).append("</InstallDate>");
		sb.append("<Param>");
		sb.append("<DevSN>").append(devSN).append("</DevSN>");
		sb.append("<Loid>").append(loid).append("</Loid>");
		sb.append("<UserInfo>").append(userInfo).append("</UserInfo>");
		sb.append("<LoidPrev>").append("").append("</LoidPrev>");
		sb.append("<IsVXLAN>").append(isVxlan > 0 ? 1 : 0).append("</IsVXLAN>");
		sb.append("<DeviceType>").append(2).append("</DeviceType>");
		sb.append("<DeviceVendor>").append(StringUtil.getStringValue(devMap, "vendor_name")).append("</DeviceVendor>");
		sb.append("<DeviceModel>").append(StringUtil.getStringValue(devMap, "device_model")).append("</DeviceModel>");
		sb.append("<Softwareversion>").append(StringUtil.getStringValue(devMap, "softwareversion")).append("</Softwareversion>");
		sb.append("<Hardwareversion>").append(StringUtil.getStringValue(devMap, "hardwareversion")).append("</Hardwareversion>");
		sb.append("<RequestId>").append(requestId).append("</RequestId>");
		sb.append("<ProInstId>").append(proInstId).append("</ProInstId>");
		sb.append("</Param>");
		sb.append("</root>");
	
		String retXml = sb.toString();
		logger.warn("vxlanGatewayInstall begin, retmessage: [{}], rul: [{}], targetname: [{}], methodname: [{}]",
				new Object[] { retXml, Global.installVxlanUrl, Global.installVxlanTargetName, Global.installVxlanMethodName});
		String res = WebServiceUtil.call(Global.installVxlanUrl, Global.installVxlanTargetName, Global.installVxlanMethodName, retXml);
		logger.warn("vxlanGatewayInstall done [{}]", res);
		//如果有vxlan业务参数下发过，调用vxlan全业务下发，解决移机换机问题,每隔2分钟查一次是否有未做的其他业务，
		//其他业务都成功时再调用配置模块下发，如果三次查询后仍有未做业务，则不处理
		CloudStaticRtCfgAllService cloudStaticService = new CloudStaticRtCfgAllService();
		if(ispDao.queryVxlanServ(userId) > 0)
		{
			Thread.sleep(2 * 60 * 1000L);
			logger.warn("开始第一次查询用户id[{}]的其他业务是否都已开通" ,userId);
			if(ispDao.queryIsServ(userId) > 0)
			{
				Thread.sleep(2 * 60 * 1000L);
				logger.warn("开始第二次查询用户id[{}]的其他业务是否都已开通" ,userId);
				if(ispDao.queryIsServ(userId) > 0)
				{
					Thread.sleep(2 * 60 * 1000L);
					logger.warn("开始第三次查询用户id[{}]的其他业务是否都已开通" ,userId);
					if(ispDao.queryIsServ(userId) > 0)
					{
						logger.warn("三次查询后，用户id[{}]的其他业务未全开通，不进行vxlan全业务下发" ,userId);
					}
					else
					{
						if(cloudStaticService.work(deviceId, userId))
						{
							logger.warn("用户id[{}]开始调用batch进行全业务vxlan下发" ,userId);
							CreateObjectFactory.createPreProcess().processDeviceStrategy(new String[]{deviceId},"2901",
									new String[]{"29", String.valueOf(userId), loid, "0"});
						}
					}
				}
				else
				{
					if(cloudStaticService.work(deviceId, userId))
					{
						logger.warn("用户id[{}]开始调用batch进行全业务vxlan下发" ,userId);
						CreateObjectFactory.createPreProcess().processDeviceStrategy(new String[]{deviceId},"2901",
								new String[]{"29", String.valueOf(userId), loid, "0"});
					}
				}
			}
			else
			{
				if(cloudStaticService.work(deviceId, userId))
				{
					logger.warn("用户id[{}]开始调用batch进行全业务vxlan下发" ,userId);
					CreateObjectFactory.createPreProcess().processDeviceStrategy(new String[]{deviceId},"2901",
							new String[]{"29", String.valueOf(userId), loid, "0"});
				}
			}
		}
	}

	/**
	 * 智网回调 北研测试用
	 * @param req_id
	 * @param xmlContent
	 * @return
	 */
	public String callRestByBuss(String req_id, String xmlContent) {
		String targetServCode = "10.1155.ws_ServiceDoneReturn.SynReq";
		String NMESB_DCN_URL = "http://42.99.0.99:53099/00.0001.O_OtherBusi_serviceHandler";
		String sendorCode = "10.1157.01";

		xmlContent = xmlContent.replaceAll("<", "&lt;");
		xmlContent = xmlContent.replaceAll(">", "&gt;");
		String res = "";
		logger.warn("callRestByBuss call [{}],[{}],[{}]", NMESB_DCN_URL, xmlContent);
		try {
			res = RestClient.sendPostOrPut1(NMESB_DCN_URL, buildESBReqContent(targetServCode, req_id, sendorCode, xmlContent), "POST");
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.warn("callRestByBuss done[{}],", res);
		return res;
	}

	/**
	 * 智网回调拼接xml
	 * @param servCode
	 * @param reqId
	 * @param sender
	 * @param contentXml
	 * @return
	 */
	public static String buildESBReqContent(String servCode, String reqId,
			String sender, String contentXml) {
		String cot = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:impl=\"http://impl.webservice.apps.itms.ai.com\"><soapenv:Header><Esb><Route><Sender>"
				+ sender
				+ "</Sender>"
				+ "<Time>20160108184413985</Time>"
				+ "<ServCode>"
				+ servCode
				+ "</ServCode>"
				+ "<Version>V0.1</Version>"
				+ "<MsgId>"
				+ reqId
				+ "</MsgId>"
				+ "<TransId>"
				+ reqId
				+ "</TransId>"
				+ "<AuthType/>"
				+ "<AuthCode/>"
				+ "<CarryType/>"
				+ "<ServTestFlag/>"
				+ "<MsgType/>"
				+ "</Route>"
				+ "</Esb>"
				+ "</soapenv:Header>"
				+ "<soapenv:Body>"
				+ "<impl:ServiceDoneReturn>"
				+ "<impl:param>"
				+ contentXml
				+ "</impl:param>"
				+ "</impl:ServiceDoneReturn></soapenv:Body>"
				+ "</soapenv:Envelope>";

		return cot;
	}

	/**
	 * 获取ipsec和vxlan开通,修改,删除回调xml
	 * @return
	 */
	private String getRetXml(boolean isVxlan) {
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<root>");
		sb.append("<CmdID>").append(obj.getRequestId()).append("</CmdID>");
		sb.append("<CmdType>CX_01</CmdType>");
		sb.append("<ClientType>6</ClientType>");
		sb.append("<DealDate>").append(new DateTimeUtil().getYYYYMMDDHHMMSS()).append("</DealDate>");
		sb.append("<Param>");
		sb.append("<UserInfoType>").append(obj.getServStatus()).append("</UserInfoType>");
		sb.append("<UserInfo>").append(obj.getServName()).append("</UserInfo>");
		sb.append("<RequestID>").append(obj.getRequestId()).append("</RequestID>");
		sb.append("<Result>").append(obj.getOpenStatus()).append("</Result>");
		// 区分vxlan和ipsec  vxlan有VXLANConfigSequence节点
		if (isVxlan) {
			sb.append("<VXLANConfigSequence>").append(obj.getVxlanConfigSequence()).append("</VXLANConfigSequence>");
		}
		sb.append("</Param>");
		sb.append("</root>");
		return sb.toString();
	}

	/**
	 * 获取ipsec和vxlan ip变动回调xml
	 * @return
	 */
	private String getIpChangeRetXml(boolean isVxlan, String value, String userInfo) {
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<root>");
		sb.append("<CmdID>").append(UUID.randomUUID()).append("</CmdID>");
		sb.append("<CmdType>CX_01</CmdType>");
		sb.append("<ClientType>6</ClientType>");
		sb.append("<InstallDate>").append(new DateTimeUtil().getYYYYMMDDHHMMSS()).append("</InstallDate>");
		sb.append("<Param>");
		
		sb.append("<DevSN>").append(obj.getDevSn()).append("</DevSN>");
		sb.append("<Loid>").append(obj.getLoid()).append("</Loid>");
		sb.append("<UserInfo>").append(userInfo).append("</UserInfo>");
		sb.append("<LoidPrev>").append("").append("</LoidPrev>");
		sb.append("<ServIPAddr>").append(obj.getIpAddress()).append("</ServIPAddr>");
		sb.append("<DeviceType>").append("2").append("</DeviceType>");
		sb.append("<DeviceVendor>").append(obj.getDeviceVendor()).append("</DeviceVendor>");
		sb.append("<DeviceModel>").append(obj.getDeviceModel()).append("</DeviceModel>");
		sb.append("<Softwareversion>").append(obj.getSoftwareversion()).append("</Softwareversion>");
		sb.append("<Hardwareversion>").append(obj.getHardwareversion()).append("</Hardwareversion>");
		// 区分vxlan和ipsec
		if (isVxlan) {
			sb.append("<IsVXLAN>").append(value).append("</IsVXLAN>");
		}
		else {
			sb.append("<IsIPSEC>").append(value).append("</IsIPSEC>");
		}
		sb.append("</Param>");
		sb.append("</root>");
		return sb.toString();
	}

//	/**
//	 * 获取vxlan 新增上报回调xml
//	 * @return
//	 */
//	private String getGatewayInstallRetXml() {
//		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//		sb.append("<root>");
//		sb.append("<CmdID>").append(obj.getRequestId()).append("</CmdID>");
//		sb.append("<CmdType>CX_01</CmdType>");
//		sb.append("<ClientType>6</ClientType>");
//		sb.append("<DealDate>").append(new DateTimeUtil().getYYYYMMDDHHMMSS()).append("</DealDate>");
//		sb.append("<Param>");
//		sb.append("<Loid>").append("").append("</Loid>");
//		sb.append("<UserInfo>").append("").append("</UserInfo>");
//		sb.append("<LoidPrev>").append("").append("</LoidPrev>");
//		sb.append("<RequestId>").append(UUID.randomUUID()).append("</RequestId>");
//		sb.append("</Param>");
//		sb.append("</root>");
//		return sb.toString();
//	}

	public static void main(String[] args) {
		String inXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><ClientType>5</ClientType><CmdID>00000000000000000001" +
				"</CmdID><CmdType>CX_01</CmdType><DealDate>20171116101010</DealDate><Param><CallBack>1</CallBack>" +
				"<UserInfo>pppoetest02@chn.xj</UserInfo><UserInfoType>1</UserInfoType>";
		Pattern p = Pattern.compile("<CallBack>\\d");
		Matcher m = p.matcher(inXml); 
		inXml = m.replaceAll("<CallBack>0");
		
		Pattern p1 = Pattern.compile("<|>");
		Matcher m1 = p1.matcher(inXml);
		inXml = m1.replaceAll("\'");
		
		StringBuffer bf = new StringBuffer();
		bf.append("<ServInfo>");
		bf.append("<servType>").append(1).append("</servType>");
		bf.append("<param>").append(inXml).append("</param>");
		bf.append("</ServInfo>");
		System.out.println(inXml);
		XML2Bean x2b = new XML2Bean(bf.toString());
		DeviceInfo obj = (DeviceInfo) x2b.getBean("ServInfo", DeviceInfo.class);
		
		byte[] param = obj.getParam().getBytes();
//		System.out.println("<".getBytes()[0]);
//		System.out.println(">".getBytes()[0]);
		int num = 0;
		for (int i=0; i < param.length; i++) {
			if (39 == param[i]) {
				if (num % 2 == 0) {
					param[i] = 60;
				}
				else {
					param[i] = 62;
				}
				num ++;
			}
		}
		
		System.out.println(new String(param));
	}

}
