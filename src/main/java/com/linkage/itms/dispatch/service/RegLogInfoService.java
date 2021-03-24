
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.RegLogInfoChecker;

/**
 * @author yinlei3 (Ailk No.73167)
 * @version 1.0
 * @since 2016年5月26日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RegLogInfoService implements IService
{

	/** 日志对象 */
	private static final Logger logger = LoggerFactory.getLogger(RegLogInfoService.class);
	private UserDeviceDAO userDevDao = new UserDeviceDAO();

	@Override
	public String work(String inXml)
	{
		logger.warn("RegLogInfoService inXml ({})", inXml);
		RegLogInfoChecker checker = new RegLogInfoChecker(inXml);
		if (false == checker.check())
		{
			logger.error(
					"servicename[RegLogInfoService]cmdId[{}]UserInfo[{}]验证未通过.返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn("servicename[RegLogInfoService]cmdId[{}]UserInfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
		String userId = "";
		String device_id = "";
		String devSn = "";
		boolean checkServ = false;
		// 设备序列号（支持后6位）
		if (checker.getUserInfoType() == 1)
		{
			// 根据终端序列号查询设备信息表
			ArrayList<HashMap<String, String>> devlist = userDevDao.queryDevInfo2(checker
					.getUserInfo());
			if (null == devlist || devlist.isEmpty())
			{
				// 根据终端序列号查询设备预置表
				devlist = userDevDao.queryDevInfoInit(checker.getUserInfo());
				if (null == devlist || devlist.isEmpty())
				{
					logger.warn(
							"servicename[RegLogInfoService]cmdId[{}]UserInfo[{}]查无此设备",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),
									inXml });
					checker.setResult(1003);
					checker.setResultDesc("查无此设备");
					checker.setDevSn("");
					checker.setRegRst("查无此设备");
					checker.setProOpin("设备不在串码管控库中,请先在CPMIS系统中录入设备");
				}
				else if (devlist.size() > 1)
				{
					logger.warn(
							"servicename[RegLogInfoService]cmdId[{}]UserInfo[{}]查询到多台设备",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),
									inXml });
					checker.setResult(1006);
					checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
					checker.setDevSn("");
					checker.setRegRst("查到多台设备");
					checker.setProOpin("请输入更多位序列号或完整序列号进行查询");
				}
				else
				{
					logger.warn(
							"servicename[RegLogInfoService]cmdId[{}]UserInfo[{}]设备信息未上报",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),
									inXml });
					checker.setResult(0);
					checker.setResultDesc("成功");
					checker.setDevSn(StringUtil.getStringValue(devlist.get(0),
							"device_serialnumber", ""));
					checker.setRegRst("注册认证失败");
					checker.setProOpin("设备信息未上报成功，请确保设备是否获取IP地址及网络通畅");
				}
			}
			else if (devlist.size() > 1)
			{
				logger.warn("servicename[RegLogInfoService]cmdId[{}]UserInfo[{}]查询到多台设备",
						new Object[] { checker.getCmdId(), checker.getUserInfo(), inXml });
				checker.setResult(1006);
				checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
				checker.setDevSn("");
				checker.setRegRst("查到多台设备");
				checker.setProOpin("请输入更多位序列号或完整序列号进行查询");
			}
			else
			{
				// 查看是否绑定设备
				userId = StringUtil.getStringValue(devlist.get(0), "user_id", "");
				if (StringUtil.IsEmpty(userId))
				{
					logger.warn(
							"serviceName[RegLogInfoService]cmdId[{}]userinfo[{}]未绑定用户",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1004);
					checker.setResultDesc("未绑定用户");
					checker.setDevSn(StringUtil.getStringValue(devlist.get(0),
							"device_serialnumber", ""));
					checker.setRegRst("注册绑定失败");
					checker.setProOpin("未绑定loid，建议重新注册");
				}
				else
				{
					device_id = StringUtil.getStringValue(devlist.get(0), "device_id");
					checkServ = true;
					devSn = StringUtil.getStringValue(devlist.get(0),
							"device_serialnumber");
				}
			}
		}
		else
		{
			// 根据参数查询数据库是否有此设备的信息,根据参数判断哪种查询用户信息
			Map<String, String> userInfoMap = userDevDao
					.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo(),
							checker.getCityId());
			if (null == userInfoMap || userInfoMap.isEmpty())
			{
				logger.warn("serviceName[RegLogInfoService]cmdId[{}]userinfo[{}]无此用户",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1002);
				checker.setResultDesc("查无此用户");
				checker.setDevSn("");
				checker.setRegRst("查无此用户");
				checker.setProOpin("请确认是否存在此用户");
			}
			else
			{
				device_id = userInfoMap.get("device_id");
				userId = userInfoMap.get("user_id");
				devSn = userInfoMap.get("device_serialnumber");
				if (StringUtil.IsEmpty(device_id))
				{
					logger.warn(
							"serviceName[RegLogInfoService]cmdId[{}]userinfo[{}]未绑定设备",
							new Object[] { checker.getCmdId(), checker.getUserInfo() });
					checker.setResult(1004);
					checker.setResultDesc("未绑定设备");
					checker.setDevSn(devSn);
					checker.setRegRst("注册绑定失败");
					checker.setProOpin("此loid未绑定设备，请使用设备序列号查询");
				}
				else
				{
					checkServ = true;
				}
			}
		}
		if (checkServ)
		{
			ArrayList<HashMap<String, String>> servList = userDevDao
					.queryServResult(userId);
			if (null == servList || servList.isEmpty())
			{
				logger.warn("serviceName[RegLogInfoService]cmdId[{}]userinfo[{}]无业务",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(1004);
				checker.setResultDesc("无业务下发");
				checker.setDevSn(devSn);
				checker.setRegRst("无业务下发");
				checker.setProOpin("无业务下发,请确认是否有相关业务");
			}
			else
			{
				String serv_type_id = "";
				String open_status = "";
				String result = "";
				String username = "";
				boolean fail = false;
				boolean undo = false;
				Map<String, String> map = null;
				for (HashMap<String, String> serv : servList)
				{
					serv_type_id = serv.get("serv_type_id");
					open_status = serv.get("open_status");
					username = serv.get("username");
					// 宽带
					if ("10".equals(serv_type_id))
					{
						if ("0".equals(open_status))
						{
							undo = true;
							// 查询策略状态
							map = userDevDao.queryStrategyResult(device_id, username,
									"1001");
							result = result + "宽带账号:" + username + " 业务未做 "
									+ Global.G_Fault_Map.get(
											StringUtil.getIntegerValue(map
													.get("result_id"))).getSolutions()
									+ "|";
						}
						else if ("1".equals(open_status))
						{
							result = result + "宽带账号:" + username + " 已开通成功|";
						}
						else
						{
							fail = true;
							// 查询策略状态
							map = userDevDao.queryStrategyResult(device_id, username,
									"1001");
							result = result
									+ "宽带账号:"
									+ username
									+ " 失败 "
									+ Global.G_Fault_Map.get(
											StringUtil.getIntegerValue(map
													.get("result_id"))).getSolutions()
									+ "|";
						}
						// iptv
					}
					else if ("11".equals(serv_type_id))
					{
						if ("0".equals(open_status))
						{
							undo = true;
							// 查询策略状态
							map = userDevDao.queryStrategyResult(device_id, username,
									"1101");
							result = result + "iptv账号:" + username + " 业务未做 "
									+ Global.G_Fault_Map.get(
											StringUtil.getIntegerValue(map
													.get("result_id"))).getSolutions()
									+ "|";
						}
						else if ("1".equals(open_status))
						{
							result = result + "iptv账号:" + username + " 已开通成功|";
						}
						else
						{
							fail = true;
							// 查询策略状态
							map = userDevDao.queryStrategyResult(device_id, username,
									"1101");
							result = result
									+ "iptv账号:"
									+ username
									+ " 失败 "
									+ Global.G_Fault_Map.get(
											StringUtil.getIntegerValue(map
													.get("result_id"))).getSolutions()
									+ "|";
						}
						// 语音
					}
					else if ("14".equals(serv_type_id))
					{
						if ("0".equals(open_status))
						{
							undo = true;
							// 查询策略状态
							map = userDevDao.queryStrategyResult(device_id, username,
									"1401");
							result = result + "语音账号:" + username + " 业务未做 "
									+ Global.G_Fault_Map.get(
											StringUtil.getIntegerValue(map
													.get("result_id"))).getSolutions()
									+ "|";
						}
						else if ("1".equals(open_status))
						{
							result = result + "语音账号:" + username + " 已开通成功|";
						}
						else
						{
							fail = true;
							// 查询策略状态
							map = userDevDao.queryStrategyResult(device_id, username,
									"1401");
							result = result
									+ "语音账号:"
									+ username
									+ " 失败 "
									+ Global.G_Fault_Map.get(
											StringUtil.getIntegerValue(map
													.get("result_id"))).getSolutions()
									+ "|";
						}
					}
				}
				logger.warn("serviceName[RegLogInfoService]cmdId[{}]userinfo[{}]成功",
						new Object[] { checker.getCmdId(), checker.getUserInfo() });
				checker.setResult(0);
				checker.setResultDesc("成功");
				checker.setDevSn(devSn);
				if (fail == true)
				{
					checker.setRegRst("注册失败");
				}
				else if (undo == true)
				{
					checker.setRegRst("存在暂未下发的业务");
				}
				else
				{
					checker.setRegRst("注册成功");
				}
				checker.setProOpin(result.substring(0, result.length() - 1).replace("\n", ""));
			}
		}
		return checker.getReturnXml();
	}
}