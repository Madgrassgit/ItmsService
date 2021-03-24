package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryNormalVersionChecker;
		
public class QueryNormalVersionService implements IService
{

	private static final Logger logger = LoggerFactory.getLogger(QueryNormalVersionService.class);
	 private UserDeviceDAO userDevDao = new UserDeviceDAO();
	@Override
	public String work(String inXml)
	{
		QueryNormalVersionChecker checker = new QueryNormalVersionChecker(inXml);
		
		if (!checker.check())
		{
			logger.error(
					"servicename[QueryNormalVersionService]cmdId[{}]vender[{}]model[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getVendor(),checker.getModel(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		//查厂家
		Map<String,String> vendorMap = userDevDao.getVendor(checker.getVendor());
		if(null==vendorMap){
			checker.setRstCode("1002");
			checker.setRstMsg("查无此厂家");
			return checker.getReturnXml();
		}
		String vendorId=StringUtil.getStringValue(vendorMap, "vendor_id");
		//查型号
		Map<String,String> modelMap = userDevDao.getDevModel(checker.getModel());
		if(null==modelMap){
			checker.setRstCode("1003");
			checker.setRstMsg("查无此型号");
			return checker.getReturnXml();
		}
		String deviceModelId=StringUtil.getStringValue(modelMap, "device_model_id");
		//查版本信息
		List<HashMap<String,String>> versionList=userDevDao.queryVersion(vendorId,deviceModelId);
		Map<String,HashMap<String,String>> versionMap=new HashMap<String,HashMap<String,String>>();
		if(null!=versionList&&versionList.size()>0){
			for(HashMap<String,String> map:versionList){
				versionMap.put(StringUtil.getStringValue(map, "devicetype_id"), map);
			}
		}
		else{
			checker.setRstCode("1004");
			checker.setRstMsg("此厂家及型号下无规范版本");
			return checker.getReturnXml();
		}
		//查语音协议
		List<HashMap<String,String>> protocolList=userDevDao.queryPotocol(vendorId, deviceModelId);
		if(null!=versionList&&versionList.size()>0){
			Map<String,String> protocolMap=new HashMap<String,String>();
			for(HashMap<String,String> map:protocolList){
				String deviceTypeId=StringUtil.getStringValue(map, "devicetype_id");
				String protocol=StringUtil.getStringValue(map, "server_type");
				if(protocolMap.containsKey(deviceTypeId)){
					String combinedProtocol=StringUtil.getStringValue(protocolMap, deviceTypeId)+"|"+protocol;
					protocolMap.put(deviceTypeId, combinedProtocol);
				}
				else{
					protocolMap.put(deviceTypeId, protocol);
				}
			}
			//遍历list把处理好后的语音协议set过来
			for(String key:protocolMap.keySet()){
				if(versionMap.containsKey(key)){
					versionMap.get(key).put("voipProtocol", protocolMap.get(key));
				}
			}
			//map转化为list
			checker.setSheeInfoList(new ArrayList<HashMap<String,String>>(versionMap.values()));
		}
		return checker.getReturnXml();
	}
}

	