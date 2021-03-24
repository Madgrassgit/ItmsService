package com.linkage.itms.dispatch.service;

import PreProcess.UserInfo;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.UpdateNetPwdChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 山东电信修改宽带密码接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2016年11月29日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class UpdateNetPwdService implements IService{

	private static Logger logger = LoggerFactory.getLogger(UpdateNetPwdService.class);

	//业务类型（宽带）
	private final String ServiceType = "10"; 

	@Override
	public String work(String inParam) {
		logger.warn("UpdateNetPwdService==>inParam({})",inParam);

		// 解析获得入参
		UpdateNetPwdChecker checker = new UpdateNetPwdChecker(inParam);

		// 验证入参
		if (false == checker.check()) {
			logger.warn("入参验证没通过,Loid=[{}],NetUserName=[{}],NetPwd=[{}]",
					new Object[] { checker.getLoid(), checker.getNetUserName(),checker.getNetPwd() });

			logger.warn("work==>inParam="+checker.getReturnXml());

			return checker.getReturnXml();
		}

		// 查询用户设备信息
		UserDeviceDAO userDevDao = new UserDeviceDAO();

		Map<String,String> userDevInfo = userDevDao.qryUserByNameAndLoid(checker.getLoid(), checker.getNetUserName());

		if (null == userDevInfo || userDevInfo.isEmpty()) {
			logger.warn(
					"servicename[UpdateNetPwdService]cmdId[{}]Loid=[{}],NetUserName=[{}]查无此用户",
					new Object[] { checker.getCmdId(), checker.getLoid(), checker.getNetUserName()});
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
		}else{
			String deviceId = userDevInfo.get("device_id");
			String user_id = userDevInfo.get("user_id");
			String oui = userDevInfo.get("oui");
			String devSN = userDevInfo.get("device_serialnumber");

			if (StringUtil.IsEmpty(deviceId) || StringUtil.IsEmpty(user_id) || StringUtil.IsEmpty(oui) || StringUtil.IsEmpty(devSN)) {
				// 未绑定设备
				logger.warn(
						"servicename[UpdateNetPwdService]cmdId[{}]netUserName[{}]netUserPwd[{}]此客户未绑定",
						new Object[] { checker.getCmdId(), checker.getNetUserName(),checker.getNetPwd()});
				checker.setResult(1004);
				checker.setResultDesc("此用户未绑定设备");
			}else{
				// 1.查询此用户开通的业务信息
				Map<String, String> userServMap = userDevDao.queryServForNet(user_id);
				if (null == userServMap || userServMap.isEmpty())
				{
					// 没有开通业务
					logger.warn(
							"servicename[updateNetPwd]cmdId[{}]userinfo[{}]此用户没有开通任何宽带业务",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1002);
					checker.setResultDesc("此用户没有开通任何宽带业务");
					return checker.getReturnXml();
				}


				// 校验设备是否在线
				GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
				ACSCorba acsCorba = new ACSCorba();

				int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
				// 设备正在被操作，不能获取节点值
				if (-3 == flag) {
					logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
					checker.setResult(1006);
					checker.setResultDesc("设备不能正常交互");
					logger.warn("return=({})", checker.getReturnXml());  // 打印回参
					return checker.getReturnXml();
				}
				// 设备在线
				else if (1 == flag) {
					logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);

					String pathJ = "1";
					String pathK = "1";

					String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
					String wanServiceList = ".X_CT-COM_ServiceList";
					String wanPPPConnection = ".WANPPPConnection.";
					String wanIPConnection = ".WANIPConnection.";
					String INTERNET = "INTERNET";

					ArrayList<String> wanConnPathsList = null;
					// 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
					wanConnPathsList = acsCorba.getParamNamesPath(deviceId, wanConnPath, 0);
					if (null == wanConnPathsList || 0 == wanConnPathsList.size()
							|| wanConnPathsList.isEmpty())
					{
						logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取",deviceId);
						wanConnPathsList = new ArrayList<String>();
						List<String> jList = acsCorba.getIList(deviceId, wanConnPath);
						if (null == jList || jList.size() == 0 || jList.isEmpty())
						{
							logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath + "下实例号失败，返回",
									deviceId);
							checker.setResult(1006);
							//							checker.setResultDesc("此路径下获取节点失败");
							checker.setResultDesc("设备不能正常交互");
							return checker.getReturnXml();
						}
						for (String j : jList)
						{
							// 获取session，
							List<String> kPPPList = acsCorba.getIList(deviceId, wanConnPath + j
									+ wanIPConnection);
							if (null == kPPPList || kPPPList.size() == 0 || kPPPList.isEmpty())
							{
								logger.warn("[QuerySheetDataService] [{}]获取" + wanConnPath
										+ wanConnPath + j + wanIPConnection + "下实例号失败", deviceId);
							}
							else
							{
								for (String kppp : kPPPList)
								{
									wanConnPathsList.add(wanConnPath + j + wanIPConnection + kppp
											+ wanServiceList);
								}
							}
						}
					}
					// serviceList节点
					ArrayList<String> serviceListList = new ArrayList<String>();
					// 所有需要采集的节点
					ArrayList<String> paramNameList = new ArrayList<String>();
					for (int i = 0; i < wanConnPathsList.size(); i++)
					{
						String namepath = wanConnPathsList.get(i);
						if (namepath.indexOf(wanServiceList) >= 0 && namepath.indexOf(wanPPPConnection)>=0)
						{
							serviceListList.add(namepath);
							paramNameList.add(namepath);
							continue;
						}
					}
					if (serviceListList.size() == 0 || serviceListList.isEmpty())
					{
						logger.warn("[QuerySheetDataService] [{}]不存在WANIP下的X_CT-COM_ServiceList节点，返回", deviceId);
						checker.setResult(1007);
						checker.setResultDesc("设备没有宽带vlan");
						return checker.getReturnXml();
					}

					String[] paramNameArr = new String[paramNameList.size()];
					int arri = 0;
					for (String paramName : paramNameList)
					{
						paramNameArr[arri] = paramName;
						arri = arri + 1;
					}
					Map<String, String> paramValueMap = new HashMap<String, String>();
					for (int k = 0; k < (paramNameArr.length / 20) + 1; k++)
					{
						String[] paramNametemp = new String[paramNameArr.length - (k * 20) > 20 ? 20
								: paramNameArr.length - (k * 20)];
						for (int m = 0; m < paramNametemp.length; m++)
						{
							paramNametemp[m] = paramNameArr[k * 20 + m];
						}
						Map<String, String> maptemp = acsCorba.getParaValueMap(deviceId,
								paramNametemp);
						if (maptemp != null && !maptemp.isEmpty())
						{
							paramValueMap.putAll(maptemp);
						}
					}
					if (paramValueMap.isEmpty())
					{
						logger.warn("[QuerySheetDataService] [{}]获取ServiceList失败", deviceId);
						checker.setResult(1007);
						checker.setResultDesc("获取ServiceList失败");
						return checker.getReturnXml();
					}
					for (Map.Entry<String, String> entry : paramValueMap.entrySet())
					{
						logger.debug("[{}]{}={} ", new Object[] { deviceId, entry.getKey(),
								entry.getValue() });
						String paramName = entry.getKey();
						String j = paramName.substring(wanConnPath.length(), paramName.indexOf(".",wanConnPath.length()));
						if (paramName.indexOf(wanPPPConnection) >= 0)
						{
						}
						else if (paramName.indexOf(wanIPConnection) >= 0)
						{
							continue;
						}
						if (paramName.indexOf(wanServiceList) >= 0)
						{
							String k = paramName.substring(paramName.indexOf(wanServiceList) - 1,
									paramName.indexOf(wanServiceList));
							if (!StringUtil.IsEmpty(entry.getValue())
									&& entry.getValue().indexOf(INTERNET) >= 0){//X_CT-COM_ServiceList的值为INTERNET的时候，此节点路径即为要删除的路径

								pathJ = j;
								pathK = k;
							}
						}
					}
					String servListPathJ = wanConnPath + pathJ + ".WANPPPConnection." + pathK;
					String connTypePath = servListPathJ + ".ConnectionType";
					String[] gatherPath = new String[]{connTypePath};

					ArrayList<ParameValueOBJ> objLlist = acsCorba.getValue(deviceId, gatherPath);

					if (null == objLlist || objLlist.isEmpty()) {
						// 采集 ConnectionType，WanType，上网方式：PPPoE_Bridged 等
						ArrayList<ParameValueOBJ> connTypeList = acsCorba.getValue(deviceId, connTypePath);
						if (null == connTypeList || connTypeList.isEmpty() || null==connTypeList.get(0) || null==connTypeList.get(0).getValue()) {
							checker.setWan_type("");
							logger.warn("[{}]采集ConnectionType失败或者值为空",	deviceId);
						}else{
							checker.setWan_type(connTypeList.get(0).getValue());
							logger.warn("[{}]采集ConnectionType成功，值为：[{}]",	deviceId,connTypeList.get(0).getValue());
						}
					}else{
						for(ParameValueOBJ pvobj : objLlist){
							if(pvobj.getName().contains("ConnectionType")){
								checker.setWan_type(pvobj.getValue());
							}
						}
					}
					if("IP_Routed".equals(checker.getWan_type())){
						//更改密码
						userDevDao.modCustomerPwd(user_id, checker.getNetUserName(), checker.getNetPwd());

						//业务下发
						boolean res = serviceDoner(deviceId, user_id, oui, devSN);
						if(!res){
							logger.warn(
									"servicename[UpdateNetPwdService]cmdId[{}]loid[{}]netUserName[{}]netPwd[{}]下发特定业务，调用后台预读模块失败，业务类型为：[{}]",
									new Object[] { checker.getCmdId(), checker.getNetUserName(), checker.getNetPwd() });
							checker.setResult(1000);
							checker.setResultDesc("下发业务失败，请稍后重试");
						}
					}else if("PPPoE_Bridged".equals(checker.getWan_type())){
						logger.warn(
								"servicename[UpdateNetPwdService]cmdId[{}]loid[{}]netUserName[{}]netPwd[{}]宽带业务类型不为路由，不支持更改密码",
								new Object[] { checker.getCmdId(), checker.getNetUserName(), checker.getNetPwd() });
						checker.setResult(1005);
						checker.setResultDesc("非路由接入方式");
					}
				}else {// 设备不在线，不能获取节点值
					logger.warn("设备不在线，无法获取节点值");
					checker.setResult(1006);
					checker.setResultDesc("设备不能正常交互");
					logger.warn("return=({})", checker.getReturnXml()); // 打印回参
					return checker.getReturnXml();
				}
			}
		}
		String returnXml = checker.getReturnXml();
		logger.warn(
				"servicename[UpdateNetPwdService]cmdId[{}]loid[{}]netUserName[{}]netPwd[{}]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), checker.getNetUserName(), checker.getNetPwd() });

		return returnXml;

	}

	/**
	 * 业务下发
	 * @param deviceId 设备编码
	 * @param user_id 用户ID
	 * @param oui 设备OUI
	 * @param devSN 设备SN
	 * @return 下发结果
	 */
	private boolean serviceDoner(String deviceId, String user_id, String oui,
			String devSN) {
		logger.warn("UpdateNetPwdService==>serviceDoner({})",new Object[]{deviceId,user_id,oui,devSN});
		boolean res = false;

		ServUserDAO servUserDao = new ServUserDAO();

		// 更新业务用户表的业务开通状态
		servUserDao.updateServOpenStatus(StringUtil.getLongValue(user_id),StringUtil.getIntegerValue(ServiceType));
		// 预读调用对象
		PreServInfoOBJ preInfoObj = new PreServInfoOBJ(user_id, deviceId, oui, devSN, ServiceType, "1");
		if (1 == CreateObjectFactory.createPreProcess().processServiceInterface(CreateObjectFactory.createPreProcess()
				.GetPPBindUserList(preInfoObj)))
		{
			res = true;
		}

		return res;
	}


	public UserInfo GetPPBindUserList(PreServInfoOBJ preInfoObj)
	{
		logger.debug("GetScheduleSQLList({})", preInfoObj);
		UserInfo uinfo = new UserInfo();
		uinfo.userId = StringUtil.getStringValue(preInfoObj.getUserId());
		uinfo.deviceId = StringUtil.getStringValue(preInfoObj.getDeviceId());
		uinfo.oui = StringUtil.getStringValue(preInfoObj.getOui());
		uinfo.deviceSn = StringUtil.getStringValue(preInfoObj.getDeviceSn());
		uinfo.gatherId = StringUtil.getStringValue(preInfoObj.getGatherId());
		uinfo.servTypeId = StringUtil.getStringValue(preInfoObj.getServTypeId());
		uinfo.operTypeId = StringUtil.getStringValue(preInfoObj.getOperTypeId());
		return uinfo;
	}


}
