package com.linkage.itms.dispatch.sxdx.service;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.DevReboot;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.sxdx.dao.PublicDAO;
import com.linkage.itms.dispatch.sxdx.obj.CpeRebootDealXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 甘肃电信重启终端接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月11日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class CpeRebootService extends ServiceFather {
	public CpeRebootService(String methodName)
	{
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(CpeRebootService.class);
	private ACSCorba corba = new ACSCorba();
	private CpeRebootDealXML dealXML;
	
	public int work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		
		dealXML = new CpeRebootDealXML(methodName);
		// 验证入参
		if (null == dealXML.getXML1(inXml)) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			return -2;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		
		PublicDAO dao = new PublicDAO();

		ArrayList<HashMap<String, String>> userDevList = dao.queryDeviceInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}", userDevList.toString());
		
		if(userDevList.size() > 1){
			logger.warn(methodName+"["+dealXML.getOpId()+"],查询到多条结果返回数量");
			return  userDevList.size();
		}
		else if(null == userDevList || userDevList.size()==0 || StringUtil.isEmpty(StringUtil.getStringValue(userDevList.get(0), "device_id"))){
			logger.warn(methodName+"["+dealXML.getOpId()+"],未查询到结果，返回0");
			return 0;
		}
		
		String deviceId = StringUtil.getStringValue(userDevList.get(0), "device_id");
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag){
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备不在线或正在被操作，无法重启，返回-1");
			return -1;
		}
		
		logger.warn(methodName+"["+dealXML.getOpId()+"],设备在线，准备重启。");
		int irt = DevReboot.reboot(deviceId);
		if(1 == irt){
			logger.warn(methodName+"["+dealXML.getOpId()+"],重启成功,返回1。");
			return 1;
		}
		else{
			logger.warn(methodName+"["+dealXML.getOpId()+"],重启失败,设备返回错误码{}。", irt);
			return -2;
		}
	}

	
}
