package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;


public class OneKeyDoneChecker extends BaseChecker {
	
	// 日志记录
	private static final Logger logger = LoggerFactory.getLogger(OneKeyDoneChecker.class);
	
	//业务类型
	private int serviceType;
	//操作类型
	private int operateType;
	
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public OneKeyDoneChecker(String inXml) {
		callXml = inXml;
	}
	
	/**
	 * 参数合法性检查
	 */
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
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			serviceType = StringUtil.getIntegerValue(param
					.elementTextTrim("ServiceType"));
			operateType = StringUtil.getIntegerValue(param
					.elementTextTrim("OperateType"));
			searchType = StringUtil.getIntegerValue(param
					.elementTextTrim("SearchType"));
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			devSn = param.elementTextTrim("DevSN");
			cityId = param.elementTextTrim("CityId");

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck() || false == devSnCheck()
				|| false == cityIdCheck() || false == searchTypeCheck()
				|| false == serviceTypeCheck() || false == operateTypeCheck()) {
			return false;
		}

		result = 0;
		resultDesc = "业务下发成功";
		
		return true;
	}

	/**
	 * 绑定类型合法性检查
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-21
	 * @return void
	 */
	private boolean serviceTypeCheck() {
		logger.debug("serviceTypeCheck()");
		if (10 != serviceType && 11 != serviceType
				&& 14 != serviceType && 0 != serviceType) {
			result = 1001;
			resultDesc = "业务类型非法";
			return false;
		}
		return true;
	}
	
	/**
	 * 绑定类型合法性检查
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-21
	 * @return void
	 */
	private boolean operateTypeCheck() {
		logger.debug("operateTypeCheck()");
		if (1 != operateType) {
			result = 1010;
			resultDesc = "操作类型非法";
			return false;
		}
		return true;
	}
	
	
	/**
	 * 返回结果字符串
	 */
	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		if ("nx_dx".equals(Global.G_instArea)) {
			document.setXMLEncoding(Global.codeTypeValue);
		} else {
			document.setXMLEncoding("GBK");
		}
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);

		return document.asXML();
	}
	
	
	
	public int getServiceType() {
		return serviceType;
	}
	
	public void setServiceType(int serviceType) {
		this.serviceType = serviceType;
	}
	
	public int getOperateType() {
		return operateType;
	}
	
	public void setOperateType(int operateType) {
		this.operateType = operateType;
	}
	
	
	
}
