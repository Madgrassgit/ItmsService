	package com.linkage.stbms.ids.service;

	import java.util.ArrayList;
	import java.util.HashMap;
import java.util.LinkedHashMap;
	import java.util.List;
	import java.util.Map;
	
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
import com.linkage.stbms.ids.util.GetStbOrderStatusNMGChecker;

	/**
	 * 内蒙古电信  机顶盒状态查询接口
	 * @author hourui
	 * @date 2017-11-20
	 * @param inParam
	 * @return
	 */
	
	public class GetStbOrderStatusNMGService
	{
		private static Logger logger = LoggerFactory.getLogger(GetStbOrderStatusNMGService.class);
		ArrayList<HashMap<String, String>> list = null;
		private Map<String, String> resultMap = new HashMap<String, String>();
		private String result = "成功";
		private int resultFlag = 1;
		
		public String work(String inParam)
		{
			logger.warn("GetStbOrderStatusNMGService==>inParam:" + inParam);
			GetStbOrderStatusNMGChecker checker = new GetStbOrderStatusNMGChecker(inParam);
			// 入参验证
			if (false == checker.check())
			{
				logger.warn("GetStbOrderStatusNMGService接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
						new Object[] { checker.getSearchType(), checker.getSearchInfo() });
				logger.warn("GetStbOrderStatusNMGService==>return：" + checker.getReturnXml());
				return checker.getReturnXml();
			}
			

			try {
				UserStbInfoDAO dao = new UserStbInfoDAO();
				List<HashMap<String,String>> userMapList =dao.getStbOrderStatusNMGInfo(checker.getSearchType(),checker.getSearchInfo());
					if (null == userMapList || userMapList.size() <= 0) {
						checker.setRstCode("1004");
						checker.setRstMsg("查无此设备");

						logger.warn("查无此设备，serchType={}，searchInfo={}", new Object[] {
								checker.getSearchType(), checker.getSearchInfo() });

						logger.warn("GetStbOrderStatusNMGService==>retParam:" + checker.getReturnXml());

						return checker.getReturnXml();

					} 
					
					if (userMapList.size() > 1) {
						checker.setRstCode("1006");
						checker.setRstMsg("查到多台设备,请输入更多位序列号或完整序列号进行查询");

						logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询，serchType={}，searchInfo={}", new Object[] {
								checker.getSearchType(), checker.getSearchInfo() });

						logger.warn("GetStbOrderStatusNMGService==>retParam:" + checker.getReturnXml());

						return checker.getReturnXml();

					} 
					String deviceId = userMapList.get(0).get("device_id");
					if (null == deviceId || deviceId.trim().length() == 0) {
						logger.warn("设备为空");
						checker.setRstCode("1000");
						checker.setRstMsg("设备为空");
						logger.warn("GetStbOrderStatusNMGService==>returnXML:" + checker.getReturnXml());
						return checker.getReturnXml();
					}
					// 获取机顶盒信息
					list = new ArrayList<HashMap<String, String>>();
						for (HashMap<String, String> infoMap : userMapList) 
						{
							LinkedHashMap<String, String> stbMap = new LinkedHashMap<String, String>();
							stbMap.put("bandNo", infoMap.get("pppoe_user"));
							stbMap.put("iptvNo", infoMap.get("serv_account"));
							stbMap.put("macAddr", infoMap.get("cpe_mac"));	
							stbMap.put("stbId", infoMap.get("stb_id"));
							stbMap.put("lastLogin", new DateTimeUtil().getLongDate(StringUtil.getLongValue(infoMap.get("last_time"))));				
							stbMap.put("onlineState", infoMap.get("online_status"));
							stbMap.put("authState", infoMap.get("cust_stat"));
							stbMap.put("AuthTime",  new DateTimeUtil().getLongDate(StringUtil.getLongValue(infoMap.get("complete_time"))));
							list.add(stbMap);
						} 

					if (list == null || list.size() <= 0) {
						checker.setRstCode("1000");
						checker.setRstMsg("信息为空");

						logger.warn("信息为空，serchType={}，searchInfo={}", new Object[] {	checker.getSearchType(), checker.getSearchInfo() });

						logger.warn("getStbInfoForNMG==>retParam:" + checker.getReturnXml());

						return checker.getReturnXml();
					}
					resultMap.put("RstCode", StringUtil.getStringValue(resultFlag));
					resultMap.put("RstMsg", result);
					String returnXML = commonReturnParamNMG(list, resultMap);
					logger.warn("getStbInfoForNMG==>returnXML:" + returnXML);
					return returnXML;
					
				}
			 catch (Exception e) {
				e.printStackTrace(); 
			}
			return checker.getReturnXml();
		}
		public static String commonReturnParamNMG(ArrayList<HashMap<String, String>> devList, Map<String, String> infoMap)
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
	}

