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

public class BridgeToRoutChecker extends BaseChecker{

	public static final Logger logger = LoggerFactory.getLogger(BridgeToRoutChecker.class);
	
	// 逻辑SN
	private String userSn = null;
	// 逻辑Id
	private String userLoid = null;
	
	// 失败原因
	private String failureReason = null;
	
	// 成功状态
	private String succStatus = null;
	
	private String operateType = "";
	
	private String offlineEnable = null;
	
	private String broadbandPassword = null;
	
	// 安徽电信路由账号
  	private String routUsername = null;
  	
  	// 安徽电信路由密码
  	private String routPassword = null;

	
	/**
	 * 构造函数
	 * 
	 * 入参 inXml  XML格式
	 * 
	 * @param inXml
	 */
	public BridgeToRoutChecker(String inXml) {
		callXml = inXml;
	}
	
	
	
	/**
	 * 检查调用接口入参的合法性
	 * 
	 */
	@Override
	public boolean check(){
		logger.debug("check()");
		
		SAXReader reader = new SAXReader();
		Document document = null;
		userSn = "";
		failureReason = "";
		succStatus = "";
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			if("js_dx".equals(Global.G_instArea))
			{
				userInfo = param.elementTextTrim("UserName");
				userLoid = param.elementTextTrim("UserLOID");
			} else {
				userInfo = param.elementTextTrim("UserInfo");
			}
			if("jx_dx".equals(Global.G_instArea) || "nx_dx".equals(Global.G_instArea) || "hlj_dx".equals(Global.G_instArea)
			|| "nmg_dx".equals(Global.G_instArea)|| "ah_dx".equals(Global.G_instArea))
			{
				operateType = param.elementTextTrim("OperateType");
			}
      
      		if ("ah_dx".equals(Global.G_instArea)) {
    	  		this.routUsername = param.elementTextTrim("UserName");
    	  		this.routPassword = param.elementTextTrim("PassWord");
      		}
      
      		if ("jl_dx".equals(Global.G_instArea))
		    {
		        operateType = param.elementTextTrim("wanType");
		    }
			if ("xj_dx".equals(Global.G_instArea))
			{
				// 新疆 默认为路由方式
				operateType = "2";
			}
			
			if ("jx_dx".equals(Global.G_instArea))
			{
				cityId = param.elementTextTrim("CityId");
			}
			if("cq_dx".equals(Global.G_instArea)){
				offlineEnable = StringUtil.getStringValue(param.elementTextTrim("OfflineEnable"));
				broadbandPassword = StringUtil.getStringValue(param.elementTextTrim("BroadbandPassword"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		//参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}
		if("jx_dx".equals(Global.G_instArea))
		{
			if(false == operateTypeCheck() || false == cityIdCheck())
			{
				return false;
			}
		}
		
		result = 0;
		resultDesc = "成功";
		
		return true;
	}
	
	boolean operateTypeCheck(){
		if(StringUtil.IsEmpty(operateType)){
			result = 1002;
			resultDesc = "操作类型不合法";
			return false;
		}
		if(!"1".equals(operateType) && !"2".equals(operateType))
		{
			result = 1002;
			resultDesc = "操作类型不合法";
			return false;
		}
		return true;
	}
	
	
	/**
	 * 组装XML字符串，并返回
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
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		// 逻辑SN
		root.addElement("SN").addText(userSn);
		// 失败原因
		root.addElement("FailureReason").addText(failureReason);
		// 成功状态
		root.addElement("SuccStatus").addText(succStatus);
		
		return document.asXML();
	}
	
	public String getUserSn() {
		return userSn;
	}

	public void setUserSn(String userSn) {
		this.userSn = userSn;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public String getSuccStatus() {
		return succStatus;
	}

	public void setSuccStatus(String succStatus) {
		this.succStatus = succStatus;
	}
	
	public String getOperateType()
	{
		return operateType;
	}

	public void setOperateType(String operateType)
	{
		this.operateType = operateType;
	}

	public String getUserLoid()
	{
		return userLoid;
	}
	
	public void setUserLoid(String userLoid)
	{
		this.userLoid = userLoid;
	}
	
	public String getOfflineEnable() {
		return offlineEnable;
	}

	public void setOfflineEnable(String offlineEnable) {
		this.offlineEnable = offlineEnable;
	}

	public String getBroadbandPassword() {
		return broadbandPassword;
	}
	
	public void setBroadbandPassword(String broadbandPassword) {
		this.broadbandPassword = broadbandPassword;
	}

	public String getRoutUsername() {
		return routUsername;
	}

	public void setRoutUsername(String routUsername) {
		this.routUsername = routUsername;
	}

	public String getRoutPassword() {
		return routPassword;
	}

	public void setRoutPassword(String routPassword) {
		this.routPassword = routPassword;
	}
}
