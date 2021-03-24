package com.linkage.stbms.ids.dao;

import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.stbms.ids.util.CommonUtil;

/**
 * 
 * @author Reno (Ailk No.)
 * @version 1.0
 * @since 2015年12月15日
 * @category com.linkage.stbms.ids.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class GetStbDeviceInfoDAO
{
	/**
	 * 根据机顶盒序列号查询
	 * @param searchInfo
	 * @return
	 */
	public Map<String,String> getStbDeviceInfoBySN(String searchInfo){
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.device_id, a.cpe_passwd, a.acs_passwd, a.loopback_ip, c.city_name, a.cpe_mac, ");
		psql.append(" b.addressing_type, DECODE(b.user_status,'1','成功','0','未做','-1','失败') as status, ");
		psql.append(" a.device_serialnumber, b.pppoe_user, b.serv_account ");
		psql.append(" from "+ CommonUtil.addPrefix("tab_gw_device") + " a left join tab_city c on a.city_id = c.city_id, ");
		psql.append(" " + CommonUtil.addPrefix("tab_customer") + " b");
		psql.append(" where a.customer_id = b.customer_id ");
		psql.append("   and a.dev_sub_sn = '"+searchInfo.substring(searchInfo.length() - 6)+"' ");
		psql.append("   and a.device_serialnumber like '%"+searchInfo+"' ");
		psql.append(" order by a.cpe_currentupdatetime desc");
		Map<String,String> map = DBOperation.getRecord(psql.getSQL());
		if (map == null || map.isEmpty()) {
			return null;
		} else {
			return map;
		}
	}
	
	/**
	 * 根据业务账号或者机顶盒mac
	 * @param searchInfo
	 * @return
	 */
	public Map<String,String> getStbDeviceInfo(String serchType, String searchInfo){
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select a.serv_account, a.cpe_mac, a.pppoe_user, a.addressing_type, ");
		psql.append(" decode(a.user_status, '1', '成功', '0', '未做', '-1', '失败') as status, c.city_name, b.cpe_passwd, ");
		psql.append(" b.acs_passwd, b.loopback_ip, b.device_serialnumber, b.device_id ");
		psql.append(" from "+ CommonUtil.addPrefix("tab_customer") + " a left join " 
					+ CommonUtil.addPrefix("tab_gw_device") + " b on a.customer_id = b.customer_id ");
		psql.append(" left join tab_city c on a.city_id = c.city_id where 1 = 1 ");
		// 业务账号
		if ("1".equals(serchType)) {
			psql.append(" and a.serv_account = '" + searchInfo + "'");
		}
		// 机顶盒mac
		if ("2".equals(serchType)) {
			psql.append(" and a.cpe_mac = '" + searchInfo + "'");
		}
		
		Map<String,String> map = DBOperation.getRecord(psql.getSQL());
		if (map == null || map.isEmpty()) {
			return null;
		} else {
			return map;
		}
	}
}
