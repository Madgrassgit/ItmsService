package com.huawei.northinterface.operation.service;

import java.io.StringReader;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.base.model.Order;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.WSClientUtil;
import com.linkage.itms.os.bio.OperationDistributeBIOJllt;

public class AsiainfoDealService  implements Runnable
{
	private static Logger logger = LoggerFactory
			.getLogger(AsiainfoDealService.class);
	// 接入工单开户
	private static String ACCESS_1 = "20_1";
	// 接入工单销户
	private static String ACCESS_3 = "20_3";
	private Order order;
	public AsiainfoDealService(Order order)
	{
		this.order = order;
	}
	@Override
	public void run() {
		logger.warn("将用户[{}]工单发向亚信平台,时间为 [{}]", order.getAd_userid(),System.currentTimeMillis());
		int asiainfoDeal = asiainfoDeal(order);
		logger.warn("将用户[{}]工单的到亚信平台返回,结果:[{}],时间为 [{}]", order.getAd_userid(),asiainfoDeal,System.currentTimeMillis());
	}
	
	private int asiainfoDeal(Order order)
	{
		try
		{
			// 工单模块 WebService接口地址
			String url = Global.ESERVER_URL;
			String method = "call";
			String toEsverXml = OperationDistributeBIOJllt.parseToXML(order, "");
			logger.warn("发往工单模块的业务XML = {}", toEsverXml);
			if (("-3".equals(toEsverXml)) || ("-4".equals(toEsverXml))
					|| ("-6".equals(toEsverXml)) || ("-2".equals(toEsverXml)))
			{
				logger.warn("返回给大唐{}",
						Integer.valueOf(StringUtil.getIntegerValue(toEsverXml)));
				return StringUtil.getIntegerValue(toEsverXml);
			}
			String order_type = order.getOrder_Type();
			// 开户业务需要先发接入工单
			if (("wband-Z".equals(order_type)) || ("iptv-Z".equals(order_type))
					|| ("voip-Z".equals(order_type)))
			{
				// 带有browserURL1参数的为stb业务，不需要发开户工单20
				if (!order.getVector_argues().contains("browserURL1"))
				{
					// 接入工单 xml 发送工单模块
					String toEsverXml20 = OperationDistributeBIOJllt.parseToXML(order,
							ACCESS_1);
					logger.warn("发往工单模块的开户XML = {}", toEsverXml20);
					// 接入工单返回
					String esverRtn20 = WSClientUtil.callRemoteService(url, toEsverXml20,
							method);
					logger.warn("工单模块返回开户工单处理结果Xml={}", esverRtn20);
					// 接入工单返回码
					String esverRtn20Code = parse(esverRtn20, "resultCode");
					// 接入工单返回成功
					if (!"000".equals(esverRtn20Code))
					{
						logger.warn("返回给大唐{}", Integer.valueOf(-6));
						return -6;
					}
				}
			}
			// 具体业务返回
			String esverRtn = WSClientUtil.callRemoteService(url, toEsverXml, method);
			logger.warn("工单模块返回业务工单处理结果Xml={}", esverRtn);
			// 返回码
			String esverRtnCode = parse(esverRtn, "resultCode");
			if ("000".equals(esverRtnCode))
			{
				logger.warn("返回给大唐{}", Integer.valueOf(1));
				return 1;
			}
			logger.warn("返回给大唐{}", Integer.valueOf(-6));
			return -6;
		}
		catch (Exception e)
		{
			logger.error("大唐服开转发接口异常{}", ExceptionUtils.getStackTrace(e));
		}
		return -6;
	}
	/**
	 * 获取xml跟节点下的参数值
	 * 
	 * @param xml
	 *            xml
	 * @param paramName
	 *            节点名
	 * @return 字符串
	 */
	public static String parse(String xml, String paramName)
	{
		SAXReader reader = new SAXReader();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(xml));
			Element root = document.getRootElement();
			return root.elementTextTrim(paramName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
