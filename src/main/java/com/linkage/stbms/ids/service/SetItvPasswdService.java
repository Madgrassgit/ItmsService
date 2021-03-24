package com.linkage.stbms.ids.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import PreProcess.UserInfo;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.stbms.cao.ACSCorba;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.obj.SetItvPasswdServiceChecker;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年9月19日
 * @category com.linkage.stbms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class SetItvPasswdService
{
	private static Logger logger = LoggerFactory.getLogger(SetItvPasswdService.class);
	
	public String work(String inXml){
		
		SetItvPasswdServiceChecker checker = new SetItvPasswdServiceChecker(inXml);
		if(false == checker.check()){
			logger.warn(
					"servicename[SetItvPasswdService]cmdId[{}]userInfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		
		UserStbInfoDAO dao = new UserStbInfoDAO();
		Map<String,String> stbMap = dao.qryStbServInfo(checker.getUserInfo());
		
		//用户与设备是否绑定
		if(null == stbMap || stbMap.isEmpty() || stbMap.size() < 1){
			logger.warn("SetItvPasswdService =>serv_account[{}] 此用户未绑定,直接更新，等待下次零配置下发新密码 ",checker.getUserInfo());
			if(StringUtil.IsEmpty(checker.getServPwd()) || StringUtil.IsEmpty(checker.getPppoePwd())){
				logger.warn("SetItvPasswdService =>serv_account[{}]密码不能为空 ",checker.getUserInfo());
				checker.setRstCode("1002");
				checker.setRstMsg("修改的密码不能为空");
				return checker.getReturnXml();
			} 
			//更新
			dao.updateStbCustPwd(checker.getUserInfo(), checker.getServPwd(), checker.getPppoePwd());
			return checker.getReturnXml();
		} 
		//查看密码是否和原有一致
		String servPwd = stbMap.get("serv_pwd");
		String pppoePwd = stbMap.get("pppoe_pwd");
		if(StringUtil.IsEmpty(checker.getServPwd())){
			checker.setServPwd(servPwd);
		}
		if(StringUtil.IsEmpty(checker.getPppoePwd())){
			checker.setPppoePwd(pppoePwd);
		}
		
		if(StringUtil.IsEmpty(checker.getServPwd()) || StringUtil.IsEmpty(checker.getPppoePwd())){
			logger.warn("SetItvPasswdService =>serv_account[{}]密码不能为空 ",checker.getUserInfo());
			checker.setRstCode("1002");
			checker.setRstMsg("修改的密码不能为空");
			return checker.getReturnXml();
		}
		if(checker.getServPwd().equals(servPwd) && checker.getPppoePwd().equals(pppoePwd)){
			logger.warn("SetItvPasswdService =>serv_account[{}] 修改的密码和原有的一致 ",checker.getUserInfo());
			checker.setRstCode("1002");
			checker.setRstMsg("修改的密码和原有的一致");
			return checker.getReturnXml();
		}
		//查询设备是否正在做业务下发
		String devId = stbMap.get("device_id");
		if(dao.isStbDoing(devId)){
			logger.warn("SetItvPasswdService =>devId[{}] 设备正在被操作，不能正常交互 ",devId);
			checker.setRstCode("1013");;
			checker.setRstMsg("设备正在被操作，不能正常交互 ");
			return checker.getReturnXml();
		}
		
		//检测设备是否在线
		int status = 0;
		try
		{
			status = new ACSCorba().getDeviceStatus(devId);
		}
		catch (Exception e1)
		{
			logger.warn("[{}]检测设备是否在线异常:{}",new Object[]{devId,e1.getMessage()});
			e1.printStackTrace();
		}
		logger.warn("[{}]设备在线状态[{}]",new Object[]{devId,status});
		if (1 != status) {
			logger.warn("SetItvPasswdService =>devId[{}] 设备不在线",devId);
			checker.setRstCode("1012");
			checker.setRstMsg("设备不在线");
			return checker.getReturnXml();
		}
		
		//更新
		dao.updateStbCustPwd(checker.getUserInfo(), checker.getServPwd(), checker.getPppoePwd());
		//业务下发
		UserInfo[] userInfo = new UserInfo[1];
		userInfo[0] = new UserInfo();
		userInfo[0].deviceId = devId;
		userInfo[0].oui = stbMap.get("oui");
		userInfo[0].deviceSn = stbMap.get("device_serialnumber");
		userInfo[0].gatherId = "1";  // 采集点
		userInfo[0].userId = stbMap.get("customer_id");;
		userInfo[0].servTypeId = "120";
		userInfo[0].operTypeId = "1";
		logger.warn("[{}]调配置模块,下发业务...", new Object[] { devId });
		int result = 1;
		try
		{
			result= CreateObjectFactory.createPreProcess(Global.GW_TYPE_STB).processServiceInterface(userInfo);
		}
		catch (Throwable e)
		{
			logger.warn("SetItvPasswdService=>调配置模块下发业务出现异常{}:{}", new Object[] { result,e.getMessage() });
			checker.setRstCode("1000");;
			checker.setRstMsg("系统内部异常");
			return checker.getReturnXml();
		}
		logger.warn("SetItvPasswdService=>调配置模块下发业务结果{}", new Object[] { result });
		if (-2 == result){
			logger.warn("调配置模块下发业务失败{}", new Object[] { devId });
			//回退到原有密码
			dao.updateStbCustPwd(checker.getUserInfo(), servPwd, pppoePwd);
			checker.setRstCode("1000");
			checker.setRstMsg("工单下发失败");
			return checker.getReturnXml();
		}else {
			logger.warn("调配置模块下发业务成功", new Object[] { devId });
			checker.setRstCode("0");
			checker.setRstMsg("密码修改成功");
			return checker.getReturnXml();
		}
	}
}
