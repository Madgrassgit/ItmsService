package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.obj.ParameValueOBJ;


public class SetParameterValuesChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(SetParameterValuesChecker.class);
	
	
	private ArrayList<ParameValueOBJ> objList = new ArrayList<ParameValueOBJ>();
	
	
	public SetParameterValuesChecker(String inXml){
		callXml = inXml;
	}
	
	
	public boolean check() {
		
		logger.debug("SetParameterValuesChecker==>check()");
		
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
			userInfo = param.elementTextTrim("UserInfo");
			
			Element parameterList = param.element("ParameterList");
			List<Element> parameterValueStruct = parameterList.elements("ParameterValueStruct");
			
			boolean flag = true;
			
			for (Iterator iterator = parameterValueStruct.iterator(); iterator.hasNext(); ) {
				
				ParameValueOBJ obj = new ParameValueOBJ();
				
				Element element = (Element) iterator.next();
				String name = element.elementTextTrim("Name");
				if (null == name || "".equals(name)) {
					result = 2001;
					resultDesc = "节点<Name>的值不能为空";
					flag = false;
					break;
				}
				String value = element.elementTextTrim("Value");
				if (null == value || "".equals(value)) {
					result = 2002;
					resultDesc = "节点<Value>的值不能为空";
					flag = false;
					break;
				}
				
				obj.setName(name);
				obj.setValue(value);
				obj.setType("1");
				
				objList.add(obj);
				
			}
			
			if (!flag) {
				return false;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			result = 1 ;
			resultDesc = "入参格式错误";
			return false;
		}
		
		
		
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
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
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		
		return document.asXML();
	}
	
	
	
	
	public ArrayList<ParameValueOBJ> getObjList() {
		return objList;
	}


	
	public void setObjList(ArrayList<ParameValueOBJ> objList) {
		this.objList = objList;
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
