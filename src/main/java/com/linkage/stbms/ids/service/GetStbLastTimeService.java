package com.linkage.stbms.ids.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.itms.dispatch.service.IService;
import com.linkage.stbms.ids.dao.GetStbLastTimeDAO;
import com.linkage.stbms.ids.obj.GetStbLastTimeCheck;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2018-1-23
 * @category com.linkage.itms.dispatch.cqdx.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class GetStbLastTimeService implements IService
{
	private static Logger logger = LoggerFactory.getLogger(GetStbLastTimeService.class);

	@Override
	public String work(String inXml)
	{
		logger.warn("GetStbLastTimeService({})", inXml);
		GetStbLastTimeCheck checker=new GetStbLastTimeCheck(inXml);
		if (false == checker.check()) {
			logger.error("验证未通过，返回：\n" + checker.getReturnXml());
			logger.warn("return=({})", checker.getReturnXml());  // 打印回参
			return checker.getReturnXml();
		}
		GetStbLastTimeDAO dao=new GetStbLastTimeDAO();
		List<HashMap<String, String>> userMap=null;
		List<HashMap<String, String>> userList=null;
		if(checker.getSearchType()==1)
		{
			userList=dao.querycustomer(checker.getSearchType(), checker.getUserInfo());
			if (userList == null || userList.isEmpty())
			{
				checker.setResult(1007);
				checker.setResultDesc("用户不存在");
				return checker.getReturnXml();
			}
			userMap=dao.queryLastTime(checker.getSearchType(), checker.getUserInfo());
			if (userMap == null || userMap.isEmpty())
			{
				checker.setResult(1008);
				checker.setResultDesc("未绑定");
				return checker.getReturnXml();
			}
		}else
		{
			userMap=dao.queryLastTime1(checker.getSearchType(), checker.getUserInfo());
			if (userMap == null || userMap.isEmpty())
			{
				checker.setResult(1009);
				checker.setResultDesc("无此设备");
				return checker.getReturnXml();
			}
			if (userMap.size()>1)
			{
				checker.setResult(1010);
				checker.setResultDesc("查出多个设备，请输入完整机顶盒序列号或者通过MAC查询");
				return checker.getReturnXml();
			}
		}
		
		checker.setResult(0);
		checker.setResultDesc("成功");
		String time=String.valueOf(userMap.get(0).get("last_time"));
		checker.setLastTime(new DateTimeUtil().getLongDate(Long.valueOf(time)));
		return checker.getReturnXml();
	}
	
}
