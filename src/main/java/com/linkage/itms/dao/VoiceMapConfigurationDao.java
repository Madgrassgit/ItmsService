package com.linkage.itms.dao;

import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**  
 *
 * @author wangyan
 * @date 创建时间：2016-11-23 上午10:28:19
 * @return 
 */
public class VoiceMapConfigurationDao {
	
	/**
	 * 语音数图配置，根据code去获取map
	 *
	 * @author wangyan
	 * @date 2016-11-23
	 * @param digitComet
	 * @return
	 */
	public Map<String,String> queryDigitMapByCode(String digitComet)
	{
		PrepareSQL pSQL = new PrepareSQL();
		
		pSQL.setSQL("select digit_map_code,digit_map_value from  tab_digit_map  where 1=1 and digit_map_code='?'");
		pSQL.setLong(1, StringUtil.getLongValue(digitComet));
		return DBOperation.getRecord(pSQL.getSQL());
	}

}
