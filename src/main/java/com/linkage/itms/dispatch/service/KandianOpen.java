package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.KaidianOpenDao;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.KandianCheck;

public class KandianOpen implements IService{

	private static Logger logger = LoggerFactory.getLogger(KandianOpen.class);

	public String work(String param)
	{
		boolean isBinded = false;
		String oui = null;
		String devSn = null;
		String device_id=null;
		String user_id = null;
		KandianCheck checker = new KandianCheck(param);
		if(false == checker.check())
		{
			logger.error(
					"servicename[KandianOpen]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[KandianOpen]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						param });
		KaidianOpenDao dao = new KaidianOpenDao();
		Map<String,String> devData = dao.queryServInfo(checker.getUserInfoType(), checker.getUserInfo());
		if(null == devData || devData.isEmpty())
		{
			logger.warn(
					"servicename[KandianOpen]cmdId[{}]userinfo[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo()});
			checker.setResult(1002);
			checker.setResultDesc("查无此用户");
		}
		else
		{
			 device_id = StringUtil.getStringValue(devData, "device_id");
			 user_id = StringUtil.getStringValue(devData, "user_id");
			if(StringUtil.IsEmpty(user_id))
			{
				logger.warn(
						"servicename[KandianOpen]cmdId[{}]userinfo[{}]用户不存在",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(1002);
				checker.setResultDesc("查无此用户");
			}
			else if(StringUtil.IsEmpty(device_id))
			{
				logger.warn(
						"servicename[KandianOpen]cmdId[{}]userinfo[{}]用户没有绑定设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo()});
				checker.setResult(0);
				checker.setResultDesc("用户没有绑定设备");
			}
			else
			{
				
				isBinded = true;
				oui = devData.get("oui");
				devSn = devData.get("device_serialnumber");
			}
		}
		
		if (true == isBinded)
		{
			// 全业务下发
			// 预读调用对象
			PreServInfoOBJ preInfoObj = new PreServInfoOBJ(
					StringUtil.getStringValue(user_id), "" + device_id, "" + oui,
					devSn, "0", "1");
			if (1 != CreateObjectFactory.createPreProcess()
					.processServiceInterface(CreateObjectFactory.createPreProcess()
							.GetPPBindUserList(preInfoObj)))
			{
				logger.warn(
						"servicename[KandianOpen]cmdId[{}]userinfo[{}]设备[{}]全业务下发，调用后台预读模块失败",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),device_id});
				checker.setResult(1000);
				checker.setResultDesc("未知错误，请稍后重试");
			}else{
				checker.setResult(0);
				checker.setResultDesc("成功");
			}
		}
		
		String returnXml = checker.getReturnXml();
		// 记录日志
		new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(), "KandianOpen");
		logger.warn(
				"servicename[KandianOpen]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),returnXml});
		// 回单
		return returnXml;
	}

}
