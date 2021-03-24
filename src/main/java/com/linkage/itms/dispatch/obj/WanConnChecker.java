package com.linkage.itms.dispatch.obj;

import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * @author Jason(3412)
 * @date 2010-4-1
 */
public class WanConnChecker extends BaseQueryChecker {

	private static Logger logger = LoggerFactory.getLogger(WanConnChecker.class);
	private Map<String,String> wanConnDeviceMap = null;
	public static String SERV_LIST_INTERNET = "INTERNET";
	public static String SERV_LIST_TR069 = "TR069";
	public static String SERV_LIST_VOIP = "VOIP";
	public static String SERV_LIST_OTHER = "OTHER";
	
	/**
	 * 构造方法
	 * 
	 * @param _callXml
	 *            客户端查询XML字符串
	 */
	public WanConnChecker(String _callXml) {
		logger.debug("CallChecker({})", _callXml);
		callXml = _callXml;
	}


	/**
	 * 返回调用结果
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-4-1
	 * @return String
	 */
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		//返回结果

		Document document = DocumentHelper.createDocument();
		if ("nx_dx".equals(Global.G_instArea)) {
			document.setXMLEncoding(Global.codeTypeValue);
		} else {
			document.setXMLEncoding("GBK");
		}
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText("" + resultDesc);
		//山东电信增加oui devsn
		root.addElement("OUI").addText("" + oui);
		root.addElement("DevSn").addText("" + devSn);
		if(Global.G_instArea.equals("hb_lt")){
			root.addElement("pppoe_name").addText("" + getUsername());
		}
       if(wanConnDeviceMap !=null && !wanConnDeviceMap.isEmpty())
       {
    	   for(String servList : wanConnDeviceMap.keySet())
    	   {
    		   String wanArr[] = servList.split("###");
    		   if(SERV_LIST_INTERNET.equals(wanArr[0]))
				{
				   Element element = root.addElement("internetIntfs");
    			   element.addText(StringUtil.getStringValue(wanConnDeviceMap,servList));
    			   element.addAttribute("vlanid", wanArr[1]);
    			   if(Global.G_instArea.equals("ah_dx")||Global.G_instArea.equals("hb_lt")){
        			   element.addAttribute("conn_type", wanArr[2]); 
    			   }
				}
				else if (SERV_LIST_VOIP.equals(wanArr[0]))
				{
					Element element = root.addElement("voipIntfs");
					element.addText("" + wanConnDeviceMap.get(servList));
					element.addAttribute("vlanid", wanArr[1]);
				}
				else if (SERV_LIST_TR069.equals(wanArr[0]))
				{
					Element element = root.addElement("tr069Intfs");
					element.addText("" + wanConnDeviceMap.get(servList));
					element.addAttribute("vlanid", wanArr[1]);
				}
    	   }
    	  
       }	
		return document.asXML();
	}


	public Map<String, String> getWanConnDeviceMap() {
		return wanConnDeviceMap;
	}


	public void setWanConnDeviceMap(Map<String, String> wanConnDeviceMap) {
		this.wanConnDeviceMap = wanConnDeviceMap;
	}


	
}
