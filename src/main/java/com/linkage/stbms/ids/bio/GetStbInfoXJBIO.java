package com.linkage.stbms.ids.bio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.dao.RecordLogDAO;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.util.GetStbInfoXJChecker;
import com.linkage.stbms.itv.main.Global;

/**
 * 
 * @author chenxj6
 * @since 2016-9-28
 * @新疆电信 机顶盒信息查询接口
 * 
 */
public class GetStbInfoXJBIO {
	private static final Logger logger = LoggerFactory.getLogger(GetStbInfoXJBIO.class);
	private Map<String, String> resultMap = new HashMap<String, String>();
	ArrayList<HashMap<String, String>> list = null;
	// 正则，字符加数字
	Pattern pattern = Pattern.compile("\\w{1,}+");
	private String result = "成功";
	private int resultFlag = 0;

	/**
	 * @param inParam
	 * @return
	 */
	public String getStbInfoForXJ(String inParam) {
		logger.warn("getStbInfoForXJ==>inParam:" + inParam);

		GetStbInfoXJChecker checker = new GetStbInfoXJChecker(inParam);

		if (false == checker.check()) {
			logger.warn(
					"获取机顶盒信息接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(),
							checker.getSearchInfo() });

			logger.warn("getStbInfoForXJ==>retParam={}", checker.getReturnXml());

			return checker.getReturnXml();
		}

		UserStbInfoDAO dao = new UserStbInfoDAO();
		
		ArrayList<HashMap<String, String>> resultList = dao.getDeviceDescTimeForXJ(checker.getSearchType(), checker.getSearchInfo());

		if (null == resultList || resultList.size() <= 0) {
			checker.setRstCode("1004");
			checker.setRstMsg("查无此设备");

			logger.warn("查无此设备，serchType={}，searchInfo={}", new Object[] {
					checker.getSearchType(), checker.getSearchInfo() });

			logger.warn("getStbInfoForXJ==>retParam:" + checker.getReturnXml());
			if("xj_dx".equals(Global.G_instArea)){
				new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
	                    checker.getReturnXml(), 1);
			}

			return checker.getReturnXml();

		} 
		
		if (resultList.size() > 1) {
			checker.setRstCode("1006");
			checker.setRstMsg("查到多台设备,请输入更多位序列号或完整序列号进行查询");

			logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询，serchType={}，searchInfo={}", new Object[] {
					checker.getSearchType(), checker.getSearchInfo() });

			logger.warn("getStbInfoForXJ==>retParam:" + checker.getReturnXml());
			if("xj_dx".equals(Global.G_instArea)){
				new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
	                    checker.getReturnXml(), 1);
			}

			return checker.getReturnXml();

		} 
		
		// 获取机顶盒信息
		list = new ArrayList<HashMap<String, String>>();
		
		//宁夏查询机顶盒版本信息接口，与新疆公用service和check，返回拉分支
		//add by fanjm 20161212
		if ("nx_dx".equals(Global.G_instArea)) {
			for (HashMap<String, String> infoMap : resultList) {
				HashMap<String, String> stbMap = new HashMap<String, String>();
				stbMap.put("stb_devsn", infoMap.get("device_serialnumber"));
				stbMap.put("stb_vendor", infoMap.get("vendor_name"));
				stbMap.put("stb_model", infoMap.get("device_model"));
				stbMap.put("serv_hardwareversion", infoMap.get("hardwareversion"));
				stbMap.put("stb_softwareversion", infoMap.get("softwareversion"));
				stbMap.put("stb_mac", infoMap.get("cpe_mac"));
				stbMap.put("stb_ip", infoMap.get("loopback_ip"));
				list.add(stbMap);
			}
		}
		else
		{
			for (HashMap<String, String> infoMap : resultList) {
				HashMap<String, String> stbMap = new HashMap<String, String>();
				stbMap.put("ip", infoMap.get("loopback_ip"));
				stbMap.put("vendor", infoMap.get("vendor_name"));
				stbMap.put("DevModel", infoMap.get("device_model"));
				stbMap.put("devMAC", infoMap.get("cpe_mac"));
				stbMap.put("DevSN", infoMap.get("device_serialnumber"));
				stbMap.put("SoftwareVersion", infoMap.get("softwareversion"));
				stbMap.put("HandwareVersion", infoMap.get("hardwareversion"));
				list.add(stbMap);
			}
		}

		if (list == null || list.size() <= 0) {
			checker.setRstCode("1000");
			checker.setRstMsg("信息为空");

			logger.warn("信息为空，serchType={}，searchInfo={}", new Object[] {	checker.getSearchType(), checker.getSearchInfo() });

			logger.warn("getStbInfoForXJ==>retParam:" + checker.getReturnXml());
			if("xj_dx".equals(Global.G_instArea)){
				new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
	                    checker.getReturnXml(), 1);
			}

			return checker.getReturnXml();
		}
		
		resultMap.put("CmdID", StringUtil.getStringValue(checker.getCmdId()));
		resultMap.put("RstCode", StringUtil.getStringValue(resultFlag));
		resultMap.put("RstMsg", result);
		String returnXML = commonReturnParamXJ(list, resultMap);
		logger.warn("getStbInfoForXJ==>returnXML:" + returnXML);
		if("xj_dx".equals(Global.G_instArea)){
			new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
                    checker.getReturnXml(), 1);
		}
		return returnXML;
	}
	
	
	/**
	 * 生成接口回参
	 * 
	 * @param infoMap
	 * @param infoMap 
	 * @return
	 */
	public static String commonReturnParamXJ(ArrayList<HashMap<String, String>> devList, Map<String, String> infoMap)
	{
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GB2312");
		Element root = document.addElement("root");
		if(infoMap != null && !infoMap.isEmpty()){
			for(Object key : infoMap.keySet())
			{
				root.addElement(key.toString()).addText(infoMap.get(key));
			}
		}
		
		//宁夏查询机顶盒版本信息接口，返回具体参数在<Sheets>节点下
		//add by fanjm 20161212
		Element Param;
		if ("nx_dx".equals(Global.G_instArea)) {
			Param = root.addElement("Sheets");
		}
		else{
			Param = root.addElement("Param");
		}
		
		for(HashMap<String,String> devMap : devList)
		{
			for(Object keyStr : devMap.keySet())
			{
				Param.addElement(keyStr.toString()).addText(devMap.get(keyStr));
			}
		}
		return document.asXML();
	}

}
