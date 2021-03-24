package com.linkage.stbms.ids.util;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class StbPingCheckerXJ extends BaseChecker{
	
	private static final Logger logger = LoggerFactory.getLogger(StbPingCheckerXJ.class);
	
	private String inParam = null;
	
	private String timeOut = null;
	
	private String dataSize = null;
	
	private String numberOfRepetitions = null;

	public StbPingCheckerXJ(String inParam){
		this.inParam = inParam;
	}
	
	/**
	 * 检查入参合法性
	 * @return
	 */
	public boolean check(){
		
		logger.debug("SetStbBindSNCheckerXJ==>check()");
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			
			searchType = param.elementTextTrim("SearchType"); // 查询类型:1：根据业务帐号查询; 2：根据MAC地址查询; 3：根据机顶盒序列号查询
			searchInfo = param.elementTextTrim("SearchInfo"); 
			ip = param.elementTextTrim("hostIp");
			
			timeOut = param.elementTextTrim("timeOut");
			dataSize = param.elementTextTrim("dataSize");
			numberOfRepetitions = param.elementTextTrim("numberOfRepetitions");
			
			if (timeOut != null && timeOut.trim().length() != 0) {
				Integer.parseInt(timeOut);
			}
			if (dataSize != null && dataSize.trim().length() != 0) {
				Integer.parseInt(dataSize);
			}
			if (numberOfRepetitions != null	&& numberOfRepetitions.trim().length() != 0) {
				Integer.parseInt(numberOfRepetitions);
			}
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			rstCode = "1";
			rstMsg = "入参格式错误";
			return false;
		}
		
		if(1!=clientType && 2!=clientType && 3!=clientType && 4!=clientType){
			logger.warn("clientType格式不对");
			rstCode = "1000";
			rstMsg = "clientType入参格式错误";
			return false;
		}
		
		//参数合法性检查
		if (false == baseCheck() || false == searchTypeCheck()
				|| false == searchInfoCheck() || false == ipCheck()) {
			return false;
		}
		
		rstCode = "0";
		rstMsg = "成功";
		
		return true;
	}
	
	/**
	 * 返回调用结果字符串
	 * 
	 */
	@Override
	public String getReturnXml(){
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("result_flag").addText(rstCode);
		// 结果描述
		root.addElement("result").addText(rstMsg);
		
		return document.asXML();
	}
	
	public String getInParam() {
		return inParam;
	}

	public void setInParam(String inParam) {
		this.inParam = inParam;
	}

	public String getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(String timeOut) {
		this.timeOut = timeOut;
	}

	public String getDataSize() {
		return dataSize;
	}

	public void setDataSize(String dataSize) {
		this.dataSize = dataSize;
	}

	public String getNumberOfRepetitions() {
		return numberOfRepetitions;
	}

	public void setNumberOfRepetitions(String numberOfRepetitions) {
		this.numberOfRepetitions = numberOfRepetitions;
	}
}

