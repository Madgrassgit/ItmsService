package com.linkage.itms.cao;


import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;
import ACS.Rpc;

import com.linkage.commons.util.StringUtil;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.SetParameterValues;


/**
 * IAD诊断操作类
 * 
 * @author Jason(3412)
 * @date 2010-11-3
 */
public class DevIADDiagCAO extends SuperAcsRpcInvoke {

	private static final Logger logger = LoggerFactory
			.getLogger(DevIADDiagCAO.class);

	//request 参数
	private static final String REQUEST = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.X_CT-COM_IADDiagnostics.IADDiagnosticsState";
	
	//测试服务器 参数
	private static final String TESTSERVER = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.X_CT-COM_IADDiagnostics.TestServer";
	
	//测试结果 参数
	private static final String TESTRESULT = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.X_CT-COM_IADDiagnostics.RegistResult";
	
	//失败原因 参数
	private static final String TESTREASON = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.X_CT-COM_IADDiagnostics.Reason";
	
//	//设备ID
//	private String devId;
	
	//注册服务器，取值范围：1：主用服务器，2：备用服务器
	private int iServ;
	
	//注册是否成功，0：成功，1：失败
	private String result;
	
	//注册失败原因
	private String reasonCode;
	
	//注册失败原因描述
	private String reasonDesc;
	
	//注册失败原因Map
	private static HashMap<String, String> reasonMap;
	
	//诊断结果
	private int diagResult;
	
	static{
		//1：IAD模块错误，2：访问路由不通，3：访问服务器无响应，4：帐号错误，5：未知错误。
		reasonMap = new HashMap<String, String>();
		reasonMap.put("1", "IAD模块错误");
		reasonMap.put("2", "访问路由不通");
		reasonMap.put("3", "访问服务器无响应");
		reasonMap.put("4", "帐号错误");
		reasonMap.put("5", "未知错误");
	}
	
	/**
	 * 构造方法
	 * @param _devId
	 * @param _iServ
	 */
	public DevIADDiagCAO(String _devId, int _iServ){
		logger.debug("DevIADDiagCAO({},{})", _devId, _iServ);
		deviceId = _devId;
		iServ = _iServ;
	}
	
	
	/**
	 * IAD诊断方法调用
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-11-3
	 * @return boolean 成功返回true；其他返回false
	 */
	public boolean diagIAD() {
		logger.debug("diagIAD():{}", deviceId);
		//诊断返回结果
		String rpcRes = getRespStr();
		//判断返回是否错误
		diagResult = getFlag();
		if(diagSeccuss(diagResult)){
			//无错误的情况下转为Map
			Map<String, String> diagIADMap = getDevParamMap(rpcRes);
			if (null != diagIADMap) {
				result = diagIADMap.get(TESTRESULT);
				reasonCode = diagIADMap.get(TESTREASON);
				reasonDesc = reasonMap.get(reasonCode);
				if(StringUtil.IsEmpty(reasonDesc)){
					//无法确认失败原因的情况下，失败描述=设备返回的失败代码
					reasonDesc = reasonCode;
				}
				return true;
			}else{
				logger.error("IAD, ACS return getDevParamMap null :{}", deviceId);
				diagResult = -9;
			}
		}
		return false;
	}
	
	
	/* (non-Javadoc)
	 * @see com.linkage.module.gwms.cao.SuperAcsRpcInvoke#createDevRPCArray()
	 */
	@Override
	public DevRpc[] createDevRPCArray() {
		logger.debug("createDevRPCArray()");
		SetParameterValues setParameterValues = getSetParam();

		GetParameterValues getParameterValues = getResponseParam();

		DevRpc[] devRpcArr = new DevRpc[1];
		devRpcArr[0] = new DevRpc();
		devRpcArr[0].devId = deviceId;
		Rpc[] rpcArr = new Rpc[2];
		rpcArr[0] = new Rpc();
		rpcArr[0].rpcId = "1";
		rpcArr[0].rpcName = "SetParameterValues";
		rpcArr[0].rpcValue = setParameterValues.toRPC();
		
		rpcArr[1] = new Rpc();
		rpcArr[1].rpcId = "2";
		rpcArr[1].rpcName = "GetParameterValues";
		rpcArr[1].rpcValue = getParameterValues.toRPC();
		
		devRpcArr[0].rpcArr = rpcArr;
		logger.debug(rpcArr[1].rpcValue);
		return devRpcArr;
	}

	
	/* (non-Javadoc)
	 * @see com.linkage.module.gwms.cao.SuperAcsRpcInvoke#getResponseParam()
	 */
	@Override
	public GetParameterValues getResponseParam() {
		logger.debug("getResponseParam()");
		GetParameterValues getParameterValues = new GetParameterValues();
		String[] parameterNamesArr = new String[2];
		parameterNamesArr[0] = TESTRESULT;
		parameterNamesArr[1] = TESTREASON;
		getParameterValues.setParameterNames(parameterNamesArr);

		return getParameterValues;
	}

	
	/* (non-Javadoc)
	 * @see com.linkage.module.gwms.cao.SuperAcsRpcInvoke#getSetParam()
	 */
	@Override
	public SetParameterValues getSetParam() {
		logger.debug("getSetParam()");
		SetParameterValues setParameterValues = new SetParameterValues();
		ParameterValueStruct[] ParameterValueStruct = new ParameterValueStruct[2];

		ParameterValueStruct[0] = new ParameterValueStruct();
		ParameterValueStruct[0]
				.setName(REQUEST);
		AnyObject anyObject = new AnyObject();
		anyObject.para_value = "Requested";
		anyObject.para_type_id = "1";
		ParameterValueStruct[0].setValue(anyObject);

		ParameterValueStruct[1] = new ParameterValueStruct();
		ParameterValueStruct[1]
				.setName(TESTSERVER);
		anyObject = new AnyObject();
		anyObject.para_value = "" + iServ;
		anyObject.para_type_id = "3";
		ParameterValueStruct[1].setValue(anyObject);

		setParameterValues.setParameterList(ParameterValueStruct);
		setParameterValues.setParameterKey("IAD");

		return setParameterValues;
	}


	public String getResult() {
		return result;
	}


	public String getReasonCode() {
		return reasonCode;
	}


	public String getReasonDesc() {
		return reasonDesc;
	}


	public int getDiagResult() {
		return diagResult;
	}
	
}
