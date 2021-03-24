package com.linkage.stbms.ids.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.itv.main.Global;
import com.linkage.system.utils.database.DBUtil;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-3-22
 * @category com.linkage.stbms.ids.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class StbDevSNServiceDAO
{
	private static Logger logger = LoggerFactory.getLogger(StbDevSNServiceDAO.class);
	
	public List<HashMap<String, String>> StbDevSNService(String servAccount)
	{
		if (StringUtil.IsEmpty(servAccount)) {
			logger.warn("servAccount is empty!");
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select ");
		if(1==DBUtil.getDbType()){
			psql.append("top 3 ");
		}
		psql.append("city_id,oui,device_serialnumber,device_id,devicetype_id,cpe_mac ");
		psql.append("from stb_tab_gw_device ");
		psql.append("where serv_account=? ");
		if("xj_dx".equals(Global.G_instArea)){
			psql.append("and cpe_allocatedstatus=1 and customer_id is not null ");
		}
		psql.setString(1,servAccount);
	
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 获取设备厂商、软件版本、硬件版本、型号
	 */
	public Map<String, String> queryInfo(String devicetype_id)
	{
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("select a.hardwareversion,a.softwareversion,c.device_model,b.vendor_name ");
		pSql.append("from stb_tab_devicetype_info a,stb_tab_vendor b,stb_gw_device_model c ");
		pSql.append("where a.vendor_id=b.vendor_id and a.device_model_id=c.device_model_id ");
		pSql.append("and a.devicetype_id=? ");
		pSql.setInt(1,StringUtil.getIntegerValue(devicetype_id));
		
		return DBOperation.getRecord(pSql.getSQL());
	}
}
