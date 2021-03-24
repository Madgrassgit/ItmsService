/**
 * LINKAGE TECHNOLOGY (NANJING) CO.,LTD.<BR>
 * Copyright 2007-2010. All right reserved.
 */
package com.linkage.itms.obj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ParameValueOBJ: TR069 SetValues;GetValues.
 * 
 * @author Alex.Yan (yanhj@lianchuang.com)
 * @version 2.0, Jul 14, 2009
 * @see
 * @since 1.0
 */
public class ParameValueOBJ {

	/** log */
	private static final Logger logger = LoggerFactory
			.getLogger(ParameValueOBJ.class);

	/** parame name */
	private String name = null;

	/** value of parame name */
	private String value = null;

	/** 参数类型：
	 * 1 string
	 * 2 int
	 * 3 unsignedInt
	 * 4 boolean
	 */
	
	private String type = null;

	/**
	 * constructor
	 */
	public ParameValueOBJ() {
		super();
	}

	/**
	 * constructor
	 * 
	 * @param name
	 * @param value
	 * @param type
	 */
	public ParameValueOBJ(String name, String value, String type) {
		super();
		this.name = name;
		this.value = value;
		this.type = type;
	}

	/**
	 * get:
	 * 
	 * @return the name
	 */
	public String getName() {
		logger.debug("getName()");

		return name;
	}

	/**
	 * get:
	 * 
	 * @return the value
	 */
	public String getValue() {
		logger.debug("getName()");

		return value;
	}

	/**
	 * get:
	 * 
	 * @return the type
	 */
	public String getType() {
		logger.debug("getName()");

		return type;
	}

	/**
	 * set:
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		logger.debug("setName({})", name);

		this.name = name;
	}

	/**
	 * set:
	 * 
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		logger.debug("setValue({})", value);

		this.value = value;
	}

	/**
	 * set:
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		logger.debug("setType({})", type);

		this.type = type;
	}

	/**
	 * object to string.
	 */
	public String toString() {
		logger.debug("toString()");

		return name + "|" + value + "|" + type;
	}

}
