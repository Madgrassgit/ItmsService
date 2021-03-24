package com.linkage.itms.dispatch.obj;


import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class CloudQueryVXLANGetWayChecker extends CloudBaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(CloudQueryVXLANGetWayChecker.class);

	protected String isVxlan = "";
	protected String vxlanStatus = "";
	
	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public CloudQueryVXLANGetWayChecker(String inXml) {
		callXml = inXml;
	}

	@Override
	public boolean check() {
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			dealDate = root.elementTextTrim("DealDate");

			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		} catch (Exception e) {
			e.printStackTrace();
			result = 3;
			resultDesc = "入参格式错误";
			return false;
		}
		//参数合法性检查
		if (!baseCheck() || !userInfoTypeCheck() || !userInfoCheck()) {
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
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
		root.addElement("RstCode").addText(String.valueOf(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		
		Element par = root.addElement("Param");
		// 设备序列号
		par.addElement("DeviceSN").addText(deviceSN);
		// 当前最新绑定的Loid
		par.addElement("Loid").addText(loid);
		// 通过账号查到多个Loid，除去最新绑定Loid之外的Loid集合
		par.addElement("LoidPrev").addText(loidPrev);
		//宽带账号
		par.addElement("UserInfo").addText(userInfo);
		// 设备类型
		par.addElement("DeviceType").addText(deviceType);
		// 厂商
		par.addElement("DeviceVendor").addText(deviceVendor);
		// 型号
		par.addElement("DeviceModel").addText(deviceModel);
		// 软件版本
		par.addElement("Softwareversion").addText(softwareversion);
		// 硬件版本
		par.addElement("Hardwareversion").addText(hardwareversion);
		// 是否有宽带业务
		par.addElement("IsNet").addText(isNet);
		// 是否有vxlan业务
		par.addElement("IsVXLAN").addText(isVxlan);
		// 上网方式
		par.addElement("WanType").addText(wanType);
		// Ip地址
		par.addElement("IpAddr").addText(ipAddr);
		// Ip类型
		par.addElement("IpType").addText(ipType);
		// 在线状态
		par.addElement("Online").addText(online);
		// 网关当前施工状态
		par.addElement("Status").addText(status);
		// vxlan通道状态
		par.addElement("VXLANStatus").addText(vxlanStatus);
	
		return document.asXML();
	}

	public String getIsVxlan() {
		return isVxlan;
	}

	public void setIsVxlan(String isVxlan) {
		this.isVxlan = isVxlan;
	}

	public String getVxlanStatus() {
		return vxlanStatus;
	}

	public void setVxlanStatus(String vxlanStatus) {
		this.vxlanStatus = vxlanStatus;
	}
}
