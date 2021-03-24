package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.obj.ParameValueOBJ;


public class MoSSIDPWChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(MoSSIDPWChecker.class);
	
	private String  passwordNodeWPA1 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.PreSharedKey.1.PreSharedKey";
	private String  passwordNodeWPA2 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.2.PreSharedKey.1.PreSharedKey";
	
	private String  passwordNodeWEP1 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.WEPKey.1.WEPKey";
	private String  passwordNodeWEP2 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.2.WEPKey.1.WEPKey";
	
	private Map<String,String> map ;
	
	private String username = "";
	
	private int ssidType = 1;
	
	private String ssidPW = "";
	
	private int isSucc = 0;
	
	private ParameValueOBJ pvOBJ = null;
	
	public MoSSIDPWChecker(String inXml){
		callXml = inXml;
	}
	public boolean check() {
		
		logger.debug("MoSSIDPWChecker==>check()");
		map = new HashMap<String,String>();
		map.put("WPA1", passwordNodeWPA1);
		map.put("WPA2", passwordNodeWPA2);
		map.put("WEP1", passwordNodeWEP1);
		map.put("WEP2", passwordNodeWEP2);
		
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
			
			ssidType = StringUtil.getIntegerValue(param.elementTextTrim("SSIDType"));
			
			ssidPW = param.elementTextTrim("SSIDPW");
			
			pvOBJ = new ParameValueOBJ();
			//默认值
			if(ssidType==1){
				pvOBJ.setName(passwordNodeWPA1);
			}else{
				pvOBJ.setName(passwordNodeWPA2);
			}
			pvOBJ.setType("1");
			pvOBJ.setValue(ssidPW);
			
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
	
	public int getSsidType() {
		return ssidType;
	}
	public void setSsidType(int ssidType) {
		this.ssidType = ssidType;
	}
	public String getSsidPW() {
		return ssidPW;
	}
	public void setSsidPW(String ssidPW) {
		this.ssidPW = ssidPW;
	}
	public int getIsSucc() {
		return isSucc;
	}
	public void setIsSucc(int isSucc) {
		this.isSucc = isSucc;
	}
	public Map<String, String> getMap() {
		return map;
	}
	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	
}
