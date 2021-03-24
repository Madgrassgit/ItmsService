
package com.linkage.itms.dispatch.obj;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.service.IService;
import com.linkage.itms.dispatch.service.UpdateNetPwdService;

/**
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-12-6
 * @category com.linkage.itms.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class UpdateNetPwdServic1 implements IService
{

	private static Logger logger = LoggerFactory.getLogger(UpdateNetPwdServic1.class);
	// 业务类型（宽带）
	private final String ServiceType = "10";

	@Override
	public String work(String inParam)
	{
		UpdateNetPwdChecker checker = new UpdateNetPwdChecker(inParam);
		if (false == checker.check())
		{
			logger.warn("入参验证没通过,Loid=[{}],NetUserName=[{}],NetPwd=[{}]", new Object[] {
					checker.getLoid(), checker.getNetUserName(), checker.getNetPwd() });
			logger.warn("work==>inParam=" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 查询用户设备信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		Map<String, String> userDevInfo = userDevDao.qryUserByNameAndLoid(
				checker.getLoid(), checker.getNetUserName());
		if (null == userDevInfo || userDevInfo.isEmpty())
		{
			logger.warn(
					"servicename[UpdateNetPwdService]cmdId[{}]Loid=[{}],NetUserName=[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getLoid(),
							checker.getNetUserName() });
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
		}
		else
		{
			String deviceId = userDevInfo.get("device_id");
			String user_id = userDevInfo.get("user_id");
			String oui = userDevInfo.get("oui");
			String devSN = userDevInfo.get("device_serialnumber");
			String wan_type = userDevInfo.get("wan_type");
			// wan_type=2路由
			if (wan_type.equals("2") && !StringUtil.IsEmpty(deviceId))
			{
				// 更改密码
				userDevDao.modPwdAndStatus(user_id, checker.getNetUserName(),
						checker.getNetPwd());
				// 业务下发
				boolean res = serviceDoner(deviceId, user_id, oui, devSN);
				if (!res)
				{
					logger.warn(
							"servicename[UpdateNetPwdService1]cmdId[{}]loid[{}]netUserName[{}]netPwd[{}]下发特定业务，调用后台预读模块失败，业务类型为：[{}]",
							new Object[] { checker.getCmdId(), checker.getNetUserName(),
									checker.getNetPwd() });
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
						checker.getNetPwd());
				logger.warn(
						"servicename[UpdateNetPwdService1]cmdId[{}]loid[{}]netUserName[{}]netPwd[{}]宽带业务类型不为路由",
						new Object[] { checker.getCmdId(), checker.getNetUserName(),
								checker.getNetPwd() });
				checker.setResult(0);
				checker.setResultDesc("成功");
			}
		}
		String returnXml = checker.getReturnXml();
		logger.warn(
				"servicename[UpdateNetPwdServic1]cmdId[{}]loid[{}]netUserName[{}]netPwd[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getNetUserName(),
						checker.getNetPwd() });
		return returnXml;
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
	 * @return 下发结果
	 */
	private boolean serviceDoner(String deviceId, String user_id, String oui, String devSN)
	{
		logger.warn("UpdateNetPwdService==>serviceDoner({})", new Object[] { deviceId,
				user_id, oui, devSN });
		boolean res = false;
		ServUserDAO servUserDao = new ServUserDAO();
		// 更新业务用户表的业务开通状态
		servUserDao.updateServOpenStatus(StringUtil.getLongValue(user_id),
				StringUtil.getIntegerValue(ServiceType));
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
