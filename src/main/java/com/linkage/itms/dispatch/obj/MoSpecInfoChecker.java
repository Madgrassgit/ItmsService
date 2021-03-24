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
 * MoSpecInfo方法接口的XML元素对象
 * @author yinlei3 (73167)
 * @version 1.0
 * @since 2015年9月21日
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class MoSpecInfoChecker extends BaseChecker
{
	private static Logger logger = LoggerFactory.getLogger(MoSpecInfoChecker.class);
	private String oldSpecInfo;
	private String newSpecInfo;
	private int isSucc = 1;
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public MoSpecInfoChecker(String inXml) {
		callXml = inXml;
	}
	@Override
	public boolean check()
	{
		SAXReader reader = new SAXReader();
		Document document = null;
			try
			{
				document = reader.read(new StringReader(callXml));
				Element root = document.getRootElement();
				cmdId = root.elementTextTrim("CmdID");
				cmdType = root.elementTextTrim("CmdType");
				clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
				Element param = root.element("Param");
				userInfoType = StringUtil.getIntegerValue(param
						.elementTextTrim("UserInfoType"));
				userInfo = param.elementTextTrim("UserName");
				oldSpecInfo = param.elementTextTrim("OldSpecInfo");
				newSpecInfo = param.elementTextTrim("NewSpecInfo");
				cityId = param.elementTextTrim("CityId");
			}
			catch (Exception e)
			{
				e.printStackTrace();
				result = 1 ;
				resultDesc = "入参格式错误";
				return false;
			}
			// 参数合法性检查
			if (false == baseCheck() || false == userInfoTypeCheck()
					|| false == userInfoCheck() || false == newSpecInfoCheck()) {
				return false;
			}

			String localCityId = transCityId(cityId);
			if (StringUtil.IsEmpty(localCityId))
			{
				result = 1007;
				resultDesc = "属地非法";
				return false;
			}

			result = 0;
			resultDesc = "成功";
			
			return true;
	}

	
	/**
	 * <pre>
	 * 将外部的城市ID转换为内部数据库城市ID
	 * AREA_ID	AREA_NAME	AREA_CODE
	 * 3	南京	nj.js.cn
	 * 4	镇江	zj.js.cn
	 * 15	无锡	wx.js.cn
	 * 20	苏州	sz.js.cn
	 * 26	南通	nt.js.cn
	 * 33	扬州	yz.js.cn
	 * 39	盐城	yc.js.cn
	 * 48	徐州	xz.js.cn
	 * 60	淮安	ha.js.cn
	 * 63	连云港	lyg.js.cn
	 * 69	常州	cz.js.cn
	 * 79	泰州	tz.js.cn
	 * 84	宿迁	sq.js.cn
	 * </pre>
	 * 
	 * @param cityId
	 * @return
	 */
	public String transCityId(String cityId)
	{
		if ("3".equals(cityId))
		{
			return "0100";
		}
		else if ("4".equals(cityId))
		{
			return "0500";
		}
		else if ("15".equals(cityId))
		{
			return "0300";
		}
		else if ("20".equals(cityId))
		{
			return "0200";
		}
		else if ("26".equals(cityId))
		{
			return "0700";
		}
		else if ("33".equals(cityId))
		{
			return "0600";
		}
		else if ("39".equals(cityId))
		{
			return "1100";
		}
		else if ("48".equals(cityId))
		{
			return "0900";
		}
		else if ("60".equals(cityId))
		{
			return "1000";
		}
		else if ("63".equals(cityId))
		{
			return "1200";
		}
		else if ("69".equals(cityId))
		{
			return "0400";
		}
		else if ("79".equals(cityId))
		{
			return "0800";
		}
		else if ("84".equals(cityId))
		{
			return "1300";
		}
		return "";
	}

	/**
	 * <pre>
	 * 将接口中设备类型转换为内部数据库设备规格类型
	 * RES_TYPE_ID	RES_NAME
	 * 1208	E8-C(XDSL)
	 * 1210	E8-C(LAN)
	 * 1281	1+1E8-C(PON)
	 * 1282	2+1E8-C(PON)
	 * 1283	4+2E8-C(PON)
	 * 1284	4+8政企融合网关(PON)E8-C
	 * 1290 4+1E8-C(PON)
	 * 1344	4+4政企融合网关(PON)
	 * 1352	2+0E8-C(PON)
	 * 1358	4+8政企融合网关(PON)通用
	 * 8522	政企融合网关(4+2)(PON)
	 * 8586	4+2E8-C(PON_BASIC)
	 * </pre>
	 * 
	 * @param specInfo
	 * @return
	 */
	public String transSpecInfo(String specInfo)
	{
		if ("1208".equals(specInfo))
		{
			return "";
		}
		else if ("1210".equals(specInfo))
		{
			return "";
		}
		else if ("1281".equals(specInfo))
		{
			// E8CP11 PON 1+1
			return "2";
		}
		else if ("1282".equals(specInfo))
		{
			// E8CP12 PON 2+1
			return "3";
		}
		else if ("1283".equals(specInfo))
		{
			// e8cp42 PON 4+2
			return "1";
		}
		else if ("1284".equals(specInfo))
		{
			// EA0804 EPON 4+8
			return "6";
		}
		else if ("1290".equals(specInfo))
		{
			// 悦me
			return "15";
		}
		else if ("1344".equals(specInfo))
		{
			// EA0404 EPON 4+4
			return "5";
		}
		else if ("1352".equals(specInfo))
		{
			// E8CP02 PON 2+0
			return "14";
		}
		else if ("1358".equals(specInfo))
		{
			return "";
		}
		else if ("8522".equals(specInfo))
		{
			// EA0204 EPON 4+2
			return "4";
		}
		else if ("8586".equals(specInfo))
		{
			return "";
		}
		return "";
	}

	/**
	 * 用户信息合法性检查
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	protected boolean newSpecInfoCheck(){
		if(StringUtil.IsEmpty(newSpecInfo)){
			result = 1002;
			resultDesc = "新终端规格为空";
			return false;
		}
		if (StringUtil.IsEmpty(transSpecInfo(newSpecInfo)))
		{
			result = 1002;
			resultDesc = "新终端规格对应itms系统不存在";
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
		root.addElement("RstCode").addText("" + result);
		//修改密码是否成功
		root.addElement("IsSuccess").addText("" + isSucc);
		// 结果描述
		root.addElement("NoReason").addText("" + resultDesc);
		
		return document.asXML();
	}
	
	public String getOldSpecInfo()
	{
		return oldSpecInfo;
	}
	
	public String getNewSpecInfo()
	{
		return newSpecInfo;
	}
	
	public void setOldSpecInfo(String oldSpecInfo)
	{
		this.oldSpecInfo = oldSpecInfo;
	}
	
	public void setNewSpecInfo(String newSpecInfo)
	{
		this.newSpecInfo = newSpecInfo;
	}
	
	public int getIsSucc()
	{
		return isSucc;
	}
	
	public void setIsSucc(int isSucc)
	{
		this.isSucc = isSucc;
	}
	
}
