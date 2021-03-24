package com.linkage.itms.rms.obj.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.rms.obj.VoiceServiceProfileLineObj;
import com.linkage.itms.rms.obj.WanConnSessObj;
import com.linkage.itms.rms.obj.domain.DeviceWireInfoObj;
import com.linkage.itms.rms.obj.domain.PONInfoOBJ;
import com.linkage.itms.rms.obj.domain.VoiceServiceProfileObj;
import com.linkage.itms.rms.obj.domain.WanObj;

/**
 * 
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2015年3月26日
 * @category com.linkage.itms.rms.obj.dao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DiagnoseDao
{
	private static final Logger logger = LoggerFactory.getLogger(DiagnoseDao.class);
	/**
	 * 
	 * @param device_id
	 * @return
	 */
	public List<HashMap<String, String>> getAllChannel(String device_id)
	{
		String sql ="select device_id, wan_id, wan_conn_id, wan_conn_sess_id, serv_list,sess_type,ip from cuc_gw_wan_conn_session where device_id=?";
		PrepareSQL psql = new PrepareSQL(sql);
		psql.setString(1, device_id);
		return DBOperation.getRecords(psql.getSQL());
	}
	
	public WanConnSessObj[] queryDevWanConnSession(HashMap<String, String> map){
		String device_id = map.get("device_id");
		String wan_id =  map.get("wan_id");
		String wan_conn_id =  map.get("wan_conn_id");
		String strSQL = "select a.*,b.vlan_id,b.vpi_id,b.vci_id from  cuc_gw_wan_conn_session a, cuc_gw_wan_conn b where a.device_id=? and a.wan_id=? and a.wan_conn_id=? and a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id";
		// mysql db
		if (3 == DBUtil.GetDB()) {
			strSQL = "select a.wan_id,a.wan_conn_id,a.wan_conn_sess_id,a.sess_type,a.enable,a.name,a.conn_type,a.serv_list,a.bind_port,a.username, " +
					"a.password,a.ip_type,a.ip,a.mask,a.gateway,a.dns_enab,a.dns,a.conn_status,a.last_conn_error,a.nat_enab,a.ppp_auth_protocol, " +
					"a.dial_num,a.work_mode,a.load_percent,a.backup_itfs,a.conn_trigger,a.gather_time,a.ip_mode,a.ip_ipv6,a.dns_ipv6, " +
					"b.vlan_id,b.vpi_id,b.vci_id from  cuc_gw_wan_conn_session a, cuc_gw_wan_conn b where a.device_id=? and a.wan_id=? and a.wan_conn_id=? and a.device_id=b.device_id and a.wan_id=b.wan_id and a.wan_conn_id=b.wan_conn_id";
		}
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setString(1, device_id);
		psql.setStringExt(2, wan_id, false);
		psql.setStringExt(3, wan_conn_id, false);
		// 执行查询
		List<HashMap<String, String>> rList = DBOperation.getRecords(psql.getSQL());
		
		WanConnSessObj[] wanConnSessObj = null;
		if (null != rList && rList.size() > 0) {
			int lSize = rList.size();
			wanConnSessObj = new WanConnSessObj[lSize];
			for (int i = 0; i < lSize; i++) {
				Map rMap = null;
				if (null != rList.get(i)
						&& (rMap = (Map) rList.get(i)).isEmpty() == false) {
					wanConnSessObj[i] = new WanConnSessObj();
					wanConnSessObj[i].setWanId(String.valueOf(rMap
							.get("wan_id")));
					wanConnSessObj[i].setWanConnId(String.valueOf(rMap
							.get("wan_conn_id")));
					wanConnSessObj[i].setWanConnSessId(String.valueOf(rMap
							.get("wan_conn_sess_id")));
					// session type:PPP OR IP
					wanConnSessObj[i].setSessType(String.valueOf(rMap
							.get("sess_type")));
					wanConnSessObj[i].setEnable(String.valueOf(rMap
							.get("enable")));
					// 连接名称
					wanConnSessObj[i].setName(String.valueOf(rMap.get("name")));
					// 连接类型 ConnectionType IP_Routed or IP_Bridged
					wanConnSessObj[i].setConnType(String.valueOf(rMap
							.get("conn_type")));
					// X_CT-COM_ServiceList TR069 or INTERNET or TR069&INTERNET
					// or OTHER
					wanConnSessObj[i].setServList(String.valueOf(rMap
							.get("serv_list")));
					// 绑定端口
					wanConnSessObj[i].setBindPort(String.valueOf(rMap
							.get("bind_port")));
					// 用户账号
					wanConnSessObj[i].setUsername(String.valueOf(rMap
							.get("username")));
					// 账号密码
					wanConnSessObj[i].setPassword(String.valueOf(rMap
							.get("password")));
					// AddressingType: DHCP or Static
					wanConnSessObj[i].setIpType(String.valueOf(rMap
							.get("ip_type")));
					// ip
					wanConnSessObj[i].setIp(String.valueOf(rMap.get("ip")));
					// IP mask
					wanConnSessObj[i].setMask(String.valueOf(rMap.get("mask")));
					// gateway
					wanConnSessObj[i].setGateway(String.valueOf(rMap
							.get("gateway")));
					// dns_enab: 1:开启 0:未
					wanConnSessObj[i].setDnsEnab(String.valueOf(rMap
							.get("dns_enab")));
					// dns
					wanConnSessObj[i].setDns(String.valueOf(rMap.get("dns")));
					// 连接状态
					wanConnSessObj[i].setStatus(String.valueOf(rMap
							.get("conn_status")));
					// 拨号错误码
					wanConnSessObj[i].setConnError(String.valueOf(rMap
							.get("last_conn_error")));
					// NAT开关
					wanConnSessObj[i].setNatEnable(String.valueOf(rMap
							.get("nat_enab")));
					// "vpi/vci"
					/**
					if(wanConnObj.getVpi_id() != null && wanConnObj.getVci_id() != null)
					{
						wanConnSessObj[i].setPvc(wanConnObj.getVpi_id() + "/"
								+ wanConnObj.getVci_id());
					}
					else
					{
						wanConnSessObj[i].setPvc(wanConnObj.getVpi_id());
					}
					
					**/
					//pvc
					if(null!=StringUtil.getStringValue(rMap,"vpi_id")){
						String pvc=StringUtil.getStringValue(rMap,"vpi_id");
						if(null!=StringUtil.getStringValue(rMap,"vci_id")){
							pvc=StringUtil.getStringValue(rMap,"vci_id")+"/"+pvc;
						}
						wanConnSessObj[i].setPvc(pvc);
					}
					
					// "vlanid"
					wanConnSessObj[i].setVlanid(StringUtil.getStringValue(rMap,"vlan_id",""));
					
					//PPPAuthenticationProtocol
					wanConnSessObj[i].setPppAuthProtocol(String.valueOf(rMap.get("ppp_auth_protocol")));
					
					//X_CT-COM_DialNumber
					wanConnSessObj[i].setDialNum(String.valueOf(rMap.get("dial_num")));
					
					//X_CT-COM_WorkMode
					wanConnSessObj[i].setWorkMode(String.valueOf(rMap.get("work_mode")));
					
					//X_CT-COM_LoadPercent
					wanConnSessObj[i].setLoadPercent(String.valueOf(rMap.get("load_percent")));
					
					//X_CT-COM_BackupInterface
					wanConnSessObj[i].setBackupItfs(String.valueOf(rMap.get("backup_itfs")));
					
					//ConnectionTrigger
					wanConnSessObj[i].setConnTrigger(String.valueOf(rMap.get("conn_trigger")));
					
					wanConnSessObj[i].setDeviceId(device_id);
					wanConnSessObj[i].setGatherTime(String.valueOf(rMap
							.get("gather_time")));
					
					// add by chenjie 2013-10-25 运营商信息
//					if(LipossGlobals.getLipossProperty("telecom").equals(Global.TELECOM_CUC))
//					{
						// CUC的vlanid在wanConnSess中
						//wanConnSessObj[i].setVlanid(String.valueOf(rMap.get("vlan_id")));
//					}
					wanConnSessObj[i].setIpMode(String.valueOf(rMap
							.get("ip_mode")));
					wanConnSessObj[i].setDns_ipv6(String.valueOf(rMap
							.get("ip_ipv6")));
					wanConnSessObj[i].setIp_ipv6(String.valueOf(rMap
							.get("dns_ipv6")));
					
				}
			}
		}
		return wanConnSessObj;
	}
	
	/**
	 * 移植自: com.linkage.module.gwms.dao.gw.VoiceServiceProfileLineDAO.getVoipProfLine(VoiceServiceProfileObj)
	 * @param voipProf
	 * @return
	 */
	public VoiceServiceProfileLineObj[] getVoipProfLine(VoiceServiceProfileObj voipProf){
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("select device_id,voip_id,prof_id,line_id,gather_time,enable,");
		sql.append("status,username,password,regist_result,physical_term_id from cuc_gw_voip_prof_line where ");
		sql.append(" device_id='");
		sql.append(voipProf.getDeviceId());
		sql.append("' and prof_id=");
		sql.append(voipProf.getProfId());
		PrepareSQL psql = new PrepareSQL(sql.toString());
    	psql.getSQL();
		List list = DBOperation.getRecords(psql.getSQL());
		
		VoiceServiceProfileLineObj[] rs = null;
		if(list.size()>0){
			rs = new VoiceServiceProfileLineObj[list.size()];
			for(int i=0;i<list.size();i++){
				Map one = (Map) list.get(i);
				rs[i] = new VoiceServiceProfileLineObj();
				rs[i].setDeviceId(String.valueOf(one.get("device_id")).toString());
				rs[i].setVoipId(String.valueOf(one.get("voip_id")).toString());
				rs[i].setProfId(String.valueOf(one.get("prof_id")).toString());
				rs[i].setLineId(String.valueOf(one.get("line_id")).toString());
				rs[i].setGatherTime(String.valueOf(one.get("gather_time")).toString());
				rs[i].setEnable(String.valueOf(one.get("enable")).toString());
				rs[i].setStatus(String.valueOf(one.get("status")).toString());
				rs[i].setRegistResult(String.valueOf(one.get("regist_result")));
				rs[i].setUsername(String.valueOf(one.get("username")).toString());
				rs[i].setPassword(String.valueOf(one.get("password")).toString());
				rs[i].setPhysicalTermId(String.valueOf(one.get("physical_term_id")).toString());
				rs[i].setIp(voipProf.getIp());
			}
		}
		
		return rs;
	}
	
	
	/**
	 * 获取指定device_id的VOIP节点，不包含线路信息  IMS 软件换
	 * 
	 * @param deviceId
	 * @return
	 */
	public VoiceServiceProfileObj[] getVoipProf(Map map){
		
		StringBuffer sql = new StringBuffer();
		
		sql.append("select device_id,voip_id,prof_id,gather_time,");
		sql.append(" prox_serv,prox_port,prox_serv_2,prox_port_2,regi_serv,");
		sql.append(" regi_port,stand_regi_serv,stand_regi_port,out_bound_proxy,");
		sql.append(" out_bound_port,stand_out_bound_proxy,stand_out_bound_port");
		sql.append(" from cuc_gw_voip_prof where device_id='");
		sql.append(StringUtil.getStringValue(map.get("device_id")));
		sql.append("'");
		PrepareSQL psql = new PrepareSQL(sql.toString());
		List list = DBOperation.getRecords(psql.getSQL());
		//int wan_id=	Integer.parseInt(StringUtil.getStringValue(map.get("wan_id")));
		//int wan_conn_id=Integer.parseInt(StringUtil.getStringValue(map.get("wan_conn_id")));
		//int wan_conn_sess_id=Integer.parseInt(StringUtil.getStringValue(map.get("wan_conn_sess_id")));
		//int sess_type=Integer.parseInt(StringUtil.getStringValue(map.get("sess_type")));
		String ip=StringUtil.getStringValue(map.get("ip"));
		VoiceServiceProfileObj[] rs = null;
		if(list.size()>0){
			rs = new VoiceServiceProfileObj[list.size()];
			for(int i=0;i<list.size();i++){
				Map one = (Map) list.get(i);
				rs[i] = new VoiceServiceProfileObj();
				rs[i].setDeviceId(String.valueOf(one.get("device_id")).toString());
				rs[i].setVoipId(String.valueOf(one.get("voip_id")).toString());
				rs[i].setProfId(String.valueOf(one.get("prof_id")).toString());
				rs[i].setGatherTime(String.valueOf(one.get("gather_time")).toString());
				rs[i].setProxServ(String.valueOf(one.get("prox_serv")).toString());
				rs[i].setProxPort(String.valueOf(one.get("prox_port")).toString());
				rs[i].setProxServ2(String.valueOf(one.get("prox_serv_2")).toString());
				rs[i].setProxPort2(String.valueOf(one.get("prox_port_2")).toString());
				rs[i].setRegiServ(String.valueOf(one.get("regi_serv")).toString());
				rs[i].setRegiPort(String.valueOf(one.get("regi_port")).toString());
				rs[i].setStandRegiServ(String.valueOf(one.get("stand_regi_serv")).toString());
				rs[i].setStandRegiPort(String.valueOf(one.get("stand_regi_port")).toString());
				rs[i].setOutBoundProxy(String.valueOf(one.get("out_bound_proxy")).toString());
				rs[i].setOutBoundPort(String.valueOf(one.get("out_bound_port")).toString());
				rs[i].setStandOutBoundProxy(String.valueOf(one.get("stand_out_bound_proxy")).toString());
				rs[i].setStandOutBoundPort(String.valueOf(one.get("stand_out_bound_port")).toString());
				rs[i].setIp(ip);
				//rs[i].setWan_id(wan_id);
				//rs[i].setWan_conn_id(wan_conn_id);
				//rs[i].setWan_conn_sess_id(wan_conn_sess_id);
				//rs[i].setSess_type(sess_type);
			}
		}
		
		return rs;
	}
	
	
	/**
	 * 获取设备的上行方式，
	 * 
	 * @param 设备ID
	 * @author Jason(3412)
	 * @date 2009-10-29
	 * @return int 1:ADSL 2:Ethernet 3:PON(EPON) 4:POTS -1:未知
	 */
	public int getAccessType(String deviceId) {
		//否则查询WAN连接信息表
		
		WanObj wanObj = getWan(deviceId, "1");
		
		if (null != wanObj) {
			if ("DSL".equals(wanObj.getAccessType())) {
				return 1;
			} else if ("Ethernet".equals(wanObj.getAccessType())) {
				return 2;
			} else if ("EPON".equalsIgnoreCase(wanObj.getAccessType()) 
					|| "PON".equalsIgnoreCase(wanObj.getAccessType())
					|| "X_CU_PON".equalsIgnoreCase(wanObj.getAccessType())) {
				return 3;
			} else if ("GPON".equalsIgnoreCase(wanObj.getAccessType())
					|| "X_CU_GPON".equalsIgnoreCase(wanObj.getAccessType())) {
				return 4;
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}
	
	/**
	 * 移植自:com.linkage.module.gwms.dao.gw.WanDAO.getWan(String, String)
	 * 根据deviceId和wanId获得一个WanObj
	 * @author gongsj
	 * @date 2009-7-16
	 * @param deviceId
	 * @param wanId
	 * @return
	 */
	public WanObj getWan(String deviceId, String wanId) {
		WanObj wanObj = null;
		String strSQL = "select * from cuc_gw_wan where device_id=? and wan_id=?";
		// mysql db
		if (3 == DBUtil.GetDB()) {
			strSQL = "select wan_conn_num,access_type,gather_time from cuc_gw_wan where device_id=? and wan_id=?";
		}
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setString(1, deviceId);
		psql.setStringExt(2, wanId, false);
		
		Map rMap = DBOperation.getRecord(psql.getSQL());
		if (null != rMap && rMap.isEmpty() == false) {
			wanObj = new WanObj();
			wanObj.setDevId(deviceId);
			wanObj.setWanId(wanId);
			wanObj.setWanConnNum(String.valueOf(rMap.get("wan_conn_num")));
			wanObj.setAccessType(String.valueOf(rMap.get("access_type")));
			wanObj.setGatherTime(String.valueOf(rMap.get("gather_time")));
			
//			//从配置文件读取参数，如果是2，accessType从versiontypeinfo表关联获取
//			if (2 == LipossGlobals.accessTypeFrom())
//			{
//				String accessTypeString = getAccessTypeFromVersion(deviceId);
//				if (!StringUtil.IsEmpty(accessTypeString))
//				{
//					wanObj.setAccessType(accessTypeString);
//				}
//			}
		}
		return wanObj;
		
	}
	
	/**
	 * 移植自:com.linkage.module.gwms.dao.gw.WireInfoDAO.queryDevWireInfo(String)
	 * 获取设备线路信息
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-24
	 * @return DeviceWireInfoObj[]
	 */
	public DeviceWireInfoObj[] queryDevWireInfo(String deviceId) {
		if (null == deviceId) {
			return null;
		}
		DeviceWireInfoObj[] wireInfoObj = null; 
		String strSQL = "select * from cuc_gw_wan_wireinfo where device_id = ?";
		// mysql db
		if (3 == DBUtil.GetDB()) {
			strSQL = "select status,data_path,down_attenuation,down_maxrate,interleave_depth,modulation_type,up_attenuation,up_maxrate,wan_inst,up_noise,down_noise from cuc_gw_wan_wireinfo where device_id = ?";
		}
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setString(1, deviceId);
		List rList = DBOperation.getRecords(psql.getSQL());
		if (null != rList && rList.size() > 0) {
			int lSize = rList.size();
			wireInfoObj = new DeviceWireInfoObj[lSize];
			for (int i = 0; i < lSize; i++) {
				Map rMap = (Map) rList.get(i);
				wireInfoObj[i] = new DeviceWireInfoObj();
				logger.debug(rMap.toString());
				if (null != rMap && rMap.isEmpty() == false) {
					wireInfoObj[i].setWireStatus(String.valueOf(rMap
							.get("status")));
					wireInfoObj[i].setDataPath(String.valueOf(rMap
							.get("data_path")));
					if (null != rMap.get("down_attenuation")) {
						wireInfoObj[i].setDownstreamAttenuation(Long
								.valueOf(String.valueOf(rMap
										.get("down_attenuation"))));
					}
					if (null != rMap.get("down_maxrate")) {
						wireInfoObj[i].setDownstreamMaxRate(Long.valueOf(String
								.valueOf(rMap.get("down_maxrate"))));
					}
					wireInfoObj[i].setInterleaveDepth(String.valueOf(rMap
							.get("interleave_depth")));
					wireInfoObj[i].setModulationType(String.valueOf(rMap
							.get("modulation_type")));
					if (null != rMap.get("up_attenuation")) {
						wireInfoObj[i].setUpstreamAttenuation(Long
								.valueOf(String.valueOf(rMap
										.get("up_attenuation"))));
					}
					if (null != rMap.get("up_maxrate")) {
						wireInfoObj[i].setUpstreamMaxRate(Long.valueOf(String
								.valueOf(rMap.get("up_maxrate"))));
					}
					wireInfoObj[i].setWanDeviceInstance(String.valueOf(rMap
							.get("wan_inst")));
					wireInfoObj[i].setUpNoise(StringUtil.getStringValue(rMap
									.get("up_noise")));
					wireInfoObj[i].setDownNoise(StringUtil.getStringValue(rMap
									.get("down_noise")));
				}
			}
		}
		return wireInfoObj;
	}
	
	/**
	 * 移植自:com.linkage.module.gwms.diagnostics.dao.DeviceInfoDAO.queryPONInfo(String)
	 * 查询PON设备信息
	 *
	 * @author wangsenbo
	 * @date Nov 4, 2010
	 * @param 
	 * @return PONInfoOBJ[]
	 */
	public PONInfoOBJ[] queryPONInfo(String deviceId){
		logger.debug("queryPONInfo in");
		if (null == deviceId) {
			return null;
		}
		PONInfoOBJ[] ponInfoOBJ = null;
		String strSQL = "select * from cuc_gw_wan_wireinfo_epon where device_id = ?";
		// mysql db
		if (3 == DBUtil.GetDB()) {
			strSQL = "select status,tx_power,rx_power,transceiver_temperature,supply_vottage,bias_current,bytes_sent,bytes_received,packets_sent, " +
					"packets_received,sunicast_packets,runicast_packets,smulticast_packets,rmulticast_packets,sbroadcast_packets,rbroadcast_packets, " +
					"fec_error,hec_error,drop_packets,spause_packets,rpause_packets from cuc_gw_wan_wireinfo_epon where device_id = ?";
		}
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setString(1, deviceId);
		List rList = DBOperation.getRecords(psql.getSQL());
		if (null != rList && rList.size() > 0) {
			int lSize = rList.size();
			ponInfoOBJ = new PONInfoOBJ[lSize];
			for (int i = 0; i < lSize; i++) {
				Map rMap = (Map) rList.get(i);
				ponInfoOBJ[i] = new PONInfoOBJ();
				logger.debug(rMap.toString());
				if (null != rMap && rMap.isEmpty() == false) {
					ponInfoOBJ[i].setStatus(StringUtil.getStringValue(rMap
							.get("status")));
				//int tx_power=Integer.parseInt(StringUtil.getStringValue(rMap.get("tx_power")));
			//	int rx_power=Integer.parseInt(StringUtil.getStringValue(rMap.get("rx_power")));
			double	tx_power=StringUtil.getDoubleValue(rMap.get("tx_power"));
			double	rx_power=StringUtil.getDoubleValue(rMap.get("rx_power"));
				
				if(tx_power>30){
					double temp_tx_power= (Math.log(tx_power/10000) /Math.log(10))*10;
					tx_power=(int) temp_tx_power;
					if(tx_power%10 >=5){
						tx_power=(tx_power/10+1)*10;
					}
					else{
						tx_power=tx_power/10*10;
					}
				}
				if(rx_power>30){
					double temp_rx_power= (Math.log(rx_power/10000) /Math.log(10))*10;
					rx_power=(int) temp_rx_power;
					if(rx_power%10 >=5){
						rx_power=(rx_power/10+1)*10;
					}
					else{
						rx_power=rx_power/10*10;
					}
				}
					ponInfoOBJ[i].setTxpower(tx_power+"");
					ponInfoOBJ[i].setRxpower(rx_power+"");
					ponInfoOBJ[i].setTransceiverTemperature(StringUtil.getStringValue(rMap
							.get("transceiver_temperature")));
					ponInfoOBJ[i].setSupplyVottage(StringUtil.getStringValue(rMap
							.get("supply_vottage")));
					ponInfoOBJ[i].setBiasCurrent(StringUtil.getStringValue(rMap
							.get("bias_current")));
					ponInfoOBJ[i].setBytesSent(StringUtil.getStringValue(rMap
							.get("bytes_sent")));
					ponInfoOBJ[i].setBytesReceived(StringUtil.getStringValue(rMap
							.get("bytes_received")));
					ponInfoOBJ[i].setPacketsSent(StringUtil.getStringValue(rMap
							.get("packets_sent")));
					ponInfoOBJ[i].setPacketsReceived(StringUtil.getStringValue(rMap
							.get("packets_received")));
					
					ponInfoOBJ[i].setSunicastPackets(StringUtil.getStringValue(rMap
							.get("sunicast_packets")));
					ponInfoOBJ[i].setRunicastPackets(StringUtil.getStringValue(rMap
							.get("runicast_packets")));
					ponInfoOBJ[i].setSmulticastPackets(StringUtil.getStringValue(rMap
							.get("smulticast_packets")));
					ponInfoOBJ[i].setRmulticastPackets(StringUtil.getStringValue(rMap
							.get("rmulticast_packets")));
					ponInfoOBJ[i].setSbroadcastPackets(StringUtil.getStringValue(rMap
							.get("sbroadcast_packets")));
					ponInfoOBJ[i].setRbroadcastPackets(StringUtil.getStringValue(rMap
							.get("rbroadcast_packets")));
					ponInfoOBJ[i].setFecError(StringUtil.getStringValue(rMap
							.get("fec_error")));
					ponInfoOBJ[i].setHecError(StringUtil.getStringValue(rMap
							.get("hec_error")));
					ponInfoOBJ[i].setDropPackets(StringUtil.getStringValue(rMap
							.get("drop_packets")));
					ponInfoOBJ[i].setSpausePackets(StringUtil.getStringValue(rMap
							.get("spause_packets")));
					ponInfoOBJ[i].setRpausePackets(StringUtil.getStringValue(rMap
							.get("rpause_packets")));
				}
			}
		}
		return ponInfoOBJ;
	}
	
	/**
	 * 获取LAN侧信息
	 * 移植自:com.linkage.module.gwms.diagnostics.dao.DeviceInfoDAO.queryLanEth(String)
	 * @param deviceId
	 * @return
	 */
	public List queryLanEth(String deviceId){
		
		logger.debug("queryLanEth(deviceId:{})",deviceId);
		
		StringBuffer sql = new StringBuffer();
		sql.append("select device_id,lan_id,lan_eth_id,enable,status,");
		sql.append("mac_address,gather_time,max_bit_rate,dupl_mode,byte_sent," );
		sql.append("byte_rece,pack_sent,pack_rece,error_sent,drop_sent,");
		sql.append("error_rece,drop_rece from cuc_gw_lan_eth where device_id='");
		sql.append(deviceId);
		sql.append("'");
		PrepareSQL psql = new PrepareSQL(sql.toString());
		return DBOperation.getRecords(psql.getSQL());
	}
	
	
	
	/**
	 * 获取WLAN
	 * 移植自:com.linkage.module.gwms.dao.gw.WlanDAO.getData(String)
	 * @param device_id
	 * @return
	 */
	public List getData(String deviceId)
	{
		logger.debug("WanObj[] getWlan({})", deviceId);
		if (deviceId == null)
		{
			logger.debug("deviceId == null");
			return null;
		}
		String sql = "select * from cuc_gw_lan_wlan where device_id='" + deviceId + "'";
		// mysql db
		if (3 == DBUtil.GetDB()) {
			sql = "select ssid,status,associated_num,enable from cuc_gw_lan_wlan where device_id='" + deviceId + "'";
		}
		PrepareSQL psql = new PrepareSQL(sql);
		logger.warn("WLAN参数查询");
		return DBOperation.getRecords(psql.getSQL());
	}
}
