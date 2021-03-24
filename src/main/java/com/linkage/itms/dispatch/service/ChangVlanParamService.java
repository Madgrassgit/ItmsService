
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.ChangVlanParamDAO;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dispatch.obj.ChangVlanParamChecker;

/**
 * 修改光猫VLAN参数接口
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-11
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ChangVlanParamService implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(ChangVlanParamService.class);

	@Override
	public String work(String inParam)
	{
		logger.warn("ChangVlanParamService==>inParam:" + inParam);
		ChangVlanParamChecker checker = new ChangVlanParamChecker(inParam);
		if (false == checker.check())
		{
			logger.warn("获取修改光猫VLAN参数接口，入参验证失败，loid=[{}]",
					new Object[] { checker.getUserInfo() });
			logger.warn("ChangVlanParamService==>retParam={}", checker.getReturnXml());
			return checker.getReturnXml();
		}
		String userId = "";
		String deviceId = "";
		ChangVlanParamDAO dao = new ChangVlanParamDAO();
		Map<String, String> userMap = dao.getUserMapByLoid(checker.getUserInfo());
		if (null == userMap || userMap.isEmpty())
		{
			logger.warn("serviceName[ChangVlanParamService]cmdId[{}]loid[{}]无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		userId = userMap.get("user_id");
		deviceId = userMap.get("device_id");
		if (StringUtil.IsEmpty(userId))
		{
			logger.warn("serviceName[ChangVlanParamService]cmdId[{}]loid[{}]无此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
			return checker.getReturnXml();
		}
		// userId存在的话，则根据userId和oldVlanId查询是否存在，如果存在的话则进行修改
		ArrayList<HashMap<String, String>> resultList = null;
		resultList = dao.getMapByUser(userId, checker.getOldVlanId());
		if (null == resultList || resultList.isEmpty())
		{
			logger.warn("serviceName[ChangVlanParamService]cmdId[{}]loid[{}]此用户未查询到业务",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1004);
			checker.setResultDesc("根据LOID,VLANID未查询到业务");
			return checker.getReturnXml();
		}
		else if (resultList.size() > 1)
		{
			logger.warn("serviceName[ChangVlanParamService]cmdId[{}]loid[{}]此用户业务数不止一个",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(1003);
			checker.setResultDesc("此用户业务数不止一个");
			return checker.getReturnXml();
		}
		dao.updateVlanId(userId, checker.getOldVlanId(), checker.getNewVlanId());
		if (null != deviceId && !deviceId.isEmpty())
		{
			ServUserDAO servUserDao = new ServUserDAO();
			String devSn = null;
			String oui = null;
			int servTypeId = StringUtil.getIntegerValue(resultList.get(0).get("serv_type_id"));
			// 更新业务用户表的开通状态
//			servUserDao.updateServOpenStatus(StringUtil.getLongValue(userId));
			servUserDao.updateServOpenStatus(StringUtil.getLongValue(userId), servTypeId);
			// 预读调用对象
			PreServInfoOBJ preInfoObj = new PreServInfoOBJ(
					StringUtil.getStringValue(userId), "" + deviceId, "" + oui, devSn,
					resultList.get(0).get("serv_type_id"), "1");
			if (1 != CreateObjectFactory.createPreProcess().processServiceInterface(CreateObjectFactory.createPreProcess()
					.GetPPBindUserList(preInfoObj)))
			{
				logger.warn(
						"servicename[ChangVlanParamService]cmdId[{}]loid[{}]设备[{}]全业务下发，调用配置模块失败",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),
								deviceId });
				checker.setResult(1000);
				checker.setResultDesc("未知错误，请稍后重试");
			}
		}
		return checker.getReturnXml();
	}
}
