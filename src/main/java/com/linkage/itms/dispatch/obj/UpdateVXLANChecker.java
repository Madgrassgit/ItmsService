package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-11-28
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class UpdateVXLANChecker extends VXLANBaseChecker
{
	private static final Logger logger = LoggerFactory.getLogger(UpdateVXLANChecker.class);

	private String inParam = null;

	public UpdateVXLANChecker(String inParam) {
		this.inParam = inParam;
	}

	@Override
	public boolean check() {
		logger.debug("UpdateVXLANChecker==>check()" + inParam);

		SAXReader reader = new SAXReader();
		Document document = null;

		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");// 接口调用唯一ID 每次调用此值不可重复
			cmdType = root.elementTextTrim("CmdType");// 接口类型 CX_01,固定
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));// 客户端类型:1：网厅 2：IPOSS	3：综调 4：RADIUS 5：翼翮
			dealDate = root.elementTextTrim("DealDate");
			servTypeId = StringUtil.getIntegerValue(root.elementTextTrim("ServTypeId"));
			operateId = StringUtil.getIntegerValue(root.elementTextTrim("OperateId"));
			vXLANConfigSequence = StringUtil.getIntegerValue(root.elementTextTrim("VXLANConfigSequence")); 
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			userType = StringUtil.getIntegerValue(param.elementTextTrim("UserType"));
			requestID = param.elementTextTrim("RequestID");
			tunnelKey = StringUtil.getIntegerValue(param.elementTextTrim("TunnelKey"));
			tunnelRemoteIp = param.elementTextTrim("TunnelRemoteIP");
			workMode = StringUtil.getIntegerValue(param.elementTextTrim("WorkMode"));
			maxMTUSize = StringUtil.getIntegerValue(param.elementTextTrim("MaxMTUSize"),1440);			
			iPAddress = param.elementTextTrim("IPAddress");
			subnetMask = param.elementTextTrim("SubnetMask"); 
			addressingType = getStringDefaultValue(param.elementTextTrim("AddressingType"),"DHCP");
			dNSServers_Master = param.elementTextTrim("DNSServers_Master");
			dNSServers_Slave = param.elementTextTrim("DNSServers_Slave");
			defaultGateway = param.elementTextTrim("DefaultGateway");
			xctcom_vlan = StringUtil.getIntegerValue(param.elementTextTrim("X_CT-COM_VLAN"),0);
			bindPort = param.elementTextTrim("X_CT_COM_VLAN_PORTLIST");
			String boolNATEnabled = param.elementTextTrim("NATEnabled");
			if(null != boolNATEnabled && !boolNATEnabled.isEmpty() && "true".equalsIgnoreCase(boolNATEnabled))
			{
				nATEnabled = 1;
			}
			else
			{
				nATEnabled = 0;
			}
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 3;
			resultDesc = "入参格式错误";
			return false;
		}
		
		// 参数合法性检查
		if (false == baseCheck()) {
			return false;
		}
		if (false == workModeCheck()) {
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		return true;
	}
	
	public boolean workModeCheck()
	{
		if(StringUtil.IsEmpty(requestID))
		{
			result = 3;
			resultDesc = "requestID不能为空";
			return false;
		}
		if(StringUtil.IsEmpty(tunnelRemoteIp))
		{
			result = 3;
			resultDesc = "tunnelRemoteIp不能为空";
			return false;
		}
		if(tunnelKey < 1 || tunnelKey > 16777216)
		{
			result = 3;
			resultDesc = "tunnelKey值要在1-16777216范围内";
			return false;
		}
		if(1 != workMode && 2 != workMode && 3 != workMode)
		{
			result = 3;
			resultDesc = "workMode值只能为1,2,3";
			return false;
		}
		if(maxMTUSize > 1440)
		{
			result = 3;
			resultDesc = "VXLAN虚接口的MTU值不超过缺省值,缺省值为1440";
			return false;
		}
		if(vXLANConfigSequence <= 0)
		{
			result = 3;
			resultDesc = "VXLANConfigSequence不能为小于1且不能为空";
			return false;
		}
		if(!"Static".equals(addressingType) && !"DHCP".equals(addressingType))
		{
			result = 3;
			resultDesc = "addressingType地址类型不对,仅可填DHCP或者Static";
			return false;
		}
		if("Static".equals(addressingType) && dNSServers_Master.equals(dNSServers_Slave))
		{
			result = 3;
			resultDesc = "addressingType地址类型为Static时,dNSServers_Master与dNSServers_Slave不能相同";
			return false;
		}
		if(!ipCheck(tunnelRemoteIp))
		{
			result = 3;
			resultDesc = "tunnelRemoteIp参数不和法,请填ip地址格式!";
			return false;
		}
		if(null != iPAddress && !iPAddress.isEmpty() && !ipCheck(iPAddress))
		{
			result = 3;
			resultDesc = "iPAddress参数不和法,请填ip地址格式!";
			return false;
		}

		if(null != subnetMask && !subnetMask.isEmpty() && !ipCheck(subnetMask))
		{
			result = 3;
			resultDesc = "subnetMask参数不和法,请填ip地址格式!";
			return false;
		}
		if(null != dNSServers_Master && !dNSServers_Master.isEmpty() && !ipCheck(dNSServers_Master))
		{
			result = 3;
			resultDesc = "dNSServers_Master参数不和法,请填ip地址格式!";
			return false;
		}
		if(null != dNSServers_Slave && !dNSServers_Slave.isEmpty() && !ipCheck(dNSServers_Slave))
		{
			result = 3;
			resultDesc = "dNSServers_Slave参数不和法,请填ip地址格式!";
			return false;
		}
		if(null != defaultGateway && !defaultGateway.isEmpty() && !ipCheck(defaultGateway))
		{
			result = 3;
			resultDesc = "defaultGateway参数不和法,请填ip地址格式!";
			return false;
		}
		return true;
	}

	/**
     * 判断IP地址的合法性，这里采用了正则表达式的方法来判断
     * return true，合法
     * */
    public static boolean ipCheck(String text) {
        if (null != text && !text.isEmpty()) {
            // 定义正则表达式
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."+
                      "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."+
                      "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."+
                      "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            // 判断ip地址是否与正则表达式匹配
            if (text.matches(regex)) {
                // 返回判断信息
                return true;
            } else {
                // 返回判断信息
                return false;
            }
        }
        return false;
    }
	
	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(StringUtil.getStringValue(resultDesc));
		// 最新绑定的Loid
		root.addElement("Loid").addText(StringUtil.getStringValue(loid));
		// 其它Loid信息
		root.addElement("LoidPrev").addText(StringUtil.getStringValue(loidPrev));
		
		return document.asXML();
	}
	
	public String getStringDefaultValue(String value, String defValue)
	{
		if(null == value || "".equals(value))
		{
			return defValue;
		}
		return value;
	}
}
