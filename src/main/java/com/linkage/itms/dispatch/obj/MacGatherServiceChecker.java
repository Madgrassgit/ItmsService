package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * 湖北下挂MAC地址采集
 * @author jiafh
 *
 */
public class MacGatherServiceChecker extends BaseChecker{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(MacGatherServiceChecker.class);
	
	private String devSn;
	
	private List<Map<String,String>> gatherMacMapList = null;

	private List<Map<String,String>> gatherMaxBitRateMapList = null;

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public MacGatherServiceChecker(String inXml) {
		callXml = inXml;
	}
	
	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root
					.elementTextTrim("ClientType"));

			Element param = root.element("Param");
			userInfoType = StringUtil.getIntegerValue(param
					.elementTextTrim("UserInfoType"));
			userInfo = param.elementTextTrim("UserInfo");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		
		//参数合法性检查
		if (false == baseCheck() || false == userInfoTypeCheck()
				|| false == userInfoCheck()) {
			return false;
		}
		
		result = 0;
		resultDesc = "成功";
		
		return true;
	}
	/**
	 * 返回绑定调用结果字符串
	 */
	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(StringUtil.getStringValue(result));
		// 结果描述
		root.addElement("RstMsg").addText(StringUtil.getStringValue(resultDesc));
		// 设备序列号
		root.addElement("DevSN").addText(StringUtil.getStringValue(devSn));
		
		// 拼接xml
		if(result == 0)
		{
			Element lanPorts = root.addElement("LanPorts");
			
			if(null != gatherMacMapList && !gatherMacMapList.isEmpty()){
				for(Map<String,String> gatherMap : gatherMacMapList){
					Element lanPort = lanPorts.addElement("LanPort");					
					Element lanPortNum = lanPort.addElement("LanPortNum");
					lanPortNum.addText(StringUtil.getStringValue(gatherMap.get("port")));
					Element macAddress = lanPort.addElement("MacAddress");
					macAddress.addText(StringUtil.getStringValue(gatherMap.get("macAddress")));
					Element hostName = lanPort.addElement("HostName");
					hostName.addText(StringUtil.getStringValue(gatherMap.get("hostName")));
				}
			}
			Element lanBitRates = root.addElement("LanBitRates");
			if(null != gatherMaxBitRateMapList && !gatherMaxBitRateMapList.isEmpty())
			{
				for (Map result : gatherMaxBitRateMapList)
				{
					Element lanPort = lanBitRates.addElement("LanPort");
					Element lanPortNum = lanPort.addElement("LanPortNum");
					lanPortNum.addText(StringUtil.getStringValue(result.get("port")));
					Element maxBitRate = lanPort.addElement("MaxBitRate");
					maxBitRate.addText(StringUtil.getStringValue(result.get("maxBitRate")));
				}
			}
		}
		
		return document.asXML();
	}
	
	/**
	 * 校验用户信息类型
	 */
	boolean userInfoTypeCheck(){
		if(1 != userInfoType && 2 != userInfoType
				&& 3 != userInfoType && 4 != userInfoType
				&& 5 != userInfoType && 6 != userInfoType ){
			result = 1001;
			resultDesc = "用户信息类型非法";
			return false;
		}
		return true;
	}

	public String getDevSn() {
		return devSn;
	}

	public void setDevSn(String devSn) {
		this.devSn = devSn;
	}

	public List<Map<String, String>> getGatherMacMapList() {
		return gatherMacMapList;
	}

	public void setGatherMacMapList(List<Map<String, String>> gatherMacMapList) {
		this.gatherMacMapList = gatherMacMapList;
	}

	public List<Map<String, String>> getGatherMaxBitRateMapList() {
		return gatherMaxBitRateMapList;
	}

	public void setGatherMaxBitRateMapList(List<Map<String, String>> gatherMaxBitRateMapList) {
		this.gatherMaxBitRateMapList = gatherMaxBitRateMapList;
	}
}