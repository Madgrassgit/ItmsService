
package com.linkage.itms.dispatch.main;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dispatch.service.DeleteHqosServiceImpl;
import com.linkage.itms.dispatch.service.HqosQueryDevSnServiceImpl;
import com.linkage.itms.dispatch.service.HqosQueryNetStatusServiceImpl;
import com.linkage.itms.dispatch.service.HqosQueryVersionServiceImpl;
import com.linkage.itms.dispatch.service.HqosTwoCfgQueryServiceImpl;
import com.linkage.itms.dispatch.service.HqosTwoStatusServiceImpl;
import com.linkage.itms.dispatch.service.OpenHqosServiceImpl;
import com.linkage.itms.dispatch.service.UpdateHqosServiceImpl;

/**
 * 
 * @author guxl3 (Ailk No.)
 * @version 1.0
 * @since 2021年2月5日
 * @category com.linkage.itms.dispatch.main
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class HqosService
{

	/**     日志记录     */
	private static Logger logger = LoggerFactory.getLogger(HqosService.class);

	/**
	 * 3.1.1.	设备SN查询
	 * @param param
	 * @return
	 */
	public String QueryDevSn(String param)
	{
		logger.warn("servicename[QueryDevSn]，调用端IP[{}],入参为：{}",getClientInfo(), param);
		return new HqosQueryDevSnServiceImpl().work(param);
	}

	/**
	 * 3.1.2.	设备版本信息查询
	 * @param param
	 * @return
	 */
	public String QueryVersion(String param)
	{
		logger.warn("servicename[QueryVersion]，调用端IP[{}],入参为：{}",getClientInfo(), param);
		return new HqosQueryVersionServiceImpl().work(param);
	}
	
	/**
	 * 3.1.3.	查询宽带状态
	 * @param param
	 * @return
	 */
	public String QueryNetStatus(String param)
	{
		logger.warn("servicename[QueryNetStatus]，调用端IP[{}],入参为：{}",getClientInfo(), param);
		return new HqosQueryNetStatusServiceImpl().work(param);
	}
	
	/**
	 * 3.2.	第二通道状态查询接口
	 * @param param
	 * @return
	 */
	public String HQoSTwoStatus(String param)
	{
		logger.warn("servicename[HQoSTwoStatus]，调用端IP[{}],入参为：{}",getClientInfo(), param);
		return new HqosTwoStatusServiceImpl().work(param);
	}
	
	/**
	 * 3.3.	第二通道保障开通接口
	 * @param param
	 * @return
	 */
	public String OpenHQoS(String param)
	{
		logger.warn("servicename[OpenHQoS]，调用端IP[{}],入参为：{}",getClientInfo(), param);
		return new OpenHqosServiceImpl().work(param);
	}
	
	
	/**
	 * 3.4.	第二通道保障更新接口
	 * @param param
	 * @return
	 */
	public String UpdateHQoS(String param)
	{
		logger.warn("servicename[UpdateHQoS]，调用端IP[{}],入参为：{}",getClientInfo(), param);
		return new UpdateHqosServiceImpl().work(param);
	}
	
	/**
	 * 3.5.	第二通道保障取消接口
	 * @param param
	 * @return
	 */
	public String DeleteHQoS(String param)
	{
		logger.warn("servicename[DeleteHQoS]，调用端IP[{}],入参为：{}",getClientInfo(), param);
		return new DeleteHqosServiceImpl().work(param);
	}
	
	
	/**
	 * 3.7.	保障配置结果查询接口
	 * @param param
	 * @return
	 */
	public String HQoSTwoCfgQuery(String param)
	{
		logger.warn("servicename[HQoSTwoCfgQuery]，调用端IP[{}],入参为：{}",getClientInfo(), param);
		return new HqosTwoCfgQueryServiceImpl().work(param);
	}
	
	
	
	private String getClientInfo()
	{
		String clientIp = null;
		MessageContext mc = MessageContext.getCurrentContext();
		HttpServletRequest request = (HttpServletRequest) mc
				.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		clientIp = request.getRemoteAddr();
		return clientIp;
	}
}