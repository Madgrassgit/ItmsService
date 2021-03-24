package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;


public class PingDiagnosticChecker extends BaseChecker {

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(PingDiagnosticChecker.class);
	
	/**
	 * Wan通道
	 */
	private String wanPassageWay = null;
	
	/**
	 * 包大小（Byte）
	 */
	private String packageByte = null;
	
	/**
	 * 测试IP或域名
	 */
	private String iPOrDomainName = null;
	
	/**
	 * 包数目
	 */
	private String packageNum = null;
	
	/**
	 * 超时时间（ms）
	 */
	private String timeOut = null;
	
	/**
	 * 成功数
	 */
	private String succesNum = null;
	
	/**
	 * 失败数
	 */
	private String failNum = null;
	
	/**
	 * 平均响应时间
	 */
	private String avgResponseTime = null;
	
	/**
	 * 最小响应时间
	 */
	private String minResponseTime = null;
	
	/**
	 * 最大响应时间
	 */
	private String maxResponseTime = null;
	
	/**
	 * 丢包率
	 */
	private String packetLossRate = null;
	
	
	/**
	 * true 表示从web调用过来的  其他表示外部系统接口
	 */
	private String isWebInvoke=""; 
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public PingDiagnosticChecker(String inXml) {
		callXml = inXml;
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
		logger.warn("Global.G_instArea======" + Global.G_instArea);
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			isWebInvoke= root.elementTextTrim("IsWebInvoke")==null?"":root.elementTextTrim("IsWebInvoke");
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			
			// chenxj6 xj : 用户信息类型:1：用户宽带帐号;2：LOID;3：IPTV宽带帐号;4：VOIP业务电话号码;5：VOIP认证帐号
			if ("xj_dx".equals(Global.G_instArea) || "nx_dx".equals(Global.G_instArea) || "jl_dx".equals(Global.G_instArea)
					|| "cq_dx".equals(Global.G_instArea) || "jl_lt".equals(Global.G_instArea)){
				userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
				userInfo = param.elementTextTrim("UserInfo");
			}else if("jx_dx".equals(Global.G_instArea)){
				userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));
				userInfo = param.elementTextTrim("UserInfo");
				devSn = param.elementTextTrim("DevSN");
				searchType = StringUtil.getIntegerValue(param.elementTextTrim("SearchType"));
			}else{
				devSn = param.elementTextTrim("DevSn");
				oui = param.elementTextTrim("OUI");
			}
			
			//注意参数大小写  web上调用和外部系统调用不一致,web页面调用取下面值
			if(isWebInvoke.equals("true")) {
				devSn = param.elementTextTrim("DevSn");
				oui = param.elementTextTrim("OUI");
			}
			
			cityId = param.elementTextTrim("CityId");
			wanPassageWay = param.elementTextTrim("WanPassageWay");
			packageByte = param.elementTextTrim("PackageByte");
			iPOrDomainName = param.elementTextTrim("IPOrDomainName");
			packageNum = param.elementTextTrim("PackageNum");
			timeOut = param.elementTextTrim("TimeOut");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		//如果不是从web页面调用的 ,增加下面省份判断 ,代表是外部接口调用 增加参数校验
		if(!isWebInvoke.equals("true")) {
		
		// chenxj6 xj
		if ("xj_dx".equals(Global.G_instArea)) {
			if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType && 5 != clientType) {
				result = 2;
				resultDesc = "客户端类型非法";
				return false;
			}
			if(1 != userInfoType && 2 != userInfoType && 3 != userInfoType && 4 != userInfoType && 5 != userInfoType){
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
			if (false == baseCheck() || false == cityIdCheck()) {
				return false;
			}
		}
		// fanjm nx
		else if ("nx_dx".equals(Global.G_instArea) || "jl_dx".equals(Global.G_instArea) || "cq_dx".equals(Global.G_instArea) || "jl_lt".equals(Global.G_instArea)) {
			if("jl_lt".equals(Global.G_instArea) && 1 != userInfoType && 2 != userInfoType){
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
			if(1 != userInfoType && 2 != userInfoType && 3 != userInfoType && 4 != userInfoType && 5 != userInfoType  && 6 != userInfoType  && 7 != userInfoType){
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
			else if(StringUtil.IsEmpty(userInfo)||StringUtil.IsEmpty(wanPassageWay)||StringUtil.IsEmpty(packageByte)||StringUtil.IsEmpty(iPOrDomainName)
					||StringUtil.IsEmpty(packageNum)||StringUtil.IsEmpty(timeOut)){
				result = 1;
				resultDesc = "参数为空";
				return false;
			}
			else if(!"1".equals(wanPassageWay)&&!"2".equals(wanPassageWay)){
				result = 1;
				resultDesc = "Wan通道非法";
				return false;
			}
			if (false == baseCheck() || false == cityIdCheck()) {
				return false;
			}
		}
		else if("jx_dx".equals(Global.G_instArea)){
			if(StringUtil.IsEmpty(cmdId)){
				result = 1000;
				resultDesc = "接口调用唯一ID非法";
				return false;
			}
			if(false == "CX_01".equals(cmdType)){
				result = 3;
				resultDesc = "接口类型非法";
				return false;
			}
			if (1 != searchType && 2 != searchType) {
				result = 2;
				resultDesc = "查询类型非法";
				return false;
			}
			
			if (1 != clientType && 2 != clientType && 3 != clientType && 4 != clientType && 5 != clientType && 6 != clientType && 7 != clientType) {
				result = 2;
				resultDesc = "客户端类型非法";
				return false;
			}
			if(2 == searchType){
				if(StringUtil.IsEmpty(devSn) || devSn.length() < 6){
					result = 2;
					resultDesc = "设备序列号非法";
					return false;
				}
			}
			if(1 == searchType){
				if(1 != userInfoType && 2 != userInfoType && 3 != userInfoType && 4 != userInfoType && 5 != userInfoType){
					result = 1002;
					resultDesc = "用户信息类型非法";
					return false;
				}
				if(StringUtil.IsEmpty(userInfo)){
					result = 1;
					resultDesc = "参数为空";
					return false;
				}
				if(StringUtil.IsEmpty(cityId) || false == Global.G_CityId_CityName_Map.containsKey(cityId)){
					result = 1007;
					resultDesc = "属地为空或者非法";
					return false;
				}
			}
			if(StringUtil.IsEmpty(wanPassageWay)||StringUtil.IsEmpty(packageByte)||StringUtil.IsEmpty(iPOrDomainName)
					||StringUtil.IsEmpty(packageNum)||StringUtil.IsEmpty(timeOut)){
				result = 1;
				resultDesc = "参数为空";
				return false;
			}
			if(!"1".equals(wanPassageWay)&&!"2".equals(wanPassageWay)){
				result = 1;
				resultDesc = "Wan通道非法";
				return false;
			}
		}
		else {
			// 参数合法性检查
			if (false == baseCheck() || false == devSnCheck()
					|| false == cityIdCheck() || false == ouiCheck()) {
				return false;
			}
		}
	
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
		root.addElement("RstCode").addText(""+result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		root.addElement("DevSn").addText(devSn==null?"":devSn);
		root.addElement("SuccesNum").addText(succesNum==null?"":succesNum);
		root.addElement("FailNum").addText(failNum==null?"":failNum);
		root.addElement("AvgResponseTime").addText(avgResponseTime==null?"":avgResponseTime);
		root.addElement("MinResponseTime").addText(minResponseTime==null?"":minResponseTime);
		root.addElement("MaxResponseTime").addText(maxResponseTime==null?"":maxResponseTime);
		root.addElement("PacketLossRate").addText(packetLossRate==null?"":packetLossRate);
		root.addElement("IPOrDomainName").addText(iPOrDomainName==null?"":iPOrDomainName);
		
		if("jx_dx".equals(Global.G_instArea)){
			root.addElement("UserInfo").addText(userInfo);
			
			String avg_time = "0";
			if(StringUtil.getIntegerValue(avgResponseTime,0) > 0){
				avg_time = "1";
			}
			root.addElement("Result").addText(avg_time);
		}
		return document.asXML();
	}
	
	public String getWanPassageWay() {
		return wanPassageWay;
	}
	
	public void setWanPassageWay(String wanPassageWay) {
		this.wanPassageWay = wanPassageWay;
	}
	
	public String getPackageByte() {
		return packageByte;
	}
	
	public void setPackageByte(String packageByte) {
		this.packageByte = packageByte;
	}
	
	public String getiPOrDomainName() {
		return iPOrDomainName;
	}
	
	public void setiPOrDomainName(String iPOrDomainName) {
		this.iPOrDomainName = iPOrDomainName;
	}
	
	public String getPackageNum() {
		return packageNum;
	}
	
	public void setPackageNum(String packageNum) {
		this.packageNum = packageNum;
	}
	
	public String getTimeOut() {
		return timeOut;
	}
	
	public void setTimeOut(String timeOut) {
		this.timeOut = timeOut;
	}
	
	public String getSuccesNum() {
		return succesNum;
	}
	
	public void setSuccesNum(String succesNum) {
		this.succesNum = succesNum;
	}
	
	public String getFailNum() {
		return failNum;
	}
	
	public void setFailNum(String failNum) {
		this.failNum = failNum;
	}
	
	public String getAvgResponseTime() {
		return avgResponseTime;
	}
	
	public void setAvgResponseTime(String avgResponseTime) {
		this.avgResponseTime = avgResponseTime;
	}
	
	public String getMinResponseTime() {
		return minResponseTime;
	}
	
	public void setMinResponseTime(String minResponseTime) {
		this.minResponseTime = minResponseTime;
	}
	
	public String getMaxResponseTime() {
		return maxResponseTime;
	}
	
	public void setMaxResponseTime(String maxResponseTime) {
		this.maxResponseTime = maxResponseTime;
	}
	
	public String getPacketLossRate() {
		return packetLossRate;
	}
	
	public void setPacketLossRate(String packetLossRate) {
		this.packetLossRate = packetLossRate;
	}
	
	public String getIsWebInvoke() {
		return isWebInvoke;
	}
	
	public void setIsWebInvoke(String isWebInvoke) {
		this.isWebInvoke = isWebInvoke;
	}
}
