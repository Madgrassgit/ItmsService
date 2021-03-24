package com.linkage.itms.dispatch.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dispatch.service.CloudDeleteStaticRtCfgService;
import com.linkage.itms.dispatch.service.CloudDeleteVXLANPublicIPNatCfgService;
import com.linkage.itms.dispatch.service.CloudGetWayRebootService;
import com.linkage.itms.dispatch.service.CloudOpenVXLANCRMService;
import com.linkage.itms.dispatch.service.CloudPingDiagnosticService;
import com.linkage.itms.dispatch.service.CloudPingIPSecDiagnosticService;
import com.linkage.itms.dispatch.service.CloudPingVXLANDiagnosticService;
import com.linkage.itms.dispatch.service.CloudQueryBssStatsService;
import com.linkage.itms.dispatch.service.CloudQueryConfigureService;
import com.linkage.itms.dispatch.service.CloudQueryDevOnlineService;
import com.linkage.itms.dispatch.service.CloudQueryGetWayService;
import com.linkage.itms.dispatch.service.CloudQueryServIpAddrService;
import com.linkage.itms.dispatch.service.CloudQueryStaticRtCfgService;
import com.linkage.itms.dispatch.service.CloudQueryVXLANBssStatsService;
import com.linkage.itms.dispatch.service.CloudQueryVXLANConfigureService;
import com.linkage.itms.dispatch.service.CloudQueryVXLANGetWayService;
import com.linkage.itms.dispatch.service.CloudQueryVXLANPublicIPNatCfgService;
import com.linkage.itms.dispatch.service.CloudQueryVpnStatsService;
import com.linkage.itms.dispatch.service.CloudServiceDoneService;
import com.linkage.itms.dispatch.service.CloudStaticRtCfgService;
import com.linkage.itms.dispatch.service.CloudVXLANPublicIPNatCfgService;
import com.linkage.itms.dispatch.service.CloudVXLANServiceDoneService;
import com.linkage.itms.dispatch.service.DeleteIpsecVPNService;
import com.linkage.itms.dispatch.service.DeleteVXLANService;
import com.linkage.itms.dispatch.service.OpenIpsecVPNService;
import com.linkage.itms.dispatch.service.OpenVXLANService;
import com.linkage.itms.dispatch.service.UpdateIpsecVPNService;
import com.linkage.itms.dispatch.service.UpdateVXLANService;

/**
 * 向综调接口提供的服务类
 * 
 * @author Jason(3412)
 * @date 2010-3-31
 */
public class CloudCallService { 

	//日志记录
	private static Logger logger = LoggerFactory.getLogger(CloudCallService.class);

	/**
	 * 4.2.1	网关信息查询接口
	 * @return
	 */
	public String QueryGetWay(String param) {
		logger.debug("QueryGetWay()");
		return new CloudQueryGetWayService().work(param);
	}

	/**
	 * 4.2.2	网关业务ip地址查询接口
	 * @return
	 */
	public String QueryServIpAddr(String param) {
		logger.debug("QueryServIpAddr()");
		return new CloudQueryServIpAddrService().work(param);
	}

	/**
	 * 4.2.3	网关状态查询接口
	 * @return
	 */
	public String QueryVpnStats(String param) {
		logger.debug("QueryVpnStats()");
		return new CloudQueryVpnStatsService().work(param);
	}

	/**
	 * 4.2.4	网关在线状态查询接口
	 * @return
	 */
	public String QueryDevOnline(String param) {
		logger.debug("QueryDevOnline()");
		return new CloudQueryDevOnlineService().work(param);
	}

	/**
	 * 4.2.5	Ping诊断接口
	 * @return
	 */
	public String PingDiagnostic(String param) {
		logger.debug("PingDiagnostic()");
		return new CloudPingDiagnosticService().work(param);
	}

	/**
	 * 4.2.6	业务下发结果查询接口
	 * @return
	 */
	public String QueryBssStats(String param) {
		logger.debug("QueryBssStats()");
		return new CloudQueryBssStatsService().work(param);
	}

	/**
	 * 4.2.7	业务下发接口
	 * @return
	 */
	public String ServiceDone(String param) {
		logger.debug("ServiceDone()");
		return new CloudServiceDoneService().work(param);
	}

	/**
	 * 4.2.8	网关重启接口
	 * @return
	 */
	public String GetWayReboot(String param) {
		logger.debug("GetWayReboot()");
		return new CloudGetWayRebootService().work(param);
	}

	/**
	 * 4.2.9	IPSecVPN配置参数查询接口
	 * @return
	 */
	public String QueryConfigure(String param) {
		logger.debug("QueryConfigure()");
		return new CloudQueryConfigureService().work(param);
	}
	
	/**
	 * ITMS+向翼翮提供的业务开通工单的接口
	 * 
	 * @param 综调接口XML字符串参数
	 * @author chenxj6
	 * @date 2017-10-19
	 * @return String 回参的XML字符串
	 */
	public String OpenIpsecVPN(String param) {
		logger.debug("OpenIpsecVPN[{}]", param);
		return new OpenIpsecVPNService().work(param);
	}

	/**
	 * ITMS+向翼翮提供的业务修改工单的接口
	 * 
	 * @param 综调接口XML字符串参数
	 * @author chenxj6
	 * @date 2017-10-19
	 * @return String 回参的XML字符串
	 */
	public String UpdateIpsecVPN(String param) {
		logger.debug("UpdateIpsecVPN[{}]", param);
		return new UpdateIpsecVPNService().work(param);
	}

	/**
	 * ITMS+向翼翮提供的业务销户工单的接口
	 * 
	 * @param 综调接口XML字符串参数
	 * @author chenxj6
	 * @date 2017-10-19
	 * @return String 回参的XML字符串
	 */
	public String DeleteIpsecVPN(String param) {
		logger.debug("deleteIpsecVPN[{}]", param);
		return new DeleteIpsecVPNService().work(param);
	}

	/**
	 * 查询网关是否能通过IPSec通道PING通对端
	 * @param param
	 * @return
	 */
	public String PingIPSecDiagnostic(String param) {
		logger.debug("pingIPSecDiagnostic[{}]", param);
		return new CloudPingIPSecDiagnosticService().work(param);
	}
	
	/**
	 * VXLAN业务开通工单接口
	 * 
	 * @param 
	 * @author banyr
	 * @date 2018-11-28
	 * @return String 回参的XML字符串
	 */
	public String OpenVXLAN(String param) {
		logger.debug("OpenIpsecVPN[{}]", param);
		return new OpenVXLANService().work(param);
	}
	
	/**
	 * VXLAN业务修改工单接口
	 * 
	 * @param 
	 * @author banyr
	 * @date 2018-11-28
	 * @return String 回参的XML字符串
	 */
	public String UpdateVXLAN(String param) {
		logger.debug("OpenIpsecVPN[{}]", param);
		return new UpdateVXLANService().work(param);
	}
	
	/**
	 * VXLAN业务销户工单接口
	 * 
	 * @param 
	 * @author banyr
	 * @date 2018-11-28
	 * @return String 回参的XML字符串
	 */
	public String DeleteVXLAN(String param) {
		logger.debug("OpenIpsecVPN[{}]", param);
		return new DeleteVXLANService().work(param);
	}
	
	/**
	 * VXLAN网关信息查询接口
	 * @return
	 */
	public String QueryVXLANGetWay(String param) {
		logger.debug("QueryVXLANGetWay()");
		return new CloudQueryVXLANGetWayService().work(param);
	}
	
	/**
	 * VXLAN业务下发结果查询接口
	 * @return
	 */
	public String QueryVXLANBssStats(String param) {
		logger.debug("QueryVXLANBssStats()");
		return new CloudQueryVXLANBssStatsService().work(param);
	}
	
	/**
	 * VXLAN业务下发接口
	 * @return
	 */
	public String VXLANServiceDone(String param) {
		logger.debug("VXLANServiceDone()");
		return new CloudVXLANServiceDoneService().work(param);
	}
	
	/**
	 * 查询网关是否能通过vxlan通道PING通对端
	 * @param param
	 * @return
	 */
	public String PingVXLANDiagnostic(String param) {
		logger.debug("PingVXLANDiagnostic[{}]", param);
		return new CloudPingVXLANDiagnosticService().work(param);
	}
	
	/**
	 * vxlan配置参数查询接口
	 * @return
	 */
	public String QueryVXLANConfigure(String param) {
		logger.debug("QueryVXLANConfigure()");
		return new CloudQueryVXLANConfigureService().work(param);
	}
	
	/**
	 * 政企网关公网地址NAT配置
	 * @param param
	 * @return
	 */
	public String VXLANPublicIPNatCfg(String param) {
		logger.debug("VXLANPublicIPNatCfg()");
		return new CloudVXLANPublicIPNatCfgService().work(param);
	}

	/**
	 * 政企网关公网地址NAT配置查询
	 * @param param
	 * @return
	 */
	public String QueryVXLANPublicIPNatCfg(String param) {
		logger.debug("QueryVXLANPublicIPNatCfg()");
		return new CloudQueryVXLANPublicIPNatCfgService().work(param);
	}
	
	/**
	 * 政企网关公网地址NAT配置删除
	 * @param param
	 * @return
	 */
	public String DeleteVXLANPublicIPNatCfg(String param) {
		logger.debug("DeleteVXLANPublicIPNatCfg()");
		return new CloudDeleteVXLANPublicIPNatCfgService().work(param);
	}
	
	/**
	 * 政企网关静态路由配置
	 * @param param
	 * @return
	 */
	public String StaticRtCfg(String param) {
		logger.debug("StaticRtCfg()");
		return new CloudStaticRtCfgService().work(param);
	}
	
	/**
	 * 政企网关静态路由查询
	 * @param param
	 * @return
	 */
	public String QueryStaticRtCfg(String param) {
		logger.debug("QueryStaticRtCfg()");
		return new CloudQueryStaticRtCfgService().work(param);
	}
	
	/**
	 * 政企网关静态路由删除
	 * @param param
	 * @return
	 */
	public String DeleteStaticRtCfg(String param) {
		logger.debug("DeleteStaticRtCfg()");
		return new CloudDeleteStaticRtCfgService().work(param);
	}
	
	/**
	 * 提供给省服开开通vxlan业务
	 * @param param
	 * @return
	 */
	public String OpenVXLANService(String param) {
		logger.debug("OpenVXLANService()");
		return new CloudOpenVXLANCRMService().work(param);
	}
}
