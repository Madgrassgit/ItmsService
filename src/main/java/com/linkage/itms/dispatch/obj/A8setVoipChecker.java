package com.linkage.itms.dispatch.obj;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-12-4
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class A8setVoipChecker extends BaseChecker
{
	private static final Logger logger = LoggerFactory.getLogger(A8setVoipChecker.class);
	//逻辑id
	private String loid;
	//语音号码
	private String Voip_phone;
	//端口号
	private String Voip_prot;
	/**
	 * 构造函数 入参
	 * @param inXml
	 */
	public A8setVoipChecker(String inXml){
		callXml = inXml;
	}
	/**
	 * 参数合法性检查
	 */
	@Override
	public boolean check()
	{
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
			loid = param.elementTextTrim("Loid");
			Voip_phone = param.elementTextTrim("Voip_phone");
			Voip_prot = param.elementTextTrim("Voip_prot");
			
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		// 参数合法性检查
		if (false == baseCheck()||false ==CS()) {
			return false;
		}
		
		result = 0;
		resultDesc = "节点值获取成功";
		
		return true;
	
	}
	public boolean CS()
	{
		if(StringUtil.IsEmpty(loid)){
			result = 1007;
			resultDesc = "逻辑id为空";
			return false;
		}
		if(StringUtil.IsEmpty(Voip_phone)){
			result = 1008;
			resultDesc = "语音号码为空";
			return false;
		}
		if(StringUtil.IsEmpty(Voip_prot)){
			result = 1009;
			resultDesc = "端口号为空";
			return false;
		}
		return true;
	}
	@Override
	public boolean baseCheck(){
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)){
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		
		if(3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType && 5 != clientType){
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}
		
		if(false == "CX_01".equals(cmdType)){
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		
		return true;
	}
	/**
	 * 回参
	 */
	@Override
	public String getReturnXml()
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(""+result);
		// 结果描述
		root.addElement("RstMsg").addText(""+resultDesc);
		return document.asXML();
	}
	
	public String getLoid()
	{
		return loid;
	}
	
	public void setLoid(String loid)
	{
		this.loid = loid;
	}
	
	public String getVoip_phone()
	{
		return Voip_phone;
	}
	
	public void setVoip_phone(String voip_phone)
	{
		Voip_phone = voip_phone;
	}
	
	public String getVoip_prot()
	{
		return Voip_prot;
	}
	
	public void setVoip_prot(String voip_prot)
	{
		Voip_prot = voip_prot;
	}
	
	
}
