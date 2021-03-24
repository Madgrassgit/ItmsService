package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 河北联通端到端测试接口
 * @author Administrator
 *
 */
public class TestSpeedHBLTChecker extends BaseChecker{

private static final Logger logger = LoggerFactory.getLogger(TestSpeedHBLTChecker.class);
	
	private String inParam = null;
	
	// 宽带账号
	private String netAccount = null;
	
	// 上报URL
	private String testSpeedReportUrl = null;
	
	// 测速URL
	private String testSpeedDownUrl = null;
	
	// 测速账号
	private String userName = null;
	
	// 测速密码
	private String password = null;
	
	// 预留河北多宽带参数
	private String vlanId = null;
	private String conType = null;
	
	private String devSn = null;
	private String testIP = null;
	private String avgSpeed = null;
	private String maxSpeed = null;
	private String signSpeed = null;
	private String starttime = null;
	private String endtime = null;
	private String processState = null;

	public TestSpeedHBLTChecker(String inParam){
		this.inParam = inParam;
	}
	
	/**
	 * 
	 * 检查入参合法性
	 * 
	 * @return
	 */
	public boolean check(){
		
		logger.debug("TestSpeedChecker==>check()");
		
		SAXReader reader = new SAXReader();
		Document document = null;
		
		try {

			document = reader.read(new StringReader(inParam));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			// 1、用户LOID，2：设备序列号
			userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo"); 
			netAccount = param.elementTextTrim("NetAccount"); 
			testSpeedReportUrl = param.elementTextTrim("TestSpeedReportUrl"); 
			testSpeedDownUrl = param.elementTextTrim("TestSpeedDownUrl"); 
			
		} catch (Exception e) {
			logger.error("inParam format is err,mesg({})", e.getMessage());
			//赋一个值，否则getReturnXml的addtext改字段会出现错误造成调用失败
			cmdId="123456789012345";
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
	
	
	/**
	 * 返回调用结果字符串
	 * 
	 * @author fanjm(35572)
	 * @date 2016-11-29
	 * @return boolean 是否校验通过
	 */
	@Override
	public String getReturnXml(){
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(cmdId);
		// 结果代码
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		if(0 == result){
			// 设备序列号
			root.addElement("DevSn").addText(StringUtil.getStringValue(devSn));
			
			// 测速宽带账号
			root.addElement("TestAccount").addText(StringUtil.getStringValue(netAccount));
			
			// 测速IP
			root.addElement("TestIP").addText(StringUtil.getStringValue(testIP));
			
			// 平均速率
			root.addElement("AvgSpeed").addText(StringUtil.getStringValue(avgSpeed));
			
			// 最大速率
			root.addElement("MaxSpeed").addText(StringUtil.getStringValue(maxSpeed));
			
			// 签约速率
			root.addElement("SignSpeed").addText(StringUtil.getStringValue(signSpeed));
			
			// 开始时间
			root.addElement("Starttime").addText(StringUtil.getStringValue(starttime));
			
			// 结束时间
			root.addElement("Endtime").addText(StringUtil.getStringValue(endtime));
			
			// 处理状态
			root.addElement("ProcessState").addText(StringUtil.getStringValue(processState));
		}
		return document.asXML();
	}
	
	
	/**
	 * 基本信息合法性检查
	 * 
	 * @author fanjm(35572)
	 * @date 2016-11-29
	 * @return boolean 是否校验通过
	 */
	public boolean baseCheck(){
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)){
			result = 4;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		
		if(3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType && 5 != clientType){
			result = 2;
			resultDesc = "客户端类型非法";
			return false;
		}
		
		if(false == "CX_01".equals(cmdType)){
			result = 3;
			resultDesc = "接口类型非法";
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * 用户信息类型合法性检查
	 * 
	 * @author fanjm(35572)
	 * @date 2016-11-29
	 * @return boolean 是否校验通过
	 */
	boolean userInfoTypeCheck(){
		if(2 != userInfoType && 6 != userInfoType){
			result = 5;
			resultDesc = "用户信息类型非法";
			return false;
		}
		return true;
	}
	
	/**
	 * 用户信息合法性检查
	 */
	protected boolean userInfoCheck(){
		if(StringUtil.IsEmpty(userInfo)){
			result = 6;
			resultDesc = "用户信息不合法";
			return false;
		}
		return true;
	}
	
	/**
	 * 宽带账号、上报URL、测速URL校验
	 * @return
	 */
	private boolean otherParamCheck(){
		if(StringUtil.IsEmpty(netAccount)){
			result = 7;
			resultDesc = "宽带账号非法";
			return false;
		}
		
		if(StringUtil.IsEmpty(testSpeedReportUrl)){
			result = 8;
			resultDesc = "上报URL非法";
			return false;
		}
		if(StringUtil.IsEmpty(testSpeedDownUrl)){
			result = 9;
			resultDesc = "测速URL非法";
			return false;
		}
		return true;
	}
	
	
	public String getInParam() {
		return inParam;
	}

	
	public void setInParam(String inParam) {
		this.inParam = inParam;
	}

	public String getNetAccount() {
		return netAccount;
	}

	public void setNetAccount(String netAccount) {
		this.netAccount = netAccount;
	}

	public String getTestSpeedReportUrl() {
		return testSpeedReportUrl;
	}

	public void setTestSpeedReportUrl(String testSpeedReportUrl) {
		this.testSpeedReportUrl = testSpeedReportUrl;
	}

	public String getTestSpeedDownUrl() {
		return testSpeedDownUrl;
	}

	public void setTestSpeedDownUrl(String testSpeedDownUrl) {
		this.testSpeedDownUrl = testSpeedDownUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVlanId() {
		return vlanId;
	}

	public void setVlanId(String vlanId) {
		this.vlanId = vlanId;
	}

	public String getConType() {
		return conType;
	}

	public void setConType(String conType) {
		this.conType = conType;
	}

	public String getDevSn() {
		return devSn;
	}

	public void setDevSn(String devSn) {
		this.devSn = devSn;
	}

	public String getTestIP() {
		return testIP;
	}

	public void setTestIP(String testIP) {
		this.testIP = testIP;
	}

	public String getAvgSpeed() {
		return avgSpeed;
	}

	public void setAvgSpeed(String avgSpeed) {
		this.avgSpeed = avgSpeed;
	}

	public String getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(String maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public String getSignSpeed() {
		return signSpeed;
	}

	public void setSignSpeed(String signSpeed) {
		this.signSpeed = signSpeed;
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

	public String getProcessState() {
		return processState;
	}

	public void setProcessState(String processState) {
		this.processState = processState;
	}
}
