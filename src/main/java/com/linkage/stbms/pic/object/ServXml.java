package com.linkage.stbms.pic.object;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author 王森博(66168) Tel:
 * @version 1.0
 * @since Sep 10, 2013 10:17:48 AM
 * @category com.linkage.litms.preprocess.object
 * @copyright 南京联创科技 网管科技部
 *
 */
public class ServXml
{
	private List<Serv> servList = null;
	
	public ServXml(){
		servList = new ArrayList<Serv>();
	}
	
	public List<Serv> getServList() {

		return servList;
	}

	public void setServList(List<Serv> servList) {

		this.servList = servList;
	}
	
	public void addServ(Serv serv)
	{
		this.servList.add(serv);
	}
	
//	public static void main(String[] args)
//	{
//		ServXml a = new ServXml();
//		List<Serv> b = new ArrayList<Serv>();
//		b.add(new Serv());
//		b.add(new Serv());
//		a.setServList(b);
//		Bean2XML c = new Bean2XML();
//		System.out.println(c.getXML(a));
//		String servXml = "<ServXml><servList><serv><deviceId>adsf</deviceId><deviceSn/><oui/><serviceId/><userId/></serv></servList></ServXml>";
//		XML2Bean xb = new XML2Bean(servXml);
//		ServXml sx = (ServXml) xb.getBean("ServXml", ServXml.class);
//		System.out.println(sx);
//	}

}
