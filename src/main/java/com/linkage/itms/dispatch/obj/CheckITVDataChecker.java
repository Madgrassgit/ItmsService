package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 新疆电信：ITMS检查光猫ITV口数据配置是否正确，采集itv的vlan配置是否正确
 * 
 * @author chenxj6
 * @date 2016-8-29
 * @param param
 * @return
 */
public class CheckITVDataChecker extends BaseChecker {
	private static final Logger logger = LoggerFactory
			.getLogger(CheckITVDataChecker.class);

	private String inParam = null;

	public CheckITVDataChecker(String inParam) {
		this.inParam = inParam;
	}

	@Override
	public boolean check() {
		logger.debug("BindInfoChecker==>check()" + inParam);

		SAXReader reader = new SAXReader();
		Document document = null;

		try {

			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();

			cmdId = root.elementTextTrim("CmdID");// 接口调用唯一ID 每次调用此值不可重复
			cmdType = root.elementTextTrim("CmdType");// 接口类型 CX_01,固定
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));// 客户端类型:1：BSS;2：IPOSS;3：综调;4：RADIUS

			Element param = root.element("Param");

			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			// 用户信息类型:1：用户宽带帐号;2：LOID;3：IPTV宽带帐号;4：VOIP业务电话号码;5：VOIP认证帐号
			userInfo = param.elementTextTrim("UserInfo"); // 用户信息类型所对应的用户信息
			devSn = param.elementTextTrim("DeviceInfo"); // 设备序列号（可填后6位），如果该数据不为空则优先按设备信息查询，如果该数据为空则按用户信息查询

			logger.warn(userInfo);

		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		if(clientType!=1 && clientType!=2 && clientType!=3 && clientType!=4){
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}
		
		if(userInfoType!=1 && userInfoType!=2 && userInfoType!=3 && userInfoType!=4 && userInfoType!=5){
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}

		if (StringUtil.IsEmpty(userInfo)) {
			result = 1000;
			resultDesc = "用户信息为空";
			return false;
		}

		// 参数合法性检查
		if (false == baseCheck()) {
			return false;
		}
		result = 0;
		resultDesc = "成功";

		return true;

	}

	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		// Element Param = root.addElement("Param");
		// Param.addElement("Loid").addText(loid);
		return document.asXML();
	}

}
