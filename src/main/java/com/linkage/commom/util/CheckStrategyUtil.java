package com.linkage.commom.util;

import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * 
 * @author chenzhangjian (Ailk No.)
 * @version 1.0
 * @since 2015-9-29
 * @category com.linkage.litms.common.util
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class CheckStrategyUtil
{
	public static boolean chechStrategy(String deviceId){
		boolean result = false;
		PrepareSQL psql = new PrepareSQL("select count(1) rcount from " + Global.STRATEGY_TABNAME + " where status not in (0,100) and device_id = ?" );
		psql.setString(1, deviceId);
		Map<String,String> resultMap= DBOperation.getRecord(psql.toString());
		if(null != resultMap){
			int count = StringUtil.getIntegerValue(resultMap.get("rcount"));
			if(count < 1){
				result = true;
			}
		}
		return result;
	}
}
