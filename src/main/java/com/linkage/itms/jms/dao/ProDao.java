package com.linkage.itms.jms.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.jms.DeviceInfo;

/**
 * 
 * @author xiangzl (Ailk No.)
 * @version 1.0
 * @since May 17, 2013
 * @category com.linkage.itms.jms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ProDao
{
	//日志记录
	private static final Logger logger = LoggerFactory.getLogger(ProDao.class);
	
	public int updatePvc()
	{
		return 1;
	}

	public HashMap<String, String> getUserServInfo(String devId, String servTypeId,String gwType)
	{
		logger.debug("getUserServInfo()");
		HashMap<String, String> map = null;
		PrepareSQL psql = new PrepareSQL();
		
		String tabName = " tab_hgwcustomer a,hgwcust_serv_info b ";
		if("2".equals(gwType))
		{
			tabName = " tab_egwcustomer a,egwcust_serv_info b ";
		}
		
		psql.append("select a.username as loid,b.username as servUsername,b.serv_type_id,b.orderid,a.city_id from "+tabName+" where a.user_id = b.user_id and a.device_id='"
				+devId+"' and b.serv_type_id ="+servTypeId);
		
		List<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		
		if(list!=null && list.size()>0)
		{
			map = list.get(0);
		}
		return map;
	}
	
	public boolean hasUserOtherServInfoUndo(String devId, String servTypeId,String gwType)
	{
		logger.debug("hasUserOtherServInfoUndo()");
		boolean hasOtherUndoServ = false;
		PrepareSQL psql = new PrepareSQL();
		
		String tabName = " tab_hgwcustomer a,hgwcust_serv_info b ";
		if("2".equals(gwType))
		{
			tabName = " tab_egwcustomer a,egwcust_serv_info b ";
		}
		
		psql.append("select a.username as loid,b.username as servUsername,b.open_status,b.serv_type_id,b.orderid,a.city_id from "+tabName+" where a.user_id = b.user_id and a.device_id='"
				+devId+"' and b.serv_type_id !="+servTypeId + " and b.open_status != 1 ");
		
		List<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		
		if(list!=null && list.size()>0)
		{
			hasOtherUndoServ = true;
		}
		return hasOtherUndoServ;
	}
	
	public List<HashMap<String,String>> getAllVoipPhone(String deviceId,String gwType)
	{
		String tabName = " tab_hgwcustomer a,tab_voip_serv_param b ";
		if("2".equals(gwType))
		{
			tabName = " tab_egwcustomer a,tab_egw_voip_serv_param b ";
		}
		
		String sql = "select b.voip_phone from " + tabName + " where a.user_id = b.user_id and a.device_id ='" +deviceId +"'"; 
		PrepareSQL psql = new PrepareSQL(sql);
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 从原始工单表查询订单id
	 * @param devId
	 * @param servTypeId
	 * @param gwType
	 * @return
	 */
	public HashMap<String, String> getUserServInfoByHistory(String devId, String servTypeId,String gwType,String voipPhone)
	{
		logger.debug("getUserServInfo()");
		HashMap<String, String> map = null;
		PrepareSQL psql = new PrepareSQL();
		
		String tabName = " tab_hgwcustomer a,tab_bss_sheet b ";
		if("2".equals(gwType))
		{
			tabName = " tab_egwcustomer a,tab_bss_sheet b ";
		}
		
		psql.append("select a.username as loid,b.servUsername,b.product_spec_id as serv_type_id,b.order_id as orderid,a.city_id from "+tabName
				+" where a.username = b.username and a.device_id='"
				+devId+"' and b.product_spec_id ="+servTypeId + " and b.servUsername = '"
				+ voipPhone +"' and b.order_id not in ('FROMWEB-0000002','AAA') order by b.receive_date desc");
		
		List<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		
		if(list!=null && list.size()>0)
		{
			map = list.get(0);
		}
		return map;
	}

	public void updateBssSheet(String loid, String servName, String servTypeId,
			DeviceInfo obj, String resrlt, String bssSheetId)
	{
		logger.debug("getUserServInfo()");
		PrepareSQL psql = new PrepareSQL();
		psql.append("update tab_bss_sheet set result_spec_state="+obj.getOpenStatus()+" ,result_spec_time="+new Date().getTime()/1000);
		psql.append(" ,result_spec_desc='"+resrlt+"' where order_id = '"+bssSheetId+"' and product_spec_id ="+servTypeId );
//		logger.warn("xiangzl:"+psql.getSQL());
		DBOperation.executeUpdate(psql.getSQL());
	}

//	public Map<String, String> getOrderid(String loid, String servName, String servTypeId, DeviceInfo obj)
//	{
//		logger.debug("getOrderid()");
//		PrepareSQL psql = new PrepareSQL();
//		psql.append("select bss_sheet_id from  tab_bss_sheet where username ='"+loid+"' and servUsername ='"+servName+"' and type="+obj.getServStatus());
//		psql.append(" and product_spec_id ="+servTypeId +"  order by receive_date desc");
//		
//		return DBOperation.getRecord(psql.getSQL());
//		
//	}
	
	public List<HashMap<String,String>> getFactoryResetReturnDiagOpid(String deviceId,String loid)
	{
		String tabName = " tab_getFactoryResetReturnDiag_sheet";
		String sql = "select op_id from " + tabName + " where device_id ='" +deviceId +"' and result = -1 ";
		if(!StringUtil.IsEmpty(loid) && "null".equalsIgnoreCase(loid)){
			sql += " and loid = '" + loid + "' ";
		}
		PrepareSQL psql = new PrepareSQL(sql);
		
		return DBOperation.getRecords(psql.getSQL());
	}
}
