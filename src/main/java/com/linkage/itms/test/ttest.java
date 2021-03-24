package com.linkage.itms.test;

import com.linkage.WSClient.WSClientProcess;
import com.linkage.itms.commom.util.XML;
import com.linkage.itms.dispatch.obj.QueryByLoidChecker;
import org.apache.axiom.om.OMElement;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ttest {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
//		completed();
//		test();
//		ttt();
		testRetXml();
	}
	
	public static void completed () {
		try {
			// 入参：xml字符串
			StringBuffer inParam = new StringBuffer();
			inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
			inParam.append("<root>\n");
			inParam.append("	<CmdID>127</CmdID>\n");
			inParam.append("	<Sheets>\n");
			inParam.append("		<Loid>127</Loid>\n");
			inParam.append("		<CityId>00</CityId>\n");
			inParam.append("		<DevSN>373001880F5C33DC2</DevSN>\n");
			inParam.append("		<DevType>1710</DevType>\n");
			inParam.append("		<Vendor>ALCATEL</Vendor>\n");
			inParam.append("		<DevModel>RG200O-CA</DevModel>\n");
			inParam.append("		<HandwareVersion>V1.0</HandwareVersion>\n");
			inParam.append("		<SoftwareVersion>ASBRG200O-CA_V1.0S_JS1107</SoftwareVersion>\n");
			inParam.append("\n");
			inParam.append("		<SheetInfo>\n");
			inParam.append("			<DealDate>1322185031</DealDate>\n");
			inParam.append("			<ServiceType>10</ServiceType>\n");
			inParam.append("			<OperateType>1</OperateType>\n");
			inParam.append("			<OpenStatus>1</OpenStatus>\n");
			inParam.append("			<Desc></Desc>\n");
			inParam.append("		</SheetInfo>\n");
			inParam.append("\n");
			inParam.append("		<SheetInfo>\n");
			inParam.append("			<DealDate>1322185065</DealDate>\n");
			inParam.append("			<ServiceType>11</ServiceType>\n");
			inParam.append("			<OperateType>1</OperateType>\n");
			inParam.append("			<OpenStatus>-1</OpenStatus>\n");
			inParam.append("			<Desc></Desc>\n");
			inParam.append("		</SheetInfo>\n");
			inParam.append("\n");
			inParam.append("		<SheetInfo>\n");
			inParam.append("			<DealDate>1314847744</DealDate>\n");
			inParam.append("			<ServiceType>14</ServiceType>\n");
			inParam.append("			<OperateType>1</OperateType>\n");
			inParam.append("			<OpenStatus>1</OpenStatus>\n");
			inParam.append("			<Desc></Desc>\n");
			inParam.append("		</SheetInfo>\n");
			inParam.append("	</Sheets>\n");
			inParam.append("</root>\n");
			
			System.out.println(inParam.toString());
			final String endPointReference = "http://112.4.93.137:8000/liposs/services/liposs_epon_netcutover_WS4ITMS?wsdl";
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(endPointReference));

			QName qn = new QName(endPointReference, "completedInfo");
			call.setOperationName(qn);
			// 调用的服务器端方法
			// 回参：xml字符串
			String returnParam = (String) call.invoke(new Object[] { inParam.toString() });
			System.out.println(returnParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void test() {
		/** 命名空间 */
		String NAMESPACE = "http://ws.netcutover_js.epon.liposs.module.linkage.com";
		/** 方法名 */
		String METHOD_GETUSERMODEMINFO = "completedInfo";
		/** webservice前缀 */
		String PREFIX = "ns1";
		/** Action前缀 */
		String ACTION_PREFIX = "ns:";
		String URL = "http://112.4.93.137:8000/liposs/services/liposs_epon_netcutover_WS4ITMS?wsdl";
		// 入参：xml字符串
		StringBuffer inParam = new StringBuffer();
		inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
		inParam.append("<root>\n");
		inParam.append(" <CmdID>127</CmdID>\n");
		inParam.append(" <Sheets>\n");
		inParam.append(" <Loid>0025-9E8D-D7BD</Loid>\n");
		inParam.append(" <CityId>00</CityId>\n");
		inParam.append(" <DevSN>255253A108631D8E</DevSN>\n");
		inParam.append(" <DevType>1710</DevType>\n");
		inParam.append(" <Vendor>ALCATEL</Vendor>\n");
		inParam.append(" <DevModel>RG200O-CA</DevModel>\n");
		inParam.append(" <HandwareVersion>V1.0</HandwareVersion>\n");
		inParam.append(" <SoftwareVersion>ASBRG200O-CA_V1.0S_JS1107</SoftwareVersion>\n");
		inParam.append("\n");
		inParam.append(" <SheetInfo>\n");
		inParam.append(" <DealDate>1322185031</DealDate>\n");
		inParam.append(" <ServiceType>602</ServiceType>\n");
		inParam.append(" <OperateType>214</OperateType>\n");
		inParam.append(" <OpenStatus>1</OpenStatus>\n");
		inParam.append(" <Desc></Desc>\n");
		inParam.append(" </SheetInfo>\n");
		inParam.append("\n");
//		inParam.append(" <SheetInfo>\n");
//		inParam.append(" <DealDate>1322185065</DealDate>\n");
//		inParam.append(" <ServiceType>11</ServiceType>\n");
//		inParam.append(" <OperateType>1</OperateType>\n");
//		inParam.append(" <OpenStatus>-1</OpenStatus>\n");
//		inParam.append(" <Desc></Desc>\n");
//		inParam.append(" </SheetInfo>\n");
//		inParam.append("\n");
//		inParam.append(" <SheetInfo>\n");
//		inParam.append(" <DealDate>1314847744</DealDate>\n");
//		inParam.append(" <ServiceType>14</ServiceType>\n");
//		inParam.append(" <OperateType>1</OperateType>\n");
//		inParam.append(" <OpenStatus>1</OpenStatus>\n");
//		inParam.append(" <Desc></Desc>\n");
//		inParam.append(" </SheetInfo>\n");
		inParam.append(" </Sheets>\n");
		inParam.append("</root>\n");
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("para", inParam.toString());
		System.out.println(inParam.toString());
		OMElement element = WSClientProcess.serviceReceive(NAMESPACE, PREFIX, ACTION_PREFIX + METHOD_GETUSERMODEMINFO, METHOD_GETUSERMODEMINFO, param, URL);
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("gb2312");
		// xml返回值
		if (element != null) {
			// 字符串转为Document
			document = null;
			try {
				String xml = element.getFirstElement().getText();
				System.out.println(xml);
			} catch (Exception e) {
				System.out.println("解析XML失败");
			}
		}
	}

	
	public static void ttt(){
		String path = System.getProperties().getProperty("user.dir");
		path = path  + "/" + "WebContent/WEB-INF/litms_conf.xml";
		XML xml = new XML(path);
		String trrr = xml.getStringValue("InstArea");
		System.out.println("========"+trrr+"============");
	}
	
	
	public static void testRetXml(){
		
		StringBuffer inParam = new StringBuffer();
		inParam.append("<?xml version=\"1.0\" encoding=\"GBK\"?>\n");
		inParam.append("<root>\n");
		inParam.append("  <CmdID>123456789012345</CmdID>\n");
		inParam.append("  <CmdType>CX_01</CmdType>\n");
		inParam.append("  <ClientType>3</ClientType>");
		inParam.append("  <Param>\n");
		inParam.append("     <UserInfoType>1</UserInfoType>\n");
		inParam.append("     <UserInfo>9911234567C </UserInfo>\n");
		inParam.append("     <AccName>njkd123456</AccName>\n");
		inParam.append("  </Param>\n");
		inParam.append("</root>\n");
		
		QueryByLoidChecker queryByLoidChecker = new QueryByLoidChecker(inParam.toString());
		
		if (false == queryByLoidChecker.check()) {
			System.out.println("======11========="+queryByLoidChecker.getReturnXml());
		}
		
		// 查询用户信息
		Map<String, String> userInfoMap = null;

		queryByLoidChecker.setResult(1002);
		queryByLoidChecker.setResultDesc("查无此用户");
		String returnXml = queryByLoidChecker.getReturnXml();
		System.out.println("==========22===="+returnXml);
	}
	
	
}
