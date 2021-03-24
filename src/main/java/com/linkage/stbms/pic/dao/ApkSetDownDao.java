
package com.linkage.stbms.pic.dao;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.stbms.pic.service.ApkSetDownChecker;

/**
 * 江西 APK 系统调用ITV终端网管平台下发业务接口
 * @return
 */
public class ApkSetDownDao
{

	final static Logger logger = LoggerFactory.getLogger(ApkSetDownDao.class);
	
	/**
	 * 根据业务账号查询用户.
	 * 
	 * @return
	 */
	public Map<String, String> getUserInfoyByServAccount(String servAccount)
	{
		logger.debug("getUserInfoyByServAccount({})", servAccount);
		
		PrepareSQL pSQL = new PrepareSQL(" select b.device_id, a.serv_account,a.pppoe_user,a.serv_pwd,b.cpe_mac from stb_tab_customer a ");
		pSQL.append(" left join stb_tab_gw_device b on a.customer_id=b.customer_id where a.cust_stat in ('1','2') ");
		pSQL.append(" and a.serv_account=? order by a.openUserdate desc ");
		
		pSQL.setString(1, servAccount);
		
		return DBOperation.getRecord(pSQL.getSQL());
	}
	
	
	/**
	 * 根据mac地址查询设备信息.
	 * 
	 * @return
	 */
	public Map<String, String> getdevInfoByMac(String mac)
	{
		logger.debug("getdevInfoByMac({})", mac);
		
		PrepareSQL pSQL = new PrepareSQL(" select a.serv_account,a.customer_id,a.cpe_allocatedstatus,a.device_id ");
		pSQL.append(" from stb_tab_gw_device a where a.cpe_mac=? ");
		
		pSQL.setString(1, mac);
		
		return DBOperation.getRecord(pSQL.getSQL());
	}
	
	/**
	 * 记录结果
	 */
	public void saveApkRes(ApkSetDownChecker checker)
	{
		PrepareSQL pSql = new PrepareSQL("insert into stb_tab_apk_res(serv_account, pppoe_user, ");
		pSql.append(" cpe_mac, result, resultdesc, add_time) values (?,?,?,?,?, ?)");
		
		int index = 0;
		pSql.setString(++index, checker.getUsername());
		pSql.setString(++index, checker.getPppoeuser());
		pSql.setString(++index, checker.getMac());
		pSql.setInt(++index, checker.getResult());
		pSql.setString(++index, checker.getResultDesc());
		pSql.setLong(++index, System.currentTimeMillis() / 1000);
		
		DBOperation.executeUpdate(pSql.getSQL());
	}
	
}
