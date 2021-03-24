package com.linkage.itms.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

/**
 * 
 * @author xiangzl (Ailk No.)
 * @version 1.0
 * @since 2014-4-9
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class IsRouteDeviceDao
{	
	private static Logger logger = LoggerFactory.getLogger(WifiDeviceDAO.class);
	
	/**
	 * 
	 * @param userType
	 * @param username
	 * @return
	 */
	public Map<String, String> queryRouteInfo(int userType, String username){
		logger.debug("IsRouteDeviceDao({})", username);
		
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select a.is_route,c.device_id, c.user_id");
		
		switch (userType)
		{
			case 1:
				psql.append("	from  hgwcust_serv_info b, tab_hgwcustomer c left join tab_gw_device d on c.device_id=d.device_id   left join tab_route_version a on d.devicetype_id=a.devicetype_id ");
				psql.append("	where  b.serv_type_id=10  and b.user_id=c.user_id ");
				psql.append("	and b.username='" + username + "'");
				break;
			case 2:
				psql.append("	from  tab_hgwcustomer c left join tab_gw_device d on c.device_id = d.device_id left join  tab_route_version a on d.devicetype_id=a.devicetype_id ");
				psql.append("	where  1=1 ");
				psql.append("	and  c.username='" + username + "'");
				break;
			case 3:
				psql.append("	from hgwcust_serv_info b , tab_hgwcustomer c left join tab_gw_device d on c.device_id=d.device_id left join  tab_route_version a on d.devicetype_id=a.devicetype_id ");
				psql.append("	where  b.serv_type_id=11  and b.user_id=c.user_id  ");
				psql.append("	and b.username='" + username + "'");
				break;
			case 4:
				psql.append("	from  tab_voip_serv_param b,tab_hgwcustomer c left join tab_gw_device d on c.device_id=d.device_id left join  tab_route_version a on d.devicetype_id=a.devicetype_id  ");
				psql.append("	where b.user_id=c.user_id  ");
				psql.append("	and   b.voip_phone='" + username + "'");
				break;
			case 5:
				psql.append("	from  tab_voip_serv_param b,tab_hgwcustomer c left join tab_gw_device d on c.device_id=d.device_id left join  tab_route_version a on d.devicetype_id=a.devicetype_id  ");
				psql.append("	where b.user_id=c.user_id ");
				psql.append("	and   b.voip_username='" + username + "'");
				break;
			default:
				psql.append(" from  tab_bss_dev_port a , tab_gw_device d  where 1=2");
				break;
		}
		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * 查询用户是否存在
	 * @param userType
	 * @param username
	 * @return
	 */
	public Map<String, String> queryUserInfo(int userType, String username){
		return null;
	}

	public Map<String, String> queryRouteServ(int userType, String username)
	{
		logger.debug("IsRouteDeviceDao({})", username);
		
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select b.wan_type,b.open_status ");
		
		switch (userType)
		{
			case 1:
				psql.append("	from  hgwcust_serv_info b ");
				psql.append("	where  b.serv_type_id=10   ");
				psql.append("	and b.username='" + username + "'");
				break;
			case 2:
				psql.append(" ,c.user_id from tab_hgwcustomer c left join ");
				// mysql db
				if (3 == DBUtil.GetDB()) {
					psql.append(" (select wan_type,open_status,user_id from hgwcust_serv_info where serv_type_id = 10) b on c.user_id = b.user_id ");
				}
				else {
					psql.append(" (select * from hgwcust_serv_info where serv_type_id = 10) b on c.user_id = b.user_id ");
				}
				psql.append(" where c.username='" + username + "'");
				break;
			case 3:
				psql.append("	from hgwcust_serv_info b  ");
				psql.append("	where b.serv_type_id = 10 and b.user_id in(select user_id from hgwcust_serv_info c where  c.serv_type_id=11   ");
				psql.append("	and c.username='" + username + "')");
				break;
			case 4:
				psql.append("	from hgwcust_serv_info b  where b.serv_type_id = 10 and b.user_id in (");
				psql.append("	select user_id from  tab_voip_serv_param  a  ");
				psql.append("	where 1=1 ");
				psql.append("	and   b.voip_phone='" + username + "')");
				break;
			case 5:
				psql.append("	from hgwcust_serv_info b  where b.serv_type_id = 10 and b.user_id in (");
				psql.append("	select user_id from  tab_voip_serv_param  a  ");
				psql.append("	where 1=1 ");
				psql.append("	and   b.voip_username='" + username + "')");
			default:
				psql.append(" from  tab_bss_dev_port a , tab_gw_device d  where 1=2");
				break;
		}
		return DBOperation.getRecord(psql.getSQL());
	}
}
