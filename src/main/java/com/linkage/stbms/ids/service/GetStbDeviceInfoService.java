
package com.linkage.stbms.ids.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.stbms.ids.dao.GetStbDeviceInfoDAO;
import com.linkage.stbms.ids.util.GetStbDeviceInfoCheck;

/**
 * @author Reno (Ailk No.)
 * @version 1.0
 * @since 2015年12月15日
 * @category com.linkage.stbms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class GetStbDeviceInfoService
{

	/** 日志 */
	private static Logger logger = LoggerFactory.getLogger(GetStbDeviceInfoService.class);

	public String work(String inParam) {
		logger.warn("GetStbDeviceInfoService==>inParam:" + inParam);
		GetStbDeviceInfoCheck checker = new GetStbDeviceInfoCheck(inParam);
		// 入参验证
		if (false == checker.check()) {
			logger.warn("机顶盒与RMS平台交互密码实时查询接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			logger.warn("GetStbDeviceInfoService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		
		// 查询设备信息
		GetStbDeviceInfoDAO dao = new GetStbDeviceInfoDAO();
		Map<String, String> devMap = new HashMap<String, String>();
		// 查询信息为机顶盒序列号
		if ("3".equals(checker.getSearchType())) {
			devMap = dao.getStbDeviceInfoBySN(checker.getSearchInfo());
		}
		// 查询信息为业务账号或者机顶盒mac
		else {
			devMap = dao.getStbDeviceInfo(checker.getSearchType(), checker.getSearchInfo());
		}
		if (null == devMap || devMap.isEmpty()) {
			logger.warn("查无此设备，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			checker.setRstCode("0");
			checker.setRstMsg("查无此设备");
			logger.warn("GetStbDeviceInfoService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		else {
			Map<String, String> resultMap = new HashMap<String, String>();
			if (getStringValue(devMap, "device_id") == null 
					|| "".equals(getStringValue(devMap, "device_id"))) {
				resultMap.put("result", "未绑定设备");
			}
			else {
				resultMap.put("result", "成功");
			}
			resultMap.put("result_flag", "1");
			resultMap.put("cpe_pwd", getStringValue(devMap, "cpe_passwd"));
			resultMap.put("rms_pwd", getStringValue(devMap, "acs_passwd"));
			resultMap.put("stb_ip", getStringValue(devMap, "loopback_ip"));
			// SDLT-REQ-2017-04-10-YUZHIJIAN-002（山东联通RMS平台机顶盒用户业务查询接口）
			resultMap.put("cityName", getStringValue(devMap, "city_name"));
			resultMap.put("stbMac", getStringValue(devMap, "cpe_mac"));
			resultMap.put("stbaccessStyle", getStringValue(devMap, "addressing_type"));
			resultMap.put("openStatus", getStringValue(devMap, "status"));
			resultMap.put("stbSN", getStringValue(devMap, "device_serialnumber"));
			resultMap.put("stbuser", getStringValue(devMap, "pppoe_user"));
			resultMap.put("servaccount", getStringValue(devMap, "serv_account"));
			
			String returnXML = checker.commonReturnParam(resultMap);
			logger.warn("GetStbDeviceInfoService==>return：" + returnXML);
			return returnXML;
		}
	}

	@SuppressWarnings("rawtypes")
	private String getStringValue(Map map, String columName) {
		if (null == columName) {
			return null;
		}
		if (null == map) {
			return null;
		}
		if (null == map.get(columName)) {
			return "";
		}
		return map.get(columName).toString();
	}
}
