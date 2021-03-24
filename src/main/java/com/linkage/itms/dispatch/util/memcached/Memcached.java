/**
 * AsiaInfo-Linkage,Inc.<BR>
 * Copyright 2005-2011. All right reserved.
 */
package com.linkage.itms.dispatch.util.memcached;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SysConf.memcached
 * 
 * @author alex(yanhj@)
 * @version 2.5
 */
public class Memcached {

	/** log */
	private static final Logger log = LoggerFactory.getLogger(Memcached.class);

	/** name */
	private String name = null;

	/** addr */
	private String addr = null;

	/** weight */
	private String weight = null;

	/** connectionPoolSize,default 1. */
	private int connectionPoolSize = -1;

	/** commandFactory: 0,Text;1,Bindary. */
	private int commandFactory = -1;

	/** SessionLocator: 0,Array (default);1,Ketama(consistent hash). */
	private int sessionLocator = -1;

	/** FailureMode:0,false(default);1,true. */
	private int failureMode = -1;

	/** operation timeout, unit:s. */
	private long opTimeout = 10;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		log.debug("setName({})", name);

		this.name = name;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		log.debug("setAddr({})", addr);

		this.addr = addr;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		log.debug("setWeight({})", weight);

		this.weight = weight;
	}

	public int getConnectionPoolSize() {
		return connectionPoolSize;
	}

	public void setConnectionPoolSize(int connectionPoolSize) {
		log.debug("setConnectionPoolSize({})", connectionPoolSize);

		this.connectionPoolSize = connectionPoolSize;
	}

	public int getCommandFactory() {
		return commandFactory;
	}

	public void setCommandFactory(int commandFactory) {
		log.debug("setCommandFactory({})", commandFactory);

		this.commandFactory = commandFactory;
	}

	public int getSessionLocator() {
		return sessionLocator;
	}

	public void setSessionLocator(int sessionLocator) {
		log.debug("setSessionLocator({})", sessionLocator);

		this.sessionLocator = sessionLocator;
	}

	public int getFailureMode() {
		return failureMode;
	}

	/**
	 * 
	 * @param failureMode
	 */
	public void setFailureMode(int failureMode) {
		log.debug("setFailureMode({})", failureMode);

		this.failureMode = failureMode;
	}

	/**
	 * 
	 * @return
	 */
	public long getOpTimeout() {
		return opTimeout;
	}

	/**
	 * 
	 * @param operTimeout
	 */
	public void setOpTimeout(long opTimeout) {
		log.debug("setOpTimeout({})", opTimeout);

		this.opTimeout = opTimeout;
	}

	/**
	 * toString
	 */
	public String toString() {

		return this.name + "|" + this.addr;
	}

}
