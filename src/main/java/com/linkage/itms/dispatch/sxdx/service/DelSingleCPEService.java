package com.linkage.itms.dispatch.sxdx.service;

import ResourceBind.ResultInfo;
import ResourceBind.UnBindInfo;
import com.ailk.tr069.devrpc.dao.corba.AcsCorbaDAO;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.itms.ResourceBindInterface;
import com.linkage.itms.dispatch.sxdx.dao.PublicDAO;
import com.linkage.itms.dispatch.sxdx.obj.DelSingleCPEDealXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 甘肃电信删除单个终端接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月12日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DelSingleCPEService extends ServiceFather {
	public DelSingleCPEService(String methodName)
	{
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(DelSingleCPEService.class);
	private DelSingleCPEDealXML dealXML;
	
	public int  work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		
		dealXML = new DelSingleCPEDealXML(methodName);
		// 验证入参
		if (null == dealXML.getXML(inXml)) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			return -2;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		
		PublicDAO dao = new PublicDAO();

		ArrayList<HashMap<String, String>> InfoList = dao.getInfoByCPEID(dealXML.getCpeID());
		//查询不到设备
		if(null == InfoList || InfoList.size()==0 || StringUtil.IsEmpty(InfoList.get(0).get("device_id"))){
			return -1;
		}
		
		String msg = null;
		StringBuffer sbSQL = new StringBuffer();
		String deviceId = InfoList.get(0).get("device_id");
		//绑定了用户
		if(!StringUtil.IsEmpty(InfoList.get(0).get("username"))){
			HashMap<String,String> args = InfoList.get(0);
			msg = ItmsRelease(args.get("user_id"), args.get("username"), deviceId, 1);
			
			msg = itmsUpdateUser(args.get("username"));
			
		}
		if("1".equals(msg)) {
			sbSQL.append(delete(deviceId));
		}
		
		try
		{
			int res = dao.doBatch(sbSQL.toString().split(";"));
			if(res<=0){
				logger.warn(methodName+"["+dealXML.getOpId()+"]批量sql更新失败.res={}.", res);
				return -2;
			}
			logger.warn(methodName+"["+dealXML.getOpId()+"]批量sql更新成功.res={}.", res);
			
			String deviceIdS[] = new String[] { deviceId };
			String deSns[] = new String[] { dealXML.getCpeID().split("-")[1] };
			String devOuis[] = new String[] { dealXML.getCpeID().split("-")[0] };
			// 使用融合版的TR069，所以将原来的注释
			// int ret = new ACSCorba().chgDev(deviceIdS, deSns, devOuis, gw_type);
			int ret = new AcsCorbaDAO(Global.getPrefixName(Global.SYSTEM_NAME)+Global.SYSTEM_ACS).chgDev(deviceIdS, deSns, devOuis, "1");
			
			if (ret == 1){
				logger.warn(methodName+"["+dealXML.getOpId()+"]ACS调用成功.res={}.", ret);
				try{
					// 删除设备
					msg = itmsDelDevice(deviceId);
					logger.warn(methodName+"["+dealXML.getOpId()+"]调绑定itmsDelDevice结束！res={}",msg);
					// 通知更新设备表
					msg = itmsUpdateDevice(deviceId);
					logger.warn(methodName+"["+dealXML.getOpId()+"]调绑定itmsUpdateDevice结束！res={}",msg);
					if("1".equals(msg)){
						return 1;
					}
					return 0;
				}
				catch (Exception exx){
					exx.printStackTrace();
					logger.warn(methodName+"["+dealXML.getOpId()+"]删除失败:调绑定Exception{}",exx);
					return -2;
				}
			}
			else{
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

	protected String delete(String deviceId)
	{
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" delete from tab_gw_res_area where res_id ='").append(deviceId).append("' and res_type = 1 ").append(";");
		sbSQL.append(" delete from gw_devicestatus where device_id ='").append(deviceId).append("' ").append(";");
		sbSQL.append(" delete from sgw_security where device_id ='").append(deviceId).append("' ").append(";");
		sbSQL.append(" delete from tab_gw_device where device_id ='").append(deviceId).append("' ");
		return sbSQL.toString();
	}
	
	protected String ItmsRelease(String userId, String username, String deviceId, int userline)
	{
		logger.debug(
				"itmsRelease(userId:{};username{};deviceId:{};userline:{})",
				new Object[] { userId, username, deviceId, userline });
		String msg = "";
		ResourceBindInterface corba = CreateObjectFactory.createResourceBind("1");
		UnBindInfo[] arr = new UnBindInfo[1];
		arr[0] = new UnBindInfo();
		arr[0].accOid = "0";
		arr[0].accName = "ItmsService";
		arr[0].userId = userId;
		arr[0].deviceId = deviceId;
		arr[0].userline = userline;
		ResultInfo rs = corba.release(arr);
		if (rs == null)
		{
			msg = "-10000";
		}
		else
		{
			msg = "" + Integer.parseInt(rs.resultId[0]);
		}
		return msg;
	}
	
	
	/**
	 * 更新内存中的用户信息
	 * 
	 * @param userName 用户帐号itms
	 * @return 更新结果
	 */
	public String itmsUpdateUser(String userName)
	{
		logger.debug("itmsUpdateUser(username{})",
				new Object[]{userName});
		
		String msg = "";
		ResourceBindInterface corba = CreateObjectFactory.createResourceBind("1");
		
		ResultInfo rs = corba.updateUser(userName);
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
	
	
	/**
	 * 删除设备
	 * 
	 * @param device_id
	 * @return 删除结果
	 */
	public String itmsDelDevice(String device_id)
	{
		logger.debug("itmsDelDevice(username{})", new Object[]{device_id});
		
		String msg = "";
		ResourceBindInterface corba = CreateObjectFactory.createResourceBind("1");
		
		ResultInfo rs = corba.delDevice(device_id);
		if(rs == null)
		{
			logger.warn("删除失败，系统内部错误");
		}
		else if(rs.resultId[0].equals("1")){
			logger.warn("删除成功");
			msg = "1";
		}
		
		return msg;
	}
	
	
	public String itmsUpdateDevice(String device_id)
	{
		logger.debug("itmsUpdateDevice(username{})", new Object[]{device_id});
		
		String msg = "";
		ResourceBindInterface corba = CreateObjectFactory.createResourceBind("1");
		
		ResultInfo rs = corba.updateDevice(device_id);
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
