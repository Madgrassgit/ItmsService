/**
 * LINKAGE TECHNOLOGY (NANJING) CO.,LTD.<BR>
 * Copyright 2007-2010. All right reserved.
 */
package com.linkage.itms.obj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AddOBJ: TR069 AddObject.
 * @author Alex.Yan (yanhj@lianchuang.com)
 * @version 2.0, Jul 14, 2009
 * @see
 * @since 1.0
 */
public class AddOBJ {

	/** log */
	private static final Logger logger = LoggerFactory.getLogger(AddOBJ.class);

	/** InstanceNumber */
	private int instance = 0;

	/** Status */
	private int status = 0;

	/**
	 * get:
	 * 
	 * @return the instance
	 */
	public int getInstance() {
		logger.debug("getInstance()");

		return instance;
	}

	/**
	 * get:
	 * 
	 * @return the status
	 */
	public int getStatus() {
		logger.debug("getInstance()");

		return status;
	}

	/**
	 * set:
	 * 
	 * @param instance
	 *            the instance to set
	 */
	public void setInstance(int instance) {
		logger.debug("setInstance({})", instance);

		this.instance = instance;
	}

	/**
	 * set:
	 * 
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		logger.debug("setStatus({})", status);

		this.status = status;
	}

	/**
	 * object to string.
	 */
	public String toString() {
		logger.debug("toString()");

		return instance + "|" + instance;
	}

}
