package com.linkage.itms.itv.dao;
import java.util.ArrayList;
import java.util.HashMap;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;


/**
 * HBDX-REQ-20170330-XuPan-001(湖北ITMS+机顶盒即插即用零配置接口)
 * @author wanghong
 *
 */
public class GetItvMacHBDAO
{
	/**
	 * 根据设备mac地址获取设备id
	 * @param stbMac
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getDevByMac(String stbMac)
	{
		PrepareSQL psql = new PrepareSQL();
		if (DBUtil.GetDB("xml-test") == 1)
		{
			psql.append("select a.device_id,b.username from tab_gw_device_stbmac a left join tab_hgwcustomer b on(a.device_id=b.device_id)  ");
			psql.append("where a.stb_mac=? ");
			psql.append("order by a.update_time desc");
			psql.setString(1,stbMac);
		}
		else
		{
			psql.append("select a.device_id,b.username from tab_gw_device_stbmac a left join tab_hgwcustomer b on(a.device_id=b.device_id) ");
			psql.append("where a.stb_mac=? ");
			psql.append("order by a.update_time desc");
			psql.setString(1,stbMac);
		}
		
		return DBOperation.getRecords(psql.getSQL());
	}
}
