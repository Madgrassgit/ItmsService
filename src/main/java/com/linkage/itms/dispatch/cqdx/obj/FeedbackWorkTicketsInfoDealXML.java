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

public class FeedbackWorkTicketsInfoDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(FeedbackWorkTicketsInfoDealXML.class);
	SAXReader reader = new SAXReader();

	List<String> loids = new ArrayList<String>();
	public Document getXML(String inXml) {
		try {
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("work_id"));
			Element loidArray = inRoot.element("LOID_ARRAY");
			@SuppressWarnings("unchecked")
			List<Element> loidElement = loidArray.elements();
			for (Element e : loidElement) {
				loids.add(StringUtil.getStringValue(e.getTextTrim()));
				
			}
			return inDocument;
		} catch (Exception e) {
			logger.error("FactoryResetDealXML.getXML() is error!", e);
			return null;
		}
	}

	public List<String> getLoids() {
		return loids;
	}

	public void setLoids(List<String> loids) {
		this.loids = loids;
	}
}
