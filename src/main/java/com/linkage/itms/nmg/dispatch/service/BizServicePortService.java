package com.linkage.itms.nmg.dispatch.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.DevReboot;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.nmg.dispatch.obj.BizServicePortChecker;
import com.linkage.itms.obj.ParameValueOBJ;

/**
* 设备重启接口
* 项目名称：ailk-itms-ItmsService   
* 类名称：BizServicePortService   
* 类描述：   
* 创建人：guxl3   
* 创建时间：2019年3月27日 下午5:21:38   
* @version
 */
public class BizServicePortService implements IService
{
	private static Logger logger = LoggerFactory.getLogger(BizServicePortService.class);
	/**
	 * 解绑执行方法
	 */
	@Override
	public String work(String inXml)
	{
		BizServicePortChecker checker = new BizServicePortChecker(inXml);
		if (false == checker.check()) {
			logger.error(
					"servicename[BizServicePortService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[BizServicePortService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						inXml });
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		// 查询用户信息
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker.getUserInfoType(), checker
						.getUserInfo());
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn(
					"servicename[BizServicePortService]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
		} 
		else
		{// 用户存在
			String deviceId = userInfoMap.get("device_id");
	
			if (StringUtil.IsEmpty(deviceId)) {
				// 用户未绑定终端
				logger.warn(
						"servicename[BizServicePortService]cmdId[{}]userinfo[{}]此客户未绑定",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1004);
				checker.setResultDesc("此客户未绑定");
			}
			else
			{
				// 校验设备是否在线
				GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
				ACSCorba acsCorba = new ACSCorba();

				int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
				// 设备正在被操作，不能获取节点值
				if (-3 == flag) {
					logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
					checker.setResult(1006);
					checker.setResultDesc("设备不能正常交互");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					return checker.getReturnXml();
				}
				// 设备在线
				else if (1 == flag) {
					logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
					
					//采集lan口
				    String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
				    List<String> iList = acsCorba.getIList(deviceId, lanPath);
				    Collections.sort(iList);
					if (null == iList || iList.isEmpty())
					{
						logger.warn("[{}]Lan口获取iList失败，返回", deviceId);
						checker.setResult(1007);
						checker.setResultDesc("设备不支持LAN口状态查询");
						return checker.getReturnXml();
					}else{
						logger.warn("[{}]Lan口获取iList成功，iList.size={}", deviceId,iList.size());
					}
					List<LinkedHashMap<String,String>> list = new ArrayList<LinkedHashMap<String,String>>();
					for(String i : iList){
						String[] gatherPath = new String[]{"InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."+i+".Status"};
						
						ArrayList<ParameValueOBJ> objLlist = acsCorba.getValue(deviceId, gatherPath);
						if (null == objLlist) {
							continue;
						}
						
						String status = "";
						for(ParameValueOBJ pvobj : objLlist){
							if(pvobj.getName().contains("Status")){
								status = pvobj.getValue();
							}
						}
						LinkedHashMap<String,String> tmp = new LinkedHashMap<String,String>();
						tmp.put("PortNUM", "LAN"+i);
						tmp.put("PortType", "1");
						tmp.put("RstState", status);
						tmp.put("ServiceAccount", "");
						list.add(tmp);
						tmp = null;
						status = null;
					}
					//采集voip口
					String voipPath = "InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line.";
					List<String> iList1 = acsCorba.getIList(deviceId, voipPath);
					Collections.sort(iList1);
					if (null == iList1 || iList1.isEmpty())
					{
						logger.warn("[{}]语音口获取iList失败，返回", deviceId);
						checker.setResult(1007);
						checker.setResultDesc("设备不支持LAN口状态查询");
						return checker.getReturnXml();
					}else{
						logger.warn("[{}]语音口获取iList成功，iList.size={}", deviceId,iList1.size());
					}
					for(String i : iList1){
						String[] gatherPath = new String[]{"InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line."+i+".Status"};
						
						ArrayList<ParameValueOBJ> objLlist = acsCorba.getValue(deviceId, gatherPath);
						if (null == objLlist) {
							continue;
						}
						
						String status = "";
						for(ParameValueOBJ pvobj : objLlist){
							if(pvobj.getName().contains("Status")){
								status = pvobj.getValue();
							}
						}
						LinkedHashMap<String,String> tmp = new LinkedHashMap<String,String>();
						tmp.put("PortNUM", "VOIP"+i);
						tmp.put("PortType", "2");
						tmp.put("RstState", status);
						tmp.put("ServiceAccount", "");
						list.add(tmp);
						tmp = null;
						status = null;
					}
					
					checker.setLanList(list);
					
					// 记录日志
					new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
							"QueryLanStateService");
								
					return checker.getReturnXml();
					
				}
			}
			
		}
		
		String returnXml = checker.getReturnXml();

		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
				"BizServicePortService");

		logger.warn(
				"servicename[BizServicePortService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});

		// 回单
		return returnXml;
	}
}
