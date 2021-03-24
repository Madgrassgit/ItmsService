package com.linkage.itms.os.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * 
 * @author Administrator(工号) Tel:
 * @version 1.0
 * @since 2011-6-24 上午08:34:24
 * @category com.linkage.itms.os
 * @copyright 南京联创科技 网管科技部
 *
 *  modify by xiangzl 2012-07-11
 */
public class RecieveSheetOBJ
{
	private static Logger logger = LoggerFactory.getLogger(RecieveSheetOBJ.class);
	private static final String netVlan = "41";
	private static final String voipVlan = "45";
	private String cmdId;
	/**定单ID**/
	private String orderId;
	/**业务类型 **/
	private String servTpe;
	/** 操作类型**/
	private String operType;
	/** 受理时间**/
	private String dealDate;
	/** 设备类型**/
	private String devType;
	/**loid **/
	private String loid;
	/**属地编码 **/
	private String cityId;
	/**局向标志 **/
	private String officeId;
	/**小区标志**/
	private String cellId;
	/**接入方式**/
	private String accessType;
	/**联系人**/
	private String linkPeople;
	/**联系电话**/
	private String telPhone;
	
	/**Email**/
	private String email;
	/**手机**/
	private String mobile;
	/**家庭住址**/
	private String address;
	/**证件号码**/
	private String cardNo;
	/**宽带账号**/
	private String netUsername;
	/**宽带密码**/
	private String netPassword;
	/**ADSL绑定电话**/
	private String phoneNumber;
	/**最大上行速率**/
	private String maxupRate;
	/**最大下行速率**/
	private String maxdownRate;
	/**最大上网用户数**/
	private String maxUserNum;
	/**Vlan id**/
	private String vlanId;
	/**VPI**/
	private String vpi;
	/**VCI**/
	private String vci;
	/**DSLAM设备编码**/
	private String dslamDevNo;
	/**DSLAM IP地址**/
	private String dslamIp;
	/**DSLAM设备机架号**/
	private String dslamDevFrameNo;
	/**DSLAM设备框号**/
	private String dslamDevBoxNo;
	/**DSLAM设备槽位号**/
	private String dslamDevSlotNo;
	/**DSLAM设备端口号**/
	private String dslamDevPortNo;
	/**上网方式**/
	private String wanType;
	/**套餐类型**/
	private String packageType;
	/**终端OUI**/
	private String oui;
	/**终端SN**/
	private String devSn;
	/**原来宽带帐号**/
	private String oldNetUsername;
	/**业务电话号码**/
	private String voipTelepone;
	/**终端标识**/
	private String regId;
	/**终端标识类型**/
	private String regIdType;
	/**主用MGC地址**/
	private String mgcIp;
	/**主用MGC端口**/
	private String mgcPort;
	
	/**备用MGC地址**/
	private String standMgcIp;
	/**备用MGC端口**/
	private String standMgcPort;
	/**标示语音口**/
	private String voipPort;
	/**标示语音协议**/
	private String voipProxool;
	/** 是否送终端**/
	private String isGiveDev;
	/** 工单类型  1：正向工单  2：反向工单*/
	private String sheetDirection;   //add by xzl
	/**客户ID*/
	private String customerId;
	/**客户账号*/
	private String customerAccount;
	/**客户密码*/
	private String customerPwd;
	/**宽带速率*/
	private String bandRate;
	/**客户等级*/
	private String custGrade;
	/**ipAddress*/
	protected String ipAddress = "";
	/**ipMask*/
	protected String ipMask = "";
	/**gateWay*/
	protected String gateWay ="";
	/**dns*/
	protected String dns = "";
	/**终端类型*/
	protected String devSpec;
	/**ip类型*/
	private String ipType;
	/**网络标识*/
	private String netType;
	
	/**
	 * iptv接入账号
	 */
	private String iptvAccount;
	
	/**
	 * iptv端口
	 */
	private String iptvPort;
	
	/**
	 * iptv个数
	 */
	private String iptvNum;
	
	
	public RecieveSheetOBJ(String strXML)
	{
		this.XML2Obj(strXML);
	}
	/**
	 * 把工单XML报文转为RecieveSheetOBJ对象
	 * @param strXML
	 */
	private void XML2Obj(String strXML)
	{
		logger.debug("XMLToObj({})", strXML);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(strXML));
			Element root = document.getRootElement();
			this.setCmdId(root.elementText("CmdID"));
			this.setOrderId(root.elementText("OrderID"));
			Element param = root.element("Param");
			this.setOperType(param.elementText("operType"));
			this.setDealDate(param.elementText("dealDate"));
					
			if ("hlj_dx".equals(Global.G_instArea) && ("4".equals(param.elementText("devType")) ||
					"5".equals(param.elementText("devType")))) {
				this.setDevType("1");//设备类型为 4 的当成 1 处理,设备类型为5(政企商务专线套餐)也当成1处理
			}else{
				this.setDevType(param.elementText("devType"));//设备类型，1或者2，
			}			
			
			//this.setDevType(this.transfDevType(param.elementText("devType")));
			if(param.elementText("servType").equals("10")){
				this.setVlanId(netVlan);
				if(this.getDevType().equals("e8b"))
				{
					this.setServTpe("10");
				}
				else
				{
					this.setServTpe("22");
				}	
			}
			else
			{
				this.setServTpe(param.elementText("servType"));
				if(this.getServTpe().equals("15")){
					this.setVlanId(voipVlan);
				}
				else
				{
					this.setVlanId(param.elementText("vlanId"));
				}
			}
			
			this.setLoid(StringUtil.getStringValue(param.elementText("loid")));
			this.setCityId(StringUtil.getStringValue(param.elementText("cityId")));
			
			this.setOfficeId(StringUtil.getStringValue(param.elementText("officeId")));
			this.setCellId(StringUtil.getStringValue(param.elementText("cellId")));
			this.setAccessType(this.transfAccessType(param.elementText("accessType")));
			this.setLinkPeople(StringUtil.getStringValue(param.elementText("linkPeople")));
			this.setTelPhone(StringUtil.getStringValue(param.elementText("telPhone")));
			this.setEmail(StringUtil.getStringValue(param.elementText("email")));
			this.setMobile(StringUtil.getStringValue(param.elementText("mobile")));
			this.setAddress(StringUtil.getStringValue(param.elementText("address")));
			
			this.setCardNo(StringUtil.getStringValue(param.elementText("cardNo")));
		    this.setNetUsername(StringUtil.getStringValue(param.elementText("netUsername")));
		    this.setNetPassword(StringUtil.getStringValue(param.elementText("netPassword")));
		    this.setPhoneNumber(StringUtil.getStringValue(param.elementText("phoneNumber")));
		    this.setMaxupRate(StringUtil.getStringValue(param.elementText("maxupRate")));
		    this.setMaxdownRate(StringUtil.getStringValue(param.elementText("maxdownRate")));
		    this.setMaxUserNum(StringUtil.getStringValue(param.elementText("maxUserNum")));
//		    this.setVlanId(param.elementText("vlanId"));
		    this.setVpi(StringUtil.getStringValue(param.elementText("vpi")));
		    this.setVci(StringUtil.getStringValue(param.elementText("vci")));
		    
		    this.setDslamIp(StringUtil.getStringValue(param.elementText("dslamIp")));
		    this.setDslamDevNo(StringUtil.getStringValue(param.elementText("dslamDevNo")));
		    this.setDslamDevFrameNo(StringUtil.getStringValue(param.elementText("dslamDevFrameNo")));
		    this.setDslamDevBoxNo(StringUtil.getStringValue(param.elementText("dslamDevBoxNo")));
		    this.setDslamDevSlotNo(StringUtil.getStringValue(param.elementText("dslamDevSlotNo")));
		    this.setDslamDevPortNo(StringUtil.getStringValue(param.elementText("dslamDevPortNo")));

		    this.setWanType(StringUtil.getStringValue(param.elementText("wanType")));
		    this.setPackageType(StringUtil.getStringValue(param.elementText("packageType")));
		    this.setOui(StringUtil.getStringValue(param.elementText("oui")));
		    this.setDevSn(StringUtil.getStringValue(param.elementText("devSn")));
		    this.setOldNetUsername(StringUtil.getStringValue(param.elementText("oldNetUsername")));
		    
		    this.setVoipTelepone(StringUtil.getStringValue(param.elementText("voipTelepone")));
		    this.setRegId(StringUtil.getStringValue(param.elementText("regId")));
		    this.setRegIdType(StringUtil.getStringValue(param.elementText("regIdType")));
		    this.setMgcIp(StringUtil.getStringValue(param.elementText("mgcIp")));
		    this.setMgcPort(StringUtil.getStringValue(param.elementText("mgcPort")));
		    this.setStandMgcIp(StringUtil.getStringValue(param.elementText("standMgcIp")));
		    this.setStandMgcPort(StringUtil.getStringValue(param.elementText("standMgcPort")));
		    
		    this.setVoipPort(StringUtil.getStringValue(param.elementText("voipPort")));
		    this.setIsGiveDev(StringUtil.getStringValue(param.elementText("isGiveDev")));
		    this.setSheetDirection(StringUtil.getStringValue(param.elementText("sheetDirection")));
		    this.setCustomerId(StringUtil.getStringValue(param.elementText("customerId")));
		    this.setCustomerAccount(StringUtil.getStringValue(param.elementText("customerId")));
		    this.setCustomerPwd(StringUtil.getStringValue(param.elementText("customerPwd")));
		    this.setBandRate(subStringStr(StringUtil.getStringValue(param.elementText("BAND_RATE"))));
		    this.setCustGrade(transCudtGrade(StringUtil.getStringValue(param.elementText("CUST_GRADE"))));
		    
		    this.setIpAddress(StringUtil.getStringValue(param.elementText("ipaddress")));
		    this.setIpMask(StringUtil.getStringValue(param.elementText("ipmask")));
		    this.setGateWay(StringUtil.getStringValue(param.elementText("gateway")));
		    this.setDns(StringUtil.getStringValue(param.elementText("ipdns")));
		    this.setVoipProxool(StringUtil.getStringValue(param.elementText("voipProtocol")));
		    // 黑龙江需求：HLJDX_HRB-REQ-20150128-JIALEI-001
		    this.setDevSpec(StringUtil.getStringValue(param.elementText("devSpec")));
		    this.setIpType(StringUtil.getStringValue(param.elementText("ipType")));
			this.setNetType(StringUtil.getStringValue(param.elementText("netType")));
			this.setIptvAccount(StringUtil.getStringValue(param.elementText("iptvAccount")));
			this.setIptvPort(StringUtil.getStringValue(param.elementText("iptvPort")));
			this.setIptvNum(StringUtil.getStringValue(param.elementText("iptvNum")));
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 用户基本转换
	 * @param str
	 * @return
	 */
	private String transCudtGrade(String str)
	{
		String result  = null;
		//1：钻石卡 2：金卡 3 ：银卡 0：普通4: 蓝宝卡
		//D--钻;  G--金;S--银; N--普通;
		if ("1".equals(str))
		{
			result = "D";
		}
		else if ("2".equals(str)) 
		{
			result = "G";
		}
		else if ("3".equals(str))
		{
			result = "S";
		}
		else 
		{
			result = "N";
		}
		return result;
	}
	/**
	 * 宽带速率截取，如10M@2 返回10；
	 * @param str
	 * @return
	 */
	private String subStringStr(String str)
	{
		String result = str;
		if (str.indexOf("M@") != -1)
		{
			result = str.substring(0,str.indexOf("M@"));
		}
		return result;
	}
	/**
	 * 终端类型转换：除了这个e8b HG810e(1数据无语音无WiFi)，其他都是e8c
	 * F460(4数据2语音带WiFi)
		* HG8245-E(4数据2语音带WiFi)
		* F420-R(4数据2语音无WiFi)
		* HG8245-G(4数据2语音带WiFi)
		* HG810e(1数据无语音无WiFi)
		* e8-c(3数据2语音带WiFi)
		* HG-220(4数据2语音带WiFi)
		* HG850e(4数据2语音无WiFi)
		* F420(4数据2语音无WiFi)
		* HG220(3数据2语音带WiFi)
	 * @param devType
	 * @return
	 */
	private String transfDevType(String devType)
	{
		String newDevType = "";
		if(StringUtil.IsEmpty(devType))
		{
			newDevType = "e8c";
		}
		else if(devType.equals("HG810e"))
		{
			newDevType = "e8b";
		}
		else
		{
			newDevType = "e8c";
		}
		return newDevType;
	}
	/**
	 * 接入方式转换
	 * 2：普通LAN 5：FTTB+LAN 6：FTTB+DSL 7：FTTH 8：DSL
     * LAN:2/AD:8/PON:5、6、7
	 * @param acccessType
	 * @return
	 */
	private String transfAccessType(String acccessType)
	{
		String newAcsType = "";
		if(StringUtil.IsEmpty(acccessType))
		{
			newAcsType = "3";
		}
		else if(acccessType.equals("8"))
		{
			newAcsType = "1";
		}
		else if(acccessType.equals("2"))
		{
			newAcsType = "2";
		}
		else
		{
			newAcsType = "3";
		}
		return newAcsType;
		
	}
	public String getOrderId()
	{
		return orderId;
	}
	
	public void setOrderId(String orderId)
	{
		this.orderId = orderId;
	}
	
	
	public String getPhoneNumber()
	{
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}
	public String getServTpe()
	{
		return servTpe;
	}
	
	public void setServTpe(String servTpe)
	{
		this.servTpe = servTpe;
	}
	
	public String getOperType()
	{
		return operType;
	}
	
	public void setOperType(String operType)
	{
		this.operType = operType;
	}
	
	public String getDealDate()
	{
		return dealDate;
	}
	
	
	public String getIpAddress()
	{
		return ipAddress;
	}
	
	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}
	
	public String getIpMask()
	{
		return ipMask;
	}
	
	public void setIpMask(String ipMask)
	{
		this.ipMask = ipMask;
	}
	
	public String getGateWay()
	{
		return gateWay;
	}
	
	public void setGateWay(String gateWay)
	{
		this.gateWay = gateWay;
	}
	
	public String getDns()
	{
		return dns;
	}
	
	public void setDns(String dns)
	{
		this.dns = dns;
	}
	
	public static String getNetvlan()
	{
		return netVlan;
	}
	
	public static String getVoipvlan()
	{
		return voipVlan;
	}
	public void setDealDate(String dealDate)
	{
		this.dealDate = dealDate;
	}
	
	public String getDevType()
	{
		return devType;
	}
	
	public void setDevType(String devType)
	{
		this.devType = devType;
	}
	
	public String getLoid()
	{
		return loid;
	}
	
	public void setLoid(String loid)
	{
		this.loid = loid;
	}
	
	public String getCityId()
	{
		return cityId;
	}
	
	public void setCityId(String cityId)
	{
		this.cityId = cityId;
	}
	
	public String getOfficeId()
	{
		return officeId;
	}
	
	public void setOfficeId(String officeId)
	{
		this.officeId = officeId;
	}
	
	public String getCellId()
	{
		return cellId;
	}
	
	public void setCellId(String cellId)
	{
		this.cellId = cellId;
	}
	
	public String getAccessType()
	{
		return accessType;
	}
	
	public void setAccessType(String accessType)
	{
		this.accessType = accessType;
	}
	
	public String getLinkPeople()
	{
		return linkPeople;
	}
	
	public void setLinkPeople(String linkPeople)
	{
		this.linkPeople = linkPeople;
	}
	
	public String getTelPhone()
	{
		return telPhone;
	}
	
	public void setTelPhone(String telPhone)
	{
		this.telPhone = telPhone;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getMobile()
	{
		return mobile;
	}
	
	public void setMobile(String mobile)
	{
		this.mobile = mobile;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public void setAddress(String address)
	{
		this.address = address;
	}
	
	public String getCardNo()
	{
		return cardNo;
	}
	
	public void setCardNo(String cardNo)
	{
		this.cardNo = cardNo;
	}
	
	public String getNetUsername()
	{
		return netUsername;
	}
	
	public void setNetUsername(String netUsername)
	{
		this.netUsername = netUsername;
	}
	
	public String getNetPassword()
	{
		return netPassword;
	}
	
	public void setNetPassword(String netPassword)
	{
		this.netPassword = netPassword;
	}
	
	public String getMaxupRate()
	{
		return maxupRate;
	}
	
	public void setMaxupRate(String maxupRate)
	{
		this.maxupRate = maxupRate;
	}
	
	public String getMaxdownRate()
	{
		return maxdownRate;
	}
	
	public void setMaxdownRate(String maxdownRate)
	{
		this.maxdownRate = maxdownRate;
	}
	
	public String getMaxUserNum()
	{
		return maxUserNum;
	}
	
	public void setMaxUserNum(String maxUserNum)
	{
		this.maxUserNum = maxUserNum;
	}
	
	public String getVlanId()
	{
		return vlanId;
	}
	
	public void setVlanId(String vlanId)
	{
		this.vlanId = vlanId;
	}
	
	public String getVpi()
	{
		return vpi;
	}
	
	public void setVpi(String vpi)
	{
		this.vpi = vpi;
	}
	
	public String getVci()
	{
		return vci;
	}
	
	public void setVci(String vci)
	{
		this.vci = vci;
	}
	
	public String getDslamDevNo()
	{
		return dslamDevNo;
	}
	
	public void setDslamDevNo(String dslamDevNo)
	{
		this.dslamDevNo = dslamDevNo;
	}
	
	public String getDslamIp()
	{
		return dslamIp;
	}
	
	public void setDslamIp(String dslamIp)
	{
		this.dslamIp = dslamIp;
	}
	
	public String getDslamDevFrameNo()
	{
		return dslamDevFrameNo;
	}
	
	public void setDslamDevFrameNo(String dslamDevFrameNo)
	{
		this.dslamDevFrameNo = dslamDevFrameNo;
	}
	
	public String getDslamDevBoxNo()
	{
		return dslamDevBoxNo;
	}
	
	public void setDslamDevBoxNo(String dslamDevBoxNo)
	{
		this.dslamDevBoxNo = dslamDevBoxNo;
	}
	
	public String getDslamDevSlotNo()
	{
		return dslamDevSlotNo;
	}
	
	public void setDslamDevSlotNo(String dslamDevSlotNo)
	{
		this.dslamDevSlotNo = dslamDevSlotNo;
	}
	
	public String getDslamDevPortNo()
	{
		return dslamDevPortNo;
	}
	
	public void setDslamDevPortNo(String dslamDevPortNo)
	{
		this.dslamDevPortNo = dslamDevPortNo;
	}
	
	public String getWanType()
	{
		return wanType;
	}
	
	public void setWanType(String wanType)
	{
		/**
		 * 上网方式分为：1：pppoe桥接拨号；2：专线
		 * 上网方式判定规则：
			1、（设备类型：1：家庭网关）+（上网方式：1：pppoe ） = ITMS家庭网关的pppoe桥接拨号；
			2、（设备类型: 2：政企网关）+（上网方式：1：pppoe）  = BBMS政企网关的路由模式；
			3、（设备类型：2：政企网关）+（上网方式：2：专线）   = BBMS政企网关的静态IP模式； 
			4、（设备类型：1：家庭网关）+（上网方式：2：专线）   = ITMS家庭网关的静态IP模式；
			5、（设备类型：3：家庭网关）+（上网方式：1：pppoe）   = 悦me网关的路由模式；
			
			6、（设备类型: 4：爱WIFI网关（含wifi））+（上网方式：1：pppoe）  =ITMS给爱WIFI网关下发路由模式宽带业务；
			7、（设备类型: 5：爱WIFI网关（不含wifi））+（上网方式：1：pppoe）  =ITMS给爱WIFI网关下发路由模式宽带业务；

		 */
		if("1".equals(this.getDevType()))
		{
			if("1".equals(wanType))
			{
				this.wanType = "1";
			}else if("2".equals(wanType)){
				this.wanType = "3";
			}
			else
			{
				this.wanType = "0";
			}
		}
		else if("2".equals(this.getDevType()))
		{
			if ("hlj_dx".equals(Global.G_instArea)){
				if("1".equals(wanType) || "2".equals(wanType))
				{
					this.wanType = wanType;
				}
				else
				{
					this.wanType = "3";
				}
			}else {
				if("1".equals(wanType))
				{
					this.wanType = "2";
				}
				else if("2".equals(wanType))
				{
					this.wanType = "3";
				}
				else
				{
					this.wanType = "0";
				}
			}
		}else if("3".equals(this.getDevType()) || "4".equals(this.getDevType()) || "5".equals(this.getDevType())){
			if("1".equals(wanType) || "2".equals(wanType))
			{
				this.wanType = "2";
			}
			else
			{
				this.wanType = "0";
			}
		}
		else
		{
			this.wanType = "0";
		}
	}
	
	public String getPackageType()
	{
		return packageType;
	}
	
	public void setPackageType(String packageType)
	{
		this.packageType = packageType;
	}
	
	public String getOui()
	{
		return oui;
	}
	
	public void setOui(String oui)
	{
		this.oui = oui;
	}
	
	public String getDevSn()
	{
		return devSn;
	}
	
	public void setDevSn(String devSn)
	{
		this.devSn = devSn;
	}
	
	public String getOldNetUsername()
	{
		return oldNetUsername;
	}
	
	public void setOldNetUsername(String oldNetUsername)
	{
		this.oldNetUsername = oldNetUsername;
	}
	
	public String getVoipTelepone()
	{
		return voipTelepone;
	}
	
	public void setVoipTelepone(String voipTelepone)
	{
		this.voipTelepone = voipTelepone;
	}
	
	public String getRegId()
	{
		return regId;
	}
	
	public void setRegId(String regId)
	{
		this.regId = regId;
	}
	
	public String getRegIdType()
	{
		return regIdType;
	}
	
	public void setRegIdType(String regIdType)
	{
		this.regIdType = regIdType;
	}
	
	public String getMgcIp()
	{
		return mgcIp;
	}
	
	public void setMgcIp(String mgcIp)
	{
		this.mgcIp = mgcIp;
	}
	
	public String getMgcPort()
	{
		return mgcPort;
	}
	
	public void setMgcPort(String mgcPort)
	{
		this.mgcPort = mgcPort;
	}
	
	public String getStandMgcIp()
	{
		return standMgcIp;
	}
	
	public void setStandMgcIp(String standMgcIp)
	{
		this.standMgcIp = standMgcIp;
	}
	
	public String getStandMgcPort()
	{
		return standMgcPort;
	}
	
	public void setStandMgcPort(String standMgcPort)
	{
		this.standMgcPort = standMgcPort;
	}
	
	public String getVoipPort()
	{
		return voipPort;
	}
	
	public void setVoipPort(String voipPort)
	{
		this.voipPort = voipPort;
	}
	
	public String getIsGiveDev()
	{
		return isGiveDev;
	}
	
	public void setIsGiveDev(String isGiveDev)
	{
		this.isGiveDev = isGiveDev;
	}
	
	public String getCmdId()
	{
		return cmdId;
	}
	
	public void setCmdId(String cmdId)
	{
		this.cmdId = cmdId;
	}
	
	public String getSheetDirection()
	{
		return sheetDirection;
	}
	
	public void setSheetDirection(String sheetDirection)
	{
		this.sheetDirection = sheetDirection;
	}
	
	public String getCustomerId()
	{
		return customerId;
	}
	
	public void setCustomerId(String customerId)
	{
		this.customerId = customerId;
	}
	
	public String getCustomerAccount()
	{
		return customerAccount;
	}
	
	public void setCustomerAccount(String customerAccount)
	{
		this.customerAccount = customerAccount;
	}
	
	public String getCustomerPwd()
	{
		return customerPwd;
	}
	
	public void setCustomerPwd(String customerPwd)
	{
		this.customerPwd = customerPwd;
	}
	
	public String getBandRate()
	{
		return bandRate;
	}
	
	public void setBandRate(String bandRate)
	{
		this.bandRate = bandRate;
	}
	
	public String getCustGrade()
	{
		return custGrade;
	}
	
	public void setCustGrade(String custGrade)
	{
		this.custGrade = custGrade;
	}
	
	public String getVoipProxool() {
		return voipProxool;
	}
	public void setVoipProxool(String voipProxool) {
		this.voipProxool = voipProxool;
	}
	
	public String getDevSpec() {
		return devSpec;
	}
	public void setDevSpec(String devSpec) {
		this.devSpec = devSpec;
	}
	public String getIpType()
	{
		return ipType;
	}
	
	public void setIpType(String ipType)
	{
		this.ipType = ipType;
	}

	public String getNetType()
	{
		return netType;
	}
	
	public void setNetType(String netType)
	{
		this.netType = netType;
	}
	
	public String getIptvAccount()
	{
		return iptvAccount;
	}
	
	public void setIptvAccount(String iptvAccount)
	{
		this.iptvAccount = iptvAccount;
	}
	
	public String getIptvPort()
	{
		return iptvPort;
	}
	
	public void setIptvPort(String iptvPort)
	{
		this.iptvPort = iptvPort;
	}
	
	public String getIptvNum()
	{
		return iptvNum;
	}
	
	public void setIptvNum(String iptvNum)
	{
		this.iptvNum = iptvNum;
	}
	
	
}
