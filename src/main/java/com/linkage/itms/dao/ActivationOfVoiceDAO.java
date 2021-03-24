package com.linkage.itms.dao;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author hp (Ailk No.)
 * @version 1.0
 * @since 2017-10-17
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 
 */
public class ActivationOfVoiceDAO {
//	private static Logger logger = LoggerFactory.getLogger(ActivationOfVoiceDAO.class);

	public List<HashMap<String, String>> queryUserInfo(String device_id, String userType) {
//		String table_customer = "";
//		String table_voip = "";
//		if (userType.equals("1")) {
//			table_customer = "tab_hgwcustomer";
//			table_voip = "tab_voip_serv_param";
//		} else if (userType.equals("2")) {
//			table_customer = "tab_egwcustomer";
//			table_voip = "tab_egw_voip_serv_param";
//		}
		PrepareSQL psql = new PrepareSQL();
		psql.append("select c.device_serialnumber,d.device_model  ");
		psql.append(" from  tab_gw_device c,gw_device_model d where ");
		psql.append(" c.device_model_id=d.device_model_id and c.device_id= ?  ");
		psql.setString(1,device_id);
		List<HashMap<String, String>> result = DBOperation.getRecords(psql.getSQL());
		return result;
	}

	public List<HashMap<String, String>> queryUserid(String userType, String userInfoType, String userInfo) {
		String table_customer = "";
		String table_voip = "";
		if (userType.equals("1")) {
			table_customer = "tab_hgwcustomer";
			table_voip = "tab_voip_serv_param";
		} else if (userType.equals("2")) {
			table_customer = "tab_egwcustomer";
			table_voip = "tab_egw_voip_serv_param";
		}
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.user_id, a.device_id, b.voip_port, b.voip_phone, b.line_id");
		if ("2".equals(userInfoType)) {
			psql.append(" from " + table_customer + " a left join " + table_voip + " b on a.user_id = b.user_id ");
			psql.append(" where a.username='" + userInfo + "'");
		}
		else if ("4".equals(userInfoType)) {
			psql.append(" from " + table_customer + " a , " + table_voip + " b where  a.user_id = b.user_id ");
			psql.append(" and b.voip_phone='" + userInfo + "'");
		}
		else if ("5".equals(userInfoType)) {
			psql.append(" from " + table_customer + " a , " + table_voip + " b where  a.user_id = b.user_id ");
			psql.append(" and b.voip_username='" + userInfo + "'");
		}
		List<HashMap<String, String>> result = DBOperation.getRecords(psql.getSQL());
		return result;
	}
}
