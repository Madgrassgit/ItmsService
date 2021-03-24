
package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.commons.util.TimeUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dispatch.obj.DeleteHqosChecker;
import com.linkage.itms.dispatch.obj.OpenHqosChecker;
import com.linkage.itms.dispatch.obj.UpdateHqosChecker;

/**
 * @author guxl3 (Ailk No.)
 * @version 1.0
 * @since 2021年2月2日
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class HqosUserDeviceDAO
{

	public Map<String, String> queryUserByNetAccount(String netAccount)
	{
		Map<String, String> map=new HashMap<String, String>(7);
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.user_id,a.username, b.device_id,b.username loid,b.city_id ,b.binddate ");
		psql.append("from hgwcust_serv_info a, tab_hgwcustomer b ");
		psql.append("where a.user_id = b.user_id and a.serv_type_id=10 and ");
		psql.append("a.username=?  order by b.binddate desc  nulls last ");
		psql.setString(1, netAccount);
		ArrayList<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		if (list!=null && !list.isEmpty())
		{
			String deviceId="";
			for (int i = 0; i < list.size(); i++)
			{
				map=list.get(i);
				deviceId=StringUtil.getStringValue("device_id");
				if (!deviceId.isEmpty())
				{
					break;
				}
			}
			if (deviceId.isEmpty())
			{
				map=list.get(0);
			}
		}
		return map;
	}
	
	
	public Map<String, String> queryUserByLoid(String loid)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select distinct(a.user_id),a.device_id,a.binddate,a.city_id from  ");
		psql.append("tab_hgwcustomer a left join hgwcust_serv_info b ");
		psql.append("on a.user_id = b.user_id ");
		psql.append("where a.username=? and (user_state='1' or user_state='2') order by a.binddate desc");
		psql.setString(1, loid);
		return DBOperation.getRecord(psql.getSQL());
	}
	
	public Map<String, String> queryNetServUserByLoid(String loid)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select distinct (a.user_id),a.device_id,a.binddate,a.city_id,b.username ");
		psql.append("from  tab_hgwcustomer a  ");
		psql.append("inner join hgwcust_serv_info b  on a.user_id = b.user_id ");
		psql.append("where a.username=? ");
		psql.append("and (a.user_state='1' or a.user_state='2') ");
		psql.append("and b.serv_type_id=10 order by a.binddate desc ");
		psql.setString(1, loid);
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
	public Map<String, String> queryUserInfoByDevSn(String devSn)
	{
		
		String oui="";
		String sn="";
		String str="-";
		if (devSn.indexOf(str)>-1) {
			oui = devSn.split(str)[0];
			sn = devSn.split(str)[1];
		}else {
			sn=devSn;
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.device_id,b.user_id,b.username loid,b.city_id ");
		psql.append("from tab_gw_device a ");
		psql.append("left join tab_hgwcustomer b on a.device_id = b.device_id  ");
		psql.append("where a.device_status = 1  and a.device_serialnumber like '%"+sn+"%' ");
		if(sn.length()>Global.DEVSNLENTH){
			String devSubSn = sn.substring(sn.length() - 6, sn.length());
			psql.append(" and a.dev_sub_sn = '"+devSubSn+"' ");
		}
		if (!StringUtil.IsEmpty(oui)) {
			psql.append(" and a.oui='"+oui+"' ");
		}
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
	public Map<String, String> queryNetServUserInfoByDevSn(String devSn)
	{
		String oui="";
		String sn="";
		String str="-";
		if (devSn.indexOf(str)>-1) {
			oui = devSn.split(str)[0];
			sn = devSn.split(str)[1];
		}else {
			sn=devSn;
		}
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.device_id,b.user_id,b.username loid,b.city_id,b.binddate,c.username ");
		psql.append("from tab_gw_device a  ");
		psql.append("inner join tab_hgwcustomer b on a.device_id = b.device_id   ");
		psql.append("inner join hgwcust_serv_info c on b.user_id=c.user_id and c.serv_type_id=10 ");
		psql.append("where a.device_status = 1  and a.device_serialnumber like '%"+sn+"%' ");
		if(sn.length()>Global.DEVSNLENTH){
			String devSubSn = sn.substring(sn.length() - 6, sn.length());
			psql.append(" and a.dev_sub_sn = '"+devSubSn+"' ");
		}
		if (!StringUtil.IsEmpty(oui)) {
			psql.append(" and a.oui='"+oui+"' ");
		}
		psql.append("order by b.binddate desc ");
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
	public Map<String, String> queryDevSn(String deviceId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.username,b.cpe_mac,b.device_name from tab_hgwcustomer a,tab_gw_device  b ");
		psql.append("where a.device_id=b.device_id and a.device_id=? ");
		psql.setString(1, deviceId);
		return DBOperation.getRecord(psql.getSQL());
	}
	
	public Map<String, String> queryVersion(String deviceId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.cpe_mac,               ");
		psql.append("   a.device_name,               ");
		psql.append("   d.hardwareversion,           ");
		psql.append("   b.vendor_add,                ");
		psql.append("   c.device_model,              ");
		psql.append("   d.softwareversion,           ");
		psql.append("   d.rela_dev_type_id,               ");
		psql.append("   e.lan_num,                   ");
		psql.append("   e.wlan_num                   ");
		psql.append("   from tab_gw_device a         ");
		psql.append(" inner join tab_vendor b on a.vendor_id=b.vendor_id ");
		psql.append(" inner join gw_device_model c on a.device_model_id=c.device_model_id ");
		psql.append(" inner join tab_devicetype_info d on a.devicetype_id=d.devicetype_id ");
		psql.append(" left join tab_bss_dev_port e on d.spec_id=e.id ");
		psql.append(" where a.device_id=? ");
		psql.setString(1, deviceId);
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
	public  Map<String, String> queryNetStatus(String deviceId,String userName)
	{
		PrepareSQL sql = new PrepareSQL();
		sql.append("select a.device_id,e.vendor_add,f.device_model,b.username loid,c.username,d.iscloudnet ");
		sql.append("from tab_gw_device a ");
		sql.append("inner join tab_vendor e on a.vendor_id=e.vendor_id ");
		sql.append("inner join gw_device_model f on a.device_model_id=f.device_model_id ");
		sql.append("inner join tab_hgwcustomer b on a.device_id=b.device_id  ");
		sql.append("left  join hgwcust_serv_info c on b.user_id=c.user_id and c.serv_type_id=10 and c.serv_status=1 ");
		sql.append("left  join tab_device_version_attribute d on a.devicetype_id=d.devicetype_id ");
		sql.append("where a.device_id=? ");
		if (!StringUtil.IsEmpty(userName))
		{
			sql.append("and  c.username='"+userName+"' ");
		}
		sql.setString(1, deviceId);
		return DBOperation.getRecord(sql.getSQL());
	}
	
	public  Map<String, String> hQosTwoStatus(String userId)
	{
		PrepareSQL sql = new PrepareSQL();
		sql.append("select a.user_id,a.username from hgwcust_serv_info a,tab_hgwcustomer b ");
		sql.append("where a.user_id=b.user_id and b.user_id=? and a.serv_type_id=46 and  a.serv_status=1 ");
		sql.setLong(1, StringUtil.getLongValue(userId));
		return DBOperation.getRecord(sql.getSQL());
	}
	
	
	public  Map<String, String> hQosTwoCfgQuery(String userId)
	{
		PrepareSQL sql = new PrepareSQL();
		sql.append("select  username, open_status, dealdate, updatetime from hgwcust_serv_info   ");
		sql.append("where   serv_status=1 and serv_type_id =46 and  user_id=? ");
		sql.setLong(1, StringUtil.getLongValue(userId));
		return DBOperation.getRecord(sql.getSQL());
	}
	
	/**
	 * loid 是否存在
	 */
	public Map<String, String> isLoidExists(String loid)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select user_id from tab_hgwcustomer ");
		psql.append(" where (user_state='1' or user_state='2') and username=? ");
		psql.setString(1, loid);
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
	public boolean servUserIsExistsByServName(long userId, int servTypeId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select username from hgwcust_serv_info where user_id=? and serv_type_id=? ");
		psql.append(" and serv_status=1");
		psql.setLong(1, userId);
		psql.setInt(2, servTypeId);
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		if (null != map && map.size() > 0)
		{
			return true;
		}
		return false;
	}
	
	
	
	
	public String saveServUserSqlNew(OpenHqosChecker obj, long userId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append(" insert into hgwcust_serv_info (user_id,serv_type_id,username,passwd,wan_type,serv_status,vpiid,vciid,vlanid,");
		psql.append(" bind_port,open_status, dealdate,opendate,updatetime,serv_num) values (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)");
		psql.setLong(1, userId);
		psql.setInt(2, StringUtil.getIntegerValue(obj.getServTypeId()));
		psql.setString(3, obj.getHqsName());
		psql.setString(4, obj.getHqsPassword());
		psql.setInt(5, StringUtil.getIntegerValue(obj.getWanType()));
		// 业务状态，开户
		psql.setInt(6, 1);
		psql.setString(7, "");
		psql.setInt(8, 0);
		psql.setString(9, obj.getVlanId());
		psql.setString(10, "");
		// 开通状态置为未开通
		psql.setInt(11, 0);
		psql.setLong(12, TimeUtil.GetCalendar(obj.getDealDate()).getTimeInMillis() / 1000);
		// 开户时间
		psql.setLong(13, System.currentTimeMillis() / 1000);
		psql.setLong(14, System.currentTimeMillis() / 1000);
		psql.setInt(15, 1);
		return psql.getSQL();
	}
	
	public String getSaveHqsServParam(OpenHqosChecker obj, long userId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append(" insert into tab_hqs_serv_param (user_id, username, serv_type_id, ipforwardlist, qos_p_value,business_id) ");
		psql.append(" values (?,?,?,?,?,? ) ");
		psql.setLong(1, userId);
		psql.setString(2, obj.getHqsName());
		psql.setInt(3, StringUtil.getIntegerValue(obj.getServTypeId()));
		psql.setString(4, obj.getIpForwardList());
		psql.setString(5, obj.getQosValue());
		psql.setString(6, obj.getBusinessId());
		return psql.getSQL();
	}
	
	
	public String updateServUserSqlNew(OpenHqosChecker obj, long userId)
	{
		PrepareSQL psql = new PrepareSQL();
		String nullString="null";
		
		psql.append("update hgwcust_serv_info set ");
		
		if (!StringUtil.IsEmpty(obj.getHqsName()) && !nullString.equalsIgnoreCase(obj.getHqsName()))
		{
			psql.append("username='" + obj.getHqsName() + "',");
		}
		if (!StringUtil.IsEmpty(obj.getHqsPassword()) && !nullString.equalsIgnoreCase(obj.getHqsPassword()))
		{
			psql.append("passwd='" + obj.getHqsPassword() + "',");
		}
		if (!StringUtil.IsEmpty(obj.getWanType()))
		{
			psql.append("wan_type=" + obj.getWanType() + ",");
		}
		if (!StringUtil.IsEmpty(obj.getVlanId()))
		{
			psql.append("vlanid='" + obj.getVlanId() + "',");
		}
		psql.append(" open_status=?,dealdate=?,updatetime=? where user_id=? and serv_type_id=? and serv_status=1");
		// 置为未开通
		psql.setInt(1, 0);
		psql.setLong(2, TimeUtil.GetCalendar(obj.getDealDate()).getTimeInMillis() / 1000);
		psql.setLong(3, System.currentTimeMillis() / 1000);
		psql.setLong(4, userId);
		psql.setInt(5, StringUtil.getIntegerValue(obj.getServTypeId()));
		return psql.getSQL();
	}
	
	public String updateServUserSqlNew(UpdateHqosChecker obj, long userId)
	{
		PrepareSQL psql = new PrepareSQL();
		String nullString="null";
		
		psql.append("update hgwcust_serv_info set ");
		
		if (!StringUtil.IsEmpty(obj.getHqsName()) && !nullString.equalsIgnoreCase(obj.getHqsName()))
		{
			psql.append("username='" + obj.getHqsName() + "',");
		}
		if (!StringUtil.IsEmpty(obj.getHqsPassword()) && !nullString.equalsIgnoreCase(obj.getHqsPassword()))
		{
			psql.append("passwd='" + obj.getHqsPassword() + "',");
		}
		if (!StringUtil.IsEmpty(obj.getWanType()))
		{
			psql.append("wan_type=" + obj.getWanType() + ",");
		}
		if (!StringUtil.IsEmpty(obj.getVlanId()))
		{
			psql.append("vlanid='" + obj.getVlanId() + "',");
		}
		psql.append(" open_status=?,dealdate=?,updatetime=? where user_id=? and serv_type_id=? and serv_status=1");
		// 置为未开通
		psql.setInt(1, 0);
		psql.setLong(2, TimeUtil.GetCalendar(obj.getDealDate()).getTimeInMillis() / 1000);
		psql.setLong(3, System.currentTimeMillis() / 1000);
		psql.setLong(4, userId);
		psql.setInt(5, StringUtil.getIntegerValue(obj.getServTypeId()));
		return psql.getSQL();
	}
	
	
	public String updateHqsServParam(OpenHqosChecker obj, long userId)
	{
		String tabName = "tab_hqs_serv_param";
		PrepareSQL psql = new PrepareSQL("update " + tabName + " set serv_type_id = " + obj.getServTypeId());
		if (!StringUtil.IsEmpty(obj.getHqsName()))
		{
			psql.append(", username='" + obj.getHqsName() + "'");
		}
		if (!StringUtil.IsEmpty(obj.getIpForwardList()))
		{
			psql.append(", ipforwardlist='" + obj.getIpForwardList() + "'");
		}
		if (!StringUtil.IsEmpty(obj.getQosValue()))
		{
			psql.append(", qos_p_value='" + obj.getQosValue() + "'");
		}
		if (!StringUtil.IsEmpty(obj.getBusinessId()))
		{
			psql.append(", business_id='" + obj.getBusinessId() + "'");
		}
		psql.append(" where user_id =" + userId );
		return psql.getSQL();
	}
	
	
	public String updateHqsServParam(UpdateHqosChecker obj, long userId)
	{
		String tabName = "tab_hqs_serv_param";
		PrepareSQL psql = new PrepareSQL("update " + tabName + " set serv_type_id = " + obj.getServTypeId());
		if (!StringUtil.IsEmpty(obj.getHqsName()))
		{
			psql.append(", username='" + obj.getHqsName() + "'");
		}
		if (!StringUtil.IsEmpty(obj.getIpForwardList()))
		{
			psql.append(", ipforwardlist='" + obj.getIpForwardList() + "'");
		}
		if (!StringUtil.IsEmpty(obj.getQosValue()))
		{
			psql.append(", qos_p_value='" + obj.getQosValue() + "'");
		}
		psql.append(" where user_id =" + userId );
		return psql.getSQL();
	}
	
	
	/**
	 * 根据用户ID(开户状态)获取是否有设备存在,存在返回设备对象,不存在返回null
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, String> checkDevice(long userId)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.device_id,a.gather_id,a.devicetype_id,a.oui,a.device_serialnumber,a.city_id ");
		psql.append(" from tab_gw_device a,tab_hgwcustomer b where a.device_id=b.device_id and b.user_id=? and user_state='1'");
		psql.setLong(1, userId);
		return DBOperation.getRecord(psql.getSQL());
	}
	
	
	public String stopServiceSql(long userId, int servTypeId)
	{
		PrepareSQL psql = new PrepareSQL("delete from hgwcust_serv_info where user_id=? and serv_type_id=? ");
		psql.setLong(1, userId);
		psql.setInt(2, servTypeId);
		return psql.getSQL();
	}
	
	public String getRemoveHqsServParam(long userId, int servTypeId)
	{
		PrepareSQL psql = new PrepareSQL("delete from  tab_hqs_serv_param  where  user_id=? and serv_type_id=? ");
		psql.setLong(1, userId);
		psql.setLong(2, servTypeId);
		return psql.getSQL();
	}
	
	
	
	
	
	public String prossBssSheet(OpenHqosChecker obj,int code) {
		String str="bss工单 ";
		PrepareSQL psql = new PrepareSQL();
		psql.append(" insert into  tab_bss_sheet (bss_sheet_id,username,product_spec_id,city_id,order_id, ");
		psql.append("type,order_type,receive_date,remark,servUsername, sheet_context,returnt_context,result,gw_type) ");
		psql.append(" values(?,?,?,?,?,   ?,?,?,?,?,  ?,?,?,?)");
		
		String bssSheetId = System.currentTimeMillis() / 1000+ StringUtil.getStringValue(Math.round(Math.random() * 1000000L))
				+ "-" + obj.getCmdId();
		psql.setString(1, bssSheetId);
		psql.setString(2, obj.getLoid());
		psql.setInt(3, StringUtil.getIntegerValue(obj.getServTypeId()));
		if (StringUtil.IsEmpty(obj.getCityId()))
		{
			psql.setString(4, "00");
		}
		else
		{
			psql.setString(4, obj.getCityId());
		}
		psql.setString(5, obj.getCmdId());
		psql.setString(6, obj.getOperateId());
		psql.setInt(7, 0);
		psql.setLong(8, System.currentTimeMillis() / 1000);
		psql.setString(9, str.trim());
		psql.setString(10, "");
		psql.setString(11, obj.getCallXml());
		psql.setString(12, obj.getResultDesc());
		// result:0,成功 1，失败
		if (0==code)
		{
			psql.setInt(13, 0);
		}
		else
		{
			psql.setInt(13, 1);
		}
		psql.setInt(14, 1);
		return psql.getSQL();
	}
	
	public String prossBssSheet(UpdateHqosChecker obj,int code) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("insert into tab_bss_sheet  (bss_sheet_id,username,product_spec_id,city_id,order_id, ");
		psql.append("type,order_type,receive_date,remark,servUsername, sheet_context,returnt_context,result,gw_type) ");
		psql.append("values(?,?,?,?,?,   ?,?,?,?,?,  ?,?,?,?)");
		
		String bssSheetId = System.currentTimeMillis() / 1000+ StringUtil.getStringValue(Math.round(Math.random() * 1000000L))
		+ "-" + obj.getCmdId();
		psql.setString(1, bssSheetId);
		psql.setString(2, obj.getLoid());
		psql.setInt(3, StringUtil.getIntegerValue(obj.getServTypeId()));
		if (StringUtil.IsEmpty(obj.getCityId()))
		{
			psql.setString(4, "00");
		}
		else
		{
			psql.setString(4, obj.getCityId());
		}
		psql.setString(5, obj.getCmdId());
		psql.setString(6, obj.getOperateId());
		psql.setInt(7, 0);
		psql.setLong(8, System.currentTimeMillis() / 1000);
		psql.setString(9, "bss工单");
		psql.setString(10, "");
		psql.setString(11, obj.getCallXml());
		psql.setString(12, obj.getResultDesc());
		// result:0,成功 1，失败
		if (0==code)
		{
			psql.setInt(13, 0);
		}
		else
		{
			psql.setInt(13, 1);
		}
		psql.setInt(14, 1);
		return psql.getSQL();
	}
	
	public String prossBssSheet(DeleteHqosChecker obj,int code) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("insert  into tab_bss_sheet (bss_sheet_id,username,product_spec_id,city_id,order_id, ");
		psql.append(" type,order_type, receive_date,remark,servUsername, sheet_context,returnt_context,result,gw_type) ");
		psql.append("values(?,?,?,?,?,   ?,?,?,?,?,  ?,?,?,?)");
		
		String bssSheetId = System.currentTimeMillis() / 1000+ StringUtil.getStringValue(Math.round(Math.random() * 1000000L))
		+ "-" + obj.getCmdId();
		psql.setString(1, bssSheetId);
		psql.setString(2, obj.getLoid());
		psql.setInt(3, StringUtil.getIntegerValue(obj.getServTypeId()));
		if (StringUtil.IsEmpty(obj.getCityId()))
		{
			psql.setString(4, "00");
		}
		else
		{
			psql.setString(4, obj.getCityId());
		}
		psql.setString(5, obj.getCmdId());
		psql.setString(6, obj.getOperateId());
		psql.setInt(7, 0);
		psql.setLong(8, System.currentTimeMillis() / 1000);
		psql.setString(9, "bss工单");
		psql.setString(10, "");
		psql.setString(11, obj.getCallXml());
		psql.setString(12, obj.getResultDesc());
		// result:0,成功 1，失败
		if (0==code)
		{
			psql.setInt(13, 0);
		}
		else
		{
			psql.setInt(13, 1);
		}
		psql.setInt(14, 1);
		return psql.getSQL();
	}
	
}
