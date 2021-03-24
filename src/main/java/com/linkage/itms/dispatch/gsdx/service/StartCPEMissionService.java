package com.linkage.itms.dispatch.gsdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.TimeUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.gsdx.beanObj.CPEMission;
import com.linkage.itms.dispatch.gsdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.gsdx.obj.StartCPEMissionXML;
import com.linkage.itms.dispatch.gsdx.obj.StrategyOBJ;

public class StartCPEMissionService  extends ServiceFather{

	public StartCPEMissionService(String methodName) {
		super(methodName);
	}
	private static Logger logger = LoggerFactory.getLogger(StartCPEMissionService.class);
	private CPEMission result = new CPEMission();
	private StartCPEMissionXML dealXML;
	
	public CPEMission work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		
		dealXML = new StartCPEMissionXML(methodName);
		if(null == dealXML.getXML(inXml)){
			result.setiOperRst(-1000);
			return result;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		// 验证入参
		CpeInfoDao dao = new CpeInfoDao();
		//设备
		Map<String, String> queryUserInfo = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}",queryUserInfo);
		if(null == queryUserInfo || queryUserInfo.isEmpty() || StringUtil.isEmpty(StringUtil.getStringValue(queryUserInfo, "device_id"))){
			result.setiOperRst(0);
			return result;
		}
		// 取得设备信息
		String deviceId = StringUtil.getStringValue(queryUserInfo, "device_id");
		List<HashMap<String, String>> deviceInfo = dao.getDeviceInfo(deviceId);
		if(null == deviceInfo || deviceInfo.isEmpty()){
			result.setiOperRst(0);
			return result;
		}
		// 获取版本ID   vendor_id  device_model_id
		String vendor_id = StringUtil.getStringValue(deviceInfo.get(0), "vendor_id");
		String device_model_id = StringUtil.getStringValue(deviceInfo.get(0), "device_model_id");
		String old_devicetypeId = StringUtil.getStringValue(deviceInfo.get(0), "devicetype_id");
        Map<String, String> deviceTypeInfo = dao.getDeviceTypeInfo(vendor_id,device_model_id,dealXML.getFileName());
        if(null == deviceTypeInfo || deviceTypeInfo.isEmpty()){
        	result.setiOperRst(-2);
        	return result;
        }
        String new_devicetypeId = StringUtil.getStringValue(deviceTypeInfo,"devicetype_id");
        Map<String, Map<String, String>> softFileInfo = dao.getSoftFileInfo();
        if(null == softFileInfo || softFileInfo.isEmpty()){
        	result.setiOperRst(-1);
        	return result;
        }
        
        // 查看版本文件是否存在
        Map<String, String> map = softFileInfo.get(new_devicetypeId);
        if(null == map || map.isEmpty()){
        	result.setiOperRst(-1);
        	return result;
        }
        String softSheet_para = softUpXml(deviceId,old_devicetypeId,new_devicetypeId,map);
       
        ArrayList<String> sqllist = new ArrayList<String>();
		String[] stragetyIds = {deviceId};
		// 配置的service_id
		StrategyOBJ strategyOBJ = new StrategyOBJ();
		strategyOBJ.setServiceId(5);
		strategyOBJ.createId();
		strategyOBJ.setDeviceId(deviceId);
		strategyOBJ.setTime(TimeUtil.getCurrentTime());
		strategyOBJ.setSheetPara(softSheet_para);
		strategyOBJ.setAccOid(1);
		strategyOBJ.setOrderId(1);
		strategyOBJ.setIsLastOne(1);
		strategyOBJ.setPriority(1);
		strategyOBJ.setType(0);
		strategyOBJ.setSheetType(2);
		strategyOBJ.setTempId(5);
		// 入策略表
		Boolean strategySQL  = dao.strategySQL(strategyOBJ);
		if(strategySQL){
			logger.warn("入库成功");
			long id = strategyOBJ.getId();
			// 调用预读
			logger.warn("立即执行，开始调用配置...");
			if (true == CreateObjectFactory.createPreProcess("1").processOOBatch(stragetyIds))
			{
				result.setiMissionID(StringUtil.getIntegerValue(id));
				result.setiOperRst(1);
			}
			else
			{
				result.setiOperRst(-1000);
			}
		}else{
			logger.warn("入库失败");
			result.setiOperRst(-1000);
		}
		return result;
	}
	
	/**
	 * 查询设备信息
	 * 
	 * @param deviceId
	 * @return
	 */
	private String softUpXml(String deviceId,String OldDeviceTypeId,String newDeviceTypeId,Map<String,String> map)
	{
		logger.debug("softUpXml({})", map);
		String strXml = null;
		if (map == null || map.isEmpty())
		{
		}
		else
		{
			// new doc
			Document doc = DocumentHelper.createDocument();
			// root node: NET
			Element root = doc.addElement("SoftUpdate");
			root.addAttribute("flag", "1");
			root.addElement("CommandKey").addText("SoftUpdate");
			root.addElement("FileType").addText("1 Firmware Upgrade Image");
			root.addElement("URL")
					.addText(StringUtil.getStringValue(this.getFilePath(deviceId, map)));
			root.addElement("Username").addText("");
			root.addElement("Password").addText("");
			root.addElement("FileSize").addText(
					StringUtil.getStringValue(map.get("softwarefile_size")));
			root.addElement("TargetFileName").addText(
					StringUtil.getStringValue(map.get("softwarefile_name")));
			root.addElement("DelaySeconds").addText("");
			root.addElement("SuccessURL").addText("");
			root.addElement("FailureURL").addText("");
			root.addElement("NewDeviceTypeId").addText(newDeviceTypeId);
			root.addElement("OldDeviceTypeId").addText(OldDeviceTypeId);
			strXml = doc.asXML();
		}
		return strXml;
	}
	
	/**
	 * 软件升级 文件服务器路径负载算法
	 * http://ip:9090/FileServer;http://ip:9090/FileServer;http://ip:7000/FileServer/FILE/SOFT/HG8245V100R003C00SPC106
	 * @return
	 */
	public String getFilePath(String deviceId,Map<String, String> softParamMap)
	{
		String url = softParamMap.get("file_url");
		String fileName = "";
		if(url.contains(";")){
			//截取文件路径： /HG8245V100R003C00SPC106
			fileName = url.substring(url.indexOf("/FILE/SOFT"), url.length());
			//截取文件服务器地址，转换数组：http://ip:9090/FileServer;http://ip:9090/FileServer;http://ip:7000/FileServer
			String[] filePathArr = url.substring(0,url.indexOf("/FILE/SOFT")).split(";");
			int index = hash(filePathArr,deviceId);
			return filePathArr[index]+fileName;
		}
		
		return url;
	}
	/**
	 * 算法
	 * 
	 * @param servNodes
	 * @param servKey
	 * @return
	 */
	private static int hash(String[] PathArr, String deviceId)
	{
		return StringUtil.getIntegerValue(deviceId) % PathArr.length;
	}

}
