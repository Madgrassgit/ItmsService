
package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

/**
 * 江西电信ITMS+家庭网关对外接口-2.31新增lan1口协商速率查询接口
 * @author songxq
 * @version 1.0
 * @since 2020-6-17
 */
public class QueryRouterSpeedChecker extends BaseChecker
{

	public QueryRouterSpeedChecker(String inXml)
	{
		this.callXml = inXml;
	}

	private static Logger logger = LoggerFactory
			.getLogger(QueryRouterSpeedChecker.class);

	private String cityId = "";

	private String userName = "";

	private String devModel = "";

	private String devSN = "";

	private String routerSpeed = "";

	private String devType = "";

	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			// 入参解析
			userInfo = param.elementTextTrim("UserInfo");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		if(2 != userInfoType && 1 != userInfoType){
			result = 2;
			resultDesc = "用户信息类型非法";
			return false;
		}
		
		if (StringUtil.IsEmpty(userInfo))
		{
			result = 1;
			resultDesc = "用户信息不能为空";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck())
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);

		// 属地
		root.addElement("CityId").addText(cityId);

		// 宽带账号
		root.addElement("UserName").addText(userName);

		// 设备类型
		root.addElement("DevModel").addText(devModel);

		// 设备序列号
		root.addElement("DevSN").addText(devSN);

		// 路由器适配速率
		root.addElement("RouterSpeed").addText(routerSpeed);

		// 光猫类型
		root.addElement("DevType").addText(devType);

		return document.asXML();
	}

	@Override
	public String getCityId() {
		return cityId;
	}

	@Override
	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDevModel() {
		return devModel;
	}

	public void setDevModel(String devModel) {
		this.devModel = devModel;
	}

	public String getDevSN() {
		return devSN;
	}

	public void setDevSN(String devSN) {
		this.devSN = devSN;
	}

	public String getRouterSpeed() {
		return routerSpeed;
	}

	public void setRouterSpeed(String routerSpeed) {
		this.routerSpeed = routerSpeed;
	}

	public String getDevType() {
		return devType;
	}

	public void setDevType(String devType) {
		this.devType = devType;
	}
}
