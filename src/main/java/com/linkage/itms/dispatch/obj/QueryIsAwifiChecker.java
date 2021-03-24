package com.linkage.itms.dispatch.obj;

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
 * 数据检查
 * awifi开通状态查询
 * 
 * @author wanghong5（72780）
 * @date 2015-03-10
 */
public class QueryIsAwifiChecker extends BaseChecker {
	
	private static Logger logger = LoggerFactory.getLogger(QueryIsAwifiChecker.class);
	
	//awifi开通状态
	protected int isAwifi;
	
	//回复使用的XML的Document
	Document document; 
		
	//XML结构root结点
	Element root;
			
	public QueryIsAwifiChecker(String _callXml) {
		logger.debug("QueryIsAwifiChecker({})", _callXml);
		callXml = _callXml;
	}
		
	@Override
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
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			isAwifi = 1;
			return false;
		}
			
		if(StringUtil.IsEmpty(cmdId)){
			result = 3;
			resultDesc = "接口类型非法";
			isAwifi = 1;
			return false;
		}
			
		// 参数合法性检查
		if (false == baseCheck1()  || false == userInfoTypeCheck1() || false  == userInfoCheck()) {
			result = 1001;
			resultDesc = "用户信息类型非法";
			isAwifi = 1;
			return false;
		}

		result = 0;
		resultDesc = "成功";

		return true;
	}

		
		
		
	/**
	 * 针对awifi开通状态查询
	 * @return
	 */
	public boolean baseCheck1(){
		logger.debug("baseCheck()");
			
		if(1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType
				&& 5 != clientType && 6 != clientType && 7 != clientType){
			result = 2;
			resultDesc = "客户端类型非法";
			isAwifi = 1;
			return false;
		}
			
		if(false == "CX_01".equals(cmdType)){
			result = 3;
			resultDesc = "接口类型非法";
			isAwifi = 1;
			return false;
		}
			
		return true;
	}
		
	/**
	 * 针对awifi开通状态查询
	 * @return
	 */
	boolean userInfoTypeCheck1(){
		if(1 != userInfoType && 2 != userInfoType && 3 != userInfoType && 4 != userInfoType && 5 != userInfoType){
			result = 1001;
			resultDesc = "用户信息类型非法";
			isAwifi = 1;
			return false;
		}
		return true;
	}
		
	
	/**
	 * 接口回复基本头格式
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-9-2
	 * @return String
	 */
	public void getBaseReturnXml(){
		logger.debug("getBaseReturnXml()");
		document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
	}	
		

	/**
	 * 返回调用结果
	 */
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		//返回结果

		Document document = DocumentHelper.createDocument();
		if ("nx_dx".equals(Global.G_instArea)) {
			document.setXMLEncoding(Global.codeTypeValue);
		} else {
			document.setXMLEncoding("GBK");
		}
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		// 上网方式
		root.addElement("IsAwifi").addText("" + isAwifi);
		return document.asXML();
	}


	public int getIsAwifi() {
		return isAwifi;
	}

	public void setIsAwifi(int isAwifi) {
		this.isAwifi = isAwifi;
	}

	

}
