
package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;

public class SpringServiceDao
{

	/** 日志 */
	private static Logger logger = LoggerFactory.getLogger(SpringServiceDao.class);

	/**
	 * 将不存在tab_accounts表的数据插入tab_excel_syn_accounts表
	 * 
	 * @param list
	 *            excel中符合条件的数据
	 * @param fileName
	 *            excel文件名
	 * @param synchronize_time
	 *            同步时间
	 */
	public void insertAccountInfo(List<Map<String, String>> list, String fileName,
			long synchronize_time)
	{
		PrepareSQL psql = null;
		ArrayList<String> listSQL = new ArrayList<String>();
		if (null == list || list.isEmpty())
		{
			logger.debug("此批次数据为空");
		}
		else
		{
			for (Map<String, String> map : list)
			{
				psql = new PrepareSQL();
				psql.append(" insert into tab_excel_syn_accounts(enName,chName,city_id,dept_name,dept_full_name,"
						+ "email,telephone,mobilephone,employee_type,dept_id,synchronize_time,itms_account,data_from)");
				psql.append(" values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
				psql.setString(1, map.get("enName"));
				psql.setString(2, map.get("chName"));
				psql.setString(3, map.get("city_id"));
				psql.setString(4, map.get("dept_name"));
				psql.setString(5, map.get("dept_full_name"));
				psql.setString(6, map.get("email"));
				psql.setString(7, map.get("telephone"));
				psql.setString(8, map.get("mobilephone"));
				psql.setString(9, map.get("employee_type"));
				psql.setString(10, map.get("dept_id"));
				psql.setLong(11, synchronize_time);
				psql.setString(12, "unknown");
				psql.setString(13, fileName);
				listSQL.add(psql.getSQL());
			}
			DBOperation.executeUpdate(listSQL);
		}
	}

	/**
	 * 根据用户名查找用户
	 * 
	 * @param loginname
	 *            登陆用户名
	 * @return 查询结果
	 */
	public Map<String, String> queryAccount(String loginname)
	{
		logger.debug("SpringServiceDao({})", loginname);
		PrepareSQL psql = new PrepareSQL();
		psql.append("select acc_oid,acc_loginname from tab_accounts where acc_loginname = '"
				+ loginname + "'");
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 根据用户名查找用户
	 * 
	 * @param loginname
	 *            登陆用户名
	 * @return 查询结果
	 */
	public Map<String, String> queryExcelAccount(String enName)
	{
		logger.debug("SpringServiceDao({})", enName);
		PrepareSQL psql = new PrepareSQL();
		psql.append("select enName,chName from tab_excel_syn_accounts where enName = '"
				+ enName + "'");
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 获取city map
	 * @return
	 */
	public Map<String, String> getCityMap()
	{
		String strSQL = "select city_name, city_id from tab_city order by city_id";
		Map<String, String> map = DBOperation.getMap(new PrepareSQL(strSQL).getSQL());
		return map;
	}
}
