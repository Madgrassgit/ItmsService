package com.linkage.stbms.pic.object;

import com.linkage.commons.xml.XML2Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ParameterListObj {

	private static final Logger log = LoggerFactory.getLogger(ParameterListObj.class);

	/** dev list */
	List<ParameterObj> parameterList = null;

	public ParameterListObj() {
		parameterList = new ArrayList<ParameterObj>();
	} 
	
	/**
	 * 
	 * @return the devList
	 */
	public List<ParameterObj> getParameterList() {
		log.debug("getParameterList()");

		return parameterList;
	}

	/**
	 * @param devList
	 *            the devList to set
	 */
	public void setParameterList(List<ParameterObj> parameterList) {
		log.debug("setParameterList({})", parameterList);

		this.parameterList = parameterList;
	}

	/**
	 * 
	 * @param informDevMQ
	 */
	public void addParameter(ParameterObj parameterObj) {
		log.debug("addParameter({})", parameterObj);

		this.parameterList.add(parameterObj);
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		log.debug("toString()");

		if (parameterList == null) {
			return "";
		}

		return "size=" + parameterList.size();
	}
	
	
	public static void main(String[] args)
	{
//		ParameterListObj aa = new ParameterListObj();
//		ParameterObj bb = new ParameterObj();
//		bb.setName("bb");
//		bb.setValue("bb");
//		bb.setType("1");
//		aa.addParameter(bb);
//		ParameterObj cc = new ParameterObj();
//		cc.setName("cc");
//		cc.setValue("cc");
//		cc.setType("2");
//		aa.addParameter(cc);
//		Bean2XML b2x = new Bean2XML();
//		String xml = b2x.getXML(aa);
//		System.out.println(xml);
		String xml = "<parameterConfig><parameterList><parameter><name>bb</name><type>1</type><value>bb</value></parameter><parameter><name>cc</name><type>2</type><value>cc</value></parameter></parameterList></parameterConfig>";
		
		XML2Bean x2b = new XML2Bean(xml);
		ParameterListObj fff = (ParameterListObj) x2b.getBean("parameterConfig",
				ParameterListObj.class);
		System.out.println(fff.getParameterList().get(0).getName());
		System.out.println(fff.getParameterList().get(1).getName());
	}
}
