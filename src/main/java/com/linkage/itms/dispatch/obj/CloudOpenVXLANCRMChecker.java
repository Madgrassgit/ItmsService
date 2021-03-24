package com.linkage.itms.dispatch.obj;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

public class CloudOpenVXLANCRMChecker extends CloudBaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(CloudOpenVXLANCRMChecker.class);

	private String userType = "";
	private String vlanId = "";
	private String requId = "";
	private String proInstId = "";
	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public CloudOpenVXLANCRMChecker(String inXml) {
		callXml = inXml;
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
			dealDate = root.elementTextTrim("DealDate");

			Element param = root.element("Param");
			userInfo = param.elementTextTrim("UserInfo");
			loid = param.elementTextTrim("Loid");
			userType = param.elementTextTrim("UserType");
			cityId = param.elementTextTrim("CityId");
			vlanId = param.elementTextTrim("VlanId");
			wanType = param.elementTextTrim("WanType");
			requId = param.elementTextTrim("RequId");
			proInstId = param.elementTextTrim("ProInstId");
		} catch (Exception e) {
			e.printStackTrace();
			result = 3;
			resultDesc = "入参格式错误";
			return false;
		}
		//参数合法性检查
		if (!paramCheck()) {
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		return true;
	}

	public String getReturnXml(ArrayList<Map<String, String>> retList) {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(String.valueOf(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		
		Element par = root.addElement("Param");
		// 当前最新绑定的Loid
		par.addElement("Loid").addText(loid);
		// 通过账号查到多个Loid，除去最新绑定Loid之外的Loid集合
		par.addElement("LoidPrev").addText(loidPrev);
		return document.asXML();
	}

	public boolean paramCheck() {
		if (StringUtil.IsEmpty(cmdId)) {
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}

		if (1 != clientType ) {
			result = 1;
			resultDesc = "无此业务类型";
			return false;
		}

		if (!"CX_01".equals(cmdType)) {
			result = 1001;
			resultDesc = "接口类型非法";
			return false;
		}

		if (StringUtil.IsEmpty(userInfo) ||
				StringUtil.IsEmpty(loid) ||
				StringUtil.IsEmpty(userType) ||
				StringUtil.IsEmpty(cityId) ||
				StringUtil.IsEmpty(vlanId) ||
				StringUtil.IsEmpty(wanType) ||
				StringUtil.IsEmpty(requId) ||
				StringUtil.IsEmpty(proInstId)) {
			result = 3;
			resultDesc = "入参格式错误";
			return false;
		}
		
		if (!"1,2".contains(userType)) {
			result = 3;
			resultDesc = "客户类型错误";
			return false;
		}
		
		if (!Global.G_CityIds.contains(cityId)) {
			result = 3;
			resultDesc = "属地错误";
			return false;
		}
		
		if (!"1,2,3,4".contains(wanType)) {
			result = 3;
			resultDesc = "上网方式错误";
			return false;
		}
		return true;
	}
	
	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getVlanId() {
		return vlanId;
	}

	public void setVlanId(String vlanId) {
		this.vlanId = vlanId;
	}

	public String getRequId() {
		return requId;
	}

	public void setRequId(String requId) {
		this.requId = requId;
	}

	public String getProInstId() {
		return proInstId;
	}

	public void setProInstId(String proInstId) {
		this.proInstId = proInstId;
	}

	@Override
	public String getReturnXml() {
		return null;
	}
}
