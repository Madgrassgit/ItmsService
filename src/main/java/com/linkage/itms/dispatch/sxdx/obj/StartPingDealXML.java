package com.linkage.itms.dispatch.sxdx.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

/**
 * 甘肃电信终端PING接口(启动PING测试)
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月21日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class StartPingDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(StartPingDealXML.class);
	SAXReader reader = new SAXReader();

	private String index = "";
	private String type = "";
	private String inftype = "";
	private String ip = "";
	private int size = 0;
	private int num = 0;
	private int overtime = 0;

	public StartPingDealXML(String methodName){
		super(methodName);
	}


	public int checkXML(String inXml) {
		this.inXml = inXml;
		try
		{
			logger.warn(methodName+"["+opId+"]入参校验开始");
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("op_id"));
			type = StringUtil.getStringValue(inRoot.elementTextTrim("type"));
			index = StringUtil.getStringValue(inRoot.elementTextTrim("index"));
			
			inftype = StringUtil.getStringValue(inRoot.elementTextTrim("inftype"));
			ip = StringUtil.getStringValue(inRoot.elementTextTrim("ip"));
			size = StringUtil.getIntegerValue(inRoot.elementTextTrim("size"));
			num = StringUtil.getIntegerValue(inRoot.elementTextTrim("num"));
			overtime = StringUtil.getIntegerValue(inRoot.elementTextTrim("overtime"));
			
			if( StringUtil.IsEmpty(type))
			{
				logger.warn(methodName+"["+opId+"]查询类型type为空");
				return -2;
			}
			else if(!"0".equals(type) && !"1".equals(type) && !"2".equals(type) && !"3".equals(type) && !"4".equals(type) && !"5".equals(type)){
				logger.warn(methodName+"["+opId+"]查询类型type范围非法：{}", type);
				return -2;
			}
			else if(StringUtil.IsEmpty(index)){
				logger.warn(methodName+"["+opId+"]查询值index为空");
				return -2;
			}
			else if(StringUtil.IsEmpty(inftype)){
				logger.warn(methodName+"["+opId+"]查询值interface为空");
				return -2;
			}
			else if(StringUtil.IsEmpty(ip)){
				logger.warn(methodName+"["+opId+"]查询值ip为空");
				return -2;
			}
			else if(0==size){
				logger.warn(methodName+"["+opId+"]查询值size为空");
				return -2;
			}
			else if(0==num){
				logger.warn(methodName+"["+opId+"]查询值num为空");
				return -2;
			}
			else if(0==overtime){
				logger.warn(methodName+"["+opId+"]查询值overtime为空");
				return -2;
			}
			
			return 1;
		} catch (Exception e) {
			logger.error(methodName+"["+opId+"] Excetion occured!", e);
			return -2;
		}
	}

	public String getIndex()
	{
		return index;
	}

	public void setIndex(String index)
	{
		this.index = index;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}


	
	public SAXReader getReader()
	{
		return reader;
	}


	
	public void setReader(SAXReader reader)
	{
		this.reader = reader;
	}


	
	public String getInftype()
	{
		return inftype;
	}


	
	public void setInftype(String inftype)
	{
		this.inftype = inftype;
	}


	
	public String getIp()
	{
		return ip;
	}


	
	public void setIp(String ip)
	{
		this.ip = ip;
	}


	
	public int getSize()
	{
		return size;
	}


	
	public void setSize(int size)
	{
		this.size = size;
	}


	
	public int getNum()
	{
		return num;
	}


	
	public void setNum(int num)
	{
		this.num = num;
	}


	
	public int getOvertime()
	{
		return overtime;
	}


	
	public void setOvertime(int overtime)
	{
		this.overtime = overtime;
	}

	
}
