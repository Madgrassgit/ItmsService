
package com.linkage.itms.os.bio;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.obj.sxlt.Order;

/**
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-8-16
 * @category com.linkage.itms.os.bio
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 注：返回码必须有.结尾，否则大唐服开接收后解析会出现异常，导致卡单。
 */
public class OperationDistributeBIOSxlt
{

	private static Logger logger = LoggerFactory.getLogger(OperationDistributeBIOSxlt.class);
	private static int MODE_ALLROUTE = 2;//大唐新加全路由模式-2（不下发端口），0-桥接，1-路由
	private static int MODE_DATANG_ROUTE = 1;//大唐路由
	private static int MODE_ROUTE = 2;
	private static int BIND_PORT_0 = 0;//传0则表示不下端口
	private String errorDesc = ".";
	private String result = "1";
	private static final String ERROR_MSG = "vector_argues为空.";
	
	
	public static void main(String[] args)
	{
		String voipAuthName = "+863585732789@si.ims.chinaunicom.cn";
		if(voipAuthName.indexOf("+86") > -1 && voipAuthName.indexOf("@") > -1)
		{
			String voipPhone = voipAuthName.replaceAll("\\+86", "0");
			voipPhone = voipPhone.substring(0, voipPhone.indexOf("@"));
			System.out.println(voipPhone);
		}
	}
	
	public Order parseXMLToOrder(String xml){
		SAXReader reader = new SAXReader();
		Document document = null;
		Order order = new Order();
		String[] argues = new String[]{"area_code", "deviceType", "device_ID", "order_No", "order_Type", "order_kind", "service_code", "vector_argues","user_address","user_name","contact_person"};
		String name = "";
		try {
			document = reader.read(new StringReader(xml));
			
			Element root = document.getRootElement();
			Element interfacemsg = root.element("interfacemsg");
			Element cust_info = interfacemsg.element("cust_info");
			
			Class<?> userClass = Class.forName("com.linkage.itms.obj.sxlt.Order");
			Method me = null;
			
			for(int i=0;i<argues.length;i++){
				name = argues[i];
				me = userClass.getMethod("set" + name.substring(0,1).toUpperCase() + name.substring(1), String.class);
				if("deviceType".equals(name)){
					name = "devicetype";
				}
				me.invoke(order,cust_info.elementTextTrim(name));
			}
			return order;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[{}]数据转换异常："+e.getMessage(),order.getDevice_ID());
			errorDesc = "数据转换异常.";
			return null;
		}
	}
	
	
	
	public Order[] parseMultiXML(Order order)
		    throws Exception
	{
	    String order_type = order.getOrder_Type();
	    String service_code = order.getService_code();
	    String[] service_codeArr = service_code.split("\\^");
	    String[] order_typeArr = order_type.split("\\^");
	    
	    if(service_codeArr.length != order_typeArr.length){
			errorDesc = "service_code与order_type数量不符.";
	    	return null;
	    }
	    
	    for(int i=0;i<service_codeArr.length;i++){
	    	if(!order_type.contains(service_codeArr[i])){
				errorDesc = "service_code与order_type不匹配.";
	    		return  null;
	    	}
	    }
	    
	    int len = service_codeArr.length;
	    for(int i=0;i<len;i++){
	    	if("cpe-Z".equals(order_typeArr[i])){
	    		len --;
	    	}
	    }
	    if(len == 0){
	    	return null;
	    }
	    
	    Order[] orders = new Order[service_codeArr.length];
	    for(int i=0;i<len;i++){
	    	if("cpe-Z".equals(order_typeArr[i])){
	    		continue;
	    	}
	    	orders[i] = (Order)order.clone();
	    	orders[i].setService_code(order_typeArr[i].substring(0, order_typeArr[i].indexOf("-")));
	    	orders[i].setOrder_Type(order_typeArr[i]);
	    }
	    
	    return orders;
	}
	
	/**
	 * 根据大唐入参对象构造发往工单模块的XML
	 * 
	 * @param order
	 *            大唐传参对象
	 * @param isAccOper
	 *            接入工单标识("20_1"：接入开户工单 "20_3":接入销户工单 其他为正常业务工单的拼接)
	 *        flag：true，发送cpe-C光猫，false，发送cpe-C机顶盒
	 * @return 发往工单模块的XML
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String parseToXML(Order order, String isAccOper, String flag) throws Exception
	{
		logger.warn("parseToXML==>方法开始{}", new Object[] { order, isAccOper });
		String order_no = order.getOrder_No(); // 工单序号 例:0755285608080001
		String order_type = order.getOrder_Type(); // 业务类型 例:wband-Z
		String service_code = order.getService_code(); // 业务代码
		String area_code = order.getArea_code().trim(); // 区域代码
		String deviceType = order.getDeviceType(); // 设备类型 e8-b/e8-c
		if ((StringUtil.IsEmpty(order_no)) || (StringUtil.IsEmpty(deviceType)))
		{
			logger.warn("[{}]order_no或deviceType为空", order.getDevice_ID());
			errorDesc = "order_no或deviceType为空.";
			return "-6";
		}
		if ("e8-b".equals(deviceType))
		{
			deviceType = "1";
		}
		else
		{
			deviceType = "2";
		}
		// 将地区编码转换成cityId
		//String cityId = "00";
		//cityId = getCityId(area_code);
		String cityId = area_code;
		if(!Global.G_CityId_CityName_Map.containsKey(area_code)){
			cityId = "00";
		}
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
		if(!order.getAd_userid().isEmpty())
		{
			param.addElement("loid").addText(order.getAd_userid());
		}
		else
		{
			param.addElement("loid").addText(order.getDevice_ID());
		}
		param.addElement("cityId").addText(cityId);
		// 用户资料接入工单
		if ("20_1".equals(isAccOper))
		{
			// 操作类型
			root.addElement("operateId").addText("1");
			// 业务类型
			root.addElement("servTypeId").addText("20");
			param.addElement("officeId").addText("");
			param.addElement("areaId").addText(area_code);
			param.addElement("accessStyle").addText("4");
			param.addElement("linkman").addText(order.getUser_name());
			param.addElement("linkPhone").addText("");
			param.addElement("email").addText("");
			param.addElement("mobile").addText("");
			param.addElement("linkAddress").addText(order.getUser_address());
			param.addElement("linkmanCredno").addText("");
			param.addElement("customerId").addText("");
			param.addElement("customerAccount").addText("");
			param.addElement("customerPwd").addText("");
			param.addElement("specId").addText("GPON(hgu21)");
			param.addElement("deviceType").addText(deviceType);
		}
		//换机
		else if ("cpe-G".equals(order_type))
		{
			// 操作类型
			root.addElement("operateId").addText("2");
			String vector_argues = order.getVector_argues();
			if (StringUtil.IsEmpty(vector_argues))
			{
				errorDesc = ERROR_MSG;
				return "-6";
			}
			String[] argues = vector_argues.split("\\^");
			Map aragsMap = new HashMap();
			for (String argue : argues)
			{
				String key = argue.split("=", -1)[0];
				String value = argue.split("=", -1)[1];
				aragsMap.put(key, value);
			}
			param.addElement("oldloid").addText(StringUtil.getStringValue(aragsMap.get("cpe_OLDID")));
			
			if("1".equals(flag)){
				// 业务类型
				root.addElement("servTypeId").addText("20");
				param.addElement("specId").addText("GPON(hgu21)");
				param.addElement("deviceType").addText(deviceType);
			}
			else if("2".equals(flag)){
				root.addElement("servTypeId").addText("25");
			}
		}
		//销户
		else if ("cpe-C".equals(order_type))
		{
			if("2".equals(flag)){
				root.addElement("servTypeId").addText("25");
				root.addElement("operateId").addText("3");
			}
			else if("1".equals(flag)){
				root.addElement("operateId").addText("3");
				root.addElement("servTypeId").addText("20");
			}
		}
		//sip语音业务修改
		else if("voip-X".equals(order_type))
		{
			root.addElement("operateId").addText("2");
			root.addElement("servTypeId").addText("14");
			String vector_argues = order.getVector_argues();
			if (StringUtil.IsEmpty(vector_argues))
			{
				errorDesc = ERROR_MSG;
				return "-6";
			}
			String[] argues = vector_argues.split("\\^");
			Map aragsMap = new HashMap();
			for (String argue : argues)
			{
				String key = argue.split("=", -1)[0];
				String value = argue.split("=", -1)[1];
				aragsMap.put(key, value);
			}
			param.addElement("voipUri").addText(
					(String) aragsMap.get("voip_URI"));
			param.addElement("voipOldUsername").addText(
					(String) aragsMap.get("voip_oldAuthName"));
			param.addElement("voipPort").addText(lanChange(aragsMap, "voip_PortNum", "V"));
			param.addElement("voipUsername").addText(
					(String) aragsMap.get("voip_AuthName"));
			param.addElement("voipPwd").addText(
					(String) aragsMap.get("voip_AuthPass"));
			param.addElement("protocol").addText(
					(String) aragsMap.get("voip_mode"));// VOIP协议类型
		}
		//宽带停机复机
		else if (("wband-T".equals(order_type)) || ("wband-U".equals(order_type)))
		{
			if("wband-T".equals(order_type))
			{
				root.addElement("operateId").addText("4");
			}
			else
			{
				root.addElement("operateId").addText("5");
			}
			root.addElement("servTypeId").addText("22");
			String vector_argues = order.getVector_argues();
			if (StringUtil.IsEmpty(vector_argues))
			{
				errorDesc = ERROR_MSG;
				return "-6";
			}
			String[] argues = vector_argues.split("\\^");
			Map aragsMap = new HashMap();
			for (String argue : argues)
			{
				String key = argue.split("=", -1)[0];
				String value = argue.split("=", -1)[1];
				aragsMap.put(key, value);
			}
			param.addElement("userName").addText(
					StringUtil.getStringValue(aragsMap.get("wband_name")));
		}
		// 开户相关业务
		else if (("wband-Z".equals(order_type)) || ("iptv-Z".equals(order_type))
				|| ("voip-Z".equals(order_type)) || ("wband-X".equals(order_type)))
		{
			root.addElement("operateId").addText("1");
			param.addElement("orderNo").addText(order.getOrder_No());
			if ("wband".equals(service_code))
			{
				root.addElement("servTypeId").addText("22");
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues))
				{
					errorDesc = ERROR_MSG;
					return "-6";
				}
				String[] argues = vector_argues.split("\\^");
				Map aragsMap = new HashMap();
				for (String argue : argues)
				{
					String key = argue.split("=", -1)[0];
					String value = argue.split("=", -1)[1];
					aragsMap.put(key, value);
				}
				param.addElement("oltFactory").addText(
						(String) aragsMap.get("OltFactory"));
				String factory = (String) aragsMap.get("OltFactory");
				param.addElement("userName").addText(
						StringUtil.getStringValue(aragsMap.get("wband_name")));
				param.addElement("password").addText(
						StringUtil.getStringValue(aragsMap.get("wband_password")));
				
				int mode = StringUtil.getIntegerValue((String) aragsMap.get("wband_mode"));
				if(mode != 0 && mode != 1 && mode != 2 ){
					errorDesc = "宽带上网模式参数：wband_mode 只能为0,1,2.";
					return "-6";
				}
				// 山西增加是否全路由模式字段 “allRoute”,默认为空，全路由模式为“1”
				if(mode == 2){
					param.addElement("allRoute").addText("1");
				}
				//全路由转换成路由（端口设置为空）
				param.addElement("wanType").addText(mode == MODE_ALLROUTE?StringUtil.getStringValue(MODE_ROUTE):
						StringUtil.getStringValue(mode + 1));
				// 将业务lan口入参"1,2", 转为"L1,L2"格式
				String wbandPortNum = lanChange(aragsMap, "wband_PortNum", "L");
				//桥接、路由、全路由(除开华为的全路由)校验下端口，局方要求必须传，全路由不下发
				if(StringUtil.IsEmpty(wbandPortNum)){
					if(!("HW".equalsIgnoreCase(factory)||"HUAWEI".equalsIgnoreCase(factory))&&(mode == MODE_ALLROUTE)){
						//非华为olt，而且业务为全路由才能为空
					}
					else{
						errorDesc = "宽带开通端口不能为空.";
						return "-6";
					}
				}
				
				param.addElement("bindPort").addText(mode == MODE_ALLROUTE?"":wbandPortNum);
				
				if(("HW".equalsIgnoreCase(factory)||"HUAWEI".equalsIgnoreCase(factory))&&(mode == MODE_ALLROUTE)){
					param.addElement("vlanId").addText("2");
				}
				else{
					param.addElement("vlanId").addText(getVlan((String) aragsMap.get("wband_PortNum"),StringUtil.getIntegerValue(aragsMap.get("wband_PortNum")), StringUtil.getStringValue(aragsMap.get("wband_vlan")), factory, "wband"));
				}
				param.addElement("orderNo").addText(order_no);
			}
			else if ("iptv".equals(service_code))
			{
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues))
				{
					errorDesc = ERROR_MSG;
					return "-6";
				}
				//带有userID/userIDPwd参数的为stb业务
		        if ("stb".equals(order.getDeviceType()) || (!vector_argues.contains("userID") && !vector_argues.contains("userIDPwd"))){
					// 业务类型
					root.addElement("servTypeId").addText("21");
					String[] argues = vector_argues.split("\\^");
					Map aragsMap = new HashMap();
					for (String argue : argues)
					{
						String key = argue.split("=", -1)[0];
						String value = argue.split("=", -1)[1];
						aragsMap.put(key, value);
					}
					param.addElement("oltFactory").addText(
							(String) aragsMap.get("OltFactory"));
					String factory = (String) aragsMap.get("OltFactory");
					String wbandPortNum = lanChange(aragsMap, "iptv_PortNum", "L");
					param.addElement("vlanId").addText(getVlan((String) aragsMap.get("iptv_PortNum"),StringUtil.getIntegerValue(aragsMap.get("iptv_PortNum")), StringUtil.getStringValue(aragsMap.get("iptv_vlan")), factory, "iptv"));
					param.addElement("wanType").addText("1");// iptv默认桥接
					// 将业务lan口入参"1,2", 转为"L1,L2"格式
					
					
					param.addElement("bindPort").addText(wbandPortNum);
					param.addElement("orderNo").addText(order_no);
		        }
		        else{
		        	root.addElement("servTypeId").addText("25");

		            String[] argues = vector_argues.split("\\^");
		            Map aragsMap = new HashMap();
		            for (String argue : argues) {
		              String key = argue.split("=", -1)[0];
		              String value = argue.split("=", -1)[1];
		              aragsMap.put(key, value);
		            }

		            param.addElement("servaccount").addText((String)aragsMap.get("userID"));
		            param.addElement("servpwd").addText((String)aragsMap.get("userIDPwd"));
		            param.addElement("mac").addText(order.getDevice_ID());
		            param.addElement("stbaccessStyle").addText("DHCP");
		        }
			}
			else if ("voip".equals(service_code))
			{
				String vector_argues = order.getVector_argues();
				if (StringUtil.IsEmpty(vector_argues))
				{
					errorDesc = ERROR_MSG;
					return "-6";
				}
				String[] argues = vector_argues.split("\\^");
				Map aragsMap = new HashMap();
				for (String argue : argues)
				{
					String key = argue.split("=", -1)[0];
					String value = argue.split("=", -1)[1];
					aragsMap.put(key, value);
				}
				param.addElement("ipaddress").addText(
						(String) aragsMap.get("voip_deviceIP"));// ont的语音业务地址
				param.addElement("ipmask").addText(
						(String) aragsMap.get("voip_deviceMask"));// ont的语音业务地址掩码
				param.addElement("gateway").addText(
						(String) aragsMap.get("voip_deviceGateway"));// ont语音业务网关
				// 语音业务有静态ip相关信息时设置上网方式为3，其余默认指定DHCP（4）
				if (null != aragsMap.get("voip_deviceIP") && !StringUtil.IsEmpty((String)aragsMap.get("voip_deviceIP")) &&
						null != aragsMap.get("voip_deviceMask") && !StringUtil.IsEmpty((String)aragsMap.get("voip_deviceMask"))
						&& null != aragsMap.get("voip_deviceGateway") && !StringUtil.IsEmpty((String)aragsMap.get("voip_deviceGateway")))
				{
					param.addElement("wanType").addText("3");
				}
				else
				{
					param.addElement("wanType").addText("4");
				}
				// h248
				if ("2".equals((String) aragsMap.get("voip_mode")))
				{
					root.addElement("servTypeId").addText("15");
					// 将语音线路"1,2", 转为"A1,A2"格式
					String voipPortNum = lanChange(aragsMap, "voip_PortNum", "A");
					param.addElement("voipPort").addText(voipPortNum);
					param.addElement("eid").addText((String) aragsMap.get("voip_EID"));// 语音业务EID
					param.addElement("mgcIp")
							.addText((String) aragsMap.get("voip_MGCIP"));
					param.addElement("mgcPort").addText(
							(String) aragsMap.get("voip_MGCPort"));
					param.addElement("standMgcIp").addText(
							(String) aragsMap.get("voip_standbyMGCIP"));
					param.addElement("standMgcPort").addText(
							(String) aragsMap.get("voip_standbyMGCPort"));
				}
				else
				{
					param.addElement("voipPort").addText(lanChange(aragsMap, "voip_PortNum", "V" ));
					root.addElement("servTypeId").addText("14");
					param.addElement("protocol").addText(
							(String) aragsMap.get("voip_mode"));// VOIP协议类型
					String voipAuthName = (String) aragsMap.get("voip_AuthName");
					if(null != voipAuthName)
					{
						if(voipAuthName.indexOf("+86") > -1 && voipAuthName.indexOf("@") > -1)
						{
							String voipPhone = voipAuthName.replaceAll("\\+86", "0");
							voipPhone = voipPhone.substring(0, voipPhone.indexOf("@"));
							param.addElement("voipPhone").addText(voipPhone); 
						}
						else
						{
							param.addElement("voipPhone").addText(
									(String) aragsMap.get("voip_AuthName"));
						}
					}
					else
					{
						param.addElement("voipPhone").addText("");
					}
					param.addElement("voipUsername").addText(
							(String) aragsMap.get("voip_AuthName"));
					param.addElement("voipPwd").addText(
							(String) aragsMap.get("voip_AuthPass"));
					param.addElement("proxServ").addText(
							(String) aragsMap.get("voip_serverAddr"));
					param.addElement("proxPort").addText(
							(String) aragsMap.get("voip_serverPort"));
					param.addElement("standProxServ").addText(
							(String) aragsMap.get("voip_standbyServerAddr"));
					param.addElement("standProxPort").addText(
							(String) aragsMap.get("voip_standbyServerPort"));
					param.addElement("regiServ").addText(
							(String) aragsMap.get("voip_regServerAddr"));
					param.addElement("regiPort").addText(
							(String) aragsMap.get("voip_regServerPort"));
					param.addElement("standRegiServ").addText(
							(String) aragsMap.get("voip_standbyRegServerAddr"));
					param.addElement("standRegiPort").addText(
							(String) aragsMap.get("voip_standbyRegServerPort"));
					// IMS专用
					if ("0".equals((String) aragsMap.get("voip_mode")))
					{
						param.addElement("outBoundProxy").addText(
								(String) aragsMap.get("voip_OutboundProxy"));
						param.addElement("outBoundPort").addText(
								(String) aragsMap.get("voip_OutboundProxyPort"));
						param.addElement("standOutBoundProxy").addText(
								(String) aragsMap.get("voip_StandbyOutboundProxy"));
						param.addElement("standOutBoundPort").addText(
								(String) aragsMap.get("voip_StandbyOutboundProxyPort"));
						param.addElement("voipUri").addText(
								(String) aragsMap.get("voip_URI"));
						param.addElement("voipOldAuthName").addText(
								(String) aragsMap.get("voip_oldAuthName"));
					}
				}
			}
			else
			{
				// service_code解析失败
				errorDesc = "service_code非法.";
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
				if (StringUtil.IsEmpty(vector_argues))
				{
					errorDesc = ERROR_MSG;
					return "-6";
				}
				String[] argues = vector_argues.split("\\^");
				Map aragsMap = new HashMap();
				for (String argue : argues)
				{
					String key = argue.split("=", -1)[0];
					String value = argue.split("=", -1)[1];
					aragsMap.put(key, value);
				}
				String factory = (String) aragsMap.get("OltFactory");
				/*param.addElement("wanType").addText(
						StringUtil.getStringValue(Integer.valueOf(Integer.valueOf(
								(String) aragsMap.get("wband_mode")).intValue() + 1)));*/
				param.addElement("userName").addText(
						StringUtil.getStringValue(aragsMap.get("wband_name")));
				
				//桥接、路由、全路由校验下端口，局方要求必须传，全路由不下发
				String wbandPortNum = lanChange(aragsMap, "wband_PortNum", "L");
				//销户端口只为计算vlan，华为才校验。
				if(StringUtil.IsEmpty(wbandPortNum) && ("HW".equalsIgnoreCase(factory)||"HUAWEI".equalsIgnoreCase(factory))){
					errorDesc = "宽带销户（华为olt）端口不能为空.";
					return "-6";
				}
				param.addElement("vlanId").addText(getVlan(StringUtil.getStringValue(aragsMap.get("wband_PortNum")),StringUtil.getIntegerValue(aragsMap.get("wband_PortNum")), StringUtil.getStringValue(aragsMap.get("wband_vlan")), factory, "wband"));
				
				int mode = StringUtil.getIntegerValue((String) aragsMap.get("wband_mode"));
				param.addElement("bindPort").addText(mode == MODE_ALLROUTE?"":lanChange(aragsMap, "wband_PortNum", "L"));
				
				param.addElement("orderNo").addText(order_no);
				param.addElement("oltFactory").addText(
						StringUtil.getStringValue(aragsMap.get("OltFactory")));
			}
			else if ("iptv".equals(service_code))
			{
				String vector_argues = order.getVector_argues();
				//stb的值为空
				//带有userID/userIDPwd参数的为stb业务
		        if ("stb".equals(order.getDeviceType()) || (vector_argues.contains("userID") || vector_argues.contains("userIDPwd"))){
		          root.addElement("servTypeId").addText("25");
		          
		          String[] argues = vector_argues.split("\\^");
		          Map aragsMap = new HashMap();
		          for (String argue : argues) {
		        	  String key = argue.split("=", -1)[0];
		        	  String value = argue.split("=", -1)[1];
		        	  aragsMap.put(key, value);
		          }

		          param.addElement("servaccount").addText((String)aragsMap.get("userID"));
		        }
		        else{
		        	root.addElement("servTypeId").addText("21");
					if (StringUtil.IsEmpty(vector_argues))
					{
						errorDesc = ERROR_MSG;
						return "-6";
					}
					String[] argues = vector_argues.split("\\^");
					Map aragsMap = new HashMap();
					for (String argue : argues)
					{
						String key = argue.split("=", -1)[0];
						String value = argue.split("=", -1)[1];
						aragsMap.put(key, value);
					}
					param.addElement("oltFactory").addText(
							(String) aragsMap.get("OltFactory"));
					String factory = (String) aragsMap.get("OltFactory");
					param.addElement("vlanId").addText(getVlan((String) aragsMap.get("iptv_PortNum"),StringUtil.getIntegerValue(aragsMap.get("iptv_PortNum")), StringUtil.getStringValue(aragsMap.get("iptv_vlan")), factory, "iptv"));
					param.addElement("wanType").addText("1");// iptv默认桥接
					// 将业务lan口入参"1,2", 转为"L1,L2"格式
					
					String wbandPortNum = lanChange(aragsMap, "iptv_PortNum", "L");
					param.addElement("bindPort").addText(wbandPortNum);
		        }
			}
			else if ("voip".equals(service_code))
			{
				String vector_argues = order.getVector_argues();
				String[] argues = vector_argues.split("\\^");
				Map aragsMap = new HashMap();
				for (String argue : argues)
				{
					String key = argue.split("=", -1)[0];
					String value = argue.split("=", -1)[1];
					aragsMap.put(key, value);
				}
				// h248
				if ("2".equals((String) aragsMap.get("voip_mode")))
				{
					root.addElement("servTypeId").addText("15");
					// 将语音线路"1,2", 转为"A1,A2"格式
					String voipPortNum = lanChange(aragsMap, "voip_PortNum", "A");
					param.addElement("voipPort").addText(voipPortNum);
					param.addElement("protocol").addText(
							(String) aragsMap.get("voip_mode"));// VOIP协议类型
				}
				// sip/ims
				else
				{
					root.addElement("servTypeId").addText("14");
					param.addElement("voipUsername").addText(
							(String) aragsMap.get("voip_AuthName"));
					param.addElement("voipPort").addText(lanChange(aragsMap, "voip_PortNum", "V"));
					param.addElement("protocol").addText(
							(String) aragsMap.get("voip_mode"));// VOIP协议类型
				}
			}
			else
			{
				// service_code解析失败
				errorDesc = "service_code非法.";
				return "-4";
			}
		}
		else if (!"cpe-Z".equals(order_type))
		{
			// order_Type 解析失败
			errorDesc = "order_Type非法.";
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
	@SuppressWarnings("rawtypes")
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

	@SuppressWarnings("rawtypes")
	public static String lanChange(Map aragsMap, String portNum, String param)
	{
		StringBuffer lan = new StringBuffer();
		if (null != aragsMap.get(portNum))
		{
			String[] lanPort = StringUtil.getStringValue(aragsMap.get(portNum))
					.split(",");
			//如果大唐绑定端口传0，算异常，这里返回空
			/*if(StringUtil.getStringValue(BIND_PORT_0).equals(StringUtil.getStringValue(aragsMap.get(portNum))) || StringUtil.IsEmpty(StringUtil.getStringValue(aragsMap.get(portNum)))){
				return "";
			}*/
			if(StringUtil.IsEmpty(StringUtil.getStringValue(aragsMap.get(portNum)))){
				return "";
			}
			for (int i = 0; i <= lanPort.length - 1; i++)
			{
				String string = param + lanPort[i];
				if (i == lanPort.length - 1)
				{
					lan.append(string);
				}
				else
				{
					lan.append(string).append(",");
				}
			}
		}
		return lan.toString();
	}
	
	/**
	 * 1、华为光猫 iptv的vlan值固定4057、宽带vlan值（桥接、路由,全路由方式的vlan值是端口+1）
       2、其它厂家的vlan值由接口参数确定，接口参数传多少值就是多少
	 * @param vlan
	 * @param vlanInt
	 * @param factory
	 * @return
	 */
	static String getVlan(String vlan, int vlanInt, String realVlan, String factory, String servType){
		if("HW".equalsIgnoreCase(factory)||"HUAWEI".equalsIgnoreCase(factory)){
			if("wband".equals(servType)){
				return StringUtil.getStringValue(vlanInt+1);
			}
			else{
				return "4057";
			}
		}
		else{
			return realVlan;
		}
	}
	
	public String getReturnXml()
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("utf-8");
		Element root = document.addElement("root");
		Element interfacemsg = root.addElement("interfacemsg");
		
		if("1".equals(result)){
			result = "0";
			errorDesc = "成功.";
		}
		else{
			result = "-1";
			if(StringUtil.IsEmpty(errorDesc)){
				errorDesc = "未知异常.";
			}
		}
		//ErrorDesc = "";
		// 结果代码
		interfacemsg.addElement("Result").addText("" + result);
		
		// 结果描述
		interfacemsg.addElement("ErrorDesc").addText(errorDesc);
		
		/*ProcessResponse rep = new ProcessResponse();
		rep.setProcessResponse(document.asXML());*/
		logger.warn("返回结果：{}",document.asXML());
		return document.asXML();
		/*String ret="<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n"
				+"\n"
				+"<root>\n"
				+"	<interfacemsg>\n"
				+"		<Result>0</Result>\n"
				+"		<ErrorDesc>成功</ErrorDesc>\n"
				+"	</interfacemsg>\n"
				+"</root>";

		return ret;*/
	}



	
	public String getResult()
	{
		return result;
	}



	
	public void setResult(String result)
	{
		this.result = result;
	}



	
	public String getErrorDesc()
	{
		return errorDesc;
	}



	
	public void setErrorDesc(String errorDesc)
	{
		this.errorDesc = errorDesc;
	}
	
}
