
package com.linkage.stbms.radius.bio;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.itv.main.StbServGlobals;

/**
 * 与AAA系统（Radius）交互接口处理类
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-6-24
 * @category com.linkage.stbms.radius.bio
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RadiusBIO
{

	private static final Logger logger = LoggerFactory.getLogger(RadiusBIO.class);
	/**
	 * 接口规范要求请求第一个参数为固定值
	 */
	private static final String FIXED_REQUEST_ID = "ID00010";
	/**
	 * Radius系统接口调用方法名
	 */
	private static final String RADIUS_METHOD_NAME = "serviceOperation";
	/**
	 * Radius返回失败报文，当调用Radius服务发生异常时，返回该失败报文
	 */
	private static final String RADIUS_FAILED_RESPONSE = "ID00010~1";
	/**
	 * cache
	 */
	private Call call = null;

	/**
	 * 机顶盒零配置接口
	 * 
	 * @param request
	 *            requestId(请求参数,固定值：ID00010)~ipAddress(IP地址)
	 * @return 成功：ID00010~0~端口信息~上网帐号~mac地址~上/下线时间~目前帐号IP地址<br>
	 *         失败：ID00010~1
	 */
	public String getItvCfg(String request)
	{
		String result = null;
		try
		{
			logger.warn("start getItvCfg({})", request);
			String radiusRequest = analize(request);
			logger.info("send request[{}] to radius by webservice.", radiusRequest);
			result = (String) getCall().invoke(new String[] { radiusRequest });
		}
		catch (Exception e)
		{
			// 如果发生任何异常，则向客户端返回失败信息
			result = RADIUS_FAILED_RESPONSE;
			logger.error("getItvCfg({}) from radius error", request);
			logger.error(e.getMessage(), e);
		}
		logger.warn("end getItvCfg, and response[{}]", result);
		return result;
	}

	private String analize(String request)
	{
		if (!StringUtil.IsEmpty(request))
		{
			String[] params = request.split("~");
			if (params.length >= 2)
			{
				String ipAddress = params[1];
				return FIXED_REQUEST_ID + "~" + ipAddress;
			}
		}
		// 请求参数中没有IP信息，则不传递IP地址
		return FIXED_REQUEST_ID + "~";
	}

	private Call getCall() throws MalformedURLException, ServiceException
	{
		if (call == null)
		{
			String radiusURL = StbServGlobals.getLipossProperty("RadiusWSUrl.URL");
			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(radiusURL));
			call.setOperation(RADIUS_METHOD_NAME);
			logger.info("get webservice client by url[{}] and method ["
					+ RADIUS_METHOD_NAME + "].", radiusURL);
			this.call = call;
		}
		return call;
	}
}
