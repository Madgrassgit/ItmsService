package com.linkage.itms.dispatch.service;


import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.HqosUserDeviceDAO;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.UpdateHqosChecker;

/**
 * @author guxl3 (Ailk No.)
 * @version 1.0
 * @since 2021年2月2日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class UpdateHqosServiceImpl implements IService
{
	private static Logger logger = LoggerFactory.getLogger(UpdateHqosServiceImpl.class);
	HqosUserDeviceDAO userDevDao = new HqosUserDeviceDAO();
	
	@Override
	public String work(String inXml)
	{
		String methodName="UpdateHQoS";
		String returnXml="";
		logger.warn("UpdateHQoS inXml:({})",getStr(inXml));
		UpdateHqosChecker checker = new UpdateHqosChecker(inXml);
		if (!checker.check()) 
		{
			DBOperation.executeUpdate(userDevDao.prossBssSheet(checker,1));
			returnXml= checker.getReturnXml();
			logger.warn("servicename[UpdateHQoS] cmdId[{}],userinfo[{}] 验证未通过，返回：{}",
					checker.getCmdId(),checker.getLoid(),getStr(inXml));
			new RecordLogDAO().recordDispatchLog(checker,1,checker.getLoid(),methodName);
			return returnXml;
		}
		Map<String, String> map = userDevDao.isLoidExists(checker.getLoid());
		if (map==null ||map.isEmpty())
		{
			checker.setResult(1);
			checker.setResultDesc("无此用户,请先开通资料工单");
			DBOperation.executeUpdate(userDevDao.prossBssSheet(checker,1));
			returnXml= checker.getReturnXml();
			logger.warn("servicename[UpdateHQoS] cmdId[{}],loid[{}] 无此用户请先开通资料工单，返回：{}",
					checker.getCmdId(),checker.getLoid(),getStr(inXml));
			new RecordLogDAO().recordDispatchLog(checker,1,checker.getLoid(),methodName);
			return returnXml;
		}
		DBOperation.executeUpdate(userDevDao.prossBssSheet(checker,0));
		int code=1;
		
		ArrayList<String> servSqlList = new ArrayList<String>();
		long userId=StringUtil.getLongValue(map, "user_id");
		servSqlList.add(userDevDao.updateServUserSqlNew(checker, userId));
		servSqlList.add(userDevDao.updateHqsServParam(checker, userId));
		
		// 用户已经有绑定的终端
		boolean userHasDev = false;
		// 判断用户是否已经绑定了设备
		Map<String, String> devMap = userDevDao.checkDevice(userId);
		if (null != devMap)
		{
			logger.debug("用户已经绑定了终端");
			userHasDev = true;
		}
		else
		{
			userHasDev = false;
		}
		if (!servSqlList.isEmpty() && DBOperation.executeUpdate(servSqlList) > 0)
		{
			if (userHasDev)
			{
				String deviceSn=StringUtil.getStringValue(devMap,"device_serialnumber");
				String deviceId=StringUtil.getStringValue(devMap,"device_id");
				String oui=StringUtil.getStringValue(devMap,"oui");
				PreServInfoOBJ preInfoObj = new PreServInfoOBJ(StringUtil.getStringValue(userId), deviceId, oui,
						deviceSn, checker.getServTypeId(),checker.getOperateId());
				CreateObjectFactory.createPreProcess().processServiceInterface(CreateObjectFactory.createPreProcess().GetPPBindUserList(preInfoObj));
			}
			code=0;
			checker.setResult(0);
			checker.setResultDesc("接收成功");
		}else {
			checker.setResult(1);
			checker.setResultDesc("接收失败");
		}
		returnXml = checker.getReturnXml();
		new RecordLogDAO().recordDispatchLog(checker,code,checker.getLoid(),methodName);
		logger.warn("servicename[UpdateHQoS] cmdId[{}],loid[{}] 处理结束，返回响应信息:{}",
				 checker.getCmdId(),checker.getLoid(),returnXml);
		return returnXml;
	}
	
	
	
	/**
	 * xml去换行
	 */
	private String getStr(String str)
	{
		if(!StringUtil.IsEmpty(str)){
			return str.replace("\n","");
		}
		return str;
	}
}
