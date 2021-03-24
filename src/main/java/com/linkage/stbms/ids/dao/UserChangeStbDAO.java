package com.linkage.stbms.ids.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.stbms.ids.util.CommonUtil;

/**
 * 
 * @author yinlei3 (73167)
 * @version 1.0
 * @since 2015年9月7日
 * @category com.linkage.stbms.ids.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class UserChangeStbDAO
{
	private static final Logger logger = LoggerFactory.getLogger(UserChangeStbDAO.class);

	/**
	 * 根据参数imei值检索
	 * @param imei
	 * @return
	 */
	public Map<String, String> getImei(String imei) {
		
		logger.debug("UserChangeStbDAO==>getImei({})",
				new Object[] { imei });
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select imei, status ");
		psql.append("  from "+CommonUtil.addPrefix("tab_imei"));
		psql.append(" where 1=1 ");
		psql.append("   and imei = '"+imei+"'");
		
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 插入imei数据
	 * @param imei
	 * @return
	 */
	public int insertImei(String imei) {
		
		logger.debug("UserChangeStbDAO==>insertImei({})",
				new Object[] { imei });
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("insert into " + CommonUtil.addPrefix("tab_imei"));
		psql.append("(imei,status,input_person,upd_date)values('");
		psql.append(imei +"',0,'unknown','"+new DateTimeUtil().getLongDateChar()+"')");
		
		
		return DBOperation.executeUpdate(psql.getSQL());
	}

	/**
	 * 根据业务账号检索
	 * @param serv_account 业务账号
	 * @return
	 */
	public Map<String, String> getCustomerByAcc(String serv_account) {
		
		logger.debug("UserChangeStbDAO==>getCustomerByAcc({})",
				new Object[] { serv_account });
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select customer_id,city_id,cpe_mac ");
		psql.append("  from "+CommonUtil.addPrefix("tab_customer"));
		psql.append(" where 1=1 ");
		psql.append("   and serv_account = '"+serv_account+"'");
		psql.append("   order by updatetime desc");
		
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 根据mac检索
	 * @param mac mac地址
	 * @return
	 */
	public Map<String, String> getCustomerByMac(String mac) {
		
		logger.debug("UserChangeStbDAO==>getCustomerByMac({})",
				new Object[] { mac });
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.customer_id,a.city_id,a.cpe_mac ");
		psql.append("  from "+CommonUtil.addPrefix("tab_customer") +" a, stb_tab_gw_device b ");
		psql.append(" where a.serv_account = b.serv_account ");
		psql.append("   and b.cpe_mac = '"+mac+"'");
		psql.append("   order by b.cpe_currentupdatetime desc");
		
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 更新用户表mac地址
	 * @param customerId 客户ID
	 * @param mac  mac地址
	 * @return
	 */
	public int updateCustomerMac(String customerId, String mac)
	{
		logger.debug("UserChangeStbDAO==>updateCustomerMac({},{})",
				new Object[] { customerId, mac });
		String sql = "update " +CommonUtil.addPrefix("tab_customer")+ " set cpe_mac='"+ mac + "', user_status=0  where customer_id = "+ customerId;
		if ("jx_dx".equals(com.linkage.stbms.itv.main.Global.G_instArea)) {
			sql = "update " +CommonUtil.addPrefix("tab_customer")+ " set cpe_mac='"+ mac + "', user_status=0, updatetime = "+ System.currentTimeMillis() / 1000 +"  where customer_id = "+ customerId;
		}
		PrepareSQL psql = new PrepareSQL(sql);
		return DBOperation.executeUpdate(psql.getSQL());
	}

	/**
	 * 更新用户表mac对应关系
	 * @param mac mac地址
	 * @return
	 */
	public int updateCustomerByNewMac(String mac)
	{
		logger.debug("UserChangeStbDAO==>updateCustomerByNewMac({})",
				new Object[] { mac });
		String sql = "update " + CommonUtil.addPrefix("tab_customer")
				+ " set cpe_mac= null  where cpe_mac = '" + mac + "'";
		PrepareSQL psql = new PrepareSQL(sql);		
		return DBOperation.executeUpdate(psql.getSQL());
		
	}
	
	/**
	 * 将设备状态重置为未绑定
	 * 
	 * @param cityId
	 *            重置城市属地
	 * @param deviceId
	 *            设备ID
	 * @return 返回重置未绑定的SQL语句
	 */
	public int unbindDevice(String cityId, String userInfo,String selectType) {
		String sql = "update ? set customer_id=null,city_id=?, serv_account=null,cpe_currentupdatetime=?"
				+ ",cpe_allocatedstatus=0"
				+ ",bind_time=?,zero_account=null,status=0"
				+ ",bind_way=null,bind_state=0";
		if("1".equals(selectType)){
			sql += " where serv_account=?";			
		}
		else{
			sql += " where cpe_mac=?";
		}
				
		PrepareSQL pSql = new PrepareSQL(sql);
		int index = 0;
		pSql.setStringExt(++index, CommonUtil.addPrefix("tab_gw_device"),
				false);
		pSql.setStringExt(++index, cityId,true);
		pSql.setLong(++index, System.currentTimeMillis() / 1000);
		pSql.setLong(++index, System.currentTimeMillis() / 1000);
		pSql.setStringExt(++index, userInfo,true);
		return DBOperation.executeUpdate(pSql.getSQL());
	}

	
	
	/**
	 * 插入一条日志记录
	 * @param imei
	 * @param oldMac
	 * @param newMac
	 * @return
	 */
	public int insertLogUserChangerStb(String imei,String oldMac,String newMac,String param,int clientType,String userName){
		
		PrepareSQL pSql = new PrepareSQL(
				"insert into ? (imei,old_mac,new_mac,sheet_param,call_time,result,client_type,user_name ) values (?,?,?,?,?,?,?,?)");
		int index = 0;
		pSql.setStringExt(++index, CommonUtil.addPrefix("tab_change_record"), false);
		pSql.setStringExt(++index,imei,true);
		pSql.setStringExt(++index,oldMac,true);
		pSql.setStringExt(++index,newMac,true);
		pSql.setStringExt(++index,param,true);
		pSql.setLong(++index, System.currentTimeMillis() / 1000);
		pSql.setInt(++index, 0);
		pSql.setInt(++index, clientType);
		pSql.setStringExt(++index, userName,true);
		return DBOperation.executeUpdate(pSql.getSQL());
	}
	
	/**
	 * 更新用户表mac地址
	 * @param customerId 客户ID
	 * @param mac  mac地址
	 * @return
	 */
	public int updateMacByServAccount(String serv_account, String mac)
	{
		logger.debug("UserChangeStbDAO==>updateMacByServAccount({},{})",
				new Object[] { serv_account, mac });
		String sql = "update " +CommonUtil.addPrefix("tab_customer")+ 
				" set cpe_mac='"+ mac + "' where serv_account = '"+ serv_account+"'";
		logger.info(sql);
		return DBOperation.executeUpdate(sql);
	}
	
}
