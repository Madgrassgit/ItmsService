
package com.linkage.itms.ids.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.Global;
import com.linkage.itms.commom.util.WSClientUtil;

/**
 * 设备状态信息上报功能开启和关闭接口
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-10-17
 * @category com.linkage.itms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DiagnosticEnableResultService
{

	private static final Logger logger = LoggerFactory
			.getLogger(DiagnosticEnableResultService.class);

	/**
	 * @param xmlRequest
	 *            xml请求报文
	 * @return 响应报文
	 */
	public String diagnosticEnableResult(String xmlRequest)
	{
		// 1.只是将请求转发给IDS WebService，并返回，透传
		logger.warn("DiagnosticEnableResultService request xml is [{}]", xmlRequest);
		String method = "diagnosticEnableResult";
		String response = WSClientUtil.callRemoteService(Global.idsServiceUrl,
				xmlRequest, method);
		logger.warn("DiagnosticEnableResultService response xml is [{}]", response);
		return response;
	}
}
