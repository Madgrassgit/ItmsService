package com.linkage.stbms.ids.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.stbms.dao.RecordLogDAO;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.util.GetStbAccountChecker;
import com.linkage.stbms.ids.util.GetStbIpChecker;
import com.linkage.stbms.itv.main.Global;

/**
 * 新疆电信  机顶盒业务账号查询接口
 * @author chenxj6
 * @date 2016-9-29
 * @param inParam
 * @return
 */
public class GetStbAccountService {
	private static Logger logger = LoggerFactory.getLogger(GetStbAccountService.class);

	public String work(String inParam)
	{
		logger.warn("GetStbAccountService==>inParam:" + inParam);
		GetStbAccountChecker checker = new GetStbAccountChecker(inParam);
		// 入参验证
		if (false == checker.check())
		{
			logger.warn("机顶盒业务账号查询接口，入参验证失败，devSn=[{}]",
					new Object[] { checker.getDevSn() });
			logger.warn("GetStbAccountService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		

		try {
			String servAccount = null;
			UserStbInfoDAO dao = new UserStbInfoDAO();
			List<HashMap<String,String>> stbDeviceMapList = dao.getServAccountByDevSn(checker.getDevSn());
			
			if (null==stbDeviceMapList || stbDeviceMapList.size()==0)
			{
				logger.warn("没有查到设备，devSn={}",	new Object[] { checker.getDevSn() });
				checker.setRstCode("1000");
				checker.setRstMsg("没有查到设备");
				if("xj_dx".equals(Global.G_instArea)){
					new RecordLogDAO().recordLog(checker.getDevSn(), checker.getInParam(), "",
	                        checker.getReturnXml(), 1);
				}
				return checker.getReturnXml();
			}
			
			if(stbDeviceMapList.size()>1){
				logger.warn("查到多组设备，请输入更多位设备序列号进行查询，devSn={}",	new Object[] { checker.getDevSn() });
				checker.setRstCode("1000");
				checker.setRstMsg("查到多组设备，请输入更多位设备序列号进行查询");
				if("xj_dx".equals(Global.G_instArea)){
					new RecordLogDAO().recordLog(checker.getDevSn(), checker.getInParam(), "",
	                        checker.getReturnXml(), 1);
				}
				return checker.getReturnXml();
			}
			
			servAccount = stbDeviceMapList.get(0).get("serv_account");

			if (null==servAccount || servAccount.trim().length()==0){
				logger.warn("业务账号为空");
				checker.setRstCode("1000");
				checker.setRstMsg("业务账号为空");
				if("xj_dx".equals(Global.G_instArea)){
					new RecordLogDAO().recordLog(checker.getDevSn(), checker.getInParam(), "",
	                        checker.getReturnXml(), 1);
				}
				return checker.getReturnXml();
			}
			
			
			checker.setServAccount(servAccount);
			logger.warn("GetStbAccountService==>returnXML:" + checker.getServAccountReturnXml());
			if("xj_dx".equals(Global.G_instArea)){
				new RecordLogDAO().recordLog(checker.getDevSn(), checker.getInParam(), "",
                        checker.getReturnXml(), 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return checker.getServAccountReturnXml();
	}
}
