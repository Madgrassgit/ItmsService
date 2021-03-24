
package com.linkage.itms.hlj.dispatch.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.hlj.dispatch.service.BindInfoQiyeService;
import com.linkage.itms.hlj.dispatch.service.BindInfoService;
import com.linkage.itms.hlj.dispatch.service.DevOnlineService;
import com.linkage.itms.hlj.dispatch.service.DevRebootService;
import com.linkage.itms.hlj.dispatch.service.DevResetService;
import com.linkage.itms.hlj.dispatch.service.DownLoadByHTTP;
import com.linkage.itms.hlj.dispatch.service.ModifyWanTypeService;
import com.linkage.itms.hlj.dispatch.service.PasswordResetService;
import com.linkage.itms.hlj.dispatch.service.PingDiagnostic;
import com.linkage.itms.hlj.dispatch.service.QueryDeviceVersionService;
import com.linkage.itms.hlj.dispatch.service.QueryLanStateService;
import com.linkage.itms.hlj.dispatch.service.QueryNetService;
import com.linkage.itms.hlj.dispatch.service.QueryPerformanceService;
import com.linkage.itms.hlj.dispatch.service.QuerySheetDataService;
import com.linkage.itms.hlj.dispatch.service.QuerySuperPwdService;
import com.linkage.itms.hlj.dispatch.service.QueryWlanStateService;
import com.linkage.itms.hlj.dispatch.service.ReleaseByIpService;
import com.linkage.itms.hlj.dispatch.service.ServiceDoneFail;
import com.linkage.itms.nmg.dispatch.service.QueryWanStateService;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-20
 * @category com.linkage.itms.hlg.dispatch.main
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class HljService
{

	/** 日志记录 */
	private static Logger logger = LoggerFactory.getLogger(HljService.class);

	/**
	 * 家庭网关基本属性查询接口
	 */
	public String bindInfo(String param)
	{
		logger.debug("bindInfo({})", param);
		return new BindInfoService().work(param);
	}

	/**
	 * 终端在线信息接口
	 */
	public String devOnline(String param)
	{
		logger.debug("devOnline({})", param);
		return new DevOnlineService().work(param);
	}

	/**
	 * 终端业务下发失败记录
	 */
	public String serviceDoneFail(String param)
	{
		logger.debug("serviceDoneFail({})", param);
		return new ServiceDoneFail().work(param);
	}

	/**
	 * 宽带上网信息查询
	 */
	public String queryNet(String param)
	{
		logger.debug("queryNet({})", param);
		return new QueryNetService().work(param);
	}

	/**
	 * 终端版本信息查询接口
	 */
	public String devVersion(String param)
	{
		logger.debug("devVersion({})", param);
		return new QueryDeviceVersionService().work(param);
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
	 * 查询家庭网关性能数据接口
	 */
	public String queryPerformance(String param)
	{
		logger.debug("queryPerformance({})", param);
		return new QueryPerformanceService().work(param);
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
	 * 家庭网关WLAN口连接状态查询接口
	 */
	public String queryWlanState(String param)
	{
		logger.debug("queryWlanState({})", param);
		return new QueryWlanStateService().work(param);
	}

	/**
	 * 超级密码查询接口
	 */
	public String querySuperPwd(String param)
	{
		logger.debug("querySuperPwd({})", param);
		return new QuerySuperPwdService().work(param);
	}

	/**
	 * PING测试接口
	 */
	public String pingDiagnostic(String param)
	{
		logger.debug("pingDiagnostic({})", param);
		return new PingDiagnostic().work(param);
	}

	/**
	 * 根据IP地址解绑接口
	 */
	public String releaseByIp(String param)
	{
		logger.debug("releaseByIp({})", param);
		return new ReleaseByIpService().work(param);
	}

	/**
	 * 设备重启
	 */
	public String reboot(String param)
	{
		logger.debug("reboot({})", param);
		return new DevRebootService().work(param);
	}

	/**
	 * 配置重新下发\恢复出厂设置
	 */
	public String reset(String param)
	{
		logger.debug("reset({})", param);
		return new DevResetService().work(param);
	}

	/**
	 * E8-C上网模式修改（路由/桥接）
	 */
	public String modifyWanType(String param)
	{
		logger.debug("modifyWanType({})", param);
		return new ModifyWanTypeService().work(param);
	}

	/**
	 * E8-C密码重置
	 */
	public String passwordReset(String param)
	{
		logger.debug("passwordReset({})", param);
		return new PasswordResetService().work(param);
	}

	/**
	 * 家庭网关测速接口
	 */
	public String downLoadByHTTP(String param)
	{
		logger.debug("downLoadByHTTP({})", param);
		return new DownLoadByHTTP().work(param, "1");
	}
	
	/**
	 * 企业网关测速接口
	 */
	public String downLoadByHTTPBbms(String param)
	{
		logger.debug("downLoadByHTTP({})", param);
		return new DownLoadByHTTP().work(param, "2");
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
	 * 企业网关基本属性查询接口
	 */
	public String queryGovAttribute(String param)
	{
		logger.debug("bindInfoQiye({})", param);
		return new BindInfoQiyeService().work(param);
	}
}
