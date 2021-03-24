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
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-11
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ChangVlanParamChecker extends BaseChecker
{
	private static final Logger logger = LoggerFactory.getLogger(ChangVlanParamChecker.class);

	private String inParam = null;
	private String oldVlanId = "";
	private String newVlanId = "";
	public ChangVlanParamChecker(String inParam){
		this.inParam = inParam;
	}
	
	@Override
	public boolean check()
	{

		
		logger.debug("BindInfoChecker==>check()"+inParam);
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {

			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			
			userInfo = param.elementTextTrim("UserInfo"); 
			oldVlanId = param.elementTextTrim("OldVlanID");
			newVlanId = param.elementTextTrim("NewVlanID");
			logger.warn(userInfo);
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		if (StringUtil.IsEmpty(userInfo)) {
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
		
		// 参数合法性检查
		if (false == baseCheck())
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		
		return true;
	
	}

	@Override
	public String getReturnXml()
	{
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
//		Element Param = root.addElement("Param");
//		Param.addElement("Loid").addText(loid);
		return document.asXML();
	}
	
	public String getOldVlanId()
	{
		return oldVlanId;
	}
	
	public void setOldVlanId(String oldVlanId)
	{
		this.oldVlanId = oldVlanId;
	}
	
	public String getNewVlanId()
	{
		return newVlanId;
	}
	
	public void setNewVlanId(String newVlanId)
	{
		this.newVlanId = newVlanId;
	}
	
}
