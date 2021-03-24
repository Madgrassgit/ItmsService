
package com.linkage.itms.nmg.dispatch.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.nmg.dispatch.service.BindInfoService;
import com.linkage.itms.nmg.dispatch.service.BizRebootService;
import com.linkage.itms.nmg.dispatch.service.BizServicePortService;
import com.linkage.itms.nmg.dispatch.service.BizbindInfoService;
import com.linkage.itms.nmg.dispatch.service.BizserviceDoneService;
import com.linkage.itms.nmg.dispatch.service.BizupdateNetPwdService;
import com.linkage.itms.nmg.dispatch.service.DevRebootService;
import com.linkage.itms.nmg.dispatch.service.DevResetService;
import com.linkage.itms.nmg.dispatch.service.ModifyBinfInfoService;
import com.linkage.itms.nmg.dispatch.service.PasswordResetService;
import com.linkage.itms.nmg.dispatch.service.PingDiagnostic;
import com.linkage.itms.nmg.dispatch.service.QueryItvSheetDataService;
import com.linkage.itms.nmg.dispatch.service.QueryLanStateService;
import com.linkage.itms.nmg.dispatch.service.QueryPerformanceService;
import com.linkage.itms.nmg.dispatch.service.QueryRunningstatService;
import com.linkage.itms.nmg.dispatch.service.QuerySheetDataService;
import com.linkage.itms.nmg.dispatch.service.QueryWanStateService;
import com.linkage.itms.nmg.dispatch.service.QueryWlanStateService;
import com.linkage.itms.nmg.dispatch.service.ServiceDoneService;


/**
 * @author yinlei3 (Ailk No.73167)
 * @version 1.0
 * @since 2016年6月3日
 * @category com.linkage.itms.nmg.dispatch.main
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class NmgService
{

	/** 日志记录 */
	private static Logger logger = LoggerFactory.getLogger(NmgService.class);

	/**
	 * 设备重启接口
	 */
	public String reboot(String param)
	{
		logger.debug("reboot({})", param);
		return new DevRebootService().work(param);
	}

	/**
	 * 设备恢复出厂接口
	 */
	public String reset(String param)
	{
		logger.debug("reset({})", param);
		return new DevResetService().work(param);
	}

	/**
	 * 业务下发 
	 */
	public String serviceDone(String param)
	{
		logger.debug("serviceDone()", param);
		return new ServiceDoneService().work(param);
	}

	/**
	 * ping诊断仿真测试
	 */
	public String pingDiagnostic(String param)
	{
		logger.debug("pingDiagnostic({})", param);
		return new PingDiagnostic().work(param);
	}

	/**
	 * 家庭网关配置稽核接口
	 */
	public String querySheetData(String param)
	{
		logger.debug("querySheetData({})", param);
		return new QuerySheetDataService().work(param);
	}

	/**
	 * 家庭网关WLAN口连接状态查询接口
	 */
	public String queryWlanState(String param)
	{
		logger.debug("queryWlanState({})", param);
		return new QueryWlanStateService().work(param);
	}

	/**
	 * LAN口状态查询接口
	 */
	public String queryLanState(String param)
	{
		logger.debug("queryLanState({})", param);
		return new QueryLanStateService().work(param);
	}

	/**
	 * 查询家庭网关性能数据接口
	 */
	public String queryPerformance(String param)
	{
		logger.debug("queryPerformance({})", param);
		return new QueryPerformanceService().work(param);
	}

	/**
	 * 查询家庭网关的基本属性信息接口
	 */
	public String bindInfo(String param)
	{
		logger.debug("bindInfo({})", param);
		return new BindInfoService().work(param);
	}

	/**
	 * 用户设备解绑接口
	 * 
	 * @author 岩
	 * @date 2016-8-29
	 * @param param
	 * @return
	 */
	public String modifyBinfInfo(String param)
	{
		logger.debug("release({})", param);
		return new ModifyBinfInfoService().work(param);
	}
	
	/**
	 * 终端密码重置
	 * @author 岩 
	 * @date 2016-11-9
	 * @param param
	 * @return
	 */
	public String resetPassword(String param)
	{
		logger.debug("resetPassword({})", param);
		return new PasswordResetService().work(param);
	}
	
	/**
	 * WAN口状态查询接口
	 * @author 岩 
	 * @date 2016-11-9
	 * @param param
	 * @return
	 */
	public String queryWanState(String param)
	{
		logger.debug("queryWanState({})", param);
		return new QueryWanStateService().work(param);
	}
	
	/**
	 * 家庭网关在线状态查询接口
	 * @param
	 * @param param
	 * @return
	 */
	public String queryRunningstat(String param)
	{
		logger.debug("queryRunningstat({})", param);
		return new QueryRunningstatService().work(param);
	}
	
	/**
	 * 家庭网关远程恢复终端出厂设置并重新下发业务接口
	 * @param
	 * @param param
	 * @return
	 */
	public String resetAndServiceDone(String param)
	{
		logger.debug("resetAndServiceDone({})", param);
		return new DevResetService().work(param);
	}
	
	/**
	 * 家庭网关ITV业务稽核接口
	 */
	public String queryItvSheetData(String param)
	{
		logger.debug("queryItvSheetData({})", param);
		return new QueryItvSheetDataService().work(param);
	}
	
	/**
	 *查询政企网关的基本信息接口
	 */
	public String BizbindInfo(String param)
	{
		logger.debug("queryItvSheetData({})", param);
		return new BizbindInfoService().work(param);
	}
	
	/**
	 *重启政企网关终端设备接口
	 */
	public String BizReboot(String param)
	{
		logger.debug("reboot({})", param);
		return new BizRebootService().work(param);
	}
	
	/**
	 *政企网关重新下发配置接口
	 */
	public String BizserviceDone(String param)
	{
		logger.debug("serviceDone()", param);
		return new BizserviceDoneService().work(param);
	}
	
	/**
	 * 修改政企网关宽带密码接口
	 */
	public String BizupdateNetPwd(String param)
	{
		logger.debug("serviceDone()", param);
		return new BizupdateNetPwdService().work(param);
	}
	
	/**
	 * 政企网关业务端口信息查询接口
	 */
	public String BizServicePort(String param)
	{
		logger.debug("serviceDone()", param);
		return new BizServicePortService().work(param);
	}
	
	/**
	 * 政企网关ping诊断仿真测试
	 */
	public String BizPingDiagnostic(String param)
	{
		logger.debug("pingDiagnostic({})", param);
		return new PingDiagnostic().work(param);
	}
}
