package com.linkage.itms.jms;

import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.commons.xml.XML2Bean;
import com.linkage.itms.Global;
import com.linkage.itms.jms.dao.ProDao;
import com.linkage.itms.mq.servinfo.thread.DevServinfoDealThread;

/**
 * 
 * @author xiangzl (Ailk No.)
 * @version 1.0
 * @since May 16, 2013
 * @category com.linkage.itms.jms
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ProcessRusultThread implements Runnable
{
	/** log */
	private static final Logger log = LoggerFactory.getLogger(ProcessRusultThread.class);
	
	/**
	 * //针对mq过来的消息
	 */
	private String message = null;
	private static final String QOS_SERVTYPEID = "<servTypeId>46</servTypeId>";
	private static final String DEV_SERVINFO = "dev.servinfo";
	
	private DeviceInfo obj = null;
	
	public void setMessage(String message)
	{
		log.debug("setMessage({})",message);
		this.message = message;
	}

	@Override
	public void run()
	{
		// 判断消息体是否为空
		if(!StringUtil.IsEmpty(this.message)){
			log.warn("接收到dev.servinfo消息:{}",this.message);
			if(this.message.contains(QOS_SERVTYPEID)){
				log.warn("包含46联通高品质业务,开始处理-");
				try
				{
					DevServinfoDealThread thread = new DevServinfoDealThread();
					thread.setMessage(message);
					thread.setTopic(DEV_SERVINFO);
					Global.G_SendThreadPool.execute(thread);
				}catch(Exception e){
					log.error("解析出错:{}",ExceptionUtils.getStackTrace(e));
					return ;
				}
			}
			return;
		}
		if(Global.AHLT.equals(Global.G_instArea)){
			log.warn("接收到dev.servinfo消息为空:{}",this.message);
			return;
		}else{
			callback();
		}
		
	}

	/**
	 * 原有方法提取
	 */
	private void callback() {
		//将mq消息转换为obj
		mqMessageHandle();
		//如果设备id，和业务类型为空，不做
		if(obj.getDevId()==null || obj.getServTypeId() == null)
		{
			return;
		}
		ProDao dao = new ProDao();
		if("cq_dx".equals(Global.G_instArea)){
			boolean hasOtherServUndo = dao.hasUserOtherServInfoUndo(obj.getDevId(), obj.getServTypeId(), obj.getGwType());
			if(hasOtherServUndo){
				log.debug("[{}]还有其他业务未做，暂不回调接口返回工厂复位结果");
			}else{
				List<HashMap<String,String>> resutlMapList = dao.getFactoryResetReturnDiagOpid(obj.getDevId(), "");
				if(null != resutlMapList && resutlMapList.size() > 0){
					for(HashMap<String,String> resultMap : resutlMapList){
						long op_id = StringUtil.getLongValue(resultMap, "op_id");
						StringBuffer inParam = new StringBuffer();
						inParam.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
						inParam.append("<root>\n");
						inParam.append("	<op_id>" + op_id + "</op_id>\n");
						inParam.append("	<result>" + obj.getOpenStatus() + "</result>\n");
						inParam.append("	<err_msg>" + obj.getOpenStatus() + "</err_msg>\n");
						inParam.append("</root>\n");
					}
				}
			}
		}else{
			
			if("14".equals(obj.getServTypeId()))
			{
				List<HashMap<String,String>> list = dao.getAllVoipPhone(obj.getDevId(), obj.getGwType());
				for(HashMap<String,String> mapPhone : list)
				{
					String voipPhone = mapPhone.get("voip_phone");
					HashMap<String,String> map = dao.getUserServInfoByHistory(obj.getDevId(), obj.getServTypeId(), obj.getGwType(), voipPhone);
					if(map != null)
					{
						log.warn("finish:"+map.get("orderid"));
						if (!StringUtil.IsEmpty(map.get("orderid")))
						{
							String resrlt = sendWebServiceToSelf(map.get("orderid"), map, obj);
							dao.updateBssSheet(map.get("loid"), map.get("servUsername"), map
										.get("serv_type_id"), obj, resrlt, map.get("orderid"));
						}
					}
				}
			}
			else 
			{//不是语音业务直接查询业务用户表的订单id
				HashMap<String, String> map = dao.getUserServInfo(obj.getDevId(),obj.getServTypeId(),obj.getGwType());
				
				if(map != null)
				{
					log.warn("finish:"+map.get("orderid"));
					if (!StringUtil.IsEmpty(map.get("orderid")) && !"AAA".equals(map.get("orderid")))
					{
						if (!"FROMWEB-0000002".equals(map.get("orderid")))
						{

							String resrlt = sendWebServiceToSelf(map.get("orderid"), map, obj);
							dao.updateBssSheet(map.get("loid"), map.get("servUsername"), map
									.get("serv_type_id"), obj, resrlt, map.get("orderid"));
						}
					}
				}
			}
		}
	}

	private String sendWebService(String orderId, HashMap<String, String> map, DeviceInfo obj2)
	{
		String returnParam = " ";
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>	\n");
			inParam.append("<root>										\n");
			inParam.append("	<CmdID>"+orderId+"</CmdID >						\n");
			inParam.append("	<Loid>"+map.get("loid")+"</Loid>					\n");
			inParam.append("	<ServiceType>"+map.get("serv_type_id")+"</ServiceType>						\n");
			inParam.append("	<ServiceUsername>"+map.get("servUsername")+"</ServiceUsername>				\n");
			inParam.append("	<OpenStatus>"+obj2.getOpenStatus()+"</OpenStatus>				\n");
			inParam.append("	<CityId>"+map.get("city_id")+"</CityId>				\n");
			inParam.append("</root>										\n");
			log.warn("send WEBservice xml :"+inParam.toString());
			final String endPointReference = Global.G_ITMS_FINISH_URL;  
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

//			QName qn = new QName(endPointReference, "call");
//			call.setOperationName(Global.G_ITMS_SERV_METHOD);
			
			call.setOperationName(new QName("http://call.gtms.module.ailk.com",Global.G_ITMS_SERV_METHOD));



			log.warn(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnParam;
	}
	
	private String sendWebServiceToSelf(String orderId, HashMap<String, String> map,
			DeviceInfo obj2)
	{
		String returnParam = " ";
		try
		{
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			inParam.append("<root>\n");
			inParam.append("	<CmdID>" + orderId + "</CmdID>\n");
			inParam.append("	<Loid>" + map.get("loid") + "</Loid>\n");
			inParam.append("	<ServiceType>" + map.get("serv_type_id")
					+ "</ServiceType>\n");
			inParam.append("	<ServiceUsername>" + map.get("servUsername")
					+ "</ServiceUsername>\n");
			inParam.append("	<OpenStatus>" + obj2.getOpenStatus() + "</OpenStatus>\n");
			inParam.append("	<CityId>" + map.get("city_id") + "</CityId>\n");
			inParam.append("</root>\n");
			log.warn("send WEBservice xml :" + inParam.toString());
			String endPointReference = Global.G_ITMS_FINISH_URL;
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));
			call.setOperationName(Global.G_ITMS_SERV_METHOD);
			returnParam = (String) call.invoke(new Object[] { inParam.toString() });
			log.warn(returnParam);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return returnParam;
	}

	private void mqMessageHandle()
	{
		XML2Bean x2b = new XML2Bean(this.message);
		this.obj = (DeviceInfo) x2b.getBean("ServInfo", DeviceInfo.class);
	}
	
	public static void main(String[] args)
	{
		String mem = "<ServInfo> <devId>123</devId> <servTypeId>20</servTypeId><servName>12345</servName><servStatus>2</servStatus><openStatus>1</openStatus></ServInfo>";
		ProcessRusultThread p  = new ProcessRusultThread();
		p.message = mem;
		p.mqMessageHandle();
		System.out.println(p.obj.getDevId());
		System.out.println(p.obj.getOpenStatus());
		System.out.println(p.obj.getServName());
	}
	
}
