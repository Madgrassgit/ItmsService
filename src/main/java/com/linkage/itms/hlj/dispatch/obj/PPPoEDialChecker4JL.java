package com.linkage.itms.hlj.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.itms.dispatch.obj.BaseChecker;


public class PPPoEDialChecker4JL extends BaseChecker{

	
	private static Logger logger = LoggerFactory.getLogger(PPPoEDialChecker4JL.class);
	
	/**
	 * 接口调用时间
	 * 格式：YYYY-MM-DD hh:mm:ss
	 * 样例：2016-03-11 12:33:00
	 */
	private String time;
	
	/**
	 * Wan通道
	 */
	private String wanPassageWay = "";
	
	
	/**
	 * REQUEST
	 */
	private String type  = "";

	
	/**
	 * pppoe用户名
	 */
	private String pppoeUser = "";

	/**
	 * 认证模式
	 */
	private String authenticationMode = "";

	/**
	 * pppoe密码
	 */
	private String pppoePassword = "";

	/**
	 * 重复次数
	 */
	private String repeatTimes = ""; 

	/**
	 * 诊断状态
	 * "None"   空
	 * "Start"  开始
	 * "Stop"   停止
	 * "Complete" 完成
	 * "Running"  运行中
	 */
	private String diagnosticStatus = "";
	
	/**
	 * 诊断结果
	 * "Success" 成功
	 * "ParamNegoFail" 协商参数失败
	 * "UserAuthenticationFail" 认证失败
	 * "Timeout" 超时
	 * "UserStop" 用户停止
	 * "unknown" 未知错误
	 */
	private String diagnosticResult = "";

	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public PPPoEDialChecker4JL(String inXml) {
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
			Element p = root.element("public");
			type = p.elementTextTrim("type");
			time = p.elementTextTrim("time");
			Element data = root.element("data");
			loid = data.elementTextTrim("loid");
			pppoeUser = data.elementTextTrim("pppoeUser");
			pppoePassword = data.elementTextTrim("pppoePassword");
			//wanPassageWay = data.elementTextTrim("wanPassageWay");
			authenticationMode = data.elementTextTrim("authenticationMode");
			repeatTimes = data.elementTextTrim("repeatTimes");
		} catch (Exception e) {
			e.printStackTrace();
			result = 0;
			resultDesc = "数据格式错误";
			return false;
		}

		// 参数合法性检查
		if (!checkParam()) {
			return false;
		}
		
		
		result = 1;
		resultDesc = "成功";
		
		return true;
	}
	
	public boolean checkParam(){
		logger.debug("checkParam()");
		
		if(StringUtil.IsEmpty(type)||!"REQUEST".equals(type)){
			result = 0;
			resultDesc = "接口类型非法";
			return false;
		}
		else if(StringUtil.IsEmpty(time)){
			result = 0;
			resultDesc = "接口调用时间非法";
			return false;
		}
		else if(StringUtil.IsEmpty(pppoeUser)){
			result = 0;
			resultDesc = "pppoe用户名非法";
			return false;
		}
		else if(StringUtil.IsEmpty(pppoePassword)){
			result = 0;
			resultDesc = "pppoe密码非法";
			return false;
		}
		/*else if(StringUtil.IsEmpty(wanPassageWay)){
			result = 0;
			resultDesc = "Wan通道非法";
			return false;
		}*/
		else if(StringUtil.IsEmpty(authenticationMode)){
			result = 0;
			resultDesc = "认证模式非法";
			return false;
		}
		else if(StringUtil.IsEmpty(repeatTimes)){
			result = 0;
			resultDesc = "重复次数非法";
			return false;
		}
		return true;
	}
	
	
	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		Element p = root.addElement("public");
		p.addElement("type").addText("RESPONSE");
		p.addElement("time").addText(new DateTimeUtil().getLongDate());
		if (1==result) {
			p.addElement("success").addText("1");
		} else {
			p.addElement("success").addText("0");
		}
		p.addElement("desc").addText(resultDesc);
		Element data = root.addElement("data");
		data.addElement("loid").addText(loid);
		data.addElement("diagnosticStatus").addText(diagnosticStatus);
		data.addElement("diagnosticResult").addText(diagnosticResult);
		
		return document.asXML();
	}
	
	
	
	public String getPppoeUser() {
		return pppoeUser;
	}
	public String getAuthenticationMode() {
		return authenticationMode;
	}
	public String getPppoePassword() {
		return pppoePassword;
	}
	public String getRepeatTimes() {
		return repeatTimes;
	}
	public String getDiagnosticStatus() {
		return diagnosticStatus;
	}
	public String getDiagnosticResult() {
		return diagnosticResult;
	}
	public void setPppoeUser(String pppoeUser) {
		this.pppoeUser = pppoeUser;
	}
	public void setAuthenticationMode(String authenticationMode) {
		this.authenticationMode = authenticationMode;
	}
	public void setPppoePassword(String pppoePassword) {
		this.pppoePassword = pppoePassword;
	}
	public void setRepeatTimes(String repeatTimes) {
		this.repeatTimes = repeatTimes;
	}
	public void setDiagnosticStatus(String diagnosticStatus) {
		this.diagnosticStatus = diagnosticStatus;
	}
	public void setDiagnosticResult(String diagnosticResult) {
		this.diagnosticResult = diagnosticResult;
	}
	public String getWanPassageWay() {
		return wanPassageWay;
	}
	
	public void setWanPassageWay(String wanPassageWay) {
		this.wanPassageWay = wanPassageWay;
	}
	
	public String getTime() {
		return time;
	}
	public String getType() {
		return type;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public void setType(String type) {
		this.type = type;
	}

}
