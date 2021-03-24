package com.linkage.stbms.ids.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.cao.ACSCorba;
import com.linkage.stbms.dao.RecordLogDAO;
import com.linkage.stbms.dao.UserDeviceDAO;
import com.linkage.stbms.ids.bio.StbTraceRouteBIO;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.util.StbTraceRouteChecker;
import com.linkage.stbms.itv.main.Global;
import com.linkage.stbms.obj.PingOBJ;

/**
 * 
 * @author Administrator(工号) Tel:78
 * @version 1.0
 * @since 2011-4-28 下午05:31:45
 * @category com.linkage.stbms.ids.service
 * @copyright 南京联创科技 网管科技部
 *
 */
public class StbTraceRouteService
{
	// 正则，字符加数字
	Pattern pattern = Pattern.compile("\\w{1,}+");
	private static Logger logger = LoggerFactory.getLogger(StbTraceRouteService.class);
	// 用户设备信息数据库操作
	UserDeviceDAO devDao;
	/**
	 * 
	 * @param inXml
	 * @return
	 */
	public String work(String inXml)
	{
		logger.warn("setStbTraceRouteIP==>work==>inParam={}", inXml);
		int flag = 0;
		String resultDesc = "";
		List<PingOBJ> pingOBJs = new ArrayList<PingOBJ>();
		List traceRouteList = readTraceRouteParam(inXml);
		String devSnXml = null;
		String devOuiXml = null;
		String deviceId = null;
		if (null == traceRouteList || traceRouteList.isEmpty())
		{
			logger.warn("traceRouteList is empty");
			flag = 0;
			resultDesc = "参数列表为空";
		}
		else
		{
			// 获取设备sn和设备oui
			devSnXml = StringUtil.getStringValue(traceRouteList.get(0));
			devOuiXml = StringUtil.getStringValue(traceRouteList.get(1));
			// 判断设备sn和设备oui是否合法
			if (StringUtil.IsEmpty(devSnXml)
					|| false == pattern.matcher(devSnXml).matches())
			{
				flag = 0;
				resultDesc = "非法设备序列号: " + devSnXml;
				logger.warn(resultDesc);
			}
			else if (StringUtil.IsEmpty(devOuiXml)
					|| false == pattern.matcher(devOuiXml).matches())
			{
				flag = 0;
				resultDesc = "非法厂商oui: " + devOuiXml;
				logger.warn(resultDesc);
			}
			else
			{
				// 获取设备信息
				devDao = new UserDeviceDAO();
				Map<String, String> userDevInfoMap = devDao.getStbDeviceInfo(devSnXml,
						devOuiXml);
				if (null == userDevInfoMap || userDevInfoMap.isEmpty())
				{
					flag = 0;
					resultDesc = "无该设备信息: devSn：" + devSnXml + " & devOui: " + devOuiXml;
					logger.warn(resultDesc);
				}
				else
				{
					deviceId = userDevInfoMap.get("device_id");
					// 在线状态
					String devStatus = userDevInfoMap.get("online_status");
					// 状态
					if ("1".equals(devStatus))
					{ // 设备状态表显示设备在线
						// List的长度
						int size = traceRouteList.size();
						logger.warn("pingList.size():" + traceRouteList.size());
						StbTraceRouteBIO traceRouteBIO = new StbTraceRouteBIO();
						for (int i = 2; i < size; i++)
						{
							PingOBJ pingObj = (PingOBJ) traceRouteList.get(i);
							pingObj.setDeviceId(deviceId);
							traceRouteBIO.setPingObj(pingObj);
							traceRouteBIO.traceRoute();
							pingOBJs.add(pingObj);
						}
						// 进行了traceRoute操作
						flag = 1;
						resultDesc = "成功";
					}
					else
					{
						flag = 0;
						resultDesc = "设备不在线: " + deviceId;
						logger.warn(resultDesc);
					}
				}
			}
		}
		String retXML = returnXML(pingOBJs,flag,resultDesc);
		// 记录日志
		new RecordLogDAO()
				.recordLog(devSnXml, inXml, deviceId,retXML, 1);
		logger.warn("retXML:" + retXML);
		return retXML;
	}
	
	
	
	/**
	 * 江西电信iTV
	 * 
	 * @param inXml
	 * @return
	 */
	public String workForJXDX(String inXml)
	{
		logger.warn("setStbTraceRoute==>workForJXDX==>inParam={}", inXml);
		
		List<PingOBJ> pingOBJs = new ArrayList<PingOBJ>();
		
		StbTraceRouteChecker checker = new StbTraceRouteChecker(inXml);
		
		if (false == checker.check()) {
			logger.warn(" TraceRoute接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			
			logger.warn("setStbTraceRoute==>workForJXDX==>retParam={}", checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		
		UserStbInfoDAO dao = new UserStbInfoDAO();
		
		Map<String, String> map = dao.getDeviceIdStr(checker.getSearchType(),
				checker.getSearchInfo(), "1");
		
		if (null == map || "".equals(map.get("device_id")))
		{
			checker.setRstCode("0");
			checker.setRstMsg("查无此设备");
			
			logger.warn("查无此设备，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			
			logger.warn("setStbTraceRoute==>workForJXDX==>retParam:" + checker.getReturnXml());
			
			return checker.getReturnXml();
			
		}else {
			
			String deviceId = map.get("device_id");
			
			// 确认设备是否在线
			int status = new ACSCorba().getDeviceStatus(deviceId);
			
			if (1 != status) {
				
				checker.setRstCode("0");
				checker.setRstMsg("此设备不在线");
				
				logger.warn("此设备不在线，不能TraceRoute，serchType={}，searchInfo={}，device_id={}",
						new Object[] { checker.getSearchType(), checker.getSearchInfo(), deviceId });
				
				logger.warn("setStbTraceRoute==>workForJXDX==>retParam:" + checker.getReturnXml());
				
				return checker.getReturnXml();
			}
			else {
				
				// 如果入参有多个IP，且用英文逗号间隔
				String [] ipArray = checker.getIp().split(",");
				
				StbTraceRouteBIO traceRouteBIO = new StbTraceRouteBIO();
				
				for (int i = 0; i < ipArray.length; i++) {
					
					PingOBJ pingObj = new PingOBJ();
					
					pingObj.setPingAddr(checker.getIp());
					pingObj.setDeviceId(deviceId);
					
					pingObj.setMaxHopCount(Global.TRACEROUTE_MAX_HOP_NUM);
					pingObj.setPackSize(Global.TRACEROUTE_PACK_SIZE);
					pingObj.setTimeout(Global.TRACEROUTE_TIME_OUT);
					pingObj.setDscp(Global.TRACEROUTE_DSCP);
					
					traceRouteBIO.setPingObj(pingObj);
					traceRouteBIO.traceRoute();
					
					pingOBJs.add(pingObj);
					
				}
				
				String retXML = returnXML(pingOBJs,1,"TraceRoute成功");
				
				logger.warn("TraceRoute成功");
				logger.warn("setStbTraceRoute==>workForJXDX==>retParam={}", retXML);
				
				return retXML;
			}
		}
	}
	
	
	
	
	
	
	/**
	 * 接口对象返回XML字符串
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-12-17
	 * @return String
	 */
	public String returnXML(List<PingOBJ> pingOBJs,int flag,String resultDesc) {
		logger.debug("returnXML()");

		Document document = DocumentHelper.createDocument();
		// root
		Element root = document.addElement("root");
		// ping操作的标识位
		root.addElement("result_flag").setText(StringUtil.getStringValue(flag));
		root.addElement("result").addText(resultDesc);
		if (1 == flag && null != pingOBJs) {
//			logger.info("pingObjs.size(): " + pingOBJs.size());
			logger.warn("pingObjs.size(): " + pingOBJs.size());
			for (PingOBJ pingOBJ : pingOBJs) {
				logger.warn("pingOBJ.getFaultCode(): " + pingOBJ.getFaultCode());
				if(pingOBJ.getFaultCode() != 1)
				{
					continue;
				}
				//logger.info("pingOBJ.getHopHostI(): " + pingOBJ.getHopHostI().size());
				root.addElement("pot_num").addText(pingOBJ.getNumberOfRouteHops());
				
				Element hopPots = root.addElement("pots");
				for(Object ip : pingOBJ.getHopHostI())
				{
					logger.warn("--------" + ip);
					Element hopPot = hopPots.addElement("pot");
					hopPot.addElement("pot_ip").addText(ip.toString());
				}
			}
		}
		return document.asXML();
	}

	/**
	 * 读取traceRoute操作接口的入参
	 * 
	 * @param
	 * @author zhangshimin(67310)
	 * @date 2011-4-25
	 * @return List 返回List，第一个元素为dev_sn，第二个元素oui及以后为ping参数对象。解析出错返回null。
	 */
	public static List readTraceRouteParam(String strXML)
	{
		logger.debug("readPingParam({})", strXML);
		SAXReader reader = new SAXReader();
		Document document = null;
		List userPingList = null;
		try
		{
			userPingList = new ArrayList();
			document = reader.read(new StringReader(strXML));
			Element root = document.getRootElement();
			userPingList.add(root.elementText("dev_sn"));
			userPingList.add(root.elementText("oui"));
			
			PingOBJ pingObj = new PingOBJ();
			String pingIp = root.elementText("ip");
			if(StringUtil.IsEmpty(pingIp)){
				logger.warn("pingIp is empty");
				return null;
			}
			else if(pingIp.equals("1"))
			{
				pingObj.setPingAddr("58.223.107.136");
			}
			else if(pingIp.equals("2"))
			{
				pingObj.setPingAddr("58.223.251.139");
			}
			else if(pingIp.equals("3"))
			{
				pingObj.setPingAddr("58.223.80.57");
			}
			else
			{
				logger.warn("pingIp is error");
				return null;
			}
			pingObj.setMaxHopCount(Global.TRACEROUTE_MAX_HOP_NUM);
			pingObj.setPackSize(Global.TRACEROUTE_PACK_SIZE);
			pingObj.setTimeout(Global.TRACEROUTE_TIME_OUT);
			pingObj.setDscp(Global.TRACEROUTE_DSCP);
			userPingList.add(pingObj);
			
//			logger.debug("解析<device>标签");
//			Iterator itor = root.elementIterator("device");
//			logger.debug("itor: " + itor);
//			if (null != itor)
//			{
//				while (itor.hasNext())
//				{
//					logger.info("itor.hasNext()");
//					Element pingEle = (Element) itor.next();
//					PingOBJ pingObj = new PingOBJ();
//					pingObj.setPingAddr(pingEle.elementText("ip"));
//					pingObj.setMaxHopCount(StringUtil.getIntegerValue(pingEle
//							.elementText("max_pot_num")));
//					pingObj.setTimeout(StringUtil.getIntegerValue(pingEle
//							.elementText("timeout")));
//					pingObj.setPackSize(128);
//					pingObj.setDscp(0);
//					userPingList.add(pingObj);
//				}
//			}
			logger.warn("userPingList:" + userPingList);
		}
		catch (DocumentException e)
		{
			logger.error(e.getMessage());
			// e.printStackTrace();
		}
		return userPingList;
	}
}
