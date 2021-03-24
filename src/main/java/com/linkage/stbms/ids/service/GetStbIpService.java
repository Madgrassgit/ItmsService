package com.linkage.stbms.ids.service;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.stbms.dao.RecordLogDAO;
import com.linkage.stbms.ids.dao.UserStbInfoDAO;
import com.linkage.stbms.ids.util.GetStbIpChecker;
import com.linkage.stbms.itv.main.Global;

/**
 * 新疆电信  获取机顶盒ip地址接口
 * @author chenxj6
 * @date 2016-8-30
 * @param inParam
 * @return
 * 
 */
public class GetStbIpService
{
	private static Logger logger = LoggerFactory
			.getLogger(GetStbIpService.class);

	public String work(String inParam)
	{
		logger.warn("GetStbIpService==>inParam:" + inParam);
		GetStbIpChecker checker = new GetStbIpChecker(inParam);
		// 入参验证
		if (false == checker.check())
		{
			logger.warn("机顶盒ip地址查询接口，入参验证失败，SearchType=[{}],SearchInfo=[{}]",
					new Object[] { checker.getSearchType(), checker.getSearchInfo() });
			logger.warn("GetStbIpService==>return：" + checker.getReturnXml());
			return checker.getReturnXml();
		}
		

		try {
			String deviceId = null;
			UserStbInfoDAO dao = new UserStbInfoDAO();
			if("1".equals(checker.getSearchType())){
				List<HashMap<String,String>> maps = dao.checkDevice(checker.getSearchInfo());
				if(null==maps || maps.size()==0){
					logger.warn("用户不存在，serchType={}，searchInfo={}", new Object[] { checker.getSearchType(), checker.getSearchInfo() });
					checker.setRstCode("1000");
					checker.setRstMsg("该用户不存在");
					if("xj_dx".equals(Global.G_instArea)){
						new RecordLogDAO().recordLog(checker.getSearchInfo(), inParam, "",
								checker.getReturnXml(), 1);
					}
					return checker.getReturnXml();
				}
			}
			
			List<HashMap<String,String>> userMapList = dao.getDeviceDescTime(checker.getSearchType(),checker.getSearchInfo(),"2");
			
			if (null==userMapList || userMapList.size()==0)
			{
				if("1".equals(checker.getSearchType())){
					logger.warn("请将机顶盒零配，serchType={}，searchInfo={}", new Object[] { checker.getSearchType(), checker.getSearchInfo() });
					checker.setRstCode("1002");
					checker.setRstMsg("IPTV账号没有绑定机顶盒，请将机顶盒零配");
					if("xj_dx".equals(Global.G_instArea)){
						new RecordLogDAO().recordLog(checker.getSearchInfo(), inParam, "",
								checker.getReturnXml(), 1);
					}
					return checker.getReturnXml();
				}
				logger.warn("没有查到设备，serchType={}，searchInfo={}",	new Object[] { checker.getSearchType(), checker.getSearchInfo() });
				checker.setRstCode("1000");
				checker.setRstMsg("没有查到设备");
				if("xj_dx".equals(Global.G_instArea)){
					new RecordLogDAO().recordLog(checker.getSearchInfo(), inParam, "",
							checker.getReturnXml(), 1);
				}
				return checker.getReturnXml();
			}
			
			if(userMapList.size()>1){
				logger.warn("查到多组设备，请输入更多位设备序列号进行查询，serchType={}，searchInfo={}",	new Object[] { checker.getSearchType(), checker.getSearchInfo() });
				checker.setRstCode("1000");
				checker.setRstMsg("查到多组设备，请输入更多位设备序列号进行查询");
				if("xj_dx".equals(Global.G_instArea)){
					new RecordLogDAO().recordLog(checker.getSearchInfo(), inParam, "",
							checker.getReturnXml(), 1);
				}
				return checker.getReturnXml();
			}
			
			deviceId = userMapList.get(0).get("device_id");

			if (null==deviceId || deviceId.trim().length()==0){
				logger.warn("IPTV账号没有绑定机顶盒，请将机顶盒零配");
				checker.setRstCode("1000");
				checker.setRstMsg("IPTV账号没有绑定机顶盒，请将机顶盒零配");
				if("xj_dx".equals(Global.G_instArea)){
					new RecordLogDAO().recordLog(checker.getSearchInfo(), inParam, "",
							checker.getReturnXml(), 1);
				}
				return checker.getReturnXml();
			}
			
			//上报机顶盒获取的IP的地址是非10段地址
			String ip = userMapList.get(0).get("loopback_ip");
			if(ip.isEmpty() || !ip.startsWith("10")){
				checker.setRstCode("1000");
				checker.setRstMsg("设备ip["+ ip +"]非10段地址");
				if("xj_dx".equals(Global.G_instArea)){
					new RecordLogDAO().recordLog(checker.getSearchInfo(), inParam, "",
							checker.getReturnXml(), 1);
				}
				return checker.getReturnXml();
			}
			
			checker.setRstCode("0");
			checker.setRstMsg(ip);
			logger.warn("GetStbIpService==>returnXML:" + checker.getReturnXml());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if("xj_dx".equals(Global.G_instArea)){
			new RecordLogDAO().recordLog(checker.getSearchInfo(), inParam, "",
					checker.getReturnXml(), 1);
		}
		return checker.getReturnXml();
	}
}
