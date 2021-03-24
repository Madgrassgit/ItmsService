/**
 * AsiaInfo-Linkage,Inc.<BR>
 * Copyright 2005-2011. All right reserved.
 */
package com.linkage.itms.dispatch.util.memcached;

import com.linkage.commons.xml.Bean2XML;
import com.linkage.commons.xml.XML2Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * conf obj:mqPool
 * 
 * @author alex(yanhj@)
 * @version 2.5
 * @since 2.5
 */
public class MemcachedPool {

	/** log */
	private static final Logger log = LoggerFactory
			.getLogger(MemcachedPool.class);

	/** mqList */
	private List<Memcached> memcachedList = null;

	/**
	 * cons
	 */
	public MemcachedPool() {
		memcachedList = new ArrayList<Memcached>();
	}

	/**
	 * 
	 * @return
	 */
	public List<Memcached> getMemcachedList() {
		return memcachedList;
	}

	/**
	 * 
	 * @param memcachedList
	 */
	public void setMemcachedListList(List<Memcached> memcachedList) {
		log.debug("setMemcachedListList({})", memcachedList);

		this.memcachedList = memcachedList;
	}

	/**
	 * 
	 * @param mq
	 */
	public void addMemcached(Memcached memcached) {
		log.debug("addMemcached({})", memcached);

		this.memcachedList.add(memcached);
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		log.debug("toString()");

		if (memcachedList == null) {
			return "";
		}

		return "size=" + memcachedList.size();
	}

	public static void main(String[] args) {

		Memcached memcached = new Memcached();
		memcached.setName("test");
		memcached.setAddr("192.1");
		MemcachedPool obj = new MemcachedPool();
		obj.getMemcachedList().add(memcached);
		
		Bean2XML bean2XML = new Bean2XML();
		String xml = bean2XML.getXML(obj);
		System.out.println(xml);

		XML2Bean bean = new XML2Bean(xml);
		MemcachedPool per = (MemcachedPool) bean.getBean("MemcachedPool",
				MemcachedPool.class);
		System.out.println(per);
	}

}
