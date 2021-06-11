package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author guxl3 (Ailk No.)
 * @version 1.0
 * @since 2021年2月5日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class UpdateHqosChecker extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(UpdateHqosChecker.class);
	private String dealDate;
	private String hqsName;
	private String hqsPassword;
	private String vlanId;
	private String wanType;
	private String ipForwardList;
	private String qosValue;
	private String servTypeId;
	private String operateId;

	public UpdateHqosChecker(String inXml)
	{
		callXml = inXml;
	}

	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		SAXReader reader = new SAXReader();
		try
		{
			reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
		}
		catch (SAXException e)
		{
			logger.error("UpdateHqosChecker.check error:", e);
		}
		Document document = null;
		try
		{
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("cmdId");
			authUser = root.elementTextTrim("authUser");
			authPwd = root.elementTextTrim("authPwd");
			Element param = root.element("param");
			dealDate = param.elementTextTrim("dealDate");
			loid = param.elementTextTrim("loid");
			hqsName = param.elementTextTrim("hqsName");
			hqsPassword = param.elementTextTrim("hqsPassword");
			cityId = param.elementTextTrim("cityId");
			vlanId = param.elementTextTrim("vlanId");
			wanType = param.elementTextTrim("wanType");
			ipForwardList = param.elementTextTrim("ipForwardlist");
			qosValue = param.elementTextTrim("P_Value_802-1");
			servTypeId="46";
			operateId="2";
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (!baseCheck())
		{
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		return true;
	}

	@Override
	public boolean baseCheck(){
		
		if(StringUtil.IsEmpty(cmdId)){
			result = 1;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		
		//|| StringUtil.IsEmpty(hqsPassword)密码可以为空，不做更改
		if (StringUtil.IsEmpty(loid) || StringUtil.IsEmpty(hqsName)
				 || StringUtil.IsEmpty(vlanId)
				|| StringUtil.IsEmpty(wanType) || StringUtil.IsEmpty(ipForwardList)
				|| StringUtil.IsEmpty(qosValue)){
			result = 1;
			resultDesc = "缺少参数值";
			return false;
		}
		
		return true;
	}
	
	
	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		Element root = document.addElement("root");
		// 结果代码
		root.addElement("resultCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("resultDes").addText(resultDesc);

		return document.asXML();
	}

	
	public String getDealDate()
	{
		return dealDate;
	}

	
	public void setDealDate(String dealDate)
	{
		this.dealDate = dealDate;
	}

	
	public String getHqsName()
	{
		return hqsName;
	}

	
	public void setHqsName(String hqsName)
	{
		this.hqsName = hqsName;
	}

	
	public String getHqsPassword()
	{
		return hqsPassword;
	}

	
	public void setHqsPassword(String hqsPassword)
	{
		this.hqsPassword = hqsPassword;
	}

	
	public String getVlanId()
	{
		return vlanId;
	}

	
	public void setVlanId(String vlanId)
	{
		this.vlanId = vlanId;
	}

	
	public String getWanType()
	{
		return wanType;
	}

	
	public void setWanType(String wanType)
	{
		this.wanType = wanType;
	}

	public String getIpForwardList()
	{
		return ipForwardList;
	}

	
	public void setIpForwardList(String ipForwardList)
	{
		this.ipForwardList = ipForwardList;
	}

	public String getQosValue()
	{
		return qosValue;
	}

	
	public void setQosValue(String qosValue)
	{
		this.qosValue = qosValue;
	}
	
	public String getServTypeId()
	{
		return servTypeId;
	}

	
	public void setServTypeId(String servTypeId)
	{
		this.servTypeId = servTypeId;
	}

	
	public String getOperateId()
	{
		return operateId;
	}

	
	public void setOperateId(String operateId)
	{
		this.operateId = operateId;
	}

}
