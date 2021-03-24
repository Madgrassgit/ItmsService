
package com.linkage.stbms.ids.util;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.ids.dao.UserChangeStbDAO;

/**
 * @author yinlei3 (73167)
 * @version 1.0
 * @since 2015年9月7日
 * @category com.linkage.stbms.ids.util
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class UserChangeStbChecker extends BaseChecker
{

	private static final Logger logger = LoggerFactory
			.getLogger(UserChangeStbChecker.class);
	private String inParam = null;
	/** 新机顶盒MAC */
	private String newStbMac = null;
	/** 手机IMEI值 */
	private String imei = null;
	/** 旧的MAC地址 **/
	private String oldStdMac = "";

	public String getOldStdMac()
	{
		return oldStdMac;
	}

	public void setOldStdMac(String oldStdMac)
	{
		this.oldStdMac = oldStdMac;
	}

	/** 构造参数 */
	public UserChangeStbChecker(String inParam)
	{
		this.inParam = inParam;
	}

	/**
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check()
	{
		logger.debug("UserChangeStbChecker==>check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			/**
			 * 查询类型 1：根据itv业务帐号查询 2：根据MAC地址查询
			 */
			selectType = param.elementTextTrim("SelectType");
			/**
			 * 查询类型所对应的用户信息 SelectType为1时为itv业务账号 SelectType为2时为机顶盒MAC
			 */
			userInfo = param.elementTextTrim("UserInfo");
			newStbMac = param.elementTextTrim("NewStbMac");
			imei = param.elementTextTrim("IMEI");
			userName = param.elementTextTrim("UserName");
		}
		catch (Exception e)
		{
			logger.error("inParam format is err,mesg({})", e.getMessage());
			rstCode = "1";
			rstMsg = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == selectTypeCheck()
				|| false == userInfoCheck() || false == oldMacCheck()
				|| false == newMacCheck())
		{
			return false;
		}
				
		if (2 != clientType && StringUtil.IsEmpty(imei))
		{
			rstCode = "1006";
			rstMsg = "用户IMEI信息不合法";
			return false;
		}
		rstCode = "0";
		rstMsg = "成功";
		return true;
	}

	public boolean baseCheck()
	{
		logger.debug("baseCheck()");
		if (StringUtil.IsEmpty(cmdId))
		{
			rstCode = "1000";
			rstMsg = "接口调用唯一ID非法";
			return false;
		}
		if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType
				&& 5 != clientType && 6 != clientType)
		{
			rstCode = "2";
			rstMsg = "客户端类型非法";
			return false;
		}
		if (false == "CX_02".equals(cmdType))
		{
			rstCode = "3";
			rstMsg = "接口类型非法";
			return false;
		}
		return true;
	}

	/**
	 * 旧MAC地址合法性检查
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	private boolean oldMacCheck()
	{
		if ("2".equals(selectType))
		{
			if (false == macPattern.matcher(userInfo).matches())
			{
				rstCode = "1004";
				rstMsg = "旧MAC地址不合法";
				return false;
			}
		}
		return true;
	}

	/**
	 * 新MAC地址合法性检查
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	private boolean newMacCheck()
	{
		if (false == macPattern.matcher(newStbMac).matches())
		{
			rstCode = "1003";
			rstMsg = "新机顶盒MAC不合法";
			return false;
		}
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
		root.addElement("RstCode").addText(rstCode);
		// 结果描述
		root.addElement("RstMsg").addText(rstMsg);
		// 根据IMEI值是不是为空来判断是不是将日志插入数据库
//		if (imei != null && imei != "")
//		{
		UserChangeStbDAO dao = new UserChangeStbDAO();
		String newMac = getNewStbMac();
		dao.insertLogUserChangerStb(imei, oldStdMac, newMac, inParam,clientType,userName);
//		}
		logger.warn("UserChangeStbService==>return：" + document.asXML());
		return document.asXML();
	}

	public String getInParam()
	{
		return inParam;
	}

	public void setInParam(String inParam)
	{
		this.inParam = inParam;
	}

	public String getNewStbMac()
	{
		return newStbMac;
	}

	public void setNewStbMac(String newStbMac)
	{
		this.newStbMac = newStbMac;
	}

	public String getImei()
	{
		return imei;
	}

	public void setImei(String imei)
	{
		this.imei = imei;
	}
}
