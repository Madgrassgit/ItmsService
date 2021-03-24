
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
 * @author Reno (Ailk No.)
 * @version 1.0
 * @since 2015年7月3日
 * @category com.linkage.stbms.ids.util
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class GetStbOnlineStatusChecker extends BaseChecker
{

	private static final Logger logger = LoggerFactory
			.getLogger(GetStbOnlineStatusChecker.class);
	private String inParam = null;
	// 1：在线 -1：不在线
	private int onlineStatus;

	// 有参构造函数
	public GetStbOnlineStatusChecker(String inParam)
	{
		this.inParam = inParam;
	}

	@Override
	public boolean check()
	{
		
		logger.debug("GetStbOnlineStatusService==>check()");
		
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
			logger.error("inParam format is err,msg({})", e.getMessage());
			rstCode = "0";
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
		root.addElement("result_flag").addText(rstCode);
		// 结果描述
		root.addElement("result").addText(rstMsg);
		if (!"0".equals(rstCode))
		{
			Element paramEle = root.addElement("Param");
			// 设备在线状态
			paramEle.addElement("OnlineStatus").addText(
					StringUtil.getStringValue(onlineStatus));
		}

		return document.asXML();
	}

	public String getInParam()
	{
		return inParam;
	}

	public int getOnlineStatus()
	{
		return onlineStatus;
	}

	public void setInParam(String inParam)
	{
		this.inParam = inParam;
	}

	public void setOnlineStatus(int onlineStatus)
	{
		this.onlineStatus = onlineStatus;
	}
}
