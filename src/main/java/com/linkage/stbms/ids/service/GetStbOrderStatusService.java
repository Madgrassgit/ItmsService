	package com.linkage.stbms.ids.service;

	import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.dao.RecordLogDAO;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.util.GetStbOrderStatusChecker;
import com.linkage.stbms.itv.main.Global;

	/**
	 * 新疆电信  机顶盒零配置状态查询接口
	 * @author chenxj6
	 * @date 2016-11-11
	 * @param inParam
	 * @return
	 */
	
	public class GetStbOrderStatusService
	{
		private static Logger logger = LoggerFactory.getLogger(GetStbOrderStatusService.class);

		public String work(String inParam)
		{
			logger.warn("GetStbOrderStatusService==>inParam:" + inParam);
			GetStbOrderStatusChecker checker = new GetStbOrderStatusChecker(inParam);
			// 入参验证
			if (false == checker.check())
			{
				logger.warn("GetStbOrderStatusService接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
						new Object[] { checker.getSearchType(), checker.getSearchInfo() });
				logger.warn("GetStbOrderStatusService==>return：" + checker.getReturnXml());
				return checker.getReturnXml();
			}
			

			try {
				String deviceId = null;
				UserStbInfoDAO dao = new UserStbInfoDAO();
				List<HashMap<String,String>> userMapList = null;
				
//				设备序列号（可填后6位），如果该数据不为空则优先按设备信息查询，如果该数据为空则按用户信息查询，当类型为3时此处为MAC地址
				if(null==checker.getDeviceInfo() || checker.getDeviceInfo().trim().length()==0){
					userMapList = dao.getDeviceInfo(checker.getSearchType(),checker.getSearchInfo());
					if (null == userMapList || userMapList.size() == 0) {
						logger.warn("没有查到设备或设备和用户之间未绑定，serchType={}，searchInfo={}",new Object[] { checker.getSearchType(),checker.getSearchInfo() });
						checker.setRstCode("1000");
						checker.setRstMsg("没有查到设备或设备和用户之间未绑定");
						logger.warn("GetStbOrderStatusService==>returnXML:" + checker.getReturnXml());
						
						if("xj_dx".equals(Global.G_instArea)){
							 new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
		                                checker.getReturnXml(), 1);
						}

						return checker.getReturnXml();
					}
					if (userMapList.size() > 1) {
						logger.warn("查到多组设备，请输入更多位设备序列号进行查询，serchType={}，searchInfo={}",new Object[] { checker.getSearchType(),checker.getSearchInfo() });
						checker.setRstCode("1000");
						checker.setRstMsg("查到多组设备，请输入更多位设备序列号进行查询");
						logger.warn("GetStbOrderStatusService==>returnXML:" + checker.getReturnXml());
						if("xj_dx".equals(Global.G_instArea)){
							 new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
		                                checker.getReturnXml(), 1);
						}
						return checker.getReturnXml();
					}
				
					deviceId = userMapList.get(0).get("device_id");
					if (null == deviceId || deviceId.trim().length() == 0) {
						logger.warn("设备为空");
						checker.setRstCode("1000");
						checker.setRstMsg("设备为空");
						logger.warn("GetStbOrderStatusService==>returnXML:" + checker.getReturnXml());
						if("xj_dx".equals(Global.G_instArea)){
							 new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
		                                checker.getReturnXml(), 1);
						}
						return checker.getReturnXml();
					}
				}else{
					if("3".equals(checker.getSearchType())){
						userMapList = dao.getDeviceInfo(checker.getSearchType(),checker.getDeviceInfo());
					}else{
						userMapList = dao.getDeviceInfo("2",checker.getDeviceInfo());
					}
					
					if (null == userMapList || userMapList.size() == 0) {
						logger.warn("没有查到设备或设备和用户之间未绑定，serchType={}，DeviceInfo={}",new Object[] { checker.getSearchType(),checker.getDeviceInfo() });
						checker.setRstCode("1000");
						checker.setRstMsg("没有查到设备或设备和用户之间未绑定");
						logger.warn("GetStbOrderStatusService==>returnXML:" + checker.getReturnXml());
						if("xj_dx".equals(Global.G_instArea)){
							 new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
		                                checker.getReturnXml(), 1);
						}
						return checker.getReturnXml();
					}
					if (userMapList.size() > 1) {
						logger.warn("查到多组设备，请输入更多位设备序列号进行查询，serchType={}，DeviceInfo={}",new Object[] { checker.getSearchType(),checker.getDeviceInfo() });
						checker.setRstCode("1000");
						checker.setRstMsg("查到多组设备，请输入更多位设备序列号进行查询");
						logger.warn("GetStbOrderStatusService==>returnXML:" + checker.getReturnXml());
						if("xj_dx".equals(Global.G_instArea)){
							 new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
		                                checker.getReturnXml(), 1);
						}
						return checker.getReturnXml();
					}
				
					deviceId = userMapList.get(0).get("device_id");
					if (null == deviceId || deviceId.trim().length() == 0) {
						logger.warn("设备为空");
						checker.setRstCode("1000");
						checker.setRstMsg("设备为空");
						logger.warn("GetStbOrderStatusService==>returnXML:" + checker.getReturnXml());
						if("xj_dx".equals(Global.G_instArea)){
							 new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
		                                checker.getReturnXml(), 1);
						}
						return checker.getReturnXml();
					}
				}
				
				String user_status = userMapList.get(0).get("user_status");
				if("1".equals(user_status)){
					checker.setRstCode("0");
					checker.setRstMsg("成功");
					checker.setErrordesc("");
					logger.warn("GetStbOrderStatusService==>returnXML:" + checker.getReturnXml());
				}else{
					HashMap<String,String> faultMap = (HashMap<String, String>) dao.getStrategyInfo(deviceId, "120");
					if(null==faultMap || faultMap.size()==0){
						logger.warn("策略表中查不到策略");
						checker.setRstCode("1000");
						checker.setRstMsg("策略表中查不到策略");
						logger.warn("GetStbOrderStatusService==>returnXML:" + checker.getReturnXml());
						if("xj_dx".equals(Global.G_instArea)){
							 new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
		                                checker.getReturnXml(), 1);
						}
						return checker.getReturnXml();
					}
					String result_id = faultMap.get("result_id");
					checker.setRstCode("1000");
					checker.setRstMsg("失败");
					checker.setErrordesc(Global.G_Fault_Map.get(StringUtil.getIntegerValue(result_id)));
					logger.warn("GetStbOrderStatusService==>returnXML:" + checker.getReturnXml());
					
				}
			} catch (Exception e) {
				e.printStackTrace(); 
			}
			if("xj_dx".equals(Global.G_instArea)){
				 new RecordLogDAO().recordLog(checker.getSearchInfo(), checker.getInParam(), "",
                           checker.getReturnXml(), 1);
			}
			return checker.getReturnXml();
		}
	}

