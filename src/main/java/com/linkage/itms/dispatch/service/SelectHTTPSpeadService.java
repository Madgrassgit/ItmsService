
package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dispatch.obj.SelectHTTPSpeadChecker;

/**
 * 新疆宽带测速结果查询
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2018-5-29
 */
public class SelectHTTPSpeadService implements IService
{

	private static Logger logger = LoggerFactory
			.getLogger(SelectHTTPSpeadService.class);

	@Override
	public String work(String inXml)
	{
		logger.warn("SelectHTTPSpeadService==>inXml({})", inXml);
		SelectHTTPSpeadChecker checker = new SelectHTTPSpeadChecker(inXml);
		if (false == checker.check())
		{
			logger.warn(
					"servicename[SelectHTTPSpeadService]cmdId[{}]userInfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		
		DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
		// 1：根据用户宽带帐号
		List<HashMap<String, String>> deviceInfoList = null;
		if(1==checker.getUserInfoType()){
			// 1：根据用户宽带帐号
			checker.setUsername(checker.getUserInfo());
			deviceInfoList = deviceInfoDAO.queryUserByNetAccount(checker.getUserInfo());
		}else if(2==checker.getUserInfoType()){
			// 1：根据逻辑SN号
			deviceInfoList = deviceInfoDAO.queryUserByLoid(checker.getUserInfo());
		}else if(3==checker.getUserInfoType()){
			// 1：根据设备序列号后6位
			deviceInfoList = deviceInfoDAO.queryDeviceByDevSN(checker.getUserInfo());
		}
		
		if(null==deviceInfoList || deviceInfoList.size()==0){
			checker.setResult(1000);
			checker.setResultDesc("没有查到设备");
			logger.warn("servicename[SelectHTTPSpeadService]cmdId[{}]userInfo[{}]没有查到设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		
		if(deviceInfoList.size()>1){
			checker.setResult(1000);
			checker.setResultDesc("查到多组设备，请输入更多位设备序列号进行查询");
			logger.warn("servicename[SelectHTTPSpeadService]cmdId[{}]userInfo[{}]查到多组设备，请输入更多位设备序列号进行查询",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		
		HashMap<String, String> deviceInfoMap = deviceInfoList.get(0);
		
		// 设备不存在
		if (null == deviceInfoMap || deviceInfoMap.isEmpty() || "".equals(deviceInfoMap.get("device_id")))
		{
			checker.setResult(1004);
			checker.setResultDesc("查无此设备");
			logger.warn("servicename[SelectHTTPSpeadService]cmdId[{}]userInfo[{}]查无此设备",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			return checker.getReturnXml();
		}
		// 设备存在
		String deviceId = StringUtil.getStringValue(deviceInfoMap, "device_id");
		// 1.查询此用户开通的业务信息
		List<HashMap<String, String>> downLoadResultList = deviceInfoDAO.queryDownLoadResult(deviceId);
		if (null == downLoadResultList || downLoadResultList.size()==0 || downLoadResultList.isEmpty()){
			checker.setResult(1006);
			checker.setResultDesc("设备未进行测速");
			logger.warn("servicename[SelectHTTPSpeadService]cmdId[{}]userInfo[{}]设备未进行测速",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
		}else{
			Map<String, String> downLoadResultMap = downLoadResultList.get(0);
			checker.setDevSn(downLoadResultMap.get("device_serialnumber"));
			checker.setUsername(downLoadResultMap.get("username"));
			checker.setSpeed(downLoadResultMap.get("speed"));
			checker.setAvgSampledTotalValues(downLoadResultMap.get("AvgSampledTotalValues"));
			checker.setMaxSampledTotalValues(downLoadResultMap.get("MaxSampledTotalValues"));
			checker.setTransportStartTime(downLoadResultMap.get("TransportStartTime"));
			checker.setTransportEndTime(downLoadResultMap.get("transportEndTime"));
			checker.setIp(downLoadResultMap.get("IP"));
			checker.setReceiveByte(downLoadResultMap.get("receiveByte"));
			checker.setTcpRequestTime(downLoadResultMap.get("tcpRequestTime"));
			checker.setTcpResponseTime(downLoadResultMap.get("tcpResponseTime"));
		}
		return checker.getReturnXml();
	}
}
