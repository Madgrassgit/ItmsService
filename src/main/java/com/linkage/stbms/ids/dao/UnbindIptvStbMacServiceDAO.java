package com.linkage.stbms.ids.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.stbms.ids.util.CommonUtil;


public class UnbindIptvStbMacServiceDAO {
	
	private static final Logger logger = LoggerFactory.getLogger(UnbindIptvStbMacServiceDAO.class);
	
	
	/**
	 * 根据机顶盒MAC地址检索，并确认业务帐号与机顶盒是否存在绑定关系
	 * @param devMac
	 * @return
	 */
	public Map<String, String> getUserInfoAndDevInfoByDevMac(String devMac) {
		
		logger.debug("UnbindIptvStbMacServiceDAO==>getUserInfoAndDevInfoByDevMac({})",
				new Object[] { devMac });
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select device_id, device_serialnumber, customer_id, serv_account, cpe_allocatedstatus ");
		psql.append("  from "+CommonUtil.addPrefix("tab_gw_device"));
		psql.append(" where 1=1 ");
		psql.append("   and cpe_allocatedstatus = 1 ");  // 关联用户状态;1绑定，0未绑定
		psql.append("   and cpe_mac = '"+devMac+"'");
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
	/**
	 * 根据业务帐号检索，并确认业务帐号与机顶盒是否存在绑定关系
	 * @param servAccount
	 * @return
	 */
	public Map<String, String> getUserInfoAndDevInfoByServAccount(String servAccount) {
		
		logger.debug(
				"UnbindIptvStbMacServiceDAO==>getUserInfoAndDevInfoByServAccount({})",
				new Object[] { servAccount });
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.device_id, a.device_serialnumber, a.customer_id, a.serv_account, a.cpe_allocatedstatus ");
		psql.append("  from "+CommonUtil.addPrefix("tab_gw_device") + " a," +CommonUtil.addPrefix("tab_customer") + " b");
		psql.append(" where 1=1 ");
		psql.append("   and a.customer_id = b.customer_id ");
		psql.append("   and a.cpe_allocatedstatus = 1 ");  // 关联用户状态;1绑定，0未绑定
		psql.append("   and b.serv_account = '"+servAccount+"'");
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
}
