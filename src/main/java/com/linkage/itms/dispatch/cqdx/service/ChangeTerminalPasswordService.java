package com.linkage.itms.dispatch.cqdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.MathUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.cqdx.obj.ChangeTerminalPasswordDealXML;
import com.linkage.itms.hlj.dispatch.dao.QueryDeviceIdDAO;
import com.linkage.itms.hlj.dispatch.dao.SuperPwdDAO;
import com.linkage.itms.obj.ParameValueOBJ;
/**
 * 修改或重置终端超级密码
 * @author wangyan10(Ailk NO.76091)
 * @version 1.0
 * @since 2017-11-19
 */
public class ChangeTerminalPasswordService {
	private static Logger logger = LoggerFactory.getLogger(ChangeTerminalPasswordService.class);

	public String work(String inXml) {
		logger.warn("servicename[ChangeTerminalPasswordService]执行，入参为：{}", inXml);
		ChangeTerminalPasswordDealXML deal = new ChangeTerminalPasswordDealXML();
		Document document = deal.getXML(inXml);
		if (document == null) {
			logger.warn("servicename[ChangeTerminalPasswordService]解析入参错误！");
			deal.setResult("-99");
			deal.setErrMsg("解析入参错误！");
			return deal.returnXML();
		}
		
		String logicId = deal.getLogicId();
		String pppUsename = deal.getPppUsename();
		String terminalPassword = deal.getTerminalPassword();
		if("".equals(logicId) && "".equals(pppUsename)){
			logger.warn("servicename[ChangeTerminalPasswordService]宽带账号和逻辑账号不能同时为空！");
			deal.setResult("-99");
			deal.setErrMsg("宽带账号和逻辑账号不能同时为空！");
			return deal.returnXML();
		}
		QueryDeviceIdDAO qdDao = new QueryDeviceIdDAO();
		String deviceId = "";
		List<HashMap<String, String>> userMap = null;
		if (!"".equals(pppUsename)){
			userMap = qdDao.qryUserByKdName(pppUsename);
		}else{
			userMap = qdDao.queryDevByLoid(logicId);
		}
		
		if (userMap == null || userMap.isEmpty())
		{
			logger.warn("servicename[ChangeTerminalPasswordService]loid[{}]查无此用户",
					new Object[] { logicId });
			deal.setResult("-1");
			deal.setErrMsg("用户不存在");
			return deal.returnXML();
		}
		if (StringUtil.IsEmpty(userMap.get(0).get("device_id")))
		{// 用户未绑定终端
			logger.warn("servicename[ChangeTerminalPasswordService]loid[{}]此客户未绑定",
					new Object[] { logicId });
			deal.setResult("-99");
			deal.setErrMsg("此客户未绑定");
			return deal.returnXML();
		}
		
		deviceId = StringUtil.getStringValue(userMap.get(0), "device_id");
		SuperPwdDAO spDao = new SuperPwdDAO();
		
		// 如果写了密码，那就以此密码为准去当作新密码，否则的话就生成随机密码
		String newSuperPwd = "";
		if(!"".equals(terminalPassword)){
			newSuperPwd = terminalPassword;
		}else{
			newSuperPwd = "telecomadmin" + MathUtil.getRandom();
		}
		
		// 下发节点
		ArrayList<ParameValueOBJ> objList = this.genObjList(newSuperPwd);
		ACSCorba corba = new ACSCorba();
		int retResult = corba.setValue(deviceId, objList);
		if (retResult == 0 || retResult == 1)
		{
			// 先下发节点 再进行数据更改
			spDao.updateSuperPwd(newSuperPwd, deviceId);
			logger.warn(deviceId+" ChangeTerminalPasswordService修改数据成功,密码为:"+newSuperPwd);
			deal.setResult("0");
			deal.setErrMsg("成功");
			deal.setReturnPwd(newSuperPwd);
		}
		else
		{
			deal.setResult("-99");
			deal.setErrMsg("设备不能正常交互");
		}
		return deal.returnXML();
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
