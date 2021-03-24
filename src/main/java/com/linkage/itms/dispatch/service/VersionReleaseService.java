
package com.linkage.itms.dispatch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dispatch.main.CallService;
import com.linkage.itms.dispatch.obj.VersionReleaseChecker;

/**
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2014年12月23日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class VersionReleaseService
{

	// 日志记录
	private static Logger logger = LoggerFactory.getLogger(VersionReleaseService.class);

	public String work(String param)
	{
		VersionReleaseChecker checker = new VersionReleaseChecker(param);
		if (checker.check())
		{
			logger.info("VersionReleaseService版本发布审核结果回馈结果：param = {}", param);
			return checker.getReturnXml();
		}
		return checker.getReturnXml();
	}
}
