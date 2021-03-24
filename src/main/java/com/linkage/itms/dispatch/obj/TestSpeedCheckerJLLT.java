package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;


public class TestSpeedCheckerJLLT extends BaseChecker{


	private static Logger logger = LoggerFactory.getLogger(TestSpeedCheckerJLLT.class);


	private String status = "";

	private String opId = "";

	private String Aspeed = null;

	private String Bspeed = null;

	private String Cspeed = null;

	private String maxspeed = null;

	private String starttime = null;

	private String endtime = null;

	private String userSpeed=null;

	private String speedTest_downloadURL=null;

	private String speedTest_reportURL=null;

	private String netAccount=null;

	private String speedTest_testMode="serverMode";

	private String city_id = null;
	/**
	 * 构造方法
	 *
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public TestSpeedCheckerJLLT(String inXml) {
		this.callXml = inXml;
	}
	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = StringUtil.getStringValue(param.elementTextTrim("UserInfo"));
			userSpeed = StringUtil.getStringValue(param.elementTextTrim("UserSpeed"));
			opId = StringUtil.getStringValue(param.elementTextTrim("op_id"));
			speedTest_downloadURL = StringUtil.getStringValue(param.elementTextTrim("TestSpeedDownUrl"));
			speedTest_reportURL = StringUtil.getStringValue(param.elementTextTrim("TestSpeedReportUrl"));

			if(StringUtil.IsEmpty(opId)){
				opId = StringUtil.getStringValue(new DateTimeUtil().getLongTime()) + StringUtil.getStringValue((int)(Math.random()*1000));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}

		//参数合法性检查
		if (!baseCheck() || !userInfoTypeCheck() || !userInfoCheck() || !otherParamCheck()) {
			return false;
		}


		result = 0;
		resultDesc = "成功";
		
		return true;
	}

	private boolean otherParamCheck() {
		if(StringUtil.IsEmpty(userSpeed)||StringUtil.IsEmpty(speedTest_downloadURL)||StringUtil.IsEmpty(speedTest_reportURL)){
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		return true;

	}

	@Override
	public String getReturnXml(){
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(""+result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		root.addElement("NetAccount").addText(netAccount==null?"":netAccount);
		root.addElement("AvgSpeed ").addText(Aspeed ==null?"":Aspeed );
		root.addElement("SignSpeed").addText(Bspeed==null?"":Bspeed);
		root.addElement("CurrentSpeed").addText(Cspeed==null?"":Cspeed);
		root.addElement("MaxSpeed").addText(maxspeed==null?"":maxspeed);
		root.addElement("starttime").addText(starttime==null?"":starttime);
		root.addElement("endtime").addText(endtime==null?"":endtime);
		root.addElement("ProcessState").addText(status==null?"":status);
		return document.asXML();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOpId() {
		return opId;
	}

	public void setOpId(String opId) {
		this.opId = opId;
	}

	public String getAspeed() {
		return Aspeed;
	}

	public void setAspeed(String aspeed) {
		Aspeed = aspeed;
	}

	public String getBspeed() {
		return Bspeed;
	}

	public void setBspeed(String bspeed) {
		Bspeed = bspeed;
	}

	public String getCspeed() {
		return Cspeed;
	}

	public void setCspeed(String cspeed) {
		Cspeed = cspeed;
	}

	public String getMaxspeed() {
		return maxspeed;
	}

	public void setMaxspeed(String maxspeed) {
		this.maxspeed = maxspeed;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}



	public String getUserSpeed() {
		return userSpeed;
	}

	public void setUserSpeed(String userSpeed) {
		this.userSpeed = userSpeed;
	}

	public String getSpeedTest_downloadURL() {
		return speedTest_downloadURL;
	}

	public void setSpeedTest_downloadURL(String speedTest_downloadURL) {
		this.speedTest_downloadURL = speedTest_downloadURL;
	}

	public String getSpeedTest_reportURL() {
		return speedTest_reportURL;
	}

	public void setSpeedTest_reportURL(String speedTest_reportURL) {
		this.speedTest_reportURL = speedTest_reportURL;
	}

	public String getNetAccount() {
		return netAccount;
	}

	public void setNetAccount(String netAccount) {
		this.netAccount = netAccount;
	}

	public String getSpeedTest_testMode() {
		return speedTest_testMode;
	}

	public void setSpeedTest_testMode(String speedTest_testMode) {
		this.speedTest_testMode = speedTest_testMode;
	}

	public String getCity_id() {
		return city_id;
	}

	public void setCity_id(String city_id) {
		this.city_id = city_id;
	}
}
