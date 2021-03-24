package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.MathUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.ChangePasChecker;
import com.linkage.itms.hlj.dispatch.dao.SuperPwdDAO;
import com.linkage.itms.obj.ParameValueOBJ;


public class ChangePasService implements IService {

	/** 日志 */
	private static final Logger logger = LoggerFactory.getLogger(ChangePasService.class);

	@Override
	public String work(String inXml) {
		ChangePasChecker checker = new ChangePasChecker(inXml);
		try {
			// 验证入参格式是否正确
			if (!checker.check()) {
				logger.warn("servicename[ChangePasService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
						new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
				return checker.getReturnXml();
			}
			logger.warn("servicename[BindInfoCpmisService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
					new Object[] {checker.getCmdId(), checker.getUserInfo(), inXml});
			
			QueryDevDAO qdDao = new QueryDevDAO();
			List<HashMap<String, String>> userMap = null;
			if (checker.getUserInfoType() == 1) {
				userMap = qdDao.queryUserByNetAccount(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 2) {
				userMap = qdDao.queryUserByLoid(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 3) {
				userMap = qdDao.queryUserByIptvAccount(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 4) {
				userMap = qdDao.queryUserByVoipPhone(checker.getUserInfo());
			}
			else if (checker.getUserInfoType() == 5) {
				userMap = qdDao.queryUserByVoipAccount(checker.getUserInfo());
			}
			if (userMap == null || userMap.isEmpty()) {
				checker.setResult(1001);
				checker.setResultDesc("无此用户信息");
				return checker.getReturnXml();
			}
			if (userMap.size() > 1 && checker.getUserInfoType() != 1) {
				checker.setResult(1000);
				checker.setResultDesc("数据不唯一，请使用逻辑SN查询");
				return checker.getReturnXml();
			}
			if (StringUtil.IsEmpty(userMap.get(0).get("device_id"))) {
				checker.setResult(1002);
				checker.setResultDesc("未绑定设备");
				return checker.getReturnXml();
			}
			
			String deviceId = StringUtil.getStringValue(userMap.get(0), "device_id", "");
			SuperPwdDAO spDao = new SuperPwdDAO();
			// 生成随机密码
			String newSuperPwd = "telecomadmin" + MathUtil.getRandom();
			// 下发节点
			ArrayList<ParameValueOBJ> objList = this.genObjList(newSuperPwd);
			ACSCorba corba = new ACSCorba();
			int retResult = corba.setValue(deviceId, objList);
			if(retResult == 0 || retResult == 1){
				// 先下发节点 再进行数据更改
				spDao.updateSuperPwd(newSuperPwd, deviceId);
				logger.warn("PasswordResetService修改数据成功");
				checker.setResultDesc("成功");
				checker.setNewPassword(newSuperPwd);
			}
			else{
				logger.warn("servicename[BindInfoCpmisService]cmdId[{}]userinfo[{}]修改设备密码失败",
						new Object[] {checker.getCmdId(), checker.getUserInfo()});
				checker.setResultDesc("不成功");
			}
		}
		catch (Exception e) {
			logger.warn("ChangePasService is error:", e);
		}
		return getReturnXml(checker);
	}
	
	/**
	 * 记录日志返回xml
	 * @param checker
	 * @return
	 */
	private String getReturnXml(ChangePasChecker checker) {
		new RecordLogDAO().recordDispatchLog(checker, "BindInfoCpmisService", checker.getCmdId());
		logger.info("servicename[BindInfoCpmisService]cmdId[{}]处理结束，返回响应信息:{}",
				new Object[] {checker.getCmdId(), checker.getReturnXml()});
		return checker.getReturnXml();
	}

	/**
	 * 下发节点
	 * @param pwd
	 * @return
	 */
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
