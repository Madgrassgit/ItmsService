package com.linkage.itms.hlj.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.MathUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.dao.SuperPwdDAO;
import com.linkage.itms.hlj.dispatch.obj.PasswordResetChecker;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-2
 * @category com.linkage.itms.hlj.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class PasswordResetService implements HljIService
{
	private static Logger logger = LoggerFactory
			.getLogger(PasswordResetService.class);

	@Override
	public String work(String jsonString)
	{
		logger.warn("PasswordResetService==>jsonString({})", jsonString);
		PasswordResetChecker checker = new PasswordResetChecker(jsonString);
		if (false == checker.check())
		{
			logger.warn("E-8c密码重置接口，入参验证失败，QueryNum=[{}]",
					new Object[] { checker.getQueryNum() });
			logger.warn("PasswordResetService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		QueryDeviceIdDAO qdDao = new QueryDeviceIdDAO();
		String deviceId = "";
		List<HashMap<String, String>> userMap = null;
		if (checker.getQueryType() == 0)
		{
			userMap = qdDao.queryUserByNetAccount(checker.getQueryNum());
		}
		else if (checker.getQueryType() == 1)
		{
			userMap = qdDao.queryDevByLoid(checker.getQueryNum());
		}
		else if (checker.getQueryType() == 2)
		{
			userMap = qdDao.queryUserByDevSN(checker.getQueryNum());
		}
		else
		{
		}
		if (userMap == null || userMap.isEmpty())
		{
			checker.setResult(8);
			checker.setResultDesc("ITMS未知异常-查询结果为空");
			return checker.getReturnXml();
		}
		if (userMap.size() > 1)
		{
			checker.setResult(1001);
			checker.setResultDesc("数据不唯一，请使用devSn查询");
			return checker.getReturnXml();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{
			checker.setResult(3);
			checker.setResultDesc("无设备信息");
			return checker.getReturnXml();
		}
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
		SuperPwdDAO spDao = new SuperPwdDAO();
		//生成随机密码
		String newSuperPwd = "telecomadmin" + MathUtil.getRandom();
		//下发节点
		ArrayList<ParameValueOBJ> objList = this.genObjList(newSuperPwd);
		ACSCorba corba = new ACSCorba();
		int retResult = corba.setValue(deviceId, objList);
		JSONObject jo = new JSONObject();
		try
		{
			jo.put("Loid", StringUtil.getStringValue(userMap.get(0), "loid", ""));
			if(retResult == 0 || retResult == 1){
				//先下发节点 再进行数据更改
				spDao.updateSuperPwd(newSuperPwd, deviceId);
				logger.warn("PasswordResetService修改数据成功");
				jo.put("OpResult", "成功");
				jo.put("OpErrorNumber", "");
			}else{
				jo.put("OpResult", "不成功");
				jo.put("OpErrorNumber", retResult);
			}
			
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return jo.toString();
	}
	
	private ArrayList<ParameValueOBJ> genObjList(String pwd){
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
