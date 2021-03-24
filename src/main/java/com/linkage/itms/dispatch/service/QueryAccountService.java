package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryAccountChecker;

/**
 * 江苏电信ITMS与宗调业务帐号查询接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2016年12月06日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class QueryAccountService implements IService{

	private static Logger logger = LoggerFactory.getLogger(QueryAccountService.class);
	
	@Override
	public String work(String inParam) {
		logger.warn("QueryAccountService==>inParam({})",inParam);

		// 解析获得入参
		QueryAccountChecker checker = new QueryAccountChecker(inParam);
		
		// 验证入参
		if (false == checker.check()) {
			logger.warn("入参验证没通过,SearchType=[{}],SearchType[{}],DevSN=[{}]",
					new Object[] { checker.getCmdId(),checker.getSearchType(), checker.getDevSn()});
			
			return checker.getReturnXml();
		}
		
		// 查询设备数目
		UserDeviceDAO userDevDao = new UserDeviceDAO();
		ArrayList<HashMap<String, String>> devNames = userDevDao.qryDevNameByDevSN(checker.getDevSn());
		
		if (null == devNames || devNames.size() == 0) {
			logger.warn(
					"servicename[QueryAccountService]CmdID[{}]SearchType[{}]DevSN=[{}]查无此设备",
					new Object[] { checker.getCmdId(), checker.getSearchType(), checker.getDevSn()});
			checker.setResult(1004);
			checker.setResultDesc("查无此设备");
		}
		else if(devNames.size() > 1) {
			StringBuffer sb = new StringBuffer(0);
			HashMap<String, String> map = new HashMap<String, String>();
			for(int i = 0;i<devNames.size();i++){
				map = devNames.get(i);
				sb.append(map.get("device_name"));
				if(i<devNames.size()-1){
					sb.append(";");
				}
			}
			
			logger.warn(
					"servicename[QueryAccountService]CmdID[{}]SearchType[{}]DevSN=[{}]设备序列号后六位查询多台设备，请输入全部十七位设备序列号",
					new Object[] { checker.getCmdId(), checker.getSearchType(), checker.getDevSn()});
			checker.setResult(1006);
			checker.setResultDesc(sb.toString());
		} 
		else{
			//初步查询宽带 iptv 语音业务账号密码
			ArrayList<HashMap<String,String>> list = userDevDao.qryServByDevSN(checker.getDevSn());
			
			// 未查询到任何相关业务信息
			if (null == list || list.size()==0) {
				logger.warn(
						"servicename[QueryAccountService]CmdID[{}]SearchType[{}]DevSN[{}]未查询到相关业务信息",
						new Object[] { checker.getCmdId(),checker.getSearchType(), checker.getDevSn()});
				checker.setResult(1003);
				checker.setResultDesc("设备未绑定");
			}
			else {
				String serv_type_id;
				String username;
				String passwd;
				boolean hasAuth = false; //是否有语音业务
				String userID = list.get(0).get("user_id");
				
				for(int i=0;i<list.size();i++){
					HashMap<String,String> map = list.get(i);
					serv_type_id = map.get("serv_type_id");
					username = map.get("username");
					passwd = map.get("passwd");
					if("10".equals(serv_type_id)){
						checker.setUsername(checker.getUsername()+username+";");
						checker.setPassword(checker.getPassword()+passwd+";");
					}
					else if("11".equals(serv_type_id)){
						checker.setItvUsername(checker.getItvUsername()+username+";");
					}
					else if("14".equals(serv_type_id)){
						hasAuth = true;
					}
				}
				
				//存在语音业务，单独查询语音
				if(hasAuth){
					ArrayList<HashMap<String,String>> authList = userDevDao.qryAuthByUserID(userID);
					
					// 未查询到语音业务信息(tab_voip_serv_param)
					if (null == authList || authList.size()==0) {
						logger.warn(
								"servicename[QueryAccountService]CmdID[{}]SearchType[{}]DevSN[{}]未查询到相关业务信息",
								new Object[] { checker.getCmdId(), checker.getSearchType(),checker.getDevSn()});
					}
					else {
						String voip_username = "";
						String voip_passwd = "";
						for(int j=0;j<authList.size();j++){
							HashMap<String,String> map = authList.get(j);
							voip_username = map.get("voip_username");
							voip_passwd = map.get("voip_passwd");
							//最后一个不加分隔符
							String endstr = ";";
							if(j == authList.size()-1){
								endstr = "";
							}
							
							checker.setAuthUserName(checker.getAuthUserName()+voip_username+endstr);
							checker.setAuthPassword(checker.getAuthPassword()+voip_passwd+endstr);
						}
					}
				}
			}
		}
		
		String returnXml = checker.getReturnXml();
		logger.warn("servicename[QueryAccountService]处理结束，返回响应信息:{}",returnXml);
	
		return returnXml;
		
	}

	
	

}
