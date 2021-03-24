package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dispatch.obj.VXLANBaseChecker;

/**
 * 
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-11-28
 * @category com.linkage.itms.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class VXLANUserDeviceDAO
{
	private static Logger logger = LoggerFactory.getLogger(VXLANUserDeviceDAO.class);

	/**
	 * 根据用户的业务账号查询用户信息
	 * 
	 * @param userType: 用户信息类型;
	 * @param username: 业务号码
	 */
	public ArrayList<HashMap<String, String>> queryUserInfo(int userType, String username) {
		logger.debug("queryUserInfo[{}]", username);

		String table_customer = "tab_egwcustomer";
		String table_serv_info = "egwcust_serv_info";
		String tab_vxlan_serv_param = "tab_vxlan_serv_param";
		
		if(1==Global.IPSEC_ISFUSED){
			table_customer = "tab_hgwcustomer";
			table_serv_info = "hgwcust_serv_info";
		}

		PrepareSQL psql = new PrepareSQL();

		psql.append(" select a.user_id,a.username,a.device_id,a.oui,a.device_serialnumber,b.username netusername ");

		switch (userType) {
		case 1:
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b ");
			psql.append(" where a.user_id=b.user_id and b.serv_status= 1 ");
			psql.append(" and b.username='" + username + "' and b.serv_type_id=10 ");
			psql.append(" order by a.updatetime desc ");
			break;
		case 2:
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b ");
			psql.append(" where a.user_id=b.user_id and b.serv_status= 1 ");
			psql.append(" and a.username='" + username + "' and b.serv_type_id=10 ");
			psql.append(" order by a.updatetime desc ");
			break;
		case 3:
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b, "+ tab_vxlan_serv_param + " c ");
			psql.append(" where a.user_id=b.user_id and b.user_id=c.user_id and b.serv_status= 1 ");
			psql.append(" and b.username='" + username + "' and b.serv_type_id=10 ");
			psql.append(" order by a.updatetime desc ");
			break;
		case 4:
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b, "+ tab_vxlan_serv_param + " c ");
			psql.append(" where a.user_id=b.user_id and b.user_id=c.user_id and b.serv_status= 1 ");
			psql.append(" and a.username='" + username + "' and b.serv_type_id=10 ");
			psql.append(" order by a.updatetime desc ");
			break;
		default:
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b, "+ tab_vxlan_serv_param + " c ");
			psql.append(" where a.user_id=b.user_id and b.user_id=c.user_id and b.serv_status= 1 ");
			psql.append(" and a.username='" + username + "' and b.serv_type_id=10 ");
			psql.append(" order by a.updatetime desc ");
		}
		return DBOperation.getRecords(psql.getSQL());
	}
	
	/**
	 * 保存工单信息
	 * @param BaseChecker: checker
	 * 
	 */
	public int saveSheet(VXLANBaseChecker checker){
		logger.debug("saveSheet");
		int index = 0;
		StringBuffer sbSQL = new StringBuffer(" insert into tab_vxlan_serv_param (user_id,username,serv_type_id,request_id, "
				+ " tunnel_key,tunnel_remote_ip,workmode,maxmtusize,ip_address,subnetmask,addressing_type,"
				+ " natenabled,dnsservers_master,dnsservers_slave,defaultgateway,xctcom_vlan,open_date,updatetime,"
				+ " serv_status,vxlanconfigsequence,open_status,bind_port)");
		sbSQL.append(" values (?,?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,?, ?,?,?,?,?,?,?)");
		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		psql.setLong(++index, checker.getUserId());
		psql.setString(++index, checker.getNetUsername());
		psql.setInt(++index, checker.getServTypeId());
		psql.setString(++index, checker.getRequestID());
		psql.setInt(++index, checker.getTunnelKey());
		psql.setString(++index, checker.getTunnelRemoteIp());
		psql.setInt(++index, checker.getWorkMode());
		psql.setInt(++index, checker.getMaxMTUSize());
		psql.setString(++index, checker.getiPAddress());
		psql.setString(++index, checker.getSubnetMask());
		psql.setString(++index, checker.getAddressingType());
		psql.setInt(++index, checker.getnATEnabled());
		psql.setString(++index, checker.getdNSServers_Master());
		psql.setString(++index, checker.getdNSServers_Slave());
		psql.setString(++index, checker.getDefaultGateway());
		psql.setInt(++index, checker.getXctcom_vlan());
		psql.setLong(++index, System.currentTimeMillis()/1000);
		psql.setLong(++index, System.currentTimeMillis()/1000);
		psql.setInt(++index, 1);
		psql.setInt(++index, checker.getvXLANConfigSequence());
		psql.setInt(++index, 0);
		psql.setString(++index, checker.getBindPort());
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 更新工单信息
	 * @param BaseChecker: checker
	 * 
	 */
	public int updateSheet(VXLANBaseChecker checker){
		logger.debug("updateSheet");
		int index = 0;
		StringBuffer sbSQL = new StringBuffer(" update tab_vxlan_serv_param set serv_status=1,open_status=0,request_id=?,"
				+ "tunnel_key=?,tunnel_remote_ip=?,workmode=?,maxmtusize=?,ip_address=?,subnetmask=?,addressing_type=?,"
				+ "natenabled=?,dnsservers_master=?,dnsservers_slave=?,defaultgateway=?,xctcom_vlan=?,updatetime=?,bind_port=?");
		sbSQL.append(" where user_id=? and username=? and serv_type_id=? and vxlanconfigsequence=?");
		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		psql.setString(++index, checker.getRequestID());
		psql.setInt(++index, checker.getTunnelKey());
		psql.setString(++index, checker.getTunnelRemoteIp());
		psql.setInt(++index, checker.getWorkMode());
		psql.setInt(++index, checker.getMaxMTUSize());
		psql.setString(++index, checker.getiPAddress());
		psql.setString(++index, checker.getSubnetMask());
		psql.setString(++index, checker.getAddressingType());
		psql.setInt(++index, checker.getnATEnabled());
		psql.setString(++index, checker.getdNSServers_Master());
		psql.setString(++index, checker.getdNSServers_Slave());
		psql.setString(++index, checker.getDefaultGateway());
		psql.setInt(++index, checker.getXctcom_vlan());
		psql.setLong(++index, System.currentTimeMillis()/1000);
		psql.setString(++index, checker.getBindPort());
		psql.setLong(++index, checker.getUserId());
		psql.setString(++index, checker.getNetUsername());
		psql.setInt(++index, checker.getServTypeId());
		psql.setLong(++index, checker.getvXLANConfigSequence());
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 销户删除工单信息
	 * @param BaseChecker: checker 
	 * 
	 */
	public int deleteSheet(VXLANBaseChecker checker){
		logger.debug("deleteSheet");

		StringBuffer sbSQL = new StringBuffer(" delete from tab_vxlan_serv_param " +
				" where user_id=? and vxlanconfigsequence=?");
		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		psql.setLong(1, checker.getUserId());
		psql.setInt(2, checker.getvXLANConfigSequence());
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 检查工单信息是否存在
	 * @param BaseChecker: checker user_id,username, serv_type_id
	 * 
	 */
	public boolean hasSheet(VXLANBaseChecker checker){
		logger.debug("hasSheet");

		StringBuffer sbSQL = new StringBuffer(" select count(1) sheetnum from tab_vxlan_serv_param " +
				" where user_id=? and username=? and serv_type_id=? and vxlanconfigsequence = ?");
		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		psql.setLong(1, checker.getUserId());
		psql.setString(2, checker.getNetUsername());
		psql.setLong(3, checker.getServTypeId());
		psql.setLong(4, checker.getvXLANConfigSequence());
		if(StringUtil.getIntValue(DBOperation.getRecord(psql.getSQL()), "sheetnum", 0) > 0){
			return true;
		}
		return false;
	}
	
	/**
	 * 修改时检查工单TunnelKey信息是否存在
	 * @param BaseChecker: checker user_id,username, serv_type_id
	 * 
	 */
	public boolean hasUpdateTunnelKeySheet(VXLANBaseChecker checker){
		logger.debug("hasSheet");

		StringBuffer sbSQL = new StringBuffer(" select count(1) sheetnum from tab_vxlan_serv_param " +
				" where user_id=? and username=? and serv_type_id=? and tunnel_key = ? and vxlanconfigsequence <> ?");
		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		psql.setLong(1, checker.getUserId());
		psql.setString(2, checker.getNetUsername());
		psql.setLong(3, checker.getServTypeId());
		psql.setLong(4, checker.getTunnelKey());
		psql.setLong(5, checker.getvXLANConfigSequence());
		if(StringUtil.getIntValue(DBOperation.getRecord(psql.getSQL()), "sheetnum", 0) > 0){
			return true;
		}
		return false;
	}
	
	/**
	 * 用户输入了通道实例号时，查询tunnel_key是否已存在，如果已存在，则采用已存在得tunnel_key对应得实例号进行开通
	 * 如果不存在，则采用用户输入得实例号进行开通
	 * @param checker
	 * @return
	 */
	public int querySequenceBytunnelkey(VXLANBaseChecker checker){
		StringBuffer sbSQL = new StringBuffer(" select vxlanconfigsequence from tab_vxlan_serv_param " +
				" where user_id=? and username=? and serv_type_id=? and tunnel_key = ? and vxlanconfigsequence <> ?");
		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		psql.setLong(1, checker.getUserId());
		psql.setString(2, checker.getNetUsername());
		psql.setLong(3, checker.getServTypeId());
		psql.setLong(4, checker.getTunnelKey());
		psql.setLong(5, checker.getvXLANConfigSequence());
		return StringUtil.getIntValue(DBOperation.getRecord(psql.getSQL()), "vxlanconfigsequence");
	}
	
	/**
	 * 获取当前未用得通道实例号 例如通道有1,2,5,7，则返回3，节省通道资源
	 * @param checker
	 * @return
	 */
	public int querySequence(VXLANBaseChecker checker){
		/*StringBuffer sbSQL = new StringBuffer(" select max(vxlanconfigsequence) from tab_vxlan_serv_param " +
				" where user_id=? and username=? and serv_type_id=? ");
		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		psql.setLong(1, checker.getUserId());
		psql.setString(2, checker.getNetUsername());
		psql.setLong(3, checker.getServTypeId());
		ArrayList<HashMap<String, String>> sequenceList = DBOperation.getRecords(psql.getSQL());
		if(null == sequenceList || sequenceList.isEmpty())
		{
			return 1;
		}
		
		List<Integer> list = new ArrayList<Integer>();
		for (HashMap<String, String> map : sequenceList) {
			list.add(StringUtil.getIntValue(map, "vxlanconfigsequence"));
		}
		Collections.sort(list);
	
		for (int i = 1; i <= list.size(); i++) {
			if (!list.contains(i)) {
				return i;
			}
		}
		return list.get(list.size() - 1) + 1;*/
		// sequence获取逻辑变更  直接顺序添加节点
		StringBuffer sbSQL = new StringBuffer(" select max(vxlanconfigsequence) num from tab_vxlan_serv_param " +
				" where user_id=? and username=? and serv_type_id=? ");
		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		psql.setLong(1, checker.getUserId());
		psql.setString(2, checker.getNetUsername());
		psql.setLong(3, checker.getServTypeId());
		Map<String, String> map = DBOperation.getRecord(psql.getSQL());
		return StringUtil.getIntValue(map, "num") + 1;
	}
	
	/**
	 * 查询vxlan业务vlanid是否已存在
	 * @param checker
	 * @return
	 */
	public int queryVlanid(VXLANBaseChecker checker){
		StringBuffer sbSQL = new StringBuffer(" select count(1) as num from tab_vxlan_serv_param where user_id=? and xctcom_vlan =? and vxlanconfigsequence <> ?");
		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		psql.setLong(1, checker.getUserId());
		psql.setLong(2, checker.getXctcom_vlan());
		psql.setLong(3, checker.getvXLANConfigSequence());
		return StringUtil.getIntValue(DBOperation.getRecord(psql.getSQL()), "num");
	}
}
