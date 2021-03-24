package com.linkage.itms.os.main;

import java.io.StringReader;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.WSClientUtil;
import com.linkage.itms.obj.Order;
import com.linkage.itms.os.bio.OperationDistributeBIO;


/**
 * 北向接口转发接口入口
 * @author fanjm (Ailk No.35572)
 * @version 1.0
 * @since 2017年3月17日
 *
 */
public class OperationDistributeService
{
  private static Logger logger = LoggerFactory.getLogger(OperationDistributeService.class);

  //接入工单开户
  private static String ACCESS_1 = "20_1";
  
  //接入工单销户
  private static String ACCESS_3 = "20_3";

  
  /**
   * 大唐服开转发接口入口
   * @param order
   * @return
   */
  public static int dealOrder(Order order)
  {
	  
    logger.warn("dealOrder==>方法开始{}", new Object[] { order.toString() });
    try
    {
      //工单模块 WebService接口地址
      String url = Global.ESERVER_URL;

      String method = "call";

      String toEsverXml = OperationDistributeBIO.parseToXML(order, "");
      logger.warn("发往工单模块的业务XML = {}", toEsverXml);
      if (("-3".equals(toEsverXml)) || ("-4".equals(toEsverXml)) || ("-6".equals(toEsverXml)) || ("-2".equals(toEsverXml))) {
        logger.warn("返回给大唐{}", Integer.valueOf(StringUtil.getIntegerValue(toEsverXml)));
        return StringUtil.getIntegerValue(toEsverXml);
      }

      String order_type = order.getOrder_Type();
      
      //开户业务需要先发接入工单
      if (("wband-Z".equals(order_type)) || ("iptv-Z".equals(order_type)) || ("voip-Z".equals(order_type)))
      { 
    	//带有browserURL1参数的为stb业务，不需要发开户工单20
        if (!order.getVector_argues().contains("browserURL1"))
        {
          //接入工单 xml 发送工单模块
          String toEsverXml20 = OperationDistributeBIO.parseToXML(order, ACCESS_1);
          logger.warn("发往工单模块的开户XML = {}", toEsverXml20);

          //接入工单返回
          String esverRtn20 = WSClientUtil.callRemoteService(url, toEsverXml20, method);
          logger.warn("工单模块返回开户工单处理结果Xml={}", esverRtn20);
  
          //接入工单返回码
          String esverRtn20Code = parse(esverRtn20, "resultCode");
          //接入工单返回成功
          if (!"000".equals(esverRtn20Code)) {
            logger.warn("返回给大唐{}", Integer.valueOf(-6));
            return -6;
          }

        }

      }
      
      //具体业务返回
      String esverRtn = WSClientUtil.callRemoteService(url, toEsverXml, method);
      logger.warn("工单模块返回业务工单处理结果Xml={}", esverRtn);
       
      //返回码
      String esverRtnCode = parse(esverRtn, "resultCode");
      if ("000".equals(esverRtnCode)) {
        logger.warn("返回给大唐{}", Integer.valueOf(1));
        return 1;
      }

      logger.warn("返回给大唐{}", Integer.valueOf(-6));
      return -6;
    }
    catch (Exception e)
    {
      logger.error("大唐服开转发接口异常{}", e.getMessage());
      e.printStackTrace();
    }return -6;
  }

  public static void main(String[] args)
  {
    Order testWband1 = new Order();//宽带开户路由
    testWband1.setArea_code("石家庄市局");
    testWband1.setDeviceType("e8-b");
    testWband1.setDevice_ID("000005-F0407B8608C0");
    testWband1.setOrder_No("order0001");
    testWband1.setOrder_Type("wband-Z");
    testWband1.setVer("CNC.Y.002");
    testWband1.setOrder_kind("SG");
    testWband1.setService_code("wband");
//    testWband1.setVector_argues("OltFactory=243^wband_mode=1^wband_vlan=500^wband_name=031103742463@adsl^wband_password=sasas");
    testWband1.setVector_argues("OltFactory=243^wband_mode=1^wband_vlan=500^wband_name=username223^wband_password=password123^wband_speed=109");

    Order testWband0 = new Order();//宽带开户桥接
    testWband0.setArea_code("石家庄市局");
    testWband0.setDeviceType("");
    testWband0.setDevice_ID("000005-F0407B8608C0");
    testWband0.setOrder_No("order0001");
    testWband0.setOrder_Type("wband-Z");
    testWband0.setVer("CNC.Y.002");
    testWband0.setOrder_kind("SG");
    testWband0.setService_code("wband");
//    testWband0.setVector_argues("OltFactory=243^X_CU_LanInterface=InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1^wband_mode=0^wband_vlan=500");
    testWband0.setVector_argues("OltFactory=243^X_CU_LanInterface=InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1^wband_mode=0^wband_vlan=500^wband_name=username123^wband_password=password1^wband_speed=70");
    
    Order testWband2 = new Order();//宽带修改速率
    testWband2.setArea_code("石家庄市局");
    testWband2.setDeviceType("e8-b");
    testWband2.setDevice_ID("000005-F0407B8608C0");
    testWband2.setOrder_No("order0001");
    testWband2.setOrder_Type("wband-X");
    testWband2.setVer("CNC.Y.002");
    testWband2.setOrder_kind("SG");
    testWband2.setService_code("wband");
    testWband2.setVector_argues("wband_name=username223^wband_speed=101");
    
    Order testIPTV1 = new Order();//IPTV开户路由
    testIPTV1.setArea_code("石家庄市局 ");
    testIPTV1.setDeviceType("");
    testIPTV1.setDevice_ID("FFFFF-FFFFFFFFFFFF");
    testIPTV1.setOrder_No("order0003");
    testIPTV1.setOrder_Type("iptv-Z");
    testIPTV1.setVer("CNC.Y.002");
    testIPTV1.setOrder_kind("SG");
    testIPTV1.setService_code("iptv");
    testIPTV1.setVector_argues("OltFactory=243^iptv_mode=1^X_CU_MulticastVlan=2500^iptv_vlan=2501^iptv_name=031103756072@iptv^iptv_password=76701^destIPAddr1=10.0.0.0^destMask1=255.0.0.0");

    Order testIPTV2 = new Order();//IPTV开户桥接
    testIPTV2.setArea_code("清苑 ");
    testIPTV2.setDeviceType("e8-b");
    testIPTV2.setDevice_ID("FFFFF-FFFFFFFFFFFF");
    testIPTV2.setOrder_No("order0003");
    testIPTV2.setOrder_Type("iptv-Z");
    testIPTV2.setVer("CNC.Y.002");
    testIPTV2.setOrder_kind("SG");
    testIPTV2.setService_code("iptv");
    testIPTV2.setVector_argues("OltFactory=243^X_CU_LanInterface=InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2^X_CU_MulticastVlan=2500^iptv_vlan=2501");

    Order testVoip = new Order();//viop开户
    testVoip.setArea_code("邯郸市局");
    testVoip.setDeviceType("e8-b");
    testVoip.setDevice_ID("000005-F0407B8608C0");
    testVoip.setOrder_No("order0005");
    testVoip.setOrder_Type("voip-Z");
    testVoip.setVer("CNC.Y.002");
    testVoip.setOrder_kind("SG");
    testVoip.setService_code("voip");
//    testVoip.setVector_argues("OltFactory=261^SubnetMask=255.255.255.0^WANDefaultGateway=20.1.116.1^WANIPAddress=20.1.116.59^voip_EID=20.1.116.59^voip_MGCIP=20.0.2.1^voip_MGCPort=2944^voip_MG_Domain=20.1.116.59^voip_standbyMGCIP=20.0.1.1^voip_standbyMGCPort=2944^voip_vlan=3318^voip_MIDformat=DomainName");
    testVoip.setVector_argues("OltFactory=261^SubnetMask=255.255.255.0^WANDefaultGateway=20.1.116.1^WANIPAddress=20.1.116.59^voip_EID=20.1.116.59^voip_MGCIP=20.0.2.1^voip_MGCPort=2944^voip_MG_Domain=20.1.116.59^voip_standbyMGCIP=20.0.1.1^voip_standbyMGCPort=2944^voip_vlan=3318^voip_MIDformat=DomainName^voipPhone=05571111111");
    
    Order testWband1_3 = new Order();//宽带销户
    testWband1_3.setArea_code("石家庄市局 ");
    testWband1_3.setDeviceType("e8-b");
    testWband1_3.setDevice_ID("EEEEE-EEEEEEEEEEEE1");
    testWband1_3.setOrder_No("order0001");
    testWband1_3.setOrder_Type("wband-C");
    testWband1_3.setVer("CNC.Y.002");
    testWband1_3.setOrder_kind("SG");
    testWband1_3.setService_code("wband");
    testWband1_3.setVector_argues("wband_mode=0");

    Order testIptv1_3 = new Order();//iptv销户
    testIptv1_3.setArea_code("石家庄市局 ");
    testIptv1_3.setDeviceType("e8-b");
    testIptv1_3.setDevice_ID("FFFFFF-FFFFFFFFFFFF");
    testIptv1_3.setOrder_No("order0001");
    testIptv1_3.setOrder_Type("iptv-C");
    testIptv1_3.setVer("CNC.Y.002");
    testIptv1_3.setOrder_kind("SG");
    testIptv1_3.setService_code("iptv");
    testIptv1_3.setVector_argues("iptv_mode=0");

    Order testVoip_3 = new Order();//语音销户
    testVoip_3.setArea_code("石家庄市局 ");
    testVoip_3.setDeviceType("e8-b");
    testVoip_3.setDevice_ID("FFFFFF-FFFFFFFFFFFF");
    testVoip_3.setOrder_No("order0001");
    testVoip_3.setOrder_Type("voip-C");
    testVoip_3.setVer("CNC.Y.002");
    testVoip_3.setOrder_kind("SG");
    testVoip_3.setService_code("voip");

    Order teststb_1 = new Order();//stb开户
    teststb_1.setArea_code("清河县");
    teststb_1.setDeviceType("e8-b");
    teststb_1.setDevice_ID("AC:4A:FE:9E:6A:64");
    teststb_1.setOrder_No("order0001");
    teststb_1.setOrder_Type("iptv-Z");
    teststb_1.setVer("CNC.Y.002");
    teststb_1.setOrder_kind("SG");
    teststb_1.setService_code("iptv");
    teststb_1.setVector_argues("iptv_mode=1^userID=0311test^userPwd=123456^browserURL1=http://10.0.3.77:8082/EDS/jsp/AuthenticationURL^NTP1=10.0.3.104^NTP2=10.0.3.105");

    Order teststb_3 = new Order();//stb销户
    teststb_3.setArea_code("石家庄市局 ");
    teststb_3.setDeviceType("e8-b");
    teststb_3.setDevice_ID("FFFFF-FFFFFFFFFFFF");
    teststb_3.setOrder_No("order0001");
    teststb_3.setOrder_Type("iptv-C");
    teststb_3.setVer("CNC.Y.002");
    teststb_3.setOrder_kind("SG");
    teststb_3.setService_code("iptv");
    teststb_3.setVector_argues("");

    String url = "http://133.96.84.69:8184/NorthInterface/services/OperationDistributeService";

//    int res = callRemoteService(url, testVoip, "dealOrder");
//    System.out.println("res=" + res);
    
//    int restestVoip = callRemoteService(url, testVoip, "dealOrder");
//    System.out.println("restestVoip=" + restestVoip);
//    int restestWband1 = callRemoteService(url, testWband1, "dealOrder");
//    System.out.println("restestWband1=" + restestWband1);
//    int restestWband0 = callRemoteService(url, testWband0, "dealOrder");
//    System.out.println("restestWband0=" + restestWband0);
    int restestWband2 = callRemoteService(url, testWband2, "dealOrder");
    System.out.println("restestWband2=" + restestWband2);
    
  }

  
  /**
	 * 发送webService
	 * @param url 发送的url路径
	 * @param inParam 参数(obj)
	 * @param method 方法名
	 * @return 调用的方法结果
	 */
  public static int callRemoteService(String url, Object inParam, String method)
  {
    int returnParam = 1000;
    try
    {
      Service service = new Service();
      Call call = null;
      call = (Call)service.createCall();

      QName qn = new QName("urn:BeanService", "Order");
      call.registerTypeMapping(Order.class, qn, new BeanSerializerFactory(Order.class, qn), new BeanDeserializerFactory(Order.class, qn));

      call.setOperationName(new QName(url, method));
      call.setTargetEndpointAddress(new URL(url));

      returnParam = ((Integer)call.invoke(new Object[] { inParam })).intValue();
    }
    catch (Exception e)
    {
      logger.error(e.getMessage(), e);
    }
    return returnParam;
  }

  /**
	 * 获取xml跟节点下的参数值
	 * @param xml xml
	 * @param paramName 节点名
	 * @return 字符串
	 */
  
  public static String parse(String xml, String paramName)
  {
    SAXReader reader = new SAXReader();
    Document document = null;
    try {
      document = reader.read(new StringReader(xml));
      Element root = document.getRootElement();
      return root.elementTextTrim(paramName);
    } catch (Exception e) {
      e.printStackTrace();
    }return null;
  }
}