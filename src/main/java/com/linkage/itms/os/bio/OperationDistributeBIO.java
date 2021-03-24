
package com.linkage.itms.os.bio;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.obj.Order;
import com.linkage.itms.os.dao.UserBindDAO;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fanjm ( No.35572)
 * @version 1.0
 * @since 2017年3月17日
 */
public class OperationDistributeBIO
{

	private static Logger logger = LoggerFactory.getLogger(OperationDistributeBIO.class);
	private static final String SERV_TYPE_ID = "servTypeId";
	private static final String OPERATE_ID = "operateId";
	private static final String OLT_FACTORY = "OltFactory";
	private static final String VLAN_ID = "vlanId";
	private static final String WAN_TYPE = "wanType";
	private static final String USER_NAME = "userName";

	/**
	 * 根据大唐入参对象构造发往工单模块的XML
	 *
	 * @param order
	 *            大唐传参对象
	 * @param isAccOper
	 *            接入工单标识("20_1"：接入开户工单 "20_3":接入销户工单 其他为正常业务工单的拼接)
	 * @return 发往工单模块的XML
	 */
	public static String parseToXML(Order order, String isAccOper) throws Exception
	{
		logger.debug("parseToXML==>方法开始{}|{}", order, isAccOper);
		String orderNo = order.getOrder_No();
		String orderType = order.getOrder_Type();
		String serviceCode = order.getService_code();
		String areaCode = order.getArea_code().trim();
		if ((StringUtil.IsEmpty(orderNo)) || (StringUtil.IsEmpty(areaCode)))
		{
			return "-6";
		}
		// 现网为e8-b,不传默认e8-b
		String deviceType = order.getDeviceType();
		if (!StringUtil.IsEmpty(deviceType) && "e8-b".equals(deviceType))
		{
			deviceType = "2";
		}
		else
		{
			deviceType = "1";
		}
		// 将地区编码转换成cityId
		String cityId = getCityId(areaCode);
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		Element root = document.addElement("root");
		// 工单ID
		root.addElement("cmdId").addText(new DateTimeUtil().getYYYYMMDDHHMMSS());
		// 鉴权账号
		root.addElement("authUser").addText("1");
		// 鉴权密码
		root.addElement("authPwd").addText("1");
		Element param = root.addElement("param");
		param.addElement("dealDate").addText(new DateTimeUtil().getYYYYMMDDHHMMSS());
		param.addElement("userType").addText("1");
		param.addElement("loid").addText(order.getDevice_ID());
		param.addElement("cityId").addText(cityId);
		// 接入工单
		if ("20_1".equals(isAccOper))
		{
			// 操作类型
			root.addElement(OPERATE_ID).addText("1");
			// 业务类型
			root.addElement(SERV_TYPE_ID).addText("20");
			param.addElement("officeId").addText("");
			param.addElement("areaId").addText("");
			param.addElement("accessStyle").addText("4");
			param.addElement("linkman").addText("");
			param.addElement("linkPhone").addText("");
			param.addElement("email").addText("");
			param.addElement("mobile").addText("");
			param.addElement("linkAddress").addText("");
			param.addElement("linkmanCredno").addText("");
			param.addElement("customerId").addText("");
			param.addElement("customerAccount").addText("");
			param.addElement("customerPwd").addText("");
			param.addElement("specId").addText("GPON(hgu21)");
			param.addElement("deviceType").addText(deviceType);
		}
		else if ("20_3".equals(isAccOper))
		{
			root.addElement(OPERATE_ID).addText("3");
			root.addElement(SERV_TYPE_ID).addText("20");
		}
		// 开户相关业务
		else if (("wband-Z".equals(orderType)) || ("iptv-Z".equals(orderType))
				|| ("voip-Z".equals(orderType)))
		{
			root.addElement(OPERATE_ID).addText("1");
			param.addElement("orderNo").addText(order.getOrder_No());
			if ("wband".equals(serviceCode))
			{
				root.addElement(SERV_TYPE_ID).addText("22");
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues))
				{
					return "-6";
				}
				// 桥接
				if (vector_argues.contains("wband_mode=0"))
				{
					String[] argues = vector_argues.split("\\^");
					Map<String,String> aragsMap = new HashMap<String, String>();
					for (String argue : argues)
					{
						String key = argue.split("=", -1)[0];
						String value = argue.split("=", -1)[1];
						aragsMap.put(key, value);
					}
					param.addElement(OLT_FACTORY)
							.addText(aragsMap.get(OLT_FACTORY));
					param.addElement(USER_NAME).addText(
							StringUtil.getStringValue(aragsMap.get("wband_name")));
					param.addElement("password").addText(
							StringUtil.getStringValue(aragsMap.get("wband_password")));
					param.addElement(WAN_TYPE).addText("1");
					// 华为olt下，此时，RMS给ont下发宽带业务时vlan固定是7
					if ("243".equals(aragsMap.get(OLT_FACTORY)))
					{
						param.addElement(VLAN_ID).addText("7");
					}
					else
					{
						param.addElement(VLAN_ID)
								.addText(aragsMap.get("wband_vlan"));
					}
					param.addElement("ipaddress").addText("");
					param.addElement("ipmask").addText("");
					param.addElement("gateway").addText("");
					param.addElement("ipdns").addText("");
					String X_CU_LanInterface = aragsMap.get("X_CU_LanInterface");
					X_CU_LanInterface = X_CU_LanInterface.replaceAll(
							"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.",
							"L");
					param.addElement("bindPort").addText(X_CU_LanInterface);
					param.addElement("speed").addText(
							StringUtil.getStringValue(aragsMap.get("wband_speed")));
				}
				// 路由
				else if (vector_argues.contains("wband_mode=1"))
				{
					String[] argues = vector_argues.split("\\^");
					Map<String,String> aragsMap = new HashMap<String, String>();
					for (String argue : argues)
					{
						String key = argue.split("=", -1)[0];
						String value = argue.split("=", -1)[1];
						aragsMap.put(key, value);
					}
					param.addElement(OLT_FACTORY)
							.addText(aragsMap.get(OLT_FACTORY));
					param.addElement(USER_NAME).addText(
							StringUtil.getStringValue(aragsMap.get("wband_name")));
					param.addElement("password").addText(
							StringUtil.getStringValue(aragsMap.get("wband_password")));
					param.addElement(WAN_TYPE).addText("2");
					// 华为olt下，此时，RMS给ont下发宽带业务时vlan固定是7
					if ("243".equals(aragsMap.get(OLT_FACTORY)))
					{
						param.addElement(VLAN_ID).addText("7");
					}
					else
					{
						param.addElement(VLAN_ID)
								.addText(aragsMap.get("wband_vlan"));
					}
					param.addElement("ipaddress").addText("");
					param.addElement("ipmask").addText("");
					param.addElement("gateway").addText("");
					param.addElement("ipdns").addText("");
					param.addElement("bindPort").addText("");
					param.addElement("speed").addText(
							StringUtil.getStringValue(aragsMap.get("wband_speed")));
				}
			}
			else if ("iptv".equals(serviceCode))
			{
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues))
				{
					return "-6";
				}
				// 带有browserURL1参数的为stb业务
				if (!vector_argues.contains("browserURL1"))
				{
					// 业务类型
					root.addElement(SERV_TYPE_ID).addText("21");
					// 路由
					if (vector_argues.contains("iptv_mode=1"))
					{
						String[] argues = vector_argues.split("\\^");
						Map<String,String> aragsMap = new HashMap<String, String>();
						for (String argue : argues)
						{
							String key = argue.split("=", -1)[0];
							String value = argue.split("=", -1)[1];
							aragsMap.put(key, value);
						}
						param.addElement("authenticationEnable").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationEnable")));
						param.addElement("authenticationMode").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationMode")));
						param.addElement("authenticationInfo").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationInfo")));
						param.addElement("authenticationInfoValue").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationInfoValue")));
						param.addElement(OLT_FACTORY)
								.addText(aragsMap.get(OLT_FACTORY));
						param.addElement(WAN_TYPE).addText("2");
						// 华为olt下，此时，RMS给ont下发iptv点播vlan固定是9
						if ("243".equals(aragsMap.get(OLT_FACTORY)))
						{
							param.addElement(VLAN_ID).addText("9");
						}
						else
						{
							param.addElement(VLAN_ID)
									.addText(aragsMap.get("iptv_vlan"));
						}
						param.addElement("multicastVlan")
								.addText(aragsMap.get("X_CU_MulticastVlan"));
						param.addElement(USER_NAME)
								.addText(aragsMap.get("iptv_name"));
						param.addElement("password")
								.addText(aragsMap.get("iptv_password"));
						param.addElement("ipaddress")
								.addText(aragsMap.get("destIPAddr1"));
						param.addElement("ipmask")
								.addText(aragsMap.get("destMask1"));
						param.addElement("iptvPort").addText("");
						param.addElement("iptvNum").addText("1");
						param.addElement("gateway").addText("");
						param.addElement("ipdns").addText("");
					}
					// 桥接
					else
					{
						String[] argues = vector_argues.split("\\^");
						Map<String,String> aragsMap = new HashMap<String, String>();
						for (String argue : argues)
						{
							String key = argue.split("=", -1)[0];
							String value = argue.split("=", -1)[1];
							aragsMap.put(key, value);
						}
						param.addElement("authenticationEnable").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationEnable")));
						param.addElement("authenticationMode").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationMode")));
						param.addElement("authenticationInfo").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationInfo")));
						param.addElement("authenticationInfoValue").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationInfoValue")));
						param.addElement(OLT_FACTORY)
								.addText(aragsMap.get(OLT_FACTORY));
						param.addElement(WAN_TYPE).addText("1");
						// 华为olt下，此时，RMS给ont下发iptv点播vlan固定是9
						if ("243".equals(aragsMap.get(OLT_FACTORY)))
						{
							param.addElement(VLAN_ID).addText("9");
						}
						else
						{
							param.addElement(VLAN_ID)
									.addText(aragsMap.get("iptv_vlan"));
						}
						param.addElement("multicastVlan")
								.addText(aragsMap.get("X_CU_MulticastVlan"));
						param.addElement(USER_NAME).addText("");// 专属宽带账号
						param.addElement("password").addText("");// 专属宽带密码
						String X_CU_LanInterface = aragsMap
								.get("X_CU_LanInterface");
						X_CU_LanInterface = X_CU_LanInterface.replaceAll(
								"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.",
								"L");
						param.addElement("iptvPort").addText(X_CU_LanInterface);
						param.addElement("ipaddress").addText("");
						param.addElement("ipmask").addText("");
						param.addElement("iptvNum").addText("1");
						param.addElement("gateway").addText("");
						param.addElement("ipdns").addText("");
					}
				}
				else
				{
					root.addElement(SERV_TYPE_ID).addText("25");
					String[] argues = vector_argues.split("\\^");
					Map<String,String> aragsMap = new HashMap<String, String>();
					for (String argue : argues)
					{
						String key = argue.split("=", -1)[0];
						String value = argue.split("=", -1)[1];
						aragsMap.put(key, value);
					}
					param.addElement("servaccount")
							.addText(aragsMap.get("userID"));
					param.addElement("servpwd").addText(aragsMap.get("userPwd"));
					param.addElement("browserurl1")
							.addText( aragsMap.get("browserURL1"));
					param.addElement("ntp1").addText(aragsMap.get("NTP1"));
					param.addElement("ntp2").addText(aragsMap.get("NTP2"));
					param.addElement("mac").addText(order.getDevice_ID());
					param.addElement("stbaccessStyle")
							.addText(aragsMap.get("iptv_mode"));
				}
			}
			else if ("voip".equals(serviceCode))
			{
				root.addElement(SERV_TYPE_ID).addText("15");
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues))
				{
					return "-6";
				}
				String[] argues = vector_argues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String, String>();
				for (String argue : argues)
				{
					String key = argue.split("=", -1)[0];
					String value = argue.split("=", -1)[1];
					aragsMap.put(key, value);
				}
				param.addElement(OLT_FACTORY)
						.addText(aragsMap.get(OLT_FACTORY));
				param.addElement("mgcIp").addText(aragsMap.get("voip_MGCIP"));
				param.addElement("mgcPort")
						.addText(aragsMap.get("voip_MGCPort"));
				param.addElement("standMgcIp")
						.addText(aragsMap.get("voip_standbyMGCIP"));
				param.addElement("standMgcPort")
						.addText(aragsMap.get("voip_standbyMGCPort"));
				if ("243".equals(aragsMap.get(OLT_FACTORY)))
				{
					param.addElement(VLAN_ID).addText("8");
				}
				else
				{
					// 语音业务vlan
					param.addElement(VLAN_ID)
							.addText(aragsMap.get("voip_vlan"));
				}
				// MG的域名
				param.addElement("regId")
						.addText(aragsMap.get("voip_MG_Domain"));
				// regId:终端标识：域名，建议配置LOID.com
				// 样例里面传的regId也是ip
				if ("IP".equalsIgnoreCase(aragsMap.get("voip_MIDformat")))
				{
					param.addElement("regIdType").addText("0");// 终端标识的类型
																// 0：IP地址，1：域名，2：设备名，当前为1
				}
				else if ("DomainName"
						.equalsIgnoreCase(aragsMap.get("voip_MIDformat")))
				{
					param.addElement("regIdType").addText("1");
				}
				param.addElement(WAN_TYPE).addText("3");// 不分桥接 路由
															// ，这里传值3代表静态ip使下面的三个参数可以接纳out的参数值
				param.addElement("ipaddress")
						.addText(aragsMap.get("WANIPAddress"));// ont的语音业务地址
				param.addElement("ipmask").addText(aragsMap.get("SubnetMask"));// ont的语音业务地址掩码
				param.addElement("gateway")
						.addText(aragsMap.get("WANDefaultGateway"));// ont语音业务网关
				param.addElement("ipdns").addText("");
				// param.addElement("voipPhone").addText("");//用户电话号码 加区号
				param.addElement("voipPhone")
						.addText(StringUtil.getStringValue(aragsMap.get("voipPhone")));// 用户电话号码
																						// 加区号
				param.addElement("voipPort").addText("A0");// 标示语音口
				param.addElement("eid").addText(aragsMap.get("voip_EID"));// 语音业务EID
				param.addElement("dscpMark").addText("0");// H248信令报文的DSCP值
				param.addElement("deviceName")
						.addText(aragsMap.get("WANIPAddress"));// deviceName
			}
			else
			{
				// service_code解析失败
				return "-4";
			}
		}
		else if (("wband-G".equals(orderType)) || ("iptv-G".equals(orderType))
				|| ("voip-G".equals(orderType)))
		{
			UserBindDAO dao = new UserBindDAO();
			root.addElement(OPERATE_ID).addText("2");
			param.addElement("orderNo").addText(order.getOrder_No());
			if ("wband".equals(serviceCode))
			{
				root.addElement(SERV_TYPE_ID).addText("22");
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues))
				{
					return "-6";
				}
				int wanType = 1;
				// 判断工单里是否传了上网方式，否：从数据库里查出原有工单的上网方式
				if (vector_argues.contains("wband_mode"))
				{
					if (vector_argues.contains("wband_mode=0"))
					{
						wanType = 1;
					}
					else if (vector_argues.contains("wband_mode=1"))
					{
						wanType = 2;
					}
				}
				else
				{
					Map<String, String> map = dao.getWanType(10, order.getDevice_ID());
					if (map != null && map.size() > 0)
					{
						wanType = StringUtil.getIntegerValue(
								StringUtil.getStringValue(map, "wan_type"));
					}
					else
					{
						logger.warn("用户[{}]无宽带业务,无法下发修改工单,请注意。", order.getDevice_ID());
						return "-6";
					}
				}
				// 桥接
				if (wanType == 1)
				{
					String[] argues = vector_argues.split("\\^");
					Map<String,String> aragsMap = new HashMap<String, String>();
					for (String argue : argues)
					{
						String key = argue.split("=", -1)[0];
						String value = argue.split("=", -1)[1];
						aragsMap.put(key, value);
					}
					param.addElement(USER_NAME).addText(
							StringUtil.getStringValue(aragsMap.get("wband_name")));
					param.addElement("password").addText(
							StringUtil.getStringValue(aragsMap.get("wband_password")));
					param.addElement(WAN_TYPE).addText("1");
					// 华为olt下，此时，RMS给ont下发宽带业务时vlan固定是7
					if ("243".equals(
							StringUtil.getStringValue(aragsMap.get(OLT_FACTORY))))
					{
						param.addElement(VLAN_ID).addText("7");
					}
					else
					{
						param.addElement(VLAN_ID).addText(
								StringUtil.getStringValue(aragsMap.get("wband_vlan")));
					}
					param.addElement("ipaddress").addText("");
					param.addElement("ipmask").addText("");
					param.addElement("gateway").addText("");
					param.addElement("ipdns").addText("");
					String X_CU_LanInterface = StringUtil
							.getStringValue(aragsMap.get("X_CU_LanInterface"));
					X_CU_LanInterface = X_CU_LanInterface.replaceAll(
							"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.",
							"L");
					param.addElement("bindPort").addText(X_CU_LanInterface);
					// param.addElement("speed").addText("");
					param.addElement("speed").addText(
							StringUtil.getStringValue(aragsMap.get("wband_speed")));
				}
				// 路由
				else if (wanType == 2)
				{
					String[] argues = vector_argues.split("\\^");
					Map<String,String> aragsMap = new HashMap<String, String>();
					for (String argue : argues)
					{
						String key = argue.split("=", -1)[0];
						String value = argue.split("=", -1)[1];
						aragsMap.put(key, value);
					}
					param.addElement(USER_NAME).addText(
							StringUtil.getStringValue(aragsMap.get("wband_name")));
					param.addElement("password").addText(
							StringUtil.getStringValue(aragsMap.get("wband_password")));
					param.addElement(WAN_TYPE).addText("2");
					// 华为olt下，此时，RMS给ont下发宽带业务时vlan固定是7
					if ("243".equals(
							StringUtil.getStringValue(aragsMap.get(OLT_FACTORY))))
					{
						param.addElement(VLAN_ID).addText("7");
					}
					else
					{
						param.addElement(VLAN_ID).addText(
								StringUtil.getStringValue(aragsMap.get("wband_vlan")));
					}
					param.addElement("ipaddress").addText("");
					param.addElement("ipmask").addText("");
					param.addElement("gateway").addText("");
					param.addElement("ipdns").addText("");
					param.addElement("speed").addText(
							StringUtil.getStringValue(aragsMap.get("wband_speed")));
				}
			}
			else if ("iptv".equals(serviceCode))
			{
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues))
				{
					return "-6";
				}
				// 带有browserURL1参数的为stb业务
				if (!vector_argues.contains("browserURL1"))
				{
					// 业务类型
					root.addElement(SERV_TYPE_ID).addText("21");
					int wanType = 1;
					// 判断工单里是否传了上网方式，否：从数据库里查出原有工单的上网方式
					if (vector_argues.contains("iptv_mode"))
					{
						if (vector_argues.contains("iptv_mode=1"))
						{
							wanType = 2;
						}
						else
						{
							wanType = 1;
						}
					}
					else
					{
						Map<String, String> map = dao.getWanType(11,
								order.getDevice_ID());
						if (map != null && map.size() > 0)
						{
							wanType = StringUtil.getIntegerValue(
									StringUtil.getStringValue(map, "wan_type"));
						}
						else
						{
							logger.warn("用户[{}]无IPTV业务,无法下发修改工单,请注意。",
									order.getDevice_ID());
							return "-6";
						}
					}
					// 路由
					if (wanType == 2)
					{
						String[] argues = vector_argues.split("\\^");
						Map aragsMap = new HashMap();
						for (String argue : argues)
						{
							String key = argue.split("=", -1)[0];
							String value = argue.split("=", -1)[1];
							aragsMap.put(key, value);
						}
						param.addElement("authenticationEnable").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationEnable")));
						param.addElement("authenticationMode").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationMode")));
						param.addElement("authenticationInfo").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationInfo")));
						param.addElement("authenticationInfoValue").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationInfoValue")));
						param.addElement(OLT_FACTORY).addText(
								StringUtil.getStringValue(aragsMap.get(OLT_FACTORY)));
						param.addElement(WAN_TYPE).addText("2");
						// 华为olt下，此时，RMS给ont下发iptv点播vlan固定是9
						if ("243".equals(
								StringUtil.getStringValue(aragsMap.get(OLT_FACTORY))))
						{
							param.addElement(VLAN_ID).addText("9");
						}
						else
						{
							param.addElement(VLAN_ID).addText(
									StringUtil.getStringValue(aragsMap.get("iptv_vlan")));
						}
						param.addElement("multicastVlan").addText(StringUtil
								.getStringValue(aragsMap.get("X_CU_MulticastVlan")));
						param.addElement(USER_NAME).addText(
								StringUtil.getStringValue(aragsMap.get("iptv_name")));
						param.addElement("password").addText(
								StringUtil.getStringValue(aragsMap.get("iptv_password")));
						param.addElement("ipaddress").addText(
								StringUtil.getStringValue(aragsMap.get("destIPAddr1")));
						param.addElement("ipmask").addText(
								StringUtil.getStringValue(aragsMap.get("destMask1")));
						param.addElement("iptvPort").addText("");
						param.addElement("iptvNum").addText("1");
						param.addElement("gateway").addText("");
						param.addElement("ipdns").addText("");
					}
					// 桥接
					else
					{
						String[] argues = vector_argues.split("\\^");
						Map<String,String> aragsMap = new HashMap<String, String>();
						for (String argue : argues)
						{
							String key = argue.split("=", -1)[0];
							String value = argue.split("=", -1)[1];
							aragsMap.put(key, value);
						}
						param.addElement("authenticationEnable").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationEnable")));
						param.addElement("authenticationMode").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationMode")));
						param.addElement("authenticationInfo").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationInfo")));
						param.addElement("authenticationInfoValue").addText(StringUtil
								.getStringValue(aragsMap.get("authenticationInfoValue")));
						param.addElement(OLT_FACTORY).addText(
								StringUtil.getStringValue(aragsMap.get(OLT_FACTORY)));
						param.addElement(WAN_TYPE).addText("1");
						// 华为olt下，此时，RMS给ont下发iptv点播vlan固定是9
						if ("243".equals(
								StringUtil.getStringValue(aragsMap.get(OLT_FACTORY))))
						{
							param.addElement(VLAN_ID).addText("9");
						}
						else
						{
							param.addElement(VLAN_ID).addText(
									StringUtil.getStringValue(aragsMap.get("iptv_vlan")));
						}
						param.addElement("multicastVlan").addText(StringUtil
								.getStringValue(aragsMap.get("X_CU_MulticastVlan")));
						param.addElement(USER_NAME).addText("");// 专属宽带账号
						param.addElement("password").addText("");// 专属宽带密码
						String X_CU_LanInterface = StringUtil
								.getStringValue(aragsMap.get("X_CU_LanInterface"));
						X_CU_LanInterface = X_CU_LanInterface.replaceAll(
								"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.",
								"L");
						param.addElement("iptvPort").addText(X_CU_LanInterface);
						param.addElement("ipaddress").addText("");
						param.addElement("ipmask").addText("");
						param.addElement("iptvNum").addText("1");
						param.addElement("gateway").addText("");
						param.addElement("ipdns").addText("");
					}
				}
			}
			else if ("voip".equals(serviceCode))
			{
				root.addElement(SERV_TYPE_ID).addText("15");
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues))
				{
					return "-6";
				}
				String[] argues = vector_argues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String,String>();
				for (String argue : argues)
				{
					String key = argue.split("=", -1)[0];
					String value = argue.split("=", -1)[1];
					aragsMap.put(key, value);
				}
				param.addElement(OLT_FACTORY)
						.addText(StringUtil.getStringValue(aragsMap.get(OLT_FACTORY)));
				param.addElement("mgcIp")
						.addText(StringUtil.getStringValue(aragsMap.get("voip_MGCIP")));
				param.addElement("mgcPort")
						.addText(StringUtil.getStringValue(aragsMap.get("voip_MGCPort")));
				param.addElement("standMgcIp").addText(
						StringUtil.getStringValue(aragsMap.get("voip_standbyMGCIP")));
				param.addElement("standMgcPort").addText(
						StringUtil.getStringValue(aragsMap.get("voip_standbyMGCPort")));
				if ("243".equals(StringUtil.getStringValue(aragsMap.get(OLT_FACTORY))))
				{
					param.addElement(VLAN_ID).addText("8");
				}
				else
				{
					param.addElement(VLAN_ID).addText(
							StringUtil.getStringValue(aragsMap.get("voip_vlan")));// 语音业务vlan
				}
				param.addElement("regId").addText(
						StringUtil.getStringValue(aragsMap.get("voip_MG_Domain")));// MG的域名
																					// regId:终端标识：域名，建议配置LOID.com
																					// 样例里面传的regId也是ip
				if ("IP".equalsIgnoreCase(
						StringUtil.getStringValue(aragsMap.get("voip_MIDformat"))))
				{
					param.addElement("regIdType").addText("0");// 终端标识的类型
																// 0：IP地址，1：域名，2：设备名，当前为1
				}
				else if ("DomainName".equalsIgnoreCase(
						StringUtil.getStringValue(aragsMap.get("voip_MIDformat"))))
				{
					param.addElement("regIdType").addText("1");
				}
				param.addElement(WAN_TYPE).addText("3");// 不分桥接 路由
															// ，这里传值3代表静态ip使下面的三个参数可以接纳out的参数值
				param.addElement("ipaddress")
						.addText(StringUtil.getStringValue(aragsMap.get("WANIPAddress")));// ont的语音业务地址
				param.addElement("ipmask")
						.addText(StringUtil.getStringValue(aragsMap.get("SubnetMask")));// ont的语音业务地址掩码
				param.addElement("gateway").addText(
						StringUtil.getStringValue(aragsMap.get("WANDefaultGateway")));// ont语音业务网关
				param.addElement("ipdns").addText("");
				// param.addElement("voipPhone").addText("");//用户电话号码 加区号
				param.addElement("voipPhone").addText(StringUtil.getStringValue(
						StringUtil.getStringValue(aragsMap.get("voipPhone"))));// 用户电话号码
																				// 加区号
				param.addElement("voipPort").addText("A0");// 标示语音口
				param.addElement("eid")
						.addText(StringUtil.getStringValue(aragsMap.get("voip_EID")));// 语音业务EID
				param.addElement("dscpMark").addText("0");// H248信令报文的DSCP值
				param.addElement("deviceName")
						.addText(StringUtil.getStringValue(aragsMap.get("WANIPAddress")));// deviceName
			}
			else
			{
				// service_code解析失败
				return "-4";
			}
		}
		else if (("wband-C".equals(orderType)) || ("iptv-C".equals(orderType))
				|| ("voip-C".equals(orderType)))
		{
			root.addElement(OPERATE_ID).addText("3");
			if ("wband".equals(serviceCode))
			{
				root.addElement(SERV_TYPE_ID).addText("22");
				String vectorArgues = order.getVector_argues();
				if (StringUtil.IsEmpty(vectorArgues))
				{
					return "-6";
				}
				String[] argues = vectorArgues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String,String>();
				for (String argue : argues)
				{
					String key = argue.split("=", -1)[0];
					String value = argue.split("=", -1)[1];
					aragsMap.put(key, value);
				}
				param.addElement(USER_NAME).addText("");
				param.addElement(WAN_TYPE)
						.addText(StringUtil.getStringValue(Integer.valueOf(
								Integer.valueOf(aragsMap.get("wband_mode"))
										.intValue() + 1)));
			}
			else if ("iptv".equals(serviceCode))
			{
				String vector_argues = order.getVector_argues();
				// stb的值为空
				if (StringUtil.IsEmpty(vector_argues))
				{
					root.addElement(SERV_TYPE_ID).addText("25");
					param.addElement("servaccount").addText("");
				}
				// iptv销户
				else
				{
					root.addElement(SERV_TYPE_ID).addText("21");
					String[] argues = vector_argues.split("\\^");
					Map<String,String> aragsMap = new HashMap<String,String>();
					for (String argue : argues)
					{
						String key = argue.split("=", -1)[0];
						String value = argue.split("=", -1)[1];
						aragsMap.put(key, value);
					}
					param.addElement(USER_NAME).addText("");
					param.addElement(WAN_TYPE)
							.addText(StringUtil.getStringValue(Integer.valueOf(
									Integer.valueOf(aragsMap.get("iptv_mode"))
											.intValue() + 1)));
				}
			}
			else if ("voip".equals(serviceCode))
			{
				root.addElement(SERV_TYPE_ID).addText("15");
				param.addElement(USER_NAME).addText("");
			}
			else
			{
				// service_code解析失败
				return "-4";
			}
		}
		else if ("wband-X".equals(orderType))// 宽带速率变更
		{
			root.addElement(OPERATE_ID).addText("5");
			if ("wband".equals(serviceCode))
			{
				root.addElement(SERV_TYPE_ID).addText("22");
				String vectorArgues = order.getVector_argues();
				if (StringUtil.IsEmpty(vectorArgues))
				{
					return "-6";
				}
				String[] argues = vectorArgues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String,String>();
				for (String argue : argues)
				{
					String key = argue.split("=", -1)[0];
					String value = argue.split("=", -1)[1];
					aragsMap.put(key, value);
				}
				param.addElement(USER_NAME)
						.addText(StringUtil.getStringValue(aragsMap.get("wband_name")));
				param.addElement("speed")
						.addText(StringUtil.getStringValue(aragsMap.get("wband_speed")));
			}
			else
			{
				// service_code解析失败
				return "-4";
			}
		}
		else if ("cpe-G".equals(orderType))// 光猫换机
		{
			root.addElement(OPERATE_ID).addText("4");
			if ("cpe".equals(serviceCode))
			{
				root.addElement(SERV_TYPE_ID).addText("20");
				String vectorArgues = order.getVector_argues();
				if (StringUtil.IsEmpty(vectorArgues))
				{
					return "-6";
				}
				String[] argues = vectorArgues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String,String>();
				for (String argue : argues)
				{
					String key = argue.split("=", -1)[0];
					String value = argue.split("=", -1)[1];
					aragsMap.put(key, value);
				}
				param.addElement("oldLoid").addText(
						StringUtil.getStringValue(aragsMap.get("old_device_ID")));
			}
			else
			{
				// service_code解析失败
				return "-4";
			}
		}
		else
		{
			// order_Type 解析失败
			return "-3";
		}
		return document.asXML();
	}

	/**
	 * 根据属地名称获取属地id
	 *
	 * @param area_code
	 *            属地名称
	 * @return 属地id，没查询到默认00
	 */
	private static String getCityId(String area_code)
	{
		if (StringUtil.IsEmpty(area_code))
		{
			return "00";
		}
		for (Map.Entry entry : Global.G_CityId_CityName_Map.entrySet())
		{
			if (area_code.equals(entry.getValue()))
			{
				return (String) entry.getKey();
			}
		}
		return "00";
	}
}
