
package com.linkage.stbms.pic.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.pic.Global;
import com.linkage.stbms.pic.db.Cursor;
import com.linkage.stbms.pic.db.DBOperation;
import com.linkage.stbms.pic.object.StrategyObj;

public class StrategyDao
{

	final static Logger logger = LoggerFactory.getLogger(StrategyDao.class);
	
	private static int priority = 1;

	/**
	 * get pp strategy from db.
	 * 
	 * @param deviceId
	 * @return
	 */
	public static ArrayList<StrategyObj> getStragegyList(String deviceId)
	{
		logger.debug("getStragegyList({})", deviceId);
		ArrayList<StrategyObj> list = null;
		boolean flag = true;
		if (flag)
		{
			String sql = "select task_id,id,type,service_id,time,status,order_id"
					+ " from "+Global.TABLENAME+" where"
					+ " status=0 and type>0 and device_id=? and exec_count<=?";
			PrepareSQL pSQL = new PrepareSQL(sql);
			pSQL.setStringExt(1, deviceId, true);
			pSQL.setInt(2, Global.G_MaxExecCountOfStrategy);
			ArrayList<HashMap<String, String>> tmpList = com.linkage.commons.db.DBOperation
					.getRecords(pSQL.getSQL());
			if (tmpList != null && tmpList.size() > 0)
			{
				logger.debug("tmpList.size()={}", tmpList.size());
				list = new ArrayList<StrategyObj>();
				Map<String, String> tmpMap = null;
				StrategyObj strategyobj = null;
				for (int i = 0; i < tmpList.size(); i++)
				{
					tmpMap = tmpList.get(i);
					strategyobj = new StrategyObj();
					strategyobj.setId(StringUtil.getLongValue(tmpMap, "id"));
					strategyobj.setTaskId(StringUtil.getStringValue(tmpMap, "task_id"));
					strategyobj
							.setServiceId(StringUtil.getIntValue(tmpMap, "service_id"));
					strategyobj.setStatus(StringUtil.getIntValue(tmpMap, "status"));
					strategyobj.setType(StringUtil.getIntValue(tmpMap, "type"));
					strategyobj.setTime(StringUtil.getLongValue(tmpMap, "time"));
					strategyobj.setOrderId(StringUtil.getIntValue(tmpMap, "order_id"));
					strategyobj.setDeviceId(deviceId);
					logger.debug("id = {}", strategyobj.getId());
					list.add(strategyobj);
				}
				tmpMap = null;
				tmpList = null;
			}
			else
			{
				logger.debug("tmpList = null");
			}
		}
		return list;
	}
	public static int getPriority()
	{
		return priority;
	}

	public static void setPriority(int priority)
	{
		StrategyDao.priority = priority;
	}
}
