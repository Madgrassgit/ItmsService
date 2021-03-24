package com.linkage.stbms.ids.util;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class GetStbInfoXJChecker extends BaseChecker {
private static final Logger logger = LoggerFactory.getLogger(GetStbInfoXJChecker.class);
	
	private String inParam = null;

	public GetStbInfoXJChecker(String inParam){
		this.inParam = inParam;
	}
	
	/**
	 * 检查入参合法性
	 * @return
	 */
	public boolean check(){
		logger.debug("GetStbInfoXJChecker==>check()");
		
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
			 * 2：根据MAC地址查询
			 * 3：根据机顶盒序列号查询
			 */
			searchType = param.elementTextTrim("SearchType");
			/**
			 * 查询类型所对应的用户信息
			 * SelectType为1时为itv业务账号
			 * SelectType为2时为机顶盒MAC
			 * SelectType为3时为机顶盒序列号
			 */
			searchInfo = param.elementTextTrim("SearchInfo"); 
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			rstCode = "1";
			rstMsg = "入参格式错误";
			return false;
		}
		
		//参数合法性检查
		if (false == baseCheck() || false == searchTypeCheck()
				|| false == searchInfoCheck()) {
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
	
}
