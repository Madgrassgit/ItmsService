
package com.linkage.itms.nmg.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.MathUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.dao.SuperPwdDAO;
import com.linkage.itms.nmg.dispatch.obj.PasswordResetChecker;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * 终端密码重置
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-11-9
 * @category com.linkage.itms.nmg.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class PasswordResetService implements IService
{

	private static Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

	@Override
	public String work(String inXml)
	{
		logger.warn("PasswordResetService==>jsonString({})", inXml);
		PasswordResetChecker checker = new PasswordResetChecker(inXml);
		if (false == checker.check())
		{
			logger.warn("终端密码重置接口，入参验证失败，loid=[{}]", new Object[] { checker.getLoid() });
			logger.warn("PasswordResetService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		QueryDeviceIdDAO qdDao = new QueryDeviceIdDAO();
		String deviceId = "";
		List<HashMap<String, String>> userMap = null;
		userMap = qdDao.queryDevByLoid(checker.getLoid());
		if (userMap == null || userMap.isEmpty())
		{
			logger.warn("servicename[PasswordResetService]loid[{}]查无此用户",
					new Object[] { checker.getLoid() });
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{// 用户未绑定终端
			logger.warn("servicename[PasswordResetService]loid[{}]此客户未绑定",
					new Object[] { checker.getLoid() });
			checker.setResult(1004);
			checker.setResultDesc("此客户未绑定");
			return checker.getReturnXml();
		}
		
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		SuperPwdDAO spDao = new SuperPwdDAO();
		// 生成随机密码
		String newSuperPwd = "telecomadmin" + MathUtil.getRandom();
		// 下发节点
		ArrayList<ParameValueOBJ> objList = this.genObjList(newSuperPwd);
		ACSCorba corba = new ACSCorba();
		int retResult = corba.setValue(deviceId, objList);
		if (retResult == 0 || retResult == 1)
		{
			// 先下发节点 再进行数据更改
			spDao.updateSuperPwd(newSuperPwd, deviceId);
			logger.warn(deviceId+" PasswordResetService修改数据成功,密码为:"+newSuperPwd);
			checker.setResult(0);
			checker.setResultDesc("成功");
		}
		else
		{
			checker.setResult(1007);
			checker.setResultDesc("设备不能正常交互");
		}
		return checker.getReturnXml();
	}

	private ArrayList<ParameValueOBJ> genObjList(String pwd)
	{
		// 修改密码到设备
		ArrayList<ParameValueOBJ> objList = new ArrayList<ParameValueOBJ>();
		ParameValueOBJ obj = new ParameValueOBJ();
		obj.setName("InternetGatewayDevice.DeviceInfo.X_CT-COM_TeleComAccount.Password");
		obj.setValue(pwd);
		obj.setType("1");
		objList.add(obj);
		return objList;
	}
}
