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
 * 新疆电信 获取机顶盒ip地址接口
 * 
 * @author chenxj6
 * @date 2016-8-30
 * @param inParam
 * @return
 * 
 */
public class GetStbIpChecker extends BaseChecker {

	private static final Logger logger = LoggerFactory
			.getLogger(GetStbIpChecker.class);

	private String inParam = null;

	public GetStbIpChecker(String inParam) {
		this.inParam = inParam;
	}

	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check() {

		logger.debug("GetStbIpChecker==>check()");

		SAXReader reader = new SAXReader();
		Document document = null;

		try {

			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();

			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType")); // 1：BSS, 2：IPOSS, 3：综调, 4：RADIUS

			Element param = root.element("Param");

			searchType = param.elementTextTrim("SearchType"); // 查询类型 1：根据业务帐号查询 2：根据MAC地址查询 3：根据机顶盒序列号查询
			
			searchInfo = param.elementTextTrim("SearchInfo"); // 查询条件信息

		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			rstCode = "1";
			rstMsg = "入参格式错误";
			return false;
		}

		if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType) {
			rstCode = "2";
			rstMsg = "客户端类型非法";
			return false;
		}

		// 参数合法性检查
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
	public String getReturnXml() {
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

}
