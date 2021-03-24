package com.linkage.stbms.ids.util;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

public class GetStbInfoChecker extends BaseChecker{
	
	private static final Logger logger = LoggerFactory.getLogger(GetStbInfoChecker.class);
	
	private String inParam = null;

	public GetStbInfoChecker(String inParam){
		this.inParam = inParam;
	}
	
	
	
	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check(){
		
		logger.debug("GetStbInfoChecker==>check()");
		
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
	
	public boolean baseCheck(){
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)){
			rstCode = "1000";
			rstMsg = "接口调用唯一ID非法";
			return false;
		}
		
		if(false == "CX_01".equals(cmdType)){
			rstCode = "3";
			rstMsg = "接口类型非法";
			return false;
		}
		
		if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType
				&& 5 != clientType && 6 != clientType ){
			if("jx_dx".equals(Global.G_instArea) && (7 == clientType || 8 == clientType || 9 == clientType)){
				return true;
			}else{
				rstCode = "2";
				rstMsg = "客户端类型非法";
				return false;
			}
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
		root.addElement("result_flag").addText(rstCode);
		// 结果描述
		root.addElement("result").addText(rstMsg);
		
		return document.asXML();
	}
	
	/**
	 * JXDX-ITV-REQ-20170427-WUWF-001(ITV终端管理平台对外接口-机顶盒信息修改)
	 * 要求入参回参编码一致gb2312
	 * @return
	 */
	public String getJXReturnXml(){
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GB2312");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("result_flag").addText(rstCode);
		// 结果描述
		root.addElement("result").addText(rstMsg);
		
		Element sheets = root.addElement("Sheets");
		Element sheetInfo = sheets.addElement("sheetInfo");
		// 查无此设备时需将成功时返回的各列信息以空值返回
		sheetInfo.addElement("first_time").addText("");
		sheetInfo.addElement("stb_mac").addText("");
		sheetInfo.addElement("stb_vendor").addText("");
		sheetInfo.addElement("stb_status").addText("");
		sheetInfo.addElement("stb_sn").addText("");
		sheetInfo.addElement("serv_account").addText("");
		sheetInfo.addElement("stb_type").addText("");
		sheetInfo.addElement("stb_ip").addText("");
		sheetInfo.addElement("softversion").addText("");
		// JXDX-ITV-REQ-20171227-WUWF-001(ITV终端网管平台-对外接口机顶盒信息查询接口调整)  stb_type1为新增返回值
		sheetInfo.addElement("stb_type1").addText("");
		//JXDX-ITV-REQ-20180918-WUWF-001(ITV终端管理平台对外接口-机顶盒信息) 新增bind_time字段
		sheetInfo.addElement("bind_time").addText("");
		return document.asXML();
	}
	
	
	
	
	public String getInParam() {
		return inParam;
	}

	
	public void setInParam(String inParam) {
		this.inParam = inParam;
	}
	
}
