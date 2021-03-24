
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
import com.linkage.itms.dispatch.obj.SetVoiceProfileChecker;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2016年4月22日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class SetVoiceProfileService implements IService
{

	/** 日志对象 */
	private static final Logger logger = LoggerFactory
			.getLogger(SetVoiceProfileService.class);
	private UserDeviceDAO userDevDao = new UserDeviceDAO();

	@Override
	public String work(String inXml)
	{
		logger.warn("SetVoiceProfileService inXml ({})", inXml);
		SetVoiceProfileChecker checker = new SetVoiceProfileChecker(inXml);
		if (false == checker.check())
		{
			logger.error(
					"servicename[SetVoiceProfileService]cmdId[{}]UserInfo[{}]验证未通过.返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[SetVoiceProfileService]cmdId[{}]UserInfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		// 根据参数查询数据库是否有此设备的信息,根据参数判断哪种查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
				checker.getUserInfoType(), checker.getUserInfo(), checker.getCityId());
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn("serviceName[SetVoiceProfileService]cmdId[{}]userinfo[{}]无此用户",
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
						"serviceName[SetVoiceProfileService]cmdId[{}]userinfo[{}]未绑定设备",
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
							"serviceName[SetVoiceProfileService]cmdId[{}]userinfo[{}]设备繁忙或者业务正在下发，请稍候重试",
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
								"serviceName[SetVoiceProfileService]cmdId[{}]userinfo[{}]，设备在线，可以进行参数配置，开始配置[{}]",
								new Object[] { checker.getCmdId(), checker.getUserInfo(),
										deviceId });
						ArrayList<ParameValueOBJ> objList = getObjList(checker);
						// 调用Corba 设置节点的值
						logger.warn("调用Corba，设置节点值,[{}]",deviceId);
						int retResult = corba.setValue(deviceId, objList);
						
						if (0 == retResult || 1 == retResult) {
							 checker.setResult(0);
							 checker.setResultDesc("成功");
							 logger.warn("return=({})", checker.getReturnXml());  // 打印回参
							 return checker.getReturnXml();
						}else if (-1 == retResult) {
							checker.setResult(1009);
							checker.setResultDesc("设备连接失败");
							logger.warn("return=({})", checker.getReturnXml());  // 打印回参
							return checker.getReturnXml();
						}else if (-6 == retResult) {
							checker.setResult(1010);
							checker.setResultDesc("设备正被操作");
							logger.warn("return=({})", checker.getReturnXml());  // 打印回参
							return checker.getReturnXml();
						}else if (-7 == retResult) {
							checker.setResult(1011);
							checker.setResultDesc("系统参数错误");
							logger.warn("return=({})", checker.getReturnXml());  // 打印回参
							return checker.getReturnXml();
						}else if (-9 == retResult) {
							checker.setResult(1012);
							checker.setResultDesc("系统内部错误");
							logger.warn("return=({})", checker.getReturnXml());  // 打印回参
							return checker.getReturnXml();
						}else {
							checker.setResult(1013);
							checker.setResultDesc("下发配置节点时tr069错误，请检查节点信息是否正确。");
							logger.warn("return=({})", checker.getReturnXml());  // 打印回参
							return checker.getReturnXml();
						}
					}
					// 设备不在线
					else
					{
						logger.warn("设备[{}]不在线，无法设置参数", deviceId);
						checker.setResult(1005);
						checker.setResultDesc("设备不在线，无法设置参数");
						logger.warn("getParameterValues：return=({})",
								checker.getReturnXml()); // 打印回参
					}
				}
			}
		}
		return null;
	}

	private ArrayList<ParameValueOBJ> getObjList(SetVoiceProfileChecker checker)
	{
		String path = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1";
		String echoCancellationPath = path + ".Line." + checker.getLine()
				+ ".VoiceProcessing.EchoCancellationEnable";
		String transmitGainPath = path + ".Line." + checker.getLine()
				+ ".VoiceProcessing.TransmitGain";
		String receiveGainPath = path + ".Line." + checker.getLine()
				+ ".VoiceProcessing.ReceiveGain";
		String controlTypePath = path + ".X_CT-COM_G711FAX.ControlType";
		String faxT38Path = path + ".FaxT38.Enable";
		
		ArrayList<ParameValueOBJ> objList = new ArrayList<ParameValueOBJ>();
		
		if(!StringUtil.IsEmpty(checker.getEchoCancellationEnable()))
		{
			ParameValueOBJ echoCancellationObj = new ParameValueOBJ();
			echoCancellationObj.setName(echoCancellationPath);
			echoCancellationObj.setValue(checker.getEchoCancellationEnable());
			echoCancellationObj.setType("1");
			objList.add(echoCancellationObj);
		}
		if(!StringUtil.IsEmpty(checker.getTransmitGain()))
		{
			ParameValueOBJ transmitGainObj = new ParameValueOBJ();
			transmitGainObj.setName(transmitGainPath);
			transmitGainObj.setValue(checker.getTransmitGain());
			transmitGainObj.setType("1");
			objList.add(transmitGainObj);
		}
		if(!StringUtil.IsEmpty(checker.getReceiveGain()))
		{
			ParameValueOBJ receiveGainObj = new ParameValueOBJ();
			receiveGainObj.setName(receiveGainPath);
			receiveGainObj.setValue(checker.getReceiveGain());
			receiveGainObj.setType("1");
			objList.add(receiveGainObj);
		}
		if(!StringUtil.IsEmpty(checker.getControlType()))
		{
			ParameValueOBJ controlTypeObj = new ParameValueOBJ();
			controlTypeObj.setName(controlTypePath);
			controlTypeObj.setValue(checker.getControlType());
			controlTypeObj.setType("1");
			objList.add(controlTypeObj);
		}
		if(!StringUtil.IsEmpty(checker.getFaxT38()))
		{
			ParameValueOBJ faxT38Obj = new ParameValueOBJ();
			faxT38Obj.setName(faxT38Path);
			faxT38Obj.setValue(checker.getFaxT38());
			faxT38Obj.setType("1");
			objList.add(faxT38Obj);
		}
	
		return objList;
	}
}
