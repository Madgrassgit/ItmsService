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
 * 终端审核版本信息同步接口 校验
 * @author fanjm(35572)
 * @date 2017-3-7
 */
public class TerminalVersionAuditChecker extends BaseQueryChecker {

	private static Logger logger = LoggerFactory.getLogger(TerminalVersionAuditChecker.class);
	
	private String inParam = null;

	private String myresult = "";
	
	private String resultDesc = "";
	
	private String vendor_name = "";

	private String vendor_id = "";
	
	private String device_model = "";
	
	private String device_model_id = "";

	private String hardwareversion = "";

	private String softwareversion = "";

	private String rela_dev_type_id = "";

	private String access_style_relay_id = "";

	private String spec_name = "";
	
	private String spec_id = "";

	private String reason = "";

	public TerminalVersionAuditChecker(String inParam){
		this.inParam = inParam;
	}
	
	
	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check(){
		
		logger.debug("TerminalVersionAuditChecker==>check()");
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {

			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			
			/**
			 * 设备厂商
			*/
			vendor_name  = param.elementTextTrim("Vendor");
			/**
			 * 设备型号
			 */
			device_model = param.elementTextTrim("Model"); 
			/**
			 * 硬件版本
			 */
			hardwareversion = param.elementTextTrim("HandwareVersion"); 
			/**
			 * 软件版本
			 */
			softwareversion = param.elementTextTrim("Softwareversion");
			/**
			 * 设备类型1:e8-c 2:e8-b
			 */
			rela_dev_type_id = param.elementTextTrim("RelaDev");
			
			/**
			 * 上行方式 1:ADSL 2:LAN 3:EPON 4:GPON
			 */
			access_style_relay_id = param.elementTextTrim("AccessStyle");
			/**
			 * 终端规格
			 */
			spec_name = param.elementTextTrim("DeviceSpec"); 
			/**
			 * 定版原因
			 */
			reason = param.elementTextTrim("Reason"); 
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			//赋一个值，否则getReturnXml的addtext改字段会出现错误造成调用失败
			myresult = "1";
			resultDesc = "数据格式错误";
			return false;
		}
		
		
		//参数合法性检查
		if (false == baseCheck() || false == paramCheck()) {
			return false;
		}
		
		
		return true;
	}
	
	
	/**
	 * 返回调用结果字符串
	 * 
	 * @author fanjm(35572)
	 * @date 2016-11-29
	 * @return boolean 是否校验通过
	 */
	@Override
	public String getReturnXml(){
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(cmdId);
		// 结果代码
		root.addElement("RstCode").addText(StringUtil.getStringValue(myresult));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		
		
		return document.asXML();
	}
	
	
	/**
	 * 基本信息合法性检查
	 * 
	 * @author fanjm(35572)
	 * @date 2016-11-29
	 * @return boolean 是否校验通过
	 */
	public boolean baseCheck(){
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)){
			myresult = "1000";
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		
		if(3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType && 5 != clientType && 6 != clientType){
			myresult = "2";
			resultDesc = "客户端类型非法";
			return false;
		}
		
		if(false == "CX_01".equals(cmdType)){
			myresult = "3";
			resultDesc = "接口类型非法";
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * 数据参数合法性检查
	 * 
	 * @author fanjm(35572)
	 * @date 2017-3-7
	 * @return boolean 是否校验通过
	 */
	boolean paramCheck(){
		if(StringUtil.IsEmpty(vendor_name) || StringUtil.IsEmpty(device_model) || StringUtil.IsEmpty(hardwareversion) 
				|| StringUtil.IsEmpty(softwareversion)|| StringUtil.IsEmpty(rela_dev_type_id)|| StringUtil.IsEmpty(access_style_relay_id)
				|| StringUtil.IsEmpty(spec_name)|| StringUtil.IsEmpty(reason)){
			myresult = "1";
			resultDesc = "数据格式错误";
			return false;
		}
		else if(!"1".equals(access_style_relay_id)&&!"2".equals(access_style_relay_id)&&!"3".equals(access_style_relay_id)&&!"4".equals(access_style_relay_id)){
			myresult = "1";
			resultDesc = "上行方式非法";
			return false;
		}
		else if(!"1".equals(rela_dev_type_id)&&!"2".equals(rela_dev_type_id)){
			myresult = "1";
			resultDesc = "设备类型非法";
			return false;
		}
		
		return true;
	}
	
	
	public String getInParam() {
		return inParam;
	}

	
	public void setInParam(String inParam) {
		this.inParam = inParam;
	}


	public String getVendor_name() {
		return vendor_name;
	}


	public String getDevice_model() {
		return device_model;
	}


	public String getHardwareversion() {
		return hardwareversion;
	}


	public String getSoftwareversion() {
		return softwareversion;
	}


	public String getRela_dev_type_id() {
		return rela_dev_type_id;
	}


	public String getAccess_style_relay_id() {
		return access_style_relay_id;
	}


	public String getSpec_name() {
		return spec_name;
	}


	public String getReason() {
		return reason;
	}


	public void setVendor_name(String vendor_name) {
		this.vendor_name = vendor_name;
	}


	public void setDevice_model(String device_model) {
		this.device_model = device_model;
	}


	public void setHardwareversion(String hardwareversion) {
		this.hardwareversion = hardwareversion;
	}


	public void setSoftwareversion(String softwareversion) {
		this.softwareversion = softwareversion;
	}


	public void setRela_dev_type_id(String rela_dev_type_id) {
		this.rela_dev_type_id = rela_dev_type_id;
	}


	public void setAccess_style_relay_id(String access_style_relay_id) {
		this.access_style_relay_id = access_style_relay_id;
	}


	public void setSpec_name(String spec_name) {
		this.spec_name = spec_name;
	}


	public void setReason(String reason) {
		this.reason = reason;
	}


	public String getVendor_id() {
		return vendor_id;
	}


	public String getDevice_model_id() {
		return device_model_id;
	}


	public void setVendor_id(String vendor_id) {
		this.vendor_id = vendor_id;
	}


	public void setDevice_model_id(String device_model_id) {
		this.device_model_id = device_model_id;
	}


	public String getSpec_id() {
		return spec_id;
	}


	public void setSpec_id(String spec_id) {
		this.spec_id = spec_id;
	}


	public String getMyresult() {
		return myresult;
	}


	public String getResultDesc() {
		return resultDesc;
	}


	public void setMyresult(String myresult) {
		this.myresult = myresult;
	}


	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}
	
}
