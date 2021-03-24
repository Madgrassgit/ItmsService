package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;

/**
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2017年11月19日
 * @category com.linkage.itms.dispatch.cqdx.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class TraceRouteDealXML extends BaseDealXML 
{
	private static Logger logger = LoggerFactory.getLogger(TraceRouteDealXML.class);
	SAXReader reader = new SAXReader();
	//trace目的地址
	private String traceHost = "";
	//每跳尝试请求次数
	private String numberOfTries = "";
	//最大跳数
	private String maxHopCount = "";
	//数据包大小
	private String dataBlockSize = "";
	
	//终端trace route测试的响应超时时间
	private String timeOut = "";
	
	
	//响应时间
	private String responseTime = "";
	//跳转数
	private String routeNumberOfentries = "";
	
	private List<RouteHopsOBJ> routeHopsList;
//	//跳转主机
//	private String hopHost;
//	//跳转地址
//	private String hopHostAddress;
//	//错误码
//	private String hopErrorCode;
//	//次数
//	private String hopRTTimes;
	


	public String returnXML() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("response");
		
		root.addElement("result_code").addText(result);
		root.addElement("result_desc").addText(errMsg);
		
		Element resultInfoparam = root.addElement("result_info");
		resultInfoparam.addElement("response_time").addText(responseTime);
		resultInfoparam.addElement("route_hops_number_of_entries").addText(routeNumberOfentries);
		
		if(null != routeHopsList)
		{
			Element routeHopsParam = resultInfoparam.addElement("route_hops");
			for(RouteHopsOBJ obj : routeHopsList)
			{
				routeHopsParam.addElement("hop_host").addText(obj.getHopHost());
				routeHopsParam.addElement("hop_host_address").addText(obj.getHopHostAddress());
				routeHopsParam.addElement("hop_error_code").addText(obj.getHopErrorCode());
				routeHopsParam.addElement("hop_rt_times").addText(obj.getHopRTTimes());
			}
		}
		
		String returnXML = document.asXML();
		logger.warn("TraceRouteDealXML-returnXML:{}",returnXML);
		return returnXML;
	}

	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			logicId = inRoot.elementTextTrim("logic_id");
			pppUsename = inRoot.elementTextTrim("ppp_username");
			

			Element param = inRoot.element("trace_param");
			traceHost = StringUtil.getStringValue(param.elementTextTrim("trace_host"));
			
			numberOfTries = StringUtil.getStringValue(param.elementTextTrim("number_of_tries"));
			maxHopCount = StringUtil.getStringValue(param.elementTextTrim("max_hop_count"));
			dataBlockSize = StringUtil.getStringValue(param.elementTextTrim("data_block_size"));
			timeOut = StringUtil.getStringValue(param.elementTextTrim("time_out"));
			
			if(StringUtil.isEmpty(logicId) && StringUtil.isEmpty(pppUsename))
			{
				result = "-99";
				errMsg="宽带帐号或逻辑id为空";
				logger.warn("宽带帐号或逻辑id为空");
				return null;
			}
			if(StringUtil.isEmpty(traceHost))
			{
				result = "-99";
				errMsg="trace_host参数为空";
				logger.warn("trace_host为空");
				return null;
			}
			if(StringUtil.isEmpty(numberOfTries))
			{
				result = "-99";
				errMsg="number_of_tries参数为空";
				return null;
			}
			if(StringUtil.isEmpty(maxHopCount))
			{
				result = "-99";
				errMsg="max_hop_count参数为空";
				return null;
			}
			
			if(StringUtil.isEmpty(dataBlockSize))
			{
				result = "-99";
				errMsg="data_block_size参数为空";
				return null;
			}
			if(StringUtil.isEmpty(timeOut))
			{
				result = "-99";
				errMsg="time_out参数为空";
				return null;
			}
			
			return inDocument;
		} catch (Exception e) {
			logger.error("TraceRouteDealXML.getXML() is error!", e);
			return null;
		}
	}
	
	public String getTraceHost()
	{
		return traceHost;
	}

	
	public void setTraceHost(String traceHost)
	{
		this.traceHost = traceHost;
	}

	
	public String getNumberOfTries()
	{
		return numberOfTries;
	}

	
	public void setNumberOfTries(String numberOfTries)
	{
		this.numberOfTries = numberOfTries;
	}

	
	public String getMaxHopCount()
	{
		return maxHopCount;
	}

	
	public void setMaxHopCount(String maxHopCount)
	{
		this.maxHopCount = maxHopCount;
	}

	
	public String getDataBlockSize()
	{
		return dataBlockSize;
	}

	
	public void setDataBlockSize(String dataBlockSize)
	{
		this.dataBlockSize = dataBlockSize;
	}

	
	public String getTimeOut()
	{
		return timeOut;
	}

	
	public void setTimeOut(String timeOut)
	{
		this.timeOut = timeOut;
	}

	
	public SAXReader getReader()
	{
		return reader;
	}

	
	public void setReader(SAXReader reader)
	{
		this.reader = reader;
	}

	
	public String getResponseTime()
	{
		return responseTime;
	}

	
	public void setResponseTime(String responseTime)
	{
		this.responseTime = responseTime;
	}

	
	public String getRouteNumberOfentries()
	{
		return routeNumberOfentries;
	}

	
	public void setRouteNumberOfentries(String routeNumberOfentries)
	{
		this.routeNumberOfentries = routeNumberOfentries;
	}

	
//	public String getHopHost()
//	{
//		return hopHost;
//	}
//
//	
//	public void setHopHost(String hopHost)
//	{
//		this.hopHost = hopHost;
//	}
//
//	
//	public String getHopHostAddress()
//	{
//		return hopHostAddress;
//	}
//
//	
//	public void setHopHostAddress(String hopHostAddress)
//	{
//		this.hopHostAddress = hopHostAddress;
//	}
//
//	
//	public String getHopErrorCode()
//	{
//		return hopErrorCode;
//	}
//
//	
//	public void setHopErrorCode(String hopErrorCode)
//	{
//		this.hopErrorCode = hopErrorCode;
//	}
//
//	
//	public String getHopRTTimes()
//	{
//		return hopRTTimes;
//	}
//
//	
//	public void setHopRTTimes(String hopRTTimes)
//	{
//		this.hopRTTimes = hopRTTimes;
//	}

	
	public List<RouteHopsOBJ> getRouteHopsList()
	{
		return routeHopsList;
	}

	
	public void setRouteHopsList(List<RouteHopsOBJ> routeHopsList)
	{
		this.routeHopsList = routeHopsList;
	}
	
	
}
