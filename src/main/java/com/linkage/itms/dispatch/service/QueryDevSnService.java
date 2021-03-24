package com.linkage.itms.dispatch.service;

import java.util.Map;

import com.linkage.itms.dao.DeviceInfoDAO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryDevSnChecker;

/**
 * 设备序列号查询接口
 * @author zhangshimin(工号) Tel:78
 * @version 1.0
 * @since 2011-5-11 下午02:54:14
 * @category com.linkage.itms.dispatch.service
 * @copyright 南京联创科技 网管科技部
 *
 */
public class QueryDevSnService implements IService
{
	private static Logger logger = LoggerFactory.getLogger(QueryDevSnService.class);
	private String returnXml=null;
	//大于等于该阈值需要检查设备是否有千兆口
	private static final String GIGABIT_BAND_LIMIT = "200M";
	
	@Override
	public String work(String inXml)
	{
		logger.warn("queryDevSN inXml:({})",getStr(inXml));
		
		QueryDevSnChecker checker = new QueryDevSnChecker(inXml);
		if (!checker.check()) 
		{
			returnXml= checker.getReturnXml();
			logger.warn("servicename[queryDevSN] cmdId[{}],userinfo[{}] 验证未通过，返回：{}",
					new Object[]{checker.getCmdId(),checker.getUserInfo(),getStr(returnXml)});
			
			return returnXml;
		}
		logger.warn("servicename[queryDevSN] cmdId[{}],userinfo[{}] 参数校验通过",
				checker.getCmdId(),checker.getUserInfo());
		
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		
		// 查询用户信息 考虑属地因素
		Map<String, String> userInfoMap = userDevDao.queryUserInfo(
														checker.getUserInfoType(), 
														checker.getUserInfo());
		
		if (null == userInfoMap || userInfoMap.isEmpty()) {
			logger.warn("servicename[queryDevSN] cmdId[{}],userinfo[{}] 查无此用户",
					checker.getCmdId(),checker.getUserInfo());
			checker.setResult(1002);
			checker.setResultDesc("无此用户信息");
		} 
		else
		{
			String devsn = userInfoMap.get("device_serialnumber");
			if (StringUtil.IsEmpty(devsn)) {// 用户未绑定终端
				logger.warn("servicename[queryDevSN] cmdId[{}],userinfo[{}] 此客户未绑定",
						checker.getCmdId(),checker.getUserInfo());
				checker.setResult(1003);
				checker.setResultDesc("此客户未绑定");
			}
			else
			{
				checker.setDevSn(devsn);
				if ("js_dx".equals(Global.G_instArea))
				{
					String oui = userInfoMap.get("oui");
					checker.setDevSn(oui + "-" + devsn);
				}
				else if("xj_dx".equals(Global.G_instArea))
				{
					String device_id = userInfoMap.get("device_id");
					checker.setDevSn(userInfoMap.get("oui") + "-" + devsn);
					
					DeviceInfoDAO devInfDao = new DeviceInfoDAO();
					Map<String,String> devMap=devInfDao.queryDevInfoByDeviceId(device_id);
					String sn = StringUtil.getStringValue(devMap,"device_serialnumber");
					// 用设备序列号的后12位，做成MAC的形式
					if(StringUtils.isNotEmpty(sn))
					{
						String macNumber = StringUtil.getUpperCase(StringUtils.substring(sn,-12));
						StringBuffer sb = new StringBuffer();
						sb.append(StringUtils.substring(macNumber,0,2));
						sb.append(":"+StringUtils.substring(macNumber,2,4));
						sb.append(":"+StringUtils.substring(macNumber,4,6));
						sb.append(":"+StringUtils.substring(macNumber,6,8));
						sb.append(":"+StringUtils.substring(macNumber,8,10));
						sb.append(":"+StringUtils.substring(macNumber,10,12));
						
						checker.setMacaddress(sb.toString());
					}
					
					String devicetype_id=StringUtil.getStringValue(devMap,"devicetype_id");
					if(StringUtils.isNotEmpty(devicetype_id))
					{
						Map<String,String> devTypeMap=devInfDao.queryInfo(devicetype_id);
						
						checker.setVendor(StringUtil.getStringValue(devTypeMap,"vendor_name"));
						checker.setDevModel(StringUtil.getStringValue(devTypeMap,"device_model"));
						checker.setHardwareVersion(StringUtil.getStringValue(devTypeMap,"hardwareversion"));
						checker.setSoftwareVersion(StringUtil.getStringValue(devTypeMap,"softwareversion"));

						//XJDX-ITMS-20191104-LJ-001 增加是否支持当前宽带值判断返回 带宽大于等于200M需要判断设备是否有千兆口
						String downBandwidth = userInfoMap.get("down_bandwidth");
						//查询表tab_device_version_attribute  gigabit_port字段-是否有千兆口 1-有
						if(!StringUtil.IsEmpty(downBandwidth) && downBandwidth.compareTo(GIGABIT_BAND_LIMIT) >= 0){
							//带宽大于等于200M 需要返回千兆口字段值为 1
							if(userDevDao.queryGigabitPort(devicetype_id) == 1){
								checker.setIsSupport(1);
							}else {
								checker.setIsSupport(0);
							}
						}else {
							checker.setIsSupport(1);
						}
					}
				}
			}
		}
		
		returnXml = checker.getReturnXml();
		new RecordLogDAO().recordDispatchLog(checker,checker.getUserInfo(),"QueryDevSnService");
		logger.warn("servicename[queryDevSN] cmdId[{}],userinfo[{}] 处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(),checker.getUserInfo(),getStr(returnXml)});
		return returnXml;
	}
	
	/**
	 * xml去换行
	 */
	private String getStr(String str)
	{
		if(!StringUtil.IsEmpty(str)){
			return str.replaceAll("\n","");
		}
		return str;
	}
}
