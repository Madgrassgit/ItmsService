package com.linkage.itms.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;

public class MoSSIDPWDAO {
	private static Logger logger = LoggerFactory.getLogger(MoSSIDPWDAO.class);
	/**
	 * 查询用户信息
	 * @param userType
	 * @param username
	 * @return
	 */
	public Map<String, String> queryServInfo(int userType, String username){
		logger.debug("MoSSIDPWDAO({})", username);
		
		if (StringUtil.IsEmpty(username)) {
			logger.error("username is Empty");
			return null;
		}
		
		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select a.user_id,a.username ");
		
		switch (userType) {
		case 1:
		case 3:
			psql.append("	from tab_hgwcustomer a, hgwcust_serv_info b");
			psql.append("	where a.user_id=b.user_id and b.serv_status=1");
			psql.append("	and b.username='" + username + "'");
			break;
		case 4:
			psql.append(" from tab_hgwcustomer a,hgwcust_serv_info b,tab_voip_serv_param c");
			psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
			psql.append(" and c.voip_phone='" + username + "'");
			break;
		case 5:
			psql.append(" from tab_hgwcustomer a,hgwcust_serv_info b,tab_voip_serv_param c");
			psql.append("	where a.user_id=b.user_id and b.user_id=c.user_id");
			psql.append(" and c.voip_username='" + username + "'");
			break;
		default:
			psql.append(" from tab_hgwcustomer a where a.user_state = '1'");
			psql.append(" and a.username = '" + username + "'");
		}
		return DBOperation.getRecord(psql.getSQL());
	}
	/**
	 * 获取用户设备信息
	 * 
	 * @param 用户ID
	 * @author bell
	 * @date 2014年4月29日
	 * @return Map<String,String>
	 */
	public Map<String, String> getUserDevInfo(String userId) {
		logger.debug("getUserDevInfo({})", userId);

		if (StringUtil.IsEmpty(userId)) {
			logger.warn("username is empty!");
			return null;
		}

		String strSQL = "select a.user_id,b.device_id,b.vendor_id,b.device_model_id,b.oui,b.device_serialnumber,b.devicetype_id,"
				+ " c.online_status, a.city_id"
				+ " from tab_hgwcustomer a left join tab_gw_device b on a.device_id=b.device_id"
				+ " left join gw_devicestatus c on a.device_id=c.device_id"
				+ " where a.user_state in ('1','2')"
				+ " and a.user_id="
				+ userId + "";
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}
}
