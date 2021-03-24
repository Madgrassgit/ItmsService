package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryStbMacDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.QueryStbMacChecker;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-10-24
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryStbMacService implements IService
{
	private static final Logger logger = LoggerFactory.getLogger(QueryStbMacService.class);

	@Override
	public String work(String inParam)
	{
		QueryStbMacChecker checker=new QueryStbMacChecker(inParam);
		QueryStbMacDAO dao=new QueryStbMacDAO();
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		ACSCorba corba = new ACSCorba();
		 String result = "";
		if (false == checker.check())
		{
			logger.warn("查询家庭网关是否学习到机顶盒接口，入参验证失败，UserInfoType=[{}]，UserInfo=[{}]",
					new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
			logger.warn("QueryStbMacService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		logger.warn("查询家庭网关是否学习到机顶盒mac接口，入参验证通过，UserInfoType=[{}]，UserInfo=[{}]",
				new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
		List<HashMap<String,String>> userMapList = null;
		String deviceId = "";
		if(checker.getUserInfoType() == 1)
		{
			userMapList = dao.queryUserByNetAccount(checker.getUserInfo());
		}else if (checker.getUserInfoType() == 2)
		{
			userMapList = dao.queryUserByLoid(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 3)
		{
			userMapList = dao.queryUserByIptvAccount(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 4)
		{
			userMapList = dao.queryUserByVoipPhone(checker.getUserInfo());
		}
		else if (checker.getUserInfoType() == 5)
		{
			userMapList = dao.queryUserByVoipAccount(checker.getUserInfo());
		}
		if (userMapList == null || userMapList.isEmpty())
		{
			logger.warn("查询家庭网关是否学习到机顶盒mac接口,UserInfoType=[{}]，UserInfo=[{}]无此客户信息",
					new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("无此客户信息");
			return checker.getReturnXml();
		}
		if(StringUtil.IsEmpty(userMapList.get(0).get("device_id")))
		{
			logger.warn("查询家庭网关是否学习到机顶盒mac接口,UserInfoType=[{}]，UserInfo=[{}]查无此设备",
					new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
			checker.setResult(1003);
			checker.setResultDesc("查无此设备");
			return checker.getReturnXml();
		}
		deviceId=StringUtil.getStringValue(userMapList.get(0).get("device_id"));
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		// 设备正在被操作，不能获取节点值
		if (-3 == flag) {
			logger.warn("查询家庭网关是否学习到机顶盒mac接口,device_id={},设备正在被操作，无法获取节点值",
					new Object[] {deviceId });
					checker.setResult(1005);
					checker.setResultDesc("设备不能正常交互");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					return checker.getReturnXml();
		}else if (1 == flag) {
			logger.warn("查询家庭网关是否学习到机顶盒mac接口,device_id={},设备在线，可以进行采集操作",
					new Object[] {deviceId });
			String macPath = "InternetGatewayDevice.Services.X_CT-COM_IPTV.STBMAC";
			ArrayList<ParameValueOBJ> macTypeList = corba.getValue(deviceId, macPath);
			if (macTypeList != null && macTypeList.size() > 0) {
				for (ParameValueOBJ pvobj : macTypeList) {
					if (pvobj.getName().endsWith("STBMAC")) {
						result = pvobj.getValue();
					}
				}
			}
			logger.warn("查询家庭网关是否学习到机顶盒mac接口,[{}],采集到的，mac",new Object[] {result });
		}else {// 设备不在线，不能获取节点值
			logger.warn("设备不在线，无法获取节点值");
			checker.setResult(1005);
			checker.setResultDesc("设备不能正常交互");
			logger.warn("return=({})", checker.getReturnXml()); // 打印回参
			return checker.getReturnXml();
		}
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),"QueryStbMacService");
		checker.setResult(0);
		checker.setResultDesc("成功");
		checker.setOnlineMAC(result);
		return checker.getReturnXml();
		
	} 
	
}
