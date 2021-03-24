
package com.linkage.itms.dispatch.service;

import ResourceBind.BindInfo;
import ResourceBind.ResultInfo;
import ResourceBind.UnBindInfo;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.itms.ResourceBindInterface;
import com.linkage.itms.commom.DateUtil;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.ZeroConfigReportDAO;
import com.linkage.itms.dispatch.obj.ZeroConfigReportChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2015年12月22日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ZeroConfigReportService4JL implements IService
{

	private static final Logger logger = LoggerFactory
			.getLogger(ZeroConfigReportService4JL.class);
	/**
	 * 业务类型：家庭网关e8-b
	 */
	private static final int SERVTYPE_ITMS_E8B = 1;
	/**
	 * 业务类型：家庭网关e8-c
	 */
	private static final int SERVTYPE_ITMS_E8C = 2;
	/**
	 * 业务类型：政企网关
	 */
	private static final int SERVTYPE_BBMS = 3;
	/**
	 * 业务类型：机顶盒
	 */
	private static final int SERVTYPE_STB = 4;
	/**
	 * 操作类型：1 绑定
	 */
	private static final int BIND_TYPE = 1;
	/**
	 * 操作类型：2 解绑
	 */
	private static final int UNBIND_TYPE = 2;
	
	/**
	 * 接口返回结果：失败
	 */
//	private static final int INTF_RS_FAIL = -1;
	
	/**
	 * 接口返回结果：成功
	 */
//	private static final int INTF_RS_OK = 1;

	@Override
	public String work(String inXml)
	{
		logger.warn("ZeroConfigReportService4JL,inParam:({})", inXml);
		ZeroConfigReportChecker checker = new ZeroConfigReportChecker(inXml);
		// 验证入参格式是否正确
		if (false == checker.check())
		{
			logger.error(
					"servicename[ZeroConfigReportService4JL],cmdId[{}],验证未通过,返回：{}",
					new Object[] { checker.getCmdId(), checker.getReturnXml() });
			logger.warn(
					"ZeroConfigReportService4JL,cmdId[{}]验证未通过,返回：{}",
					new Object[] { checker.getCmdId(), checker.getReturnXml() });
			return checker.getReturnXml();
		}
		
		
		ZeroConfigReportDAO zeroCfgDao = new ZeroConfigReportDAO();
		long updateTime = DateUtil.transTime(checker.getSendTime(), "yyyyMMddHHmmss");
		checker.setUpdateTime(updateTime);
		//零配置参数信息入库
//		zeroCfgDao.saveZeroConfigReport(checker);
		// 查询用户信息
		Map<String, String> userInfoMap = zeroCfgDao.getUserInfo(checker.getServiceType(),
				checker.getUserInfo());
		// 用户不存在
		if (null == userInfoMap || userInfoMap.isEmpty())
		{
			logger.warn("servicename[ZeroConfigReportService4JL]cmdId[{}]userinfo[{}]失败,暂未查出此用户",
					new Object[] { checker.getCmdId(), checker.getUserInfo() });
			checker.setResult(-1);
			checker.setResultDesc("失败,暂未查出此用户");
			//更新报表接口返回值
//			zeroCfgDao.updateZeroConfigReport(INTF_RS_OK,checker.getCmdId());
		}
		// 用户信息存在，还需要查询设备信息是否存在
		else
		{
			if (checker.getOperateType() == BIND_TYPE)
			{
				zeroCfgDao.updateCpeMac(checker.getDevSn(), checker.getUserInfo());
			}
			
			DeviceInfoDAO devDao = new DeviceInfoDAO();
			Map<String, String> devMap = null;
			if (SERVTYPE_STB == checker.getServiceType() && UNBIND_TYPE == checker.getOperateType())
			{
				devMap = devDao.queryDevInfo4JL(StringUtil.getLongValue(userInfoMap, "customer_id", 0));
			}else
			{
				devMap = devDao.queryDevInfo(checker.getServiceType(), 
						checker.getDevSn());
			}
			
			if (null == devMap || devMap.isEmpty()
					|| StringUtil.IsEmpty(devMap.get("device_id")))
			{
				logger.warn(
						"servicename[ZeroConfigReportService4JL]cmdId[{}]deviceSn[{}]成功,暂未查出此设备",
						new Object[] { checker.getCmdId(), checker.getDevSn() });
				checker.setResult(0);
				checker.setResultDesc("成功");
				//更新报表接口返回值
//				zeroCfgDao.updateZeroConfigReport(INTF_RS_OK,checker.getCmdId());
			}
			// 用户信息存在，设备存在，开始解绑或绑定
			else
			{
				
				ResultInfo rs = null;
				if (checker.getOperateType() == BIND_TYPE)
				{
					ResourceBindInterface corba = this.getCorba(checker.getServiceType());
					BindInfo[] bindInfo = getBindInfo(checker.getServiceType(),
							checker.getUserInfo(), userInfoMap, devMap);
					if(corba != null){
						rs = corba.bind(bindInfo);
					}
				}
				else if (checker.getOperateType() == UNBIND_TYPE)
				{
					ResourceBindInterface corba = this.getCorba(checker.getServiceType());
					UnBindInfo[] unBindInfo = getUnBindInfo(checker.getServiceType(),
							checker.getUserInfo(), userInfoMap, devMap);
					if(corba != null){
						rs = corba.release4JL(unBindInfo, checker.getServiceType());
					}
				}
				
				
				// 结果值为空或不等于1，则失败
				if (null == rs || (!rs.resultId[0].equals("1")))
				{
					checker.setResult(1000);
					checker.setResultDesc("失败，系统内部错误");
					//更新报表接口返回值
//					zeroCfgDao.updateZeroConfigReport(INTF_RS_FAIL,checker.getCmdId());
				}
				else
				{
					checker.setResult(0);
					checker.setResultDesc("成功");
					//更新报表接口返回值
//					zeroCfgDao.updateZeroConfigReport(INTF_RS_OK,checker.getCmdId());
				}
			}
		}
		String returnXml = checker.getReturnXml();
		logger.warn("ZeroConfigReportService4JL, returnXML:{}",returnXml);
		return returnXml;
	}

	/**
	 * 根据业务类型获取不同corba
	 * 
	 * @param serviceType
	 * @return
	 */
	private ResourceBindInterface getCorba(int serviceType)
	{
		ResourceBindInterface corba = null;
		if ((serviceType == SERVTYPE_ITMS_E8B) || (serviceType == SERVTYPE_ITMS_E8C))
		{
			corba = CreateObjectFactory.createResourceBind(Global.GW_TYPE_ITMS);
		}
		else if (serviceType == SERVTYPE_BBMS)
		{
			corba = CreateObjectFactory.createResourceBind(Global.GW_TYPE_BBMS);
		}
		else if (serviceType == SERVTYPE_STB)
		{
			corba = CreateObjectFactory.createResourceBind(Global.GW_TYPE_STB);
		}
		return corba;
	}

	/**
	 * 获取绑定信息对象
	 * 
	 * @param serviceType
	 * @param userInfo
	 * @param userInfoMap
	 * @param devMap
	 * @return
	 */
	private BindInfo[] getBindInfo(int serviceType, String userInfo,
			Map<String, String> userInfoMap, Map<String, String> devMap)
	{
		BindInfo[] arr = new BindInfo[1];
		arr[0] = new BindInfo();
		if (serviceType == SERVTYPE_STB)
		{
			// 机顶盒业务:取的 stb_tab_customer表里的 customer_id 字段
			arr[0].accOid = StringUtil.getStringValue(userInfoMap, "customer_id");
		}
		else
		{
			// 家庭网关、政企网关业务： 取的用户表的 user_id 字段
			arr[0].accOid = StringUtil.getStringValue(userInfoMap, "user_id");
		}
		arr[0].accName = "admin";
		// loid或业务账号，是接口传过来的userinfo值
		arr[0].username = userInfo;
		// 设备表的设备id
		arr[0].deviceId = StringUtil.getStringValue(devMap, "device_id");
		// 1 手工绑定，2 自动绑定。
		arr[0].userline = 2;
		return arr;
	}

	/**
	 * 获取解绑信息对象
	 * 
	 * @param serviceType
	 * @param userInfo
	 * @param userInfoMap
	 * @param devMap
	 * @return
	 */
	private UnBindInfo[] getUnBindInfo(int serviceType, String userInfo,
			Map<String, String> userInfoMap, Map<String, String> devMap)
	{
		UnBindInfo[] arr = new UnBindInfo[1];
		arr[0] = new UnBindInfo();
		if (serviceType == SERVTYPE_STB)
		{
			// 机顶盒业务:取的 stb_tab_customer表里的 customer_id 字段
			arr[0].accOid = StringUtil.getStringValue(userInfoMap, "customer_id");
			arr[0].userId = StringUtil.getStringValue(userInfoMap, "customer_id");
		}
		else
		{
			// 家庭网关、政企网关业务： 取的用户表的 user_id 字段
			arr[0].accOid = StringUtil.getStringValue(userInfoMap, "user_id");
			arr[0].userId = StringUtil.getStringValue(userInfoMap, "user_id");
		}
		arr[0].accName = "admin";
		arr[0].deviceId = StringUtil.getStringValue(devMap, "device_id");
		// 1 手工绑定，2 自动绑定。
		arr[0].userline = 2;
		return arr;
	}

}
