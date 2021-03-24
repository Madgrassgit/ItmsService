package com.linkage.stbms.ids.util;

import java.io.StringReader;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 新疆电信  机顶盒零配置状态查询接口
 * @author chenxj6
 * @date 2016-11-11
 * @param inParam
 * @return
 */

public class GetStbOrderStatusChecker extends BaseChecker {

	private static final Logger logger = LoggerFactory.getLogger(GetStbOrderStatusChecker.class);

	private String inParam = null;
	
	private String deviceInfo;
	
	private String errordesc;

	public GetStbOrderStatusChecker(String inParam) {
		this.inParam = inParam;
	}

	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check() {

		logger.debug("GetStbOrderStatusChecker==>check()");

		SAXReader reader = new SAXReader();
		Document document = null;

		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();

			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType")); // 1：BSS, 2：IPOSS, 3：综调, 4：RADIUS

			Element param = root.element("Param");

			searchType = param.elementTextTrim("UserInfoType"); // 查询类型 1：用户业务账号;2：设备序列号;3：MAC地址
			
			searchInfo = param.elementTextTrim("UserInfo"); // 查询条件信息
			
			//设备序列号（可填后6位），如果该数据不为空则优先按设备信息查询，如果该数据为空则按用户信息查询，当类型为3时此处为MAC地址
			deviceInfo = param.elementTextTrim("DeviceInfo"); 
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			rstCode = "1";
			rstMsg = "入参格式错误";
			return false;
		}
		
		if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType) {
			rstCode = "2";
			rstMsg = "客户端类型非法";
			return false;
		}
		
		if(null!=deviceInfo && deviceInfo.trim().length()!=0){
			if(!"3".equals(searchType) && deviceInfo.trim().length()<6){
				rstCode = "1005";
				rstMsg = "字段DeviceInfo为设备序列号时，长度不可小于6";
				return false;
			}
			if("3".equals(searchType) && !macPattern.matcher(deviceInfo).matches()){
				rstCode = "1004";
				rstMsg = "字段DeviceInfo为MAC地址时，地址不合法";
				return false;
			}
		}

		// 参数合法性检查
		if (false == baseCheck() || false == searchTypeCheck()
				|| false == searchInfoCheck4XJ()) {
			return false;
		}

		rstCode = "0";
		rstMsg = "成功";

		return true;
	}
	
	boolean searchInfoCheck4XJ(){
		/**
		 * 设备序列号正则表达式
		 */
		String devSnPattern = "\\w{1,}+";
		
		if(StringUtil.IsEmpty(searchInfo)){
			rstCode = "1002";
			rstMsg = "字段searchInfo不能为空";
			return false;
		}
		// 3：机顶盒MAC
		else if ("3".equals(searchType)) {
			if(false == macPattern.matcher(searchInfo).matches()){
				rstCode = "1004";
				rstMsg = "MAC地址不合法";
				return false;
			}
		}
		// 2：机顶盒序列号
		else if ("2".equals(searchType)) {
			Pattern snPattern = Pattern.compile(devSnPattern); 
			
			if(false == snPattern.matcher(searchInfo).matches() || searchInfo.length() < 6){
				rstCode = "1005";
				rstMsg = "设备序列号不合法";
				return false;
			}
		}
		return true;
	}

	/**
	 * 返回调用结果字符串
	 * 
	 */
	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(rstCode);
		
		Element param = root.addElement("Param");
		param.addElement("stborderstatus").addText(rstMsg==null ? "" : rstMsg);
		param.addElement("errordesc").addText(errordesc==null ? "" : errordesc);

		return document.asXML();
	}

	public String getInParam() {
		return inParam;
	}

	public void setInParam(String inParam) {
		this.inParam = inParam;
	}

	public String getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public String getErrordesc() {
		return errordesc;
	}

	public void setErrordesc(String errordesc) {
		this.errordesc = errordesc;
	}

}
