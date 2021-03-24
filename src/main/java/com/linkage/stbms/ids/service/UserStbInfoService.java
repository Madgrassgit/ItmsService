package com.linkage.stbms.ids.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.stbms.ids.bio.BoxCheck;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-3-22
 * @category com.linkage.stbms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class UserStbInfoService
{
	private static Logger logger = LoggerFactory.getLogger(UserStbInfoService.class);
	public String work(String inParam) {
			return new BoxCheck().getBoxcheck(inParam);
	}
}
