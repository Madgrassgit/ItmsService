
package com.linkage.itms.oss.bio;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.service.ServiceHandle;
import com.linkage.itms.oss.bean.SpecInfoBean;
import com.linkage.itms.oss.dao.SpecInfoDAO;

/**
 * <pre>
 * 江苏电信ITMS与OSS数据查询接口之
 * 用户及设备规格信息查询接口
 * 可根据用户LOID查询用户和设备规格信息。
 * </pre>
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-4-2
 * @category com.linkage.itms.oss.bio
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class SpecInfoBIO
{

	private static final Logger logger = LoggerFactory.getLogger(SpecInfoBIO.class);
	private static final List<String> VALID_QUERY_TYPE = Arrays.asList("1", "2", "3",
			"4", "5");
	private SpecInfoBean bean = new SpecInfoBean();
	private SpecInfoDAO dao = new SpecInfoDAO();
	private Map<String, String> userMap = null;
	private Map<String, String> deviceMap = null;
	private String rstCode = "1000";
	private String rstMsg = "未知错误";

	public String querySpecInfo(String xmlParam)
	{
		// 解析xml
		if (!analizeXml(xmlParam))
		{
			return returnXml();
		}
		// xml参数校验
		if (!checkParameter())
		{
			return returnXml();
		}
		// 查询
		doQuerySpec();
		return returnXml();
	}

	private boolean analizeXml(String xmlParam)
	{
		logger.warn("用户及设备规格信息查询接口,xml[{}]", xmlParam);
		try
		{
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new StringReader(xmlParam));
			Element root = doc.getRootElement();
			bean.setCmdId(root.elementTextTrim("CmdID"));
			bean.setCmdType(root.elementTextTrim("CmdType"));
			bean.setClientType(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			bean.setUserInfoType(param.elementTextTrim("UserInfoType"));
			bean.setUsername(param.elementTextTrim("UserName"));
			bean.setCityId(param.elementTextTrim("CityId"));
			return true;
		}
		catch (Exception e)
		{
			logger.warn("analize xml[" + xmlParam
					+ "] failed, may be OSS send xml parameter is invalid", e);
			rstCode = "1";
			rstMsg = "XML数据格式错误";
			return false;
		}
	}

	private String returnXml()
	{
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GBK");
		Element root = doc.addElement("root");
		root.addElement("CmdID").addText(StringUtil.nvl(bean.getCmdId()));
		root.addElement("RstCode").addText(rstCode);
		root.addElement("RstMsg").addText(rstMsg);
		if ("0".equals(rstCode))
		{
			Element param = root.addElement("Param");
			param.addElement("UserSpec").addText(transSpecId(userMap.get("spec_id")));
			param.addElement("DevNum").addText(deviceMap.get("device_serialnumber"));
			param.addElement("DevSpec").addText(transSpecId(deviceMap.get("spec_id")));
		}
		String result = doc.asXML();
		logger.warn("[{}]-用户及设备规格信息查询接口,返回xml[{}]", bean.getUsername(), result);
		return result;
	}

	private boolean checkParameter()
	{
		if (!"5".equals(bean.getClientType()))
		{
			rstCode = "2";
			rstMsg = "客户端类型非法";
			return false;
		}
		if (!VALID_QUERY_TYPE.contains(bean.getUserInfoType()))
		{
			rstCode = "1001";
			rstMsg = "查询类型非法";
			return false;
		}
		if (StringUtil.isEmpty(bean.getUsername()))
		{
			rstCode = "1002";
			rstMsg = "查无此客户";
			return false;
		}
		String localCityId = transCityId(bean.getCityId());
		if (StringUtil.isEmpty(localCityId))
		{
			rstCode = "1007";
			rstMsg = "属地非法";
			return false;
		}
		else
		{
			bean.setCityId(localCityId);
		}
		return true;
	}

	private void doQuerySpec()
	{
		if ("1".equals(bean.getUserInfoType()))
		{
			userMap = dao.queryUserByLoid(bean.getUsername());
		}
		else if ("2".equals(bean.getUserInfoType()))
		{
			userMap = dao.queryUserByNetAccount(bean.getUsername());
		}
		else if ("3".equals(bean.getUserInfoType()))
		{
			userMap = dao.queryUserByIptvAccount(bean.getUsername());
		}
		else if ("4".equals(bean.getUserInfoType()))
		{
			userMap = dao.queryUserByVoipPhone(bean.getUsername());
		}
		else if ("5".equals(bean.getUserInfoType()))
		{
			userMap = dao.queryUserByVoipAccount(bean.getUsername());
		}
		if (userMap == null || userMap.isEmpty())
		{
			rstCode = "1002";
			rstMsg = "查无此客户";
			return;
		}
		if (StringUtil.isEmpty(userMap.get("device_id")))
		{
			rstCode = "1003";
			rstMsg = "未绑定设备";
			return;
		}
		if (new ServiceHandle().cityMatch(bean.getCityId(), userMap.get("city_id")) == false)
		{
			logger.warn("[{}]用户的属地与实际用户属地[{}]不同", bean.getUsername(),
					userMap.get("city_id"));
			rstCode = "1002";
			rstMsg = "查无此客户";
			return;
		}
		deviceMap = dao.queryDeviceSpec(userMap.get("device_id"));
		rstCode = "0";
		rstMsg = "成功";
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
	private static String transCityId(String cityId)
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
	 * 将内部数据库设备规格类型转换为接口中设备类型
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
	 * @param specId
	 * @return
	 */
	private static String transSpecId(String specId)
	{
		if ("1".equals(specId))
		{
			// e8cp42 PON 4+2
			return "1283";
		}
		else if ("2".equals(specId))
		{
			// E8CP11 PON 1+1
			return "1281";
		}
		else if ("3".equals(specId))
		{
			// E8CP12 PON 2+1
			return "1282";
		}
		else if ("4".equals(specId))
		{
			// EA0204 EPON 4+2
			return "8522";
		}
		else if ("5".equals(specId))
		{
			// EA0404 EPON 4+4
			return "1344";
		}
		else if ("6".equals(specId))
		{
			// EA0804 EPON 4+8
			return "1284";
		}
		else if ("7".equals(specId))
		{
			// EA0808 EPON 8+8
			return "";
		}
		else if ("8".equals(specId))
		{
			// GA0204 GPON 4+2
			return "";
		}
		else if ("9".equals(specId))
		{
			// GA0404 GPON 4+4
			return "1344";
		}
		else if ("10".equals(specId))
		{
			// GA0804 GPON 4+8
			return "1284";
		}
		else if ("11".equals(specId))
		{
			// GA0808 GPON 8+8
			return "";
		}
		else if ("12".equals(specId))
		{
			// E8CP24 PON 4+2
			return "1283";
		}
		else if ("13".equals(specId))
		{
			// E8CG12 GPON 2+1
			return "1282";
		}
		else if ("14".equals(specId))
		{
			// E8CP02 PON 2+0
			return "1352";
		}
		else if ("15".equals(specId))
		{
			// 悦me
			return "1290";
		}
		return "";
	}
}
