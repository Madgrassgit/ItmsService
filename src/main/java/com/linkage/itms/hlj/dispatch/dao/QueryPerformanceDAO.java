package com.linkage.itms.hlj.dispatch.dao;

import java.util.ArrayList;
import java.util.HashMap;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

/**
 * 
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-2
 * @category com.linkage.itms.hlj.dispatch.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryPerformanceDAO
{
	public ArrayList<HashMap<String, String>> getMapByDev( String deviceId )
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select cpe_currentstatus, cpe_currentupdatetime  from tab_gw_device ");
		psql.append(" where 1=1 and device_id = '" + deviceId +"'");
		ArrayList<HashMap<String, String>> map = DBOperation.getRecords(psql.getSQL());
		return map;
	}
}
