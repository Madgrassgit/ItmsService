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
import com.linkage.itms.Global;

/**
 * 
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-6-27
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */


public class QueryLanStateAddStatusChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryLanStateAddStatusChecker.class);
	
	private List<HashMap<String,String>> lanList = new ArrayList<HashMap<String,String>>();
	
	// 1：在线 -1：不在线
	private int onlineStatus;
	
	/**
	 * 构造函数 入参
	 * @param inXml
	 */
	public QueryLanStateAddStatusChecker(String inXml){
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
		
		if ("nx_dx".equals(Global.G_instArea)) {
			// 参数合法性检查
			if (false == baseCheckNX()) {
				return false;
			}
			
			if(1 != userInfoType && 2 != userInfoType
					&& 3 != userInfoType && 4 != userInfoType
					&& 5 != userInfoType ){
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
			
			if(false == userInfoCheck()){
				return false;
			}
			
		}else{
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
		// 在线状态描述
		root.addElement("OnlineStatus").addText("" + onlineStatus);
		Element lanPorts = root.addElement("LanPort");
		if(lanList != null && lanList.size() > 0){
			HashMap<String,String> tmp = null;
			for(int i = 0; i < lanList.size(); i++){
				tmp = lanList.get(i);
				String lanPort = StringUtil.getStringValue(tmp.get("LanPortNUM"));
				String lanPostNum = StringUtil.getStringValue(tmp.get("LanPortNUM"));
				String status = StringUtil.getStringValue(tmp.get("RstState"));
				String BytesReceived = StringUtil.getStringValue(tmp.get("BytesReceived"));
				String BytesSent = StringUtil.getStringValue(tmp.get("BytesSent"));
				Element lanPortNums = lanPorts.addElement("LanPort").addAttribute("num", lanPort);
				lanPortNums.addElement("LanPortNUM").addText(lanPostNum);
				lanPortNums.addElement("RstState").addText(status);
				lanPortNums.addElement("BytesReceived").addText(BytesReceived);
				lanPortNums.addElement("BytesSent").addText(BytesSent);
			}
		}

		return document.asXML();
	}
	
	public List<HashMap<String, String>> getLanList() {
		return lanList;
	}


	public void setLanList(List<HashMap<String, String>> lanList) {
		this.lanList = lanList;
	}

	public int getOnlineStatus()
	{
		return onlineStatus;
	}

	public void setOnlineStatus(int onlineStatus)
	{
		this.onlineStatus = onlineStatus;
	}
	
}
