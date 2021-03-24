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
import com.linkage.itms.obj.ParameValueOBJ;


public class HowTerminalChecker extends BaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(MoSSIDPWChecker.class);
	
	private String  NUM = "InternetGatewayDevice.Services.X_CT-COM_MWBAND.TotalTerminalNumber";
	
	private String username = "";
	
	private int internetNo = 0;
	
	private int isSucc = 0;
	
	private ParameValueOBJ pvOBJ = null;
	
	public HowTerminalChecker(String inXml){
		callXml = inXml;
	}
	public boolean check() {
		
		logger.debug("HowTerminalChecker==>check()");
		
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
			internetNo =StringUtil.getIntegerValue(param.elementTextTrim("InternetNo"));
			
			pvOBJ = new ParameValueOBJ();
			pvOBJ.setName(NUM);
			pvOBJ.setType("2");
			pvOBJ.setValue(internetNo+"");
			
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
		if ("xj_dx".equals(Global.G_instArea))
		{
			//修改密码是否成功
			root.addElement("RstMsg").addText("" + resultDesc);
		}
		else
		{
			//修改密码是否成功
			root.addElement("IsSuccess").addText("" + isSucc);
			// 结果描述
			root.addElement("NoReason").addText("" + resultDesc);
		}
		
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
	
	public int getInternetNo() {
		return internetNo;
	}
	public void setInternetNo(int internetNo) {
		this.internetNo = internetNo;
	}
	public int getIsSucc() {
		return isSucc;
	}
	public void setIsSucc(int isSucc) {
		this.isSucc = isSucc;
	}
}
