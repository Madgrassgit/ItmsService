package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.obj.ParameValueOBJ;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class GetParameterValuesChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(GetParameterValuesChecker.class);
	
	private String[] pathStr = null;
	
	private ArrayList<ParameValueOBJ> parameterValues = null;
	
	
	/**
	 * 构造函数 入参
	 * @param inXml
	 */
	public GetParameterValuesChecker(String inXml){
		callXml = inXml;
	}
	
	
	/**
	 * 参数合法性检查
	 */
	public boolean check() {
		
		logger.debug("check()");
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			
			// 获取ParameterNames下的所有节点
			Element parameterValuesPath = param.element("ParameterNames");
			// 获取 string 节点
			List<Element> PathStrList = parameterValuesPath.elements("string");
			
			StringBuffer sb = new StringBuffer("");
			
			// 将string节点的值，拼接
			for(Iterator it = PathStrList.iterator();it.hasNext();){
				Element PathStr = (Element)it.next();
				sb.append(PathStr.getText()).append(";");
			}
			
			int endIndex = sb.toString().lastIndexOf(";");
			// 将 string 节点里的值，转化成字符串数组
			pathStr = sb.toString().substring(0, endIndex).split(";");
			
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck() || false == parameterNamesCheck()) {
			return false;
		}
		
		// 表示 userInfo 入的是设备序列号
		if (6 == userInfoType) {
			if (userInfo.length() < 6) {
				result = 1007;
				resultDesc = "设备序列号长度不能小于6位";
				return false;
			}
		}

		result = 0;
		resultDesc = "节点值获取成功";
		
		return true;
	}
	
	
	private boolean parameterNamesCheck(){
		logger.debug("parameterNamesCheck()");
		if (null==pathStr || pathStr.length == 0) {
			result = 1006;
			resultDesc = "节点路径不能为空";
			return false;
		}
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
		
		if (null != parameterValues && !parameterValues.isEmpty()) {
			
			Element parameterList = root.addElement("ParameterList");
			
			int i = 0;
			for(ParameValueOBJ objParameValueOBJ : parameterValues){
				i = i + 1;
				Element parameterValueStruct = parameterList.addElement("ParameterValueStruct");
				parameterValueStruct.addElement("Name").addText(objParameValueOBJ.getName());
				parameterValueStruct.addElement("Value").addText(
						objParameValueOBJ.getValue() == null ? "" : objParameValueOBJ
								.getValue());
			}
		}

		return document.asXML();
	}

	
	
	public String[] getPathStr() {
		return pathStr;
	}


	
	public void setPathStr(String[] pathStr) {
		this.pathStr = pathStr;
	}

	
	
	public ArrayList<ParameValueOBJ> getParameterValues() {
		return parameterValues;
	}


	
	public void setParameterValues(ArrayList<ParameValueOBJ> parameterValues) {
		this.parameterValues = parameterValues;
	}
	

//	public static void main(String[] args) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("<?xml version=\"1.0\" encoding=\"GBK\"?>")
//		  .append("<root>")
//		  .append("   <CmdID>123456789012345</CmdID>")
//		  .append("   <CmdType>CX_01</CmdType>")
//		  .append("   <ClientType>3</ClientType>")
//		  .append("   <Param>")
//		  .append("      <UserInfoType>6</UserInfoType>")
//		  .append("      <UserInfo>njkd123456</UserInfo>")
//		  .append("      <ParameterNames>")
//		  .append("         <string>InternetGatewayDevice.WANDevice.1.WANConnectionDevice.2.X_CT-COM_WANEponLinkConfig.VLANIDMark</string>")
//		  .append("         <string>InternetGatewayDevice.WANDevice.1.WANConnectionDevice.3.X_CT-COM_WANEponLinkConfig.VLANIDMark</string>")
//		  .append("      </ParameterNames>")
//		  .append("   </Param>")
//		  .append("</root>");
//		
//		GetParameterValuesChecker checker = new GetParameterValuesChecker(sb.toString());
//		checker.check();
//	}

}
