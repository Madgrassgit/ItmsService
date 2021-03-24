
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commom.util.CheckStrategyUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryVoiceProfileChecker;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2016年4月22日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QueryVoiceProfileService implements IService
{

	/** 日志对象 */
	private static final Logger logger = LoggerFactory
			.getLogger(QueryVoiceProfileService.class);
	private UserDeviceDAO userDevDao = new UserDeviceDAO();

	@Override
	public String work(String inXml)
	{
		logger.warn("QueryVoiceProfileService inXml ({})", inXml);
		QueryVoiceProfileChecker checker = new QueryVoiceProfileChecker(inXml);
		if (false == checker.check())
		{
			logger.error(
					"servicename[QueryVoiceProfileService]cmdId[{}]UserInfo[{}]验证未通过.返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[QueryVoiceProfileService]cmdId[{}]UserInfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		// 根据参数查询数据库是否有此设备的信息,根据参数判断哪种查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo(), checker.getCityId());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn("serviceName[QueryVoiceProfileService]cmdId[{}]userinfo[{}]无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("查无此客户");
			return checker.getReturnXml();
		}
		else
		{
			String deviceId = userInfoMap.get("device_id");
			if (StringUtil.IsEmpty(deviceId))
			{
				logger.warn(
						"serviceName[QueryVoiceProfileService]cmdId[{}]userinfo[{}]未绑定设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1003);
				checker.setResultDesc("未绑定设备");
				return checker.getReturnXml();
			}
			else
			{
				// (江西)判断设备是否繁忙或者业务正在下发
				if ("jx_dx".equals(Global.G_instArea)
						&& false == CheckStrategyUtil.chechStrategy(deviceId))
				{
					logger.warn(
							"serviceName[QueryVoiceProfileService]cmdId[{}]userinfo[{}]设备繁忙或者业务正在下发，请稍候重试",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1003);
					checker.setResultDesc("设备繁忙或者业务正在下发，请稍候重试");
					return checker.getReturnXml();
				}
				else
				{
					// 判断设备是否正在被操作是否在线
					GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
					ACSCorba corba = new ACSCorba();
					int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
					// 设备正在被操作，不能获取节点值
					if (-3 == flag)
					{
						logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
						checker.setResult(1000);
						checker.setResultDesc("设备正在被操作，不能正常交互");
						logger.warn("return=({})", checker.getReturnXml()); // 打印回参
						return checker.getReturnXml();
					}
					// 设备在线
					else if (1 == flag)
					{
						logger.warn(
								"serviceName[QueryVoiceProfileService]cmdId[{}]userinfo[{}]，设备在线，可以进行采集操作，开始采集[{}]",
								new Object[] { checker.getCmdId(), checker.getUserInfo(),
										deviceId });
						String path = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1";
						String echoCancellationPath = path + ".Line." + checker.getLine()
								+ ".VoiceProcessing.EchoCancellationEnable";
						String transmitGainPath = path + ".Line." + checker.getLine()
								+ ".VoiceProcessing.TransmitGain";
						String receiveGainPath = path + ".Line." + checker.getLine() + ".VoiceProcessing.ReceiveGain";
						String controlTypePath = path + ".X_CT-COM_G711FAX.ControlType";
						String faxT38Path = path +".FaxT38.Enable";
						
						String [] arr1 = new String[2];
						arr1[0] = controlTypePath;
						arr1[1] = faxT38Path;
						// 调用Corba 获取节点的值
						logger.warn("调用Corba，获取设备[{}]节点值",deviceId);
						
						//要求line参数如果传参是设备没有的线路的话，也要查出传真控制方式 和 FaxT38传真模式 两个节点的值，所以只能查两次
						ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, arr1);
						getObjValue(checker,objLlist);
						
						String [] arr2 = new String[3];
						arr2[0] = echoCancellationPath;
						arr2[1] = transmitGainPath;
						arr2[2] = receiveGainPath;
						// 调用Corba 获取节点的值
						logger.warn("调用Corba，获取设备[{}]节点值",deviceId);
						
						//要求line参数如果传参是设备没有的线路的话，也要查出传真控制方式 和 FaxT38传真模式 两个节点的值，所以只能查两次
						ArrayList<ParameValueOBJ> objLlist2 = corba.getValue(deviceId, arr2);
						getObjValue(checker,objLlist2);						
					
					}
					// 设备不在线
					else {
						logger.warn("设备[{}]不在线，无法获取节点值",deviceId);
						checker.setResult(1005);
						checker.setResultDesc("设备不在线，无法获取节点值");
						logger.warn("getParameterValues：return=({})", checker.getReturnXml());  // 打印回参
					}
				}
			}
		}
		return checker.getReturnXml();
	}
	
	private void getObjValue(QueryVoiceProfileChecker checker, ArrayList<ParameValueOBJ> parameterValues)
	{
		String value = "";
		if (null != parameterValues && !parameterValues.isEmpty())
		{
			for (ParameValueOBJ objParameValueOBJ : parameterValues)
			{
				value = objParameValueOBJ.getValue();
				if(objParameValueOBJ.getName().contains("EchoCancellationEnable"))
				{
					checker.setEchoCancellationEnable(value);
				}
				else if(objParameValueOBJ.getName().contains("TransmitGain"))
				{
					checker.setTransmitGain(value);
				}
				else if(objParameValueOBJ.getName().contains("ReceiveGain"))
				{
					checker.setReceiveGain(value);
				}
				else if(objParameValueOBJ.getName().contains("ControlType"))
				{
					checker.setControlType(value);
				}
				else if(objParameValueOBJ.getName().contains("FaxT38"))
				{
					checker.setFaxT38(value);
				}
				
			}
		}
	}
}
