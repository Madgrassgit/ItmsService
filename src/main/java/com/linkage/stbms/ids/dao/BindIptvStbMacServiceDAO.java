package com.linkage.stbms.ids.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.stbms.ids.util.CommonUtil;


public class BindIptvStbMacServiceDAO {
	
	private static final Logger logger = LoggerFactory.getLogger(BindIptvStbMacServiceDAO.class);
	
	
	/**
	 * 根据机顶盒MAC或业务账号或者机顶盒序列号检索，并确认业务账号与机顶盒是否存在绑定关系
	 * @param devMac || servAccount || devSN
	 * @return map<String,String>
	 */
	public Map<String, String> getSearchInfoAndDevInfo(String searchType,String searchInfo) {
			
		logger.debug("BindIptvStbMacServiceDAO==>getSearchInfoAndDevInfo({},{})",
				new Object[] { searchType,searchInfo });
		
		String type = "";
		if("1".equals(searchType)){
			//业务账号
			type = "serv_account";
		}else if("2".equals(searchType)){
			//机顶盒mac
			type = "cpe_mac";
		}else if("3".equals(searchType)){
			//机顶盒序列号 
			type = "device_serialnumber";
		}
		PrepareSQL psql = new PrepareSQL();
		psql.append("select device_id, device_serialnumber, customer_id, serv_account, cpe_allocatedstatus ");
		psql.append("  from "+CommonUtil.addPrefix("tab_gw_device"));
		psql.append(" where 1=1 ");
		psql.append("   and " + type + " = '"+searchInfo+"'");
		 
		type = null;
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
}
