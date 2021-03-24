
package com.linkage.stbms.pic.bio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DateTimeUtil;
import com.linkage.commons.util.StringUtil;
import com.linkage.commons.util.TimeUtil;
import com.linkage.stbms.ids.obj.SysConstant;
import com.linkage.stbms.pic.Global;
import com.linkage.stbms.pic.dao.ZeroConfDao;
import com.linkage.stbms.pic.object.StbDeviceOBJ;
import com.linkage.stbms.pic.object.StbZeroconfigFailObj;
import com.linkage.stbms.pic.object.StrategyObj;
import com.linkage.stbms.pic.util.AbcUtil;
import com.linkage.stbms.pic.util.StrUtil;
import com.linkage.stbms.pic.util.corba.RPCManagerClient;

/**
 * @author 李玉林(71496) Tel:
 * @version 1.0
 * @since Sep 16, 2015 1:15:29 PM
 * @category com.linkage.stbms.pic.bio
 * @copyright 
 */
public class ZeroConfBio
{

	private static final Logger logger = LoggerFactory.getLogger(ZeroConfBio.class);
	private ZeroConfDao dao = new ZeroConfDao();
	private RPCManagerClient acsCorba = new RPCManagerClient();  
	
	
	@SuppressWarnings("unused")
	public ArrayList<HashMap<String,String>> getUserByCustAccount(String custAccount){
		logger.debug("getUserByCustAccount({})",custAccount);
		ArrayList<HashMap<String,String>> userList = dao.getUserBynetName(custAccount);
		
		// 假如根据宽带账号查询为空，再根据业务账号查询
		if(null == userList || userList.isEmpty()){
			userList = dao.getUserByservAccount(custAccount);
		}
		return userList;
		
	}
	
	/**
	 * 获取StbDeviceOBJ
	 * 
	 * @param param
	 * @param paramType
	 *            1:deviceId 2:userId
	 * @return
	 */
	public StbDeviceOBJ getStbObj(String param, int paramType)
	{
		logger.debug("getStbObj({},{})", new Object[] { param, paramType });
		Map<String, String> devMap = dao.queryDeviceInfo(param, paramType);
		if (null == devMap || devMap.isEmpty())
		{
			return null;
		}
		StbDeviceOBJ stbDeviceOBJ = new StbDeviceOBJ();
		stbDeviceOBJ.setDeviceId(StringUtil.getStringValue(devMap, "device_id"));
		stbDeviceOBJ.setCityId(StringUtil.getStringValue(devMap, "city_id"));
		stbDeviceOBJ.setOui(StringUtil.getStringValue(devMap, "oui"));
		stbDeviceOBJ.setSn(StringUtil.getStringValue(devMap, "device_serialnumber"));
		stbDeviceOBJ.setStatus(StringUtil.getIntValue(devMap, "status"));
		stbDeviceOBJ.setZeroCount(StringUtil.getStringValue(devMap, "zero_account"));
		stbDeviceOBJ.setIsZeroCfgVersion(StringUtil
				.getIntValue(devMap, "is_zero_version"));
		stbDeviceOBJ.setVendorId(StringUtil.getStringValue(devMap, "vendor_id"));
		stbDeviceOBJ.setDeviceModelId(StringUtil
				.getStringValue(devMap, "device_model_id"));
		stbDeviceOBJ.setStbMac(StringUtil.getStringValue(devMap, "cpe_mac"));
		stbDeviceOBJ.setDeviceTypeId(StringUtil.getLongValue(devMap, "devicetype_id"));
		stbDeviceOBJ.setUserId(StringUtil.getLongValue(devMap, "customer_id"));
		stbDeviceOBJ.setIpAddress(StringUtil.getStringValue(devMap, "loopback_ip"));
		stbDeviceOBJ.setServAccount(StringUtil.getStringValue(devMap, "serv_account"));
		stbDeviceOBJ.setBindWay(StringUtil.getIntValue(devMap, "bind_way"));
		stbDeviceOBJ.setBindState(StringUtil.getIntValue(devMap, "bind_state"));
		stbDeviceOBJ.setCompletetime(StringUtil.getLongValue(devMap, "complete_time"));
		String deviceSerialnumber = StringUtil.getStringValue(devMap, "device_serialnumber");
		stbDeviceOBJ.setDeviceSerialnumber(deviceSerialnumber);
		stbDeviceOBJ.setStbZeroconfigFailObj(getStbZeroconfigFailObj(deviceSerialnumber));
		return stbDeviceOBJ;
	}

	private StbZeroconfigFailObj getStbZeroconfigFailObj(String deviceSerialnumber){
		StbZeroconfigFailObj stbZeroconfigFailObj = new StbZeroconfigFailObj();
		stbZeroconfigFailObj.setStartTime(System.currentTimeMillis() / 1000);
		if(!StringUtil.IsEmpty(deviceSerialnumber)&&deviceSerialnumber.length()>=12){
			stbZeroconfigFailObj.setBussId(deviceSerialnumber.substring(deviceSerialnumber.length()-12)
					+ System.currentTimeMillis());
		}
		return stbZeroconfigFailObj;
	}
	/**
	 * 绑定和下发
	 * 
	 * @param userList
	 * @param cfgType
	 *            1:自动配置 2：串号配置
	 * @return
	 */
	public boolean bindProcess(List<HashMap<String, String>> userList,
			StbDeviceOBJ stbDeviceOBJ, int cfgType)
	{
		Map<String, String> _newUserMap = userList.get(0);
		StbZeroconfigFailObj stbZeroconfigFailObj = stbDeviceOBJ.getStbZeroconfigFailObj();
		
		// 如果用户状态为销户，则不开通
		if ("3".equals(_newUserMap.get("user_status")))
		{
			logger.warn("[zerocfg-{}] 机顶盒用户[{}]状态为销户，不予开通",
					stbDeviceOBJ.getDeviceId(), _newUserMap.get("serv_account"));
			String returnValue = "机顶盒用户[" + _newUserMap.get("serv_account") + "]状态为销户";
			stbZeroconfigFailObj.setFailReasonId(3);
			stbZeroconfigFailObj.setReturnValue(returnValue);
			zeroCfgFailed(StringUtil.getLongValue(_newUserMap, "customer_id"), stbDeviceOBJ,"3");
			return false;
		}
		logger.warn("[zerocfg-{}] 机顶盒用户[{}]绑定", new Object[] { stbDeviceOBJ.getDeviceId(),
				_newUserMap.get("serv_account") });
		ArrayList<String> sqlList = new ArrayList<String>();
		
		// 当前时间
		stbDeviceOBJ.setCurrTime(System.currentTimeMillis() / 1000);
				
		// 更新设备表、零配置工单表
		sqlList.add(dao.updateDeviceByBindSucess(_newUserMap.get("customer_id"),
				_newUserMap.get("city_id"), _newUserMap.get("serv_account"),SysConstant.ZERO_CFG_AUTOING,stbDeviceOBJ));
		sqlList.add(dao.updateZeroCfgSheetByBind(stbDeviceOBJ.getOui(),
				stbDeviceOBJ.getSn(), _newUserMap.get("city_id"), _newUserMap
						.get("serv_account"),
				cfgType == 1 ? SysConstant.ZERO_CFG_SHEET_DOING
						: SysConstant.ZERO_CFG_SHEET_ACCT_DOING, 2 == stbDeviceOBJ
						.getStatus() ? 3 : 0));
		sqlList.add(dao.updateUserStatusSql(_newUserMap.get("customer_id"),1));
		sqlList.add(dao.logZeroCfg(StringUtil.getLongValue(_newUserMap, "customer_id"),stbDeviceOBJ));
		// 绑定的sql执行
		int _rs = DBOperation.executeUpdate(sqlList);
		sqlList.clear();
		if (_rs != 1)
		{
			logger.warn("[zerocfg-{}] 执行绑定sql失败",
					new Object[] { stbDeviceOBJ.getDeviceId() });
			return false;
		}
		
		return true;
	}
	
	/**
	 * 获取机顶盒下发rpc命令数组
	 * 
	 * @param customer_id,device_id
	 * @param 
	 * @return
	 */
	public DevRpc[] getDevRpc(String customer_id,String device_id) {
		logger.warn("getDevPpc({},{})",customer_id,device_id);
		Map<String, String> userMap = dao.getUserInfo(customer_id);
		if (userMap == null)
		{
			logger.warn("getDevRpc:deviceId=[{}]userId=[{}]机顶盒零配置用户不存在", device_id, customer_id);
			return null;
		}
		ArrayList<String> paramValuelist = new ArrayList<String>();
		ArrayList<String> paramNamelist = new ArrayList<String>();
		
		paramValuelist.add(StringUtil.getStringValue(userMap, "pppoe_user",""));
		paramNamelist.add("Device.X_CTC_IPTV.ServiceInfo.PPPoEID");
		
		paramValuelist.add(StringUtil.getStringValue(userMap, "pppoe_pwd",""));
		paramNamelist.add("Device.X_CTC_IPTV.ServiceInfo.PPPoEPassword");
		
		paramValuelist.add(StringUtil.getStringValue(userMap, "serv_account",""));
		paramNamelist.add("Device.X_CTC_IPTV.ServiceInfo.UserID");
		
		paramValuelist.add(StringUtil.getStringValue(userMap, "serv_pwd",""));
		paramNamelist.add("Device.X_CTC_IPTV.ServiceInfo.UserPassword");
		if(paramNamelist.size()>0){
			String[] paramNames = new String[paramNamelist.size()];
			String[] paramValues = new String[paramNamelist.size()];
			String[] paramTypeIds = new String[paramNamelist.size()];
			
			for (int i = 0; i < paramNamelist.size(); i++)
			{
				paramNames[i] = paramNamelist.get(i);
				paramValues[i] = paramValuelist.get(i);
				paramTypeIds[i] = "1";
			}
			
			return acsCorba.realSetParamsArrInt(paramNames, paramValues, paramTypeIds, device_id);
		}
		return null;
	}

	/**
	 * STB软件升级XML
	 * 
	 * @author gongsj
	 * @date 2010-11-8
	 * @param taskInfoMap
	 * @return
	 */
	private String logo2Xml(Map<String, String> taskInfoMap)
	{
		logger.debug("logo2Xml...");
		String strXml = null;
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("STB");
		
		Element StartPicURL = root.addElement("StartPicURL");
		Element BootPicURL = root.addElement("BootPicURL");
		Element AuthenticatePicURL = root.addElement("AuthenticatePicURL");
		
		if(!StrUtil.IsEmpty(taskInfoMap.get("sd_qd_pic_url"))){
			StartPicURL.addAttribute("flag", "1").addText(taskInfoMap.get("sd_qd_pic_url"));
		}else{
			StartPicURL.addAttribute("flag", "0");
		}
		
		if(!StrUtil.IsEmpty(taskInfoMap.get("sd_kj_pic_url"))){
			BootPicURL.addAttribute("flag", "1").addText(taskInfoMap.get("sd_kj_pic_url"));
		}else{
			BootPicURL.addAttribute("flag", "0");
		}
		
		if(!StrUtil.IsEmpty(taskInfoMap.get("sd_rz_pic_url"))){
			AuthenticatePicURL.addAttribute("flag", "1").addText(taskInfoMap.get("sd_rz_pic_url"));	
		}else{
			AuthenticatePicURL.addAttribute("flag", "0");	
		}
		
		
		
		strXml = doc.asXML();
		return strXml;
	}

	/**
	 * 获得策略对象
	 * 
	 * @author gongsj
	 * @date 2010-11-8
	 * @param deviceId
	 * @param sheetPara
	 * @return
	 */
	private StrategyObj getStrategyObj(String deviceId, String taskId, String sheetPara)
	{
		StrategyObj strategyOBJ = new StrategyObj();
		strategyOBJ.setServiceId(StrUtil.getIntegerValue(Global.SERVICE_ID_STB_LOGO_CON));
		strategyOBJ.createId();
		strategyOBJ.setDeviceId(deviceId);
		strategyOBJ.setTime(TimeUtil.getCurrentTime());
		strategyOBJ.setSheetPara(sheetPara);
		strategyOBJ.setAccOid(1);
		strategyOBJ.setOrderId(1);
		strategyOBJ.setIsLastOne(1);
		strategyOBJ.setPriority(1);
		strategyOBJ.setType(4);
		// 是新类型的策略，策略参数为XML，组装模板
		strategyOBJ.setSheetType(2);
		strategyOBJ.setRedo(0);
		strategyOBJ.setTempId(StrUtil.getIntegerValue(Global.SERVICE_ID_STB_LOGO_CON));
		strategyOBJ.setTaskId(taskId);
		return strategyOBJ;
	}
	
	public void zeroCfgFailed(long reqUserId, StbDeviceOBJ stbDeviceOBJ,String userStatus)
	{	
		stbDeviceOBJ.setCurrTime(System.currentTimeMillis() / 1000);
		dao.updateUserStatusSql(reqUserId, -1);
		ArrayList<String> sqlList = new ArrayList<String>();
		sqlList.add(dao.logZeroCfg(reqUserId,stbDeviceOBJ));
		if(!"3".equals(userStatus)){
			sqlList.add(dao.updateDeviceByBindFail(stbDeviceOBJ));
		}		
		DBOperation.executeUpdate(sqlList);
	}
	
	/**
	 * 江西零配置结果入库
	 * @author chenxj6
	 * @date 2017-05-09
	 * @param stbDeviceOBJ, servAccount, zeroResult
	 * @return
	 */
	public void saveZeroConfgRes(StbDeviceOBJ stbDeviceOBJ, String servAccount, int zeroResult) {
		try {
			if (1 == stbDeviceOBJ.getStbIsNew()	&& !StringUtil.IsEmpty(servAccount)) {
				Map<String, String> zeroConfgMap = getZeroConfgResByServAcc(servAccount);
				if (null == zeroConfgMap || zeroConfgMap.isEmpty()) {
					recordZeroConfg(stbDeviceOBJ, servAccount, zeroResult, stbDeviceOBJ.getCityId());
				} else {
					if (Global.ZERO_CONFIG_RESULT_SUCCESS == zeroResult) {
						// 如果存在，则判断当前时间和上次记录时间，是否在5分钟之内，并且上次结果为失败，更新为成功（时间也更新），否则不更新
						long oldTime = StringUtil.getLongValue(zeroConfgMap.get("zero_time"));
						long nowTime = System.currentTimeMillis() / 1000;
						if ((nowTime - oldTime) < (Global.ZERO_CONFIG_RESULT_UPDATETIME) && 
								Global.ZERO_CONFIG_RESULT_FAILED == StringUtil.getIntegerValue(zeroConfgMap.get("zero_result"))) {
							updateZeroConfg(zeroResult, servAccount,stbDeviceOBJ);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("[{}]零配置结果入库操作发生异常：e[{}]", servAccount, e);
		}
	}
	
	/**
	 * 江西记录零配置结果,是否存在记录
	 * @author chenxj6
	 * @date 2017-05-09
	 * @param servAccount
	 * @return
	 */
	public Map<String,String> getZeroConfgResByServAcc(String servAccount){
		PrepareSQL pSql = new PrepareSQL(
				"select device_id,username,zero_result,zero_time from stb_zero_config_record where username=? ");
		int index = 0;
		pSql.setString(++index, servAccount);
		
		return DBOperation.getRecord(pSql.getSQL());
	}
	
	/**
	 * 江西记录零配置结果
	 * @author chenxj6
	 * @date 2017-05-09
	 * @param deviceId,userName,zeroResult
	 * @return
	 */
	public void recordZeroConfg(StbDeviceOBJ stbDeviceOBJ,String userName,int zeroResult,String cityId){
		PrepareSQL pSql = new PrepareSQL(
				"insert into stb_zero_config_record(device_id,username,zero_result,zero_time,city_id,bind_way,return_value,serv_result, " +
				" is_dhcp) values (?,?,?,?,?, ?,?,?,?)");
		int index = 0;
		pSql.setString(++index, stbDeviceOBJ.getDeviceId());
		pSql.setString(++index, userName);
		pSql.setInt(++index, zeroResult);
		pSql.setLong(++index, System.currentTimeMillis() / 1000);
		pSql.setString(++index, cityId);
		pSql.setInt(++index, stbDeviceOBJ.getBindWay());
		pSql.setString(++index, stbDeviceOBJ.getStbZeroconfigFailObj().getReturnValue());
		pSql.setInt(++index, zeroResult);
		pSql.setInt(++index, Global.IS_DHCP);
		
		DBOperation.executeUpdate(pSql.getSQL());
	}
	
	/**
	 * 江西更新零配置结果
	 * @author chenxj6
	 * @date 2017-05-09
	 * @param deviceId,userName,zeroResult
	 * @return
	 */
	public void updateZeroConfg(int zeroResult, String userName, StbDeviceOBJ stbDeviceOBJ){
		PrepareSQL pSql = new PrepareSQL(
				"update stb_zero_config_record set zero_result=?, zero_time=?, bind_way=?, return_value=?, serv_result=?, is_dhcp=? " +
				" where username=? ");
		int index = 0;
		pSql.setInt(++index, zeroResult);
		pSql.setLong(++index, System.currentTimeMillis() / 1000);
		pSql.setInt(++index, stbDeviceOBJ.getBindWay());
		pSql.setString(++index, stbDeviceOBJ.getStbZeroconfigFailObj().getReturnValue());
		pSql.setInt(++index, zeroResult);
		pSql.setInt(++index, Global.IS_DHCP);
		pSql.setString(++index, userName);
		
		DBOperation.executeUpdate(pSql.getSQL());
	}
	
	/**
	 * modify date 2011-5-20
	 * 
	 * 插入绑定日志表，bind_log，自修改日起，所有绑定日志均操作bind_log
	 * 
	 */
	public int getSQLByAddBindlog(String username,String deviceId,
			int bindStatus,int bindResult,String bindDesc,int userline,
			String remark,int operType,int bindType,String dealstaff){
		logger.debug("UserInstReleaseDAO=>getSQLByAddBindlog()");
		
		PrepareSQL ppSQL = new PrepareSQL(" insert into stb_bind_log (bind_id,username," +
							"device_id,binddate,bind_status,bind_result," +
							"bind_desc,userline,remark,oper_type,bind_type," +
							"dealstaff) values (?,?,?,?,?,?,?,?,?,?,?,?)");
		ppSQL.setLong(1,AbcUtil.generateLongId());
		ppSQL.setString(2, username);
		ppSQL.setString(3, deviceId);
		ppSQL.setLong(4, new DateTimeUtil().getLongTime());
		ppSQL.setInt(5, bindStatus);
		ppSQL.setInt(6, bindResult);
		ppSQL.setString(7, bindDesc);
		ppSQL.setInt(8, userline);
		ppSQL.setString(9, remark);
		ppSQL.setInt(10, operType);
		ppSQL.setInt(11, bindType);
		ppSQL.setString(12, dealstaff);
		
		
		int res=DBOperation.executeUpdate(ppSQL.toString());
		return res;
	}
	
}
