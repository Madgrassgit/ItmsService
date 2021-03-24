
package com.linkage.stbms.ids.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.stbms.ids.dao.UserChangeStbDAO;
import com.linkage.stbms.ids.util.SetStbBindSNChecker;

/**
 * 安徽电信 设置用户和设备的对应关系
 * 通过此接口将机顶盒mac地址和机顶盒需要绑定的业务账号传过来入库，方便绑定模块进行关系绑定
 * 类似于itv的掌调扫描接口
 * 
 * @author chensiqing (Ailk No.)
 * @version 1.0
 * @since 2016年3月30日
 * @category com.linkage.stbms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class SetStbBindSNService
{

	private static Logger logger = LoggerFactory.getLogger(SetStbBindSNService.class);

	public String work(String inParam)
	{
		logger.warn("SetStbBindSNService==>inParam:" + inParam);
		SetStbBindSNChecker checker = new SetStbBindSNChecker(inParam);
		// 入参验证
		if (false == checker.check())
		{
			logger.warn("SetStbBindSNService，入参验证失败，mac[{}],serv_account[{}],operType[{}]",
					new Object[] { checker.getMac(), checker.getServ_account(),checker.getOperate_type()});
			logger.warn("SetStbBindSNService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		UserChangeStbDAO dao = new UserChangeStbDAO();
		int rs = dao.updateMacByServAccount(checker.getServ_account(), checker.getMac());
		if(rs>0)
		{
			checker.setRstCode("0");
			checker.setRstMsg("成功");
		}
		else
		{
			checker.setRstCode("1000");
			checker.setRstMsg("更新入库失败，业务账号不存在或数据库异常");
			logger.warn("更新入库失败，业务账号[{}]不存在或数据库异常",checker.getServ_account());
		}
		logger.warn("SetStbBindSNService==>returnXML:" + checker.getReturnXml());
		return checker.getReturnXml();
	}
}
