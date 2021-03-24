
package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.EncryptionUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * @author zhangsm
 * @version 1.0
 * @since 2011-10-9 上午09:28:36
 * @category com.linkage.itms.dispatch.obj<br>
 * @copyright 亚信联创 网管产品部
 */
public class QuerySheetDataChecker extends BaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(QuerySheetDataChecker.class);
	private Map<String, Map<String, String>> sheetDataMap;

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public QuerySheetDataChecker(String inXml)
	{
		callXml = inXml;
	}
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck())
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}
	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		if (result == 0)
		{
			// 接入方式
			Map<String, String> accessTypeMap = sheetDataMap.get("AccessType");			
			root.addElement("AccessType").addText(StringUtil.getStringValue(accessTypeMap, "AccessType", ""));
			
			// 安徽电信走此逻辑  安徽电信目前只有VOIP语音业务 ================================================
			if ("ah_dx".equals(Global.G_instArea)) {
				// 设备基本信息
				Map<String, String> deviceInfoMap = sheetDataMap.get("deviceInfoMap");
				Element deviceBasicDate = root.addElement("DeviceBasicDate");
				deviceBasicDate.addElement("LOID").addText(StringUtil.getStringValue(deviceInfoMap, "LOID", ""));
				deviceBasicDate.addElement("DeviceSerialnumber").addText(StringUtil.getStringValue(deviceInfoMap, "device_serialnumber", ""));
				deviceBasicDate.addElement("VendorName").addText(StringUtil.getStringValue(deviceInfoMap, "vendor_name", ""));
				deviceBasicDate.addElement("DeviceModel").addText(StringUtil.getStringValue(deviceInfoMap, "device_model", ""));
				deviceBasicDate.addElement("SoftwareVersion").addText(StringUtil.getStringValue(deviceInfoMap, "softwareversion", ""));
				deviceBasicDate.addElement("RelaDevType").addText(StringUtil.getStringValue(deviceInfoMap, "rela_dev_type_id", ""));
				deviceBasicDate.addElement("CityID").addText(StringUtil.getStringValue(deviceInfoMap, "city_id", ""));
				deviceBasicDate.addElement("CpeCurrentstatus").addText(StringUtil.getStringValue(deviceInfoMap, "cpe_currentstatus", ""));
				deviceBasicDate.addElement("IP").addText(StringUtil.getStringValue(deviceInfoMap, "loopback_ip", ""));
				deviceBasicDate.addElement("Is200M").addText(StringUtil.getStringValue(deviceInfoMap, "is200M", ""));
				deviceBasicDate.addElement("Is500M").addText(StringUtil.getStringValue(deviceInfoMap, "is500M", ""));
				
				// VOIP业务
				Map<String, String> voipMap = sheetDataMap.get("VOIP");
				if (null != voipMap && !voipMap.isEmpty())
				{
					logger.warn("组装VOIP工单参数");
					Element voip = root.addElement("VOIP");
					voip.addElement("WanType").addText(StringUtil.getStringValue(voipMap, "wan_type", ""));
					voip.addElement("VlanId").addText(StringUtil.getStringValue(voipMap, "vlanid", ""));
					Element lines = voip.addElement("Lines");
					Element voiceService = voip.addElement("VoiceService");
					// 线路1语音参数
					Map<String, String> line1 = sheetDataMap.get("line1");
					boolean isVoipServer = false;
					if (null != line1 && !line1.isEmpty())
					{
						Element line = lines.addElement("Line");
						line.addAttribute("num", "1");   
						line.addElement("Phone").addText(StringUtil.getStringValue(line1, "voip_phone", ""));
						line.addElement("Enable").addText(StringUtil.getStringValue(line1, "parm_stat", "").equals("1") ? "1" : "0");
						line.addElement("AuthUserName").addText(StringUtil.getStringValue(line1, "voip_username", ""));
						line.addElement("AuthPassword").addText(EncryptionUtil.decryption(StringUtil.getStringValue(line1, "voip_passwd", "")));
						isVoipServer = true;
						voiceService.addElement("ProtocolType").addText(StringUtil.getStringValue(line1, "protocol", ""));
						voiceService.addElement("ProxyServer").addText(StringUtil.getStringValue(line1, "prox_serv", ""));
						voiceService.addElement("ProxyServerPort").addText(StringUtil.getStringValue(line1, "prox_port", ""));
						voiceService.addElement("StandByProxyServer").addText(StringUtil.getStringValue(line1, "stand_prox_serv", ""));
						voiceService.addElement("StandByProxyServerPort").addText(StringUtil.getStringValue(line1, "stand_prox_port", ""));
						voiceService.addElement("RegistrarServer").addText(StringUtil.getStringValue(line1, "regi_serv", ""));
						voiceService.addElement("RegistrarServerPort").addText(StringUtil.getStringValue(line1, "regi_port", ""));
						voiceService.addElement("StandByRegistrarServer").addText(StringUtil.getStringValue(line1, "stand_regi_serv", ""));
						voiceService.addElement("StandByRegistrarServerPort").addText(StringUtil.getStringValue(line1, "stand_regi_port", ""));
						voiceService.addElement("OutboundProxy").addText(StringUtil.getStringValue(line1, "out_bound_proxy", ""));
						voiceService.addElement("OutboundProxyPort").addText(StringUtil.getStringValue(line1, "out_bound_port", ""));
						voiceService.addElement("StandByOutboundProxy").addText(StringUtil.getStringValue(line1, "stand_out_bound_proxy", ""));
						voiceService.addElement("StandByOutboundProxyPort").addText(StringUtil.getStringValue(line1, "stand_out_bound_port", ""));
					}
					// 线路2语音参数
					Map<String, String> line2 = sheetDataMap.get("line2");
					if (null != line2 && !line2.isEmpty())
					{
						Element line = lines.addElement("Line");
						line.addAttribute("num", "2");
						line.addElement("Phone").addText(StringUtil.getStringValue(line2, "voip_phone", ""));
						line.addElement("Enable").addText(StringUtil.getStringValue(line2,"parm_stat","").equals("1") ? "1" : "0");
						line.addElement("AuthUserName").addText(StringUtil.getStringValue(line2, "voip_username", ""));
						line.addElement("AuthPassword").addText(EncryptionUtil.decryption(String.valueOf(StringUtil.getStringValue(line2, "voip_passwd", ""))));
						if (!isVoipServer)
						{
							voiceService.addElement("ProtocolType").addText(StringUtil.getStringValue(line2, "protocol", ""));
							voiceService.addElement("ProxyServer").addText(StringUtil.getStringValue(line2, "prox_serv", ""));
							voiceService.addElement("ProxyServerPort").addText(StringUtil.getStringValue(line2, "prox_port", ""));
							voiceService.addElement("StandByProxyServer").addText(StringUtil.getStringValue(line2, "stand_prox_serv", ""));
							voiceService.addElement("StandByProxyServerPort").addText(StringUtil.getStringValue(line2, "stand_prox_port", ""));
							voiceService.addElement("RegistrarServer").addText(StringUtil.getStringValue(line2, "regi_serv", ""));
							voiceService.addElement("RegistrarServerPort").addText(StringUtil.getStringValue(line2, "regi_port", ""));
							voiceService.addElement("StandByRegistrarServer").addText(StringUtil.getStringValue(line2, "stand_regi_serv", ""));
							voiceService.addElement("StandByRegistrarServerPort").addText(StringUtil.getStringValue(line2, "stand_regi_port", ""));
							voiceService.addElement("OutboundProxy").addText(StringUtil.getStringValue(line2, "out_bound_proxy", ""));
							voiceService.addElement("OutboundProxyPort").addText(StringUtil.getStringValue(line2, "out_bound_port", ""));
							voiceService.addElement("StandByOutboundProxy").addText(StringUtil.getStringValue(line2, "stand_out_bound_proxy", ""));
							voiceService.addElement("StandByOutboundProxyPort").addText(StringUtil.getStringValue(line2, "stand_out_bound_port", ""));
						}
					}
				}else{
					logger.warn("组装VOIP工单参数");
					Element voip = root.addElement("VOIP");
					voip.addElement("WanType").addText(StringUtil.getStringValue(voipMap, "wan_type", ""));
					voip.addElement("VlanId").addText(StringUtil.getStringValue(voipMap, "vlanid", ""));
					Element lines = voip.addElement("Lines");
					Element voiceService = voip.addElement("VoiceService");
					// 线路1语音参数
					Map<String, String> line1 = sheetDataMap.get("line1");
					Element line = lines.addElement("Line");
					line.addAttribute("num", "1");
					line.addElement("Phone").addText(StringUtil.getStringValue(line1, "voip_phone", ""));
					line.addElement("Enable").addText(StringUtil.getStringValue(line1, "parm_stat", "").equals("1") ? "1" : "0");
					line.addElement("AuthUserName").addText(StringUtil.getStringValue(line1, "voip_username", ""));
					line.addElement("AuthPassword").addText(EncryptionUtil.decryption(String.valueOf(StringUtil.getStringValue(line1, "voip_passwd", ""))));
					voiceService.addElement("ProtocolType").addText(StringUtil.getStringValue(line1, "protocol", ""));
					voiceService.addElement("ProxyServer").addText(StringUtil.getStringValue(line1, "prox_serv", ""));
					voiceService.addElement("ProxyServerPort").addText(StringUtil.getStringValue(line1, "prox_port", ""));
					voiceService.addElement("StandByProxyServer").addText(StringUtil.getStringValue(line1, "stand_prox_serv", ""));
					voiceService.addElement("StandByProxyServerPort").addText(StringUtil.getStringValue(line1, "stand_prox_port", ""));
					voiceService.addElement("RegistrarServer").addText(StringUtil.getStringValue(line1, "regi_serv", ""));
					voiceService.addElement("RegistrarServerPort").addText(StringUtil.getStringValue(line1, "regi_port", ""));
					voiceService.addElement("StandByRegistrarServer").addText(StringUtil.getStringValue(line1, "stand_regi_serv", ""));
					voiceService.addElement("StandByRegistrarServerPort").addText(StringUtil.getStringValue(line1, "stand_regi_port", ""));
					voiceService.addElement("OutboundProxy").addText(StringUtil.getStringValue(line1, "out_bound_proxy", ""));
					voiceService.addElement("OutboundProxyPort").addText(StringUtil.getStringValue(line1, "out_bound_port", ""));
					voiceService.addElement("StandByOutboundProxy").addText(StringUtil.getStringValue(line1, "stand_out_bound_proxy", ""));
					voiceService.addElement("StandByOutboundProxyPort").addText(StringUtil.getStringValue(line1, "stand_out_bound_port", ""));
				}
			}
			// 其他省市电信走此逻辑
			else {
				// 上网业务的工单配置数据
				Map<String, String> internetMap = sheetDataMap.get("Internet");
				if (null != internetMap && !internetMap.isEmpty())
				{
					logger.warn("组装Internet工单参数");
					Element internet = root.addElement("Internet");
					internet.addElement("WanType").addText(StringUtil.getStringValue(internetMap,"wan_type",""));
					internet.addElement("BindPort").addText(StringUtil.getStringValue(internetMap,"bind_port",""));
					internet.addElement("Username").addText(StringUtil.getStringValue(internetMap,"username",""));
					internet.addElement("Password").addText(StringUtil.getStringValue(internetMap,"passwd",""));
					internet.addElement("Pvc").addText("PVC:" + StringUtil.getStringValue(internetMap,"vciid","") + "/"+ StringUtil.getStringValue(internetMap,"vpiid",""));
					internet.addElement("VlanId").addText("" + StringUtil.getStringValue(internetMap,"vlanid",""));
				}
				// IPTV业务
				Map<String, String> iptvMap = sheetDataMap.get("IPTV");
				if (null != iptvMap && !iptvMap.isEmpty())
				{
					logger.warn("组装IPTV工单参数");
					Element iptv = root.addElement("IPTV");
					iptv.addElement("WanType").addText(StringUtil.getStringValue(iptvMap,"wan_type",""));
					iptv.addElement("BindPort").addText(StringUtil.getStringValue(iptvMap,"bind_port",""));
					iptv.addElement("Pvc").addText("PVC:" + StringUtil.getStringValue(iptvMap,"vciid","") + "/" + StringUtil.getStringValue(iptvMap,"vpiid",""));
					iptv.addElement("VlanId").addText(StringUtil.getStringValue(iptvMap,"vlanid",""));
				}
				// VOIP业务
				Map<String, String> voipMap = sheetDataMap.get("VOIP");
				if (null != voipMap && !voipMap.isEmpty())
				{
					logger.warn("组装VOIP工单参数");
					Element voip = root.addElement("VOIP");
					voip.addElement("WanType").addText(StringUtil.getStringValue(voipMap, "wan_type", ""));
					voip.addElement("Pvc").addText("PVC:" + StringUtil.getStringValue(voipMap, "vciid", "") + "/" + StringUtil.getStringValue(voipMap, "vpiid", ""));
					voip.addElement("VlanId").addText(StringUtil.getStringValue(voipMap, "vlanid", ""));
					Element lines = voip.addElement("Lines");
					Element voiceService = voip.addElement("VoiceService");
					// 线路1语音参数
					Map<String, String> line1 = sheetDataMap.get("line1");
					boolean isVoipServer = false;
					if (null != line1 && !line1.isEmpty())
					{
						Element line = lines.addElement("Line");
						line.addAttribute("num", "1");
						line.addElement("Phone").addText(StringUtil.getStringValue(line1, "voip_phone", ""));
						line.addElement("Enable").addText(StringUtil.getStringValue(line1, "parm_stat", "").equals("1") ? "1" : "0");
						line.addElement("AuthUserName").addText(StringUtil.getStringValue(line1, "voip_username", ""));
						line.addElement("AuthPassword").addText(StringUtil.getStringValue(line1, "voip_passwd", ""));
						isVoipServer = true;
						voiceService.addElement("ProtocolType").addText(StringUtil.getStringValue(line1, "protocol", ""));
						voiceService.addElement("ProxyServer").addText(StringUtil.getStringValue(line1, "prox_serv", ""));
						voiceService.addElement("ProxyServerPort").addText(StringUtil.getStringValue(line1, "prox_port", ""));
						voiceService.addElement("StandByProxyServer").addText(StringUtil.getStringValue(line1, "stand_prox_serv", ""));
						voiceService.addElement("StandByProxyServerPort").addText(StringUtil.getStringValue(line1, "stand_prox_port", ""));
						voiceService.addElement("RegistrarServer").addText(StringUtil.getStringValue(line1, "regi_serv", ""));
						voiceService.addElement("RegistrarServerPort").addText(StringUtil.getStringValue(line1, "regi_port", ""));
						voiceService.addElement("StandByRegistrarServer").addText(StringUtil.getStringValue(line1, "stand_regi_serv", ""));
						voiceService.addElement("StandByRegistrarServerPort").addText(StringUtil.getStringValue(line1, "stand_regi_port", ""));
						voiceService.addElement("OutboundProxy").addText(StringUtil.getStringValue(line1, "out_bound_proxy", ""));
						voiceService.addElement("OutboundProxyPort").addText(StringUtil.getStringValue(line1, "out_bound_port", ""));
						voiceService.addElement("StandByOutboundProxy").addText(StringUtil.getStringValue(line1, "stand_out_bound_proxy", ""));
						voiceService.addElement("StandByOutboundProxyPort").addText(StringUtil.getStringValue(line1, "stand_out_bound_port", ""));
					}
					// 线路2语音参数
					Map<String, String> line2 = sheetDataMap.get("line2");
					if (null != line2 && !line2.isEmpty())
					{
						Element line = lines.addElement("Line");
						line.addAttribute("num", "2");
						line.addElement("Phone").addText(StringUtil.getStringValue(line2, "voip_phone", ""));
						line.addElement("Enable").addText(StringUtil.getStringValue(line2, "parm_stat", "").equals("1") ? "1" : "0");
						line.addElement("AuthUserName").addText(StringUtil.getStringValue(line2, "voip_username", ""));
						line.addElement("AuthPassword").addText(StringUtil.getStringValue(line2, "voip_passwd", ""));
						if (!isVoipServer)
						{
							voiceService.addElement("ProtocolType").addText(StringUtil.getStringValue(line2, "protocol", ""));
							voiceService.addElement("ProxyServer").addText(StringUtil.getStringValue(line2, "prox_serv", ""));
							voiceService.addElement("ProxyServerPort").addText(StringUtil.getStringValue(line2, "prox_port", ""));
							voiceService.addElement("StandByProxyServer").addText(StringUtil.getStringValue(line2, "stand_prox_serv", ""));
							voiceService.addElement("StandByProxyServerPort").addText(StringUtil.getStringValue(line2, "stand_prox_port", ""));
							voiceService.addElement("RegistrarServer").addText(StringUtil.getStringValue(line2, "regi_serv", ""));
							voiceService.addElement("RegistrarServerPort").addText(StringUtil.getStringValue(line2, "regi_port", ""));
							voiceService.addElement("StandByRegistrarServer").addText(StringUtil.getStringValue(line2, "stand_regi_serv", ""));
							voiceService.addElement("StandByRegistrarServerPort").addText(StringUtil.getStringValue(line2, "stand_regi_port", ""));
							voiceService.addElement("OutboundProxy").addText(StringUtil.getStringValue(line2, "out_bound_proxy", ""));
							voiceService.addElement("OutboundProxyPort").addText(StringUtil.getStringValue(line2, "out_bound_port", ""));
							voiceService.addElement("StandByOutboundProxy").addText(StringUtil.getStringValue(line2, "stand_out_bound_proxy", ""));
							voiceService.addElement("StandByOutboundProxyPort").addText(StringUtil.getStringValue(line2, "stand_out_bound_port", ""));
						}
					}
				}
			}
		}
		return document.asXML();
	}
	public Map<String, Map<String, String>> getSheetDataMap()
	{
		return sheetDataMap;
	}

	public void setSheetDataMap(Map<String, Map<String, String>> sheetDataMap)
	{
		this.sheetDataMap = sheetDataMap;
	}
}
