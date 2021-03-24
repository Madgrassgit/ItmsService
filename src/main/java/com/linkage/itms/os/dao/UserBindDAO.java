
package com.linkage.itms.os.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.os.obj.RecieveSheetOBJ;

/**
 * 用户绑定操作类
 * 
 * @author zhangshimin(工号) Tel:
 * @version 1.0
 * @since 2011-6-24 下午04:09:26
 * @category com.linkage.itms.os.dao
 * @copyright 南京联创科技 网管科技部
 */
public class UserBindDAO
{

	private static Logger logger = LoggerFactory.getLogger(UserBindDAO.class);

	/**
	 * 获取绑定设备信息
	 * 
	 * @param orderId
	 * @return
	 */
	public Map<String, String> getUserBind(String username)
	{
		logger.debug("getUserBind()");
		StringBuffer sql = new StringBuffer();
		sql.append("select a.username,b.device_name,b.cpe_mac,b.device_serialnumber,b.oui,d.rela_dev_type_id,e.device_model,f.vendor_name");
		sql.append(" from tab_hgwcustomer a,tab_gw_device b,tab_devicetype_info d,gw_device_model e,tab_vendor f");
		sql.append(" where a.device_id=b.device_id and 	b.devicetype_id=d.devicetype_id and");
		sql.append(
				" d.vendor_id=f.vendor_id and d.device_model_id=e.device_model_id  and a.username='")
				.append(username).append("'");
		PrepareSQL psql = new PrepareSQL(sql.toString());
		Map<String, String> resMap = DBOperation.getRecord(psql.getSQL());
		if (resMap == null || resMap.isEmpty())
		{
			resMap = new HashMap<String, String>();
			resMap.put("username", "");
			resMap.put("device_serialnumber", "");
			resMap.put("oui", "");
			resMap.put("rela_dev_type_id", "");
			resMap.put("device_model", "");
			resMap.put("vendor_name", "");
			resMap.put("device_name", "");
			resMap.put("cpe_mac", "");
		}
		return resMap;
	}

	public void saveOriginSheet(String sheet,RecieveSheetOBJ obj)
	{
		logger.debug("saveOriginSheet({})", sheet);
		logger.warn("["+obj.getLoid()+"]保存工单信息tab_egw_bsn_open_original and tab_bss_sheet");
		// 回单结果
		int result = 0;
		// 回单结果说明
		String result_info = "成功";
		// 接收时间
		long receive_date = new Date().getTime();
		// 工单id
		long id = Math.round(Math.random() * 1000000000000L);
//		if("6".equals(obj.getOperType()) || "2".equals(obj.getIsGiveDev()))
//		{
			if("2".equals(obj.getIsGiveDev())){
				result = 9;
			}
			String insertSQL = "insert into tab_egw_bsn_open_original(id, product_spec_id,"
				+ "type, result, result_info, receive_date, "
				+ "city_id,username,sheet_para_desc) "
				+ " values ('"
				+ id
				+ "','"
				+ obj.getServTpe()
				+ "',"
				+ obj.getOperType()
				+ ","
				+ result
				+ ",'"
				+ result_info
				+ "',"
				+ receive_date
				+ ",'"
				+ obj.getCityId()
				+ "','"
				+ obj.getLoid()
				+ "','"
				+ "" + "')";
		PrepareSQL psql = new PrepareSQL(insertSQL);
		DBOperation.executeUpdate(psql.getSQL());
		//入BSS业务工单表,用于统计
		String strSQL = "insert into tab_bss_sheet ("
			+ "bss_sheet_id,customer_id,product_spec_id"
			+ ",type,result,receive_date"
			+ ",city_id,username) values ('"
			+ id
			+ "','"
			+ obj.getLoid()
			+ "',"
			+ obj.getServTpe()
			+ ","
			+ obj.getOperType()
			+ ",0,"
			+ receive_date
			+ ",'"
			+ obj.getCityId()
			+ "','"
			+ obj.getLoid()
			+ "')";
		psql = new PrepareSQL(strSQL);
		DBOperation.executeUpdate(psql.getSQL());
//		}

	}
	
	public Map<String,String> queryLoidByVoip(String voipPhone,String cityId,String devType)
	{
		logger.debug("queryLoidBynet({})", voipPhone);
		String tab_voip_serv = "tab_voip_serv_param";
		String tab_customer = "tab_hgwcustomer";
		if(devType != null && !"".equals(devType) && "2".equals(devType)){
			tab_voip_serv = "tab_egw_voip_serv_param";
			tab_customer = "tab_egwcustomer";
		}
		String strSQL = "select a.username,b.voip_port from " + tab_customer + " a, " + tab_voip_serv + " b where a.user_id=b.user_id " +
				"and a.city_id='" + cityId + "' and b.voip_phone='" + voipPhone + "'";
		PrepareSQL psql = new PrepareSQL(strSQL);
		return DBOperation.getRecord(psql.getSQL());
		
	}
	public String queryLoidBynet(String username,String cityId,String devType)
	{
		logger.debug("queryLoidBynet({})", username);
		String tab_serv_info = "hgwcust_serv_info";
		String tab_customer = "tab_hgwcustomer";
		if(devType != null && !"".equals(devType) && "2".equals(devType)){
			tab_serv_info = "egwcust_serv_info";
			tab_customer = "tab_egwcustomer";
		}
		String loid = "";
		String strSQL = "select a.username from " + tab_customer + " a, " + tab_serv_info + " b where a.user_id=b.user_id " +
				"and a.city_id='" + cityId + "' and b.username='" + username + "'";
		PrepareSQL psql = new PrepareSQL(strSQL);
		try{
			Map<String, String> resMap = DBOperation.getRecord(psql.getSQL());
			if(resMap == null || resMap.isEmpty()){
				loid = "";
			}
			else
			{
				loid = resMap.get("username");
				
			}
		}
		catch(Exception e)
		{
			logger.warn("获取LOID出现异常:{}",e.getMessage());
			loid = "";
		}
		return loid;
		
	}
	/** 销户用到的操作 */

	/**
	 * 检查用户是否还存在给定的servTypeId以外的业务,返回业务个数,没用则返回0
	 * 
	 * @param
	 * @author zhangsm
	 * @date 2011-8-13
	 * @return int
	 */
	public int hasElseService(String username, int servTypeId,String devType) {
		logger.debug("hasElseService({},{})", new Object[]{username, servTypeId});
		// 判断是否还有其他业务处于非销户状态
		String tab_serv_info = "hgwcust_serv_info";
		String tab_customer = "tab_hgwcustomer";
		if(devType != null && !"".equals(devType) && "2".equals(devType)){
			tab_serv_info = "egwcust_serv_info";
			tab_customer = "tab_egwcustomer";
		}
		String strSQL = "select a.username from " + tab_serv_info + " a," + tab_customer + " b where a.user_id=b.user_id"
				+ " and b.username=? and a.serv_type_id <> ?  and (a.serv_status=1 or a.serv_status=2)";

		PrepareSQL psql = new PrepareSQL();
		psql.setSQL(strSQL);
		psql.setString(1, username);
		psql.setInt(2, servTypeId);

		// 查询
		List recordList = DBOperation.getRecords(psql.getSQL());
		if (null != recordList && recordList.size() > 0) {
			return recordList.size();
		}
		return 0;
	}
	public int hasElseService(String username, int servTypeId,String voipPhone,String devType) {
		logger.debug("hasElseService({},{})", new Object[]{username, servTypeId});
		// 判断是否还有其他业务处于非销户状态
		String tab_voip_serv = "tab_voip_serv_param";
		String tab_customer = "tab_hgwcustomer";
		if(devType != null && !"".equals(devType) && "2".equals(devType)){
			tab_voip_serv = "tab_egw_voip_serv_param";
			tab_customer = "tab_egwcustomer";
		}
		String strSQL = "select b.username from " + tab_voip_serv + " a," + tab_customer + " b where a.user_id=b.user_id"
				+ " and b.username=? and a.voip_phone <> ?  ";

		PrepareSQL psql = new PrepareSQL();
		psql.setSQL(strSQL);
		psql.setString(1, username);
		psql.setString(2, voipPhone);

		// 查询
		List recordList = DBOperation.getRecords(psql.getSQL());
		if (null != recordList && recordList.size() > 0) {
			return recordList.size();
		}
		return 0;
	}
	public Map<String, String> getOriginSheet(String loId,String productSpecId,String type){
		String sql = " select * from tab_egw_bsn_open_original where username = '"+loId +"' and product_spec_id = '"+ productSpecId + "' and type = "+type + " order by receive_date desc";
		// mysql db
		if (3 == DBUtil.GetDB()) {
			sql = " select sheet_para_desc from tab_egw_bsn_open_original where username = '"+loId +"' and product_spec_id = '"+ productSpecId + "' and type = "+type + " order by receive_date desc";
		}
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL(sql);
		List<HashMap<String, String>>  list = DBOperation.getRecords(psql.getSQL());
		return list.size() > 0 ? list.get(0) : null;
	}
	
	public Map<String, String> getWanType(int serv_type_id,String loId){
		StringBuffer sql = new StringBuffer();
		sql.append("select b.wan_type from tab_hgwcustomer a inner join hgwcust_serv_info b on a.user_id=b.user_id ");
		sql.append("where b.serv_type_id=? and a.username=?");
		PrepareSQL psql = new PrepareSQL(sql.toString());
		psql.setInt(1, serv_type_id);
		psql.setString(2, loId);
		Map<String, String>  map = DBOperation.getRecord(psql.getSQL());
		return map;
	}
}
