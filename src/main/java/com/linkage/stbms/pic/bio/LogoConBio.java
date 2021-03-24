
package com.linkage.stbms.pic.bio;

import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.TimeUtil;
import com.linkage.stbms.pic.Global;
import com.linkage.stbms.pic.dao.LogoConDao;
import com.linkage.stbms.pic.object.StrategyObj;
import com.linkage.stbms.pic.util.SqlUtil;
import com.linkage.stbms.pic.util.StrUtil;

/**
 * @author 王森博(66168) Tel:
 * @version 1.0
 * @since Apr 8, 2013 5:15:29 PM
 * @category com.linkage.litms.preprocess.bio
 * @copyright 南京联创科技 网管科技部 现在版本只支持批量导入升级
 */
public class LogoConBio
{

	private static final Logger logger = LoggerFactory.getLogger(LogoConBio.class);
	private LogoConDao dao = new LogoConDao();

	/**
	 * STB软件升级XML
	 * 
	 * @author gongsj
	 * @date 2010-11-8
	 * @param taskInfoMap
	 * @return
	 */
	private String logo2Xml(Map<String, String> taskInfoMap)
	{
		logger.debug("logo2Xml...");
		String strXml = null;
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("STB");
		
		Element StartPicURL = root.addElement("StartPicURL");
		Element BootPicURL = root.addElement("BootPicURL");
		Element AuthenticatePicURL = root.addElement("AuthenticatePicURL");
		
		if(!StrUtil.IsEmpty(taskInfoMap.get("sd_qd_pic_url"))){
			StartPicURL.addAttribute("flag", "1").addText(taskInfoMap.get("sd_qd_pic_url"));
		}else{
			StartPicURL.addAttribute("flag", "0");
		}
		
		if(!StrUtil.IsEmpty(taskInfoMap.get("sd_kj_pic_url"))){
			BootPicURL.addAttribute("flag", "1").addText(taskInfoMap.get("sd_kj_pic_url"));
		}else{
			BootPicURL.addAttribute("flag", "0");
		}
		
		if(!StrUtil.IsEmpty(taskInfoMap.get("sd_rz_pic_url"))){
			AuthenticatePicURL.addAttribute("flag", "1").addText(taskInfoMap.get("sd_rz_pic_url"));	
		}else{
			AuthenticatePicURL.addAttribute("flag", "0");	
		}
		
		
		
		strXml = doc.asXML();
		return strXml;
	}

	/**
	 * 获得策略对象
	 * 
	 * @author gongsj
	 * @date 2010-11-8
	 * @param deviceId
	 * @param sheetPara
	 * @return
	 */
	private StrategyObj getStrategyObj(String deviceId, String taskId, String sheetPara)
	{
		StrategyObj strategyOBJ = new StrategyObj();
		strategyOBJ.setServiceId(StrUtil.getIntegerValue(Global.SERVICE_ID_STB_LOGO_CON));
		strategyOBJ.createId();
		strategyOBJ.setDeviceId(deviceId);
		strategyOBJ.setTime(TimeUtil.getCurrentTime());
		strategyOBJ.setSheetPara(sheetPara);
		strategyOBJ.setAccOid(1);
		strategyOBJ.setOrderId(1);
		strategyOBJ.setIsLastOne(1);
		strategyOBJ.setPriority(1);
		strategyOBJ.setType(4);
		// 是新类型的策略，策略参数为XML，组装模板
		strategyOBJ.setSheetType(2);
		strategyOBJ.setRedo(0);
		strategyOBJ.setTempId(StrUtil.getIntegerValue(Global.SERVICE_ID_STB_LOGO_CON));
		strategyOBJ.setTaskId(taskId);
		return strategyOBJ;
	}
	
	/**
	 * 
	 * 定制软件升级
	 * 
	 * @param deviceId
	 * @param deviceTypeId
	 * @param userName
	 * @return
	 */
	public StrategyObj getLogoConStrategy(String deviceId)
	{
		String taskId = dao.queryHaveTask(deviceId);
		if(StrUtil.IsEmpty(taskId)){
			logger.warn("设备[{}]没有需要执行的开机画面任务", new Object[] { deviceId });
			return null;
		}else{
			Map<String, String> taskInfoMap = dao.getBatchConTaskInfo(taskId);
			if(taskInfoMap == null ||taskInfoMap.isEmpty()){
				logger.warn("设备[{}]开机画面任务[{}]不存在", new Object[] { deviceId, taskId });
				return null;
			}else{
				StrategyObj strategyObj = getStrategyObj(deviceId, taskId, logo2Xml(taskInfoMap));
				insertAndInvokePP(deviceId, taskId, strategyObj);
				return strategyObj;
			}
		}
	}
	
	/**
	 * 入策略表和机顶盒版本升级记录表
	 * 
	 * @author gongsj
	 * @date 2010-11-8
	 * @param deviceId
	 * @param taskId
	 * @param deviceTypeId
	 * @param strategyObj
	 */
	private void insertAndInvokePP(String deviceId, String taskId,
			StrategyObj strategyObj)
	{
		SqlUtil sqlUtil = new SqlUtil();
		sqlUtil.addStrategy(strategyObj);
		//xzl  ITVProcess已经插入 ，这里取消插入
//		dao.insertRecord(strategyObj.getId(), deviceId, StrUtil.getLongValue(taskId));
		dao.updateRecent(deviceId, StrUtil.getLongValue(taskId));
	}
	
}
