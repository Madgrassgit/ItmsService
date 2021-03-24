package com.linkage.itms.ct.obj;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 网厅接口数据检查基类(抽象类)
 * 
 * @author Jason(3412)
 * @date 2010-7-13
 */
public abstract class CtBaseChecker {

	private static Logger logger = LoggerFactory.getLogger(CtBaseChecker.class);

	// 正则，字符加数字
	static Pattern pattern = Pattern.compile("\\w{1,}+");

	// 客户端调用XML字符串
	String callXml;
	// 调用ID
	String cmdId;
	// 调用类型：CX_01, CX_02, CX_03
	String cmdType;
	// 调用客户端类型：1：BSS 2：IPOSS 3：综调 4：RADIUS 5:网厅
	int clientType;

	// 用户信息
	String username;

	// 终端信息
	String devSn;

	// 查询结果
	int result;
	// 查询结果描述
	String resultDesc;

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

	/**
	 * XML接口基本参数检查
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-7-13
	 * @return boolean
	 */
	boolean baseCheck() {
		logger.debug("baseCheck()");

		if (StringUtil.IsEmpty(cmdId)) {
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}

		if (5 != clientType) {
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}

		if (false == cmdTypeCheck()) {
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}

		return true;
	}

	/**
	 * 接口类型合法性判断
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-7-13
	 * @return boolean
	 */
	abstract boolean cmdTypeCheck();

	/**
	 * 用户信息合法性检查
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	boolean usernameCheck() {
		logger.debug("usernameCheck()");
		if (false == pattern.matcher(username).matches()) {
			result = 1002;
			resultDesc = "用户信息不合法";
			return false;
		}
		return true;
	}

	/**
	 * 设备序列号合法性检查
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	boolean devSnCheck() {
		logger.debug("devSnCheck()");
		if (false == pattern.matcher(devSn).matches() || devSn.length() < 6) {
			result = 1005;
			resultDesc = "设备序列号不合法";
			return false;
		}
		return true;
	}

	
	/** getter, setter methods */

	public String getCallXml() {
		return callXml;
	}

	public void setCallXml(String callXml) {
		this.callXml = callXml;
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

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getResultDesc() {
		return resultDesc;
	}

	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDevSn() {
		return devSn;
	}

	public void setDevSn(String devSn) {
		this.devSn = devSn;
	}

}
