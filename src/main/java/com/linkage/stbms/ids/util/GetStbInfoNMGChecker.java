package com.linkage.stbms.ids.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkage.stbms.ids.util.BaseChecker;
import com.linkage.commons.util.StringUtil;

public class GetStbInfoNMGChecker extends BaseChecker {
private static final Logger logger = LoggerFactory.getLogger(GetStbInfoNMGChecker.class);
	
	private String inParam = null;
	private  ArrayList<Map<String,String>> stbInfoList= null;
	public GetStbInfoNMGChecker(String inParam){
		this.inParam = inParam;
	}
	
	/**
	 * 检查入参合法性
	 * @return
	 */
	public boolean check(){
		logger.debug("GetStbInfoNMGChecker==>check()");
		
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
	 * 查询类型
	 * 1： 业务帐号
	 * 2：机顶盒MAC（不提供）
	 * 3：机顶盒序列号（不提供）
	 * @return
	 */
	@Override
	boolean searchTypeCheck(){
		if(StringUtil.IsEmpty(searchType)){
			rstCode = "1003";
			rstMsg = "字段searchType不能为空";
			return false;
		}
		
		if (!"1".equals(searchType) ) {
			rstCode = "1001";
			rstMsg = "查询类型非法";
			return false;
		}
		return true;
	}
	@Override
	public boolean baseCheck(){
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)){
			rstCode = "1000";
			rstMsg = "接口调用唯一ID非法";
			return false;
		}
		
		if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType) {
			rstCode = "2";
			rstMsg = "客户端类型非法";
			return false;
		}
		
		if(false == "CX_01".equals(cmdType)){
			rstCode = "3";
			rstMsg = "接口类型非法";
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

	
	public ArrayList<Map<String, String>> getStbInfoList()
	{
		return stbInfoList;
	}

	
	public void setStbInfoList(ArrayList<Map<String, String>> stbInfoList)
	{
		this.stbInfoList = stbInfoList;
	}
	
}
