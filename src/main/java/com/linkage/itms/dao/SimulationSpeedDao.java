package com.linkage.itms.dao;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;

/**
 * 仿真测速dao
 * @author jianglp (75508)
 * @version 1.0
 * @since 2016-12-13
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * 
 */
public class SimulationSpeedDao {
	public static final Logger logger = LoggerFactory
			.getLogger(SimulationSpeedDao.class);
	
	//添加测速记录入库
	public int addSimulationSpeedLog(String deviceId, String status,
			String aveSpeed, String maxSpeed) {
		String sql = "insert into tab_testspeed_result(device_id,status,aveDownloadSpeed,maxDownloadSpeed,record_time)values(?,?,?,?,?)";
		PrepareSQL ppSQL = new PrepareSQL(sql);
		ppSQL.setString(1, StringUtil.getStringValue(deviceId));
		ppSQL.setInt(2, StringUtil.getIntegerValue(status));
		ppSQL.setString(3, aveSpeed);
		ppSQL.setString(4, maxSpeed);
		ppSQL.setLong(5, System.currentTimeMillis() / 1000);
		return DBOperation.executeUpdate(ppSQL.getSQL());
	}

	/**
	 * 
	 * @param oui
	 * @param device_serialnumber
	 * @param status
	 * @param test_time
	 * @param download_url
	 * @param eth_priority
	 * @return
	 */
	public int addHTTPDiagResult(String oui,String device_serialnumber,String status,String test_time,String download_url,String eth_priority,
 String RequestsReceivedTime,
			String TransportStartTime, String TransportEndTime,
			String ReceiveByteContainHead, String ReceiveByte,
			String TCPRequestTime, String TCPResponseTime, String cityName,
			String netAccount, String loid, String maxSpeed, String avgSpeed) {
		PrepareSQL psql = new PrepareSQL(
				"insert into tab_http_diag_result (oui, device_serialnumber, status, test_time, download_url, eth_priority,rom_time,bom_time,eom_time,test_bytes_rece,total_bytes_rece,tcp_req_time,tcp_resp_time, cityname,netaccount,loid,maxspeed,avgspeed) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		psql.setString(1, StringUtil.getStringValue(oui));
		psql.setString(2, StringUtil.getStringValue(device_serialnumber));
		psql.setInt(3, StringUtil.getIntegerValue(status));
		psql.setLong(4, StringUtil.getLongValue(test_time));
		psql.setString(5, StringUtil.getStringValue(download_url));
		psql.setInt(6, StringUtil.getIntegerValue(eth_priority));
		psql.setString(7, StringUtil.getStringValue(RequestsReceivedTime));
		psql.setString(8, StringUtil.getStringValue(TransportStartTime));
		psql.setString(9, StringUtil.getStringValue(TransportEndTime));
		psql.setLong(10, StringUtil.getLongValue(ReceiveByteContainHead));
		psql.setLong(11, StringUtil.getLongValue(ReceiveByte));
		psql.setString(12, StringUtil.getStringValue(TCPRequestTime));
		psql.setString(13, StringUtil.getStringValue(TCPResponseTime));

		// 江苏新加字段 city_id,netAccount,loid,maxSpeed,avgSpeed
		psql.setString(14, cityName);
		psql.setString(15, netAccount);
		psql.setString(16, loid);
		psql.setString(17, maxSpeed);
		psql.setString(18, avgSpeed);
		int num = DBOperation.executeUpdate(psql.getSQL());
		return num;
	}
	
	//获取城市和测速url
	public Map<String, String> getCitySpeedUrlMap() {
		String sql = "select city_id,test_url from tab_speed_param where 1=1";
		return DBOperation.getMap(sql); 
	}

	//获取当前城市测速url
	public String getSpeedUrlByCityId(String cityId) {
		String url = "";
		if (!StringUtil.IsEmpty(url = Global.citySpeedUrlMap.get(cityId))) {
			logger.warn("cityId:{},url:{}",cityId,Global.citySpeedUrlMap.get(cityId));
			return url;
		} else {
			String parentId = "";
			if (!StringUtil.IsEmpty(parentId = Global.G_City_Pcity_Map.get(cityId))) {
				logger.warn("parentId:{},url:{}",parentId,Global.citySpeedUrlMap.get(parentId));
				return getSpeedUrlByCityId(parentId);
			} else {
				return "";
			}
		}
	}

	//判断设备是否在线
	public boolean isOnline(String devId){
		if (StringUtil.IsEmpty(devId)) {
			return false;
		}
		PrepareSQL psql = new PrepareSQL();
		psql.append("select online_status from gw_devicestatus where device_id = '"+ devId + "'");
		String is_online=StringUtil.getStringValue(DBOperation.getRecord(psql.getSQL()),"online_status");
		if (StringUtil.IsEmpty(is_online) || "-1".equals(is_online)) {
			return false;
		}
		return true;
	}
	
	//判断设备是否支持仿真测速
	public boolean doSupportSpeed(String deviceTypeId) {
		String sql = "select is_speedTest from tab_device_version_attribute where devicetype_id="
				+ deviceTypeId;
		String is_speedTest = StringUtil.getStringValue(DBOperation.getRecord(sql),"is_speedtest");
		if (StringUtil.IsEmpty(is_speedTest) || "0".equals(is_speedTest)) {
			return false;
		}
		return true;
	}
}
