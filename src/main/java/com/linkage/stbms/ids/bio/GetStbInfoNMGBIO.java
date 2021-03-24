package com.linkage.stbms.ids.bio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.DateUtil;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.util.GetStbInfoNMGChecker;

/**
 * 
 * @author hourui
 * @since 2017-11-20
 * @NMG电信 机顶盒信息查询接口
 * 
 */
public class GetStbInfoNMGBIO {
	private static final Logger logger = LoggerFactory.getLogger(GetStbInfoNMGBIO.class);
	private Map<String, String> resultMap = new HashMap<String, String>();
	ArrayList<HashMap<String, String>> list = null;
	// 正则，字符加数字
	Pattern pattern = Pattern.compile("\\w{1,}+");
	private String result = "成功";
	private int resultFlag = 1;

	/**
	 * @param inParam
	 * @return
	 */
	public String getStbInfoForNMG(String inParam) {
		logger.warn("getStbInfoForNMG==>inParam:" + inParam);

		GetStbInfoNMGChecker checker = new GetStbInfoNMGChecker(inParam);

		if (false == checker.check()) {
			logger.warn("获取机顶盒信息接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(),
							checker.getSearchInfo() });

			logger.warn("getStbInfoForNMG==>retParam={}", checker.getReturnXml());

			return checker.getReturnXml();
		}

		UserStbInfoDAO dao = new UserStbInfoDAO();
		
		ArrayList<HashMap<String, String>> resultList = dao.getStbInfoNMGinfo(checker.getSearchType(), checker.getSearchInfo());

		if (null == resultList || resultList.size() <= 0) {
			checker.setRstCode("1004");
			checker.setRstMsg("查无此设备");

			logger.warn("查无此设备，serchType={}，searchInfo={}", new Object[] {
					checker.getSearchType(), checker.getSearchInfo() });

			logger.warn("getStbInfoForNMG==>retParam:" + checker.getReturnXml());

			return checker.getReturnXml();

		} 
		
		if (resultList.size() > 1) {
			checker.setRstCode("1006");
			checker.setRstMsg("查到多台设备,请输入更多位序列号或完整序列号进行查询");

			logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询，serchType={}，searchInfo={}", new Object[] {
					checker.getSearchType(), checker.getSearchInfo() });

			logger.warn("getStbInfoForNMG==>retParam:" + checker.getReturnXml());

			return checker.getReturnXml();

		} 
		String deviceId = resultList.get(0).get("device_id");
		if (null == deviceId || deviceId.trim().length() == 0) {
			logger.warn("设备为空");
			checker.setRstCode("1000");
			checker.setRstMsg("设备为空");
			logger.warn("getStbInfoForNMG==>returnXML:" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		// 获取机顶盒信息
		list = new ArrayList<HashMap<String, String>>();
			for (HashMap<String, String> infoMap : resultList) {
				LinkedHashMap<String, String> stbMap = new LinkedHashMap<String, String>();
				stbMap.put("iptvNo", infoMap.get("serv_account"));
				stbMap.put("iptvAnId", infoMap.get("pppoe_user"));
				stbMap.put("snId", infoMap.get("device_serialnumber"));
				stbMap.put("stbId", infoMap.get("stb_id"));
				stbMap.put("macAddr", infoMap.get("cpe_mac"));
				stbMap.put("facturer", infoMap.get("vendor_name"));
				stbMap.put("model", infoMap.get("device_model"));
				stbMap.put("hardwareVersion", infoMap.get("hardwareversion"));
				stbMap.put("softwareVersion", infoMap.get("softwareversion"));		
				stbMap.put("accessTime", new DateTimeUtil().getLongDate(StringUtil.getLongValue(infoMap.get("complete_time"))));
				stbMap.put("stbIp", infoMap.get("loopback_ip"));
				stbMap.put("authState", infoMap.get("cust_stat"));
				stbMap.put("onlineState", infoMap.get("online_status"));
				stbMap.put("LOID", "null");
				list.add(stbMap);
			} 

		if (list == null || list.size() <= 0) {
			checker.setRstCode("1000");
			checker.setRstMsg("信息为空");

			logger.warn("信息为空，serchType={}，searchInfo={}", new Object[] {	checker.getSearchType(), checker.getSearchInfo() });

			logger.warn("getStbInfoForNMG==>retParam:" + checker.getReturnXml());

			return checker.getReturnXml();
		}
		
		resultMap.put("CmdID", StringUtil.getStringValue(checker.getCmdId()));
		resultMap.put("RstCode", StringUtil.getStringValue(resultFlag));
		resultMap.put("RstMsg", result);
		String returnXML = commonReturnParamXJ(list, resultMap);
		logger.warn("getStbInfoForNMG==>returnXML:" + returnXML);
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
		root.addElement("result_flag").addText(infoMap.get("RstCode"));
		root.addElement("result").addText(infoMap.get("RstMsg"));
		
		Element Param= root.addElement("stbInfo");

		
		for(HashMap<String,String> devMap : devList)
		{
			for(Object keyStr : devMap.keySet())
			{
				Param.addElement(keyStr.toString()).addText(devMap.get(keyStr));
			}
		}
		return document.asXML();
	}


	
	public String getResult()
	{
		return result;
	}


	
	public void setResult(String result)
	{
		this.result = result;
	}


	
	public int getResultFlag()
	{
		return resultFlag;
	}


	
	public void setResultFlag(int resultFlag)
	{
		this.resultFlag = resultFlag;
	}

}
