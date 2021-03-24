package com.linkage.itms.dispatch.service;

import com.linkage.itms.Global;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.dao.DecayServInfoDAO;
import com.linkage.itms.dispatch.obj.DecayServInfoCheck;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author xiangzl (Ailk No.)
 * @version 1.0
 * @since 2013-12-10
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DecayServInfoService implements IService
{
	private static Logger logger = LoggerFactory.getLogger(DecayServInfoService.class);
	/** SuperGather CORBA */
	private SuperGatherCorba sgCorba;

	@Override
	public String work(String inXml)
	{
		logger.warn("DecayServInfoService:inXml({})", inXml);
		final DecayServInfoCheck binder = new DecayServInfoCheck(inXml);
		
		if (false == binder.check())
		{
			logger.error("验证未通过，返回：" + binder.getReturnXml());
			return null;
		}
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				doWork(binder);
			}}).start();
		return null;
	}
	
	private void doWork(DecayServInfoCheck binder)
	{
		String resultDesc = "成功";
		try
		{
			Thread.sleep(Global.jxFtthSleepTime  * 1000L);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			// Restore interrupted state...     
			Thread.currentThread().interrupt();
		}
		
		DecayServInfoDAO infoDao = new DecayServInfoDAO();
		//查询业务下发状态
		Map<String,String> deviceInfo = infoDao.queryDeviceInfo(binder.getLoid(), binder.getGwType());
		ArrayList<HashMap<String,String>> servInfo = infoDao.queryServResult(binder.getLoid(), binder.getGwType());
		Map<String,String> decayMap = null;
//		String deviceId = StringUtil.getStringValue(deviceInfo, "device_id", "");
//		String accessType = StringUtil.getStringValue(deviceInfo, "access_style_id", "");
//		String user_id = StringUtil.getStringValue(deviceInfo, "user_id", "");
		
		//调用采集模块采集，
		/*sgCorba = new SuperGatherCorba();
		int rsint = sgCorba.getCpeParams(deviceId, 0, 1);
		// 采集失败
		if (rsint != 1)
		{
			 logger.warn("[{}]getData sg fail", deviceId);
			 resultDesc = "设备采集失败";
		}
		// 采集成功获取需要的信息
		else
		{
			//采集成功，更新结果
			infoDao.updateTestResult(user_id);
			
			if("2".equals(accessType))
			{//LAN
				infoDao.queryDevWireInfo(deviceId);
			}
			else if("3".equals(accessType))
			{//EPON
				decayMap = infoDao.queryPONInfo(deviceId);
		     }
			else if("4".equals(accessType))
			{//GPON
				decayMap = infoDao.queryPONInfo(deviceId);
			}
		}*/
		// 发送结果给对端系统
		try
		{
			final String endPointReference = Global.jxTestFtthUrl;
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));
			QName qn = new QName(endPointReference, Global.jxTestFtthMethod);
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { binder.getReturnXml(
					resultDesc, deviceInfo, servInfo, decayMap) });
			logger.warn(returnParam);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
