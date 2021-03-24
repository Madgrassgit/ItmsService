
package com.linkage.itms.ids.obj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.ids.wsdl.InterfaceService_PortType;
import com.linkage.itms.ids.wsdl.InterfaceService_ServiceLocator;

/**
 * 处理数据线程类
 * 
 * @author yinlei
 * @version 1.0
 * @since 2012-5-28 下午02:46:15
 * @category com.linkage.itms.customer.data2reportForHB
 * @copyright 南京联创科技 网管科技部
 */
public class ProccesDataThread implements Runnable
{

	/** log */
	private static final Logger logger = LoggerFactory.getLogger(ProccesDataThread.class);

	private String param;
	private String loid;

	public ProccesDataThread(String param,
			String loid)
	{
		this.param = param;
		this.loid = loid;
	}

	@Override
	public void run()
	{
		String returnParam = "";
		try
		{
			InterfaceService_ServiceLocator locator=new InterfaceService_ServiceLocator();
			InterfaceService_PortType type=locator.getInterfaceServiceSOAP();
			returnParam =type.interfaceService(param);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			logger.warn("[{}]  调用测速平台接口失败", new Object[] { loid });
		}
		logger.warn("[{}]  调用测速平台接口返回结果 :{}", new Object[] { loid, returnParam });
	}
}
