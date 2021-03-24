
package com.huawei.northinterface.operation.service;

import com.huawei.base.model.Order;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.WSClientUtil;
import com.linkage.itms.os.bio.OperationDistributeBIOJllt;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.StringReader;
import java.net.URL;

/**
 * 北向接口转发接口入口
 * 
 * @author fanjm (Ailk No.35572)
 * @version 1.0
 * @since 2017年3月17日
 */
public class OperationDistributeServiceJllt
{
	private static Logger logger = LoggerFactory.getLogger(OperationDistributeServiceJllt.class);
	// 接入工单开户
	private static String ACCESS_1 = "20_1";
	// 接入工单销户
	//private static String ACCESS_3 = "20_3";

	/**
	 * 大唐服开转发接口入口
	 */
	public int dealOrder(Order order)
	{
		logger.warn("dealOrder==>吉林方法开始{}", new Object[] { order.toString() });
		// 异步发送至亚信平台--发送url在配置文件中进行配置
		
		int res = -1;
		int asiaRes = 0;
		if("1".equals(Global.is_SendToOldRMS)){
			Global.G_SendThreadPool.execute(new AsiainfoDealService(order));
			logger.warn("将用户[{}]工单发向华为平台,时间为 [{}],工单内容为:[{}]", order.getAd_userid(),
					System.currentTimeMillis(),order.toString());
			res = callRemoteService(Global.ESERVER_OLD_URL, order, "dealOrder");
			logger.warn("用户[{}]工单得到华为平台返回消息,返回结果为[{}]时间为 [{}]", order.getAd_userid(),
					res, System.currentTimeMillis());
		}else{
			asiaRes = asiainfoDeal(order);
		}
		
		if("1".equals(Global.is_SendToOldRMS)){
			return res;
		}
		else{
			return asiaRes;
		}
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
				logger.warn("返回给大唐{}",StringUtil.getIntegerValue(toEsverXml));
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
					String toEsverXml20 = OperationDistributeBIOJllt.parseToXML(order,ACCESS_1);
					logger.warn("发往工单模块的开户XML = {}", toEsverXml20);
					// 接入工单返回
					String esverRtn20 = WSClientUtil.callRemoteService(url, toEsverXml20,method);
					logger.warn("工单模块返回开户工单处理结果Xml={}", esverRtn20);
					// 接入工单返回码
					String esverRtn20Code = parse(esverRtn20, "resultCode");
					// 接入工单返回成功
					if (!"000".equals(esverRtn20Code)){
						logger.warn("返回给大唐{}", -6);
						return -6;
					}
				}
			}
			
			// 具体业务返回
			String esverRtn = WSClientUtil.callRemoteService(url, toEsverXml, method);
			logger.warn("工单模块返回业务工单处理结果Xml={}", esverRtn);
			// 返回码
			String esverRtnCode = parse(esverRtn, "resultCode");
			if ("000".equals(esverRtnCode)){
				logger.warn("返回给大唐{}", 1);
				return 1;
			}
			
			logger.warn("返回给大唐{}", -6);
			return -6;
		}
		catch (Exception e)
		{
			logger.error("大唐服开转发接口异常{}", ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
		return -6;
	}

	/**
	 * 发送webService
	 */
	public static int callRemoteService(String url, Object inParam, String method)
	{
		int returnParam = -1;
		Service service = new Service();
		Call call = null;
		try
		{
			call = (Call) service.createCall();
			QName qn = new QName("urn:BeanService", "Order");
			call.registerTypeMapping(Order.class, qn, new BeanSerializerFactory(
					Order.class, qn), new BeanDeserializerFactory(Order.class, qn));
			call.setOperationName(new QName(url, method));
			call.setTargetEndpointAddress(new URL(url));
			call.setTimeout(1000 * 5);
			returnParam = ((Integer) call.invoke(new Object[] { inParam })).intValue();
		}
		catch (Exception e)
		{
			logger.warn("再试一次：");
			logger.error("调用接口错误:{}", ExceptionUtils.getStackTrace(e));
			try
			{
				if(call != null){
					returnParam = (Integer) call.invoke(new Object[] { inParam });
				}else {
					logger.error("再一次调用，call为空");
				}

			}
			catch (Exception e1)
			{
				logger.warn("第二次失败");
				logger.error("调用接口错误,第二次失败:{}", ExceptionUtils.getStackTrace(e1));
			}
		}
		return returnParam;
	}

	/**
	 * 获取xml跟节点下的参数值
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
			return null;
		}
	}
}
