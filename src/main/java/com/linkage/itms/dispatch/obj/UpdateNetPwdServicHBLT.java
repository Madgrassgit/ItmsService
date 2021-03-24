
package com.linkage.itms.dispatch.obj;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;

/**
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-12-6
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class UpdateNetPwdServicHBLT
{

	private static Logger logger = LoggerFactory.getLogger(UpdateNetPwdServicHBLT.class);

	public int work(String adAcount, String lSHNo, String orderType, String newPassWord)
	{
		UpdateNetPwd4HBLTChecker checker = new UpdateNetPwd4HBLTChecker();
		if ("1" != checker.check(adAcount,lSHNo,orderType,newPassWord))
		{
			logger.warn("入参验证没通过,Loid=[{}],NetUserName=[{}],NetPwd=[{}],servType=[{}]", new Object[] {
					checker.getLoid(), checker.getNetUserName(), checker.getNetPwd() ,checker.getServType()});
			return checker.getResult();
		}
		logger.warn("入参验证通过,NetUserName=[{}],NetPwd=[{}],servType=[{}]", new Object[] {
				 checker.getNetUserName(), checker.getNetPwd() ,checker.getServType()});
		
		// 查询用户设备信息
		boolean isExist = true;
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		Map<String, String> userDevInfo = userDevDao.qryUserByNameAndLoid(
				checker.getLoid(), checker.getNetUserName(), checker.getServType());
		if (null == userDevInfo || userDevInfo.isEmpty())
		{
			logger.warn("servicename[UpdateNetPwdService]NetUserName=[{}]查无此用户",checker.getNetUserName());
			String username = checker.getNetUserName();
			int index = username.indexOf("@");
			if(index>=0){
				checker.setNetUserName(username.substring(0, index));
				userDevInfo = userDevDao.qryUserByNameAndLoid(
						checker.getLoid(), checker.getNetUserName(), checker.getServType());
			}
			
			if (null == userDevInfo || userDevInfo.isEmpty())
			{
				logger.warn("servicename[UpdateNetPwdService]NetUserName=[{}]查无此用户",checker.getNetUserName());
				isExist = false;
				checker.setResult(-1);
				checker.setResultDesc("无此用户信息");
			}
		}
		if(isExist)
		{
			String deviceId = userDevInfo.get("device_id");
			String user_id = userDevInfo.get("user_id");
			String oui = userDevInfo.get("oui");
			String devSN = userDevInfo.get("device_serialnumber");
			String wan_type = userDevInfo.get("wan_type");
			
			if("1".equals(checker.getServType())){
				checker.setServType("10");
			}else if("2".equals(checker.getServType())){
				checker.setServType("11");
			}
			
			
			// wan_type=2路由
			if (wan_type.equals("2") && !StringUtil.IsEmpty(deviceId))
			{
				// 更改密码
				userDevDao.modPwdAndStatus(user_id, checker.getNetUserName(),
						checker.getNetPwd(), checker.getServType());
				
				// 业务下发
				boolean res = serviceDoner(deviceId, user_id, oui, devSN, checker.getServType());
				if (!res)
				{
					logger.warn(
							"servicename[UpdateNetPwdService1]cmdId[{}]loid[{}]netUserName[{}]netPwd[{}]ServType[{}]下发特定业务，调用后台预读模块失败，业务类型为：[{}]",
							new Object[] { checker.getCmdId(), checker.getLoid(), checker.getNetUserName(),checker.getNetPwd(),checker.getServType(),checker.getServType() });
					checker.setResult(1000);
					checker.setResultDesc("下发业务失败，请稍后重试");
				}
				checker.setResult(0);
				checker.setResultDesc("成功");
			}
			else
			{
				// 更改密码
				userDevDao.modCustomerPwd(user_id, checker.getNetUserName(),
						checker.getNetPwd(), checker.getServType());
				logger.warn(
						"servicename[UpdateNetPwdService1]cmdId[{}]loid[{}]netUserName[{}]netPwd[{}]宽带业务类型不为路由",
						new Object[] { checker.getCmdId(), checker.getNetUserName(),
								checker.getNetPwd() });
				checker.setResult(0);
				checker.setResultDesc("成功");
			}
		}
		
		if(checker.getResult()==0){
			return 1;
		}
		else if(checker.getResult()==-3){
			return -3;
		}
		else if(checker.getResult()==-1){
			return -1;
		}
		else{
			return -6;
		}
	}

	/**
	 * 业务下发
	 * 
	 * @param deviceId
	 *            设备编码
	 * @param user_id
	 *            用户ID
	 * @param oui
	 *            设备OUI
	 * @param devSN
	 *            设备SN
	 * @param ServiceType  业务类型
	 * @return 下发结果
	 */
	private boolean serviceDoner(String deviceId, String user_id, String oui, String devSN, String ServiceType)
	{
		logger.warn("UpdateNetPwdService==>serviceDoner({}{}{}{}{})", new Object[] { deviceId,
				user_id, oui, devSN, ServiceType });
		boolean res = false;
		ServUserDAO servUserDao = new ServUserDAO();
		// 更新业务用户表的业务开通状态
		/*servUserDao.updateServOpenStatus(StringUtil.getLongValue(user_id),
				StringUtil.getIntegerValue(ServiceType));*/
		// 预读调用对象
		PreServInfoOBJ preInfoObj = new PreServInfoOBJ(user_id, deviceId, oui, devSN,
				ServiceType, "1");
		if (1 == CreateObjectFactory.createPreProcess().processServiceInterface(
				CreateObjectFactory.createPreProcess().GetPPBindUserList(preInfoObj)))
		{
			res = true;
		}
		return res;
	}
}
