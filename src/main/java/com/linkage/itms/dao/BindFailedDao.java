package com.linkage.itms.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.Global;
		
public class BindFailedDao
{
	private static Logger logger = LoggerFactory.getLogger(BindFailedDao.class);
	/**
	 * 根据用户的业务账号查询用户信息
	 * 
	 * @param userInfoType:用户信息类型
	 *            userInfo:业务号码
	 * @author cczhong
	 * @date 2010-6-22
	 * @return Map<String,String>
	 */
	public Map<String, String> queryUserInfo(int userInfoType, String userInfo) {
		logger.debug("queryUserInfo({})", userInfo);
		Map<String, String> map=null;
		//如果是GBMS
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		//如果是BBMS
		if("BBMS".equals(Global.SYSTEM_NAME)){
			table_customer = "tab_egwcustomer";
			table_serv_info = "egwcust_serv_info";
		}

		PrepareSQL psql = new PrepareSQL();
		
		psql.append("select a.user_id,a.username,a.device_id");
		//宽带账号
		if(userInfoType==1){
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b");
			psql.append(" where a.user_id=b.user_id and b.serv_status=1 and b.serv_type_id=10");
			psql.append(" and b.username='" + userInfo + "'");
			psql.append(" order by a.updatetime desc");
		}
		//逻辑SN
		if(userInfoType==2){
			psql.append(" from " + table_customer + " a where a.user_state = '1'");
			psql.append(" and a.username = '" + userInfo + "'");
		}
		map=DBOperation.getRecord(psql.getSQL());
		return map;
	}
	/**
	 *  @author cczhong
	 *  @description 根据devicdId查询具体的绑定失败原因
	 */
	public List<HashMap<String, String>> getFailReason(String username){
		PrepareSQL psql = new PrepareSQL();
		psql.append("select fail_desc,start_time from tab_bind_fail where username= ? order by start_time desc");
		psql.setString(1, username);
		return DBOperation.getRecords(psql.getSQL());
		
	}
}

	