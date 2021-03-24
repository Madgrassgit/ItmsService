
package com.linkage.stbms.ids.util;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2016年3月30日
 * @category com.linkage.stbms.ids.util
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class SetStbBindSNChecker extends BaseChecker
{

	private static final Logger logger = LoggerFactory
			.getLogger(SetStbBindSNChecker.class);
	private String inParam = null;
	
	/**
	 * 接口入参：mac地址
	 */
	private String mac = "";
	
	/**
	 * 接口入参： 业务账号
	 */
	private String serv_account = "";
	
	/**
	 * 接口入参： 操作类型
	 */
	private String operate_type = "";

	public SetStbBindSNChecker(String inParam)
	{
		this.inParam = inParam;
	}

	@Override
	public boolean check()
	{
		logger.debug("SetStbBindSNChecker==>check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {

			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			
			this.mac = param.elementTextTrim("mac");
			
			this.serv_account = param.elementTextTrim("serv_account"); 
			this.operate_type =  param.elementTextTrim("operate_type"); 
			
		} catch (Exception e) {
			logger.error("inParam format is err,msg({})", e.getMessage());
			rstCode = "1";
			rstMsg = "入参格式错误";
			return false;
		}
		
		
		//参数合法性检查
		if (false == baseCheck() || false == macCheck() || false == servAccountCheck() || false == operTypeCheck()) {
			return false;
		}
		
		
		return true;
	}
	
	private boolean macCheck()
	{
		if(StringUtil.IsEmpty(mac)){
			rstCode = "1001";
			rstMsg = "mac地址为空";
			return false;
		}
		return true;
	}
	
	private boolean servAccountCheck()
	{
		if(StringUtil.IsEmpty(serv_account)){
			rstCode = "1001";
			rstMsg = "serv_account为空";
			return false;
		}
		return true;
	}
	
	private boolean operTypeCheck()
	{
		if(StringUtil.IsEmpty(operate_type)){
			rstCode = "1001";
			rstMsg = "operate_type为空";
			return false;
		}
		return true;
	}


	@Override
	public String getReturnXml()
	{
		logger.debug("SetStbBindSNChecker->getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(rstCode);
		// 结果描述
		root.addElement("RstMsg").addText(rstMsg);
		return document.asXML();
	}

	
	public String getInParam()
	{
		return inParam;
	}

	
	public void setInParam(String inParam)
	{
		this.inParam = inParam;
	}

	
	public String getMac()
	{
		return mac;
	}

	
	public void setMac(String mac)
	{
		this.mac = mac;
	}

	
	public String getServ_account()
	{
		return serv_account;
	}

	
	public void setServ_account(String serv_account)
	{
		this.serv_account = serv_account;
	}

	
	public String getOperate_type()
	{
		return operate_type;
	}

	
	public void setOperate_type(String operate_type)
	{
		this.operate_type = operate_type;
	}
	
	
}
