package com.linkage.itms.itv.bio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.itv.dao.GetItvAccountDAO;
import com.linkage.itms.itv.obj.AccountOBJ;
import com.linkage.itms.itv.obj.GetItvAccountOBJ;

/**
 * @author zhangsm
 * @version 1.0
 * @since 2011-9-23 上午09:30:05
 * @category com.linkage.itms.itv.bio<br>
 * @copyright 亚信联创 网管产品部
 */
public class GetItvAccountBIO
{
	private static Logger logger = LoggerFactory.getLogger(GetItvAccountBIO.class);
	
	public String getItvAccount(String inParam)
	{
		GetItvAccountOBJ obj = new GetItvAccountOBJ();
		obj.setCallXml(inParam);
		GetItvAccountDAO dao = new GetItvAccountDAO();
		if(false == obj.check())
		{
			logger.warn("getItvAccount-[{}]-验证未通过，返回-[{}]",obj.getStbMac(),obj.getReturnXml());
			return obj.getReturnXml();
		}
		//根据机顶盒mac获取家庭网关
		List<HashMap<String,String>> devIds = dao.getDevByStbMac(obj.getStbMac());
		if(null == devIds || devIds.isEmpty() || null == devIds.get(0) || devIds.get(0).isEmpty())
		{
			logger.warn("getItvAccount-[{}]-机顶盒Mac地址对应的家庭网关不存在",obj.getStbMac());
			obj.setResult(1002);
			obj.setResultDesc("机顶盒Mac地址对应的家庭网关不存在");
		}
		else if(devIds.size() > 1)
		{
			logger.warn("getItvAccount-[{}]-Mac对应多个家庭网关",obj.getStbMac());
			obj.setResult(1005);
			obj.setResultDesc("Mac对应多个家庭网关");
		}
		else
		{
			String devId = devIds.get(0).get("device_id");
			String lan_port = StringUtil.getStringValue(devIds.get(0).get("lan_port"));
			obj.setLan_port(lan_port);
			//根据deviceid获取对应的itv账号
			List<HashMap<String,String>> accounts = dao.getAccountByDevId(devId);
			if(null == accounts || accounts.isEmpty())
			{
				logger.warn("getItvAccount-[{}]-Mac地址对应的家庭网关未绑定",obj.getStbMac());
				obj.setResult(1004);
				obj.setResultDesc("Mac地址对应的家庭网关未绑定");
			}
			else
			{
				AccountOBJ accountOBJ = null;
				List<AccountOBJ> accoutObjs = new ArrayList<AccountOBJ>();
				
				for(Map<String,String> map : accounts)
				{
					accountOBJ = new AccountOBJ();
					accountOBJ.setUserName(map.get("username"));
					if("xj_dx".equals(Global.G_instArea) || "jl_dx".equals(Global.G_instArea)){
						accountOBJ.setIptvRealBindPort(StringUtil.getStringValue(map, "real_bind_port"));
					}
					accoutObjs.add(accountOBJ);
				}
				obj.setAcccounts(accoutObjs);
			}
		}
		String retXML = obj.getReturnXml();
		logger.warn("getItvAccount-[{}]-回参-[{}]", obj.getStbMac(), retXML);
		
		//记录日志
		new RecordLogDAO().recordLog(obj.getCmdId(), obj.getClientType(), "getItvAccount",
				null, null, null, obj.getResult(),
				inParam, retXML, System.currentTimeMillis()/1000);
		
		return retXML;
	}
}
