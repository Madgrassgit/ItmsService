
package com.linkage.stbms.ids.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.stbms.cao.ACSCorba;
import com.linkage.stbms.cao.PingCAO;
import com.linkage.stbms.dao.RecordLogDAO;
import com.linkage.stbms.dao.UserDeviceDAO;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.util.StbPingChecker;
import com.linkage.stbms.ids.util.StbPingCheckerXJ;
import com.linkage.stbms.itv.main.Global;
import com.linkage.stbms.obj.PingOBJ;
import com.linkage.stbms.obj.RetPingOBJ;

/**
 * @author zhangshimin(工号) Tel：78
 * @version 1.0
 * @since 2011-4-28 下午04:21:39
 * @category com.linkage.stbms.ids.service
 * @copyright 南京联创科技 网管科技部
 */
public class StbPingService
{
	// 正则，字符加数字
	Pattern pattern = Pattern.compile("\\w{1,}+");
	private static Logger logger = LoggerFactory.getLogger(StbPingService.class);
	// 用户设备信息数据库操作
	UserDeviceDAO devDao;
	/**
	 * 
	 * @param inXml
	 * @return
	 */
	public String work(String inXml)
	{
		logger.warn("setStbPingIP==>work==>inParam={}", inXml);
		RetPingOBJ retPingObj = new RetPingOBJ();
		List<Object> pingList = readPingParam(inXml);
		String devSnXml = null;
		String devOuiXml = null;
		if (null == pingList || pingList.isEmpty())
		{
			logger.warn("pingList is empty");
			retPingObj.setFlag(-1);
		}
		else
		{
			// 获取设备sn和设备oui
			devSnXml = StringUtil.getStringValue(pingList.get(0));
			devOuiXml = StringUtil.getStringValue(pingList.get(1));
			// 判断设备sn和设备oui是否合法
			if (StringUtil.IsEmpty(devSnXml)
					|| false == pattern.matcher(devSnXml).matches())
			{
				logger.warn("非法设备序列号: {}",new Object[]{devSnXml});
				retPingObj.setFlag(-1);
			}
			else if (StringUtil.IsEmpty(devOuiXml)
					|| false == pattern.matcher(devOuiXml).matches())
			{
				logger.warn("非法厂商oui: {}",new Object[]{devSnXml});
				retPingObj.setFlag(-1);
			}
			else
			{
				// 获取设备信息
				devDao = new UserDeviceDAO();
				Map<String, String> userDevInfoMap = devDao.getStbDeviceInfo(devSnXml,
						devOuiXml);
				if (null == userDevInfoMap || userDevInfoMap.isEmpty())
				{
					logger.warn("无该设备信息: devSn：{} & devOui: {}",devSnXml,devOuiXml);
					retPingObj.setFlag(-1);
				}
				else
				{
					String devId = StringUtil.getStringValue(userDevInfoMap.get("device_id"));
					retPingObj.setDeviceId(devId);
					// 在线状态
					String devStatus = userDevInfoMap.get("online_status");
					// 状态
					if ("1".equals(devStatus))
					{ // 设备状态表显示设备在线
						// List的长度
						int size = pingList.size();
						logger.warn("pingList.size():{}",pingList.size());
						PingCAO pingCao = new PingCAO();
						PingOBJ[] pingObjArr = new PingOBJ[size - 2];
						for (int i = 2; i < size; i++)
						{
							PingOBJ pingObj = (PingOBJ) pingList.get(i);
							pingObj.setDeviceId(devId);
							pingCao.setPingObj(pingObj);
							pingCao.ping();
							pingObjArr[i - 2] = pingObj;
						}
						for (int i = 0; i < pingObjArr.length; i++) {
							logger.warn("==pingObjArr["+i+"]="+pingObjArr[i]+"===");
						}
						// 进行了ping操作
						retPingObj.setFlag(1);
						retPingObj.setPingObj(pingObjArr);
					}
					else
					{
						logger.warn("设备不在线: {}",devId);
						retPingObj.setFlag(-1);
					}
				}
			}
		}
		String retXML = retPingObj.obj2xml();
		// 记录日志
		new RecordLogDAO()
				.recordLog(devSnXml, inXml, retPingObj.getDeviceId(), retXML, 1);
		logger.warn("StbPingService==>retXML:{}",retXML);
		return retXML;
	}
	
	
	/**
	 * 江西电信ITV
	 * 
	 * @param inXml
	 * @return
	 */
	public String workForJXDX(String inXml)
	{
		logger.warn("setStbPingIP==>workForJXDX==>inParam={}", inXml);
		
		RetPingOBJ retPingObj = new RetPingOBJ();
		
		StbPingChecker checker = new StbPingChecker(inXml);
		
		if (false == checker.check()) {
			logger.warn("StbPing接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			
			logger.warn("setStbPingIP==>workForJXDX==>retParam={}", checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		
		UserStbInfoDAO dao = new UserStbInfoDAO();
		
		Map<String,String> map = dao.getDeviceIdStr(checker.getSearchType(),
				checker.getSearchInfo(), "1");
		
		if (null == map || "".equals(map.get("device_id")))
		{
			checker.setRstCode("0");
			checker.setRstMsg("查无此设备");
			
			logger.warn("查无此设备，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			
			logger.warn("setStbPingIP==>workForJXDX==>retParam:{}",checker.getReturnXml());
			
			return checker.getReturnXml();
			
		}
		else {
			
			String deviceId = map.get("device_id");
			
			// 确认设备是否在线
			logger.warn("当前时间:{}", new DateTimeUtil().getLongDate());
			int status = new ACSCorba().getDeviceStatus(deviceId);
			logger.warn("当前时间:{}",new DateTimeUtil().getLongDate());
			if (1 != status) {
				
				checker.setRstCode("0");
				checker.setRstMsg("此设备不在线");
				
				logger.warn("此设备不在线，不能Ping，serchType={}，searchInfo={}，device_id={}",
						new Object[] { checker.getSearchType(), checker.getSearchInfo(), deviceId });
				
				logger.warn("setStbPingIP==>workForJXDX==>retParam:{}",checker.getReturnXml());
				
				return checker.getReturnXml();
			}
			else {
				// 如果入参有多个IP，且用英文逗号间隔
				String [] ipArray = checker.getIp().split(",");
				
				PingCAO pingCao = new PingCAO();
				PingOBJ[] pingObjArr = new PingOBJ[ipArray.length];
				
				for (int i = 0; i < ipArray.length; i++) {
					
					PingOBJ pingObj = new PingOBJ();
					
					pingObj.setDeviceId(deviceId);
					pingObj.setPingAddr(ipArray[i]);
					pingObj.setPackSize(Global.PING_PACK_SIZE);
					pingObj.setPackNum(Global.PING_PACK_NUM);
					pingObj.setTimeout(Global.PING_TIME_OUT);
					pingObj.setDscp(Global.PING_DSCP);
					
					pingCao.setPingObj(pingObj);
					pingCao.ping();
					
					pingObjArr[i] = pingObj;
				}
				
				retPingObj.setFlag(1);
				retPingObj.setPingObj(pingObjArr);
				retPingObj.setSearchInfo(checker.getSearchInfo());
				
				String retXML = retPingObj.obj2xml();
				logger.warn("setStbPingIP==>workForJXDX==>retParam:{}",retXML);
				return retXML;
			}
		}
		
	}
	
	
	/**
	 * 新疆电信ITV
	 * 
	 * @param inXml
	 * @return
	 */
	public String workForXJDX(String inXml)
	{
		logger.warn("setStbPingIP==>workForXJDX==>inParam={}", inXml);
		
		RetPingOBJ retPingObj = new RetPingOBJ();
		
		StbPingCheckerXJ checker = new StbPingCheckerXJ(inXml);
		
		if (false == checker.check()) {
			logger.warn("StbPing接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			logger.warn("setStbPingIP==>workForXJDX==>retParam={}", checker.getReturnXml());
			
			return checker.getReturnXml();
		}
		
		UserStbInfoDAO dao = new UserStbInfoDAO();
		
		ArrayList<HashMap<String,String>> mapList = dao.getDeviceDescTime(checker.getSearchType(),
				checker.getSearchInfo(), "1");
		
		if (null == mapList || mapList.size()==0)
		{
			logger.warn("没有查到设备，serchType={}，searchInfo={}",	new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			
			checker.setRstCode("1000");
			checker.setRstMsg("没有查到设备");
			logger.warn("setStbPingIP==>workForXJDX==>retParam:{}",checker.getReturnXml());
			new RecordLogDAO().recordLog(checker.getSearchInfo(), inXml, "",
					checker.getReturnXml(), 1);
			return checker.getReturnXml();
		}
		
		if (mapList.size()>1)
		{
			logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询，serchType={}，searchInfo={}",	new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			
			checker.setRstCode("1006");
			checker.setRstMsg("查到多台设备,请输入更多位序列号或完整序列号进行查询");
			logger.warn("setStbPingIP==>workForXJDX==>retParam:{}",checker.getReturnXml());
			new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
					checker.getReturnXml(), 1);
			return checker.getReturnXml();
		}
		
		HashMap<String,String> map = mapList.get(0);
		
		if (null == map || "".equals(map.get("device_id")))
		{
			logger.warn("查无设备，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			
			checker.setRstCode("1000");
			checker.setRstMsg("查无设备");
			
			logger.warn("setStbPingIP==>workForXJDX==>retParam:{}",checker.getReturnXml());
			new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
					checker.getReturnXml(), 1);
			return checker.getReturnXml();
		}
		else {
			String deviceId = map.get("device_id");
			// 确认设备是否在线
			logger.warn("当前时间:{}", new DateTimeUtil().getLongDate());
			int status = new ACSCorba().getDeviceStatus(deviceId);
			logger.warn("当前时间{}",new DateTimeUtil().getLongDate());
			if (1 != status) {
				logger.warn("此设备不在线，不能Ping，serchType={}，searchInfo={}，device_id={}",
						new Object[] { checker.getSearchType(), checker.getSearchInfo(), deviceId });
				
				checker.setRstCode("1003");
				checker.setRstMsg("设备不能正常交互");
				
				logger.warn("setStbPingIP==>workForXJDX==>retParam:{}", checker.getReturnXml());
				new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
						checker.getReturnXml(), 1);
				return checker.getReturnXml();
			}
			else {
				// 如果入参有多个IP，且用英文逗号间隔
				String [] ipArray = checker.getIp().split(",");
				
				PingCAO pingCao = new PingCAO();
				PingOBJ[] pingObjArr = new PingOBJ[ipArray.length];
				
				for (int i = 0; i < ipArray.length; i++) {
					PingOBJ pingObj = new PingOBJ();
					
					pingObj.setDeviceId(deviceId);
					pingObj.setPingAddr(ipArray[i]);
					
					String timeOut = checker.getTimeOut(); // 超时时间 :默认10s
					String dataSize = checker.getDataSize(); // 包大小 :默认32byte
					String numberOfRepetitions = checker.getNumberOfRepetitions(); // 包数目 :默认 2
					
					if(timeOut!=null && timeOut.trim().length()!=0){
						pingObj.setTimeout(StringUtil.getIntegerValue(timeOut));
					}else{
						pingObj.setTimeout(Global.PING_TIME_OUT_XJ);
					}
					
					if(dataSize!=null && dataSize.trim().length()!=0){
						pingObj.setPackSize(StringUtil.getIntegerValue(dataSize));
					}else{
						pingObj.setPackSize(Global.PING_PACK_SIZE_XJ);
					}
					
					if(numberOfRepetitions!=null && numberOfRepetitions.trim().length()!=0){
						pingObj.setPackNum(StringUtil.getIntegerValue(numberOfRepetitions));
					}else{
						pingObj.setPackNum(Global.PING_PACK_NUM_XJ);
					}
					
					pingObj.setDscp(Global.PING_DSCP);
					
					pingCao.setPingObj(pingObj);
					pingCao.ping();
					
					pingObjArr[i] = pingObj;
				}
				
				retPingObj.setFlag(1);
				retPingObj.setPingObj(pingObjArr);
				
				String retXML = retPingObj.obj2xml();
				logger.warn("setStbPingIP==>workForXJDX==>retParam:" + retXML);
				new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
						checker.getReturnXml(), 1);
				return retXML;
			}
		}
	}
	
	
	
	/**
	 * 读取ping操作接口的入参
	 * 
	 * @param
	 * @author zhangshimin(67310)
	 * @date 2009-12-15
	 * @return List 返回List，第一个元素为dev_sn，第二个元素oui及以后为ping参数对象。解析出错返回null。
	 */
	public static List<Object> readPingParam(String strXML)
	{
		logger.debug("readPingParam({})", strXML);
		SAXReader reader = new SAXReader();
		Document document = null;
		List<Object> userPingList = null;
		try
		{
			userPingList = new ArrayList<Object>();
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
			else if (pingIp.equals("9")) {
				pingObj.setPingAddr(root.element("device").elementText("ping_ip"));
			}
			else
			{
				logger.warn("pingIp is error");
				return null;
			}
			pingObj.setPackSize(Global.PING_PACK_SIZE);
			pingObj.setPackNum(Global.PING_PACK_NUM);
			pingObj.setTimeout(Global.PING_TIME_OUT);
			pingObj.setDscp(Global.PING_DSCP);
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
//					pingObj.setPingAddr(pingEle.elementText("ping_ip"));
//					pingObj.setPackSize(StringUtil.getIntegerValue(pingEle
//							.elementText("pack_size")));
//					pingObj.setPackNum(StringUtil.getIntegerValue(pingEle
//							.elementText("pack_num")));
//					pingObj.setTimeout(StringUtil.getIntegerValue(pingEle
//							.elementText("timeout")));
//					pingObj.setDscp(StringUtil.getIntegerValue(pingEle
//							.elementText("dscp")));
//					userPingList.add(pingObj);
//				}
//			}
			logger.warn("userPingList:{}",new Object[]{userPingList});
		}
		catch (DocumentException e)
		{
			logger.error("异常：{()}",e.getMessage());
			// e.printStackTrace();
		}
		return userPingList;
	}
}
