
package com.linkage.itms.os.bio;

import com.huawei.base.model.Order;
import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class OperationDistributeBIOJllt
{
	private static Logger logger = LoggerFactory.getLogger(OperationDistributeBIOJllt.class);

	/**
	 * 根据大唐入参对象构造发往工单模块的XML
	 * @param order 大唐传参对象
	 * @param isAccOper 接入工单标识("20_1"：接入开户工单 "20_3":接入销户工单 其他为正常业务工单的拼接)
	 * @return 发往工单模块的XML
	 */
	public static String parseToXML(Order order, String isAccOper) throws Exception
	{
		logger.debug("parseToXML==>方法开始{}", new Object[] { order, isAccOper });
		String order_no = order.getOrder_No();
		String order_type = order.getOrder_Type();
		String service_code = order.getService_code();
		String area_code = order.getArea_code().trim();
		//由于voip-X 语音IP变更工单 需要统计接受成功 失败，故此直接将order转换成xml字符串 所有判断放置4WS模块处理。
		if ( !"voip-X".equals(order_type) && ((StringUtil.IsEmpty(order_no)) || (StringUtil.IsEmpty(area_code)))){
			return "-6";
		}
		// 现网为e8-b,不传默认e8-b
		String deviceType = order.getDeviceType();
		if (!StringUtil.IsEmpty(deviceType) && "e8-c".equals(deviceType)){
			deviceType = "2";
		}else{
			deviceType = "1";
		}
		// 11101:省公司 0：所有本地网(其他)
		if("11101".equals(area_code)){
			area_code = "00";
		}
		//cityId = getCityId(area_code);
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
		if (!order.getAd_userid().isEmpty()){
			param.addElement("loid").addText(order.getAd_userid());
		}else{
			//param.addElement("loid").addText(order.getDevice_ID());
			param.addElement("loid").addText("");
		}
		param.addElement("cityId").addText(area_code);

		// 接入工单
		if ("20_1".equals(isAccOper)||"cpe-Z".equals(order_type))
		{
			// 操作类型
			root.addElement("operateId").addText("1");
			// 业务类型
			root.addElement("servTypeId").addText("20");
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
		else if ("cpe-C".equals(order_type))
		{
			root.addElement("operateId").addText("3");
			root.addElement("servTypeId").addText("20");
		}
		// 开户相关业务
		else if (("wband-Z".equals(order_type)) || ("iptv-Z".equals(order_type))
				|| ("voip-Z".equals(order_type)))
		{
			root.addElement("operateId").addText("1");
			param.addElement("orderNo").addText(order.getOrder_No());
			if ("wband".equals(service_code))
			{
				root.addElement("servTypeId").addText("22");
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues)){
					return "-6";
				}

				String[] argues = vector_argues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String,String>();
				for (String argue : argues){
					aragsMap.put(argue.split("=",-1)[0], argue.split("=",-1)[1]);
				}

				// 桥接
				if (vector_argues.contains("wband_mode=0")){
					param.addElement("userName").addText(aragsMap.get("wband_name"));
					param.addElement("password").addText(aragsMap.get("wband_password"));
					param.addElement("wanType").addText("1");
					param.addElement("vlanId").addText(aragsMap.get("wband_vlan"));
					if(!StringUtil.IsEmpty(aragsMap.get("Speed"))){
						param.addElement("speed").addText(aragsMap.get("Speed"));
					}
				}
				// 路由
				else if (vector_argues.contains("wband_mode=1"))
				{
					param.addElement("userName").addText(aragsMap.get("wband_name"));
					param.addElement("password").addText(aragsMap.get("wband_password"));
					param.addElement("wanType").addText("2");
					param.addElement("vlanId").addText(aragsMap.get("wband_vlan"));
					if(!StringUtil.IsEmpty(aragsMap.get("Speed"))){
						param.addElement("speed").addText(aragsMap.get("Speed"));
					}
				}
			}
			else if ("iptv".equals(service_code))
			{
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues)){
					return "-6";
				}
				// 业务类型
				root.addElement("servTypeId").addText("21");
				String[] argues = vector_argues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String,String>();
				for (String argue : argues){
					aragsMap.put(argue.split("=", -1)[0], argue.split("=", -1)[1]);
				}
				// 吉林vlanId 默认45
				param.addElement("vlanId").addText("45");
				param.addElement("wanType").addText("1");// iptv默认桥接
				param.addElement("orderNo").addText(order_no);
				param.addElement("multicastVlan").addText(aragsMap.get("M_Vlan"));
			}
			else if ("voip".equals(service_code))
			{
				root.addElement("servTypeId").addText("15");
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues)){
					return "-6";
				}
				String[] argues = vector_argues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String,String>();
				for (String argue : argues){
					aragsMap.put(argue.split("=", -1)[0], argue.split("=", -1)[1]);
				}

				param.addElement("voipPort").addText(aragsMap.get("voip_TID"));
				param.addElement("vlanId").addText(aragsMap.get("voip_vlan"));// 语音业务vlan
				param.addElement("ipaddress").addText(aragsMap.get("WANIPAddress"));// ont的语音业务地址
				param.addElement("ipmask").addText(aragsMap.get("WANSubnetMask"));// ont的语音业务地址掩码
				param.addElement("gateway").addText(aragsMap.get("WANDefaultGateway"));// ont语音业务网关

				// 语音业务有静态ip相关信息时设置上网方式为3，其余默认指定DHCP（4）
				if (!StringUtil.IsEmpty(aragsMap.get("WANIPAddress"))
						&& !StringUtil.IsEmpty(aragsMap.get("WANSubnetMask"))
						&& !StringUtil.IsEmpty(aragsMap.get("WANDefaultGateway")))
				{
					param.addElement("wanType").addText("3");
				}else{
					param.addElement("wanType").addText("4");
				}

				param.addElement("mgcIp").addText((String) aragsMap.get("voip_MGCIP"));
				param.addElement("mgcPort").addText(StringUtil.IsEmpty((String) aragsMap.get("voip_MGCPort"))?"2944":(String) aragsMap.get("voip_MGCPort"));
				param.addElement("standMgcIp").addText(StringUtil.IsEmpty((String) aragsMap.get("voip_standbyMGCIP"))?(String) aragsMap.get("voip_MGCIP"):(String) aragsMap.get("voip_standbyMGCIP"));
				param.addElement("standMgcPort").addText(StringUtil.IsEmpty((String) aragsMap.get("voip_standbyMGCPort"))?"2944":(String) aragsMap.get("voip_standbyMGCPort"));
				param.addElement("regId").addText((String)aragsMap.get("voip_Domain"));

				if ("IP".equalsIgnoreCase((String) aragsMap.get("voip_MIDFormat"))){
					param.addElement("regIdType").addText("0");// 终端标识的类型
																// 0：IP地址，1：域名，2：设备名，当前为1
				}
				else if ("DomainName".equalsIgnoreCase((String) aragsMap.get("voip_MIDFormat"))){
					param.addElement("regIdType").addText("1");
				}

				param.addElement("eid").addText("");// 语音业务EID？
				param.addElement("prefix").addText((String) aragsMap.get("voip_Prefix"));
				param.addElement("dscpMark").addText("0");//H248信令报文的DSCP值
			}
			else
			{
				// service_code解析失败
				return "-4";
			}
		}
		else if (("wband-C".equals(order_type)) || ("iptv-C".equals(order_type))
				|| ("voip-C".equals(order_type)))
		{
			root.addElement("operateId").addText("3");
			if ("wband".equals(service_code))
			{
				root.addElement("servTypeId").addText("22");
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues)){
					return "-6";
				}
				String[] argues = vector_argues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String,String>();
				for (String argue : argues){
					aragsMap.put(argue.split("=", -1)[0], argue.split("=", -1)[1]);
				}
				param.addElement("userName").addText("");
				param.addElement("wanType").addText(StringUtil.getStringValue(
						Integer.valueOf(aragsMap.get("wband_mode")).intValue() + 1));
				param.addElement("vlanId").addText(aragsMap.get("wband_vlan"));
			}
			else if ("iptv".equals(service_code))
			{
				String vector_argues = order.getVector_argues();
				root.addElement("servTypeId").addText("21");
				String[] argues = vector_argues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String,String>();
				for (String argue : argues){
					aragsMap.put(argue.split("=", -1)[0], argue.split("=", -1)[1]);
				}
				// 吉林vlanId 默认45
				param.addElement("vlanId").addText("45");
				param.addElement("multicastVlan").addText(aragsMap.get("M_Vlan"));
				param.addElement("wanType").addText("1");
			}
			else if ("voip".equals(service_code))
			{
				String vector_argues = order.getVector_argues();
				String[] argues = vector_argues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String,String>();
				for (String argue : argues){
					aragsMap.put(argue.split("=",-1)[0], argue.split("=",-1)[1]);
				}

				root.addElement("servTypeId").addText("15");
				param.addElement("voipPort").addText("A0");
				param.addElement("vlanId").addText(StringUtil.getStringValue(aragsMap, "voip_vlan",""));// aragsMap.get("voip_vlan")语音业务vlan
				param.addElement("ipaddress").addText(StringUtil.getStringValue(aragsMap,"WANIPAddress",""));// aragsMap.get("WANIPAddress")ont的语音业务地址
				param.addElement("ipmask").addText(StringUtil.getStringValue(aragsMap,"WANSubnetMask",""));//  aragsMap.get("WANSubnetMask")ont的语音业务地址掩码
				param.addElement("gateway").addText(StringUtil.getStringValue(aragsMap,"WANDefaultGateway",""));//  aragsMap.get("WANDefaultGateway")ont语音业务网关

				// 语音业务有静态ip相关信息时设置上网方式为3，其余默认指定DHCP（4）
				if (!StringUtil.IsEmpty(aragsMap.get("WANIPAddress"))
						&& !StringUtil.IsEmpty(aragsMap.get("WANSubnetMask"))
						&& !StringUtil.IsEmpty(aragsMap.get("WANDefaultGateway")))
				{
					param.addElement("wanType").addText("3");
				}else{
					param.addElement("wanType").addText("4");
				}

				param.addElement("mgcIp").addText((String) aragsMap.get("voip_MGCIP"));
				param.addElement("mgcPort").addText(StringUtil.IsEmpty((String) aragsMap.get("voip_MGCPort"))?"2944":(String) aragsMap.get("voip_MGCPort"));
				/*param.addElement("standMgcIp").addText(StringUtil.IsEmpty((String) aragsMap.get("voip_standbyMGCIP"))?(String) aragsMap.get("voip_MGCIP"):(String) aragsMap.get("voip_standbyMGCIP"));
				param.addElement("standMgcPort").addText(StringUtil.IsEmpty((String) aragsMap.get("voip_standbyMGCPort"))?"2944":(String) aragsMap.get("voip_standbyMGCPort"));
				param.addElement("regId").addText((String)aragsMap.get("voip_Domain"));
				
				if ("IP".equalsIgnoreCase((String) aragsMap.get("voip_MIDformat"))){
					param.addElement("regIdType").addText("0");// 终端标识的类型
																// 0：IP地址，1：域名，2：设备名，当前为1
				}
				else if ("DomainName".equalsIgnoreCase((String) aragsMap.get("voip_MIDformat"))){
					param.addElement("regIdType").addText("1");
				}
				
				param.addElement("eid").addText((String) aragsMap.get("voip_TID"));// 语音业务EID？
				param.addElement("prefix").addText((String) aragsMap.get("voip_Prefix"));*/
			}
			else
			{
				// service_code解析失败
				return "-4";
			}
		}
		else if ("wband-X".equals(order_type) || "wband-S".equals(order_type))// 宽带速率变更
		{
			root.addElement("operateId").addText("5");
			if ("wband".equals(service_code))
			{
				root.addElement("servTypeId").addText("22");
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues)){
					return "-6";
				}
				String[] argues = vector_argues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String,String>();
				for (String argue : argues){
					aragsMap.put(argue.split("=",-1)[0], argue.split("=", -1)[1]);
				}
				param.addElement("userName").addText(aragsMap.get("wband_name"));
				if("wband-S".equals(order_type)){
					//吉林联通工单接口指定格式
					if(!StringUtil.IsEmpty(aragsMap.get("Speed"))){
						param.addElement("speed").addText(aragsMap.get("Speed"));
					}
				}else{
					param.addElement("speed").addText(aragsMap.get("wband_speed"));
				}
			}
			else
			{
				// service_code解析失败
				return "-4";
			}
		}
		else if("voip-X".equals(order_type))   //修改语音IP
		{
			root.addElement("operateId").addText("4");
			root.addElement("servTypeId").addText("15");
			String vector_argues = order.getVector_argues();
			//WANIPAddress=11.87.199.12^WANDefaultGateway=11.87.199.1^WANSubnetMask=255.255.255.0^voip_Domain=11.87.199.12
			if (StringUtil.IsEmpty(vector_argues)){
				param.addElement("regId").addText("");
				param.addElement("ipaddress").addText("");
				param.addElement("ipmask").addText("");// ont的语音业务地址掩码
				param.addElement("gateway").addText("");// ont语音业务网关
			}
			else
            {
				String[] argues = vector_argues.split("\\^");
				Map<String,String> aragsMap = new HashMap<String,String>();
				for (String argue : argues){
					aragsMap.put(argue.split("=",-1)[0], argue.split("=",-1)[1]);
				}
				param.addElement("regId").addText(StringUtil.getStringValue(aragsMap.get("voip_Domain")));
				param.addElement("ipaddress").addText(StringUtil.getStringValue(aragsMap.get("WANIPAddress")));// ont的语音业务地址
				param.addElement("ipmask").addText(StringUtil.getStringValue(aragsMap.get("WANSubnetMask")));// ont的语音业务地址掩码
				param.addElement("gateway").addText(StringUtil.getStringValue(aragsMap.get("WANDefaultGateway")));// ont语音业务网关

            }
			param.addElement("orderNo").addText(order_no);
			param.addElement("cityId").addText(area_code);
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
	 * @param area_code 属地名称
	 * @return 属地id，没查询到默认00
	 */
	@SuppressWarnings({ "unused", "rawtypes" })
	private static String getCityId(String area_code)
	{
		if (StringUtil.IsEmpty(area_code)){
			return "00";
		}
		for (Map.Entry entry : Global.G_CityId_CityName_Map.entrySet())
		{
			if (area_code.equals(entry.getValue())){
				return (String) entry.getKey();
			}
		}
		return "00";
	}
}
