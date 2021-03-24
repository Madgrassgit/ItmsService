/**
 * AsiaInfo-Linkage,Inc.<BR>
 * Copyright 2005-2011. All right reserved.
 */
package com.linkage.itms.dispatch.util.memcached;

import com.linkage.commons.redis.XMemDataChildInterface;
import net.rubyeye.xmemcached.GetsResponse;
import net.rubyeye.xmemcached.KeyIterator;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * A new thread to start CORBA SERVICE.
 * 
 * @author Eric(qixq@)
 * @version 1.0
 * @since 1.0
 * @date 2011-5-31
 */
public class MemcachedDataUtil implements XMemDataChildInterface {
	
	// log
	private static Logger log = LoggerFactory.getLogger(MemcachedDataUtil.class);
	
	// memcached客户端
	private MemcachedClient memcachedClient = null;
	
	// memcached名称
	private String memcachedName;
	
	
	@Override
	public String getMemcachedName() {
		return memcachedName;
	}

	@Override
	public void setMemcachedName(String memcachedName) {
		
		this.memcachedName = memcachedName;
		memcachedClient = MemcachedClientUtil.getMemcachedClient(memcachedName);
		
	}
	
	/**
	 * 增加，如果已经有KEY会失败
	 * 
	 * @param key
	 * @param exp
	 * @param value
	 * @return
	 */
	public boolean add(final String key, final int exp, final Object value) {
		log.debug("add({},{},{})", new Object[] { key, exp, value });

		boolean flag = true;

		try {
			flag = memcachedClient.add(key, exp, value);

			log.info("mem.add({},{},value)", key, exp);
		} catch (TimeoutException e) {
			log.error("mem.add({},{},{}) TimeoutException:{}", new Object[] {
					key, exp, value, e.getMessage() });

			flag = false;
		} catch (InterruptedException e) {
			log.error("mem.add({},{},{}) InterruptedException:", key, exp, value, e);

			flag = false;
			// Restore interrupted state...      
			Thread.currentThread().interrupt();
		} catch (MemcachedException e) {
			log.error("mem.add({},{},{}) MemcachedException:{}", new Object[] {
					key, exp, value, e.getMessage() });
			flag = false;
		}catch(IllegalArgumentException e)
		{
			log.error("mem.add({},{},{}) IllegalArgumentException:{}", new Object[] {
					key, exp, value, e.getMessage() });
			flag = false;
		}catch(Exception e)
		{
			log.error("mem.add({},{},{}) Exception:{}", new Object[] {
					key, exp, value, e.getMessage() });
			flag = false;
		}

		return flag;
	}

	/**
	 * 更新，如果已经有KEY也成功
	 * 
	 * @param key
	 * @param exp
	 * @param value
	 * @return
	 */
	public boolean set(final String key, final int exp, final Object value) {
		log.debug("set({},{},{})", new Object[] { key, exp, value });

		boolean flag = true;

		try {
			flag = memcachedClient.set(key, exp, value);

			log.info("mem.set({},{},{},value)", new Object[] {key, exp,flag});
		} catch (TimeoutException e) {
			log.error("mem.set({},{},{}) TimeoutException:{}", new Object[] {
					key, exp, value, e.getMessage() });

			flag = false;
		} catch (InterruptedException e) {
			log.error("mem.set({},{},{}) InterruptedException:", key, exp, value, e);

			flag = false;
			// Restore interrupted state...      
			Thread.currentThread().interrupt();
		} catch (MemcachedException e) {
			log.error("mem.set({},{},{}) MemcachedException:{}", new Object[] {
					key, exp, value, e.getMessage() });

			flag = false;
		}catch(IllegalArgumentException e)
		{
			log.error("mem.set({},{},{}) IllegalArgumentException:{}", new Object[] {
					key, exp, value, e.getMessage() });
			flag = false;
		}catch(Exception e)
		{
			log.error("mem.set({},{},{}) Exception:{}", new Object[] {
					key, exp, value, e.getMessage() });
			flag = false;
		}

		return flag;
	}

	/**
	 * 更新，如果已经没有KEY会失败
	 * 
	 * @param key
	 * @param exp
	 * @param value
	 * @return
	 */
	public boolean replace(final String key, final int exp, final Object value) {
		log.debug("replace({},{},{})", new Object[] { key, exp, value });

		boolean flag = true;

		try {
			flag = memcachedClient.replace(key, exp, value);

			log.info("mem.replace({},{},value)", key, exp);
		} catch (TimeoutException e) {
			log.error("mem.replace({},{},{}) TimeoutException:{}",
					new Object[] { key, exp, value, e.getMessage() });
			flag = false;
		} catch (InterruptedException e) {
			log.error("mem.replace({},{},{}) InterruptedException:", key, exp, value, e);
			flag = false;
			// Restore interrupted state...      
			Thread.currentThread().interrupt();
		} catch (MemcachedException e) {
			log.error("mem.replace({},{},{}) MemcachedException:{}",
					new Object[] { key, exp, value, e.getMessage() });
			flag = false;
		}catch(IllegalArgumentException e)
		{
			log.error("mem.replace({},{},{}) IllegalArgumentException:{}", new Object[] {
					key, exp, value, e.getMessage() });
			flag = false;
		}catch(Exception e)
		{
			log.error("mem.replace({},{},{}) Exception:{}", new Object[] {
					key, exp, value, e.getMessage() });
			flag = false;
		}

		return flag;
	}

	/**
	 * 更新：前追加，如果没有KEY会失败
	 * 
	 * @param key
	 * @param obj
	 * @return
	 */
	public boolean prepend(final String key, Object obj) {
		log.debug("prepend({},{})", new Object[] { key, obj });

		boolean flag = true;

		try {
			flag = memcachedClient.prepend(key, obj);

			log.info("mem.prepend({},value)", key);
		} catch (TimeoutException e) {
			log.error("mem.prepend({},{}) TimeoutException:{}", new Object[] {
					key, obj, e.getMessage() });
			flag = false;
		} catch (InterruptedException e) {
			log.error("mem.prepend({},{}) InterruptedException:", key, obj, e);
			flag = false;
			// Restore interrupted state...      
			Thread.currentThread().interrupt();
		} catch (MemcachedException e) {
			log.error("mem.prepend({},{}) MemcachedException:{}", new Object[] {
					key, obj, e.getMessage() });
			flag = false;
		}

		return flag;
	}

	/**
	 * 更新：后追加，如果没有KEY会失败
	 * 
	 * @param key
	 * @param obj
	 * @return
	 */
	public boolean append(final String key, Object obj) {
		log.debug("append({},{})", new Object[] { key, obj });

		boolean flag = true;

		try {
			flag = memcachedClient.append(key, obj);

			log.info("mem.append({},value)", key);
		} catch (TimeoutException e) {
			log.error("mem.append({},{}) TimeoutException:{}", new Object[] {
					key, obj, e.getMessage() });
			flag = false;
		} catch (InterruptedException e) {
			log.error("mem.append({},{}) InterruptedException:", key, obj, e);
			flag = false;
			// Restore interrupted state...      
			Thread.currentThread().interrupt();
		} catch (MemcachedException e) {
			log.error("mem.append({},{}) MemcachedException:{}", new Object[] {
					key, obj, e.getMessage() });
			flag = false;
		}

		return flag;
	}

	/**
	 * 删除，如果没有KEY会失败
	 * 
	 * @param key
	 * @return
	 */
	public boolean del(final String key) {
		log.debug("del({})", new Object[] { key });

		boolean flag = true;

		try {
			flag = memcachedClient.delete(key);

			log.info("mem.del({})", key);
		} catch (TimeoutException e) {
			log.error("mem.del({}) TimeoutException:{}",
					new Object[] { key, e.getMessage() });
			flag = false;
		} catch (InterruptedException e) {
			log.error("mem.del({}) InterruptedException:", key, e);
			flag = false;
			// Restore interrupted state...      
			Thread.currentThread().interrupt();
		} catch (MemcachedException e) {
			log.error("mem.del({}) MemcachedException:{}", new Object[] { key,
					e.getMessage() });
			flag = false;
		}

		return flag;
	}

	/**
	 * 删除:无等待，如果没有KEY会失败
	 * 
	 * @param key
	 * @return
	 */
	public boolean deleteWithNoReply(final String key) {
		log.debug("deleteWithNoReply({})", new Object[] { key });

		boolean flag = true;
		try {
			memcachedClient.deleteWithNoReply(key);

			log.info("mem.deleteWithNoReply({})", key);
		} catch (InterruptedException e) {
			log.error("mem.deleteWithNoReply({}) InterruptedException:", key, e);
			flag = false;
			// Restore interrupted state...      
			Thread.currentThread().interrupt();
		} catch (MemcachedException e) {
			log.error("mem.deleteWithNoReply({}) MemcachedException:{}",
					new Object[] { key, e.getMessage() });
			flag = false;
		}
		return flag;
	}

	/**
	 * 取数据,如果没有KEY会失败
	 * 
	 * @param key
	 * @return
	 */
	public Object get(final String key) {
		log.debug("get({})", new Object[] { key });

		Object obj = null;
		try {
			obj = memcachedClient.get(key);

			log.info("mem.get({})", key);
		} catch (TimeoutException e) {
			log.error("mem.get({}) TimeoutException:{}",
					new Object[] { key, e.getMessage() });
		} catch (InterruptedException e) {
			log.error("mem.get({}) InterruptedException:", key, e);
			// Restore interrupted state...      
			Thread.currentThread().interrupt();
		} catch (MemcachedException e) {
			log.error("mem.get({}) MemcachedException:{}", new Object[] { key,
					e.getMessage() });
		}

		log.debug("get({})={}", key, obj);

		return obj;
	}

	/**
	 * 包含
	 * 
	 * @param key
	 * @return
	 */
	public boolean containsKey(final String key) {
		log.debug("containsKey({})", new Object[] { key });

		boolean flag = false;

		Object obj = null;
		try {
			obj = memcachedClient.get(key);

			log.info("mem.containsKey({})", key);
		} catch (TimeoutException e) {
			log.error("mem.containsKey({}) TimeoutException:{}", new Object[] {
					key, e.getMessage() });
		} catch (InterruptedException e) {
			log.error("mem.containsKey({}) InterruptedException:", key, e);
			// Restore interrupted state...      
			Thread.currentThread().interrupt();
		} catch (MemcachedException e) {
			log.error("mem.containsKey({}) MemcachedException:{}",
					new Object[] { key, e.getMessage() });
		}

		if (null != obj) {
			log.debug("null != obj");

			flag = true;
		}

		return flag;
	}

	/**
	 * 取指定memcached中所有人key列表
	 * 
	 * @param addr
	 *            memcached 地址
	 * @return KeyList
	 */
	public Collection<String> getAllKeyListByAddr(InetSocketAddress addr) {
		log.debug("getAllKeyListByAddr({})", new Object[] { addr });

		Collection<String> list = null;

		KeyIterator keyIterator = null;
		try {
			keyIterator = memcachedClient.getKeyIterator(addr);

			log.info("mem.getAllKeyListByAddr.getKeyIterator({})", addr);
		} catch (TimeoutException e) {
			log.error(
					"mem.getAllKeyListByAddr.getKeyIterator({}) TimeoutException:{}",
					new Object[] { addr, e.getMessage() });
		} catch (InterruptedException e) {
			log.error(
					"mem.getAllKeyListByAddr.getKeyIterator({}) InterruptedException:", addr, e);
			// Restore interrupted state...      
			Thread.currentThread().interrupt();
		} catch (MemcachedException e) {
			log.error(
					"mem.getAllKeyListByAddr.getKeyIterator({}) MemcachedException:{}",
					new Object[] { addr, e.getMessage() });
		}

		if (null == keyIterator) {
			log.debug("null == keyIterator");

			return list;
		}

		list = new ArrayList<String>();
		while (true == keyIterator.hasNext()) {
			try {
				list.add(keyIterator.next());

			} catch (Exception e) {
				log.error("mem.getAllKeyListByAddr({}) Exception:{}",
						new Object[] { addr, e.getMessage() });
			}
		}

		return list;
	}

	/**
	 * get value list by key list.
	 * 
	 * @param <T>
	 * @param list
	 * @param timeout
	 * @return
	 */
	public <T> Map<String, GetsResponse<T>> gets(Collection<String> list,
			long timeout) {
		log.debug("gets({})", new Object[] { list });

		Map<String, GetsResponse<T>> map = null;

		if (null == list || 0 == list.size()) {
			log.debug("null == list");

			return map;
		}

		try {
			map = memcachedClient.gets(list, timeout);
		} catch (TimeoutException e) {
			log.error("mem.gets() TimeoutException:{}",
					new Object[] { e.getMessage() });
		} catch (InterruptedException e) {
			log.error("mem.gets() InterruptedException:", e);
			// Restore interrupted state...      
			Thread.currentThread().interrupt();
		} catch (MemcachedException e) {
			log.error("mem.gets() MemcachedException:{}",
					new Object[] { e.getMessage() });
		}

		return map;
	}
	
	
	/**
	 * +1，如果已经有KEY也成功
	 *
	 * @param key
	 * @param exp
	 * @return
	 */
	public boolean incr(final String key, final int exp) {
		log.debug("incr({},{},{})", new Object[] { key, exp });

		boolean flag = true;

		try {
			long result = memcachedClient.incr(key, exp);
			if(result<=0){
				flag = false;
			}

			log.info("mem.incr({},{},value)", new Object[] {key, exp,flag});
		} catch (TimeoutException e) {
			log.error("mem.incr({},{}) TimeoutException:{}", new Object[] {
					key, exp, e.getMessage() });

			flag = false;
		} catch (InterruptedException e) {
			log.error("mem.incr({},{}) InterruptedException:", key, exp, e);

			flag = false;
			// Restore interrupted state...      
			Thread.currentThread().interrupt();
		} catch (MemcachedException e) {
			log.error("mem.incr({},{}) MemcachedException:{}", new Object[] {
					key, exp, e.getMessage() });

			flag = false;
		}catch(IllegalArgumentException e)
		{
			log.error("mem.incr({},{}) IllegalArgumentException:{}", new Object[] {
					key, exp, e.getMessage() });
			flag = false;
		}catch(Exception e)
		{
			log.error("mem.incr({},{}) Exception:{}", new Object[] {
					key, exp, e.getMessage() });
			flag = false;
		}

		return flag;
	}
}
