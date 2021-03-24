package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.A8setVoipDAO;
import com.linkage.itms.dispatch.obj.A8setVoipChecker;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-12-4
 * @category com.linkage.itms.dispatch.cqdx.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class A8setVoipService implements IService
{
	private static Logger logger = LoggerFactory.getLogger(A8setVoipService.class);

	@Override
	public String work(String inXml)
	{
		A8setVoipChecker checker=new A8setVoipChecker(inXml);
		if (false == checker.check())
		{
			logger.error( "servicename[A8setVoipService]cmdId[{}]ClientType[{}]Loid[{}]Voip_phone[{}]Voip_prot[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getClientType(), checker.getLoid(), checker.getVoip_phone() , checker.getVoip_prot(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
			A8setVoipDAO dao=new A8setVoipDAO();
			List<HashMap<String, String>> userMapList = null;
			List<HashMap<String, String>> voipMapList = null;
			userMapList=dao.queryUserByLoid(checker.getLoid(),checker.getVoip_phone(),checker.getVoip_prot());
			if (userMapList == null || userMapList.isEmpty()) {
				logger.warn("servicename[A8setVoipService]Loid[{}]无此客户信息"+checker.getLoid());
				checker.setResult(1002);
				checker.setResultDesc("无此客户信息");
				return checker.getReturnXml();
			}
			String userId = StringUtil.getStringValue(userMapList.get(0),"user_id", "");
			//判断用户是否开通语音业务
			voipMapList=dao.queryvoip(userId, checker.getVoip_phone(),checker.getVoip_prot());
			if (voipMapList == null || voipMapList.isEmpty()) {
				logger.warn("servicename[A8setVoipService]Loid[{}]此用户未绑定"+checker.getLoid());
				checker.setResult(1004);
				checker.setResultDesc("此用户未绑定");
				return checker.getReturnXml();
			}
			//String deviceId = StringUtil.getStringValue(userMapList.get(0),"device_id", "");
			/*if (StringUtil.IsEmpty(deviceId)) {
				logger.warn("servicename[A8setVoipService]Loid[{}]此用户未绑定"+checker.getLoid());
				checker.setResult(1004);
				checker.setResultDesc("此用户未绑定");
				return checker.getReturnXml();
			}*/
			//判断端口是否被占用
			int tote=dao.querysum(checker.getLoid(),checker.getVoip_phone(),checker.getVoip_prot());
			if(tote>0)
			{
				logger.warn("servicename[A8setVoipService]Loid[{}]Voip_phone[{}]Voip_prot[{}]端口被占用"+checker.getLoid(),checker.getVoip_phone(),checker.getVoip_prot());
				checker.setResult(4);
				checker.setResultDesc("端口被占用");
				return checker.getReturnXml();
			}
			//修改语音端口
			int update=dao.UpdateVoipprot(userId, checker.getVoip_phone(),checker.getVoip_prot());
			if(update>0)
			{
				checker.setResult(0);
				checker.setResultDesc("成功");
				String returnXml = checker.getReturnXml();
				logger.warn("servicename[A8setVoipService]cmdId[{}]ClientType[{}]Loid[{}]Voip_phone[{}]Voip_prot[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getClientType(), checker.getLoid(), checker.getVoip_phone() , checker.getVoip_prot(),
						checker.getReturnXml(), returnXml });
				return returnXml;
			}else
			{
				checker.setResult(1000);
				checker.setResultDesc("未知错误");
				String returnXml = checker.getReturnXml();
				logger.warn("servicename[A8setVoipService]cmdId[{}]ClientType[{}]Loid[{}]Voip_phone[{}]Voip_prot[{}]处理结束，返回响应信息:{}",
						new Object[] { checker.getCmdId(), checker.getClientType(), checker.getLoid(), checker.getVoip_phone() , checker.getVoip_prot(),
						checker.getReturnXml()});
				return returnXml;
			}
}
}
