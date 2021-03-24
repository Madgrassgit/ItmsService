package com.linkage.itms.dispatch.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.VoipPortDao;
import com.linkage.itms.dispatch.obj.VoipPortChecker;
		
public class VoipPortService implements IService
{
	private VoipPortDao voipPortDao=new VoipPortDao();
	private static Logger logger = LoggerFactory.getLogger(VoipPortService.class);
	/**
	 *  @author cczhong
	 *  @decription 可通过输入物理号码（语音号码），查询该物理号码在工单中的端口信息
	 */
	@Override
	public String work(String inXml)
	{
		//检查合法性
		VoipPortChecker checker = new VoipPortChecker(inXml);
				if(false == checker.check()){
					logger.error(
							"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]验证未通过，返回：{}",
							new Object[] { checker.getCmdId(), checker.getUserInfo(),
									checker.getReturnXml() });
					return checker.getReturnXml();
				}
				logger.warn(
						"servicename[QueryDeviceInfoService]cmdId[{}]userinfo[{}]参数校验通过，入参为：{}",
						new Object[] { checker.getCmdId(), checker.getUserInfo(),
								inXml });
			String userInfo=checker.getUserInfo();
			Map<String,String> voipMap=voipPortDao.queryVoipPort(userInfo);
			if(voipMap==null){
				checker.setRstCode("1002");
				checker.setRstMsg("查不到对应的客户信息");
			}else if(null==StringUtil.getStringValue(voipMap,"line_id")){
				checker.setRstCode("1000");
				checker.setRstMsg("未知错误");
			}else{
				//返回数值
				checker.setVoipPort(StringUtil.getStringValue(voipMap,"line_id"));
			}
			String result=checker.getReturnXml();
			return result;
	}
}

	