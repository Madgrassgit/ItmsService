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
 * 内蒙古电信 机顶盒状态查询接口
 * @author hourui
 * @date 2017-11-20
 * @param inParam
 * @return
 */

public class GetStbOrderStatusNMGChecker extends BaseChecker {

	private static final Logger logger = LoggerFactory.getLogger(GetStbOrderStatusNMGChecker.class);

	private String inParam = null;
	
	
	private String errordesc;

	public GetStbOrderStatusNMGChecker(String inParam) {
		this.inParam = inParam;
	}

	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check() {

		logger.debug("GetStbOrderStatusNMGChecker==>check()");

		SAXReader reader = new SAXReader();
		Document document = null;

		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();

			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType")); // 1：BSS, 2：IPOSS, 3：综调, 4：RADIUS

			Element param = root.element("Param");

			searchType = param.elementTextTrim("SearchType"); // 查询类型 1：用户业务账号;2：MAC地址
			
			searchInfo = param.elementTextTrim("SearchInfo"); // 查询条件信息
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			rstCode = "0";
			rstMsg = "入参格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == searchTypeCheck()|| false == searchInfoCheck4NMG()) {
			return false;
		}

		rstCode = "1";
		rstMsg = "成功";

		return true;
	}
	
	boolean searchInfoCheck4NMG(){
	
		
		if(StringUtil.IsEmpty(searchInfo)){
			rstCode = "1002";
			rstMsg = "字段searchInfo不能为空";
			return false;
		}
		// 2：机顶盒MAC
		else if ("2".equals(searchType)) {
			if(false == macPattern.matcher(searchInfo).matches()){
				rstCode = "1004";
				rstMsg = "MAC地址不合法";
				return false;
			}
		}
		return true;
	}
    
	/**
	 * 查询类型
	 * 1： 业务帐号
	 * 2：机顶盒MAC
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
		
		if (!"1".equals(searchType) && !"2".equals(searchType) ) {
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
	 * 生成接口回参
	 * 
	 * @param infoMap 
	 * @return
	 */
	@Override
	public String getReturnXml()
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GB2312");
		Element root = document.addElement("root");
		root.addElement("result_flag").addText(rstCode);
		root.addElement("result").addText(rstMsg);
		return document.asXML();
	}

	public String getInParam() {
		return inParam;
	}

	public void setInParam(String inParam) {
		this.inParam = inParam;
	}


	public String getErrordesc() {
		return errordesc;
	}

	public void setErrordesc(String errordesc) {
		this.errordesc = errordesc;
	}

	

}
