package com.linkage.itms.dispatch.obj;


import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class CloudQueryConfigureChecker extends CloudBaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(CloudQueryConfigureChecker.class);


	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public CloudQueryConfigureChecker(String inXml) {
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
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		} catch (Exception e) {
			e.printStackTrace();
			result = 3;
			resultDesc = "入参格式错误";
			return false;
		}
		//参数合法性检查
		if (!baseCheck() || !userInfoTypeCheck() || !userInfoCheck()) {
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
		root.addElement("RstCode").addText(String.valueOf(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		
		Element par = root.addElement("Param");
		// 当前最新绑定的Loid
		par.addElement("Loid").addText(loid);
		// 通过账号查到多个Loid，除去最新绑定Loid之外的Loid集合
		par.addElement("LoidPrev").addText(loidPrev);
		
		par.addElement("RemoteSubnet").addText(remoteSubnet);
		par.addElement("LocalSubnet").addText(localSubnet);
		par.addElement("RemoteDomain").addText(remoteDomain);
		par.addElement("IPSecOutInterface").addText(iPSecOutInterface);
		par.addElement("IPSecEncapsulationMode").addText(iPSecEncapsulationMode);
		// IPSec 类型
		par.addElement("IPSecType").addText(ipSecType);
		// Site-to-Site 模式下对端IP 地址
		par.addElement("RemoteIP").addText(remoteIP);
		// IKE 协商方式
		par.addElement("ExchangeMode").addText(exchangeMode);
		// IKE认证算法
		par.addElement("IKEAuthenticationAlgorithm").addText(ikeAuthenticationAlgorithm);
		// IKE 验证方法
		par.addElement("IKEAuthenticationMethod").addText(ikeAuthenticationMethod);
		// IKE 加密算法
		par.addElement("IKEEncryptionAlgorithm").addText(ikeEncryptionAlgorithm);
		// IKE DH 组
		par.addElement("IKEDHGroup").addText(ikeDHGroup);
		// IKE 身份类型
		par.addElement("IKEIDType").addText(ikeIDType);
		// IKE 本端名称
		par.addElement("IKELocalName").addText(ikeLocalName);
		// IKE 对端名称
		par.addElement("IKERemoteName").addText(ikeRemoteName);
		// IKE 预共享密钥
		par.addElement("IKEPreshareKey").addText(ikePreshareKey);
		// IPSec 安全协议
		par.addElement("IPSecTransform").addText(ipSecTransform);
		// IPsec 验证算法
		par.addElement("ESPAuthenticationAlgorithm").addText(espAuthenticationAlgorithm);
		// IPsec 加密算法
		par.addElement("ESPEncryptionAlgorithm").addText(espEncryptionAlgorithm);
		// IPSec DH 组
		par.addElement("IPSecPFS").addText(ipSecPFS);
		// 设置IKE SA 生命周期
		par.addElement("IKESAPeriod").addText(ikeSAPeriod);
		// 设置IPsec SA 时间生命周期
		par.addElement("IPSecSATimePeriod").addText(ipSecSATimePeriod);
		// 设置IPsec SA 流量生命周期
		par.addElement("IPSecSATrafficPeriod").addText(ipSecSATrafficPeriod);
		// AH 验证算法
		par.addElement("AHAuthenticationAlgorithm").addText(ahAuthenticationAlgorithm);
		return document.asXML();
	}
}
