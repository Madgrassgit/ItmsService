package com.linkage.itms.nmg.dispatch.obj;

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
 * 山东电信修改宽带密码接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2016年12月2日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class BizupdateNetPwdChecker extends NmgBaseChecker{

private static final Logger logger = LoggerFactory.getLogger(BizupdateNetPwdChecker.class);
	
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
	public BizupdateNetPwdChecker(String inParam){
		this.inParam = inParam;
	}
	
	
	
	
	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check(){
		
		logger.debug("UpdateNetPwdChecker==>check()");
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			
			//Loid
			loid = param.elementTextTrim("loid");
			
			//宽带账号
			netUserName = param.elementTextTrim("netUserName"); 
			
			//宽带密码
			netPwd = param.elementTextTrim("netPwd"); 
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		
		//参数合法性检查
		if (false == baseCheck() || false == loidCheck() || false == netUserNameCheck() || false == netPwdCheck()) {
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
	
}
