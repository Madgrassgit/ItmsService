
package com.linkage.itms.hlj.dispatch.obj;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;


/**
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-7-25
 * @category com.linkage.itms.hlj.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class BindInfoChecker extends HljBaseChecker
{

	private static final Logger logger = LoggerFactory.getLogger(BindInfoChecker.class);
	private String inParam = null;

	public BindInfoChecker(String inParam)
	{
		this.inParam = inParam;
	}
	
	@Override
	public boolean check()
	{

		logger.debug("BindInfoChecker==>check()"+inParam);
		
		try
		{
			JSONObject jo = new JSONObject(inParam);
			String JsonStr = jo.getString("input");
			infcode = jo.getString("infcode");
			
			JSONObject jo2 = new JSONObject(JsonStr);
			QueryNum = jo2.getString("QueryNum");
			QueryType = Integer.parseInt(jo2.getString("QueryType"));
			StreamingNum = jo2.getString("StreamingNum");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (!"1".equals(infcode)){
			result = 8;
			resultDesc = "接口代码与方法名不一致";
			return false;
		}
		if (2 == QueryType ){
			if(QueryNum.length()<6){
				result = 8;
				resultDesc = "SN至少输入6位数";
				return false;
			}
		}
		// 参数合法性检查
		if (StringUtil.IsEmpty(QueryNum) || (1 != QueryType && 2 != QueryType && 0 != QueryType )
				|| StringUtil.IsEmpty(StreamingNum))
		{
			result = 8;
			return false;
		}
		return true;
	
	}

	@Override
	public String getReturnXml()
	{

		JSONObject jo = new JSONObject();
		try
		{
			jo.put("resultCode", result);
			jo.put("streamingNum", StreamingNum);
			jo.put("queryRspInfo", "");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return jo.toString();
	
	}
}
