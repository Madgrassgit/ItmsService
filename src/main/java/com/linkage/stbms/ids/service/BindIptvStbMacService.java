package com.linkage.stbms.ids.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ResourceBind.BindInfo;
import ResourceBind.ResultInfo;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.ResourceBindInterface;
import com.linkage.stbms.cao.ResourceBindCorba;
import com.linkage.stbms.ids.dao.BindIptvStbMacServiceDAO;
import com.linkage.stbms.ids.util.BindIptvStbMacChecker;
import com.linkage.stbms.itv.main.Global;


public class BindIptvStbMacService {
	
	private static final Logger logger = LoggerFactory.getLogger(BindIptvStbMacService.class);
	
	public String work(String inParam) {
		
		logger.warn("BindIptvStbMacService==start==>inParam={"+inParam+"}");
		
		BindIptvStbMacChecker check = new BindIptvStbMacChecker(inParam);
		
		if (false == check.check()) {
			logger.warn(
					"[{}]入参验证没通过，返回：{}",
					new Object[] {
							"SearchType=" + check.getSearchType() + "；SearchInfo="
									+ check.getSearchInfo(), check.getReturnXml() });
			logger.warn("BindIptvStbMacService==>return："+check.getReturnXml());
			return check.getReturnXml();
		}
		
		BindIptvStbMacServiceDAO dao = new BindIptvStbMacServiceDAO();
		
		/**
		 * 查询类型
		 * 1：根据业务帐号查询
		 * 2：根据MAC地址查询
		 * 3:根据机顶盒序列号查询
		 */
		String searchType = check.getSearchType();
		/**
		 * 查询类型所对应的用户信息
		 * searchType为1时为itv业务账号
		 * searchType为2时为机顶盒MAC
		 * searchType为3时为机顶盒序列号
		 */
		String searchInfo = check.getSearchInfo();
		
		/**
		 * 需要绑定的业务账号
		 */
		String userAccount = check.getUserAccount();
		
		Map<String, String> infoMap = null;
		
		//根据业务账号或者机顶盒MAC或者机顶盒序列号查询确认业务账号是否设备存在绑定关系
		infoMap = dao.getSearchInfoAndDevInfo(searchType, searchInfo);
		
		// 业务帐号已绑定了设备 或设备不存在
		if (null == infoMap || infoMap.isEmpty()) {
			check.setRstCode("1004");
			check.setRstMsg("对应设备不存在，请检查相关参数是否正确！");
			logger.warn("BindIptvStbMacService==>return："+check.getReturnXml());
			return check.getReturnXml();
		}else {
			int cpe_allocatedstatus = StringUtil.getIntValue(infoMap, "cpe_allocatedstatus");
			if(cpe_allocatedstatus == 1){
				check.setRstCode("1004");
				check.setRstMsg("业务帐号已绑定了设备，请先解绑当前绑定设备");
				logger.warn("BindIptvStbMacService==>return："+check.getReturnXml());
				return check.getReturnXml();
			}
		}
		
		String customer_id = StringUtil.getStringValue(infoMap, "customer_id", "");
		String deviceId = StringUtil.getStringValue(infoMap, "device_id", "");
		
		BindInfo[] bindInfo = new BindInfo[1];
		bindInfo[0] = new BindInfo();
		bindInfo[0].accOid = customer_id;
		bindInfo[0].accName = "admin";
		bindInfo[0].username = userAccount; //业务账号
		bindInfo[0].deviceId = deviceId;
		bindInfo[0].userline = 1;
		 
		// 绑定
		//ResultInfo rs = corba.DoBind(bindInfo,"web");
		ResultInfo rs = null;
		if("jx_dx".equals(Global.G_instArea)){
			 ResourceBindInterface corba = CreateObjectFactory.createResourceBind(Global.GW_TYPE_STB);
		     rs = corba.bind(bindInfo,"web");
		}else{
			ResourceBindCorba corba = new ResourceBindCorba();
			rs = corba.DoBind(bindInfo,"web");
		}				
		
		if(rs == null)
		{
			check.setRstCode("1005");
			check.setRstMsg("绑定失败，系统内部错误");
			logger.warn("BindIptvStbMacService==>return："+check.getReturnXml());
			return check.getReturnXml();
		}
		else
		{
			// 成功
			if(rs.resultId[0].equals("1"))
			{
				check.setRstCode("1006");
				check.setRstMsg("绑定成功！");
				logger.warn("BindIptvStbMacService==>return："+check.getReturnXml());
				return check.getReturnXml();
			}
			// 失败
			else
			{
				check.setRstCode("1005");
				check.setRstMsg("绑定失败！");
				logger.warn("BindIptvStbMacService==>return："+check.getReturnXml());
				logger.warn("绑定失败，返回结果为：[{}]", rs.resultId[0]);
				return check.getReturnXml();
			}
		}
		
	}
}
