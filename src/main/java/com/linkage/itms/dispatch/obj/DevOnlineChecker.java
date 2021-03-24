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

/**
 * 设备是否在线状态接口格式检查
 * 
 * @author Jason(3412)
 * @date 2010-9-2
 */
public class DevOnlineChecker extends BaseQueryChecker {

	// 日志记录
	private static Logger logger = LoggerFactory
			.getLogger(DevOnlineChecker.class);

	// 1：在线 -1：不在线
	private int onlineStatus;
	// 注册时间:YYYY-MM-DD hh:mm:ss, 样例：2016-03-11 12:33:00
	private String regTime;
	
	// 查询用户帐号
	private String username;
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public DevOnlineChecker(String inXml) {
		callXml = inXml;
	}
	/**
	 * 检查调用接口入参的合法性
	 * 
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
			searchType = StringUtil.getIntegerValue(param
					.elementTextTrim("SearchType"));
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			username = param.elementTextTrim("UserName");
			devSn = param.elementTextTrim("DevSN");
			if(!"nmg_dx".equals(Global.G_instArea) && !"nx_dx".equals(Global.G_instArea) && !"CUC".equalsIgnoreCase(Global.G_OPERATOR)){
				cityId = param.elementTextTrim("CityId");
			}
			

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		if ("nx_dx".equals(Global.G_instArea)) {
			if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType) {
				result = 2;
				resultDesc = "客户端类型非法";
				return false;
			}
			if (1 != userInfoType && 2 != userInfoType && 3 != userInfoType
					&& 4 != userInfoType && 5 != userInfoType) {
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
		}
		
		// 参数合法性检查
		if (false == baseCheck() || false == devSnCheck()
				|| false == cityIdCheck() || false == searchTypeCheck() || false == userInfoTypeCheck()) {
			return false;
		}
		
		// 用户账号合法性检查
		if (1 == searchType && StringUtil.IsEmpty(username)) {
			result = 1002;
			resultDesc = "用户帐号不合法";
			return false;
		}

		result = 0;
		resultDesc = "成功";

		return true;
	}
	
	public boolean cityIdCheck(){
		if("nx_dx".equals(Global.G_instArea) || "nmg_dx".equals(Global.G_instArea) || "CUC".equalsIgnoreCase(Global.G_OPERATOR)){
			return true;
		}
		if(StringUtil.IsEmpty(cityId) || false == Global.G_CityId_CityName_Map.containsKey(cityId)){
			result = 1007;
			resultDesc = "属地非法";
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.linkage.itms.dispatch.obj.BaseChecker#getReturnXml()
	 */
	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		//返回结果
		
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
		
		Element paramEle = root.addElement("Param");
		// 设备在线状态
		paramEle.addElement("OnlineStatus").addText(
				StringUtil.getStringValue(onlineStatus));
		
//		if("nx_dx".equals(Global.G_instArea)){
//			// 宁夏电信回参需返回cityid
//			paramEle.addElement("CityId ").addText(
//					StringUtil.getStringValue(cityId));
//		}
		
		if("nx_dx".equals(Global.G_instArea)){
			// 宁夏电信回参需返回 regTime（注册时间）
			paramEle.addElement("regTime").addText(
					StringUtil.getStringValue(regTime));
		}

		return document.asXML();
	}

	
	/** getter, setter methods */
	
	public int getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(int onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public String getRegTime() {
		return regTime;
	}

	public void setRegTime(String regTime) {
		this.regTime = regTime;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
}
