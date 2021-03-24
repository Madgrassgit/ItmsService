package com.linkage.itms.dispatch.obj;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * 接口数据检查基类(抽象类)
 * 
 * @author Jason(3412)
 * @date 2010-6-17
 */
public abstract class BaseChecker {
	
	private static Logger logger = LoggerFactory.getLogger(BaseChecker.class);
	
	// IP正则表达式
	private static String ipPattern = "(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";
	
	// 正则，字符加数字
	static Pattern pattern = Pattern.compile("\\w{1,}+");
	
	// 正则，mac地址
	static Pattern patternMac = Pattern.compile("[A-F\\d]{2}:[A-F\\d]{2}:[A-F\\d]{2}:[A-F\\d]{2}:[A-F\\d]{2}:[A-F\\d]{2}");
	static Pattern patternMac_jl = Pattern.compile("[a-f\\d]{2}:[a-f\\d]{2}:[a-f\\d]{2}:[a-f\\d]{2}:[a-f\\d]{2}:[a-f\\d]{2}");

	// 客户端调用XML字符串
	protected String callXml;
	// 调用ID
	protected String cmdId;
	// 调用类型：CX_01,固定
	protected String cmdType;
	// 调用客户端类型：1：BSS 2：IPOSS 3：综调 4：RADIUS 5:掌上运维 6：预处理 7：云网端到端
	protected int clientType;

	// 查询类型: 1：客户账号 2：设备序列号
	protected int searchType;
	
	protected String userName;
	//用户信息类型
	/**
	1：用户宽带帐号
	2：逻辑SN号
	3：IPTV宽带帐号
	4：VOIP业务电话号码
	5：VOIP认证帐号
	6：设备序列号
	*/
	protected int userInfoType;
	//用户信息
	protected String userInfo;
	
	//终端信息
	protected String devSn = "";
	
	// 厂商OUI
	protected String oui;
	
	// IP地址
	protected String ip;
		
	// 网关
	protected String gateWay;
	
	//属地ID
	protected String cityId;
	
	//属地名称
	protected String cityName;
	
	// 用户类型标识，1：表示家庭网关，2：表示企业网关
	protected String gwType = null;
	
	// 查询结果
	protected int result;
	// 查询结果描述
	protected String resultDesc;
	
	// 新疆FTTH用户逻辑标识
	protected String loid ;
	
	protected String authUser ;
	protected String authPwd ;

	/**
	 * 检查客户端的XML字符串是否合法，如果合法将字符串转换成对象的属性
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-4-1
	 * @return boolean
	 */
	public abstract boolean check();

	/**
	 * 返回调用结果
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-4-1
	 * @return String
	 */
	public abstract String getReturnXml();

	
	public boolean baseCheck(){
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)){
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		
		if(3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType && 5 != clientType && 6 != clientType && 7 != clientType){
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
	
	public boolean baseCheckNX(){
		logger.debug("baseCheck()");
		
		if(StringUtil.IsEmpty(cmdId)){
			result = 1000;
			resultDesc = "接口调用唯一ID非法";
			return false;
		}
		
		if(3 != clientType && 2 != clientType && 1 != clientType && 4 != clientType && 5 != clientType && 6 != clientType && 7 != clientType){
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
	 * 查询方式合法性检查
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	boolean searchTypeCheck(){
		if(1 != searchType && 2 != searchType){
			result = 1001;
			resultDesc = "查询类型非法";
			return false;
		}
		return true;
	}
	
	/**
	 * 用户信息类型合法性检查
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	//2017-02-07 xiangzl修改 JSDX_ITMS-BUG-20170116-WJY-001  去掉    6 != userInfoType
	boolean userInfoTypeCheck(){
		if("js_dx".equals(Global.G_instArea))
		{
			if(1 != userInfoType && 2 != userInfoType
					&& 3 != userInfoType && 4 != userInfoType
					&& 5 != userInfoType){
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
		}
		else if("hb_dx".equals(Global.G_instArea))
		{
			if(1 != userInfoType && 2 != userInfoType
					&& 3 != userInfoType && 4 != userInfoType
					&& 5 != userInfoType &&6 != userInfoType){
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
		}
		else if("hlj_dx".equals(Global.G_instArea))
		{// 黑龙江电信 2020/10/30
			if(1 != userInfoType && 6 != userInfoType){
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
		}
		else
		{
			if(1 != userInfoType && 2 != userInfoType
					&& 3 != userInfoType && 4 != userInfoType
					&& 5 != userInfoType && 6 != userInfoType && 7 != userInfoType){
				result = 1002;
				resultDesc = "用户信息类型非法";
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 用户信息合法性检查
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	protected boolean userInfoCheck(){
		if(StringUtil.IsEmpty(userInfo)){
			result = 1002;
			resultDesc = "用户信息不合法";
			return false;
		}
		return true;
	}
	
	protected boolean usernameCheck(){
		if(StringUtil.IsEmpty(userName)){
			result = 1002;
			resultDesc = "用户信息不合法";
			return false;
		}
		return true;
	}
	/**
	 * 设备序列号合法性检查
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	protected boolean devSnCheck(){
		if("ah_dx".equals(Global.G_instArea))
		{
			return true;
		}
		if (2 == searchType || 0 == searchType) {
			if(false == pattern.matcher(devSn).matches() || devSn.length() < 6){
				result = 1005;
				resultDesc = "设备序列号不合法";
				return false;
			}
		}
		return true;
	}
	
	protected boolean macCheck(){
		if (2 == searchType || 0 == searchType) {
			if(false == patternMac.matcher(devSn).matches() || devSn.length() < 6){
				result = 1006;
				resultDesc = "MAC地址不合法";
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 属地合法性检查，属地ID是否存在于数据库中；
	 * 简化属地存在的判断，不考虑只是本地网的情况
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2010-6-18
	 * @return boolean
	 */
	protected boolean cityIdCheck(){
		if("nx_dx".equals(Global.G_instArea) || "nmg_dx".equals(Global.G_instArea) || "cq_dx".equals(Global.G_instArea) || "jl_lt".equals(Global.G_instArea)){
			return true;
		}
		if(StringUtil.IsEmpty(cityId) || false == Global.G_CityId_CityName_Map.containsKey(cityId)){
			result = 1007;
			resultDesc = "属地非法";
			return false;
		}
		return true;
	}
	
	protected boolean cityNameCheck(){
		if(StringUtil.IsEmpty(cityName) || false == Global.G_CityId_CityName_Map.containsValue(cityName)){
			result = 1007;
			resultDesc = "属地非法";
			return false;
		}
		return true;
	}
	
	
	/**
	 * add by zhangchy 2012-02-08
	 * 
	 * 新疆 根据用户LOID获取用户业务放装情况
	 * 对入参LOID(FTTH用户逻辑标识)的合法性做判断
	 * FTTH用户逻辑标识 以大写英文字母C结尾
	 * 
	 */
	boolean loidCheck(){
		if (StringUtil.IsEmpty(loid) || !loid.endsWith("C")) {
			result = 1002;
			resultDesc = "用户信息不合法";
			return false;
		}
		return true;
	}
	
	
	/**
	 * 用户类型标识，1：表示家庭网关，2：标识企业网关
	 * @return
	 */
	boolean gwTypeCheck(){
		if (!"1".equals(gwType) || !"2".equals(gwType) ) {
			result = 1007;
			resultDesc = "用户类型标识非法";
			return false;
		}
		return true;
	}
	
	
	/**
	 * 检查厂商OUI
	 * @return
	 */
	boolean ouiCheck(){
		if(StringUtil.IsEmpty(oui)){
			result = 1003;
			resultDesc = "厂商OUI不能为空";
			return false;
		}
		return true;
	}
	
	
	/**
	 * IP 地址验证
	 * @return
	 */
	boolean ipCheck(){
		if(StringUtil.IsEmpty(ip)){
			result = 1004;
			resultDesc = "IP地址不合法";
			return false;
		}
		
		Pattern pattern = Pattern.compile(ipPattern); 
		Matcher matcher = pattern.matcher(ip);
		if (false == matcher.matches()) {
			result = 1004;
			resultDesc = "IP地址不合法";
			return false ;
		}

		return true;
	}
	
	
	/**
	 * 网关 地址验证
	 * @return
	 */
	boolean gateWayCheck(){
		if(StringUtil.IsEmpty(gateWay)){
			result = 1005;
			resultDesc = "网关地址不合法";
			return false;
		}
		
		Pattern pattern = Pattern.compile(ipPattern); 
		Matcher matcher = pattern.matcher(gateWay);
		if (false == matcher.matches()) {
			result = 1005;
			resultDesc = "网关地址不合法";
			return false ;
		}

		return true;
	}
	
	
	
	
	
	/** getter, setter methods */

	public String getCallXml() {
		return callXml;
	}

	public void setCallXml(String callXml) {
		this.callXml = callXml;
	}

	public String getCmdId() {
		return cmdId;
	}

	public void setCmdId(String cmdId) {
		this.cmdId = cmdId;
	}

	public String getCmdType() {
		return cmdType;
	}

	public void setCmdType(String cmdType) {
		this.cmdType = cmdType;
	}

	public int getClientType() {
		return clientType;
	}

	public void setClientType(int clientType) {
		this.clientType = clientType;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getResultDesc() {
		return resultDesc;
	}

	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	
	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public int getUserInfoType() {
		return userInfoType;
	}

	public void setUserInfoType(int userInfoType) {
		this.userInfoType = userInfoType;
	}

	public String getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}

	public String getDevSn() {
		return devSn;
	}

	public void setDevSn(String devSn) {
		this.devSn = devSn;
	}

	public int getSearchType() {
		return searchType;
	}

	public void setSearchType(int searchType) {
		this.searchType = searchType;
	}

	public String getLoid() {
		return loid;
	}

	public void setLoid(String loid) {
		this.loid = loid;
	}
	
	public String getGwType() {
		return gwType;
	}

	
	public void setGwType(String gwType) {
		this.gwType = gwType;
	}

	
	public String getOui() {
		return oui;
	}

	
	public void setOui(String oui) {
		this.oui = oui;
	}

	
	public String getIp() {
		return ip;
	}

	
	public void setIp(String ip) {
		this.ip = ip;
	}

	
	public String getGateWay() {
		return gateWay;
	}

	
	public void setGateWay(String gateWay) {
		this.gateWay = gateWay;
	}

	
	public String getUserName()
	{
		return userName;
	}

	
	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	
	public String getAuthUser()
	{
		return authUser;
	}

	
	public void setAuthUser(String authUser)
	{
		this.authUser = authUser;
	}

	
	public String getAuthPwd()
	{
		return authPwd;
	}

	public void setAuthPwd(String authPwd)
	{
		this.authPwd = authPwd;
	}

}
