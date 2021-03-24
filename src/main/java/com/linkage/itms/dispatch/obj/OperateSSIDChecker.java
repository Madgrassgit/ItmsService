package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.obj.ParameValueOBJ;


public class OperateSSIDChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(OperateSSIDChecker.class);
	
	private String  Enable1 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.Enable";
	private String  Enable2 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.2.Enable";
	
	private String username = "";
	
	private int isSucc = 0;
	
	private int operateType = 0 ;
	
	private String ssidType = "";
	
	private ParameValueOBJ pvOBJ = null;
	
	public OperateSSIDChecker(String inXml,String ssidtype){
		callXml = inXml;
		ssidType = ssidtype;
	}
	public boolean check() {
		
		logger.debug("Enable1==>check()");
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			username = param.elementTextTrim("UserName");
			
			operateType = StringUtil.getIntegerValue(param.elementTextTrim("OperateType"));
			
			pvOBJ = new ParameValueOBJ();
			if(ssidType.equals("1")){
				pvOBJ.setName(Enable1);
			}else{
				pvOBJ.setName(Enable2);
			}
			if(operateType==1){
				pvOBJ.setValue("1");
			}else{
				pvOBJ.setValue("0");
			}
			pvOBJ.setType("3");
			
			
		} catch (Exception e) {
			e.printStackTrace();
			result = 1 ;
			isSucc = 1 ;
			resultDesc = "入参格式错误";
			return false;
		}
		
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()) {
			return false;
		}
		// userInfoType == 6 表示 userInfo 入的是设备序列号，如果是设备序列号，那么设备序列号至少为后6位
		if (6 == userInfoType) {
			if (userInfo.length() < 6) {
				result = 1007;
				resultDesc = "设备序列号长度不能小于6位";
				return false;
			}
		}
		
		result = 0;
		resultDesc = "节点值设置成功";
		
		return true;
	}
	
	/**
	 * 回参
	 */
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		//修改密码是否成功
		root.addElement("IsSuccess").addText("" + isSucc);
		// 结果描述
		root.addElement("NoReason").addText("" + resultDesc);
		
		return document.asXML();
	}
	
	
	
	public ParameValueOBJ getPvOBJ() {
		return pvOBJ;
	}
	public void setPvOBJ(ParameValueOBJ pvOBJ) {
		this.pvOBJ = pvOBJ;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public int getOperateType() {
		return operateType;
	}
	public void setOperateType(int operateType) {
		this.operateType = operateType;
	}
	public String getSsidType() {
		return ssidType;
	}
	public void setSsidType(String ssidType) {
		this.ssidType = ssidType;
	}
	public int getIsSucc() {
		return isSucc;
	}
	public void setIsSucc(int isSucc) {
		this.isSucc = isSucc;
	}
	public static void main(String[] args) {
		StringBuffer inParam = new StringBuffer();
		inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
		inParam.append("<root>\n");
		inParam.append("	<CmdID>123456789012345</CmdID>       \n");
		inParam.append("	<CmdType>CX_01</CmdType>           \n");
		inParam.append("	<ClientType>3</ClientType>         \n");
		inParam.append("	<Param>                            \n");
		inParam.append("		<UserInfoType>2</UserInfoType>      \n");
		inParam.append("		<UserInfo>123456789</UserInfo>  \n");
		inParam.append("		<ParameterList>  \n");
		inParam.append("			<ParameterValueStruct>  \n");
		inParam.append("				<Name>InternetGatewayDevice.WANDevice.WANConnectionDevice.2.WANPPPConnection.Username</Name>  \n");
		inParam.append("				<Value>noc_test</Value>  \n");
		inParam.append("			</ParameterValueStruct>  \n");
		inParam.append("			<ParameterValueStruct>  \n");
		inParam.append("				<Name>InternetGatewayDevice.WANDevice.WANConnectionDevice.2.WANPPPConnection.Password</Name>  \n");
		inParam.append("				<Value>noc123</Value>  \n");
		inParam.append("			</ParameterValueStruct>  \n");
		inParam.append("		</ParameterList>  \n");
		inParam.append("	</Param>  \n");
		inParam.append("</root>  \n");
		
		SetParameterValuesChecker checker = new SetParameterValuesChecker(inParam.toString());
		checker.check();
	}
}
