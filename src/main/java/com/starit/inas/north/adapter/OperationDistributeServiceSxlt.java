
package com.starit.inas.north.adapter;

import java.io.StringReader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.holders.IntHolder;
import javax.xml.rpc.holders.StringHolder;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.WSClientUtil;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.gsdx.beanObj.Para;
import com.linkage.itms.dispatch.gsdx.beanObj.UserIndex;
import com.linkage.itms.dispatch.service.TestSpeedSXLTService;
import com.linkage.itms.dispatch.sxlt.ServiceChangeService;
import com.linkage.itms.obj.sxlt.Order;
import com.linkage.itms.os.bio.OperationDistributeBIOSxlt;

/**
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-8-17
 * @category com.linkage.itms.os.main
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class OperationDistributeServiceSxlt
{

	private static Logger logger = LoggerFactory
			.getLogger(OperationDistributeServiceSxlt.class);
	// 接入工单开户
	private static String ACCESS_1 = "20_1";
	private static SAXReader reader = new SAXReader();
	private long id = Math.round(Math.random() * 1000000000) + new DateTimeUtil().getLongTime();

	public static void main(String[] args)
	{
		OperationDistributeServiceSxlt order = new OperationDistributeServiceSxlt();
		order.processWorkOrder("<root><interfacemsg><cust_info><order_No>20190925162516938242</order_No><order_LSH/><order_Time>2019-09-04 09:09:11</order_Time><order_Remark/><order_Type>cpe-C</order_Type><device_ID>90834B-YUNGA9000FD0</device_ID><Ad_password/><user_name>刘茜</user_name><user_address>太航苑48号楼6单元303</user_address><service_code>cpe</service_code><area_code>0351</area_code><contact_person>刘茜</contact_person><order_kind>SG</order_kind><Time_limit/><vector_argues></vector_argues><order_status/><subArea_code/><devicetype>e8-b</devicetype><cardNo/><cardKey/><protocolType>IPV4</protocolType><user_Type/><device_wan/></cust_info></interfacemsg></root>", new StringHolder());
	}
	/**
	 * 大唐服开转发接口入口
	 * 
	 * @param order
	 * @return
	 */
	public void processWorkOrder(String xml, StringHolder processResponse)
	{
		new RecordLogDAO().recordLog(id, xml, "processWorkOrder");
		OperationDistributeBIOSxlt bio = new OperationDistributeBIOSxlt();
		Order order = bio.parseXMLToOrder(xml);

		if(null == order){
			logger.warn("[{}]校验失败(转order)，返回给大唐{}",new Object[]{-6});
			bio.setResult("-6");
			processResponse.value = bio.getReturnXml();
			return;
		}
		logger.warn("dealOrder==>方法开始{}", new Object[] { order.toString() });
		try
		{
			// 工单模块 WebService接口地址
			String url = Global.ESERVER_URL;
			String method = "call";
			
			//拆解同时传多种业务场景
			Order[] orders = bio.parseMultiXML(order);
			if(null == orders || orders.length == 0){
				//logger.warn("[{}]校验失败（拆解同时传多种业务场景），返回给大唐{}",new Object[]{order.getDevice_ID(), -6});
				bio.setResult("1");
				processResponse.value = bio.getReturnXml();
				return;
			}
			
			String[] toEsverXmls;
			if(order.getOrder_Type().contains("cpe-C") || order.getOrder_Type().contains("cpe-G")){
				toEsverXmls = new String[orders.length + 1];
			}
			else{
				toEsverXmls = new String[orders.length];
			}
			boolean isStb = false;
			String flag = "3";//1:第一次cpe-C 2：第二次cpe-C 3:其他业务
			//cpe-C/cpe-G会发2个，stb和光猫
			
			for(int i=0;i<toEsverXmls.length;i++){
				String toEsverXml = "";
				if(("1".equals(flag))||("cpe-C".equals(orders[i].getOrder_Type()) || "cpe-G".equals(orders[i].getOrder_Type()))){
					if("3".equals(flag)){
						flag = "1";
						toEsverXml = bio.parseToXML(orders[i], "",flag);
					}
					else if("1".equals(flag)){
						flag = "2";
						toEsverXml = bio.parseToXML(orders[i-1], "",flag);
					}
				}
				else{
					flag = "3";
					toEsverXml = bio.parseToXML(orders[i], "",flag);
				}
				
				if (("-3".equals(toEsverXml)) || ("-4".equals(toEsverXml))
						|| ("-6".equals(toEsverXml)) || ("-2".equals(toEsverXml)))
				{
					logger.warn("[{}]校验失败（解析具体业务参数），返回给大唐{}",new Object[]{order.getDevice_ID(), Integer.valueOf(StringUtil.getIntegerValue(toEsverXml))});
					bio.setResult(toEsverXml);
					processResponse.value = bio.getReturnXml();
					return;
				}
				toEsverXmls[i] = toEsverXml;
				
				if("stb".equals(order.getDeviceType()) || ("iptv".equals(order.getService_code()) && (order.getVector_argues().contains("userID") || order.getVector_argues().contains("userIDPwd")))){
					isStb = true;
				}
			}
				
			
			String order_type = order.getOrder_Type();
			// 开户业务需要先发接入工单,机顶盒不处理
			if (!isStb && (order_type.contains("wband-Z") || order_type.contains("iptv-Z") || order_type.contains("voip-Z")))
			{
				// 接入工单 xml 发送工单模块
				String toEsverXml20 = bio.parseToXML(order,ACCESS_1,"");
				logger.warn("[{}]发往工单模块的接入XML = {}", new Object[]{order.getDevice_ID(), toEsverXml20});
				// 接入工单返回
				String esverRtn20 = WSClientUtil.callRemoteService(url, toEsverXml20,
						method);
				logger.warn("[{}]工单模块返回接入工单处理结果Xml={}", new Object[]{order.getDevice_ID(), esverRtn20});
				// 接入工单返回码
				String esverRtn20Code = parse(esverRtn20, "resultCode");
				// 接入工单返回成功
				if (!"000".equals(esverRtn20Code))
				{
					logger.warn("[{}]接入工单发送失败，返回给大唐{}", new Object[]{order.getDevice_ID(), Integer.valueOf(-6)});
					bio.setResult("-6");
					processResponse.value = bio.getReturnXml();
					return;
				}
			}
			
			int servRtn = 1;
			flag = "3";//1:第一次cpe-C 2：第二次cpe-C 3:其他业务
			if("cpe-G".equals(order.getOrder_Type())){
				servRtn = -1;
			}
			for(int i=0;i<toEsverXmls.length;i++){
				String toEsverXml = toEsverXmls[i];
				Order thisOrder = new Order();
				
				if("1".equals(flag)||("cpe-C".equals(orders[i].getOrder_Type()) || "cpe-G".equals(orders[i].getOrder_Type()))){
					if("3".equals(flag)){
						flag = "1";
						thisOrder = orders[i];
					}
					else if("1".equals(flag)){
						flag = "2";
						thisOrder = orders[i-1];
					}
				}
				else{
					flag = "3";
					thisOrder = orders[i];
				}
				
				logger.warn("[{}]发往工单模块的业务[{}]的XML = {}", new Object[]{order.getDevice_ID(),thisOrder.getOrder_Type(), toEsverXml});
				// 具体业务返回
				String esverRtn = WSClientUtil.callRemoteService(url, toEsverXml, method);
				logger.warn("[{}]工单模块返回业务[{}]工单处理结果Xml={}",new Object[]{order.getDevice_ID(),thisOrder.getOrder_Type(),  esverRtn});
				// 返回码
				String esverRtnCode = parse(esverRtn, "resultCode");
				
				if("cpe-G".equals(order.getOrder_Type())){
					if ("000".equals(esverRtnCode))
					{
						logger.debug("设备[{}]业务[{}]单成功，结果{}", new Object[]{order.getDevice_ID(), thisOrder.getOrder_Type(), 1});
						servRtn = 1;
					}
					else{
						logger.debug("设备[{}]业务[{}]单失败，结果{}", new Object[]{order.getDevice_ID(), thisOrder.getOrder_Type(), -6});
						bio.setErrorDesc(parse(esverRtn, "resultDes"));
					}
				}
				else{
					if ("000".equals(esverRtnCode))
					{
						logger.debug("设备[{}]业务[{}]单成功，结果{}", new Object[]{order.getDevice_ID(), thisOrder.getOrder_Type(), 1});
					}
					else{
						logger.debug("设备[{}]业务[{}]单失败，结果{}", new Object[]{order.getDevice_ID(), thisOrder.getOrder_Type(), -6});
						servRtn = -6;
						bio.setErrorDesc(parse(esverRtn, "resultDes"));
					}
				}
			}
			
			logger.warn("设备[{}],业务开通结果返回给大唐{}", new Object[]{order.getDevice_ID(), servRtn});
			bio.setResult(StringUtil.getStringValue(servRtn));
			
			processResponse.value = bio.getReturnXml();
			return;
		}
		catch (Exception e)
		{
			logger.error("[{}]转发模块处理异常{}", new Object[]{order.getDevice_ID(), e.getMessage()});
			e.printStackTrace();
		}
		bio.setResult("-6");
		processResponse.value = bio.getReturnXml();
		return;
	}
	

	 
	
	/**
	 * 发送webService
	 * 
	 * @param url
	 *            发送的url路径
	 * @param inParam
	 *            参数(obj)
	 * @param method
	 *            方法名
	 * @return 调用的方法结果
	 */
	public static int callRemoteService(String url, Object inParam, String method)
	{
		int returnParam = 1000;
		try
		{
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			QName qn = new QName("urn:BeanService", "Order");
			call.registerTypeMapping(Order.class, qn, new BeanSerializerFactory(
					Order.class, qn), new BeanDeserializerFactory(Order.class, qn));
			call.setOperationName(new QName(url, method));
			call.setTargetEndpointAddress(new URL(url));
			returnParam = ((Integer) call.invoke(new Object[] { inParam })).intValue();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		return returnParam;
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
	
	
	/**
	 *@描述 山西联通宽带修改密码接口
	 *@参数  [adAcount, LSHNo, orderType, newPassWord, result]
	 *@返回值  void
	 *@创建人  lsr
	 *@创建时间  2019/8/16
	 *@throws
	 *@修改人和其它信息
	 */
	public void ServiceChange(String adAcount, String LSHNo, String orderType, String newPassWord, IntHolder result,StringHolder result_desc){
		logger.warn("ServiceChange["+LSHNo+"]==>方法开始:adAcount[{}],LSHNo[{}],orderType[{}],newPassWord[{}]", new Object[] {adAcount, LSHNo, orderType, newPassWord});
		try {
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("LSHNo").addText(StringUtil.getStringValue(LSHNo));
			intRequest.addElement("adAcount").addText(StringUtil.getStringValue(adAcount));
			intRequest.addElement("orderType").addText(StringUtil.getStringValue(orderType));
			intRequest.addElement("newPassWord").addText(newPassWord);
			
			String resultXml = new ServiceChangeService("ServiceChange").work(intRequest.asXML());
			logger.warn("ServiceChange["+LSHNo+"]==>方法结束返回{}", new Object[] { resultXml });

			Document outDocument = reader.read(new StringReader(resultXml));
			Element outRoot = outDocument.getRootElement();
			result.value = StringUtil.getIntegerValue(outRoot.elementTextTrim("result_code"));
			result_desc.value = StringUtil.getStringValue(outRoot.elementTextTrim("result_desc"));
		} catch (Exception e) {
			logger.error("ServiceChange["+LSHNo+"] Exception occured!", e);
		}
	}
	
	
	/**
	 * 测速
	 * @param user
	 * @param testType
	 * @param paras
	 * @param res
	 */
	public void diagnoseTest(UserIndex user, String testType, Para[] paras, IntHolder res){
    	String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime()) + StringUtil.getStringValue((int)(Math.random()*1000));  
    	logger.warn("diagnoseTest["+LSHNo+"]==>方法开始UserIndex user[{}],testType:[{}],paras.length:[{}]", new Object[] { user,testType,paras.length});;
    	try {
    		for(int i=0;i<paras.length;i++){
    			logger.warn(i+":"+paras[i].getName()+", "+ paras[i].getValue());
    		}
    		Document inDocument = DocumentHelper.createDocument();
    		Element root = inDocument.addElement("root");
    		Element intRequest = root.addElement("Param");
    		intRequest.addElement("op_id").addText(LSHNo);
    		intRequest.addElement("type").addText(StringUtil.getStringValue(user.getType()));
    		intRequest.addElement("index").addText(user.getIndex());
    		for(Para para : paras){
    			intRequest.addElement(para.getName()).addText(para.getValue());
    		}
    		if("SpeedTest".equals(testType)){
    			String xml = new TestSpeedSXLTService().work(inDocument.asXML());
    			SAXReader reader = new SAXReader();
    			Document document = null;
    			try {
    				document = reader.read(new StringReader(xml));
    				Element rootRes = document.getRootElement();
    				res.value = StringUtil.getIntegerValue(rootRes.elementTextTrim("RstCode"));
    			} catch (Exception e) {
    				e.printStackTrace();
    				res.value = -1000;
    			}
    		}
    		logger.warn("diagnoseTest["+LSHNo+"]==>方法结束,返回{}",res.value);
    	} catch (Exception e) {
    		e.printStackTrace();
    		logger.error("diagnoseTest["+LSHNo+"] Exception occured!", e);
    	}
    }
	
}