package com.linkage.itms.itv.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dispatch.obj.BaseChecker;


/**
 * @author zhangsm
 * @version 1.0
 * @since 2011-9-23 上午09:40:19
 * @category com.linkage.itms.itv.obj<br>
 * @copyright 亚信联创 网管产品部
 */
public class GetItvAccountOBJ extends BaseChecker
{
	// 日志记录
	private static Logger logger = LoggerFactory
			.getLogger(GetItvAccountOBJ.class);

	//回复使用的XML的Document
	private Document document; 
	//机顶盒mac
	private String stbMac;
	//机顶盒接入lan口，新疆吉林专用
	private String lan_port;
	//XML结构root结点
	private Element root;
	//返回的itv账号集合
	private List<AccountOBJ> acccounts;
	
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(getCallXml()));
			Element root = document.getRootElement();
			this.setCmdId(root.elementTextTrim("CmdID"));
			this.setCmdType(root.elementTextTrim("CmdType"));
			this.setClientType(StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType")));

			Element param = root.element("Param");
			stbMac = param.elementTextTrim("mac");
	
		} catch (Exception e) {
			e.printStackTrace();
			this.setResult(1);
			this.setResultDesc("数据格式错误");
			return false;
		}
		
		//参数合法性检查
		if (false == baseCheck()) {
			return false;
		}
		
		this.setResult(0);
		this.setResultDesc("成功");
		
		return true;
	}

	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		if(this.getAcccounts() == null || this.getAcccounts().isEmpty())
		{
			this.setAcccounts(new ArrayList<AccountOBJ>());
		}
		Element root = document.addElement("root");
		// 结果代码
		root.addElement("RstCode").addText("" + getResult());
		// 结果描述
		root.addElement("RstMsg").addText(getResultDesc());
		//mac
		root.addElement("mac").addText(this.getStbMac());
		if(null != this.getAcccounts() || !this.getAcccounts().isEmpty())
		{
			Element accounts = root.addElement("accounts");
			if("xj_dx".equals(Global.G_instArea) || "jl_dx".equals(Global.G_instArea)){
				for(AccountOBJ account : this.getAcccounts())
				{
					if(!StringUtil.IsEmpty(account.getIptvRealBindPort()) && 
					   !StringUtil.IsEmpty(this.getLan_port()) && account.getIptvRealBindPort().endsWith(this.getLan_port()))
					{
						Element accElement = accounts.addElement("account");
						accElement.addElement("username").addText(StringUtil.getStringValue(account.getUserName()));						
						accElement.addElement("iptvRealBindPort").addText(StringUtil.getStringValue(account.getIptvRealBindPort()));
						break;
					}
				}
			}else{
				for(AccountOBJ account : this.getAcccounts())
				{
					Element accElement = accounts.addElement("account");
					accElement.addElement("username").addText(StringUtil.getStringValue(account.getUserName()));
				}
			}
		}
		return document.asXML();
	}
	
	public List<AccountOBJ> getAcccounts()
	{
		return acccounts;
	}

	
	public void setAcccounts(List<AccountOBJ> acccounts)
	{
		this.acccounts = acccounts;
	}

	
	public String getStbMac()
	{
		return stbMac;
	}

	
	public void setStbMac(String stbMac)
	{
		this.stbMac = stbMac;
	}

	
	public Document getDocument()
	{
		return document;
	}

	
	public void setDocument(Document document)
	{
		this.document = document;
	}

	
	public Element getRoot()
	{
		return root;
	}

	
	public void setRoot(Element root)
	{
		this.root = root;
	}

	public String getLan_port() {
		return lan_port;
	}

	public void setLan_port(String lan_port) {
		this.lan_port = lan_port;
	}
	
}
