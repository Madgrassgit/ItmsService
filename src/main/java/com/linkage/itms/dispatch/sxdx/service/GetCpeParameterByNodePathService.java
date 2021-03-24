package com.linkage.itms.dispatch.sxdx.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.sxdx.beanObj.GetParameterResult;
import com.linkage.itms.dispatch.sxdx.beanObj.Para;
import com.linkage.itms.dispatch.sxdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.sxdx.obj.CpeParameterByNodePathXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

/**
	通过此接口，传入某个节点名称的路径获取到该节点下的所有叶子节点的参数值（包含下面所有层级） *
 */
public class GetCpeParameterByNodePathService  extends ServiceFather {

	private static Logger logger = LoggerFactory.getLogger(NorthQueryCPEParaService.class);
	private ACSCorba corba = new ACSCorba();
	private GetParameterResult result = new GetParameterResult();
	private CpeParameterByNodePathXML dealXML;
	
	public GetCpeParameterByNodePathService(String methodName) {
		super(methodName);
	}
	
	public GetParameterResult work(String inXml){
		logger.warn(methodName+"执行，入参为：{}",inXml);
		
		dealXML = new CpeParameterByNodePathXML(methodName);
		// 验证入参
		if (null == dealXML.getXML(inXml)) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			return result;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		CpeInfoDao dao = new CpeInfoDao();
		 Map<String, String> queryUserInfo = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}",queryUserInfo);
		if(null == queryUserInfo || queryUserInfo.size()==0){
			result.setErrorCode(0);
			result.setErrorInfo("没有对应的终端");
			return result;
		}
		String deviceId = StringUtil.getStringValue(queryUserInfo, "device_id");
		if(StringUtil.IsEmpty(deviceId)){
			result.setErrorCode(0);
			result.setErrorInfo("没有对应的终端");
			return result;
		}
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag){
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备不在线或正在被操作，无法获取节点值，device_id={}", deviceId);
			result.setErrorCode(-1); 
			result.setErrorInfo("终端不在线");
			return result;
		}
		String lanPath = dealXML.getNodePath();
		logger.warn(methodName+"["+dealXML.getOpId()+"],device_id={}设备在线开始采集，设备节点:{}.", deviceId,lanPath);
		
		// 获取全部路径
		ArrayList<String> landevicePathsList = new ArrayList<String>();
		landevicePathsList = corba.getParamNamesPath(deviceId, lanPath, 0);
		logger.warn(methodName+"["+dealXML.getOpId()+"],LANDevice.size:{}", landevicePathsList.size());
		if (landevicePathsList == null || landevicePathsList.size() == 0 || landevicePathsList.isEmpty()){
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备异常，无法获取节点值，device_id={}", deviceId);
			result.setErrorCode(-4); 
			result.setErrorInfo("终端异常");
			return result;
		}
		else
		{
			ArrayList<String> paramNameList = new ArrayList<String>();
			for (int i = 0; i < landevicePathsList.size(); i++)
			{
				paramNameList.add(landevicePathsList.get(i));
			}
			
			if(paramNameList.size() > 0){
				String[] gatherPathArray = new String[paramNameList.size()];
				paramNameList.toArray(gatherPathArray);
				// 处理设备采集结果
				ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPathArray);
				if (null == objLlist || objLlist.isEmpty()){
					logger.warn("{}|[{}],采集失败，device_id={}", methodName,dealXML.getOpId(),deviceId);
					result.setErrorCode(-4); 
					result.setErrorInfo("终端异常");
					return result;
				}
				
				ArrayList<Para> paralist = new ArrayList<Para>();
				for (ParameValueOBJ parameValueOBJ : objLlist) {
					//拼接返回值
					paralist.add(setPara(parameValueOBJ.getName(),parameValueOBJ.getValue()));
				}
				result.setErrorCode(1); 
				Para[] array = (Para[])paralist.toArray(new Para[paralist.size()]);
				result.setParas((array));
				result.setErrorInfo("查询成功");
				return result;
			}else{
				logger.warn(methodName+"["+dealXML.getOpId()+"],无下挂节点，device_id={}", deviceId);
				result.setErrorCode(-4); 
				result.setErrorInfo("终端异常");
				return result;
			}
		}
	}
	
}
