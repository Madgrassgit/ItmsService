package com.linkage.itms.itv.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.obj.BaseChecker;

/**
 * AHDX_ITMS-REQ-20170227YQW-001(通过MCA地址，查询stbind事件中对应的LOID信息)
 * @author wanghong
 *
 */
public class GetItvMacOBJ extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(GetItvMacOBJ.class);
	//回复使用的XML的Document
	private Document document; 
	//机顶盒mac
	private String stbMac="";
	//用户loid
	private String loid="";
	//XML结构root结点
	private Element root;
	
	
	@Override
	public boolean check()
	{
		SAXReader reader = new SAXReader();
		Document document = null;
		try 
		{
			document = reader.read(new StringReader(getCallXml()));
			Element root = document.getRootElement();
			this.setCmdId(root.elementTextTrim("CmdID"));
			this.setCmdType(root.elementTextTrim("CmdType"));
			this.setClientType(StringUtil.getIntegerValue(root.elementTextTrim("ClientType")));

			Element param = root.element("Param");
			stbMac = param.elementTextTrim("Mac");
		} 
		catch (Exception e) 
		{
			logger.error("GetItvMacOBJ 数据格式错误:"+e);
			e.printStackTrace();
			setResult(1);
			setResultDesc("数据格式错误");
			return false;
		}
		
		//参数合法性检查
		if (false == baseCheck()) 
		{
			return false;
		}
		
		this.setResult(0);
		this.setResultDesc("成功");
		
		return true;
	}
	
	@Override
	public boolean baseCheck()
	{
		if(StringUtil.IsEmpty(cmdId))
		{
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		
		if(3!= clientType && 2!= clientType && 1!= clientType && 4!= clientType && 5!= clientType)
		{
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}
		
		if(!"CX_01".equals(cmdType))
		{
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		
		if(StringUtil.IsEmpty(stbMac)){
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		return true;
	}

	@Override
	public String getReturnXml()
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		
		Element root = document.addElement("root");
		// 结果代码
		root.addElement("RstCode").addText("" + getResult());
		// 结果描述
		root.addElement("RstMsg").addText(getResultDesc());
		//mac
		root.addElement("Mac").addText(getStbMac());
		root.addElement("Loid").addText(getLoid());
		
		String returnParam=document.asXML();
		logger.warn("[{}]GetItvMacOBJ returnParam:[{}]",getStbMac(),returnParam);
		return returnParam;
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

	public String getLoid() {
		return loid;
	}

	public void setLoid(String loid) {
		this.loid = loid;
	}
	
	
}
