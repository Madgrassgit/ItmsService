package com.linkage.itms.hlj.dispatch.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linkage.itms.dispatch.service.*;
import com.linkage.itms.dispatch.service.DevOnlineService;
import com.linkage.itms.dispatch.service.DevRebootService;
import com.linkage.itms.dispatch.service.PingDiagnostic;
import com.linkage.itms.dispatch.service.QueryLanStateService;
import com.linkage.itms.hlj.dispatch.obj.XmlTranslate;
import com.linkage.itms.hlj.dispatch.service.*;
import com.linkage.itms.hlj.dispatch.service.PingConnectivityService;
import com.linkage.itms.nmg.dispatch.service.DevResetService;
import com.linkage.itms.nmg.dispatch.service.QueryPerformanceService;
import com.linkage.stbms.ids.bio.StbRebootBIO;
import com.linkage.stbms.ids.service.*;

/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-11-1
 * @category com.linkage.itms.hlj.dispatch.main
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class JlService {

	/** 日志记录 */
	private static Logger logger = LoggerFactory.getLogger(JlService.class);

	/**
	 * 家庭网关状态查询接口
	 */
	public String devOnline(String param) {
		logger.debug("JlService——》devOnline({})", param);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(param);
		String toOther = translate.jlToOtherLoid();
		// 2.将调整后的入参传入到service内
		String oldOut = new DevOnlineService().work(toOther);
		logger.warn("JlService——》devOnline({})", oldOut);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlDevOnline(oldOut);
		return otherTo;
	}

	/**
	 * 企业网关状态查询接口
	 */
	public String bbmsDevOnline(String param) {
		logger.debug("JlService——》devOnline({})", param);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(param);
		String toOther = translate.jlToOtherLoidBBMS();
		// 2.将调整后的入参传入到service内
		String oldOut = new DevOnlineService().work(toOther);
		logger.warn("JlService——》devOnline({})", oldOut);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlDevOnlineBBMS(oldOut);
		return otherTo;
	}
	
	/**
	 * 设备光功率，wan口状态查询
	 */
	public String ponInfo(String param) {
		logger.debug("JlService——》ponInfo({})", param);
		XmlTranslate translate = new XmlTranslate(param);
		// 1.转换param成原有的入参格式
		String toOther = translate.jlToOtherLoid();
		// 2.将调整后的入参传入到service内
		String oldOut = new QueryPerformanceService().work(toOther);
		logger.warn("JlService——》ponInfo({})", oldOut);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlPon(oldOut);
		return otherTo;
	}

	/**
	 * 家庭网关LAN口状态查询接口
	 */
	public String queryLanState(String param) {
		logger.debug("JlService——》queryLanState({})", param);
		XmlTranslate translate = new XmlTranslate(param);
		String toOther = translate.jlToOtherLoid();
		String oldOut = QueryDevConfigInfoService.getInstance().work(toOther);
		logger.warn("JlService——》queryLanState({})", oldOut);
		String otherTo = translate.otherToJlLAN(oldOut);
		return otherTo;
	}

	/**
	 * 家庭网关配置查询接口
	 * 
	 * @author 岩
	 * @date 2016-11-1
	 * @param param
	 * @return
	 */
	public String queryDeviceConfig4JL(String param) {
		logger.debug("JlService——》queryDeviceConfig4JL({})", param);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(param);
		String toOther = translate.jlToOtherLoid();
		// 2.将调整后的入参传入到service内
		String oldOut = new DevConfigService().work(toOther);
		logger.warn("JlService——》queryDeviceConfig4JL({})", oldOut);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlDevCon(oldOut);
		return otherTo;
	}

	/**
	 * 家庭网关远程恢复出厂设置接口
	 */
	public String reset(String param) {
		logger.debug("JlService——》reset({})", param);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(param);
		String toOther = translate.jlToOtherLoid();
		// 2.将调整后的入参传入到service内
		String oldOut = new DevResetService().work(toOther);
		logger.warn("JlService——》reset({})", oldOut);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlReset(oldOut);
		return otherTo;
	}

	/**
	 * 超级密码查询接口
	 */
	public String call(String param) {
		logger.debug("JlService——》call({})", param);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(param);
		String toOther = translate.jlToOtherLoid();
		// 2.将调整后的入参传入到service内
		String oldOut = new TelepasswdService().work(toOther);
		logger.warn("JlService——》call({})", oldOut);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlCall(oldOut);
		return otherTo;
	}

	/**
	 * 设备重启
	 */
	public String reboot(String param) {
		logger.debug("JlService——》reboot({})", param);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(param);
		String toOther = translate.jlToOtherLoid();
		// 2.将调整后的入参传入到service内
		String oldOut = new DevRebootService().work(toOther);
		logger.warn("JlService——》reboot({})", oldOut);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlReset(oldOut);
		return otherTo;
	}

	/**
	 * 业务下发
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-6-25
	 * @return String
	 */
	public String doService(String param) {
		logger.debug("JlService——》doService({})", param);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(param);
		String toOther = translate.jlToOtherDev();
		// 2.将调整后的入参传入到service内
		String outXml = new ServiceDoneService().work(toOther);
		logger.warn("JlService——》doService({})", outXml);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlDoService(outXml);
		return otherTo;
	}

	/**
	 * 电信ITV用户与设备解绑 可通过输入用户业务账号或者机顶盒mac来解除用户与设备的绑定关系
	 * 
	 * @param inParam
	 * @return
	 */
	public String UnbindIptvStbMac(String inParam) {
		logger.debug("JlService——》UnbindIptvStbMac({})", inParam);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(inParam);
		String toOther = translate.jlToOtherStbAcc();
		// 2.将调整后的入参传入到service内
		String outXml = new UnbindIptvStbMacService().work(toOther);
		logger.warn("JlService——》UnbindIptvStbMac({})", outXml);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlStbUnbind(outXml);
		return otherTo;
	}

	/**
	 * 电信ITV用户与设备手工绑定 可通过输入用户业务账号或者机顶盒mac或机顶盒序列号来绑定用户与设备的关系
	 * 
	 * @param inParam
	 * @return
	 */
	public String bindIptvStbMac(String inParam) {
		logger.debug("JlService——》bindIptvStbMac({})", inParam);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(inParam);
		String toOther = translate.jlToOtherStbBind();
		if ("-1".equals(toOther)) {
			return translate.otherToJlStbbind2();
		}
		// 2.将调整后的入参传入到service内
		String outXml = new BindIptvStbMacService().work(toOther);
		logger.warn("JlService——》bindIptvStbMac({})", outXml);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlStbbind(outXml);
		return otherTo;
	}

	/**
	 * 重启接口
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @param inParam
	 * @return
	 */
	public String setStbReboot(String inParam) {
		logger.debug("JlService——》setStbReboot({})", inParam);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(inParam);
		String toOther = translate.jlToOtherStbAcc();
		// 2.将调整后的入参传入到service内
		String outXml = new StbRebootBIO().setStbRebootTwo(toOther);
		logger.warn("JlService——》setStbReboot({})", outXml);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlStbReboot(outXml);
		return otherTo;
	}

	/**
	 * 机顶盒业务下发
	 * 
	 * @author 岩
	 * @date 2016-11-2
	 * @param inParam
	 * @return
	 */
	public String doStbService(String inParam) {
		logger.debug("JlService——》doStbService({})", inParam);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(inParam);
		String toOther = translate.jlToOtherStbAcc();
		// 2.将调整后的入参传入到service内
		String outXml = new DoServiceService().work(toOther);
		logger.warn("JlService——》doStbService({})", outXml);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlStbDoService(outXml);
		return otherTo;
	}

	/**
	 * @author 岩
	 * @date 2016-11-2
	 * @param inParam
	 * @return
	 */
	public String devStbOnline(String inParam) {
		logger.debug("JlService——》devStbOnline({})", inParam);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(inParam);
		String toOther = translate.jlToOtherStbAcc();
		// 2.将调整后的入参传入到service内
		String outXml = new GetStbOnlineStatusService().work(toOther);
		logger.warn("JlService——》devStbOnline({})", outXml);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlStbOnline(outXml);
		return otherTo;
	}
	
	/**
	 * 
	 * @author wangyan
	 * @date 2016-11-18
	 * @param inParam
	 * @return
	 */
	public String updateStbIptvPsw(String inParam) {
		logger.debug("JlService——》updateStbIptvPsw({})", inParam);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(inParam);
		String toOther = translate.jlToOtherUpdatePwd();
		logger.warn("JlService——》updateStbIptvPsw({})", toOther);
		// 更新失败，则直接返回错误提示 更改失败，acc不存在
		if ("-1".equals(toOther)) {
			return translate.otherToJlStbUpPwd();
		}
		// 2.将调整后的入参传入到service内
		String outXml = new DoServiceService().work(toOther);
		logger.warn("JlService——》updateStbIptvPsw({})", outXml);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlStbDoService(outXml);
		return otherTo;
	}
	
	/**
	 * 家庭网关信息查询接口
	 */
	public String queryDevState(String param) {
		logger.debug("JlService——》queryDevState({})", param);
		XmlTranslate translate = new XmlTranslate(param);
		String toOther = translate.jlToOtherLoid();
		String oldOut = QueryDevConfigInfoService.getInstance().work(toOther);
		logger.warn("JlService——》queryDevState({})", oldOut);
		String otherTo = translate.otherToJlDevState(oldOut);
		return otherTo;
	}
	
	public String devStbVersion(String param) {
		logger.debug("JlService——》devStbVersion({})", param);
		XmlTranslate translate = new XmlTranslate(param);
		// 1.转换param成原有的入参格式
		String toOther = translate.jlToOtherStbVersion();
		// 2.将调整后的入参传入到service内
		String oldOut = new GetStbInfo4xjService().work(toOther);
		logger.warn("JlService——》devStbVersion({})", oldOut);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlDevVersion(oldOut);
		return otherTo;
	}
	
	/**
	 * 终端Ping测试接口
	 *
	 * @param param 入参xml
	 * @return 返回xml
	 */
	public String pingDiagnostic(String param) {
		logger.debug("JlService==>pingDiagnostic({})", param);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(param);
		String toOther = translate.jlToOtherPing();
		// 2.将调整后的入参传入到service内
		String oldOut = new PingDiagnostic().work(toOther);
		logger.warn("JlService==>pingDiagnostic({})", oldOut);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlPing(oldOut);
		return otherTo ;
	}
	
	
	/**
	 * 终端下载质量测速接口
	 * @param param 入参xml
	 * @return 返回xml
	 */
	public String downLoadByHTTPSpead(String param) {
		logger.debug("JlService==>downLoadByHTTPSpead({})", param);
		String rtnXML = new DownLoadByHTTP4JL().work(param);
		return rtnXML;
	}
	
	
	/**
	 * 2.22.模拟PPPoE拨测接口
	 * @param param 入参xml
	 * @return 返回xml
	 */
	public String PPPoEDial(String param) {
		logger.debug("JlService==>PPPoEDial({})", param);
		String rtnXML = new PPPoEDial4JL().work(param);
		return rtnXML;
	}
	
	
	
	/**
	 * 2.23.桥接和路由切换接口
	 *
	 * @param param 入参xml
	 * @return 返回xml
	 */
	public String BridgeToRout(String param) {
		logger.debug("JlService==>BridgeToRout({})", param);
		// 1.转换param成原有的入参格式
		XmlTranslate translate = new XmlTranslate(param);
		String toOther = translate.jlToOtherBridgeToRout();
		// 2.将调整后的入参传入到service内
		String oldOut = new BridgeToRoutService4Jx().work(toOther);
		logger.warn("JlService==>BridgeToRout({})", oldOut);
		// 3.再将回参进行相应的调整并返回
		String otherTo = translate.otherToJlBridgeToRout(oldOut);
		return otherTo ;
	}
	
	/**
	 * JLDX-REQ-20170216-JIANGHAO6-001 终端管理地址连通性测试
	 * @param param
	 * @return
	 */
	public String pingConnectivity(String param) {
		logger.debug("pingConnectivity({})", param);
		return new PingConnectivityService().work(param);
	}
	
	/**
	 * JLDX-REQ-20170801-JIANGHAO6-001 家庭网关语音业务下发接口
	 * @param param
	 * @return
	 */
	public String doVoipService(String param) {
		logger.debug("doVoipService({})", param);
		return new DoVoipService().work(param);
	}
	
	/**
	 * JLDX-REQ-20170801-JIANGHAO6-001 家庭网关语音工单注册状态信息查询接口
	 * @param param
	 * @return
	 */
	public String queryVoipBssState(String param) {
		logger.debug("queryVoipBssState({})", param);
		return new QueryVoipBssStateService().work(param);
	}
	
	/**
	 * JLDX-REQ-20170801-JIANGHAO6-001 家庭网关语音全配置查询接口
	 * @param param
	 * @return
	 */
	public String queryVoipDeviceIdState(String param) {
		logger.debug("queryVoipDeviceIdState({})", param);
		return new QueryVoipDeviceIdStateService().work(param);
	}
	
	/**
	 * JLDX-REQ-20170814-JIANGHAO6-001(家庭网关单独LAN口状态查询接口)
	 * @param param
	 * @date 20170817
	 * @return
	 */
	public String querySplitLanState(String param) {
		logger.debug("JlService——》querySplitLanState({})", param);
		XmlTranslate translate = new XmlTranslate(param);
		String toOther = translate.jlToOtherQuerySplitLanState();
		String oldOut = new QueryLanStateService().work(toOther);
		logger.warn("JlService——》querySplitLanState({})", oldOut);
		String otherTo = translate.otherToJlQuerySplitLanState(oldOut);
		return otherTo; 
	}
}
