package com.linkage.itms.dispatch.service;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.base.model.Order;
import com.huawei.northinterface.operation.service.OperationDistributeServiceJllt;

/**
 * 吉林联通工单接口测试工具
 * 原需要用soup工具发对象给北向接口NorthInterface，待其处理后再发工单给工单接口EServer4WS
 * 先开发新接口，页面发xml格式，接口封装成对象直接调北向接口NorthInterface
 */
public class TestServerJLLTService 
{
	private static Logger logger = LoggerFactory.getLogger(TestServerJLLTService.class);
	
	public String work(String inParam)
	{
		logger.warn("TestServerJLLTService.work inParam:" + inParam);
		String result="-100";
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {
			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();

			Order order=new Order();
			order.setAd_account(root.elementTextTrim("ad_account"));
			order.setAd_userid(root.elementTextTrim("ad_userid"));
			order.setArea_code(root.elementTextTrim("area_code"));
			order.setDeviceType(root.elementTextTrim("deviceType"));
			order.setOrder_LSH(root.elementTextTrim("order_LSH"));
			order.setOrder_No(root.elementTextTrim("order_No"));
			order.setOrder_Type(root.elementTextTrim("order_Type"));
			order.setService_code(root.elementTextTrim("service_code"));
			order.setUser_name(root.elementTextTrim("user_name"));
			order.setUser_Type(root.elementTextTrim("user_Type"));
			order.setVector_argues(root.elementTextTrim("vector_argues"));
			
			result=new OperationDistributeServiceJllt().dealOrder(order)+"";
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			return result;
		}
		
		return result;
	}
}
