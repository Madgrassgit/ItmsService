
package com.linkage.stbms.ids.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.cao.DevOnlineCAO;
import com.linkage.stbms.dao.RecordLogDAO;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.util.GetStbOnlineStatusChecker;
import com.linkage.stbms.itv.main.Global;

/**
 * 设备在线情况查询接口
 * 
 * @author yinlei3 (73167)
 * @version 1.0
 * @since 2015年7月3日
 * @category com.linkage.stbms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class GetStbOnlineStatusService
{

	private static Logger logger = LoggerFactory
			.getLogger(GetStbOnlineStatusService.class);

	public String work(String inParam)
	{
		logger.warn("GetStbOnlineStatusService==>inParam:" + inParam);
		GetStbOnlineStatusChecker checker = new GetStbOnlineStatusChecker(inParam);
		// 入参验证
		if (false == checker.check())
		{
			logger.warn("机顶盒设备在线情况查询接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			logger.warn("GetStbOnlineStatusService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 在线情况
		boolean succ = false;
		String strOnline = null;
		String deviceId = null;
		UserStbInfoDAO dao = new UserStbInfoDAO();
		Map<String, String> userMap = dao.getDeviceIdStr(checker.getSearchType(),
				checker.getSearchInfo(), "1");
		if (null == userMap
				|| StringUtil.IsEmpty(StringUtil.getStringValue(userMap, "device_id")))
		{
			logger.warn("查无此设备，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			checker.setRstCode("0");
			checker.setRstMsg("查无此设备");
		}
		else
		{
			// 设备ID
			deviceId = userMap.get("device_id");
			checker.setRstCode("1");
			checker.setRstMsg("成功");
			// 实时获取在线状态
			int iOnline = DevOnlineCAO.devOnlineTest(deviceId) == 1 ? 1 : -1;
			// 设置参数
			checker.setOnlineStatus(iOnline);
		}
		logger.warn("GetStbOnlineStatusService==>returnXML:" + checker.getReturnXml());
		if("xj_dx".equals(Global.G_instArea)){
			new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
	                checker.getReturnXml(), 1);
		}
		
		return checker.getReturnXml();
	}
}
