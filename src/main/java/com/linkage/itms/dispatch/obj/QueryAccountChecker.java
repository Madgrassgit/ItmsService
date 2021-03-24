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
 * 江苏电信ITMS与宗调业务帐号查询接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2016年12月06日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryAccountChecker extends BaseChecker{

private static final Logger logger = LoggerFactory.getLogger(QueryAccountChecker.class);
	
	
	private String inParam = null;

	//语音业务查询结果
	private String Username = "";
	
	//语音业务密码查询结果
	private String Password = "";
	
	//IPTV业务查询结果
	private String ItvUsername = "";
	
	//语音业务查询结果
	private String AuthUserName = "";
	
	//语音业务密码查询结果
	private String AuthPassword = "";
	
	public QueryAccountChecker(String inParam){
		this.inParam = inParam;
	}
	
	
	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check(){
		
		logger.debug("QueryAccountChecker==>check()");
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {

			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			
			//输入信息类型
			searchType = StringUtil.getIntegerValue(param.elementTextTrim("SearchType"));
			
			//输入设备序列号
			devSn = param.elementTextTrim("DevSN"); 
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		
		//参数合法性检查
		if (false == baseCheck() || false == searchTypeCheck()
				|| false == devSnCheck() ) {
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		
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
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		
		//宽带查询结果
		if(!StringUtil.IsEmpty(Username)&&";".equals(Username.substring(Username.length()-1))){
			Username = Username.substring(0, Username.length()-1);
		}
		root.addElement("Username").addText(Username);
		if(!StringUtil.IsEmpty(Password)&&";".equals(Password.substring(Password.length()-1))){
			Password = Password.substring(0, Password.length()-1);
		}
		root.addElement("Password").addText(Password);
		
		//IPTV查询结果
		if(!StringUtil.IsEmpty(ItvUsername)&&";".equals(ItvUsername.substring(ItvUsername.length()-1))){
			ItvUsername = ItvUsername.substring(0, ItvUsername.length()-1);
		}
		root.addElement("ItvUsername").addText(ItvUsername);
		
		//语音业务查询结果
		root.addElement("AuthUserName").addText(AuthUserName);
		root.addElement("AuthPassword").addText(AuthPassword);		
		
		return document.asXML();
	}
	
	
	/**
	 * 基本信息合法性检查
	 * 
	 * @author fanjm(35572)
	 * @date 2016-12-6
	 * @return boolean 是否校验通过
	 */
	public boolean baseCheck(){
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)){
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		
		//1：BSS 2：IPOSS 3：综调 4：RADIUS 5：智能诊断系统
		if(3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType){
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}
		
		if(false == "CX_01".equals(cmdType)){
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		
		return true;
	}
	
	/**
	 * 查询方式合法性检查
	 * 
	 * @author fanjm
	 * @date 2016-12-6
	 * @return boolean
	 */
	boolean searchTypeCheck(){
		if(2 != searchType){
			result = 1001;
			resultDesc = "信息类型非法";
			return false;
		}
		return true;
	}
	
	/**
	 * 设备序列号合法性检查
	 * 
	 * @author fanjm
	 * @date 2016-12-6
	 * @return boolean
	 */
	protected boolean devSnCheck(){
		if(false == pattern.matcher(devSn.replaceAll("-", "")).matches() || devSn.length() < 6){
			result = 1005;
			resultDesc = "设备序列号非法";
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


	public String getUsername() {
		return Username;
	}


	public String getPassword() {
		return Password;
	}


	public String getItvUsername() {
		return ItvUsername;
	}


	public String getAuthUserName() {
		return AuthUserName;
	}


	public String getAuthPassword() {
		return AuthPassword;
	}


	public void setUsername(String username) {
		Username = username;
	}


	public void setPassword(String password) {
		Password = password;
	}


	public void setItvUsername(String itvUsername) {
		ItvUsername = itvUsername;
	}


	public void setAuthUserName(String authUserName) {
		AuthUserName = authUserName;
	}


	public void setAuthPassword(String authPassword) {
		AuthPassword = authPassword;
	}


}
