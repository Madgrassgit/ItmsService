package com.linkage.stbms.ids.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.dao.UserDeviceDAO;
import com.linkage.stbms.ids.util.CommonUtil;
import com.linkage.stbms.itv.main.Global;
import com.linkage.system.utils.database.DBUtil;

/**
 * @author zhangshimin(NO.) E-mail:
 * @version 1.0
 * @since 2011-4-19 下午06:20:15
 * @category com.linkage.stbms.ids.dao<br>
 * @copyright asiainfo-linkage.com
 */
public class UserStbInfoDAO extends SuperDAO
{
	private static Logger logger = LoggerFactory.getLogger(UserDeviceDAO.class);

	/**
	 * 根据业务账号获取设备信息
	 * @author zhangsm
	 * @date 2011-04-19
	 * @param devSn
	 * @return Map<String, String>
	 */
//	public List<HashMap<String, String>> getUserStbInfo(String servAccount) {// 没有地方使用
//
//		logger.debug("getUserStbInfo()", servAccount);
//		if (StringUtil.IsEmpty(servAccount)) {
//			logger.warn("servAccount is empty!");
//			return null;
//		}
//		/**
//		 * modify by zhangchy 2012-07-10
//		 * 
//		 * 应席梦男需求单JSDX_ITV_HTW-REQ-20120710-XMN-001要求
//		 * 只返回当前业务账号最近三次的上线记录，将SQL做了如下改动，在SQL中加了 top 3
//		 * 同时排序字段将原来的order by a.device_id 改为 order by g.last_time desc
//		 */
////		StringBuffer sbSQL = new StringBuffer();
//		PrepareSQL psql = new PrepareSQL();
//		if(1==DBUtil.getDbType()){
//			psql.append("select top 3 a.city_id,a.oui,a.device_serialnumber,a.device_id,g.last_time ");
//		}else{
//			psql.append("select b.city_id,b.oui,b.device_serialnumber,b.device_id,b.last_time from ");
//			psql.append("(select a.city_id,a.oui,a.device_serialnumber,a.device_id,g.last_time ");
//		}
//		psql.append(" from tab_gw_device a,gw_devicestatus g ");
//		psql.append("  where a.serv_account='");
//		psql.append(servAccount);
//		psql.append("' and g.device_id=a.device_id");
////		psql.append(" order by a.device_id");
//		psql.append(" order by g.last_time desc ");
//		if(1!=DBUtil.getDbType()){
//			psql.append(") b where rownum<4"); 
//		}
////		logger.warn(sbSQL.toString());
//		return DBOperation.getRecords(psql.getSQL());
//	}
	
	
	/** 如果输入的设备序列号不是完整的，而是末六位的话，此SQL将查询不到设备信息  */
	public String getDeviceId(String devSn,String oui)
	{
		String sql = "select device_id from tab_gw_device where oui='" 
							+ oui + "' and device_serialnumber='" + devSn + "'";
		PrepareSQL psql = new PrepareSQL(sql);
		Map<String,String> map = DBOperation.getRecord(psql.getSQL());
		if(map == null || map.isEmpty())
		{
			return null;
		}
		else
		{
		     return map.get("device_id");
		}
	}
	
	/**
	 * 修改机顶盒密码
	 * @param username
	 * @param password
	 * @return
	 */
//	public int updateCustomerPwd(String username, String password)
//	{
//		logger.debug("StbChangepwdDao.updateCustomerPwd:" + username+"=" + password);
//		String sql = "update " +CommonUtil.addPrefix("tab_customer")+ 
//	" set pppoe_pwd='"+ password + "' where pppoe_user = '"+username+"@itv'";
//		PrepareSQL psql = new PrepareSQL(sql);
//		return DBOperation.executeUpdate(psql.getSQL());
//	}
	
	/**
	 * 
	 * @param searchType
	 * @param searchInfo
	 * @param fromTabDevice 1:单独查询设备表，其他：需要关联用户表（tab_customer）查询
	 * @return
	 */
	public Map<String,String> getDeviceIdStr(String searchType, String searchInfo, String fromTabDevice){
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.device_id,a.oui,a.device_serialnumber,a.customer_id,a.serv_account,a.cpe_allocatedstatus ");
		if ("1".equals(fromTabDevice)) {
			psql.append("  from "+CommonUtil.addPrefix("tab_gw_device")+" a");
			psql.append(" where 1=1 ");
		}else {
			psql.append("  from "+CommonUtil.addPrefix("tab_gw_device")+" a, "+CommonUtil.addPrefix("tab_customer")+" b ");
			psql.append(" where a.customer_id = b.customer_id ");
		}
		
		// searchType=1时searchInfo=业务帐号
		if ("1".equals(searchType)) {
			psql.append("   and a.serv_account = '"+searchInfo+"' ");
		}
		// searchType=2时searchInfo=机顶盒MAC
		else if ("2".equals(searchType)) {
			psql.append("   and a.cpe_mac = '"+searchInfo+"' ");
		}
		// searchType=3时searchInfo=机顶盒序列号
		else if ("3".equals(searchType)) {
			psql.append("   and a.dev_sub_sn = '"+searchInfo.substring(searchInfo.length() - 6)+"' ");
			psql.append("   and a.device_serialnumber like '%"+searchInfo+"' ");
		}
		else // searchType=1时searchInfo=接入帐号
			if ("4".equals(searchType)) {
				psql.append("   and b.pppoe_user = '"+searchInfo+"@itv' ");
			}
		// 江西电信需求  按照最近一次上报时间倒序，也就是取最近一次连接时间的 数据
		if("jx_dx".equals(Global.G_instArea)){
			psql.append(" order by a.cpe_currentupdatetime desc");
		}
		Map<String,String> map = DBOperation.getRecord(psql.getSQL());
		
		if (map == null || map.isEmpty()) {
			return null;
		} else {
			return map;
		}
	}
	
	/**
	 * 查询机顶盒信息，多个根据时间倒叙
	 * @param searchType
	 * @param searchInfo
	 * @param fromTabDevice 
	 * @return
	 */
	public ArrayList<HashMap<String,String>> getDeviceDescTime(String searchType, 
			String searchInfo, String fromTabDevice)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.device_id,a.oui,a.device_serialnumber,a.customer_id,");
		psql.append("a.serv_account,a.loopback_ip,a.cpe_mac,a.device_status,a.vendor_id,");
		psql.append("a.buy_time,a.bind_time,d.softwareversion ,e.device_model ");
		if ("jx_dx".equals(Global.G_instArea)) {
			psql.append(",d.category  ");
		}
		if ("1".equals(fromTabDevice)) {
			psql.append("from "+CommonUtil.addPrefix("tab_gw_device")+" a,");
			psql.append(CommonUtil.addPrefix("gw_devicestatus")+" c,");
			psql.append(CommonUtil.addPrefix("tab_devicetype_info")+" d,");
			psql.append(CommonUtil.addPrefix("gw_device_model")+" e ");
			psql.append("where a.device_id = c.device_id ");
			psql.append("and a.devicetype_id = d.devicetype_id ");
			psql.append("and d.device_model_id = e.device_model_id ");
			
			// searchType=1时searchInfo=业务帐号
			if ("1".equals(searchType)) {
				psql.append("and a.serv_account = '"+searchInfo+"' ");
			}
			// searchType=2时searchInfo=机顶盒MAC
			else if ("2".equals(searchType)) {
				psql.append("and a.cpe_mac = '"+searchInfo+"' ");
			}
			// searchType=3时searchInfo=机顶盒序列号
			else if ("3".equals(searchType)) {
				psql.append("and a.dev_sub_sn ='"+searchInfo.substring(searchInfo.length() - 6)+"' ");
				psql.append("and a.device_serialnumber like '%"+searchInfo+"' ");
			}
		}else {
			psql.append("from "+CommonUtil.addPrefix("tab_gw_device")+" a,");
			psql.append(CommonUtil.addPrefix("tab_customer")+" b,");
			psql.append(CommonUtil.addPrefix("gw_devicestatus")+" c,");
			psql.append(CommonUtil.addPrefix("tab_devicetype_info")+" d,");
			psql.append(CommonUtil.addPrefix("gw_device_model")+" e ");
			psql.append("where a.customer_id = b.customer_id ");
			psql.append("and a.device_id = c.device_id ");
			psql.append("and a.devicetype_id = d.devicetype_id ");
			psql.append("and d.device_model_id = e.device_model_id ");
			
			// searchType=1时searchInfo=业务帐号
			if ("1".equals(searchType)) {
				psql.append("and b.serv_account = '"+searchInfo+"' ");
			}
			// searchType=2时searchInfo=机顶盒MAC
			else if ("2".equals(searchType)) {
				psql.append("and a.cpe_mac = '"+searchInfo+"' ");
			}
			// searchType=3时searchInfo=机顶盒序列号
			else if ("3".equals(searchType)) {
				psql.append("and a.dev_sub_sn ='"+searchInfo.substring(searchInfo.length() - 6)+"' ");
				psql.append("and a.device_serialnumber like '%"+searchInfo+"' ");
			}
		}
		
		psql.append(" order by c.last_time desc");
		ArrayList<HashMap<String,String>> result = DBOperation.getRecords(psql.getSQL());
		
		
		return result;
		
	}
	
	
	/**
	 * 查询机顶盒信息，多个根据时间倒叙
	 * @param searchType
	 * @param searchInfo
	 * @param fromTabDevice 
	 * @return
	 */
	public ArrayList<HashMap<String,String>> checkDevice(String searchInfo)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.append("select b.customer_id from "+CommonUtil.addPrefix("tab_customer")+" b ");
		psql.append(" where b.serv_account = '"+searchInfo+"' ");
		ArrayList<HashMap<String,String>> result = DBOperation.getRecords(psql.getSQL());
		return result;
		
	}
	
	/**
	 * 设备表关联厂商表，型号表，版本表查询设备基本信息
	 * @param device_id
	 * @return
	 */
//	public ArrayList<HashMap<String, String>> getBaseInfo(String device_id) {
//		
//		PrepareSQL psql = new PrepareSQL();
//		psql.append("select a.device_id, a.oui, a.device_serialnumber, a.serv_account, a.loopback_ip, a.cpe_mac, ");
//		psql.append("       b.vendor_name, b.vendor_add, c.device_model, d.softwareversion");
//		psql.append("  from "+CommonUtil.addPrefix("tab_gw_device")+" a, "+CommonUtil.addPrefix("gw_vendor")+" b, "+CommonUtil.addPrefix("gw_device_model")+" c, "+CommonUtil.addPrefix("tab_devicetype_info")+" d");
//		psql.append(" where a.vendor_id = b.vendor_id ");
//		psql.append("   and b.vendor_id = c.vendor_id ");
//		psql.append("   and a.devicetype_id = d.devicetype_id");
//		psql.append("   and c.device_model_id = d.device_model_id ");
//		psql.append("   and a.device_id = '"+device_id+"' ");
//		
//		return DBOperation.getRecords(psql.getSQL());
//	}
	
	
	/**
	 * 
	 * @param devSn
	 * @param oui
	 * @return
	 */
//	public String getDeviceID(String devSn,String oui)
//	{
//		StringBuffer sqlBuffer = new StringBuffer();
//		
//		sqlBuffer.append("select device_id ");
//		if(DBAdapter.getDriverURL("xml-test").contains("oracle")){
//			sqlBuffer .append("  from tab_gw_device") ;
//		}else{
//			sqlBuffer .append("  from tab_gw_device(index i_dev_sub_sn)") ;
//		}
//        sqlBuffer.append(" where 1=1 ")
//                 .append("   and oui = '").append(oui).append("'")
//                 .append("   and device_serialnumber like '%").append(devSn).append("'");
//		if (devSn.length()>5) {
//			sqlBuffer.append("   and dev_sub_sn = '").append(devSn.substring(devSn.length()-6, devSn.length())).append("'");
//		}
//		
//		PrepareSQL psql = new PrepareSQL(sqlBuffer.toString());
//		Map<String,String> map = DBOperation.getRecord(psql.getSQL());
//		if(map == null || map.isEmpty()){
//			return null;
//		}
//		return map.get("device_id");
//	}
	
	/**
	 * 
	 * @param devSn
	 * @param oui
	 * @return
	 */
	public void updateDevieStatus(String devSn,String status)
	{
		String sql="update tab_gw_device set status="+status+" where  device_serialnumber='"+devSn+"'";
		DBOperation.executeUpdate(sql);
	}
	
	/**
	 * 查询当前用户所属EPG分组
	 * @param dto
	 * @return
	 */
	public List<HashMap<String,String>> getCurrentEpg(String stbIp)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("select b.group_name epg_group ,a.city_name epg_name");
		sb.append(" from tab_ip_epggroup a,tab_epg_group b");
		sb.append(" where a.start_ip<='" + CommonUtil.getFillIP(stbIp.trim()));
		sb.append("' and a.end_ip>='"+CommonUtil.getFillIP(stbIp.trim())+"' and a.group_id=b.group_id");
		
		PrepareSQL psql = new PrepareSQL(sb.toString());
		
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 查询设备信息
	 * @param dto
	 * @return
	 */
	public List<HashMap<String,String>> getDeviceInfo(int conditionType,String condition)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("select a.device_id, a.oui, a.device_serialnumber, a.customer_id, a.serv_account, a.city_id, c.last_time, d.vendor_name, d.vendor_add,b.addressing_type, e.device_model, f.softwareversion, f.hardwareversion ");
		sb.append("from tab_gw_device a, tab_customer b, gw_devicestatus c, gw_vendor d, gw_device_model e, tab_devicetype_info f ");
		sb.append(" where a.customer_id = b.customer_id ");
		sb.append(" and a.device_id = c.device_id ");
		sb.append(" and a.vendor_id = d.vendor_id ");
		sb.append(" and d.vendor_id = e.vendor_id ");
		sb.append(" and a.devicetype_id = f.devicetype_id ");
		sb.append(" and e.device_model_id = f.device_model_id ");
		// 业务帐号
		if(1 == conditionType){
			sb.append(" and a.serv_account =  '").append(condition).append("'");
		}
		// 设备序列号
		if(2 == conditionType){
			sb.append(" and a. device_serialnumber like '%").append(condition).append("'");
			sb.append(" and dev_sub_sn ='").append(condition.substring(condition.length()-6, condition.length()));
		}

		PrepareSQL psql = new PrepareSQL(sb.toString());
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	public int insertZeroConfReportRepair(String cityId, String deviceSerialnumber,String workId,String servAccount,String addressType)
	{
		
		String sql="insert into tab_zero_conf_report" +
				"(city_id,work_id,serv_account_new,serv_account_old,oper_type,bss_deal_time,work_status,sn,prod_id,addressing_type) " +
				"values('"+cityId+"','"+
				workId+"','"+
				servAccount+"','"+
				servAccount+
				"',3,"+
				new Date().getTime()/1000+
				",2,'','','"+addressType+"')";
		return DBOperation.executeUpdate(sql);
	}
	
	public int insertZeroConfReportManul(String cityId, String deviceSerialnumber,String workId,String servAccount,String addressType)
	{
		
		String sql = "insert into tab_zero_conf_report(work_id,prod_id,serv_account_new,oper_type,work_status,is_from_bss,city_id,bss_deal_time,addressing_type) " +
				"values('"+workId+"','','"+servAccount+"',0,2,0,'"+cityId+"',"+new DateTimeUtil().getLongTime()+",'"+addressType+"')";

		return DBOperation.executeUpdate(sql);
	}

	/**
	 * 获取机顶盒在线状态
	 * 
	 * @param 设备Id
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return Map<String,String>
	 */
	public Map<String, String> getDevStatus(String device_id) {
		logger.debug("getDevStatus({})", device_id);

		String strSQL = "select b.online_status"
				+ " from stb_tab_gw_device a left join stb_gw_devicestatus b on a.device_id=b.device_id"
				+ " where a.device_id='"
				+ device_id +"'" ;
		logger.info(strSQL);
		return DBOperation.getRecord(strSQL);
	}
	
	/**
	 * 新疆查询机顶盒信息，多个根据时间倒叙
	 * @param searchType
	 * @param searchInfo
	 * @param fromTabDevice 
	 * @return
	 */
	public ArrayList<HashMap<String,String>> getDeviceDescTimeForXJ(String searchType, String searchInfo ){
		
		PrepareSQL psql = new PrepareSQL();
		psql.append(" select a.device_serialnumber, a.loopback_ip, a.cpe_mac, b.vendor_name, d.hardwareversion, d.softwareversion, e.device_model ");
		psql.append(" from "+CommonUtil.addPrefix("tab_gw_device")+" a, " +CommonUtil.addPrefix("tab_vendor")+" b, " +CommonUtil.addPrefix("gw_devicestatus")+" c, "+CommonUtil.addPrefix("tab_devicetype_info")+" d," +CommonUtil.addPrefix("gw_device_model")+" e ");
		psql.append(" where 1=1 and a.device_id = c.device_id and a.vendor_id = b.vendor_id and a.devicetype_id = d.devicetype_id and d.device_model_id = e.device_model_id ");
		
		// searchType=1时searchInfo=业务帐号
		if ("1".equals(searchType)) {
			psql.append("   and a.serv_account = '"+searchInfo+"' ");
		}
		// searchType=2时searchInfo=机顶盒MAC
		else if ("2".equals(searchType)) {
			psql.append("   and a.cpe_mac = '"+searchInfo+"' ");
		}
		// searchType=3时searchInfo=机顶盒序列号
		else if ("3".equals(searchType)) {
			psql.append("   and a.dev_sub_sn = '"+searchInfo.substring(searchInfo.length() - 6)+"' ");
			psql.append("   and a.device_serialnumber like '%"+searchInfo+"' ");
		}
		
		psql.append(" order by c.last_time desc");
		ArrayList<HashMap<String,String>> result = DBOperation.getRecords(psql.getSQL());
		
		return result;
		
	}
	
	
	/**
	 * 新疆根据设备序列号查询机顶盒业务账号
	 * @param devSn
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getServAccountByDevSn(String devSn) {

		PrepareSQL psql = new PrepareSQL();
		psql.append(" select a.device_id, a.device_serialnumber, a.serv_account ");
		psql.append(" from " + CommonUtil.addPrefix("tab_gw_device") + " a ");
		psql.append(" where 1=1 ");
		psql.append(" and a.dev_sub_sn = '" + devSn.substring(devSn.length() - 6) + "' ");
		psql.append(" and a.device_serialnumber like '%" + devSn + "' ");

		ArrayList<HashMap<String, String>> result = DBOperation.getRecords(psql.getSQL());

		return result;

	}
	
	
	/**
	 * 新疆电信  机顶盒零配置状态查询接口
	 * @author chenxj6
	 * @date 2016-11-11
	 * @param searchType,searchInfo
	 * @return
	 */
	public List<HashMap<String,String>> getDeviceInfo(String searchType, String searchInfo){
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.device_id, a.oui, a.device_serialnumber, a.customer_id, a.serv_account, a.cpe_allocatedstatus, ");
		psql.append(" b.user_status from "+CommonUtil.addPrefix("tab_gw_device")+" a, "+CommonUtil.addPrefix("tab_customer")+" b ");
		psql.append(" where a.customer_id = b.customer_id ");
		
		// searchType=1时searchInfo=业务帐号
		if ("1".equals(searchType)) {
			psql.append("   and a.serv_account = '"+searchInfo+"' ");
		}
		// searchType=2时searchInfo=机顶盒MAC
		else if ("3".equals(searchType)) {
			psql.append("   and a.cpe_mac = '"+searchInfo+"' ");
		}
		// searchType=3时searchInfo=机顶盒序列号
		else if ("2".equals(searchType)) {
			psql.append("   and a.dev_sub_sn = '"+searchInfo.substring(searchInfo.length() - 6)+"' ");
			psql.append("   and a.device_serialnumber like '%"+searchInfo+"' ");
		}
		
		List<HashMap<String,String>> list = DBOperation.getRecords(psql.getSQL());
		
		if (list == null || list.size()==0) {
			return null;
		} else {
			return list;
		}
	}
	
	/**
	 * 新疆电信  机顶盒零配置状态查询接口
	 * @author chenxj6
	 * @date 2016-11-11
	 * @param deviceId ,serviceId
	 * @return
	 */
	public Map<String,String> getStrategyInfo(String deviceId, String serviceId){
//		String sql = " select result_id from stb_gw_serv_strategy where DEVICE_ID='"+deviceId+"' and SERVICE_ID="+serviceId;
		String sql = " select result_id from " + com.linkage.stbms.pic.Global.TABLENAME + "  where DEVICE_ID='"+deviceId+"' and SERVICE_ID="+serviceId;
		PrepareSQL psql = new PrepareSQL(sql);
		
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 获取厂商
	 * @param vendor_id
	 * @return
	 */
	public Map<String, String> getVendorAdd(String vendor_id) {
		PrepareSQL psql = new PrepareSQL();
		psql.append("select vendor_add from ");
		psql.append(CommonUtil.addPrefix("tab_vendor")+" ");
		psql.append("where vendor_id=? ");
		psql.setString(1,vendor_id);
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
	/**
	 * 内蒙古查询机顶盒信息
	 * @param searchType
	 * @param searchInfo
	 * @param fromTabDevice 
	 * @return
	 */
	public ArrayList<HashMap<String,String>> getStbInfoNMGinfo(String searchType, String searchInfo ){
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("SELECT a.device_id,h.stb_id, a.complete_time, f.serv_account , f.pppoe_user, a.device_serialnumber, a.loopback_ip, a.cpe_mac, b.vendor_name, d.hardwareversion, d.softwareversion, e.device_model , f. cust_stat , c. online_status FROM stb_tab_gw_device a,stb_tab_vendor b,stb_gw_devicestatus c,tab_gw_stbid h,stb_tab_devicetype_info d,stb_gw_device_model e,stb_tab_customer f WHERE 1=1 AND a.device_id = c.device_id AND a.vendor_id = b.vendor_id AND a.devicetype_id = d.devicetype_id AND d.device_model_id = e.device_model_id AND a.customer_id = f.customer_id   AND h.device_id=a.device_id ");
		
		// searchType=1时searchInfo=业务帐号
		if ("1".equals(searchType)) {
			psql.append("   and f.serv_account = '"+searchInfo+"' ");
		}
		psql.append(" order by c.last_time desc");
		ArrayList<HashMap<String,String>> result = DBOperation.getRecords(psql.getSQL());
		
		return result;
		
	}
	/**
	 * 内蒙古电信  机顶盒状态查询接口
	 * @author hourui
	 * @date 2017-11-20
	 * @param searchType,searchInfo
	 * @return
	 */
	public List<HashMap<String,String>> getStbOrderStatusNMGInfo(String searchType, String searchInfo){
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select distinct a.device_id ,c.last_time, h.stb_id,a.complete_time,f.serv_account ,f.pppoe_user,  a.cpe_mac, f. cust_stat ,c. online_status from stb_tab_gw_device a,stb_tab_vendor b,stb_gw_devicestatus c,stb_tab_devicetype_info d,stb_gw_device_model e,stb_tab_customer f,tab_gw_stbid h ");
		psql.append(" where 1=1 and a.device_id = c.device_id and a.vendor_id = b.vendor_id and a.devicetype_id = d.devicetype_id and d.device_model_id = e.device_model_id  and h.device_id=a.device_id and a.customer_id=f.customer_id");
		
		// searchType=1时searchInfo=业务帐号
		if ("1".equals(searchType)) {
			psql.append("   and a.serv_account = '"+searchInfo+"' ");
		}
		// searchType=2时searchInfo=机顶盒MAC
		else if ("2".equals(searchType)) {
			psql.append("   and a.cpe_mac = '"+searchInfo+"' ");
		}

		List<HashMap<String,String>> list = DBOperation.getRecords(psql.getSQL());
		
		if (list == null || list.size()==0) {
			return null;
		} else {
			return list;
		}
	}
	
	/**
	 * 查询iptv表数据
	 * @param servAccount
	 * @param pppoeUser
	 * @return
	 */
	public Map<String,String> getStbCustomerInfo(String servAccount, String pppoeUser){
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("select customer_id from stb_tab_customer where 1 = 1 ");
		if (!StringUtil.IsEmpty(servAccount)) {
			psql.append(" and serv_account = '" + servAccount + "'");
		}
		if (!StringUtil.IsEmpty(pppoeUser)) {
			psql.append(" and pppoe_user = '" + pppoeUser + "'");
		}
		Map<String,String> map = DBOperation.getRecord(psql.getSQL());
		return map;
	}
	
	/**
	 * 修改iptv表密码
	 * @param servAccount
	 * @param pppoeUser
	 * @return
	 */
	public int updateStbCustomerPswd(String servAccount, String servpwd, 
			String pppoeUser, String pppoePwd){
		
		PrepareSQL psql = new PrepareSQL();
		psql.append("update stb_tab_customer set ");
		if (!StringUtil.IsEmpty(servAccount) && !StringUtil.IsEmpty(pppoeUser)) {
			psql.append(" serv_pwd = '" + servpwd + "', pppoe_pwd = '" + pppoePwd + "'");
		}
		else if (!StringUtil.IsEmpty(servAccount)) {
			psql.append(" serv_pwd = '" + servpwd + "'");
		}
		else if (!StringUtil.IsEmpty(pppoeUser)) {
			psql.append(" pppoe_pwd = '" + pppoePwd + "'");
		}
		psql.append(" where 1 = 1 ");
		if (!StringUtil.IsEmpty(servAccount)) {
			psql.append(" and serv_account = '" + servAccount + "'");
		}
		if (!StringUtil.IsEmpty(pppoeUser)) {
			psql.append(" and pppoe_user = '" + pppoeUser + "'");
		}
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 更新用户的业务开通状态
	 * 
	 * @param userId
	 */
	public void updateServOpenStatus(String customerId, long result) {
		logger.debug("updateServOpenStatus({}, {})", customerId);
		// 更新SQL语句
		String strSQL = "update stb_tab_customer set user_status = ? where customer_id=?";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, result);
		psql.setLong(2, StringUtil.getLongValue(customerId));
		// 执行查询
		DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 
	 * @param serv_passwd业务账号
	 * @return
	 */
	public Map<String,String> qryStbServInfo(String serv_account){
		String sql = "select a.customer_id,b.device_id,b.device_serialnumber,b.oui,a.serv_pwd,a.pppoe_pwd "
				   + "from stb_tab_customer a,stb_tab_gw_device b where a.customer_id =b.customer_id and a.serv_account=? ";
		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setString(1,serv_account);
		
		try
		{
			return DBOperation.getRecord(pSQL.getSQL());
		}
		catch (Exception e)
		{
			 logger.error("qryStbServInfo error,msgs:{}",e.getMessage());
			 return null;
		}
	}
	
	/**
	 * 查看设备是否在进行业务下发
	 * @param devId
	 * @return
	 */
	public boolean isStbDoing(String devId){
		String sql = "select count(1) num from ? where status not in(0,100) and device_id=?";
		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setStringExt(1, com.linkage.stbms.pic.Global.TABLENAME,false);
		pSQL.setString(2, devId);
		
		try
		{
			Map<String,String> maps = DBOperation.getRecord(pSQL.getSQL());
			if(null != maps && !maps.isEmpty() && (StringUtil.getIntegerValue(maps.get("num"),0) > 0)){
				return true;
			} 
		}
		catch (Exception e)
		{
			 logger.error("isStbDoing error ,msgs:{}",e.getMessage());
			 return true;
		}
		return false;
	}
	
	public int updateStbCustPwd(String custAccount,String serPwd,String pppoePwd){
		String sql = "update stb_tab_customer set serv_pwd=? ,pppoe_pwd=? where serv_account=?";
		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setString(1, serPwd);
		pSQL.setString(2, pppoePwd);
		pSQL.setString(3, custAccount);
        
		try
		{
			return DBOperation.executeUpdate(pSQL.getSQL());
		}
		catch (Exception e)
		{
			 logger.error("updateStbCustPwd error,msgs:{}",e.getMessage());
			 return 0;
		}
	}
	
}
