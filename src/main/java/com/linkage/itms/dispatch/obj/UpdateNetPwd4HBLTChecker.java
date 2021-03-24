package com.linkage.itms.dispatch.obj;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 山东电信修改宽带密码接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2016年12月2日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class UpdateNetPwd4HBLTChecker extends BaseChecker{

private static final Logger logger = LoggerFactory.getLogger(UpdateNetPwd4HBLTChecker.class);
	
	/**
	 * 宽带账号
	 */
	private String netUserName = null  ;

	/**
	 * 宽带密码
	 */
	private String netPwd = null;
	
	private String inParam = null;
	private String wan_type = null;
	private String servType = null;
	
	
	
	
	/**
	 * 
	 * 检查入参合法性
	 * @param newPassWord 
	 * @param orderType 
	 * @param lSHNo 
	 * @param adAcount 
	 * 
	 * @return
	 */
	public String check(String adAcount, String lSHNo, String orderType, String newPassWord){
		
		logger.debug("UpdateNetPwd4HBLTChecker==>check()");
			
		//宽带账号
		netUserName = adAcount; 
		
		//宽带密码
		netPwd = newPassWord;
		int index = netPwd.indexOf("password=") ;
		if(index==-1){
			return "-6";
		}
		netPwd = netPwd.substring(index + "password=".length());
		
		if("wband-X".equals(orderType)){
			servType = "1";
		}
		else if("iptv-X".equals(orderType)){
			servType = "2";
		}
		else{
			servType = "0";
		}
		
		if(!loidServType()){
			return "-3";
		}
		
		
		if (false == loidServType() || false == netPwdCheck()) {
			return "-6";
		}
		
		return "1";
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
		
		
		return document.asXML();
	}
	
	
	/**
	 * 基本信息合法性检查
	 * 
	 * @author fanjm(35572)
	 * @date 2016-12-2
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
		if(3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType && 5 != clientType){
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
	 * 用户逻辑标识合法性检查
	 * 
	 * @author fanjm(35572)
	 * @date 2016-12-2
	 * @return boolean 是否校验通过
	 */
	boolean loidCheck(){
		if(StringUtil.IsEmpty(loid)){
			result = 1;
			resultDesc = "用户逻辑标识非法";
			return false;
		}
		return true;
	}
	
	
	/**
	 * 业务类型合法性检查
	 * 
	 * @author fanjm(35572)
	 * @date 2016-12-2
	 * @return boolean 是否校验通过
	 */
	boolean loidServType(){
		if(StringUtil.IsEmpty(servType)){
			result = -3;
			resultDesc = "业务类型非法";
			return false;
		}else if(!"1".equals(servType) && !"2".equals(servType)){
			result = -3;
			resultDesc = "业务类型非法";
			return false;
		}
		return true;
	}
	
	/**
	 * 宽带账号合法性检查
	 * 
	 * @author fanjm(35572)
	 * @date 2016-12-2
	 * @return boolean 是否校验通过
	 */
	boolean netUserNameCheck(){
		if(StringUtil.IsEmpty(netUserName)){
			result = 1;
			resultDesc = "宽带账号非法";
			return false;
		}
		return true;
	}
	
	/**
	 * 宽带密码合法性检查
	 * 
	 * @author fanjm(35572)
	 * @date 2016-12-2
	 * @return boolean 是否校验通过
	 */
	boolean netPwdCheck(){
		if(StringUtil.IsEmpty(netPwd)){
			result = 1;
			resultDesc = "宽带密码非法";
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




	public String getNetUserName() {
		return netUserName;
	}




	public void setNetUserName(String netUserName) {
		this.netUserName = netUserName;
	}




	public String getNetPwd() {
		return netPwd;
	}




	public void setNetPwd(String netPwd) {
		this.netPwd = netPwd;
	}




	public String getWan_type() {
		return wan_type;
	}




	public void setWan_type(String wan_type) {
		this.wan_type = wan_type;
	}




	
	public String getServType()
	{
		return servType;
	}




	
	public void setServType(String servType)
	{
		this.servType = servType;
	}


	@Override
	public boolean check()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
}
