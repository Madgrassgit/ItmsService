package com.linkage.itms.rms.dao;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
		
public class InquiryConfigDao
{
	public static final Logger logger = LoggerFactory.getLogger(QueryKpiDao.class);
	
    /**
            * 根据参数查设备属地和设备SN号
            * @author cczhong
    		* @param userInfoType
    		* @param userInfo
    		* @return
     */
	public List<HashMap<String, String>> inquiryConfig(int userInfoType,String userInfo)
	{
		 logger.warn("InquiryConfigDao->inquiryConfig");
		List<HashMap<String, String>> resultList=null;
		StringBuffer sql = new StringBuffer();
		//查询city_id和device_serialnumber
		//宽带账号
		if(userInfoType==1){
			sql.append("select c.device_serialnumber,a.city_id,a.user_id");
			sql.append(" from tab_hgwcustomer a,hgwcust_serv_info b,tab_gw_device c");
			sql.append(" where a.user_id=b.user_id and a.device_id=c.device_id and b.serv_type_id = 10 and b.username="+"'"+userInfo+"'");
		}
		//LOID(逻辑SN号)
		if(userInfoType==2){
			sql.append("select a.city_id，b.device_serialnumber,a.user_id");
			sql.append(" from tab_hgwcustomer a,tab_gw_device b");
			sql.append(" where a.device_id=b.device_id and a.username ="+"'"+userInfo+"'");
		}
		//电话号码
        if(userInfoType==4){
        	sql.append("select c.device_serialnumber,a.city_id,a.user_id ");
        	sql.append(" from tab_hgwcustomer a,tab_voip_serv_param b,tab_gw_device c");
        	sql.append(" where a.user_id=b.user_id and a.device_id=c.device_id and b.voip_phone ="+"'"+userInfo+"'");
		}
        //设备SN号
		if(userInfoType==6){
			sql.append("select a.city_id，b.device_serialnumber,a.user_id ");
			sql.append(" from tab_hgwcustomer a,tab_gw_device b");
			sql.append(" where a.device_id=b.device_id and b.device_serialnumber like '%"+userInfo+"'");
		}                 
		PrepareSQL psql = new PrepareSQL(sql.toString());
		resultList = DBOperation.getRecords(psql.getSQL());
		return resultList;
	}
}

	