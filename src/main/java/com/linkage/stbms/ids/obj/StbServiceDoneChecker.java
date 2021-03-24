
package com.linkage.stbms.ids.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 业务下发对象
 * 
 * @author Jason(3412)
 * @date 2010-6-21
 */
public class StbServiceDoneChecker {

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(StbServiceDoneChecker.class);
	private String inParam = null;
	private String cmdId = "";

	private String rstCode = "";
	private String rstMsg = "";
	private String cmdType = "";
	private int clientType = 0;

	private int searchType;
	private String searchInfo;

	/**
	 * 构造方法
	 * 
	 * @param inXml  接口调用入参，xml字符串
	 */
	public StbServiceDoneChecker(String inXml) {
		inParam = inXml;
	}

	/**
	 * 参数合法性检查
	 */
	public boolean check() {
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");

			searchType = StringUtil.getIntegerValue(param.elementTextTrim("SearchType"));
			searchInfo = param.elementTextTrim("SearchInfo");
		}
		catch (Exception e) {
			e.printStackTrace();
			rstCode = "0";
			rstMsg = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == searchInfoCheck()) {
			return false;
		}
		rstCode = "1";
		rstMsg = "业务下发成功";
		return true;
	}

	/**
	 * 数据验证
	 * @return
	 */
	private boolean searchInfoCheck(){
		if(3 != searchType && 2 != searchType && 1 != searchType){
			rstCode = "1002";
			rstMsg = "用户信息类型非法";
			return false;
		}
		if (StringUtil.IsEmpty(searchInfo)) {
			rstCode = "1002";
			rstMsg = "用户信息不合法";
			return false;
		}
		return true;
	}
	
	/**
	 * 基本验证
	 * @return
	 */
	private boolean baseCheck() {
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)) {
			rstCode = "1000";
			rstMsg = "接口调用唯一ID非法";
			return false;
		}
		
		if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType) {
			rstCode = "2";
			rstMsg = "客户端类型非法";
			return false;
		}
		
		if(false == "CX_01".equals(cmdType)) {
			rstCode = "3";
			rstMsg = "接口类型非法";
			return false;
		}
		return true;
	}

	/**
	 * 返回结果字符串
	 */
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(StringUtil.getStringValue(rstCode));
		// 结果描述
		root.addElement("RstMsg").addText(StringUtil.getStringValue(rstMsg));
		return document.asXML();
	}

	public String getRstCode() {
		return rstCode;
	}

	public void setRstCode(String rstCode) {
		this.rstCode = rstCode;
	}

	public String getRstMsg() {
		return rstMsg;
	}

	public void setRstMsg(String rstMsg) {
		this.rstMsg = rstMsg;
	}

	public int getSearchType() {
		return searchType;
	}

	public void setSearchType(int searchType) {
		this.searchType = searchType;
	}

	public String getSearchInfo() {
		return searchInfo;
	}

	public void setSearchInfo(String searchInfo) {
		this.searchInfo = searchInfo;
	}

	public String getCmdId() {
		return cmdId;
	}

	public void setCmdId(String cmdId) {
		this.cmdId = cmdId;
	}
}
