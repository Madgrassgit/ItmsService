package com.linkage.itms.nmg.dispatch.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
* 项目名称：ailk-itms-ItmsService   
* 类名称：BizbindInfoChecker   
* 类描述：   
* 创建人：guxl3   
* 创建时间：2019年3月27日 上午11:50:54   
* @version
 */
public class BizbindInfoChecker extends NmgBaseChecker
{
	
	private static final Logger logger = LoggerFactory.getLogger(BizbindInfoChecker.class);
	
	private String inParam = null;

	public BizbindInfoChecker(String inParam){
		this.inParam = inParam;
	}
	
	// 正则，字符加数字
		private Pattern reg = Pattern.compile("\\w{1,}+");
	
	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check(){
		
		logger.debug("BizbindInfoChecker==>check()"+inParam);
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {

			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			
			cmdId = root.elementTextTrim("CmdID");
			logger.warn(cmdId);
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			
			userInfoType = Integer.parseInt(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo"); 
			devSn = param.elementTextTrim("DevSN"); 
			cityId = param.elementTextTrim("CityId"); 
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		
		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck())
		{
			return false;
		}
		
		if (!StringUtil.IsEmpty(devSn)
				&& (false == reg.matcher(devSn).matches() || devSn.length() < 6))
		{
			result = 1005;
			resultDesc = "设备序列号不合法";
			return false;
		}
		result = 0;
		resultDesc = "成功";
		
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
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		Element param = root.addElement("Param");
		param.addElement("pppoe_Account").addText("");
		param.addElement("voip_Account").addText("");
		param.addElement("iptv_Account").addText("");
		param.addElement("city_id").addText("");
		param.addElement("device_status").addText("");
		param.addElement("vendor").addText("");
		param.addElement("DevModel").addText("");
		param.addElement("HardwareVersion").addText("");
		param.addElement("SoftwareVersion").addText("");
		param.addElement("complete_time").addText("");
		param.addElement("loid").addText("");
		param.addElement("devsn").addText("");
		param.addElement("access_type").addText("");
		param.addElement("lan_num").addText("");
		param.addElement("VoIP_num").addText("");
		param.addElement("MacAddress").addText("");
		param.addElement("WanType").addText("");
		param.addElement("telecom_password").addText("");
		return document.asXML();
	}
	
	public String commonReturnParam(ArrayList<LinkedHashMap<String, String>> devList, Map<String, String> infoMap)
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		if(infoMap != null && !infoMap.isEmpty()){
			for(Object key : infoMap.keySet())
			{
				root.addElement(key.toString()).addText(infoMap.get(key));
			}
		}
		Element param = root.addElement("Param");
		for(LinkedHashMap<String,String> devMap : devList)
		{
			for(Object keyStr : devMap.keySet())
			{
				param.addElement(keyStr.toString()).addText(devMap.get(keyStr));
			}
		}
		return document.asXML();
	}
	
	
	public String getInParam() {
		return inParam;
	}

	
	public void setInParam(String inParam) {
		this.inParam = inParam;
	}
}
