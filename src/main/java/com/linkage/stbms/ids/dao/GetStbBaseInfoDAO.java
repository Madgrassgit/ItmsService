package com.linkage.stbms.ids.dao;

import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.stbms.ids.util.CommonUtil;

/**
 * 
 * @author yinlei3 (73167)
 * @version 1.0
 * @since 2015年12月15日
 * @category com.linkage.stbms.ids.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class GetStbBaseInfoDAO
{
	/**
	 * 
	 * @param searchType
	 * @param searchInfo
	 * @return
	 */
	public Map<String,String> getDevBaseInfo(String searchType, String searchInfo){
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select c.city_name, d.vendor_name,e.hardwareversion,e.softwareversion,f.device_model ");
		psql.append(" from "+CommonUtil.addPrefix("tab_gw_device")+" a, "+CommonUtil.addPrefix("tab_customer")+" b, tab_city c,");
		psql.append(CommonUtil.addPrefix("tab_vendor")+" d," + CommonUtil.addPrefix("tab_devicetype_info")+" e," +CommonUtil.addPrefix("gw_device_model")+" f ");
		psql.append(" where a.customer_id = b.customer_id and a.city_id = c.city_id and a.vendor_id = d.vendor_id and a.devicetype_id = e.devicetype_id and a.device_model_id = f.device_model_id");
		
		// searchType=1时searchInfo=业务帐号
		if ("1".equals(searchType)) {
			psql.append("   and a.serv_account = '"+searchInfo+"' ");
		}
		// searchType=2时searchInfo=机顶盒MAC
		else if ("2".equals(searchType)) {
			psql.append("   and a.cpe_mac = '"+searchInfo+"' ");
		}
		// searchType=3时searchInfo=机顶盒序列号
		else if ("3".equals(searchType)) {
			psql.append("   and a.dev_sub_sn = '"+searchInfo.substring(searchInfo.length() - 6)+"' ");
			psql.append("   and a.device_serialnumber like '%"+searchInfo+"' ");
		}
		psql.append(" order by a.cpe_currentupdatetime desc");

		Map<String,String> map = DBOperation.getRecord(psql.getSQL());
		
		if (map == null || map.isEmpty()) {
			return null;
		} else {
			return map;
		}
	}
}
