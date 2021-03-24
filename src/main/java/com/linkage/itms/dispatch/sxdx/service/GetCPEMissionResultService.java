package com.linkage.itms.dispatch.sxdx.service;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.sxdx.beanObj.CPEMissionResult;
import com.linkage.itms.dispatch.sxdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.sxdx.obj.GetCPEMissionResultXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetCPEMissionResultService extends ServiceFather {

	public GetCPEMissionResultService(String methodName) {
		super(methodName);
	}
	private static Logger logger = LoggerFactory.getLogger(NorthQueryCPEParaService.class);
	private CPEMissionResult result;
	private GetCPEMissionResultXML dealXML;
	
	public CPEMissionResult work(String inXml){
		logger.warn(methodName+"执行，入参为：{}",inXml);
		if(null == dealXML.getXML(inXml)){
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			result.setiOperRst(-1);
			return result;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		// 验证入参
		CpeInfoDao dao = new CpeInfoDao();
		//设备
		Map<String, String> queryUserInfo = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}",queryUserInfo);
		if(null == queryUserInfo || queryUserInfo.isEmpty() || StringUtil.isEmpty(StringUtil.getStringValue(queryUserInfo, "device_id"))){
			result.setiOperRst(0);
			return result;
		}
		// 取得设备信息
		String deviceId = StringUtil.getStringValue(queryUserInfo, "device_id");
		List<HashMap<String, String>> deviceInfo = dao.getDeviceInfo(deviceId);
		if(null == deviceInfo || deviceInfo.isEmpty()){
			result.setiOperRst(0);
			logger.warn(methodName+"["+dealXML.getOpId()+"]没获取设备成功.");
			return result;
		}
		Map<String,String> inforesult = dao.getSoftResultInfo(deviceId);
		
		return result;
	}
}
