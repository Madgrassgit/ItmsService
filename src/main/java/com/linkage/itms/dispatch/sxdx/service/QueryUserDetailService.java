package com.linkage.itms.dispatch.sxdx.service;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dispatch.sxdx.beanObj.UserDetail;
import com.linkage.itms.dispatch.sxdx.dao.PublicDAO;
import com.linkage.itms.dispatch.sxdx.obj.QueryUserDetailDealXML;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * 甘肃电信查询用户详细信息接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月10日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryUserDetailService extends ServiceFather {
	public QueryUserDetailService(String methodName)
	{
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(QueryUserDetailService.class);
	private ACSCorba corba = new ACSCorba();
	private UserDetail userdetail = new UserDetail();
	private QueryUserDetailDealXML dealXML;
	
	public UserDetail work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		
		dealXML = new QueryUserDetailDealXML(methodName);
		// 验证入参
		if (null == dealXML.getXML(inXml)) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			return userdetail;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		
		PublicDAO dao = new PublicDAO();

		UserDetail userdetail = dao.queryUserDetail(StringUtil.getIntegerValue(dealXML.getiParaType()), dealXML.getValue());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}",userdetail.toString());
		
		//在线，连设备校验是否真的在线，如果在，进一步采集wifi使能相关节点
		if("1".equals(userdetail.getOnline_status())){
			int index = userdetail.getCpe_status().indexOf("#");
			if(index<0) return userdetail;
			String deviceId = userdetail.getCpe_status().substring(index+1);
			userdetail.setCpe_status(userdetail.getCpe_status().substring(0, index));
			//采集实时在线情况以及wifi使能
			connectDevGather(deviceId);
		}
		
		return userdetail;
	}

	
	/**
	 * 根据设备id查询wifi开关是否打开
	 * @param deviceId 设备id
	 */
	private void connectDevGather(String deviceId)
	{
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
		if (1 != flag){
			userdetail.setOnline_status("0");
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备不在线或正在被操作，无法获取节点值，device_id={}", deviceId);
			return;
		}
		else{
			logger.warn(methodName+"["+dealXML.getOpId()+"],设备在线开始采集，device_id={}", deviceId);
			String lanPath = "InternetGatewayDevice.LANDevice.";
			// 获取全部路径
			ArrayList<String> landevicePathsList = new ArrayList<String>();
			landevicePathsList = corba.getParamNamesPath(deviceId, lanPath, 0);
			logger.warn(methodName+"["+dealXML.getOpId()+"],LANDevice.size:{}", landevicePathsList.size());
			if (landevicePathsList == null || landevicePathsList.size() == 0 || landevicePathsList.isEmpty()){
				return;
			}
			else
			{
				ArrayList<String> paramNameList = new ArrayList<String>();
				for (int i = 0; i < landevicePathsList.size(); i++)
				{
					String namepath = landevicePathsList.get(i);
					if (namepath.indexOf(".WLANConfiguration") > 0 && namepath.indexOf(".Enable") > 0){
						paramNameList.add(namepath);
					}
				}
				
				if(paramNameList.size() > 0){
					String[] gatherPathArray = new String[paramNameList.size()];
					paramNameList.toArray(gatherPathArray);
					// 处理设备采集结果
					ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPathArray);
					if (null == objLlist || objLlist.isEmpty()){
						logger.warn("{}|[{}],采集wifi使能失败，device_id={}",methodName,dealXML.getOpId(),deviceId);
						return ;
					}
					else{
						for (ParameValueOBJ pvobj : objLlist){
							if ("1".equals(pvobj.getValue())){
								userdetail.setWifi_status("1");
								return;
							}
						}
					}
				}
			}
		}
		
	}
}
