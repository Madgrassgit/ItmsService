
package com.linkage.itms.dispatch.main;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.Global;
import com.linkage.itms.dispatch.cqdx.service.ChangeWifiPasswordService;
import com.linkage.itms.dispatch.cqdx.service.CommonInterfaceOperationService;
import com.linkage.itms.dispatch.cqdx.service.FactoryResetService;
import com.linkage.itms.dispatch.cqdx.service.FeedbackWorkTicketsInfoService;
import com.linkage.itms.dispatch.cqdx.service.GetGetServiceStatusService;
import com.linkage.itms.dispatch.cqdx.service.GetVLanInfoOfTerminalService;
import com.linkage.itms.dispatch.cqdx.service.QueryActionInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryBussinessInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryConnectionInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryLanInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryRgModeInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryTerminalInfoService;
import com.linkage.itms.dispatch.cqdx.service.QueryTerminalPasswdService;
import com.linkage.itms.dispatch.cqdx.service.QueryWorkTicketsInfoService;
import com.linkage.itms.dispatch.cqdx.service.SetGatewayTerminalNumberService;
import com.linkage.itms.dispatch.cqdx.service.StartGetUserInfoDiagService;
import com.linkage.itms.dispatch.cqdx.service.StartPingDiagService;
import com.linkage.itms.dispatch.cqdx.service.StartRebootDiagService;
import com.linkage.itms.dispatch.obj.UpdateNetPwdServic1;
import com.linkage.itms.dispatch.obj.UpdateNetPwdServicHBLT;
import com.linkage.itms.dispatch.service.*;
import com.linkage.itms.ids.service.DiagnosticEnableResultService;
import com.linkage.itms.ids.service.DiagnosticEnableService;
import com.linkage.itms.ids.service.ReportProidResultService;
import com.linkage.itms.ids.service.Reportperoid;
import com.linkage.itms.nx.dispatch.service.ModifyWifiChannelService;
import com.linkage.itms.nx.dispatch.service.ModifyWifiFaxModelService;
import com.linkage.itms.nx.dispatch.service.ModifyWifiSendPowerService;
import com.linkage.itms.oss.bio.SpecInfoBIO;
import com.linkage.stbms.ids.service.GetStbInfoService;

/**
 * 向综调接口提供的服务类
 * 
 * @author Jason(3412)
 * @date 2010-3-31
 */
public class CallService
{

	// 日志记录
	private static Logger logger = LoggerFactory.getLogger(CallService.class);

	/**
	 * 根据用户帐号或者设备序列号查询终端的电信维护密码
	 * 
	 * @param 综调接口XML字符串参数
	 * @author Jason(3412)
	 * @date 2010-3-31
	 * @return String 回参的XML字符串
	 */
	public String call(String param)
	{
		logger.debug("call()");
		logger.warn("servicename[TelepasswdService]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new TelepasswdService().work(param);
	}

	public String querySheetData(String param)
	{
		logger.debug("querySheetData({})", param);
		return new QuerySheetDataService().work(param);
	}

	/**
	 * radius系统获取宽带帐号
	 * 
	 * @author zhangshimin
	 * @date 2012-3-15
	 * @return String
	 */
	public String getNetUsername(String param)
	{
		logger.debug("getNetUsername({})", param);
		return new GetNetUsernameService().work(param);
	}

	/**
	 * 设备恢复出厂接口
	 * 
	 * @author zhangshimin
	 * @date 2012-2-25
	 * @return String
	 */
	public String reset(String param)
	{
		logger.debug("reset({})", param);
		logger.warn("servicename[DevResetService]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new DevResetService().work(param);
	}

	/**
	 * 设备重启接口
	 * 
	 * @author zhangshimin
	 * @date 2012-2-25
	 * @return String
	 */
	public String reboot(String param)
	{
		logger.debug("reboot({})", param);
		logger.warn("servicename[DevRebootService]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new DevRebootService().work(param);
	}

	/**
	 * 用户设备解绑接口
	 * 
	 * @author zhangshimin
	 * @date 2011-5-11
	 * @return String
	 */
	public String release(String param)
	{
		logger.debug("release({})", param);
		if (Global.JXDX.equals(Global.G_instArea))
		{
			return new JXReleaseService().work(param);
		}
		else
		{
			return new ReleaseService().work(param);
		}
	}

	/**
	 * 设备配置查询接口
	 * 
	 * @author zhangshimin
	 * @date 2011-5-17
	 * @return String
	 */
	public String queryDeviceConfig(String param)
	{
		logger.debug("queryDeviceConfig ({})", param);
		return new DevConfigService().work(param);
	}

	/**
	 * 设备配置查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryDeviceService(String param)
	{
		logger.debug("queryDeviceService ({})", param);
		return new DevConfigServiceNewone().work(param);
	}

	/**
	 * BSS业务工单查询接口
	 * 
	 * @author zhangshimin
	 * @date 2011-5-11
	 * @return String
	 */
	public String queryBssSheetAndOpenStatus(String param)
	{
		logger.debug("queryBssSheetAndOpenStatus({})", param);
		return new BssSheetService().work(param);
	}

	/**
	 * 绑定情况查询
	 * 
	 * @author onelinesky
	 * @date 2011-1-17
	 * @return String
	 */
	public String bindInfo(String param)
	{
		logger.debug("bindInfo({})", param);
		logger.warn("servicename[BindInfoService]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new BindInfoService().work(param);
	}

	/**
	 * 用户设备绑定
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-25
	 * @return String
	 */
	public String bind(String param)
	{
		logger.debug("bind()");
		return new BindService().work(param);
	}

	/**
	 * 业务下发
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-25
	 * @return String
	 */
	public String serviceDone(String param)
	{
		logger.debug("serviceDone()");
		logger.warn("servicename[serviceDone]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new ServiceDoneService().work(param);
	}

	// /**
	// * 业务开通情况查询
	// *
	// * @param
	// * @author Jason(3412)
	// * @date 2010-6-25
	// * @return String
	// */
	// public String serviceQuery(String param) {
	// logger.debug("serviceQuery()");
	// return new ServiceQueryService().work(param);
	// }
	/**
	 * 根据用户帐号或者设备序列号查询终端的在线情况
	 * 
	 * @param 综调接口XML字符串参数
	 * @author Jason(3412)
	 * @date 2010-9-2
	 * @return String 回参的XML字符串
	 */
	public String devOnline(String param)
	{
		logger.debug("devOnline()");
		logger.warn("servicename[devOnline]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new DevOnlineService().work(param);
	}

	/**
	 * 根据用户帐号或者设备序列号查询用户的多PVC改造情况
	 * 
	 * @param 综调接口XML字符串参数
	 * @author Jason(3412)
	 * @date 2010-9-2
	 * @return String 回参的XML字符串
	 */
	public String pvcReformed(String param)
	{
		logger.debug("pvcReformed()");
		return new PvcReformedService().work(param);
	}

	/**
	 * 诊断接口
	 * 
	 * @param param
	 * @return
	 */
	public String serviceDiagnostic(String param)
	{
		logger.debug("serviceDiagnostic()");
		return new DiagnosticService().work(param);
	}

	/**
	 * 用于新疆电信 根据用户LOID获取用户业务放装情况
	 * 
	 * @author zhangchy
	 * @date 2012-02-08
	 * @return String
	 */
	public String getSerResult(String param)
	{
		logger.debug("getSerResult({})", param);
		logger.warn("servicename[QueryResultByLoidService]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new QueryResultByLoidService().work(param);
	}

	/**
	 * 江苏电信需求单：JSDX_ITMS-REQ-20120220-LUHJ-003 综调报竣工接口
	 */
	public String zdEndOfWork(String param)
	{
		logger.debug("CallService==>zdEndOfWork({})", param);
		return new ZongDiaoEndOfWorkService().work(param);
	}

	/**
	 * 江苏ITMS需求单： JSDX_ITMS-REQ-20120220-LUHJ-004 新疆优化也走这个接口 桥改路由
	 */
	public String BridgeToRout(String param)
	{
		logger.debug("CallService==>BridgeToRout({})", param);
		if (Global.JXDX.equals(Global.G_instArea) || Global.XJDX.equals(Global.G_instArea)
				|| Global.NXDX.equals(Global.G_instArea)
				|| Global.HLJDX.equals(Global.G_instArea)
				|| Global.NMGDX.equals(Global.G_instArea)
				|| Global.AHDX.equals(Global.G_instArea))
		{
			logger.warn("servicename[BridgeToRout]，调用端IP[{}],入参为：{}",
					new Object[] { getClientInfo(), param });
			return new BridgeToRoutService4Jx().work(param);
		}
		else if (Global.JSDX.equals(Global.G_instArea))
		{
			return new BridgeToRoutServiceForJs().work(param);
		}
		else
		{
			return new BridgeToRoutService().work(param);
		}
	}

	/**
	 * 新疆电信 VOIP语音协议查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String voipProtocol(String param)
	{
		logger.debug("CallService==voipProtocol({})", new Object[] { param });
		return new XJVoipProtocolQueryService().work(param);
	}

	/**
	 * 江苏ITMS 升级到规范版本接口 JSDX_ITMS-REQ-20120911-LUHJ-003
	 * 
	 * @param param
	 * @return
	 */
	public String softwareupgrade(String param)
	{
		logger.debug("CallService==UpgradeToStandardVersion()");
		logger.warn("servicename[UpgradeToStandardVersionService]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new UpgradeToStandardVersionService().work(param);
	}

	/**
	 * 江苏移动测试通过WebService发工单
	 * 
	 * @param inXml
	 * @return
	 */
	public String sendSheetByChinaMobile(String inXml)
	{
		logger.debug("CallService==>sendSheetByChinaMobile()");
		return new SendSheetByChinaMobileService().work(inXml);
	}

	/**
	 * 设备版本信息查询接口
	 * 
	 * @param inXml
	 * @return
	 */
	public String devversion(String inXml)
	{
		logger.debug("CallService==>devversion()");
		logger.warn("servicename[QueryDeviceVersion]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), inXml });
		return new QueryDeviceVersion().work(inXml);
	}

	/**
	 * 安徽电信 节点值获取接口
	 * 
	 * @param inXml
	 * @return
	 */
	public String getParameterValues(String inXml)
	{
		return new GetParameterValuesService().work(inXml);
	}

	/**
	 * 序列号、串号查询接口
	 */
	public String A8setVoip(String inXml)
	{
		return new A8setVoipService().work(inXml);
	}

	/**
	 * 安徽电信 节点值设置接口
	 * 
	 * @param inXml
	 * @return
	 */
	public String setParameterValues(String inXml)
	{
		return new SetParameterValuesService().work(inXml);
	}

	/**
	 * 安徽电信 一键预处理
	 * 
	 * @param inXml
	 * @return
	 */
	public String oneKeyDone(String inXml)
	{
		return new OneKeyDoneServiceForAH().work(inXml);
	}

	/**
	 * 设备状态信息上报功能开启和关闭接口
	 * 
	 * @param xmlRequest
	 *            xml请求报文
	 * @return 响应报文
	 */
	public String diagnosticEnable(String xmlRequest)
	{
		return new DiagnosticEnableService().diagnosticEnable(xmlRequest);
	}

	/**
	 * 设备状态信息上报功能开启和关闭结果返回接口
	 * 
	 * @param xmlRequest
	 *            xmlRequest xml请求报文
	 * @return 响应报文
	 */
	public String diagnosticEnableResult(String xmlRequest)
	{
		return new DiagnosticEnableResultService().diagnosticEnableResult(xmlRequest);
	}

	/**
	 * ping诊断仿真测试
	 * 
	 * @param param
	 * @return
	 */
	public String pingDiagnostic(String param)
	{
		logger.debug("pingDiagnostic({})", param);
		logger.warn("servicename[pingDiagnostic]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new PingDiagnostic().work(param);
	}

	/**
	 * PPPoE拨测接口
	 * 
	 * @param param
	 * @return
	 */
	public String PPPoEDial(String param)
	{
		logger.debug("PPPoEDial({})", param);
		return new PPPoEDial().work(param);
	}

	/**
	 * A8语音端口修改
	 * 
	 * @param param
	 * @return
	 */
	public String VlanStatus(String param)
	{
		logger.debug("VlanStatus({})", param);
		return new VlanStatusService().work(param);
	}

	/**
	 * A8语音端口修改
	 * 
	 * @param param
	 * @return
	 */
	public String SetL2forVlan43(String param)
	{
		logger.debug("SetL2forVlan43 ({})", param);
		return new SetL2forVlan43Service().work(param);
	}

	/**
	 * Voice拨测接口
	 * 
	 * @param param
	 * @return
	 */
	public String VoiceDial(String param)
	{
		logger.debug("VoiceDial({})", param);
		if (Global.CTC.equalsIgnoreCase(Global.G_OPERATOR))
		{
			return new VoiceDial4ctc().work(param);
		}
		else
		{
			return new VoiceDial().work(param);
		}
	}

	/**
	 * Voice质量监控
	 * 
	 * @param param
	 * @return
	 */
	public String voiceQuality(String param)
	{
		logger.debug("VoiceDial({})", param);
		return new VoiceQuality().work(param);
	}

	/**
	 * HTTP下载业务质量测试接口
	 * 
	 * @param param
	 * @return
	 */
	public String downLoadByHTTP(String param)
	{
		logger.debug("downLoadByHTTP({})", param);
		return new DownLoadByHTTP().work(param);
	}

	/**
	 * 安徽电信桥接网路接入方式的HTTP下载业务质量测试接口
	 * 
	 * @param param
	 * @return
	 */
	public String downLoadByHTTPForAH(String param)
	{
		logger.debug("downLoadByHTTP({})", param);
		return new DownLoadByHTTP().workForAH(param);
	}

	/**
	 * 安徽电信桥接网路接入方式的HTTP下载业务质量测试接口
	 * 
	 * @param param
	 * @return
	 */
	public String downLoadByHTTPSpead(String param)
	{
		logger.debug("downLoadByHTTP({})", param);
		if (Global.AHDX.equals(Global.G_instArea))
		{
			return new DownLoadByHTTPSpeadService().work(param);
		}
		else if (Global.XJDX.equals(Global.G_instArea)
				|| Global.NXDX.equals(Global.G_instArea)
				|| Global.JXDX.equals(Global.G_instArea))
		{
			logger.warn("servicename[DownLoadByHTTPSpeadService4JX]，调用端IP[{}],入参为：{}",
					getClientInfo(), param);
			return new DownLoadByHTTPSpeadService4JX().work(param);
		}
		else if (Global.SDDX.equals(Global.G_instArea))
		{
			return new DownLoadByHTTPSpeadService4SD().work(param);
		}
		else
		{
			return new DownLoadByHTTP().workSpead(param);
		}
	}

	/**
	 * 获取设备信息
	 * 
	 * @param param
	 * @return
	 */
	public String deviceinfo(String param)
	{
		logger.debug("deviceinfo({})", param);
		return new QueryDeviceInfoService().work(param);
	}

	/**
	 * 获取wan连接
	 * 
	 * @param param
	 * @return
	 */
	public String queryWanConn(String param)
	{
		logger.debug("queryWanConn({})", param);
		return new WanConnService().work(param);
	}

	/**
	 * 语音注册
	 * 
	 * @param param
	 * @return
	 */
	public String voiceServicesRegister(String param)
	{
		logger.debug("voiceServicesRegister ({})", param);
		return new VoipRegistTestService().work(param);
	}

	/**
	 * 获取用户终端wifi能力
	 * 
	 * @param param
	 * @return
	 */
	public String hasWifi(String param)
	{
		logger.debug("hasWifi ({})", param);
		return new WifiInfoService().work(param);
	}

	/**
	 * 查询宽带密码接口
	 * 
	 * @param param
	 * @return
	 */
	public String netPassword(String param)
	{
		logger.debug("netPassword ({})", param);
		return new NetPasswordService().work(param);
	}

	/**
	 * 江西采集光功率及业务信息返回给其他系统
	 * 
	 * @param param
	 * @return
	 */
	public String decayServInfo(String param)
	{
		logger.debug("decayServInfo({})", param);
		new DecayServInfoService().work(param);
		return null;
	}

	/**
	 * 江苏 查询用户是否可开路由查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String isRoute(String param)
	{
		logger.debug("IsRoute({})", param);
		return new IsRoute().work(param);
	}

	/**
	 * 江苏 路由下发结果查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String openRouteResult(String param)
	{
		logger.debug("OpenRouteResult({})", param);
		return new OpenrouteResult().work(param);
	}

	/**
	 * 江苏天翼看店开通
	 * 
	 * @param param
	 * @return
	 */
	public String kandianOpen(String param)
	{
		logger.debug("kandianOpen({})", param);
		return new KandianOpen().work(param);
	}

	/**
	 * 江苏多宽带查询
	 * 
	 * @param param
	 * @return
	 */
	public String queryMultiInterPort(String param)
	{
		logger.debug("queryMultiInterPort({})", param);
		return new QueryMultiInterPort().work(param);
	}

	/**
	 * 修改SSID密码
	 * 
	 * @param param
	 * @return
	 */
	public String moSSIDPW(String param)
	{
		logger.debug("moSSIDPW({})", param);
		return new MoSSIDPW().work(param);
	}

	/**
	 * SSID1开通和关闭
	 * 
	 * @param param
	 * @return
	 */
	public String operateSSID1(String param)
	{
		logger.debug("operateSSID1({})", param);
		return new OperateSSID().work(param, "1");
	}

	/**
	 * SSID2开通和关闭
	 * 
	 * @param param
	 * @return
	 */
	public String operateSSID2(String param)
	{
		logger.debug("operateSSID2({})", param);
		return new OperateSSID().work(param, "2");
	}

	/**
	 * 修改终端上网个数
	 * 
	 * @param param
	 * @return
	 */
	public String howTerminal(String param)
	{
		return new HowTerminal().work(param);
	}

	/**
	 * 设备上报周期变更
	 * 
	 * @param param
	 *            xml请求报文
	 * @return 响应报文
	 */
	public String reportPeroid(String param)
	{
		logger.debug("reportPeroid({})", param);
		return new Reportperoid().work(param);
	}

	/**
	 * 设备上报周期变更接口返回
	 * 
	 * @param param
	 *            xml请求报文
	 * @return 响应报文
	 */
	public String reportPeroidResult(String param)
	{
		logger.debug("reportPeroidResult({})", param);
		return new ReportProidResultService().work(param);
	}

	/**
	 * 江苏电信ITMS查询设备序列号
	 * 
	 * @param param
	 *            xml请求报文
	 * @return 响应报文
	 */
	public String queryDevSN(String param)
	{
		logger.warn("servicename[queryDevSN]，调用端IP[{}],入参为：{}", getClientInfo(), param);
		return new QueryDevSnService().work(param);
	}

	public String querySpecInfo(String xmlParam)
	{
		return new SpecInfoBIO().querySpecInfo(xmlParam);
	}

	/**
	 * 综合网管调用智能网管
	 * 
	 * @param operation
	 * @param xmlData
	 * @return
	 */
	public String process(String operation, String xmlData)
	{
		return "";
	}

	/**
	 * 智能网管调用综合网管
	 * 
	 * @param xmlData
	 * @return
	 */
	public String process(String xmlData)
	{
		return new SpringService().work(xmlData);
	}

	/**
	 * 版本发布审核结果回馈接口
	 */
	public String versionRelease(String param)
	{
		return new VersionReleaseService().work(param);
	}

	/**
	 * 电子运维提供升级清单附件名称和任务号，ITMS根据任务号调用终端升级
	 * 
	 * @param param
	 *            <?xml version="1.0" encoding="GBK"?> <root>
	 *            <CmdID>123456789012345</CmdID> <CmdType>CX_01</CmdType>
	 *            <ClientType>5</ClientType> <Param> <Operator>njadmin</Operator>
	 *            <CallDate>1304234577</CallDate> <Tasknumber>2014113001</Tasknumber>
	 *            </Param> </root>
	 * @return
	 */
	public String callUpgrade(String param)
	{
		return new UpgradeService().work(param);
	}

	/**
	 * 电子运维发送版本升级信心给ITMS，ITMS根据发送版本信息查询升级情况返回给电子运维。
	 * 
	 * @param param
	 * @return
	 */
	public String upgradeResults(String param)
	{
		return new UpgradeResultsService().work(param);
	}

	/**
	 * 上网方式查询 wanghong5 2015-03-10
	 */
	public String queryWanType(String param)
	{
		logger.debug("queryWanType({})", param);
		if (Global.JLLT.equals(Global.G_instArea))
		{
			return new QueryWanTypeJLLTService().work(param);
		}
		return new QueryWanTypeService().work(param);
	}

	/**
	 * 接入方式查询 wanghong5 2015-03-10
	 */
	public String queryAccessType(String param)
	{
		logger.debug("queryAccessType({})", param);
		return new QueryAccessTypeService().work(param);
	}

	/**
	 * awifi开通状态查询 wanghong5 2015-03-10
	 */
	public String queryIsAwifi(String param)
	{
		logger.debug("queryIsAwifi({})", param);
		return new QueryIsAwifiService().work(param);
	}

	/**
	 * 江苏电信终端能力开发，无线下挂终端数量查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryDevNumber(String param)
	{
		return new QueryDevNumberService().work(param);
	}

	/**
	 * 江苏电信终端能力开发，SSID名称查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String querySSIDName(String param)
	{
		return new QuerySSIDNameService().work(param);
	}

	/**
	 * 江苏电信终端能力开发，宽带路由拨号错误码查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryFailedCode(String param)
	{
		return new QueryFailedCodeService().work(param);
	}

	/**
	 * 江苏电信终端能力开发，关闭aWiFi接口
	 * 
	 * @param param
	 * @return
	 */
	public String OpenOrCloseAwifi(String param)
	{
		// 新疆关闭aWiFi 通过设置节点enable节点实现
		if (Global.XJDX.equals(Global.G_instArea))
		{
			return new OpenOrCloseAwifiXJService().work(param);
		}
		// 江苏关闭aWiFi 通过删除节点实现
		else
		{
			return new OpenOrCloseAwifiService().work(param);
		}
	}

	/**
	 * 江苏电信绑定失败原因查询
	 * 
	 * @author cczhong
	 * @description
	 */
	public String bindFailed(String param)
	{
		return new BindFailedService().work(param);
	}

	/**
	 * 安徽电信语音端口查询
	 * 
	 * @author cczhong
	 * @description
	 */
	public String voipPort(String param)
	{
		return new VoipPortService().work(param);
	}

	/**
	 * 江苏电信与综调接口，LAN口状态接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryLanState(String param)
	{
		logger.warn("servicename[QueryLanStateService]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new QueryLanStateService().work(param);
	}

	/**
	 * 新疆电信，在线和LAN口状态查询接口（新疆电信ITMS系统智慧客服的一键整断接口改造）
	 * 
	 * @param param
	 * @return
	 */
	public String devOnlineAndLanState(String param)
	{
		return new QueryLanStateAddStatusService().work(param);
	}

	/**
	 * 江西ITV LAN口状态接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryITVLanState(String param)
	{
		return new QueryITVLanStateService().work(param);
	}

	/**
	 * 江苏电信与综调接口，语音口状态查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryVoipState(String param)
	{
		return new QueryVoipStateService().work(param);
	}

	/**
	 * 江苏电信与综调接口，WLAN口状态接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryWlanState(String param)
	{
		return new QueryWlanStateService().work(param);
	}

	/**
	 * 江苏电信与综调接口，SSID1名称修改接口
	 * 
	 * @param param
	 * @return
	 */
	public String ModifySSID1(String param)
	{
		return new ModifySSID1Service().work(param);
	}

	/**
	 * 江苏电信与综调接口，SSID发射功率查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String querySSIDPower(String param)
	{
		return new QuerySSIDPowerService().work(param);
	}

	/**
	 * 江苏电信与综调接口，WIFI的状态查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryWiFi(String param)
	{
		return new QueryWiFiService().work(param);
	}

	/**
	 * 江苏电信与综调接口，WIFI下挂设备的MAC地址查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryWiFiDeviceMAC(String param)
	{
		return new QueryWiFiDeviceMACService().work(param);
	}

	/**
	 * 江西电信itms，显示设备学习到的mac地址
	 * 
	 * @param param
	 * @return
	 */
	public String queryWIFIDeviceMAC(String param)
	{
		return new QueryJXDeviceMACService().work(param);
	}

	/**
	 * 江苏电信与综调接口，路由模式设备IP及DNS地址查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryDeviceIPorDNS(String param)
	{
		return new QueryDeviceIPorDNSService().work(param);
	}

	/**
	 * 江苏电信与综调接口，终端MAC地址查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryDeviceMAC(String param)
	{
		return new QueryDeviceMACService().work(param);
	}

	/**
	 * 江苏电信与综调接口、河北联通接口，终端的温度查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryDeviceTemp(String param)
	{
		logger.warn("servicename[QueryDeviceTempService]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new QueryDeviceTempService().work(param);
	}

	/**
	 * 江苏电信与综调接口，终端的供电电压查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryDeviceVottage(String param)
	{
		return new QueryDeviceVottageService().work(param);
	}

	/**
	 * 江苏电信与综调接口，终端的偏安电流查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryDeviceCurrent(String param)
	{
		return new QueryDeviceCurrentService().work(param);
	}

	public String ItmsTestService(String param)
	{
		return "ItmsTestService";
	}

	/**
	 * 江西设备端口信息采集接口 </br>
	 * 设备端口信息采集接口
	 * 
	 * @param param
	 * @return
	 */
	public String DevInfoGather(String param)
	{
		return new DevInfoGatherService().work(param);
	}

	/**
	 * @author cczhong
	 * @description 山东RMS可通过输入宽带账号查询绑定的设备LAN端口状态和WAN端口状态,发送接收光功率
	 * @param param
	 * @return
	 */
	public String portPonInfo(String param)
	{
		return new PortPonInfoService().work(param);
	}

	/**
	 * @author chenzj5
	 * @description 终端信息查询接口
	 * @param param
	 * @return
	 */
	public String queryDevConfigInfo(String param)
	{
		if (Global.NXDX.equals(Global.G_instArea))
		{
			logger.warn("servicename[QueryDevConfigInfoService]，调用端IP[{}],入参为：{}",
					new Object[] { getClientInfo(), param });
			return new QueryDevConfigInfoService4NX().work(param);
		}
		return QueryDevConfigInfoService.getInstance().work(param);
	}

	/**
	 * 湖北oui认证查询
	 * 
	 * @author cczhong
	 * @param param
	 * @return
	 */
	public String ouisearch(String param)
	{
		return new OuiSearchService().work(param);
	}

	/**
	 * 湖北规范版本设备查询
	 * 
	 * @author cczhong
	 * @param param
	 * @return
	 */
	public String querynormalversion(String param)
	{
		return new QueryNormalVersionService().work(param);
	}

	/**
	 * 江苏修改用户终端规格接口
	 * 
	 * @author yinlei3
	 * @param param
	 * @return
	 */
	public String moSpecInfo(String param)
	{
		return new MoSpecInfoService().work(param);
	}

	/**
	 * 江苏ITMS需求单：JSDX_ITMS-REQ-20151014-SHUJIE-001 宽带路由改桥接口
	 */
	public String RoutToBridge(String param)
	{
		if (Global.JSDX.equals(Global.G_instArea))
		{
			return new RoutToBridgeServiceForJs().work(param);
		}
		else
		{
			return new RoutToBridgeService().work(param);
		}
	}

	/**
	 * 江苏ITMS需求单：JSDX_ITMS-REQ-20161201-WJY-001（仿真测速接口)
	 */
	public String SimulationSpeed(String param)
	{
		return new SimulationSpeedService().work(param);
	}

	/**
	 * 新疆ITMS需求单：XJDX-REQ-20151028-HUJG3-001【ITMS系统自动零配置需求】
	 * 
	 * @param param
	 * @return
	 */
	public String zeroDeviceConfig(String param)
	{
		if (Global.JLDX.equals(Global.G_instArea))
		{
			return new ZeroConfigReportService4JL().work(param);
		}
		else
		{
			return new ZeroConfigReportService().work(param);
		}
	}

	/**
	 * 江西itms需求单：JXDX-ITMS-REQ-20151223-WUWF-001(ITMS平台对外接口需求) 无线信道修改功能接口
	 * 
	 * @param param
	 * @return
	 */
	public String modifyChannel(String param)
	{
		return new ModifyChannelService().work(param);
	}

	/**
	 * @author cczhong
	 * @description 山东RMS可通过输入宽带账号查询绑定的设备发送接收光功率
	 * @param param
	 * @return
	 */
	public String ponInfo(String param)
	{
		if (Global.NXDX.equals(Global.G_instArea))
		{
			logger.warn("servicename[PonInfoService]，调用端IP[{}],入参为：{}",
					new Object[] { getClientInfo(), param });
			return new PonInfoService4NX().work(param);
		}
		else
		{
			return new PonInfoService().work(param);
		}
	}

	/**
	 * @author cczhong
	 * @description 山东RMS可通过输入宽带账号查询绑定的设备LAN端口状态和WAN端口状态
	 * @param param
	 * @return
	 */
	public String portInfo(String param)
	{
		if (Global.JLLT.equals(Global.G_instArea))
		{
			return new PortInfoJLLTService().work(param);
		}
		return new PortInfoService().work(param);
	}

	/**
	 * @author yinlei3
	 * @description 江西终端设备管控查询接口
	 * @param param
	 * @return
	 */
	public String queryDeviceControl(String param)
	{
		return new QueryDeviceControlService().work(param);
	}

	/**
	 * 江西语音参数三项数据查询接口
	 * 
	 * @return
	 */
	public String queryVoIPWanInfo(String param)
	{
		return new QueryVOIPWanInfoService().work(param);
	}

	/**
	 * 江西语音参数五项数据查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryVoiceProfile(String param)
	{
		return new QueryVoiceProfileService().work(param);
	}

	/**
	 * 江西语音参数五项数据配置接口
	 * 
	 * @param param
	 * @return
	 */
	public String setVoiceProfile(String param)
	{
		return new SetVoiceProfileService().work(param);
	}

	/**
	 * 终端注册结果查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String regLogInfo(String param)
	{
		return new RegLogInfoService().work(param);
	}

	/**
	 * 工单状态查询接口
	 * 
	 * @author 岩
	 * @date 2016-6-30
	 * @param param
	 * @return
	 */
	public String queryLoidStatus(String param)
	{
		logger.debug("queryLoidStatus({})", param);
		return new QueryLoidStatusService().work(param);
	}

	/**
	 * 修改光猫VLAN参数接口
	 * 
	 * @author 岩
	 * @date 2016-7-11
	 * @param param
	 * @return
	 */
	public String changVlanParam(String param)
	{
		logger.debug("changVlanParamService({})", param);
		if (Global.NXDX.equals(Global.G_instArea))
		{
			logger.warn("servicename[changVlanParamService]，调用端IP[{}],入参为：{}",
					new Object[] { getClientInfo(), param });
			return new ChangVlanParamService4NX().work(param);
		}
		return new ChangVlanParamService().work(param);
	}

	/**
	 * 光猫管控设备序列号添加接口
	 * 
	 * @author yinlei3
	 * @date 2016-9-2
	 * @param 参数
	 * @return
	 */
	public String addDevSnParam(String param)
	{
		logger.debug("addDevSnParam({})", param);
		return new AddDevSnParamService().work(param);
	}

	/**
	 * 新疆电信：检查光猫版本是否支持组播接口
	 * 
	 * @author chenxj6
	 * @date 2016-8-29
	 * @param param
	 * @return
	 */
	public String queryIsMulticastVlan(String param)
	{
		logger.debug("queryIsMulticastVlan({})", param);
		return new QueryIsMulticastVlanService().work(param);
	}

	/**
	 * 新疆电信：ITMS检查光猫ITV口数据配置是否正确，采集itv的vlan配置是否正确
	 * 
	 * @author chenxj6
	 * @date 2016-8-29
	 * @param param
	 * @return
	 */
	public String checkITVData(String param)
	{
		logger.debug("checkITVData({})", param);
		return new CheckITVDataService().work(param);
	}

	/**
	 * 新疆电信：ITMS检查光猫cpe口数据配置是否正确，采集cpe的vlan配置是否正确
	 * 
	 * @author chenxj6
	 * @date 2016-11-07
	 * @param param
	 * @return
	 */
	public String checkcpeData(String param)
	{
		logger.debug("checkcpeData({})", param);
		return new CheckCpeDataService().work(param);
	}

	/**
	 * 新疆电信：ITMS家庭网关宽带账号查询接口
	 * 
	 * @author chenxj6guoqing
	 * @date 2016-9-28
	 * @param param
	 * @return
	 */
	public String getNetAccount(String param)
	{
		logger.debug("getNetAccount({})", param);
		return new GetNetAccountService().work(param);
	}

	/**
	 * 宁夏电信 设备配置查询接口
	 * 
	 * @author chenxj6
	 * @date 2016-10-13
	 * @return String
	 */
	public String queryDeviceConfig4NX(String param)
	{
		logger.debug("queryDeviceConfig4NX ({})", param);
		logger.warn("servicename[queryDeviceConfig4NX]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new DevConfigService4NX().work(param);
	}

	/**
	 * 宁夏电信 设备配置查询接口
	 * 
	 * @author chenxj6
	 * @date 2016-10-13
	 * @return String
	 */
	public String queryDeviceConfigNew(String param)
	{
		if (Global.HBLT.equals(Global.G_instArea))
		{
			return new DevConfigServiceLT().work(param);
		}
		else if (Global.NXDX.equals(Global.G_instArea))
		{
			logger.warn("servicename[queryDeviceConfigNew]，调用端IP[{}],入参为：{}",
					new Object[] { getClientInfo(), param });
			return new DevConfigServiceNew4NX().work(param);
		}
		logger.debug("queryDeviceConfigNew ({})", param);
		return new DevConfigServiceNew().work(param);
	}

	/**
	 * 语音数图配置接口
	 * 
	 * @author wangyan
	 * @date 2016-11-22
	 * @param param
	 * @return
	 */
	public String VoiceMapConfiguration(String param)
	{
		logger.debug("VoiceMapConfiguration ({})", param);
		return new VoiceMapConfiguration().work(param);
	}

	/**
	 * 山东联通RMS平台机HGU单个用户测速接口：
	 */
	public String testSpeed(String param)
	{
		logger.debug("testSpeed({})", param);
		if (Global.HBLT.equals(Global.G_instArea))
		{
			return testSpeedHBLT(param);
		}
		if (Global.SXLT.equals(Global.G_instArea))
		{
			return testSpeedSXLT(param);
		}
		if("nx_lt".equals(Global.G_instArea)){
			
			return testSpeedNXLT(param);
		}
		if ("jl_lt".equals(Global.G_instArea))
		{
			return testSpeedJLLT(param);
		}
		return new TestSpeedService().work(param);
	}
	
	private String testSpeedJLLT(String param)
	{
		logger.debug("testSpeedHBLT({})", param);
		return new TestSpeedJLLTService().work(param);
	}

	/**
	 * 宁夏联通RMS单台测试接口
	 * @param param
	 * @return
	 */
	public String testSpeedNXLT(String param){
		//return new TestSpeedService4NXLT().work(param);
		return null;
	}
	
	/**
	 * 河北联通RMS平台端到端单台测速接口
	 * 
	 * @param param
	 * @return
	 */
	public String testSpeedHBLT(String param)
	{
		logger.debug("testSpeedHBLT({})", param);
		return new TestSpeedHBLTService().work(param);
	}

	/**
	 * @描述 山西联通光猫测试接口
	 * @参数
	 * @返回值
	 * @创建人 lsr
	 * @创建时间 2019/8/21
	 * @throws @修改人和其它信息
	 */
	public String testSpeedSXLT(String param)
	{
		logger.debug("testSpeedSXLT({})", param);
		return new TestSpeedSXLTService().work(param);
	}

	/**
	 * 河北联通RMS平台HTTP现在业务质量测试接口：
	 */
	public String testSpeed4HBLT(String param)
	{
		logger.debug("testSpeed4HBLT({})", param);
		return new TestSpeedService4HBLT().work(param);
	}

	/**
	 * 吉林联通RMS平台HTTP现在业务质量测试接口：
	 */
	public String testSpeed4JLLT(String param)
	{
		if (Global.SXLT.equals(Global.G_instArea))
		{
			return new TestSpeedService4SXLT().work(param);
		}
		return new TestSpeedService4JLLT().work(param);
	}

	/**
	 * 安徽联通RMS平台HTTP现在业务质量测试接口：
	 */
	public String testSpeed4AHLT(String param)
	{
		logger.warn("testSpeed4JLLT({})", param);
		return new TestSpeedService4AHLT().work(param);
	}

	/**
	 * （新）河北联通RMS平台HTTP现在业务质量测试接口：上行和下行
	 */
	public String UpAndDownSpeedHBLT(String param)
	{
		logger.debug("testSpeed4HBLT({})", param);
		return new TestSpeedServiceUpAndDownHBLT().work(param);
	}

	/**
	 * 山东电信ITMS与OSS功能前置接口 修改密码：
	 */
	public String updateNetPwd(String param)
	{
		logger.debug("updateNetPwd({})", param);
		if (Global.SDLT.equals(Global.G_instArea))
		{
			return new UpdateNetPwdServic1().work(param);
		}
		else
		{
			return new UpdateNetPwdService().work(param);
		}
	}

	/**
	 * 河北ITMS与OSS功能前置接口 修改密码：
	 */
	public int ServiceChange(String adAcount, String LSHNo, String orderType,
			String newPassWord)
	{
		logger.warn("ServiceChange({})", adAcount, LSHNo, orderType, newPassWord);
		int res = new UpdateNetPwdServicHBLT().work(adAcount, LSHNo, orderType,
				newPassWord);
		logger.warn("ServiceChange end({})", res);
		return res;
	}

	/**
	 * 江苏电信ITMS与宗调 业务帐号查询接口：
	 */
	public String queryServiceAccount(String param)
	{
		logger.debug("queryServiceAccount({})", param);
		return new QueryAccountService().work(param);
	}

	/**
	 * 查询出用户48小时内解绑的设备序列号，设备序列号按照解绑时间倒叙排序：
	 * JSDX_ITMS-REQ-20170206-WJY-001（ITMS与综调系统回收终端查询接口)
	 */
	public String recycleDevSN(String param)
	{
		logger.debug("recycleDevSN({})", param);
		return new RecycleDevSN().work(param);
	}

	/**
	 * JSDX_ITMS-REQ-20170113-WJY-001（语音数图零配置管理新增功能)
	 * 
	 * @return
	 */
	public String voiceUser(String param)
	{
		logger.debug("VoiceUser({})", param);
		return new VoiceUserService().work(param);
	}

	/**
	 * JSDX_ITMS-REQ-20170113-WJY-001（语音数图零配置管理新增功能)
	 * 
	 * @return
	 */
	public String voiceUserSetResult(String param)
	{
		logger.debug("VoiceUserSetResult({})", param);
		return new VoiceUserSetResultService().work(param);
	}

	/**
	 * JSDX_ITMS-REQ-20161201-WJY-001（仿真测速需求新增功能)
	 * 
	 * @return
	 */
	public String speedResult(String param)
	{
		logger.debug("speedResult({})", param);
		return new SpeedResultService().work(param);
	}

	/**
	 * JSDX_ITMS-REQ-20170224-WJY-001（终端审核版本信息同步接口)
	 * 
	 * @return
	 */
	public String terminalVersionAudit(String param)
	{
		logger.debug("terminalVersionAudit({})", param);
		return new TerminalVersionAuditService().work(param);
	}

	/**
	 * 江西电信：Itv业务组播vlan查询接口
	 * 
	 * @author chenxj6
	 * @date 2017-03-24
	 * @param param
	 * @return
	 */
	public String queryMulticastVlan(String param)
	{
		logger.debug("queryMulticastVlan({})", param);
		return new QueryMulticastVlanService().work(param);
	}

	/**
	 * 安徽电信:序列号、串号查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String serialNo(String param)
	{
		logger.info("serialNo({})", param);
		return new SerialNoService().work(param);
	}

	/**
	 * 语音注册
	 * 
	 * @param param
	 * @return
	 */
	public String voiceServicesRegister4AH(String param)
	{
		logger.debug("voiceServicesRegister4AH ({})", param);
		return new VoipRegistTestService4AH().work(param);
	}

	/**
	 * JSDX_ITMS-REQ-20170410-WJY-001（综调接口新增终端属地查询接口)
	 * 
	 * @param param
	 * @return
	 */
	public String queryCityId(String param)
	{
		logger.debug("queryCityId ({})", param);
		return new QueryCityIdService().work(param);
	}

	/**
	 * AHDX_ITMS-REQ-20170509YQW-001（是否支持零配置开通设备版本查询接口）
	 * 
	 * @param param
	 * @return
	 */
	public String isstbbind(String param)
	{
		logger.debug("isstbbind ({})", param);
		return new IsStbBindService().work(param);
	}

	/**
	 * HBDX-REQ-20170912-XuPan-001（湖北ITMS+加V即插即用接口）
	 * 
	 * @param param
	 * @return
	 */
	public String itvAutoConfig(String param)
	{
		logger.debug("isstbbind ({})", param);
		return new ItvAutoConfigService().work(param);
	}

	/**
	 * 新疆电信：检查光猫版本是否支持百兆宽带接口
	 * 
	 * @param
	 * @param param
	 * @return
	 */
	public String queryIsMbBroadBand(String param)
	{
		logger.debug("queryIsMbBroadBand({})", param);
		return new QueryIsMbBroadBandService().work(param);
	}

	/**
	 * 查询家庭网关是否学习到机顶盒mac接口
	 */
	public String queryStbMac(String param)
	{
		logger.debug("queryStbMac({})", param);
		return new QueryStbMacService().work(param);
	}

	/**
	 * 语音POTS口的物理连接状态接口
	 * 
	 * @param param
	 * @return
	 */
	public String phoneConnectivityTest(String param)
	{
		logger.debug("phoneConnectivityTest({})", param);
		return new PhoneConnectivityTest().work(param);
	}

	/**
	 * JLDX-REQ-20170216-JIANGHAO6-001 终端管理地址连通性测试
	 * 
	 * @param param
	 * @return
	 */
	public String pingConnectivity(String param)
	{
		logger.debug("pingConnectivity({})", param);
		return new PingConnectivityService().work(param);
	}

	/**
	 * JXDX-ITMS-REQ-20170628-WUWF-001(ITMS平台对外接口-用户绑定设备情况查询cpmis侧需求)
	 * 
	 * @param param
	 * @return
	 */
	public String bindInfoCpmis(String param)
	{
		logger.debug("bindInfoCpmis({})", param);
		return new BindInfoCpmisService().work(param);
	}

	/**
	 * ping诊断仿真测试
	 * 
	 * @param param
	 * @return
	 */
	public String pingTest(String param)
	{
		logger.debug("pingTest({})", param);
		return new PingTest().work(param);
	}

	/**
	 * SDLT-REQ-2017-06-29-YUZHIJIAN-001(山东电信ITMS平台光宽接口) 管理密码重置
	 * 
	 * @param param
	 * @return
	 */
	public String changePas(String param)
	{
		logger.debug("changePas({})", param);
		return new ChangePasService().work(param);
	}

	/**
	 * A8语音查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryvoip(String param)
	{
		logger.debug("queryvoip({})", param);
		return new QueryVoip().work(param);
	}

	/**
	 * 山东联通RMS平台HTTP现在业务质量测试接口：
	 */
	public String testSpeed4SDLT(String param)
	{
		logger.debug("testSpeed4SDLT({})", param);
		return new TestSpeedService4SDLT().work(param);
	}

	/**
	 * 湖北电信下挂设备信息采集
	 * 
	 * @param param
	 * @return
	 */
	public String macGather(String param)
	{
		logger.warn("macGather({})", param);
		return new MacGatherService().work(param);
	}

	/**
	 * 语音端口、电话号码查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String VoipportInfo(String param)
	{
		logger.warn("VoipportInfo({})", param);
		return new ActivationOfVoiceService().work(param);
	}

	/************************* 重庆接口开始 *********************************************/
	/**
	 * 用户信息查询
	 * 
	 * @param param
	 * @return String
	 */
	public String startGetUserInfoDiag(String param)
	{
		logger.debug("startGetUserInfoDiag({})", param);
		return new StartGetUserInfoDiagService().work(param);
	}

	/**
	 * PING接口
	 * 
	 * @param param
	 * @return String
	 */
	public String startPingDiag(String param)
	{
		logger.debug("startPingDiag({})", param);
		return new StartPingDiagService().work(param);
	}

	/**
	 * 设备重启接口
	 * 
	 * @param param
	 * @return String
	 */
	public String startRebootDiag(String param)
	{
		logger.debug("startRebootDiag({})", param);
		return new StartRebootDiagService().work(param);
	}

	/**
	 * 业务查询接口
	 * 
	 * @param param
	 * @return String
	 */
	public String getGetServiceStatus(String param)
	{
		logger.debug("getGetServiceStatus({})", param);
		return new GetGetServiceStatusService().work(param);
	}

	/**
	 * 查询终端超级密码
	 * 
	 * @param param
	 * @return String
	 */
	public String queryTerminalPasswd(String param)
	{
		logger.debug("queryTerminalPasswd({})", param);
		return new QueryTerminalPasswdService().work(param);
	}

	/**
	 * 业务重发
	 * 
	 * @param param
	 * @return String
	 */
	public String factoryReset(String param)
	{
		logger.debug("factoryReset({})", param);
		return new FactoryResetService().work(param, false);
	}

	/**
	 * 用户配置执行情况查询
	 * 
	 * @param param
	 * @return String
	 */
	public String queryBussinessInfo(String param)
	{
		logger.debug("queryBussinessInfo({})", param);
		return new QueryBussinessInfoService().work(param);
	}

	/**
	 * 用户配置信息查询
	 * 
	 * @param param
	 * @return String
	 */
	public String queryActionInfo(String param)
	{
		logger.debug("queryActionInfo({})", param);
		return new QueryActionInfoService().work(param);
	}

	/**
	 * 终端信息查询
	 * 
	 * @param param
	 * @return
	 */
	public String queryTerminalInfo(String param)
	{
		logger.debug("queryTerminalInfo({})", param);
		return new QueryTerminalInfoService().work(param);
	}

	/**
	 * 绑定关系查询
	 * 
	 * @param param
	 * @return
	 */
	public String feedbackWorkTicketsInfo(String param)
	{
		logger.debug("feedbackWorkTicketsInfo({})", param);
		return new FeedbackWorkTicketsInfoService().work(param);
	}

	/**
	 * 修改wifi密码（非公众wifi）
	 * 
	 * @param param
	 * @return
	 */
	public String changeWifiPassword(String param)
	{
		logger.debug("changeWifiPassword({})", param);
		return new ChangeWifiPasswordService().work(param);
	}

	/**
	 * 工单下发情况查询
	 * 
	 * @param param
	 * @return
	 */
	public String queryWorkTicketsInfo(String param)
	{
		logger.debug("queryWorkTicketsInfo({})", param);
		return new QueryWorkTicketsInfoService().work(param);
	}

	/**
	 * 桥接/路由模式查询
	 * 
	 * @param param
	 * @return
	 */
	public String queryRgModeInfo(String param)
	{
		logger.debug("queryRgModeInfo({})", param);
		return new QueryRgModeInfoService().work(param);
	}

	/**
	 * OUN的网口状态查询
	 * 
	 * @param param
	 * @return
	 */
	public String queryLanInfo(String param)
	{
		logger.debug("queryLanInfo({})", param);
		return new QueryLanInfoService().work(param);
	}

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
	 * 设备光功率及连接信息查询
	 * 
	 * @param param
	 * @return
	 */
	public String queryConnectionInfo(String param)
	{
		logger.debug("queryConnectionInfo({})", param);
		return new QueryConnectionInfoService().work(param);
	}

	/**
	 * 通用接口查询
	 * 
	 * @param param
	 * @return
	 */
	public String commonInterfaceOperation(String param)
	{
		logger.debug("commonInterfaceOperation({})", param);
		return new CommonInterfaceOperationService().work(param);
	}

	/**
	 * 修改光猫无线接入数
	 * 
	 * @param param
	 * @return
	 */
	public String setGatewayTerminalNumber(String param)
	{
		logger.debug("setGatewayTerminalNumber({})", param);
		return new SetGatewayTerminalNumberService().work(param);
	}

	/**
	 * VLAN信息实时查询
	 * 
	 * @param param
	 * @return
	 */
	public String getVLanInfoOfTerminal(String param)
	{
		logger.debug("getVLanInfoOfTerminal({})", param);
		return new GetVLanInfoOfTerminalService().work(param);
	}

	/************************* 重庆接口结束 *********************************************/
	/**
	 * 根据用户帐号或者设备序列号查询ITV所在LAN口情况
	 * 
	 * @param
	 * @param param
	 * @return
	 */
	public String queryIptvLanPort(String param)
	{
		logger.debug("queryIptvLanPort()");
		return new QueryIptvLanPortService().work(param);
	}

	/**
	 * JSDX_ITMS-REQ-20180425-WJY-002（综调接口新增光猫LAN连接速率、状态、适配速率接口功能)
	 * 
	 * @param param
	 * @return
	 */
	public String queryLanEtherConfig(String param)
	{
		return new QueryLanService().work(param);
	}

	/**
	 * XJDX-REQ-20180504-laijun-001
	 * 
	 * @param param
	 * @return
	 */
	public String selectHTTPSpead(String param)
	{
		return new SelectHTTPSpeadService().work(param);
	}

	/**
	 * 宁夏 NXDX-ITMS-20180710-LX-002 家庭网关修改WIFI信道接口
	 **/
	public String changeWIFIChannel(String param)
	{
		logger.warn("changeWIFIChannel start!");
		logger.warn("servicename[changeWIFIChannel]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new ModifyWifiChannelService().work(param);
	}

	/**
	 * 宁夏 NXDX-ITMS-20180710-LX-002 家庭网关修改WIFI发送功率
	 **/
	public String changeWIFITPower(String param)
	{
		logger.warn("changeWIFITPower start!");
		logger.warn("servicename[changeWIFITPower]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new ModifyWifiSendPowerService().work(param);
	}

	/**
	 * 宁夏 NXDX-ITMS-20180710-LX-002 家庭网关修改WIFI传真模式
	 **/
	public String changeFaxPattern(String param)
	{
		logger.warn("changeFaxPattern start!");
		logger.warn("servicename[changeWIFITPower]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new ModifyWifiFaxModelService().work(param);
	}

	/**
	 * 吉林开通上网IPV6功能接口
	 * 
	 * @param param
	 * @return
	 */
	public String openInternetIpv6(String param)
	{
		logger.warn("openInternetIpv6 start!");
		return new OpenInternetIpvsixService().work(param);
	}

	/**
	 * 宁夏通过设备序列号查询loid和宽带账号
	 * 
	 * @param param
	 * @return
	 */
	public String queryLoidNet(String param)
	{
		logger.warn("queryLoidNet start");
		logger.warn("servicename[queryLoidNet]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new QueryLoidNetService().work(param);
	}

	/**
	 * SDDX-ITMS-20180702-FMK-001(ITMS新增采集光猫业务信息的接口)
	 * 
	 * @param param
	 * @return
	 */
	public String getServInfofromDev(String param)
	{
		return new GetServInfofromDevService().work(param);
	}

	/**
	 * NMGDX_HHHT-REQ-20180925-JIALEI-002 光猫注册信息查询接口
	 * 
	 * @param param
	 * @return
	 */
	public String queryDevRegInfo(String param)
	{
		return new QueryDevRegInfoService().work(param);
	}

	/**
	 * JLDX-ITMS-20181016-JH-001(家庭网关mac和sn查询接口)
	 * 
	 * @param param
	 * @return
	 */
	public String queryDevSNMAC(String param)
	{
		return new QueryDevSNMACService().work(param);
	}

	/**
	 * JLLT-ITMS-REQ-20201029-JH001(政企网关SN和MAC地址查询)
	 * 
	 * @param param
	 * @return
	 */
	public String queryBbmsDevSNMAC(String param)
	{
		return new QueryBbmsDevSNMACService().work(param);
	}

	/**
	 * 吉林开启光猫igmpsnooping接口
	 * 
	 * @param param
	 * @return
	 */
	public String openIgmpSnooping(String param)
	{
		logger.warn("openIgmpSnooping start!");
		return new OpenIgmpSnoopingService().work(param);
	}

	/**
	 * 通过LOID查询设备MAC接口
	 * 
	 * @param param
	 * @return
	 */
	public String devMac(String param)
	{
		logger.warn("devMac start!");
		logger.warn("servicename[DevMacQueryBySNService]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new DevMacQueryBySNService().work(param);
	}

	/**
	 * 江西电信根据用户逻辑SN、宽带账号查询光猫是百兆光猫还是千兆光猫
	 * 
	 * @param param
	 * @return
	 */
	public String queryOnuRate(String param)
	{
		logger.warn("queryOnuRate start");
		return new QueryOnuRateBySNService().work(param);
	}

	/**
	 * 安徽电信根据用户逻辑SN、宽带账号查询终端当前wlan的连接状态（桥接还是路由），并返回调用结果。
	 * 
	 * @param param
	 * @return
	 */
	public String onuConnType(String param)
	{
		logger.warn("OnuConnType start");
		return new OnuConnTypeService().work(param);
	}

	/**
	 * 宁夏设备上传业务质量测试接口
	 * 
	 * @param param
	 * @return
	 */
	public String upLoadByHTTPSpead(String param)
	{
		logger.warn("upLoadByHTTPSpead start");
		logger.warn("servicename[UpLoadByHTTPSpeadService]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new UpLoadByHTTPSpeadService().work(param);
	}

	/**
	 * HTTP上传业务质量测试接口
	 * 
	 * @param param
	 * @return
	 */
	public String upLoadByHTTP(String param)
	{
		logger.debug("upLoadByHTTP({})", param);
		return new UpLoadByHTTP().work(param);
	}

	/**
	 * 江西电信ITMS+家庭网关互联网专线测速接口 2019/02/27
	 * 
	 * @param param
	 * @return
	 */
	public String specialSpeed(String param)
	{
		logger.debug("specialSpeed({})", param);
		return new SpecialSpeedService4JX().work(param);
	}

	/**
	 * JLDX-ITMS-20190514-JH-001
	 * 
	 * @param
	 * @param param
	 * @return
	 */
	public String devInternetIpv6Info(String param)
	{
		logger.debug("devInternetIpv6Info({})", param);
		return new DevInternetIpv6InfoService().work(param);
	}

	/**
	 * AHDX_ITMS-REQ-20190408YQW-002(预制设备查询接口) 2019/02/27
	 * 
	 * @param param
	 * @return
	 */
	public String InIt(String param)
	{
		logger.warn("InIt start !");
		return new QueryDeviceInItService().work(param);
	}

	/**
	 * 获取调用者信息
	 * 
	 * @return 调用者ip
	 */
	private String getClientInfo()
	{
		String clientIP = null;
		MessageContext mc = MessageContext.getCurrentContext();
		HttpServletRequest request = (HttpServletRequest) mc
				.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		clientIP = request.getRemoteAddr();
		return clientIP;
	}

	/**
	 * 河北联通ping文件服务器 @Description TODO @author guxl3 @date 2019年6月11日 @param
	 * param @return @throws
	 */
	public String fileServerPing_hb(String param)
	{
		logger.debug("devInternetIpv6Info({})", param);
		return new fileServerPing_hb().work(param);
	}

	public String fileServerFTP_hb(String param)
	{
		logger.debug("devInternetIpv6Info({})", param);
		return new fileServerFTP_hb().work(param);
	}

	/**
	 * 宁夏修改宽带密码
	 * 
	 * @author yaoli
	 * @param param
	 * @return
	 */
	public String setGMPasswd(String param)
	{
		logger.debug("setGMPasswd ({})", param);
		return new SetGMPasswdService().work(param);
	}

	public String setWIFIPasswd(String param)
	{
		logger.debug("setWIFIPasswd ({})", param);
		return new SetWIFIPasswdService().work(param);
	}

	/**
	 * 宁夏电信获取终端SSID和wifi密码
	 * 
	 * @param param
	 * @return
	 */
	public String showWIFIPasswd(String param)
	{
		logger.warn("servicename[showWIFIPasswd]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new QuerySSIDWifiPwdService().work(param);
	}

	/**
	 * 家庭网关最新在线时间接口 for 新疆
	 * 
	 * @param param
	 * @return
	 */
	public String LatestOnlineTime(String param)
	{
		logger.debug("LatestOnlineTime ({})", param);
		return new LatestOnlineTimeService().work(param);
	}

	/**
	 * 家庭网关设备信息查询接口 for 吉林联通
	 * 
	 * @param param
	 * @return
	 */
	public String queryDevInfo(String param)
	{
		logger.debug("getDeviceInfo ({})", param);
		return new GetDeviceInfoService().work(param);
	}

	/**
	 * 用于测试吉林联通工单接口，页面发xml格式工单即可 内部测试用
	 */
	public String testService_jllt(String param)
	{
		return new TestServerJLLTService().work(param);
	}

	/**
	 * 山西获取参数接口
	 * 
	 * @param param
	 * @return
	 */
	public String deviceQueryByPath(String param)
	{
		return new QueryByPathService().work(param);
	}

	/**
	 * 山西获取参数接口
	 * 
	 * @param param
	 * @return
	 */
	public String cpeConfig(String param)
	{
		return new CpeConfigService().work(param);
	}

	/**
	 * AHDX_ITMS-REQ-202005YQW-001（FRIST校园业务对外接口）initSuperGather
	 * 
	 * @param param
	 * @return
	 */
	public String OpenFrist(String param)
	{
		return new OpenFristService().work(param);
	}

	/**
	 * JXDX-REQ-ITMS-20200609-WWF-001(江西电信ITMS+家庭网关对外接口-2.31新增lan1口协商速率查询接口)-修改
	 * 
	 * @param param
	 * @return
	 */
	public String queryMaxBitRate(String param)
	{
		return new QueryMaxBitRateService().work(param);
	}

	public String queryRouterSpeed(String param)
	{
		logger.debug("CallService==queryRouterSpeed()");
		logger.warn("servicename[queryRouterSpeed]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new QueryRouterSpeedService().work(param);
	}

	/**
	 * REQ-ITMS-20200805-LWX-001（终端查询诊断封装能力接口）
	 * 
	 * @param param
	 * @return
	 */
	public String packingCapability(String param)
	{
		logger.warn("servicename[packingCapability]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new PackingCapabilityService().work(param);
	}

	/**
	 * REQ-ITMS-20200805-LWX-001（终端查询诊断封装能力接口）
	 * 
	 * @param param
	 * @return
	 */
	public String maxConnectionNumber(String param)
	{
		logger.warn("servicename[maxConnectionNumber]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new MaxConnectionNumberService().work(param);
	}

	/**
	 * REQ-ITMS-20200805-LWX-001（甘肃电信光猫WIFI信道修改接口）
	 * 
	 * @param param
	 * @return
	 */
	public String modifyWifiChannl(String param)
	{
		logger.warn("servicename[modifyWifiChannl]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new ModifyWifiChannlService().work(param);
	}

	/**
	 * REQ-ITMS-20200805-LWX-001（甘肃电信光猫穿墙模式与WIFI信号强度修改接口）
	 * 
	 * @param param
	 * @return
	 */
	public String modifyWifiPower(String param)
	{
		logger.warn("servicename[modifyWifiPower]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new ModifyWifiPowerService().work(param);
	}

	/**
	 * NXDX-REQ-ITMS-20200814-LX-001（开关光猫的ITV无线接口） @param param @return @throws
	 */
	public String setITVWIFI(String param)
	{
		logger.warn("setITVWIFI start!");
		logger.warn("servicename[setITVWIFI]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new SetITVWIFIService().work(param);
	}

	/**
	 * GSDX-REQ-ITMS-20200918-LWX-001 宽带上网诊断能力封装接口及loid和宽带账号一对多问题优化需求
	 * 采集宽带通道：连接方式、连接状态、绑定端口 光功率
	 * 
	 * @param param
	 * @return
	 */
	public String internetDetails(String param)
	{
		logger.warn("begin InternetDetails with param:{}", param);
		return new InternetDetailsService().work(param);
	}

	public String deviceSN(String param)
	{
		logger.warn("servicename[deviceSN]，调用端IP[{}],入参为：{}",
				new Object[] { getClientInfo(), param });
		return new DeviceSNService().work(param);
	}

	public String hbdxIptvSetValue(String param)
	{
		logger.warn("begin hbdxIptvSetValue with param:{}", param);
		return new HbdxIptvSetValueService().work(param);
	}

	/***
	 * @param param
	 * @return
	 */
	public String queryOperation(String param)
	{
		logger.warn("begin queryOperation with param:{}", param);
		return new QueryOperationServiceImpl().work(param);
	}
}