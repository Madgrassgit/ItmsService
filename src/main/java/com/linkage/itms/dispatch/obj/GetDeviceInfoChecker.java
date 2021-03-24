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
 * @author songxq
 * @version 1.0
 * @since 2020年1月8日 下午2:43:47
 * @category 
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class GetDeviceInfoChecker  extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(GetDeviceInfoChecker.class);
	private String inParam = null;
	
	//厂商，型号，硬件版本，软件版本，设备序列号，MAC地址
	private String vendor;
	private String deviceModel;
	private String hardwareversion;
	private String softwareversion;
	private String mac;
	/**
	 * @param inXml
	 */
	public GetDeviceInfoChecker(String inXml)
	{
		this.inParam = inXml;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean check()
	{
		logger.debug("GetDeviceInfoChecker==>check()" + inParam);

		SAXReader reader = new SAXReader();
		Document document = null;
		

		try {

			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();

			cmdId = root.elementTextTrim("CmdID");// 接口调用唯一ID 每次调用此值不可重复
			cmdType = root.elementTextTrim("CmdType");// 接口类型 CX_01,固定
			
			/* 客户端类型
			 * 1：大唐IOM
			 * 2：IPOSS
			 * 3：网调
			 * 4：RADIUS
			*/
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");

			// 用户信息类型:1：用户宽带帐号;2：LOID
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
		
			// 用户信息类型所对应的用户信息
			userInfo = param.elementTextTrim("UserInfo"); 

			logger.warn(userInfo);

		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		if(userInfoType!=1 && userInfoType!=2){
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}

		if (StringUtil.IsEmpty(userInfo)) {
			result = 1000;
			resultDesc = "用户信息为空";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck()) {
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
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		
		if(0 == result)
		{
			root.addElement("DevVendor").addText(StringUtil.getStringValue(vendor));
			root.addElement("DevModel").addText(StringUtil.getStringValue(deviceModel));
			root.addElement("HardwareVersion").addText(StringUtil.getStringValue(hardwareversion));
			root.addElement("SoftwareVersion").addText(StringUtil.getStringValue(softwareversion));
			root.addElement("DevSN").addText(StringUtil.getStringValue(devSn));
			root.addElement("Mac").addText(StringUtil.getStringValue(mac));
		}
		

		return document.asXML();
	}

	
	public String getInParam()
	{
		return inParam;
	}

	
	public void setInParam(String inParam)
	{
		this.inParam = inParam;
	}

	
	public String getVendor()
	{
		return vendor;
	}

	
	public void setVendor(String vendor)
	{
		this.vendor = vendor;
	}

	
	public String getMac()
	{
		return mac;
	}

	
	public void setMac(String mac)
	{
		this.mac = mac;
	}

	
	public String getDeviceModel()
	{
		return deviceModel;
	}

	
	public void setDeviceModel(String deviceModel)
	{
		this.deviceModel = deviceModel;
	}

	
	public String getHardwareversion()
	{
		return hardwareversion;
	}

	
	public void setHardwareversion(String hardwareversion)
	{
		this.hardwareversion = hardwareversion;
	}

	
	public String getSoftwareversion()
	{
		return softwareversion;
	}

	
	public void setSoftwareversion(String softwareversion)
	{
		this.softwareversion = softwareversion;
	}
	
	
}

