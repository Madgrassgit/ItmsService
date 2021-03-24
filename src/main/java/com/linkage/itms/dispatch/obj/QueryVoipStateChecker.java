package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;


public class QueryVoipStateChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryVoipStateChecker.class);
	
	private List<HashMap<String,String>> voipList = new ArrayList<HashMap<String,String>>();
	

	/**
	 * 构造函数 入参
	 * @param inXml
	 */
	public QueryVoipStateChecker(String inXml){
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
			
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
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
		resultDesc = "成功";
		
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
		
		Element voipPorts = root.addElement("VoipPort");
		logger.warn("VoipPort.size()=" + voipList.size());
		if(voipList != null && voipList.size() > 0){
			HashMap<String,String> tmp = null;
			for(int i = 0; i < voipList.size(); i++){
				tmp = voipList.get(i);
				String voipPort = StringUtil.getStringValue(tmp.get("VoipPortNUM"));
				String voipPortNum = StringUtil.getStringValue(tmp.get("VoipPortNUM"));
				String status = StringUtil.getStringValue(tmp.get("RstState"));
				String BytesReceived = StringUtil.getStringValue(tmp.get("BytesReceived"));
				String BytesSent = StringUtil.getStringValue(tmp.get("BytesSent"));
				Element voipPortNums = voipPorts.addElement("VoipPort").addAttribute("num", voipPort);
				voipPortNums.addElement("VoipPortNUM").addText(voipPortNum);
				voipPortNums.addElement("RstState").addText(status);
				voipPortNums.addElement("BytesReceived").addText(BytesReceived);
				voipPortNums.addElement("BytesSent").addText(BytesSent);
			}
		}

		return document.asXML();
	}
	
	public List<HashMap<String, String>> getVoipList() {
		return voipList;
	}

	public void setVoipList(List<HashMap<String, String>> voipList) {
		this.voipList = voipList;
	}
	
}
