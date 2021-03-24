package com.linkage.itms.dispatch.gsdx.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.gsdx.beanObj.GetParameterResult;
import com.linkage.itms.dispatch.gsdx.beanObj.Para;
import com.linkage.itms.dispatch.gsdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.gsdx.obj.GetCpeParameterByNodeNameXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

public class GetCpeParameterByNodeNameService  extends ServiceFather{

	public GetCpeParameterByNodeNameService(String methodName) {
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(NorthQueryCPEParaService.class);
	private ACSCorba corba = new ACSCorba();
	private GetParameterResult result = new GetParameterResult();
	private GetCpeParameterByNodeNameXML dealXML;
	
	public GetParameterResult work(String inXml,String[] parameterNames) {
		logger.warn(methodName+"执行，入参为：{},parameterNames:{}",inXml,parameterNames);
		dealXML = new GetCpeParameterByNodeNameXML(methodName);
		if(null == dealXML.getXML(inXml,parameterNames)){
			result.setErrorCode(StringUtil.getIntegerValue(dealXML.getResult(),-3));
			result.setErrorInfo(dealXML.getErrMsg());
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
		//判断终端是否在线
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag){
			logger.warn("{}|[{}],设备不在线或正在被操作，无法获取节点值，device_id={}", methodName,dealXML.getOpId(),deviceId);
			result.setErrorCode(-1); 
			result.setErrorInfo("终端不在线");
			return result;
		}
		ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, parameterNames);
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
	}

	public void setDealXML(GetCpeParameterByNodeNameXML dealXML) {
		this.dealXML = dealXML;
	}
}
