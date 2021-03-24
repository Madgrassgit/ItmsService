
package com.linkage.itms.dispatch.main;

import javax.xml.rpc.holders.IntHolder;
import javax.xml.rpc.holders.StringHolder;

import com.linkage.itms.Global;
import com.linkage.itms.dispatch.gsdx.service.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.gsdx.beanObj.CPEMission;
import com.linkage.itms.dispatch.gsdx.beanObj.CPEMissionResult;
import com.linkage.itms.dispatch.gsdx.beanObj.CpeOnlineInfo;
import com.linkage.itms.dispatch.gsdx.beanObj.CpeOnlineInfoRst;
import com.linkage.itms.dispatch.gsdx.beanObj.GetXDSLInfoResult;
import com.linkage.itms.dispatch.gsdx.beanObj.Para;
import com.linkage.itms.dispatch.gsdx.beanObj.ParaWithType;
import com.linkage.itms.dispatch.gsdx.beanObj.SetParameterResult;
import com.linkage.itms.dispatch.gsdx.beanObj.UserIndex;
import com.linkage.itms.dispatch.gsdx.beanObj.pingInfo;
import com.linkage.itms.dispatch.gsdx.holders.CpeInfoRstHolder;
import com.linkage.itms.dispatch.gsdx.holders.CpeOnlineInfoHolder;
import com.linkage.itms.dispatch.gsdx.holders.CpeOnlineInfoRstHolder;
import com.linkage.itms.dispatch.gsdx.holders.DiagnoseResultHolder;
import com.linkage.itms.dispatch.gsdx.holders.GetParameterResultHolder;
import com.linkage.itms.dispatch.gsdx.holders.SetParameterResultHolder;
import com.linkage.itms.dispatch.gsdx.holders.pingResultHolder;
import com.linkage.itms.dispatch.sxdx.holders.CpeFlowHolder;
import com.linkage.itms.dispatch.sxdx.service.GetCpeFlowService;
import com.linkage.itms.dispatch.sxdx.service.GetSnRstService;
import com.linkage.itms.dispatch.sxdx.service.GetVoipPWDService;
import com.linkage.itms.dispatch.sxdx.service.setCpeServiceStateService;

/**
 * 甘肃电信业务发放场景接口入口类
 * 
 * @author fanjm (Ailk No.)
 * @version 1.0
 * @since 2019-6-13
 * @category com.linkage.itms.dispatch.main
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DiagnoseService
{

	private static Logger logger = LoggerFactory.getLogger(DiagnoseService.class);

	/**
	 * 终端重启接口
	 * 
	 * @param user
	 * @return int
	 */
	public void cpeReboot(UserIndex user, IntHolder cpeRebootReturn)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("cpeReboot[{}]==>方法开始UserIndex user[{}]", LSHNo, user);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			cpeRebootReturn.value = new CpeRebootService("cpeReboot")
					.work(intRequest.asXML());
			logger.warn("cpeReboot[{}]==>方法结束,返回{}", LSHNo, cpeRebootReturn.value);
		}
		catch (Exception e)
		{
			logger.error("cpeReboot[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 恢复终端出厂设置接口
	 * 
	 * @param user
	 * @param type
	 * @return int >1:成功且多条数据，返回值为结果个数。在此情况下不对任何终端执行恢复出厂操作。 1：成功且对某个终端执行了恢复出厂操作。
	 *         0：没有找到符合条件的终端 -1：终端不在线 -2: 其他错误。
	 */
	public void cpeFactoryReset(UserIndex user, int type, IntHolder cpeFactoryResetReturn)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("cpeFactoryReset[{}]==>方法开始UserIndex user[{}],type[{}]", LSHNo, user,
				type);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			intRequest.addElement("resetType").addText(StringUtil.getStringValue(type));
			cpeFactoryResetReturn.value = new CpeFactoryResetService("cpeFactoryReset")
					.work(intRequest.asXML());
			logger.warn("cpeFactoryReset[{}]==>方法结束,返回{}", LSHNo,
					cpeFactoryResetReturn.value);
		}
		catch (Exception e)
		{
			logger.error("cpeFactoryReset[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 查询终端的详细信息接口
	 * 
	 * @param user
	 * @return int
	 */
	public void getCpeOnlineInfo(UserIndex user,
			CpeOnlineInfoHolder getCpeOnlineInfoReturn)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("getCpeOnlineInfo[{}]==>方法开始UserIndex user[{}]", LSHNo, user);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			GetCpeOnlineInfoService service = new GetCpeOnlineInfoService(
					"getCpeOnlineInfo");
			int errcode = service.work(intRequest.asXML());
			CpeOnlineInfo cpeOnlineInfo = new CpeOnlineInfo();
			cpeOnlineInfo.setBasicInfo(service.getBasicInfo());
			cpeOnlineInfo.setUserInfo(service.getUserInfo());
			cpeOnlineInfo.setServiceInfoList(service.getServiceInfoList());
			cpeOnlineInfo.setErrorCode(errcode);
			getCpeOnlineInfoReturn.value = cpeOnlineInfo;
			logger.warn("getCpeOnlineInfo[{}]==>方法结束,错误码{},返回对象{}", LSHNo, errcode,
					cpeOnlineInfo);
		}
		catch (Exception e)
		{
			logger.error("getCpeOnlineInfo[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 查询终端在线详细信息
	 * 
	 * @param user
	 * @param getCpeOnlineRst
	 */
	public void getCpeOnlineInfoForDsl(UserIndex user,
			CpeOnlineInfoRstHolder getCpeOnlineRst)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("getCpeOnlineInfoForDsl[{}]==>方法开始UserIndex user[{}]", LSHNo, user);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			GetCpeOnlineInfoForDslService service = new GetCpeOnlineInfoForDslService(
					"getCpeOnlineInfoForDsl");
			CpeOnlineInfoRst cpeOnlineInfoRst = service.work(intRequest.asXML());
			getCpeOnlineRst.value = cpeOnlineInfoRst;
			logger.warn("CpeOnlineInfoRst[{}]==>方法结束,错误码{},返回对象{}", LSHNo,
					cpeOnlineInfoRst.getiOpRst(), cpeOnlineInfoRst);
		}
		catch (Exception e)
		{
			logger.error("CpeOnlineInfoRst[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 终端的TR069节点下发配置值
	 * 
	 * @param user
	 * @param parameterValues
	 *            下发参数
	 */
	public void setCpeParameterValues(UserIndex user, ParaWithType[] parameterValues,
			SetParameterResultHolder setCpeParameterValuesReturn)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn(
				"setCpeParameterValues[{}]==>方法开始UserIndex user[{}],ParaWithType parameterValues [{}]",
				LSHNo, user, parameterValues);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			for (ParaWithType paramType : parameterValues)
			{
				Element param = intRequest.addElement("param");
				param.addElement("name").addText(paramType.getName());
				param.addElement("value").addText(paramType.getValue());
			}
			SetCpeParameterValuesService service = new SetCpeParameterValuesService(
					"setCpeParameterValues");
			SetParameterResult result = service.work(intRequest.asXML());
			setCpeParameterValuesReturn.value = result;
			logger.warn("setCpeParameterValues[{}]==>方法结束,返回对象{}", LSHNo, result);
		}
		catch (Exception e)
		{
			logger.error("setCpeParameterValues[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 终端PING接口(启动PING测试)
	 * 
	 * @param user
	 *            用户对象
	 * @param ping
	 *            ping对象
	 * @return int 返回
	 */
	public void startPing(UserIndex user, pingInfo ping, IntHolder res)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("startPing[{}]==>方法开始UserIndex user[{}],pingInfo[{}]", LSHNo, user,
				ping);
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			intRequest.addElement("inftype")
					.addText(StringUtil.getStringValue(ping.getInterface()));
			intRequest.addElement("ip").addText(ping.getIp());
			intRequest.addElement("size")
					.addText(StringUtil.getStringValue(ping.getSize()));
			intRequest.addElement("num")
					.addText(StringUtil.getStringValue(ping.getNum()));
			intRequest.addElement("overtime")
					.addText(StringUtil.getStringValue(ping.getOvertime()));
			res.value = new StartPingService("startPing").work(intRequest.asXML());
			logger.warn("startPing[{}]==>方法结束,返回{}", LSHNo, res.value);
		}
		catch (Exception e)
		{
			logger.error("startPing[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 终端PING接口(获取PING结果信息)
	 * 
	 * @param user
	 * @return int
	 */
	public void getPingResult(UserIndex user, pingResultHolder res)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("getPingResult[{}]==>方法开始UserIndex user[{}]", LSHNo, user);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			res.value = new GetPingResultService("getPingResult")
					.work(intRequest.asXML());
			logger.warn("getPingResult[{}]==>方法结束,返回{}", LSHNo, res.value);
		}
		catch (Exception e)
		{
			logger.error("getPingResult[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 查询终端/家庭网关的信息
	 * 
	 * @param user
	 * @return int
	 */
	public void getCpeInfo(UserIndex user, String InterfaceType,
			CpeInfoRstHolder getCpeInfoReturn)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("getCpeInfo[{}]==>方法开始UserIndex user[{}],InterfaceType:[{}]", LSHNo,
				user, InterfaceType);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			intRequest.addElement("InterfaceType").addText(InterfaceType);
			if ("ah_lt".equals(Global.G_instArea))
			{
				getCpeInfoReturn.value = new GetCpeInfoServiceAHLT("getCpeInfo")
						.work(intRequest.asXML());
			}
			else
			{
				getCpeInfoReturn.value = new GetCpeInfoService("getCpeInfo")
						.work(intRequest.asXML());
			}
			logger.warn("getCpeInfo[{}]==>方法结束,返回{}", LSHNo, getCpeInfoReturn.value);
		}
		catch (Exception e)
		{
			logger.error("getCpeInfo[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 通过此接口，传入某个节点名称的路径获取到该节点下的所有叶子节点的参数值（包含下面所有层级）
	 * 
	 * @param user
	 * @return int
	 */
	public void getCpeParameterByNodePath(UserIndex user, String nodePath,
			GetParameterResultHolder res)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn(
				"getCpeParameterByNodePath[{}]==>方法开始UserIndex user[{}],nodePath:[{}]",
				LSHNo, user, nodePath);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			intRequest.addElement("nodePath").addText(nodePath);
			res.value = new GetCpeParameterByNodePathService("getCpeParameterByNodePath")
					.work(intRequest.asXML());
			logger.warn("getCpeParameterByNodePath[{}]==>方法结束,返回{}", LSHNo, res.value);
		}
		catch (Exception e)
		{
			logger.error("getCpeParameterByNodePath[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 通过此接口，传入某个节点名称的参数路径获数组返回这些节点参数名称对应的参数值
	 * 
	 * @param user
	 * @return int
	 */
	public void getCpeParameterByNodeName(UserIndex user, String[] parameterNames,
			GetParameterResultHolder res)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn(
				"getCpeParameterByNodeName[{}]==>方法开始UserIndex user[{}],parameterNames:[{}]",
				LSHNo, user, parameterNames);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			res.value = new GetCpeParameterByNodeNameService("getCpeParameterByNodeName")
					.work(intRequest.asXML(), parameterNames);
			logger.warn("getCpeParameterByNodeName[{}]==>方法结束,返回{}", LSHNo, res.value);
		}
		catch (Exception e)
		{
			logger.error("getCpeParameterByNodeName[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 通用诊断接口
	 * 
	 * @param user
	 * @return int
	 */
	public void diagnoseTest(UserIndex user, String procName, Para[] paraList,
			IntHolder res, IntHolder diagnoseTestReturn)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("diagnoseTest[{}]==>方法开始UserIndex user[{}],procName:[{}]", LSHNo,
				user, procName);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			intRequest.addElement("procName").addText(procName);
			res.value = new DiagnoseTestService("diagnoseTest").work(intRequest.asXML(),
					paraList);
			diagnoseTestReturn.value = res.value;
			logger.warn("diagnoseTest[{}]==>方法结束,返回{}", LSHNo, res.value);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("diagnoseTest[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 通用诊断接口
	 * 
	 * @param user
	 * @return int
	 */
	public void getDiagnoseResult(UserIndex user, String procName,
			DiagnoseResultHolder res, DiagnoseResultHolder getDiagnoseResultReturn)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("getDiagnoseResult[{}]==>方法开始UserIndex user[{}],procName:[{}]", LSHNo,
				user, procName);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			intRequest.addElement("procName").addText(procName);
			res.value = new GetDiagnoseResultService("getDiagnoseResult")
					.work(intRequest.asXML());
			getDiagnoseResultReturn.value = res.value;
			logger.warn("getDiagnoseResult[{}]==>方法结束,返回{}", LSHNo, res.value);
		}
		catch (Exception e)
		{
			logger.error("getDiagnoseResult[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 该接口实现单个终端快速执行计划任务
	 * 
	 * @param user
	 * @return int
	 */
	public void startCPEMission(UserIndex user, String fileName, int iOperType,
			CPEMission cpeMission)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn(
				"startCPEMission[{}]==>方法开始UserIndex user[{}],iOperType:[{}],fileName:[{}]",
				LSHNo, user, iOperType, fileName);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			intRequest.addElement("iOperType")
					.addText(StringUtil.getStringValue(iOperType));
			intRequest.addElement("fileName").addText(fileName);
			cpeMission = new StartCPEMissionService("startCPEMission")
					.work(intRequest.asXML());
			logger.warn("startCPEMission[{}]==>方法结束,返回{}", LSHNo, cpeMission);
		}
		catch (Exception e)
		{
			logger.error("startCPEMission[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 该接口查询单个终端计划执行结果信息
	 * 
	 * @param user
	 * @return int
	 */
	public void getCPEMissionResult(UserIndex user, int iMissionID,
			CPEMissionResult cpeMission)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("getCPEMissionResult[{}]==>方法开始UserIndex user[{}],iMissionID:[{}]",
				LSHNo, user, iMissionID);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			intRequest.addElement("iMissionID")
					.addText(StringUtil.getStringValue(iMissionID));
			cpeMission = new GetCPEMissionResultService("getCPEMissionResult")
					.work(intRequest.asXML());
			logger.warn("getCPEMissionResult[{}]==>方法结束,返回{}", LSHNo, cpeMission);
		}
		catch (Exception e)
		{
			logger.error("getCPEMissionResult[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 该接口重新注册语音业务
	 * 
	 * @param user
	 * @return int
	 */
	public void resetService(UserIndex user, IntHolder resetServiceReturn)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("resetService[{}]==>方法开始UserIndex user[{}]", LSHNo, user);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("iParaType")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("Value").addText(user.getIndex());
			resetServiceReturn.value = new ResetService("resetService")
					.work(intRequest.asXML());
			logger.warn("resetService[{}]==>方法结束,返回{}", LSHNo, resetServiceReturn);
		}
		catch (Exception e)
		{
			logger.error("resetService[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 该接口查询终端/家庭网关节点信息
	 * 
	 * @param user
	 * @return int
	 */
	public void getXDSLInfo(UserIndex user, GetXDSLInfoResult res)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("getXDSLInfo[{}]==>方法开始UserIndex user[{}]", LSHNo,
				user);
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			res = new GetXDSLInfoService("getXDSLInfo").work(intRequest.asXML());
			logger.warn("getXDSLInfo[{}]==>方法结束,返回{}", LSHNo, res);
		}
		catch (Exception e)
		{
			logger.error("getXDSLInfo[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 该接口重新注册语音业务
	 * 
	 * @param user
	 * @return int
	 */
	public void setCpeServiceState(UserIndex user, String serviceType,
			String serviceState, IntHolder setCpeServiceStateReturn)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn(
				"resetService[{}]==>方法开始UserIndex user[{}],serviceType:{},serviceState:{}",
				LSHNo, user, serviceType, serviceState);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("iParaType")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("Value").addText(user.getIndex());
			intRequest.addElement("serviceType").addText(serviceType);
			intRequest.addElement("serviceState").addText(serviceState);
			setCpeServiceStateReturn.value = new setCpeServiceStateService(
					"setCpeServiceState").work(intRequest.asXML());
			logger.warn("setCpeServiceState[{}]==>方法结束,返回{}", LSHNo,
					setCpeServiceStateReturn);
		}
		catch (Exception e)
		{
			logger.error("setCpeServiceState[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 通过该接口查询终端序列号是否在ITMS网管存在
	 * 
	 * @param user
	 * @param SnRst
	 */
	public void getSnRst(UserIndex user, IntHolder SnRst)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("getSnRst[{}]==>方法开始UserIndex user[{}]", LSHNo, user);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			SnRst.value = new GetSnRstService("getSnRst").work(intRequest.asXML());
			logger.warn("getSnRst[{}]==>方法结束,返回{}", LSHNo, SnRst);
		}
		catch (Exception e)
		{
			logger.error("getSnRst[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 通过该接口查询语音工单里的语音鉴权密码值
	 * 
	 * @param user
	 * @param VoipPWD
	 */
	public void getVoipPWD(UserIndex user, StringHolder VoipPWD)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("getVoipPWD[{}]==>方法开始UserIndex user[{}]", LSHNo, user);
		;
		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			VoipPWD.value = new GetVoipPWDService("getVoipPWD").work(intRequest.asXML());
			logger.warn("getVoipPWD[{}]==>方法结束,返回{}", LSHNo, VoipPWD);
		}
		catch (Exception e)
		{
			logger.error("getVoipPWD[{}] Exception occured!", LSHNo, e);
		}
	}

	/**
	 * 通过该接口采集终端流量节点值
	 * 
	 * @param user
	 * @param res
	 */
	public void getCpeFlow(UserIndex user, CpeFlowHolder cpeFlow)
	{
		String LSHNo = StringUtil.getStringValue(new DateTimeUtil().getLongTime())
				+ StringUtil.getStringValue((int) (Math.random() * 1000));
		logger.warn("getCpeFlow[{}]==>方法开始UserIndex user[{}]", LSHNo, user);

		try
		{
			Document inDocument = DocumentHelper.createDocument();
			Element intRequest = inDocument.addElement("request");
			intRequest.addElement("op_id").addText(LSHNo);
			intRequest.addElement("type")
					.addText(StringUtil.getStringValue(user.getType()));
			intRequest.addElement("index").addText(user.getIndex());
			cpeFlow.value = new GetCpeFlowService("getCpeFlow").work(intRequest.asXML());
			logger.warn("getCpeFlow[{}]==>方法结束,返回{}", LSHNo, cpeFlow.value);
		}
		catch (Exception e)
		{
			logger.error("getCpeFlow[{}] Exception occured!", LSHNo, e);
		}
	}
}
