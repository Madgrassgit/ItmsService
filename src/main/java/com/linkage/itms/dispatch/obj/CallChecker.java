package com.linkage.itms.dispatch.obj;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * @author Jason(3412)
 * @date 2010-4-1
 */
public class CallChecker extends BaseQueryChecker {

	private static Logger logger = LoggerFactory.getLogger(CallChecker.class);

	// 电信维护密码
	private String telePasswd;
	//loid
	private String loId; 

	/**
	 * 构造方法
	 * 
	 * @param _callXml
	 *            客户端查询XML字符串
	 */
	public CallChecker(String _callXml) {
		logger.debug("CallChecker({})", _callXml);
		callXml = _callXml;
	}


	/**
	 * 返回调用结果
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-4-1
	 * @return String
	 */
	@Override
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
		
		Element paramEle = root.addElement("Param");
		// 电信维护密码
		paramEle.addElement("DevPwd").addText(
				StringUtil.getStringValue(telePasswd));
		
		if(("nx_dx").equals(Global.G_instArea)){
			// 宁夏电信回参需要返回loid
			paramEle.addElement("LoId").addText(
					StringUtil.getStringValue(loId));
		}

		return document.asXML();
	}

	
	/** getter, setter methods */


	public String getTelePasswd() {
		return telePasswd;
	}

	public void setTelePasswd(String telePasswd) {
		this.telePasswd = telePasswd;
	}
	
	public String getLoId() {
		return loId;
	}

	public void setLoId(String loId) {
		this.loId = loId;
	}

}
