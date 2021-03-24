
package com.linkage.stbms.ids.main;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dispatch.main.CallService;
import com.linkage.itms.dispatch.service.ZeroConfigReportService;
import com.linkage.itms.dispatch.service.ZeroConfigReportService4JL;
import com.linkage.stbms.ids.service.BindIptvStbMacService;
import com.linkage.stbms.ids.service.ChangePasswordService;
import com.linkage.stbms.ids.service.DoService;
import com.linkage.stbms.ids.service.DoServiceService;
import com.linkage.stbms.ids.service.GetStbAccountService;
import com.linkage.stbms.ids.service.GetStbBaseInfoService;
import com.linkage.stbms.ids.service.GetStbConfInfoService;
import com.linkage.stbms.ids.service.GetStbConfigResultService;
import com.linkage.stbms.ids.service.GetStbDeviceInfoService;
import com.linkage.stbms.ids.service.GetStbInfo4xjService;
import com.linkage.stbms.ids.service.GetStbInfoNMGService;
import com.linkage.stbms.ids.service.GetStbInfoService;
import com.linkage.stbms.ids.service.GetStbIpService;
import com.linkage.stbms.ids.service.GetStbLastTimeService;
import com.linkage.stbms.ids.service.GetStbOnlineStatusService;
import com.linkage.stbms.ids.service.GetStbOrderStatusNMGService;
import com.linkage.stbms.ids.service.GetStbOrderStatusService;
import com.linkage.stbms.ids.service.SetItvPasswdService;
import com.linkage.stbms.ids.service.SetStbBindSNService;
import com.linkage.stbms.ids.service.StbPingService;
import com.linkage.stbms.ids.service.StbRebootService;
import com.linkage.stbms.ids.service.StbResetService;
import com.linkage.stbms.ids.service.StbServiceDoneService;
import com.linkage.stbms.ids.service.StbTraceRouteService;
import com.linkage.stbms.ids.service.UnbindIptvStbMacService;
import com.linkage.stbms.ids.service.UserChangeStbService;
import com.linkage.stbms.ids.service.UserStbInfoService;
import com.linkage.stbms.itv.main.Global;

/**
 * 向综调提供的服务类接口
 * 
 * @author zhangshimin(67310) Tel:78
 * @version 1.0
 * @since 2011-4-21 下午07:27:28
 * @category com.linkage.stbms.ids.main<br>
 * @copyright 南京联创科技 网管科技部
 */
public class IdsService
{

	// 日志记录
	private static Logger logger = LoggerFactory.getLogger(IdsService.class);
	/**
	 * 机顶盒信息查询接口
	 * 
	 * @param inParam
	 * @return
	 */
	public String getStbInfo(String inParam)
	{
		return new GetStbInfoService().work(inParam);
	}

	/**
	 * 设备重启接口
	 * 
	 * @param inParam
	 * @return
	 */
	public String setStbReboot(String inParam)
	{
		logger.warn("servicename[setStbReboot]，调用端IP[{}],入参为：{}",new Object[] {getClientInfo(),inParam });
		return new StbRebootService().work(inParam);
	}

	/**
	 * Ping操作接口
	 * 
	 * @param inParam
	 * @return
	 */
	public String setStbPingIP(String inParam)
	{
		if ("jx_dx".equals(Global.G_instArea) || "sd_lt".equals(Global.G_instArea))
		{
			return new StbPingService().workForJXDX(inParam);
		}
		else if("xj_dx".equals(Global.G_instArea))
		{
			return new StbPingService().workForXJDX(inParam);
		}
		else
		{
			return new StbPingService().work(inParam);
		}
	}

	/**
	 * TraceRoute接口
	 * 
	 * @param inParam
	 * @return
	 */
	public String setStbTraceRoute(String inParam)
	{
		if ("jx_dx".equals(Global.G_instArea))
		{
			return new StbTraceRouteService().workForJXDX(inParam);
		}
		else
		{
			return new StbTraceRouteService().work(inParam);
		}
	}

	/**
	 * 江西电信ITV用户与设备解绑 可通过输入用户业务账号或者机顶盒mac来解除用户与设备的绑定关系
	 * 
	 * @param inParam
	 * @return
	 */
	public String UnbindIptvStbMac(String inParam)
	{
		return new UnbindIptvStbMacService().work(inParam);
	}

	/**
	 * 江西电信ITV用户与设备手工绑定
	 * 可通过输入用户业务账号或者机顶盒mac或机顶盒序列号来绑定用户与设备的关系
	 * @param inParam
	 * @return
	 */
	public String bindIptvStbMac(String inParam){ 
		return new BindIptvStbMacService().work(inParam);
	}
	
	/**
	 * 机顶盒回复出厂设置
	 * 
	 * @param inParam
	 * @return
	 */
	public String setStbReset(String inParam)
	{
		return new StbResetService().work(inParam);
	}

	/**
	 * 江西iTV、宁夏电信、 内蒙古电信ITV 业务下发
	 * 
	 * @param inParam
	 * @return
	 */
	public String doService (String inParam){
		if ("jx_dx".equals(Global.G_instArea) || "nx_dx".equals(Global.G_instArea) || "nmg_dx".equals(Global.G_instArea)) {
			logger.warn("servicename[doService]，调用端IP[{}],入参为：{}",new Object[] {getClientInfo(),inParam });
			return new DoServiceService().work(inParam);
		}else {
			return new DoService().work(inParam);
		}
	}
	
	/**
	 * 设备在线情况查询接口
	 * 
	 * @param inParam
	 * @return
	 */
	public String getStbOnlineStatus(String inParam)
	{
		return new GetStbOnlineStatusService().work(inParam);
	}

	/**
	 * 用户设备解绑接口(江西电信ITV终端管理平台与行业应用APP接口)
	 * 
	 * @param inParam
	 * @return
	 */
	public String userChangeStbService(String inParam)
	{
		return new UserChangeStbService().work(inParam);
	}

	/**
	 * 机顶盒设备信息实时查询(山东联通)
	 * 
	 * @param inParam
	 * @return
	 */
	public String getStbBaseInfo(String inParam)
	{
		return new GetStbBaseInfoService().work(inParam);
	}

	/**
	 * 机顶盒与RMS平台交互密码实时查询(山东联通)
	 * 
	 * @param inParam
	 * @return
	 */
	public String getStbDeviceInfo(String inParam)
	{
		return new GetStbDeviceInfoService().work(inParam);
	}

	/**
	 * 机顶盒工单参数实时查询(山东联通)
	 * 
	 * @param inParam
	 * @return
	 */
	public String getStbConfInfo(String inParam)
	{
		return new GetStbConfInfoService().work(inParam);
	}
	/**
	 * 根据业务账号查询设备接口
	 * 
	 * @param inParam
	 * @return
	 */
	public String getUserStbInfo(String inParam)
	{
		return new UserStbInfoService().work(inParam);
	}
	/**
	 * 安徽电信 设置用户和设备的对应关系
	 * @param inParam
	 * @return
	 */
	public String setStbBindSN(String inParam)
	{
		return new SetStbBindSNService().work(inParam);
	}
	
	/**
	 * 江西电信  零配置结果查询接口
	 * @author 岩 
	 * @date 2016-7-18
	 * @param inParam
	 * @return
	 */
	public String getStbConfigResult(String inParam)
	{
		return new GetStbConfigResultService().work(inParam);
	}
	
	/**
	 * 新疆电信  获取机顶盒ip地址接口
	 * @author chenxj6
	 * @date 2016-8-30
	 * @param inParam
	 * @return
	 */
	public String getStbIp(String inParam)
	{
		return new GetStbIpService().work(inParam);
	}
	
	/**
	 * 新疆电信  机顶盒信息查询接口
	 * @author chenxj6
	 * @date 2016-9-28
	 * @param inParam
	 * @return
	 */
	public String getStbInfo4xj(String inParam)
	{
		return new GetStbInfo4xjService().work(inParam);
	}
	
	/**
	 * 新疆电信  机顶盒业务账号查询接口
	 * @author chenxj6
	 * @date 2016-9-29
	 * @param inParam
	 * @return
	 */
	public String getStbAccount(String inParam)
	{
		logger.warn("servicename[GetStbAccountService]，调用端IP[{}],入参为：{}",new Object[] {getClientInfo(),inParam });
		return new GetStbAccountService().work(inParam);
	}
	
	/**
	 * 新疆电信  机顶盒零配置状态查询接口
	 * @author chenxj6
	 * @date 2016-11-11
	 * @param inParam
	 * @return
	 */
	public String getStbOrderStatus(String inParam)
	{
		return new GetStbOrderStatusService().work(inParam);
	}
	
	/**
	 * 宁夏 查询机顶盒版本信息接口
	 * @author fanjm
	 * @date 2016-12-12
	 * @param inParam
	 * @return
	 */
	public String getStbVersion(String inParam)
	{
		logger.warn("servicename[getStbInfoForXJ]，调用端IP[{}],入参为：{}",new Object[] {getClientInfo(),inParam });
		return new GetStbInfo4xjService().work(inParam);
	}
	/**
	 * 内蒙古 查询机顶盒信息接口
	 * @author hourui
	 * @date 2017-11-20
	 * @param Param
	 * @return String
	 */
	public String getStbDevInfo(String param)
	{   
		return new GetStbInfoNMGService().work(param);
	}
	/**
	 * 内蒙古 机顶盒状态查询接口
	 * @author hourui
	 * @date 2017-11-20
	 * @param Param
	 * @return String
	 */
	public String getStbDevStatusInfo(String param)
	{    
		return new GetStbOrderStatusNMGService().work(param);
	}
	
	/**
	 * JXDX-ITV-REQ-20171128-WUWF-001(ITV终端网管平台与预处理改密接口需求)
	 * @param param
	 * @return
	 */
	public String changePassword(String param) {
		return new ChangePasswordService().work(param);
	}
	/**
	 * 用户最后一次上线接口
	 */
	public String stbLastTime(String param)
	{
			return new GetStbLastTimeService().work(param);
	}
	
	/**
	 * XJDX-ITMS-20181016-LJ-001(机顶盒业务下发接口)v1.1
	 * @param param
	 * @return
	 */
	public String stbServiceDone(String param) {
		return new StbServiceDoneService().work(param);
	}
	
	/**
	 * 获取调用者信息
	 * @return 调用者ip
	 */
	private String getClientInfo()
	{
		String clientIP = null;
		MessageContext mc = MessageContext.getCurrentContext();
		HttpServletRequest request = (HttpServletRequest)mc.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        clientIP = request.getRemoteAddr();
		return clientIP;
	}
	
	
	/**
	 * 新疆ITMS需求单：XJDX-REQ-20151028-HUJG3-001【ITMS系统自动零配置需求】
	 * 
	 * @param param
	 * @return
	 */
	public String stbZeroDeviceConfig(String param)
	{
		if ("jl_dx".equals(Global.G_instArea))
		{
			return new ZeroConfigReportService4JL().work(param);
		}
		else
		{
			return new ZeroConfigReportService().work(param);
		}
	}
	
	/**NXDX-REQ-ITMS-20190918-LX-002(修改机顶盒密码-接口)
	 * @author yaoli
	 * @param param
	 * @return
	 */
	public String setItvPasswd(String param){
		logger.warn("servicename[setItvPasswd]，调用端IP[{}],入参为：{}",new Object[] {getClientInfo(),param });
		return new SetItvPasswdService().work(param);
	}
}