package com.linkage.itms.message;

/**
 * 向绑定模块发送消息bean
 * 
 * @author jiafh (Ailk NO.)
 * @version 1.0
 * @since 2016-11-3
 * @category com.linkage.module.gwms.util.message
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 
 */
public class BindInfoMessage {

	// 方法名
	private String methodName;

	// 唯一标识
	private String clientId;

	// 优先级
	private String priority;

	// 绑定信息
	private BindBean[] BindBeanArr;

	// 类型
	private String type;

	// 操作类型
	private String operate;

	// 参数
	private String parameter;

	// 用户名
	private String userName;

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public BindBean[] getBindBeanArr() {
		return BindBeanArr;
	}

	public void setBindBeanArr(BindBean[] bindBeanArr) {
		BindBeanArr = bindBeanArr;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOperate() {
		return operate;
	}

	public void setOperate(String operate) {
		this.operate = operate;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
