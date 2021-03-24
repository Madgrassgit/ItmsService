package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceInfoDAO;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年9月18日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class SetGMPasswdService implements IService
{
	
	private static Logger logger = LoggerFactory.getLogger(SetGMPasswdService.class); 

	@Override
	public String work(String inXml)
	{
		logger.warn("SetGMPasswdService => param:{}",inXml);
		SetGMPasswdServiceChecker checker = new SetGMPasswdServiceChecker(inXml);
		if(!checker.check()){
			logger.warn(
					"servicename[SetGMPasswdService]cmdId[{}]userInfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		
		DeviceInfoDAO dao = new DeviceInfoDAO();
		List<HashMap<String,String>> devMaps = dao.qryNetDevInfo(checker.getUserInfoType(), checker.getUserInfo());
		if(null == devMaps || devMaps.isEmpty() || devMaps.size() < 1){
			logger.warn("SetGMPasswdService =>userinfo[{}] 此用户未绑定 ",checker.getUserInfo());
			checker.setResult(1004);
			checker.setResultDesc("此用户未绑定或者不存在宽带业务");
			return checker.getReturnXml();
		}
		//查看查询信息是否多条
		if(devMaps.size() > 1){
			logger.warn("SetGMPasswdService =>userinfo[{}] 查询出多个设备 ",checker.getUserInfo());
			checker.setResult(1014);
			checker.setResultDesc("查询出多个设备");
			return checker.getReturnXml();
		}
		//查看是否路由模式
		String wan_type = devMaps.get(0).get("wan_type");
		if(!"2".equals(wan_type)){
			logger.warn("SetGMPasswdService =>userinfo[{}]wan_type[{}]非路由模式不修改密码 ",checker.getUserInfo());
			checker.setResult(1002);
			checker.setResultDesc("非路由模式不修改密码");
			return checker.getReturnXml();
		}
		//查看密码是否和原有一致
		String pass = devMaps.get(0).get("passwd");
		if(!StringUtil.IsEmpty(pass) && pass.equals(checker.getPasswd())){
			logger.warn("SetGMPasswdService =>userinfo[{}] 需修改的密码和原有的一致",new Object[]{checker.getUserInfo()});
			checker.setResult(1002);
			checker.setResultDesc("需修改的密码和原有的一致");
			return checker.getReturnXml();
		}
		//确定设备的非空性
		String devId = devMaps.get(0).get("device_id");
		if(StringUtil.IsEmpty(devId)){
			logger.warn("SetGMPasswdService =>userinfo[{}],devId[{}] 获取的设备为空 ",new Object[]{checker.getUserInfo(),devId});
			checker.setResult(1000);
			checker.setResultDesc("系统内部错误");
			return checker.getReturnXml();
		}
		//查询设备是否正在做业务下发
		if(dao.isDevDoing(devId)){
			logger.warn("SetGMPasswdService =>userinfo[{}],devId[{}]设备正在被操作，不能正常交互 ",new Object[]{checker.getUserInfo(),devId});
			checker.setResult(1013);
			checker.setResultDesc("设备正在被操作，不能正常交互");
			return checker.getReturnXml();
		}
		//检测设备是否在线
		ACSCorba acsCorba = new ACSCorba();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(devId, acsCorba);
		if(flag != 1){
			logger.warn("SetGMPasswdService =>userinfo[{}],devId[{}]设备不在线 ",new Object[]{checker.getUserInfo(),devId});
			checker.setResult(1012);
			checker.setResultDesc("设备不在线");
			return checker.getReturnXml();
		}
		//组装下发参数
		PreServInfoOBJ preObj = new PreServInfoOBJ();
		preObj.setUserId(devMaps.get(0).get("user_id"));
		preObj.setDeviceId(devId);
		preObj.setDeviceSn(devMaps.get(0).get("device_serialnumber"));
		preObj.setOui(devMaps.get(0).get("oui"));
		preObj.setGatherId(devMaps.get(0).get("gather_id"));
		preObj.setServTypeId("10");//默认宽带业务
		preObj.setOperTypeId("1");//默认开通
		
		//下发宽带业务
		dao.updateNetPass(devMaps.get(0).get("user_id"), devMaps.get(0).get("username"), checker.getPasswd());
		if (1 == CreateObjectFactory.createPreProcess().processServiceInterface(CreateObjectFactory.createPreProcess().GetPPBindUserList(preObj)))
		{
			//更新密码
			logger.warn("SetGMPasswdService =>userinfo[{}],devId[{}]调用预读成功 ",new Object[]{checker.getUserInfo(),devId}); 
			checker.setResult(0);
			checker.setResultDesc("调用预读成功");
			return checker.getReturnXml();
		}else{
			//回退到原有密码
			dao.updateNetPass(devMaps.get(0).get("user_id"), devMaps.get(0).get("username"), pass);
			logger.warn("SetGMPasswdService =>userinfo[{}],devId[{}]调用预读失败",new Object[]{checker.getUserInfo(),devId}); 
			checker.setResult(0);
			checker.setResultDesc("调用预读失败");
			return checker.getReturnXml();
		}
	}
	
}
