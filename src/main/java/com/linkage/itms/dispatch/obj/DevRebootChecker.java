package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * 设备重启接口的XML元素对象
 * @author zhangsm(工号) Tel:??
 * @version 1.0
 * @since 2012-2-25 下午02:59:10
 * @category com.linkage.itms.dispatch.obj
 * @copyright 南京联创科技 网管科技部
 *
 */
public class DevRebootChecker extends BaseChecker
{
	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(DevRebootChecker.class);
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public DevRebootChecker(String inXml) {
		callXml = inXml;
	}
	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
			if ("jx_dx".equals(Global.G_instArea))
			{
				cityId = param.elementTextTrim("CityId");
			}
			

		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		if ("nx_dx".equals(Global.G_instArea)) {
			if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType) {
				result = 2;
				resultDesc = "客户端类型非法";
				return false;
			}
			if (1 != userInfoType && 2 != userInfoType && 3 != userInfoType 
					&& 4 != userInfoType && 5 != userInfoType) {
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
		}

		// 参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}
		// 江西电信cityId为必须入力项, 其他电信不是
		if ("jx_dx".equals(Global.G_instArea) && false == cityIdCheck())
		{
			return false;
		}

		result = 0;
		resultDesc = "重启成功";
		
		return true;
	}

	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);

		return document.asXML();
	}
}
