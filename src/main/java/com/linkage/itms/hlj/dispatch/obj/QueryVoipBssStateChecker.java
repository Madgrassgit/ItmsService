package com.linkage.itms.hlj.dispatch.obj;


import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.DateUtil;

public class QueryVoipBssStateChecker extends HljBaseChecker {

	public static final Logger logger = LoggerFactory.getLogger(QueryVoipBssStateChecker.class);
		
	private String type = "";
	private String time = "";
	
	private String viopNumber = "";
	private String cityId = "";
	private String callXml = null;
	private String loid = "";
	private String voipBindSate = "";

	/**
	 * 构造函数
	 * @param inXml XML格式
	 */
	public QueryVoipBssStateChecker(String inXml) {
		callXml = inXml;
	}

	@Override
	public boolean check() {
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			Element publicElt = root.element("public");
			type = publicElt.elementTextTrim("type");
			time = publicElt.elementTextTrim("time");

			Element dataElt = root.element("data");
			cityId = dataElt.elementTextTrim("cityId");
			viopNumber = dataElt.elementTextTrim("viopNumber");
		} catch (Exception e) {
			e.printStackTrace();
			result = 0;
			resultDesc = "数据格式错误";
			return false;
		}
		//参数合法性检查
		if (false == baseCheck() || false == cityIdCheck()) {
			result = 0;
			return false;
		}
		result = 1;
		resultDesc = "成功";
		return true;
	}

	@Override
	public String getReturnXml() {
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		
		Element publicElt = root.addElement("public");
		publicElt.addElement("type").addText("RESPONSE");
		publicElt.addElement("time").addText(DateUtil.getNowTime("yyyy-MM-dd HH:mm:ss"));
		publicElt.addElement("success").addText(String.valueOf(result));
		publicElt.addElement("desc").addText(resultDesc);
		
		Element dataElt = root.addElement("data");
		dataElt.addElement("cityId").addText(cityId);
		dataElt.addElement("viopNumber").addText(viopNumber);
		dataElt.addElement("loid").addText(loid);
		dataElt.addElement("voipBindSate").addText(voipBindSate);
		return document.asXML();
	}



	/**
	 * 数据验证
	 * @return
	 */
	boolean baseCheck(){	
		if(StringUtil.IsEmpty(cityId) || StringUtil.IsEmpty(viopNumber)
				|| StringUtil.IsEmpty(type) || StringUtil.IsEmpty(time)){
			resultDesc = "数据不能为空";
			return false;
		}
		return true;
	}

	/**
	 * cityId 验证
	 * @return
	 */
	boolean cityIdCheck(){
		if(StringUtil.IsEmpty(cityId) || false == Global.G_CityId_CityName_Map.containsKey(cityId)){
			result = 1007;
			resultDesc = "属地非法";
			return false;
		}
		return true;
	}
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getViopNumber() {
		return viopNumber;
	}

	public void setViopNumber(String viopNumber) {
		this.viopNumber = viopNumber;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getLoid() {
		return loid;
	}

	public void setLoid(String loid) {
		this.loid = loid;
	}

	public String getVoipBindSate() {
		return voipBindSate;
	}

	public void setVoipBindSate(String voipBindSate) {
		this.voipBindSate = voipBindSate;
	}
}
