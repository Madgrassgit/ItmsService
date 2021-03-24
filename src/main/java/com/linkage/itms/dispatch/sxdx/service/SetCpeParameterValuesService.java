package com.linkage.itms.dispatch.sxdx.service;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.sxdx.beanObj.SetParameterResult;
import com.linkage.itms.dispatch.sxdx.dao.PublicDAO;
import com.linkage.itms.dispatch.sxdx.obj.SetCpeParameterValuesDealXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 甘肃电信对终端的TR069节点下发配置值
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月19日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class SetCpeParameterValuesService extends ServiceFather {
	public SetCpeParameterValuesService(String methodName)
	{
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(SetCpeParameterValuesService.class);
	private ACSCorba corba = new ACSCorba();
	private SetCpeParameterValuesDealXML dealXML;
	private SetParameterResult result = new SetParameterResult();
	
	public SetParameterResult work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		
		dealXML = new SetCpeParameterValuesDealXML(methodName);
		
		SetParameterResult chekRe = dealXML.checkXML(inXml);
		// 验证入参
		if ("1".equals(chekRe.getErrorCode())) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			return chekRe;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		
		PublicDAO dao = new PublicDAO();

		ArrayList<HashMap<String, String>> userDevList = dao.queryDeviceInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}", userDevList.toString());
		
		if(userDevList.size() > 1){
			logger.warn(methodName+"["+dealXML.getOpId()+"],查询到多条结果返回数量");
			result.setErrorCode(-1000);
			result.setErrorInfo("查询到多个终端，数量"+userDevList.size()+"");
			return result;
		}
		else if(null == userDevList || userDevList.size()==0 || StringUtil.isEmpty(StringUtil.getStringValue(userDevList.get(0), "device_id"))){
			logger.warn(methodName+"["+dealXML.getOpId()+"],未查询到终端");
			result.setErrorCode(0);
			result.setErrorInfo("未查询到终端");
			return result;
		}
		
		String deviceId = StringUtil.getStringValue(userDevList.get(0), "device_id");
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag){
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备不在线或正在被操作，返回-1");
			result.setErrorCode(-1);
			result.setErrorInfo("设备不在线或正在被操作");
			return result;
		}
		
		logger.warn(methodName+"["+dealXML.getOpId()+"],设备在线，准备配置参数。");
		int retResult = setParameters(deviceId, dealXML.getParamList());
		if (retResult == 0 || retResult == 1)
		{
			result.setStatus(retResult);
			result.setErrorCode(1);
			result.setErrorInfo("参数设置成功");
		}
		else{
			result.setErrorCode(retResult);
			result.setErrorInfo("参数设置失败");
		}
		return result;
	}

	private int setParameters(String deviceId, List<HashMap<String, String>> paramList)
	{
		ArrayList<ParameValueOBJ> objList = this.getObjList(paramList);
		ACSCorba corba = new ACSCorba();
		int retResult = corba.setValue(deviceId, objList);
		
		return retResult;
	}

	private ArrayList<ParameValueOBJ> getObjList(List<HashMap<String, String>> paramList)
	{
		// 修改密码到设备
		ArrayList<ParameValueOBJ> objList = new ArrayList<ParameValueOBJ>();
		ParameValueOBJ obj = new ParameValueOBJ();
		for(HashMap<String, String> map : paramList){
			obj.setName(StringUtil.getStringValue(map,"name"));
			obj.setValue(StringUtil.getStringValue(map,"value"));
			obj.setType("3");
			objList.add(obj);
		}
		return objList;
	}
	
}
