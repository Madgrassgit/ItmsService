
package com.linkage.stbms.pic.service;

import java.io.StringReader;
import java.util.UUID;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;
import ACS.Rpc;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.obj.BaseChecker;
import com.linkage.stbms.pic.Global;

/**
 * 业务下发对象
 * 
 * @author Jason(3412)
 * @date 2010-6-21
 */
public class ZeroConfChecker extends BaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(ZeroConfChecker.class);
	// device id
	private String deviceId;
	private String custAccount;
	private DevRpc devRpc;
	private String callId;
	private String clientId;
	private String type;
	private String priority;

	

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public ZeroConfChecker(String inXml)
	{
		callXml = inXml;
	}

	/**
	 * 参数合法性检查
	 */
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			deviceId = param.elementTextTrim("deviceId");
			custAccount = param.elementTextTrim("custAccount");

			callId = "ItmsService" + UUID.randomUUID();
			clientId = "ItmsService" + UUID.randomUUID();
			type = "1";
			priority = "1";
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == deviceIdCheck() || false == custAccountCheck())
		{
			return false;
		}
		result = 0;
		resultDesc = "业务下发成功";
		return true;
	}

	/**
	 * 
	 * @return
	 */
	boolean deviceIdCheck(){
		if(StringUtil.IsEmpty(deviceId)){
			result = 1001;
			resultDesc = "deviceid不能为空";
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	boolean custAccountCheck(){
		if(StringUtil.IsEmpty(custAccount)){
			result = 1001;
			resultDesc = "custAccount不能为空";
			return false;
		}
		return true;
	}
	/**
	 * 返回结果字符串
	 */
	@Override
	public String getReturnXml()
	{
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
		//设备ID
		root.addElement("deviceCallRpcServOBJ").addElement("devId").addText(StringUtil.getStringValue(deviceId));
		//callId of call
		root.element("deviceCallRpcServOBJ").addElement("callId").addText(StringUtil.getStringValue(callId));
		//clientId of call
		root.element("deviceCallRpcServOBJ").addElement("clientId").addText(StringUtil.getStringValue(clientId));
		//命令类型
		root.element("deviceCallRpcServOBJ").addElement("type").addText(StringUtil.getStringValue(type));
		//call priority
		root.element("deviceCallRpcServOBJ").addElement("priority").addText(StringUtil.getStringValue(priority));
		//rpc信息
		if(null != devRpc && null!=devRpc.rpcArr){
			for (int i = 0; i < devRpc.rpcArr.length; i++) {
				root.element("deviceCallRpcServOBJ").addElement("rpcInfo")
				.addElement("rpcId").addText(devRpc.rpcArr[i].rpcId).getParent()
				.addElement("rpcName").addText(devRpc.rpcArr[i].rpcName).getParent()
				.addElement("rpcValue").addText(devRpc.rpcArr[i].rpcValue);
			}
		}
		return document.asXML();
	}

	public static void main(String[] args)
	{
		String inXml = "<?xml version=\"1.0\" encoding=\"GBK\"?>"+
						"<root>"+
							"<CmdID>123456789012345</CmdID>"+
							"<CmdType> CX_01</CmdType>"+
							"<ClientType>3</ClientType>"+
							"<Param>"+
								"<deviceId>njkd123456</deviceId>"+
							"</Param>"+
						"</root>";
		ZeroConfChecker conChecker = new ZeroConfChecker(inXml);
		conChecker.check();
		
		Rpc[] rpcarr = new Rpc[2];
		rpcarr[0] = new Rpc("id111", "name111", "value111");
		rpcarr[1] = new Rpc("id222", "name222", "value222");
		DevRpc devRpc = new DevRpc(conChecker.getDeviceId(), rpcarr);
		
		conChecker.setDevRpc(devRpc);
		conChecker.setCallId("call123");
		conChecker.setClientId("client123");
		conChecker.setType("类型1");
		conChecker.setPriority("priority123");
		
		System.out.println(conChecker.getReturnXml());
	}
	public String getDeviceId()
	{
		return deviceId;
	}

	
	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}

	public String getCustAccount() {
		return custAccount;
	}

	public void setCustAccount(String custAccount) {
		this.custAccount = custAccount;
	}

	public DevRpc getDevRpc()
	{
		return devRpc;
	}
	public void setDevRpc(DevRpc devRpc)
	{
		this.devRpc = devRpc;
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	

}
