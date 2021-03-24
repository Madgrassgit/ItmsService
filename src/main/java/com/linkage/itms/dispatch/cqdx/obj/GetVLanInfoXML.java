package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * bind方法接口的XML元素对象
 * 
 * @author hourui(76958)
 * @date 2010-6-17
 */
public class GetVLanInfoXML extends BaseDealXML {

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(GetVLanInfoXML.class);

	private String callXml;
	//用户帐号
	private String ppp_username;
	//逻辑ID
	private String logic_id;
	private List<HashMap<String, String>> resMapList=null;
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public GetVLanInfoXML(String inXml) {
		callXml = inXml;
	}

	/**
	 * 检查接口调用字符串的合法性
	 */
	public boolean check() {
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			ppp_username=root.elementTextTrim("ppp_username");
			logic_id=root.elementTextTrim("logic_id");
		
			
			if( StringUtil.IsEmpty(ppp_username)  && StringUtil.IsEmpty(logic_id)  )
			{
				this.result ="-99";
				this.errMsg ="宽带账号和逻辑ID同时为空";
				return false;
			}


		} catch (Exception e) {
			e.printStackTrace();
			result = "-99";
			errMsg = "数据格式错误";
			return false;
		}

		return true;
	}


	/**
	 * 返回绑定调用结果字符串
	 */
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("GBK");		
		Element root = document.addElement("response");
		// 结果代码
		root.addElement("result_code").addText(result);
		// 结果描述
		root.addElement("result_desc").addText(errMsg);
		Element result_info = root.addElement("result_info");
		String serv_type_id="";
		String multicast_vlanid="";
		String broadband_vlanid = "";
		String iptv_vlanid = "";
		String voip_vlanid = "";
        if(resMapList != null && resMapList.size() > 0)
        {
        	for(int i=0;i<resMapList.size();i++){
        		HashMap<String,String> resMap = resMapList.get(i);
        		serv_type_id=resMap.get("serv_type_id");
        		if("10".equals(serv_type_id)){
        			broadband_vlanid = broadband_vlanid + StringUtil.getStringValue(resMap, "vlanid") +",";
        		}
        		else if("11".equals(serv_type_id))
    			{
        			iptv_vlanid = iptv_vlanid + StringUtil.getStringValue(resMap, "vlanid")  + ",";
        			multicast_vlanid= multicast_vlanid + StringUtil.getStringValue(resMap, "multicast_vlanid") + ",";
    			}
        		else if("14".equals(serv_type_id))
    			{
        			voip_vlanid = voip_vlanid + StringUtil.getStringValue(resMap, "vlanid")  + ",";
    			}
        		
        	}
        	broadband_vlanid = StringUtil.IsEmpty(broadband_vlanid)?"":broadband_vlanid.substring(0,broadband_vlanid.length()-1);
        	iptv_vlanid = StringUtil.IsEmpty(iptv_vlanid)?"":iptv_vlanid.substring(0,iptv_vlanid.length()-1);
        	voip_vlanid = StringUtil.IsEmpty(voip_vlanid)?"":voip_vlanid.substring(0,voip_vlanid.length()-1);
        	multicast_vlanid = StringUtil.IsEmpty(multicast_vlanid)?"":multicast_vlanid.substring(0,multicast_vlanid.length()-1);
        	
			result_info.addElement("broadband_vlanid").addText(broadband_vlanid);
			result_info.addElement("iptv_vlanid").addText(iptv_vlanid);
			result_info.addElement("voip_vlanid").addText(voip_vlanid);
			result_info.addElement("multicast_vlanid").addText(multicast_vlanid);
        }
		return document.asXML();
	}



	
	public String getCallXml()
	{
		return callXml;
	}

	
	public void setCallXml(String callXml)
	{
		this.callXml = callXml;
	}

	
	public String getPpp_username()
	{
		return ppp_username;
	}

	
	public void setPpp_username(String ppp_username)
	{
		this.ppp_username = ppp_username;
	}

	
	public String getLogic_id()
	{
		return logic_id;
	}

	
	public void setLogic_id(String logic_id)
	{
		this.logic_id = logic_id;
	}



	public void setResulltCode(int result_code)
	{
		this.result = StringUtil.getStringValue(result_code);
	}
	public void setResultDesc(String result_desc)
	{
		this.errMsg=result_desc;
	}

	
	public List<HashMap<String, String>> getResMap()
	{
		return resMapList;
	}

	
	public void setResMap(List<HashMap<String, String>> resMap)
	{
		this.resMapList = resMap;
	}
   
}
