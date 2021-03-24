package com.linkage.itms.rms.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.Global;

		
public class QueryKpiDao
{
	/**
	 * @description:查询指定时间内所有用户数，或语音业务开通总数
	 */
	public static final Logger logger = LoggerFactory.getLogger(QueryKpiDao.class);
	public Map<String,String> getTotalUserQuantity(String mark,long dateStart,long dateEnd){
		logger.warn("QueryKpiDao->getTotalUserQuantity");
		PrepareSQL sql = new PrepareSQL();
		//查询HGU用户,VOIP
		sql.append("select sum(census_count) as quantity from kpi_census where census_date<= ? and census_date> ? and type=? ");
		sql.setLong(1,dateEnd);
		sql.setLong(2,dateStart);
		sql.setString(3, mark);
		return DBOperation.getRecord(sql.getSQL());
	}
	/**
	 * @description:查询特定时间最大用户数或语音业务的城市
	 */
	public List<HashMap<String,String>> getMaxUserQuantity(String mark,long dateStart,long dateEnd){
		logger.warn("QueryKpiDao->getMaxUserQuantity");
		PrepareSQL sql = new PrepareSQL();
		//查询HGU用户,VOIP
		sql.append("select a.city_id ,a.quantity from (select city_id,sum(census_count) as quantity from kpi_census where census_date<= ? and census_date> ? and type= ? group by city_id) a");
		sql.append(" where a.quantity=(select max(b.quantity) from (select city_id,sum(census_count) as quantity from kpi_census where census_date<= ? and census_date> ? and type= ? group by city_id) b)");
		sql.setLong(1, dateEnd);
		sql.setLong(2, dateStart);
		sql.setString(3, mark);
		sql.setLong(4, dateEnd);
		sql.setLong(5, dateStart);
		sql.setString(6, mark);
		return DBOperation.getRecords(sql.getSQL());
	}
	/**
	 * @description:查询特定时间最小用户数或语音业务的城市
	 */
	public List<HashMap<String,String>> getMinUserQuantity(String mark,long dateStart,long dateEnd){
		logger.warn("QueryKpiDao->getMinUserQuantity");
		PrepareSQL sql = new PrepareSQL();
		//查询HGU用户,VOIP
		sql.append("select a.city_id ,a.quantity from (select city_id,sum(census_count) as quantity from kpi_census where census_date<= ? and census_date> ? and type= ? group by city_id) a");
		sql.append(" where a.quantity=(select min(b.quantity) from (select city_id,sum(census_count) as quantity from kpi_census where census_date<= ? and census_date> ? and type= ? group by city_id) b)");
		sql.setLong(1, dateEnd);
		sql.setLong(2, dateStart);
		sql.setString(3, mark);
		sql.setLong(4, dateEnd);
		sql.setLong(5, dateStart);
		sql.setString(6, mark);
		return DBOperation.getRecords(sql.getSQL());
	}
	/**
	 * @description:查询一天所有的用户或语音业务开通数据
	 */
	public List<HashMap<String,String>> getDayUserCountList(String mark,long dateStart,long dateEnd){
		logger.warn("QueryKpiDao->getDayUserCountList");
		PrepareSQL sql = new PrepareSQL();
		sql.append("select city_id,sum(census_count) census_count from kpi_census where census_date> ? and census_date<= ? and type= ? group by city_id");
		sql.setLong(1,dateStart);
		sql.setLong(2, dateEnd);
		sql.setString(3, mark);
		return DBOperation.getRecords(sql.getSQL());
	}
}

	