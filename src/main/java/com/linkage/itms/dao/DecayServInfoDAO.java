
package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;

/**
 * @author xiangzl (Ailk No.)
 * @version 1.0
 * @since 2013-12-10
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DecayServInfoDAO
{

	private static Logger logger = LoggerFactory.getLogger(DecayServInfoDAO.class);

	public Map<String, String> queryDeviceInfo(String loid,String gwtype)
	{
		logger.debug("queryDeviceInfo({})", loid);
		String tabName = "tab_hgwcustomer a,tab_gw_device b ";
		if("2".equals(gwtype))
		{
			tabName = "tab_egwcustomer a,tab_gw_device b ";
		}
		String sql = "select a.user_id,a.access_style_id,a.city_id,a.username,a.device_serialnumber,b.device_type,b.device_id from " + tabName + " where a.device_id = b.device_id and a.username = '"+ loid +"'";
		PrepareSQL psql = new PrepareSQL(sql);
		return DBOperation.getRecord(psql.getSQL());
	}
	
	public ArrayList<HashMap<String,String>> queryServResult(String loid,String gwType)
	{
		logger.debug("queryServResult({})" , loid);
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		String tabName = " tab_hgwcustomer a,hgwcust_serv_info b left join tab_voip_serv_param c on b.user_id = c.user_id ";
		if("2".equals(gwType))
		{
			tabName = " tab_egwcustomer a,egwcust_serv_info b left join tab_egw_voip_serv_param c on b.user_id = c.user_id ";
		}
		
		String sql = "select b.dealdate,b.serv_type_id,b.open_status,b.username,c.voip_phone from " + tabName + " where a.user_id = b.user_id and a.username = '"+loid+ "'";
		PrepareSQL psql = new PrepareSQL(sql);
		ArrayList<HashMap<String,String>> data = DBOperation.getRecords(psql.getSQL());
		HashMap<String,String> voipMap = null;
		StringBuffer voipPhone = new StringBuffer();
		for(HashMap<String,String> map : data)
		{
			if (null != map)
			{
				if ("14".equals(StringUtil.getStringValue(map, "serv_type_id", "")))
				{
					voipPhone.append(StringUtil.getStringValue(map, "voip_phone"))
							.append("|");
					voipMap = map;
				}
				else
				{
					list.add(map);
				}
			}
				
		}
		if(null != voipMap)
		{
			voipMap.put("username", voipPhone.substring(0, voipPhone.length()-1).toString());
			list.add(voipMap);
		}
		return list;
		
	}

	public void queryDevWireInfo(String deviceId)
	{
		// TODO Auto-generated method stub
		
	}

	public void updateTestResult(String userId)
	{
		String sql = "update tab_customer_ftth set test_stat = 2,last_modified = " + new DateTimeUtil().getLongTime() + " where user_id = "+userId;
		PrepareSQL psql = new PrepareSQL(sql);
		DBOperation.executeUpdate(psql.getSQL());
		
	}
//	public Map<String,String> queryPONInfo(String deviceId)//不被使用
//	{
//		String strSQL = "select * from " + getTabName(deviceId, "gw_wan_wireinfo_epon") +  "  where device_id = ?";
//		PrepareSQL psql = new PrepareSQL(strSQL);
//		psql.setString(1, deviceId);
//		
//		return DBOperation.getRecord(psql.getSQL());
//	}
	
	public static String getTabName(String gw_type, String tabName)
	{
		return tabName + getSuffixName(gw_type);
	}
	
	/**  获取后缀名称 */
	public static String getSuffixName (String gw_type){
		if("1".equals(gw_type)){
			return "";
		} else if("2".equals(gw_type)){
			return "_bbms";
		} else if("3".equals(gw_type)) {
			return "_stb";
		} else {
			return "";
		}
	}
}
