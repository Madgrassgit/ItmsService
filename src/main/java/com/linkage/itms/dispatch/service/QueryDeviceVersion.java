
package com.linkage.itms.dispatch.service;

import java.util.Map;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.QueryDeviceVersionDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryDeviceVersionChecker;

public class QueryDeviceVersion implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceVersion.class);

	public String work(String inXml)
	{
		QueryDeviceVersionChecker checker = new QueryDeviceVersionChecker(inXml);
		if (false == checker.check())
		{
			logger.error(
					"servicename[QueryDeviceVersion]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn("servicename[QueryDeviceVersion]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo());
		// loid
		checker.setLoid(StringUtil.getStringValue(userInfoMap, "username", ""));
		if (null == userInfoMap || userInfoMap.isEmpty()
				|| "".equals(StringUtil.getStringValue(userInfoMap, "user_id")))
		{
			logger.warn("servicename[QueryDeviceVersion]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1001);
			checker.setResultDesc("查无此用户");
			return checker.getReturnXml();
		}
		String deviceId = StringUtil.getStringValue(userInfoMap, "device_id", "");
		if ("".equals(deviceId))
		{
			logger.warn("servicename[QueryDeviceVersion]cmdId[{}]userinfo[{}]此用户未绑定设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("此用户未绑定设备");
			return checker.getReturnXml();
		}
		QueryDeviceVersionDAO dao = new QueryDeviceVersionDAO();
		Map<String, String> infoMap = dao.getDeviceVersion(deviceId);
		if (null == infoMap || infoMap.isEmpty())
		{
			logger.warn(
					"servicename[QueryDeviceVersion]cmdId[{}]userinfo[{}]根据设备ID没查询到相关设备厂商，设备型号，软件版本等信息",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1003);
			checker.setResultDesc("没查询到相关设备厂商，设备型号，软件版本等信息");
			return checker.getReturnXml();
		}
		logger.warn("servicename[QueryDeviceVersion]cmdId[{}]userinfo[{}]版本信息：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), infoMap });
		String vendorAdd = StringUtil.getStringValue(infoMap, "vendor_add", "");
		if ("".equals(vendorAdd))
		{
			vendorAdd = StringUtil.getStringValue(infoMap, "vendor_name", "");
		}
		// String IsStandard = StringUtil.getStringValue(infoMap, "is_normal");
		String is_check = StringUtil.getStringValue(infoMap, "is_check");
		String spec_desc = StringUtil.getStringValue(infoMap, "spec_desc");
		// 数据库中 1：已审核,0：已测试，未审核,-1：未测试
		if (!"1".equals(is_check))
		{
			is_check = "2"; // 接口文档规定 1 表示规范 2 表示不规范
		}
		else
		{
			is_check = "1"; // 接口文档规定 1 表示规范 2 表示不规范
		}
		// 设备厂商
		checker.setDevVendor(vendorAdd);
		// 设备型号
		checker.setDevModel(StringUtil.getStringValue(infoMap, "device_model", ""));
		// 软件版本
		checker.setDevSoftwareversion(StringUtil.getStringValue(infoMap,
				"softwareversion", ""));
		// 是否是规范版本
		checker.setIsStandard(is_check);
		// ip
		checker.setIp(StringUtil.getStringValue(infoMap, "loopback_ip", ""));
		// 设备sn
		checker.setDevSn(StringUtil.getStringValue(infoMap, "device_serialnumber", ""));
		// 规格
		if ("js_dx".equals(Global.G_instArea))
		{
			if (StringUtil.IsEmpty(spec_desc))
			{
				checker.setResult(1000);
				checker.setResultDesc("终端版本不规范");
				return checker.getReturnXml();
			}
		}
		checker.setSpecDesc(spec_desc);
		if("nx_dx".equals(Global.G_instArea))
		{
			final int COUNT = 0;
			String device_model_id = StringUtil.getStringValue(infoMap, "device_model_id", "");
			// NXDX-REQ-ITMS-20200310-LX-001 接口改造，增加返回 500M 设备改造的列表，维护在表tab_device_e8c_remould中，如果是该表里的设备接口返回500M，不再根据维护的速率返回100M
			String deviceSerialnumber = StringUtil.getStringValue(infoMap, "device_serialnumber", "");
			Map<String, String> deviceCountInfo = dao.getDeviceCountBySn(deviceSerialnumber);
			if(COUNT != StringUtil.getIntValue(deviceCountInfo,"num")){
				checker.setBps("500");
			}else {
				Map<String, String> etherMessage = dao.getEtherMessage(device_model_id);
				checker.setBps(StringUtil.getStringValue(etherMessage, "etherrate", ""));
			}
		}
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
				"QueryDeviceVersion");
		String retXml = checker.getReturnXml();
		logger.warn("servicename[QueryDeviceVersion]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), retXml });
		// 回参
		return retXml;
	}
}
