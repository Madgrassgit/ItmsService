package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.DateTimeUtil;

/**
 * 业务用户表数据库操作类
 * 
 * @author Jason(3412)
 * @date 2010-6-23
 */
public class ServUserDAO {

	private static Logger logger = LoggerFactory.getLogger(ServUserDAO.class);
	/**
	 * 查询业务工单信息
	 * 
	 * @author zhangshimin
	 * @param num_splitPage
	 * @param curPage_splitPage
	 * @date Sep 13, 2010
	 * 
	 * 针对需求单JSDX_ITMS-REQ-20120306-LUHJ-004，增加了业务帐号b.username
	 * 
	 * @param
	 * @return List<Map>
	 */
	public List<Map<String,String>> getBssSheetServInfo(String userId)
	{
		logger.debug("getBssSheetServInfo({})", new Object[] {userId});
		/**
		 * Map<city_id,city_name>
		 */
		Map<String, String> cityMap = null;
		Map<String, String> servTypeMap = null;
		List<Map<String,String>>  resList = new ArrayList<Map<String,String>>();
		Map<String, String> resmap = null;
		
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
		}

		
		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select a.user_id, a.username, a.city_id, a.device_serialnumber, a.oui, a.device_id, b.vlanid, ");
		psql.append(" b.username as user_name, b.serv_type_id, b.serv_status, b.dealdate, b.completedate, b.opendate, ");
		psql.append(" b.open_status, b.wan_type, b.bind_port, d.spec_name, ");
		if ("sd_lt".equals(Global.G_instArea)) {
			psql.append(" (case when c.type_id='1' then 'hgu' else 'hgw' end) as type_id " );
		}
		else {
			psql.append(" (case when c.type_id='1' then 'E8-B' else 'E8-C' end) as type_id " );
		}
		psql.append(" from " + table_customer + " a left join " + table_serv_info + " b on (a.user_id=b.user_id) ");
		psql.append(" left join gw_cust_user_dev_type c on (a.user_id=c.user_id) left join tab_bss_dev_port d on (a.spec_id = d.id)");
		psql.append(" where 1=1 ");
		psql.append(" and a.user_id = ");
		psql.append(userId);
	
		cityMap = CityDAO.getCityIdCityNameMap();
		
		servTypeMap = getServType();
		
		List<HashMap<String,String>> list = DBOperation.getRecords(psql.getSQL());
		
		for(HashMap<String,String> rs : list)
		{
			resmap = new HashMap<String, String>();
			resmap.put("user_id", rs.get("user_id"));
			resmap.put("wan_type", rs.get("wan_type"));
			resmap.put("username", rs.get("username"));
			resmap.put("user_name", rs.get("user_name"));
			resmap.put("vlanId", rs.get("vlanid"));
			String city_id = rs.get("city_id");
			resmap.put("city_id", city_id);
			String city_name = StringUtil.getStringValue(cityMap.get(city_id));
			if (false == StringUtil.IsEmpty(city_name))
			{
				resmap.put("city_name", city_name);
			}
			else
			{
				resmap.put("city_name", "");
			}
			resmap.put("device_serialnumber", rs.get("device_serialnumber"));
			String serv_type_id = rs.get("serv_type_id");
			resmap.put("serv_type_id", serv_type_id);
			String tmp = "-";
			if (false == StringUtil.IsEmpty(serv_type_id))
			{
				tmp = servTypeMap.get(serv_type_id);
			}
			resmap.put("serv_type", tmp);
			// 将dealdate转换成时间
			try
			{
				long dealdate = StringUtil.getLongValue(rs.get("dealdate"));
				DateTimeUtil dt = new DateTimeUtil(dealdate * 1000);
				resmap.put("dealdate", dt.getLongDate());
			}
			catch (NumberFormatException e)
			{
				resmap.put("dealdate", "");
			}
			catch (Exception e)
			{
				resmap.put("dealdate", "");
			}
			if("CUC".equalsIgnoreCase(Global.G_OPERATOR)){
				resmap.put("dealdate", StringUtil.getStringValue(rs.get("dealdate")));
			}
			// 将completedate转换成时间
			try
			{
				long completedate = StringUtil.getLongValue(rs.get("completedate"));
				DateTimeUtil dt = new DateTimeUtil(completedate * 1000);
				resmap.put("completedate", dt.getLongDate());
			}
			catch (NumberFormatException e)
			{
				resmap.put("completedate", "");
			}
			catch (Exception e)
			{
				resmap.put("completedate", "");
			}
			resmap.put("open_status", rs.get("open_status"));
			resmap.put("type_id", rs.get("type_id"));
			//*****SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口) 新增字段 begin*****//
			resmap.put("spec_name", rs.get("spec_name"));
			resmap.put("bind_port", rs.get("bind_port"));
			// 将opendate转换成时间
			try
			{
				long opendate = StringUtil.getLongValue(rs.get("opendate"));
				DateTimeUtil dt = new DateTimeUtil(opendate * 1000);
				resmap.put("opendate", dt.getLongDate());
			}
			catch (NumberFormatException e)
			{
				resmap.put("opendate", "");
			}
			catch (Exception e)
			{
				resmap.put("opendate", "");
			}
			//*****SDLT-REQ-2017-04-13-YUZHIJIAN-001（山东联通RMS平台用户业务查询接口) 新增字段 end*****//
			resList.add(resmap);
		}
		cityMap = null;
		return resList;
	}
	
	
	/**
	 * 查询VOIP电话号码，VOIP帐号等参数
	 * @param user_id
	 * @return
	 */
	public List<HashMap<String, String>> getVoipPhone(String user_id) {
		String table_voip = "tab_voip_serv_param";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_voip = "tab_egw_voip_serv_param";
		}
		PrepareSQL psql = new PrepareSQL();
		// mysql db
		if (3 == DBUtil.GetDB()) {
			psql.append("select protocol, voip_phone, voip_port, digit_map from " + table_voip + " where user_id ="+user_id);
		} else {
			psql.append("select * from " + table_voip + " where user_id ="+user_id);
		}
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	public Map<String, String> getServType()
	{
		String sql = "select serv_type_id,serv_type_name from tab_gw_serv_type";
		PrepareSQL psql = new PrepareSQL(sql);
		List<HashMap<String,String>> list = DBOperation.getRecords(psql.getSQL());
		Map<String,String> servTypeMap = new HashMap<String, String>();
		for (Map<String,String> map : list)
		{
			servTypeMap.put(StringUtil.getStringValue(map.get("serv_type_id")),
					StringUtil.getStringValue(map.get("serv_type_name")).toUpperCase());
		}
		return servTypeMap;
	}
	/**
	 * 根据用户ID获取对应的业务信息数组
	 * 
	 * @param userId:用户ID
	 * @author Jason(3412)
	 * @date 2010-6-11
	 * @return HgwServUserObj[] 业务用户数组，未查询到结果则返回null
	 */
	public ArrayList<HashMap<String, String>> queryHgwcustServUserByDevId(
			long userId) {
		logger.debug("queryHgwcustServUserByDevId({})", userId);
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_serv_info = "egwcust_serv_info";
		}

		String strSQL = "select b.user_id, b.serv_type_id, b.username, b.open_status"
				+ " from " + table_serv_info + " b "
				+ " where b.serv_status=1 and b.user_id=?";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, userId);

		// 执行查询
		ArrayList<HashMap<String, String>> resList = DBOperation
				.getRecords(psql.getSQL());

		return resList;
	}

	/**
	 * 更新用户的业务开通状态
	 * 
	 * @param userId:用户ID；
	 *            servTypeId:业务类型ID
	 * @author Jason(3412)
	 * @date 2010-6-11
	 * @return int 1:成功
	 */
	public int updateServOpenStatus(long userId) {
		logger.debug("updateServOpenStatus({})", userId);
		return updateServOpenStatus(userId, 0);
	}

	/**
	 * 更新用户的业务开通状态
	 * 
	 * @param userId:用户ID；
	 *            servTypeId:业务类型ID
	 * @author Jason(3412)
	 * @date 2010-6-11
	 * @return int 1:成功
	 */
	public int updateServOpenStatus(long userId, int servTypeId) {
		logger.debug("updateServOpenStatus({}, {})", userId, servTypeId);
		String table_serv_info = "hgwcust_serv_info";
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_serv_info = "egwcust_serv_info";
		}

		// 更新SQL语句
		String strSQL = "update " + table_serv_info + " set open_status=0 "
				+ " where serv_status=1 and open_status!=0 and user_id=?";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, userId);
		if (0 != servTypeId) {
			psql.append(" and serv_type_id=" + servTypeId);
		}

		// 执行查询
		int reti = DBOperation.executeUpdate(psql.getSQL());

		return reti;
	}

	/**
	 * 查询用户路由业务的信息
	 * 
	 * @param _userId：用户ID
	 * @author Jason(3412)
	 * @date 2010-7-14
	 * @return Map<String,String>
	 * 
	 */
	public Map<String, String> queryRoutedUserInfo(long _userId) {
		logger.debug("queryRoutedUserInfo({})", _userId);

		String strSQL = "select * from res_user_serv_parm_conn where user_id=?";
		// mysql db
		if (3 == DBUtil.GetDB()) {
			strSQL = "select parm_stat from res_user_serv_parm_conn where user_id=?";	
		}
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, _userId);

		Map<String, String> retMap = DBOperation.getRecord(psql.getSQL());

		return retMap;
	}
	
	
	/**
	 * insert一条路由业务用户记录
	 * 
	 * @param _userId:用户ID
	 *            _username：宽带账号 _passwd：宽带密码 _paramTypeId：参数类型ID
	 *            _paramStat:业务状态
	 * @author Jason(3412)
	 * @date 2010-7-14
	 * @return int
	 */
	public int saveRoutedUser(long _userId, String _username, String _passwd,
			int _paramTypeId, int _paramStat) {
		logger.debug("saveRoutedUser({},{},{},{},{})", new Object[] { _userId,
				_username, _passwd, _paramTypeId, _paramStat });
		String strSQL = "insert into res_user_serv_parm_conn ("
			+ "user_id, parm_type_id, parm_stat, username, passwd, upd_time)"
			+ " values (?,?,?,?,?,  ?)";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, _userId);
		psql.setInt(2, _paramTypeId);
		psql.setInt(3, _paramStat);
		psql.setString(4, _username);
		psql.setString(5, _passwd);
		psql.setLong(6, System.currentTimeMillis()/1000);
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 更新用户的参数状态为未开通
	 * 
	 * @param 
	 * 	_userId:用户ID
	 * 	_paramTypeId：参数类型ID
	 * @author Jason(3412)
	 * @date 2010-7-14
	 * @return int
	 */
	public int updateRouteUser(long _userId, int _paramTypeId){
		logger.debug("updateRouteUser({}, {})", _userId, _paramTypeId);
		String strSQL = "update res_user_serv_parm_conn "
			+ " set parm_stat=0, upd_time=?"
			+ " where user_id=? and parm_type_id=?";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, System.currentTimeMillis()/1000);
		psql.setLong(2, _userId);
		psql.setInt(3, _paramTypeId);
		return DBOperation.executeUpdate(psql.getSQL());
	}

	public Map<String, String> getDigitMapValue(String digit_map_code)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select digit_map_code,digit_map_value from tab_digit_map where digit_map_code = ? ");
		psql.setString(1, digit_map_code);
		Map<String,String> map = DBOperation.getRecord(psql.getSQL());
		return map;
	}

	public Map<String, String> queryStrategyResult(String device_id,String service_id)
	{
		String sql = "select result_id from gw_serv_strategy_serv where device_id=? and service_id =? ";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, device_id);
		psql.setInt(2, StringUtil.getIntegerValue(service_id));
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		if (map == null || map.isEmpty())
		{
			map = new HashMap<String, String>();
		}
		return map;
	}
}
