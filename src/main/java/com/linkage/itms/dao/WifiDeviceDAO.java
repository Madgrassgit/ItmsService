package com.linkage.itms.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author Administrator (Ailk No.)
 * @version 1.0
 * @since 2013-11-22
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class WifiDeviceDAO
{	
	private static Logger logger = LoggerFactory.getLogger(WifiDeviceDAO.class);
	
	public Map<String, String> queryWifiInfo(int userType, String username){
		logger.debug("queryWifiInfo({})", username);
		
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL();
		
		
		switch (userType)
		{
			case 1:
				psql.append("select a.wlan_num,d.city_id ");
				psql.append("	from  hgwcust_serv_info b, tab_hgwcustomer c, tab_gw_device d, tab_devicetype_info e, tab_bss_dev_port a ");
				psql.append("	where  b.serv_type_id=10  and b.user_id=c.user_id and c.device_id=d.device_id and d.devicetype_id=e.devicetype_id and e.spec_id=a.id");
				psql.append("	and b.username='" + username + "'");
				break;
			case 2:
				psql.append(" select a.username, e.wlan_num from tab_hgwcustomer a left join ");
				psql.append(" (select b.device_id, d.wlan_num from tab_gw_device b, tab_devicetype_info c, tab_bss_dev_port d ");
				psql.append(" where b.devicetype_id = c.devicetype_id and c.spec_id=d.id) e on a.device_id = e.device_id ");
				psql.append(" where a.username='" + username + "'");
//				psql.append(" select a.wlan_num ");
//				psql.append("	from  tab_hgwcustomer c, tab_gw_device d, tab_devicetype_info e, tab_bss_dev_port a ");
//				psql.append("	where c.device_id=d.device_id and d.devicetype_id=e.devicetype_id and e.spec_id=a.id ");
//				psql.append("	and  c.username='" + username + "'");
				break;
			case 3:
				psql.append("select a.wlan_num,d.city_id ");
				psql.append("	from hgwcust_serv_info b , tab_hgwcustomer c, tab_gw_device d, tab_devicetype_info e, tab_bss_dev_port a ");
				psql.append("	where  b.serv_type_id=11  and b.user_id=c.user_id and c.device_id=d.device_id and d.devicetype_id=e.devicetype_id and e.spec_id=a.id");
				psql.append("	and b.username='" + username + "'");
				break;
			case 4:
				psql.append("select a.wlan_num,d.city_id ");
				psql.append("	from  tab_voip_serv_param b,tab_hgwcustomer c,tab_gw_device d,tab_devicetype_info e,tab_bss_dev_port a  ");
				psql.append("	where b.user_id=c.user_id and c.device_id=d.device_id and d.devicetype_id=e.devicetype_id and e.spec_id=a.id ");
				psql.append("	and   b.voip_phone='" + username + "'");
				break;
			case 5:
				psql.append("select a.wlan_num,d.city_id ");
				psql.append("	from  tab_voip_serv_param b,tab_hgwcustomer c,tab_gw_device d,tab_devicetype_info e,tab_bss_dev_port a  ");
				psql.append("	where b.user_id=c.user_id and c.device_id=d.device_id and d.devicetype_id=e.devicetype_id and e.spec_id=a.id ");
				psql.append("	and   b.voip_username='" + username + "'");
				break;
			default:
				psql.append("select a.wlan_num,d.city_id ");
				psql.append(" from  tab_bss_dev_port a , tab_gw_device d  where 1=2");
				break;
		}
		return DBOperation.getRecord(psql.getSQL());
	}
}
