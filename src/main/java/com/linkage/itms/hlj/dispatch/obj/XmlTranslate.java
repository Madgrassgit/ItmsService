package com.linkage.itms.hlj.dispatch.obj;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.DateTimeUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-11-1
 * @category com.linkage.itms.hlj.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class XmlTranslate {

	private static final Logger logger = LoggerFactory
			.getLogger(XmlTranslate.class);
	private String callXml;
	private String loid;
	private String rstCode;
	private String rstMsg;
	private String time;
	private String bssType;
	private String acc;
	private String mac;
	private String outTime;
	private Long out_time;
	private String iptvPwd;
	private String ipoePwd;
	private String allDevSn;
	private String packageByte;
	private String timeOut;
	private String ipOrDomainName;
	private String wanPassageWay;
	private String packageNum;
	private String wanType;
	private String lanId;

	public XmlTranslate(String inXml) {
		callXml = inXml;
	}

	/**
	 * 吉林入参转其他入参 家庭网关除业务下发均通过此方法进行入参调整
	 * 
	 * @author 岩
	 * @date 2016-11-1
	 * @return
	 */
	public String jlToOtherLoid() {
		logger.debug("XmlTranslate==>jlToOtherLoid()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			Element p = root.element("public");
			time = p.elementTextTrim("time");
			Element data = root.element("data");
			loid = data.elementTextTrim("loid");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		root.addElement("CmdID").addText("123456789012345");
		root.addElement("CmdType").addText("CX_01");
		root.addElement("ClientType").addText("3");
		Element param = root.addElement("Param");
		param.addElement("UserInfoType").addText("2");
		param.addElement("UserInfo").addText(loid);
		// 当SearchType为2的时候，是根据devSn进行查询
		param.addElement("SearchType").addText("2");
		// 当SearchType为2的时候，username,userInfoType只要不为空即可
		param.addElement("UserName").addText("2");
		// devsn不能为空，且长度不低于六位
		// 根据当前loid获取devSn，如果没有相应的设备序列号 则返回一个不存在的六位数
		List<HashMap<String, String>> userMap = null;
		userMap = getDevSnByLoid(loid);
		if (userMap == null || userMap.isEmpty()) {
			allDevSn = "";
			param.addElement("DevSN").addText("");
			param.addElement("CityId").addText("");
		} else if (userMap.get(0).get("device_serialnumber") == null
				|| "".equals(userMap.get(0).get("device_serialnumber"))) {
			allDevSn = "";
			param.addElement("DevSN").addText("");
			param.addElement("CityId").addText(userMap.get(0).get("city_id"));
		} else {
			allDevSn = "存在";
			param.addElement("DevSN").addText(
					userMap.get(0).get("device_serialnumber"));
			param.addElement("CityId").addText(userMap.get(0).get("city_id"));
		}
		// serviceType,OperateType 业务下发接口时
		return createDocument.asXML();
	}

	public String jlToOtherLoidBBMS() {
		logger.debug("XmlTranslate==>jlToOtherLoid()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			Element p = root.element("public");
			time = p.elementTextTrim("time");
			Element data = root.element("data");
			loid = data.elementTextTrim("loid");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		root.addElement("CmdID").addText("123456789012345");
		root.addElement("CmdType").addText("CX_01");
		root.addElement("ClientType").addText("3");
		Element param = root.addElement("Param");
		param.addElement("UserInfoType").addText("2");
		param.addElement("UserInfo").addText(loid);
		// 当SearchType为2的时候，是根据devSn进行查询
		param.addElement("SearchType").addText("2");
		// 当SearchType为2的时候，username,userInfoType只要不为空即可
		param.addElement("UserName").addText("2");
		// devsn不能为空，且长度不低于六位
		// 根据当前loid获取devSn，如果没有相应的设备序列号 则返回一个不存在的六位数
		List<HashMap<String, String>> userMap = null;
		userMap = getDevSnByLoidBBMS(loid);
		if (userMap == null || userMap.isEmpty()) {
			allDevSn = "";
			param.addElement("DevSN").addText("");
			param.addElement("CityId").addText("");
		} else if (userMap.get(0).get("device_serialnumber") == null
				|| "".equals(userMap.get(0).get("device_serialnumber"))) {
			allDevSn = "";
			param.addElement("DevSN").addText("");
			param.addElement("CityId").addText(userMap.get(0).get("city_id"));
		} else {
			allDevSn = "存在";
			param.addElement("DevSN").addText(
					userMap.get(0).get("device_serialnumber"));
			param.addElement("CityId").addText(userMap.get(0).get("city_id"));
		}
		// serviceType,OperateType 业务下发接口时
		return createDocument.asXML();
	}
	/**
	 * 吉林入参转其他入参 业务下发入参内多了一个上网类型，与其他不同
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @return
	 */
	public String jlToOtherDev() {
		logger.debug("XmlTranslate==>jlToOtherDev()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			Element p = root.element("public");
			time = p.elementTextTrim("time");
			Element data = root.element("data");
			loid = data.elementTextTrim("loid");
			bssType = data.elementTextTrim("bsstype");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		root.addElement("CmdID").addText("123456789012345");
		root.addElement("CmdType").addText("CX_01");
		root.addElement("ClientType").addText("3");
		Element param = root.addElement("Param");
		// 当SearchType为2的时候，是根据devSn进行查询
		param.addElement("SearchType").addText("2");
		// 当SearchType为2的时候，username,userInfoType只要不为空即可
		param.addElement("UserInfoType").addText("2");
		param.addElement("UserInfo").addText("2");
		// devsn不能为空，且长度不低于六位
		// 根据当前loid获取devSn，如果没有相应的设备序列号 则返回一个不存在的六位数
		List<HashMap<String, String>> userMap = null;
		userMap = getDevSnByLoid(loid);
		if (userMap == null || userMap.isEmpty()) {
			param.addElement("DevSN").addText("");
			param.addElement("CityId").addText("");
		} else if (userMap.get(0).get("device_serialnumber") == null
				|| "".equals(userMap.get(0).get("device_serialnumber"))) {
			param.addElement("DevSN").addText("");
			param.addElement("CityId").addText(userMap.get(0).get("city_id"));
		} else {
			param.addElement("DevSN").addText(
					userMap.get(0).get("device_serialnumber"));
			param.addElement("CityId").addText(userMap.get(0).get("city_id"));
		}
		// serviceType,operateType 业务下发特有属性
		param.addElement("OperateType").addText("1");
		if ("0".equals(bssType)) {
			param.addElement("ServiceType").addText("10");
		} else {
			param.addElement("ServiceType").addText("11");
		}
		return createDocument.asXML();
	}

	/**
	 * 吉林入参转其他入参 除机顶盒绑定接口均通过此方法转入参
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @return
	 */
	public String jlToOtherStbAcc() {
		logger.debug("XmlTranslate==>jlToOtherStbAcc()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			Element p = root.element("public");
			time = p.elementTextTrim("time");
			Element data = root.element("data");
			acc = data.elementTextTrim("acc");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		root.addElement("CmdID").addText("123456789012345");
		root.addElement("CmdType").addText("CX_01");
		root.addElement("ClientType").addText("3");
		Element param = root.addElement("Param");
		// 绑定接口
		param.addElement("SelectType").addText("1");
		param.addElement("UserInfo").addText(acc);
		// 重启接口，机顶盒在线状态查询接口
		param.addElement("SearchType").addText("1");
		param.addElement("SearchInfo").addText(acc);
		// 业务下发接口
		param.addElement("queryConditionType").addText("1");
		param.addElement("queryCondition").addText(acc);
		param.addElement("operType").addText("1");
		return createDocument.asXML();
	}

	/**
	 * 吉林入参转其他入参,机顶盒版本信息查询
	 * 
	 * @author wangyan
	 * @date 2016-12-14
	 * @return
	 */
	public String jlToOtherStbVersion() {
		logger.debug("XmlTranslate==>jlToOtherStbAcc()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			Element p = root.element("public");
			time = p.elementTextTrim("time");
			Element data = root.element("data");
			mac = data.elementTextTrim("mac");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		root.addElement("CmdID").addText("123456789012345");
		root.addElement("CmdType").addText("CX_01");
		root.addElement("ClientType").addText("3");
		Element param = root.addElement("Param");
		// 机顶盒版本信息查询接口
		param.addElement("SearchType").addText("2");
		param.addElement("SearchInfo").addText(mac);
		return createDocument.asXML();
	}

	/**
	 * 机顶盒绑定接口入参调整
	 * 
	 * @author 岩
	 * @date 2016-11-3
	 * @return
	 */
	public String jlToOtherStbBind() {
		logger.debug("XmlTranslate==>jlToOtherStbBind()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			Element p = root.element("public");
			time = p.elementTextTrim("time");
			Element data = root.element("data");
			acc = data.elementTextTrim("acc");
			mac = data.elementTextTrim("mac");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		root.addElement("CmdID").addText("123456789012345");
		root.addElement("CmdType").addText("CX_01");
		root.addElement("ClientType").addText("3");
		Element param = root.addElement("Param");
		// 重启接口，机顶盒在线状态查询接口
		param.addElement("SearchType").addText("2");
		param.addElement("SearchInfo").addText(mac);
		param.addElement("UserAccount").addText(acc);
		String newMac = getStbMac(acc);
		if (newMac.equals(mac)) {
			return createDocument.asXML();
		} else {
			return "-1";
		}

	}

	/**
	 * 机顶盒修改iptvpwd,ipoepwd
	 * 
	 * @author wangyan
	 * @date 2016-11-18
	 * @return
	 */
	public String jlToOtherUpdatePwd() {
		logger.debug("XmlTranslate==>jlToOtherStbBind()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			Element p = root.element("public");
			time = p.elementTextTrim("time");
			Element data = root.element("data");
			acc = data.elementTextTrim("acc");
			iptvPwd = data.elementTextTrim("iptvpsw");
			ipoePwd = data.elementTextTrim("ipoepsw");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		int status = updateSuperPwd(iptvPwd, ipoePwd, acc);
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		root.addElement("CmdID").addText("123456789012345");
		root.addElement("CmdType").addText("CX_01");
		root.addElement("ClientType").addText("3");
		Element param = root.addElement("Param");
		// 重启接口，机顶盒在线状态查询接口
		param.addElement("SearchType").addText("1");
		param.addElement("SearchInfo").addText(acc);
		if (1 == status) {
			return createDocument.asXML();
		} else {
			return "-1";
		}

	}

	/**
	 * 家庭网关状态查询接口
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @param outXml
	 * @return
	 */
	public String otherToJlDevOnline(String outXml) {
		logger.debug("XmlTranslate==>otherToJlDevOnline()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		String status = null;
		String regTime = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
			if ("0".equals(rstCode)) {
				Element p = root.element("Param");
				status = p.elementTextTrim("OnlineStatus");
			}
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		// 终端状态
		if ("1".equals(status)) {
			data.addElement("status").addText("UP");
		} else if (!"".equals(allDevSn)) {
			data.addElement("status").addText("DOWN");
		}else{
			data.addElement("status").addText("");
		}
		if (null == getDevCompleteTime(loid)
				|| "".equals(getDevCompleteTime(loid))) {
			data.addElement("regTime").addText("");
		} else {
			regTime = new DateTimeUtil().getLongDate(StringUtil
					.getLongValue(getDevCompleteTime(loid)));
			// 时间原有接口不能查询
			data.addElement("regTime").addText(regTime);
		}
		return createDocument.asXML();
	}

	
	/**
	 * 企业网关状态查询接口
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @param outXml
	 * @return
	 */
	public String otherToJlDevOnlineBBMS(String outXml) {
		logger.debug("XmlTranslate==>otherToJlDevOnline()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		String status = null;
		String regTime = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
			if ("0".equals(rstCode)) {
				Element p = root.element("Param");
				status = p.elementTextTrim("OnlineStatus");
			}
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		// 终端状态
		if ("1".equals(status)) {
			data.addElement("status").addText("UP");
		} else if ( !"".equals(allDevSn)) {
			data.addElement("status").addText("DOWN");
		}else{
			data.addElement("status").addText("");
		}
		if (null == getDevCompleteTimeBBMS(loid)
				|| "".equals(getDevCompleteTimeBBMS(loid))) {
			data.addElement("regTime").addText("");
		} else {
			regTime = new DateTimeUtil().getLongDate(StringUtil
					.getLongValue(getDevCompleteTimeBBMS(loid)));
			// 时间原有接口不能查询
			data.addElement("regTime").addText(regTime);
		}
		return createDocument.asXML();
	}

	/**
	 * 设备光功率
	 * 
	 * @author 岩
	 * @date 2016-11-1
	 * @param outXml
	 * @return
	 */
	public String otherToJlPon(String outXml) {
		logger.debug("XmlTranslate==>otherToJlPon()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		String status = null;
		String tXPower = null;
		String rXPower = null;
		String bytesSent = null;
		String bytesReceived = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
			status = root.elementTextTrim("Status");
			tXPower = root.elementTextTrim("TXPower");
			rXPower = root.elementTextTrim("RXPower");
			bytesSent = root.elementTextTrim("BytesSent");
			bytesReceived = root.elementTextTrim("BytesReceived");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		data.addElement("sendOptical").addText(tXPower);
		data.addElement("recvOptical").addText(rXPower);
		if ("0".equals(status)) {
			data.addElement("status").addText("up");
		} else {
			data.addElement("status").addText("down");
		}
		data.addElement("bytesSent").addText(bytesSent);
		data.addElement("bytesReceived").addText(bytesReceived);
		return createDocument.asXML();
	}

	/**
	 * 家庭网关LAN口状态查询接口
	 * 
	 * @author 岩
	 * @date 2016-11-1
	 * @param outXml
	 * @return
	 */
	public String otherToJlLAN(String outXml) {
		logger.debug("XmlTranslate==>otherToJlLAN()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		String lanName = null;
		String linkRate = null;
		String linkStats = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
			Element internet = root.element("Internet");
			lanName = internet.elementTextTrim("LanName");
			linkRate = internet.elementTextTrim("LinkRate");
			linkStats = internet.elementTextTrim("LinkStats");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		data.addElement("lanName").addText(lanName);
		data.addElement("linkRate").addText(linkRate);
		data.addElement("linkStats").addText(linkStats);
		return createDocument.asXML();
	}

	/**
	 * 家庭网关配置查询接口
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @param outXml
	 * @return
	 */
	public String otherToJlDevCon(String outXml) {
		logger.debug("XmlTranslate==>otherToJlDevCon()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		String wanType = "";
		String internetVlan = "";
		String iptvVlan = "";
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
			if ("0".equals(rstCode)) {
				Element internet = root.element("Internet");
				wanType = internet.elementTextTrim("WanType");
				internetVlan = internet.elementTextTrim("KdPvcOrVlanId");
				Element iptv = root.element("IPTV");
				iptvVlan = iptv.elementTextTrim("IPTVPvcOrVlanId");
			}
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		data.addElement("internetVlan").addText(internetVlan);
		data.addElement("itvVlan").addText(iptvVlan);
		data.addElement("single").addText(iptvVlan);
		if ("99".equals(iptvVlan)) {
			data.addElement("multi").addText("100");
		} else {
			data.addElement("multi").addText("");
		}
		if ("0".equals(rstCode)) {
			data.addElement("dhcp").addText("on");
			if ("2".equals(wanType)) {
				data.addElement("wifi").addText("on");
				data.addElement("accessType").addText("路由");
			} else {
				data.addElement("wifi").addText("off");
				data.addElement("accessType").addText("桥接");
			}
		} else {
			data.addElement("dhcp").addText("");
			data.addElement("wifi").addText("");
			data.addElement("accessType").addText("");
		}

		return createDocument.asXML();
	}

	/**
	 * 家庭网关远程恢复出厂设置接口/重启接口
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @param outXml
	 * @return
	 */
	public String otherToJlReset(String outXml) {
		logger.debug("XmlTranslate==>otherToJlReset()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		return createDocument.asXML();
	}

	/**
	 * 家庭网关管理密码查询接口
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @param outXml
	 * @return
	 */
	public String otherToJlCall(String outXml) {
		logger.debug("XmlTranslate==>otherToJlCall()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		String devPwd = "";
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
			Element param = root.element("Param");
			devPwd = param.elementTextTrim("DevPwd");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		data.addElement("psw").addText(devPwd);
		return createDocument.asXML();
	}

	/**
	 * 家庭网关业务下发接口
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @param outXml
	 * @return
	 */
	public String otherToJlDoService(String outXml) {
		logger.debug("XmlTranslate==>otherToJlDoService()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		return createDocument.asXML();
	}

	/**
	 * 机顶盒解绑接口
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @param outXml
	 * @return
	 */
	public String otherToJlStbUnbind(String outXml) {
		logger.debug("XmlTranslate==>otherToJlStbUnbind()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("1006".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("acc").addText(acc);
		return createDocument.asXML();
	}

	/**
	 * @author 岩
	 * @date 2016-11-2
	 * @param outXml
	 * @return
	 */
	public String otherToJlStbbind(String outXml) {
		logger.debug("XmlTranslate==>otherToJlStbbind()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("1006".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("acc").addText(acc);
		data.addElement("mac").addText(mac);
		return createDocument.asXML();
	}

	/**
	 * 机顶盒绑定模块acc和mac不匹配情况下出参
	 * 
	 * @author wangyan
	 * @date 2016-11-18
	 * @return
	 */
	public String otherToJlStbbind2() {
		logger.debug("XmlTranslate==>otherToJlStbbind()" + this.callXml);

		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		this.out_time = Long.valueOf(dt.getLongTime());
		this.outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(this.out_time));
		p.addElement("time").addText(this.outTime);
		p.addElement("success").addText("0");
		p.addElement("desc").addText("该业务账号与该mac地址在工单中不匹配");
		Element data = root.addElement("data");
		data.addElement("acc").addText(this.acc);
		data.addElement("mac").addText(this.mac);
		return createDocument.asXML();
	}

	/**
	 * 机顶盒重启接口
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @param outXml
	 * @return
	 */
	public String otherToJlStbReboot(String outXml) {
		logger.debug("XmlTranslate==>otherToJlStbReboot()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("result_flag");
			rstMsg = root.elementTextTrim("result");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("1".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("acc").addText(acc);
		return createDocument.asXML();
	}

	/**
	 * 机顶盒业务下发
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @param outXml
	 * @return
	 */
	public String otherToJlStbDoService(String outXml) {
		logger.debug("XmlTranslate==>otherToJlStbDoService()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("result_flag");
			rstMsg = root.elementTextTrim("result");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("1".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("acc").addText(acc);
		return createDocument.asXML();
	}

	/**
	 * 机顶盒状态查询接口
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @param outXml
	 * @return
	 */
	public String otherToJlStbOnline(String outXml) {
		logger.debug("XmlTranslate==>otherToJlStbOnline()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		String regTime = null;
		String stbStatus = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("result_flag");
			rstMsg = root.elementTextTrim("result");
			if (!"0".equals(rstCode)) {
				Element p = root.element("Param");
				stbStatus = p.elementTextTrim("OnlineStatus");
			}
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("1".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("acc").addText(acc);
		if ("0".equals(rstCode)) {
			if ("查无此设备".equals(rstMsg)){
				data.addElement("status").addText("");
			}else{
				data.addElement("status").addText("DOWN");
			}
		} else {
			if ("1".equals(stbStatus)) {
				data.addElement("status").addText("UP");
			} else {
				data.addElement("status").addText("DOWN");
			}
		}
		if (null == getStbCompleteTime(acc)
				|| "".equals(getStbCompleteTime(acc))) {
			data.addElement("regTime").addText("");
		} else {
			regTime = new DateTimeUtil().getLongDate(StringUtil
					.getLongValue(getStbCompleteTime(acc)));
			// 时间原有接口不能查询
			data.addElement("regTime").addText(regTime);
		}
		return createDocument.asXML();
	}

	/**
	 * 机顶盒修改密码失败情况下出参
	 * 
	 * @author wangyan
	 * @date 2016-11-18
	 * @return
	 */
	public String otherToJlStbUpPwd() {
		logger.debug("XmlTranslate==>otherToJlStbUpPwd()");

		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		this.out_time = Long.valueOf(dt.getLongTime());
		this.outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(this.out_time));
		p.addElement("time").addText(this.outTime);
		p.addElement("success").addText("0");
		p.addElement("desc").addText("acc不存在,修改密码失败");
		Element data = root.addElement("data");
		data.addElement("acc").addText(this.acc);
		return createDocument.asXML();
	}

	/**
	 * 机顶盒家庭网关设备信息查询接口出参
	 * 
	 * @author wangyan
	 * @date 2016-12-14
	 * @param outXml
	 * @return
	 */
	public String otherToJlDevState(String outXml) {
		logger.debug("XmlTranslate==>otherToJlDevState()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		String lanName = null;
		String linkRate = null;
		String linkStats = null;
		String status = null;
		String tXPower = null;
		String rXPower = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
			Element internet = root.element("Internet");
			lanName = internet.elementTextTrim("LanName");
			linkRate = internet.elementTextTrim("LinkRate");
			linkStats = internet.elementTextTrim("LinkStats");
			Element ponInfo = root.element("PonInfo");
			status = ponInfo.elementTextTrim("PonStat");
			tXPower = ponInfo.elementTextTrim("TXPower");
			rXPower = ponInfo.elementTextTrim("RXPower");
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		data.addElement("sendOptical").addText(tXPower);
		data.addElement("recvOptical").addText(rXPower);
		if ("Up".equals(status)) {
			data.addElement("status").addText("up");
		} else {
			data.addElement("status").addText("down");
		}
		data.addElement("lanName").addText(lanName);
		data.addElement("linkRate").addText(linkRate);
		data.addElement("linkStats").addText(linkStats);
		return createDocument.asXML();
	}

	/**
	 * 机顶盒版本信息查询接口出参
	 * 
	 * @author wangyan
	 * @date 2016-12-14
	 * @param outXml
	 * @return
	 */
	public String otherToJlDevVersion(String outXml) {
		logger.debug("XmlTranslate==>otherToJlDevVersion()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		String vendor = "";
		String model = "";
		String softwareVersion = "";
		String hardwareVersion = "";
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
			if ("0".equals(rstCode)){
				Element param = root.element("Param");
				vendor = param.elementTextTrim("vendor");
				model = param.elementTextTrim("DevModel");
				softwareVersion = param.elementTextTrim("SoftwareVersion");
				hardwareVersion = param.elementTextTrim("HandwareVersion");
			}
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil
				.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("mac").addText(mac);
		data.addElement("vendor").addText(vendor);
		data.addElement("model").addText(model);
		data.addElement("softwareVersion").addText(softwareVersion);
		data.addElement("hardwareVersion").addText(hardwareVersion);
		if ("0".equals(rstCode)) {
			data.addElement("conformStandard").addText("Y");
		}else{
			data.addElement("conformStandard").addText("");
		}
		return createDocument.asXML();
	}
	
	
	
	/**
	 * 吉林终端ping测试接收xml转换
	 * 
	 * @author fanjm 35572
	 * @date 2017-02-28
	 * @return xml
	 */
	public String jlToOtherPing() {
		logger.debug("XmlTranslate==>jlToOtherPing()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			Element p = root.element("public");
			time = p.elementTextTrim("time");
			Element data = root.element("data");
			loid = data.elementTextTrim("loid");
			wanPassageWay = data.elementTextTrim("wanPassageWay");
			packageByte = data.elementTextTrim("packageByte");
			ipOrDomainName = data.elementTextTrim("ipOrDomainName");
			packageNum = data.elementTextTrim("packageNum");
			timeOut = data.elementTextTrim("timeOut");
		} catch (Exception e) {
			logger.error("Parse xml error!");
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		root.addElement("CmdID").addText("123456789012345");
		root.addElement("CmdType").addText("CX_01");
		root.addElement("ClientType").addText("3");
		Element param = root.addElement("Param");
		param.addElement("UserInfoType").addText("2");
		param.addElement("UserInfo").addText(loid);
		
		param.addElement("CityId").addText("00");
		param.addElement("WanPassageWay").addText(wanPassageWay);
		param.addElement("PackageByte").addText(packageByte);
		param.addElement("IPOrDomainName").addText(ipOrDomainName);
		param.addElement("PackageNum").addText(packageNum);
		param.addElement("TimeOut").addText(timeOut);
		
		return createDocument.asXML();
	}
	
	
	
	
	
	
	/**
	 * 终端Ping测试
	 * 
	 * @author fanjm
	 * @date 2017-02-28
	 * @param outXml 原service返回xml
	 * @return xml
	 */
	public String otherToJlPing(String outXml) {
		logger.debug("XmlTranslate==>otherToJlPing()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		String succesNum = null;
		String failNum = null;
		String avgResponseTime = null;
		String minResponseTime = null;
		String maxResponseTime = null;
		String packetLossRate = null;
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
			succesNum = root.elementTextTrim("SuccesNum");
			failNum = root.elementTextTrim("FailNum");
			avgResponseTime = root.elementTextTrim("AvgResponseTime");
			minResponseTime = root.elementTextTrim("MinResponseTime");
			maxResponseTime = root.elementTextTrim("MaxResponseTime");
			packetLossRate = root.elementTextTrim("PacketLossRate");
			ipOrDomainName = root.elementTextTrim("IPOrDomainName");
		} catch (Exception e) {
			logger.error("Parse xml error!");
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		p.addElement("time").addText(new DateTimeUtil().getLongDate());
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		data.addElement("succesNum").addText(succesNum);
		data.addElement("failNum").addText(failNum);
		data.addElement("avgResponseTime").addText(avgResponseTime);
		data.addElement("minResponseTime").addText(minResponseTime);
		data.addElement("maxResponseTime").addText(maxResponseTime);
		data.addElement("packetLossRate").addText(packetLossRate);
		data.addElement("ipOrDomainName").addText(ipOrDomainName);
		
		return createDocument.asXML();
	}
	
	
	
	/**
	 * 吉林2.23.桥接和路由切换接口 -----按callService else地区流程走
	 * 
	 * @author fanjm 35572
	 * @date 2017-03-16
	 * @return xml
	 */
	public String jlToOtherBridgeToRout() {
		logger.debug("XmlTranslate==>jlToOtherBridgeToRout()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			Element p = root.element("public");
			time = p.elementTextTrim("time");
			Element data = root.element("data");
			loid = data.elementTextTrim("loid");
			wanType = data.elementTextTrim("wanType");
		} catch (Exception e) {
			logger.error("Parse xml error!");
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		root.addElement("CmdID").addText("123456789012345");
		root.addElement("CmdType").addText("CX_01");
		root.addElement("ClientType").addText("3");
		Element param = root.addElement("Param");
		param.addElement("UserInfoType").addText("2");
		param.addElement("UserInfo").addText(loid);
		param.addElement("wanType").addText(wanType);
		
		return createDocument.asXML();
	}
	
	
	
	
	
	
	/**
	 * 吉林2.23.桥接和路由切换接口 -----按callService else地区流程走
	 * 
	 * @author fanjm
	 * @date 2017-03-16
	 * @param outXml 原service返回xml
	 * @return xml
	 */
	public String otherToJlBridgeToRout(String outXml) {
		logger.debug("XmlTranslate==>otherToJlBridgeToRout()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		String failureReason = ""; //失败原因
		String succStatus = ""; //成功状态
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");
			loid = root.elementTextTrim("SN");
			failureReason = root.elementTextTrim("FailureReason");
			succStatus = root.elementTextTrim("SuccStatus");
		} catch (Exception e) {
			logger.error("Parse xml error!");
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		p.addElement("time").addText(new DateTimeUtil().getLongDate());
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		data.addElement("failureReason").addText(failureReason);
		data.addElement("succStatus").addText(succStatus);
		
		logger.debug("XmlTranslate==>otherToJlBridgeToRout()方法结束{}" + createDocument.asXML());
		return createDocument.asXML();
	}
	
	
	
	/**
	 * 根据Loid获取DevSn
	 * 
	 * @author 岩
	 * @date 2016-11-3
	 * @param userId
	 * @return
	 */
	public List<HashMap<String, String>> getDevSnByLoid(String loid) {
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select device_serialnumber,city_id from tab_hgwcustomer ");
		psql.append(" where 1=1 and username = '" + loid + "'");
		List<HashMap<String, String>> devSnList = DBOperation.getRecords(psql
				.getSQL());
		return devSnList;
	}

	public List<HashMap<String, String>> getDevSnByLoidBBMS(String loid) {
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select device_serialnumber,city_id from tab_egwcustomer ");
		psql.append(" where 1=1 and username = '" + loid + "'");
		List<HashMap<String, String>> devSnList = DBOperation.getRecords(psql
				.getSQL());
		return devSnList;
	}

	
	
	/**
	 * 根据LOID获取设备注册时间
	 * 
	 * @author 岩
	 * @date 2016-11-3
	 * @param loid
	 * @return
	 */
	public String getDevCompleteTime(String loid) {
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select a.complete_time from tab_gw_device a,tab_hgwcustomer b ");
		psql.append(" where 1=1 and a.device_id=b.device_id and b.username='"
				+ loid + "'");
		List<HashMap<String, String>> devTimeList = DBOperation.getRecords(psql
				.getSQL());
		if (devTimeList != null && !devTimeList.isEmpty()) {
			return StringUtil.getStringValue(devTimeList.get(0),
					"complete_time", "");
		} else {
			return "";
		}
	}

	public String getDevCompleteTimeBBMS(String loid) {
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select a.complete_time from tab_gw_device a,tab_egwcustomer b ");
		psql.append(" where 1=1 and a.device_id=b.device_id and b.username='"
				+ loid + "'");
		List<HashMap<String, String>> devTimeList = DBOperation.getRecords(psql
				.getSQL());
		if (devTimeList != null && !devTimeList.isEmpty()) {
			return StringUtil.getStringValue(devTimeList.get(0),
					"complete_time", "");
		} else {
			return "";
		}
	}
	/**
	 * 根据业务账号获取机顶盒注册时间
	 * 
	 * @author 岩
	 * @date 2016-11-3
	 * @param acc
	 * @return
	 */
	public String getStbCompleteTime(String acc) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.complete_time from stb_tab_gw_device a,stb_tab_customer b ");
		psql.append("where 1=1 and a.customer_id=b.customer_id and b.serv_account='"
				+ acc + "'");
		List<HashMap<String, String>> devSnList = DBOperation.getRecords(psql
				.getSQL());
		if (devSnList != null && !devSnList.isEmpty()) {
			return StringUtil.getStringValue(devSnList.get(0), "complete_time",
					"");
		} else {
			return "";
		}
	}

	/**
	 * 根据acc去获取mac地址，与原mac进行对比
	 * 
	 * @author wangyan
	 * @date 2016-11-18
	 * @param acc
	 * @return
	 */
	public String getStbMac(String acc) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.cpe_mac from stb_tab_customer a ");
		psql.append("where 1=1 and a.serv_account='" + acc + "'");
		List<HashMap<String, String>> userList = DBOperation.getRecords(psql
				.getSQL());
		if ((userList != null) && (!userList.isEmpty())) {
			return StringUtil.getStringValue(userList.get(0), "cpe_mac", "");
		} else {
			return "";
		}
	}

	/**
	 * 根据acc对ipoe,iptv进行密码修改
	 * 
	 * @author wangyan
	 * @date 2016-11-18
	 * @param newIptvPwd
	 * @param newIpoePwd
	 * @param userAccount
	 * @return
	 */
	public int updateSuperPwd(String newIptvPwd, String newIpoePwd,
			String userAccount) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("update stb_tab_customer ");
		psql.append(" set serv_pwd = '" + newIptvPwd + "', pppoe_pwd = '"
				+ newIpoePwd + "'");
		psql.append(" where 1=1");
		psql.append(" and serv_account = '" + userAccount + "'");
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	
	/**
	 * JLDX-REQ-20170814-JIANGHAO6-001(家庭网关单独LAN口状态查询接口)
	 * @date 20170817
	 * @return
	 */
	public String jlToOtherQuerySplitLanState() {
		logger.debug("XmlTranslate==>jlToOtherQuerySplitLanState()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			Element p = root.element("public");
			time = p.elementTextTrim("time");
			Element data = root.element("data");
			loid = data.elementTextTrim("loid");
			lanId = data.elementTextTrim("lanId");
			if(false==checkLanId(lanId)){
				return null;
			}
		} catch (Exception e) {
			return null;
		}
		
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		root.addElement("CmdID").addText("123456789012345");
		root.addElement("CmdType").addText("CX_01");
		root.addElement("ClientType").addText("3");
		Element param = root.addElement("Param");
		param.addElement("UserInfoType").addText("2");
		param.addElement("UserInfo").addText(loid);
		
		return createDocument.asXML();
	}
	
	
	/**
	 * JLDX-REQ-20170814-JIANGHAO6-001(家庭网关单独LAN口状态查询接口)
	 * @date 20170817
	 * @param outXml
	 * @return
	 */
	public String otherToJlQuerySplitLanState(String outXml) {
		logger.debug("XmlTranslate==>otherToJlQuerySplitLanState()" + callXml);
		SAXReader reader = new SAXReader();
		Document document = null;
		
		String lanPortNumResult = "";
		String rstStateResult = "";	
		try {
			document = reader.read(new StringReader(outXml));
			Element root = document.getRootElement();
			rstCode = root.elementTextTrim("RstCode");
			rstMsg = root.elementTextTrim("RstMsg");			
			List<String> lanIdList = new ArrayList<String>();
			
			if(!StringUtil.IsEmpty(lanId)){
				String[] lanIdArr = lanId.split(",");
				if(null!=lanIdArr){
					for(int i=0;i<lanIdArr.length;i++){
						if(!StringUtil.IsEmpty(lanIdArr[i])){
							lanIdList.add(lanIdArr[i]);
						}
					}
				}
			}
			
			for(String lanIdStr : lanIdList){				
				boolean flag = false;				
				Iterator accountIter = root.element("LanPort").elementIterator("LanPort");
				while (accountIter.hasNext()) {
					Element accElement = (Element) accountIter.next();
					String lanPortNum = accElement.elementTextTrim("LanPortNUM");
					String rstState = accElement.elementTextTrim("RstState");
					if(!StringUtil.IsEmpty(lanIdStr) && lanIdStr.equals(lanPortNum)){
						lanPortNumResult += "LAN"+lanIdStr+",";
						rstStateResult += rstState+",";
						flag = true;
					}
				}				
				if(!flag){
					lanPortNumResult += "LAN"+lanIdStr+",";
					rstStateResult += "LAN"+lanIdStr+"没有采集结果,";
				}
			}
		} catch (Exception e) {
			return null;
		}
		Document createDocument = DocumentHelper.createDocument();
		createDocument.setXMLEncoding("GBK");
		Element root = createDocument.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		DateTimeUtil dt = new DateTimeUtil();
		out_time = dt.getLongTime();
		outTime = new DateTimeUtil().getLongDate(StringUtil.getLongValue(out_time));
		p.addElement("time").addText(outTime);
		if ("0".equals(rstCode)) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(rstMsg);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		
		if(!StringUtil.IsEmpty(lanPortNumResult) && lanPortNumResult.endsWith(",")){
			lanPortNumResult = lanPortNumResult.substring(0,lanPortNumResult.length()-1);
		}
		
		if(!StringUtil.IsEmpty(rstStateResult) && rstStateResult.endsWith(",")){
			rstStateResult = rstStateResult.substring(0,rstStateResult.length()-1);
		}
		
		if(!"0".equals(rstCode)){
			lanPortNumResult = "";
			rstStateResult = "";
		}
		
		data.addElement("lanName").addText(lanPortNumResult);
		data.addElement("linkStats").addText(rstStateResult);
		return createDocument.asXML();
	}
	
	public boolean checkLanId(String lanId){
		if(StringUtil.IsEmpty(lanId)){
			return false;
		}
		String[] lanIdArr = lanId.split(",");
		
		if(null==lanIdArr || lanIdArr.length==0){
			return false;
		}
		
		for(int i=0;i<lanIdArr.length;i++){
			String lanIdStr = lanIdArr[i];
			try {
				Integer.parseInt(lanIdStr);
			} catch (Exception e) {
				return false;
			}
		}
		
		int count = 0;
		for(int i=0;i<lanId.length();i++){
			String lanIdTemp = lanId.substring(i,i+1);
			if(",".equals(lanIdTemp)){
				count++;
			}
		}		
		if(count!=(lanIdArr.length-1)){
			return false;
		}
		
		return true;
		
	}
}
