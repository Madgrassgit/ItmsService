
package com.linkage.stbms.ids.bio;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.service.StbPingService;
import com.linkage.stbms.ids.util.CommonParamUtil;
import com.linkage.stbms.ids.util.CommonUtil;
import com.linkage.stbms.ids.util.GetStbInfoChecker;
import com.linkage.stbms.itv.main.Global;
import com.linkage.stbms.itv.main.StbService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author zhangshimin(工号) Tel:??
 * @version 1.0
 * @since 2011-6-3 上午10:54:51
 * @category com.linkage.stbms.ids.bio
 * @copyright 南京联创科技 网管科技部
 */
public class GetStbInfoBIO
{

	private static final Logger logger = LoggerFactory.getLogger(GetStbInfoBIO.class);
	private Map<String, String> resultMap = new HashMap<String, String>();
	ArrayList<HashMap<String, String>> list = null;
	// 正则，字符加数字
	Pattern pattern = Pattern.compile("\\w{1,}+");
	private UserStbInfoDAO dao;
	private String result = "成功";
	private int resultFlag = 1;

	public String getStbInfo(String inParam)
	{
		logger.warn("GetStbInfoBIO==>inParam:" + inParam);
		Map<String, String> inParamMap = CommonParamUtil.getCommonInParam(inParam);
		String devSn = inParamMap.get("dev_sn");
		String oui = inParamMap.get("oui");
		if (StringUtil.IsEmpty(devSn) || false == pattern.matcher(devSn).matches())
		{
			logger.warn("非法设备序列号: " + devSn);
			resultFlag = 0;
			result = "非法设备序列号: " + devSn;
		}
		else if (StringUtil.IsEmpty(oui) || false == pattern.matcher(oui).matches())
		{
			logger.warn("非厂商oui: " + oui);
			resultFlag = 0;
			result = "非厂商oui: " + oui;
		}
		else
		{
			// 1、获取机顶盒信息
			Map<String, String> stbMap = this.getBaseInfo(devSn, oui);
			String stbIp = "";
			if (stbMap != null && !stbMap.isEmpty())
			{
				stbIp = stbMap.get("stb_ip");
				resultMap.putAll(stbMap);
			}
			else
			{
				resultFlag = 0;
				result = "失败";
			}
			if (stbIp != null && !stbIp.equals(""))
			{
				// 2、获取EGP信息
				Map<String, String> egpMap = this.getEgpInfo(stbIp);
				if (egpMap != null && !egpMap.isEmpty())
				{
					resultMap.putAll(egpMap);
				}
				else
				{
					resultFlag = 0;
					result = "失败";
				}
			}
			else
			{
				resultFlag = 0;
				result = "失败";
			}
		}
		resultMap.put("result_flag", StringUtil.getStringValue(resultFlag));
		resultMap.put("result", result);
		String returnXML = CommonParamUtil.commonReturnParam(resultMap);
		logger.warn("GetStbInfoBIO==>returnXML:" + returnXML);
		return returnXML;
	}
	
	
	/**
	 * 
	 * 
	 * @param inParam
	 * @return
	 */
	public String getStbInfoForOther(String inParam)
	{
		logger.warn("getStbInfoForOther==>inParam:" + inParam);
		
		GetStbInfoChecker checker = new GetStbInfoChecker(inParam);
		
		if (false == checker.check()) {
			logger.warn("获取机顶盒信息接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			
			logger.warn("getStbInfoForOther==>retParam={}", checker.getJXReturnXml());
			
			return checker.getJXReturnXml();
		}
		
		UserStbInfoDAO dao = new UserStbInfoDAO();
		
		ArrayList<HashMap<String,String>> resultList = null;
		// 江西业务会修改serv_account,根据业务账号查询时,必须关联用户表
//		if ("1".equals(checker.getSearchType())) {
//			resultList  = dao.getDeviceDescTime(checker.getSearchType(), checker.getSearchInfo(), "2");
//		}
//		else {
//			resultList  = dao.getDeviceDescTime(checker.getSearchType(), checker.getSearchInfo(), "1");
//		}
		// 根据业务账号,map,序列号查询时,都需要关联用户表
		resultList  = dao.getDeviceDescTime(checker.getSearchType(), checker.getSearchInfo(), "2");
		
		if (null == resultList || resultList.size()<=0)
		{
			checker.setRstCode("0");
			checker.setRstMsg("查无此设备");
			
			logger.warn("查无此设备，serchType={}，searchInfo={}",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			
			logger.warn("getStbInfoForOther==>retParam:" + checker.getJXReturnXml());
			
			return checker.getJXReturnXml();
			
		}
		else {
			// 1、获取机顶盒信息
			list = new ArrayList<HashMap<String, String>>();
			String vendor_id=null;
			String category = null;
			for(HashMap<String,String> infoMap : resultList)
			{
				HashMap<String, String> stbMap = new HashMap<String, String>();
				stbMap.put("stb_ip", infoMap.get("loopback_ip"));
				stbMap.put("serv_account", infoMap.get("serv_account"));
				
				vendor_id=infoMap.get("vendor_id");
				Map<String,String> map=dao.getVendorAdd(vendor_id);
				if(map==null || map.isEmpty()){
					stbMap.put("stb_vendor","");
				}else{
					stbMap.put("stb_vendor",map.get("vendor_add"));
				}
				stbMap.put("stb_type", infoMap.get("device_model"));
				stbMap.put("stb_mac", infoMap.get("cpe_mac"));
				stbMap.put("stb_sn", infoMap.get("device_serialnumber"));
				stbMap.put("softversion", infoMap.get("softwareversion"));
				stbMap.put("stb_status", infoMap.get("device_status"));
				stbMap.put("first_time", new DateTimeUtil().getLongDate(StringUtil.getLongValue(infoMap.get("buy_time"))));
				if ("jx_dx".equals(Global.G_instArea)||"jl_dx".equals(Global.G_instArea)) {
					category = StringUtil.getStringValue(infoMap.get("category"));
					if ("1".equals(category)){
						stbMap.put("stb_type1","4K");
					}else if ("2".equals(category)){
						stbMap.put("stb_type1","高清");
					}else if ("3".equals(category)){
						stbMap.put("stb_type1","标清");
					}else if ("4".equals(category)){
						stbMap.put("stb_type1","融合");
					}else{
						stbMap.put("stb_type1","");
					}
				}
				//JXDX-ITV-REQ-20180822-WUWF-001 返回参数新增bind_time字段
				if ("jx_dx".equals(Global.G_instArea))
				{
					stbMap.put("bind_time", new DateTimeUtil().getLongDate(StringUtil
							.getLongValue(infoMap.get("bind_time"))));
				}
				list.add(stbMap);
			}
			
			if (list == null || list.isEmpty())
			{
				resultFlag = 0;
				result = "失败";
			}
		}
			
		resultMap.put("result_flag", StringUtil.getStringValue(resultFlag));
		resultMap.put("result", result);
		resultMap.put("CmdID", StringUtil.getStringValue(checker.getCmdId()));
		
		String returnXML = CommonParamUtil.commonReturnParam(list, resultMap);
		logger.warn("getStbInfoForOther==>returnXML:" + returnXML);
		return returnXML;
	}
	
	
	

	/**
	 * 获取EGP信息
	 * 
	 * @param stbIp
	 * @return
	 */
	public Map<String, String> getEgpInfo(String stbIp)
	{
		dao = new UserStbInfoDAO();
		// 获取Egp信息
		List<HashMap<String, String>> egpInfoList = dao.getCurrentEpg(stbIp);
		// 获取机顶盒基本信息
		// Map<String,String> stbBaseInfoMap = getBaseInfo(devSn, oui);
		if (egpInfoList == null || egpInfoList.isEmpty())
		{
			logger.warn("获取EGP信息为空");
			return null;
		}
		return egpInfoList.get(0);
	}

	/**
	 * 调用Stbservice接口获取机顶盒基础信息
	 * 
	 * @param devSn
	 * @param oui
	 * @return
	 */
	public Map<String, String> getBaseInfo(String devSn, String oui)
	{
		Map<String, String> map = new HashMap<String, String>();
		// 调用Stbservice接口获取机顶盒基础信息
		String retBaseInfo = new StbService().getUserStbInfo(getStbServiceParam(devSn,
				oui));
		if (StringUtil.IsEmpty(retBaseInfo))
		{
			logger.warn("调用Stbservice接口获取机顶盒基础信息失败");
			result = "获取机顶盒信息失败";
			return null;
		}
		// 解析Stbservice接口回参
		SAXReader reader = new SAXReader();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(retBaseInfo));
			Element root = document.getRootElement();
			map.put("stb_ip", root.elementText("stb_ip"));
			map.put("auth_url", root.elementText("auth_url"));
			map.put("serv_account", root.elementText("stb_username"));
			map.put("stb_type", root.elementText("stb_type"));
			map.put("stb_mac", root.elementText("stb_mac"));
			map.put("stb_sn", root.elementText("stb_sn"));
			map.put("softversion", root.elementText("softversion"));
			map.put("stb_state", root.elementText("stb_state"));
			String mediaIp = root.elementText("media_ip");
			if (StringUtil.IsEmpty(mediaIp) || mediaIp.equals("-1"))
			{
				mediaIp = "";
			}
			map.put("media_ip", mediaIp);
			logger.warn("mediaIp---zhangshimin" + mediaIp);
			map.put("media_state", getMediaStatus(mediaIp, devSn, oui));
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 获取媒体服务器状态
	 * 
	 * @param mediaIp
	 * @param devSn
	 * @param oui
	 * @return
	 */
	private String getMediaStatus(String mediaIp, String devSn, String oui)
	{
		// 检查媒体服务器IP
		if (!CommonUtil.checkIP(mediaIp))
		{
			return Global.STATE_GRAY;
		}// 对媒体服务ping结果
		else if (!pingMediaIp(mediaIp, devSn, oui))
		{
			return Global.STATE_RED;
		}
		else
		{
			return Global.STATE_GREEN;
		}
	}

	/**
	 * 对媒体服务器进行ping
	 * 
	 * @param mediaIp
	 * @param devSn
	 * @param oui
	 * @return
	 */
	private boolean pingMediaIp(String mediaIp, String devSn, String oui)
	{
		/******** ---调用ping接口---- ******/
		// 1、ping接口入参
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("gb2312");
		Element root = document.addElement("root");
		root.addElement("dev_sn").addText(devSn);
		root.addElement("oui").addText(oui);
		root.addElement("ip").addText("9");  // Ping操作接口中需要用到此参数，为了兼容，此处用9表示
		Element device = root.addElement("device");
		device.addElement("ping_ip").addText(mediaIp);
		device.addElement("pack_size").addText("1336");
		device.addElement("pack_num").addText("5");
		device.addElement("timeout").addText("2000");
		device.addElement("dscp").addText("48");
		// 2、执行ping操作
		String retPing = new StbPingService().work(document.asXML());
		if (StringUtil.IsEmpty(retPing))
		{
			logger.warn("对媒体服务器ping操作失败");
			return false;
		}
		// 3、ping接口 回参
		SAXReader reader = new SAXReader();
		Document retDocument = null;
		try
		{
			retDocument = reader.read(new StringReader(retPing));
			Element retRoot = retDocument.getRootElement();
			String pingIp = retRoot.element("device").elementText("ping_ip");
			String successPack = retRoot.element("device").elementText("success_packs");
			logger.warn("#####################successPack----------:" + successPack);
			logger.warn("#####################pingIp----------:" + pingIp);
			if (successPack.equals("0") || !pingIp.equals(mediaIp))
			{
				return false;
			}
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
		}
		return true;
	}

	private String getStbServiceParam(String devSn, String oui)
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GB2312");
		Element root = document.addElement("root");
		root.addElement("device_serialnumber").addText(devSn);
		root.addElement("oui").addText(oui);
		return document.asXML();
	}
}
