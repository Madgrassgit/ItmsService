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
 * ITMS+向翼翮提供的业务修改工单的接口
 * 
 * @param 综调接口XML字符串参数
 * @author chenxj6
 * @date 2017-10-19
 * @return String 回参的XML字符串
 */
public class UpdateIpsecVPNChecker extends IpsecBaseChecker {
	private static final Logger logger = LoggerFactory.getLogger(UpdateIpsecVPNChecker.class);

	private String inParam = null;

	public UpdateIpsecVPNChecker(String inParam) {
		this.inParam = inParam;
	}

	@Override
	public boolean check() {
		logger.debug("UpdateIpsecVPNChecker==>check()" + inParam);

		SAXReader reader = new SAXReader();
		Document document = null;

		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");// 接口调用唯一ID 每次调用此值不可重复
			cmdType = root.elementTextTrim("CmdType");// 接口类型 CX_01,固定
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));// 客户端类型:1：网厅 2：IPOSS	3：综调 4：RADIUS 5：翼翮
			dealDate = root.elementTextTrim("DealDate");
			servTypeId = StringUtil.getIntegerValue(root.elementTextTrim("ServTypeId"));
			operateId = StringUtil.getIntegerValue(root.elementTextTrim("OperateId"));

			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			userType = StringUtil.getIntegerValue(param.elementTextTrim("UserType"));
			requestID = param.elementTextTrim("RequestID");
			iPSecType = param.elementTextTrim("IPSecType");
			remoteIP = param.elementTextTrim("RemoteIP");
			remoteSubnet = param.elementTextTrim("RemoteSubnet");
			localSubnet = param.elementTextTrim("LocalSubnet");			
			exchangeMode = param.elementTextTrim("ExchangeMode");
			iKEAuthenticationAlgorithm = param.elementTextTrim("IKEAuthenticationAlgorithm"); 
			iKEAuthenticationMethod = param.elementTextTrim("IKEAuthenticationMethod");
			iKEEncryptionAlgorithm = param.elementTextTrim("IKEEncryptionAlgorithm");
			iKEDHGroup = param.elementTextTrim("IKEDHGroup");
			iKEIDType = param.elementTextTrim("IKEIDType");
			iKELocalName = param.elementTextTrim("IKELocalName");
			iKERemoteName = param.elementTextTrim("IKERemoteName");
			iKEPreshareKey = param.elementTextTrim("IKEPreshareKey");
			iPSecEncapsulationMode = param.elementTextTrim("IPSecEncapsulationMode");
			iPSecTransform = param.elementTextTrim("IPSecTransform");
			eSPAuthenticationAlgorithm = param.elementTextTrim("ESPAuthenticationAlgorithm");
			eSPEncryptionAlgorithm = param.elementTextTrim("ESPEncryptionAlgorithm");
			iPSecPFS = param.elementTextTrim("IPSecPFS");
			iKESAPeriod = StringUtil.getIntegerValue(param.elementTextTrim("IKESAPeriod"));
			iPSecSATimePeriod = StringUtil.getIntegerValue(param.elementTextTrim("IPSecSATimePeriod"));
			iPSecSATrafficPeriod = StringUtil.getIntegerValue(param.elementTextTrim("IPSecSATrafficPeriod"));
			
			aHAuthenticationAlgorithm = param.elementTextTrim("AHAuthenticationAlgorithm");
			dPDEnable = StringUtil.getIntegerValue(param.elementTextTrim("DPDEnable"));
			dPDThreshold = StringUtil.getIntegerValue(param.elementTextTrim("DPDThreshold"));
			dPDThreshold=dPDThreshold==0?10:dPDThreshold;
			dPDRetry = StringUtil.getIntegerValue(param.elementTextTrim("DPDRetry"));
			dPDRetry=dPDRetry==0?5:dPDRetry;
			remoteDomain=param.elementTextTrim("RemoteDomain");
			
			//IPSec 安全协议，当iPSecTransform值为AH时，eSPAuthenticationAlgorithm
			//和eSPEncryptionAlgorithm都必须为空，且aHAuthenticationAlgorithm不能为空
			//当iPSecTransform值为ESP时，aHAuthenticationAlgorithm必须为空，其他两个必须不为空
			//当iPSecTransform值为AH-ESP时，三个值都不能为空
			if(iPSecTransform.equals("AH")){
				eSPAuthenticationAlgorithm="";
				eSPEncryptionAlgorithm="";
			}else if(iPSecTransform.equals("ESP")){
				aHAuthenticationAlgorithm="";
			}
			if(!StringUtil.IsEmpty(remoteIP)) {
				remoteDomain="";
			}
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 3;
			resultDesc = "入参格式错误";
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
		root.addElement("RstMsg").addText(StringUtil.getStringValue(resultDesc));
		// 最新绑定的Loid
		root.addElement("Loid").addText(StringUtil.getStringValue(loid));
		// 其它Loid信息
		root.addElement("LoidPrev").addText(StringUtil.getStringValue(loidPrev));
		
		return document.asXML();
	}

}
