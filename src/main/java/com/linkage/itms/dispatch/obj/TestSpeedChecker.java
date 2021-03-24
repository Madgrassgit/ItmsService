package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 山东联通RMS hgu单个用户测速接口checker类
 * @author fanjm 35572
 * @version 1.0
 * @since 2016年11月29日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class TestSpeedChecker extends BaseChecker{

private static final Logger logger = LoggerFactory.getLogger(TestSpeedChecker.class);
	
	private String inParam = null;

	public TestSpeedChecker(String inParam){
		this.inParam = inParam;
	}
	
	
	
	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check(){
		
		logger.debug("TestSpeedChecker==>check()");
		
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
			1：用户宽带帐号
			2：用户IPTV宽带帐号
			*/
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			/**
			 * 查询类型所对应的用户信息
			 * SelectType为1时为itv业务账号
			 * SelectType为2时为机顶盒MAC
			 * SelectType为3时为机顶盒序列号
			 */
			userInfo = param.elementTextTrim("UserInfo"); 
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			//赋一个值，否则getReturnXml的addtext改字段会出现错误造成调用失败
			cmdId="123456789012345";
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		
		//参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		
		return true;
	}
	
	
	/**
	 * 返回调用结果字符串
	 * 
	 * @author fanjm(35572)
	 * @date 2016-11-29
	 * @return boolean 是否校验通过
	 */
	@Override
	public String getReturnXml(){
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(cmdId);
		// 结果代码
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		
		
		return document.asXML();
	}
	
	
	/**
	 * 基本信息合法性检查
	 * 
	 * @author fanjm(35572)
	 * @date 2016-11-29
	 * @return boolean 是否校验通过
	 */
	public boolean baseCheck(){
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)){
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		
		if(3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType ){
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}
		
		if(false == "CX_01".equals(cmdType)){
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * 用户信息类型合法性检查
	 * 
	 * @author fanjm(35572)
	 * @date 2016-11-29
	 * @return boolean 是否校验通过
	 */
	boolean userInfoTypeCheck(){
		if(1 != userInfoType && 2 != userInfoType){
			result = 1002;
			resultDesc = "用户信息类型非法";
			return false;
		}
		return true;
	}
	
	
	public String getInParam() {
		return inParam;
	}

	
	public void setInParam(String inParam) {
		this.inParam = inParam;
	}
	
}
