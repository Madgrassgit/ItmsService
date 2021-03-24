package com.linkage.itms.dispatch.sxdx.service;

import ResourceBind.ResultInfo;
import com.ailk.tr069.devrpc.dao.corba.AcsCorbaDAO;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.itms.ResourceBindInterface;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.DevReset;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.sxdx.dao.PublicDAO;
import com.linkage.itms.dispatch.sxdx.obj.CpeFactoryResetDealXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 甘肃电信恢复出厂设置接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月14日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class CpeFactoryResetService extends ServiceFather {
	public CpeFactoryResetService(String methodName)
	{
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(CpeFactoryResetService.class);
	private ACSCorba corba = new ACSCorba();
	private CpeFactoryResetDealXML dealXML;

	public int work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);

		dealXML = new CpeFactoryResetDealXML(methodName);
		// 验证入参
		if (null == dealXML.getXML1(inXml)) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			return -2;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");

		PublicDAO dao = new PublicDAO();

		ArrayList<HashMap<String, String>> userDevList = dao.queryDeviceInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}", userDevList.toString());

		if(userDevList.size() > 1){
			logger.warn(methodName+"["+dealXML.getOpId()+"],查询到多条结果返回数量");
		}
		else if(null == userDevList || userDevList.size()==0 || StringUtil.isEmpty(StringUtil.getStringValue(userDevList.get(0), "device_id"))){
			logger.warn(methodName+"["+dealXML.getOpId()+"],未查询到结果，返回0");
			return 0;
		}

		String deviceId = StringUtil.getStringValue(userDevList.get(0), "device_id");
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag){
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备不在线或正在被操作，无法操作，返回-1");
			return -1;
		}

		logger.warn(methodName+"["+dealXML.getOpId()+"],设备在线，准备恢复出厂设置。");
		int irt = DevReset.reset(deviceId);
		if(1 == irt){
			logger.warn(methodName+"["+dealXML.getOpId()+"],恢复出厂设置成功,根据恢复出厂类型resetType[{}]进一步操作。",dealXML.getResetType());
			if("1".equals(dealXML.getResetType())){
				logger.warn(methodName+"["+dealXML.getOpId()+"],resetType=[{}]恢复出厂后直接结束。",dealXML.getResetType());
			}
			else if("2".equals(dealXML.getResetType())){

				DelSingleCPEService delService = new DelSingleCPEService("cpeFactoryReset");
				
				//2表示终端恢复出厂之后，作废此终端对应的工单，并把其状态改为入网。执行了此操作后，该终端即可作为新的终端进行放号
				logger.warn(methodName+"["+dealXML.getOpId()+"],resetType=[{}]恢复出厂后，删除用户、业务和设备。",dealXML.getResetType());
				
				String userId = StringUtil.getStringValue(userDevList.get(0), "user_id");
				
				//有绑定的设备，解绑、删除业务、语音参数、资源客户终端信息表、用户表信息
				if(!StringUtil.isEmpty(userId)){
					List<String> sqlList = new ArrayList<String>();
					
					sqlList.addAll(dao.stopAllServiceSql(StringUtil.getLongValue(userId)));
					sqlList.add(dao.delGwCustUserDevType(StringUtil.getLongValue(userId)));
					sqlList.addAll(dao.stopUserSql(StringUtil.getLongValue(userId)));
					String[] sqls = new String[sqlList.size()];
					sqlList.toArray(sqls);
					int res = dao.doBatch(sqls);
					
					if(res<=0){
						logger.warn(methodName+"["+dealXML.getOpId()+"]删除业务、语音参数、资源客户终端信息表、用户表信息批量sql更新失败.res={}.", res);
						return -2;
					}
					logger.warn(methodName+"["+dealXML.getOpId()+"]删除业务、语音参数、资源客户终端信息表、用户表信息批量sql更新成功.res={}.", res);
					//更新用户缓存
					itmsDeleteUser(userDevList.get(0).get("loid"));
				}
				
				
				StringBuffer sbSQL = new StringBuffer();
				sbSQL.append(delService.delete(deviceId));
				
				try
				{
					int res = dao.doBatch(sbSQL.toString().split(";"));
					if(res<=0){
						logger.warn(methodName+"["+dealXML.getOpId()+"]删除设备相关信息批量sql更新失败.res={}.", res);
						return -2;
					}
					logger.warn(methodName+"["+dealXML.getOpId()+"]删除设备相关信息批量sql更新成功.res={}.", res);
					
					String deviceIdS[] = new String[] { deviceId };
					String deSns[] = new String[] { userDevList.get(0).get("sn") };
					String devOuis[] = new String[] { userDevList.get(0).get("oui") };
					// 使用融合版的TR069，所以将原来的注释
					// int ret = new ACSCorba().chgDev(deviceIdS, deSns, devOuis, gw_type);
					int ret = new AcsCorbaDAO(Global.getPrefixName(Global.SYSTEM_NAME)+Global.SYSTEM_ACS).chgDev(deviceIdS, deSns, devOuis, "1");
					
					if (ret == 1)
					{
						logger.warn(methodName+"["+dealXML.getOpId()+"]ACS调用成功.res={}.", ret);
						try
						{
							// 删除设备
							String msg = delService.itmsDelDevice(deviceId);
							logger.warn(methodName+"["+dealXML.getOpId()+"]调绑定itmsDelDevice结束！res={}",msg);
							// 通知更新设备表
							msg = delService.itmsUpdateDevice(deviceId);
							logger.warn(methodName+"["+dealXML.getOpId()+"]调绑定itmsUpdateDevice结束！res={}",msg);
							return 0;
						}
						catch (Exception exx)
						{
							exx.printStackTrace();
							logger.warn(methodName+"["+dealXML.getOpId()+"]删除失败:调绑定Exception{}",exx);
							return -2;
						}
					}
					else
					{
						logger.warn(methodName+"["+dealXML.getOpId()+"]ACS调用失败.res={}.", ret);
						if (ret == 0)
						{
							logger.warn(methodName+"["+dealXML.getOpId()+"]删除失败:调用ACS参数错误");
						}
						else if (ret == -1)
						{
							logger.warn(methodName+"["+dealXML.getOpId()+"]删除失败:通知ACS失败");
						}
						else
						{
							logger.warn(methodName+"["+dealXML.getOpId()+"]调用ACS删除失败");
						}
						return -2;
					}
				}
				catch (Exception e)
				{
					logger.warn(methodName+"["+dealXML.getOpId()+"]删除失败,Excetion{}",e);
					return -2;
				}
			}
			else if("3".equals(dealXML.getResetType())){
				//3表示终端恢复出厂之后，把此终端对应的非作废工单状态改为正在执行，并把其状态改为绑定。执行了此操作后，即可在不重新下发工单的情况下允许此终端重新注册。
				dao.updateCustStatusFailure(StringUtil.getLongValue(userDevList.get(0).get("user_id")), 0);
				logger.warn(methodName+"["+dealXML.getOpId()+"],resetType=[{}]恢复出厂后将业务状态置为未做。",dealXML.getResetType());
			}
			
			return 1;
			
		}
		else{
			// 调用配置模块，或者acs模块对设备下发恢复出厂设置命令失败后，业务用户表修改成成功状态
			if(!StringUtil.isEmpty(StringUtil.getStringValue(userDevList.get(0), "user_id")))
			dao.updateCustStatusFailure(StringUtil.getLongValue(userDevList.get(0).get("user_id")), 1);
			logger.warn(methodName+"["+dealXML.getOpId()+"],恢复出厂设置失败,设备返回错误码{}。", irt);
			return -2;
		}
	}
	
	

	/**
	 * 删除内存中的用户信息
	 * 
	 * @param userName 用户帐号itms
	 * @return 更新结果
	 */
	private String itmsDeleteUser(String userName)
	{
		logger.debug("itmsUpdateUser(username{})",
				new Object[]{userName});
		
		String msg = "";
		ResourceBindInterface corba = CreateObjectFactory.createResourceBind("1");
		
		ResultInfo rs = corba.deleteUser(userName);
		if(rs == null)
		{
			logger.warn("更新失败，系统内部错误");
		}
		else if(rs.resultId[0].equals("1")){
			logger.warn("更新成功");
			msg = "1";
		}
		
		return msg;
	}
	
}
