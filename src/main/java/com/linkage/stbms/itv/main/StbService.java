package com.linkage.stbms.itv.main;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.cao.DeviceGatherCAO;
import com.linkage.stbms.cao.PingCAO;
import com.linkage.stbms.dao.GatherInfoDAO;
import com.linkage.stbms.dao.RecordLogDAO;
import com.linkage.stbms.dao.UserDeviceDAO;
import com.linkage.stbms.itv.inter.IService;
import com.linkage.stbms.obj.PingOBJ;
import com.linkage.stbms.obj.RetPingOBJ;
import com.linkage.stbms.obj.UserStbInfoOBJ;
import com.linkage.stbms.pic.service.LogoConService;
import com.linkage.stbms.pic.service.ZeroConfService;
import com.linkage.stbms.radius.service.RadiusSerivce;
import com.linkage.stbms.service.GetStbPPPOEAccountService;

/**
 * 提供服务接口类
 * 
 * @author Jason(3412)
 * @date 2009-12-17
 */
public class StbService implements IService {

	private static Logger logger = LoggerFactory.getLogger(StbService.class);

	// 正则，字符加数字
	Pattern pattern = Pattern.compile("\\w{1,}+");

	// 用户设备信息数据库操作
	UserDeviceDAO devDao;
	
	/**
	 * 获取机顶盒接入账号
	 */
	public String getStbPppoeAccount(String inParm)
	{
		logger.info("getStbPppoeAccount({})", inParm);
		return new GetStbPPPOEAccountService().getStbPppoeAccount(inParm);
	}
	
	/**
	 * 根据设备IP地址获取机顶盒信息
	 * @param request
	 * @return
	 */
	public String getItvCfg(String request)
	{
		return new RadiusSerivce().getItvCfg(request);
	}
	
	/**
	 * STB查询信息接口
	 */
	@Override
	public String getUserStbInfo(String inXml) {
		logger.warn("StbService==>getUserStbInfo({})", inXml);
		// 解析入参inXml
		Map<String, String> inParamMap = readXml(inXml);
		// 设备查询信息对象
		UserStbInfoOBJ userStbObj = new UserStbInfoOBJ();
		// 获取设备sn和设备oui
		String devSnXml = inParamMap.get("device_serialnumber");
		String devOuiXml = inParamMap.get("oui");
		// 判断设备sn和设备oui是否合法
		if (StringUtil.IsEmpty(devSnXml)
				|| false == pattern.matcher(devSnXml).matches()) {
			logger.warn("非法设备序列号: " + devSnXml);
			userStbObj.setConnState(UNKOWN);
		} else if (StringUtil.IsEmpty(devOuiXml)
				|| false == pattern.matcher(devOuiXml).matches()) {
			logger.warn("非法厂商oui: " + devOuiXml);
			userStbObj.setConnState(UNKOWN);
		} else {
			// 获取用户设备信息
			devDao = new UserDeviceDAO();
			Map<String, String> userDevInfoMap = devDao.getStbDeviceInfo(
					devSnXml, devOuiXml);
			if (null == userDevInfoMap || userDevInfoMap.isEmpty()) {
				logger.warn("无该设备信息: devSn：" + devSnXml + " & devOui: "
						+ devOuiXml);
				userStbObj.setConnState(UNKOWN);
			} else {
				String devId = userDevInfoMap.get("device_id");
				// 记录设备ID
				userStbObj.setDeviceId(devId);
				userStbObj.setIp(userDevInfoMap.get("loopback_ip"));
				// 厂商，型号，序列号
				String devSn = userDevInfoMap.get("device_serialnumber");
				String devVersionId = userDevInfoMap.get("devicetype_id");
				String devStatus = userDevInfoMap.get("online_status");
				userStbObj.setSn(devSn);
				// 设备型号和厂商信息
				if (false == StringUtil.IsEmpty(devVersionId)) {
					Map<String, String> devModelMap = devDao
							.getDevVendorModelVersion(devVersionId);
					if (null != devModelMap && false == devModelMap.isEmpty()) {
						userStbObj.setVendor(devModelMap.get("vendor_name"));
						userStbObj.setModel(devModelMap.get("device_model"));
						userStbObj.setSoftversion(devModelMap
								.get("softwareversion"));
					}
				}
				// 状态
				if ("1".equals(devStatus)) { // 设备状态表显示设备在线
					logger.warn("设备状态表显示设备在线(非实时状态)：" + devId);
					// 采集设备信息CAO
					DeviceGatherCAO devCao = new DeviceGatherCAO();
					// int iret = devCao.gatherStbAll(devId);
					int iret = devCao.gatherUserInterfaceInfo(devId);
					int iret4 = devCao.gatherLan(devId);
					int iret2 = devCao.gatherIPTVServiceInfo(devId);
					int iret3 = devCao.gatherSTBDeviceInfo(devId);
					logger.warn("==iret=="+iret+"===");
					logger.warn("==iret2=="+iret2+"===");
					logger.warn("==iret3=="+iret3+"===");
					logger.warn("==iret4=="+iret4+"===");
					if (iret == 1 || iret2 == 1 || iret3 == 1 || iret4 == 1) { // 1：采集成功
						logger.warn("*********采集成功**********");
						// 设备在线
						userStbObj.setConnState(ONLINE);
						// 设备结点信息DAO
						GatherInfoDAO gatherDao = new GatherInfoDAO();
						// c.service_name
						Map<String, String> stbDeviceInfoMap = gatherDao
								.getStbDeviceInfo(devId);
						if (null != stbDeviceInfoMap
								&& false == stbDeviceInfoMap.isEmpty()) {
							String servName = stbDeviceInfoMap
									.get("service_name");
							userStbObj.setServName(servName);
						}
						// a.mac,a.address_type
						Map<String, String> stbLanMap = gatherDao
								.getStbLanInfo(devId);
						if (null != stbLanMap && false == stbLanMap.isEmpty()) {
							String mac = stbLanMap.get("mac");
							String addressType = stbLanMap.get("address_type");
							userStbObj.setMac(mac);
							userStbObj.setAccessType(addressType);
						}
						// b.stream_serv_ip,
						Map<String, String> stbUserInterfaceMap = gatherDao
								.getStbUserInterfaceInfo(devId);
						if (null != stbUserInterfaceMap
								&& false == stbUserInterfaceMap.isEmpty()) {
							String streamServIp = stbUserInterfaceMap
									.get("stream_serv_ip");
							userStbObj.setMediaAddr(streamServIp);
						}
						// d.user_id,d.user_pwd,d.auth_url
						Map<String, String> stbIptvMap = gatherDao
								.getStbX_CTC_IPTVInfo(devId);
						if (null != stbIptvMap && false == stbIptvMap.isEmpty()) {
							String servAccount = stbIptvMap.get("user_id");
							String stbPasswd = stbIptvMap.get("user_pwd");
							userStbObj.setStbUsername(servAccount);
							userStbObj.setStbPasswd(stbPasswd);

							userStbObj.setAuthUrl(stbIptvMap.get("auth_url"));
						}
					} else if (2 == iret || -2 == iret || -1 == iret) { // 2：设备不在线
						logger.warn("设备不在线或连接失败: " + " " + devId + iret);
						userStbObj.setConnState(OFFLINE);
					} else if (5 == iret) { // 5：采集失败
						logger.warn("采集未得知设备在线状态，采集失败: " + devId + " 5");
						userStbObj.setConnState(OFFLINE);
					} else { // 3：设备正被操作 4：采集返回9000后错误
						logger.warn("采集失败: " + devId + ",采集返回 结果： " + iret);
						userStbObj.setConnState(ONLINE);
					}
				} else {
					logger.warn("设备不在线: " + devId);
					userStbObj.setConnState(OFFLINE);
				}
				// }
			}
		}
		String retXML = userStbObj.obj2xml();
		// 记录日志
		new RecordLogDAO().recordLog(devSnXml, inXml, userStbObj.getDeviceId(),
				retXML, 1);
		logger.warn("StbService==>retXML:" + retXML);
		return retXML;
	}

	/**
	 * PING操作接口
	 */
	@Override
	public String setStbPingIP(String inXml) {
		logger.info("setStbPingIP({})", inXml);
		RetPingOBJ retPingObj = new RetPingOBJ();
		List pingList = readPingParam(inXml);

		String devSnXml = null;
		String devOuiXml = null;
		if (null == pingList || pingList.isEmpty()) {
			logger.warn("pingList is empty");
			retPingObj.setFlag(-1);
		} else {
			// 获取设备sn和设备oui
			devSnXml = StringUtil.getStringValue(pingList.get(0));
			devOuiXml = StringUtil.getStringValue(pingList.get(1));
			// 判断设备sn和设备oui是否合法
			if (StringUtil.IsEmpty(devSnXml)
					|| false == pattern.matcher(devSnXml).matches()) {
				logger.warn("非法设备序列号: " + devSnXml);
				retPingObj.setFlag(-1);
			} else if (StringUtil.IsEmpty(devOuiXml)
					|| false == pattern.matcher(devOuiXml).matches()) {
				logger.warn("非法厂商oui: " + devOuiXml);
				retPingObj.setFlag(-1);
			} else {
				// 获取设备信息
				devDao = new UserDeviceDAO();
				Map<String, String> userDevInfoMap = devDao.getStbDeviceInfo(
						devSnXml, devOuiXml);
				if (null == userDevInfoMap || userDevInfoMap.isEmpty()) {
					logger.warn("无该设备信息: devSn：" + devSnXml + " & devOui: "
							+ devOuiXml);
					retPingObj.setFlag(-1);
				} else {
					String devId = userDevInfoMap.get("device_id");
					retPingObj.setDeviceId(devId);
					// 在线状态
					String devStatus = userDevInfoMap.get("online_status");
					// 状态
					if ("1".equals(devStatus)) { // 设备状态表显示设备在线
						// List的长度
						int size = pingList.size();
						logger.warn("pingList.size():" + pingList.size());
						PingCAO pingCao = new PingCAO();
						PingOBJ[] pingObjArr = new PingOBJ[size - 2];
						for (int i = 2; i < size; i++) {
							PingOBJ pingObj = (PingOBJ) pingList.get(i);
							pingObj.setDeviceId(devId);
							pingCao.setPingObj(pingObj);
							pingCao.ping();
							pingObjArr[i - 2] = pingObj;
						}
						logger.info("444444444444");
						// 进行了ping操作
						retPingObj.setFlag(1);
						retPingObj.setPingObj(pingObjArr);
					} else {
						logger.warn("设备不在线: " + devId);
						retPingObj.setFlag(-1);
					}
				}
			}
		}
		String retXML = retPingObj.obj2xml();
		// 记录日志
		new RecordLogDAO().recordLog(devSnXml, inXml, retPingObj.getDeviceId(),
				retXML, 1);
		logger.warn("retXML:" + retXML);
		return retXML;
	}

	/**
	 * 读取参数调用的用户账号
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return String
	 */
	String readUsername(String strXML) {
		logger.debug("read({})", strXML);
		SAXReader reader = new SAXReader();
		Document document = null;
		String username = null;
		try {
			document = reader.read(new StringReader(strXML));
			Element root = document.getRootElement();
			username = root.elementText("username");
		} catch (DocumentException e) {
			logger.error(e.getMessage());
			// e.printStackTrace();
		}
		return username;
	}

	/**
	 * 读取stb查询接口入参xml
	 * 
	 * @param
	 * @author zhangsm
	 * @date 2011-02-23
	 * @return Map
	 */
	Map<String, String> readXml(String strXML) {
		logger.debug("read({})", strXML);
		SAXReader reader = new SAXReader();
		Map<String, String> resultMap = new HashMap<String, String>();
		Document document = null;
		try {
			document = reader.read(new StringReader(strXML));
			Element root = document.getRootElement();
			resultMap.put("device_serialnumber",
					root.elementText("device_serialnumber"));
			resultMap.put("oui", root.elementText("oui"));
		} catch (DocumentException e) {
			logger.error(e.getMessage());
			// e.printStackTrace();
		}
		return resultMap;
	}

	/**
	 * 读取ping操作接口的入参
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return List
	 *         返回List，第一个元素为device_serialnumber，第二个元素oui及以后为ping参数对象。解析出错返回null。
	 *         modify by zhangsm 2011-02-23 username改为device_serialnumber+oui
	 */
	List readPingParam(String strXML) {
		logger.debug("readPingParam({})", strXML);
		SAXReader reader = new SAXReader();
		Document document = null;
		List userPingList = null;
		try {
			userPingList = new ArrayList();
			document = reader.read(new StringReader(strXML));
			Element root = document.getRootElement();
			userPingList.add(root.elementText("device_serialnumber"));
			userPingList.add(root.elementText("oui"));
			Iterator itor = root.elementIterator("device_ping");
			logger.debug("itor: " + itor);
			if (null == itor || false == itor.hasNext()) {
				logger.debug("解析<device>标签");
				itor = root.elementIterator("device");
				logger.debug("itor: " + itor);
			}
			if (null != itor) {
				while (itor.hasNext()) {
					logger.warn("itor.hasNext()");
					Element pingEle = (Element) itor.next();
					PingOBJ pingObj = new PingOBJ();
					pingObj.setPingAddr(pingEle.elementText("ping_ip"));
					pingObj.setPackSize(StringUtil.getIntegerValue(pingEle
							.elementText("pack_size")));
					pingObj.setPackNum(StringUtil.getIntegerValue(pingEle
							.elementText("pack_num")));
					pingObj.setTimeout(StringUtil.getIntegerValue(pingEle
							.elementText("timeout")));
					pingObj.setDscp(StringUtil.getIntegerValue(pingEle
							.elementText("dscp")));
					userPingList.add(pingObj);
				}
			}
			logger.info("userPingList:" + userPingList);
		} catch (DocumentException e) {
			logger.error(e.getMessage());
			// e.printStackTrace();
		}
		return userPingList;
	}
	/**
	 * 江西DHCP机顶盒获取下发指令
	 * @param xmlData
	 * @return
	 */
	public String queryRpc4DHCP(String xmlData)
	{
		return new LogoConService().work(xmlData);
	}
	/**
	 * 江西DHCP/static机顶盒零配置获取下发指令
	 * @param xmlData
	 * @return
	 */
	public String queryRpc4ZeroConf(String xmlData)
	{
		return new ZeroConfService().work(xmlData);
	}
}
