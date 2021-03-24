package com.linkage.itms.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.BindFailedDao;
import com.linkage.itms.dispatch.obj.BindFailedChecker;
		
public class BindFailedService implements IService
{
	private BindFailedDao bindFailDao=new BindFailedDao();
	private static Logger logger = LoggerFactory.getLogger(BindFailedService.class);
	/**
	 *  @author cczhong
	 *  @decription 输入用户账号（宽带账号、逻辑SN）进行查询终端当前绑定失败的原因
	 */
	@Override
	public String work(String inXml)
	{
		//检查合法性
		BindFailedChecker checker = new BindFailedChecker(inXml);
		if(false == checker.check()){
			logger.error(
					"servicename[BindFailedService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[BindFailedService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), checker.getUserInfo(),
						inXml });
		int userInfoType=checker.getUserInfoType();
		String userInfo=checker.getUserInfo();
		Map<String,String> userMap = bindFailDao.queryUserInfo(userInfoType, userInfo);
		if (userMap == null || StringUtil.getStringValue(userMap,"user_id") == null){
			checker.setRstCode("1003");
			checker.setRstMsg("查不到对应的客户信息");
		}
		else if (StringUtil.getStringValue(userMap,"device_id") == null){
			List<HashMap<String,String>> list=bindFailDao.getFailReason(StringUtil.getStringValue(userMap,"username"));
			if(list==null||list.size()==0){
				checker.setRstCode("1000");
				checker.setRstMsg("未知错误");
			}else{
				//最新失败原因
				checker.setBindFailed(StringUtil.getStringValue(list.get(0),"fail_desc",""));
			}
		}else{
			checker.setBindFailed("该用户已绑定");
		}
		String result=checker.getReturnXml();
		return result;
			
	}
}

	