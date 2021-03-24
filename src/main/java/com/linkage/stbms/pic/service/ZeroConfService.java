
package com.linkage.stbms.pic.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.stbms.ids.obj.ResultBean;
import com.linkage.stbms.itv.main.StbServGlobals;
import com.linkage.stbms.pic.Global;
import com.linkage.stbms.pic.bio.ZeroConfBio;
import com.linkage.stbms.pic.object.StbDeviceOBJ;
import com.linkage.stbms.pic.object.StbZeroconfigFailObj;

public class ZeroConfService
{

	private final Logger logger = LoggerFactory.getLogger(ZeroConfService.class);

	public String work(String inXml)
	{
		// 检查合法性
		ZeroConfChecker serviceDoner = new ZeroConfChecker(inXml);
		if (false == serviceDoner.check())
		{
			logger.error(
					"servicename[ZeroConfService]cmdId[{}]deviceid[{}]验证未通过，返回：{}",
					new Object[] { serviceDoner.getCmdId(), serviceDoner.getDeviceId(),
							serviceDoner.getReturnXml() });
			return serviceDoner.getReturnXml();
		}
		logger.warn(
				"servicename[ZeroConfService]cmdId[{}]deviceid[{}]参数校验通过，入参为：{}",
				new Object[] { serviceDoner.getCmdId(), serviceDoner.getDeviceId(), inXml });
		ZeroConfBio confBio = new ZeroConfBio();
		StbDeviceOBJ stbDeviceOBJ = confBio.getStbObj(serviceDoner.getDeviceId(), 1);
		if (stbDeviceOBJ == null)
		{
			logger.warn("servicename[ZeroConfService]cmdId[{}]deviceid[{}]查询机顶盒信息为空,退出", new Object[] { serviceDoner.getDeviceId()});
			serviceDoner.setResult(1001);
			serviceDoner.setResultDesc("查无此设备");
			return serviceDoner.getReturnXml();
		}
		
		stbDeviceOBJ.setBindWay(2);
		String returnValue = "";
		ArrayList<HashMap<String,String>> userList = confBio.getUserByCustAccount(serviceDoner.getCustAccount());
		if(null == userList || userList.isEmpty()){
			logger.warn(
					"servicename[ZeroConfService]cmdId[{}]deviceid[{}]机顶盒上报的宽带账号[{}]对应的业务账号不存在，请确认宽带账号是否正确，重新输入",
					new Object[] { serviceDoner.getCmdId(), serviceDoner.getDeviceId(), serviceDoner.getCustAccount() });
			
			returnValue = "servicename[ZeroConfService]cmdId[{"+serviceDoner.getCmdId()+"}]" +
					"deviceid[{"+serviceDoner.getDeviceId()+"}]机顶盒上报的宽带账号[{"+serviceDoner.getCustAccount()+"}]对应的业务账号不存在";	
			setBindResult(stbDeviceOBJ, 2, -1, 11, returnValue);
			confBio.zeroCfgFailed(0, stbDeviceOBJ, null);
						
			serviceDoner.setResult(1002);
			serviceDoner.setResultDesc("机顶盒上报的宽带账号对应的业务账号不存在");
			return serviceDoner.getReturnXml();
		}else{
			
			ArrayList<HashMap<String, String>> userList_new = new ArrayList<HashMap<String, String>>();
			for(HashMap<String, String> map : userList)
			{
				if (null!=map && ("".equals(map.get("device_id")) || map.get("device_id") == null)){
					userList_new.add(map);
				}
			}			
			Map<String, String> _newUserMap = null;			
			if(null!=userList_new && !userList_new.isEmpty())
			{
				userList = userList_new;
				_newUserMap = userList.get(0);
			}else
			{
				_newUserMap = userList.get(userList.size()-1);
			}
			
			String returnValueTemp = "";
//			Map<String, String> _newUserMap = userList.get(0);
			String isPrepay = "1".equals(_newUserMap.get("is_prepay")) ? "预付费用户" : "非预付费用户";
					
			if(!"".equals(_newUserMap.get("device_id")) && _newUserMap.get("device_id") != null){
				logger.warn(
						"servicename[ZeroConfService]cmdId[{}]deviceid[{}]isPrepay[{}]机顶盒上报的宽带账号[{}]对应的业务账号已绑定，请确认宽带账号是否正确，重新输入",
						new Object[] { serviceDoner.getCmdId(), serviceDoner.getDeviceId(), isPrepay, serviceDoner.getCustAccount() });
				returnValue = "IsPrepay["+ isPrepay +"]-机顶盒上报的宽带账号["+ serviceDoner.getCustAccount() +"]对应的业务账号已绑定";	
				setBindResult(stbDeviceOBJ, 2, -1, 11, returnValue);
				confBio.zeroCfgFailed(0, stbDeviceOBJ, null);
				
				serviceDoner.setResult(1003);
				serviceDoner.setResultDesc("IsPrepay["+ isPrepay +"]-机顶盒上报的宽带账号对应的业务账号已绑定");
				// chenxj6 20170515 ITV终端管理平台对零配置结果按照需求进行记录保存设计文档
				stbDeviceOBJ.setStbIsNew(getStbIsNew(stbDeviceOBJ.getCompletetime()));
				confBio.saveZeroConfgRes(stbDeviceOBJ, _newUserMap.get("serv_account"), Global.ZERO_CONFIG_RESULT_FAILED);	
				return serviceDoner.getReturnXml();
			}
			
			returnValue = "IsPrepay["+ isPrepay +"]-机顶盒上报的宽带账号[" + serviceDoner.getCustAccount() + "]查询业务用户["
					+ _newUserMap.get("serv_account") + "]成功";
			setBindResult(stbDeviceOBJ, 2, 1, 6, returnValue);			
			boolean flag = confBio.bindProcess(userList, stbDeviceOBJ, 1);
			if(!flag){
				logger.warn(
						"servicename[ZeroConfService]cmdId[{}]deviceid[{}]isPrepay[{}]和上报宽带账号对应业务账号[{}]绑定失败，请确认宽带账号是否正确，重新输入",
						new Object[] { serviceDoner.getCmdId(), serviceDoner.getDeviceId(), isPrepay, serviceDoner.getCustAccount() });
				serviceDoner.setResult(1004);
				serviceDoner.setResultDesc("绑定失败");
				// chenxj6 20170515 ITV终端管理平台对零配置结果按照需求进行记录保存设计文档
				returnValueTemp = "servicename[ZeroConfService]cmdId[{"+serviceDoner.getCmdId()+"}]deviceid[{"+serviceDoner.getDeviceId()
						+ "}]isPrepay[{"+isPrepay+"}]和上报宽带账号对应业务账号[{"+serviceDoner.getCustAccount()+"}]绑定失败，请确认宽带账号是否正确，重新输入";
				stbDeviceOBJ.setStbIsNew(getStbIsNew(stbDeviceOBJ.getCompletetime()));
				stbDeviceOBJ.getStbZeroconfigFailObj().setReturnValue(returnValueTemp);
				confBio.saveZeroConfgRes(stbDeviceOBJ, _newUserMap.get("serv_account"), Global.ZERO_CONFIG_RESULT_FAILED);
				return serviceDoner.getReturnXml();
			}
			//绑定日志入库
			confBio.getSQLByAddBindlog( _newUserMap.get("serv_account"),
					stbDeviceOBJ.getDeviceId(), 0, 99, null, 5, null, 1, 5,
					"system");
			String customer_id = StringUtil.getStringValue(_newUserMap, "customer_id");
			String cpe_mac = stbDeviceOBJ.getStbMac();
			DevRpc[] devRpcs = confBio.getDevRpc(customer_id, serviceDoner.getDeviceId());
			if (devRpcs.length > 0)
			{
				serviceDoner.setDevRpc(devRpcs[0]);
				// 华为ITV认证平台对新终端进行解绑操作
				excuteBusinessService(null, cpe_mac, new DateTimeUtil().getLongDateChar()
						+ (new Random().nextInt(900) + 100));
			}else{
				serviceDoner.setResult(1005);
				serviceDoner.setResultDesc("获取RPC命令失败");
				// chenxj6 20170515 ITV终端管理平台对零配置结果按照需求进行记录保存设计文档
				returnValueTemp = "servicename[ZeroConfService]cmdId[{"+serviceDoner.getCmdId()+"}]deviceid[{"+serviceDoner.getDeviceId()
						+ "}]isPrepay[{"+isPrepay+"}]和上报宽带账号对应业务账号[{"+serviceDoner.getCustAccount()+"}]绑定失败，获取RPC命令失败";
				stbDeviceOBJ.setStbIsNew(getStbIsNew(stbDeviceOBJ.getCompletetime()));
				stbDeviceOBJ.getStbZeroconfigFailObj().setReturnValue(returnValueTemp);
				confBio.saveZeroConfgRes(stbDeviceOBJ, _newUserMap.get("serv_account"), Global.ZERO_CONFIG_RESULT_FAILED);
				return serviceDoner.getReturnXml();
			}
			
			if("1".equals(_newUserMap.get("is_prepay"))){
				logger.warn("servicename[ZeroConfService]cmdId[{}]deviceid[{}]用户为预付费用户，不做业务下发",
						new Object[] { serviceDoner.getCmdId(), serviceDoner.getDeviceId()});
				serviceDoner.setResult(1006);
				serviceDoner.setResultDesc("用户为预付费用户，不做业务下发");
				return serviceDoner.getReturnXml();
			}
			// chenxj6 20170515 ITV终端管理平台对零配置结果按照需求进行记录保存设计文档
			stbDeviceOBJ.setStbIsNew(getStbIsNew(stbDeviceOBJ.getCompletetime()));
			confBio.saveZeroConfgRes(stbDeviceOBJ, _newUserMap.get("serv_account"), Global.ZERO_CONFIG_RESULT_SUCCESS);
		}
		String str = serviceDoner.getReturnXml();
		logger.warn(
				"servicename[ZeroConfService]cmdId[{}]deviceid[{}]处理结束，回参为：{}",
				new Object[] { serviceDoner.getCmdId(), serviceDoner.getDeviceId(), str });
		return str;
	}

	private static void setBindResult(StbDeviceOBJ stbDeviceOBJ,int bindWay,int bindState,int failResonId,String returnValue){
		stbDeviceOBJ.setBindWay(bindWay);
		stbDeviceOBJ.setBindState(bindState);
		StbZeroconfigFailObj stbZeroconfigFailObj = stbDeviceOBJ.getStbZeroconfigFailObj();
		stbZeroconfigFailObj.setFailReasonId(failResonId);
		stbZeroconfigFailObj.setReturnValue(returnValue);
		stbDeviceOBJ.setStbZeroconfigFailObj(stbZeroconfigFailObj);
	}
	
	/**
	 * 调用解绑机顶盒MAC地址接口
	 * 
	 * @param userID
	 *            ITV帐号ID
	 * @param mac
	 *            MAC地址
	 * @param sequenceID
	 *            操作流水号
	 */
	private void excuteBusinessService(String userID, String mac, String sequenceID)
	{
		// 接口方法
		String method = "unbind";
		// 接口url
		String url = StringUtil.getStringValue(StbServGlobals
				.getLipossProperty("BusinessService.url"));
		logger.warn("开始调用解绑机顶盒MAC地址接口");
		logger.warn("url=" + url);
		logger.warn("method=" + method);
		logger.warn("入参   userID= " + userID + "  mac= " + mac + "  sequenceID= "
				+ sequenceID);
		try
		{
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(url));
			// 注册对象
			QName qn = new QName(url, method);
			call.registerTypeMapping(ResultBean.class, qn, new BeanSerializerFactory(
					ResultBean.class, qn), new BeanDeserializerFactory(ResultBean.class,
					qn));
			call.setOperationName(qn);
			// 添加参数
			call.addParameter("userID", org.apache.axis.Constants.XSD_STRING,
					ParameterMode.IN);
			call.addParameter("mac", org.apache.axis.Constants.XSD_STRING,
					ParameterMode.IN);
			call.addParameter("sequenceID", org.apache.axis.Constants.XSD_STRING,
					ParameterMode.IN);
			call.setReturnType(qn, ResultBean.class);
			ResultBean unbindReturn = (ResultBean) call.invoke(new Object[] { userID,
					mac, sequenceID });
			logger.warn("调用外部接口[解绑机顶盒MAC地址接口]的结果 : returnCode="
					+ unbindReturn.getReturnCode() + " returnMessage= "
					+ unbindReturn.getReturnMessage());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 判断设备是否是新设备
	 * @author chenxj6
	 * @param completetime
	 * @return
	 */
	private int getStbIsNew(long completetime) {
		long nowtime = System.currentTimeMillis() / 1000;
		if ((completetime + (60 * 60 * 24 * 3)) > nowtime) {
			return 1;
		}
		return 0;
	}
}
