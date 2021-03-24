package com.linkage.itms.itv.dao;
import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;


/**
 * AHDX_ITMS-REQ-20170227YQW-001(通过MCA地址，查询stbind事件中对应的LOID信息)
 * @author wanghong
 *
 */
public class GetItvMacDAO
{
	/**
	 * 根据设备mac地址获取设备id
	 * @param stbMac
	 * @return
	 */
	public Map<String,String> getDevByMac(String stbMac)
	{
		PrepareSQL psql = new PrepareSQL();

		// mysql db
		if (3 == DBUtil.GetDB()) 
		{
			psql.append("select device_id from tab_gw_device_stbmac where stb_mac=? order by update_time desc limit 1");
			psql.setString(1,stbMac);
		}
		else 
		{
			if (DBUtil.GetDB("xml-test") == 1)
			{
				psql.append("select * from ");
				psql.append("(select device_id from tab_gw_device_stbmac ");
				psql.append("where stb_mac=? ");
				psql.append("order by update_time desc) a ");
				psql.append("where rownum<2 ");
				psql.setString(1,stbMac);
			}
			else
			{
				psql.append("select top 1 device_id from tab_gw_device_stbmac ");
				psql.append("where stb_mac=? ");
				psql.append("order by update_time desc");
				psql.setString(1,stbMac);
			}
		}
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * 根据设备id获取用户loid
	 * @param device_id
	 * @return
	 */
	public Map<String,String> getLoid(String device_id)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select username from tab_hgwcustomer ");
		psql.append("where device_id=? ");
		psql.setString(1,device_id);
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
}
