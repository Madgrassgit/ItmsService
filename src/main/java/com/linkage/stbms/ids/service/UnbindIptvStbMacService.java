package com.linkage.stbms.ids.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ResourceBind.ResultInfo;
import ResourceBind.UnBindInfo;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.ResourceBindInterface;
import com.linkage.stbms.cao.ResourceBindCorba;
import com.linkage.stbms.ids.dao.UnbindIptvStbMacServiceDAO;
import com.linkage.stbms.ids.util.UnbindIptvStbMacChecker;
import com.linkage.stbms.itv.main.Global;

public class UnbindIptvStbMacService {
	
	private static final Logger logger = LoggerFactory.getLogger(UnbindIptvStbMacService.class);
	
	public String work(String inParam) {
		
		logger.debug("UnbindIptvStbMacService==>work({})",new Object[]{inParam});
		logger.warn("UnbindIptvStbMacService==>inParam={"+inParam+"}");
		
		UnbindIptvStbMacChecker check = new UnbindIptvStbMacChecker(inParam);
		
		if (false == check.check()) {
			logger.warn(
					"[{}]入参验证没通过，返回：{}",
					new Object[] {
							"SelectType=" + check.getSelectType() + "；UserInfo="
									+ check.getUserInfo(), check.getReturnXml() });
			logger.warn("UnbindIptvStbMacService==>return："+check.getReturnXml());
			return check.getReturnXml();
		}
		
		UnbindIptvStbMacServiceDAO dao = new UnbindIptvStbMacServiceDAO();
		
		/**
		 * 查询类型
		 * 1：根据业务帐号查询
		 * 2：根据MAC地址查询
		 */
		String selectType = check.getSelectType();
		/**
		 * 查询类型所对应的用户信息
		 * SelectType为1时为itv业务账号
		 * SelectType为2时为机顶盒MAC
		 */
		String userInfo = check.getUserInfo();
		
		Map<String, String> infoMap = null;
		
		// 根据业务帐号检索  SelectType为1时为itv业务账号
		if ("1".equals(selectType)) {
			// 根据业务帐号确认是否与设备存在绑定关系
			infoMap = dao.getUserInfoAndDevInfoByServAccount(userInfo);
			// 业务帐号不存在绑定关系
			if (null == infoMap || infoMap.isEmpty()) {
				check.setRstCode("1004");
				check.setRstMsg("业务帐号不存在绑定关系");
				logger.warn("UnbindIptvStbMacService==>return："+check.getReturnXml());
				return check.getReturnXml();
			}
		}
		// 根据机顶盒MAC地址检索  SelectType为2时为机顶盒MAC
		else if ("2".equals(selectType)) {
			// 根据业务帐号确认是否与设备存在绑定关系
			infoMap = dao.getUserInfoAndDevInfoByDevMac(userInfo.toUpperCase());
			// 业务帐号不存在绑定关系
			if (null == infoMap || infoMap.isEmpty()) {
				check.setRstCode("1004");
				check.setRstMsg("业务帐号不存在绑定关系");
				logger.warn("UnbindIptvStbMacService==>return："+check.getReturnXml());
				return check.getReturnXml();
			}
		}
		
		String customer_id = StringUtil.getStringValue(infoMap, "customer_id", "");
		String deviceId = StringUtil.getStringValue(infoMap, "device_id", "");
		
		UnBindInfo[] arr = new UnBindInfo[1];
		arr[0] = new UnBindInfo();
		arr[0].accOid = customer_id;
		arr[0].accName = "admin";
		arr[0].userId = customer_id;
		arr[0].deviceId = deviceId;
		arr[0].userline = 1;
		
		// 解绑
		ResultInfo rs = null;
		if("jx_dx".equals(Global.G_instArea)){
		     ResourceBindInterface corba = CreateObjectFactory.createResourceBind(Global.GW_TYPE_STB);
		     rs = corba.release(arr,"StbService");
		}else{
			ResourceBindCorba corba = new ResourceBindCorba();
			rs = corba.release(arr);
		}
		
		if(rs == null)
		{
			check.setRstCode("1005");
			check.setRstMsg("解绑失败，系统内部错误");
			logger.warn("UnbindIptvStbMacService==>return："+check.getReturnXml());
			return check.getReturnXml();
		}
		else
		{
			// 成功
			if(rs.resultId[0].equals("1"))
			{
				check.setRstCode("1006");
				check.setRstMsg("解绑成功！");
				logger.warn("UnbindIptvStbMacService==>return："+check.getReturnXml());
				return check.getReturnXml();
			}
			// 失败
			else
			{
				check.setRstCode("1005");
				check.setRstMsg("解绑失败！");
				logger.warn("UnbindIptvStbMacService==>return："+check.getReturnXml());
				return check.getReturnXml();
			}
		}
		
	}
}
