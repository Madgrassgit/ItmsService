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
 * 江西电信：Itv业务组播vlan查询接口
 * @author chenxj6
 * @date 2017-03-24
 * @param param
 * @return
 */
public class QueryMulticastVlanChecker extends BaseChecker {
	private static final Logger logger = LoggerFactory.getLogger(QueryMulticastVlanChecker.class);

	private String inParam = null;
	
	private String multicastVlanId;

	public QueryMulticastVlanChecker(String inParam) {
		this.inParam = inParam;
	}

	@Override
	public boolean check() {
		logger.debug("QueryMulticastVlanChecker==>check()" + inParam);
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();

			cmdId = root.elementTextTrim("CmdID");// 接口调用唯一ID 每次调用此值不可重复
			cmdType = root.elementTextTrim("CmdType");// 接口类型 CX_01,固定
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");

//			searchType 1：客户账号；2：设备序列号
			searchType = StringUtil.getIntegerValue(param.elementTextTrim("SearchType"));
			
//			userInfoType 1：用户宽带帐号；2：LOID；3：IPTV宽带帐号；4：VOIP业务电话号码；5：VOIP认证帐号
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserName");
			
//			SearchTyp：2时必须，允许至少最后6位序列号
			devSn = param.elementTextTrim("DevSN");
			
//			本地网属地代码，见5.1属地代码表
			cityId = param.elementTextTrim("CityId");


		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		// 参数合法性检查
		if (false == baseCheck()) {
			return false;
		}
		
		if(searchType!=1 && searchType!=2){
			result = 1001;
			resultDesc = "查询类型非法";
			return false;
		}
		
		if(userInfoType!=1 && userInfoType!=2 && userInfoType!=3 && userInfoType!=4 && userInfoType!=5){
			result = 1000;
			resultDesc = "用户信息查询类型非法";
			return false;
		}
		
		if(searchType==1){
			if(StringUtil.IsEmpty(userInfo)){
				result = 1000;
				resultDesc = "用户账号不可为空";
				return false;
			}
		}
		
		if(searchType==2){
			if(StringUtil.IsEmpty(devSn)){
				result = 1000;
				resultDesc = "设备序列号不可为空";
				return false;
			}else{
				if(devSn.length()<6){
					result = 1005;
					resultDesc = "设备序列号非法";
					return false;
				}
			}
		}
		
		
//		本地网属地代码，见5.1属地代码表  1007     属地非法,以下是江西文档里给出的属性代码表，确认下是用baseCheck里的cityIdCheck()，还是用文档给的属地代码表判断属地。
//		701 , 790 , 791 , 792 , 793 , 794 , 795 , 796 , 797 , 798 , 799 , 00 ？？？？？？？？？？？？？？？？？？？
		if (false == cityIdCheck()) {
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
		Element Param = root.addElement("Param");
		// 设备在线状态
		Param.addElement("X_CT-COM_MulticastVlan").addText(StringUtil.getStringValue(multicastVlanId));
		
		return document.asXML();
	}

	public String getMulticastVlanId() {
		return multicastVlanId;
	}

	public void setMulticastVlanId(String multicastVlanId) {
		this.multicastVlanId = multicastVlanId;
	}

}
