package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.DateTimeUtil;

public class XinJiangFTTHSheetDao {

	private static Logger logger = LoggerFactory.getLogger(XinJiangFTTHSheetDao.class);
	
	
	/**
	 * 新疆综调查询接口
	 * 
	 * 
	 * @param userId  FTTH逻辑标识
	 * @return
	 */
	public List<Map<String,String>> getVOIPAndIPTVBssSheetServInfo(String userId, String gw_type){
		
		logger.debug("getVOIPAndIPTVBssSheetServInfo({})", new Object[] {userId});
		
		Map<String, String> servTypeMap = null;
		List<Map<String,String>>  resList = new ArrayList<Map<String,String>>();
		Map<String, String> resmap = null;
		
		/**
		 * 为了减轻数据库的压力，下面的筛选条件被注释，先查出所有，然后筛选逻辑Java代码里做了实现
		 */
		PrepareSQL psql = new PrepareSQL();
		psql.append("select b.user_id, b.serv_type_id, b.dealdate, b.open_status, b.serv_num, completedate ");
		if ("2".equals(gw_type)) {
			psql.append("  from egwcust_serv_info b ");
		}else {
			psql.append("  from hgwcust_serv_info b ");
		}
		
		psql.append(" where 1=1 ");
//		psql.append("   and (b.serv_type_id = 11 and b.serv_num > 1)");  // 11 标识IPTV  大于1表示 IPTV二路
//		psql.append("   or b.serv_type_id = 14");  //  14 标识VOIP
		psql.append("   and b.user_id = "+userId);
	
		ServUserDAO servUserDAO = new ServUserDAO();
		servTypeMap = servUserDAO.getServType();
		
		List<HashMap<String,String>> list = DBOperation.getRecords(psql.getSQL());
		
		for(HashMap<String,String> rs : list)
		{
			resmap = new HashMap<String, String>();
			resmap.put("user_id", rs.get("user_id"));
			String serv_type_id = rs.get("serv_type_id");
			resmap.put("serv_type_id", serv_type_id);
			String tmp = "-";
			if (false == StringUtil.IsEmpty(serv_type_id)){
				tmp = servTypeMap.get(serv_type_id);
			}
			resmap.put("serv_type", tmp);
			
			// 将dealdate转换成时间
			try{
				long dealdate = StringUtil.getLongValue(rs.get("dealdate"));
				DateTimeUtil dt = new DateTimeUtil(dealdate * 1000);
				resmap.put("dealdate", dt.getLongDate());
			}catch (NumberFormatException e){
				resmap.put("dealdate", "");
			}catch (Exception e){
				resmap.put("dealdate", "");
			}
			
			resmap.put("open_status", rs.get("open_status"));
			resmap.put("serv_num", rs.get("serv_num"));
			
			resList.add(resmap);
		}
		return resList;
	}
	
}
