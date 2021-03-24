package com.linkage.itms.dao;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.system.utils.database.Cursor;
import com.linkage.system.utils.database.DataSetBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceInfoDAO
{
	private static Logger logger = LoggerFactory.getLogger(DeviceInfoDAO.class);
	/**业务类型：机顶盒*/
	private static final int SERVTYPE_STB = 4;

	/**
	 * 根据设备序列号，厂商OUI检索设备信息
	 */
	public Map<String, String> queryDevInfo(String DevSn, String oui)
	{
		logger.debug("DeviceInfoDAO==>queryDevInfo({},{})", new Object[] { DevSn, oui });
		String devSubSn = DevSn.substring(DevSn.length() - 6, DevSn.length());
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("select device_id, oui, device_serialnumber, city_id, loopback_ip, device_status, devicetype_id, cpe_mac, device_model_id, vendor_id, dev_sub_sn ");
		pSql.append("  from tab_gw_device ");
		pSql.append(" where 1=1 and dev_sub_sn = '" + devSubSn + "' ");
		pSql.append("   and device_serialnumber like  '%" + DevSn + "' ");
		pSql.append("   and oui = '" + oui + "' ");
		return DBOperation.getRecord(pSql.getSQL());
	}

	/**
	 * 根据设备序列号，厂商OUI检索设备信息
	 */
	public Map<String, String> queryDevInfo(String DevSn, String oui, String loid)
	{
		logger.debug("DeviceInfoDAO==>queryDevInfo({},{},{})", new Object[] { DevSn, oui,loid });
		PrepareSQL pSql = new PrepareSQL();
		// 如果设备序列号不为空
		if (!StringUtil.IsEmpty(DevSn))
		{
			pSql.append("select device_id, loopback_ip");
			pSql.append("  from tab_gw_device ");
			pSql.append(" where device_serialnumber = '" + DevSn + "' ");
			if (!StringUtil.IsEmpty(oui))
			{
				pSql.append("   and oui = '" + oui + "' ");
			}
		}
		else
		{
			pSql.append("select device_id from tab_hgwcustomer where username=? ");
			pSql.setString(1, loid);
		}
		return DBOperation.getRecord(pSql.getSQL());
	}

	/**
	 * 查询设备信息
	 */
	public Map<String, String> queryDevInfo(int serviceType, String devSn)
	{
		logger.debug("DeviceInfoDAO==>queryDevInfo({},{})", serviceType, devSn);
		// device_serialnumber
		String query_dev_sql = "";
		// 机顶盒业务查 stb_tab_gw_device表
		if (SERVTYPE_STB == serviceType)
		{
			// query_dev_sql =
			// "select device_id from stb_tab_gw_device where device_serialnumber = ? ";
			query_dev_sql = "select device_id from stb_tab_gw_device where cpe_mac = ? ";
		}
		// 其他业务查 tab_gw_device
		else
		{
			// query_dev_sql =
			// "select device_id from tab_gw_device where device_serialnumber = ? ";
			query_dev_sql = "select device_id from tab_gw_device where cpe_mac = ? ";
//			query_dev_sql = "select device_id from tab_gw_device where device_id_ex = ? ";
		}
		PrepareSQL psql = new PrepareSQL(query_dev_sql);
		psql.setString(1, devSn);
		ArrayList<HashMap<String, String>> list = DBOperation.getRecords(psql.getSQL());
		if (null != list && !list.isEmpty())
		{
			// 如果查出多条，就返回第一条，一般来说只会查出一条
			return list.get(0);
		}
		else
		{
			return null;
		}
	}

	/**
	 * 安徽电信下载拨测专属测试用户查询
	 */
	public List<HashMap<String, String>> getTestUserList(String spead)//字段都用
	{
		PrepareSQL psql = new PrepareSQL(
				"select * from tab_http_test_user where testname = ?");
		psql.setString(1, spead);
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 
	 * @param
	 * @param spead
	 * @return
	 */
	public List<HashMap<String, String>> getTestRateByTestUser(String username,String password)//字段都用
	{
		PrepareSQL psql = new PrepareSQL(
				"select * from tab_http_test_user where username = ? and password = ?");
		psql.setString(1, username);
		psql.setString(2, password);
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 
	 * @param
	 * @param spead
	 * @return
	 */
	public Map<String, String> isSupportGbBroadBand(String devSn)
	{
		PrepareSQL psql = new PrepareSQL(
				"select b.gbbroadband from tab_gw_device a, tab_device_version_attribute b where a.devicetype_id = b.devicetype_id and a.device_serialnumber =?");
		psql.setString(1, devSn);
		return DBOperation.getRecord(psql.getSQL());
	}

	/**
	 * 根据设备Id获取设备序列号
	 * 
	 * @param devId
	 * @return
	 */
	public Map<String, String> queryDevSn(String devId)
	{
		logger.debug("DeviceInfoDAO==>queryDevSn({})", new Object[] { devId });
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("select device_serialnumber, loopback_ip from tab_gw_device where device_id=? ");
		pSql.setString(1, devId);
		return DBOperation.getRecord(pSql.getSQL());
	}

	@SuppressWarnings({ "rawtypes" })
	public List getWanConnIds(String device_id)
	{
		StringBuffer sql = new StringBuffer();
		List<Map> list = new ArrayList<Map>();
		// oracle db
		if (1 == DBUtil.GetDB())
		{
			sql.append("select b.conn_type,b.sess_type,b.serv_list,a.vlan_id,to_char(a.vpi_id) || '/' || to_char(a.vci_id) pvc,b.wan_conn_id,b.wan_conn_sess_id ");
		}
		// mysql db
		else if (3 == DBUtil.GetDB()) 
		{
			sql.append("select b.conn_type,b.sess_type,b.serv_list,a.vlan_id,concat(a.vpi_id, '/', a.vci_id) pvc,b.wan_conn_id,b.wan_conn_sess_id ");
		}
		else
		{
			sql.append("select b.conn_type,b.sess_type,b.serv_list,a.vlan_id,convert(varchar,a.vpi_id)+'/'+convert(varchar,a.vci_id) pvc,b.wan_conn_id,b.wan_conn_sess_id ");
		}
		sql.append("from "
				+ Global.G_TABLENAME_MAP.get("gw_wan_conn")
				+ " a,"
				+ Global.G_TABLENAME_MAP.get("gw_wan_conn_session")
				+ " b where a.device_id=b.device_id and a.wan_conn_id=b.wan_conn_id  and a.device_id='");
		sql.append(device_id).append("'");
		PrepareSQL psql = new PrepareSQL(sql.toString());
		psql.getSQL();
		Cursor cursor = DataSetBean.getCursor(sql.toString());
		for (int i = 0; i < cursor.getRecordSize(); i++)
		{
			list.add(cursor.getRecord(i));
		}
		return list;
	}
	
	/**
	 * 根据设备device_id检索设备信息
	 * chenxj6 20160928
	 * @param device_id
	 * @return
	 */
	public Map<String, String> queryDevInfoByDeviceId(String device_id)
	{
		logger.debug("DeviceInfoDAO==>queryDevInfoByDeviceId({})", new Object[] { device_id });
		PrepareSQL pSql = new PrepareSQL();
		pSql.append(" select device_id, oui, device_serialnumber, city_id, loopback_ip, device_status, devicetype_id, cpe_mac, device_model_id, vendor_id, dev_sub_sn ");
		pSql.append(" from tab_gw_device ");
		pSql.append(" where 1=1 and device_id = '" + device_id + "' ");
		return DBOperation.getRecord(pSql.getSQL());
	}
	
	/**
	 * 获取设备厂商、软件版本、硬件版本、型号
	 */
	public Map<String, String> queryInfo(String devicetype_id)
	{
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("select a.hardwareversion,a.softwareversion,c.device_model,b.vendor_name ");
		pSql.append("from tab_devicetype_info a,tab_vendor b,gw_device_model c ");
		pSql.append("where a.vendor_id=b.vendor_id and a.device_model_id=c.device_model_id ");
		pSql.append("and a.devicetype_id=? ");
		pSql.setInt(1,StringUtil.getIntegerValue(devicetype_id));
		
		return DBOperation.getRecord(pSql.getSQL());
	}
	
	
	/**
	 * 根据设备devSn查询家庭网关宽带账号 
	 * chenxj6 20160928
	 * @param devSn
	 * @return
	 */
	public List<HashMap<String, String>> getNetAccountByDevSn(String devSn)
	{
		logger.debug("DeviceInfoDAO==>getNetAccountByDevSn({})", new Object[] { devSn });
		String devSubSn = devSn.substring(devSn.length() - 6, devSn.length());
		PrepareSQL pSql = new PrepareSQL();
		pSql.append(" select a.username ,b.username netaccount,b.wan_type,b.passwd from tab_hgwcustomer a, hgwcust_serv_info b, tab_gw_device c ");
		pSql.append(" where a.device_id=c.device_id and a.user_id=b.user_id and b.serv_type_id = 10 ");
		pSql.append(" and c.device_serialnumber like '%"+devSn+"' and c.dev_sub_sn='"+devSubSn+"'");
		return DBOperation.getRecords(pSql.getSQL());
	}
	
	/**
	 * 根据用户宽带账号查询用户信息
	 * @param netAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByNetAccount(String netAccount)
	{
		String sql = "select a.user_id, b.device_id from hgwcust_serv_info a, tab_hgwcustomer b"
				+ " where a.user_id = b.user_id and a.serv_type_id=10 and a.username=?  order by b.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, netAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据用户宽带账号查询用户信息和设备厂商
	 * @param netAccount
	 * @return
	 */
	public List<HashMap<String, String>> queryUserVendorByNetAccount(String netAccount)
	{
		String sql = "select a.user_id, b.device_id,c.vendor_id,d.device_model, b.city_id,a.username,b.device_serialnumber,e.hardwareversion  " +
				"  from hgwcust_serv_info a, tab_hgwcustomer b left join tab_gw_device c on b.device_id=c.device_id left join gw_device_model d on c.device_model_id = d.device_model_id " +
				"left join tab_devicetype_info e on c.devicetype_id = e.devicetype_id" +
				" where a.user_id = b.user_id " +
				"   and a.serv_type_id = 10 and a.username=?  order by b.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, netAccount);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}

	/**
	 * 根据用户LOID查询用户信息
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByLoidNx(String loid)
	{
		String sql = "select distinct(a.user_id),a.device_id from tab_hgwcustomer a,tab_gw_device b where a.device_id = b.device_id and a.username= ? order by a.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户LOID查询用户信息
	 * @param loid
	 * @return
	 */
	public List<HashMap<String, String>> queryUserByLoid(String loid)
	{
		String sql = "select distinct(a.user_id),a.device_id from tab_hgwcustomer a where a.username= ? order by a.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, loid);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户设备SN查询设备
	 * @param devSn
	 * @return
	 */
	public List<HashMap<String, String>> queryDeviceByDevSN(String devSN)
	{
		String sql = "select a.device_id,a.device_serialnumber,a.customer_id as user_id from tab_gw_device a where a.device_status = 1 " +
				" and a.device_serialnumber like '%"+ devSN + "'";
		if (!StringUtil.IsEmpty(devSN) && devSN.length() >= 6) {
			sql += " where 1=1 and dev_sub_sn = '" + devSN.substring(devSN.length()-6,devSN.length()) + "' ";
		}
		PrepareSQL pSql = new PrepareSQL(sql);
		List<HashMap<String, String>> result = DBOperation.getRecords(pSql.getSQL());
		return result;
	}
	
	/**
	 * 根据用户 user_id 查询用户 sip_id,regi_serv
	 * @param user_id
	 * @return
	 */
	public Map<String, String> querySipInfoByUserId(String user_id)
	{
		String sql = "select a.sip_id, b.regi_serv from tab_voip_serv_param a, tab_sip_info b where a.sip_id=b.sip_id and " +
				" a.user_id= ? order by a.updatetime desc";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, user_id);
		return DBOperation.getRecord(pSql.getSQL());
	}
	
	/**
	 * JSDX_ITMS-REQ-20170410-WJY-001（综调接口新增终端属地查询接口)
	 * @param devSn
	 * @return
	 */
	public ArrayList<HashMap<String, String>> queryDevCityId(String devSn) {
		logger.debug("DeviceInfoDAO==>queryDevCityId({})", new Object[] { devSn});
		String devSubSn = devSn.substring(devSn.length() - 6, devSn.length());
		PrepareSQL pSql = new PrepareSQL();
		pSql.append("select device_id, oui, device_serialnumber, city_id, loopback_ip, device_status, devicetype_id, cpe_mac, device_model_id, vendor_id, dev_sub_sn ");
		pSql.append("  from tab_gw_device ");
		pSql.append(" where 1=1 and dev_sub_sn = '" + devSubSn + "' ");
		pSql.append("   and device_serialnumber like  '%" + devSn + "' ");
		return DBOperation.getRecords(pSql.getSQL());
	}
	
	/**
	 * 是否支持零配置开通设备版本查询接口
	 * @param
	 * @param device_id
	 * @return
	 */
	public Map<String, String> queryZeroConfByDeviceId(String device_id)
	{
		logger.debug("DeviceInfoDAO==>queryZeroConfByDeviceId({})", new Object[] { device_id });
		PrepareSQL pSql = new PrepareSQL();
		pSql.append(" select b.zeroconf from tab_gw_device a, tab_devicetype_info b ");
		pSql.append(" where a.devicetype_id = b.devicetype_id and a.device_id = '" + device_id + "' ");
		return DBOperation.getRecord(pSql.getSQL());
	}
	/**
	 * 根据VOIP业务电话号码查询用户信息
	 * @param voipPhone
	 * @return
	 */
	public List<HashMap<String, String>> queryIpByCityIdandProtocol (String cityId,String protocol)
	{
		String sql = "select * from tab_voice_ping_param where city_id = ? and protocol = ?";
		if (3 == DBUtil.GetDB()) {
			sql = "select ping_ip,package_num,timeout,package_byte from tab_voice_ping_param where city_id = ? and protocol = ?";
		}
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, cityId);
		pSql.setInt(2, Integer.valueOf(protocol));
		return DBOperation.getRecords(pSql.getSQL());
	}
	
	
	/**
	 * 仿真测速接口，新增结果入库
	 * @param
	 * @param deviceId
	 * @param devSn
	 * @param username
	 * @param cityId
	 * @param speed
	 * @param AvgSampledTotalValues
	 * @param MaxSampledTotalValues
	 * @param TransportStartTime
	 * @param TransportEndTime
	 * @param Ip
	 * @param ReceiveByte
	 * @param TCPRequestTime
	 * @param TCPResponseTime
	 * Avg2,Max2一直为0
	 * Speed_status默认为0
	 * updateTime为当前时间
	 * @return
	 */
	public int ahInsertTestSpeedDev(String deviceId,String devSn,String username,String cityId,String speed,String AvgSampledTotalValues,
			String MaxSampledTotalValues,String TransportStartTime,String TransportEndTime,String Ip,String ReceiveByte,String TCPRequestTime,
			String TCPResponseTime,int speedStatus)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("insert into tab_intf_speed_result(Device_id,device_serialnumber,username,city_id ,speed," +
					"AvgSampledTotalValues,MaxSampledTotalValues,Avg2,Max2,TransportStartTime,TransportEndTime,IP,ReceiveByte,TCPRequestTime," +
					"TCPResponseTime,Speed_status,updateTime) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		psql.setString(1, deviceId);
		psql.setString(2, devSn);
		psql.setString(3, username);
		psql.setString(4, cityId);
		psql.setString(5, speed);
		psql.setString(6, AvgSampledTotalValues);
		psql.setString(7, MaxSampledTotalValues);
		psql.setString(8, "0");
		psql.setString(9, "0");
		psql.setString(10, TransportStartTime);
		psql.setString(11, TransportEndTime);
		psql.setString(12, Ip);
		psql.setString(13, ReceiveByte);
		psql.setString(14, TCPRequestTime);
		psql.setString(15, TCPResponseTime);
		psql.setInt(16, speedStatus);
		psql.setLong(17, System.currentTimeMillis()/1000);
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 
	 * @param
	 * @param deviceId
	 * @param devSn
	 * @param username
	 * @param cityId
	 * @param speed
	 * @param AvgSampledTotalValues
	 * @param MaxSampledTotalValues
	 * @param TransportStartTime
	 * @param TransportEndTime
	 * @param Ip
	 * @param ReceiveByte
	 * @param TCPRequestTime
	 * @param TCPResponseTime
	 * @param wanType 
	 * @param testStartTime 
	 * @param testUserName 
	 * @param loid 
	 * @param clientType 
	 * @param speedStatus
	 * @return
	 */
	public int jxInsertTestSpeedDev(String deviceId,String cmdId,int resultCode,String resultDesc,String devSn,String username,String speed,String AvgSampledTotalValues,
			String MaxSampledTotalValues,String TransportStartTime,String TransportEndTime,String Ip,String ReceiveByte,String TCPRequestTime,
			String TCPResponseTime,String downUrl, long testStartTime, String wanType, String loid, String testUserName, int clientType)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("insert into tab_http_diag_result_intf(device_id,cmdId,RstCode,RstMsg,device_serialnumber,username,speed," +
					"AvgSampledTotalValues,MaxSampledTotalValues,TransportStartTime,TransportEndTime,IP,ReceiveByte,TCPRequestTime," +
					"TCPResponseTime,downUrl,updateTime,test_time,wan_type, loid, testUserName,clientType) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		psql.setString(1, deviceId);
		psql.setString(2, cmdId);
		psql.setInt(3, resultCode);
		psql.setString(4, resultDesc);
		psql.setString(5, devSn);
		psql.setString(6, username);
		psql.setString(7, speed);
		psql.setString(8, AvgSampledTotalValues);
		psql.setString(9, MaxSampledTotalValues);
		psql.setString(10, TransportStartTime);
		psql.setString(11, TransportEndTime);
		psql.setString(12, Ip);
		psql.setString(13, ReceiveByte);
		psql.setString(14, TCPRequestTime);
		psql.setString(15, TCPResponseTime);
		psql.setString(16, downUrl);
		psql.setLong(17, System.currentTimeMillis()/1000);
		psql.setLong(18, testStartTime);
		psql.setString(19, wanType);
		psql.setString(20, loid);
		psql.setString(21, testUserName);
		psql.setInt(22, clientType);
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 查询设备信息
	 * @param customerId
	 * @return
	 */
	public Map<String, String> queryDevInfo4JL(long customerId)
	{
		logger.debug("DeviceInfoDAO==>queryDevInfo4JL({})", customerId);
		PrepareSQL pSQL = new PrepareSQL();
		pSQL.append(" select device_id from stb_tab_gw_device where customer_id=" + customerId);
		return DBOperation.getRecord(pSQL.getSQL());
	}
	
	/**
	 * 查询设备最近一次测速结果
	 * @param
	 * @param deviceId
	 * @return
	 */
	public List<HashMap<String, String>> queryDownLoadResult(String deviceId)
	{
		logger.debug("DeviceInfoDAO==>queryDownLoadResult({})", deviceId);
		PrepareSQL pSQL = new PrepareSQL();
		
		if (3 == DBUtil.GetDB()) 
		{
			pSQL.append(" select device_serialnumber,username,speed,AvgSampledTotalValues,MaxSampledTotalValues,TransportStartTime,transportEndTime,IP,receiveByte,tcpRequestTime,tcpResponseTime from tab_http_diag_result_intf where device_id='" + deviceId+"' order by test_time desc");
		}
		else {
			pSQL.append(" select * from tab_http_diag_result_intf where device_id='" + deviceId+"' order by test_time desc");
		}
		
		return DBOperation.getRecords(pSQL.getSQL());
	}
	
	/**
	 * mac和sn查询
	 * @param user_id
	 * @return
	 */
	public Map<String, String> queryDevSNMAC(String user_id) {
		logger.debug("DeviceInfoDAO==>queryDevSNMAC({})", user_id);
		PrepareSQL pSql = new PrepareSQL();
		pSql.append(" select b.username netaccount, c.oui, c.device_serialnumber, c.cpe_mac from tab_hgwcustomer a, hgwcust_serv_info b, tab_gw_device c ");
		pSql.append(" where a.device_id = c.device_id and a.user_id = b.user_id ");
		pSql.append(" and a.user_id =" + user_id);
		
		return DBOperation.getRecord(pSql.getSQL());
	}

	/**
	 * mac和sn查询
	 * @param user_id
	 * @return
	 */
	public Map<String, String> queryBbmsDevSNMAC(String user_id) {
		logger.debug("DeviceInfoDAO==>queryBbmsDevSNMAC({})", user_id);
		PrepareSQL pSql = new PrepareSQL();
		pSql.append(" select b.username netaccount, c.oui, c.device_serialnumber, c.cpe_mac from tab_egwcustomer a, egwcust_serv_info b, tab_gw_device c ");
		pSql.append(" where a.device_id = c.device_id and a.user_id = b.user_id and c.gw_type = 2 and b.serv_type_id = 10");
		pSql.append(" and a.user_id =" + user_id);
		return DBOperation.getRecord(pSql.getSQL());
	}
	
	public int jxInsertSpecialSpeed(String deviceId,String cmdId,int resultCode,String resultDesc,String devSn,String username,String netMask,String AvgSampledTotalValues,
			String MaxSampledTotalValues,String TransportStartTime,String TransportEndTime,String Ip,String ReceiveByte,String TCPRequestTime,
			String TCPResponseTime,String downUrl, long testStartTime, String wanType, String loid, String gateWay, int clientType, String dns)
	{
		PrepareSQL psql = new PrepareSQL();
		psql.setSQL("insert into TAB_HTTP_SPECIAL_SPEED_INTF (device_id,cmdId,RstCode,RstMsg,device_serialnumber,username,netMask," +
					"AvgSampledTotalValues,MaxSampledTotalValues,TransportStartTime,TransportEndTime,IP,ReceiveByte,TCPRequestTime," +
					"TCPResponseTime,downUrl,updateTime,test_time,wan_type, loid, gateWay,clientType,dns) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		psql.setString(1, deviceId);
		psql.setString(2, cmdId);
		psql.setInt(3, resultCode);
		psql.setString(4, resultDesc);
		psql.setString(5, devSn);
		psql.setString(6, username);
		psql.setString(7, netMask);
		psql.setString(8, AvgSampledTotalValues);
		psql.setString(9, MaxSampledTotalValues);
		psql.setString(10, TransportStartTime);
		psql.setString(11, TransportEndTime);
		psql.setString(12, Ip);
		psql.setString(13, ReceiveByte);
		psql.setString(14, TCPRequestTime);
		psql.setString(15, TCPResponseTime);
		psql.setString(16, downUrl);
		psql.setLong(17, System.currentTimeMillis()/1000);
		psql.setLong(18, testStartTime);
		psql.setString(19, wanType);
		psql.setString(20, loid);
		psql.setString(21, gateWay);
		psql.setInt(22, clientType);
		psql.setString(23, dns);
		return DBOperation.executeUpdate(psql.getSQL());
	}

	/**
	 * 输入序列号进行查询终端当前是否已导入预制设备
	 * @param dsn
	 * @return
	 */
	public Map<String, String> queryDevInitInfo(String dsn) {
		logger.debug("DeviceInfoDAO==>queryDevInfo4JL({})", dsn);
		PrepareSQL pSQL = new PrepareSQL();
		pSQL.append(" select device_id,device_serialnumber,city_id,status from tab_gw_device_init where device_serialnumber='" + dsn +"' ");
		return DBOperation.getRecord(pSQL.getSQL());
	}
	
	/**
	 * 查询设备宽带信息
	 * @param ckType 1(宽带账号) 2(loid)  3(设备序列号后六位)
	 * @param userinfo
	 * @return
	 */
	public List<HashMap<String,String>> qryNetDevInfo(int ckType , String userinfo){
		String ckSql =  "select a.user_id,b.passwd,c.device_id ,c.device_serialnumber,c.oui,c.gather_id,b.username,b.wan_type"
				  + " from tab_hgwcustomer a,hgwcust_serv_info b,tab_gw_device c "
				  + " where a.user_id=b.user_id and a.device_id=c.device_id";
	   boolean flag = false;
		//用户宽带账号
		if(ckType == 1){
			ckSql = ckSql + " and b.username=?";
		}
		//LOID
		else if(ckType == 2){
			ckSql = ckSql + " and a.username=?";
		}
		//设备序列号后六位
		else if(ckType == 3){
			ckSql = ckSql + " and c.dev_sub_sn=? and c.device_serialnumber like '%?'";
			flag = true;
		}else{
			return null;
		}
		
		ckSql = ckSql + " and b.serv_type_id=10";
		PrepareSQL pSQL = new PrepareSQL(ckSql);
		pSQL.setString(1, userinfo);
		if(flag){
			pSQL.setStringExt(2, userinfo,false);
		}
		
		try
		{
			return DBOperation.getRecords(pSQL.getSQL());
		}
		catch (Exception e)
		{
			logger.error("qryNetDevInfo err ,msgs:{}",e.getMessage());
			return null;
		}
	}
	
	/**
	 * 查看设备是否正在被操作
	 * @param devId
	 * @return
	 */
	public boolean isDevDoing(String devId){
		String sql = "select count(1) num from gw_serv_strategy where status not in(0,100) and device_id=?";
		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setString(1, devId);
		try
		{
			Map<String,String> maps = DBOperation.getRecord(pSQL.getSQL());
			if(null != maps && !maps.isEmpty() && (StringUtil.getIntegerValue(maps.get("num"),0) > 0)){
				return true;
			} 
		}
		catch (Exception e)
		{
			logger.error("exit! isDevDoing error ,msgs:{}",e.getMessage());
			return true;
		}
		return false;
	}
	
	/**
	 * 更新宽带开通状态和密码
	 * @param userId
	 * @param username
	 * @param pwd
	 * @return
	 */
	public int updateNetPass(String userId,String username,String pwd){
		String sql = "update hgwcust_serv_info set passwd=?,dealdate=?, updatetime=?,open_status=0 where user_id=? and username=? and serv_type_id=10";
		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setString(1, pwd);
		pSQL.setLong(2, System.currentTimeMillis()/1000);
		pSQL.setLong(3, System.currentTimeMillis()/1000);
		pSQL.setLong(4, Long.valueOf(userId));
		pSQL.setString(5, username);
	
		try
		{
			return DBOperation.executeUpdate(pSQL.getSQL());
		}
		catch (Exception e)
		{
			logger.warn("updateNetPass error,msgs:{}",e.getMessage());
			return 0;
		}
	}
	
	/**
	 * 查询设备宽带信息
	 * @param ckType 1(宽带账号) 2(loid)  3(设备序列号后六位)
	 * @param userinfo
	 * @return
	 */
	public List<HashMap<String,String>> qryDevInfo(int ckType , String userinfo){
		String ckSql =  "select c.device_id";
	   boolean flag = false;
		//用户宽带账号
		if(ckType == 1){
			ckSql = ckSql + " from tab_hgwcustomer a,hgwcust_serv_info b,tab_gw_device c "
					  + " where a.user_id=b.user_id and a.device_id=c.device_id  and b.username=?";
		}
		//LOID
		else if(ckType == 2){
			ckSql = ckSql + " from tab_hgwcustomer a,tab_gw_device c "
					  + " where a.device_id=c.device_id and a.username=?";
		}
		//设备序列号后六位
		else if(ckType == 3){
			ckSql = ckSql + " from tab_hgwcustomer a,tab_gw_device c "
					  + " where a.device_id=c.device_id and c.dev_sub_sn=? and c.device_serialnumber like '%?'";
			flag = true;
		}else{
			return null;
		}
		
		PrepareSQL pSQL = new PrepareSQL(ckSql);
		pSQL.setString(1, userinfo);
		if(flag){
			pSQL.setStringExt(2, userinfo,false);
		}
		
		try
		{
			return DBOperation.getRecords(pSQL.getSQL());
		}
		catch (Exception e)
		{
			logger.error("qryDevInfo err ,msgs:{}",e.getMessage());
			return null;
		}
	}
	
	/**
	 * 根据LOID查询用户设备信息
	 * @param loid
	 * @return
	 */
	public Map<String, String> queryLoidInfoByDeviceId(String device_id) {
		logger.debug("queryDeviceInfoByLoid,loid({})", device_id);
		PrepareSQL psql = new PrepareSQL();
		psql.append("select a.username,a.user_id,b.device_id,b.device_serialnumber from tab_hgwcustomer a ");
		psql.append(", tab_gw_device b where a.device_id = b.device_id");
		psql.append(" and b.device_id = '" + device_id + "'");
		psql.append(" and a.user_state in ('1','2')");
		
		return DBOperation.getRecord(psql.getSQL());
	}
	
	public Map<String, String> queryUserInfoForGS(int userType, String username) {
		String table_customer = "tab_hgwcustomer";
		String table_serv_info = "hgwcust_serv_info";
		String devSubSn = "";
		// 查询用户信息
		String strSQL = "select t.device_id, t.user_id, t.username as logic_id "
		+" from tab_hgwcustomer t left join tab_gw_device t1 on t1.device_id = t.device_id where ";
		switch (userType) {
			// 用户宽带帐号
			case 1:
				strSQL = "select a.device_id,a.user_id,a.username as logic_id from " + table_customer + " a  left join tab_gw_device t1 on a.device_id = t1.device_id ," + table_serv_info + " b where a.user_id = b.user_id and b.serv_type_id = 10 and b.username='" + username + "' order by t1.complete_time desc nulls last";
				break;
			//逻辑ID
			case 2:
				strSQL += " username = '" + username + "' order by t1.complete_time desc nulls last";
				break;
			//目前没有文件定义，3默认设备序列号
			case 3:
				if(username.length()>6){
					devSubSn = username.substring(username.length() - 6, username.length());
				}
				strSQL += " t1.device_serialnumber like'%" + username + "' ";
				if(username.length()>6){
					devSubSn = username.substring(username.length() - 6, username.length());
					strSQL +=" and t1.dev_sub_sn = '"+devSubSn+"'";
				}
				break;
			default:
				return null;
		}
		ArrayList<HashMap<String, String>> userIdList = DBOperation.getRecords(new PrepareSQL(strSQL).getSQL());

		Map<String, String> userInfoMap = new HashMap<String, String>();
		if (null != userIdList && !userIdList.isEmpty() && null != userIdList.get(0)) {
			userInfoMap.put("loid", userIdList.get(0).get("logic_id"));
			userInfoMap.put("user_id", userIdList.get(0).get("user_id"));
			userInfoMap.put("device_id", userIdList.get(0).get("device_id"));
		}

		return userInfoMap;
	}
	
	
	public Map<String, String> queryDeviceInfo(String deviceId)
	{
		String sql = "select b.devicetype,b.devicemodel,b.wifinum,b.maxspeed,b.lannum from tab_gw_device a,tab_speed_lannub b where a.device_model_id=b.devicemodel_id and  a.device_id=?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, deviceId);
		Map<String, String> result = DBOperation.getRecord(pSql.getSQL());
		return result;
	}
	
	public int updateMaxNetNum(String num, long userId, String username) {
		String sql = "update tab_net_serv_param  set  max_net_num=?  where  user_id=? ";
		if (!StringUtil.IsEmpty(username)) {
			sql += " and  username=? ";
		}
		PrepareSQL pSQL = new PrepareSQL(sql);
		pSQL.setInt(1, StringUtil.getIntegerValue(num));
		pSQL.setLong(2, userId);
		if (!StringUtil.IsEmpty(username)) {
			pSQL.setString(3, username);
		}
		return DBOperation.executeUpdate(pSQL.getSQL());
	}

	public Map<String, String> queryDeviceType(String deviceId)
	{
		String sql = "select a.device_type,b.device_model from tab_gw_device a,gw_device_model b where a.device_model_id=b.device_model_id and a.device_id=?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, deviceId);
		return DBOperation.getRecord(pSql.getSQL());
	}

	/**
	 * 判断该设备型号在表fmk_model 中是否存在 for SD_LT
	 * @param devModel
	 * @return
	 */
	public boolean queryCountByModel(String devModel){
		String sql = "select count(*) num from fmk_model where device_model=?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, devModel);
		Map<String, String> record = DBOperation.getRecord(pSql.getSQL());
		return record != null && record.size() > 0 && Integer.parseInt(record.get("num")) > 0;
	}

	/**
	 * 判断该设备序列号在表fmk_huawei 中是否存在 for SD_LT
	 * @param devSn
	 * @return
	 */
	public boolean queryCountBySn(String devSn){
		String sql = "select count(*) num from fmk_huawei where sn=?";
		PrepareSQL pSql = new PrepareSQL(sql);
		pSql.setString(1, devSn);
		Map<String, String> record = DBOperation.getRecord(pSql.getSQL());
		return record != null && record.size() > 0 && Integer.parseInt(record.get("num")) > 0;
	}
	
}
