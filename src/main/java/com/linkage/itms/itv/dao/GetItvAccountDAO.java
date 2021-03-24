package com.linkage.itms.itv.dao;
import java.util.HashMap;
import java.util.List;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.Global;


/**
 * @author zhangsm
 * @version 1.0
 * @since 2011-9-23 上午10:19:45
 * @category com.linkage.itms.itv.dao<br>
 * @copyright 亚信联创 网管产品部
 */
public class GetItvAccountDAO
{
	
	public List<HashMap<String,String>> getDevByStbMac(String stbMac)
	{
		String strSQL = "select top 1 a.device_id from tab_gw_device_stbmac a where a.stb_mac='" +stbMac + "' order by a.update_time desc";
		if (DBUtil.GetDB("xml-macdb") == 1)
		{
			strSQL = "select * from (select a.device_id from tab_gw_device_stbmac a where a.stb_mac='" +stbMac + "' order by a.update_time desc) where rownum<2";
		}
		
		// mysql db
		if (3 == DBUtil.GetDB()) {
			strSQL = "select b.device_id from (select a.device_id from tab_gw_device_stbmac a where a.stb_mac='" +stbMac + "' order by a.update_time desc) b limit 1";
		}
		
		if("xj_dx".equals(Global.G_instArea) || "jl_dx".equals(Global.G_instArea)){
			strSQL = "select top 1 a.device_id,a.lan_port from tab_gw_device_stbmac a where a.stb_mac='" +stbMac + "' order by a.update_time desc";
			if (DBUtil.GetDB("xml-macdb") == 1)
			{
				strSQL = "select a.device_id,a.lan_port from tab_gw_device_stbmac a where a.stb_mac='" +stbMac + "' order by a.update_time desc";
			}
			// mysql db
			if (3 == DBUtil.GetDB()) {
				strSQL = "select a.device_id,a.lan_port from tab_gw_device_stbmac a where a.stb_mac='" +stbMac + "' order by a.update_time desc";
			}
		}
		
		PrepareSQL psql = new PrepareSQL(strSQL);
		
		return DBOperation.getRecords(psql.getSQL(),"xml-macdb");
	}
	
	public List<HashMap<String,String>> getAccountByDevId(String devId)
	{
		String strSQL = "";
		if("xj_dx".equals(Global.G_instArea) || "jl_dx".equals(Global.G_instArea))
		{
			strSQL = "select b.username, b.real_bind_port from tab_hgwcustomer a,hgwcust_serv_info b where a.user_id=b.user_id and b.serv_type_id=11 and a.device_id='" + devId + "'";
			
		}
		else
		{
			strSQL = "select b.username from tab_hgwcustomer a,hgwcust_serv_info b where a.user_id=b.user_id and b.serv_type_id=11 and a.device_id='" + devId + "'";
		}
		
		PrepareSQL psql = new PrepareSQL(strSQL);
		return DBOperation.getRecords(psql.getSQL());
	}
	
}
