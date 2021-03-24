package com.linkage.stbms.ids.util;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class DoServiceServiceChecker extends BaseChecker{
	
	private static final Logger logger = LoggerFactory.getLogger(DoServiceServiceChecker.class);
	
	private String inParam = null;

	public DoServiceServiceChecker(String inParam){
		this.inParam = inParam;
	}
	
	private int queryConditionType;
	
	private String queryCondition = null;
	
	private int operType ;
	
	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check(){
		
		logger.debug("StbBindServiceChecker==>check()");
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {

			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			
			/**
			 * 查询类型
			 * 1：根据业务帐号查询
			 * 2：根据机顶盒序列号查询
			 */
			queryConditionType = StringUtil.getIntegerValue(param.elementTextTrim("queryConditionType"));
			
			/**
			 * 查询类型所对应的用户信息
			 */
			queryCondition = param.elementTextTrim("queryCondition"); 
			
			/**
			 * 1：手工下发
			 * 2：返修登记
			 */
			operType = StringUtil.getIntegerValue(param.elementTextTrim("operType"));
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			rstCode = "1";
			rstMsg = "入参格式错误";
			return false;
		}
		
		
		//参数合法性检查
		if (false == baseCheck() || false == conditionTypeCheck()) {
			return false;
		}
		
		rstCode = "0";
		rstMsg = "成功";
		
		return true;
	}
	
	private boolean conditionTypeCheck()
	{
		if (1 != queryConditionType || 2 != queryConditionType)
		{
			rstCode = "1001";
			rstMsg = "查询条件类型非法";
			return false;
		}
		if (2 == queryConditionType && queryCondition.length() < 6)
		{
			rstCode = "1005";
			rstMsg = "按设备序列号查询时，查询序列号字段少于6位";
			return false;
		}
		if (1 != operType || 2 != operType)
		{
			rstCode = "1001";
			rstMsg = "查询条件类型非法";
			return false;
		}
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
		root.addElement("RstCode").addText(rstCode);
		// 结果描述
		root.addElement("RstMsg").addText(rstMsg);
		
		return document.asXML();
	}
	
	
	
	
	public String getInParam() {
		return inParam;
	}

	
	public void setInParam(String inParam) {
		this.inParam = inParam;
	}


	public int getQueryConditionType() {
		return queryConditionType;
	}

	public void setQueryConditionType(int queryConditionType) {
		this.queryConditionType = queryConditionType;
	}

	public String getQueryCondition() {
		return queryCondition;
	}


	public void setQueryCondition(String queryCondition) {
		this.queryCondition = queryCondition;
	}

	public int getOperType() {
		return operType;
	}

	public void setOperType(int operType) {
		this.operType = operType;
	}

}
