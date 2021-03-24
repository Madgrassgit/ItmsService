package com.linkage.itms.test;

import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import com.linkage.commons.util.DateTimeUtil;

/**
 * 
 * @author Administrator(工号) Tel:??
 * @version 1.0
 * @since 2011-5-12 上午09:44:11
 * @category com.linkage
 * @copyright 南京联创科技 网管科技部
 *
 */
public class TestItmsService
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
//		testCall();      // 
//		testBindInfo();  // 绑定情况查询  
//		testBind();      // 用户设备绑定
//		testServiceDone();  // 业务下发
//		testDevOnline();    // 根据用户帐号或者设备序列号查询终端的在线情况
//		testPvcReformed();  // 根据用户帐号或者设备序列号查询用户的多PVC改造情况
//		release();          // 用户设备解绑接口
//		queryBssSheetAndOpenStatus();  // BSS业务工单查询接口   
//		queryDeviceConfig();   // 设备配置查询接口                
//		recieveSheet();
//		completed();
//		querySheetData();      //
//		getItvAccount();
//		serviceDiagnostic();   // 诊断接口
//		sendAlarmInfo();
//		releaseWarnInfo();
//		zdEndOfWork();         //综调接口
//		BridgeToRout();        // 桥改路由
//		voipProtocol();        // 新疆电信ITMS与IP网管接口  VOIP语音协议查询
//		getSerResult();
//		reset();// 恢复出场设置
//		reboot();
//		netPassword();
//		testFtth();
//		UpgradeToStandardVersionService();
//		sendSheetByChinaMobile();
//		getParameterValues();  // 节点值获取
//		setParameterValues();  // 节点值设置
	}
	
	private static void testFtth()
	{
		try {
			// 入参：xml字符串
			String strsend = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><info><CmdID>10415614817</CmdID><RstCode>0</RstCode><RstMsg>成功</RstMsg><rLightFade>-10.0</rLightFade><sLightFade>3.0</sLightFade><Sheets><SN>2587791020000006</SN><CityId>0100</CityId><DevSN>4321432143214A001</DevSN><DevType>e8-c</DevType></Sheets><sheetInfo><DealDate>1320903034</DealDate><ServiceType>10</ServiceType><OpenStatus>0</OpenStatus><KdUserName>02510718888</KdUserName></sheetInfo><sheetInfo><DealDate>1311819357</DealDate><ServiceType>11</ServiceType><OpenStatus>0</OpenStatus><IPTVUserName>02510718888</IPTVUserName></sheetInfo><sheetInfo><DealDate>1314847744</DealDate><ServiceType>14</ServiceType><OpenStatus>0</OpenStatus><VoipUserName>02584220578</VoipUserName></sheetInfo></info>";
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("	<CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<loid>12EC45CE76D78B28</loid>      \n");
			inParam.append("		<gwType>1</gwType>  \n");
			inParam.append("	</Param>  \n");
			inParam.append("</root>  \n");
//			System.out.println(inParam.toString());
//			final String endPointReference = "http://202.102.39.141:8080/ItmsService/services/CallService";// 61.191.44.237   134.64.195.68
			final String endPointReference = "http://134.224.36.70:7001/css/services/InfoService";
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "addInfo");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { strsend });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	//nE7jA%5m
	
	public static void setParameterValues () {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("	<CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>6</UserInfoType>      \n");
			inParam.append("		<UserInfo>B020070B921A848D7</UserInfo>  \n");
			inParam.append("		<ParameterList>  \n");
			inParam.append("			<ParameterValueStruct>  \n");
			inParam.append("				<Name>InternetGatewayDevice.WANDevice.1.WANConnectionDevice.2.WANPPPConnection.1.Username</Name>  \n");
			inParam.append("				<Value>noc_test</Value>  \n");
			inParam.append("			</ParameterValueStruct>  \n");
			inParam.append("			<ParameterValueStruct>  \n");
			inParam.append("				<Name>InternetGatewayDevice.WANDevice.1.WANConnectionDevice.2.WANPPPConnection.1.Enable</Name>  \n");
			inParam.append("				<Value>1</Value>  \n");
			inParam.append("			</ParameterValueStruct>  \n");
			inParam.append("		</ParameterList>  \n");
			inParam.append("	</Param>  \n");
			inParam.append("</root>  \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://134.64.195.67:8080/ItmsService/services/CallService";// 61.191.44.237   134.64.195.68
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "setParameterValues");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void getParameterValues () {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("	<CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>123456789</UserInfo>  \n");
			inParam.append("		<ParameterNames>  \n");
			inParam.append("			<string>InternetGatewayDevice.WANDevice.1.WANConnectionDevice.2.X_CT-COM_WANGponLinkConfig.VLANIDMark</string> \n");
			inParam.append("			<string>InternetGatewayDevice.WANDevice.1.WANConnectionDevice.2.WANIPConnectionNumberOfEntries</string>");
			inParam.append("		</ParameterNames>  \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://134.64.195.67:8080/ItmsService/services/CallService";// 61.191.44.237   134.64.195.68
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "getParameterValues");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void sendSheetByChinaMobile(){
		try {
			// 入参：xml字符串
			StringBuffer sendSheet = new StringBuffer();
//			StringBuffer deleUserSheet = new StringBuffer();
//			StringBuffer wideNet = new StringBuffer();
//			StringBuffer delewideNet = new StringBuffer();
			
			DateTimeUtil dt = new DateTimeUtil();
			
//			sendSheet.append("123456789001|||")
//			.append("20|||").append("1|||").append(dt.getYYYYMMDDHHMMSS()).append("|||").append("1|||")
//			.append("zhangcy_test|||").append("0|||").append("1641|||").append("xiaoqu|||")
//			.append("2|||").append("联系人|||").append("02522222|||").append("zhang@163.com|||").append("13912911111|||")
//			.append("江苏南京|||").append("142623198008081010|||")
//			.append("test001|||").append("account123|||").append("123456|||e8cp42LINKAGE")
//			.append("#").append("123456789001|||").append("22|||").append("1|||").append(dt.getYYYYMMDDHHMMSS()).append("|||")
//			.append("1|||").append("zhangcy_test|||").append("0257777777|||").append("777777777|||").append("0|||")
//			.append("41|||").append("1|||").append("|||").append("|||").append("|||").append("LINKAGE");
			
//			delewideNet.append("123456789001|||").append("22|||").append("3|||").append(dt.getYYYYMMDDHHMMSS()).append("|||")
//			.append("1|||").append("zhangcy_test|||").append("0LINKAGE");
			
//			sendSheet.append("123456789001|||20|||3|||").append(dt.getYYYYMMDDHHMMSS()+"|||1|||zhangcy_test|||0LINKAGE");
			
			
			sendSheet.append("123456789001|||21|||1|||").append(dt.getYYYYMMDDHHMMSS()).append("|||").append("1|||zhangcy_test|||iptvuser|||0|||1|||L2|||100LINKAGE");
			
//			System.out.println(sendSheet.toString());
			final String endPointReference = "http://192.168.1.100:6060/ItmsService/services/CallService";  // 江苏移动测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "sendSheetByChinaMobile");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			
			String[] sheetArr = sendSheet.toString().split("#");
			for (int i = 0; i < sheetArr.length; i++) {
				System.out.println(sheetArr[i]);
				String returnParam = (String) call.invoke(new Object[] { sheetArr[i] });
				System.out.println(returnParam);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public static void UpgradeToStandardVersionService(){
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>7710297266</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>1979D18B17AC56CC</UserInfo>  \n");
			inParam.append("		<CityId>0400</CityId>  \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://202.102.39.139:8383/ItmsService/services/CallService";  // 江苏测试环境
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";  // 新疆测试
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService"; 
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "softwareupgrade");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void reboot(){
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>340553BBBBBBBB0000000001</UserInfo>  \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://222.74.28.10:9090/ItmsService/services/CallService";//内蒙古
//			final String endPointReference = "http://134.64.195.67:8080/ItmsService/services/CallService";// 61.191.44.237   134.64.195.68
//			final String endPointReference = "http://202.102.39.141:7070/ItmsService/services/CallService";
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "reboot");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void netPassword(){
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>340553BBBBBBBB0000000001</UserInfo>  \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://222.74.28.10:9090/ItmsService/services/CallService";//内蒙古
//			final String endPointReference = "http://172.16.7.68:20000/ItmsService/services/CallService";// 61.191.44.237   134.64.195.68
//			final String endPointReference = "http://202.102.39.141:7070/ItmsService/services/CallService";
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "netPassword");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void reset(){
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>1</UserInfoType>      \n");
			inParam.append("		<UserInfo>079111113</UserInfo>  \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://202.102.39.141:9090/ItmsService/services/CallService";  // 141测试
			final String endPointReference = "http://134.224.44.10:7070/ItmsService/services/CallService";  // 江西测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "reset");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void getSerResult(){
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>1325228122932</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>9932000371C</UserInfo>  \n");
			inParam.append("        <AccName>111</AccName >    \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://202.102.39.141:9090/ItmsService/services/CallService";  // 141测试
			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "getSerResult");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * VOIP语音协议查询
	 */
	public static void voipProtocol(){
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>1325228122932</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>9950003076C</UserInfo>  \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://202.102.39.141:9090/ItmsService/services/CallService";  // 141测试
			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "voipProtocol");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * 桥改路由
	 */
	public static void BridgeToRout () {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>1325228122932</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>2577777777777777</UserInfo>  \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
////			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
////			final String endPointReference = "http://202.102.39.141:7070/ItmsService/services/CallService";  // 141测试
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
//			Service service = new Service();
//			Call call = null;
//			call = (Call) service.createCall();
//			call.setTargetEndpointAddress(new URL(endPointReference));
//
//			QName qn = new QName(endPointReference, "BridgeToRout");
//			call.setOperationName(qn);
//			// 调用的服务器端方法
//			// 回参：xml字符串
//			String returnParam = (String) call.invoke(new Object[] { inParam.toString() });
//			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 综调
	 */
	public static void zdEndOfWork () {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>1325228122932</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>25E2E5C280417873</UserInfo>  \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://202.102.39.141:9090/ItmsService/services/CallService";  // 141测试
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "zdEndOfWork");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void releaseWarnInfo()
	{
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>5</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<WarnId>123456789012345</WarnId>      \n");
			inParam.append("		<isRelease>1</isRelease>      \n");

			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
//			final String endPointReference = "http://136.192.27.180:8088/axis2/GetInfo.jws";
			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();	
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "releaseWarnInfo");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void sendAlarmInfo()
	{
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>5</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<WarnId>123456789012345</WarnId>      \n");
			inParam.append("		<WarnName>家庭网关告警</WarnName>      \n");
			inParam.append("		<WarnGrade>1</WarnGrade>      \n");
			inParam.append("		<WarnTime>20111114151719</WarnTime>      \n");
			inParam.append("		<WarnType>WarnType</WarnType>      \n");
			inParam.append("		<Loid>4511LO00000020</Loid>      \n");
			inParam.append("		<Username>4511LO00000020</Username>      \n");
			inParam.append("		<StaticIp>192.168.1.1</StaticIp>      \n");
			inParam.append("		<DevSN>devicesn</DevSN>      \n");
			inParam.append("		<OUI>deviceoui</OUI>      \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
//			final String endPointReference = "http://136.192.27.180:8088/axis2/GetInfo.jws"; ///ip:141 port:7070
			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();	
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "sendWarnInfo");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void getItvAccount() {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<mac>00:22:93:1D:F8:0E</mac>      \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/ItmsService";
			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "getItvAccount");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void querySheetData () {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>340553BBBBBBBB0000000001</UserInfo>  \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://202.102.39.141:7070/ItmsService/services/CallService";  // 江苏测试
//			final String endPointReference = "http://134.64.195.67:8080/ItmsService/services/CallService";// 61.191.44.237   134.64.195.68
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "querySheetData");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void completed () {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<OrderID>152290230</OrderID>           \n");
			inParam.append("	<Loid>4511LO00000020</Loid>         \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CompletedService";
			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "completedInfo");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void recieveSheet() {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>                \n");  
			inParam.append("<root>                                              \n");  
			inParam.append("	<CmdID>201107221311336354171</CmdID>              \n");  
			inParam.append("	<CmdType>CX_01</CmdType>                          \n");  
			inParam.append("	<ClientType>3</ClientType>                        \n");  
			inParam.append("	<OrderID>152290230</OrderID>                      \n");  
			inParam.append("	<Param>                                           \n"); 
			inParam.append("		<isGiveDev>1</isGiveDev>                         \n");  
			inParam.append("		<servType>10</servType>                         \n");  
			inParam.append("		<!-- 操作类型-->                                \n");  
			inParam.append("		<operType>1</operType>                          \n");  
			inParam.append("		<!-- 受理时间-->                                \n");  
			inParam.append("		<dealDate>20110730151719</dealDate>             \n");  
			inParam.append("		<!-- 设备类型-->                                \n");  
			inParam.append("		<devType>HG8245-E</devType>                     \n");  
			inParam.append("		<!--loid -->                                    \n");  
			inParam.append("		<loid>4511LO000000aa</loid>                     \n");  
			inParam.append("		<!--属地编码 -->                                \n");  
			inParam.append("		<cityId>451</cityId>                            \n");  
			inParam.append("		<!--局向标志 -->                                \n");  
			inParam.append("		<officeId></officeId>                           \n");  
			inParam.append("		<!--小区标志-->                                 \n");  
			inParam.append("		<cellId></cellId>                               \n");  
			inParam.append("		<!--接入方式-->                                 \n");  
			inParam.append("		<accessType>7</accessType>                      \n");  
			inParam.append("		<!--联系人-->                                   \n");  
			inParam.append("		<linkPeople>临时</linkPeople>                   \n");  
			inParam.append("		<!--联系电话-->                                 \n");  
			inParam.append("		<telPhone>12345678</telPhone>                   \n");  
			inParam.append("		<!--Email-->                                    \n");  
			inParam.append("		<email></email>                                 \n");  
			inParam.append("		<!--手机-->                                     \n");  
			inParam.append("		<mobile></mobile>                               \n");  
			inParam.append("		<!--家庭住址-->                                 \n");  
			inParam.append("		<address>临时</address>                         \n");  
			inParam.append("		<!--证件号码-->                                 \n");  
			inParam.append("		<cardNo>0</cardNo>                              \n");  
			inParam.append("		<!--宽带账号-->                                 \n");  
			inParam.append("		<netUsername>zdmkuandaitestaa</netUsername>         \n");  
			inParam.append("		<!--宽带密码-->                                 \n");  
			inParam.append("		<netPassword>zdmkuandaitestpswd</netPassword>      \n");  
			inParam.append("		<!--ADSL绑定电话-->                             \n");  
			inParam.append("		<phoneNumber>123456789</phoneNumber>            \n");  
			inParam.append("		<!--最大上行速率-->                             \n");  
			inParam.append("		<maxupRate>4</maxupRate>                        \n");  
			inParam.append("		<!--最大下行速率-->                             \n");  
			inParam.append("		<maxdownRate>4</maxdownRate>                    \n");  
			inParam.append("		<!--最大上网用户数-->                           \n");  
			inParam.append("		<maxUserNum>4</maxUserNum>                      \n");  
			inParam.append("		<!--Vlan id-->                                  \n");  
			inParam.append("		<vlanId>502</vlanId>                            \n");  
			inParam.append("		<!--VPI-->                                      \n");  
			inParam.append("		<vpi>0</vpi>                                    \n");  
			inParam.append("		<!--VCI-->                                      \n");  
			inParam.append("		<vci>35</vci>                                   \n");  
			inParam.append("		<!--DSLAM设备编码-->                            \n");  
			inParam.append("		<dslamDevNo></dslamDevNo>                       \n");  
			inParam.append("		<!--DSLAM IP地址-->                             \n");  
			inParam.append("		<dslamIp>172.17.145.33</dslamIp>                \n");  
			inParam.append("		<!--DSLAM设备机架号-->                          \n");  
			inParam.append("		<dslamDevFrameNo></dslamDevFrameNo>             \n");  
			inParam.append("		<!--DSLAM设备框号-->                            \n");  
			inParam.append("		<dslamDevBoxNo></dslamDevBoxNo>                 \n");  
			inParam.append("		<!--DSLAM设备槽位号-->                          \n");  
			inParam.append("		<dslamDevSlotNo></dslamDevSlotNo>               \n");  
			inParam.append("		<!--DSLAM设备端口号-->                          \n");  
			inParam.append("		<dslamDevPortNo></dslamDevPortNo>               \n");  
			inParam.append("		<!--上网方式-->                                 \n");  
			inParam.append("		<wanType></wanType>                             \n");  
			inParam.append("		<!--套餐类型-->                                 \n");  
			inParam.append("		<packageType></packageType>                     \n");  
			inParam.append("		<!--终端OUI-->                                  \n");  
			inParam.append("		<oui></oui>                                     \n");  
			inParam.append("		<!--终端SN-->                                   \n");  
			inParam.append("		<devSn></devSn>                                 \n");  
			inParam.append("		<!--原来宽带帐号-->                             \n");  
			inParam.append("		<oldNetUsername>0012345678</oldNetUsername>               \n");  
			
		    inParam.append("     <!--业务电话号码-->                 \n");
			inParam.append("     <voipTelepone>12345678</voipTelepone>       \n");
			inParam.append("     <!--终端标识-->                     \n");
			inParam.append("     <regId>4511LO000000aa.voip</regId>                     \n");
			inParam.append("     <!--终端标识类型-->                 \n");
			inParam.append("     <regIdType>1</regIdType>             \n");
			inParam.append("     <!--主用MGC地址-->                  \n");
			inParam.append("     <mgcIp>172.16.0.10</mgcIp>                     \n");
			inParam.append("     <!--主用MGC端口-->                  \n");
			inParam.append("     <mgcPort>2944</mgcPort>                 \n");
			inParam.append("     <!--备用MGC地址-->                  \n");
			inParam.append("     <standMgcIp>172.16.0.2</standMgcIp>           \n");
			inParam.append("     <!--备用MGC端口-->                  \n");
			inParam.append("     <standMgcPort>2944</standMgcPort>       \n");
			inParam.append("     <!--标示语音口-->                   \n");
			inParam.append("     <voipPort>0</voipPort>               \n");
			inParam.append("	 </Param>                                          \n"); 

			inParam.append("</root>                                             \n");  

			System.out.println(inParam.toString());
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/OpenService";
			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "recieveSheet");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void release () {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>177A4561BC3BB6E8</UserInfo>  \n");
			inParam.append("        <CityId>001006</CityId>        \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "release");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void queryDeviceConfig () {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>4</UserInfoType>      \n");
			inParam.append("		<UserInfo>02584607155</UserInfo>  \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://202.102.39.141:9090/ItmsService/services/CallService";  // 141测试
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "queryDeviceConfig");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void queryBssSheetAndOpenStatus () {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>25E2E5C280417873</UserInfo>  \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://202.102.39.141:9090/ItmsService/services/CallService";  // 141测试
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "queryBssSheetAndOpenStatus");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void testPvcReformed() {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			
			inParam.append("		<SearchType>1</SearchType>      \n");
			inParam.append("		<UserName>2506A3D2BD742A98</UserName>  \n");
			inParam.append("		<DevSN>3A3003846082CD739</DevSN>            \n");
			inParam.append("        <CityId>000101</CityId>        \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "pvcReformed");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void testDevOnline() {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<SearchType>1</SearchType>      \n");
			inParam.append("		<UserInfoType>1</UserInfoType>      \n");
			inParam.append("		<UserName>079111113</UserName>  \n");
			inParam.append("		<DevSN>39300781DBA3423D1</DevSN>            \n");
			inParam.append("        <CityId>0100</CityId>        \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			final String endPointReference = "http://134.224.44.10:7070/ItmsService/services/CallService";  // 江西测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "devOnline");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void testCall() {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			
			inParam.append("		<SearchType>1</SearchType>      \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserName>161720C2C2397985</UserName>  \n");
			inParam.append("		<DevSN>41300781DBAF90CBE</DevSN>            \n");
			inParam.append("        <CityId>000902</CityId>        \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "call");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testServiceDone() {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<ServiceType>14</ServiceType>      \n");
			inParam.append("		<OperateType>1</OperateType>      \n");
			inParam.append("		<SearchType>1</SearchType>      \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>340553BBBBBBBB0000000001</UserInfo>  \n");
			inParam.append("		<DevSN>463000019C7B5A438</DevSN>            \n");
			inParam.append("        <CityId>551</CityId>        \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://134.64.195.67:8080/ItmsService/services/CallService";// 安徽电信 61.191.44.237   134.64.195.68
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "serviceDone");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testBind() {
		try {
			// 入参：xml字符串
			String str = "<?xml version=\"1.0\" encoding=\"GBK\"?><root><CmdID>123456789012345</CmdID><CmdType> CX_01</CmdType><ClientType>3</ClientType><Param><BindType>1</BindType ><UserInfoType>1</UserInfoType><UserInfo>njkd123456</UserInfo><DevSN>123456</DevSN><CityId>0100</CityId><Desc>终端损坏</Desc></Param></root>";
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>123456789012345</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<BindType>1</BindType >      \n");
			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
			inParam.append("		<UserInfo>177A4561BC3BB6E8</UserInfo>  \n");
			inParam.append("		<DevSN>4E3004CB16CB3F9EA</DevSN>            \n");
			inParam.append("		<CityId>001006</CityId>        \n");
			inParam.append("        <Desc>111111</Desc>       \n");
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
//			System.out.println(inParam.toString());
			final String endPointReference = "http://222.74.28.10:9090/ItmsService/services/CallService";
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://192.168.15.5:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "bind");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { str });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void testBindInfo() {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>	<CmdID>9615aa74-5e86-4d53-ba73-8bd83b8119d9</CmdID>	<CmdType>CX_01</CmdType>	<ClientType>5</ClientType>	<Param>		<UserInfoType/>		<UserInfo/>		<DevSN>0ED4A7</DevSN><CityId>1010</CityId></Param></root>");
//			inParam.append("<root>\n");
//			inParam.append("    <CmdID>12345678901234544</CmdID>       \n");
//			inParam.append("	<CmdType>CX_01</CmdType>           \n");
//			inParam.append("	<ClientType>3</ClientType>         \n");
//			inParam.append("	<Param>                            \n");
//			inParam.append("		<UserInfoType>2</UserInfoType>      \n");
//			inParam.append("		<UserInfo>25E2E5C280417873</UserInfo>  \n");//25njminhongbing1   25zhengqiwangguan
//			inParam.append("		<DevSN>47300548998676444</DevSN>            \n");  //4E300000AC23F5368    600103405111920029
//			inParam.append("        <CityId>0100</CityId>        \n");  //00
//			inParam.append("	</Param>                           \n");
//			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
//			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://202.102.39.141:9090/ItmsService/services/CallService";  // 141测试
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			final String endPointReference = "http://61.128.117.21:8080/ItmsService/services/CallService";  // 新疆测试
			
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "bindInfo");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// add by chenjie 2011-12-12
	public static void serviceDiagnostic()
	{

		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("    <CmdID>111</CmdID>       \n");
			inParam.append("	<CmdType>CX_01</CmdType>           \n");
			inParam.append("	<ClientType>3</ClientType>         \n");
			inParam.append("	<Param>                            \n");
			inParam.append("		<UserInfoType>4</UserInfoType>      \n");  // 4:VOIP电话号码
			inParam.append("		<UserInfo>02584607155</UserInfo>  \n"); //    //itvtest2
			inParam.append("	</Param>                           \n");
			inParam.append("</root>                              \n");
			System.out.println(inParam.toString());
			final String endPointReference = "http://192.168.2.28:8383/ItmsService/services/CallService";
//			final String endPointReference = "http://202.102.39.141:9090/ItmsService/services/CallService";  // 141测试
//			final String endPointReference = "http://192.168.18.4:7070/ItmsService/services/CallService";  // 新疆测试
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "serviceDiagnostic");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam
					.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
