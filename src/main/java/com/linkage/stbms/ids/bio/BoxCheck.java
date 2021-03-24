package com.linkage.stbms.ids.bio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.dao.RecordLogDAO;
import com.linkage.stbms.ids.dao.StbDevSNServiceDAO;
import com.linkage.stbms.ids.obj.BoxCheckObj;
import com.linkage.stbms.ids.util.Box_check;
import com.linkage.stbms.itv.main.Global;

/**
 * 根据业务账号查询设备接口
 */
public class BoxCheck
{
	private StbDevSNServiceDAO dao = new StbDevSNServiceDAO();;
	// 正则，字符加数字
	Pattern pattern = Pattern.compile("\\S{1,}");
	private static Logger logger = LoggerFactory.getLogger(BoxCheck.class);
	private String result;
	private int result_flag;
	private String returnXml;
	
	/**
	 * 根据业务账号获取设备信息
	 */
	public String getBoxcheck(String inParam) 
	{
		logger.warn("getUserStbInfo inXml:({})",getStr(inParam));
		
		List<HashMap<String, String>> stbInfoMap = null;
		String servAccount =Box_check.getUserInfot(inParam);
		if (StringUtil.IsEmpty(servAccount)
				|| !pattern.matcher(servAccount).matches()) 
		{
			result_flag = 0;
			if("xj_dx".equals(Global.G_instArea)){
				result_flag = 1;
			}
			result = "非法业务账号: " + servAccount;
		}
		else 
		{
			stbInfoMap = dao.StbDevSNService(servAccount);
			
			if (stbInfoMap == null || stbInfoMap.isEmpty()) {
				result_flag = 0;
				if("xj_dx".equals(Global.G_instArea)){
					result_flag = 1;
				}
				result = "根据业务账号: " + servAccount + "没有查到机顶盒";
			} else {
				result_flag = 1;
				if("xj_dx".equals(Global.G_instArea)){
					result_flag = 0;
				}
				result = "成功";
			}
		}
		
		returnXml=Box_check.boxXML(transferResult(stbInfoMap), result_flag,result);
		if("xj_dx".equals(Global.G_instArea)){
			 new RecordLogDAO().recordLog(servAccount, inParam, "",returnXml,1);
		}
		
		logger.warn("getUserStbInfo({}) returnXml:({})",servAccount,getStr(returnXml));
		return returnXml;
	}
	
	private List<BoxCheckObj> transferResult(List<HashMap<String, String>> stbInfoMap) 
	{
		List<BoxCheckObj> relList = new ArrayList<BoxCheckObj>();
		BoxCheckObj infoObj;
		if(stbInfoMap == null || stbInfoMap.isEmpty()){
			return null;
		}
		
		for(HashMap<String,String> map : stbInfoMap)
		{
			infoObj = new BoxCheckObj();
			infoObj.setDevSn(map.get("device_serialnumber"));
			infoObj.setOui(map.get("oui"));
			
			Map<String, String> stbMap=null;
			if("xj_dx".equals(Global.G_instArea))
			{
				infoObj.setMac(map.get("cpe_mac"));
				
				stbMap=dao.queryInfo(map.get("devicetype_id"));
				
				infoObj.setDevModel(stbMap.get("device_model"));
				infoObj.setVendor(stbMap.get("vendor_name"));
				infoObj.setHardwareVersion(stbMap.get("hardwareversion"));
				infoObj.setSoftwareVersion(stbMap.get("softwareversion"));
				
				stbMap=null;
			}
			
			relList.add(infoObj);
		}
		
		return relList;
	}
	
	/**
	 * xml去换行
	 */
	private String getStr(String str)
	{
		if(!StringUtil.IsEmpty(str)){
			return str.replaceAll("\n","");
		}
		return str;
	}
	
	
	public StbDevSNServiceDAO getDao(){
		return dao;
	}

	public void setDao(StbDevSNServiceDAO dao){
		this.dao = dao;
	}

	public String getResult(){
		return result;
	}
	
	public void setResult(String result){
		this.result = result;
	}
	
	public int getResult_flag(){
		return result_flag;
	}
	
	public void setResult_flag(int result_flag){
		this.result_flag = result_flag;
	}
	
	
}
