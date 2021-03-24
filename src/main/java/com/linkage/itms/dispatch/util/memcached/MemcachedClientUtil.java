/**
 * AsiaInfo-Linkage,Inc.<BR>
 * Copyright 2005-2011. All right reserved.
 */
package com.linkage.itms.dispatch.util.memcached;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.stbms.ids.obj.SysConstant;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * A new thread to start CORBA SERVICE.
 * 
 * @author Eric(qixq@)
 * @version 1.0
 * @since 1.0
 * @date 2011-5-31
 */
public class MemcachedClientUtil {
	/** log */
	private static final Logger log = LoggerFactory
			.getLogger(MemcachedClientUtil.class);

	/**
	 * 
	 */
	public void init() {
		log.debug("init()");

		MemcachedClient memcachedClient = null;

		/** MemcachedClientBuilder */
		MemcachedClientBuilder builder = null;

		// init memcached
		for (Memcached memcached : Global.memcachedPool.getMemcachedList()) {

			if (null == memcached
					|| true == StringUtil.IsEmpty(memcached.getName(), true)) {
				log.debug("memcached is null");

				continue;
			}

			builder = this.initMemcached(memcached);
			memcachedClient = this.initClient(builder);

			Global.memcachedMap.put(memcached.getName(), memcachedClient);
		}

		log.warn("memcached init ok.");
	}

	/**
	 * 设置memcached
	 */
	private MemcachedClientBuilder initMemcached(Memcached memcached) {
		log.debug("initMemcached({})", memcached);

		MemcachedClientBuilder builder = null;

		int[] weightArr = this.setWeight(memcached.getWeight());

		if (weightArr == null) {
			builder = new XMemcachedClientBuilder(
					AddrUtil.getAddresses(memcached.getAddr()));
		} else { // 设置节点权重
			builder = new XMemcachedClientBuilder(
					AddrUtil.getAddresses(memcached.getAddr()), weightArr);
		}

		// 默认分布的策略是按照key的哈希值模以连接数得到的余数,一致性哈希（consistent hash
		if (1 == memcached.getSessionLocator()) {
			builder.setSessionLocator(new KetamaMemcachedSessionLocator());
		}

		// Nio连接池
		builder.setConnectionPoolSize(memcached.getConnectionPoolSize());

		// 使用二进制协议/默认使用的TextCommandFactory，也就是文本协议
		if (1 == memcached.getCommandFactory()) {
			builder.setCommandFactory(new BinaryCommandFactory());
		}

		// 失败模式
		if (1 == memcached.getFailureMode()) {
			builder.setFailureMode(true);
		}

		return builder;
	}

	/**
	 * 
	 */
	private MemcachedClient initClient(MemcachedClientBuilder builder) {
		log.debug("initClient()");
		MemcachedClient memcachedClient = null;

		try {
			memcachedClient = builder.build();
			memcachedClient.setOpTimeout(10 * 1000L);
		} catch (IOException e) {
			log.error("Exception:{}", e.getMessage());
		}

		return memcachedClient;
	}

	/**
	 * 
	 * @param weight
	 * @return
	 */
	private int[] setWeight(String weight) {
		log.debug("setWeight({})", weight);

		int[] weightArr = null;

		if (true == StringUtil.IsEmpty(weight)) {
			log.debug("weight==null");

			return weightArr;
		}

		String[] tmpArr = weight.split("\\,");
		if (null == tmpArr || 0 == tmpArr.length) {
			log.debug("weight==null");

			return weightArr;
		}

		weightArr = new int[tmpArr.length];
		for (int i = 0; i < tmpArr.length; i++) {
			try {
				weightArr[i] = Integer.parseInt(tmpArr[i]);
			} catch (Exception e) {
				log.error("memcached.weight conf error");

				weightArr = null;

				break;
			}
		}

		return weightArr;
	}

	/**
	 * 
	 * @return
	 */
	public static Map<String, MemcachedClient> getMemcachedMap() {

		return Global.memcachedMap;
	}

	/**
	 * 根据名称获取MemcachedClient
	 * 
	 * @return
	 */
	public static MemcachedClient getMemcachedClient(String name) {
		log.debug("getMemcachedClient({})", name);

		MemcachedClient memcachedClient = null;

		if (true == StringUtil.IsEmpty(name, true)) {

			return memcachedClient;
		}

		if (null == Global.memcachedMap) {

			return memcachedClient;
		}

		memcachedClient = Global.memcachedMap.get(name);

		return memcachedClient;
	}

}
