
package com.linkage.itms.ids.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.Global;
import com.linkage.itms.commom.util.WSClientUtil;

/**
 * 设备周期变更上报
 * 
 * @author zhangsb (Ailk No.)
 * @version 1.0
 * @since 2014年5月16日 
 * @category com.linkage.itms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ReportProidResultService
{

	private static final Logger logger = LoggerFactory
			.getLogger(ReportProidResultService.class);

	/**
	 * @param xmlRequest
	 *            xml请求报文
	 * @return 响应报文
	 */
	public String work(String xmlRequest)
	{
		// 1.只是将请求转发给IDS WebService，并返回，透传
		logger.warn("reportPeroidResult--》request xml is [{}]", xmlRequest);
		String method = "reportPeroidResult";
		String response = WSClientUtil.callRemoteService(Global.idsServiceUrl,
				xmlRequest, method);
		logger.warn("response xml is [{}]", response);
		return response;
	}
}
