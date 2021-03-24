package com.linkage.itms.dispatch.gsdx.obj;

import org.dom4j.Document;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

/**
 * 重庆公共服务类
 *
 */
public class BaseDealXML {
	
	protected String inXml;
	// 操作流水号
	protected String opId = "";
	// 逻辑id
	protected String logicId = "";
	// 宽带账号
	protected String pppUsename = "";
	// 设备序列号
	protected String serialNumber = "";
	// 客户号
	protected String customerId = "";
	// 结果值
	protected String result = "0";
	// 结果描述
	protected String errMsg = "";
	// 调用原接口的返回值
	protected String resultXML = "";
	// 调用改校验类的接口名称
	protected String methodName = "";

	
	public BaseDealXML(String methodName)
	{
		super();
		this.methodName = methodName;
	}

	/**
	 * 记录日志
	 * 
	 * @param  _cmdName：调用方法名称 _username：用户账号
	 *            _devSn：终端序列号  _reqInfo：调用接口字符串  _respInfo：回复字符串
	 * @return void
	 */
	public void recordLog(String _cmdName, String _username, String _devSn,
			String _reqInfo, String _respInfo) {

		String strSQL = "insert into log_gtms_service ("
				+ " serv_id,itfs_id,client_type_id,cmd_name,username,"
				+ " device_sn,city_id,resp_code,req_info,resp_info,"
				+ " itfs_time) values " + " (?,?,?,?,?,   ?,?,?,?,?,   ?)";

		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, Math.round(Math.random() * 1000000000));
		psql.setString(2, opId); // 调用接口唯一ID
		psql.setInt(3, 3);	// 客户端ID类型
		psql.setString(4, _cmdName);
		psql.setString(5, _username);
		psql.setString(6, _devSn);
		psql.setString(7, ""); // cityId
		psql.setInt(8, Integer.parseInt(result)); // 结果码
		psql.setString(9, _reqInfo);
		psql.setString(10, _respInfo);
		psql.setLong(11, System.currentTimeMillis() / 1000);

		DBOperation.executeUpdate(psql.getSQL());
	}

	public Document getXML(String inXml) {
		return null;
	}

	public String returnXML() {
		return null;
	}

	public String getOpId() {
		return opId;
	}

	public void setOpId(String opId) {
		this.opId = opId;
	}

	public String getLogicId() {
		return logicId;
	}

	public void setLogicId(String logicId) {
		this.logicId = logicId;
	}

	public String getPppUsename() {
		return pppUsename;
	}

	public void setPppUsename(String pppUsename) {
		this.pppUsename = pppUsename;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getResultXML() {
		return resultXML;
	}

	public void setResultXML(String resultXML) {
		this.resultXML = resultXML;
	}

	
	public String getInXml()
	{
		return inXml;
	}

	
	public void setInXml(String inXml)
	{
		this.inXml = inXml;
	}
	
	public String getMethodName()
	{
		return methodName;
	}

	
	public void setMethodName(String methodName)
	{
		this.methodName = methodName;
	}
}
