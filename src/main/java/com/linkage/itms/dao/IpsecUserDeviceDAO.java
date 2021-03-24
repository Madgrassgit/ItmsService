package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dispatch.obj.IpsecBaseChecker;

public class IpsecUserDeviceDAO {

	private static Logger logger = LoggerFactory.getLogger(IpsecUserDeviceDAO.class);

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
		String table_ipsec_serv_param = "tab_ipsec_serv_param";
		
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
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b, "+ table_ipsec_serv_param + " c ");
			psql.append(" where a.user_id=b.user_id and b.user_id=c.user_id and b.serv_status= 1 ");
			psql.append(" and b.username='" + username + "' and b.serv_type_id=10 ");
			psql.append(" order by a.updatetime desc ");
			break;
		case 4:
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b, "+ table_ipsec_serv_param + " c ");
			psql.append(" where a.user_id=b.user_id and b.user_id=c.user_id and b.serv_status= 1 ");
			psql.append(" and a.username='" + username + "' and b.serv_type_id=10 ");
			psql.append(" order by a.updatetime desc ");
			break;
		default:
			psql.append(" from " + table_customer + " a, " + table_serv_info + " b, "+ table_ipsec_serv_param + " c ");
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
	public int saveSheet(IpsecBaseChecker checker){
		logger.debug("saveSheet");
		
		int index = 0;

		StringBuffer sbSQL = new StringBuffer(" insert into tab_ipsec_serv_param (user_id,username,serv_type_id,request_id, "
				+ " remote_ip,ike_localname,ike_remotename,ike_presharekey, "
				+ " open_date,updatetime,remote_subnet,local_subnet,serv_status ");
		
		if(!StringUtil.IsEmpty(checker.getiPSecType())){
			sbSQL.append(" ,ipsec_type ");
		}
		if(!StringUtil.IsEmpty(checker.getExchangeMode())){
			sbSQL.append(" ,exchange_mode ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEAuthenticationAlgorithm())){
			sbSQL.append(" ,ike_auth_algorithm ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEAuthenticationMethod())){
			sbSQL.append(" ,ike_auth__method ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEDHGroup())){
			sbSQL.append(" ,ike_dhgroup ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEIDType())){
			sbSQL.append(" ,ike_idtype ");
		}
		if(!StringUtil.IsEmpty(checker.getiPSecTransform())){
			sbSQL.append(" ,ipsec_transform ");
		}
		if(!StringUtil.IsEmpty(checker.geteSPAuthenticationAlgorithm())){
			sbSQL.append(" ,esp_auth_algorithem ");
		}
		if(!StringUtil.IsEmpty(checker.geteSPEncryptionAlgorithm())){
			sbSQL.append(" ,esp_encrypt_algorithm ");
		}
		if(!StringUtil.IsEmpty(checker.getiPSecPFS())){
			sbSQL.append(" ,ipsec_pfs ");
		}
		if(!StringUtil.IsEmpty(checker.getaHAuthenticationAlgorithm())){
			sbSQL.append(" ,ah_auth_algorithm ");
		}
		if(!StringUtil.IsEmpty(checker.getiPSecEncapsulationMode())){
			sbSQL.append(" ,ipsec_encapsulation_mode ");
		}
		if(0!=checker.getdPDEnable()){
			sbSQL.append(" ,dpd_enable ");
		}
		if(10!=checker.getdPDThreshold()){
			sbSQL.append(" ,dpd_threshold ");
		}
		if(5!=checker.getdPDRetry()){
			sbSQL.append(" ,dpd_retry ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEEncryptionAlgorithm())){
			sbSQL.append(" ,ike_encryption_algorithm ");
		}
		if(10800!=checker.getiKESAPeriod()){
			sbSQL.append(" ,ike_saperiod ");
		}
		if(3600!=checker.getiPSecSATimePeriod()){
			sbSQL.append(" ,ipsec_satime_period ");
		}
		if(1843200!=checker.getiPSecSATrafficPeriod()){
			sbSQL.append(" ,ipsec_satraffic_period ");
		}
		if(!StringUtil.IsEmpty(checker.getRemoteDomain())){
			sbSQL.append(" ,remote_domain ");
		}
		sbSQL.append(" ) values (?,?,?,?,?,  ?,?,?,?,?,  ?,?,? ");
		
		if(!StringUtil.IsEmpty(checker.getiPSecType())){
			sbSQL.append(" ,'"+checker.getiPSecType()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getExchangeMode())){
			sbSQL.append(" ,'"+checker.getExchangeMode()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEAuthenticationAlgorithm())){
			sbSQL.append(" ,'"+checker.getiKEAuthenticationAlgorithm()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEAuthenticationMethod())){
			sbSQL.append(" ,'"+checker.getiKEAuthenticationMethod()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEDHGroup())){
			sbSQL.append(" ,'"+checker.getiKEDHGroup()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEIDType())){
			sbSQL.append(" ,'"+checker.getiKEIDType()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiPSecTransform())){
			sbSQL.append(" ,'"+checker.getiPSecTransform()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.geteSPAuthenticationAlgorithm())){
			sbSQL.append(" ,'"+checker.geteSPAuthenticationAlgorithm()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.geteSPEncryptionAlgorithm())){
			sbSQL.append(" ,'"+checker.geteSPEncryptionAlgorithm()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiPSecPFS())){
			sbSQL.append(" ,'"+checker.getiPSecPFS()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getaHAuthenticationAlgorithm())){
			sbSQL.append(" ,'"+checker.getaHAuthenticationAlgorithm()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiPSecEncapsulationMode())){
			sbSQL.append(" ,'"+checker.getiPSecEncapsulationMode()+"' ");
		}
		if(0!=checker.getdPDEnable()){
			sbSQL.append(" ,"+checker.getdPDEnable()+" ");
		}
		if(10!=checker.getdPDThreshold()){
			sbSQL.append(" ,"+checker.getdPDThreshold()+" ");
		}
		if(5!=checker.getdPDRetry()){
			sbSQL.append(" ,"+checker.getdPDRetry()+" ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEEncryptionAlgorithm())){
			sbSQL.append(" ,'"+checker.getiKEEncryptionAlgorithm()+"' ");
		}
		if(10800!=checker.getiKESAPeriod()){
			sbSQL.append(" ,"+checker.getiKESAPeriod()+" ");
		}
		if(3600!=checker.getiPSecSATimePeriod()){
			sbSQL.append(" ,"+checker.getiPSecSATimePeriod()+" ");
		}
		if(1843200!=checker.getiPSecSATrafficPeriod()){
			sbSQL.append(" ,"+checker.getiPSecSATrafficPeriod()+" ");
		}
		if(!StringUtil.IsEmpty(checker.getRemoteDomain())){
			sbSQL.append(" ,'"+checker.getRemoteDomain()+"' ");
		}
		sbSQL.append(" ) ");
		
		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		
		psql.setLong(++index, checker.getUserId());
		psql.setString(++index, checker.getNetUsername());
		psql.setInt(++index, checker.getServTypeId());
		psql.setString(++index, checker.getRequestID());
		psql.setString(++index, checker.getRemoteIP());
		psql.setString(++index, checker.getiKELocalName());
		psql.setString(++index, checker.getiKERemoteName());
		psql.setString(++index, checker.getiKEPreshareKey());
		psql.setLong(++index, System.currentTimeMillis()/1000);
		psql.setLong(++index, System.currentTimeMillis()/1000);
		psql.setString(++index, checker.getRemoteSubnet());
		psql.setString(++index, checker.getLocalSubnet());
		psql.setInt(++index, 1);
		
		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 更新工单信息
	 * @param BaseChecker: checker
	 * 
	 */
	public int updateSheet(IpsecBaseChecker checker){
		logger.debug("updateSheet");
		
		int index = 0;

		StringBuffer sbSQL = new StringBuffer(" update tab_ipsec_serv_param set serv_status=1,open_status=0,request_id=?, "
				+ " remote_ip=?,ike_localname=?,ike_remotename=?,ike_presharekey=?, "
				+ " updatetime=?,remote_subnet=?,local_subnet=? ");
		
		if(!StringUtil.IsEmpty(checker.getiPSecType())){
			sbSQL.append(" ,ipsec_type='"+checker.getiPSecType()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getExchangeMode())){
			sbSQL.append(" ,exchange_mode='"+checker.getExchangeMode()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEAuthenticationAlgorithm())){
			sbSQL.append(" ,ike_auth_algorithm='"+checker.getiKEAuthenticationAlgorithm()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEAuthenticationMethod())){
			sbSQL.append(" ,ike_auth__method='"+checker.getiKEAuthenticationMethod()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEEncryptionAlgorithm())){
			sbSQL.append(" ,ike_encryption_algorithm='"+checker.getiKEEncryptionAlgorithm()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEDHGroup())){
			sbSQL.append(" ,ike_dhgroup='"+checker.getiKEDHGroup()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiKEIDType())){
			sbSQL.append(" ,ike_idtype='"+checker.getiKEIDType()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiPSecTransform())){
			sbSQL.append(" ,ipsec_transform='"+checker.getiPSecTransform()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.geteSPAuthenticationAlgorithm())){
			sbSQL.append(" ,esp_auth_algorithem='"+checker.geteSPAuthenticationAlgorithm()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.geteSPEncryptionAlgorithm())){
			sbSQL.append(" ,esp_encrypt_algorithm='"+checker.geteSPEncryptionAlgorithm()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiPSecPFS())){
			sbSQL.append(" ,ipsec_pfs='"+checker.getiPSecPFS()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getaHAuthenticationAlgorithm())){
			sbSQL.append(" ,ah_auth_algorithm='"+checker.getaHAuthenticationAlgorithm()+"' ");
		}
		if(!StringUtil.IsEmpty(checker.getiPSecEncapsulationMode())){
			sbSQL.append(" ,ipsec_encapsulation_mode='"+checker.getiPSecEncapsulationMode()+"' ");
		}
		if(0!=checker.getdPDEnable()){
			sbSQL.append(" ,dpd_enable="+checker.getdPDEnable()+" ");
		}
		if(10!=checker.getdPDThreshold()){
			sbSQL.append(" ,dpd_threshold="+checker.getdPDThreshold()+" ");
		}
		if(5!=checker.getdPDRetry()){
			sbSQL.append(" ,dpd_retry="+checker.getdPDRetry()+" ");
		}
		if(10800!=checker.getiKESAPeriod()){
			sbSQL.append(" ,ike_saperiod="+checker.getiKESAPeriod()+" ");
		}
		if(3600!=checker.getiPSecSATimePeriod()){
			sbSQL.append(" ,ipsec_satime_period="+checker.getiPSecSATimePeriod()+" ");
		}
		if(1843200!=checker.getiPSecSATrafficPeriod()){
			sbSQL.append(" ,ipsec_satraffic_period="+checker.getiPSecSATrafficPeriod()+" ");
		}
		if(!StringUtil.IsEmpty(checker.getRemoteDomain())){
			sbSQL.append(" ,remote_domain='"+checker.getRemoteDomain()+"' ");
		}
		sbSQL.append(" where user_id=? and username=? and serv_type_id=? ");

		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		
		psql.setString(++index, checker.getRequestID());
		psql.setString(++index, checker.getRemoteIP());		
		psql.setString(++index, checker.getiKELocalName());
		psql.setString(++index, checker.getiKERemoteName());
		psql.setString(++index, checker.getiKEPreshareKey());
		psql.setLong(++index, System.currentTimeMillis()/1000);
		psql.setString(++index, checker.getRemoteSubnet());
		psql.setString(++index, checker.getLocalSubnet());
		psql.setLong(++index, checker.getUserId());
		psql.setString(++index, checker.getNetUsername());
		psql.setLong(++index, checker.getServTypeId());

		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 销户删除工单信息
	 * @param BaseChecker: checker 
	 * 
	 */
	public int deleteSheet(IpsecBaseChecker checker){
		logger.debug("deleteSheet");

		StringBuffer sbSQL = new StringBuffer(" delete from tab_ipsec_serv_param " +
				" where user_id=? and username=? and serv_type_id=? ");
		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		psql.setLong(1, checker.getUserId());
		psql.setString(2, checker.getNetUsername());
		psql.setLong(3, checker.getServTypeId());

		return DBOperation.executeUpdate(psql.getSQL());
	}
	
	/**
	 * 检查工单信息是否存在
	 * @param BaseChecker: checker user_id,username, serv_type_id
	 * 
	 */
	public boolean hasSheet(IpsecBaseChecker checker){
		logger.debug("hasSheet");

		StringBuffer sbSQL = new StringBuffer(" select count(1) sheetnum from tab_ipsec_serv_param " +
				" where user_id=? and username=? and serv_type_id=? ");
		PrepareSQL psql = new PrepareSQL(sbSQL.toString());
		psql.setLong(1, checker.getUserId());
		psql.setString(2, checker.getNetUsername());
		psql.setLong(3, checker.getServTypeId());

		if(StringUtil.getIntValue(DBOperation.getRecord(psql.getSQL()), "sheetnum", 0) > 0){
			return true;
		}
		return false;
	}
	
}
