
package com.linkage.itms.dispatch.gsdx.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.linkage.itms.dispatch.gsdx.obj.GetCpeInfoBasicXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;

import com.linkage.itms.dispatch.gsdx.beanObj.CpeInfoRst;
import com.linkage.itms.dispatch.gsdx.beanObj.Para;
import com.linkage.itms.dispatch.gsdx.dao.CpeInfoDao;

/**
 * getCpeInfo
 * 
 * @author zhaixx
 */
public class GetCpeInfoServiceAHLT extends ServiceFather
{

	public GetCpeInfoServiceAHLT(String methodName)
	{
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(GetCpeInfoServiceAHLT.class);
	private ACSCorba corba = new ACSCorba();
	private GetCpeInfoBasicXML dealXML = new GetCpeInfoBasicXML(methodName);
	private CpeInfoRst result = new CpeInfoRst();
	private GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
	private String  noDevCode = "没有匹配索引的结果";
	public CpeInfoRst work(String inXml)
	{
		logger.warn("{}执行，入参为：{}", this.methodName, inXml);
		if (null == dealXML.getXML(inXml))
		{
			result.setErrorCode(Integer.parseInt(dealXML.getResult()));
			result.setErrorInfo(dealXML.getErrMsg());
			return result;
		}

		this.result.setErrorCode(1);
		this.result.setErrorInfo("成功");

		CpeInfoDao dao = new CpeInfoDao();
		Map<String, String> queryUserInfo = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn( "{}[{}],根据条件查询结果{}", methodName ,dealXML.getOpId() ,queryUserInfo);
		if (null == queryUserInfo || queryUserInfo.size() == 0)
		{
			result.setErrorCode(0);
			result.setErrorInfo(noDevCode);
			return result;
		}
		String deviceSerialnumber = StringUtil.getStringValue(queryUserInfo, "oui") + "-"
				+ StringUtil.getStringValue(queryUserInfo, "device_serialnumber");
		String deviceId = StringUtil.getStringValue(queryUserInfo, "device_id");
		String loopbackIp = StringUtil.getStringValue(queryUserInfo, "loopback_ip");
		String userId = StringUtil.getStringValue(queryUserInfo, "user_id");
		if (StringUtil.isEmpty(deviceId))
		{
			result.setErrorCode(0);
			result.setErrorInfo(noDevCode);
			return result;
		}
		String interfaceType = dealXML.getInterfaceType();
		// 判断InterfaceType
		String[] split = interfaceType.split(",");
		ArrayList<Para> paraList = new ArrayList<Para>();
		paraList.add(setPara("deviceID", deviceSerialnumber));
		paraList.add(setPara("ipAddress", loopbackIp));
		if (null != split && split.length > 0)
		{
			for (String Interface : split)
			{
				if ("getCpeBasicInfo".equals(Interface))
				{
					Map<String, String> devMapss = dao.getDevStatus(
							StringUtil.getStringValue(queryUserInfo, "user_id"));
					if (null == devMapss || devMapss.isEmpty())
					{
						// 成功但是没有结果
						result.setErrorCode(0);
						result.setErrorInfo(noDevCode);
						return result;
					}
					else
					{
						int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
						String onlineStatus = "0";
						if (1 != flag)
						{
							result.setErrorCode(-1);
							result.setErrorInfo("终端不在线");
							onlineStatus = "1";
						}
						paraList.add(setPara("onlineStatus", onlineStatus));
						paraList.add(setPara("lastConnectTime",
								getTime(devMapss.get("last_time"))));
					}
				}

				paraList.add(setPara("serviceInfo",dao.getServList(userId)));

				/* getCpeBasicInfo 查询终端/家庭网关基本信息及注册信息。 **/
				getCpeBasicInfo(deviceId, paraList);
			}
		}

		return result;
	}

	private void getCpeBasicInfo(String deviceId,
			ArrayList<Para> paraList)
	{
		CpeInfoDao dao = new CpeInfoDao();
		Map<String, String> infoMap = dao.getDeviceVersionAHLT(deviceId);
		if (null == infoMap || infoMap.isEmpty())
		{
			// 成功但是没有结果
			result.setErrorCode(0);
			result.setErrorInfo(noDevCode);
			return;
		}
		String vendorAdd = StringUtil.getStringValue(infoMap, "vendor_add");
		if ("".equals(vendorAdd))
		{
			vendorAdd = StringUtil.getStringValue(infoMap, "vendor_name");
		}
		// 设备厂商
		paraList.add(setPara("cpeManufacturer", vendorAdd));
		// 设备型号
		paraList.add(
				setPara("cpeType", StringUtil.getStringValue(infoMap, "device_model")));
		// 软件版本
		paraList.add(setPara("cpeVersion",
				StringUtil.getStringValue(infoMap, "softwareversion")));
		// 上行方式
		paraList.add(setPara("wanType",
				StringUtil.getStringValue(infoMap, "access_style_relay_id")));
		// 集团对终端的分类
		paraList.add(setPara("cpeClass",
				StringUtil.getStringValue(infoMap, "rela_dev_type_id")));
		// 是否只是qos
		String isqos = StringUtil.getStringValue(infoMap, "is_qos");
		if (!"1".equals(isqos))
		{
			isqos = "0";
		}
		paraList.add(setPara("isSupportQos", isqos));
		Para[] array =  paraList.toArray(new Para[paraList.size()]);
		result.setBasicInfoList(array);
	}

	public String getTime(String time)
	{
		if (null == time || time.isEmpty())
		{
			return "";
		}
		long timeStamp = StringUtil.getLongValue(time) * 1000L;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return simpleDateFormat.format(new Date(timeStamp));
	}
}
