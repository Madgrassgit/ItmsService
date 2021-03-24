package com.linkage.itms.dispatch.cqdx.obj;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class FeedbackWorkTicketsInfo4CommDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(FeedbackWorkTicketsInfo4CommDealXML.class);
	SAXReader reader = new SAXReader();

	List<String> loids = new ArrayList<String>();
	List<String> serialNumbers = new ArrayList<String>();
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("work_id"));
			Element loidArray = inRoot.element("input_array");
			@SuppressWarnings("unchecked")
			List<Element> query_keys = loidArray.elements();
			String loid = "";
			String serialnumber="";
			for (Element e : query_keys) {
				loid = StringUtil.getStringValue(e.elementTextTrim("logic_id"));
				serialnumber = StringUtil.getStringValue(e.elementTextTrim("serial_number"));
				if(StringUtil.IsEmpty(loid) && StringUtil.IsEmpty(serialnumber)){
					return null;
				}
				loids.add(loid);
				serialNumbers.add(serialnumber);
			}
			return inDocument;
		} catch (Exception e) {
			logger.error("FeedbackWorkTicketsInfo4CommDealXML.getXML() is error!", e);
			return null;
		}
	}

	public List<String> getLoids() {
		return loids;
	}

	public void setLoids(List<String> loids) {
		this.loids = loids;
	}

	
	public List<String> getSerialNumbers()
	{
		return serialNumbers;
	}

	
	public void setSerialNumbers(List<String> serialNumbers)
	{
		this.serialNumbers = serialNumbers;
	}
}
