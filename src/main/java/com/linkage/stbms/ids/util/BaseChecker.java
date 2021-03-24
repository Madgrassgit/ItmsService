package com.linkage.stbms.ids.util;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 接口数据检查基类(抽象类)
 * 
 * @author Jason(3412)
 * @date 2010-6-17
 */
public abstract class BaseChecker {
	
	private static Logger logger = LoggerFactory.getLogger(BaseChecker.class);
	
	// MAC地址的正则表达数
	static Pattern macPattern = Pattern
			.compile("^[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}+:[a-fA-F0-9]{2}$");

	/**
	 * 设备序列号正则表达式
	 */
	private static String devSnPattern = "\\w{1,}+";
	
	private static String ipPattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	// 调用ID
	protected String cmdId;
	
	// 调用类型：CX_01,固定
	protected String cmdType;
	
	// 调用客户端类型：1：BSS 2：IPOSS 3：综调 4：RADIUS
	protected int clientType;
	
	String userName;

	/**
	 * 查询类型
	 * 1：根据业务帐号查询
	 * 2：根据MAC地址查询
	 */
	String selectType;
	
	/**
	 * 查询类型所对应的用户信息
	 * SelectType为1时为itv业务账号
	 * SelectType为2时为机顶盒MAC
	 */
	String userInfo;
	
	/**
	 * 查询类型
	 * 1： 业务帐号
	 * 2：机顶盒MAC
	 * 3：机顶盒序列号
	 */
	String searchType;
	
	/**
	 * 查询类型searchType对应的值
	 * searchType=1时， 业务帐号
	 * searchType=2时，机顶盒MAC
	 * searchType=3时，机顶盒序列号
	 */
	String searchInfo;
	
	/**
	 * 需要绑定的业务账号
	 */
	String userAccount; 
	
	String ip;

	
	/**
	 * 返回代码
	 */
	protected String rstCode;
	
	/**
	 * 返回描述
	 */
	protected String rstMsg;
	
	/**
	 * 检查客户端的XML字符串是否合法，如果合法将字符串转换成对象的属性
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-4-1
	 * @return boolean
	 */
	public abstract boolean check();

	/**
	 * 返回调用结果
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-4-1
	 * @return String
	 */
	public abstract String getReturnXml();

	
	public boolean baseCheck(){
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)){
			rstCode = "1000";
			rstMsg = "接口调用唯一ID非法";
			return false;
		}
		
		if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType
				&& 5 != clientType && 6 != clientType && 7 != clientType && 8 != clientType && 9 != clientType) {
			rstCode = "2";
			rstMsg = "客户端类型非法";
			return false;
		}
		
		if(false == "CX_01".equals(cmdType)){
			rstCode = "3";
			rstMsg = "接口类型非法";
			return false;
		}
		
		return true;
	}
	
	/**
	 * 查询方式合法性检查
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	boolean selectTypeCheck(){
		if(!"1".equals(selectType) && !"2".equals(selectType)){
			rstCode = "1001";
			rstMsg = "查询类型非法";
			return false;
		}
		return true;
	}
	
	
	/**
	 * 用户信息合法性检查
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	boolean userInfoCheck(){
		if(StringUtil.IsEmpty(userInfo)){
			rstCode = "1002";
			rstMsg = "业务帐号或MAC地址不能为空";
			return false;
		}
		return true;
	}
	
	
	/**
	 * 查询类型
	 * 1： 业务帐号
	 * 2：机顶盒MAC
	 * 3：机顶盒序列号
	 * @return
	 */
	boolean searchTypeCheck(){
		if(StringUtil.IsEmpty(searchType)){
			rstCode = "1003";
			rstMsg = "字段searchType不能为空";
			return false;
		}
		
		if (!"1".equals(searchType) && !"2".equals(searchType) && !"3".equals(searchType)) {
			rstCode = "1001";
			rstMsg = "查询类型非法";
			return false;
		}
		return true;
	}
	
	
	
	boolean searchInfoCheck(){
		if(StringUtil.IsEmpty(searchInfo)){
			rstCode = "1002";
			rstMsg = "字段searchInfo不能为空";
			return false;
		}
		// 2：机顶盒MAC
		else if ("2".equals(searchType)) {
			if(false == macPattern.matcher(searchInfo).matches()){
				rstCode = "1004";
				rstMsg = "MAC地址不合法";
				return false;
			}
		}
		// 3：机顶盒序列号
		else if ("3".equals(searchType)) {
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
	 * 业务账号检查
	 * 不能为空
	 * @return
	 */
	boolean userAccountCheck(){ 
		if(StringUtil.IsEmpty(userAccount)){
			rstCode = "1003";
			rstMsg = "字段userAccount不能为空";
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * 设备序列号合法性检查
	 * 
	 * @return
	 */
	boolean devSnCheck(){
		if ("3".equals(searchType)){
			
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
	 * IP地址合法性验证
	 * 
	 * @return
	 */
	boolean ipCheck(){
		
		if(StringUtil.IsEmpty(ip)){
			rstCode = "1006";
			rstMsg = "设备IP不能为空";
			return false;
		}
		// 验证IP地址
		else {
			String [] ipArray = ip.split(",");
			Pattern devIpPattern = Pattern.compile(ipPattern); 
			
			for (int i = 0; i < ipArray.length; i++) {
				
				if(false == devIpPattern.matcher(ipArray[i]).matches()){
					rstCode = "1007";
					rstMsg = "IP地址不合法";
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	/**
	 * 设备MAC地址合法性检查
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	boolean devMacCheck(){
		if ("2".equals(selectType) && false == macPattern.matcher(userInfo).matches()) {
			rstCode = "1005";
			rstMsg = "MAC地址不合法";
			return false;
		}
		return true;
	}
	
	
	
	
	public String getCmdId() {
		return cmdId;
	}

	public void setCmdId(String cmdId) {
		this.cmdId = cmdId;
	}

	public String getCmdType() {
		return cmdType;
	}

	public void setCmdType(String cmdType) {
		this.cmdType = cmdType;
	}

	public int getClientType() {
		return clientType;
	}

	public void setClientType(int clientType) {
		this.clientType = clientType;
	}

	public String getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}

	public String getSelectType() {
		return selectType;
	}

	public void setSelectType(String searchType) {
		this.selectType = searchType;
	}

	
	
	public String getSearchType() {
		return searchType;
	}

	
	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

	
	public String getSearchInfo() {
		return searchInfo;
	}

	
	public void setSearchInfo(String searchInfo) {
		this.searchInfo = searchInfo;
	}

	
	public String getIp() {
		return ip;
	}

	
	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getRstCode() {
		return rstCode;
	}

	
	public void setRstCode(String rstCode) {
		this.rstCode = rstCode;
	}

	
	public String getRstMsg() {
		return rstMsg;
	}

	
	public void setRstMsg(String rstMsg) {
		this.rstMsg = rstMsg;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
